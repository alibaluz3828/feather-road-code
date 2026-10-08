package com.AsDfGhJ.jMiKoLp.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AsDfGhJ.jMiKoLp.core.config.GameConfig
import com.AsDfGhJ.jMiKoLp.domain.model.Cell
import com.AsDfGhJ.jMiKoLp.domain.model.Direction
import com.AsDfGhJ.jMiKoLp.domain.model.RoutePlan
import com.AsDfGhJ.jMiKoLp.domain.model.RouteTier
import com.AsDfGhJ.jMiKoLp.domain.model.RunResult
import com.AsDfGhJ.jMiKoLp.domain.repository.ProgressRepository
import com.AsDfGhJ.jMiKoLp.domain.repository.RouteRepository
import com.AsDfGhJ.jMiKoLp.domain.repository.SettingsRepository
import com.AsDfGhJ.jMiKoLp.domain.usecase.BuildChipStatesUseCase
import com.AsDfGhJ.jMiKoLp.domain.usecase.SaveProgressUseCase
import com.AsDfGhJ.jMiKoLp.domain.usecase.ScoreRunUseCase
import com.AsDfGhJ.jMiKoLp.domain.usecase.SimulateRunUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val routeRepository: RouteRepository,
    private val progressRepository: ProgressRepository,
    private val settingsRepository: SettingsRepository,
    private val simulateRun: SimulateRunUseCase,
    private val scoreRun: ScoreRunUseCase,
    private val buildChipStates: BuildChipStatesUseCase,
    private val saveProgress: SaveProgressUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<GameUiState?>(null)
    val state: StateFlow<GameUiState?> = _state.asStateFlow()

    private val _result = MutableStateFlow<GameResult?>(null)
    val result: StateFlow<GameResult?> = _result.asStateFlow()

    private var tier: RouteTier = RouteTier.STANDARD
    private var routeNumber: Int = 1
    private var mountedAt: Long = 0L
    private var started = false
    private var resolved = false
    private var realInputSeen = false

    private var idleJob: Job? = null
    private var engagedJob: Job? = null
    private var assistJob: Job? = null
    private var runJob: Job? = null
    private var coachJob: Job? = null

    fun start(tier: RouteTier, routeNumber: Int) {
        if (started) {
            return
        }
        started = true
        this.tier = tier
        this.routeNumber = routeNumber
        mountedAt = System.currentTimeMillis()
        val settings = settingsRepository.settings()
        val plan = routeRepository.nextRoute(tier, routeNumber, mountedAt)
        val coach = !progressRepository.isCoachSeen()
        _state.value = GameUiState(
            plan = plan,
            placements = emptyMap(),
            dock = buildDock(plan, emptyMap(), null),
            selected = null,
            selectedSocket = null,
            actionsLeft = tier.actionBudget,
            launches = 0,
            phase = GamePhase.IDLE,
            chipStates = buildChipStates(plan.codeLength, 0, NO_FAILURE),
            cartCell = plan.entry,
            cartHeading = plan.entryHeading,
            trail = emptyList(),
            passedCheckpoints = emptySet(),
            coachVisible = coach,
            highContrast = settings.highContrast,
            reducedMotion = settings.reducedMotion,
            crashPulse = 0,
            clearPulse = 0
        )
        if (coach) {
            progressRepository.markCoachSeen()
            coachJob = viewModelScope.launch {
                delay(GameConfig.COACH_TIMEOUT_MS)
                hideCoach()
            }
        }
        armIdleBackstop()
        armAutoAssist()
    }

    fun onRealInput() {
        if (!realInputSeen) {
            realInputSeen = true
            assistJob?.cancel()
            assistJob = null
        }
        armEngagedBackstop()
    }

    fun selectDirection(direction: Direction) {
        val current = _state.value ?: return
        if (current.phase == GamePhase.RUNNING) {
            return
        }
        val next = if (current.selected == direction) null else direction
        _state.value = current.copy(
            selected = next,
            dock = buildDock(current.plan, current.placements, next)
        )
    }

    fun tapSocket(cell: Cell) {
        val current = _state.value ?: return
        if (current.phase == GamePhase.RUNNING) {
            return
        }
        val placed = current.placements[cell]
        if (placed != null) {
            val updated = current.placements.toMutableMap()
            updated.remove(cell)
            _state.value = current.copy(
                placements = updated,
                dock = buildDock(current.plan, updated, current.selected),
                selectedSocket = cell,
                phase = GamePhase.ARRANGING
            )
            return
        }
        val chosen = current.selected ?: return
        if (remainingFor(current.plan, current.placements, chosen) <= 0) {
            return
        }
        if (current.actionsLeft <= 0) {
            return
        }
        applyPlacement(current, cell, chosen)
    }

    private fun applyPlacement(current: GameUiState, cell: Cell, direction: Direction) {
        val updated = current.placements.toMutableMap()
        updated[cell] = direction
        _state.value = current.copy(
            placements = updated,
            dock = buildDock(current.plan, updated, null),
            selected = null,
            selectedSocket = cell,
            actionsLeft = current.actionsLeft - 1,
            phase = GamePhase.ARRANGING,
            coachVisible = false
        )
        coachJob?.cancel()
        coachJob = null
    }

    fun launch() {
        val current = _state.value ?: return
        if (current.phase == GamePhase.RUNNING || resolved) {
            return
        }
        runJob?.cancel()
        runJob = viewModelScope.launch {
            playRun(current.plan)
        }
    }

    private suspend fun playRun(plan: RoutePlan) {
        val base = _state.value ?: return
        val outcome = simulateRun(plan, base.placements)
        val stepDelay = if (base.reducedMotion) {
            GameConfig.CART_STEP_FAST_MS
        } else {
            GameConfig.CART_STEP_MS
        }
        _state.value = base.copy(
            phase = GamePhase.RUNNING,
            trail = listOf(plan.entry),
            cartCell = plan.entry,
            cartHeading = plan.entryHeading,
            passedCheckpoints = emptySet()
        )
        val visited = LinkedHashSet<Cell>()
        visited.add(plan.entry)
        val passed = LinkedHashSet<Cell>()
        for (step in outcome.steps) {
            val snapshot = _state.value ?: return
            visited.add(step.cell)
            if (plan.checkpoints.contains(step.cell)) {
                passed.add(step.cell)
            }
            _state.value = snapshot.copy(
                cartCell = step.cell,
                cartHeading = step.heading,
                trail = visited.toList(),
                passedCheckpoints = passed.toSet(),
                chipStates = buildChipStates(plan.codeLength, step.segment, NO_FAILURE)
            )
            delay(stepDelay)
        }
        when (outcome) {
            is RunResult.Cleared -> finishCleared(plan, outcome.clearedSegments)
            is RunResult.Crashed -> finishCrashed(plan, outcome.clearedSegments)
        }
    }

    private suspend fun finishCleared(plan: RoutePlan, clearedSegments: Int) {
        val current = _state.value ?: return
        engagedJob?.cancel()
        engagedJob = null
        idleJob?.cancel()
        idleJob = null
        _state.value = current.copy(
            phase = GamePhase.CLEARED,
            chipStates = buildChipStates(plan.codeLength, plan.codeLength, NO_FAILURE),
            clearPulse = current.clearPulse + 1
        )
        delay(GameConfig.CLEAR_HOLD_MS)
        val stars = scoreRun(true, current.launches, current.actionsLeft)
        saveProgress(plan.tier, stars, true)
        resolve(plan, cleared = true, stars = stars, clearedSegments = clearedSegments, failedAt = NO_FAILURE, outOfActions = false)
    }

    private suspend fun finishCrashed(plan: RoutePlan, clearedSegments: Int) {
        val current = _state.value ?: return
        val launches = current.launches + 1
        _state.value = current.copy(
            phase = GamePhase.CRASHED,
            launches = launches,
            chipStates = buildChipStates(plan.codeLength, clearedSegments, clearedSegments),
            crashPulse = current.crashPulse + 1
        )
        delay(GameConfig.CRASH_HOLD_MS)
        val after = _state.value ?: return
        val outOfActions = after.actionsLeft <= 0
        if (launches >= GameConfig.MAX_LAUNCHES || outOfActions) {
            _state.value = after.copy(phase = GamePhase.EXHAUSTED)
            delay(GameConfig.EXHAUSTED_HOLD_MS)
            saveProgress(plan.tier, 0, false)
            resolve(
                plan,
                cleared = false,
                stars = 0,
                clearedSegments = clearedSegments,
                failedAt = clearedSegments,
                outOfActions = outOfActions
            )
            return
        }
        _state.value = after.copy(
            phase = GamePhase.ARRANGING,
            cartCell = plan.entry,
            cartHeading = plan.entryHeading,
            trail = emptyList(),
            passedCheckpoints = emptySet()
        )
    }

    private fun armIdleBackstop() {
        idleJob?.cancel()
        idleJob = viewModelScope.launch {
            delay(GameConfig.IDLE_BACKSTOP_MS)
            forceResolution()
        }
    }

    private fun armEngagedBackstop() {
        if (resolved) {
            return
        }
        engagedJob?.cancel()
        engagedJob = viewModelScope.launch {
            delay(GameConfig.ENGAGED_BACKSTOP_MS)
            forceResolution()
        }
    }

    private fun armAutoAssist() {
        assistJob?.cancel()
        assistJob = viewModelScope.launch {
            delay(GameConfig.AUTO_ASSIST_DELAY_MS)
            val plan = _state.value?.plan ?: return@launch
            val sockets = plan.sockets
            for (index in sockets.indices) {
                if (realInputSeen || resolved) {
                    return@launch
                }
                val socket = sockets[index]
                val direction = plan.solution[socket]
                val current = _state.value ?: return@launch
                if (direction != null && !current.placements.containsKey(socket)) {
                    applyPlacement(current, socket, direction)
                }
                if (index < sockets.size - 1) {
                    delay(GameConfig.AUTO_ASSIST_STEP_MS)
                }
            }
            if (realInputSeen || resolved) {
                return@launch
            }
            delay(GameConfig.AUTO_ASSIST_LAUNCH_MS)
            if (realInputSeen || resolved) {
                return@launch
            }
            val plannedRun = _state.value?.plan ?: return@launch
            playRun(plannedRun)
        }
    }

    private suspend fun forceResolution() {
        var guard = 0
        while (guard < RESOLUTION_RETRIES) {
            guard++
            if (resolved) {
                return
            }
            if (_state.value?.phase != GamePhase.RUNNING) {
                break
            }
            delay(RESOLUTION_RETRY_MS)
        }
        if (resolved) {
            return
        }
        val current = _state.value ?: return
        val plan = current.plan
        saveProgress(plan.tier, 0, false)
        resolve(
            plan,
            cleared = false,
            stars = 0,
            clearedSegments = 0,
            failedAt = NO_FAILURE,
            outOfActions = true
        )
    }

    private suspend fun resolve(
        plan: RoutePlan,
        cleared: Boolean,
        stars: Int,
        clearedSegments: Int,
        failedAt: Int,
        outOfActions: Boolean
    ) {
        if (resolved) {
            return
        }
        resolved = true
        idleJob?.cancel()
        engagedJob?.cancel()
        assistJob?.cancel()
        val elapsed = System.currentTimeMillis() - mountedAt
        val remaining = GameConfig.RESULT_FLOOR_MS - elapsed
        if (remaining > 0L) {
            delay(remaining)
        }
        val current = _state.value
        val actionsUsed = plan.tier.actionBudget - (current?.actionsLeft ?: 0)
        _result.value = GameResult(
            tier = plan.tier,
            routeNumber = plan.routeNumber,
            cleared = cleared,
            stars = stars,
            actionsUsed = actionsUsed,
            launches = current?.launches ?: 0,
            clearedSegments = clearedSegments,
            failedAt = failedAt,
            codeOrdinals = plan.code.map { it.ordinal }.toIntArray(),
            outOfActions = outOfActions
        )
    }

    fun consumeResult() {
        _result.value = null
    }

    private fun hideCoach() {
        val current = _state.value ?: return
        _state.value = current.copy(coachVisible = false)
    }

    private fun buildDock(
        plan: RoutePlan,
        placements: Map<Cell, Direction>,
        selected: Direction?
    ): List<DockSlot> {
        val demand = plan.dockDemand()
        val slots = ArrayList<DockSlot>(demand.size)
        for (entry in demand) {
            val used = placements.values.count { it == entry.key }
            slots.add(
                DockSlot(
                    direction = entry.key,
                    remaining = entry.value - used,
                    selected = selected == entry.key
                )
            )
        }
        return slots
    }

    private fun remainingFor(
        plan: RoutePlan,
        placements: Map<Cell, Direction>,
        direction: Direction
    ): Int {
        val total = plan.dockDemand()[direction] ?: 0
        val used = placements.values.count { it == direction }
        return total - used
    }

    override fun onCleared() {
        super.onCleared()
        idleJob?.cancel()
        engagedJob?.cancel()
        assistJob?.cancel()
        runJob?.cancel()
        coachJob?.cancel()
    }

    private companion object {
        const val NO_FAILURE = -1
        const val RESOLUTION_RETRIES = 14
        const val RESOLUTION_RETRY_MS = 1000L
    }
}
