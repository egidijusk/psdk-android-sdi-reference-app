package com.priv.verifone.psdk.sdiapplication

import android.app.Service
import android.content.Intent

import android.os.IBinder


class MyService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Code for the service to execute
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup code
    }
}

