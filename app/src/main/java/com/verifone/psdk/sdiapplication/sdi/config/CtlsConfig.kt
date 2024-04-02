/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.verifone.psdk.sdiapplication.sdi.config

import android.content.Context
import android.util.Log
import com.verifone.psdk.sdiapplication.sdi.config.model.EmvCtlsConfig
import com.verifone.psdk.sdiapplication.sdi.utils.Utils
import com.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.hexStringToByteArray
import com.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.toHexString
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.verifone.payment_sdk.*
import java.util.*
import kotlin.collections.ArrayList

// This is mapped to emv contactless configuration and respective operations
class CtlsConfig(private val context: Context, private val sdk: PaymentSdk) {

    private val ctlsConfig = Gson().fromJson(
        Utils.getDataFromAssets(context, "config/emvctls.json"),
        EmvCtlsConfig::class.java
    )

    /*
     * Following APis are for configuring the contactless kernel
     *
     * */
    private fun initialize(): SdiResultCode {
        Log.d(TAG, "Ctls Init Framework Command (40-00)")
        val initOptions = SdiEmvOptions.create()
        initOptions.setOption(SdiEmvOption.TRACE, true)
        //initOptions.setOption(SdiEmvOption.CONFIG_MODE, true)
        initOptions.setOption(SdiEmvOption.TRACE_ADK_LOG, true)
        val result = sdk.sdiManager?.emvCtls?.initFramework(60, initOptions)
        Log.d(TAG, "Command result: ${result?.name}")
        return result!!
    }

    private fun exit() {
        Log.d(
            TAG,
            "Exit CTLS Framework result: ${sdk.sdiManager?.emvCtls?.exitFramework(null)?.name}"
        )
    }

    fun setCtlsConfiguration(): SdiResultCode {
        var result = initialize()

        if (result != SdiResultCode.OK)
            return result!!
        result = setCtlsTerminalConfiguration()
        if (result != SdiResultCode.OK)
            return result!!
        result = setCtlsAidConfiguration()
        if (result != SdiResultCode.OK)
            return result
        result = setCtlsCapkConfiguration()
        exit()
        return result
    }

    private fun setCtlsTerminalConfiguration(): SdiResultCode {
        val termConfig = getCtlsTerminalConfig()
        val result = sdk.sdiManager?.emvCtls?.setTermData(termConfig)
        Log.d(TAG, " Ctls Terminal config result: ${result?.name}")
        return result!!
    }

    private fun setCtlsAidConfiguration(): SdiResultCode {
        Log.d(TAG, "Ctls AID Config ")
        val aidConfigList = getCtlsApplicationConfig()
        for (aidConfig in aidConfigList) {
            Log.d(
                TAG,
                " Ctls AID Config : ${aidConfig.aid.toHexString()} kernel Id: ${aidConfig.kernelID}"
            )
            val result =
                sdk.sdiManager?.emvCtls?.setAppData(aidConfig.kernelID, aidConfig.aid, aidConfig)
            if (result != SdiResultCode.OK) {
                Log.d(TAG, " Ctls AID Config result: ${result?.name}")
                return result!!
            }
        }
        return SdiResultCode.OK
    }

    private fun setCtlsCapkConfiguration(): SdiResultCode {
        Log.d(TAG, "Ctls CAPK Config ")
        val capks = getCtlsCapks()

        for (capk in capks) {
            val result = sdk.sdiManager?.emvCtls?.setCapKey(
                capk.rid.hexStringToByteArray(),
                capk.indexDF09.toShort(radix = 16),
                capk.keyDF0B.hexStringToByteArray(),
                capk.exponentDF0D.toShort(radix = 16),
                capk.hashDF0C.hexStringToByteArray(),
                capk.certificateRevocationListDF0E.hexStringToByteArray()
            )
            if (result != SdiResultCode.OK) {
                Log.d(TAG, " Ctls CAPK result: ${result?.name}")
                return result!!
            }
        }
        return SdiResultCode.OK
    }

