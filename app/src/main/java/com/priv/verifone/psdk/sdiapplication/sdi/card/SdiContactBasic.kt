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
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.dateToString
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.hexStringToByteArray
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.toHexString
import com.priv.verifone.psdk.sdiapplication.ui.transaction.SdiTransactionViewModel
import com.verifone.payment_sdk.*

/*
 * This is responsible for processing EMV contact transaction in callback mode
 * Here POS app receives the required trigger events on SdiEmvCallback, where POS app needs to handle the use-case(PIN, Multiple Application Prompt)
 */
class SdiContactBasic(private val sdiManager: SdiManager, private val config: Config):SdiContact(sdiManager, config) {

    companion object {
        private const val TAG = "SdiCardCT"
    }

    private val emvCallback = EMVCallback()

    override fun startTransaction(amount: Long): SdiEmvTxnResponse {
        // Since the sample app implements both Contact Callback flow and contact re entrance flow
        // we are setting the callback here otherwise you can set it once
        sdiManager.setEmvCallback(emvCallback)
        Log.d(TAG, "EMV CT Start Transaction Command (39-10)")
        val ctTxnConfig = SdiEmvTxn.create()
        val txnOptions = SdiEmvTransactionOptions.create()
//        RETURN_CANDIDATE_LIST | RETURN_AFTER_READ_RECORD | RETURN_FOR_CVM_PROCESS

        // Enable Application selection callback for multi app cards
        txnOptions.setCtOption(SdiEmvCtTransactionOption.EMV_CT_SELOP_CBCK_APPLI_SEL, true)
        // Enable callback during read record
        txnOptions.setCtOption(SdiEmvCtTransactionOption.EMV_CT_TXNOP_LOCAL_CHCK_CALLBACK, true)

        //txnOptions.setCtOption(SdiEmvCtTransactionOption.EMV_CT_SELOP_CBCK_DOMESTIC_APPS, true)
        ctTxnConfig.setTransactionOptions(txnOptions)

        val today = Utils.getCurrentDateTime()
        val date = today.dateToString("yyMMdd").hexStringToByteArray()
        val time = today.dateToString("hhmmss").hexStringToByteArray()

        // EMV CT Transaction (39-10)
        val sdiEmvTxnResponse: SdiEmvTxnResponse = sdiManager.emvCt.startTransaction(
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
        if (sdiEmvTxnResponse.result != SdiResultCode.OK) {
            Log.e(TAG, "Failed to start transaction")
        }
        return sdiEmvTxnResponse
    }

    override fun continueOffline(): SdiEmvTxnResponse {
        // EMV CT Continue Offline (39-11)

        Log.d(TAG, "EMV CT Continue Offline Command (39-11)")
        val sdiEmvTxn = SdiEmvTxn.create()
//        RETURN_CANDIDATE_LIST | RETURN_AFTER_READ_RECORD | RETURN_FOR_CVM_PROCESS
        // 1st GEn AC
        val result = sdiManager.emvCt.continueOffline(sdiEmvTxn)
        Log.d(TAG, "Command Result: ${result.result.name}")

        retrieveTags(result.txn)
        retrieveTagsUsingApi(config.getCtTagsToFetch())
        return result
    }

    // This is the callback where PSDK-SDI triggers the event for EMV processing.
    private inner class EMVCallback : SdiEmvCallback() {
        @ExperimentalStdlibApi
        override fun emvCallback(type: SdiEmvCallbackType?, input: SdiEmvTxn?, output: SdiEmvTxn?) {
            when (type) {
                SdiEmvCallbackType.REDUCE_CANDIDATES -> {
                    Log.d(TAG, "EMV Callback ${type?.name}")
                    val cbCandidateList = input?.cbCandidateList
                    // Send list to UI for selection
                    if (cbCandidateList != null) {
                        for (app in cbCandidateList) {
                            Log.d(TAG, "Application Name: ${app.aid.toHexString()}")
                            Log.d(TAG, "Application Label: ${app.appName}")
                        }
                    }
                    // Implement UI for application selection
                    val selectedCandidate =
                        "01".hexStringToByteArray() // index + 1 , this will select the 1st candidate
                    output?.reducedCanditateList = selectedCandidate
                }
                SdiEmvCallbackType.PIN -> {
                    Log.d(TAG, "Pin Info: ${input?.pinInfo?.toString(16)}")
                    Log.d(TAG, "Pin Info: ${input?.pinInfo?.toString()}")

                    Log.d(TAG, "EMV Callback ${type?.name}")
                    listener.setSensitiveDataGreenButtonText(SdiTransactionViewModel.CONFIRM)
                    listener.sensitiveDataEntryTitle("Enter Pin")
                    listener.showSensitiveDataEntry()
                    val result = getPinUsingCallback()
                    // We need to find out how to read PinInfo field, that has information on whether
                    // the PIN is online or offline, it toggles between value 0 and 2
                    if (result == SdiResultCode.OK) {
                        validateOfflinePin()
                    }
                    if (output != null) {
                        Log.d(TAG, "EMV Callback return PIN Status : code : ${result.ordinal.toShort()} name: ${result.name}")

                        Log.d(TAG, "Setting PIN Info: ${output.setPINInfo(result.ordinal.toShort())}")
                    }
                    listener.pinEntryComplete()
                }
                SdiEmvCallbackType.LOCAL_CHECKS -> Log.d(TAG, "EMV Callback ${type.name}")
                else -> {
                    retrieveTags(input!!)
                    retrieveTagsUsingApi(config.getCtTagsToFetch())
                    val today = Utils.getCurrentDateTime()
                    val date = today.dateToString("yyMMdd").hexStringToByteArray()
                    val time = today.dateToString("hhmmss").hexStringToByteArray()
                    val amount = input!!.amount
                    output!!.transactionDate = date
                    output!!.transactionTime = time
                    output!!.amount =amount + amount
                    output!!.cashbackAmount = amount
                    Log.d(TAG, "EMV Callback ${type?.name}")
                }
            }
        }
    }
}