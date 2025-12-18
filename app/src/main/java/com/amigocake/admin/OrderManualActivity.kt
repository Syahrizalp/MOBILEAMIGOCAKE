package com.amigocake.admin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amigocake.admin.api.ApiConfig
import com.amigocake.admin.databinding.ActivityOrderManualBinding
import com.amigocake.admin.models.ApiResponse
import com.amigocake.admin.models.OrderRequest
import com.amigocake.admin.models.OrderResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

class OrderManualActivity : AppCompatActivity() {

    private lateinit var inputCustomerName: EditText
    private lateinit var inputCustomerContact: EditText
    private lateinit var inputAddress: EditText
    private lateinit var inputProductName: EditText
    private lateinit var inputDiameter: EditText
    private lateinit var inputQuantity: EditText
    private lateinit var inputPickupDate: EditText
    private lateinit var inputPrice: EditText
    private lateinit var buttonOrder: Button
    private lateinit var binding: ActivityOrderManualBinding


    private var selectedDate = ""
    private var selectedTime = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_manual)

        binding = ActivityOrderManualBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupPriceFormatter()
        setupClickListeners()
        setupNavigation()
    }

    private fun initViews() {
        inputCustomerName = findViewById(R.id.input_customer_name)
        inputCustomerContact = findViewById(R.id.input_customer_contact)
        inputAddress = findViewById(R.id.input_address)
        inputProductName = findViewById(R.id.input_product_name)
        inputDiameter = findViewById(R.id.input_diameter)
        inputQuantity = findViewById(R.id.input_quantity)
        inputPickupDate = findViewById(R.id.input_pickup_date)
        inputPrice = findViewById(R.id.input_price)
        buttonOrder = findViewById(R.id.button_order)
    }

    // ================= PRICE FORMATTER =================
    private fun setupPriceFormatter() {
        inputPrice.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return

                isFormatting = true

                // Hapus semua karakter non-digit
                val cleanString = s.toString().replace("[^\\d]".toRegex(), "")

                if (cleanString.isNotEmpty()) {
                    try {
                        // Format dengan pemisah ribuan
                        val formatted = formatRupiah(cleanString.toLong())
                        inputPrice.setText(formatted)
                        inputPrice.setSelection(formatted.length)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                isFormatting = false
            }
        })
    }

    // Format angka dengan pemisah ribuan (titik)
    private fun formatRupiah(value: Long): String {
        val symbols = DecimalFormatSymbols(Locale("id", "ID"))
        symbols.groupingSeparator = '.'
        val formatter = DecimalFormat("#,###", symbols)
        return formatter.format(value)
    }

    // Ambil nilai price sebagai integer (tanpa format)
    private fun getPriceValue(): Int {
        val cleanString = inputPrice.text.toString().replace("[^\\d]".toRegex(), "")
        return if (cleanString.isNotEmpty()) cleanString.toInt() else 0
    }

    private fun setupClickListeners() {
        // Date picker
        inputPickupDate.setOnClickListener {
            showDatePicker()
        }

        // Save order button
        buttonOrder.setOnClickListener {
            createOrder()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectedDate = dateFormat.format(calendar.time)

                val displayFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                inputPickupDate.setText(displayFormat.format(calendar.time))

                // Show time picker after date is selected
                showTimePicker()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
            show()
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()

        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedTime = String.format("%02d:%02d:00", hourOfDay, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun createOrder() {
        // Validate inputs
        val customerName = inputCustomerName.text.toString().trim()
        val customerContact = inputCustomerContact.text.toString().trim()
        val address = inputAddress.text.toString().trim()
        val productName = inputProductName.text.toString().trim()
        val diameter = inputDiameter.text.toString().trim()
        val price = getPriceValue() // ✅ Gunakan fungsi getPriceValue()

        if (customerName.isEmpty() || customerContact.isEmpty() ||
            address.isEmpty() || selectedDate.isEmpty() || price == 0) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Get user ID from SharedPreferences
        val sharedPreferences = getSharedPreferences("AmigoCakePrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", 0)

        // Create order request
        val orderRequest = OrderRequest(
            idUsers = userId,
            kategori = "Custom Cake",
            idProduct = null,
            namaPemesan = customerName,
            telp = customerContact,
            alamat = address,
            tanggal = selectedDate,
            diameter = diameter,
            varian = productName,
            tulisan = "",
            harga = price,
            waktu = selectedTime.ifEmpty { null }
        )

        // Disable button
        buttonOrder.isEnabled = false
        buttonOrder.text = "Saving..."

        // API call
        val apiService = ApiConfig.apiService
        apiService.createOrder(orderRequest).enqueue(object : Callback<ApiResponse<OrderResponse>> {
            override fun onResponse(
                call: Call<ApiResponse<OrderResponse>>,
                response: Response<ApiResponse<OrderResponse>>
            ) {
                buttonOrder.isEnabled = true
                buttonOrder.text = "Save Order"

                if (response.isSuccessful) {
                    val apiResponse = response.body()

                    if (apiResponse?.success == true) {
                        showSuccessPopup()
                    } else {
                        Toast.makeText(
                            this@OrderManualActivity,
                            apiResponse?.message ?: "Failed to create order",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@OrderManualActivity,
                        "Server error: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<OrderResponse>>, t: Throwable) {
                buttonOrder.isEnabled = true
                buttonOrder.text = "Save Order"

                Toast.makeText(
                    this@OrderManualActivity,
                    "Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun showSuccessPopup() {
        val popupContainer = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(
            R.id.popup_order_successful
        )
        popupContainer.visibility = android.view.View.VISIBLE

        findViewById<Button>(R.id.popup_button_close).setOnClickListener {
            popupContainer.visibility = android.view.View.GONE
            clearForm()
        }
    }

    private fun clearForm() {
        inputCustomerName.text.clear()
        inputCustomerContact.text.clear()
        inputAddress.text.clear()
        inputProductName.text.clear()
        inputDiameter.text.clear()
        inputQuantity.text.clear()
        inputPickupDate.text.clear()
        inputPrice.text.clear()
        selectedDate = ""
        selectedTime = ""
    }

    private fun setupNavigation() {
        binding.navHomeContainer.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        binding.navManualOrderContainer.setOnClickListener {
            // Already on this screen
        }

        binding.navReviewContainer.setOnClickListener {
            startActivity(Intent(this, OrderRecapActivity::class.java))
        }

        binding.navOrderListContainer.setOnClickListener {
            startActivity(Intent(this, OrderListActivity::class.java))
        }

        binding.navTopicContainer.setOnClickListener {
            startActivity(Intent(this, ProductManagementActivity::class.java))
        }

        binding.profileIcon.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

}