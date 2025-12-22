package com.rsgl.cngpos.payment

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