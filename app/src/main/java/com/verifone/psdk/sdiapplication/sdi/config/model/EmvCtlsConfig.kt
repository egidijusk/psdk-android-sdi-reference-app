/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.verifone.psdk.sdiapplication.sdi.config.model

data class EmvCtlsConfig(
    val capks: List<Capk>,
    val fetchTags: List<String>,
    val sensitiveTags: List<String>,
    val mastercard: Mastercard,
    val terminal: Terminal,
    val visa: Visa
) {
    data class Capk(
        val certificateRevocationListDF0E: String, // 123456
        val exponentDF0D: String, // 01
        val hashDF0C: String, // B769775668CACB5D22A647D1D993141EDAB7237B
        val indexDF09: String, // 50
        val keyDF0B: String, // D11197590057B84196C2F4D11A8F3C05408F422A35D702F90106EA5B019BB28AE607AA9CDEBCD0D81A38D48C7EBB0062D287369EC0C42124246AC30D80CD602AB7238D51084DED4698162C59D25EAC1E66255B4DB2352526EF0982C3B8AD3D1CCE85B01DB5788E75E09F44BE7361366DEF9D1E1317B05E5D0FF5290F88A0DB47
        val rid: String // A000000003
    )

    data class Mastercard(
        val applications: List<Application>
    ) {
        data class Application(
            val additionalTagsCRDDFAB21: String,
            val additionalTagsTRMDFAB20: String,
            val aid: String, // A0000000041010
            val appFlowCapDFAB03: String, // 0000000000
            val asiDFAB02: String, // 01
            val defaultApplicationNameDFAB22: String, // Mastercard
            val kernelID: String, // 020000
            val masterCardAid: MasterCardAid,
            val retapFieldOffDFAB08: String, // 0A
            val specialTRXConfigDFAB05: String // 2120110000000000
        ) {
            data class MasterCardAid(
                val acquirerIdentifier9F01: String, // 000000000000
                val additionalTerminalCapabilities9F40: String, // F000F0A001
                val appFlowCapDFAB31: String, // 0000000000
                val cardDataInputCapabilityDF8117: String, // E0
                val chipCVMAboveLimitDF8118: String, // 60
                val chipCVMBelowLimitDF8119: String, // 08
                val chipVersionNumber9F09: String, // 0002
                val cvmRequiredLimitDF8126: String, // 000000004000
                val deTimeoutValueDF8127: String, // 01F4
                val dsRequestedOperatorID9F5C: String, // FFFFFFFFFFFFFFFF
                val floorLimitDF8123: String, // 000000006000
                val holdTimeValueDF8130: String, // 00
                val kernelConfigurationDF811B: String, // B0
                val magstripeCVMAboveLimitDF811E: String, // 20
                val magstripeCVMbelowLimitDF812C: String, // 00
                val merchantCategoryCode9F15: String, // 5999
                val merchantIdentifier9F16: String,
                val merchantNameAndLocation9F4E: String,
                val messageHoldTimeDF812D: String, // 000000
                val msrVersionNumber9F6D: String, // 0001
                val phoneMessageTableDF8131: String, // 0000000000000000
                val proceedToFirstWriteFlagDF8110: String, // FE
                val rrAccuracyThresholdDF8136: String, // 012C
                val rrExpTransTimeCAPDUDF8134: String, // 0012
                val rrExpTransTimeRAPDUDF8135: String, // 0018
                val rrMaxGracePeriodDF8133: String, // 0032
                val rrMinGracePeriodDF8132: String, // 0014
                val rrTransTimeMismatchThresholdDF8137: String, // 32
                val securityCapabilityDF811F: String, // 08
                val tacDefaultDF8120: String, // FE50BCA000
                val tacDenialDF8121: String, // 0000000000
                val tacOnlineDF8122: String, // FE50BCF800
                val tagsToReadDF8112: String,
                val tagsToWriteAfterGenACFF8103: String,
                val tagsToWriteBeforeGenACFF8102: String,
                val termIdent9F1C: String, // 3132333435363738
                val terminalCountryCode9F1A: String, // 0036
                val terminalRiskManagementData9F1D: String, // 6C7A800000000000
                val terminalType9F35: String, // 22
                val tornTransactionLifetimeDF811C: String, // 0078
                val tornTransactionNumberDF811D: String, // 02
                val transactionCategoryCode9F53: String, // 52
                val transactionLimitNoOnDeviceDF8124: String, // 000999999999
                val transactionLimitOnDeviceDF8125: String // 000999999999
            )
        }
    }

    data class Terminal(
        val beepFrequencyAlert: String, // 0000
        val beepFrequencySuccess: String, // 0000
        val beepVolume: String, // 0075
        val secondTapDelay: String, // 0A
        val terminalCountryCode: String, // 0036
        val terminalType: String, // 22
        val transactionCurrency: String, // AUD
        val transactionCurrencyExp: String // 2
    )

    data class Visa(
        val applications: List<Application>
    ) {
        data class Application(
            val additionalTagsCRDDFAB21: String,
            val additionalTagsTRMDFAB20: String,
            val aid: String, // A0000000031010
            val appFlowCapDFAB03: String, // 1000000000
            val asiDFAB02: String, // 01
            val defaultApplicationNameDFAB22: String, // VISA
            val kernelID: String, // 030000
            val retapFieldOffDFAB08: String, // 0A
            val specialTRXConfigDFAB05: String, // 2120110000000000
            val visaAid: VisaAid
        ) {
            data class VisaAid(
                val additionalTerminalCapabilities9F40: String, // F000F0A001
                val appFlowCapDFAB31: String, // 8203000000
                val contactlessCVMRequiredLimitDFAB42: String, // 000000010000
                val contactlessFloorLimitDFAB40: String, // 000000004000
                val contactlessTransactionLimitDFAB41: String, // 000999999999
                val merchantCategoryCode9F15: String, // 5999
                val merchantIdentifier9F16: String,
                val merchantNameAndLocation9F4E: String,
                val tecSupportDFAB30: String, // 01
                val termIdent9F1C: String, // 3132333435363738
                val terminalCapabilities9F33: String, // E06008
                val terminalCountryCode9F1A: String, // 0036
                val terminalTransactionQualifier9F66: String, // 36000000
                val terminalType9F35: String, // 22
                val versionNumber9F09: String // 008D
            )
        }
    }
}