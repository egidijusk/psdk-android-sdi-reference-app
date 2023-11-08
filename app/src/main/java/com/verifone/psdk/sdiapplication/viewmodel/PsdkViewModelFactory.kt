package com.verifone.psdk.sdiapplication.viewmodel

import android.app.Application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.verifone.psdk.sdiapplication.ui.config.SdiConfigurationViewModel
import com.verifone.psdk.sdiapplication.ui.home.SdiConnectionViewModel
import com.verifone.psdk.sdiapplication.ui.transaction.SdiTransactionViewModel
import com.verifone.psdk.sdiapplication.ui.updateservice.UpdateServiceViewModel

class PsdkViewModelFactory(
    private val application: Application,
        ) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SdiConnectionViewModel::class.java)) {
            return SdiConnectionViewModel(application) as T
        }
        if (modelClass.isAssignableFrom(SdiTransactionViewModel::class.java)) {
            return SdiTransactionViewModel(application) as T
        }
        if (modelClass.isAssignableFrom(SdiConfigurationViewModel::class.java)) {
            return SdiConfigurationViewModel(application) as T
        }
        if (modelClass.isAssignableFrom(UpdateServiceViewModel::class.java)) {
            return UpdateServiceViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}