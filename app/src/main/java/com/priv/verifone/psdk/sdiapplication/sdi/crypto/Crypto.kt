package com.priv.verifone.psdk.sdiapplication.sdi.crypto

import android.util.Log
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.toHexString
import com.verifone.payment_sdk.SdiIntegerResponse
import com.verifone.payment_sdk.SdiManager
import com.verifone.payment_sdk.SdiResultCode

class Crypto(val sdiManager: SdiManager) {
    companion object {
        private const val TAG = "SDICrypto"
        private const val PIN_HANDLE = "TDES_DUKPT_PIN"
    }


    private fun open(host: String): Int? {
        Log.d(TAG, "Command Crypto Open")
        val result:SdiIntegerResponse = sdiManager.crypto.open(host)
        Log.d(TAG, "Command Result: ${result.result} ")
        Log.d(TAG, "Command Result Response: ${result.response} ")
        if (result.result == SdiResultCode.OK) {
            return result.response
        }
        return null
    }

    fun keyInventory(): String? {
        Log.d(TAG, "Command Key Inventory")
        val handle = open(PIN_HANDLE)
        if (handle != null) {
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
        Log.d(TAG, "Command Get Encrypted PIN")
        val handle = open(PIN_HANDLE)
        if (handle != null) {
            val result = sdiManager.crypto.getEncryptedPin(handle, 0x00, false)
            Log.d(TAG, "Command Result: ${result.result} ")
            Log.d(TAG, "Command Result pin block: ${result.pinblock.toHexString()} ")
            Log.d(TAG, "Command Result pin ksn: ${result.ksn.toHexString()} ")
            sdiManager.crypto.close(handle)
        } else {
            Log.d(TAG, "Command Result: Crypto Open Failed")
        }
    }
}