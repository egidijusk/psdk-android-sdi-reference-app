package com.verifone.psdk.sdiapplication.sdi.config

import android.content.Context
import android.util.Log
import com.verifone.psdk.sdiapplication.sdi.card.SdiContact
import com.verifone.psdk.sdiapplication.sdi.config.model.EmvContactConfig
import com.verifone.psdk.sdiapplication.sdi.utils.Utils
import com.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.hexStringToByteArray
import com.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.toHexString
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.verifone.payment_sdk.*
import java.util.*
import kotlin.collections.ArrayList

class CtConfig(private val context: Context, private val sdk: PaymentSdk) {

    private val ctConfig = Gson().fromJson(
        Utils.getDataFromAssets(context, "config/emvct.json"),
        EmvContactConfig::class.java
    )

    fun setContactConfiguration(): SdiResultCode {

        var result = initialize()
        if (result != SdiResultCode.OK)
            return result!!
        result = setCtTerminalConfiguration()
        if (result != SdiResultCode.OK)
            return result!!
        result = setCtAidConfiguration()
        if (result != SdiResultCode.OK)
            return result
        result = setCtCapkConfiguration()
        exit()
        return result
    }

    private fun initialize():SdiResultCode {
        Log.d(TAG, "Init CT Framework Command (39 00) ")
        val initOptions = SdiEmvOptions.create()
        initOptions.setOption(SdiEmvOption.TRACE, true)
        //initOptions.setOption(SdiEmvOption.CONFIG_MODE, true)
        initOptions.setOption(SdiEmvOption.TRACE_ADK_LOG, true)
        val result = sdk.sdiManager?.emvCt?.initFramework(60, initOptions)
        Log.d(TAG, "Command result: ${result?.name}")
        return result!!
    }

    private fun exit() : SdiResultCode {
        Log.d(TAG, "Exit CT Framework Command (39 00)")
        val result = sdk.sdiManager?.emvCt?.exitFramework(null)
        Log.d(TAG, "Command result: ${result?.name}")
        return result!!
    }


    private fun setCtTerminalConfiguration(): SdiResultCode {
        val termConfig = getCtTerminalConfig()
        Log.d(TAG, " CT Terminal config Command (39-01)")
        val result = sdk.sdiManager?.emvCt?.setTermData(termConfig)
        Log.d(TAG, " Command result: ${result?.name}")
        return result!!
    }

    private fun setCtAidConfiguration(): SdiResultCode {
        val aidConfigList = getCtApplicationConfig()
        for (aidConfig in aidConfigList) {
            Log.d(TAG, " CT AID Config Command (39-02) : ${aidConfig.aid.toHexString()}")
            val result = sdk.sdiManager?.emvCt?.setAppData(aidConfig.aid, aidConfig)
            Log.d(TAG, " Command result: ${result?.name}")
            if (result != SdiResultCode.OK) {
                return result!!
            }
        }
        return SdiResultCode.OK
    }

    private fun setCtCapkConfiguration(): SdiResultCode {
        val capks = getCtCapks()
        for (capk in capks) {
            Log.d(TAG, " CT Capk Config Command (39-03) : ${capk.rid}")
            val result = sdk.sdiManager?.emvCt?.setCAPKey(
                capk.rid.hexStringToByteArray(),
                capk.indexDF09.toShort(radix = 16),
                capk.keyDF0B.hexStringToByteArray(),
                capk.exponentDF0D.toShort(radix = 16),
                capk.hashDF0C.hexStringToByteArray(),
                capk.certificateRevocationListDF0E.hexStringToByteArray()
            )
            Log.d(TAG, "Command result: ${result?.name}")
            if (result != SdiResultCode.OK) {
                return result!!
            }
        }
        return SdiResultCode.OK
    }

    private fun getCtTerminalConfig(): SdiEmvConf {

        val sdiEmvConf = SdiEmvConf.create();
        sdiEmvConf.terminalType = ctConfig.terminal.terminalType.toShort(radix = 16)
        sdiEmvConf.terminalCountryCode = ctConfig.terminal.terminalCountryCode.toInt(radix = 16)
        sdiEmvConf.terminalCapabilities =
            ctConfig.terminal.terminalCapabilities.hexStringToByteArray()
        sdiEmvConf.additionalCapabilities =
            ctConfig.terminal.additionalTerminalCapabilities.hexStringToByteArray()
        sdiEmvConf.transactionCurrency = SdiCurrency.valueOf(ctConfig.terminal.transactionCurrency)
        sdiEmvConf.transactionCurrencyExp = ctConfig.terminal.transactionCurrencyExp.toShort();
        return sdiEmvConf
    }


