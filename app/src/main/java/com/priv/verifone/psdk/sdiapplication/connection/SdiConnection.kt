/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.connection

import android.util.Log
import com.priv.verifone.psdk.sdiapplication.PSDKContext
import com.verifone.payment_sdk.CommerceEvent
import com.verifone.payment_sdk.CommerceListener2
import com.verifone.payment_sdk.PaymentSdk
import com.verifone.payment_sdk.PsdkDeviceInformation
import com.verifone.payment_sdk.PsdkLogLevel
import com.verifone.payment_sdk.SdiDisconnectCallback
import com.verifone.payment_sdk.Status
import com.verifone.payment_sdk.StatusCode
import com.verifone.payment_sdk.TransactionManager

class SdiConnection(private val paymentSdk: PaymentSdk, private val callback: ConnectionCallback) {

    companion object {
        const val TAG = "SdiConnection"
    }

    enum class State {
        NOT_CONNECTED,
        CONNECTED,
    }

    private val connectionListener: CommerceListener2 = SimpleCommerceListener()
    private var state: State = State.NOT_CONNECTED

    fun connect() {
        val config = HashMap<String, String>()
        config[TransactionManager.DEVICE_PROTOCOL_KEY] = TransactionManager.DEVICE_PROTOCOL_SDI
        config[PsdkDeviceInformation.DEVICE_ADDRESS_KEY] = getIPAddress()
        config[PsdkDeviceInformation.DEVICE_CONNECTION_TYPE_KEY] = "tcpip"
        paymentSdk.configureLogLevel(PsdkLogLevel.LOG_TRACE)
        paymentSdk.initializeFromValues(connectionListener, config)
    }

    private fun getIPAddress(): String {
        return if (PSDKContext.ON_DEVICE_MODE) {
            "vfi-terminal"
        } else {
            "192.168.1.4" // Enter your terminal IP address here
        }
    }

    fun disconnect() {
        paymentSdk.tearDown()
    }

    fun connectionState(): State {
        return state
    }

    private inner class NetworkCallback : SdiDisconnectCallback() {
        override fun disconnectCallback() {
            Log.i(TAG, "connection with SDI Server is lost")
            paymentSdk.tearDown()
        }
    }

    private inner class SimpleCommerceListener : CommerceListener2() {
        private fun eventReceived(status: Int, type: String, message: String) {
            Log.i(TAG, "Received event: $type with status: $status message: $message")
        }

        override fun handleCommerceEvent(event: CommerceEvent) {
            eventReceived(event.status, event.type, event.message)
        }

        override fun handleStatus(status: Status) {
            eventReceived(status.status, status.type, status.message)
            val statusCode = status.status
            //statusMessage.postValue(status.message)
            when (status.type) {
                Status.STATUS_INITIALIZED -> when {
                    StatusCode.SUCCESS == statusCode -> {
                        Log.i(TAG, "Initialize Success")
                        //(app as PSDKContext).sdiManager = paymentSdk.sdiManager
                        paymentSdk.sdiManager.setDisconnectCallback(NetworkCallback())
                        state = State.CONNECTED
                        callback.onConnected()
                    }

                    StatusCode.CONFIGURATION_REQUIRED == statusCode -> {
                        Log.i(TAG, "Configuration required")
                    }

                    else -> {
                        Log.i(TAG, "Initialization failed")
                    }
                }

                Status.STATUS_TEARDOWN -> if (status.status == StatusCode.SUCCESS) {
                    Log.i(TAG, "Teardown Success")
                    callback.onDisconnected()
                    state = State.NOT_CONNECTED
                } else {
                    Log.i(TAG, "Teardown Failed")
                }

                else -> {
                    Log.i(TAG, "Unhandled event: ${status.type}")
                }
            }
        }
    }
}