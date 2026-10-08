package com.AsDfGhJ.jMiKoLp.data.sample

import com.AsDfGhJ.jMiKoLp.domain.model.Cell
import com.AsDfGhJ.jMiKoLp.domain.model.Direction
import com.AsDfGhJ.jMiKoLp.domain.model.RoutePlan
import com.AsDfGhJ.jMiKoLp.domain.model.RouteTier

object SampleData {

    val ROUTE_NAMES = listOf(
        "NORTH GATE",
        "COAL SIDING",
        "GRAIN LOOP",
        "SCRAP YARD",
        "NIGHT DEPOT",
        "FEATHER LANE"
    )

    val TIER_CAPTIONS = mapOf(
        RouteTier.SHORT_HAUL to "ONE CRATE, FOUR TURNS",
        RouteTier.STANDARD to "THE EVERYDAY SHIFT RUN",
        RouteTier.LONG_HAUL to "ACROSS THE WHOLE YARD",
        RouteTier.NIGHT_SHIFT to "FLOODLIGHTS AND SHARP TURNS"
    )

    val COACH_LINES = listOf(
        "READ THE CODE",
        "TRACE THE ROUTE",
        "SET ONE SIGN"
    )

    fun routeName(routeNumber: Int): String =
        ROUTE_NAMES[((routeNumber - 1).coerceAtLeast(0)) % ROUTE_NAMES.size]

    fun fallbackRoute(tier: RouteTier, routeNumber: Int): RoutePlan = when (tier) {
        RouteTier.SHORT_HAUL -> shortHaul(routeNumber)
        RouteTier.STANDARD -> standard(routeNumber)
        RouteTier.LONG_HAUL -> longHaul(routeNumber)
        RouteTier.NIGHT_SHIFT -> nightShift(routeNumber)
    }

    fun tutorialRoute(): RoutePlan {
        val sockets = listOf(Cell(0, 0))
        return RoutePlan(
            tier = RouteTier.SHORT_HAUL,
            boardSize = 3,
            entry = Cell(0, 2),
            entryHeading = Direction.UP,
            exit = Cell(2, 0),
            code = listOf(Direction.UP, Direction.RIGHT),
            sockets = sockets,
            solution = mapOf(Cell(0, 0) to Direction.RIGHT),
            checkpoints = listOf(Cell(0, 1)),
            obstacles = listOf(Cell(1, 2)),
            path = listOf(Cell(0, 2), Cell(0, 1), Cell(0, 0), Cell(1, 0), Cell(2, 0)),
            routeName = "TRAINING BAY",
            routeNumber = 0
        )
    }

    private fun shortHaul(routeNumber: Int): RoutePlan {
        val sockets = listOf(Cell(0, 2), Cell(2, 2), Cell(2, 0))
        return RoutePlan(
            tier = RouteTier.SHORT_HAUL,
            boardSize = 5,
            entry = Cell(0, 4),
            entryHeading = Direction.UP,
            exit = Cell(4, 0),
            code = listOf(Direction.UP, Direction.RIGHT, Direction.UP, Direction.RIGHT),
            sockets = sockets,
            solution = mapOf(
                Cell(0, 2) to Direction.RIGHT,
                Cell(2, 2) to Direction.UP,
                Cell(2, 0) to Direction.RIGHT
            ),
            checkpoints = listOf(Cell(0, 3), Cell(1, 2)),
            obstacles = listOf(Cell(1, 4), Cell(3, 2)),
            path = listOf(
                Cell(0, 4), Cell(0, 3), Cell(0, 2), Cell(1, 2), Cell(2, 2),
                Cell(2, 1), Cell(2, 0), Cell(3, 0), Cell(4, 0)
            ),
            routeName = routeName(routeNumber),
            routeNumber = routeNumber
        )
    }

