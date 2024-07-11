package com.priv.verifone.psdk.sdiapplication.ui.updateservice

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.priv.verifone.psdk.sdiapplication.tms.TmsService
import com.priv.verifone.psdk.sdiapplication.tms.TmsServiceCallback
import com.priv.verifone.psdk.sdiapplication.ui.viewmodel.BaseViewModel
import com.verifone.updateservicelib.IUpdateServiceCallback
import com.verifone.updateservicelib.UpdateStatus

class UpdateserviceViewModel(application: Application) : BaseViewModel(app = application) {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Update Service Fragment"
    }
    val text: LiveData<String> = _text

    companion object {
        private const val TAG = "UpdateServiceViewModel"
    }

    private val tmsService = TmsService(application)
    private val tmsServiceCallback = object : TmsServiceCallback {
        override fun onStatus(status: Int) {
            Log.i(TAG, "onStatus(): $status")
            showResult(status)
        }
    }

    init {
        tmsService.setCallback(tmsServiceCallback)
    }
    // Install an Android APK
    fun installApk() {
        background {
            val result = tmsService.installApk("HelloWorldApp.apk")
            showResult(result)
        }
    }

    // Uninstall an Android APK
    fun unInstallApk() {
        background {
            val result = tmsService.unInstallApk("com.verifone.helloworldapp")
            showResult(result)
        }
    }

    fun installAndroidOtaPackage() {
        background {
            val fileName = "ota_update_no_sig.zip" // dummy zip file
            val result = tmsService.installAndroidOtaPackage(fileName)
            showResult(result)
        }
    }

    // Install a package that contains multiple parts - params, APK, Engage OTA, Engage pkg, and Android OTA
    fun installSuperPackage() {
        background {
            val fileName = "initial_config_setup.zip"
            val result = tmsService.installSuperPackage(fileName)
            showResult(result)
        }
    }
    fun installVrkPayload() {
        background {
            val fileName = "VRKv2-neo-TDES-DUKPT1.json"
            val result = tmsService.installVRKPayloadPackage(fileName)
            showResult(result)
        }
    }

    fun fetchLastRecoveryStatus() {
        background {
            val status = tmsService.fetchLastRecoveryStatus()
            showToastMessage(status)
        }
    }

    private fun showResult(status: Int) {
        val message: String =
            when (status) {
                UpdateStatus.STATUS_SUCCESS, UpdateStatus.STATUS_SUCCESS_REBOOTING -> {
                    "Result: successful"
                }
                UpdateStatus.STATUS_PENDING -> {
                    "Result: pending"
                }
                else -> {
                    "Result: failed ($status)"
                }
            }
        _text.postValue(message)
    }


}