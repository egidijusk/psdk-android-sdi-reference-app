/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.scanner

import android.util.Log
import com.verifone.payment_sdk.PaymentSdk
import com.verifone.payment_sdk.ScannerConfiguration.ATTRIBUTE_BARCODE
import com.verifone.payment_sdk.ScannerConfiguration.ATTRIBUTE_BARCODE_FORMAT
import com.verifone.payment_sdk.ScannerConfiguration.STATUS_BARCODE_DETECTED
import com.verifone.payment_sdk.ScannerListener

class BarcodeScanner(private val paymentSdk: PaymentSdk, callback: ScannerCallback) {

    companion object {
        const val TAG = "BarcodeScanner"
    }

    private val scannerListener = ScannerListener { status, attributes ->
        var barcode: String? = ""
        var barcodeType: String? = ""
        var message: String? = ""
        if (status === STATUS_BARCODE_DETECTED) {
            if (attributes.containsKey(ATTRIBUTE_BARCODE)) {
                barcode = attributes[ATTRIBUTE_BARCODE] as String?
            }
            if (attributes.containsKey(ATTRIBUTE_BARCODE_FORMAT)) {
                barcodeType = attributes[ATTRIBUTE_BARCODE_FORMAT] as String?
            }
            message = "Scanned Data : $barcode, Type : $barcodeType"
            Log.d(TAG, "Scanner status :$status, $message")
            callback.onSuccess(barcode!!)
        } else {
            message = "Scanner status : $status"
            Log.d(TAG, message)
            callback.onError(status)
        }
    }

    fun scanBarcode(scanningAttributes: HashMap<String, Any>) {
        paymentSdk.initScanListener(scannerListener) // Set the listener to receive the scanned data event
        paymentSdk.startBarcodeScanner(scanningAttributes) // Start the scanning based on provided attributes
    }

    fun stopBarcodeScanning() {
        paymentSdk.stopBarcodeScanner()
    }
}