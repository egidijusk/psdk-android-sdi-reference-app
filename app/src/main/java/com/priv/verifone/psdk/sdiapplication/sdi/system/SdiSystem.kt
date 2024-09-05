/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.sdi.system

import android.content.Context
import android.os.Build
import android.util.Log
import com.priv.verifone.psdk.sdiapplication.sdi.utils.JavaUtils
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.getBase64EncodedBitmap
import com.priv.verifone.psdk.sdiapplication.sdi.utils.Utils.Companion.getTestHtmlReceipt
import com.verifone.payment_sdk.SdiComponentVersion
import com.verifone.payment_sdk.SdiManager
import com.verifone.payment_sdk.SdiResultCode
import com.verifone.payment_sdk.SdiSysPropertyInt
import com.verifone.payment_sdk.SdiSysPropertyString
import java.util.ArrayList

class SdiSystem(internal val sdiManager: SdiManager) {

    companion object {
        private const val TAG = "SDISystem"
    }

    // Prints the bitmap image
    fun printBmp(app: Context) {
        val bitmap =
            Utils.getBitmapFromAsset(app.applicationContext, "receipt/bmp/verifone-logo.bmp")
        val bytes = JavaUtils.convertBitmapTo1bpp(bitmap)
        Log.d(TAG, "printBitmap Command")
        val result = sdiManager.printer.printBitmap(bitmap!!.width, bitmap.height, bytes)
        Log.d(TAG, "Command Result: ${result.name}")
    }

    // Prints the receipt from a hardcoded html format file present inside assets folder
    fun printHtml(app: Context) {
        Log.d(TAG, "printHTML Command")
        val html = getTestHtmlReceipt(app.applicationContext, "receipt/html/receipt.html")
        val result = sdiManager.printer.printHTML(html, false)
        Log.d(TAG, "Command Result: ${result.name}")
    }

    fun printBmpHack(app: Context) {
        val HTML = "<html>" +
                "<head>" +
                "</head>" +
                "<body>" +
                "<div>" +
                "<img src=\"data:image/png;base64,encoded_data_placeholder\"/>" +
                "</div>" +
                "<br>" +
                "<br>" +
                "</body>" +
                "</html>"
        val bitmap =
            Utils.getBitmapFromAsset(app.applicationContext, "receipt/bmp/verifone-logo.bmp")
        var encodedString = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            encodedString = getBase64EncodedBitmap(bitmap!!)
        }
        val finalHtml = HTML.replace("encoded_data_placeholder", encodedString)
        Log.d(TAG, "Print Command ")
        val result = this.sdiManager.printer.printHTML(finalHtml, false)
        Log.d(TAG, "Command Result: ${result.name}")
    }

    fun isPhysicalKeyboardPresent(): Boolean {
        Log.d(TAG, "System Property Command KEYBOARD_HW (20-1A)")
        val response = sdiManager.system.getPropertyInt(SdiSysPropertyInt.KEYBOARD_HW, 0x01)
        Log.d(TAG, "Command Response : ${response.response}")
        Log.d(TAG, "Command Result: ${response.result.name}")

        return (response.response == 0x01)
    }

    fun toggleKeyboardBacklight(): Boolean {
        Log.d(TAG, "System Property Command KEYB_BACKLIGHT (20-1A)")
        val response = sdiManager.system.getPropertyInt(SdiSysPropertyInt.KEYB_BACKLIGHT, 0x01)

        val value = if (response.response == 1) 0 else 1

        val result = sdiManager.system.setPropertyInt(SdiSysPropertyInt.KEYB_BACKLIGHT, value, 0x01)
        Log.d(TAG, "Command Result : ${result.ordinal}")
        Log.d(TAG, "Command Result: ${result.name}")

        return (result == SdiResultCode.OK)
    }

    // Abort the current command in progress
    // Note: Not all commands can be aborted.
    fun abort() {
        Log.d(TAG, "Abort Command (20-02)")
        val result = sdiManager.system.abort()
        Log.d(TAG, "Command Result: ${result.name}")
    }

    // Read terminal serial number
    fun serialNumber(): String {
        Log.d(TAG, "Serial Number Command (System Tags)")
        val response = sdiManager.system.serialNumber
        Log.d(TAG, "Command Response : ${response.response}")
        Log.d(TAG, "Command Result: ${response.result.name}")
        return response.response
    }

    // Read hardware serial number through getPropertyString() api
    fun hardwareSerialNumber(): String {
        Log.d(TAG, "System Property Command HW_SERIALNO (20-1A)")
        val response = sdiManager.system.getPropertyString(SdiSysPropertyString.HW_SERIALNO, 0x01)
        Log.d(TAG, "Command Response : ${response.response}")
        Log.d(TAG, "Command Result: ${response.result.name}")

        return response.response
    }


    // Read hardware model number
    fun modelName(): String {
        Log.d(TAG, "System Property Command HW_MODEL_NAME (20-1A)")
        val response = sdiManager.system.getPropertyString(SdiSysPropertyString.HW_MODEL_NAME, 0x01)
        Log.d(TAG, "Command Response : ${response.response}")
        Log.d(TAG, "Command Result: ${response.result.name}")
        return response.response
    }

    // Read PCI Reboot time
    fun pciRebootTime(): String {
        Log.d(TAG, "System Property Command PCI_REBOOT_TIME (20-1A)")
        val response =
            sdiManager.system.getPropertyString(SdiSysPropertyString.PCI_REBOOT_TIME, 0x01)
        Log.d(TAG, "Command Response : ${response.response}")
        Log.d(TAG, "Command Result: ${response.result.name}")

        return response.response
    }

    fun setAndroidTime(time: String): String {
        Log.d(TAG, "System Property Command ANDROID_TIME  (20-1A)")
        val response =
            sdiManager.system.getPropertyString(SdiSysPropertyString.ANDROID_TIME, 0x01)
        Log.d(TAG, "Command Response : ${response.response}")
        Log.d(TAG, "Command Result: ${response.result.name}")

        val setResponse =
            sdiManager.system.setPropertyString(SdiSysPropertyString.ANDROID_TIME, time, 0x01)
        Log.d(TAG, "Command Response : ${setResponse}")
        Log.d(TAG, "Command Result: ${setResponse.name}")

        return response.response
    }

    // Read the SDI component installed on terminal with versions
    fun sdiVersion(): ArrayList<SdiComponentVersion>? {
        Log.d(TAG, "System Property Command SDI Versions (20-1A)")
        val response = sdiManager.system.getSdiVersion(0x01)
        for (component in response.info) {
            Log.d(TAG, "Command Response: Component Name : ${component.name}")
            Log.d(TAG, "Command Response: Component Version : ${component.version}")
        }
        Log.d(TAG, "Command Result: ${response.result.name}")

        return response.info
    }

    // TODO implement UI
    fun reboot() {
        Log.d(TAG, "reboot Command (20-17)")
        val response = sdiManager.system.reboot()
        Log.d(TAG, "Command Result: ${response.name}")
    }

    // TODO implement UI
    fun hibernate() {
        Log.d(TAG, "hibernate Command (20-17)")
        val response = sdiManager.system.hibernate()
        Log.d(TAG, "Command Result: ${response.name}")
    }

    // TODO implement UI
    fun shutdown() {
        Log.d(TAG, "shutdown Command (20-17)")
        val response = sdiManager.system.shutdown()
        Log.d(TAG, "Command Result: ${response.name}")
    }
}
