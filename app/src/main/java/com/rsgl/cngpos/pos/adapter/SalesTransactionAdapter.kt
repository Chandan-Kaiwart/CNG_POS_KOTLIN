package com.rsgl.cngpos.pos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.rsgl.cngpos.R

import com.rsgl.cngpos.pos.model.SaleTransaction

class SalesTransactionAdapter(
    private val transactions: List<SaleTransaction>,
    private val onItemClick: (SaleTransaction) -> Unit
) : RecyclerView.Adapter<SalesTransactionAdapter.TransactionViewHolder>() {

    private var selectedPosition = -1

    inner class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardTransaction: CardView = view.findViewById(R.id.cardTransaction)
        val tvPosSaleId: TextView = view.findViewById(R.id.tvPosSaleId)
        val tvDateTime: TextView = view.findViewById(R.id.tvDateTime)
        val tvTransactionStatus: TextView = view.findViewById(R.id.tvTransactionStatus)
        val tvDispenser: TextView = view.findViewById(R.id.tvDispenser)
        val tvNozzle: TextView = view.findViewById(R.id.tvNozzle)
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)

        // Conditional layouts
        val layoutIotOrderId: LinearLayout = view.findViewById(R.id.layoutIotOrderId)
        val tvIotOrderId: TextView = view.findViewById(R.id.tvIotOrderId)
        val layoutManualSaleId: LinearLayout = view.findViewById(R.id.layoutManualSaleId)
        val tvManualSaleId: TextView = view.findViewById(R.id.tvManualSaleId)

        fun bind(transaction: SaleTransaction, position: Int) {
            // POS Sale ID
            tvPosSaleId.text = "Sale #${transaction.posSaleId ?: "N/A"}"

            // Date Time
            tvDateTime.text = transaction.date

            // Dispenser & Nozzle
            tvDispenser.text = "Dispenser: ${transaction.dispenserId}"
            tvNozzle.text = "Nozzle: ${transaction.nozzleId}"

            // Quantity & Amount
            tvQuantity.text = "${transaction.quantity} KG"
            tvAmount.text = "₹${transaction.amount}"

            // Transaction Status - show only if not null
            if (!transaction.transactionStatus.isNullOrEmpty()) {
                tvTransactionStatus.visibility = View.VISIBLE
                tvTransactionStatus.text = transaction.transactionStatus

                // Color coding based on status
                when (transaction.transactionStatus.uppercase()) {
                    "PENDING" -> {
                        tvTransactionStatus.setBackgroundResource(R.drawable.badge_pending)
                    }
                    "SUCCESS", "COMPLETED" -> {
                        tvTransactionStatus.setBackgroundResource(R.drawable.badge_success)
                    }
                    "FAILED", "CANCELLED" -> {
                        tvTransactionStatus.setBackgroundResource(R.drawable.badge_failed)
                    }
                    else -> {
                        tvTransactionStatus.setBackgroundResource(R.drawable.badge_pending)
                    }
                }
            } else {
                tvTransactionStatus.visibility = View.GONE
            }

            // IOT Order ID - show only if not null
            if (!transaction.iotOrderId.isNullOrEmpty()) {
                layoutIotOrderId.visibility = View.VISIBLE
                tvIotOrderId.text = transaction.iotOrderId
            } else {
                layoutIotOrderId.visibility = View.GONE
            }

            // Manual Sale ID - show only if not null
            if (!transaction.orderId.isNullOrEmpty()) {
                layoutManualSaleId.visibility = View.VISIBLE
                tvManualSaleId.text = transaction.orderId
            } else {
                layoutManualSaleId.visibility = View.GONE
            }

            // Selection highlight
            if (position == selectedPosition) {
                cardTransaction.setCardBackgroundColor(
                    itemView.context.getColor(R.color.selection_blue)
                )
            } else {
                cardTransaction.setCardBackgroundColor(
                    itemView.context.getColor(android.R.color.white)
                )
            }

            // Click listener
            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onItemClick(transaction)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position], position)
    }

    override fun getItemCount() = transactions.size
}