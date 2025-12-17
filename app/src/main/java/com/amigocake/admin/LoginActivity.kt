package com.amigocake.admin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
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
        checkAutoLogin()

        initViews()
        setupClickListeners()
    }

    private fun checkAutoLogin() {
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            val userId = sharedPreferences.getInt("userId", 0)
            val userLevel = sharedPreferences.getString("userLevel", "")

            // Validasi data user
            if (userId > 0 && userLevel == "ADMIN") {
                navigateToHome()
            } else {
                // Data tidak valid, clear session
                sharedPreferences.edit().clear().apply()
            }
        }
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
            // RegisterActivity mungkin hanya untuk customer
            Toast.makeText(this, "Registrasi hanya untuk customer", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login() {
        if (!validateInput()) {
            return
        }

        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        showLoading(true)

        val loginRequest = LoginRequest(username, password)
        val apiService = ApiConfig.apiService

        apiService.login(loginRequest).enqueue(object : Callback<ApiResponse<User>> {
            override fun onResponse(
                call: Call<ApiResponse<User>>,
                response: Response<ApiResponse<User>>
            ) {
                showLoading(false)

                if (response.isSuccessful) {
                    val apiResponse = response.body()

                    if (apiResponse?.success == true && apiResponse.data != null) {
                        val user = apiResponse.data

                        // DEBUG: Tampilkan data user
                        Log.d("LoginActivity", "User data: $user")

                        // Check if user is ADMIN
                        if (user.level.uppercase() != "ADMIN") {
                            Toast.makeText(
                                this@LoginActivity,
                                "Hanya admin yang dapat mengakses aplikasi ini",
                                Toast.LENGTH_LONG
                            ).show()
                            return
                        }

                        saveUserData(user)

                        Toast.makeText(
                            this@LoginActivity,
                            "Login berhasil! Selamat datang ${user.nama}",
                            Toast.LENGTH_SHORT
                        ).show()

                        navigateToHome()
                    } else {
                        val errorMsg = apiResponse?.message ?: "Username atau password salah"
                        Toast.makeText(
                            this@LoginActivity,
                            errorMsg,
                            Toast.LENGTH_LONG
                        ).show()

                        // Clear password field
                        etPassword.text?.clear()
                    }
                } else {
                    handleErrorResponse(response.code())
                }
            }

            override fun onFailure(call: Call<ApiResponse<User>>, t: Throwable) {
                showLoading(false)

                Log.e("LoginActivity", "Login failed: ${t.message}", t)

                // Check if it's a network error
                val errorMessage = if (t.message?.contains("Unable to resolve host") == true) {
                    "Tidak dapat terhubung ke server. Periksa koneksi internet Anda"
                } else {
                    "Error: ${t.message}"
                }

                Toast.makeText(
                    this@LoginActivity,
                    errorMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun validateInput(): Boolean {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        var isValid = true

        // Clear previous errors
        etUsername.error = null
        etPassword.error = null

        if (username.isEmpty()) {
            etUsername.error = "Username tidak boleh kosong"
            etUsername.requestFocus()
            isValid = false
        }

        if (password.isEmpty()) {
            etPassword.error = "Password tidak boleh kosong"
            if (isValid) etPassword.requestFocus()
            isValid = false
        } else if (password.length < 6) {
            etPassword.error = "Password minimal 6 karakter"
            if (isValid) etPassword.requestFocus()
            isValid = false
        }

        return isValid
    }

    private fun handleErrorResponse(errorCode: Int) {
        val message = when (errorCode) {
            401 -> "Unauthorized access"
            404 -> "Server not found"
            500 -> "Internal server error"
            else -> "Error: $errorCode"
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showLoading(show: Boolean) {
        val loginButton = findViewById<android.widget.TextView>(R.id.buttonsignin)

        // Cara 1: Tanpa ProgressBar (hanya ubah text button)
        loginButton.isEnabled = !show
        loginButton.text = if (show) "Loading..." else "Sign In"

        // Cara 2: Jika ada ProgressBar di XML
        // val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        // if (progressBar != null) {
        //     if (show) {
        //         loginButton.visibility = View.GONE
        //         progressBar.visibility = View.VISIBLE
        //     } else {
        //         loginButton.visibility = View.VISIBLE
        //         progressBar.visibility = View.GONE
        //     }
        // } else {
        //     // Fallback jika tidak ada ProgressBar
        //     loginButton.isEnabled = !show
        //     loginButton.text = if (show) "Loading..." else "Sign In"
        // }
    }

    private fun saveUserData(user: User) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", true)
        editor.putInt("userId", user.id)
        editor.putString("userName", user.nama)
        editor.putString("username", user.username)
        editor.putString("userLevel", user.level)
        editor.putLong("loginTime", System.currentTimeMillis())

        // Karena tidak ada email di database, buat dari username
        editor.putString("userEmail", "${user.username}")

        editor.apply()

        // Debug log
        Log.d("LoginActivity", "Saved user data: ${user.nama}, Level: ${user.level}")
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Exit app when back pressed from login
        finishAffinity()
    }
}