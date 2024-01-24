/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.verifone.psdk.sdiapplication.ui.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.verifone.psdk.sdiapplication.R

@BindingAdapter("LedImage")
fun ImageView.setLedImage(on: Boolean) {
    setImageResource(
        if (on) {
            R.drawable.led_on
        } else {
            R.drawable.led_off
        }
    )
}
