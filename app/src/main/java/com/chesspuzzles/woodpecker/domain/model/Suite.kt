package com.chesspuzzles.woodpecker.domain.model

data class Suite(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val themes: List<PuzzleTheme>,
    val ratingMin: Int,
    val ratingMax: Int,
    val puzzleCount: Int,
    val cycleCount: Int = 0,
    val lastCycleAccuracy: Float? = null,
    val activeCycleProgress: Int? = null
)