    private fun standard(routeNumber: Int): RoutePlan {
        val sockets = listOf(Cell(0, 2), Cell(2, 2), Cell(2, 0), Cell(4, 0))
        return RoutePlan(
            tier = RouteTier.STANDARD,
            boardSize = 5,
            entry = Cell(0, 4),
            entryHeading = Direction.UP,
            exit = Cell(4, 2),
            code = listOf(
                Direction.UP, Direction.RIGHT, Direction.UP, Direction.RIGHT, Direction.DOWN
            ),
            sockets = sockets,
            solution = mapOf(
                Cell(0, 2) to Direction.RIGHT,
                Cell(2, 2) to Direction.UP,
                Cell(2, 0) to Direction.RIGHT,
                Cell(4, 0) to Direction.DOWN
            ),
            checkpoints = listOf(Cell(0, 3), Cell(1, 2)),
            obstacles = listOf(Cell(1, 4), Cell(3, 4)),
            path = listOf(
                Cell(0, 4), Cell(0, 3), Cell(0, 2), Cell(1, 2), Cell(2, 2),
                Cell(2, 1), Cell(2, 0), Cell(3, 0), Cell(4, 0), Cell(4, 1), Cell(4, 2)
            ),
            routeName = routeName(routeNumber),
            routeNumber = routeNumber
        )
    }

    private fun longHaul(routeNumber: Int): RoutePlan {
        val sockets = listOf(Cell(0, 2), Cell(2, 2), Cell(2, 0), Cell(4, 0), Cell(4, 3))
        return RoutePlan(
            tier = RouteTier.LONG_HAUL,
            boardSize = 5,
            entry = Cell(0, 4),
            entryHeading = Direction.UP,
            exit = Cell(2, 3),
            code = listOf(
                Direction.UP, Direction.RIGHT, Direction.UP,
                Direction.RIGHT, Direction.DOWN, Direction.LEFT
            ),
            sockets = sockets,
            solution = mapOf(
                Cell(0, 2) to Direction.RIGHT,
                Cell(2, 2) to Direction.UP,
                Cell(2, 0) to Direction.RIGHT,
                Cell(4, 0) to Direction.DOWN,
                Cell(4, 3) to Direction.LEFT
            ),
            checkpoints = listOf(Cell(0, 3), Cell(1, 2)),
            obstacles = listOf(Cell(1, 0), Cell(3, 1)),
            path = listOf(
                Cell(0, 4), Cell(0, 3), Cell(0, 2), Cell(1, 2), Cell(2, 2),
                Cell(2, 1), Cell(2, 0), Cell(3, 0), Cell(4, 0), Cell(4, 1),
                Cell(4, 2), Cell(4, 3), Cell(3, 3), Cell(2, 3)
            ),
            routeName = routeName(routeNumber),
            routeNumber = routeNumber
        )
    }

    private fun nightShift(routeNumber: Int): RoutePlan {
        val sockets = listOf(
            Cell(0, 3), Cell(2, 3), Cell(2, 0), Cell(4, 0), Cell(4, 2), Cell(3, 2)
        )
        return RoutePlan(
            tier = RouteTier.NIGHT_SHIFT,
            boardSize = 5,
            entry = Cell(0, 4),
            entryHeading = Direction.UP,
            exit = Cell(3, 4),
            code = listOf(
                Direction.UP, Direction.RIGHT, Direction.UP, Direction.RIGHT,
                Direction.DOWN, Direction.LEFT, Direction.DOWN
            ),
            sockets = sockets,
            solution = mapOf(
                Cell(0, 3) to Direction.RIGHT,
                Cell(2, 3) to Direction.UP,
                Cell(2, 0) to Direction.RIGHT,
                Cell(4, 0) to Direction.DOWN,
                Cell(4, 2) to Direction.LEFT,
                Cell(3, 2) to Direction.DOWN
            ),
            checkpoints = listOf(Cell(2, 1), Cell(1, 3)),
            obstacles = listOf(Cell(1, 1), Cell(4, 3)),
            path = listOf(
                Cell(0, 4), Cell(0, 3), Cell(1, 3), Cell(2, 3), Cell(2, 2),
                Cell(2, 1), Cell(2, 0), Cell(3, 0), Cell(4, 0), Cell(4, 1),
                Cell(4, 2), Cell(3, 2), Cell(3, 3), Cell(3, 4)
            ),
            routeName = routeName(routeNumber),
            routeNumber = routeNumber
        )
    }
}
