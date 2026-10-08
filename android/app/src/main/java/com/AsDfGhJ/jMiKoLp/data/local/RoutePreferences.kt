package com.AsDfGhJ.jMiKoLp.data.local

import android.content.Context
import android.content.SharedPreferences
import com.AsDfGhJ.jMiKoLp.domain.model.RouteTier

class RoutePreferences(context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(STORE, Context.MODE_PRIVATE)

    fun routesCleared(): Int = preferences.getInt(KEY_ROUTES, 0)

    fun totalStars(): Int = preferences.getInt(KEY_STARS, 0)

    fun bestCodeLength(): Int = preferences.getInt(KEY_BEST, 0)

    fun starsForTier(tier: RouteTier): Int = preferences.getInt(KEY_TIER_STARS + tier.id, 0)

    fun selectedTierId(): String? = preferences.getString(KEY_TIER, null)

    fun setSelectedTierId(id: String) {
        preferences.edit().putString(KEY_TIER, id).apply()
    }

    fun storeRun(tier: RouteTier, stars: Int, cleared: Boolean) {
        val editor = preferences.edit()
        if (cleared) {
            editor.putInt(KEY_ROUTES, routesCleared() + 1)
            if (tier.codeLength > bestCodeLength()) {
                editor.putInt(KEY_BEST, tier.codeLength)
            }
        }
        val previous = starsForTier(tier)
        if (stars > previous) {
            editor.putInt(KEY_TIER_STARS + tier.id, stars)
            editor.putInt(KEY_STARS, totalStars() + (stars - previous))
        }
        editor.apply()
    }

    fun coachSeen(): Boolean = preferences.getBoolean(KEY_COACH, false)

    fun setCoachSeen() {
        preferences.edit().putBoolean(KEY_COACH, true).apply()
    }

    fun haptics(): Boolean = preferences.getBoolean(KEY_HAPTICS, true)

    fun setHaptics(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_HAPTICS, enabled).apply()
    }

    fun highContrast(): Boolean = preferences.getBoolean(KEY_CONTRAST, false)

    fun setHighContrast(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_CONTRAST, enabled).apply()
    }

    fun reducedMotion(): Boolean = preferences.getBoolean(KEY_MOTION, false)

    fun setReducedMotion(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_MOTION, enabled).apply()
    }

    fun clearProgress() {
        val editor = preferences.edit()
        editor.remove(KEY_ROUTES)
        editor.remove(KEY_STARS)
        editor.remove(KEY_BEST)
        for (tier in RouteTier.entries) {
            editor.remove(KEY_TIER_STARS + tier.id)
        }
        editor.apply()
    }

    private companion object {
        const val STORE = "feather_road_code_store"
        const val KEY_ROUTES = "routes_cleared"
        const val KEY_STARS = "total_stars"
        const val KEY_BEST = "best_code_length"
        const val KEY_TIER_STARS = "tier_stars_"
        const val KEY_TIER = "selected_tier"
        const val KEY_COACH = "coach_seen"
        const val KEY_HAPTICS = "haptics_enabled"
        const val KEY_CONTRAST = "high_contrast"
        const val KEY_MOTION = "reduced_motion"
    }
}
