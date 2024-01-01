/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.verifone.psdk.sdiapplication.ui.updateservice

import android.app.Application
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Toast
import com.verifone.psdk.sdiapplication.viewmodel.BaseViewModel
import com.verifone.updateservicelib.IUpdateServiceCallback
import com.verifone.updateservicelib.UpdateServiceApi
import com.verifone.updateservicelib.UpdateStatus
import java.io.File
import java.io.FileOutputStream

class UpdateServiceViewModel(private val app: Application) : BaseViewModel(app) {

    companion object {
        private const val TAG = "UpdateServiceViewModel"
    }

    private var result = UpdateStatus.STATUS_FAILURE
    private lateinit var updateService: UpdateServiceApi
    private val updateServiceCallback = object : IUpdateServiceCallback.Stub() {
        override fun onStatus(status: Int) {
            Log.i(TAG, "onStatus(): $status")
            updateService.unbind()
        }
    }

    init {
        background {
            updateService = UpdateServiceApi.getInstance(app)
        }
    }

    // Install an Android APK
    fun installApk() {
        background {
            try {
                val fileName = "HelloWorldApp.apk"
                updateService.registerCallback(updateServiceCallback)
                copyTestFiles(fileName)

                val file = File(app.cacheDir, fileName)
                val fileDescriptor =
                    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                result = updateService.installApkPackage(fileName, fileDescriptor)
                Log.d(TAG, "installApkPackage: $result")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            showResult(result)
        }
    }

    // Uninstall an Android APK
    fun unInstallApk() {
        background {
            try {
                val component = "com.verifone.helloworldapp"
                updateService.registerCallback(updateServiceCallback)
                result = updateService.uninstallApkPackage(component)
                Log.d(TAG, "uninstallApkPackage: $result")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            showResult(result)
        }
    }

    // Install an Android OTA
    fun installAndroidOtaPackage() {
        background {
            try {
                val fileName = "ota_update_no_sig.zip"
                updateService.registerCallback(updateServiceCallback)
                copyTestFiles(fileName)

                val file = File(app.cacheDir, fileName)
                val fileDescriptor =
                    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                result = updateService.installAndroidOtaPackage(fileName, fileDescriptor)
                Log.d(TAG, "installPackage: $result")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            showResult(result)
        }
    }

    // Install a package that contains multiple parts - params, APK, Engage OTA, Engage pkg, and Android OTA
    fun installSuperPackage() {
        background {
            try {
                val fileName = "flash_params_apk_android.zip"
                updateService.registerCallback(updateServiceCallback)
                copyTestFiles(fileName)

                val file = File(app.cacheDir, fileName)
                val fileDescriptor =
                    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                result = updateService.installPackage(fileName, fileDescriptor)
                Log.d(TAG, "installPackage: $result")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            showResult(result)
        }
    }

    private fun showResult(status: Int) {
        val message: String =
            when (status) {
                UpdateStatus.STATUS_SUCCESS, UpdateStatus.STATUS_SUCCESS_REBOOTING -> {
                    "Result: successful"
                }
                UpdateStatus.STATUS_PENDING -> {
                    "Result: pending"
                }
                else -> {
                    "Result: failed ($status)"
                }
            }
        onUiThread { Toast.makeText(app, message, Toast.LENGTH_SHORT).show() }
    }

    private fun copyTestFiles(fileName: String) {
        try {
            app.assets.open(fileName).use { `in` ->
                FileOutputStream(File(app.cacheDir, fileName)).use { out ->
                    val buf = ByteArray(1024)
                    var len: Int
                    while (`in`.read(buf).also { len = it } > 0) {
                        out.write(buf, 0, len)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}