    private fun getCtlsTerminalConfig(): SdiEmvConf {

        val sdiEmvConf = SdiEmvConf.create();
        sdiEmvConf.terminalType = ctlsConfig.terminal.terminalType.toShort(radix = 16)
        sdiEmvConf.terminalCountryCode = ctlsConfig.terminal.terminalCountryCode.toInt(radix = 16)
        sdiEmvConf.transactionCurrency =
            SdiCurrency.valueOf(ctlsConfig.terminal.transactionCurrency)
        sdiEmvConf.transactionCurrencyExp = ctlsConfig.terminal.transactionCurrencyExp.toShort();
        sdiEmvConf.beepVolume = ctlsConfig.terminal.beepVolume.toInt(16)
        sdiEmvConf.beepFreqAlert = ctlsConfig.terminal.beepFrequencyAlert.toInt(16)
        sdiEmvConf.beepFreqSuccess = ctlsConfig.terminal.beepFrequencySuccess.toInt(16)
        return sdiEmvConf
    }

    private fun getCtlsApplicationConfig(): ArrayList<SdiEmvConf> {
        val sdiAidConfList: ArrayList<SdiEmvConf>
        // Visa
        val visaList = getCtlsVisaApplicationConfig()
        // Mastercard
        val mastercardList = getCtlsMastercardApplicationConfig()
        sdiAidConfList = listOf(mastercardList, visaList).flatten() as ArrayList<SdiEmvConf>

        return sdiAidConfList
    }

    private fun getCtlsVisaApplicationConfig(): ArrayList<SdiEmvConf> {
        val sdiAidConfList = ArrayList<SdiEmvConf>()
        // Visa
        for (application in ctlsConfig.visa.applications) {
            val sdiEmvConf = SdiEmvConf.create();
            // Common parameters for all CTLS
            sdiEmvConf.aid = application.aid.hexStringToByteArray()
            sdiEmvConf.defaultAppName = application.defaultApplicationNameDFAB22
            sdiEmvConf.kernelID = application.kernelID.toLong(radix = 16)
            sdiEmvConf.asi = application.asiDFAB02.toShort(radix = 16)
            val options = SdiEmvSpecialTransactions.create()
            options.fallback = SdiEmvSpecialFallback.SDI_YES
            sdiEmvConf.setSpecialTransactions(options)
            sdiEmvConf.retapFieldOff = application.retapFieldOffDFAB08.toShort(radix = 16)

            // Parameters specific to visa
            sdiEmvConf.terminalID = application.visaAid.termIdent9F1C.hexStringToByteArray()
            sdiEmvConf.terminalCountryCode =
                application.visaAid.terminalCountryCode9F1A.toInt(radix = 16)
            sdiEmvConf.terminalType = application.visaAid.terminalType9F35.toShort(radix = 16)
            sdiEmvConf.transactionQualifier =
                application.visaAid.terminalTransactionQualifier9F66.hexStringToByteArray()
            sdiEmvConf.terminalCapabilities =
                application.visaAid.terminalCapabilities9F33.hexStringToByteArray()
            sdiEmvConf.additionalCapabilities =
                application.visaAid.additionalTerminalCapabilities9F40.hexStringToByteArray()
            sdiEmvConf.chipAppVersionNumber =
                arrayListOf(application.visaAid.versionNumber9F09.toInt(radix = 16))
            sdiEmvConf.merchantCategory =
                application.visaAid.merchantCategoryCode9F15.hexStringToByteArray()
            sdiEmvConf.tecSupport = application.visaAid.tecSupportDFAB30.toShort(radix = 16)
            // TODO figure this out
            sdiEmvConf.ctlsAppFlowCapabilities =
                EnumSet.of(SdiEmvCtlsAppFlowCapabilities.CASHBACK_SUPPORT, SdiEmvCtlsAppFlowCapabilities.START_REMOVAL_DETECTION)

            sdiEmvConf.floorLimit =
                application.visaAid.contactlessFloorLimitDFAB40.toLong(radix = 10)
            sdiEmvConf.ctlsTransactionLimit =
                application.visaAid.contactlessTransactionLimitDFAB41.toLong(radix = 10)
            sdiEmvConf.cvmRequiredLimit =
                application.visaAid.contactlessCVMRequiredLimitDFAB42.toLong(radix = 10)
            sdiAidConfList.add(sdiEmvConf)
        }
        return sdiAidConfList
    }

