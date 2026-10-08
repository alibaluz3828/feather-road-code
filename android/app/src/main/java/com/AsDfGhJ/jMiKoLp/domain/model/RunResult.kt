package com.AsDfGhJ.jMiKoLp.domain.model

sealed class RunResult {

    abstract val steps: List<RunStep>
    abstract val clearedSegments: Int

    data class Cleared(
        override val steps: List<RunStep>,
        override val clearedSegments: Int
    ) : RunResult()

    data class Crashed(
        override val steps: List<RunStep>,
        override val clearedSegments: Int,
        val reason: CrashReason
    ) : RunResult()
}
