package com.apc.demo_pos.payment.utils

import com.apc.demo_pos.payment.SaleData

data class SaleIdInfo(
    val type: String, // "MANUAL" or "AUTO"
    val id: Int
)

fun extractSaleId(saleData: SaleData): SaleIdInfo? {
    return if (saleData.id != null) {
        SaleIdInfo(
            type = if (saleData.isManualTransaction) "MANUAL" else "AUTO",
            id = saleData.id
        )
    } else {
        null
    }
}