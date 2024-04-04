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

import com.priv.verifone.psdk.sdiapplication.sdi.card.SdiContactless
import com.verifone.payment_sdk.SdiEmvCandidate
import com.verifone.payment_sdk.SdiTouchButton

// This callback triggers to POS app for PSDK notifications and PSDK callback events
interface TransactionListener {
    fun display(message:String)
    fun showLeds(b: Boolean)
    fun activateLed(led: SdiContactless.LED, activate:Boolean)
    fun getSensitiveDataTouchCoordinates(): ArrayList<SdiTouchButton>
    fun sensitiveDataEntryTitle(message:String)
    fun showSensitiveDataEntry()
    fun pinEntryComplete()
    fun sensitiveDigitsEntered(digits: String)
    fun setSensitiveDataGreenButtonText(text: String)
    fun applicationSelection(candidates:ArrayList<SdiEmvCandidate>): Int
}