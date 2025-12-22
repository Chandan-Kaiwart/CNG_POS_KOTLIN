package com.rsgl.cngpos.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rsgl.cngpos.payment.utils.OrderResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch


class PaymentViewModel(
    private val repository: PhonePeRepository
) : ViewModel() {

    private val _paymentEvent = MutableSharedFlow<OrderResponse>()
    val paymentEvent = _paymentEvent.asSharedFlow()

    fun startPayment(amount: Double, saleId: String) {
        viewModelScope.launch {
            try {
                val tokenRes = repository.fetchToken()

                val orderRes = repository.createOrder(
                    token = tokenRes.access_token,
                    amount = amount,
                    saleId = saleId
                )

                _paymentEvent.emit(orderRes)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
