package com.verifone.psdk.sdiapplication.sdi.config

import android.content.Context
import com.verifone.payment_sdk.*


class Config(private val context: Context, private val sdk: PaymentSdk) {

    //private val ctConfig = Gson().fromJson(getDataFromAssets(context, "config/emvct.json"), EmvContactConfig::class.java)

    private val ctConfig = CtConfig(context, sdk)
    private val ctlsConfig =  CtlsConfig(context, sdk)

    fun setContactConfiguration(): SdiResultCode {
        return ctConfig.setContactConfiguration()
    }

    /*
    * Following APis are for configuring the contactless kernel
    *
    * */

    fun setCtlsConfiguration(): SdiResultCode {
        return ctlsConfig.setCtlsConfiguration()
    }

    fun getCtTagsToFetch(): List<String> {
        return ctConfig.getTagsToFetch()
    }

    fun getCtlsTagsToFetch(): List<String> {
        return ctlsConfig.getTagsToFetch()
    }

    fun logCtConfiguration() {
        ctConfig.logConfiguration()
    }

    fun logCtlsConfiguration() {
        ctlsConfig.logConfiguration()
    }

    companion object {
        private const val TAG = "EMVConfig"
    }
}