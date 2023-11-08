package com.verifone.psdk.sdiapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class BaseViewModel(private val app: Application) : AndroidViewModel(app) {

    protected fun background(action: () -> Unit) {
        // Launching within the view model scope for this example, but in production, these should
        // be launched from some scope that lives with the application instead of the UI.
        viewModelScope.launch {
            performBackgroundAction(action)
        }
    }

    protected fun onUiThread(action: () -> Unit) {
        // Launching within the view model scope for this example, but in production, these should
        // be launched from some scope that lives with the application instead of the UI.
        viewModelScope.launch {
            action()
        }
    }

    private suspend fun performBackgroundAction(action: () -> Unit) =
        withContext(Dispatchers.Default) {
            action()
        }
}