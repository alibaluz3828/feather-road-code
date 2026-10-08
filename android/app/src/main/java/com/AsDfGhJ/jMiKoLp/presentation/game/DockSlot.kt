package com.AsDfGhJ.jMiKoLp.presentation.game

import com.AsDfGhJ.jMiKoLp.domain.model.Direction

data class DockSlot(
    val direction: Direction,
    val remaining: Int,
    val selected: Boolean
)
