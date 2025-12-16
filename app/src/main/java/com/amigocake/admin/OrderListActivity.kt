package com.amigocake.admin

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
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

    private var orderList = mutableListOf<Order>()
    private var currentStatus = "all"

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
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(orderList) { order ->
            // Handle item click
            val intent = Intent(this, OrderDetailActivity::class.java)
            intent.putExtra("ORDER_ID", order.id)
            startActivity(intent)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@OrderListActivity)
            adapter = orderAdapter
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

        // Search functionality
        searchInput.setOnEditorActionListener { _, _, _ ->
            searchOrders(searchInput.text.toString())
            true
        }
    }

    private fun loadOrders(status: String = "all") {
        val apiService = ApiConfig.getApiService()

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
                        orderList.clear()
                        orderList.addAll(apiResponse.data)
                        orderAdapter.notifyDataSetChanged()

                        Toast.makeText(
                            this@OrderListActivity,
                            "${orderList.size} orders loaded",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@OrderListActivity,
                            apiResponse?.message ?: "Failed to load orders",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<Order>>>, t: Throwable) {
                Toast.makeText(
                    this@OrderListActivity,
                    "Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun searchOrders(query: String) {
        if (query.isEmpty()) {
            loadOrders(currentStatus)
            return
        }

        val filteredList = orderList.filter { order ->
            order.namaPemesan.contains(query, ignoreCase = true) ||
                    (order.namaProduct?.contains(query, ignoreCase = true) == true)
        }

        orderAdapter.updateList(filteredList)
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
                sortOrders(which)
            }
            .show()
    }

    private fun sortOrders(sortType: Int) {
        when (sortType) {
            0 -> orderList.sortByDescending { it.createdAt }
            1 -> orderList.sortBy { it.createdAt }
            2 -> orderList.sortByDescending { it.harga }
            3 -> orderList.sortBy { it.harga }
        }
        orderAdapter.notifyDataSetChanged()
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
}