package com.rsgl.cngpos.payment

import com.rsgl.cngpos.payment.utils.OrderResponse
import android.util.Log
import com.rsgl.cngpos.payment.dataclass.UpdatePaymentStatusRequest
import com.rsgl.cngpos.payment.dataclass.UpdatePaymentStatusResponse

class PhonePeRepository(
    private val api: PhonePeApiService
) {

    private val TAG = "PhonePeRepository"

    // ✅ Existing function - keep as is
    suspend fun createPaymentOrder(amount: Double, saleId: String): OrderResponse {
        try {
            val tokenResponse = api.getPhonePeToken()
            val token = tokenResponse.access_token

            val orderRequest = CreateOrderRequest(
                token = token,
                amount = (amount * 100).toInt(),
                merchantOrderId = saleId
            )

            return api.createOrder(orderRequest)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating payment order", e)
            throw e
        }
    }

    // ✅ Check if payment already exists for this order
    suspend fun checkExistingPaymentStatus(
        token: String,
        merchantOrderId: String
    ): CheckOrderStatusResponse {
        return try {
            Log.d(TAG, "Checking payment status for order: $merchantOrderId")

            val request = CheckOrderStatusRequest(
                token = token,
                merchantOrderId = merchantOrderId
            )

            Log.d(TAG, "Request payload: token=${token.take(20)}..., merchantOrderId=$merchantOrderId")

            val response = api.getPhonePeOrderStatus(request)

            Log.d(TAG, "Response: success=${response.success}, code=${response.data?.state}")

            response

        } catch (e: retrofit2.HttpException) {
            // Parse error response body
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(TAG, "HTTP ${e.code()} Error: $errorBody")

            // If ORDER_NOT_FOUND, return NotFound instead of error
            if (errorBody?.contains("ORDER_NOT_FOUND") == true) {
                CheckOrderStatusResponse(
                    success = false,
                    message = "ORDER_NOT_FOUND",
                    data = null
                )
            } else {
                CheckOrderStatusResponse(
                    success = false,
                    message = "HTTP ${e.code()}: $errorBody",
                    data = null
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error checking payment status", e)
            CheckOrderStatusResponse(
                success = false,
                message = e.message,
                data = null
            )
        }
    }

    // ✅ Update payment status in database
    suspend fun updatePaymentStatusInDb(
        iotOrderId: Int?,
        manualSaleId: Int?,
        status: String,
        transactionId: String?,
        amountPaid: Double?
    ): UpdatePaymentStatusResponse {
        return try {
            val request = UpdatePaymentStatusRequest(
                iot_order_id = iotOrderId,
                manual_sale_id = manualSaleId,
                status = status,
                transaction_id = transactionId,
                amount_paid = amountPaid
            )

            Log.d(TAG, "Updating payment status: $request")
            api.updatePaymentStatus(request)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating payment status", e)
            UpdatePaymentStatusResponse(
                success = false,
                message = e.message ?: "Unknown error"
            )
        }
    }

    suspend fun fetchToken(): TokenResponse {
        return api.getPhonePeToken()
    }

    suspend fun createOrder(
        token: String,
        amount: Double,
        saleId: String
    ): OrderResponse {
        Log.d(TAG, "Creating order with merchantOrderId: $saleId")

        val body = CreateOrderRequest(
            token = token,
            amount = (amount * 100).toInt(), // Convert to paisa
            merchantOrderId = saleId  // ✅ FIXED: Use the provided saleId
        )

        Log.d(TAG, "Order request: amount=${body.amount} paisa, merchantOrderId=${body.merchantOrderId}")

        return api.createOrder(body)
    }

    // ✅ Payment status check
    suspend fun checkPaymentStatus(
        authToken: String,
        orderId: String
    ): PhonePeStatusResponse? {
        val statusRepo = PaymentStatusRepository(api)
        return statusRepo.checkOrderStatus(authToken, orderId)
    }
}