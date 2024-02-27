/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.verifone.psdk.sdiapplication.ui.utils

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.View
import androidx.core.text.HtmlCompat
import com.verifone.payment_sdk.PsdkDeviceInformation
import com.verifone.payment_sdk.ScannerBarcodeFormatEnum
import com.verifone.payment_sdk.ScannerConfiguration
import com.verifone.psdk.sdiapplication.sdi.system.SdiSystem
import com.verifone.psdk.sdiapplication.sdi.config.Config

fun getDeviceInformation(deviceInfo: PsdkDeviceInformation?, system: SdiSystem, config: Config): Spanned {
    val sb = StringBuilder()
    sb.apply {
        append("<h3>Device Details</h3>")
        append("<br>")
        append("<b>Address: </b>" + (deviceInfo?.address ?: "N/A"))
        append("<br>")
        append("<b>Connection Type: </b>" + (deviceInfo?.connectionType ?: "N/A"))
        append("<br>")
        append("<b>Payment Protocol: </b>" + (deviceInfo?.paymentProtocol ?: "N/A"))
        append("<br>")
        append("<b>HW Serial Number:</b> ${system.hardwareSerialNumber()}")
        append("<br>")
        append("<b>Serial Number:</b> ${system.serialNumber()}")
        append("<br>")
        append("<b>Model:</b> ${system.modelName()}")
        append("<br>")
        append("<b>PCI reboot time: </b>${system.pciRebootTime()}")
        append("<br>")
        append("<h3>Kernel Details</h3>")
        append("<br>")
        append("<b>EMV Contact Kernel: </b> ${config.getEmvContactKernelVersions()}")
        append("<br>")
        append("<b>EMV Contactless Kernel: </b> ${config.getEmvContactlessKernelVersions()}")
        append("<br>")
        append("<h3>Component versions</h3>")
        append("<br>")
        for (component in system.sdiVersion()!!) {
            append("<b> ${component.name}: </b> ${component.version}")
            append("<br>")
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return Html.fromHtml(sb.toString(), Html.FROM_HTML_MODE_LEGACY)
    } else {
        return HtmlCompat.fromHtml(sb.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
    }
}

fun getGlobalVisibleRectForView(view: View): Rect {
    val rect = Rect()
    val result = view.getGlobalVisibleRect(rect)
    Log.d("EMV", "result: $result :rect : ${rect.top} : ${rect.left} : ${rect.bottom} : ${rect.right}")
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