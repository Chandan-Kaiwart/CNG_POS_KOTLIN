package com.rsgl.cngpos.payment

import android.util.Log
import com.rsgl.cngpos.payment.dataclass.UpdatePaymentStatusRequest
import com.rsgl.cngpos.payment.dataclass.UpdatePaymentStatusResponse
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*

class PaymentStatusRepository(
    private val api: PhonePeApiService,
    private val baseUrl: String = "https://api-preprod.phonepe.com/apis/pg-sandbox"
) {

    companion object {
        private const val TAG = "PaymentStatusRepo"
        private const val MAX_RETRIES = 3
    }

    // Generate transaction ID (same logic as React Native)
    fun generateTransactionId(
        iotOrderId: Int,
        totalAmount: Double,
        saleDate: String
    ): String {
        val amount = (totalAmount * 100).toInt()
        val date = try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsedDate = sdf.parse(saleDate)
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(parsedDate ?: Date())
        } catch (e: Exception) {
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        }

        return "CNG$iotOrderId$amount$date"
    }

    // Check order status with retry logic
    suspend fun checkOrderStatus(
        authToken: String,
        orderId: String
    ): PhonePeStatusResponse? {
        return try {
            // ✅ Use your PHP endpoint, not PhonePe direct API
            val request = CheckOrderStatusRequest(
                token = authToken,
                merchantOrderId = orderId
            )

            Log.d(TAG, "Checking order status for: $orderId")

            val response = api.getPhonePeOrderStatus(request)

            if (response.success && response.data != null) {
                Log.d(TAG, "Order status response: ${response.data}")

                // Convert to PhonePeStatusResponse
                PhonePeStatusResponse(
                    orderId = response.data.orderId,
                    state = response.data.state,
                    amount = response.data.amount,
                    paymentDetails = response.data.paymentDetails,
                    errorCode = null,
                    detailedErrorCode = null,
                    errorContext = null
                )
            } else {
                Log.e(TAG, "Order status check failed: ${response.message}")
                null
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP Error checking order status: ${e.code()} - ${e.message()}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error checking order status: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // Get payment status for a dispenser (returns UI status)
    suspend fun getPaymentStatus(
        authToken: String,
        iotOrderId: Int,
        totalAmount: Double,
        saleDate: String
    ): String {
        try {
            val transactionId = generateTransactionId(iotOrderId, totalAmount, saleDate)
            Log.d(TAG, "Checking status for transaction: $transactionId")

            val statusResult = checkOrderStatus(authToken, transactionId)

            // Add delay to avoid rate limiting
            delay(1000)

            if (statusResult == null) {
                return "available"
            }

            // Map PhonePe state to UI status
            return when {
                statusResult.state == "COMPLETED" && !statusResult.paymentDetails.isNullOrEmpty() -> {
                    "error" // Already paid (show red)
                }
                statusResult.state == "FAILED" || statusResult.errorCode != null -> {
                    "available" // Failed - can retry (show green)
                }
                statusResult.state == "PENDING" -> {
                    "in-use" // Payment in progress (show orange)
                }
                statusResult.state == "NOT_FOUND" -> {
                    "available" // Order not found - can create new (show green)
                }
                else -> {
                    "available" // Default to available
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error getting payment status: ${e.message}")
            return "available"
        }
    }

    // ✅ Update payment status in database
    suspend fun updatePaymentStatus(
        request: UpdatePaymentStatusRequest
    ): UpdatePaymentStatusResponse {
        return try {
            Log.d(TAG, "Updating payment status with request: $request")

            val response = api.updatePaymentStatus(request)

            Log.d(TAG, "Update response: success=${response.success}, message=${response.message}")

            response

        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(TAG, "HTTP Error updating payment status: ${e.code()} - $errorBody")

            UpdatePaymentStatusResponse(
                success = false,
                message = "HTTP ${e.code()}: $errorBody"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error updating payment status: ${e.message}")
            e.printStackTrace()

            UpdatePaymentStatusResponse(
                success = false,
                message = "Update failed: ${e.message}"
            )
        }
    }

    // Helper: Update auto payment status
    suspend fun updateAutoPaymentStatus(
        iotOrderId: Int,
        status: String,
        transactionId: String? = null,
        amountPaid: Double? = null
    ): UpdatePaymentStatusResponse {
        return updatePaymentStatus(
            UpdatePaymentStatusRequest(
                iot_order_id = iotOrderId,
                manual_sale_id = null,
                status = status,
                transaction_id = transactionId,
                amount_paid = amountPaid
            )
        )
    }

    // Helper: Update manual payment status
    suspend fun updateManualPaymentStatus(
        manualSaleId: Int,
        status: String,
        transactionId: String? = null,
        amountPaid: Double? = null
    ): UpdatePaymentStatusResponse {
        return updatePaymentStatus(
            UpdatePaymentStatusRequest(
                iot_order_id = null,
                manual_sale_id = manualSaleId,
                status = status,
                transaction_id = transactionId,
                amount_paid = amountPaid
            )
        )
    }
}