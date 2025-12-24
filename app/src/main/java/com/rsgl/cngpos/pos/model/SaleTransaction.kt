package com.rsgl.cngpos.pos.model

import com.google.gson.annotations.SerializedName

//data class SaleTransaction(
//    val dispenserId: String,
//    val nozzleId: String,
//    val quantity: String,
//    val amount: String,
//    val date: String,
//    val time: String,
//    val pricePerKg: String,
//    val orderId: String? = null,
//    val sourceType: String? = null,
//    @SerializedName("Manual_SaleID")
//    val manualSaleId: String?,
//    @SerializedName("IOT_OrderID")
//    val iotOrderId: String? = null,
//    val transactionStatus: String? = null,
//    val posSaleId: String? = null
//)

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: SaleTransaction?
)


data class SaleTransaction(
    // Basic fields
    val dispenserId: String,
    val nozzleId: String,
    val quantity: String,
    val amount: String,
    val date: String,
    val time: String = "",
    val pricePerKg: String,

    // IDs
    val orderId: String?,           // Manual_SaleID

    // Status fields
    val sourceType: String,         // "AUTO" or "MANUAL"
    val iotOrderId: String?,        // IOT_OrderID (string from API)
    val transactionStatus: String?,
    val posSaleId: String?
) {
    /**
     * ✅ Returns IOT Order ID as Int (handles all cases)
     */
    fun getIotOrderIdAsInt(): Int {
        return iotOrderId?.trim()?.toIntOrNull() ?: 0
    }

    /**
     * ✅ Returns Manual Sale ID as Int (handles all cases)
     */
    fun getManualSaleIdAsInt(): Int {
        return orderId?.trim()?.toIntOrNull() ?: -1
    }

    fun getDisplayTransactionId(): String {
        return when (sourceType) {
            "MANUAL" -> orderId ?: "N/A"
            "AUTO" -> iotOrderId ?: "N/A"
            else -> "N/A"
        }
    }

    fun isAutomatic(): Boolean = sourceType == "AUTO"
    fun isManual(): Boolean = sourceType == "MANUAL"
}