    private fun getCtlsMastercardApplicationConfig(): ArrayList<SdiEmvConf> {
        val sdiAidConfList = ArrayList<SdiEmvConf>()
        // Mastercard
        for (application in ctlsConfig.mastercard.applications) {
            val sdiEmvConf = SdiEmvConf.create();

            // Common parameters for all CTLS
            sdiEmvConf.aid = application.aid.hexStringToByteArray()
            sdiEmvConf.defaultAppName = application.defaultApplicationNameDFAB22
            sdiEmvConf.kernelID = application.kernelID.toLong(radix = 16)
            sdiEmvConf.asi = application.asiDFAB02.toShort(radix = 16)
            //TODO In the sample app we are not setting app flow capabilities , you can set it according to your
            //sdiEmvConf.ctlsAppFlowCapabilities = EnumSet.of(SdiEmvCtlsAppFlowCapabilities.CASHBACK_SUPPORT)
            val options = SdiEmvSpecialTransactions.create()
            options.fallback = SdiEmvSpecialFallback.SDI_YES
            sdiEmvConf.setSpecialTransactions(options)

            sdiEmvConf.retapFieldOff = application.retapFieldOffDFAB08.toShort(radix = 16)
            // Parameters specific to mastercard
            sdiEmvConf.terminalID = application.masterCardAid.termIdent9F1C.hexStringToByteArray()
            sdiEmvConf.terminalCountryCode =
                application.masterCardAid.terminalCountryCode9F1A.toInt(radix = 16)
            sdiEmvConf.terminalType = application.masterCardAid.terminalType9F35.toShort(radix = 16)

            sdiEmvConf.additionalCapabilities =
                application.masterCardAid.additionalTerminalCapabilities9F40.hexStringToByteArray()
            //sdiEmvConf.chipAppVersionNumber = application.visaAid.versionNumber9F09
            sdiEmvConf.merchantCategory =
                application.masterCardAid.merchantCategoryCode9F15.hexStringToByteArray()
            sdiEmvConf.ctlsAppFlowCapabilities =
                EnumSet.of(SdiEmvCtlsAppFlowCapabilities.CASHBACK_SUPPORT, SdiEmvCtlsAppFlowCapabilities.START_REMOVAL_DETECTION)
            sdiEmvConf.floorLimitMK = application.masterCardAid.floorLimitDF8123.toLong(radix = 10)
            sdiEmvConf.transactionLimitNoCVMOnDevice =
                application.masterCardAid.transactionLimitNoOnDeviceDF8124.toLong(radix = 10)
            sdiEmvConf.transactionLimitCVMOnDevice =
                application.masterCardAid.transactionLimitOnDeviceDF8125.toLong(radix = 10)
            sdiEmvConf.magstripeCVMAboveLimit =
                application.masterCardAid.magstripeCVMAboveLimitDF811E.toShort(radix = 16)
            sdiEmvConf.magstripeCVMBelowLimit =
                application.masterCardAid.magstripeCVMbelowLimitDF812C.toShort(radix = 16)
            sdiEmvConf.chipCVMAboveLimit =
                application.masterCardAid.chipCVMAboveLimitDF8118.toShort(radix = 16)
            sdiEmvConf.chipCVMBelowLimit =
                application.masterCardAid.chipCVMBelowLimitDF8119.toShort(radix = 16)
            sdiEmvConf.securityCapability =
                application.masterCardAid.securityCapabilityDF811F.toShort(radix = 16)
            sdiEmvConf.cardDataInputCapability =
                application.masterCardAid.cardDataInputCapabilityDF8117.toShort(radix = 16)
            sdiEmvConf.cvmRequiredLimitMK =
                application.masterCardAid.cvmRequiredLimitDF8126.toLong(radix = 10)
            /* TODO Check if its hex or decimal value change the logic
                 to convert the string to an array list of ints
             */
            sdiEmvConf.msrVersionNumber =
                arrayListOf(application.masterCardAid.msrVersionNumber9F6D.toInt(radix = 16))
            /* TODO Check if its hex or decimal value change the logic
                 to convert the string to an array list of ints
             */
            sdiEmvConf.chipAppVersionNumber =
                arrayListOf(application.masterCardAid.chipVersionNumber9F09.toInt(radix = 16))

            sdiEmvConf.kernelConfiguration =
                application.masterCardAid.kernelConfigurationDF811B.toShort(radix = 16)
            sdiEmvConf.transactionCategory =
                application.masterCardAid.transactionCategoryCode9F53.toShort(radix = 16)
            sdiEmvConf.tacDefaultMK =
                application.masterCardAid.tacDefaultDF8120.hexStringToByteArray()
            sdiEmvConf.tacDenialMK =
                application.masterCardAid.tacDenialDF8121.hexStringToByteArray()
            sdiEmvConf.tacOnlineMK =
                application.masterCardAid.tacOnlineDF8122.hexStringToByteArray()
            sdiEmvConf.terminalRiskManagement =
                application.masterCardAid.terminalRiskManagementData9F1D.hexStringToByteArray()
            sdiEmvConf.merchantID = application.masterCardAid.merchantIdentifier9F16
            sdiEmvConf.merchantNameLocation = application.masterCardAid.merchantNameAndLocation9F4E
            sdiEmvConf.acquirerID =
                application.masterCardAid.acquirerIdentifier9F01.hexStringToByteArray()
            sdiEmvConf.messageHoldTime =
                application.masterCardAid.messageHoldTimeDF812D.toInt(radix = 16)
            sdiEmvConf.tornTransactionLifetime =
                application.masterCardAid.tornTransactionLifetimeDF811C.toInt(radix = 16)
            sdiEmvConf.tornTransactionNumber =
                application.masterCardAid.tornTransactionNumberDF811D.toShort(radix = 16)
            sdiEmvConf.phoneMessageTable =
                application.masterCardAid.phoneMessageTableDF8131.hexStringToByteArray()
            sdiEmvConf.tagsToRead =
                application.masterCardAid.tagsToReadDF8112.hexStringToByteArray()
            sdiEmvConf.fieldOffTime = application.masterCardAid.holdTimeValueDF8130.toShort(16)
            //TODO include this if its needed for your implementation, we are not using it in test app
            //sdiEmvConf.setTagsToWriteBeforeGenAC()
            //sdiEmvConf.setTagsToWriteAfterGenAC()
            sdiEmvConf.proceedToFirstWriteFlag =
                application.masterCardAid.proceedToFirstWriteFlagDF8110.toShort(radix = 16)
            sdiEmvConf.dataStoreRequestedOperatorID =
                application.masterCardAid.dsRequestedOperatorID9F5C.hexStringToByteArray()
            sdiEmvConf.dataExchangeTimeout =
                application.masterCardAid.deTimeoutValueDF8127.toInt(radix = 16)
            sdiEmvConf.relayResistanceMinGracePeriod =
                application.masterCardAid.rrMinGracePeriodDF8132.toInt(radix = 16)
            sdiEmvConf.relayResistanceMaxGracePeriod =
                application.masterCardAid.rrMaxGracePeriodDF8133.toInt(radix = 16)
            sdiEmvConf.relayResistanceCAPDUTime =
                application.masterCardAid.rrExpTransTimeCAPDUDF8134.toInt(radix = 16)
            sdiEmvConf.relayResistanceRAPDUTime =
                application.masterCardAid.rrExpTransTimeRAPDUDF8135.toInt(radix = 16)
            sdiEmvConf.relayResistanceAccuracy =
                application.masterCardAid.rrAccuracyThresholdDF8136.toInt(radix = 16)
            sdiEmvConf.relayResistanceTimeMismatchThreshold =
                application.masterCardAid.rrTransTimeMismatchThresholdDF8137.toShort(radix = 16)
            sdiAidConfList.add(sdiEmvConf)
        }

        return sdiAidConfList
    }

