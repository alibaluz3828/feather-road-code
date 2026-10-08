package com.AsDfGhJ.jMiKoLp.presentation.game

import com.AsDfGhJ.jMiKoLp.domain.model.Cell
import com.AsDfGhJ.jMiKoLp.domain.model.ChipState
import com.AsDfGhJ.jMiKoLp.domain.model.Direction
import com.AsDfGhJ.jMiKoLp.domain.model.RoutePlan

data class GameUiState(
    val plan: RoutePlan,
    val placements: Map<Cell, Direction>,
    val dock: List<DockSlot>,
    val selected: Direction?,
    val selectedSocket: Cell?,
    val actionsLeft: Int,
    val launches: Int,
    val phase: GamePhase,
    val chipStates: List<ChipState>,
    val cartCell: Cell?,
    val cartHeading: Direction,
    val trail: List<Cell>,
    val passedCheckpoints: Set<Cell>,
    val coachVisible: Boolean,
    val highContrast: Boolean,
    val reducedMotion: Boolean,
    val crashPulse: Int,
    val clearPulse: Int
) {
    val signsPlaced: Int get() = placements.size
    val socketCount: Int get() = plan.sockets.size
}
