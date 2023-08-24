package com.verifone.psdk.sdiapplication.sdi.card

import android.util.Log
import com.verifone.psdk.sdiapplication.sdi.config.Config
import com.verifone.psdk.sdiapplication.sdi.transaction.TransactionListener
import com.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.dateToString
import com.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.getCurrentDateTime
import com.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.hexStringToByteArray
import com.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.toHexString
import com.verifone.payment_sdk.*

abstract class SdiContact(private val sdiManager: SdiManager, private val config: Config)
    : SdiCard(sdiManager= sdiManager, config = config) {

    companion object {
        private const val TAG = "SdiCardCT"
    }

    internal abstract fun startTransaction(amount: Long): SdiEmvTxnResponse

    internal abstract fun continueOffline(): SdiEmvTxnResponse


    override fun initialize():SdiResultCode {
        super.initialize()
        Log.d(TAG, "Init CT Framework Command (39 00) ")
        val initOptions = SdiEmvOptions.create()
        initOptions.setOption(SdiEmvOption.TRACE, true)
        //initOptions.setOption(SdiEmvOption.CONFIG_MODE, true)
        initOptions.setOption(SdiEmvOption.TRACE_ADK_LOG, true)
        initOptions.setOption(SdiEmvOption.L1_DUMP, true)
        val result = sdiManager.emvCt?.initFramework(60, initOptions)
        Log.d(TAG, "Command result: ${result?.name}")
        return result!!
    }

    fun exit() : SdiResultCode {
        Log.d(TAG, "End Transaction Command (39-15)")
        sdiManager.emvCt.endTransaction(0)
        Log.d(TAG, "Exit CT Framework Command (39 00)")
        val result = sdiManager.emvCt?.exitFramework(null)
        Log.d(TAG, "Command result: ${result?.name}")
        return result!!
    }

    private fun cardActivate(): SdiBinaryResponse {
        // Smart Card Activate Command (41-02)
        Log.d(TAG, "Smart Card Activate(41-02)")
        val smartCardResponse: SdiBinaryResponse = sdiManager.smartCardCt.activate()

        Log.i(TAG, "result: ${smartCardResponse.result.name} " +
                    "response:${smartCardResponse.response.toHexString()} ")

        return smartCardResponse
    }

    override fun startTransactionFlow(amount: Long): SdiResultCode {

        val activateResp = cardActivate()
        if (activateResp.result != SdiResultCode.OK) {
            listener.display("Failed to Activate smartCard reader: ${activateResp.result.name}")
            return activateResp.result
        }
        // Card Activated, start transaction sequence
        val startTxnResp = startTransaction(amount)
        if (startTxnResp.result == SdiResultCode.OK) {
            val response = continueOffline()
            Log.d(TAG, "First GEN AC response: ${response.result.name}")
            when (response.result) {
                SdiResultCode.EMVSTATUS_ARQC -> {
                    fetchEncryptedData(config.getCtSensitiveTagsToFetch())
                    // Go to Host for approval
                    // change host response code to decimal value of Amount entry
                    val genAcResponse = continueOnline(true, byteArrayOf(0x30, 0x30))
                    if (genAcResponse.result == SdiResultCode.EMVSTATUS_TC) {
                        listener.display("Transaction Approved")
                        Log.d(TAG, "Transaction Approved")
                    } else {
                        Log.d(TAG, "Transaction Declined Offline")
                        listener.display("Transaction Offline")
                    }
                }
                SdiResultCode.EMVSTATUS_AAC -> {
                    Log.d(TAG, "Offline Decline")
                    listener.display("Offline Decline")

                }
                SdiResultCode.EMVSTATUS_TC -> {
                    Log.d(TAG, "Offline Approved")
                    listener.display("Offline Approved")
                }
                SdiResultCode.EMVSTATUS_ABORT-> {
                    Log.d(TAG, "Transaction Aborted")
                    listener.display("Transaction Aborted")
                }
                else -> {
                    Log.d(TAG, "First GEN AC response not handled: ${response.result.name}")
                    listener.display("First GEN AC response not handled: ${response.result.name}")
                    Log.d(TAG, "Transaction completed")
                }
            }
        }
        return SdiResultCode.OK
    }

    internal fun continueOnline(onlineResult: Boolean, resp: ByteArray): SdiEmvTxnResponse {
        Log.d(TAG, "EMV CT Continue Online Command (39-12)")
        val sdiEmvTxn = SdiEmvTxn.create()
        // TAG 91 sdiEmvTxn.authData
        // TAG 71 sdiEmvTxn.criticalScript
        // TAG 72 sdiEmvTxn.nonCriticalScript
        // 2st GEn AC
        // resp 0x3030 - approved
        // resp 0x3035 - decline
        val result = sdiManager.emvCt.continueOnline(onlineResult, resp, sdiEmvTxn)

        Log.d(TAG, "Command Result: ${result.result.name}")

        retrieveTags(result.txn)

        retrieveTagsUsingApi(config.getCtTagsToFetch())
        return result
    }

    @ExperimentalStdlibApi
    internal fun validateOfflinePin() {
        Log.d(TAG, "Send Offline PIN Command (41-05)")
        val offlinePinResponse = sdiManager.smartCardCt.sendOfflinePIN()
        Log.d(TAG, "Command Response: ${offlinePinResponse.response.toString(16)}")
        Log.d(TAG, "Command Result: ${offlinePinResponse.result}")
        val result = offlinePinResponse.response.toString(16)

        if (result != "9000" && result.length == 4) {
            val attempts = result[3]
            listener.display("Invalid PIN, please re-try. Attempts left: ${attempts.digitToInt(16)}")
        }
    }
}