/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.tms

import android.app.Application
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import android.util.Log
import com.verifone.updateservicelib.IUpdateServiceCallback
import com.verifone.updateservicelib.RecoveryLogStatus
import com.verifone.updateservicelib.UpdateServiceApi
import com.verifone.updateservicelib.UpdateStatus
import java.io.File
import java.io.FileOutputStream

// This is responsible for handling Update Service apis as per UI request call
class TmsService(private val app: Application) {

    companion object {
        private const val TAG = "TmsService"
    }

    private var result = UpdateStatus.STATUS_FAILURE
    private lateinit var updateService: UpdateServiceApi

    // POS app receives the update status through this event
    private val updateServiceCallback = object : IUpdateServiceCallback.Stub() {
        override fun onStatus(status: Int) {
            Log.i(TAG, "onStatus(): $status")
            updateService.unbind()
        }
    }

    init {
        Thread(Runnable {
            // Instantiate UpdateService
            updateService = UpdateServiceApi.getInstance(app)
        }).start()
    }

    // Install an Android APK
    fun installApk(fileName: String): Int {
        try {
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
        return result
    }

    // Uninstall an Android APK
    fun unInstallApk(packageName: String): Int {
        try {
            updateService.registerCallback(updateServiceCallback)
            result = updateService.uninstallApkPackage(packageName)
            Log.d(TAG, "uninstallApkPackage: $result")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    // Install an Android OTA package
    // NOTE : The ota zip file used here doesn't have relevant files as this will vary respective to terminals.
    // NOTE : This can be used as reference and respective OTA packages can be added instead of "ota_update_no_sig.zip" file
    fun installAndroidOtaPackage(fileName: String): Int {
        try {
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
        return result
    }

    // Install a package that contains multiple parts - params, APK, Engage OTA, Engage pkg, and Android OTA
    fun installSuperPackage(fileName: String): Int {
        try {
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
        return result
    }

    // This API can be used to get the status of the last Android OTA by returning the data from the recovery logs.
    // If a log form an install is found, the bundle will be populated, otherwise it will be empty.
    fun fetchLastRecoveryStatus(): String {
        var status: String
        try {
            val bundleStatus = updateService.lastRecoveryLogStatus
            if (bundleStatus != null) {
                bundleStatus.classLoader = RecoveryLogStatus::class.java.classLoader
                val recoveryLogStatus: RecoveryLogStatus? =
                    bundleStatus.getParcelable(RecoveryLogStatus.RECOVERY_LOG_STATUS_PARCELABLE_KEY)
                if (recoveryLogStatus != null) {
                    status =
                        when (recoveryLogStatus.recoveryStatus) {
                            RecoveryLogStatus.RECOVERY_STATUS_UNKNOWN -> "RECOVERY_STATUS_UNKNOWN"
                            RecoveryLogStatus.RECOVERY_STATUS_PENDING -> "RECOVERY_STATUS_PENDING"
                            RecoveryLogStatus.RECOVERY_STATUS_SUCCESS -> "RECOVERY_STATUS_SUCCESS"
                            RecoveryLogStatus.RECOVERY_STATUS_ABORTED -> "RECOVERY_STATUS_ABORTED"
                            RecoveryLogStatus.RECOVERY_STATUS_FAILED -> "RECOVERY_STATUS_FAILED"
                            else -> "UNKNOWN STATUS"
                        }

                    Log.i(TAG, "OTA File name: " + recoveryLogStatus.otaFileName)
                    Log.i(TAG, "Target Build: " + recoveryLogStatus.targetBuild)
                    Log.i(TAG, "Recovery Status: " + recoveryLogStatus.recoveryStatus + ":" + status)
                    Log.i(TAG, "Recovery Log creation ms: " + recoveryLogStatus.recoveryLogCreationMs)
                    Log.i(TAG, "OTA Update error code: " + if (recoveryLogStatus.otaErrorCode != 0) recoveryLogStatus.otaErrorCode else "Not Available")
                } else {
                    status = "No RecoveryLogStatus found"
                    Log.i(TAG, status)
                }
            } else {
                status = "LastRecoveryLogStatus is empty"
                Log.i(TAG, status)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
            status = "Exception occurs, please try again"
        }
        return status
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
