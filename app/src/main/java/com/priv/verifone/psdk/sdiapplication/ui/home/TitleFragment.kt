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

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.snackbar.Snackbar
import com.priv.verifone.psdk.sdiapplication.R
import com.priv.verifone.psdk.sdiapplication.databinding.FragmentTitleBinding
import com.priv.verifone.psdk.sdiapplication.viewmodel.PsdkViewModelFactory

class TitleFragment : Fragment() {

    private lateinit var viewModel: SdiConnectionViewModel
    private lateinit var binding: FragmentTitleBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<FragmentTitleBinding>(inflater,
                R.layout.fragment_title,container,false)

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), PsdkViewModelFactory(requireActivity().application)).get(
            SdiConnectionViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.statusMessage.observe(requireActivity(), Observer {
            if (it != null) { // Observed state is true.
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    it,
                    Snackbar.LENGTH_SHORT // How long to display the message.
                ).setTextColor(Color.GREEN).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        Log.d("TitleFragment", "Resume")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.options_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("PSDKV", "onOptionsItemSelected: " + R.id.connect + " " + item.itemId)
        val id = item.itemId; // Retrieve the id of the selected menu item

        when(id) {
            R.id.connect -> {
                Log.d("PSDKV", "initialize")
                binding.viewModel?.initialize()
            }
            R.id.disconnect -> {
                Log.d("PSDKV", "teardown")
                binding.viewModel?.teardown()
            }
        }
        return NavigationUI.onNavDestinationSelected(item,requireView().findNavController())
                ||super.onOptionsItemSelected(item)
    }
}
