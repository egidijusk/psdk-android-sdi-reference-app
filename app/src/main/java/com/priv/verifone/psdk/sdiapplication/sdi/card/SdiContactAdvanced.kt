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
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.dateToString
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.hexStringToByteArray
import com.priv.verifone.psdk.sdiapplication.utils.Constants.Companion.CONFIRM
import com.verifone.payment_sdk.*
import java.util.*
import com.verifone.payment_sdk.SdiEmvTxn
import kotlin.collections.ArrayList

/*
 * This is responsible for processing EMV contact transaction in re-entrance mode
 * Here POS app receives the required trigger events on particular api which are called in loop as shown in code
 */
class SdiContactAdvanced(private val sdiManager: SdiManager) :
    SdiContact(sdiManager) {

    companion object {
        private const val TAG = "SdiCardContactAdvanced"
    }
    private var amount: Long = 0

    override fun startTransaction(amount: Long): SdiEmvTxnResponse {
        this.amount = amount
        sdiManager.setEmvCallback(null)
        Log.d(TAG, "EMV CT Start Transaction Command (39-10)")
        // Enable Application selection for multi app cards
        val ctTxnConfig = SdiEmvTxn.create()

        // The the break points where we app needs control back
        val txnSteps = EnumSet.of(
            SdiEmvTransactionSteps.RETURN_CANDIDATE_LIST,
            SdiEmvTransactionSteps.RETURN_AFTER_READ_RECORD,
            SdiEmvTransactionSteps.RETURN_FOR_CVM_PROCESS
        )
        ctTxnConfig.transactionSteps = txnSteps

        val txnOptions = SdiEmvTransactionOptions.create()
        txnOptions.setCtOption(SdiEmvCtTransactionOption.EMV_CT_SELOP_CBCK_APPLI_SEL, true)
        ctTxnConfig.setTransactionOptions(txnOptions)

        /* Exclude configured AID for transaction */
        ctTxnConfig.setExcludeAID(ArrayList<ByteArray>().apply { add("A0000000032020".hexStringToByteArray())})

        val today = Utils.getCurrentDateTime()
        val date = today.dateToString("yyMMdd").hexStringToByteArray()
        val time = today.dateToString("hhmmss").hexStringToByteArray()

        var continueStartTxn: Boolean
        var sdiEmvTxnResponse: SdiEmvTxnResponse?

        do {
            sdiEmvTxnResponse = sdiManager.emvCt.startTransaction(
                SdiEmvTransaction.GOODS_SERVICE,
                amount,
                date,
                time,
                txnCounter.toLong(),
                ctTxnConfig
            )
            // Candidate list will come here
            // get the candidate list and send result back to start transaction as part of  setBuildAppList(SdiEmvBuildOptions opt)
            Log.i(TAG, "Command Result: ${sdiEmvTxnResponse.result.name}")
            when (sdiEmvTxnResponse.result) {

                SdiResultCode.OK -> continueStartTxn = false

                SdiResultCode.EMVSTATUS_APP_REQ_CANDIDATE -> {
                    continueStartTxn = true
                    val cbCandidateList = sdiEmvTxnResponse?.txn?.candidateList
                    if (cbCandidateList != null) {
                        // UI call for user to select application and return the result of selection
                        Log.d(TAG, cbCandidateList.toString())
                        val selection = uiListener.applicationSelection(cbCandidateList)

                        var options:SdiEmvBuildOptions?= null

                        when (selection) {
                            0-> options = SdiEmvBuildOptions.REUSE_EXISTING_LIST_SEL_0
                            1-> options = SdiEmvBuildOptions.REUSE_EXISTING_LIST_SEL_1
                            2-> options = SdiEmvBuildOptions.REUSE_EXISTING_LIST_SEL_2
                            3-> options = SdiEmvBuildOptions.REUSE_EXISTING_LIST_SEL_3
                            4-> options = SdiEmvBuildOptions.REUSE_EXISTING_LIST_SEL_4
                        }
                        ctTxnConfig.buildAppList = options
                    }
                }
                SdiResultCode.EMVSTATUS_ABORT->continueStartTxn = false

                else -> continueStartTxn = false
            }

        }while(continueStartTxn)
        if (sdiEmvTxnResponse?.result != SdiResultCode.OK) {
            Log.e(TAG, "Failed to start transaction")
        }
        if (sdiEmvTxnResponse?.txn != null) {
            retrieveTags(sdiEmvTxnResponse.txn)
        }
        return sdiEmvTxnResponse!!
    }

    @ExperimentalStdlibApi
    override fun continueOffline(): SdiEmvTxnResponse {
        // EMV CT Continue Offline (39-11)
        var continueOffline: Boolean
        Log.d(TAG, "EMV CT Continue Offline Command (39-11)")
        val sdiEmvTxn = SdiEmvTxn.create()

        var response: SdiEmvTxnResponse?
        do {
            response = sdiManager.emvCt.continueOffline(sdiEmvTxn)
            Log.d(TAG, "Command Result: ${response.result.name}")

            when (response.result) {
                SdiResultCode.EMVSTATUS_APP_REQ_READREC -> {
                    Log.d(TAG, "Read Record")
                    // show cash ui based on emv tags
                    /*
                     //Example usecase: Validate if cashback is allowed
                     // Check PDOL if it requested for the tags you are going to change
                     //prompt user for cashback
                     // Update the tags you want modified ex Transaction Type, Amount , other mount and set it back in txn object
                    val today = Utils.getCurrentDateTime()
                    val date = today.dateToString("yyMMdd").hexStringToByteArray()
                    val time = today.dateToString("hhmmss").hexStringToByteArray()
                    sdiEmvTxn.transactionDate = date
                    sdiEmvTxn.transactionTime = time
                    sdiEmvTxn.amount =amount + amount
                    sdiEmvTxn.cashbackAmount = amount
                    */
                    continueOffline = true
                    retrieveTags(response.txn)
                    //retrieveTagsUsingApi(config.getCtTagsToFetch())
                }
                // MS_DECLINE_AAC for requestCardData after read record
                SdiResultCode.EMVSTATUS_APP_REQ_ONL_PIN,
                SdiResultCode.EMVSTATUS_APP_REQ_OFFL_PIN,
                SdiResultCode.EMVSTATUS_APP_REQ_PLAIN_PIN -> {
                    retrieveTags(response.txn)
                    //retrieveTagsUsingApi(config.getCtTagsToFetch())
                    // Interaction with UI elements
                    uiListener.setSensitiveDataGreenButtonText(CONFIRM)
                    // Interaction with UI elements
                    uiListener.sensitiveDataEntryTitle("Enter Pin")
                    // Interaction with UI elements
                    uiListener.showSensitiveDataEntry() // showing PIN entry
                    val pinResult = getPinUsingPolling()
                    Log.d(TAG, "PIN Entry Result: ${pinResult.name}")
                    if (pinResult == SdiResultCode.ERR_PED_BYPASS) {
                        val txnSteps = EnumSet.of(SdiEmvTransactionSteps.MS_PIN_BYPASS)
                        sdiEmvTxn.transactionSteps = txnSteps
                    } else if (pinResult == SdiResultCode.ERR_CANCELLED || pinResult == SdiResultCode.ERR_PED_TIMEOUT) {
                        val txnSteps = EnumSet.of(SdiEmvTransactionSteps.MS_ABORT_TXN)
                        sdiEmvTxn.transactionSteps = txnSteps
                    }
                    if ((response.result == SdiResultCode.EMVSTATUS_APP_REQ_OFFL_PIN ||
                        response.result == SdiResultCode.EMVSTATUS_APP_REQ_PLAIN_PIN) &&
                        pinResult == SdiResultCode.OK) {
                        validateOfflinePin()
                    }
                    if (response.result == SdiResultCode.EMVSTATUS_APP_REQ_ONL_PIN &&
                        pinResult == SdiResultCode.OK) {
                        crypto.getEncryptedPinBlock()
                    }
                    continueOffline = true
                }
                // 1st GEN AC Result
                SdiResultCode.EMVSTATUS_ARQC,
                SdiResultCode.EMVSTATUS_TC,
                SdiResultCode.EMVSTATUS_AAC-> {
                    Log.d(TAG, "Continue Offline Completed :${response.result.name}")
                    continueOffline = false
                }

                else -> {
                    Log.d(TAG, "Not Handled:${response.result.name}")
                    continueOffline = false
                }
            }
        } while (continueOffline)

        return response!!
    }
}