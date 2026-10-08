package com.AsDfGhJ.jMiKoLp.data.repository

import com.AsDfGhJ.jMiKoLp.data.local.RoutePreferences
import com.AsDfGhJ.jMiKoLp.domain.model.ProgressSnapshot
import com.AsDfGhJ.jMiKoLp.domain.model.RouteTier
import com.AsDfGhJ.jMiKoLp.domain.repository.ProgressRepository

class ProgressRepositoryImpl(
    private val preferences: RoutePreferences
) : ProgressRepository {

    override fun snapshot(): ProgressSnapshot {
        val byTier = LinkedHashMap<String, Int>()
        for (tier in RouteTier.entries) {
            byTier[tier.id] = preferences.starsForTier(tier)
        }
        return ProgressSnapshot(
            routesCleared = preferences.routesCleared(),
            totalStars = preferences.totalStars(),
            bestCodeLength = preferences.bestCodeLength(),
            starsByTier = byTier
        )
    }

    override fun selectedTier(): RouteTier = RouteTier.fromId(preferences.selectedTierId())

    override fun selectTier(tier: RouteTier) {
        preferences.setSelectedTierId(tier.id)
    }

    override fun recordRun(tier: RouteTier, stars: Int, cleared: Boolean) {
        preferences.storeRun(tier, stars, cleared)
    }

    override fun isCoachSeen(): Boolean = preferences.coachSeen()

    override fun markCoachSeen() {
        preferences.setCoachSeen()
    }

    override fun reset() {
        preferences.clearProgress()
    }
}
