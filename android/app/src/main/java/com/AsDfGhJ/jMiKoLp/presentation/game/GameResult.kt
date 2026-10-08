package com.AsDfGhJ.jMiKoLp.presentation.game

import com.AsDfGhJ.jMiKoLp.domain.model.RouteTier

data class GameResult(
    val tier: RouteTier,
    val routeNumber: Int,
    val cleared: Boolean,
    val stars: Int,
    val actionsUsed: Int,
    val launches: Int,
    val clearedSegments: Int,
    val failedAt: Int,
    val codeOrdinals: IntArray,
    val outOfActions: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is GameResult) {
            return false
        }
        return tier == other.tier &&
            routeNumber == other.routeNumber &&
            cleared == other.cleared &&
            stars == other.stars &&
            actionsUsed == other.actionsUsed &&
            launches == other.launches &&
            clearedSegments == other.clearedSegments &&
            failedAt == other.failedAt &&
            outOfActions == other.outOfActions &&
            codeOrdinals.contentEquals(other.codeOrdinals)
    }

    override fun hashCode(): Int {
        var result = tier.hashCode()
        result = 31 * result + routeNumber
        result = 31 * result + cleared.hashCode()
        result = 31 * result + stars
        result = 31 * result + actionsUsed
        result = 31 * result + launches
        result = 31 * result + clearedSegments
        result = 31 * result + failedAt
        result = 31 * result + outOfActions.hashCode()
        result = 31 * result + codeOrdinals.contentHashCode()
        return result
    }
}
