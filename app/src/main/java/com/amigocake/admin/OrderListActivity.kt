package com.amigocake.admin

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amigocake.admin.adapters.OrderAdapter
import com.amigocake.admin.api.ApiConfig
import com.amigocake.admin.models.ApiResponse
import com.amigocake.admin.models.Order
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrderListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var searchInput: EditText
    private lateinit var filterStatusButton: MaterialButton
    private lateinit var filterSortButton: MaterialButton
    private lateinit var tvEmptyState: TextView

    private var allOrders = mutableListOf<Order>() // Semua order
    private var displayedOrders = mutableListOf<Order>() // Order yang ditampilkan
    private var currentStatus = "all"
    private var currentSort = 0 // 0=Terbaru, 1=Terlama, 2=Harga Tertinggi, 3=Harga Terendah

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_list)

        initViews()
        setupRecyclerView()
        setupClickListeners()
        setupNavigation()
        loadOrders()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.order_recycler_view)
        searchInput = findViewById(R.id.search_input)
        filterStatusButton = findViewById(R.id.filter_status_button)
        filterSortButton = findViewById(R.id.filter_sort_button)
        tvEmptyState = findViewById(R.id.tv_empty_state)
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(displayedOrders) { order ->
            // Handle item click - Navigasi ke OrderDetailActivity
            val intent = Intent(this, OrderDetailActivity::class.java)
            intent.putExtra("ORDER_ID", order.id)
            startActivity(intent)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@OrderListActivity)
            adapter = orderAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        // Filter by status
        filterStatusButton.setOnClickListener {
            showStatusFilterDialog()
        }

        // Sort button
        filterSortButton.setOnClickListener {
            showSortDialog()
        }

        // Search functionality dengan TextWatcher
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                searchOrders(s.toString())
            }
        })
    }

    private fun loadOrders(status: String = "all") {
        Log.d("DEBUG_ORDER", "Loading orders with status: $status")

        val apiService = ApiConfig.apiService
        val call = if (status == "all") {
            apiService.getAllOrders()
        } else {
            apiService.getOrdersByStatus(status)
        }

        call.enqueue(object : Callback<ApiResponse<List<Order>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<Order>>>,
                response: Response<ApiResponse<List<Order>>>
            ) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()

                    if (apiResponse?.success == true && apiResponse.data != null) {
                        allOrders.clear()
                        allOrders.addAll(apiResponse.data)

                        // Apply filter status
                        applyStatusFilter(status)

                        // Apply search jika ada
                        val query = searchInput.text.toString()
                        if (query.isNotEmpty()) {
                            searchOrders(query)
                        } else {
                            updateDisplayedOrders()
                        }

                        Log.d("DEBUG_ORDER", "Loaded ${allOrders.size} orders")

                        runOnUiThread {
                            if (displayedOrders.isEmpty()) {
                                tvEmptyState.visibility = View.VISIBLE
                                tvEmptyState.text = "Tidak ada order $status"
                            } else {
                                tvEmptyState.visibility = View.GONE
                            }

                            Toast.makeText(
                                this@OrderListActivity,
                                "${displayedOrders.size} order ditemukan",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Log.e("DEBUG_ORDER", "API success=false or data null")
                        runOnUiThread {
                            tvEmptyState.visibility = View.VISIBLE
                            tvEmptyState.text = apiResponse?.message ?: "Gagal memuat data"
                            displayedOrders.clear()
                            orderAdapter.updateList(displayedOrders)
                        }
                    }
                } else {
                    Log.e("DEBUG_ORDER", "Response not successful: ${response.code()}")
                    runOnUiThread {
                        tvEmptyState.visibility = View.VISIBLE
                        tvEmptyState.text = "Error: ${response.code()}"
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<Order>>>, t: Throwable) {
                Log.e("DEBUG_ORDER", "API Call Failed", t)
                runOnUiThread {
                    tvEmptyState.visibility = View.VISIBLE
                    tvEmptyState.text = "Gagal terhubung ke server"
                    Toast.makeText(
                        this@OrderListActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    private fun applyStatusFilter(status: String) {
        currentStatus = status

        displayedOrders.clear()
        if (status == "all") {
            displayedOrders.addAll(allOrders)
        } else {
            displayedOrders.addAll(allOrders.filter { it.status == status })
        }

        // Apply current sort
        applySort(currentSort)
    }

    private fun searchOrders(query: String) {
        if (query.isEmpty()) {
            // Reset ke filtered list berdasarkan status
            applyStatusFilter(currentStatus)
            updateDisplayedOrders()
            return
        }

        val filteredList = allOrders.filter { order ->
            (order.namaPemesan.contains(query, ignoreCase = true)) ||
                    (order.namaProduct?.contains(query, ignoreCase = true) == true) ||
                    (order.telp.contains(query, ignoreCase = true)) ||
                    (order.status?.contains(query, ignoreCase = true) == true)
        }

        // Filter lagi berdasarkan status
        val statusFiltered = if (currentStatus == "all") {
            filteredList
        } else {
            filteredList.filter { it.status == currentStatus }
        }

        displayedOrders.clear()
        displayedOrders.addAll(statusFiltered)
        applySort(currentSort)
        updateDisplayedOrders()
    }

    private fun applySort(sortType: Int) {
        currentSort = sortType

        val sortedList = when (sortType) {
            0 -> displayedOrders.sortedByDescending {
                // Sort by created_at (terbaru)
                it.createdAt ?: "1970-01-01"
            }
            1 -> displayedOrders.sortedBy {
                // Sort by created_at (terlama)
                it.createdAt ?: "1970-01-01"
            }
            2 -> displayedOrders.sortedByDescending {
                // Sort by harga tertinggi
                it.harga ?: 0
            }
            3 -> displayedOrders.sortedBy {
                // Sort by harga terendah
                it.harga ?: 0
            }
            else -> displayedOrders
        }

        displayedOrders.clear()
        displayedOrders.addAll(sortedList)
    }

    private fun updateDisplayedOrders() {
        runOnUiThread {
            orderAdapter.updateList(displayedOrders)

            if (displayedOrders.isEmpty()) {
                tvEmptyState.visibility = View.VISIBLE
                val query = searchInput.text.toString()
                tvEmptyState.text = if (query.isNotEmpty()) {
                    "Tidak ditemukan hasil untuk '$query'"
                } else {
                    "Tidak ada order $currentStatus"
                }
            } else {
                tvEmptyState.visibility = View.GONE
            }
        }
    }

    private fun showStatusFilterDialog() {
        val statuses = arrayOf("All", "Process", "Done", "Cancelled")

        android.app.AlertDialog.Builder(this)
            .setTitle("Filter by Status")
            .setItems(statuses) { _, which ->
                currentStatus = when (which) {
                    0 -> "all"
                    1 -> "Process"
                    2 -> "Done"
                    3 -> "Cancelled"
                    else -> "all"
                }

                filterStatusButton.text = statuses[which]
                loadOrders(currentStatus)
            }
            .show()
    }

    private fun showSortDialog() {
        val sortOptions = arrayOf("Terbaru", "Terlama", "Harga Tertinggi", "Harga Terendah")

        android.app.AlertDialog.Builder(this)
            .setTitle("Sort by")
            .setItems(sortOptions) { _, which ->
                filterSortButton.text = sortOptions[which]
                currentSort = which
                applySort(which)
                updateDisplayedOrders()
            }
            .show()
    }

    private fun setupNavigation() {
        findViewById<android.widget.LinearLayout>(R.id.nav_home_container).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        findViewById<android.widget.LinearLayout>(R.id.nav_manual_order_container).setOnClickListener {
            startActivity(Intent(this, OrderManualActivity::class.java))
        }

        findViewById<android.widget.LinearLayout>(R.id.nav_review_container).setOnClickListener {
            startActivity(Intent(this, OrderRecapActivity::class.java))
        }

        findViewById<android.widget.LinearLayout>(R.id.nav_order_list_container).setOnClickListener {
            // Already on this screen
        }

        findViewById<android.widget.LinearLayout>(R.id.nav_topic_container).setOnClickListener {
            startActivity(Intent(this, TopicActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data ketika kembali ke layar
        loadOrders(currentStatus)
    }
}