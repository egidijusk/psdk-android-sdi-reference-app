package com.priv.verifone.psdk.sdiapplication.ui.utils

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import java.util.Calendar


object DateTimePickerUtil {
    fun showDatePicker(context: Context?, listener: OnDateSetListener?) {
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        val day = calendar[Calendar.DAY_OF_MONTH]
        val datePickerDialog = DatePickerDialog(context!!, listener, year, month, day)
        datePickerDialog.show()
    }

    fun showTimePicker(context: Context?, listener: OnTimeSetListener?) {
        val calendar = Calendar.getInstance()
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val minute = calendar[Calendar.MINUTE]
        val timePickerDialog = TimePickerDialog(context, listener, hour, minute, true)
        timePickerDialog.show()
    }
}


