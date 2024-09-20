/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.ui.transaction

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.priv.verifone.psdk.sdiapplication.PSDKContext
import com.priv.verifone.psdk.sdiapplication.sdi.card.SdiContactless
import com.priv.verifone.psdk.sdiapplication.sdi.display.OffDeviceTransactionListenerImpl
import com.priv.verifone.psdk.sdiapplication.sdi.system.SdiUtils
import com.priv.verifone.psdk.sdiapplication.sdi.transaction.TransactionListener
import com.priv.verifone.psdk.sdiapplication.sdi.transaction.TransactionManager
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.toHexString
import com.priv.verifone.psdk.sdiapplication.ui.viewmodel.BaseViewModel
import com.priv.verifone.psdk.sdiapplication.utils.Constants.Companion.CONFIRM
import com.verifone.payment_sdk.Decimal
import com.verifone.payment_sdk.SdiCurrency
import com.verifone.payment_sdk.SdiEmvCandidate
import com.verifone.payment_sdk.SdiTouchButton
import java.math.BigDecimal

class TransactionViewModel(application: Application) : BaseViewModel(app = application) {

    companion object {
        private const val TAG = "TransactionViewModel"
    }

    enum class State {
        Idle,
        TransactionInProgress,
        SensitiveDataEntry,
        RemoveCard
    }

    var transactionState = MutableLiveData(State.Idle)
    private val sdiUtils = SdiUtils((application as PSDKContext).paymentSDK.sdiManager)

    // Transaction Screen Variables
    private var amount: Long = 12000L
    private val paymentSdk = (application as PSDKContext).paymentSDK
    private val transactionManager = TransactionManager(paymentSdk.sdiManager)
    var ledsState = MutableLiveData(false)
    var led1 = MutableLiveData(false)
    var led2 = MutableLiveData(false)
    var led3 = MutableLiveData(false)
    var led4 = MutableLiveData(false)

    // Sensitive Data Entry Variables
    private var sensitiveDataTouchButtons = MutableLiveData<ArrayList<SdiTouchButton>>()
    var sensitiveDataDigits = MutableLiveData("")
    var sensitiveDataTitle = MutableLiveData("")
    var sensitiveDataGreenButtonText = MutableLiveData(CONFIRM)

    init {
        transactionManager.setListener(getTransactionListener())
    }

    private fun getTransactionListener(): TransactionListener {
        return if (PSDKContext.ON_DEVICE_MODE) {
            OnDeviceTransactionListenerImpl()
        } else {
            OffDeviceTransactionListenerImpl(paymentSdk.sdiManager.display)
        }
    }

    private inner class OnDeviceTransactionListenerImpl : TransactionListener {
        // override transaction listener
        override fun display(message: String) {
            _text.postValue(message)
        }

        override fun presentCard(tec: Short, amount: Long, currency: SdiCurrency) {
            _text.postValue("$${Decimal(2, amount).toBigDecimal()} \n Present Card")
        }

        override fun showLeds(activateStatus: Boolean) {
            ledsState.postValue(activateStatus)
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
            Log.d(TAG, "getSensitiveDataTouchCoordinates")
            Log.d(TAG, "${sensitiveDataTouchButtons.value?.isEmpty()}")
            Log.d(TAG, "${sensitiveDataTouchButtons.value?.size}")
            return sensitiveDataTouchButtons.value!!
        }

        override fun sensitiveDataEntryTitle(message: String) {
            sensitiveDataTitle.postValue(message)
        }

        override fun showSensitiveDataEntry() {
            Log.d(TAG, "showSensitiveDataEntry")
            background {
                sensitiveDataDigits.postValue("")
                transactionState.postValue(State.SensitiveDataEntry)
            }
            // TODO
            // shows the pin entry screen
            // wait for screen to inflate fully
            // get coordinates once inflated then return
            // till this is done, use  Thread.sleep to adjust
            Thread.sleep(800)
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

        override fun captureSignature(): ByteArray {
            // TODO Add UI to capture signature and return
            return byteArrayOf()
        }

        override fun waitForCardRemoval() {
            transactionState.postValue(State.RemoveCard)
            _text.postValue("Remove Card")
        }

        override fun applicationSelection(candidates: ArrayList<SdiEmvCandidate>): Int {
            for (app in candidates) {
                Log.d(TAG, "Application Name: ${app.aid.toHexString()}")
                Log.d(TAG, "Application Label: ${app.appName}")
            }
            // TODO Always returning first application till UI is implemented
            return 0
        }

        override fun endTransaction() {
            // Do Nothing
        }
    }


    fun setSensitiveDataTouchButtons(sensitiveDataTouchButtons: ArrayList<SdiTouchButton>) {
        Log.d(TAG, "setSensitiveDataTouchButtons")
        this.sensitiveDataTouchButtons.postValue(sensitiveDataTouchButtons)
    }

    private val _text = MutableLiveData<String>().apply {
        value = "This is Transaction Fragment"
    }
    val text: LiveData<String> = _text

    fun startTransaction() {
        background {
            transactionState.postValue(State.TransactionInProgress)
            transactionManager.startTransactionFlow(amount, false)
            transactionState.postValue(State.Idle)
        }
    }

    fun abort() {
        background {
            sdiUtils.abort()
        }
    }

    fun startManualEntry() {
        background {
            transactionState.postValue(State.TransactionInProgress)
            transactionManager.startManualEntryTransactionFlow(amount)
            transactionState.postValue(State.Idle)
        }
    }

    fun setAmount(amt: String) {
        var temp = amt
        if (amt.isNullOrEmpty()) {
            temp = "100"
        }
        var amount = BigDecimal(temp)
        if (amount.toLong() == 0L) {
            amount = BigDecimal(1000L)
        }
        this.amount = amount.longValueExact()
    }
}