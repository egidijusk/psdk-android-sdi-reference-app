package com.priv.verifone.psdk.sdiapplication.ui.configuration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.priv.verifone.psdk.sdiapplication.databinding.FragmentConfigurationBinding
import com.priv.verifone.psdk.sdiapplication.databinding.FragmentUpdateserviceBinding
import com.priv.verifone.psdk.sdiapplication.ui.viewmodel.PsdkViewModelFactory

class ConfigurationFragment : Fragment() {

    private var _binding: FragmentConfigurationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val configurationViewModel = ViewModelProvider(requireActivity(),
            PsdkViewModelFactory(requireActivity().application)
        )[ConfigurationViewModel::class.java]


        _binding = FragmentConfigurationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSlideshow
        configurationViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        binding.btnCtConfig.setOnClickListener{
            configurationViewModel.setContactConfiguration()
        }
        binding.btnCtTlvConfig.setOnClickListener{
            configurationViewModel.setContactConfigThroughTlvAccess()
        }
        binding.btnCtlsConfig.setOnClickListener{
            configurationViewModel.setContactlessConfiguration()
        }
        binding.btnCtlsTlvConfig.setOnClickListener {
            configurationViewModel.setContactlessConfigThroughTlvAccess()
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}