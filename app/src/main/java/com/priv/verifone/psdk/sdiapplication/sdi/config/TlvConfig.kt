/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.sdi.config

import android.util.Log
import com.priv.verifone.psdk.sdiapplication.PSDKContext
import com.priv.verifone.psdk.sdiapplication.sdi.config.enums.EmvConfigType
import com.priv.verifone.psdk.sdiapplication.sdi.config.tlvmodel.Drl
import com.priv.verifone.psdk.sdiapplication.sdi.config.tlvmodel.Field
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.hexStringToByteArray
import com.verifone.payment_sdk.SdiEmvConf
import com.verifone.payment_sdk.SdiEmvDynamicReaderLimits
import com.verifone.payment_sdk.SdiTlv

class TlvConfig {

    companion object {
        const val TAG = "TlvConfig"
        const val F0 = 0xF0
        const val TAG_AID = 0x4F
        const val TAG_KERNEL_ID = 0xDF810C
        const val TAG_DRL = 0xFFAB01
    }

    private val ctConfigTlv = PSDKContext.ctConfigTlvData
    private val ctlsConfigTlv = PSDKContext.ctlsConfigTlvData

    fun getCtTerminalConfig(): SdiEmvConf {
        Log.d(TAG, "Contact Terminal Config")
        val sdiEmvConf = SdiEmvConf.create()
        loadTlvAccessFields(sdiEmvConf.accessTlv(), ctConfigTlv.terminal.fields, true)
        return sdiEmvConf
    }

    fun getCtlsTerminalConfig(): SdiEmvConf {
        Log.d(TAG, "Contactless Terminal Config")
        val sdiEmvConf = SdiEmvConf.create()
        loadTlvAccessFields(sdiEmvConf.accessTlv(), ctlsConfigTlv.terminal.fields, true)
        return sdiEmvConf
    }

    fun getCtApplicationConfig(): ArrayList<SdiEmvConf> {
        Log.d(TAG, "Contact AID Config")
        val sdiAidConfList = ArrayList<SdiEmvConf>()
        val applications = ctConfigTlv.applications

        for (application in applications) {
            val sdiEmvConf = SdiEmvConf.create()
            loadTlvAccessFields(sdiEmvConf.accessTlv(), application.fields, true)
            sdiAidConfList.add(sdiEmvConf)
        }
        return sdiAidConfList
    }

    fun getCtlsApplicationConfig(): ArrayList<SdiEmvConf> {
        Log.d(TAG, "Contactless AID Config")
        val sdiAidConfList = ArrayList<SdiEmvConf>()
        val applicationDataList = ctlsConfigTlv.ApplicationData

        for (applicationData in applicationDataList) {
            val sdiEmvConf = SdiEmvConf.create()
            val tlvAccess = sdiEmvConf.accessTlv()

            sdiEmvConf.aid = applicationData.AID.hexStringToByteArray()
            tlvAccess.obtain(F0).obtain(TAG_AID)
                .assignBinary(applicationData.AID.hexStringToByteArray()) // Aid

            sdiEmvConf.kernelID = applicationData.KernelID.toLong(radix = 16)
            tlvAccess.obtain(F0).obtain(TAG_KERNEL_ID)
                .assignBinary(applicationData.KernelID.hexStringToByteArray()) // Kernel ID

            loadTlvAccessFields(tlvAccess, applicationData.common.fields, true) // Common fields
            loadTlvAccessFields(tlvAccess, applicationData.scheme.fields, true) // Scheme fields

            if (applicationData.scheme.drl != null) {
                loadDrlParams(tlvAccess, applicationData.scheme.drl) // Drl fields
            }

            sdiAidConfList.add(sdiEmvConf)
        }
        return sdiAidConfList
    }

    private fun loadDrlParams(tlvAccess: SdiTlv, drlParam: Drl) {
        Log.d(TAG, "Drl Scheme : ${drlParam.name}")
        for ((drlIndex, drlValue) in drlParam.values.withIndex()) {
            val drlAccessField = SdiEmvConf.create().accessTlv()
            loadTlvAccessFields(drlAccessField, drlValue.fields, false)
            tlvAccess.obtain(F0).obtainIndex(TAG_DRL, drlIndex + 1).assignTlv(drlAccessField)
        }
    }

    private fun loadTlvAccessFields(tlvAccess: SdiTlv, fields: List<Field>, f0Padding: Boolean) {
        for (field in fields) {
            Log.d(TAG, "field : $field")
            if (field.value != null) {
                when (EmvConfigType.parseConfigType(field.type)) {
                    EmvConfigType.INTEGER -> loadIntegerField(tlvAccess, field, f0Padding)
                    EmvConfigType.STRING -> loadStringField(tlvAccess, field, f0Padding)
                    EmvConfigType.ENCODED_HEX -> loadHexField(tlvAccess, field, f0Padding)

                    else -> Log.d(TAG, "${field.tag} of ${field.type} is not recognized")
                }
            } else {
                Log.d(TAG, "${field.tag} loading ignored as it contains null value")
            }
        }
    }

    private fun loadIntegerField(tlvAccess: SdiTlv, field: Field, f0Padding: Boolean) {
        if (f0Padding) {
            tlvAccess.obtain(F0).obtain(field.tag.toInt(radix = 16))
                .assignInt(field.value.toInt())
        } else {
            tlvAccess.obtain(field.tag.toInt(radix = 16))
                .assignInt(field.value.toInt())
        }
    }

    private fun loadStringField(tlvAccess: SdiTlv, field: Field, f0Padding: Boolean) {
        if (f0Padding) {
            tlvAccess.obtain(F0).obtain(field.tag.toInt(radix = 16))
                .assignString(field.value)
        } else {
            tlvAccess.obtain(field.tag.toInt(radix = 16))
                .assignString(field.value)
        }
    }

    private fun loadHexField(tlvAccess: SdiTlv, field: Field, f0Padding: Boolean) {
        if (f0Padding) {
            tlvAccess.obtain(F0).obtain(field.tag.toInt(radix = 16))
                .assignBinary(field.value.hexStringToByteArray())
        } else {
            tlvAccess.obtain(field.tag.toInt(radix = 16))
                .assignBinary(field.value.hexStringToByteArray())
        }
    }
/*
    fun getDrlParams(): ArrayList<SdiEmvDynamicReaderLimits> {
        val drlLimitList = ArrayList<SdiEmvDynamicReaderLimits>()

        val drl1 = SdiEmvDynamicReaderLimits(
            "3102682620".hexStringToByteArray(),
            "000000003001".toLong(),
            "000000999999".toLong(),
            "000000003001".toLong(),
            "00".toShort()
        )
        val drl2 = SdiEmvDynamicReaderLimits(
            "3102682612000003".hexStringToByteArray(),
            "000000001001".toLong(),
            "000000999999".toLong(),
            "000000001501".toLong(),
            "01".toShort()
        )
        val drl3 = SdiEmvDynamicReaderLimits(
            "3102682612".hexStringToByteArray(),
            "000000002501".toLong(),
            "000000999999".toLong(),
            "000000001501".toLong(),
            "02".toShort()
        )
        val drl4 = SdiEmvDynamicReaderLimits(
            "3102682600".hexStringToByteArray(),
            "000000001501".toLong(),
            "000000999999".toLong(),
            "000000002001".toLong(),
            "03".toShort()
        )

        drlLimitList.add(drl1)
        drlLimitList.add(drl2)
        drlLimitList.add(drl3)
        drlLimitList.add(drl4)

        return drlLimitList
    }*/
}