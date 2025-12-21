package com.apc.demo_pos.payment


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SaleData(
    val id: Int? = null,
    val IOT_OrderID: String? = null,
    val Dispenser_Id: String? = null,
    val Nozzle_Id: String? = null,
    val Sale_Amount: Double? = null,
    val Sale_Quantity: Double? = null,
    val Sale_Rate: Double? = null,
    val SaleDate: String? = null,
    val fuel_type: String? = "CNG",
    val isManualTransaction: Boolean = false,
    val ARM_Last_Fill_Amt_Rs: Double? = null,
    val ARM_Last_Fill_Qty_Kg: Double? = null,
    val ARM_GasRate_RsPerKg: Double? = null,
    val timestamp: String? = null,
    val payment_status: String? = null,
    val transaction_id: String? = null
) : Parcelable {

    fun getTotalAmount(): Double = Sale_Amount ?: ARM_Last_Fill_Amt_Rs ?: 0.0
    fun getQuantity(): Double = Sale_Quantity ?: ARM_Last_Fill_Qty_Kg ?: 0.0
    fun getPricePerUnit(): Double = Sale_Rate ?: ARM_GasRate_RsPerKg ?: 0.0
    fun getDispenserId(): String = Dispenser_Id ?: "Unknown"
    fun getNozzleId(): String = Nozzle_Id ?: "Unknown"

    fun getTransactionId(): String {
        if (!IOT_OrderID.isNullOrEmpty() && id != null) {
            val amount = (getTotalAmount() * 100).toInt()
            val date = (SaleDate ?: timestamp ?: "").replace("-", "").take(8)
            return "CNG$id$amount$date"
        }

        if (isManualTransaction) {
            val randomId = (100000..999999).random()
            val ts = System.currentTimeMillis().toString().takeLast(6)
            val amount = (getTotalAmount() * 100).toInt()
            val date = (SaleDate ?: "").replace("-", "").take(8)
            return "MAN$randomId$ts$amount$date"
        }

        return "TXN${System.currentTimeMillis()}"
    }
}
