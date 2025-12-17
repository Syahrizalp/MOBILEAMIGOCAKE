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
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val tvItemName: TextView = itemView.findViewById(R.id.tv_item_name)
        val tvCustomerName: TextView = itemView.findViewById(R.id.tv_customer_name)
        val tvOrderAmount: TextView = itemView.findViewById(R.id.tv_order_amount)
        val tvOrderQuantity: TextView = itemView.findViewById(R.id.tv_order_quantity)
        val tvOrderStatus: TextView = itemView.findViewById(R.id.tv_order_status)
        val tvOrderDate: TextView = itemView.findViewById(R.id.tv_order_date)

        fun bind(order: Order) {
            // Product name - handle null
            tvItemName.text = order.namaProduct ?: "Custom Order"

            // Customer name - handle non-nullable field (sesuai model Order)
            tvCustomerName.text = order.namaPemesan

            // Amount - handle null harga
            val harga = order.harga ?: 0
            tvOrderAmount.text = formatRupiah(harga)

            // Quantity - kosongkan atau isi jika ada (tidak ada field jumlah di model)
            tvOrderQuantity.text = ""

            // Status - handle berbagai status
            val status = order.status ?: "Process" // order.status sudah non-nullable di model
            tvOrderStatus.text = status
            when (status) {
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
                else -> {
                    tvOrderStatus.setTextColor(Color.parseColor("#757575"))
                    tvOrderStatus.setBackgroundResource(R.drawable.bg_status_processing)
                }
            }

            // Date - handle parsing error
            tvOrderDate.text = try {
                // order.tanggal sudah non-nullable di model
                formatDate(order.tanggal)
            } catch (e: Exception) {
                order.tanggal
            }

            // Click listener
            cardView.setOnClickListener {
                onItemClick(order)
            }
        }

        // Fungsi helper untuk format Rupiah
        private fun formatRupiah(amount: Int): String {
            return try {
                val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                formatter.format(amount)
            } catch (e: Exception) {
                "Rp 0"
            }
        }

        // Fungsi helper untuk format tanggal
        private fun formatDate(dateStr: String): String {
            return try {
                // Coba format dengan tanggal dan waktu jika ada
                val dateToParse = if (dateStr.contains(" ")) {
                    // Format: yyyy-MM-dd HH:mm:ss
                    val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    dateTimeFormat.parse(dateStr)
                } else {
                    // Format: yyyy-MM-dd
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    dateFormat.parse(dateStr)
                }

                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                dateToParse?.let { outputFormat.format(it) } ?: dateStr
            } catch (e: Exception) {
                // Jika parsing gagal, coba format createdAt
                dateStr
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
}