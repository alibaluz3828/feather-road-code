package com.AsDfGhJ.jMiKoLp.data.repository

import com.AsDfGhJ.jMiKoLp.data.sample.SampleData
import com.AsDfGhJ.jMiKoLp.domain.model.RoutePlan
import com.AsDfGhJ.jMiKoLp.domain.model.RouteTier
import com.AsDfGhJ.jMiKoLp.domain.repository.RouteRepository
import com.AsDfGhJ.jMiKoLp.domain.usecase.GenerateRouteUseCase

class RouteRepositoryImpl(
    private val generateRoute: GenerateRouteUseCase
) : RouteRepository {

    override fun nextRoute(tier: RouteTier, routeNumber: Int, seed: Long): RoutePlan {
        val name = SampleData.routeName(routeNumber)
        val generated = generateRoute(tier, routeNumber, name, seed)
        return generated ?: SampleData.fallbackRoute(tier, routeNumber)
    }

    override fun tutorialRoute(): RoutePlan = SampleData.tutorialRoute()
}
