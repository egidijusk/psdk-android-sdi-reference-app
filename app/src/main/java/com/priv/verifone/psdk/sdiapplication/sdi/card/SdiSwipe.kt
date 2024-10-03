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
import com.verifone.payment_sdk.SdiManager
import com.verifone.payment_sdk.SdiResultCode

// This is responsible for processing swipe transaction
class SdiSwipe(private val sdiManager: SdiManager) :
    SdiCard(sdiManager = sdiManager) {

    companion object {
        private const val TAG = "SdiSwipe"
        private val DATE =
            byteArrayOf(0x20, 0x07, 0x31) // Date (DDMMYY) against which the card is to be checked
    }

    override fun startTransactionFlow(amount: Long): SdiResultCode {
        val response = performValidationChecks(DATE, returnAdditional = true)
        // retrieveTagsUsingApi(config.getMagstripeTagsToFetch())

        uiListener.captureSignature()

        // Go to Host for approval
        uiListener.display("Transaction Approved")
        Log.d(TAG, "Transaction Approved")
        return SdiResultCode.OK
    }
}