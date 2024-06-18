/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.sdi.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class Utils {

    companion object {
        fun Date.dateToString(format: String, locale: Locale = Locale.getDefault()): String {
            val formatter = SimpleDateFormat(format, locale)
            return formatter.format(this)
        }

        fun getCurrentDateTime(): Date {
            return Calendar.getInstance().time
        }

        fun String.bit(bitPosition: Int) {
            this.forEachIndexed { index, bit ->
                if (bit == '1' ) {
                    takeAction(index)
                }
            }
        }

        fun takeAction(bitPosition: Int) {
            when (bitPosition) {
                0 -> println("Action for bit 0: Something special for the first bit!")
                1 -> println("Action for bit 1: Maybe toggle a setting?")
                2 -> println("Action for bit 2: Launch a function.")
                3 -> println("Action for bit 3: Display a message.")
                else -> println("Action for bit $bitPosition: Default action.")
            }
        }

        fun String.hexStringToByteArray(): ByteArray {
            val result = ByteArray(this.length / 2)
            for (i in result.indices) {
                val index = i * 2
                result[i] = this.substring(index, index + 2).toLong(radix = 16).toByte()
                //Log.i("Utils", "result[i]: ${result[i]}")
            }
            return result
        }

        fun ByteArray.toHexString(): String =
            joinToString(separator = "") { eachByte -> "%02X".format(eachByte) }

        fun getTestHtmlReceipt(context: Context, fileName: String): String {
            var contents = ""
            try {
                val stream: InputStream = context.assets.open(fileName)
                val size: Int = stream.available()
                val buffer = ByteArray(size)
                stream.read(buffer)
                stream.close()
                contents = String(buffer)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return contents
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun getBase64EncodedBitmap(bitmap: Bitmap): String {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray: ByteArray = byteArrayOutputStream.toByteArray()

            return Base64.getEncoder().encodeToString(byteArray)
        }

        fun getBitmapFromAsset(context: Context, filePath: String?): Bitmap? {
            val assetManager = context.assets
            val istr: InputStream
            var bitmap: Bitmap? = null
            try {
                istr = assetManager.open(filePath!!)
                bitmap = BitmapFactory.decodeStream(istr)
            } catch (e: IOException) {
                // handle exception
            }
            return bitmap!!
        }

        fun getDataFromAssets(context: Context, fileName: String): String {
            var contents = ""
            try {
                val stream: InputStream = context.assets.open(fileName)
                val size: Int = stream.available()
                val buffer = ByteArray(size)
                stream.read(buffer)
                stream.close()
                contents = String(buffer)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return contents
        }
    }
}