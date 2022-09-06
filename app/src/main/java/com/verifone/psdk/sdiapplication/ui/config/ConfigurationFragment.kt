/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.verifone.psdk.sdiapplication.ui.config


import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.verifone.psdk.sdiapplication.R
import com.verifone.psdk.sdiapplication.databinding.FragmentConfigurationBinding
import com.verifone.psdk.sdiapplication.viewmodel.PsdkViewModelFactory


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
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
