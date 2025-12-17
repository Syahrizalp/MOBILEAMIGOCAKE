package com.amigocake.admin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amigocake.admin.api.ApiConfig
import com.amigocake.admin.models.ApiResponse
import com.amigocake.admin.models.DashboardStats
import com.amigocake.admin.models.Order
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvTotalOrderCount: TextView
    private lateinit var tvOrderListCount: TextView
    private lateinit var tvRevenueTodayValue: TextView
    private lateinit var tvRevenueMonthValue: TextView
    private lateinit var tvDeadlineItemName: TextView
    private lateinit var tvDeadlineDate: TextView
    private lateinit var ivProfileSettings: android.widget.ImageView

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        sharedPreferences = getSharedPreferences("AmigoCakePrefs", Context.MODE_PRIVATE)

        // Check login
        if (!isUserLoggedIn()) {
            navigateToLogin()
            return
        }

        initViews()
        setupGreeting()
        setupNavigation()
        loadDashboardStats()
    }

    private fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false) &&
                sharedPreferences.getInt("userId", 0) > 0 &&
                sharedPreferences.getString("userLevel", "") == "ADMIN"
    }

    private fun initViews() {
        tvGreeting = findViewById(R.id.tv_greeting)
        tvTotalOrderCount = findViewById(R.id.tv_total_order_count)
        tvOrderListCount = findViewById(R.id.tv_order_list_count)
        tvRevenueTodayValue = findViewById(R.id.tv_revenue_today_value)
        tvRevenueMonthValue = findViewById(R.id.tv_revenue_month_value)
        tvDeadlineItemName = findViewById(R.id.tv_deadline_item_name)
        tvDeadlineDate = findViewById(R.id.tv_deadline_date)
        ivProfileSettings = findViewById(R.id.iv_profile_settings)
    }

    private fun setupGreeting() {
        val userName = sharedPreferences.getString("userName", "Admin") ?: "Admin"
        tvGreeting.text = "Hi, $userName"
    }

    private fun loadDashboardStats() {
        val apiService = ApiConfig.apiService

        apiService.getDashboardStatistics().enqueue(object : Callback<ApiResponse<DashboardStats>> {
            override fun onResponse(
                call: Call<ApiResponse<DashboardStats>>,
                response: Response<ApiResponse<DashboardStats>>
            ) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()

                    if (apiResponse?.success == true && apiResponse.data != null) {
                        updateUI(apiResponse.data)
                    } else {
                        showDefaultData()
                        Toast.makeText(
                            this@HomeActivity,
                            apiResponse?.message ?: "Gagal memuat data dashboard",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    showDefaultData()
                    Toast.makeText(
                        this@HomeActivity,
                        "Error server: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<DashboardStats>>, t: Throwable) {
                showDefaultData()
                Toast.makeText(
                    this@HomeActivity,
                    "Error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun updateUI(stats: DashboardStats) {
        // 1. TOTAL ORDER (Semua waktu)
        tvTotalOrderCount.text = stats.totalOrders.toString()

        // 2. ORDER AKTIF (Process)
        tvOrderListCount.text = stats.activeOrders.toString()

        // 3. PENDAPATAN HARI INI
        tvRevenueTodayValue.text = formatRupiah(stats.revenueToday)

        // 4. PENDAPATAN BULAN INI
        tvRevenueMonthValue.text = formatRupiah(stats.revenueMonth)

        // 5. DEADLINE PESANAN TERDEKAT
        updateDeadlineInfo(stats.nearestDeadline)
    }

    private fun updateDeadlineInfo(order: Order?) {
        if (order != null) {
            val itemName = order.namaProduct ?: "Custom Order"
            val customerName = order.namaPemesan

            // Jika ada info tambahan dari order
            val displayText = if (order.diameter?.isNotEmpty() == true) {
                "$itemName (${order.diameter}cm) - $customerName"
            } else {
                "$itemName - $customerName"
            }

            tvDeadlineItemName.text = displayText
            tvDeadlineDate.text = formatDate(order.tanggal)

            // Tampilkan juga waktu jika ada
            if (!order.waktu.isNullOrEmpty()) {
                tvDeadlineDate.text = "${formatDate(order.tanggal)} | ${order.waktu}"
            }
        } else {
            tvDeadlineItemName.text = "Tidak ada deadline"
            tvDeadlineDate.text = "-"
        }
    }

    private fun showDefaultData() {
        // Set default values jika gagal load data
        tvTotalOrderCount.text = "0"
        tvOrderListCount.text = "0"
        tvRevenueTodayValue.text = formatRupiah(0)
        tvRevenueMonthValue.text = formatRupiah(0)
        tvDeadlineItemName.text = "Tidak ada data"
        tvDeadlineDate.text = "-"
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

    private fun setupNavigation() {
        // Manual Order Button
        findViewById<android.widget.Button>(R.id.btn_manual_order).setOnClickListener {
            startActivity(Intent(this, OrderManualActivity::class.java))
        }

        // Order Review Header
        findViewById<TextView>(R.id.tv_header_review).setOnClickListener {
            startActivity(Intent(this, OrderRecapActivity::class.java))
        }

        // Order List Header
        findViewById<TextView>(R.id.tv_order_list_header).setOnClickListener {
            startActivity(Intent(this, OrderListActivity::class.java))
        }

        // Profile Icon - NAVIGASI KE PROFILE
        ivProfileSettings.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Bottom Navigation
        findViewById<android.widget.LinearLayout>(R.id.nav_home_container).setOnClickListener {
            // Already on home - refresh data
            loadDashboardStats()
        }

        findViewById<android.widget.LinearLayout>(R.id.nav_manual_order_container).setOnClickListener {
            startActivity(Intent(this, OrderManualActivity::class.java))
        }

        findViewById<android.widget.LinearLayout>(R.id.nav_review_container).setOnClickListener {
            startActivity(Intent(this, OrderRecapActivity::class.java))
        }

        findViewById<android.widget.LinearLayout>(R.id.nav_order_list_container).setOnClickListener {
            startActivity(Intent(this, OrderListActivity::class.java))
        }

        findViewById<android.widget.LinearLayout>(R.id.nav_topic_container).setOnClickListener {
            startActivity(Intent(this, ManagementOrderActivity::class.java))
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to screen
        loadDashboardStats()
        setupGreeting() // Update greeting jika nama berubah
    }
}