/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.ui.home

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.priv.verifone.psdk.sdiapplication.PSDKContext
import com.priv.verifone.psdk.sdiapplication.databinding.FragmentHomeBinding
import com.priv.verifone.psdk.sdiapplication.ui.utils.DateTimePickerUtil
import com.priv.verifone.psdk.sdiapplication.ui.utils.DateTimeUtil
import com.priv.verifone.psdk.sdiapplication.ui.viewmodel.PsdkViewModelFactory
import com.verifone.payment_sdk.SdiNfc
import com.verifone.payment_sdk.SdiNfcPollingBitmap
import com.verifone.payment_sdk.SdiResultCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.EnumSet
import java.util.Locale

class HomeFragment : Fragment() {

    private val TAG = "HomeFragment"
    
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private var _viewModel: HomeViewModel? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val viewModel get() = _viewModel!!

    private lateinit var calendar:Calendar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewModel = ViewModelProvider(requireActivity(),
            PsdkViewModelFactory(requireActivity().application))[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textview
        viewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        val infoView: TextView = binding.infoview
        viewModel.info.observe(viewLifecycleOwner) {
            Log.d("Abilash", it.toString())
            infoView.text = it
        }

        viewModel.textMode.observe(viewLifecycleOwner) {
            binding.textMode.text = it
        }
        // Access the button by its ID
        val btnConnect = binding.btnConnect
        val btnDisconnect = binding.btnDisconnect
        val btnLogs = binding.btnLogs
        calendar = Calendar.getInstance()
        // Set up the click listeners
        btnConnect.setOnClickListener {
            // Handle the connect action
            lifecycleScope.launch {
                viewModel.connect()
            }
        }
        binding.btnCrash.setOnClickListener {
            val nfc = (requireActivity().application as PSDKContext).paymentSDK.sdiManager.nfc
            readNfcA(nfc)
        }

        btnDisconnect.setOnClickListener {
            // Handle the disconnect action
            viewModel.disconnect()
        }
        binding.btnStandard.setOnClickListener {
            // Handle the disconnect action
            viewModel.useStandardMode()
        }
        binding.btnKiosk.setOnClickListener {
            // Handle the disconnect action
            viewModel.useKioskMode()
        }
        btnLogs.setOnClickListener {
            // Handle the disconnect action
            viewModel.transferLogs()
        }
        binding.btnKeyboardBacklight.setOnClickListener {
            // Handle the disconnect action
            viewModel.toggleKeyboardBacklight()
        }

        // Set a listener for changes in the checkbox state
        binding.btnDarkMode.setOnCheckedChangeListener{ buttonView, isChecked ->
            if (isChecked) {
                // Enable dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                // Disable dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Optionally, set the checkbox state based on the current theme
        val currentNightMode = (resources.configuration.uiMode
                and Configuration.UI_MODE_NIGHT_MASK)
        binding.btnDarkMode.isChecked = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        binding.btnSetDateTime.setOnClickListener { v -> showDateTimePicker() }

        viewModel.keyboardPresent.observe(viewLifecycleOwner, Observer {
            if (it!= null) {
                if (it) {
                    binding.btnKeyboardBacklight.visibility = View.VISIBLE
                } else {
                    binding.btnKeyboardBacklight.visibility = View.INVISIBLE
                }
            }
        })

        return root
    }

    private fun readNfcA(nfc: SdiNfc) {
        val nfcPollingSet: EnumSet<SdiNfcPollingBitmap> = EnumSet.of(
            SdiNfcPollingBitmap.A
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                var sdiResultCode = nfc.open()
                if (sdiResultCode != SdiResultCode.OK) {
                    android.util.Log.d(TAG, "nfc.open() - $sdiResultCode")
                    return@launch
                }

                sdiResultCode = nfc.fieldOn()
                if (sdiResultCode != SdiResultCode.OK) {
                    android.util.Log.d(TAG, "nfc.fieldOn() - $sdiResultCode")
                    return@launch
                }

                var sdiNfcPollResult = nfc.fieldPollingExt(nfcPollingSet, 4000, byteArrayOf())
                if (sdiNfcPollResult.result != SdiResultCode.OK) {
                    android.util.Log.d(TAG, "nfc.fieldPolling() - ${sdiNfcPollResult.result}")
                    return@launch
                }

                val sdiNfcCard = sdiNfcPollResult.detectedCards?.firstOrNull()
                if (sdiNfcCard === null) {
                    android.util.Log.d(TAG, "no cards detected")
                    return@launch
                }

                android.util.Log.d(TAG, "Card UID - ${sdiNfcCard.cardInfo.toHexString()}")

                sdiResultCode = nfc.fieldActivation(sdiNfcCard.cardType.toLong(), byteArrayOf())
                if (sdiResultCode != SdiResultCode.OK) {
                    android.util.Log.d(TAG, "nfc.fieldActivation() - $sdiResultCode")
                    return@launch
                }

                val sdiBinaryResponse = nfc.mifareRead(sdiNfcCard.cardType, 0, 1)
                if (sdiBinaryResponse.result != SdiResultCode.OK) {
                    android.util.Log.d(TAG, "nfc.mifareRead() - ${sdiBinaryResponse.result}")
                    return@launch
                }

                android.util.Log.d(TAG, "nfc.mifareRead() - ${sdiBinaryResponse.response.toHexString()}")
            } catch (e: Exception) {
                android.util.Log.d(TAG, e.stackTraceToString())
            } finally {
                var sdiResultCode = nfc.fieldOff()
                if (sdiResultCode != SdiResultCode.OK) {
                    android.util.Log.d(TAG, "nfc.fieldOff() - $sdiResultCode")
                }

                sdiResultCode = nfc.close()
                if (sdiResultCode != SdiResultCode.OK) {
                    android.util.Log.d(TAG, "nfc.close() - $sdiResultCode")
                }
            }
        }
    }

    fun ByteArray.toHexString(): String {
        return joinToString("") { "%02X".format(it) }
    }

    private fun showDateTimePicker() {
        DateTimePickerUtil.showDatePicker(requireContext()) { view, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            DateTimePickerUtil.showTimePicker(requireContext()) { view1, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)

                // Now set the date and time using the selected values
                //setDateTime(calendar.timeInMillis)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

                viewModel.setDateTime(dateFormat.format(calendar.time))
            }
        }
    }

    private fun setDateTime(timestamp: Long) {
        DateTimeUtil.setDateTime(requireContext(), timestamp)
    }

    override fun onStart() {
        super.onStart()
        viewModel.setCurrentMode()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}