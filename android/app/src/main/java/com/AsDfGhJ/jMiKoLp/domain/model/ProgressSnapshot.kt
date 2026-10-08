package com.AsDfGhJ.jMiKoLp.domain.model

data class ProgressSnapshot(
    val routesCleared: Int,
    val totalStars: Int,
    val bestCodeLength: Int,
    val starsByTier: Map<String, Int>
) {
    fun starsFor(tier: RouteTier): Int = starsByTier[tier.id] ?: 0

    companion object {
        val EMPTY = ProgressSnapshot(0, 0, 0, emptyMap())
    }
}
