package com.apc.demo_pos.pos.model

data class SaleTransaction(
    val dispenserId: String,
    val nozzleId: String,
    val quantity: String,
    val amount: String,
    val date: String,
    val time: String,
    val pricePerKg: String,
    val orderId: String? = null
)

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: SaleTransaction?
)