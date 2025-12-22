package com.rsgl.cngpos.pos

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.rsgl.cngpos.databinding.FragmentTransactionPreviewBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import com.rsgl.cngpos.RetrofitClient
import com.rsgl.cngpos.payment.PaymentViewModel
import com.rsgl.cngpos.payment.PhonePeRepository
import com.phonepe.intent.sdk.api.PhonePeKt

class TransactionPreviewFragment : Fragment() {

    private var _binding: FragmentTransactionPreviewBinding? = null
    private val binding get() = _binding!!

    private var totalAmount: Double = 0.0
    private var saleId: String = ""
    private var transactionType: String = ""
    private var dispenser: String = ""
    private var nozzle: String = ""
    private var quantity: Double = 0.0
    private var pricePerKg: Double = 0.0
    private val viewModel: PaymentViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {

                val repository = PhonePeRepository(
                    api = RetrofitClient.phonePeApi
                )

                return PaymentViewModel(repository) as T
            }
        }
    }

    private val phonePeLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // STEP-4: Payment status check (backend)
                Log.d("PhonePe", "Payment finished, check status")
            } else {
                Log.e("PhonePe", "Payment cancelled / failed")
            }
        }

    private val TAG = "TransactionPreview"
    private var currentOrderId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadTransactionData()
        setupButtons()

        // ✅ COLLECT PAYMENT EVENT
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.paymentEvent.collect { response ->

                Log.d("PhonePe", "OrderId = ${response.orderId}")
                Log.d("PhonePe", "Token = ${response.token}")

                openPhonePe(
                    orderId = response.orderId,
                    token = response.token
                )
            }
        }
    }

    private fun loadTransactionData() {
        val args = TransactionPreviewFragmentArgs.fromBundle(requireArguments())
        transactionType = args.transactionType
        dispenser = args.dispenser
        nozzle = args.nozzle
        quantity = args.quantity.toDouble()
        pricePerKg = args.pricePerKg.toDouble()
        totalAmount = quantity * pricePerKg
        saleId = generateOrderId()

        val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault())
        val currentDateTime = dateFormat.format(Date())
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        binding.tvTransactionType.text = transactionType
        binding.tvOrderId.text = saleId
        binding.tvDateTime.text = currentDateTime
        binding.tvDispenser.text = dispenser
        binding.tvNozzle.text = nozzle
        binding.tvFuelType.text = "CNG"
        binding.tvQuantity.text = String.format("%.2f kg", quantity)
        binding.tvPricePerKg.text = currencyFormat.format(pricePerKg)
        binding.tvTotalAmount.text = currencyFormat.format(totalAmount)

        Log.d(TAG, "Total Amount: ₹$totalAmount")




    }

    private fun generateOrderId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (100000..999999).random()
        return "TXN${timestamp}${random}"
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnPayPhonePe.setOnClickListener {
            viewModel.startPayment(
                amount = totalAmount,
                saleId = saleId
            )
        }
    }

    private fun openPhonePe(orderId: String, token: String) {
        try {
            PhonePeKt.startCheckoutPage(
                context = requireActivity(),
                token = token,
                orderId = orderId,
                activityResultLauncher = phonePeLauncher
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}