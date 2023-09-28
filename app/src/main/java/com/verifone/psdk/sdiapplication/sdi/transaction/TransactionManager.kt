package com.verifone.psdk.sdiapplication.sdi.transaction


import android.util.Log
import com.verifone.payment_sdk.SdiManager
import com.verifone.payment_sdk.SdiResultCode
import com.verifone.psdk.sdiapplication.sdi.card.*
import com.verifone.psdk.sdiapplication.sdi.card.SdiCard.Companion.cardDetect
import com.verifone.psdk.sdiapplication.sdi.config.Config
import kotlin.experimental.and

class TransactionManager(private val sdiManager: SdiManager, config: Config) {

    companion object {
        private const val TAG = "TransactionManager"

    }

    private val contactTransactionBasic = SdiContactBasic(sdiManager, config)
    private val contactTransactionAdvanced = SdiContactAdvanced(sdiManager, config)
    private val manualTransaction = SdiManual(sdiManager, config)
    private lateinit var contactTransaction: SdiContact
    private val ctlsTransaction = SdiContactless(sdiManager, config)
    private val swipeTransaction = SdiSwipe(sdiManager, config)
    private var techEnabled = SdiCard.TEC_ALL

    private lateinit var listener: TransactionListener

    fun setListener(listener: TransactionListener) {
        contactTransactionBasic.setListener(listener)
        contactTransactionAdvanced.setListener(listener)
        ctlsTransaction.setListener(listener)
        manualTransaction.setListener(listener)
        swipeTransaction.setListener(listener)
        this.listener = listener
    }

    private fun initialize(): SdiResultCode {
        var result = contactTransaction.initialize()
        if (result != SdiResultCode.OK)
            return result
        result = ctlsTransaction.initialize()
        return result
    }


    // Function called from UI
    fun startManualEntryTransactionFlow(amount: Long) {
        manualTransaction.initialize()
        manualTransaction.startTransactionFlow(amount)
    }

    fun startTransactionFlow(amount: Long, basic: Boolean) {

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
                listener.display("Present Card")
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
                            techEnabled = SdiCard.TEC_CT
                            listener.showLeds(false)
                            continue
                        }
                        if (ctlsResult == SdiResultCode.EMVSTATUS_TXN_CTLS_MOBILE) {
                            techEnabled = SdiCard.TEC_CTLS
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
            // Wait for card removal
            Log.d(TAG, "Wait for removal (CT Only)")
            //val response = sdiManager.cardDetect.waitForRemoval(30)
            Log.d(TAG, "Card removed")
            exit()
        }
    }

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