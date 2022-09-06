package com.verifone.psdk.sdiapplication.ui.home


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.verifone.psdk.sdiapplication.PSDKContext
import com.verifone.psdk.sdiapplication.ui.utils.getDeviceInformation
import com.verifone.payment_sdk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import com.verifone.psdk.sdiapplication.sdi.system.SdiSystem

public class SdiConnectionViewModel(private val app: Application) : AndroidViewModel(app) {

    enum class State {
        NOT_CONNECTED,
        CONNECTED,
    }

    companion object {
        private const val TAG = "SdiConnectionViewModel"
    }

    private val psdkListener: CommerceListener2 = SimpleCommerceListener()
    private var paymentSdk = (app as PSDKContext).paymentSDK
    lateinit var system : SdiSystem
    private var deviceInformation = MutableLiveData<PsdkDeviceInformation?>()

    val devInfo = Transformations.map(deviceInformation) {
        getDeviceInformation(it, system)
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


    private fun background(action: () -> Unit) {
        // Launching within the view model scope for this example, but in production, these should
        // be launched from some scope that lives with the application instead of the UI.
        viewModelScope.launch {
            performBackgroundAction(action)
        }
    }

    private suspend fun performBackgroundAction(action: () -> Unit) = withContext(Dispatchers.Default) {
        action()
    }

    init {
        start()
    }

    fun start() {
        state.value = State.NOT_CONNECTED
    }

    fun initialize() {
        background {
            val config = HashMap<String, String>()
            config[TransactionManager.DEVICE_PROTOCOL_KEY] = TransactionManager.DEVICE_PROTOCOL_SDI
            config[PsdkDeviceInformation.DEVICE_ADDRESS_KEY] = "vfi-terminal" //
            config[PsdkDeviceInformation.DEVICE_CONNECTION_TYPE_KEY] = "tcpip"
            paymentSdk.configureLogLevel(PsdkLogLevel.LOG_TRACE)
            paymentSdk.initializeFromValues(psdkListener, config) // CM5
            //mPaymentSdk.initialize(mCommerceListener) // Trinity
        }
    }

    fun teardown() {
        background {
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
