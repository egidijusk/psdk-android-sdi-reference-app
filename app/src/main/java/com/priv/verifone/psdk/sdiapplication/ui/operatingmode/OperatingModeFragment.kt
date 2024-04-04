/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.ui.operatingmode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.priv.verifone.psdk.sdiapplication.R
import com.priv.verifone.psdk.sdiapplication.databinding.FragmentOperatingModeBinding
import com.priv.verifone.psdk.sdiapplication.databinding.FragmentUpdateBinding
import com.priv.verifone.psdk.sdiapplication.viewmodel.PsdkViewModelFactory

class OperatingModeFragment : Fragment() {

    companion object {
        private const val TAG = "OperatingModeFragment"
    }

    private lateinit var viewModel: OperatingModeViewModel
    private lateinit var binding: FragmentOperatingModeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_operating_mode, container, false)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(
            requireActivity(), PsdkViewModelFactory(requireActivity().application)
        ).get(OperatingModeViewModel::class.java)
        viewModel.setCurrentMode()
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = this
    }
}