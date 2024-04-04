/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.ui.operatingmode

import android.app.Application
import android.content.Intent
import android.provider.Settings;
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.priv.verifone.psdk.sdiapplication.viewmodel.BaseViewModel

// This is responsible for switching the operating modes as per UI request call
class OperatingModeViewModel(private val app: Application) : BaseViewModel(app) {

    companion object {
        private const val TAG = "OperatingModeViewModel"
    }

    var textMode = MutableLiveData("")

    // Sets current existing mode on UI
    fun setCurrentMode() {
        val currentMode = Settings.Global.getInt(app.contentResolver, "device_operating_mode")
        Log.d(TAG, "currentMode : $currentMode")
        if (currentMode == 3) textMode.postValue("Kiosk Mode") else textMode.postValue("Standard Mode")
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
}