/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.ui.updateservice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.priv.verifone.psdk.sdiapplication.databinding.FragmentUpdateserviceBinding
import com.priv.verifone.psdk.sdiapplication.ui.viewmodel.PsdkViewModelFactory

class UpdateServiceFragment : Fragment() {

    private var _binding: FragmentUpdateserviceBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val updateServiceViewModel = ViewModelProvider(requireActivity(),
            PsdkViewModelFactory(requireActivity().application)
        )[UpdateserviceViewModel::class.java]

        _binding = FragmentUpdateserviceBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.statusText
        updateServiceViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        binding.btnInstallApk.setOnClickListener{
            updateServiceViewModel.installApk()
        }
        binding.btnInstallOta.setOnClickListener{
            updateServiceViewModel.installAndroidOtaPackage()
        }
        binding.btnUninstallApk.setOnClickListener{
            updateServiceViewModel.unInstallApk()
        }
        binding.btnSuperPackage.setOnClickListener{
            updateServiceViewModel.installSuperPackage()
        }
        binding.btnVrkPayload.setOnClickListener{
            updateServiceViewModel.installVrkPayload()
        }
        binding.btnLastRecoveryLog.setOnClickListener{
            updateServiceViewModel.fetchLastRecoveryStatus()
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}