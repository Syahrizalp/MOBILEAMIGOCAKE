package com.amigocake.admin

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.amigocake.admin.api.ApiConfig
import com.amigocake.admin.models.ApiResponse
import com.amigocake.admin.models.Order
import com.amigocake.admin.models.OrderUpdateRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var tvOrderId: TextView
    private lateinit var tvProductName: TextView
    private lateinit var tvCustomerName: TextView
    private lateinit var tvCustomerContact: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvPickupDate: TextView
    private lateinit var tvPickupTime: TextView
    private lateinit var tvDiameter: TextView
    private lateinit var tvVariant: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnUpdateStatus: Button
    private lateinit var btnDelete: Button
    private lateinit var btnBack: ImageView
    private lateinit var progressBar: ProgressBar

    private var orderId = 0
    private var currentOrder: Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        orderId = intent.getIntExtra("ORDER_ID", 0)

        if (orderId == 0) {
            Toast.makeText(this, "Invalid Order ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupClickListeners()
        loadOrderDetail()
    }

    private fun initViews() {
        tvOrderId = findViewById(R.id.tv_order_id)
        tvProductName = findViewById(R.id.tv_product_name)
        tvCustomerName = findViewById(R.id.tv_customer_name)
        tvCustomerContact = findViewById(R.id.tv_customer_contact)
        tvAddress = findViewById(R.id.tv_address)
        tvPickupDate = findViewById(R.id.tv_pickup_date)
        tvPickupTime = findViewById(R.id.tv_pickup_time)
        tvDiameter = findViewById(R.id.tv_diameter)
        tvVariant = findViewById(R.id.tv_variant)
        tvPrice = findViewById(R.id.tv_price)
        tvStatus = findViewById(R.id.tv_status)
        btnUpdateStatus = findViewById(R.id.btn_update_status)
        btnDelete = findViewById(R.id.btn_delete)
        btnBack = findViewById(R.id.btn_back)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnUpdateStatus.setOnClickListener {
            showUpdateStatusDialog()
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun loadOrderDetail() {
        showLoading(true)

        val apiService = ApiConfig.getApiService()
        apiService.getOrderById(orderId).enqueue(object : Callback<ApiResponse<Order>> {
            override fun onResponse(
                call: Call<ApiResponse<Order>>,
                response: Response<ApiResponse<Order>>
            ) {
                showLoading(false)

                if (response.isSuccessful) {
                    val apiResponse = response.body()

                    if (apiResponse?.success == true && apiResponse.data != null) {
                        currentOrder = apiResponse.data
                        displayOrderDetail(apiResponse.data)
                    } else {
                        Toast.makeText(
                            this@OrderDetailActivity,
                            apiResponse?.message ?: "Failed to load order",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse<Order>>, t: Throwable) {
                showLoading(false)
                Toast.makeText(
                    this@OrderDetailActivity,
                    "Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        })
    }

    private fun displayOrderDetail(order: Order) {
        tvOrderId.text = "Order #${order.id}"
        tvProductName.text = order.namaProduct ?: "Custom Order"
        tvCustomerName.text = order.namaPemesan
        tvCustomerContact.text = order.telp
        tvAddress.text = order.alamat
        tvPickupDate.text = formatDate(order.tanggal)
        tvPickupTime.text = order.waktu ?: "-"
        tvDiameter.text = if (order.diameter.isNullOrEmpty()) "-" else "${order.diameter} cm"
        tvVariant.text = order.varian ?: "-"
        tvPrice.text = formatRupiah(order.harga)

        // Status
        tvStatus.text = order.status
        updateStatusColor(order.status)
    }

    private fun updateStatusColor(status: String) {
        when (status) {
            "Process" -> {
                tvStatus.setTextColor(getColor(android.R.color.holo_orange_dark))
                tvStatus.setBackgroundResource(R.drawable.bg_status_processing)
            }
            "Done" -> {
                tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))
                tvStatus.setBackgroundResource(R.drawable.bg_paid)
                btnUpdateStatus.visibility = View.GONE
            }
            "Cancelled" -> {
                tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
                tvStatus.setBackgroundResource(R.drawable.bg_status_canceled)
                btnUpdateStatus.visibility = View.GONE
            }
        }
    }

    private fun showUpdateStatusDialog() {
        val statuses = arrayOf("Process", "Done", "Cancelled")

        AlertDialog.Builder(this)
            .setTitle("Update Status")
            .setItems(statuses) { _, which ->
                val newStatus = statuses[which]
                updateOrderStatus(newStatus)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateOrderStatus(newStatus: String) {
        currentOrder?.let { order ->
            showLoading(true)

            val updateRequest = OrderUpdateRequest(
                id = order.id,
                status = newStatus,
                namaPemesan = order.namaPemesan,
                telp = order.telp,
                alamat = order.alamat,
                tanggal = order.tanggal,
                harga = order.harga
            )

            val apiService = ApiConfig.getApiService()
            apiService.updateOrder(updateRequest).enqueue(object : Callback<ApiResponse<String>> {
                override fun onResponse(
                    call: Call<ApiResponse<String>>,
                    response: Response<ApiResponse<String>>
                ) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        val apiResponse = response.body()

                        if (apiResponse?.success == true) {
                            Toast.makeText(
                                this@OrderDetailActivity,
                                "Status updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Reload data
                            loadOrderDetail()
                        } else {
                            Toast.makeText(
                                this@OrderDetailActivity,
                                apiResponse?.message ?: "Failed to update status",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                    showLoading(false)
                    Toast.makeText(
                        this@OrderDetailActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Order")
            .setMessage("Are you sure you want to delete this order?")
            .setPositiveButton("Delete") { _, _ ->
                deleteOrder()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteOrder() {
        showLoading(true)

        val apiService = ApiConfig.getApiService()
        apiService.deleteOrder(orderId).enqueue(object : Callback<ApiResponse<String>> {
            override fun onResponse(
                call: Call<ApiResponse<String>>,
                response: Response<ApiResponse<String>>
            ) {
                showLoading(false)

                if (response.isSuccessful) {
                    val apiResponse = response.body()

                    if (apiResponse?.success == true) {
                        Toast.makeText(
                            this@OrderDetailActivity,
                            "Order deleted successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Kembali ke OrderListActivity
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(
                            this@OrderDetailActivity,
                            apiResponse?.message ?: "Failed to delete order",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                showLoading(false)
                Toast.makeText(
                    this@OrderDetailActivity,
                    "Error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnUpdateStatus.isEnabled = !show
        btnDelete.isEnabled = !show
    }

    private fun formatRupiah(amount: Int): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(amount)
    }

    private fun formatDate(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            val date = inputFormat.parse(dateStr)
            date?.let { outputFormat.format(it) } ?: dateStr
        } catch (e: Exception) {
            dateStr
        }
    }
}