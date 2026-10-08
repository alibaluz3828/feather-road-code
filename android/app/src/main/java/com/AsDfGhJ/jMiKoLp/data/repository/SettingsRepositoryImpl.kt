package com.AsDfGhJ.jMiKoLp.data.repository

import com.AsDfGhJ.jMiKoLp.data.local.RoutePreferences
import com.AsDfGhJ.jMiKoLp.domain.model.GameSettings
import com.AsDfGhJ.jMiKoLp.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val preferences: RoutePreferences
) : SettingsRepository {

    override fun settings(): GameSettings = GameSettings(
        haptics = preferences.haptics(),
        highContrast = preferences.highContrast(),
        reducedMotion = preferences.reducedMotion()
    )

    override fun setHaptics(enabled: Boolean) {
        preferences.setHaptics(enabled)
    }

    override fun setHighContrast(enabled: Boolean) {
        preferences.setHighContrast(enabled)
    }

    override fun setReducedMotion(enabled: Boolean) {
        preferences.setReducedMotion(enabled)
    }
}