    private fun getCtlsCapks(): List<EmvCtlsConfig.Capk> {
        return ctlsConfig.capks
    }

    fun getTagsToFetch(): List<String> {
        return ctlsConfig.fetchTags
    }

    fun getSensitiveTagsToFetch(): List<String> {
        return ctlsConfig.sensitiveTags
    }

    fun logConfiguration() {
        if (initialize() == SdiResultCode.OK) {
            val terminalConfig = logTerminalConfig()
            if (terminalConfig == null) {
                Log.d(TAG, "~~~~~~~~~~~~~~~~Contactless Configuration Start~~~~~~~~~~~~~~~~~~")
                Log.d(TAG, "Invalid Terminal config")
                Log.d(TAG, "~~~~~~~~~~~~~~~~Contactless Configuration End~~~~~~~~~~~~~~~~~~")
            }
            val visaAidConfig = logVisaAidConfig()
            val capkConfig = logCapkConfig()
            val ctlsConfig = EmvCtlsConfig(
                visa = visaAidConfig,
                terminal = terminalConfig!!,
                capks = capkConfig,
                fetchTags = listOf(),
                sensitiveTags = listOf(),
                mastercard = logMastercardAidConfig()
            )
            Log.d(TAG, "~~~~~~~~~~~~~~~~Contactless Configuration Start~~~~~~~~~~~~~~~~~~")
            Log.d(TAG, GsonBuilder().setPrettyPrinting().create().toJson(ctlsConfig.terminal))
            Log.d(TAG, "~~~~~~~~~~~~~~~~Contactless Configuration VISA~~~~~~~~~~~~~~~~~~")
            for (application in ctlsConfig.visa.applications)
                Log.d(TAG, GsonBuilder().setPrettyPrinting().create().toJson(application))
            Log.d(TAG, "~~~~~~~~~~~~~~~~Contactless Configuration MASTERCARD~~~~~~~~~~~~~~~~~~")
            for (application in ctlsConfig.mastercard.applications)
                Log.d(TAG, GsonBuilder().setPrettyPrinting().create().toJson(application))
            for (capk in ctlsConfig.capks)
                Log.d(TAG, GsonBuilder().setPrettyPrinting().create().toJson(capk))
            Log.d(TAG, "~~~~~~~~~~~~~~~~Contactless Configuration End~~~~~~~~~~~~~~~~~~")
            exit()
        }
    }

