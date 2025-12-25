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
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(TAG, "HTTP ${e.code()} Error: $errorBody")

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

    // ✅ FIXED: Update payment status in database with proper null handling
    suspend fun updatePaymentStatusInDb(
        iotOrderId: Int?,
        manualSaleId: Int?,
        status: String,
        transactionId: String?,
        amountPaid: Double?
    ): UpdatePaymentStatusResponse {
        return try {
            // ✅ Validate that at least one ID is provided
            if (iotOrderId == null && manualSaleId == null) {
                Log.e(TAG, "❌ Cannot update: Both iotOrderId and manualSaleId are null")
                return UpdatePaymentStatusResponse(
                    success = false,
                    message = "Either iot_order_id or manual_sale_id is required"
                )
            }

            // ✅ Build request with only non-null values
            val request = UpdatePaymentStatusRequest(
                iot_order_id = iotOrderId,
                manual_sale_id = manualSaleId,
                status = status,
                transaction_id = transactionId,
                amount_paid = amountPaid
            )

            Log.d(TAG, "========================================")
            Log.d(TAG, "📤 UPDATING PAYMENT STATUS")
            Log.d(TAG, "IOT Order ID: $iotOrderId")
            Log.d(TAG, "Manual Sale ID: $manualSaleId")
            Log.d(TAG, "Status: $status")
            Log.d(TAG, "Transaction ID: $transactionId")
            Log.d(TAG, "Amount Paid: ₹$amountPaid")
            Log.d(TAG, "Request: $request")
            Log.d(TAG, "========================================")

            val response = api.updatePaymentStatus(request)

            Log.d(TAG, "========================================")
            Log.d(TAG, "📥 DB UPDATE RESPONSE")
            Log.d(TAG, "Success: ${response.success}")
            Log.d(TAG, "Message: ${response.message}")
            Log.d(TAG, "Affected Rows: ${response.affected_rows}")
            Log.d(TAG, "========================================")

            response

        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(TAG, "❌ HTTP Error updating payment status")
            Log.e(TAG, "Status Code: ${e.code()}")
            Log.e(TAG, "Error Body: $errorBody")

            UpdatePaymentStatusResponse(
                success = false,
                message = "HTTP ${e.code()}: $errorBody"
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating payment status: ${e.message}")
            e.printStackTrace()

            UpdatePaymentStatusResponse(
                success = false,
                message = e.message ?: "Unknown error occurred"
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
            amount = (amount * 100).toInt(),
            merchantOrderId = saleId
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