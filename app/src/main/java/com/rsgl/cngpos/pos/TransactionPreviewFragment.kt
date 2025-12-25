package com.rsgl.cngpos.pos

import android.app.Activity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rsgl.cngpos.databinding.FragmentTransactionPreviewBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import com.rsgl.cngpos.RetrofitClient
import com.rsgl.cngpos.payment.PaymentViewModel
import com.rsgl.cngpos.payment.PhonePeRepository
import com.phonepe.intent.sdk.api.PhonePeKt
import com.rsgl.cngpos.R
import com.rsgl.cngpos.payment.PaymentStatusResult
import com.rsgl.cngpos.payment.ExistingPaymentCheckResult

class TransactionPreviewFragment : Fragment() {

    private var _binding: FragmentTransactionPreviewBinding? = null
    private val binding get() = _binding!!

    private var totalAmount: Double = 0.0
    private var saleId: String = ""
    private var iotOrderId: Int = 0
    private var manualSaleId: Int = -1
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
            Log.d(TAG, "========================================")
            Log.d(TAG, "PhonePe callback received")
            Log.d(TAG, "Result Code: ${result.resultCode}")
            Log.d(TAG, "PhonePe Order ID: $currentOrderId")
            Log.d(TAG, "Merchant Order ID (saleId): $saleId")
            Log.d(TAG, "========================================")

            hasCheckedStatus = true

            Toast.makeText(
                requireContext(),
                "Checking payment status...",
                Toast.LENGTH_SHORT
            ).show()

