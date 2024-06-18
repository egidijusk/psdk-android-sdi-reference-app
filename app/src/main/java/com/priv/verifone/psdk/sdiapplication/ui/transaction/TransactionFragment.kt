package com.priv.verifone.psdk.sdiapplication.ui.transaction

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.priv.verifone.psdk.sdiapplication.R
import com.priv.verifone.psdk.sdiapplication.databinding.FragmentTransactionBinding
import com.priv.verifone.psdk.sdiapplication.ui.viewmodel.PsdkViewModelFactory
import com.priv.verifone.psdk.sdiapplication.utils.getGlobalVisibleRectForView
import com.verifone.payment_sdk.SdiTouchButton

class TransactionFragment : Fragment() {
    companion object {
        const val TAG = "TransactionFragment"
    }

    private var _binding: FragmentTransactionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var transactionViewModel: TransactionViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        transactionViewModel = ViewModelProvider(
            requireActivity(),
            PsdkViewModelFactory(requireActivity().application)
        )[TransactionViewModel::class.java]


        _binding = FragmentTransactionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textview
        transactionViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        binding.btnStartTxn.setOnClickListener {
            hideKeyboard()
            transactionViewModel.setAmount(binding.amountEditText.text.toString())
            transactionViewModel.startTransaction()
        }
        binding.btnStartManualEntryTxn.setOnClickListener {
            hideKeyboard()
            transactionViewModel.startManualEntry()
        }
        binding.buttonTxnCancel.setOnClickListener {
            transactionViewModel.abort()
        }

        handleScreenChanges()
        handleLeds()

        // PIN Entry Handling

