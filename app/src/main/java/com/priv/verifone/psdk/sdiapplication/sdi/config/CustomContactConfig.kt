package com.priv.verifone.psdk.sdiapplication.sdi.config

import android.util.Log
import com.priv.verifone.psdk.sdiapplication.PSDKContext
import com.priv.verifone.psdk.sdiapplication.sdi.config.model.EmvContactConfig
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.hexStringToByteArray
import com.verifone.payment_sdk.PaymentSdk
import com.verifone.payment_sdk.SdiCurrency
import com.verifone.payment_sdk.SdiEmvConf
import com.verifone.payment_sdk.SdiEmvCtAppFlowCapabilities
import java.util.EnumSet

class CustomContactConfig(private val sdk: PaymentSdk) : CtConfig(sdk) {
    companion object {
        const val TAG = "CustomContactConfig"
    }
    private val ctConfig = PSDKContext.ctConfigData

    private fun getIsoCountryCode(emvCode: String): String {
        val countryCodeMap = mapOf(
            "0840" to "USD",
            "0124" to "CAD",
            "0036" to "AUD"
        )

        return countryCodeMap[emvCode] ?: "Invalid code"
    }

    override fun getCtTerminalConfig(): SdiEmvConf {
        Log.d(TAG, "Contact Terminal Config")
        val sdiEmvConf = SdiEmvConf.create();

        return sdiEmvConf
    }

    override fun getCtApplicationConfig(): ArrayList<SdiEmvConf> {
        Log.d(TAG, "Contact AID Config")
        val sdiAidConfList = ArrayList<SdiEmvConf>()
        for (application in ctConfig.applications) {
            val sdiEmvConf = SdiEmvConf.create();

            sdiAidConfList.add(sdiEmvConf)
        }
        return sdiAidConfList
    }

    override fun getCtCapks(): List<EmvContactConfig.Capk> {
        Log.d(TAG, "Contactless AID Config")
        val capks = ArrayList<EmvContactConfig.Capk>()
        for (capk in ctConfig.capks) {
            val obj = EmvContactConfig.Capk(certificateRevocationListDF0E = "",
                exponentDF0D = capk.exponentDF0D,
                rid = capk.rid,
                hashDF0C = capk.hashDF0C,
                indexDF09 = capk.indexDF09,
                keyDF0B = capk.keyDF0B)
            capks.add(obj)
        }
        return capks
    }
}