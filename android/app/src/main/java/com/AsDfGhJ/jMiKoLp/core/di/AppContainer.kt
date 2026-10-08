package com.AsDfGhJ.jMiKoLp.core.di

import android.content.Context
import com.AsDfGhJ.jMiKoLp.data.local.RoutePreferences
import com.AsDfGhJ.jMiKoLp.data.repository.ProgressRepositoryImpl
import com.AsDfGhJ.jMiKoLp.data.repository.RouteRepositoryImpl
import com.AsDfGhJ.jMiKoLp.data.repository.SettingsRepositoryImpl
import com.AsDfGhJ.jMiKoLp.domain.repository.ProgressRepository
import com.AsDfGhJ.jMiKoLp.domain.repository.RouteRepository
import com.AsDfGhJ.jMiKoLp.domain.repository.SettingsRepository
import com.AsDfGhJ.jMiKoLp.domain.usecase.BuildChipStatesUseCase
import com.AsDfGhJ.jMiKoLp.domain.usecase.GenerateRouteUseCase
import com.AsDfGhJ.jMiKoLp.domain.usecase.LoadProgressUseCase
import com.AsDfGhJ.jMiKoLp.domain.usecase.SaveProgressUseCase
import com.AsDfGhJ.jMiKoLp.domain.usecase.ScoreRunUseCase
import com.AsDfGhJ.jMiKoLp.domain.usecase.SimulateRunUseCase

class AppContainer(context: Context) {

    private val preferences = RoutePreferences(context.applicationContext)

    val progressRepository: ProgressRepository = ProgressRepositoryImpl(preferences)

    val settingsRepository: SettingsRepository = SettingsRepositoryImpl(preferences)

    val routeRepository: RouteRepository = RouteRepositoryImpl(GenerateRouteUseCase())

    val simulateRun = SimulateRunUseCase()

    val scoreRun = ScoreRunUseCase()

    val buildChipStates = BuildChipStatesUseCase()

    val saveProgress = SaveProgressUseCase(progressRepository)

    val loadProgress = LoadProgressUseCase(progressRepository)
}
