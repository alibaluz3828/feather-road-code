package com.AsDfGhJ.jMiKoLp.domain.model

enum class RouteTier(
    val id: String,
    val codeLength: Int,
    val actionBudget: Int,
    val boardSize: Int
) {
    SHORT_HAUL("short_haul", 4, 7, 5),
    STANDARD("standard", 5, 8, 5),
    LONG_HAUL("long_haul", 6, 9, 5),
    NIGHT_SHIFT("night_shift", 7, 10, 5);

    fun next(): RouteTier {
        val all = RouteTier.entries
        return all[(all.indexOf(this) + 1) % all.size]
    }

    companion object {
        fun fromId(id: String?): RouteTier = RouteTier.entries.firstOrNull { it.id == id } ?: STANDARD
    }
}
