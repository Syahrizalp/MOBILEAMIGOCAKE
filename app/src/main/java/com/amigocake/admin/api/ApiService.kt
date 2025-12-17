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

    // ========== PRODUCTS API ==========
    @GET("products.php")
    fun getAllProducts(): Call<ApiResponse<List<Product>>>

    @GET("products.php")
    fun getProductById(@Query("id") productId: Int): Call<ApiResponse<Product>>

    @GET("products.php")
    fun getProductsByCategory(@Query("kategori") category: String): Call<ApiResponse<List<Product>>>

    // ========== STATISTICS API ==========
    @GET("statistics.php")
    fun getDashboardStatistics(): Call<ApiResponse<DashboardStats>>

    // ========== GALERY API ==========
    @GET("galery.php")
    fun getAllGalery(): Call<ApiResponse<List<Galery>>>

    // ========== RECAP API ==========
    @GET("recap.php")
    fun getOrderRecap(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Call<OrderRecapResponse>
}