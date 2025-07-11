/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.sdi.card

import android.util.Log
import com.verifone.payment_sdk.SdiEmvCtReaderOptions
import com.verifone.payment_sdk.SdiManager
import java.util.*

class SdiSamCard(private val sdiManager: SdiManager) {

    companion object {
        private const val TAG = "SdiSamCard"
    }

    fun enable() {
    }

    fun detect() {
        val response = sdiManager.smartCardCt.smartDetect(10000)
        Log.d(TAG, "detect result: ${response.result}")
        Log.d(TAG, "detect response: ${response.response}")
    }

    fun activate() {
        Log.d(TAG, "smartcard active : ")
        val response = sdiManager.smartCardCt.activateWith(EnumSet.of(SdiEmvCtReaderOptions.SAM_2,
            SdiEmvCtReaderOptions.SAM_1,
            SdiEmvCtReaderOptions.WARMRESET_OR_SAM_EMV_MODE))
        Log.d(TAG, "activate result: ${response.result}")
        Log.d(TAG, "activate response: ${response.response}")
    }

    fun exchangeAPDU() {
        //sdiManager.smartCardCt.se
    }

    fun deactivate() {

    }
}