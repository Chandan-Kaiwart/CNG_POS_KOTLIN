package com.apc.demo_pos.payment.utils


data class OrderResponse(
    val orderId: String,
    val state: String,
    val expireAt: Long,
    val token: String
)

