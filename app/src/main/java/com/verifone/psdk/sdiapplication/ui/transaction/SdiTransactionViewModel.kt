/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.verifone.psdk.sdiapplication.ui.transaction

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.verifone.payment_sdk.*
import com.verifone.psdk.sdiapplication.PSDKContext
import com.verifone.psdk.sdiapplication.sdi.card.SdiContactless
import com.verifone.psdk.sdiapplication.sdi.system.SdiSystem
import com.verifone.psdk.sdiapplication.sdi.transaction.TransactionListener
import com.verifone.psdk.sdiapplication.sdi.transaction.TransactionManager
import com.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.toHexString
import com.verifone.psdk.sdiapplication.viewmodel.BaseViewModel

public class SdiTransactionViewModel(private val app: Application) :
    BaseViewModel(app) {

    private val amount: Long = 100L

    enum class State {
        Idle,
        TransactionInProgress,
        SensitiveDataEntry
    }

    companion object {
        private const val TAG = "SdiTransactionViewModel"
        const val CONFIRM = "CONFIRM"
        const val ENTER   = "ENTER  "
    }

    private val psdkListener: CommerceListener2 = ConnectionListener()
    private val transactionListener = TransactionListenerImpl()

    private var paymentSdk = (app as PSDKContext).paymentSDK
    private val transactionManager =
        TransactionManager((app as PSDKContext).paymentSDK.sdiManager, app.config)
    private val sdiSystem = SdiSystem((app as PSDKContext).paymentSDK.sdiManager)
    private var sensitiveDataTouchButtons = MutableLiveData<ArrayList<SdiTouchButton>>()
    private var ledsState = MutableLiveData(false)
    private var transactionState = MutableLiveData(State.Idle)

    var statusMessage = MutableLiveData("")
    var sensitiveDataDigits = MutableLiveData("")
    var sensitiveDataTitle = MutableLiveData("")
    var sensitiveDataGreenButtonText = MutableLiveData(CONFIRM)
    var led1 = MutableLiveData(false)
    var led2 = MutableLiveData(false)
    var led3 = MutableLiveData(false)
    var led4 = MutableLiveData(false)

    val showLeds = Transformations.map(ledsState) {
        if (it == true) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    val sensitiveDataEntryState = Transformations.map(transactionState) {
        if (it == State.SensitiveDataEntry) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    val idleState = Transformations.map(transactionState) {
        if (it == State.Idle) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    val transactionInProgress = Transformations.map(transactionState) {
        if (it == State.TransactionInProgress) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private inner class TransactionListenerImpl : TransactionListener {
        // override transaction listener
        override fun display(message: String) {
            statusMessage.postValue(message)
        }

        override fun showLeds(b: Boolean) {
            ledsState.postValue(b)
        }

        override fun activateLed(led: SdiContactless.LED, activate: Boolean) {
            background {
                when (led) {
                    SdiContactless.LED.ONE -> led1.postValue(activate)
                    SdiContactless.LED.TWO -> led2.postValue(activate)
                    SdiContactless.LED.THREE -> led3.postValue(activate)
                    SdiContactless.LED.FOUR -> led4.postValue(activate)
                }
            }
        }

        override fun getSensitiveDataTouchCoordinates(): ArrayList<SdiTouchButton> {
            // implementation is in TransactionFragment.kt
            // under getSensitiveDataTouchButtons() function
            return sensitiveDataTouchButtons.value!!
        }

        override fun sensitiveDataEntryTitle(message: String) {
            sensitiveDataTitle.postValue(message)
        }

        override fun showSensitiveDataEntry() {
            sensitiveDataDigits.postValue("")
            transactionState.postValue(State.SensitiveDataEntry)
            // TODO
            // shows the pin entry screen
            // wait for screen to inflate fully
            // get coordinates once inflated then return
            // till this is done, use  Thread.sleep to adjust
            Thread.sleep(500)
        }

        override fun pinEntryComplete() {
            sensitiveDataDigits.postValue("")
            transactionState.postValue(State.TransactionInProgress)
        }

        override fun sensitiveDigitsEntered(digits: String) {
            sensitiveDataDigits.postValue(digits)
        }

        override fun setSensitiveDataGreenButtonText(text: String) {
            sensitiveDataGreenButtonText.postValue(text)
        }

        override fun applicationSelection(candidates: ArrayList<SdiEmvCandidate>): Int {
            for (app in candidates) {
                Log.d(TAG, "Application Name: ${app.aid.toHexString()}")
                Log.d(TAG, "Application Label: ${app.appName}")
            }
            //TODO Always returning first application till UI is implemented
            return 0
        }
    }

    init {
        start()
    }

    fun printBmpHacked() {
        background {
            sdiSystem.printBmpHack(app.applicationContext)
        }
    }

    fun printBmp() {
        background {
            sdiSystem.printBmp(app.applicationContext)
        }
    }

    fun printHtml() {
        background {
            sdiSystem.printHtml(app.applicationContext)
        }
    }

    fun abort() {
        background {
            sdiSystem.abort()
        }
    }

    private fun start() {
        // to check for connection status during this flow
        paymentSdk.addListener(psdkListener)
        transactionManager.setListener(transactionListener)
    }

    fun startTransaction() {
        background {
            transactionState.postValue(State.TransactionInProgress)
            transactionManager.startTransactionFlow(amount, false)
            transactionState.postValue(State.Idle)
        }
    }

    fun startNfcProcessing() {
        background {
            transactionState.postValue(State.TransactionInProgress)
            transactionManager.startNfcProcessingFlow()
            transactionState.postValue(State.Idle)
        }
    }

    fun startManualEntryTransaction() {
        background {
            transactionState.postValue(State.TransactionInProgress)
            transactionManager.startManualEntryTransactionFlow(amount)
            transactionState.postValue(State.Idle)
        }
    }

    fun setSensitiveDataTouchButtons(pinTouchButtons: ArrayList<SdiTouchButton>) {
        this.sensitiveDataTouchButtons.postValue(pinTouchButtons)
    }

    private inner class ConnectionListener : CommerceListener2() {
        private fun eventReceived(status: Int, type: String, message: String) {
            Log.i(TAG, "Received event: $type with status: $status message: $message")
        }

        override fun handleCommerceEvent(event: CommerceEvent) {
            eventReceived(event.status, event.type, event.message)
        }

        override fun handleStatus(status: Status) {
            eventReceived(status.status, status.type, status.message)
            Log.d(TAG, "handleStatus statusCode: ${status.status}")
            statusMessage.postValue(status.message)
        }
    }
}
