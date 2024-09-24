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
import com.priv.verifone.psdk.sdiapplication.sdi.card.SdiContactless
import com.priv.verifone.psdk.sdiapplication.sdi.transaction.TransactionListener
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.toHexString
import com.verifone.payment_sdk.SdiCurrency
import com.verifone.payment_sdk.SdiDisplay
import com.verifone.payment_sdk.SdiEmvCandidate
import com.verifone.payment_sdk.SdiTouchButton

// This listener callback triggers in off device case
class OffDeviceTransactionListenerImpl(sdiDisplay: SdiDisplay) : TransactionListener {

    companion object {
        private const val TAG = "OffDeviceTransactionListenerImpl"
    }

    private val display = Display(sdiDisplay)

    override fun display(message: String) {
        display.textMessage(message, beep = false)
    }

    override fun presentCard(tec: Short, amount: Long, currency: SdiCurrency) {
        display.cardRequest(tec, amount, currency)
    }

    override fun showLeds(activateStatus: Boolean) {
        display.enableLed(activateStatus)
    }

    override fun activateLed(led: SdiContactless.LED, activate: Boolean) {
        // Do nothing, as LED handled internally after enabled
    }

    override fun getSensitiveDataTouchCoordinates(): ArrayList<SdiTouchButton> {
        // NOTE : This TLV tag is supported for headless mode only (ignored for standard or off device mode).
        return ArrayList()
    }

    override fun sensitiveDataEntryTitle(message: String) {
        // Do Nothing
    }

    override fun showSensitiveDataEntry() {
        // Do Nothing
    }

    override fun pinEntryComplete() {
        // Do Nothing
    }

    override fun sensitiveDigitsEntered(digits: String) {
        // Do Nothing
    }

    override fun setSensitiveDataGreenButtonText(text: String) {
        // Do Nothing
    }

    override fun captureSignature(): ByteArray {
        return display.captureSignature()
    }

    override fun waitForCardRemoval() {
        display.textMessage("Remove Card", beep = true)
    }

    override fun applicationSelection(candidates: ArrayList<SdiEmvCandidate>): Int {
        val appNameList = ArrayList<String>()
        for (app in candidates) {
            Log.d(TAG, "Application Name: ${app.aid.toHexString()}")
            Log.d(TAG, "Application Label: ${app.appName}")
            appNameList.add(app.appName)
        }

        // This will return the selected app index
        // Menu api is tested but app selection is not tested
        return display.menu("Select Application", appNameList)
    }

    override fun endTransaction() {
        display.clearScreen()
    }
}