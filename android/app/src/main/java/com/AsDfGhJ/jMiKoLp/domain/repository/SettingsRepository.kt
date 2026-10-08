package com.AsDfGhJ.jMiKoLp.domain.repository

import com.AsDfGhJ.jMiKoLp.domain.model.GameSettings

interface SettingsRepository {
    fun settings(): GameSettings
    fun setHaptics(enabled: Boolean)
    fun setHighContrast(enabled: Boolean)
    fun setReducedMotion(enabled: Boolean)
}
