/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.verifone.psdk.sdiapplication.sdi.card

import android.util.Log
import com.verifone.psdk.sdiapplication.sdi.config.Config
import com.verifone.psdk.sdiapplication.ui.transaction.SdiTransactionViewModel
import com.verifone.psdk.sdiapplication.ui.transaction.SdiTransactionViewModel.Companion.ENTER
import com.verifone.payment_sdk.SdiControlCallback
import com.verifone.payment_sdk.SdiManager
import com.verifone.payment_sdk.SdiResultCode
import com.verifone.payment_sdk.SdiTlv

class SdiManual(private val sdiManager: SdiManager, private val config: Config) :
    SdiCard(sdiManager, config) {
    companion object {
        private const val TAG = "SdiCardManual"
    }
    private val controlCallback: ControlCallback = ControlCallback()

    override fun initialize(): SdiResultCode {
        sdiManager.setControlCallback(controlCallback)
        return super.initialize()
    }

    override fun startTransactionFlow(amount: Long): SdiResultCode {
        listener.setSensitiveDataGreenButtonText(ENTER)
        listener.sensitiveDataEntryTitle("Enter Card Number")
        listener.showSensitiveDataEntry()
        val buttons = listener.getSensitiveDataTouchCoordinates()
        Log.d(TAG, "cardDataEntry Command (21-02)")

        // 01 to skip CVV sdiManager.system.setCVVDeactivation(0x01)
        // 02 to skip CVV and expiry date  sdiManager.system.setCVVDeactivation(0x02)

        val result = sdiManager.msr.cardDataEntry(buttons)
        Log.d(TAG, "Command Result response : ${result.response}")
        Log.d(TAG, "Command Result result : ${result.result.name}")
        result.response
        return SdiResultCode.OK
    }

    private inner class ControlCallback : SdiControlCallback() {
        override fun controlCallback(data: SdiTlv?): Int {
            val CONTINUE = 1
            val RETRY = 2
            val ABORT = 3
            val messageTag = 0xF0
            val panEnteredTag = 0xBF01
            val expiryDateEntered = 0xBF02
            val cardValidationCheck = 0xDFA12C
            val luhnCheck = 0xDFA12E
            val expiryDateCheck = 0xDFA12D

            var result = ABORT
            if ( data != null) {

                Log.d(TAG, "panEnteredTag : ${data.obtain(messageTag).count(panEnteredTag)}")
                // Checking if PAN was entered
                if (data.obtain(messageTag).count(panEnteredTag) > 0) {
                    if (data.obtain(messageTag).obtain(panEnteredTag).count(cardValidationCheck) > 0) {
                        Log.d(TAG, "bin range: ${data.obtain(messageTag).obtain(panEnteredTag).obtain(cardValidationCheck).string}")
                    }
                    if (data.obtain(messageTag).obtain(panEnteredTag).count(luhnCheck) > 0) {
                        Log.d(TAG, "luhn check ${data.obtain(messageTag).obtain(panEnteredTag).obtain(luhnCheck).number}")
                    }
                    Log.d(TAG, "Pan Entered")
                    result = CONTINUE
                    // if 0x14
                    listener.setSensitiveDataGreenButtonText(SdiTransactionViewModel.CONFIRM)
                    listener.sensitiveDataEntryTitle("Enter Expiry Date")
                    listener.sensitiveDigitsEntered("")
                    //if 0x15 // probably for MC torn transaction test case
                    //listener.display("Re-tap")
                }
                Log.d(TAG, "expiryDateEntered : ${data.obtain(messageTag).count(expiryDateEntered)}")
                // Checking if Expiry Date was entered
                if (data.obtain(messageTag).count(expiryDateEntered) > 0) {
                    Log.d(TAG, "expiryDateCheck : ${data.obtain(messageTag).obtain(expiryDateEntered).count(expiryDateCheck)}")
                    Log.d(TAG, "Expiry Date Entered")
                    listener.setSensitiveDataGreenButtonText(SdiTransactionViewModel.CONFIRM)
                    listener.sensitiveDataEntryTitle("Enter CVV")
                    listener.sensitiveDigitsEntered("")
                    result = CONTINUE
                    // if 0x14
                    //if 0x15 // probably for MC torn transaction test case
                    //listener.display("Re-tap")
                }
            }
            return result
        }
    }
}
