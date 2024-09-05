/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.sdi.display

import android.util.Log
import com.verifone.payment_sdk.SdiCurrency
import com.verifone.payment_sdk.SdiDisplay
import com.verifone.payment_sdk.SdiInputType
import com.verifone.payment_sdk.SdiLanguage

/*
 * This is responsible for displaying message and UI on terminal
 * And only supported in Headed mode or off device integration case
 */
class Display(private val sdiDisplay: SdiDisplay) {

    companion object {
        private const val TAG = "Display"
        private const val DISPLAY_TEXT = "text"
    }

    fun textMessage(dataValue: String, beep: Boolean) {
        Log.d(TAG, "Handle Display (24-03)")
        val resultCode = sdiDisplay.text(1, DISPLAY_TEXT, dataValue, 0, null, beep, null)
        Log.d(TAG, "Command Result: $resultCode")
    }

    fun enableLed(status: Boolean) {
        Log.d(TAG, "Activate LEDs (24-09)")
        val resultCode = sdiDisplay.leds(status)
        Log.d(TAG, "Command Result: $resultCode")
    }

    fun cardRequest(tec: Short, amount: Long, currency: SdiCurrency) {
        Log.d(TAG, "Handle Card Request Display (24-06)")
        val resultCode = sdiDisplay.cardRequest(tec, 0, amount, currency, null)
        Log.d(TAG, "Command Result: $resultCode")
    }

    fun secureInput(inputType: SdiInputType, beep: Boolean) {
        Log.d(TAG, "Handle Secure Input (24-04)")
        val resultCode =
            sdiDisplay.input(inputType, SdiLanguage.NO_LANGUAGE, 0, null, null, beep, null)
        Log.d(TAG, "Command Result: $resultCode")
    }

    fun menu(headLine: String, entries: ArrayList<String>) {
        Log.d(TAG, "Handle Menu (24-05)")
        val resultCode = sdiDisplay.menu(headLine, entries, 0, false, null)
        Log.d(TAG, "Command Result: $resultCode")
    }

    fun captureSignature() {
        Log.d(TAG, "Handle Signature Capture (24-08)")
        val resultCode = sdiDisplay.signatureCapture(SdiLanguage.NO_LANGUAGE, 0, null)
        Log.d(TAG, "Command Result: $resultCode")
    }

    fun clearScreen() {
        Log.d(TAG, "Display Idle Screen (24-07)")
        val resultCode = sdiDisplay.idleScreen(null)
        Log.d(TAG, "Command Result: $resultCode")
    }
}