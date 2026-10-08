package com.AsDfGhJ.jMiKoLp.domain.repository

import com.AsDfGhJ.jMiKoLp.domain.model.RoutePlan
import com.AsDfGhJ.jMiKoLp.domain.model.RouteTier

interface RouteRepository {
    fun nextRoute(tier: RouteTier, routeNumber: Int, seed: Long): RoutePlan
    fun tutorialRoute(): RoutePlan
}
