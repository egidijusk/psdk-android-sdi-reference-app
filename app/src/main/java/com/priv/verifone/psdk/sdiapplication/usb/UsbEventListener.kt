package com.priv.verifone.psdk.sdiapplication.usb

import com.verifone.usbconnman.Constants

interface UsbEventListener {
    fun onDataReceived(data: ByteArray)
    fun onError(connId: Int, status: Constants.OperationStatus)
    fun onStatus(connId: Int, status: Constants.ConnectionStatus)
}