    private fun logCapkConfig(): List<EmvCtlsConfig.Capk> {
        val result = sdk.sdiManager.emvCtls.capKeys
        val capkList = ArrayList<EmvCtlsConfig.Capk>()
        if (result.result == SdiResultCode.OK) {
            val keys = result.keys
            for (key in keys) {
                val capk = EmvCtlsConfig.Capk(
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

    private fun logVisaAidConfig(): EmvCtlsConfig.Visa {
        val aidList = hashMapOf<String, String>("030000" to "A0000000031010")
        val applications = ArrayList<EmvCtlsConfig.Visa.Application>()
        for (aid in aidList) {
            val result = sdk.sdiManager.emvCtls.getAppDataByAid(
                aid.key.toLong(radix = 16),
                aid.value.hexStringToByteArray()
            )
            try {
                if (result.result == SdiResultCode.OK) {
                    val sdiEmvConf = result.emv
                    val visaAid = EmvCtlsConfig.Visa.Application.VisaAid(
                        additionalTerminalCapabilities9F40 = sdiEmvConf.additionalCapabilities.toHexString(),
                        appFlowCapDFAB31 = sdiEmvConf.ctlsAppFlowCapabilities.toString(),
                        contactlessCVMRequiredLimitDFAB42 = sdiEmvConf.cvmRequiredLimit.toString(10),
                        contactlessFloorLimitDFAB40 = sdiEmvConf.floorLimit.toString(10),
                        contactlessTransactionLimitDFAB41 = sdiEmvConf.ctlsTransactionLimit.toString(10),
                        merchantCategoryCode9F15 = sdiEmvConf.merchantCategory.toHexString(),
                        merchantIdentifier9F16 = sdiEmvConf.merchantID,
                        merchantNameAndLocation9F4E = sdiEmvConf.merchantNameLocation,
                        tecSupportDFAB30 = sdiEmvConf.tecSupport.toString(16),
                        termIdent9F1C = sdiEmvConf.terminalID.toHexString(),
                        terminalCapabilities9F33 = sdiEmvConf.terminalCapabilities.toHexString(),
                        terminalCountryCode9F1A = sdiEmvConf.terminalCountryCode.toString(16),
                        terminalTransactionQualifier9F66 = sdiEmvConf.transactionQualifier.toHexString(),
                        terminalType9F35 = sdiEmvConf.terminalType.toString(16),
                        versionNumber9F09 = sdiEmvConf.chipAppVersionNumber.toString()
                    )
                    val application = EmvCtlsConfig.Visa.Application(
                        aid = sdiEmvConf.aid.toHexString(),
                        defaultApplicationNameDFAB22 = sdiEmvConf.defaultAppName,
                        kernelID = sdiEmvConf.kernelID.toString(radix = 16),
                        asiDFAB02 = sdiEmvConf.asi.toString(16),
                        visaAid = (visaAid),
                        appFlowCapDFAB03 = sdiEmvConf.ctlsAppFlowCapabilities.toString(),
                        retapFieldOffDFAB08 = sdiEmvConf.retapFieldOff.toString(16),
                        additionalTagsCRDDFAB21 = sdiEmvConf.additionalTagsCRD?.toHexString()?:"",
                        additionalTagsTRMDFAB20 = "",
                        specialTRXConfigDFAB05 = ""
                    )
                    applications.add(application)
                } else {
                    Log.d(TAG, "AID retrieval failed ${result.result.name} ")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return EmvCtlsConfig.Visa(applications.toList())
    }

    private fun logMastercardAidConfig(): EmvCtlsConfig.Mastercard {
        val aidList = hashMapOf<String, String>("020000" to "A0000000041010")
        val applications = ArrayList<EmvCtlsConfig.Mastercard.Application>()
        // Mastercard
        for (aid in aidList) {
            val result = sdk.sdiManager.emvCtls.getAppDataByAid(
                aid.key.toLong(radix = 16),
                aid.value.hexStringToByteArray()
            )
            if (result.result == SdiResultCode.OK) {
                val sdiEmvConf = result.emv
                val mastercardAid = EmvCtlsConfig.Mastercard.Application.MasterCardAid(
                    termIdent9F1C = sdiEmvConf.terminalID.toHexString(),
                    terminalCountryCode9F1A = sdiEmvConf.terminalCountryCode.toString(16),
                    terminalType9F35 = sdiEmvConf.terminalType.toString(16),
                    additionalTerminalCapabilities9F40 = sdiEmvConf.additionalCapabilities.toHexString(),
                    merchantCategoryCode9F15 = sdiEmvConf.merchantCategory.toHexString(),
                    appFlowCapDFAB31 = sdiEmvConf.ctlsAppFlowCapabilities.toString(),
                    floorLimitDF8123 = sdiEmvConf.floorLimitMK.toString(10),
                    transactionLimitNoOnDeviceDF8124 =
                    sdiEmvConf.transactionLimitNoCVMOnDevice.toString(10),
                    transactionLimitOnDeviceDF8125 =
                    sdiEmvConf.transactionLimitCVMOnDevice.toString(10),
                    magstripeCVMAboveLimitDF811E = sdiEmvConf.magstripeCVMAboveLimit.toString(16),
                    magstripeCVMbelowLimitDF812C = sdiEmvConf.magstripeCVMBelowLimit.toString(16),
                    chipCVMAboveLimitDF8118 = sdiEmvConf.chipCVMAboveLimit.toString(16),
                    chipCVMBelowLimitDF8119 = sdiEmvConf.chipCVMBelowLimit.toString(16),
                    securityCapabilityDF811F = sdiEmvConf.securityCapability.toString(16),
                    cardDataInputCapabilityDF8117 = sdiEmvConf.cardDataInputCapability.toString(16),
                    cvmRequiredLimitDF8126 = sdiEmvConf.cvmRequiredLimitMK.toString(10),
                    msrVersionNumber9F6D = sdiEmvConf.msrVersionNumber.toString(),
                    chipVersionNumber9F09 = sdiEmvConf.chipAppVersionNumber.toString(),
                    kernelConfigurationDF811B = sdiEmvConf.kernelConfiguration.toString(16),
                    transactionCategoryCode9F53 = sdiEmvConf.transactionCategory.toString(16),
                    tacDefaultDF8120 = sdiEmvConf.tacDefaultMK.toHexString(),
                    tacDenialDF8121 = sdiEmvConf.tacDenialMK.toHexString(),
                    tacOnlineDF8122 = sdiEmvConf.tacOnlineMK.toHexString(),
                    terminalRiskManagementData9F1D = sdiEmvConf.terminalRiskManagement.toHexString(),
                    merchantIdentifier9F16 = sdiEmvConf.merchantID,
                    merchantNameAndLocation9F4E = sdiEmvConf.merchantNameLocation,
                    acquirerIdentifier9F01 = sdiEmvConf.acquirerID.toHexString(),
                    messageHoldTimeDF812D = sdiEmvConf.messageHoldTime.toString(16),
                    tornTransactionLifetimeDF811C = sdiEmvConf.tornTransactionLifetime.toString(16),
                    tornTransactionNumberDF811D = sdiEmvConf.tornTransactionNumber.toString(16),
                    phoneMessageTableDF8131 = sdiEmvConf.phoneMessageTable.toHexString(),
                    tagsToReadDF8112 = sdiEmvConf.tagsToRead?.toHexString()?:"",
                    proceedToFirstWriteFlagDF8110 = sdiEmvConf.proceedToFirstWriteFlag?.toString(16)?:"",
                    dsRequestedOperatorID9F5C = sdiEmvConf.dataStoreRequestedOperatorID.toHexString(),
                    deTimeoutValueDF8127 = sdiEmvConf.dataExchangeTimeout.toString(16),
                    rrMinGracePeriodDF8132 = sdiEmvConf.relayResistanceMinGracePeriod.toString(16),
                    rrMaxGracePeriodDF8133 = sdiEmvConf.relayResistanceMaxGracePeriod.toString(16),
                    rrExpTransTimeCAPDUDF8134 = sdiEmvConf.relayResistanceCAPDUTime.toString(16),
                    rrExpTransTimeRAPDUDF8135 = sdiEmvConf.relayResistanceRAPDUTime.toString(16),
                    rrAccuracyThresholdDF8136 = sdiEmvConf.relayResistanceAccuracy.toString(16),
                    rrTransTimeMismatchThresholdDF8137 =
                    sdiEmvConf.relayResistanceTimeMismatchThreshold.toString(16),
                    holdTimeValueDF8130 = sdiEmvConf.fieldOffTime.toString(16),
                    // TODO
                    tagsToWriteAfterGenACFF8103 = "",
                    tagsToWriteBeforeGenACFF8102 = ""
                )
                val application = EmvCtlsConfig.Mastercard.Application(
                    aid = sdiEmvConf.aid.toHexString(),
                    defaultApplicationNameDFAB22 = sdiEmvConf.defaultAppName,
                    kernelID = sdiEmvConf.kernelID.toString(radix = 16),
                    asiDFAB02 = sdiEmvConf.asi.toString(16),
                    masterCardAid = (mastercardAid),
                    appFlowCapDFAB03 = sdiEmvConf.ctlsAppFlowCapabilities.toString(),
                    retapFieldOffDFAB08 = sdiEmvConf.retapFieldOff.toString(16),
                    additionalTagsCRDDFAB21 = sdiEmvConf.additionalTagsCRD?.toHexString()?:"",
                    additionalTagsTRMDFAB20 = "",
                    specialTRXConfigDFAB05 = ""
                )
                applications.add(application)
            }
        }
        return EmvCtlsConfig.Mastercard(applications.toList())
    }

    private fun logTerminalConfig(): EmvCtlsConfig.Terminal? {
        val result = sdk.sdiManager.emvCtls.termData
        val sdiEmvConf = result.emv
        try {
            return EmvCtlsConfig.Terminal(
                terminalType = sdiEmvConf.terminalType.toString(radix = 16),
                terminalCountryCode = sdiEmvConf.terminalCountryCode.toString(radix = 16),
                transactionCurrency = sdiEmvConf.transactionCurrency.name,
                transactionCurrencyExp = sdiEmvConf.transactionCurrencyExp.toString(radix = 16),
                beepFrequencyAlert = sdiEmvConf.beepFreqAlert.toString(),
                beepFrequencySuccess = sdiEmvConf.beepFreqSuccess.toString(),
                beepVolume = sdiEmvConf.beepVolume.toString(),
                secondTapDelay = sdiEmvConf.secondTapDelay.toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getEmvContactlessKernelVersions(): String? {
        val result = initialize()
        if (result != SdiResultCode.OK) return ""
        val ctlsKernelInfo = sdk.sdiManager.emvCtls.termData.emv.kernelVersion
        Log.d(TAG, "emvContactlessKernelVersions: $ctlsKernelInfo")
        exit()
        return ctlsKernelInfo
    }

    companion object {
        private const val TAG = "EMVCTLSConfig"
    }
}