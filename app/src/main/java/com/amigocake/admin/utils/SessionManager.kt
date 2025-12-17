// utils/SessionManager.kt
package com.amigocake.admin.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.amigocake.admin.LoginActivity

object SessionManager {

    private const val PREF_NAME = "AmigoCakePrefs"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private const val KEY_USER_ID = "userId"
    private const val KEY_USER_NAME = "userName"
    private const val KEY_USERNAME = "username"
    private const val KEY_USER_LEVEL = "userLevel"

    fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveLogin(context: Context, userId: Int, userName: String, username: String, level: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putInt(KEY_USER_ID, userId)
        editor.putString(KEY_USER_NAME, userName)
        editor.putString(KEY_USERNAME, username)
        editor.putString(KEY_USER_LEVEL, level)
        editor.putLong("loginTime", System.currentTimeMillis())
        editor.apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        val prefs = getSharedPreferences(context)
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) &&
                prefs.getInt(KEY_USER_ID, 0) > 0 &&
                prefs.getString(KEY_USER_LEVEL, "") == "ADMIN"
    }

    fun getUserId(context: Context): Int {
        return getSharedPreferences(context).getInt(KEY_USER_ID, 0)
    }

    fun getUserName(context: Context): String {
        return getSharedPreferences(context).getString(KEY_USER_NAME, "Admin") ?: "Admin"
    }

    fun getUsername(context: Context): String {
        return getSharedPreferences(context).getString(KEY_USERNAME, "admin") ?: "admin"
    }

    fun getUserLevel(context: Context): String {
        return getSharedPreferences(context).getString(KEY_USER_LEVEL, "ADMIN") ?: "ADMIN"
    }

    fun logout(context: Context) {
        // Clear all preferences
        getSharedPreferences(context).edit().clear().apply()

        // Redirect to login
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}