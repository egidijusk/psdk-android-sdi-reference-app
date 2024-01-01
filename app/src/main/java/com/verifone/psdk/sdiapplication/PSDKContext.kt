/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.verifone.psdk.sdiapplication

import android.app.Application
import com.verifone.payment_sdk.PaymentSdk
import com.verifone.payment_sdk.SdiManager
import com.verifone.psdk.sdiapplication.sdi.config.Config

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