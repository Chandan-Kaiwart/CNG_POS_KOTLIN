package com.apc.demo_pos.payment

import android.util.Log
import com.apc.demo_pos.*
import com.apc.demo_pos.RetrofitClient
import com.apc.demo_pos.payment.utils.OrderResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
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
