package com.ruangtenang.data

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    fun saveSession(userId: Int, username: String, isGuest: Boolean) {
        prefs.edit()
            .putInt("userId", userId)
            .putString("username", username)
            .putBoolean("isGuest", isGuest)
            .apply()
    }

    fun getUserId(): Int = prefs.getInt("userId", -1)
    fun getUsername(): String? = prefs.getString("username", null)
    fun isGuest(): Boolean = prefs.getBoolean("isGuest", false)
    fun isLoggedIn(): Boolean = getUserId() != -1

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}