    private fun getCtApplicationConfig(): ArrayList<SdiEmvConf> {

        val sdiAidConfList = ArrayList<SdiEmvConf>()
        for (application in ctConfig.applications) {
            val sdiEmvConf = SdiEmvConf.create();
            val appVersionNumber: ArrayList<Int> = ArrayList()
            appVersionNumber.add(0x0000008D)
            sdiEmvConf.aid = application.aid.hexStringToByteArray()
            sdiEmvConf.chipAppVersionNumber =
                arrayListOf(application.appVersionNumber.toInt(radix = 16))
            sdiEmvConf.defaultAppName = application.defaultAppName
            sdiEmvConf.asi = application.asi.toShort(radix = 16)
            sdiEmvConf.merchantCategory = application.merchantCategoryCode.hexStringToByteArray()
            sdiEmvConf.floorLimit = application.floorLimit.toLong(radix = 16)
            sdiEmvConf.securityLimit = application.securityLimit.toLong(16)
            sdiEmvConf.capabilitiesBelowLimit =
                application.belowLimitTerminalCapabilities.hexStringToByteArray()
            sdiEmvConf.threshold = application.threshold.toLong(radix = 16)
            sdiEmvConf.riskManagementTargetPercentage = application.targetPercentage.toInt()
            sdiEmvConf.riskManagementMaxTargetPercentage = application.maxTargetPercentage.toInt()
            sdiEmvConf.tacDenial = application.tacDenial.hexStringToByteArray()
            sdiEmvConf.tacOnline = application.tacOnline.hexStringToByteArray()
            sdiEmvConf.tacDefault = application.tacDefault.hexStringToByteArray()
            sdiEmvConf.emvApplication = 0x01.toShort()
            sdiEmvConf.defaultTDOL = byteArrayOf()
            sdiEmvConf.defaultDDOL = application.defaultDDOL.hexStringToByteArray()
            sdiEmvConf.cdaProcessing = application.cdaProcessing.toShort(radix = 16)
            sdiEmvConf.offlineOnly = false
            sdiEmvConf.aipNoCVM = application.aipCvmNotSupported.toShort(radix = 16)
            sdiEmvConf.posEntryMode = application.posEntryMode.toShort(radix = 16)
            sdiEmvConf.ctAppFlowCapabilities =
                EnumSet.of(SdiEmvCtAppFlowCapabilities.CASHBACK_SUPPORT, SdiEmvCtAppFlowCapabilities.DOMESTIC_CHECK)
            sdiEmvConf.terminalCapabilities = application.appTermCap.hexStringToByteArray()
            sdiEmvConf.terminalCountryCode = application.countryCode.toInt(radix = 16)
            sdiEmvConf.additionalCapabilities =
                application.appTermAddCap.hexStringToByteArray()
            sdiEmvConf.terminalType = application.appTerminalType.toShort(radix = 16)
            sdiAidConfList.add(sdiEmvConf)
        }
        return sdiAidConfList
    }

    private fun getCtCapks(): List<EmvContactConfig.Capk> {
        return ctConfig.capks
    }

    fun getTagsToFetch(): List<String> {
        return ctConfig.fetchTags
    }

    fun logConfiguration() {
        if (initialize()  == SdiResultCode.OK) {
            val terminalConfig = logTerminalConfig()
            if (terminalConfig == null) {
                Log.d(TAG, "~~~~~~~~~~~~~~~~Contact Configuration Start~~~~~~~~~~~~~~~~~~")
                Log.d(TAG, "Invalid Terminal config")
                Log.d(TAG, "~~~~~~~~~~~~~~~~Contact Configuration End~~~~~~~~~~~~~~~~~~")
            }
            val aidConfig = logAidConfig()
            val capkConfig = logCapkConfig()
            val contactConfig = EmvContactConfig(
                applications = aidConfig,
                terminal = terminalConfig!!,
                capks = capkConfig,
                fetchTags = listOf(),
            )
            Log.d(TAG, "~~~~~~~~~~~~~~~~Contact Configuration Start~~~~~~~~~~~~~~~~~~")
            Log.d(TAG, GsonBuilder().setPrettyPrinting().create().toJson(contactConfig.terminal))
            for (application in contactConfig.applications)
                Log.d(TAG, GsonBuilder().setPrettyPrinting().create().toJson(application))
            for (capk in contactConfig.capks)
                Log.d(TAG, GsonBuilder().setPrettyPrinting().create().toJson(capk))
            Log.d(TAG, "~~~~~~~~~~~~~~~~Contact Configuration End~~~~~~~~~~~~~~~~~~")
            exit()
        }
    }

