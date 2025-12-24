package com.rsgl.cngpos.payment.dataclass

data class UpdatePaymentStatusResponse(
    val success: Boolean,
    val message: String,
    val affected_rows: Int? = null,
    val updated_record: UpdatedRecord? = null
)

data class UpdatedRecord(
    val POS_SaleID: Int,
    val SourceType: String,
    val IOT_OrderID: Int?,
    val Manual_SaleID: Int?,
    val SaleDate: String,
    val Dispenser_Id: String,
    val Nozzle_Id: String,
    val Sale_Quantity: Double,
    val Gas_Rate: Double,
    val Total_Amount: Double,
    val Transaction_Status: String,
    val Amount_Paid: Double?,
    val TransactionId: String?,
    val Created_At: String
)