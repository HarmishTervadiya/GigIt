package com.example.gigit.data.repository

import com.example.gigit.data.source.LocalUserPreferencesSource

class UserPreferencesRepository(private val source: LocalUserPreferencesSource) {

    fun hasCompletedOnboarding(): Boolean {
        return source.hasCompletedOnboarding()
    }

    fun setOnboardingCompleted() {
        source.setOnboardingCompleted(true)
    }
}