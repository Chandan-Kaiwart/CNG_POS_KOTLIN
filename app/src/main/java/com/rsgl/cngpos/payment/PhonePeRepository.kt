package com.rsgl.cngpos.payment

import com.rsgl.cngpos.payment.utils.OrderResponse

class PhonePeRepository(
    private val api: PhonePeApiService
) {

    suspend fun fetchToken(): TokenResponse {
        return api.getPhonePeToken()
    }

    suspend fun createOrder(
        token: String,
        amount: Double,
        saleId: String
    ): OrderResponse {

        val body = CreateOrderRequest(
            token = token,
            amount = (amount * 100).toInt(), // paisa
            merchantOrderId = "TXN${System.currentTimeMillis()}"
        )

        return api.createOrder(body)
    }
}
