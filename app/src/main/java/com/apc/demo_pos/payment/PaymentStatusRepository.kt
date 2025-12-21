package com.apc.demo_pos.payment

import android.util.Log
import com.apc.demo_pos.RetrofitClient

class PaymentStatusRepository {
//
//    private val api = RetrofitClient.paymentApiService
//    private val TAG = "PaymentStatusRepo"
//
//    suspend fun updatePaymentStatus(
//        saleId: Int,
//        type: String, // "MANUAL" or "AUTO"
//        status: String, // "PAID" or "FAILED"
//        transactionId: String,
//        amount: Double
//    ): Result<Boolean> {
//        return try {
//            val params = mapOf(
//                "sale_id" to saleId,
//                "type" to type,
//                "status" to status,
//                "transaction_id" to transactionId,
//                "amount" to amount
//            )
//
//            Log.d(TAG, "Updating payment status: $params")
//            val response = api.updatePaymentStatus(params)
//
//            if (response.isSuccessful && response.body()?.success == true) {
//                Log.d(TAG, "Payment status updated successfully")
//                Result.success(true)
//            } else {
//                Log.e(TAG, "Failed to update payment status: ${response.body()?.message}")
//                Result.failure(Exception("Update failed"))
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Payment status update error", e)
//            Result.failure(e)
//        }
//    }
}