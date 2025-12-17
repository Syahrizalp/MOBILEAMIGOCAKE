package com.amigocake.admin.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {

    private const val BASE_URL = "https://amigocake.com/frontend_costumer/api/"

    // Tambahkan base URL untuk gambar
    const val IMAGE_BASE_URL = "https://amigocake.com/frontend_costumer/uploads/bukti/"

    private val retrofit: Retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // Fungsi untuk mendapatkan URL gambar lengkap
    fun getImageUrl(imagePath: String?): String? {
        return if (imagePath.isNullOrEmpty()) {
            null
        } else if (imagePath.startsWith("http")) {
            imagePath
        } else {
            "$IMAGE_BASE_URL$imagePath"
        }
    }
}