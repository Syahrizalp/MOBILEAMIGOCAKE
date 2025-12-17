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

    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)

    private val monthNames = arrayOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_recap)

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
                        return value.toInt().toString()
                    }
                }
            }

            // Left Y Axis
            axisLeft.apply {
                setDrawGridLines(true)
                granularity = 1f
                textColor = Color.BLACK
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
            minValue = currentYear - 10 // 10 tahun ke belakang
            maxValue = currentYear + 5   // 5 tahun ke depan
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

        // ✅ PERBAIKAN: Gunakan ApiConfig.apiService langsung
        ApiConfig.apiService.getOrderRecap(month, year)
            .enqueue(object : Callback<OrderRecapResponse> {

                override fun onResponse(
                    call: Call<OrderRecapResponse>,
                    response: Response<OrderRecapResponse>
                ) {
                    Log.d("RECAP", "Response code: ${response.code()}")

                    if (!response.isSuccessful) {
                        Log.e("RECAP", "HTTP Error: ${response.code()}")
                        Toast.makeText(
                            this@OrderRecapActivity,
                            "Error loading data: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    val recapResponse = response.body()
                    Log.d("RECAP", "Response body: $recapResponse")

                    if (recapResponse == null) {
                        Log.e("RECAP", "Response body is null")
                        Toast.makeText(
                            this@OrderRecapActivity,
                            "No data received",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    if (!recapResponse.success) {
                        Log.e("RECAP", "API returned success=false: ${recapResponse.message}")
                        Toast.makeText(
                            this@OrderRecapActivity,
                            recapResponse.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    val data = recapResponse.data
                    if (data == null) {
                        Log.e("RECAP", "Data is null")
                        return
                    }

                    Log.d("RECAP", "Total Orders: ${data.totalOrder}")
                    Log.d("RECAP", "Total Revenue: ${data.totalPendapatan}")
                    Log.d("RECAP", "Chart data size: ${data.chart.size}")

                    // Update UI
                    tvTotalOrders.text = "Total Order : ${data.totalOrder} Order"
                    tvTotalRevenue.text = "Total Pendapatan : ${formatRupiah(data.totalPendapatan)}"

                    // Update Chart
                    updateChart(data.chart)
                }

                override fun onFailure(
                    call: Call<OrderRecapResponse>,
                    t: Throwable
                ) {
                    Log.e("RECAP", "API Call Failed", t)
                    Toast.makeText(
                        this@OrderRecapActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
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

        for (item in chartData) {
            val total = item.total.toFloatOrNull()
            if (total != null) {
                entries.add(Entry(item.day.toFloat(), total))
                Log.d("RECAP", "Chart entry: day=${item.day}, total=$total")
            }
        }

        if (entries.isEmpty()) {
            lineChart.clear()
            lineChart.invalidate()
            Log.d("RECAP", "No valid entries")
            return
        }

        val dataSet = LineDataSet(entries, "Pendapatan Harian (Rp)").apply {
            color = Color.parseColor("#982B15")
            lineWidth = 2.5f
            setCircleColor(Color.parseColor("#982B15"))
            circleRadius = 4f
            setDrawCircleHole(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
            setDrawFilled(true)
            fillColor = Color.parseColor("#FFCDD2")
            fillAlpha = 50
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.animateX(1000)
        lineChart.invalidate()

        Log.d("RECAP", "Chart updated successfully")
    }

    // ================= BOTTOM NAV =================
    private fun setupBottomNavigation() {
        findViewById<LinearLayout>(R.id.nav_home_container).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.nav_manual_order_container).setOnClickListener {
            startActivity(Intent(this, OrderManualActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.nav_order_list_container).setOnClickListener {
            startActivity(Intent(this, OrderListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.nav_review_container).setOnClickListener {
            // Already on this screen
        }

        findViewById<LinearLayout>(R.id.nav_topic_container).setOnClickListener {
            startActivity(Intent(this, TopicActivity::class.java))
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
}