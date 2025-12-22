package com.rsgl.cngpos.payment.utils

import com.rsgl.cngpos.payment.SaleData

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