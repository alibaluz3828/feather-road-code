package com.AsDfGhJ.jMiKoLp.domain.model

data class GameSettings(
    val haptics: Boolean,
    val highContrast: Boolean,
    val reducedMotion: Boolean
) {
    companion object {
        val DEFAULT = GameSettings(haptics = true, highContrast = false, reducedMotion = false)
    }
}
