/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.sdi.config.model

data class EmvContactConfig(
    val applications: List<Application>,
    val capks: List<Capk>,
    val fetchTags: List<String>,
    val sensitiveTags: List<String>,
    val terminal: Terminal
) {
    data class Application(
        val acBeforeAfter: String, // 00
        val additionalVersionNumbers: String, // 010102020303040405050606070708080096FFFF
        val aid: String, // A0000000031010
        val aipCvmNotSupported: String, // 00
        val appFlowCap: String, // 3F1F170000
        val appTermAddCap: String, // F000F0A001
        val appTermCap: String, // E0F8C8
        val appTerminalType: String, // 22
        val appVersionNumber: String, // 008D
        val asi: String, // 01
        val belowLimitTerminalCapabilities: String, // E008C8
        val cdaProcessing: String, // 00
        val countryCode: String, // 0036
        val defaultAppName: String, // Visa
        val defaultDDOL: String, // 9F3704
        val floorLimit: String, // 000007D0
        val maxTargetPercentage: String, // 00
        val merchantCategoryCode: String, // 5999
        val merchantIdent: String, // 202020202020202020202020202020
        val posEntryMode: String, // 05
        val securityLimit: String, // 00000000
        val tacDefault: String, // DC4000A800
        val tacDenial: String, // 0000000000
        val tacOnline: String, // DC4004F800
        val targetPercentage: String, // 00
        val termIdent: String, // 3132333435363738
        val threshold: String // 000001F4
    )

    data class Capk(
        val certificateRevocationListDF0E: String, // 123456
        val exponentDF0D: String, // 01
        val hashDF0C: String, // B769775668CACB5D22A647D1D993141EDAB7237B
        val indexDF09: String, // 50
        val keyDF0B: String, // D11197590057B84196C2F4D11A8F3C05408F422A35D702F90106EA5B019BB28AE607AA9CDEBCD0D81A38D48C7EBB0062D287369EC0C42124246AC30D80CD602AB7238D51084DED4698162C59D25EAC1E66255B4DB2352526EF0982C3B8AD3D1CCE85B01DB5788E75E09F44BE7361366DEF9D1E1317B05E5D0FF5290F88A0DB47
        val rid: String // A000000003
    )

    data class Terminal(
        val additionalTerminalCapabilities: String, // F000F0A001
        val terminalCapabilities: String, // E0F8C8
        val terminalCountryCode: String, // 0036
        val terminalType: String, // 22
        val transactionCurrency: String, // AUD
        val transactionCurrencyExp: String // 2
    )
}