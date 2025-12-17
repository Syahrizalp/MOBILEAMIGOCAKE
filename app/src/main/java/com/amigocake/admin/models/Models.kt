package com.amigocake.admin.models

import com.google.gson.annotations.SerializedName

// ========== ApiResponse ==========
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: T?
)

// ========== User ==========
data class User(
    @SerializedName("ID_USERS")
    val id: Int,

    @SerializedName("NAMA")
    val nama: String,

    @SerializedName("USERNAME")
    val username: String,

    @SerializedName("LEVEL")
    val level: String
)

// ========== Login & Register Requests ==========
data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val nama: String,
    val username: String,
    val password: String
)

// ========== Order ==========
data class Order(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("id_users")
    val idUsers: Int? = null,

    @SerializedName("kategori")
    val kategori: String? = null,

    @SerializedName("id_product")
    val idProduct: Int? = null,

    @SerializedName("nama_pemesan")
    val namaPemesan: String,

    @SerializedName("telp")
    val telp: String,

    @SerializedName("alamat")
    val alamat: String,

    @SerializedName("tanggal")
    val tanggal: String,

    @SerializedName("diameter")
    val diameter: String? = "",

    @SerializedName("varian")
    val varian: String? = "",

    @SerializedName("tulisan")
    val tulisan: String? = "",

    @SerializedName("harga")
    val harga: Int = 0,

    @SerializedName("waktu")
    val waktu: String? = null,

    @SerializedName("status")
    val status: String = "Process",

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("customer_name")
    val customerName: String? = null,

    @SerializedName("NAMA_PRODUCT")
    val namaProduct: String? = null
)

// ========== Order Request (untuk create order) ==========
data class OrderRequest(
    @SerializedName("id_users")
    val idUsers: Int? = null,

    @SerializedName("kategori")
    val kategori: String? = null,

    @SerializedName("id_product")
    val idProduct: Int? = null,

    @SerializedName("nama_pemesan")
    val namaPemesan: String,

    @SerializedName("telp")
    val telp: String,

    @SerializedName("alamat")
    val alamat: String,

    @SerializedName("tanggal")
    val tanggal: String,

    @SerializedName("diameter")
    val diameter: String? = "",

    @SerializedName("varian")
    val varian: String? = "",

    @SerializedName("tulisan")
    val tulisan: String? = "",

    @SerializedName("harga")
    val harga: Int,

    @SerializedName("waktu")
    val waktu: String? = null
)

// ========== Order Response ==========
data class OrderResponse(
    @SerializedName("id")
    val id: Int
)

// ========== Order Update Request ==========
data class OrderUpdateRequest(
    @SerializedName("id")
    val id: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("nama_pemesan")
    val namaPemesan: String,

    @SerializedName("telp")
    val telp: String,

    @SerializedName("alamat")
    val alamat: String,

    @SerializedName("tanggal")
    val tanggal: String,

    @SerializedName("harga")
    val harga: Int
)

// ========== Product ==========
data class Product(
    @SerializedName("ID_PRODUCT")
    val id: Int,

    @SerializedName("NAMA_PRODUCT")
    val nama: String,

    @SerializedName("KATEGORI_PRODUCT")
    val kategori: String,

    @SerializedName("DIAMETER_SIZE")
    val diameter: String,

    @SerializedName("DESKRIPSI_PRODUCT")
    val deskripsi: String,

    @SerializedName("HARGA")
    val harga: Int,

    @SerializedName("PATH_GAMBAR")
    val pathGambar: String
)

// ========== Dashboard Statistics ==========
data class DashboardStats(
    @SerializedName("total_orders")
    val totalOrders: Int,

    @SerializedName("active_orders")
    val activeOrders: Int,

    @SerializedName("revenue_today")
    val revenueToday: Int,

    @SerializedName("revenue_month")
    val revenueMonth: Int,

    @SerializedName("nearest_deadline")
    val nearestDeadline: Order?,

    @SerializedName("recent_orders")
    val recentOrders: List<Order>?
)

// ========== Galery ==========
data class Galery(
    @SerializedName("id")
    val id: Int,

    @SerializedName("nama_kegiatan")
    val namaKegiatan: String,

    @SerializedName("image_path")
    val imagePath: String,

    @SerializedName("created_at")
    val createdAt: String
)

// ========== Order Recap ==========
data class OrderRecapResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: RecapData?
)

data class RecapData(
    @SerializedName("totalOrder")
    val totalOrder: Int,

    @SerializedName("totalPendapatan")
    val totalPendapatan: Int,

    @SerializedName("chart")
    val chart: List<ChartItem>
)

data class ChartItem(
    @SerializedName("day")
    val day: Int,

    @SerializedName("total")
    val total: String
)