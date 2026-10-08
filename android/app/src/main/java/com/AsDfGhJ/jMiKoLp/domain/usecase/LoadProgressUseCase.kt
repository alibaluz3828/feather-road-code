package com.AsDfGhJ.jMiKoLp.domain.usecase

import com.AsDfGhJ.jMiKoLp.domain.model.ProgressSnapshot
import com.AsDfGhJ.jMiKoLp.domain.repository.ProgressRepository

class LoadProgressUseCase(private val repository: ProgressRepository) {

    operator fun invoke(): ProgressSnapshot = repository.snapshot()
}
