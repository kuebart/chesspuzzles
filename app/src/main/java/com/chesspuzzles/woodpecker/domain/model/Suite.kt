package com.chesspuzzles.woodpecker.domain.model

data class Suite(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val themes: List<PuzzleTheme>,
    val ratingMin: Int,
    val ratingMax: Int,
    val puzzleCount: Int,
    val lastAccessedAt: Long = 0,
    val version: Int = 1,
    val groupId: Long = 0,
    val cycleCount: Int = 0,
    val lastCycleAccuracy: Float? = null,
    val activeCycleProgress: Int? = null,
    val failedPuzzleCount: Int = 0
)
