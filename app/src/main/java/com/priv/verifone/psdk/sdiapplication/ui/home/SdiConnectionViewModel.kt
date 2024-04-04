/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.verifone.payment_sdk.*
import com.priv.verifone.psdk.sdiapplication.PSDKContext
import com.priv.verifone.psdk.sdiapplication.sdi.system.SdiSystem
import com.priv.verifone.psdk.sdiapplication.ui.utils.getDeviceInformation
import com.priv.verifone.psdk.sdiapplication.viewmodel.BaseViewModel

public class SdiConnectionViewModel(private val app: Application) : BaseViewModel(app) {

    enum class State {
        NOT_CONNECTED,
        CONNECTED,
    }

    companion object {
        private const val TAG = "SdiConnectionViewModel"
    }

    // This listener triggers callback event from PSDK
    private val psdkListener: CommerceListener2 = SimpleCommerceListener()
    private val networkListener: SdiDisconnectCallback = NetworkCallback()
    private var paymentSdk = (app as PSDKContext).paymentSDK
    lateinit var system : SdiSystem
    private var deviceInformation = MutableLiveData<PsdkDeviceInformation?>()

    val devInfo = Transformations.map(deviceInformation) {
        getDeviceInformation(it, system, app as PSDKContext)
    }
    var statusMessage = MutableLiveData<String?>()
    // Status Display
    private var state = MutableLiveData<State>()

    val stateNotConnected = Transformations.map(state) {
        it == null || it.equals(State.NOT_CONNECTED)
    }

    val stateConnected = Transformations.map(state) {
        it.equals(State.CONNECTED)
    }

    init {
        start()
    }

    fun start() {
        state.value = State.NOT_CONNECTED
    }

    /*
     * Establish the connection with PSDK based on below configuration parameters
     * And the callback event will be triggered from PSDK through CommerceListener2
     */
    fun initialize() {
        background {
            val config = HashMap<String, String>()
            config[TransactionManager.DEVICE_PROTOCOL_KEY] = TransactionManager.DEVICE_PROTOCOL_SDI
            config[PsdkDeviceInformation.DEVICE_ADDRESS_KEY] = "vfi-terminal"
            config[PsdkDeviceInformation.DEVICE_CONNECTION_TYPE_KEY] = "tcpip"
            paymentSdk.configureLogLevel(PsdkLogLevel.LOG_TRACE)
            paymentSdk.initializeFromValues(psdkListener, config)
        }
    }

    /*
     * Closes connection between POS app and PSDK
     * After successful teardown, POS app have to initialize the PSDK again to perform any kind of operation
     */
    fun teardown() {
        background {
            paymentSdk.tearDown()
        }
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
            statusMessage.postValue(status.message)
            when (status.type) {
                Status.STATUS_INITIALIZED -> when {
                    StatusCode.SUCCESS == statusCode -> {
                        Log.i(TAG, "Initialize Success")
                        (app as PSDKContext).sdiManager = paymentSdk.sdiManager
                        paymentSdk.sdiManager.setDisconnectCallback(networkListener)
                        state.postValue(State.CONNECTED)
                        system = SdiSystem(sdiManager = paymentSdk.sdiManager)
                        deviceInformation.postValue(paymentSdk.deviceInformation)
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
                    (app as PSDKContext).sdiManager = null
                    state.postValue(State.NOT_CONNECTED)
                } else {
                    Log.i(TAG, "Teardown Failed")
                } else -> {
                Log.i(TAG, "Unhandled event: ${status.type}")
                }
            }
        }
    }
}
