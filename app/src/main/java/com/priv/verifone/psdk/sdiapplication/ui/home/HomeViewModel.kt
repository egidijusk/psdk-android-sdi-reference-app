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
import android.content.Intent
import android.provider.Settings
import android.text.Html
import android.text.Spanned
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.priv.verifone.psdk.sdiapplication.PSDKContext
import com.priv.verifone.psdk.sdiapplication.connection.ConnectionCallback
import com.priv.verifone.psdk.sdiapplication.connection.SdiConnection
import com.priv.verifone.psdk.sdiapplication.sdi.system.SdiUtils
import com.priv.verifone.psdk.sdiapplication.ui.viewmodel.BaseViewModel
import com.priv.verifone.psdk.sdiapplication.utils.getDeviceInformation
import com.verifone.persistentloggerlibrary.PersistentLoggerApi

class HomeViewModel(val app: Application) : BaseViewModel(app), ConnectionCallback {

    companion object {
        const val TAG = "HomeViewModel"
    }

    private val paymentSdk = (app as PSDKContext).paymentSDK
    private val sdiConnection = SdiConnection(paymentSdk, this)
    private lateinit var sdiUtils: SdiUtils

    suspend fun connect() {
        _text.postValue("Connecting...")
        val result = sdiConnection.connect()
    }

    fun disconnect() {
        sdiConnection.disconnect()
    }

    fun transferLogs() {
        background {
            val logger = PersistentLoggerApi.getInstance(app.applicationContext)
            logger.transferLogs()
        }
    }

    private val _text = MutableLiveData<String>().apply {
        value = "Press Connect to connect to SDI Server"
    }

    val text: LiveData<String> = _text

    private val _textMode = MutableLiveData<String>().apply {
        value = "Press Connect to connect to SDI Server"
    }

    val textMode: LiveData<String> = _textMode

    private val _info = MutableLiveData<Spanned>().apply {
        value = Html.fromHtml("Device Information", Html.FROM_HTML_MODE_LEGACY)
    }

    val info: LiveData<Spanned> = _info

    private val _keyboardPresent = MutableLiveData<Boolean>().apply {
        value = false
    }

    val keyboardPresent: LiveData<Boolean> = _keyboardPresent
    override fun onConnected() {
        _text.postValue("Connected")
        _info.postValue(getDeviceInformation(paymentSdk))
        sdiUtils = SdiUtils(paymentSdk.sdiManager)
        _keyboardPresent.postValue(sdiUtils.isPhysicalKeyboardPresent())
    }

    override fun onDisconnected() {
        _text.postValue("Disconnected")
    }

    fun setCurrentMode() {
        try {
            val currentMode = Settings.Global.getInt(app.contentResolver, "device_operating_mode")
            Log.d(TAG, "currentMode : $currentMode")

            if (currentMode == 3) _textMode.postValue("Operating Mode: Kiosk Mode") else _textMode.postValue(
                "Operating Mode: Standard Mode"
            )
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    // Switches the current mode to standard mode
    fun useStandardMode() {
        try {
            Settings.Global.putInt(app.contentResolver, "device_operating_mode", 1)
            val intent = Intent("com.verifone.DEVICE_OPERATING_MODE")
            app.sendBroadcast(intent)
            _textMode.postValue("Standard Mode")
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    // Switches the current mode to kiosk mode
    fun useKioskMode() {
        try {
            Settings.Global.putInt(app.contentResolver, "device_operating_mode", 3)
            val intent = Intent("com.verifone.DEVICE_OPERATING_MODE")
            app.sendBroadcast(intent)
            _textMode.postValue("Kiosk Mode")
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    fun toggleKeyboardBacklight() {
        if (sdiUtils.isPhysicalKeyboardPresent()) {
            sdiUtils.toggleKeyboardBacklight()
        }
    }

    fun setDateTime(time: String) {
        sdiUtils.setAndroidTime(time)
    }
}