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
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.toHexString
import com.priv.verifone.psdk.sdiapplication.utils.Constants
import com.verifone.payment_sdk.*
import java.util.EnumSet

/*
 * This is responsible for handling EMV contact transaction apis
 * Here this reference app divides emv chip processing in 2 ways i.e. basic and re-entrance mode
 * Any one of them can be used for processing chip transaction based on the user requirement
 * This class uses SdiContactBasic or SdiContactAdvanced internally based on the selected mode for transaction processing
 */
abstract class SdiContact(private val sdiManager: SdiManager)
    : SdiCard(sdiManager= sdiManager) {

    companion object {
        private const val TAG = "SdiCardCT"
    }
    val configData = PSDKContext.ctConfigData

    internal abstract fun startTransaction(amount: Long): SdiEmvTxnResponse

    internal abstract fun continueOffline(): SdiEmvTxnResponse

    override fun initialize():SdiResultCode {
        super.initialize()
        Log.d(TAG, "Init CT Framework Command (39 00) ")
        val initOptions = SdiEmvOptions.create()
        initOptions.setOption(SdiEmvOption.TRACE, true)
        initOptions.setOption(SdiEmvOption.TRACE_ADK_LOG, true)
        //initOptions.setOption(SdiEmvOption.VIRT_1, true)
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
            uiListener.display("Failed to Activate smartCard reader: ${activateResp.result.name}")
            return activateResp.result
        }
        // Card Activated, start transaction sequence
        val startTxnResp = startTransaction(amount) // exceute

        if (startTxnResp.result == SdiResultCode.OK) {
            val response = continueOffline()
            Log.d(TAG, "First GEN AC response: ${response.result.name}")
            when (response.result) {
                SdiResultCode.EMVSTATUS_ARQC -> {
                    // Fetch transaction data and send online request to Host for approval
                    crypto.getSensitiveEncryptedData(configData.sensitiveTags)
                    // SCA - RETRY ONLINE PIN
                    /*
                        uiListener.setSensitiveDataGreenButtonText(Constants.CONFIRM)
                        // Interaction with UI elements
                        uiListener.sensitiveDataEntryTitle("Re Enter Pin")
                        // Interaction with UI elements
                        uiListener.showSensitiveDataEntry() // showing PIN entry
                        val pinResult = getPinUsingCallback()
                        Log.d(TAG, "PIN Entry Result: ${pinResult.name}")
                        if (pinResult == SdiResultCode.ERR_PED_BYPASS) {
                            Log.d(TAG, "PIN Entry Result: ${pinResult.name}")
                        } else if (pinResult == SdiResultCode.ERR_CANCELLED || pinResult == SdiResultCode.ERR_PED_TIMEOUT) {
                            Log.d(TAG, "PIN Entry Result: ${pinResult.name}")
                        }
                        crypto.getEncryptedPinBlock()
                    */
                    // Change host response code to decimal value of Amount entry
                    // Map the host response for below api call to execute 2nd GenC execution
                    val genAcResponse = continueOnline(true, byteArrayOf(0x30, 0x30))
                    if (genAcResponse.result == SdiResultCode.EMVSTATUS_TC) {
                        uiListener.display("Transaction Approved")
                        Log.d(TAG, "Transaction Approved")
                    } else {
                        Log.d(TAG, "Transaction Declined Offline")
                        uiListener.display("Transaction Offline")
                    }
                }
                SdiResultCode.EMVSTATUS_AAC -> {
                    Log.d(TAG, "Offline Decline")
                    uiListener.display("Offline Decline")
                }
                SdiResultCode.EMVSTATUS_TC -> {
                    Log.d(TAG, "Offline Approved")
                    uiListener.display("Offline Approved")
                }
                SdiResultCode.EMVSTATUS_ABORT-> {
                    Log.d(TAG, "Transaction Aborted")
                    uiListener.display("Transaction Aborted")
                }
                else -> {
                    Log.d(TAG, "First GEN AC response not handled: ${response.result.name}")
                    uiListener.display("First GEN AC response not handled: ${response.result.name}")
                    Log.d(TAG, "Transaction completed")
                }
            }
        }
        uiListener.waitForCardRemoval()
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
        retrieveTagsUsingApi(configData.fetchTags)
        return result
    }

    @ExperimentalStdlibApi
    internal fun validateOfflinePin() {
        Log.d(TAG, "Send Offline PIN Command (41-05)")

        val offlinePinResponse = sdiManager.smartCardCt.sendOfflinePIN()
        Log.d(TAG, "Command Response: ${offlinePinResponse.response.toString(16)}")
        Log.d(TAG, "Command Result: ${offlinePinResponse.result}")
        val result = offlinePinResponse.response.toString(16)
        //SW1 SW2
        // 90 00
        // 90 03
        if (result != "9000" && result.length == 4) {
            val attempts = result[3] // Remaining attempt for offline pin entry
            uiListener.display("Invalid PIN, please re-try. Attempts left: ${attempts.digitToInt(16)}")
        }
    }
}