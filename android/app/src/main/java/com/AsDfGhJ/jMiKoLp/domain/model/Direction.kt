package com.AsDfGhJ.jMiKoLp.domain.model

enum class Direction(val dx: Int, val dy: Int) {
    UP(0, -1),
    RIGHT(1, 0),
    DOWN(0, 1),
    LEFT(-1, 0);

    fun opposite(): Direction = when (this) {
        UP -> DOWN
        RIGHT -> LEFT
        DOWN -> UP
        LEFT -> RIGHT
    }

    fun perpendicular(): List<Direction> = when (this) {
        UP, DOWN -> listOf(LEFT, RIGHT)
        LEFT, RIGHT -> listOf(UP, DOWN)
    }

    fun degrees(): Float = when (this) {
        UP -> 0f
        RIGHT -> 90f
        DOWN -> 180f
        LEFT -> 270f
    }
}
