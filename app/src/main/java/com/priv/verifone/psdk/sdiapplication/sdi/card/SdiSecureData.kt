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
import com.verifone.payment_sdk.*
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.hexStringToByteArray
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.toHexString
import java.util.*

// This handles the security and data related operations
class SdiSecureData(private val sdiCrypto: SdiCrypto, private val sdiData: SdiData) {

    companion object {
        private const val TAG = "SdiSecureData"
    }

    private var cryptoHandle = 0

    // Crypto Open Command (70-00) with DUKPT "05"
    fun open(hostName: String): SdiResultCode {
        Log.d(TAG, "Command Crypto Open (70-00)")
        val openResponse = sdiCrypto.open(hostName)
        Log.d(TAG, "Command Response : $openResponse")

        if (openResponse.result == SdiResultCode.OK) {
            cryptoHandle = openResponse.response
        }
        return openResponse.result
    }

    // Get key inventory (70-09)
    fun getKeyInventory() {
        // This command is not part of the sequence but useful to know if encryption keys are present.
        // Absence of keys could lead to ERR_EXECUTION/FAIL for the other crypto commands issued.
        Log.d(TAG, "Command Get key inventory (70-09)")
        val keyResponse = sdiCrypto.getKeyInventory(cryptoHandle)
        Log.d(TAG, "Command Response : $keyResponse")
    }

    // Crypto Get Versions (70-0C)
    fun getCryptoVersion(): String {
        Log.d(TAG, "Command Crypto Get Versions (70-0C)")
        val response = sdiCrypto.versions
        Log.d(TAG, "Command Response : $response")
        return response.response
    }

    // GetValidationInfo (29-06)
    fun getValidationInfo(): SdiStringResponse {
        Log.d(TAG, "Command Get Validation Info (29-06)")
        val response = sdiData.validationInfo
        Log.d(TAG, "Command Response : $response")
        return response
    }

    // Get Encrypted pin - Crypto Get Encrypted Pin Command (70-08)
    fun getEncryptedPin(): SdiEncryptedPinResponse {
        val format = 0.toShort() // Pin Block Format (0-ISO0, 1-ISO1, 2-ISO2, 3-ISO3)
        val zeroPin = false // if true request zero PIN block

        Log.d(TAG, "Command getEncryptedPin (70-08)")
        val response = sdiCrypto.getEncryptedPin(cryptoHandle, format, zeroPin)
        Log.d(TAG, "Command Result: ${response.result} ")
        Log.d(TAG, "Command Response: ${response.pinblock.toHexString()}")
        return response
    }

    // Get encrypted transaction data - Get Enc Data (29-00)
    fun getEncryptedData(tagList: List<String>): SdiDataEncResponse {
        Log.d(TAG, "sensitiveTagsToRetrieve : $tagList")

        val tagListString = StringBuilder()
        for (tag in tagList) {
            tagListString.append(tag)
            tagListString.append("00") // Make the tag response as variable length
        }

        val appData = byteArrayOf() // Optional application data (BERTLV encoded)
        val options = EnumSet.of(
            SdiDataOption.CONCATENATE,
            SdiDataOption.PAD_FF
        ) // data options truncation/padding
        val useStoredTx =
            false // Use stored transaction data, 00 = disabled (default) / 01 = enabled
        val iv = null // optional initialization vector

        Log.d(TAG, "Command getEncData (29-00)")
        val encodedDataResponse = sdiData.getEncData(
            cryptoHandle,
            tagListString.toString().hexStringToByteArray(),
            appData,
            options,
            useStoredTx,
            iv
        )
        Log.d(TAG, "Command Result: ${encodedDataResponse.result} ")
        Log.d(TAG, "Command Response: ${encodedDataResponse.response.toHexString()}")
        return encodedDataResponse
    }

    // Get encrypted message data - Get Enc Message Data (29-01)
    fun getEncryptedMessageData(): SdiDataEncResponse {
        val messageTemplate =
            "12341B58312456781B5832249988".toByteArray() // message template including placeholders for sensitive data elements
        val placeHolder = ArrayList<SdiDataPlaceHolder>() // placeholder data
        val useStoredTx =
            false // Use stored transaction data, 00 = disabled (default) / 01 = enabled
        val iv = null // optional initialization vector

        Log.d(TAG, "Command getEncMsgData (29-01)")
        val encodedMsgData =
            sdiData.getEncMsgData(cryptoHandle, messageTemplate, placeHolder, useStoredTx, iv)
        Log.d(TAG, "Command Result: ${encodedMsgData.result}")
        Log.d(TAG, "Command Response: ${encodedMsgData.response.toHexString()}")
        return encodedMsgData
    }

    // Get message signature (29-04)
    fun getMessageSignature(): SdiDataEncResponse {
        val messageTemplate =
            "12341B58312456781B5832249988".toByteArray() // message template including placeholders for sensitive data elements
        val placeHolder = ArrayList<SdiDataPlaceHolder>() // placeholder data
        val useStoredTx =
            false // Use stored transaction data, 00 = disabled (default) / 01 = enabled
        val iv = null // optional initialization vector

        Log.d(TAG, "Command getMsgSignature (29-04)")
        val encodedMsgSignature =
            sdiData.getMsgSignature(cryptoHandle, messageTemplate, placeHolder, useStoredTx, iv)
        Log.d(TAG, "Command Result: ${encodedMsgSignature.result}")
        Log.d(TAG, "Command Response: ${encodedMsgSignature.response.toHexString()}")
        return encodedMsgSignature
    }

    // Crypto Close Command (70-01)
    fun close() {
        Log.d(TAG, "Command Crypto Close (70-01)")
        val resultCode = sdiCrypto.close(cryptoHandle)
        Log.d(TAG, "Command Result: $resultCode ")
    }

    // Clear Data Store (29-03)
    fun clearDataStore() {
        Log.d(TAG, "Command clearDataStore (29-03)")
        val resultCode = sdiData.clearDataStore()
        Log.d(TAG, "Command Result: $resultCode ")
    }
}