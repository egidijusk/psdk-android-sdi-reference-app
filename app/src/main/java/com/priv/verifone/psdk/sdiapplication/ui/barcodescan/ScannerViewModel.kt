/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.ui.barcodescan

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.priv.verifone.psdk.sdiapplication.PSDKContext
import com.priv.verifone.psdk.sdiapplication.scanner.BarcodeScanner
import com.priv.verifone.psdk.sdiapplication.scanner.ScannerCallback
import com.priv.verifone.psdk.sdiapplication.ui.viewmodel.BaseViewModel

class ScannerViewModel(app: Application) : BaseViewModel(app), ScannerCallback {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Scanner Fragment"
    }
    val text: LiveData<String> = _text
    private val paymentSdk = (app as PSDKContext).paymentSDK
    private val barcodeScanner = BarcodeScanner(paymentSdk, this)

    fun scanBarCode(scanningAttributes: HashMap<String, Any>) {
        background {
            barcodeScanner.scanBarcode(scanningAttributes)
        }
    }

    override fun onSuccess(barcodeData: String) {
        _text.postValue(barcodeData)
        barcodeScanner.stopBarcodeScanning()
    }

    override fun onError(status: String) {
        _text.postValue(status)
    }
}