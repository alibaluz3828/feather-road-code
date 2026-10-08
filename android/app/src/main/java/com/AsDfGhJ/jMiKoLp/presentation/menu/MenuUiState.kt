package com.AsDfGhJ.jMiKoLp.presentation.menu

import com.AsDfGhJ.jMiKoLp.domain.model.RouteTier

data class MenuUiState(
    val routesCleared: Int,
    val totalStars: Int,
    val bestCodeLength: Int,
    val selectedTier: RouteTier,
    val nextRouteNumber: Int
) {
    companion object {
        val EMPTY = MenuUiState(
            routesCleared = 0,
            totalStars = 0,
            bestCodeLength = 0,
            selectedTier = RouteTier.STANDARD,
            nextRouteNumber = 1
        )
    }
}
