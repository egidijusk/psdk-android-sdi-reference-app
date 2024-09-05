/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.sdi.card

import android.util.Log
import com.priv.verifone.psdk.sdiapplication.PSDKContext
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.dateToString
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.getCurrentDateTime
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.hexStringToByteArray
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.toHexString
import com.priv.verifone.psdk.sdiapplication.utils.Constants
import com.verifone.payment_sdk.*
import java.util.*

// This is responsible for processing EMV contactless transaction
class SdiContactless(private val sdiManager: SdiManager) :
    SdiCard(sdiManager = sdiManager) {

    private val notifyCallback: NotifyCallback = NotifyCallback()
    private val config = PSDKContext.ctlsConfigData
    private val emvCtlsCallback: EMVCTLSCallback = EMVCTLSCallback()
    init {
        // Search for 9E-01 in SDI Docs
        sdiManager.setNotifyCallback(notifyCallback)
    }

    enum class LED {
        ONE,
        TWO,
        THREE,
        FOUR
    }

    companion object {
        private const val TAG = "SdiCardCTLS"
    }

    override fun initialize(): SdiResultCode {
        super.initialize()
        sdiManager.setEmvCallback(emvCtlsCallback)
        Log.d(TAG, "Ctls Init Framework Command (40-00)")
        val initOptions = SdiEmvOptions.create()
        initOptions.setOption(SdiEmvOption.TRACE, true)
        //initOptions.setOption(SdiEmvOption.CONFIG_MODE, true)
        initOptions.setOption(SdiEmvOption.TRACE_ADK_LOG, true)
        initOptions.setOption(SdiEmvOption.LED_CBK_EXT, true)
        initOptions.setOption(SdiEmvOption.AUTO_RETAP, true)
        initOptions.setOption(SdiEmvOption.BEEP_CBK_EXT, true)
        val result = sdiManager.emvCtls?.initFramework(60, initOptions)

        Log.d(TAG, "Command result: ${result?.name}")

        return result!!
    }


    fun exit(): SdiResultCode {
        sdiManager.setEmvCallback(null)
        Log.d(TAG, "Ctls End Transaction Command  (40-15)")
        sdiManager.emvCtls.endTransaction(null)
        Log.d(TAG, " Ctls Exit Framework Command  (40-00)")
        val result = sdiManager.emvCtls?.exitFramework(null)
        Log.d(TAG, "Command result: ${result?.name}")
        return result!!
    }

    override fun startTransactionFlow(amount: Long): SdiResultCode {

        val response = continueOffline()

        when (response.result) {
            SdiResultCode.EMVSTATUS_ARQC -> {
                // Fetch transaction data and send online request to Host for approval
                retrieveTags(response.txn)
                retrieveTagsUsingApi(config.fetchTags)
                crypto.getSensitiveEncryptedData(config.sensitiveTags)

                Log.d(TAG, "Transaction Approved")
                uiListener.display("Transaction Approved")
            }

            SdiResultCode.EMVSTATUS_AAC -> {
                Log.d(TAG, "Offline Decline")
                uiListener.display("Offline Decline")
            }

            SdiResultCode.EMVSTATUS_TC -> {
                Log.d(TAG, "Offline Approved")
                uiListener.display("Offline Approved")
            }

            SdiResultCode.EMVSTATUS_ABORT -> {
                Log.d(TAG, "Transaction Aborted")
                uiListener.display("Transaction Aborted")
            }

            else -> {
                Log.d(TAG, "First GEN AC response not handled: ${response.result.name}")
                uiListener.display("First GEN AC response not handled: ${response.result.name}")
                Log.d(TAG, "Transaction completed")
            }
        }

        uiListener.waitForCardRemoval()
        sdiManager.smartCardCtls.smartPowerOff(EnumSet.of(SdiEmvCtlsReaderOptions.DETECT_REMOVAL))
        var result: SdiResultCode?
        do {
            result = sdiManager.smartCardCtls.cardRemoval(0)
        } while (result != SdiResultCode.OK)
        Log.i(TAG, "Card Removed")
        return response.result
    }

    fun setupTransaction(amount: Long): SdiEmvTxnResponse {
        // EMV CTLS setup Transaction (40-10)
        Log.i(TAG, "Setup Transaction Command (40-10)")

        val ctlsTxnConfig = SdiEmvTxn.create()

        val today = getCurrentDateTime()
        val date = today.dateToString("yyMMdd").hexStringToByteArray()
        val time = today.dateToString("hhmmss").hexStringToByteArray()
        val txnOptions = SdiEmvTransactionOptions.create()
        txnOptions.setCtlsOption(SdiEmvCtlsTransactionOption.EMV_CTLS_TXNOP_CANDLIST_CALLBACK, true)
        ctlsTxnConfig.setTransactionOptions(txnOptions)
        /*
        // Disable AID

        ctlsTxnConfig.setExcludeAID(ArrayList<ByteArray>().apply {
            add("A0000000032020".hexStringToByteArray())
        }
        )
        */
        val sdiEmvTxnResponse = sdiManager.emvCtls.setupTransaction(
            SdiEmvTransaction.GOODS_SERVICE,
            amount, date, time, txnCounter.toLong(), ctlsTxnConfig
        )

        Log.i(TAG, "Command Result: ${sdiEmvTxnResponse.result.name}")
        if (sdiEmvTxnResponse.result != SdiResultCode.OK) {
            Log.e(TAG, "Failed to setup transaction")
        }

        return sdiEmvTxnResponse
    }

    private fun continueOffline(): SdiEmvTxnResponse {
        val ctlsTxnConfig = SdiEmvTxn.create()
        Log.i(TAG, "EMV CTLS Continue Offline Command (40-11) ")
        val response: SdiEmvTxnResponse? = sdiManager.emvCtls.continueOffline(null)
        Log.i(TAG, "Command Result: ${response?.result?.name}")
        /* DF42 for CVM Processing Logic
            0x00000001u  ///< this means Online PIN CVM was performed during the transaction (CT: already done in a callback by the app, CTLS: to be performed by the app once the card is out of the field) --> Online PIN processing (host encryption) is required if the transaction is not declined or if there is no fallback.
            0x00000002u  ///< this means signature CVM was performed and the signature line must be printed on the receipt if the transaction is not declined or if there is no fallback.
            0x00000004u  ///< Forced acceptance transaction
            0x00000008u  ///< user defined CVM, this means that a custom CVM was performed and according to custom/domestic rules additional steps may apply
            0x00000010u  ///< An On-Device CVM was performed (Amex: "Mobile CVM", Visa: "Consumer device CVM"). Caution, obsolete: For VFI-Reader it indicates if CVM has been performed on the consumer device even if transaction flow CVM was no CVM (e.g. because below CVM-Limit). But for VERTEX this bit is set if CD-CVM is indicated in the kernel outcome with exception for Amex. Replaced by #EMV_ADK_SI_CONSUMER_DEVICE and #EMV_ADK_SI_OUTCOME_CD_CVM with a consistent behaviour.
            0x00000080u  ///< Result is EMV_ADK_ABORT because customer has pulled out the card
            0x00000100u  ///< A tip transaction may follow this payment
            0x00000200u  ///< PIN try counter exceeded
            0x00000400u  ///< Last entered offline PIN was wrong
            0x00000800u  ///< A contactless chip transaction
            0x00001000u  ///< A contactless magstripe txn
            0x00002000u  ///< A torn transaction was created (PP3) @n not used for contact
            0x00004000u  ///< A torn transaction was tried to recover @n not used for contact
            0x00008000u  ///< The contactless transaction is still in the card waiting phase
            0x00008000u  ///< The contact transaction is still ongoing
            0x00010000u  ///< Contactless "long tap" \if is_ctls_doc , only appears if transaction goes online, card has to remain in field until EMV_CTLS_ContinueOnline() returned \endif
            0x00020000u  ///< Contactless kernel asks for second activation after online request in any case even in case of online problem (e.g. girocard OUT.ORD=ANY)
            0x00040000u  ///< Contactless kernel set OUT.ORD to EMV&OTGO and conditions for offline approval are valid. Retap required if unable to go online or script/auth data available.
            0x00080000u  ///< Contactless transaction performed by a consumer device (smart phone, tablet, watch) supporting CD-CVM (Consumer Device Cardholder Verification Method).
            0x00100000u  ///< Contactless device, key fob or mini card without alternate interfaces. Note: This bit is derived from various card properties like "switch interface" or form factor indicators and is not reliable for all schemes . Don't use this information for control flow. It is intended as additional information or for statistics.
            0x00200000u  ///< Contactless set if kernel Outcome Parameter CVM = Confirmation Code Verified (Consumer Device CVM successfully performed, see Book A)
            0x00400000u  ///< Contactless set if kernel Outcome Parameter Receipt = Yes (provide a receipt, see Book A). Note: For instance only supported for MasterCard, girocard, JCB and RuPay.
            0x00800000u  ///< Contact Write data storage has failed
            0x01000000u  ///< Do not prompt for re-tap, no matter if there are issuer authentication data or scripts. This is a requirement for JCB online outcome neither 2 presentments nor present and hold and other kernels (e.g. Gemalto-PURE). It is also set for kernels that do not process issuer update at all (e.g. MasterCard, Visa, Amex, CUP, ...) and contactless mag. stripe transactions
            0x02000000u  ///< Only prompt for re-tap in case issuer scripts are present, that is discard Issuer Authentication Data (IAuD, tag '91'). This flag is set for Discover and WISE transactions.
        */
        return response!!
    }

    // This callback will get the Led update notifications and the display notification during retap
    private inner class NotifyCallback : SdiNotifyCallback() {
        override fun notifyCallback(data: SdiTlv) {

            Log.d(TAG, "Notify Callback")
            val messageTag = 0xF0
            val ctlsLedTag = 0xBF10
            val ctlsDisplay = 0xBF14
            if (data.obtain(messageTag).count(ctlsLedTag) > 0) {
                Log.d(TAG, "Notify Callback: Led")
                handleLedDisplay(data)
            }
            if (data.obtain(messageTag).count(ctlsDisplay) > 0) {
                handleDisplay(data)
                Log.d(TAG, "Notify Callback: Display")
            }
        }

        private fun handleLedDisplay(data: SdiTlv) {
            val messageTag = 0xF0
            val ctlsLedTag = 0xBF10
            val ctlsLedState = 0xC8
            if (data.obtain(messageTag).count(ctlsLedTag) > 0) {
                val ledsToActivate: Int =
                    data.obtain(messageTag).obtain(ctlsLedTag).obtain(ctlsLedState)
                        .number

                if (ledsToActivate and 0x08 == 0x08) {
                    // last led
                    uiListener.activateLed(LED.FOUR, true)
                } else {
                    uiListener.activateLed(LED.FOUR, activate = false)
                }
                if (ledsToActivate and 0x04 == 0x04) {
                    // 3rd led
                    uiListener.activateLed(LED.THREE, true)
                } else {
                    uiListener.activateLed(LED.THREE, false)
                }
                if (ledsToActivate and 0x02 == 0x02) {
                    // 2nd led
                    uiListener.activateLed(LED.TWO, true)
                } else {
                    uiListener.activateLed(LED.TWO, false)
                }
                if (ledsToActivate and 0x01 == 0x01) {
                    uiListener.activateLed(LED.ONE, true)
                } else {
                    uiListener.activateLed(LED.ONE, false)
                }
            }
        }

        private fun handleDisplay(data: SdiTlv) {
            val messageTag = 0xF0
            val ctlsDisplay = 0xBF14
            val displayTextId = 0xDF8F12 // Text Id
            if (data.obtain(messageTag).count(ctlsDisplay) > 0) {
                val text: Int =
                    data.obtain(messageTag).obtain(ctlsDisplay).obtain(displayTextId).number
                Log.d(TAG, "Display Text: ${text.toString(16)}")
                // if 0x14
                uiListener.display("Please see phone")
                //if 0x15 // probably for MC torn transaction test case
                //listener.display("Re-tap")
                // EMV_ADK_TXT_SEE_PHONE           0x14
                // EMV_ADK_TXT_RETAP_SAME          0x15
                // EMV_ADK_TXT_RETAP_SAME_L1       0x16
                // EMV_ADK_TXT_2_CARDS_IN_FIELD    0x17
                // EMV_ADK_TXT_CARD_READ_COMPLETE  0x18
            }
        }
    }

    private inner class EMVCTLSCallback : SdiEmvCallback() {
        @ExperimentalStdlibApi
        override fun emvCallback(type: SdiEmvCallbackType, input: SdiEmvTxn?, output: SdiEmvTxn?) {
            Log.d(TAG, "EMV CTLS Callback ${type.name}")
            when (type) {
                SdiEmvCallbackType.MODIFY_CANDIDATES -> {
                    val candidateList = input?.cbCandidateList
                    // Send list to UI for selection
                    if (candidateList != null) {
                        for (app in candidateList) {
                            Log.d(TAG, "Application Name: ${app.aid.toHexString()}")
                            Log.d(TAG, "Application Label: ${app.appName}")
                        }
                    }
                    // Implement UI for application selection
                    val selectedCandidate =
                        "01".hexStringToByteArray() // index + 1 , this will select the 1st candidate
                    output?.reducedCanditateList = selectedCandidate
                }

                else -> {

                }
            }
        }
    }
}