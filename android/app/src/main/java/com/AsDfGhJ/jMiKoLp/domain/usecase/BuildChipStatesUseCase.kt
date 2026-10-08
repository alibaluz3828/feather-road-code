package com.AsDfGhJ.jMiKoLp.domain.usecase

import com.AsDfGhJ.jMiKoLp.domain.model.ChipState

class BuildChipStatesUseCase {

    operator fun invoke(codeLength: Int, cleared: Int, failedAt: Int): List<ChipState> {
        val states = ArrayList<ChipState>(codeLength)
        for (index in 0 until codeLength) {
            val state = when {
                index < cleared -> ChipState.CLEARED
                index == failedAt && failedAt >= 0 -> ChipState.FAILED
                index == 0 -> ChipState.FIXED
                else -> ChipState.PENDING
            }
            states.add(state)
        }
        return states
    }
}
