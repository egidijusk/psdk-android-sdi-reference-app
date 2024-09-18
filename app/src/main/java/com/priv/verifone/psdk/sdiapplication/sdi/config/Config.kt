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

import com.verifone.payment_sdk.*

/*
 * This used to map the configuration stored in assets folder and cached to contact and contacless config respectively
 * Load the configs(terminal, application and capk) to terminals
 * Log the loaded config
 */
class Config(private val sdk: PaymentSdk) {

    private val ctConfig = CtConfig(sdk)
    private val ctlsConfig =  CtlsConfig(sdk)
    private val tlvConfig = TlvConfig()

    fun setContactConfiguration(): SdiResultCode {
        return ctConfig.setContactConfiguration()
    }

    fun setContactTlvConfiguration(): SdiResultCode {
        return ctConfig.setContactTlvConfiguration(tlvConfig)
    }

    /*
    * Following APis are for configuring the contactless kernel
    *
    * */
    fun setCtlsConfiguration(): SdiResultCode {
        return ctlsConfig.setCtlsConfiguration()
    }

    fun setCtlsTlvConfiguration(): SdiResultCode {
        return ctlsConfig.setCtlsTlvConfiguration(tlvConfig)
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