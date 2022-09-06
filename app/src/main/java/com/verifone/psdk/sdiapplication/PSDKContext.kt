package com.verifone.psdk.sdiapplication

import android.app.Application
import android.util.Log
import com.verifone.psdk.sdiapplication.sdi.config.Config
import com.verifone.payment_sdk.PaymentSdk
import com.verifone.payment_sdk.SdiManager


class PSDKContext : Application() {
    lateinit var paymentSDK: PaymentSdk
    var sdiManager: SdiManager? = null
    lateinit var config : Config
    companion object {
        lateinit var instance: PSDKContext
    }
    override fun onCreate() {
        super.onCreate()
        paymentSDK = PaymentSdk.create(this)
        instance = this
        config = Config(this, paymentSDK)
    }
}