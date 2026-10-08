package com.AsDfGhJ.jMiKoLp.domain.usecase

import com.AsDfGhJ.jMiKoLp.domain.model.Cell
import com.AsDfGhJ.jMiKoLp.domain.model.CrashReason
import com.AsDfGhJ.jMiKoLp.domain.model.Direction
import com.AsDfGhJ.jMiKoLp.domain.model.RoutePlan
import com.AsDfGhJ.jMiKoLp.domain.model.RunResult
import com.AsDfGhJ.jMiKoLp.domain.model.RunStep

class SimulateRunUseCase {

    operator fun invoke(plan: RoutePlan, placements: Map<Cell, Direction>): RunResult {
        var position = plan.entry
        var heading = plan.entryHeading
        var segment = 0
        val visited = HashSet<Cell>()
        visited.add(position)
        val steps = ArrayList<RunStep>()
        steps.add(RunStep(position, heading, segment))

        var guard = 0
        while (guard < MAX_STEPS) {
            guard++
            if (plan.sockets.contains(position)) {
                val sign = placements[position]
                    ?: return RunResult.Crashed(steps, segment, CrashReason.NO_SIGN)
                heading = sign
                segment++
                steps.add(RunStep(position, heading, segment))
            }
            val next = position.step(heading)
            if (!next.isInside(plan.boardSize)) {
                return RunResult.Crashed(steps, segment, CrashReason.WALL)
            }
            if (plan.obstacles.contains(next)) {
                return RunResult.Crashed(steps, segment, CrashReason.OBSTACLE)
            }
            if (visited.contains(next)) {
                return RunResult.Crashed(steps, segment, CrashReason.CROSSED)
            }
            position = next
            visited.add(position)
            steps.add(RunStep(position, heading, segment))
            if (position == plan.exit) {
                val allCheckpoints = visited.containsAll(plan.checkpoints)
                val fullCode = segment == plan.codeLength - 1
                return if (allCheckpoints && fullCode) {
                    RunResult.Cleared(steps, plan.codeLength)
                } else {
                    RunResult.Crashed(steps, segment, CrashReason.MISSED)
                }
            }
        }
        return RunResult.Crashed(steps, segment, CrashReason.CROSSED)
    }

    private companion object {
        const val MAX_STEPS = 256
    }
}
