package com.rsgl.cngpos.payment

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class PaymentState {
    object Idle : PaymentState()
    object Loading : PaymentState()
    data class Success(
        val paymentUrl: String,
        val transactionId: String,
        val authToken: String
    ) : PaymentState()
    data class Error(val message: String) : PaymentState()
}

class SaleInfoViewModel(
    private val repository: PhonePeRepository
) : ViewModel() {

    private val TAG = "SaleInfoViewModel"

    private val _paymentState =
        MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> =
        _paymentState.asStateFlow()

//    fun initiatePayment(saleData: SaleData) {
//        viewModelScope.launch {
//            try {
//                _paymentState.value = PaymentState.Loading
//
//                val authToken = repository.getAuthToken()
//
//                val transactionId = saleData.getTransactionId()
//                val userId = "USER${(100000..999999).random()}"
//
//                val orderResponse = repository.createOrder(
//                    authToken = authToken,
//                    merchantOrderId = transactionId,
//                    amount = saleData.getTotalAmount(),
//                    userId = userId
//                )
//
//                _paymentState.value = PaymentState.Success(
//                    paymentUrl = orderResponse.redirectUrl,
//                    transactionId = transactionId,
//                    authToken = authToken
//                )
//
//            } catch (e: Exception) {
//                Log.e(TAG, "Payment error", e)
//                _paymentState.value =
//                    PaymentState.Error(e.message ?: "Unknown error")
//            }
//        }
//    }

    fun resetPaymentState() {
        _paymentState.value = PaymentState.Idle
    }
}