            viewModel.checkPaymentStatus(saleId)
        }

    private val TAG = "TransactionPreview"
    private var currentOrderId: String = ""
    private var currentToken: String = ""
    private var hasCheckedStatus: Boolean = false
    private var phonePeToken: String = ""
    private var orderAlreadyCreated: Boolean = false
    private var isPaymentAlreadyCompleted: Boolean = false

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
        observePaymentEvents()
        observePaymentStatus()
        observeDbUpdateStatus()

        checkAndAutoCreateOrder()
    }

    override fun onResume() {
        super.onResume()

        if (currentOrderId.isNotEmpty() && !hasCheckedStatus && !isPaymentAlreadyCompleted) {
            Log.d(TAG, "========================================")
            Log.d(TAG, "Fragment resumed - checking payment status")
            Log.d(TAG, "Order ID: $currentOrderId")
            Log.d(TAG, "========================================")

            Toast.makeText(
                requireContext(),
                "Checking payment status...",
                Toast.LENGTH_SHORT
            ).show()

            hasCheckedStatus = true
            viewModel.checkPaymentStatus(currentOrderId)
        }
    }

    private fun observePaymentStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.paymentStatusEvent.collect { result ->
                when (result) {
                    is PaymentStatusResult.Success -> {
                        Log.d(TAG, "✅ PAYMENT SUCCESS")
                        Log.d(TAG, "Transaction ID: ${result.transactionId}")
                        Log.d(TAG, "Amount: ₹${result.amount}")

                        // ✅ FIXED: Properly validate and pass IDs
                        val updateIotOrderId = if (iotOrderId > 0) iotOrderId else null
                        val updateManualSaleId = if (manualSaleId > 0) manualSaleId else null

                        Log.d(TAG, "Updating DB - IOT: $updateIotOrderId, Manual: $updateManualSaleId")

                        viewModel.updatePaymentStatusInDb(
                            iotOrderId = updateIotOrderId,
                            manualSaleId = updateManualSaleId,
                            status = "PAID",
                            transactionId = result.transactionId ?: "PHONEPE_${System.currentTimeMillis()}",
                            amountPaid = result.amount
                        )

                        isPaymentAlreadyCompleted = true
                        binding.btnPayPhonePe.isEnabled = false
                        binding.btnPayPhonePe.text = "Payment Completed ✓"

                        Toast.makeText(
                            requireContext(),
                            "✅ Payment Successful! ₹${result.amount}",
                            Toast.LENGTH_LONG
                        ).show()

                        android.os.Handler(Looper.getMainLooper()).postDelayed({
                            navigateToSuccessScreen(result)  // Navigate to success screen
                        }, 1500)
                    }

                    is PaymentStatusResult.Failed -> {
                        Log.e(TAG, "❌ PAYMENT FAILED: ${result.errorMessage}")

                        // ✅ FIXED: Properly validate and pass IDs
                        val updateIotOrderId = if (iotOrderId > 0) iotOrderId else null
                        val updateManualSaleId = if (manualSaleId > 0) manualSaleId else null

                        viewModel.updatePaymentStatusInDb(
                            iotOrderId = updateIotOrderId,
                            manualSaleId = updateManualSaleId,
                            status = "FAILED",
                            transactionId = currentOrderId,
                            amountPaid = null
                        )

                        binding.btnPayPhonePe.isEnabled = true
                        binding.btnPayPhonePe.text = "Retry Payment"

                        Toast.makeText(
                            requireContext(),
                            "❌ Payment Failed: ${result.errorMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is PaymentStatusResult.Pending -> {
                        Log.w(TAG, "⏳ PAYMENT PENDING")

                        // ✅ FIXED: Properly validate and pass IDs
                        val updateIotOrderId = if (iotOrderId > 0) iotOrderId else null
                        val updateManualSaleId = if (manualSaleId > 0) manualSaleId else null

                        viewModel.updatePaymentStatusInDb(
                            iotOrderId = updateIotOrderId,
                            manualSaleId = updateManualSaleId,
                            status = "PENDING",
                            transactionId = result.orderId,
                            amountPaid = null
                        )

                        binding.btnPayPhonePe.isEnabled = true
                        binding.btnPayPhonePe.text = "Complete Payment"

                        Toast.makeText(
                            requireContext(),
                            "⏳ Payment is pending\nPlease check your UPI app",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is PaymentStatusResult.Error -> {
                        Log.e(TAG, "💥 ERROR: ${result.message}")

                        binding.btnPayPhonePe.isEnabled = true
                        binding.btnPayPhonePe.text = "Retry Payment"

                        Toast.makeText(
                            requireContext(),
                            "⚠️ Error: ${result.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> { }
                }
            }
        }
    }

    private fun observeDbUpdateStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dbUpdateStatus.collect { response ->
                response?.let {
                    if (it.success) {
                        Log.d(TAG, "✅ DB Update Success: ${it.message}")
                        Log.d(TAG, "Affected Rows: ${it.affected_rows}")
                        Log.d(TAG, "Updated Record: ${it.updated_record}")
                    } else {
                        Log.e(TAG, "❌ DB Update Failed: ${it.message}")
                        Toast.makeText(
                            requireContext(),
                            "Warning: DB update failed - ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
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

        iotOrderId = args.iotOrderId
        manualSaleId = args.manualSaleId

        if (iotOrderId == 0 && manualSaleId == -1) {
            Log.e(TAG, "⚠️ WARNING: Both IOT and Manual Sale IDs are invalid!")
            Toast.makeText(
                requireContext(),
                "Error: No valid sale ID found!",
                Toast.LENGTH_LONG
            ).show()
        }

        Log.d(TAG, "Transaction Data Loaded:")
        Log.d(TAG, "Type: $transactionType")
        Log.d(TAG, "IOT Order ID: $iotOrderId")
        Log.d(TAG, "Manual Sale ID: $manualSaleId")
        Log.d(TAG, "Sale ID (Merchant Order ID): $saleId")
        Log.d(TAG, "Total Amount: ₹$totalAmount")

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
    }

    private fun checkAndAutoCreateOrder() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "========================================")
                Log.d(TAG, "🚀 AUTO ORDER CREATION STARTED")
                Log.d(TAG, "Merchant Order ID: $saleId")
                Log.d(TAG, "========================================")

                binding.btnPayPhonePe.isEnabled = false
                binding.btnPayPhonePe.text = "Checking..."

                Log.d(TAG, "Step 1: Getting PhonePe token...")
                val tokenResponse = RetrofitClient.phonePeApi.getPhonePeToken()
                phonePeToken = tokenResponse.access_token
                Log.d(TAG, "✅ Token received")

                Log.d(TAG, "Step 2: Checking if order already exists...")
                viewModel.checkExistingPayment(phonePeToken, saleId)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error in auto order creation", e)
                binding.btnPayPhonePe.isEnabled = true
                binding.btnPayPhonePe.text = "Retry Payment"

                Toast.makeText(
                    requireContext(),
                    "Failed to check order: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun observePaymentEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.paymentEvent.collect { response ->
                Log.d(TAG, "========================================")
                Log.d(TAG, "✅ PAYMENT ORDER CREATED")
                Log.d(TAG, "OrderId = ${response.orderId}")
                Log.d(TAG, "Token = ${response.token.take(20)}...")
                Log.d(TAG, "State = ${response.state}")
                Log.d(TAG, "========================================")

                currentOrderId = response.orderId
                currentToken = response.token
                orderAlreadyCreated = true
                hasCheckedStatus = false

                binding.btnPayPhonePe.isEnabled = true
                binding.btnPayPhonePe.text = "Pay with PhonePe"

                Log.d(TAG, "Order ready - waiting for user to click button")
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.existingPaymentStatus.collect { result ->
                result?.let {
                    Log.d(TAG, "Existing payment check result: $it")
                    handleExistingPaymentStatus(it)
                }
            }
        }
    }

    private fun handleExistingPaymentStatus(result: ExistingPaymentCheckResult) {
        when (result) {
            is ExistingPaymentCheckResult.AlreadyPaid -> {
                Log.d(TAG, "========================================")
                Log.d(TAG, "✅ PAYMENT ALREADY EXISTS!")
                Log.d(TAG, "Transaction ID: ${result.transactionId}")
                Log.d(TAG, "Amount: ₹${result.amount}")
                Log.d(TAG, "========================================")

                // ✅ FIXED: Properly validate and pass IDs
                val updateIotOrderId = if (iotOrderId > 0) iotOrderId else null
                val updateManualSaleId = if (manualSaleId > 0) manualSaleId else null

                viewModel.updatePaymentStatusInDb(
                    iotOrderId = updateIotOrderId,
                    manualSaleId = updateManualSaleId,
                    status = "PAID",
                    transactionId = result.transactionId,
                    amountPaid = result.amount
                )

                isPaymentAlreadyCompleted = true
                binding.btnPayPhonePe.isEnabled = false
                binding.btnPayPhonePe.text = "Already Paid ✓"

                Toast.makeText(
                    requireContext(),
                    "✅ Payment already completed!\n₹${result.amount}",
                    Toast.LENGTH_LONG
                ).show()

                binding.root.postDelayed({
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }, 2000)
            }

            is ExistingPaymentCheckResult.Pending -> {
                Log.d(TAG, "⏳ Payment is PENDING")

                // ✅ FIXED: Properly validate and pass IDs
                val updateIotOrderId = if (iotOrderId > 0) iotOrderId else null
                val updateManualSaleId = if (manualSaleId > 0) manualSaleId else null

                viewModel.updatePaymentStatusInDb(
                    iotOrderId = updateIotOrderId,
                    manualSaleId = updateManualSaleId,
                    status = "PENDING",
                    transactionId = currentOrderId,
                    amountPaid = null
                )

                binding.btnPayPhonePe.isEnabled = true
                binding.btnPayPhonePe.text = "Complete Payment"

                Toast.makeText(
                    requireContext(),
                    "⏳ Previous payment pending. Please complete.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is ExistingPaymentCheckResult.Failed -> {
                Log.d(TAG, "❌ Previous payment FAILED")

                // ✅ FIXED: Properly validate and pass IDs
                val updateIotOrderId = if (iotOrderId > 0) iotOrderId else null
                val updateManualSaleId = if (manualSaleId > 0) manualSaleId else null

                viewModel.updatePaymentStatusInDb(
                    iotOrderId = updateIotOrderId,
                    manualSaleId = updateManualSaleId,
                    status = "FAILED",
                    transactionId = currentOrderId,
                    amountPaid = null
                )

                binding.btnPayPhonePe.isEnabled = true
                binding.btnPayPhonePe.text = "Retry Payment"

                Toast.makeText(
                    requireContext(),
                    "Previous payment failed. Please retry.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is ExistingPaymentCheckResult.NotFound -> {
                Log.d(TAG, "========================================")
                Log.d(TAG, "🆕 NO EXISTING PAYMENT FOUND")
                Log.d(TAG, "📦 CREATING ORDER...")
                Log.d(TAG, "========================================")

                binding.btnPayPhonePe.text = "Creating order..."

                viewModel.startPayment(
                    amount = totalAmount,
                    saleId = saleId
                )
            }

            is ExistingPaymentCheckResult.Error -> {
                Log.e(TAG, "💥 Error checking existing payment: ${result.message}")
                binding.btnPayPhonePe.isEnabled = true
                binding.btnPayPhonePe.text = "Retry"

                Toast.makeText(
                    requireContext(),
                    "Error: ${result.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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
            Log.d(TAG, "========================================")
            Log.d(TAG, "📘 PAY BUTTON CLICKED")
            Log.d(TAG, "Payment already completed: $isPaymentAlreadyCompleted")
            Log.d(TAG, "Order already created: $orderAlreadyCreated")
            Log.d(TAG, "========================================")

            // ✅ PREVENT RE-PAYMENT if already completed
            if (isPaymentAlreadyCompleted) {
                Toast.makeText(
                    requireContext(),
                    "Payment already completed!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (orderAlreadyCreated && currentOrderId.isNotEmpty() && currentToken.isNotEmpty()) {
                Log.d(TAG, "Opening PhonePe with existing order")
                openPhonePe(currentOrderId, currentToken)
            } else {
                Log.d(TAG, "Order not ready, creating now...")
                binding.btnPayPhonePe.isEnabled = false
                binding.btnPayPhonePe.text = "Creating order..."

                viewModel.startPayment(
                    amount = totalAmount,
                    saleId = saleId
                )
            }
        }
    }

    private fun openPhonePe(orderId: String, token: String) {
        try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "📱 OPENING PHONEPE APP")
            Log.d(TAG, "Order ID: $orderId")
            Log.d(TAG, "========================================")

            PhonePeKt.startCheckoutPage(
                context = requireActivity(),
                token = token,
                orderId = orderId,
                activityResultLauncher = phonePeLauncher
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error opening PhonePe: ${e.message}")
            binding.btnPayPhonePe.isEnabled = true
            binding.btnPayPhonePe.text = "Retry Payment"

            Toast.makeText(
                requireContext(),
                "Failed to open PhonePe: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }

    private fun navigateToSuccessScreen(result: PaymentStatusResult.Success) {
        val bundle = Bundle().apply {
            putString("orderId", saleId) // Merchant Order ID
            putString("transactionId", result.transactionId ?: "")
            putString("paymentMode", "UPI (PhonePe)")

            // Format date time
            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault())
            putString("dateTime", sdf.format(Date()))

            putString("dispenser", dispenser)
            putString("nozzle", nozzle)
            putString("fuelType", "CNG")
            putDouble("quantity", quantity)
            putDouble("pricePerKg", pricePerKg)
            putDouble("totalPaid", result.amount)
        }

        findNavController().navigate(
            R.id.action_transactionPreview_to_transactionSuccess,
            bundle
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}