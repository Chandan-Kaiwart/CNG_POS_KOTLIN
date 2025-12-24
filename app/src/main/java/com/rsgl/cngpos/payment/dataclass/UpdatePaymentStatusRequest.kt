package com.rsgl.cngpos.payment.dataclass

data class UpdatePaymentStatusRequest(
    val iot_order_id: Int? = null,
    val manual_sale_id: Int? = null,
    val status: String,
    val transaction_id: String? = null,
    val amount_paid: Double? = null
)