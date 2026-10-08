package com.AsDfGhJ.jMiKoLp.presentation.gameover

import com.AsDfGhJ.jMiKoLp.domain.model.ChipState
import com.AsDfGhJ.jMiKoLp.domain.model.Direction
import com.AsDfGhJ.jMiKoLp.domain.model.RouteTier

data class GameOverUiState(
    val tier: RouteTier,
    val nextTier: RouteTier,
    val routeNumber: Int,
    val nextRouteNumber: Int,
    val cleared: Boolean,
    val outOfActions: Boolean,
    val stars: Int,
    val actionsUsed: Int,
    val launches: Int,
    val code: List<Direction>,
    val chipStates: List<ChipState>
) {
    companion object {
        val EMPTY = GameOverUiState(
            tier = RouteTier.STANDARD,
            nextTier = RouteTier.STANDARD,
            routeNumber = 1,
            nextRouteNumber = 1,
            cleared = false,
            outOfActions = false,
            stars = 0,
            actionsUsed = 0,
            launches = 0,
            code = emptyList(),
            chipStates = emptyList()
        )
    }
}
