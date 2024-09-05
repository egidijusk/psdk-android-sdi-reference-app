/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.ui.peripherals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.priv.verifone.psdk.sdiapplication.databinding.FragmentPeripheralBinding
import com.priv.verifone.psdk.sdiapplication.ui.viewmodel.PsdkViewModelFactory
import com.priv.verifone.psdk.sdiapplication.utils.getAttributesForBarcodeScanning

class PeripheralFragment : Fragment() {

    private var _binding: FragmentPeripheralBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val peripheralViewModel = ViewModelProvider(
            requireActivity(),
            PsdkViewModelFactory(requireActivity().application)
        )[PeripheralViewModel::class.java]

        _binding = FragmentPeripheralBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.statusText
        peripheralViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        binding.btnScanBarcode.setOnClickListener {
            peripheralViewModel.scanBarCode(getAttributesForBarcodeScanning(requireContext()))
        }
        binding.btnPrintBitmap.setOnClickListener {
            peripheralViewModel.printBitmapReceipt()
        }
        binding.btnPrintHtml.setOnClickListener {
            peripheralViewModel.printHTMLReceipt()
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}