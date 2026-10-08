package com.AsDfGhJ.jMiKoLp.domain.usecase

import com.AsDfGhJ.jMiKoLp.domain.model.RouteTier
import com.AsDfGhJ.jMiKoLp.domain.repository.ProgressRepository

class SaveProgressUseCase(private val repository: ProgressRepository) {

    operator fun invoke(tier: RouteTier, stars: Int, cleared: Boolean) {
        repository.recordRun(tier, stars, cleared)
    }
}
