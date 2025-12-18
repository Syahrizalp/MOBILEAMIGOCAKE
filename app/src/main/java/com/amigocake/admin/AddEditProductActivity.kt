package com.amigocake.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amigocake.admin.api.ApiConfig
import com.amigocake.admin.models.ApiResponse
import com.amigocake.admin.models.Product
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddEditProductActivity : AppCompatActivity() {

    private lateinit var ivProductImage: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var etProductName: EditText
    private lateinit var etCategory: EditText
    private lateinit var etDiameter: EditText
    private lateinit var etDescription: EditText
    private lateinit var etPrice: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private var productId: Int? = null
    private var imageUri: Uri? = null
    private var mode = "ADD" // "ADD" or "EDIT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_product)

        // Initialize views
        initViews()

        mode = intent.getStringExtra("MODE") ?: "ADD"
        productId = intent.getIntExtra("PRODUCT_ID", -1).takeIf { it != -1 }

        setupUI()
        if (mode == "EDIT" && productId != null) {
            loadProductData(productId!!)
        }
    }

    private fun initViews() {
        ivProductImage = findViewById(R.id.ivProductImage)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        etProductName = findViewById(R.id.etProductName)
        etCategory = findViewById(R.id.etCategory)
        etDiameter = findViewById(R.id.etDiameter)
        etDescription = findViewById(R.id.etDescription)
        etPrice = findViewById(R.id.etPrice)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
    }

    private fun setupUI() {
        btnSelectImage.setOnClickListener {
            selectImage()
        }

        btnSave.setOnClickListener {
            if (validateInput()) {
                if (mode == "ADD") {
                    createProduct()
                } else {
                    updateProduct()
                }
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(): Boolean {
        if (etProductName.text.isNullOrEmpty()) {
            Toast.makeText(this, "Nama produk harus diisi", Toast.LENGTH_SHORT).show()
            return false
        }
        if (etCategory.text.isNullOrEmpty()) {
            Toast.makeText(this, "Kategori harus diisi", Toast.LENGTH_SHORT).show()
            return false
        }
        if (etPrice.text.isNullOrEmpty()) {
            Toast.makeText(this, "Harga harus diisi", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            ivProductImage.setImageURI(imageUri)
        }
    }

    private fun loadProductData(id: Int) {
        ApiConfig.apiService.getProductById(id).enqueue(object : Callback<ApiResponse<Product>> {
            override fun onResponse(
                call: Call<ApiResponse<Product>>,
                response: Response<ApiResponse<Product>>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val product = response.body()?.data
                    product?.let { displayProductData(it) }
                } else {
                    Toast.makeText(this@AddEditProductActivity, "Gagal memuat produk", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<Product>>, t: Throwable) {
                Toast.makeText(this@AddEditProductActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayProductData(product: Product) {
        etProductName.setText(product.nama)
        etCategory.setText(product.kategori)
        etDiameter.setText(product.diameter)
        etDescription.setText(product.deskripsi)
        etPrice.setText(product.harga.toString())

        // Load image nanti dengan Glide
        if (product.pathGambar.isNotEmpty()) {
            val imageUrl = "https://amigocake.com/frontend_costumer/${product.pathGambar}"
            // Implement load image
        }
    }

    private fun createProduct() {
        // Sementara buat produk tanpa gambar dulu
        val productData = hashMapOf<String, Any>(
            "nama" to etProductName.text.toString(),
            "kategori" to etCategory.text.toString(),
            "diameter" to etDiameter.text.toString(),
            "deskripsi" to etDescription.text.toString(),
            "harga" to etPrice.text.toString().toInt()
        )

        // TODO: Implement API call untuk create product
        showMessage("Fitur tambah produk akan segera tersedia")
    }

    private fun updateProduct() {
        if (productId == null) {
            Toast.makeText(this, "ID produk tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val productData = hashMapOf<String, Any>(
            "id" to productId!!,
            "nama" to etProductName.text.toString(),
            "kategori" to etCategory.text.toString(),
            "diameter" to etDiameter.text.toString(),
            "deskripsi" to etDescription.text.toString(),
            "harga" to etPrice.text.toString().toInt()
        )

        // TODO: Implement API call untuk update product
        showMessage("Fitur edit produk akan segera tersedia")
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 100
    }
}