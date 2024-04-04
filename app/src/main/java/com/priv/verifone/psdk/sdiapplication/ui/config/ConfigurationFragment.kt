/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.ui.config

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.priv.verifone.psdk.sdiapplication.R
import com.priv.verifone.psdk.sdiapplication.databinding.FragmentConfigurationBinding
import com.priv.verifone.psdk.sdiapplication.viewmodel.PsdkViewModelFactory

class ConfigurationFragment : Fragment() {

    private lateinit var viewModel: SdiConfigurationViewModel
    private lateinit var binding: FragmentConfigurationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<FragmentConfigurationBinding>(
            inflater,
            R.layout.fragment_configuration, container, false
        )

        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(
            requireActivity(),
            PsdkViewModelFactory(requireActivity().application)
        ).get(
            SdiConfigurationViewModel::class.java
        )
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.statusMessage.observe(viewLifecycleOwner, Observer {
            try {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    it,
                    Snackbar.LENGTH_SHORT // How long to display the message.
                ).setTextColor(Color.GREEN).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
        binding.lifecycleOwner = this
    }

    override fun onResume() {
        super.onResume()
        Log.d("ConfigurationFragment", "Resume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("ConfigurationFragment", "Pause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ConfigurationFragment", "onDestroy")
    }
}
