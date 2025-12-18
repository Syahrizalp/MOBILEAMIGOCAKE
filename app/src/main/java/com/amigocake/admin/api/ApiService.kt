package com.amigocake.admin.api

import com.amigocake.admin.models.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // ========== USERS API ==========
    @POST("users.php?action=login")
    fun login(@Body loginRequest: LoginRequest): Call<ApiResponse<User>>

    @POST("users.php?action=register")
    fun register(@Body registerRequest: RegisterRequest): Call<ApiResponse<User>>

    @GET("users.php")
    fun getUser(@Query("id") userId: Int): Call<ApiResponse<User>>

    // ========== ORDERS API ==========
    @GET("orders.php")
    fun getAllOrders(): Call<ApiResponse<List<Order>>>

    @GET("orders.php")
    fun getOrderById(@Query("id") orderId: Int): Call<ApiResponse<Order>>

    @GET("orders.php")
    fun getOrdersByUserId(@Query("user_id") userId: Int): Call<ApiResponse<List<Order>>>

    @GET("orders.php")
    fun getOrdersByStatus(@Query("status") status: String): Call<ApiResponse<List<Order>>>

    @POST("orders.php")
    fun createOrder(@Body order: OrderRequest): Call<ApiResponse<OrderResponse>>

    @PUT("orders.php")
    fun updateOrder(@Body order: OrderUpdateRequest): Call<ApiResponse<String>>

    @DELETE("orders.php")
    fun deleteOrder(@Query("id") orderId: Int): Call<ApiResponse<String>>

    // PRODUCT API
    @GET("products.php")
    fun getAllProducts(): Call<ApiResponse<List<Product>>>

    @GET("products.php")
    fun getProductById(@Query("id") productId: Int): Call<ApiResponse<Product>>

    @DELETE("products.php")
    fun deleteProduct(@Query("id") productId: Int): Call<ApiResponse<String>>

    // Untuk create dan update, jika belum ada:
    @POST("products.php")
    fun createProduct(@Body productData: Map<String, Any>): Call<ApiResponse<Map<String, Int>>>

    @PUT("products.php")
    fun updateProduct(@Body productData: Map<String, Any>): Call<ApiResponse<String>>

    // ========== STATISTICS API ==========
    @GET("statistics.php")
    fun getDashboardStatistics(): Call<ApiResponse<DashboardStats>>

    // ========== GALERY API ==========
    @GET("galery.php")
    fun getAllGalery(): Call<ApiResponse<List<Galery>>>

    // ========== RECAP API ==========
    @GET("order_recap.php")
    fun getOrderRecap(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Call<OrderRecapResponse>
}