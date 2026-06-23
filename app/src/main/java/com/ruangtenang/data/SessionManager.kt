package com.ruangtenang.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("ruangtenang_session", Context.MODE_PRIVATE)

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean("is_onboarding_completed", false)
        set(value) = prefs.edit().putBoolean("is_onboarding_completed", value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean("is_logged_in", false)
        set(value) = prefs.edit().putBoolean("is_logged_in", value).apply()

    var isGuestMode: Boolean
        get() = prefs.getBoolean("is_guest_mode", false)
        set(value) = prefs.edit().putBoolean("is_guest_mode", value).apply()

    var registeredName: String?
        get() = prefs.getString("registered_name", null)
        set(value) = prefs.edit().putString("registered_name", value).apply()

    var registeredEmail: String?
        get() = prefs.getString("registered_email", null)
        set(value) = prefs.edit().putString("registered_email", value).apply()

    var registeredPassword: String?
        get() = prefs.getString("registered_password", null)
        set(value) = prefs.edit().putString("registered_password", value).apply()
}
