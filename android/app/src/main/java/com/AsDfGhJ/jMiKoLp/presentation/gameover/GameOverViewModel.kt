package com.AsDfGhJ.jMiKoLp.presentation.gameover

import androidx.lifecycle.ViewModel
import com.AsDfGhJ.jMiKoLp.domain.model.Direction
import com.AsDfGhJ.jMiKoLp.domain.model.RouteTier
import com.AsDfGhJ.jMiKoLp.domain.repository.ProgressRepository
import com.AsDfGhJ.jMiKoLp.domain.usecase.BuildChipStatesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameOverViewModel(
    private val progressRepository: ProgressRepository,
    private val buildChipStates: BuildChipStatesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GameOverUiState.EMPTY)
    val state: StateFlow<GameOverUiState> = _state.asStateFlow()

    private var loaded = false

    fun load(
        tier: RouteTier,
        routeNumber: Int,
        cleared: Boolean,
        stars: Int,
        actionsUsed: Int,
        launches: Int,
        clearedSegments: Int,
        failedAt: Int,
        codeOrdinals: IntArray,
        outOfActions: Boolean
    ) {
        if (loaded) {
            return
        }
        loaded = true
        val directions = Direction.entries
        val code = codeOrdinals.map { directions[it % directions.size] }
        val snapshot = progressRepository.snapshot()
        _state.value = GameOverUiState(
            tier = tier,
            nextTier = if (cleared) tier.next() else tier,
            routeNumber = routeNumber,
            nextRouteNumber = snapshot.routesCleared + 1,
            cleared = cleared,
            outOfActions = outOfActions,
            stars = stars,
            actionsUsed = actionsUsed,
            launches = launches,
            code = code,
            chipStates = buildChipStates(code.size, clearedSegments, failedAt)
        )
    }
}
