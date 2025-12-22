package com.rsgl.cngpos.payment.utils


data class OrderResponse(
    val orderId: String,
    val state: String,
    val expireAt: Long,
    val token: String
)

