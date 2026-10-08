package com.AsDfGhJ.jMiKoLp.domain.model

data class RoutePlan(
    val tier: RouteTier,
    val boardSize: Int,
    val entry: Cell,
    val entryHeading: Direction,
    val exit: Cell,
    val code: List<Direction>,
    val sockets: List<Cell>,
    val solution: Map<Cell, Direction>,
    val checkpoints: List<Cell>,
    val obstacles: List<Cell>,
    val path: List<Cell>,
    val routeName: String,
    val routeNumber: Int
) {
    val codeLength: Int get() = code.size

    fun dockDemand(): Map<Direction, Int> {
        val demand = LinkedHashMap<Direction, Int>()
        for (socket in sockets) {
            val direction = solution[socket] ?: continue
            demand[direction] = (demand[direction] ?: 0) + 1
        }
        return demand
    }

    fun socketIndex(cell: Cell): Int = sockets.indexOf(cell)
}
