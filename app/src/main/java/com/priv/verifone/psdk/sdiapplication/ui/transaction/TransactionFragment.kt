/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.ui.transaction

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.priv.verifone.psdk.sdiapplication.R
import com.priv.verifone.psdk.sdiapplication.databinding.FragmentTxnBinding
import com.priv.verifone.psdk.sdiapplication.ui.utils.getGlobalVisibleRectForView
import com.priv.verifone.psdk.sdiapplication.viewmodel.PsdkViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.verifone.payment_sdk.SdiTouchButton

class TransactionFragment : Fragment() {

    private lateinit var viewModel: SdiTransactionViewModel
    private lateinit var binding: FragmentTxnBinding

    companion object {
        private const val TAG = "EMVTransactionFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<FragmentTxnBinding>(
            inflater,
            R.layout.fragment_txn, container, false
        )

        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(requireActivity(), PsdkViewModelFactory(requireActivity().application)
        ).get(SdiTransactionViewModel::class.java)
        binding.viewModel = viewModel
        val layout = binding.pinEntryLayout
        val vto = layout.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (layout.visibility == View.VISIBLE) {
                    layout.viewTreeObserver
                        .removeOnGlobalLayoutListener(this)
                    viewModel.setSensitiveDataTouchButtons(getSensitiveDataTouchButtons())
                }
            }
        })

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

    // Here POS app maps the touch co-ordinates with the dynamically created buttons
    // POS app can create their own buttons but have to map the specified value for respective buttons
    private fun getSensitiveDataTouchButtons(): ArrayList<SdiTouchButton> {
        val buttons = ArrayList<SdiTouchButton>()

        var rect = getGlobalVisibleRectForView(binding.button1)
        var button = SdiTouchButton(rect.left.toShort(), rect.top.toShort(), rect.width().toShort(), rect.height().toShort(), 0x31)

        //Log.d(TAG, "Touch Button 1: ${button.toString()}")
        buttons.add(button)

        rect = getGlobalVisibleRectForView(binding.button2)
        button = SdiTouchButton(rect.left.toShort(), rect.top.toShort(), rect.width().toShort(), rect.height().toShort(), 0x32)
        buttons.add(button)
        //Log.d(TAG, "Touch Button 2: ${button.toString()}")

        rect = getGlobalVisibleRectForView(binding.button3)
        button = SdiTouchButton(rect.left.toShort(), rect.top.toShort(), rect.width().toShort(), rect.height().toShort(), 0x33)
        buttons.add(button)
        //Log.d(TAG, "Touch Button 3: ${button.toString()}")

        rect = getGlobalVisibleRectForView(binding.button4)
        button = SdiTouchButton(rect.left.toShort(), rect.top.toShort(), rect.width().toShort(), rect.height().toShort(), 0x34)
        buttons.add(button)
        //Log.d(TAG, "Touch Button 4: ${button.toString()}")

        rect = getGlobalVisibleRectForView(binding.button5)
        button = SdiTouchButton(rect.left.toShort(), rect.top.toShort(), rect.width().toShort(), rect.height().toShort(), 0x35)
        buttons.add(button)
        //Log.d(TAG, "Touch Button 5: ${button.toString()}")

        rect = getGlobalVisibleRectForView(binding.button6)
        button = SdiTouchButton(rect.left.toShort(), rect.top.toShort(), rect.width().toShort(), rect.height().toShort(), 0x36)
        buttons.add(button)
        //Log.d(TAG, "Touch Button 6: ${button.toString()}")

        rect = getGlobalVisibleRectForView(binding.button7)
        button = SdiTouchButton(rect.left.toShort(), rect.top.toShort(), rect.width().toShort(), rect.height().toShort(), 0x37)
        buttons.add(button)
        //Log.d(TAG, "Touch Button 7: ${button.toString()}")

        rect = getGlobalVisibleRectForView(binding.button8)
        button = SdiTouchButton(rect.left.toShort(), rect.top.toShort(), rect.width().toShort(), rect.height().toShort(), 0x38)
        buttons.add(button)
        //Log.d(TAG, "Touch Button 8: ${button.toString()}")

        rect = getGlobalVisibleRectForView(binding.button9)
        button = SdiTouchButton(rect.left.toShort(), rect.top.toShort(), rect.width().toShort(), rect.height().toShort(), 0x39)
        buttons.add(button)
        //Log.d(TAG, "Touch Button 9: ${button.toString()}")

        rect = getGlobalVisibleRectForView(binding.button0)
        button = SdiTouchButton(rect.left.toShort(), rect.top.toShort(), rect.width().toShort(), rect.height().toShort(), 0x30)
        buttons.add(button)
        //Log.d(TAG, "Touch Button 0: ${button.toString()}")

        rect = getGlobalVisibleRectForView(binding.buttonClear)
        button = SdiTouchButton(rect.left.toShort(), rect.top.toShort(), rect.width().toShort(), rect.height().toShort(), 0x08)
        buttons.add(button)
        //Log.d(TAG, "Touch Button clear: ${button.toString()}")

        rect = getGlobalVisibleRectForView(binding.buttonCancel)
        button = SdiTouchButton(rect.left.toShort(), rect.top.toShort(), rect.width().toShort(), rect.height().toShort(), 0x1B)
        buttons.add(button)
        //Log.d(TAG, "Touch Button cancel: ${button.toString()}")

        rect = getGlobalVisibleRectForView(binding.buttonConfirm)
        button = SdiTouchButton(rect.left.toShort(), rect.top.toShort(), rect.width().toShort(), rect.height().toShort(), 0x0D)
        buttons.add(button)
        //Log.d(TAG, "Touch Button Confirm: ${button.toString()}")

        return buttons
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "Start")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Resume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Pause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }
}
