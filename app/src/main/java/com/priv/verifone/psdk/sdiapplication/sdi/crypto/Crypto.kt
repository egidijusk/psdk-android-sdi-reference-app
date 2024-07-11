/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.sdi.crypto

import android.util.Log
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.hexStringToByteArray
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.toHexString
import com.verifone.payment_sdk.SdiDataEncResponse
import com.verifone.payment_sdk.SdiDataOption
import com.verifone.payment_sdk.SdiDataPlaceHolder
import com.verifone.payment_sdk.SdiIntegerResponse
import com.verifone.payment_sdk.SdiManager
import com.verifone.payment_sdk.SdiResultCode
import java.util.EnumSet

// This handles the security and secure data related operations
class Crypto(val sdiManager: SdiManager) {

    companion object {
        private const val TAG = "SDICrypto"

        // These handles are usually the hostnames which configured in SCCFG config
        // So these values may varies with respective to the terminal with different SCCFG config
        // Crypto api or encryption process may fails in case of wrong configuration
        private const val PIN_HANDLE = "TDES_DUKPT_PIN" // HostName used for PIN ENCRYPTION process
        private const val DATA_HANDLE = "TDES_DUKPT_DATA" // HostName used for DATA ENCRYPTION process
        private const val MAC_HANDLE = "TDES_DUKPT_MAC" // HostName used for MAC GENERATION and VERIFICATION process
    }

    private fun open(host: String): Int? {
        Log.d(TAG, "Command Crypto Open (70-00)")
        val result: SdiIntegerResponse = sdiManager.crypto.open(host)
        Log.d(TAG, "Command Result: ${result.result} ")
        Log.d(TAG, "Command Result Response: ${result.response} ")
        if (result.result == SdiResultCode.OK) {
            return result.response
        }
        return null
    }

    // Crypto Get Versions (70-0C)
    fun getCryptoVersion(): String {
        val handle = open(PIN_HANDLE)
        if (handle != null) {
            Log.d(TAG, "Command Crypto Get Versions (70-0C)")
            val response = sdiManager.crypto.versions
            Log.d(TAG, "Command Response : $response")
            sdiManager.crypto.close(handle)
            return response.response
        } else {
            Log.d(TAG, "Command Result: Crypto Open Failed")
            return "Crypto Versions Not found"
        }
    }

    // This api is not part of the sequence but useful to know if encryption keys are present.
    // Absence of keys could lead to ERR_EXECUTION/FAIL for the other crypto commands issued.
    fun keyInventory(): String? {
        val handle = open(PIN_HANDLE)
        if (handle != null) {
            Log.d(TAG, "Command Key Inventory (70-09)")
            val result = sdiManager.crypto.getKeyInventory(handle)
            Log.d(TAG, "Command Result: ${result.result} ")
            Log.d(TAG, "Command Result Response: ${result.response} ")
            sdiManager.crypto.close(handle)
            return result.response
        } else {
            Log.d(TAG, "Command Result: Crypto Open Failed")
            return "Key Inventory Not found"
        }
    }

    fun getEncryptedPinBlock() {
        val handle = open(PIN_HANDLE)
        if (handle != null) {
            val format = 0.toShort() // Pin Block Format (0-ISO0, 1-ISO1, 2-ISO2, 3-ISO3)
            val zeroPin = false // if true request zero PIN block

            Log.d(TAG, "Command Get Encrypted PIN (70-08)")
            val result = sdiManager.crypto.getEncryptedPin(handle, format, zeroPin)
            Log.d(TAG, "Command Result: ${result.result} ")
            Log.d(TAG, "Command Result pin block: ${result.pinblock.toHexString()} ")
            Log.d(TAG, "Command Result pin ksn: ${result.ksn.toHexString()} ")
            sdiManager.crypto.close(handle)
        } else {
            Log.d(TAG, "Command Result: Crypto Open Failed")
        }
    }

    // Get encrypted transaction data - Get Enc Data (29-00)
    fun getSensitiveEncryptedData(tagList: List<String>) {
        val handle = open(DATA_HANDLE)
        if (handle != null) {
            val tagListString = StringBuilder()
            for (tag in tagList) {
                tagListString.append(tag)
                tagListString.append("00") // Make the tag response as variable length
            }
            val appData = byteArrayOf() // Optional application data (BERTLV encoded)
            val options: EnumSet<SdiDataOption>? = null // data options truncation/padding
            val useStoredTx = false // Use stored transaction data
            val iv = null // optional initialization vector

            Log.d(TAG, "Command getEncData (29-00)")
            val encodedDataResponse: SdiDataEncResponse = sdiManager.data.getEncData(
                handle,
                tagListString.toString().hexStringToByteArray(),
                appData,
                options,
                useStoredTx,
                iv
            )
            Log.d(TAG, "Command Result: ${encodedDataResponse.result} ")
            Log.d(TAG, "Command Response: ${encodedDataResponse?.response?.toHexString()}")
            Log.d(TAG, "Command KSN: ${encodedDataResponse?.ksn?.toHexString()}")
            Log.d(TAG, "Command IV: ${encodedDataResponse?.iv?.toHexString()}")
            sdiManager.crypto.close(handle)
        } else {
            Log.d(TAG, "Command Result: Crypto Open Failed")
        }
    }

