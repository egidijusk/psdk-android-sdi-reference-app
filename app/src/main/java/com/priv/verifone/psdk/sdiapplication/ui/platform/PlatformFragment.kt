/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.ui.platform

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.priv.verifone.psdk.sdiapplication.R
import com.priv.verifone.psdk.sdiapplication.databinding.FragmentPlatformBinding
import com.priv.verifone.psdk.sdiapplication.viewmodel.PsdkViewModelFactory

class PlatformFragment : Fragment() {

    companion object {
        private const val TAG = "PlatformFragment"
    }

    private lateinit var viewModel: PlatformViewModel
    private lateinit var binding: FragmentPlatformBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_platform, container, false)
        setHasOptionsMenu(true)
        checkAndRequestSystemWritePermission()
        viewModel = ViewModelProvider(
            requireActivity(), PsdkViewModelFactory(requireActivity().application)
        ).get(PlatformViewModel::class.java)
        viewModel.setCurrentMode()
        binding.viewModel = viewModel
        binding.brightnessSlider.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.setBrightness(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        viewModel.getBrightness().observe(requireActivity(),
            Observer<Int> { newBrightness: Int? ->
                binding.brightnessSlider.setProgress(
                    newBrightness!!
                )
            })

        var isUpdatingCheck = false
        viewModel.getBluetoothEnabled()?.observe(viewLifecycleOwner, Observer { isEnabled ->
            isUpdatingCheck = true  // Set flag to indicate programmatic change
            binding.switchBluetooth.isChecked = isEnabled!!
            isUpdatingCheck = false // Reset flag after update
        })

        binding.switchBluetooth.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (!isUpdatingCheck) {  // Only react to user-driven changes
                viewModel.toggleBluetooth(isChecked)
            }
        })
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun checkAndRequestSystemWritePermission() {
        if (!Settings.System.canWrite(requireActivity())) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + requireActivity().packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}