package com.priv.verifone.psdk.sdiapplication.ui.home

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.priv.verifone.psdk.sdiapplication.databinding.FragmentHomeBinding
import com.priv.verifone.psdk.sdiapplication.ui.viewmodel.PsdkViewModelFactory
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var _viewModel: HomeViewModel? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val viewModel get() = _viewModel!!

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

        // Set up the click listeners
        btnConnect.setOnClickListener {
            // Handle the connect action
            lifecycleScope.launch {
                viewModel.connect()
            }

        }
        binding.btnCrash.setOnClickListener {
            throw RuntimeException("This is a crash simulation.")
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
        // Set a listener for changes in the checkbox state
        // Set a listener for changes in the checkbox state
        binding.btnDarkMode.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // Enable dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                // Disable dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        })

        // Optionally, set the checkbox state based on the current theme

        // Optionally, set the checkbox state based on the current theme
        val currentNightMode = (resources.configuration.uiMode
                and Configuration.UI_MODE_NIGHT_MASK)
        binding.btnDarkMode.isChecked = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        return root
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