        transactionViewModel.sensitiveDataDigits.observe(viewLifecycleOwner) {
            binding.pinEntryDigits.text = it
        }
        transactionViewModel.sensitiveDataTitle.observe(viewLifecycleOwner) {
            binding.pinEntryTitle.text = it
        }
        transactionViewModel.sensitiveDataGreenButtonText.observe(viewLifecycleOwner) {
            binding.buttonConfirm.text = it
        }
        val layout = binding.pinEntryLayout
        val vto = layout.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (layout.visibility == View.VISIBLE) {
                    layout.viewTreeObserver
                        .removeOnGlobalLayoutListener(this)
                    transactionViewModel.setSensitiveDataTouchButtons(getSensitiveDataTouchButtons())
                }
            }
        })
        return root
    }

    private fun hideKeyboard() {

        val inputMethodManager = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
    }

    private fun handleLeds() {
        transactionViewModel.ledsState.observe(viewLifecycleOwner) {
            if (it) {
                binding.ctlsLedLayout.visibility = View.VISIBLE
            } else {
                binding.ctlsLedLayout.visibility = View.GONE
            }
        }
        transactionViewModel.led1.observe(viewLifecycleOwner) {
            if (it) {
                binding.ctlsLed1.setImageResource(R.drawable.led_on)
            } else {
                binding.ctlsLed1.setImageResource(R.drawable.led_off)
            }
        }
        transactionViewModel.led2.observe(viewLifecycleOwner) {
            if (it) {
                binding.ctlsLed2.setImageResource(R.drawable.led_on)
            } else {
                binding.ctlsLed2.setImageResource(R.drawable.led_off)
            }
        }
        transactionViewModel.led3.observe(viewLifecycleOwner) {
            if (it) {
                binding.ctlsLed3.setImageResource(R.drawable.led_on)
            } else {
                binding.ctlsLed3.setImageResource(R.drawable.led_off)
            }
        }
        transactionViewModel.led4.observe(viewLifecycleOwner) {
            if (it) {
                binding.ctlsLed4.setImageResource(R.drawable.led_on)
            } else {
                binding.ctlsLed4.setImageResource(R.drawable.led_off)
            }
        }
    }

    private fun handleScreenChanges() {
        transactionViewModel.transactionState.observe(viewLifecycleOwner) {
            when (it) {
                TransactionViewModel.State.Idle -> {
                    showMenu(true)
                    showCardEntry(false)
                    showSensitiveDataEntry(false)
                    binding.textview.text = ""

                }

                TransactionViewModel.State.TransactionInProgress -> {
                    binding.textview.text = "Present Card"
                    showCardEntry(true)
                    showMenu(false)
                    showSensitiveDataEntry(false)
                }

                TransactionViewModel.State.SensitiveDataEntry -> {
                    showSensitiveDataEntry(true)
                    showCardEntry(false)
                    showMenu(false)
                    binding.textview.text = ""
                }

                TransactionViewModel.State.RemoveCard -> {
                    showSensitiveDataEntry(false)
                    showCardEntry(false)
                    showMenu(false)
                    binding.textview.text = "Remove Card"
                }
            }
        }
    }

    private fun showSensitiveDataEntry(show: Boolean) {
        if (show) {
            binding.pinEntryLayout.visibility = View.VISIBLE
        } else {
            binding.pinEntryLayout.visibility = View.GONE
        }
    }

    private fun showCardEntry(show: Boolean) {
        if (show) {
            binding.ctlsLedLayout.visibility = View.VISIBLE
            binding.buttonTxnCancel.visibility = View.VISIBLE
        } else {
            binding.ctlsLedLayout.visibility = View.GONE
            binding.buttonTxnCancel.visibility = View.GONE
        }
    }

    private fun showMenu(show: Boolean) {
        if (show) {
            binding.menuLayout.visibility = View.VISIBLE
        } else {
            binding.menuLayout.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getSensitiveDataTouchButtons(): ArrayList<SdiTouchButton> {
        val buttons = ArrayList<SdiTouchButton>()

        var rect = getGlobalVisibleRectForView(binding.button1)
        var button = SdiTouchButton(
            rect.left.toShort(),
            rect.top.toShort(),
            rect.width().toShort(),
            rect.height().toShort(),
            0x31
        )

        //Log.d(TAG, "Touch Button 1: ${button.toString()}")
        buttons.add(button)

        rect = getGlobalVisibleRectForView(binding.button2)
        button = SdiTouchButton(
            rect.left.toShort(),
            rect.top.toShort(),
            rect.width().toShort(),
            rect.height().toShort(),
            0x32
        )
        buttons.add(button)
        //Log.d(TAG, "Touch Button 2: ${button.toString()}")

        rect = getGlobalVisibleRectForView(binding.button3)
        button = SdiTouchButton(
            rect.left.toShort(),
            rect.top.toShort(),
            rect.width().toShort(),
            rect.height().toShort(),
            0x33
        )
        buttons.add(button)
        //Log.d(TAG, "Touch Button 3: ${button.toString()}")

        rect = getGlobalVisibleRectForView(binding.button4)
        button = SdiTouchButton(
            rect.left.toShort(),
            rect.top.toShort(),
            rect.width().toShort(),
            rect.height().toShort(),
            0x34
        )
        buttons.add(button)
        //Log.d(TAG, "Touch Button 4: ${button.toString()}")

        rect = getGlobalVisibleRectForView(binding.button5)
        button = SdiTouchButton(
            rect.left.toShort(),
            rect.top.toShort(),
            rect.width().toShort(),
            rect.height().toShort(),
            0x35
        )
        buttons.add(button)
        //Log.d(TAG, "Touch Button 5: ${button.toString()}")

        rect = getGlobalVisibleRectForView(binding.button6)
        button = SdiTouchButton(
            rect.left.toShort(),
            rect.top.toShort(),
            rect.width().toShort(),
            rect.height().toShort(),
            0x36
        )
        buttons.add(button)
        //Log.d(TAG, "Touch Button 6: ${button.toString()}")

        rect = getGlobalVisibleRectForView(binding.button7)
        button = SdiTouchButton(
            rect.left.toShort(),
            rect.top.toShort(),
            rect.width().toShort(),
            rect.height().toShort(),
            0x37
        )
        buttons.add(button)
        //Log.d(TAG, "Touch Button 7: ${button.toString()}")

        rect = getGlobalVisibleRectForView(binding.button8)
        button = SdiTouchButton(
            rect.left.toShort(),
            rect.top.toShort(),
            rect.width().toShort(),
            rect.height().toShort(),
            0x38
        )
        buttons.add(button)
        //Log.d(TAG, "Touch Button 8: ${button.toString()}")

        rect = getGlobalVisibleRectForView(binding.button9)
        button = SdiTouchButton(
            rect.left.toShort(),
            rect.top.toShort(),
            rect.width().toShort(),
            rect.height().toShort(),
            0x39
        )
        buttons.add(button)
        //Log.d(TAG, "Touch Button 9: ${button.toString()}")

        rect = getGlobalVisibleRectForView(binding.button0)
        button = SdiTouchButton(
            rect.left.toShort(),
            rect.top.toShort(),
            rect.width().toShort(),
            rect.height().toShort(),
            0x30
        )
        buttons.add(button)
        //Log.d(TAG, "Touch Button 0: ${button.toString()}")

        rect = getGlobalVisibleRectForView(binding.buttonClear)
        button = SdiTouchButton(
            rect.left.toShort(),
            rect.top.toShort(),
            rect.width().toShort(),
            rect.height().toShort(),
            0x08
        )
        buttons.add(button)
        //Log.d(TAG, "Touch Button clear: ${button.toString()}")

        rect = getGlobalVisibleRectForView(binding.buttonCancel)
        button = SdiTouchButton(
            rect.left.toShort(),
            rect.top.toShort(),
            rect.width().toShort(),
            rect.height().toShort(),
            0x1B
        )
        buttons.add(button)
        //Log.d(TAG, "Touch Button cancel: ${button.toString()}")

        rect = getGlobalVisibleRectForView(binding.buttonConfirm)
        button = SdiTouchButton(
            rect.left.toShort(),
            rect.top.toShort(),
            rect.width().toShort(),
            rect.height().toShort(),
            0x0D
        )
        buttons.add(button)
        //Log.d(TAG, "Touch Button Confirm: ${button.toString()}")

        return buttons
    }
}