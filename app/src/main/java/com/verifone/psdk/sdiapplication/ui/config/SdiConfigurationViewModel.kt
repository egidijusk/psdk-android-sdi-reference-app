/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.verifone.psdk.sdiapplication.ui.config

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.verifone.payment_sdk.CommerceEvent
import com.verifone.payment_sdk.CommerceListener2
import com.verifone.payment_sdk.Status
import com.verifone.psdk.sdiapplication.PSDKContext
import com.verifone.psdk.sdiapplication.viewmodel.BaseViewModel

public class SdiConfigurationViewModel(private val app: Application) :
    BaseViewModel(app) {

    companion object {
        private const val TAG = "EMVConfigViewModel"
    }

    // This listener triggers callback event from PSDK
    private val psdkListener: CommerceListener2 = ConnectionListener()
    private var paymentSdk = (app as PSDKContext).paymentSDK
    private val emvConfig = (app as PSDKContext).config
    private var listenerAdded = false

    var statusMessage = MutableLiveData<String>()

    init {
        start()
    }

    private fun start() {
        addListener()
    }

    private fun addListener() {
        if (!listenerAdded) {
            paymentSdk.addListener(psdkListener)
            listenerAdded = true
        }
    }

    fun removeListener() {
        if (listenerAdded) {
            paymentSdk.removeListener(psdkListener)
            listenerAdded = false
        }
    }

    fun setContactConfig() {
        background {
            val result = emvConfig.setContactConfiguration()
            Log.d(TAG, " CT config result: ${result.name}")
            statusMessage.postValue(" CT config result: ${result.name}")
        }
    }

    fun setCtlsConfig() {
        background {
            val result = emvConfig.setCtlsConfiguration()
            statusMessage.postValue(" Ctls config result: ${result.name}")
        }
    }

    fun logCtConfig() {
        background {
            Log.d(TAG, "Log CT Config ")
            emvConfig.logCtConfiguration()
        }
    }

    fun logCtlsConfig() {
        background {
            Log.d(TAG, "Log CTLS Config ")
            emvConfig.logCtlsConfiguration()
        }
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
