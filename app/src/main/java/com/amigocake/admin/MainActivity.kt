package com.amigocake.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val SPLASH_DELAY = 2000L // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check login status after delay
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, SPLASH_DELAY)
    }

    private fun checkLoginStatus() {
        val sharedPreferences = getSharedPreferences("AmigoCakePrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        val intent = if (isLoggedIn) {
            // User sudah login, langsung ke Home
            Intent(this, HomeActivity::class.java)
        } else {
            // User belum login, ke Login screen
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        finish() // Tutup MainActivity agar tidak bisa back
    }
}