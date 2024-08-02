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
import com.priv.verifone.psdk.sdiapplication.sdi.crypto.Crypto
import com.priv.verifone.psdk.sdiapplication.sdi.transaction.TransactionListener
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.toHexString
import com.priv.verifone.psdk.sdiapplication.utils.Constants
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.collections.ArrayList

abstract class SdiCard(private val sdiManager: SdiManager) {

    companion object {
        private const val TAG = "SdiCard"

            val TEC_CT: Short = 0x01
            val TEC_MSR: Short = 0x02
            val TEC_CTLS: Short = 0x04
            val TEC_MANUAL: Short = 0x08
            val TEC_ALL: Short = 0x0F

            val TIMEOUT_CARD_DETECT = 30000

        // Synchronous api used for detecting card based on the techEnabled parameter
        fun cardDetect(techEnabled: Short, sdiManager: SdiManager): SdiCardDetectResponse {
            Log.d(TAG, "Card Detection Command (23-01)")
            val ctOptions = EnumSet.of(SdiEmvCtReaderOptions.DETECT_WRONG_ATR)

            val sdiTecOptions = SdiTecOptions(
                null, ctOptions, null,
                null, null, null, null
            )
            // Card Detection Command (23-01)
            val response = sdiManager.cardDetect.detect(
                techEnabled,
                sdiTecOptions, true, TIMEOUT_CARD_DETECT, null, null, null, null
            )
            Log.d(TAG, "Command Result: ${response.result.name}")
            Log.d(TAG, "Command Response: ${response.emvOut}")
            return response
        }
    }

    internal var txnCounter = 1

    private val minPinDigit = 4
    private val maxPinDigit = 6
    private val pinEntryTimeout = 30

    private val cardDetectCallback: CardDetectCallback = CardDetectCallback()
    private val statusCallback: StatusCallback = StatusCallback()
    internal val crypto = Crypto(sdiManager)
    internal lateinit var uiListener: TransactionListener

    open fun setListener(callback: TransactionListener) {
        this.uiListener = callback
    }

    open fun initialize(): SdiResultCode {
        Log.d(TAG, "initialize")
        // Will update once R&D is done
        sdiManager.setCardDetectCallback(cardDetectCallback)
        // it is for PIN entry and for manual entry search for 9F-XX in SDI docs
        sdiManager.setStatusCallback(statusCallback)
        return SdiResultCode.OK
    }

    abstract fun startTransactionFlow(amount: Long): SdiResultCode

    // Retrieve the required tags from the response
    internal fun retrieveTags(data: SdiEmvTxn) {
        Log.d(TAG, "Amount: 9F02: ${data.amount}")
        Log.d(TAG, "Cashback Amount: 9F03: ${data.cashbackAmount}")
        Log.d(TAG, "PAN: ${data.pan?.toHexString()}")
        Log.d(TAG, "Track 2: ${data.obfuscatedTrack2?.toHexString()}")
        Log.d(TAG, "AID: 84: ${data.dedicatedFilename?.toHexString()}")
        Log.d(TAG, "TVR: 95: ${data.tvr?.toHexString()}")
        Log.d(TAG, "TSI: 9B: ${data.transactionStatusInfo?.toHexString()}")
        Log.d(TAG, "Cryptogram: 9F27: ${data.cryptogramInfo?.toString(radix = 16)}")
        Log.d(TAG, "Cryptogram: 9F26: ${data.cryptogram?.toHexString()}")
        Log.d(TAG, "AIP: 82: ${data.aip?.toHexString()}")
        Log.d(TAG, "Issuer Application Data: 9F10: ${data.issuerAppData?.toHexString()}")
        Log.d(TAG, "CVM Result 9F34: ${data.cvmResults?.toHexString()}")
    }

    /*
    * Retrieve the required tags afterwards using fetchTnxTags API
    * In this function we are reading the required tags from the json config and passing the same
    * to the PSDK SDI API to retrieve the tags.
    * Once the tags are retrieved we use SDiTlv Class to parse and print the tags
    */
    internal fun retrieveTagsUsingApi(tagsToRetrieve: List<String>) {
        val tags = ArrayList<Long>()
        for (tag in tagsToRetrieve) {
            tags.add(tag.toLong(radix = 16))
        }
        Log.d(TAG,"Command fetchTxnTags (29-02)")
        val tagReceived = sdiManager.data.fetchTxnTags(tags, 2, false)
        Log.d(TAG,"Command Result: ${tagReceived.result} ")
        Log.d(TAG,"Command Response: ${tagReceived.response.toHexString()}")

        val messageTag = 0xF0
        val tlvData = SdiTlv.create()
        tlvData.load(tagReceived.response, false)
        for (tag in tags) {
            if (tlvData.obtain(messageTag).count(tag.toInt()) > 0) {
                val value =
                    tlvData.obtain(messageTag).obtain(tag.toInt()).store(false).toHexString()
                Log.d(TAG, "TAG ${tag.toString(radix = 16)}: $value")
            }
        }
    }

    // PIN Entry using Status Callback method
    fun getPinUsingCallback() = runBlocking {
        var pinResult = SdiResultCode.FAIL
        val job = launch { // launch a new coroutine and keep a reference to its Job
            val buttons:ArrayList<SdiTouchButton> = uiListener.getSensitiveDataTouchCoordinates()

            Log.d(TAG, "GetPin using touch buttons Command (22-01)")
            val result = sdiManager.ped.getPinTouchButtons(
                buttons, // Button layout touch co-ordinates
                true, // PIN Bypass
                pinEntryTimeout,
                0, // Navigator Mode
                minPinDigit,
                maxPinDigit,
                null // language (optional)
            )
            pinResult = result
            Log.d(TAG, "Command Result: ${result.name}")
            uiListener.display(result.name)
        }
        job.join() // wait until child coroutine completes
        return@runBlocking pinResult
    }

