package com.verifone.psdk.sdiapplication.ui.utils

import android.graphics.Rect
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.View
import androidx.core.text.HtmlCompat
import com.verifone.psdk.sdiapplication.sdi.system.SdiSystem
import com.verifone.payment_sdk.PsdkDeviceInformation


fun getDeviceInformation(deviceInfo: PsdkDeviceInformation?, system: SdiSystem): Spanned {
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