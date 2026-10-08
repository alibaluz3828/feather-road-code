package com.AsDfGhJ.jMiKoLp.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AsDfGhJ.jMiKoLp.core.config.GameConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private val _handOffReady = MutableStateFlow(false)
    val handOffReady: StateFlow<Boolean> = _handOffReady.asStateFlow()

    private val _progressLabel = MutableStateFlow(STAGE_BOOT)
    val progressLabel: StateFlow<Int> = _progressLabel.asStateFlow()

    private var timerJob: Job? = null
    private var started = false

    fun start() {
        if (started) {
            return
        }
        started = true
        timerJob = viewModelScope.launch {
            val slice = GameConfig.LOADER_DURATION_MS / STAGES
            for (stage in 0 until STAGES) {
                _progressLabel.value = stage
                delay(slice)
            }
            val remainder = GameConfig.LOADER_DURATION_MS - slice * STAGES
            if (remainder > 0L) {
                delay(remainder)
            }
            _handOffReady.value = true
        }
    }

    fun consumeHandOff() {
        _handOffReady.value = false
        timerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        timerJob = null
    }

    private companion object {
        const val STAGES = 4
        const val STAGE_BOOT = 0
    }
}
