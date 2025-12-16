package com.amigocake.admin.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.amigocake.admin.R
import com.amigocake.admin.models.Order
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(
    private var orders: List<Order>,
    private val onItemClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView as CardView
        val tvItemName: TextView = itemView.findViewById(R.id.tv_item_name)
        val tvCustomerName: TextView = itemView.findViewById(R.id.tv_customer_name)
        val tvOrderAmount: TextView = itemView.findViewById(R.id.tv_order_amount)
        val tvOrderStatus: TextView = itemView.findViewById(R.id.tv_order_status)
        val tvOrderDate: TextView = itemView.findViewById(R.id.tv_order_date)

        fun bind(order: Order) {
            // Product name
            tvItemName.text = order.namaProduct ?: "Custom Order"

            // Customer name
            tvCustomerName.text = order.namaPemesan

            // Amount
            tvOrderAmount.text = formatRupiah(order.harga)

            // Status
            tvOrderStatus.text = order.status
            when (order.status) {
                "Process" -> {
                    tvOrderStatus.setTextColor(Color.parseColor("#FF9800"))
                    tvOrderStatus.setBackgroundResource(R.drawable.bg_status_processing)
                }
                "Done" -> {
                    tvOrderStatus.setTextColor(Color.parseColor("#4CAF50"))
                    tvOrderStatus.setBackgroundResource(R.drawable.bg_paid)
                }
                "Cancelled" -> {
                    tvOrderStatus.setTextColor(Color.parseColor("#F44336"))
                    tvOrderStatus.setBackgroundResource(R.drawable.bg_status_canceled)
                }
            }

            // Date
            tvOrderDate.text = formatDate(order.tanggal)

            // Click listener
            cardView.setOnClickListener {
                onItemClick(order)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_card, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    fun updateList(newList: List<Order>) {
        orders = newList
        notifyDataSetChanged()
    }

    private fun formatRupiah(amount: Int): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(amount)
    }

    private fun formatDate(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            val date = inputFormat.parse(dateStr)
            date?.let { outputFormat.format(it) } ?: dateStr
        } catch (e: Exception) {
            dateStr
        }
    }
}