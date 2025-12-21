package com.apc.demo_pos.payment

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface PaymentApiService {

    @POST("pos/updatePaymentStatus.php")
    suspend fun updatePaymentStatus(
        @Body body: Map<String, Any>
    ): Response<PaymentUpdateResponse>
}

data class PaymentUpdateResponse(
    val success: Boolean,
    val message: String
)