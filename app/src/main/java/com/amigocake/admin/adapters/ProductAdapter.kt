package com.amigocake.admin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amigocake.admin.R
import com.amigocake.admin.api.ApiConfig
import com.amigocake.admin.models.Product
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class ProductAdapter(
    private var products: List<Product>,
    private val onItemClick: (Product) -> Unit,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)
    }

    override fun getItemCount() = products.size

    fun updateList(newList: List<Product>) {
        products = newList
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvProductCategory)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)

        fun bind(product: Product) {
            // Load image dengan Glide
            loadProductImage(product.pathGambar)

            tvName.text = product.nama
            tvCategory.text = "${product.kategori} | Diameter: ${product.diameter}"
            tvPrice.text = "Rp ${formatRupiah(product.harga)}"

            // Item click for detail
            itemView.setOnClickListener {
                onItemClick(product)
            }

            // Edit button click
            btnEdit.setOnClickListener {
                onEditClick(product)
            }

            // Delete button click
            btnDelete.setOnClickListener {
                onDeleteClick(product)
            }
        }

        private fun loadProductImage(imagePath: String) {
            if (imagePath.isNotEmpty()) {
                // Format URL yang benar
                val baseUrl = "https://amigocake.com/frontend_costumer/"
                val imageUrl = if (imagePath.startsWith("http")) {
                    imagePath
                } else if (imagePath.startsWith("/")) {
                    baseUrl + imagePath.substring(1)
                } else {
                    baseUrl + imagePath
                }

                // Debug log
                println("DEBUG - Loading product image: $imageUrl")

                try {
                    Glide.with(itemView.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivImage)
                } catch (e: Exception) {
                    e.printStackTrace()
                    ivImage.setImageResource(R.drawable.placeholder_image)
                }
            } else {
                ivImage.setImageResource(R.drawable.placeholder_image)
            }
        }

        private fun formatRupiah(amount: Int): String {
            return String.format("%,d", amount).replace(',', '.')
        }
    }
}