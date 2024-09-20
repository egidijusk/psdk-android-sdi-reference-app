package com.priv.verifone.psdk.sdiapplication

import android.app.Service
import android.content.Intent

import android.os.IBinder


class MyService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        launchApp()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Code for the service to execute
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup code
    }

    private fun launchApp() {
        // Intent to launch the MainActivity (or any other activity)
        val launchIntent = Intent(this, MainActivity::class.java)
        // Make sure to set the flags for a new task
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        // Start the activity from the service
        startActivity(launchIntent)
    }
}

