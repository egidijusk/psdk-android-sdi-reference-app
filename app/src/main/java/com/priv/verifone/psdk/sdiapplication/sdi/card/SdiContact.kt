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
import com.priv.verifone.psdk.sdiapplication.sdi.config.Config
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.toHexString
import com.verifone.payment_sdk.*

/*
 * This is responsible for handling EMV contact transaction apis
 * Here this reference app divides emv chip processing in 2 ways i.e. basic and re-entrance mode
 * Any one of them can be used for processing chip transaction based on the user requirement
 * This class uses SdiContactBasic or SdiContactAdvanced internally based on the selected mode for transaction processing
 */
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
        initOptions.setOption(SdiEmvOption.TRACE_ADK_LOG, true)
        val result = sdiManager.emvCt?.initFramework(60, initOptions)
        Log.d(TAG, "Command result: ${result?.name}")
        return result!!
    }
    fun waitForCardRemoval() {
        // Wait for card removal
        Log.d(TAG, "Command waitForRemoval ")
        val result = sdiManager.cardDetect.waitForRemoval(30)
        Log.d(TAG, "Command Result: ${result.name}")
    }

    fun exit() : SdiResultCode {
        Log.d(TAG, "End Transaction Command (39-15)")
        sdiManager.emvCt.endTransaction(0)
        Log.d(TAG, "Exit CT Framework Command (39 00)")
        val result = sdiManager.emvCt?.exitFramework(null)
        Log.d(TAG, "Command result: ${result?.name}")
        return result!!
    }

    // Activates the chip card here
    private fun cardActivate(): SdiBinaryResponse {
        // Smart Card Activate Command (41-02)
        Log.d(TAG, "Smart Card Activate Command (41-02)")
        val smartCardResponse: SdiBinaryResponse = sdiManager.smartCardCt.activate()

        Log.i(TAG, "Command result: ${smartCardResponse.result.name} " +
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
        waitForCardRemoval()
        return SdiResultCode.OK
    }

    // This api should be used after host response received.
    // Host response can be provided to card through this 2nd gen ac command
    internal fun continueOnline(onlineResult: Boolean, resp: ByteArray): SdiEmvTxnResponse {
        Log.d(TAG, "EMV CT Continue Online Command (39-12)")
        val sdiEmvTxn = SdiEmvTxn.create()

        // TAG 91 sdiEmvTxn.authData
        // TAG 71 sdiEmvTxn.criticalScript
        // TAG 72 sdiEmvTxn.nonCriticalScript
        // 2nd Gen AC
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
            val attempts = result[3] // Remaining attempt for offline pin entry
            listener.display("Invalid PIN, please re-try. Attempts left: ${attempts.digitToInt(16)}")
        }
    }
}