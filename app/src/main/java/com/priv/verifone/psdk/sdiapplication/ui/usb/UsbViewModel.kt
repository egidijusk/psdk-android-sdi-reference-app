/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.ui.usb

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.priv.verifone.psdk.sdiapplication.usb.UsbEventListener
import com.priv.verifone.psdk.sdiapplication.usb.UsbService
import com.priv.verifone.psdk.sdiapplication.viewmodel.BaseViewModel
import com.verifone.usbconnman.Constants
import com.verifone.usbconnman.Constants.SerialDeviceNumber
import java.nio.charset.StandardCharsets


// This is responsible for usb operations
class UsbViewModel(private val app: Application) : BaseViewModel(app), UsbEventListener {

    companion object {
        private const val TAG = "UsbViewModel"
    }

    var operationStatus = MutableLiveData<String>()
    var receivedData = MutableLiveData<String>()
    private val usbService = UsbService()

    fun connect () {
        usbService.connect(SerialDeviceNumber.SERIAL_8)
    }

    fun send () {
        usbService.send("Test data")
    }

    fun disconnect() {
        usbService.disconnect()
    }

    init {
        usbService.setCallback(this)
    }

    override fun onDataReceived(data: ByteArray) {

        receivedData.postValue("Data Received: ${String(data, StandardCharsets.UTF_8)}")
    }

    override fun onError(connId: Int, status: Constants.OperationStatus) {
        operationStatus.postValue("Error Status: ${status.name}")
    }

    override fun onStatus(connId: Int, status: Constants.ConnectionStatus) {
        operationStatus.postValue("Status: ${status.name}")
    }
}