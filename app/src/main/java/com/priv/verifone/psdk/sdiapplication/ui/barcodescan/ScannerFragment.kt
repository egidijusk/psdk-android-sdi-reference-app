/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.ui.barcodescan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.priv.verifone.psdk.sdiapplication.databinding.FragmentScannerBinding
import com.priv.verifone.psdk.sdiapplication.ui.viewmodel.PsdkViewModelFactory
import com.priv.verifone.psdk.sdiapplication.utils.getAttributesForBarcodeScanning

class ScannerFragment : Fragment() {

    private var _binding: FragmentScannerBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val scannerViewModel = ViewModelProvider(
            requireActivity(),
            PsdkViewModelFactory(requireActivity().application)
        )[ScannerViewModel::class.java]

        _binding = FragmentScannerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.statusText
        scannerViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        binding.btnScanBarcode.setOnClickListener {
            scannerViewModel.scanBarCode(getAttributesForBarcodeScanning(requireContext()))
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}