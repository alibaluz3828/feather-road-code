package com.AsDfGhJ.jMiKoLp.domain.model

data class Cell(val col: Int, val row: Int) {

    fun step(direction: Direction): Cell = Cell(col + direction.dx, row + direction.dy)

    fun isInside(size: Int): Boolean = col in 0 until size && row in 0 until size

    fun distanceTo(other: Cell): Int =
        kotlin.math.abs(col - other.col) + kotlin.math.abs(row - other.row)
}
