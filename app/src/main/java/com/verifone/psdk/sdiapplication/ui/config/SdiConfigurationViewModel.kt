package com.verifone.psdk.sdiapplication.ui.config


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.verifone.psdk.sdiapplication.PSDKContext
import com.verifone.payment_sdk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


public class SdiConfigurationViewModel(private val app: Application) :
    AndroidViewModel(app) {


    companion object {
        private const val TAG = "EMVConfigViewModel"
    }

    private val psdkListener: CommerceListener2 = ConnectionListener()
    private var paymentSdk = (app as PSDKContext).paymentSDK
    private val emvConfig = (app as PSDKContext).config
    private var listenerAdded = false

    var statusMessage = MutableLiveData<String>()

    private fun background(action: () -> Unit) {
        // Launching within the view model scope for this example, but in production, these should
        // be launched from some scope that lives with the application instead of the UI.
        viewModelScope.launch {
            performBackgroundAction(action)
        }
    }

    private suspend fun performBackgroundAction(action: () -> Unit) =
        withContext(Dispatchers.Default) {
            action()
        }

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
            Log.d(TAG, "Log CT Config ")
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
