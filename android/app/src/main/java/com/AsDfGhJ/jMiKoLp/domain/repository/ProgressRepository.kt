package com.AsDfGhJ.jMiKoLp.domain.repository

import com.AsDfGhJ.jMiKoLp.domain.model.ProgressSnapshot
import com.AsDfGhJ.jMiKoLp.domain.model.RouteTier

interface ProgressRepository {
    fun snapshot(): ProgressSnapshot
    fun selectedTier(): RouteTier
    fun selectTier(tier: RouteTier)
    fun recordRun(tier: RouteTier, stars: Int, cleared: Boolean)
    fun isCoachSeen(): Boolean
    fun markCoachSeen()
    fun reset()
}
