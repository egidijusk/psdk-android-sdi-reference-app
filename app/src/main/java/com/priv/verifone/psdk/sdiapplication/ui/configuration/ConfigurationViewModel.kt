/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.ui.configuration

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.priv.verifone.psdk.sdiapplication.PSDKContext
import com.priv.verifone.psdk.sdiapplication.sdi.config.Config
import com.priv.verifone.psdk.sdiapplication.ui.viewmodel.BaseViewModel

class ConfigurationViewModel(application: Application) : BaseViewModel(application) {

    companion object {
        private const val TAG = "ConfigurationViewModel"
    }

    private val _text = MutableLiveData<String>().apply {
        value = "This is EMV Configuration Fragment"
    }
    val text: LiveData<String> = _text
    private var paymentSdk = (application as PSDKContext).paymentSDK
    private val emvConfig = Config(paymentSdk)

    fun setContactConfiguration() {
        background {
            val result = emvConfig.setContactConfiguration()
            Log.d(TAG, " CT config result: ${result.name}")
            _text.postValue(" CT config result: ${result.name}")
        }
    }

    fun setContactlessConfiguration() {
        background {
            val result = emvConfig.setCtlsConfiguration()
            _text.postValue(" Ctls config result: ${result.name}")
        }
    }

    fun setContactConfigThroughTlvAccess() {
        background {
            val result = emvConfig.setContactTlvConfiguration()
            Log.d(TAG, " CT config tlv result: ${result.name}")
            _text.postValue(" CT config tlv result: ${result.name}")
        }
    }

    fun setContactlessConfigThroughTlvAccess() {
        background {
            val result = emvConfig.setCtlsTlvConfiguration()
            Log.d(TAG, " Ctls config tlv result: ${result.name}")
            _text.postValue(" Ctls config tlv result: ${result.name}")
        }
    }
}