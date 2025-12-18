package com.amigocake.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.amigocake.admin.adapters.ProductAdapter
import com.amigocake.admin.api.ApiConfig
import com.amigocake.admin.databinding.ActivityManagementOrderBinding
import com.amigocake.admin.models.ApiResponse
import com.amigocake.admin.models.Product
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManagementOrderBinding
    private lateinit var productAdapter: ProductAdapter
    private val products = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi View Binding
        binding = ActivityManagementOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ubah judul
        binding.textTitle.text = "Manajemen Produk Kue"

        setupRecyclerView()
        setupClickListeners()
        loadProducts()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            products,
            onItemClick = { product ->
                showProductDetail(product)
            },
            onEditClick = { product ->
                val intent = Intent(this, AddEditProductActivity::class.java).apply {
                    putExtra("PRODUCT_ID", product.id)
                    putExtra("MODE", "EDIT")
                }
                startActivity(intent)
            },
            onDeleteClick = { product ->
                confirmDeleteProduct(product)
            }
        )

        binding.recyclerViewManagement.apply {
            layoutManager = LinearLayoutManager(this@ProductManagementActivity)
            adapter = productAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddManagement.setOnClickListener {
            val intent = Intent(this, AddEditProductActivity::class.java).apply {
                putExtra("MODE", "ADD")
            }
            startActivity(intent)
        }


        // Bottom navigation
        binding.navHomeContainer.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        binding.navManualOrderContainer.setOnClickListener {
            startActivity(Intent(this, OrderManualActivity::class.java))
            finish()
        }

        binding.navReviewContainer.setOnClickListener {
            startActivity(Intent(this, OrderRecapActivity::class.java))
            finish()
        }

        binding.navOrderListContainer.setOnClickListener {
            startActivity(Intent(this, OrderListActivity::class.java))
            finish()
        }

        // Management sudah aktif
        binding.navTopicContainer.setOnClickListener {
            // Already here, do nothing
        }

        binding.ivAdminIcon1.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        
    }

    private fun loadProducts() {
        ApiConfig.apiService.getAllProducts().enqueue(object : Callback<ApiResponse<List<Product>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<Product>>>,
                response: Response<ApiResponse<List<Product>>>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    products.clear()
                    response.body()?.data?.let { productList ->
                        products.addAll(productList)
                    }
                    productAdapter.updateList(products)
                } else {
                    showToast("Gagal memuat produk")
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<Product>>>, t: Throwable) {
                showToast("Error jaringan: ${t.message}")
            }
        })
    }

    private fun showProductDetail(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Detail Produk")
            .setMessage(
                """
                Nama: ${product.nama}
                Kategori: ${product.kategori}
                Diameter: ${product.diameter}
                Harga: Rp ${formatRupiah(product.harga)}
                Deskripsi: ${product.deskripsi}
                """.trimIndent()
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun confirmDeleteProduct(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Produk")
            .setMessage("Yakin ingin menghapus ${product.nama}?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteProduct(product.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteProduct(productId: Int) {
        ApiConfig.apiService.deleteProduct(productId).enqueue(object : Callback<ApiResponse<String>> {
            override fun onResponse(
                call: Call<ApiResponse<String>>,
                response: Response<ApiResponse<String>>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    showToast("Produk berhasil dihapus")
                    loadProducts()
                } else {
                    showToast("Gagal menghapus produk")
                }
            }

            override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                showToast("Error jaringan: ${t.message}")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        loadProducts()
    }

    private fun formatRupiah(amount: Int): String {
        return String.format("%,d", amount).replace(',', '.')
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}