package com.amigocake.admin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileEmail: TextView
    private lateinit var btnBack: ImageView
    private lateinit var btnLogout: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sharedPreferences = getSharedPreferences("AmigoCakePrefs", Context.MODE_PRIVATE)

        // Check login status
        if (!isUserLoggedIn()) {
            navigateToLogin()
            return
        }

        initViews()
        setupClickListeners()
        loadUserData()
    }

    private fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false) &&
                sharedPreferences.getInt("userId", 0) > 0
    }

    private fun initViews() {
        tvProfileName = findViewById(R.id.tvProfileName)
        tvProfileEmail = findViewById(R.id.tvProfileEmail)
        btnBack = findViewById(R.id.btnBack)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun loadUserData() {
        val userName = sharedPreferences.getString("userName", "Admin")
        val username = sharedPreferences.getString("username", "admin")
        val userLevel = sharedPreferences.getString("userLevel", "ADMIN")

        // Display user data
        tvProfileName.text = userName ?: "Admin"

        // Karena tidak ada email di database, tampilkan username
        tvProfileEmail.text = "Username: ${username ?: "admin"}\nLevel: ${userLevel ?: "ADMIN"}"
    }

    private fun setupClickListeners() {
        // Tombol kembali ke HomeActivity
        btnBack.setOnClickListener {
            navigateToHome()
        }

        // Tombol logout
        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun navigateToHome() {
        // Kembali ke HomeActivity
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Logout")
            .setMessage("Apakah Anda yakin ingin keluar dari akun?")
            .setPositiveButton("Ya, Logout") { dialog, which ->
                performLogout()
            }
            .setNegativeButton("Batal", null)
            .setCancelable(true)
            .show()
    }

    private fun performLogout() {
        // Clear semua data shared preferences
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        // Redirect ke LoginActivity dengan clear stack
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()

        Toast.makeText(this, "Anda telah logout", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Override back button untuk kembali ke Home
        navigateToHome()
    }
}