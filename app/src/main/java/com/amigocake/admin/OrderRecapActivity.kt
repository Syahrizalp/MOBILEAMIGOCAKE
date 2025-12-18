package com.amigocake.admin

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amigocake.admin.api.ApiConfig
import com.amigocake.admin.databinding.ActivityOrderManualBinding
import com.amigocake.admin.databinding.ActivityOrderRecapBinding
import com.amigocake.admin.models.OrderRecapResponse
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.*

class OrderRecapActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private lateinit var tvTotalOrders: TextView
    private lateinit var tvTotalRevenue: TextView
    private lateinit var btnMonthPicker: MaterialButton
    private lateinit var imgProfile: ImageView
    private lateinit var binding: ActivityOrderRecapBinding

    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)

    private val monthNames = arrayOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_recap)

        binding = ActivityOrderRecapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        setupLineChart()
        setupMonthPicker()
        setupBottomNavigation()
        setupProfileClick()

        // Set initial month text
        updateMonthButtonText()

        loadRecapData(selectedMonth, selectedYear)
    }

    // ================= INIT VIEW =================
    private fun initView() {
        lineChart = findViewById(R.id.lineChart)
        tvTotalOrders = findViewById(R.id.tv_total_orders)
        tvTotalRevenue = findViewById(R.id.tv_total_revenue)
        btnMonthPicker = findViewById(R.id.buttonMonthPicker)
        imgProfile = findViewById(R.id.imageView7)
    }

    // ================= LINE CHART =================
    private fun setupLineChart() {
        lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)

            // X Axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                textColor = Color.BLACK
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()} ${monthNames[selectedMonth - 1]}"
                    }
                }
            }

            // Left Y Axis
            axisLeft.apply {
                setDrawGridLines(true)
                granularity = 1000f  // Kelipatan 1000 untuk rupiah
                textColor = Color.BLACK
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return formatRupiahShort(value.toInt())
                    }
                }
            }

            // Right Y Axis
            axisRight.isEnabled = false

            // Legend
            legend.isEnabled = true

            setNoDataText("Tidak ada data untuk bulan ini")
            setNoDataTextColor(Color.GRAY)
        }
    }

    // ================= MONTH & YEAR PICKER =================
    private fun setupMonthPicker() {
        btnMonthPicker.setOnClickListener {
            showMonthYearPicker()
        }
    }

    private fun showMonthYearPicker() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_month_year_picker, null)

        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.month_picker)
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.year_picker)

        // Setup Month Picker
        monthPicker.apply {
            minValue = 0
            maxValue = 11
            value = selectedMonth - 1
            displayedValues = monthNames
            wrapSelectorWheel = true
        }

        // Setup Year Picker
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.apply {
            minValue = currentYear - 2 // 2 tahun ke belakang
            maxValue = currentYear     // Hingga tahun ini
            value = selectedYear
            wrapSelectorWheel = false
        }

        AlertDialog.Builder(this)
            .setTitle("Pilih Bulan dan Tahun")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                selectedMonth = monthPicker.value + 1
                selectedYear = yearPicker.value

                updateMonthButtonText()
                loadRecapData(selectedMonth, selectedYear)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateMonthButtonText() {
        btnMonthPicker.text = "${monthNames[selectedMonth - 1]} $selectedYear"
    }

    // ================= LOAD DATA (API) =================
    private fun loadRecapData(month: Int, year: Int) {
        Log.d("RECAP", "Loading data for month: $month, year: $year")

        // Tampilkan loading state
        showLoading(true)

        ApiConfig.apiService.getOrderRecap(month, year)
            .enqueue(object : Callback<OrderRecapResponse> {

                override fun onResponse(
                    call: Call<OrderRecapResponse>,
                    response: Response<OrderRecapResponse>
                ) {
                    showLoading(false)

                    Log.d("RECAP", "Response code: ${response.code()}")

                    if (!response.isSuccessful) {
                        Log.e("RECAP", "HTTP Error: ${response.code()}")
                        showEmptyState("Error: ${response.code()}")
                        return
                    }

                    val recapResponse = response.body()
                    Log.d("RECAP", "Response body: $recapResponse")

                    if (recapResponse == null) {
                        Log.e("RECAP", "Response body is null")
                        showEmptyState("Tidak ada data diterima")
                        return
                    }

                    if (!recapResponse.success) {
                        Log.e("RECAP", "API returned success=false: ${recapResponse.message}")
                        showEmptyState(recapResponse.message)
                        return
                    }

                    val data = recapResponse.data
                    if (data == null) {
                        Log.e("RECAP", "Data is null")
                        showEmptyState("Data kosong")
                        return
                    }

                    Log.d("RECAP", "Total Orders: ${data.totalOrder}")
                    Log.d("RECAP", "Total Revenue: ${data.totalPendapatan}")
                    Log.d("RECAP", "Chart data size: ${data.chart.size}")

                    // Update UI
                    updateUI(data)
                }

                override fun onFailure(
                    call: Call<OrderRecapResponse>,
                    t: Throwable
                ) {
                    showLoading(false)
                    Log.e("RECAP", "API Call Failed", t)
                    showEmptyState("Gagal menghubungi server")
                }
            })
    }

    private fun showLoading(show: Boolean) {
        // Anda bisa tambah ProgressBar di XML jika mau
        if (show) {
            lineChart.visibility = android.view.View.GONE
            tvTotalOrders.text = "Memuat data..."
            tvTotalRevenue.text = ""
        } else {
            lineChart.visibility = android.view.View.VISIBLE
        }
    }

    private fun showEmptyState(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        lineChart.clear()
        lineChart.invalidate()
        tvTotalOrders.text = "Total Order : 0 Order"
        tvTotalRevenue.text = "Total Pendapatan : Rp 0"
    }

    private fun updateUI(data: com.amigocake.admin.models.RecapData) {
        tvTotalOrders.text = "Total Order : ${data.totalOrder} Order"
        tvTotalRevenue.text = "Total Pendapatan : ${formatRupiah(data.totalPendapatan)}"

        if (data.totalPendapatan > 0) {
            updateChart(data.chart)
        } else {
            lineChart.clear()
            lineChart.invalidate()
            lineChart.setNoDataText("Tidak ada transaksi di bulan ini")
        }
    }

    // ================= UPDATE CHART =================
    private fun updateChart(chartData: List<com.amigocake.admin.models.ChartItem>) {
        Log.d("RECAP", "Updating chart with ${chartData.size} items")

        if (chartData.isEmpty()) {
            lineChart.clear()
            lineChart.invalidate()
            Log.d("RECAP", "Chart cleared - no data")
            return
        }

        val entries = mutableListOf<Entry>()

        // Sort by day
        val sortedChartData = chartData.sortedBy { it.day }

        for (item in sortedChartData) {
            val total = item.total.toFloatOrNull() ?: 0f
            entries.add(Entry(item.day.toFloat(), total))
            Log.d("RECAP", "Chart entry: day=${item.day}, total=$total")
        }

        if (entries.isEmpty()) {
            lineChart.clear()
            lineChart.invalidate()
            Log.d("RECAP", "No valid entries")
            return
        }

        val dataSet = LineDataSet(entries, "Pendapatan Harian").apply {
            color = Color.parseColor("#982B15")
            lineWidth = 2.5f
            setCircleColor(Color.parseColor("#982B15"))
            circleRadius = 4f
            setDrawCircleHole(false)
            setDrawValues(true)
            valueTextSize = 10f
            valueTextColor = Color.BLACK
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return formatRupiahShort(value.toInt())
                }
            }
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
            setDrawFilled(true)
            fillColor = Color.parseColor("#FFCDD2")
            fillAlpha = 50
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.animateX(1000)

        // Set viewport untuk menampilkan semua data
        lineChart.xAxis.axisMinimum = 1f
        lineChart.xAxis.axisMaximum = 31f
        lineChart.invalidate()

        Log.d("RECAP", "Chart updated successfully")
    }

    // ================= BOTTOM NAV =================
    private fun setupBottomNavigation() {
        binding.navHomeContainer.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        binding.navManualOrderContainer.setOnClickListener {
            startActivity(Intent(this, OrderManualActivity::class.java))
        }

        binding.navOrderListContainer.setOnClickListener {
            startActivity(Intent(this, OrderListActivity::class.java))
        }

        binding.navReviewContainer.setOnClickListener {
            // Already on this screen
        }

        binding.navTopicContainer.setOnClickListener {
            startActivity(Intent(this, ProductManagementActivity::class.java))
        }

        binding.imageView7.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    // ================= PROFILE =================
    private fun setupProfileClick() {
        imgProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    // ================= FORMAT RUPIAH =================
    private fun formatRupiah(value: Int): String {
        val localeID = Locale("id", "ID")
        val format = NumberFormat.getCurrencyInstance(localeID)
        return format.format(value).replace("Rp", "Rp ")
    }

    private fun formatRupiahShort(value: Int): String {
        return if (value >= 1000000) {
            "Rp${value / 1000000}JT"
        } else if (value >= 1000) {
            "Rp${value / 1000}K"
        } else {
            "Rp$value"
        }
    }
}