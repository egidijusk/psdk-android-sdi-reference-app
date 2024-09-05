/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.verifone.payment_sdk.PaymentSdk
import com.verifone.payment_sdk.SdiManager
import com.priv.verifone.psdk.sdiapplication.sdi.config.Config
import com.priv.verifone.psdk.sdiapplication.sdi.config.model.EmvContactConfig
import com.priv.verifone.psdk.sdiapplication.sdi.config.model.EmvCtlsConfig
import com.priv.verifone.psdk.sdiapplication.sdi.config.tlvmodel.EmvContactConfigTlv
import com.priv.verifone.psdk.sdiapplication.sdi.config.tlvmodel.EmvCtlsConfigTlv
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils
import java.lang.Exception

class PSDKContext : Application() {

    lateinit var paymentSDK: PaymentSdk
    //lateinit var sdiManager: SdiManager
    //lateinit var config : Config

    companion object {
        const val ON_DEVICE_MODE = false
        lateinit var instance: PSDKContext
        lateinit var ctConfigData: EmvContactConfig
        lateinit var ctlsConfigData: EmvCtlsConfig
        lateinit var ctConfigTlvData: EmvContactConfigTlv
        lateinit var ctlsConfigTlvData: EmvCtlsConfigTlv
    }

    override fun onCreate() {
        super.onCreate()
        paymentSDK = PaymentSdk.create(this)
        instance = this
        ctConfigData = Gson().fromJson(
            Utils.getDataFromAssets(this, "config/emvct.json"),
            EmvContactConfig::class.java
        )
        ctlsConfigData = Gson().fromJson(
            Utils.getDataFromAssets(this, "config/emvctls.json"),
            EmvCtlsConfig::class.java
        )
        ctConfigTlvData = Gson().fromJson(
            Utils.getDataFromAssets(this, "config/tlvemvct.json"),
            EmvContactConfigTlv::class.java
        )
        ctlsConfigTlvData = Gson().fromJson(
            Utils.getDataFromAssets(this, "config/tlvemvctls.json"),
            EmvCtlsConfigTlv::class.java
        )
        // config = Config(this, paymentSDK)
    }
}