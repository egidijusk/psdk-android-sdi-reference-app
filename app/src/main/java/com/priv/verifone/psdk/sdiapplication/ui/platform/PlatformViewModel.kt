/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.ui.platform

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.priv.verifone.psdk.sdiapplication.viewmodel.BaseViewModel


// This is responsible for platform operations
class PlatformViewModel(private val app: Application) : BaseViewModel(app) {

    companion object {
        private const val TAG = "PlatformViewModel"
    }

    var textMode = MutableLiveData("")

    // Sets current existing mode on UI
    fun setCurrentMode() {
        val currentMode = Settings.Global.getInt(app.contentResolver, "device_operating_mode")
        Log.d(TAG, "currentMode : $currentMode")
        if (currentMode == 3) textMode.postValue("Operating Mode: Kiosk Mode") else textMode.postValue(
            "Operating Mode: Standard Mode"
        )
    }

    // Switches the current mode to standard mode
    fun useStandardMode() {
        Settings.Global.putInt(app.contentResolver, "device_operating_mode", 1)
        val intent = Intent("com.verifone.DEVICE_OPERATING_MODE")
        app.sendBroadcast(intent)
        textMode.postValue("Standard Mode")
    }

    // Switches the current mode to kiosk mode
    fun useKioskMode() {
        Settings.Global.putInt(app.contentResolver, "device_operating_mode", 3)
        val intent = Intent("com.verifone.DEVICE_OPERATING_MODE")
        app.sendBroadcast(intent)
        textMode.postValue("Kiosk Mode")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun setupWifi() {
        background {
            val connector = WiFiConnector()
            connector.connectToWiFi("Test", "12345678", app)
            // Make sure to unregister in an appropriate lifecycle method:
            // wiFiConnector.unregisterNetworkCallback(this);
        }
    }

    private val brightness = MutableLiveData<Int>()

    fun getBrightness(): MutableLiveData<Int> {
        return brightness
    }

    private fun fetchSystemBrightness(): Int {
        val resolver: ContentResolver = app.contentResolver
        return try {
            Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: SettingNotFoundException) {
            e.printStackTrace()
            0
        }
    }

    fun setBrightness(value: Int) {
        background {
            val resolver: ContentResolver = app.contentResolver
            Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, value)
            brightness.postValue(value) // Update LiveData to reflect change
        }
    }

    // Bluetooth

    private val isBluetoothEnabled = MutableLiveData<Boolean>()
    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    fun getBluetoothEnabled(): LiveData<Boolean?>? {
        return isBluetoothEnabled
    }

    private fun updateBluetoothState() {
        isBluetoothEnabled.postValue(bluetoothAdapter.isEnabled)
    }

    fun toggleBluetooth(enable: Boolean) {
        background {
            if (isBluetoothEnabled.value != enable) {
                bluetoothAdapter.let {
                    if (enable && !it.isEnabled) {
                        it.enable()
                    } else if (!enable && it.isEnabled) {
                        it.disable()
                    }
                    isBluetoothEnabled.postValue(enable)
                }
            }
        }
    }

    init {
        updateBluetoothState()
        brightness.postValue(fetchSystemBrightness())
    }
}