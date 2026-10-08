package com.AsDfGhJ.jMiKoLp.domain.usecase

import com.AsDfGhJ.jMiKoLp.domain.model.Cell
import com.AsDfGhJ.jMiKoLp.domain.model.Direction
import com.AsDfGhJ.jMiKoLp.domain.model.RoutePlan
import com.AsDfGhJ.jMiKoLp.domain.model.RouteTier
import kotlin.random.Random

class GenerateRouteUseCase {

    operator fun invoke(
        tier: RouteTier,
        routeNumber: Int,
        routeName: String,
        seed: Long
    ): RoutePlan? {
        val random = Random(seed)
        repeat(ATTEMPTS) {
            val built = Builder(tier.boardSize, tier.codeLength, random).build()
            if (built != null) {
                return assemble(tier, built, routeNumber, routeName, random)
            }
        }
        return null
    }

    private fun assemble(
        tier: RouteTier,
        built: BuiltRoute,
        routeNumber: Int,
        routeName: String,
        random: Random
    ): RoutePlan {
        val size = tier.boardSize
        val entry = Cell(0, size - 1)
        val segments = built.segments
        val directions = built.directions
        val sockets = ArrayList<Cell>()
        for (index in 0 until segments.size - 1) {
            sockets.add(segments[index].last())
        }
        val exit = segments.last().last()
        val solution = LinkedHashMap<Cell, Direction>()
        for (index in sockets.indices) {
            solution[sockets[index]] = directions[index + 1]
        }
        val path = ArrayList<Cell>()
        path.add(entry)
        for (segment in segments) {
            path.addAll(segment)
        }
        val reserved = HashSet<Cell>()
        reserved.add(entry)
        reserved.add(exit)
        reserved.addAll(sockets)

        val checkpoints = pickCheckpoints(segments, reserved)
        val occupied = HashSet<Cell>(path)
        val obstacles = pickObstacles(size, occupied, exit, random)

        return RoutePlan(
            tier = tier,
            boardSize = size,
            entry = entry,
            entryHeading = directions.first(),
            exit = exit,
            code = directions,
            sockets = sockets,
            solution = solution,
            checkpoints = checkpoints,
            obstacles = obstacles,
            path = path,
            routeName = routeName,
            routeNumber = routeNumber
        )
    }

    private fun pickCheckpoints(
        segments: List<List<Cell>>,
        reserved: Set<Cell>
    ): List<Cell> {
        val ordered = segments.sortedByDescending { it.size }
        val chosen = ArrayList<Cell>()
        for (segment in ordered) {
            if (chosen.size >= CHECKPOINTS) {
                break
            }
            val candidate = segment[segment.size / 2]
            if (!reserved.contains(candidate) && !chosen.contains(candidate)) {
                chosen.add(candidate)
                continue
            }
            val alternative = segment.firstOrNull { !reserved.contains(it) && !chosen.contains(it) }
            if (alternative != null) {
                chosen.add(alternative)
            }
        }
        return chosen
    }

    private fun pickObstacles(
        size: Int,
        occupied: Set<Cell>,
        exit: Cell,
        random: Random
    ): List<Cell> {
        val free = ArrayList<Cell>()
        for (col in 0 until size) {
            for (row in 0 until size) {
                val cell = Cell(col, row)
                if (occupied.contains(cell)) {
                    continue
                }
                if (cell.distanceTo(exit) <= 1) {
                    continue
                }
                free.add(cell)
            }
        }
        if (free.isEmpty()) {
            return emptyList()
        }
        return free.shuffled(random).take(OBSTACLES)
    }

    private class BuiltRoute(
        val directions: List<Direction>,
        val segments: List<List<Cell>>
    )

    private class Builder(
        private val size: Int,
        private val length: Int,
        private val random: Random
    ) {
        private val directions = ArrayList<Direction>()
        private val segments = ArrayList<List<Cell>>()
        private val visited = HashSet<Cell>()

        fun build(): BuiltRoute? {
            directions.clear()
            segments.clear()
            visited.clear()
            val entry = Cell(0, size - 1)
            visited.add(entry)
            return if (extend(0, entry, Direction.UP)) {
                BuiltRoute(ArrayList(directions), ArrayList(segments))
            } else {
                null
            }
        }

        private fun extend(index: Int, from: Cell, forced: Direction?): Boolean {
            if (index == length) {
                return true
            }
            val options = forced?.let { listOf(it) }
                ?: directions[index - 1].perpendicular().shuffled(random)
            for (direction in options) {
                for (span in SPANS.shuffled(random)) {
                    val cells = trace(from, direction, span) ?: continue
                    directions.add(direction)
                    segments.add(cells)
                    visited.addAll(cells)
                    if (extend(index + 1, cells.last(), null)) {
                        return true
                    }
                    directions.removeAt(directions.size - 1)
                    segments.removeAt(segments.size - 1)
                    visited.removeAll(cells.toSet())
                }
            }
            return false
        }

        private fun trace(from: Cell, direction: Direction, span: Int): List<Cell>? {
            val cells = ArrayList<Cell>(span)
            var cursor = from
            for (step in 0 until span) {
                cursor = cursor.step(direction)
                if (!cursor.isInside(size) || visited.contains(cursor) || cells.contains(cursor)) {
                    return null
                }
                cells.add(cursor)
            }
            return cells
        }
    }

    private companion object {
        const val ATTEMPTS = 48
        const val CHECKPOINTS = 2
        const val OBSTACLES = 2
        val SPANS = listOf(1, 2, 3)
    }
}
