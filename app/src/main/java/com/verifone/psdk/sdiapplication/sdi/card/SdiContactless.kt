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
import com.verifone.psdk.sdiapplication.sdi.config.Config
import com.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.dateToString
import com.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.getCurrentDateTime
import com.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.hexStringToByteArray
import com.verifone.payment_sdk.*
import java.util.*

class SdiContactless(private val sdiManager: SdiManager, private val config: Config) :
    SdiCard(sdiManager = sdiManager, config = config) {

    private val notifyCallback: NotifyCallback = NotifyCallback()

    enum class LED {
        ONE,
        TWO,
        THREE,
        FOUR
    }

    companion object {
        private const val TAG = "SdiCardCTLS"
    }

    override fun initialize(): SdiResultCode {
        super.initialize()
        Log.d(TAG, "Ctls Init Framework Command (40-00)")
        val initOptions = SdiEmvOptions.create()
        initOptions.setOption(SdiEmvOption.TRACE, true)
        //initOptions.setOption(SdiEmvOption.CONFIG_MODE, true)
        initOptions.setOption(SdiEmvOption.TRACE_ADK_LOG, true)
        initOptions.setOption(SdiEmvOption.LED_CBK_EXT, true)
        initOptions.setOption(SdiEmvOption.AUTO_RETAP, true)
        val result = sdiManager.emvCtls?.initFramework(60, initOptions)
        Log.d(TAG, "Command result: ${result?.name}")
        // TODO Setting this per transaction in the sample app, this needs to be set only once.
        // Search for 9E-01 in SDI Docs
        sdiManager.setNotifyCallback(notifyCallback)
        return result!!
    }

    fun exit(): SdiResultCode {
        Log.d(TAG, "Ctls End Transaction Command  (40-15)")
        sdiManager.emvCtls.endTransaction(null)
        Log.d(TAG, " Ctls Exit Framework Command  (40-00)")
        val result = sdiManager.emvCtls?.exitFramework(null)
        Log.d(TAG, "Command result: ${result?.name}")
        return result!!
    }

    override fun startTransactionFlow(amount: Long): SdiResultCode {

        val resp = continueOffline()
        listener.display("Continue Offline: ${resp.result.name}")
        retrieveTags(resp.txn)
        retrieveTagsUsingApi(config.getCtlsTagsToFetch())
        sdiManager.smartCardCtls.smartPowerOff(EnumSet.of(SdiEmvCtlsReaderOptions.DETECT_REMOVAL))
        var result : SdiResultCode?
        do  {
            result = sdiManager.smartCardCtls.cardRemoval(0)
        } while (result != SdiResultCode.OK)
        Log.i(TAG, "Card Removed")
        return resp.result
    }

    fun setupTransaction(amount: Long): SdiEmvTxnResponse {
        // EMV CTLS setup Transaction (40-10)
        Log.i(TAG, "Setup Transaction Command (40-10)")

        val ctlsTxnConfig = SdiEmvTxn.create()

        val today = getCurrentDateTime()
        val date = today.dateToString("yyMMdd").hexStringToByteArray()
        val time = today.dateToString("hhmmss").hexStringToByteArray()

        val sdiEmvTxnResponse = sdiManager.emvCtls.setupTransaction(
            SdiEmvTransaction.GOODS_SERVICE,
            amount, date, time, txnCounter.toLong(), ctlsTxnConfig
        )
        Log.i(TAG, "Command Result: ${sdiEmvTxnResponse.result.name}")
        if (sdiEmvTxnResponse.result != SdiResultCode.OK) {
            Log.e(TAG, "Failed to setup transaction")
        }

        return sdiEmvTxnResponse
    }

    private fun continueOffline(): SdiEmvTxnResponse {

        Log.i(TAG, "EMV CT Continue Offline Command (40-11) ")
        var response: SdiEmvTxnResponse? = null
        response = sdiManager.emvCtls.continueOffline(null)
        Log.i(TAG, "Command Result: ${response.result.name}")
        return response!!
    }

    // This callback will get the Led update notifications and the display notification during retap
    private inner class NotifyCallback : SdiNotifyCallback() {
        override fun notifyCallback(data: SdiTlv) {
            Log.d(TAG, "Notify Callback")
            val messageTag = 0xF0
            val ctlsLedTag = 0xBF10
            val ctlsDisplay = 0xBF14
            if (data.obtain(messageTag).count(ctlsLedTag) > 0) {
                Log.d(TAG, "Notify Callback: Led")
                handleLedDisplay(data)
            }
            if (data.obtain(messageTag).count(ctlsDisplay) > 0) {
                handleDisplay(data)
                Log.d(TAG, "Notify Callback: Display")
            }
        }

        private fun handleLedDisplay(data: SdiTlv) {
            val messageTag = 0xF0
            val ctlsLedTag = 0xBF10
            val ctlsLedState = 0xC8
            if (data.obtain(messageTag).count(ctlsLedTag) > 0) {
                val leds: Int =
                    data.obtain(messageTag).obtain(ctlsLedTag).obtain(ctlsLedState)
                        .number

                if (leds and 0x08 == 0x08) {
                    // last led
                    listener.activateLed(LED.FOUR, true)
                } else {
                    listener.activateLed(LED.FOUR, activate = false)
                }
                if (leds and 0x04 == 0x04) {
                    // 3rd led
                    listener.activateLed(LED.THREE, true)
                } else {
                    listener.activateLed(LED.THREE, false)
                }
                if (leds and 0x02 == 0x02) {
                    // 2nd led
                    listener.activateLed(LED.TWO, true)
                } else {
                    listener.activateLed(LED.TWO, false)
                }
                if (leds and 0x01 == 0x01) {
                    listener.activateLed(LED.ONE, true)
                } else {
                    listener.activateLed(LED.ONE, false)
                }
            }
        }

        private fun handleDisplay(data: SdiTlv) {
            val messageTag = 0xF0
            val ctlsDisplay = 0xBF14
            val displayTextId = 0xDF8F12 // Text Id
            if (data.obtain(messageTag).count(ctlsDisplay) > 0) {
                val text: Int =
                    data.obtain(messageTag).obtain(ctlsDisplay).obtain(displayTextId).number
                Log.d(TAG, "Display Text: ${text.toString(16)}")
                // if 0x14
                listener.display("Please see phone")
                //if 0x15 // probably for MC torn transaction test case
                //listener.display("Re-tap")
            }
        }
    }
}