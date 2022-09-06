package com.verifone.psdk.sdiapplication.sdi.transaction

import com.verifone.psdk.sdiapplication.sdi.card.SdiContactless
import com.verifone.payment_sdk.SdiEmvCandidate
import com.verifone.payment_sdk.SdiEmvCandidateExt
import com.verifone.payment_sdk.SdiTouchButton

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