    // This api encrypts the given data with the given host config and responds with the encrypted data
    fun encryptData(plainTextData: String) {
        val handle = open(DATA_HANDLE)
        if (handle != null) {
            Log.d(TAG, "Command Crypto Encrypt (70-02)")
            val encryptedDataResponse =
                sdiManager.crypto.encrypt(handle, plainTextData.toByteArray(), null)
            Log.d(TAG, "Command Result: ${encryptedDataResponse.result}")
            Log.d(
                TAG,
                "Command Response, encrypted data : ${encryptedDataResponse.out?.toHexString()}," +
                        " IV : ${encryptedDataResponse.iv?.toHexString()}," +
                        " KSN : ${encryptedDataResponse.ksn.toHexString()}"
            )
            sdiManager.crypto.close(handle)
        } else {
            Log.d(TAG, "Command Result: Crypto Open Failed")
        }
    }

    // Get encrypted message data
    fun getEncryptedMessageData() {
        val handle = open(DATA_HANDLE)
        if (handle != null) {
            Log.d(TAG, "Command getEncMsgData (29-01)")
            val messageTemplate =
                "12341B58312456781B5832249988".toByteArray() // message template including placeholders for sensitive data elements
            val placeHolder = ArrayList<SdiDataPlaceHolder>() // placeholder data
            val useStoredTx =
                false // Use stored transaction data, 00 = disabled (default) / 01 = enabled
            val iv = null // optional initialization vector

            val encodedMsgData =
                sdiManager.data.getEncMsgData(handle, messageTemplate, placeHolder, useStoredTx, iv)
            Log.d(TAG, "Command Result: ${encodedMsgData.result}")
            Log.d(TAG, "Command Response: ${encodedMsgData.response.toHexString()}")
            sdiManager.crypto.close(handle)
        } else {
            Log.d(TAG, "Command Result: Crypto Open Failed")
        }
    }

    fun createMACSignature(data: String) {
        val handle = open(MAC_HANDLE)
        if (handle != null) {
            Log.d(TAG, "Command Crypto Sign (70-04)")
            val signedResponse =
                sdiManager.crypto.sign(handle, data.toByteArray(), null)
            Log.d(TAG, "Command Result: ${signedResponse.result}")
            Log.d(
                TAG, "Command Response, MAC data : ${signedResponse.out?.toHexString()}," +
                        " IV : ${signedResponse.iv?.toHexString()}," +
                        " KSN : ${signedResponse.ksn.toHexString()}"
            )
            sdiManager.crypto.close(handle)
        } else {
            Log.d(TAG, "Command Result: Crypto Open Failed")
        }
    }

    fun verifyMAC(data: String, mac: ByteArray) {
        val handle = open(MAC_HANDLE)
        if (handle != null) {
            Log.d(TAG, "Command Crypto Verify (70-05)")
            val verifyResponse =
                sdiManager.crypto.verify(handle, data.toByteArray(), mac, null)
            Log.d(TAG, "Command Result: ${verifyResponse.result}")
            Log.d(
                TAG, "Command Response, MAC data : ${verifyResponse.out?.toHexString()}," +
                        " IV : ${verifyResponse.iv?.toHexString()}," +
                        " KSN : ${verifyResponse.ksn.toHexString()}"
            )
            sdiManager.crypto.close(handle)
        } else {
            Log.d(TAG, "Command Result: Crypto Open Failed")
        }
    }

    // The response is not an encrypted message, rather, a signature is calculated of a host message
    fun getMessageSignature() {
        val handle = open(MAC_HANDLE)
        if (handle != null) {
            val messageTemplate =
                "12341B58312456781B5832249988".toByteArray() // message template including placeholders for sensitive data elements
            val placeHolder = ArrayList<SdiDataPlaceHolder>() // placeholder data
            val useStoredTx =
                false // Use stored transaction data, 00 = disabled (default) / 01 = enabled
            val iv = null // optional initialization vector

            Log.d(TAG, "Command getMsgSignature (29-04)")
            val encodedMsgSignature =
                sdiManager.data.getMsgSignature(
                    handle,
                    messageTemplate,
                    placeHolder,
                    useStoredTx,
                    iv
                )
            Log.d(TAG, "Command Result: ${encodedMsgSignature.result}")
            Log.d(TAG, "Command Response: ${encodedMsgSignature.response.toHexString()}")
            sdiManager.crypto.close(handle)
        } else {
            Log.d(TAG, "Command Result: Crypto Open Failed")
        }
    }
}