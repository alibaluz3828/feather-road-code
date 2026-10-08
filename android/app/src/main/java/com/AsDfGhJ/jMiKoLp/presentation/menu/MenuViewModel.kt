package com.AsDfGhJ.jMiKoLp.presentation.menu

import androidx.lifecycle.ViewModel
import com.AsDfGhJ.jMiKoLp.domain.model.RouteTier
import com.AsDfGhJ.jMiKoLp.domain.repository.ProgressRepository
import com.AsDfGhJ.jMiKoLp.domain.usecase.LoadProgressUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MenuViewModel(
    private val progressRepository: ProgressRepository,
    private val loadProgress: LoadProgressUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MenuUiState.EMPTY)
    val state: StateFlow<MenuUiState> = _state.asStateFlow()

    fun refresh() {
        val snapshot = loadProgress()
        _state.value = MenuUiState(
            routesCleared = snapshot.routesCleared,
            totalStars = snapshot.totalStars,
            bestCodeLength = snapshot.bestCodeLength,
            selectedTier = progressRepository.selectedTier(),
            nextRouteNumber = snapshot.routesCleared + 1
        )
    }

    fun selectTier(tier: RouteTier) {
        progressRepository.selectTier(tier)
        refresh()
    }
}
