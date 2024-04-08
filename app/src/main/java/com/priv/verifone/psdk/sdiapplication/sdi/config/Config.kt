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

import android.content.Context
import com.verifone.payment_sdk.*

/*
 * This used to map the configuration stored in assets folder and cached to contact and contacless config respectively
 * Load the configs(terminal, application and capk) to terminals
 * Log the loaded config
 */
class Config(private val context: Context, private val sdk: PaymentSdk) {

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

    fun getCtSensitiveTagsToFetch(): List<String> {
        return ctConfig.getSensitiveTagsToFetch()
    }

    fun getCtlsSensitiveTagsToFetch(): List<String> {
        return ctlsConfig.getSensitiveTagsToFetch()
    }

    fun getMagstripeTagsToFetch(): List<String> {
        return listOf("57", "5A", "5F24", "9F02", "9F03", "5F2A", "9F35")
    }

    // Returns all the configured EMV contact kernels in terminal
    fun getEmvContactKernelVersions(): String? {
        return ctConfig.getEmvContactKernelVersions()
    }

    // Returns all the configured EMV contactless kernels in terminal
    fun getEmvContactlessKernelVersions(): String? {
        return ctlsConfig.getEmvContactlessKernelVersions()?.replace(";", "<br>")
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