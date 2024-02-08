/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.verifone.psdk.sdiapplication.sdi.card

import android.util.Log
import com.verifone.payment_sdk.SdiManager
import com.verifone.payment_sdk.SdiNfcApduSendReceiveResponse
import com.verifone.payment_sdk.SdiNfcPollResponse
import com.verifone.payment_sdk.SdiNfcPollingBitmap
import com.verifone.payment_sdk.SdiResultCode
import com.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.toHexString
import java.util.EnumSet

// This class provides the reference for NFC card processing through PSDK-SDI apis
class SdiNfcCard(private val sdiManager: SdiManager) {

    companion object {
        private const val TAG = "SdiNfcCard"
    }

    // By calling NFC_Client_Init() the application will define whether the client going to communicate with NFC Framework directly or serial.
    // Once the connection is established there is no way to change it unless reboot.
    // So this only needed in first time installation or after a reboot
    private fun initConnection(): SdiResultCode {
        Log.d(TAG, "NFC Client Init (31-10)")
        val result = sdiManager.nfc?.init(0)
        Log.d(TAG, "Command result: ${result?.name}")
        return result!!
    }

    fun initialize(): SdiResultCode {
        Log.d(TAG, "NFC Ping (31-00)")
        val response = sdiManager.nfc.ping()
        Log.d(TAG, "Command result: ${response.result}")

        Log.d(TAG, "NFC Pass Through Open (31-01)")
        var resultCode = sdiManager.nfc.open()
        Log.d(TAG, "Command result: $resultCode")

        if (SdiResultCode.OK == resultCode) {
            Log.d(TAG, "NFC Pass Through Field On (31-03)")
            resultCode = sdiManager.nfc.fieldOn()
            Log.d(TAG, "Command result: $resultCode")
        }

        return resultCode
    }

    fun startTransaction(): SdiResultCode {
        val pollingResponse = polling()
        var resultCode = pollingResponse.result
        if (SdiResultCode.OK == resultCode && pollingResponse.detectedCards.isNotEmpty()) {
            val detectedCard = pollingResponse.detectedCards[0]
            Log.d(TAG, "NFC card type: ${detectedCard.cardType}")
            Log.d(TAG, "NFC card info: ${detectedCard.cardInfo.toHexString()}")

            Log.d(TAG, "NFC Pass Through Card Activation (31-07)")
            resultCode = sdiManager.nfc.fieldActivation(
                detectedCard.cardType.toLong(),
                detectedCard.cardInfo
            )
            Log.d(TAG, "Command result: $resultCode")

            val inputData = byteArrayOf(
                0x00,
                0xA4.toByte(),
                0x04,
                0x00,
                0x0E, // 2PAY.SYS.DDF01
                0x32,
                0x50,
                0x41,
                0x59,
                0x2E,
                0x53,
                0x59,
                0x53,
                0x2E,
                0x44,
                0x44,
                0x46,
                0x30,
                0x31,
                0x00
            )
            val nfcApduResponse = exchangeAPDU(inputData)
            resultCode = nfcApduResponse.result
        } else {
            Log.d(TAG, "Polling Failed : ${pollingResponse.result}")
        }

        exit()
        return resultCode
    }

    private fun polling(): SdiNfcPollResponse {
        Log.d(TAG, "NFC Pass Through Field Polling (31-05)")
        val technologyBitmap = EnumSet.of(SdiNfcPollingBitmap.A)
        val timeOut: Long = 10000
        val customData = byteArrayOf(0x74, 0x65, 0x73, 0x74, 0x31)
        val nfcPollResponse = sdiManager.nfc.fieldPolling(technologyBitmap, timeOut, customData)

        Log.d(TAG, "Command result: ${nfcPollResponse.result}")
        Log.d(TAG, "NFC Card Count: ${nfcPollResponse.cardCount}")
        return nfcPollResponse
    }

    private fun exchangeAPDU(inputData: ByteArray): SdiNfcApduSendReceiveResponse {
        Log.d(TAG, "NFC APDU Exchange (31-1C)")
        val response = sdiManager.nfc.apduSendReceive(inputData)
        Log.d(TAG, "Command result: ${response.result}")
        Log.d(TAG, "Command response: ${response.response.toHexString()}")
        Log.d(TAG, "Card sw1sw2: ${response.cardsw1sw2}")
        return response
    }

    fun getVersion(): String {
        initConnection() // We can ignore the resultCode as connection may be already established before
        Log.d(TAG, "NFC Get Version (31-11)")
        val nfcVersion = sdiManager.nfc.version.response
        Log.d(TAG, "Command result: $nfcVersion")
        return nfcVersion
    }

    private fun exit(): SdiResultCode {
        Log.d(TAG, "NFC Pass Through Field Off (31-04)")
        var resultCode = sdiManager.nfc.fieldOff()
        Log.d(TAG, "Command result: $resultCode")

        Log.d(TAG, "NFC Pass Through Close (31-02)")
        resultCode = sdiManager.nfc.close()
        Log.d(TAG, "Command result: $resultCode")
        return resultCode
    }
}