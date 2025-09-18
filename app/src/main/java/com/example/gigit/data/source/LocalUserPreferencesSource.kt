package com.example.gigit.data.source

import android.content.Context
import android.content.SharedPreferences

class LocalUserPreferencesSource(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("GigItPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    fun hasCompletedOnboarding(): Boolean {
        // Returns false by default if the key doesn't exist
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }
}