package com.amigocake.admin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amigocake.admin.api.ApiConfig
import com.amigocake.admin.databinding.ActivityHomeBinding
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

    private lateinit var binding: ActivityHomeBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi View Binding
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("AmigoCakePrefs", Context.MODE_PRIVATE)

        // Check login
        if (!isUserLoggedIn()) {
            navigateToLogin()
            return
        }

        setupGreeting()
        setupNavigation()
        loadDashboardStats()
    }

    private fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false) &&
                sharedPreferences.getInt("userId", 0) > 0 &&
                sharedPreferences.getString("userLevel", "") == "ADMIN"
    }

    private fun setupGreeting() {
        val userName = sharedPreferences.getString("userName", "Admin") ?: "Admin"
        binding.tvGreeting.text = "Hi, $userName"
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
        binding.tvTotalOrderCount.text = stats.totalOrders.toString()

        // 2. ORDER AKTIF (Process)
        binding.tvOrderListCount.text = stats.activeOrders.toString()

        // 3. PENDAPATAN HARI INI
        binding.tvRevenueTodayValue.text = formatRupiah(stats.revenueToday)

        // 4. PENDAPATAN BULAN INI
        binding.tvRevenueMonthValue.text = formatRupiah(stats.revenueMonth)

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

            binding.tvDeadlineItemName.text = displayText
            binding.tvDeadlineDate.text = formatDate(order.tanggal)

            // Tampilkan juga waktu jika ada
            if (!order.waktu.isNullOrEmpty()) {
                binding.tvDeadlineDate.text = "${formatDate(order.tanggal)} | ${order.waktu}"
            }
        } else {
            binding.tvDeadlineItemName.text = "Tidak ada deadline"
            binding.tvDeadlineDate.text = "-"
        }
    }

    private fun showDefaultData() {
        // Set default values jika gagal load data
        binding.tvTotalOrderCount.text = "0"
        binding.tvOrderListCount.text = "0"
        binding.tvRevenueTodayValue.text = formatRupiah(0)
        binding.tvRevenueMonthValue.text = formatRupiah(0)
        binding.tvDeadlineItemName.text = "Tidak ada data"
        binding.tvDeadlineDate.text = "-"
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
        binding.btnManualOrder.setOnClickListener {
            startActivity(Intent(this, OrderManualActivity::class.java))
        }

        // Order Review Header
        binding.tvHeaderReview.setOnClickListener {
            startActivity(Intent(this, OrderRecapActivity::class.java))
        }

        // Order List Header
        binding.tvOrderListHeader.setOnClickListener {
            startActivity(Intent(this, OrderListActivity::class.java))
        }

        // Profile Icon - NAVIGASI KE PROFILE
        binding.ivProfileSettings.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Bottom Navigation
        binding.navHomeContainer.setOnClickListener {
            // Already on home - refresh data
            loadDashboardStats()
        }

        binding.navManualOrderContainer.setOnClickListener {
            startActivity(Intent(this, OrderManualActivity::class.java))
        }

        binding.navReviewContainer.setOnClickListener {
            startActivity(Intent(this, OrderRecapActivity::class.java))
        }

        binding.navOrderListContainer.setOnClickListener {
            startActivity(Intent(this, OrderListActivity::class.java))
        }

        binding.navTopicContainer.setOnClickListener {
            startActivity(Intent(this, ProductManagementActivity::class.java)) // Ubah ke ProductManagementActivity
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