package com.apc.demo_pos.pos.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.apc.demo_pos.R
import com.apc.demo_pos.pos.model.SaleTransaction

class SalesTransactionAdapter(
    private val transactions: List<SaleTransaction>,
    private val onItemClick: (SaleTransaction) -> Unit
) : RecyclerView.Adapter<SalesTransactionAdapter.TransactionViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardTransaction)
        val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        val tvDispenser: TextView = itemView.findViewById(R.id.tvDispenser)
        val tvNozzle: TextView = itemView.findViewById(R.id.tvNozzle)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)

        @RequiresApi(Build.VERSION_CODES.M)
        fun bind(transaction: SaleTransaction, position: Int) {
            tvOrderId.text = "Order #${transaction.orderId ?: "N/A"}"
            tvDateTime.text = "${transaction.date} ${transaction.time}"
            tvDispenser.text = "Dispenser: ${transaction.dispenserId}"
            tvNozzle.text = "Nozzle: ${transaction.nozzleId}"
            tvQuantity.text = "${transaction.quantity} KG"
            tvAmount.text = "₹${transaction.amount}"

            // Highlight selected item
            if (selectedPosition == position) {
                cardView.setCardBackgroundColor(itemView.context.getColor(R.color.primary_blue))
                tvOrderId.setTextColor(itemView.context.getColor(android.R.color.white))
                tvDateTime.setTextColor(itemView.context.getColor(android.R.color.white))
                tvDispenser.setTextColor(itemView.context.getColor(android.R.color.white))
                tvNozzle.setTextColor(itemView.context.getColor(android.R.color.white))
                tvQuantity.setTextColor(itemView.context.getColor(android.R.color.white))
                tvAmount.setTextColor(itemView.context.getColor(android.R.color.white))
            } else {
                cardView.setCardBackgroundColor(itemView.context.getColor(android.R.color.white))
                tvOrderId.setTextColor(itemView.context.getColor(R.color.text_dark))
                tvDateTime.setTextColor(itemView.context.getColor(R.color.text_gray))
                tvDispenser.setTextColor(itemView.context.getColor(R.color.text_dark))
                tvNozzle.setTextColor(itemView.context.getColor(R.color.text_gray))
                tvQuantity.setTextColor(itemView.context.getColor(R.color.primary_blue))
                tvAmount.setTextColor(itemView.context.getColor(R.color.success_green))
            }

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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position], position)
    }

    override fun getItemCount() = transactions.size

    fun getSelectedTransaction(): SaleTransaction? {
        return if (selectedPosition != RecyclerView.NO_POSITION) {
            transactions[selectedPosition]
        } else null
    }
}