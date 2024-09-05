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
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.toHexString
import com.verifone.payment_sdk.SdiCurrency
import com.verifone.payment_sdk.SdiDialogOptions
import com.verifone.payment_sdk.SdiDisplay
import com.verifone.payment_sdk.SdiInputType
import com.verifone.payment_sdk.SdiLanguage
import java.util.EnumSet

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

    fun asyncTextMessage(
        valueMap: HashMap<String, String>, keyActions: HashMap<String, String>,
        headerText: String, enterText: String,
        clearText: String, cancelText: String
    ) {
        Log.d(TAG, "Handle Display (24-03)")
        val dialogOptions = EnumSet.of(SdiDialogOptions.ASYNC, SdiDialogOptions.SUCCESS_LOGO)
        val resultCode = sdiDisplay.textWith(
            1, dialogOptions, valueMap, keyActions,
            headerText, enterText, clearText, cancelText, 0, null
        )
        Log.d(TAG, "Command Result: $resultCode")
    }

    // In case that a previous command Handle Display (24-03) was executed asynchronously
    // with TLV tag DFA13D containing dialog options DLG_Async and DLG_StoreAsyncResult,
    // this command can be used to get the status and result of this asynchronous dialog.
    fun getAsyncDisplayResult() {
        Log.d(TAG, "Get Async Display Result (24-0B)")
        val resultCode = sdiDisplay.getAsyncResult(null)
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

    fun secureInput(inputType: SdiInputType) {
        Log.d(TAG, "Handle Secure Input (24-04)")
        val inputResponse =
            sdiDisplay.input(inputType, SdiLanguage.NO_LANGUAGE, 0, null, null, false, null)
        Log.d(TAG, "Command Result: $inputResponse")
    }

    fun menu(headLine: String, entries: ArrayList<String>) {
        Log.d(TAG, "Handle Menu (24-05)")
        val menuResponse = sdiDisplay.menu(headLine, entries, 0, false, null)
        Log.d(TAG, "Command Result: $menuResponse")
    }

    fun captureSignature() {
        Log.d(TAG, "Handle Signature Capture (24-08)")
        val signatureResponse = sdiDisplay.signatureCapture(SdiLanguage.NO_LANGUAGE, 0, null)
        Log.d(TAG, "Command Result: $signatureResponse")
        Log.d(TAG, "Captured signature: ${signatureResponse.outData.toHexString()}")
    }

    // API to handle a customer specific HTML dialog for special user inputs.
    // For customized dialogs w/o user input, please use text API
    fun htmlDialog(htmlFileName: String, valueMap: HashMap<String, String>) {
        Log.d(TAG, "Handle HTML Dialog (24-0A)")
        val dialogResponse =
            sdiDisplay.dialog(htmlFileName, valueMap, SdiLanguage.NO_LANGUAGE, false, null)
        Log.d(TAG, "Command Result: $dialogResponse")
    }

    fun clearScreen() {
        Log.d(TAG, "Display Idle Screen (24-07)")
        val resultCode = sdiDisplay.idleScreen(null)
        Log.d(TAG, "Command Result: $resultCode")
    }
}