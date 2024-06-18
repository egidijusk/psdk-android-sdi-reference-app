/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.ui.viewmodel

import android.app.Application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.priv.verifone.psdk.sdiapplication.ui.configuration.ConfigurationViewModel
import com.priv.verifone.psdk.sdiapplication.ui.home.HomeViewModel
import com.priv.verifone.psdk.sdiapplication.ui.transaction.TransactionViewModel
import com.priv.verifone.psdk.sdiapplication.ui.updateservice.UpdateserviceViewModel
import com.priv.verifone.psdk.sdiapplication.ui.usb.UsbViewModel


class PsdkViewModelFactory(
    private val application: Application,
        ) : ViewModelProvider.Factory {


    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(application) as T
        }
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            return TransactionViewModel(application) as T
        }
        if (modelClass.isAssignableFrom(UpdateserviceViewModel::class.java)) {
            return UpdateserviceViewModel(application) as T
        }
        if (modelClass.isAssignableFrom(ConfigurationViewModel::class.java)) {
            return ConfigurationViewModel(application) as T
        }
        if (modelClass.isAssignableFrom(UsbViewModel::class.java)) {
            return UsbViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}