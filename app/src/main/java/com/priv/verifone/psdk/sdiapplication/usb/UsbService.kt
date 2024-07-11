/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.usb

import android.os.RemoteException
import android.util.Log
import com.verifone.usbconnman.*
import com.verifone.usbconnman.Constants.OperationStatus
import com.verifone.usbconnman.Constants.SerialInterface
import java.nio.charset.StandardCharsets

class UsbService {

    companion object {
        private const val TAG = "UsbService"
        private const val BAUD_RATE = 115200
        private const val DATA_BITS = 8
        private const val TIMEOUT_MS = 5000
        private const val RECEIVE_DATA_LEN = 8

    }

    private var serialConnection: SerialConnection? = null
    private var usbconnman : UsbConnMan? = null
    private val PARITY = SerialInterface.PARITY_NONE
    private val STOP_BITS = SerialInterface.STOP_BITS_1
    private val written = IntArray(1)
    private var callback: UsbEventListener? = null

    fun setCallback(listener: UsbEventListener){
        callback = listener
    }

    fun connect(SERIAL: Constants.SerialDeviceNumber) { //SerialDeviceNumber.SERIAL_8
        try {
            // 1- Get UsbConnMan Instance
            usbconnman = UsbConnMan.create()
            var status = usbconnman?.setListener(listener)
            // 2- Register listener in the usbconnman service (Remember to remove when finished)
            Log.d(TAG, "Register listener: ${status?.name}")

            // 3- Create configurations, can add one or more serial ports (Other com types also)
            val connections: Array<ConnectionType> = UsbConnMan.ConfigurationBuilder()
                .addSerial(SERIAL)
                .build()
            status = usbconnman?.setConfiguration(connections)
            // 4- Set created configuration in UsbConnMan service
            Log.d(TAG, "Set Configuration: ${status?.name}")

            // 5- Request serial port objects for the needed ports
            serialConnection = usbconnman?.requestSerial(SERIAL)

            // 6- Open Serial ports via serial port objects (Remember to close when finished)
            status = serialConnection?.open()
            Log.d(TAG, "Open Serial Port: ${status?.name}")


            // 7- Configure Serial ports with needed configurations
            status = serialConnection?.configure(
                BAUD_RATE,
                DATA_BITS,
                PARITY,
                STOP_BITS
            )
            Log.d(TAG, "Configure SERIAL_PORT: ${status?.name}")

            // 8- Give some time for the connection to be established
            Thread.sleep(300)

            // 9- Init receiving async (Data will be received in onSerialReadCompleted in the listener below)
            status = serialConnection?.receive(RECEIVE_DATA_LEN, TIMEOUT_MS)
            Log.d(TAG, "Read from SERIAL_PORT: ${status?.name}")

        } catch (e: Throwable) {
            Log.e(TAG, "Init FAILED: ", e)
        }

    }

    fun disconnect() {
        serialConnection?.close()
        usbconnman?.removeListener()
    }

    fun send(data: String) {
        serialConnection!!.send((data + "\n\r").toByteArray(), written)
    }

    var listener: IConnectionListener = object : IConnectionListener.Stub() {
        override fun onStatus(connId: Int, inStatus: Int) {
            val status = Constants.ConnectionStatus.values()[inStatus]
            callback?.onStatus(connId, status)
        }

        override fun onError(connId: Int, errorCode: Int) {
            val status = OperationStatus.values()[errorCode]
            callback?.onError(connId, status)
        }

        override fun onSerialReadCompleted(connId: Int, bytes: ByteArray, errorCode: Int) {
            try {
                // If there is data received, read it
                if (bytes.isNotEmpty()) {
                    val string_bytes = String(bytes, StandardCharsets.UTF_8)
                    Log.d(TAG, "RECEIVED <<< $string_bytes")
                    callback?.onDataReceived(bytes)
                }
                // Re-register receiver for next data
                val status  = serialConnection!!.receive(RECEIVE_DATA_LEN, TIMEOUT_MS)
                Log.d(TAG, "receive: ${status.name}")

            } catch (e: RemoteException) {
                Log.d(TAG, "Failed to re-register receive for COM Port")
                // send an onError callback ??? , well its upto the application this
                //  implementation is just an example
            }
        }
    }
}