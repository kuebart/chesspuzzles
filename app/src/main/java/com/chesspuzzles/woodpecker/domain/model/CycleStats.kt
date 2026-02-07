package com.chesspuzzles.woodpecker.domain.model

data class CycleStats(
    val totalPuzzles: Int,
    val solvedCount: Int,
    val totalTimeMs: Long,
    val averageTimeMs: Long,
    val accuracy: Float,
    val previousCycleTotalTimeMs: Long? = null,
    val previousCycleAccuracy: Float? = null
) {
    val timeDeltaMs: Long?
        get() = previousCycleTotalTimeMs?.let { totalTimeMs - it }

    val accuracyDelta: Float?
        get() = previousCycleAccuracy?.let { accuracy - it }
}
