/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.ui.utils

import android.app.AlarmManager
import android.content.Context
import android.widget.Toast

object DateTimeUtil {
    fun setDateTime(context: Context, timestamp: Long) {
        setTime(context, timestamp)
    }

    private fun setTime(context: Context, timestamp: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (am != null) {
            am.setTime(timestamp)
            Toast.makeText(context, "Date and Time Set Successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to Set Date and Time", Toast.LENGTH_LONG).show()
        }
    }
}



