/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.sdi.transaction

import android.util.Log
import com.verifone.payment_sdk.SdiManager
import com.verifone.payment_sdk.SdiResultCode
import com.priv.verifone.psdk.sdiapplication.sdi.card.*
import com.priv.verifone.psdk.sdiapplication.sdi.card.SdiCard.Companion.cardDetect
import com.priv.verifone.psdk.sdiapplication.sdi.config.Config
import com.verifone.payment_sdk.Decimal
import java.math.BigDecimal
import kotlin.experimental.and
import kotlin.experimental.or

class TransactionManager(private val sdiManager: SdiManager) {

    companion object {
        private const val TAG = "TransactionManager"
    }

    private val contactTransactionBasic = SdiContactBasic(sdiManager)
    private val contactTransactionAdvanced = SdiContactAdvanced(sdiManager)
    private val manualTransaction = SdiManual(sdiManager)
    private lateinit var contactTransaction: SdiContact
    private val ctlsTransaction = SdiContactless(sdiManager)
    private val swipeTransaction = SdiSwipe(sdiManager)
    private val nfcTransaction = SdiNfcCard(sdiManager)

    // This field is used for enabling the card reader detecting interfaces during detection process
    private var techEnabled = SdiCard.TEC_ALL

    private lateinit var listener: TransactionListener

    // This sets the required listener for UI Interactions in the reference app
    fun setListener(listener: TransactionListener) {
        contactTransactionBasic.setListener(listener)
        contactTransactionAdvanced.setListener(listener)
        ctlsTransaction.setListener(listener)
        manualTransaction.setListener(listener)
        swipeTransaction.setListener(listener)
        this.listener = listener
    }

    // Initializes contact and ctls modules and returns the SdiResultCode
    private fun initialize(): SdiResultCode {
        var result = contactTransaction.initialize()
        if (result != SdiResultCode.OK)
            return result
        result = ctlsTransaction.initialize()
        return result
    }

    /*
     * Function called from UI
     * This initiate the manual entry transaction
     *
     * @param amount : Transaction amount for processing
     */
    fun startManualEntryTransactionFlow(amount: Long) {
        manualTransaction.initialize()
        manualTransaction.startTransactionFlow(amount)
    }

    /*
     * Function called from UI
     * This initiate the NFC processing
     */
    fun startNfcProcessingFlow() {
        var result = nfcTransaction.initialize()
        if (SdiResultCode.OK == result) {
            result = nfcTransaction.startTransaction()
            if (SdiResultCode.OK != result) listener.display("NFC Error : ${result.name}")
        } else {
            listener.display("NFC initialization failed : ${result.name}")
        }
    }

    /*
     * Function called from UI
     * This initiate the EMV transaction (Invokes the required api sequences and EMV flow)
     * Initialize, setUp Transaction, Card Detection, transaction processing flow and close framework
     *
     * @param amount : Transaction amount for processing
     * @param basic  : Selects the emv contact processing way (Callback mode or Re-Entrance mode)
     */
    fun startTransactionFlow(amount: Long, basic: Boolean) {
        techEnabled = SdiCard.TEC_ALL
        contactTransaction = if (basic) {
            // callback - simple
            contactTransactionBasic
        } else {
            // re-entrance mode - advanced
            contactTransactionAdvanced
        }
        // For multi currency configuration scenario initialize and exit needs to be called per
        // transaction as the currency config is set during initialization
        // If your solution is using single currency the init and exit can be called once
        val result = initialize()
        if (result == SdiResultCode.OK) {
            var transactionComplete = false
            while (!transactionComplete) {
                if (techEnabled.and(SdiCard.TEC_CTLS) == SdiCard.TEC_CTLS) {
                    val resp = ctlsTransaction.setupTransaction(amount = amount)
                    if (resp.result == SdiResultCode.OK) {
                        listener.showLeds(true)
                    } else {
                        techEnabled = SdiCard.TEC_CT
                    }
                }

                listener.display("$${Decimal(2, amount).toBigDecimal()}\nPresent Card")
                val detectResp = cardDetect(techEnabled, sdiManager = sdiManager)
                if (detectResp.result == SdiResultCode.OK) {
                    Log.d(TAG, "Card detected successfully : ${detectResp.result.name}, tec : ${detectResp.tecOut}")
                    if (detectResp.tecOut == SdiCard.TEC_CT) {
                        listener.showLeds(false)
                        val ctResult = contactTransaction.startTransactionFlow(amount)
                    }
                    if (detectResp.tecOut == SdiCard.TEC_CTLS) {
                        val ctlsResult = ctlsTransaction.startTransactionFlow(amount)
                        // Fallback to Chip, below code does not cover all use cases
                        if (ctlsResult == SdiResultCode.EMVSTATUS_TXN_EMPTY_LIST) {
                            techEnabled = SdiCard.TEC_CT or SdiCard.TEC_MSR
                            listener.showLeds(false)
                            continue
                        }
                        if (ctlsResult == SdiResultCode.EMVSTATUS_TXN_CTLS_MOBILE) {
                            techEnabled = SdiCard.TEC_ALL
                            continue
                        }
                    }
                    if (detectResp.tecOut == SdiCard.TEC_MSR) {
                        val swipeResult = swipeTransaction.startTransactionFlow(amount)
                    }
                } else {
                    listener.display("Card Read Error : ${detectResp.result.name}")
                }
                listener.showLeds(false)
                transactionComplete = true
            }
            exit()
        } else {
            listener.display("Failed to initialize the emv component.. Please try again")
        }
    }

    // This is called during end of the transaction, which used to clear the transaction details
    private fun exit() {
        var exitResult = ctlsTransaction.exit()
        if (exitResult != SdiResultCode.OK) {
            listener.display("Exit CTLS Frame Work Failed: ${exitResult.name}")
        }
        exitResult = contactTransaction.exit()
        if (exitResult != SdiResultCode.OK) {
            listener.display("Exit CT Frame Work Failed: ${exitResult.name}")
        }
    }
}