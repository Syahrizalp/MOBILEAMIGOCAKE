package com.amigocake.admin

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.amigocake.admin.api.ApiConfig
import com.amigocake.admin.models.ApiResponse
import com.amigocake.admin.models.Order
import com.amigocake.admin.models.OrderUpdateRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
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
    private lateinit var tvMessage: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvOrderDate: TextView
    private lateinit var tvCategory: TextView
    private lateinit var btnUpdateStatus: Button
    private lateinit var btnDelete: Button
    private lateinit var btnBack: ImageView
    private lateinit var progressBar: ProgressBar

    // Tambahan untuk bukti pembayaran
    private lateinit var cardBuktiPembayaran: CardView
    private lateinit var ivBuktiPembayaran: ImageView
    private lateinit var progressBarImage: ProgressBar
    private lateinit var tvNoPayment: TextView
    private lateinit var btnViewPayment: Button
    private lateinit var tvPaymentMethod: TextView

    private var orderId = 0
    private var currentOrder: Order? = null
    private var paymentProofUrl: String? = null

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
        tvMessage = findViewById(R.id.tv_message)
        tvPrice = findViewById(R.id.tv_price)
        tvStatus = findViewById(R.id.tv_status)
        tvOrderDate = findViewById(R.id.tv_order_date)
        tvCategory = findViewById(R.id.tv_category)
        btnUpdateStatus = findViewById(R.id.btn_update_status)
        btnDelete = findViewById(R.id.btn_delete)
        btnBack = findViewById(R.id.btn_back)
        progressBar = findViewById(R.id.progress_bar)

        // Inisialisasi views bukti pembayaran
        cardBuktiPembayaran = findViewById(R.id.card_bukti_pembayaran)
        ivBuktiPembayaran = findViewById(R.id.iv_bukti_pembayaran)
        progressBarImage = findViewById(R.id.progress_bar_image)
        tvNoPayment = findViewById(R.id.tv_no_payment)
        btnViewPayment = findViewById(R.id.btn_view_payment)
        tvPaymentMethod = findViewById(R.id.tv_payment_method)
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

        btnViewPayment.setOnClickListener {
            paymentProofUrl?.let { url ->
                showFullScreenImage(url)
            }
        }

        ivBuktiPembayaran.setOnClickListener {
            paymentProofUrl?.let { url ->
                showFullScreenImage(url)
            }
        }
    }

    private fun loadOrderDetail() {
        showLoading(true)

        val apiService = ApiConfig.apiService
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
                        loadPaymentProof(apiResponse.data)
                    } else {
                        Toast.makeText(
                            this@OrderDetailActivity,
                            apiResponse?.message ?: "Gagal memuat detail order",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } else {
                    Toast.makeText(
                        this@OrderDetailActivity,
                        "Server error: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
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
        // 1. Order ID
        tvOrderId.text = "Order #${order.id}"

        // 2. Product Name
        val productName = if (!order.namaProduct.isNullOrEmpty()) {
            order.namaProduct
        } else if (!order.kategori.isNullOrEmpty()) {
            "Custom ${order.kategori}"
        } else {
            "Custom Order"
        }
        tvProductName.text = productName

        // 3. Customer Information
        tvCustomerName.text = order.namaPemesan
        tvCustomerContact.text = order.telp
        tvAddress.text = order.alamat

        // 4. Order Details
        tvPickupDate.text = formatDate(order.tanggal)
        tvPickupTime.text = order.waktu ?: "-"

        // Diameter
        val diameterText = if (!order.diameter.isNullOrEmpty()) {
            "${order.diameter} cm"
        } else {
            "-"
        }
        tvDiameter.text = diameterText

        // Variant
        tvVariant.text = order.varian ?: "-"

        // Tulisan di Kue
        tvMessage.text = order.tulisan ?: "-"

        // Kategori
        tvCategory.text = order.kategori ?: "Custom Cake"

        // 5. Price
        tvPrice.text = formatRupiah(order.harga ?: 0)

        // 6. Status
        tvStatus.text = order.status
        updateStatusUI(order.status)

        // 7. Order Date (created_at)
        val orderDate = if (!order.createdAt.isNullOrEmpty()) {
            formatDateTime(order.createdAt)
        } else {
            "-"
        }
        tvOrderDate.text = orderDate
    }

    private fun loadPaymentProof(order: Order) {
        // Cek apakah ada bukti pembayaran
        paymentProofUrl = order.buktiBayar

        // Tampilkan metode pembayaran
        val paymentMethod = order.paymentMethod ?: "cod"

        if (paymentMethod.isNotEmpty()) {
            tvPaymentMethod.visibility = View.VISIBLE
            tvPaymentMethod.text = "Metode: ${paymentMethod.uppercase()}"
        } else {
            tvPaymentMethod.visibility = View.GONE
        }

        if (paymentProofUrl.isNullOrEmpty() || paymentMethod == "cod") {
            // Tidak ada bukti pembayaran (COD)
            cardBuktiPembayaran.visibility = View.GONE
            tvNoPayment.visibility = View.VISIBLE
            btnViewPayment.visibility = View.GONE
            tvNoPayment.text = if (paymentMethod == "cod") {
                "Pembayaran Cash on Delivery (COD)"
            } else {
                "Belum ada bukti pembayaran"
            }
            return
        }

        // Ada bukti pembayaran
        cardBuktiPembayaran.visibility = View.VISIBLE
        tvNoPayment.visibility = View.GONE
        btnViewPayment.visibility = View.VISIBLE

        // Load gambar dengan Glide
        showImageLoading(true)

        // PERBAIKAN: Gunakan ApiConfig untuk mendapatkan URL gambar
        val fullImageUrl = ApiConfig.getImageUrl(paymentProofUrl)

        if (fullImageUrl.isNullOrEmpty()) {
            showImageLoading(false)
            cardBuktiPembayaran.visibility = View.GONE
            tvNoPayment.visibility = View.VISIBLE
            tvNoPayment.text = "URL gambar tidak valid"
            return
        }

        // Debug log untuk melihat URL
        println("DEBUG - Loading image from: $fullImageUrl")

        // PERBAIKAN: Ganti placeholder dengan warna default Android untuk testing
        Glide.with(this)
            .load(fullImageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .placeholder(android.R.color.darker_gray) // Placeholder default
            .error(android.R.color.holo_red_light)    // Error default
            .into(ivBuktiPembayaran)

        showImageLoading(false)
    }

    private fun showImageLoading(show: Boolean) {
        progressBarImage.visibility = if (show) View.VISIBLE else View.GONE
        ivBuktiPembayaran.visibility = if (show) View.INVISIBLE else View.VISIBLE
    }

    private fun showFullScreenImage(imageUrl: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_fullscreen_image, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.iv_fullscreen)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progress_bar_fullscreen)

        progressBar.visibility = View.VISIBLE

        // Gunakan ApiConfig untuk URL jika diperlukan
        val fullImageUrl = if (imageUrl.startsWith("http")) {
            imageUrl
        } else {
            ApiConfig.getImageUrl(imageUrl) ?: imageUrl
        }

        Glide.with(this)
            .load(fullImageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .placeholder(android.R.color.darker_gray)
            .error(android.R.color.holo_red_light)
            .into(imageView)

        progressBar.visibility = View.GONE

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setNegativeButton("Tutup") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.show()
    }

    private fun updateStatusUI(status: String) {
        when (status) {
            "Process" -> {
                tvStatus.setTextColor(getColor(android.R.color.holo_orange_dark))
                tvStatus.setBackgroundResource(R.drawable.bg_status_processing)
                btnUpdateStatus.visibility = View.VISIBLE
                btnDelete.visibility = View.VISIBLE
            }
            "Done" -> {
                tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))
                tvStatus.setBackgroundResource(R.drawable.bg_paid)
                btnUpdateStatus.visibility = View.GONE
                btnDelete.visibility = View.VISIBLE
            }
            "Cancelled" -> {
                tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
                tvStatus.setBackgroundResource(R.drawable.bg_status_canceled)
                btnUpdateStatus.visibility = View.GONE
                btnDelete.visibility = View.VISIBLE
            }
        }
    }

    private fun showUpdateStatusDialog() {
        val statuses = arrayOf("Process", "Done", "Cancelled")

        AlertDialog.Builder(this)
            .setTitle("Update Status Order")
            .setItems(statuses) { _, which ->
                val newStatus = statuses[which]
                updateOrderStatus(newStatus)
            }
            .setNegativeButton("Batal", null)
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

            val apiService = ApiConfig.apiService
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
                                "Status berhasil diupdate",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Reload data
                            loadOrderDetail()
                        } else {
                            Toast.makeText(
                                this@OrderDetailActivity,
                                apiResponse?.message ?: "Gagal update status",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@OrderDetailActivity,
                            "Server error: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
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
        } ?: run {
            Toast.makeText(this, "Order data tidak tersedia", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Order")
            .setMessage("Apakah Anda yakin ingin menghapus order ini?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteOrder()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteOrder() {
        showLoading(true)

        val apiService = ApiConfig.apiService
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
                            "Order berhasil dihapus",
                            Toast.LENGTH_SHORT
                        ).show()

                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(
                            this@OrderDetailActivity,
                            apiResponse?.message ?: "Gagal menghapus order",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@OrderDetailActivity,
                        "Server error: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
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
        btnBack.isEnabled = !show
    }

    private fun formatRupiah(amount: Int): String {
        return try {
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            formatter.format(amount)
        } catch (e: Exception) {
            "Rp 0"
        }
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

    private fun formatDateTime(dateTimeStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            val date = inputFormat.parse(dateTimeStr)
            date?.let { outputFormat.format(it) } ?: dateTimeStr
        } catch (e: Exception) {
            dateTimeStr
        }
    }
}