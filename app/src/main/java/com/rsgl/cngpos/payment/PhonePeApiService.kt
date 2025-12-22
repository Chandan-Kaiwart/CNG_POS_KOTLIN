package com.rsgl.cngpos.payment

import com.rsgl.cngpos.payment.utils.OrderResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface PhonePeApiService {

    @Headers("Content-Type: application/json")

    @POST("pos/get_phonepe_orderid.php")
    suspend fun createOrder(
        @Body body: CreateOrderRequest
    ): OrderResponse

    @GET("pos/get_phonepe_token.php")
    suspend fun getPhonePeToken(): TokenResponse


    @POST
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun getAuthToken(
        @Url url: String,
        @Body body: RequestBody
    ): Response<PhonePeAuthResponse>

    @POST
    @Headers("Content-Type: application/json")
    suspend fun createOrder(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Body request: PhonePeOrderRequest
    ): Response<PhonePeOrderResponse>

    @GET
    suspend fun checkOrderStatus(
        @Url url: String,
        @Header("Authorization") authorization: String
    ): Response<PhonePeStatusResponse>
}

// Models
data class PhonePeAuthResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int
)

data class PhonePeOrderRequest(
    val merchantOrderId: String,
    val amount: Long,
    val expireAfter: Int = 1200,
    val metaInfo: MetaInfo,
    val paymentFlow: PaymentFlow
)

data class MetaInfo(
    val udf1: String,
    val udf2: String,
    val udf3: String
)

data class PaymentFlow(
    val type: String = "PG_CHECKOUT",
    val message: String,
    val merchantUrls: MerchantUrls
)

data class MerchantUrls(
    val redirectUrl: String,
    val failureRedirectUrl: String
)

data class PhonePeOrderResponse(
    val orderId: String,
    val redirectUrl: String,
    val state: String,
    val expireAt: String?
)

data class PhonePeStatusResponse(
    val orderId: String,
    val state: String,
    val amount: Long?,
    val paymentDetails: List<PaymentDetail>?,
    val errorCode: String?,
    val detailedErrorCode: String?,
    val errorContext: ErrorContext?
)

data class PaymentDetail(
    val transactionId: String,
    val paymentMode: String,
    val amount: Long
)
data class TokenResponse(
    val access_token: String,
    val expires_in: Int,
    val token_type: String
)

data class CreateOrderRequest(
    val token: String,
    val amount: Int,
    val merchantOrderId: String
)


data class CreateOrderResponse(
    val orderId: String,
    val token: String,
    val state: String
)

data class ErrorContext(
    val description: String?

)