    // PIN Entry using Polling method
    fun getPinUsingPolling() = runBlocking {
        var pinResult = SdiResultCode.OK
        val job = launch { // launch a new coroutine and keep a reference to its Job
            val buttons = uiListener.getSensitiveDataTouchCoordinates()
            val startPinResult = sdiManager.ped.startPin(
                buttons, 0, 0, minPinDigit, maxPinDigit, true, false
            )
            Log.d(TAG, "Start PIN result : ${startPinResult.name}")

            var pollResult = sdiManager.ped.pollPin()
            Log.d(TAG, "Poll PIN result : ${pollResult.pin.name}")
            while (pollResult.pin == SdiPinStatus.COLLECTING) {
                var current = ""
                var digit = 0
                while (digit < pollResult.digits) {
                    current += "*"
                    digit++
                }
                uiListener.sensitiveDigitsEntered(current)
                pollResult = sdiManager.ped.pollPin()
            }
            val resp = sdiManager.ped.pollPin()
            Log.d(TAG, "Poll PIN Status : ${resp.pin.name}")
            Log.d(TAG, "Poll PIN result : ${resp.result.name}")
            uiListener.display(resp.pin.name)
            pinResult = resp.result
            val stopPinResult = sdiManager.ped.stopPin()
            Log.d(TAG, "Stop PIN result : ${stopPinResult.name}")
        }
        job.join() // wait until child coroutine completes

        return@runBlocking pinResult
    }

    // PIN Entry using Status Callback method in SDI thread
    // wont work
    fun getPin(): SdiResultCode {
        val pinResult: SdiResultCode

        val buttons = uiListener.getSensitiveDataTouchCoordinates()
        val result = sdiManager.ped.getPinTouchButtons(
            buttons,
            true,
            pinEntryTimeout,
            0,
            minPinDigit,
            maxPinDigit,
            SdiLanguage.ENGLISH
        )
        Log.d(TAG, "PIN result : ${result.name}")
        pinResult = result

        return pinResult
    }

    // Validation checks for the current card regarding the validation table (cardranges.json) stored on the device.
    fun performValidationChecks(date: ByteArray, returnAdditional: Boolean): SdiDataValidationResponse {
        Log.i(TAG, "Perform Validation Checks  Command (29-05) ")
        val response = sdiManager.data.performValidationChecks(date, returnAdditional)
        //Check results will deliver 01 if check is OK, 00 if check is failed
        // and FF if check has not been performed because it's disabled in the validation table.
        //In case of IIN is used no check results are delivered.
        Log.i(TAG, "Command Result: ${response.result.name}")
        Log.i(TAG, "Command Result Matching Record: ${response.match.record} " +
                "luhnCheck: ${response.match.luhnCheck} " +
                "expirationCheck: ${response.match.expirationCheck} " +
                "activationCheck: ${response.match.activationCheck}")
        for (record in response.additional) {
            Log.i(TAG, "Command Result Additional Record : ${record.record}, " +
                    "activationCheck: ${record.activationCheck} " +
                    "expirationCheck:${record.expirationCheck} " +
                    "luhnCheck:${record.luhnCheck}")
        }
        val crypto = Crypto(sdiManager)
        crypto.getSensitiveEncryptedData(listOf("57"))
        return response
    }

    private inner class StatusCallback : SdiStatusCallback() {
        override fun statusCallback(digits: Int, value: String?) {
            Log.d(TAG, "StatusCallback Pin Digits entered : $digits : $value")
            var current = ""
            var digit = 0
            while (digit < digits) {
                current += "*"
                digit++
            }

            if (digits == 254) {
                // 0xFE
                uiListener.display("PAN entered but not confirmed, correction still possible")
                // Change Enter button to Confirm
                uiListener.setSensitiveDataGreenButtonText(Constants.CONFIRM)
                if (value == null || value.isEmpty()) {
                    uiListener.sensitiveDigitsEntered(current)
                } else {
                    uiListener.sensitiveDigitsEntered(value)
                }
            }
            else  if (digits == 253) {
                // 0xFD Invalid expiry date entered, already entered expiry date is deleted, re-entry necessary
                uiListener.display("Invalid expiry date entered, please re-enter")
                uiListener.sensitiveDigitsEntered("")
            } else  if (digits == 252) {
                uiListener.display("PAN, Expiry date or CVV not completely entered, additional digit entry necessary")
                // 0xFC PAN, Expiry date or CVV not completely entered, additional digit entry necessary
            } else  if (digits == 251) {
                // 0xFB PAN, Expiry date or CVV maximum number of digits reached, additional digits will be ignored
                uiListener.display("PAN, Expiry date or CVV maximum number of digits reached, additional digits will be ignored")
            } else if (value == null || value.isEmpty()) {
                uiListener.sensitiveDigitsEntered(current)
            } else {
                uiListener.sensitiveDigitsEntered(value)
            }
        }
    }

    private inner class CardDetectCallback : SdiCardDetectCallback() {
        override fun cardDetectCallback(returnCode: Int, tecOut: Short, sdiEmvTxn: SdiEmvTxn?, pluginResult: ByteArray?) {
            Log.d(TAG, "CardDetectCallback $returnCode : $tecOut : ${sdiEmvTxn?.cardType}: ${pluginResult.contentToString()}")
        }
    }
}