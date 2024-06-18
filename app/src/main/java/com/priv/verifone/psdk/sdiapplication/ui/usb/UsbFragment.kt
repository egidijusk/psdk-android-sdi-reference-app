package com.priv.verifone.psdk.sdiapplication.ui.usb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.priv.verifone.psdk.sdiapplication.databinding.FragmentUsbBinding
import com.priv.verifone.psdk.sdiapplication.ui.viewmodel.PsdkViewModelFactory

class UsbFragment : Fragment() {

    private var _binding: FragmentUsbBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel = ViewModelProvider(requireActivity(),
            PsdkViewModelFactory(requireActivity().application)
        )[UsbViewModel::class.java]


        _binding = FragmentUsbBinding.inflate(inflater, container, false)
        val root: View = binding.root


        binding.btnConnect.setOnClickListener{
            viewModel.connect()
        }
        binding.btnDisconnect.setOnClickListener{
            viewModel.disconnect()
        }
        binding.btnSend.setOnClickListener{
            viewModel.send()
        }
        viewModel.receivedData.observe(viewLifecycleOwner, Observer{data->
            if (!data.isNullOrEmpty()) {
                binding.receivedData.text = data
            }
        })
        viewModel.operationStatus.observe(viewLifecycleOwner, Observer{status->
            if (!status.isNullOrEmpty()) {
                binding.status.text = status
            }
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}