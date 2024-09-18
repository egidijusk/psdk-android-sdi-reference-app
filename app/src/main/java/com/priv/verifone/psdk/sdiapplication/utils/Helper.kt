/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.utils

import android.app.ActivityManager
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.View
import com.priv.verifone.psdk.sdiapplication.PSDKContext
import com.priv.verifone.psdk.sdiapplication.sdi.card.SdiNfcCard
import com.priv.verifone.psdk.sdiapplication.sdi.crypto.Crypto
import com.priv.verifone.psdk.sdiapplication.sdi.system.SdiUtils
import com.verifone.payment_sdk.PaymentSdk
import com.verifone.payment_sdk.ScannerBarcodeFormatEnum
import com.verifone.payment_sdk.ScannerConfiguration
import com.verifone.payment_sdk.SdiEmvOption
import com.verifone.payment_sdk.SdiEmvOptions
import com.verifone.payment_sdk.SdiResultCode

fun getDeviceInformation(paymentSdk: PaymentSdk): Spanned {
    val sb = StringBuilder()
    val utils = SdiUtils(paymentSdk.sdiManager)
    val nfc = SdiNfcCard(paymentSdk.sdiManager)
    val crypto = Crypto(paymentSdk.sdiManager)
    sb.apply {
        append("<h3>Device Details</h3>")
        append(softwareInfo())
        append("<br>")
        append("<b>HW Serial Number:</b> ${utils.hardwareSerialNumber()}")
        append("<br>")
        append("<b>Serial Number:</b> ${utils.serialNumber()}")
        append("<br>")
        append("<b>Model:</b> ${utils.modelName()}")
        append("<br>")
        append("<b>PCI reboot time: </b>${utils.pciRebootTime()}")
        append("<br>")
        append("<br>")
        append("<b>Key Inventory: </b>${crypto.keyInventory()}")
        append("<br>")
        append("<h3>Kernel Details</h3>")
        append("<br>")
        append("<b>EMV Contact Kernel: </b> ${getEmvContactKernelVersions(paymentSdk)}")
        append("<br>")
        append("<b>EMV Contactless Kernel: </b> ${getEmvContactlessKernelVersions(paymentSdk)}")
        append("<br>")
        append("<b>NFC version: </b> ${nfc.getVersion()}")
        append("<br>")
        append("<h3>Component versions</h3>")
        append("<br>")
        for (component in utils.sdiVersion()!!) {
            append("<b> ${component.name}: </b> ${component.version}")
            append("<br>")
        }
    }

    return Html.fromHtml(sb.toString(), Html.FROM_HTML_MODE_LEGACY)
}


fun getEmvContactKernelVersions(sdk: PaymentSdk): String? {
    val initOptions = SdiEmvOptions.create()
    initOptions.setOption(SdiEmvOption.TRACE, true)
    //initOptions.setOption(SdiEmvOption.CONFIG_MODE, true)
    initOptions.setOption(SdiEmvOption.TRACE_ADK_LOG, true)
    var result = sdk.sdiManager?.emvCt?.initFramework(60, initOptions)
    val TAG = "EMVCT"

    Log.d(TAG, "Command result: ${result?.name}")

    if (result != SdiResultCode.OK) return ""
    val ctKernelInfo = sdk.sdiManager.emvCt.termData.emv.kernelVersion
    Log.d(TAG, "emvContactKernelVersions: $ctKernelInfo")
    Log.d(TAG, "Exit CT Framework Command (39 00)")
    result = sdk.sdiManager?.emvCt?.exitFramework(null)
    Log.d(TAG, "Command result: ${result?.name}")
    return ctKernelInfo
}

fun getEmvContactlessKernelVersions(sdk: PaymentSdk): String? {
    val TAG = "EMVCTLS"
    Log.d(TAG, "Ctls Init Framework Command (40-00)")
    val initOptions = SdiEmvOptions.create()
    initOptions.setOption(SdiEmvOption.TRACE, true)
    //initOptions.setOption(SdiEmvOption.CONFIG_MODE, true)
    initOptions.setOption(SdiEmvOption.TRACE_ADK_LOG, true)
    val result = sdk.sdiManager?.emvCtls?.initFramework(60, initOptions)
    Log.d(TAG, "Command result: ${result?.name}")
    if (result != SdiResultCode.OK) return ""
    val ctlsKernelInfo = sdk.sdiManager.emvCtls.termData.emv.kernelVersion
    Log.d(TAG, "emvContactlessKernelVersions: $ctlsKernelInfo")
    Log.d(TAG, "Exit CTLS Framework: ${sdk.sdiManager?.emvCtls?.exitFramework(null)?.name}")
    return ctlsKernelInfo
}

