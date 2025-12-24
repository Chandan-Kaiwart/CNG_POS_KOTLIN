package com.rsgl.cngpos.payment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rsgl.cngpos.payment.utils.OrderResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class PaymentViewModel(
    private val repository: PhonePeRepository
) : ViewModel() {
    private val TAG = "PaymentViewModel"
    private val _paymentEvent = MutableSharedFlow<OrderResponse>()
    val paymentEvent = _paymentEvent.asSharedFlow()

    private val _existingPaymentStatus = MutableStateFlow<ExistingPaymentCheckResult?>(null)
    val existingPaymentStatus: StateFlow<ExistingPaymentCheckResult?> = _existingPaymentStatus.asStateFlow()

    // ✅ Payment status flow
    private val _paymentStatusEvent = MutableStateFlow<PaymentStatusResult?>(null)
    val paymentStatusEvent = _paymentStatusEvent.asStateFlow()

    fun startPayment(amount: Double, saleId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "========================================")
                Log.d(TAG, "📱 STARTING PAYMENT")
                Log.d(TAG, "Amount: ₹$amount")
                Log.d(TAG, "Sale ID: $saleId")
                Log.d(TAG, "========================================")

                Log.d(TAG, "Step 1: Fetching token...")
                val tokenRes = repository.fetchToken()
                Log.d(TAG, "✅ Token received: ${tokenRes.access_token.take(20)}...")
                Log.d(TAG, "Token expires in: ${tokenRes.expires_in} seconds")

                Log.d(TAG, "Step 2: Creating order...")
                Log.d(TAG, "Calling repository.createOrder()")
                Log.d(TAG, "  - token: ${tokenRes.access_token.take(20)}...")
                Log.d(TAG, "  - amount: $amount (will be ${(amount * 100).toInt()} paisa)")
                Log.d(TAG, "  - saleId: $saleId")

                val orderRes = repository.createOrder(
                    token = tokenRes.access_token,
                    amount = amount,
                    saleId = saleId
                )

                Log.d(TAG, "✅ Order created successfully!")
                Log.d(TAG, "Order Response:")
                Log.d(TAG, "  - orderId: ${orderRes.orderId}")
                Log.d(TAG, "  - token: ${orderRes.token.take(20)}...")
                Log.d(TAG, "  - state: ${orderRes.state}")

                Log.d(TAG, "Step 3: Emitting payment event...")
                _paymentEvent.emit(orderRes)
                Log.d(TAG, "✅ Payment event emitted successfully")
                Log.d(TAG, "========================================")

            } catch (e: retrofit2.HttpException) {
                Log.e(TAG, "========================================")
                Log.e(TAG, "❌ HTTP ERROR DURING PAYMENT")
                Log.e(TAG, "HTTP Code: ${e.code()}")
                Log.e(TAG, "HTTP Message: ${e.message()}")

                try {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e(TAG, "Error Response Body: $errorBody")
                } catch (ex: Exception) {
                    Log.e(TAG, "Could not read error body: ${ex.message}")
                }

                Log.e(TAG, "Stack trace:")
                e.printStackTrace()
                Log.e(TAG, "========================================")

            } catch (e: Exception) {
                Log.e(TAG, "========================================")
                Log.e(TAG, "❌ PAYMENT ERROR")
                Log.e(TAG, "Error type: ${e.javaClass.simpleName}")
                Log.e(TAG, "Error message: ${e.message}")
                Log.e(TAG, "Stack trace:")
                e.printStackTrace()
                Log.e(TAG, "========================================")
            }
        }
    }

    // ✅ Check payment status after PhonePe returns
    fun checkPaymentStatus(orderId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "========================================")
                Log.d(TAG, "🔍 CHECKING PAYMENT STATUS")
                Log.d(TAG, "Order ID: $orderId")
                Log.d(TAG, "========================================")

                // Get fresh token
                Log.d(TAG, "Fetching fresh token...")
                val tokenRes = repository.fetchToken()
                Log.d(TAG, "✅ Token received")

                // Check order status
                Log.d(TAG, "Checking order status...")
                val statusResponse = repository.checkPaymentStatus(
                    authToken = tokenRes.access_token,
                    orderId = orderId
                )

                if (statusResponse != null) {
                    Log.d(TAG, "Status Response:")
                    Log.d(TAG, "  - orderId: ${statusResponse.orderId}")
                    Log.d(TAG, "  - state: ${statusResponse.state}")
                    Log.d(TAG, "  - amount: ${statusResponse.amount}")

                    val result = when (statusResponse.state) {
                        "COMPLETED" -> {
                            // ✅ Convert amount from paisa (Long) to rupees (Double)
                            val amountInRupees = (statusResponse.amount ?: 0L) / 100.0
                            Log.d(TAG, "✅ Payment COMPLETED - Amount: ₹$amountInRupees")
                            PaymentStatusResult.Success(
                                orderId = statusResponse.orderId,
                                amount = amountInRupees,
                                transactionId = statusResponse.paymentDetails?.firstOrNull()?.transactionId
                            )
                        }
                        "FAILED" -> {
                            Log.e(TAG, "❌ Payment FAILED")
                            PaymentStatusResult.Failed(
                                orderId = statusResponse.orderId,
                                errorMessage = statusResponse.errorContext?.description ?: "Payment failed"
                            )
                        }
                        "PENDING" -> {
                            Log.w(TAG, "⏳ Payment PENDING")
                            PaymentStatusResult.Pending(orderId = statusResponse.orderId)
                        }
                        else -> {
                            Log.w(TAG, "❓ Unknown state: ${statusResponse.state}")
                            PaymentStatusResult.Unknown(orderId = statusResponse.orderId)
                        }
                    }

                    _paymentStatusEvent.value = result
                    Log.d(TAG, "Payment status result set: $result")
                    Log.d(TAG, "========================================")
                } else {
                    Log.e(TAG, "❌ Status response is NULL")
                    _paymentStatusEvent.value = PaymentStatusResult.Error("Failed to check status")
                }

            } catch (e: Exception) {
                Log.e(TAG, "========================================")
                Log.e(TAG, "❌ ERROR CHECKING STATUS")
                Log.e(TAG, "Error: ${e.message}")
                e.printStackTrace()
                Log.e(TAG, "========================================")
                _paymentStatusEvent.value = PaymentStatusResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    // ✅ Check if payment already exists when opening preview
    fun checkExistingPayment(token: String, merchantOrderId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "========================================")
                Log.d(TAG, "Checking if payment already exists")
                Log.d(TAG, "Merchant Order ID: $merchantOrderId")
                Log.d(TAG, "========================================")

                val response = repository.checkExistingPaymentStatus(token, merchantOrderId)

                // ✅ IMPORTANT: Check message for ORDER_NOT_FOUND
                if (response.message?.contains("ORDER_NOT_FOUND") == true) {
                    Log.d(TAG, "✅ No existing payment found - this is a new transaction")
                    _existingPaymentStatus.value = ExistingPaymentCheckResult.NotFound
                    return@launch
                }

                if (response.success && response.data != null) {
                    val state = response.data.state

                    Log.d(TAG, "Payment state found: $state")

                    when (state) {
                        "COMPLETED" -> {
                            // Payment already successful
                            val transactionId = response.data.transactionId ?: ""
                            val amount = (response.data.amount ?: 0L) / 100.0

                            Log.d(TAG, "✅ Payment already completed!")
                            Log.d(TAG, "Transaction ID: $transactionId")
                            Log.d(TAG, "Amount: ₹$amount")

                            _existingPaymentStatus.value = ExistingPaymentCheckResult.AlreadyPaid(
                                transactionId = transactionId,
                                amount = amount
                            )
                        }
                        "PENDING" -> {
                            Log.d(TAG, "⏳ Payment is PENDING")
                            _existingPaymentStatus.value = ExistingPaymentCheckResult.Pending
                        }
                        "FAILED" -> {
                            Log.d(TAG, "❌ Payment FAILED")
                            _existingPaymentStatus.value = ExistingPaymentCheckResult.Failed
                        }
                        else -> {
                            Log.d(TAG, "❓ Unknown state: $state")
                            _existingPaymentStatus.value = ExistingPaymentCheckResult.NotFound
                        }
                    }
                } else {
                    // No existing payment found or error
                    Log.d(TAG, "No payment data returned")
                    _existingPaymentStatus.value = ExistingPaymentCheckResult.NotFound
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error checking existing payment", e)
                // ✅ Treat errors as "not found" so user can try payment
                _existingPaymentStatus.value = ExistingPaymentCheckResult.NotFound
            }
        }
    }

    // ✅ Update payment status in database
    fun updatePaymentStatusInDb(
        iotOrderId: Int?,
        manualSaleId: Int?,
        status: String,
        transactionId: String?,
        amountPaid: Double?
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "========================================")
                Log.d(TAG, "📝 UPDATING PAYMENT STATUS IN DATABASE")
                Log.d(TAG, "IOT Order ID: $iotOrderId")
                Log.d(TAG, "Manual Sale ID: $manualSaleId")
                Log.d(TAG, "Status: $status")
                Log.d(TAG, "Transaction ID: $transactionId")
                Log.d(TAG, "Amount Paid: ₹$amountPaid")
                Log.d(TAG, "========================================")

                val response = repository.updatePaymentStatusInDb(
                    iotOrderId = iotOrderId,
                    manualSaleId = manualSaleId,
                    status = status,
                    transactionId = transactionId,
                    amountPaid = amountPaid
                )
                if (iotOrderId == null && manualSaleId == null) {
                    Log.e(TAG, "Cannot update: No valid order ID")

                }
                if (response.success) {
                    Log.d(TAG, "✅ Payment status updated successfully in DB")
                } else {
                    Log.e(TAG, "❌ Failed to update payment status: ${response.message}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error updating payment status in DB")
                Log.e(TAG, "Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}

// ✅ Sealed class for existing payment check results
sealed class ExistingPaymentCheckResult {
    data class AlreadyPaid(val transactionId: String, val amount: Double) : ExistingPaymentCheckResult()
    object Pending : ExistingPaymentCheckResult()
    object Failed : ExistingPaymentCheckResult()
    object NotFound : ExistingPaymentCheckResult()
    data class Error(val message: String) : ExistingPaymentCheckResult()
}

// ✅ Sealed class for payment status results
sealed class PaymentStatusResult {
    data class Success(
        val orderId: String,
        val amount: Double,
        val transactionId: String?
    ) : PaymentStatusResult()

    data class Failed(
        val orderId: String,
        val errorMessage: String
    ) : PaymentStatusResult()

    data class Pending(
        val orderId: String
    ) : PaymentStatusResult()

    data class Unknown(
        val orderId: String
    ) : PaymentStatusResult()

    data class Error(
        val message: String
    ) : PaymentStatusResult()
}