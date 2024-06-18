package com.priv.verifone.psdk.sdiapplication.connection

interface ConnectionCallback {
    fun onConnected()
    fun onDisconnected()
}