fun getGlobalVisibleRectForView(view: View): Rect {
    val rect = Rect()
    val result = view.getGlobalVisibleRect(rect)
    Log.d(
        "EMV",
        "result: $result :rect : ${rect.top} : ${rect.left} : ${rect.bottom} : ${rect.right}"
    )
    return rect
}

fun getAttributesForBarcodeScanning(context: Context): HashMap<String, Any> {
    val attributes = HashMap<String, Any>()

    // Display the scanner at 90% of display view
    val metrics = context.resources.displayMetrics
    val rect = Rect(
        (metrics.widthPixels.toFloat() * .1f).toInt(),
        (metrics.heightPixels.toFloat() * .1f).toInt(),
        (metrics.widthPixels.toFloat() * .9f).toInt(),
        (metrics.heightPixels.toFloat() * .9f).toInt()
    )
    attributes[ScannerConfiguration.ATTRIBUTE_SCAN_AREA_LIMIT] = rect
    attributes[ScannerConfiguration.ATTRIBUTE_SET_DIRECTION] = 2
    attributes[ScannerConfiguration.ATTRIBUTE_ACTIVATE_LIGHT] = false
    attributes[ScannerConfiguration.ATTRIBUTE_PLAY_SOUND] = true
    attributes[ScannerConfiguration.ATTRIBUTE_DISPLAY_FEED_PARENT] = context
    attributes[ScannerConfiguration.ATTRIBUTE_SCANNING_FORMATS] = createScanFormatList()
    return attributes
}

// Optionally limit the barcode formats. Leaving this empty
// will search for the default list of barcodes.
fun createScanFormatList(): Array<ScannerBarcodeFormatEnum?> {
    // Check and add any other particular format here
    return arrayOf(
        ScannerBarcodeFormatEnum.DATA_MATRIX,
        ScannerBarcodeFormatEnum.UPCA,
        ScannerBarcodeFormatEnum.UPCE,
        ScannerBarcodeFormatEnum.QRCODE,
        ScannerBarcodeFormatEnum.CODE128,
        ScannerBarcodeFormatEnum.CODABAR,
        ScannerBarcodeFormatEnum.MAXICODE,
        ScannerBarcodeFormatEnum.MSI,
        ScannerBarcodeFormatEnum.CODE39,
        ScannerBarcodeFormatEnum.AZTEC
    )
}

fun softwareInfo(): String {
    val mi = ActivityManager.MemoryInfo()
    (PSDKContext.instance.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(
        mi
    )
    val totalMemory = mi.totalMem
    val availableMemory = mi.availMem


// Get Flash Storage Information
    val stat = StatFs(Environment.getDataDirectory().path)
    val bytesAvailable = stat.blockSize.toLong() * stat.availableBlocks.toLong()
    val totalStorage = stat.blockSize.toLong() * stat.blockCount.toLong()
    val model = Build.MODEL
    val device = Build.DEVICE
    val manufacturer = Build.MANUFACTURER
    val brand = Build.BRAND
    val product = Build.PRODUCT
    val id = Build.ID
    val hardware = Build.HARDWARE
    val androidVersion = Build.VERSION.RELEASE
    val sdkVersion = Build.VERSION.SDK_INT
    val info = StringBuilder()
    info.append("Model: $model").append("<br>")
    info.append("Device: $device").append("<br>")
    info.append("RAM: ${formatSize(totalMemory)}").append("<br>")
    info.append("Available RAM: ${formatSize(availableMemory)}").append("<br>")
    info.append("Storage: ${formatSize(totalStorage)}").append("<br>")
    info.append("Available Storage: ${formatSize(bytesAvailable)}").append("<br>")
    info.append("Android Version: $androidVersion (SDK: $sdkVersion)").append("<br>")
    info.append("Hardware: $hardware").append("<br>")
    return info.toString()
}

fun formatSize(size: Long): String {
    var size = size
    var suffix: String? = null
    if (size >= 1024) {
        suffix = "KB"
        size /= 1024
        if (size >= 1024) {
            suffix = "MB"
            size /= 1024
            if (size >= 1024) {
                suffix = "GB"
                size /= 1024
            }
        }
    }
    val resultBuffer = java.lang.StringBuilder(size.toString())
    var commaOffset = resultBuffer.length - 3
    while (commaOffset > 0) {
        resultBuffer.insert(commaOffset, ',')
        commaOffset -= 3
    }
    if (suffix != null) resultBuffer.append(suffix)
    return resultBuffer.toString()
}