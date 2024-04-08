/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.ui.usb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.priv.verifone.psdk.sdiapplication.R
import com.priv.verifone.psdk.sdiapplication.databinding.FragmentUsbBinding
import com.priv.verifone.psdk.sdiapplication.viewmodel.PsdkViewModelFactory

class UsbFragment : Fragment() {

    companion object {
        private const val TAG = "UsbFragment"
    }

    private lateinit var viewModel: UsbViewModel
    private lateinit var binding: FragmentUsbBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_usb, container, false)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(
            requireActivity(), PsdkViewModelFactory(requireActivity().application)
        ).get(UsbViewModel::class.java)
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
    }
}