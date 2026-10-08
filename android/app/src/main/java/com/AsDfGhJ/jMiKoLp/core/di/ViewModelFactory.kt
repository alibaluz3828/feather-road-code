package com.AsDfGhJ.jMiKoLp.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.AsDfGhJ.jMiKoLp.presentation.game.GameViewModel
import com.AsDfGhJ.jMiKoLp.presentation.gameover.GameOverViewModel
import com.AsDfGhJ.jMiKoLp.presentation.menu.MenuViewModel
import com.AsDfGhJ.jMiKoLp.presentation.splash.SplashViewModel

class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val created: ViewModel = when {
            modelClass.isAssignableFrom(SplashViewModel::class.java) -> SplashViewModel()
            modelClass.isAssignableFrom(MenuViewModel::class.java) -> MenuViewModel(
                container.progressRepository,
                container.loadProgress
            )
            modelClass.isAssignableFrom(GameViewModel::class.java) -> GameViewModel(
                container.routeRepository,
                container.progressRepository,
                container.settingsRepository,
                container.simulateRun,
                container.scoreRun,
                container.buildChipStates,
                container.saveProgress
            )
            modelClass.isAssignableFrom(GameOverViewModel::class.java) -> GameOverViewModel(
                container.progressRepository,
                container.buildChipStates
            )
            else -> SplashViewModel()
        }
        return created as T
    }
}