    private fun logCapkConfig(): List<EmvContactConfig.Capk> {
        val result = sdk.sdiManager.emvCt.capKeys
        val capkList = ArrayList<EmvContactConfig.Capk>()
        if (result.result == SdiResultCode.OK) {
            val keys = result.keys
            for (key in keys) {
                val capk = EmvContactConfig.Capk(
                    exponentDF0D = "",
                    rid = key.rid.toHexString(),
                    indexDF09 = key.index.toString(radix = 16),
                    hashDF0C = "",
                    keyDF0B = "",
                    certificateRevocationListDF0E = ""
                )
                capkList.add(capk)
            }
        }
        return capkList.toList()
    }

    private fun logAidConfig(): List<EmvContactConfig.Application> {
        val aidList = arrayListOf<String>(
            "A0000000031010",
            "A0000000032010", "A0000000041010", "A0000000043060"
        )
        val applications = ArrayList<EmvContactConfig.Application>()
        for (aid in aidList) {
            val result = sdk.sdiManager.emvCt.getAppDataByAid(aid.hexStringToByteArray())
            try {
                if (result.result == SdiResultCode.OK) {
                    val sdiEmvConf = result.emv
                    val application = EmvContactConfig.Application(
                        aid = sdiEmvConf.aid.toHexString(),
                        appVersionNumber = sdiEmvConf.chipAppVersionNumber.toString(),
                        defaultAppName = sdiEmvConf.defaultAppName,
                        asi = sdiEmvConf.asi.toString(radix = 16),
                        merchantCategoryCode = sdiEmvConf.merchantCategory.toHexString(),
                        floorLimit = sdiEmvConf.floorLimit.toString(radix = 16),
                        securityLimit = sdiEmvConf.securityLimit.toString(radix = 16),
                        belowLimitTerminalCapabilities = sdiEmvConf.capabilitiesBelowLimit.toHexString(),
                        threshold = sdiEmvConf.threshold.toString(radix = 16),
                        targetPercentage = sdiEmvConf.riskManagementTargetPercentage?.toString(radix = 16)?:"",
                        maxTargetPercentage = sdiEmvConf.riskManagementMaxTargetPercentage?.toString(radix = 16)?:"",
                        tacDenial = sdiEmvConf.tacDenial.toHexString(),
                        tacOnline = sdiEmvConf.tacOnline.toHexString(),
                        tacDefault = sdiEmvConf.tacDefault.toHexString(),
                        defaultDDOL = sdiEmvConf.defaultDDOL.toHexString(),
                        cdaProcessing = sdiEmvConf.cdaProcessing.toString(radix = 16),
                        aipCvmNotSupported = sdiEmvConf.aipNoCVM.toString(radix = 16),
                        posEntryMode = sdiEmvConf.posEntryMode.toString(radix = 16),
                        appTermCap = sdiEmvConf.terminalCapabilities.toHexString(),
                        countryCode = sdiEmvConf.terminalCountryCode.toString(radix = 16),
                        appTermAddCap = sdiEmvConf.additionalCapabilities.toHexString(),
                        appTerminalType = sdiEmvConf.terminalType.toString(radix = 16),
                        termIdent = sdiEmvConf.terminalID.toHexString(),

                        merchantIdent = sdiEmvConf.merchantID,
                        //Following fields are hardcoded int test app
                        appFlowCap = "",
                        acBeforeAfter = "",
                        additionalVersionNumbers = ""
                        /*
                            sdiEmvConf.offlineOnly = false
                            sdiEmvConf.ctAppFlowCapabilities =
                            EnumSet.of(SdiEmvCtAppFlowCapabilities.CASHBACK_SUPPORT)
                        */
                    )
                    applications.add(application)
                } else {
                    Log.d(TAG, "AID retrieval failed ${result.result.name} ")
                }
            } catch (e:Exception) {
                e.printStackTrace()
            }
        }

        return applications.toList()
    }

    private fun logTerminalConfig(): EmvContactConfig.Terminal? {
        val result = sdk.sdiManager.emvCt.termData
        val sdiEmvConf = result.emv
        try {
            return EmvContactConfig.Terminal(
                terminalType = sdiEmvConf.terminalType.toString(radix = 16),
                terminalCapabilities = sdiEmvConf.terminalCapabilities.toHexString(),
                terminalCountryCode = sdiEmvConf.terminalCountryCode.toString(radix = 16),
                additionalTerminalCapabilities = sdiEmvConf.additionalCapabilities.toHexString(),
                transactionCurrency = sdiEmvConf.transactionCurrency.name,
                transactionCurrencyExp = sdiEmvConf.transactionCurrencyExp.toString(radix = 16)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    companion object {
        private const val TAG = "EMVCTConfig"
    }
}