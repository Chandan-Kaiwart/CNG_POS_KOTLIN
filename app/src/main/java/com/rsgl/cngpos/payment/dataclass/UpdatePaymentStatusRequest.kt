package com.rsgl.cngpos.payment.dataclass

import com.google.gson.annotations.SerializedName

data class UpdatePaymentStatusRequest(
    @SerializedName("iot_order_id")
    val iot_order_id: Int? = null,

    @SerializedName("manual_sale_id")
    val manual_sale_id: Int? = null,

    @SerializedName("status")
    val status: String,

    @SerializedName("transaction_id")
    val transaction_id: String? = null,

    @SerializedName("amount_paid")
    val amount_paid: Double? = null
)