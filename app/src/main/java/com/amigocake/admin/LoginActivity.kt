package com.amigocake.admin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amigocake.admin.api.ApiConfig
import com.amigocake.admin.models.ApiResponse
import com.amigocake.admin.models.LoginRequest
import com.amigocake.admin.models.User
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences("AmigoCakePrefs", Context.MODE_PRIVATE)

        // Check if already logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            navigateToHome()
            return
        }

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
    }

    private fun setupClickListeners() {
        findViewById<android.widget.TextView>(R.id.buttonsignin).setOnClickListener {
            login()
        }

        findViewById<android.widget.TextView>(R.id.tvSignUp).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val loginButton = findViewById<android.widget.TextView>(R.id.buttonsignin)
        loginButton.isEnabled = false
        loginButton.text = "Loading..."

        val loginRequest = LoginRequest(username, password)
        val apiService = ApiConfig.apiService

        apiService.login(loginRequest).enqueue(object : Callback<ApiResponse<User>> {
            override fun onResponse(
                call: Call<ApiResponse<User>>,
                response: Response<ApiResponse<User>>
            ) {
                loginButton.isEnabled = true
                loginButton.text = "Sign In"

                if (response.isSuccessful) {
                    val apiResponse = response.body()

                    if (apiResponse?.success == true && apiResponse.data != null) {
                        val user = apiResponse.data
                        saveUserData(user)

                        Toast.makeText(
                            this@LoginActivity,
                            "Login successful!",
                            Toast.LENGTH_SHORT
                        ).show()

                        navigateToHome()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            apiResponse?.message ?: "Login failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Server error: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<User>>, t: Throwable) {
                loginButton.isEnabled = true
                loginButton.text = "Sign In"

                Toast.makeText(
                    this@LoginActivity,
                    "Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun saveUserData(user: User) {
        sharedPreferences.edit().apply {
            putBoolean("isLoggedIn", true)
            putInt("userId", user.id)
            putString("userName", user.nama)
            putString("userEmail", user.username)
            putString("userLevel", user.level)
            apply()
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}