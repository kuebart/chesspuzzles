package com.chesspuzzles.woodpecker.domain.model

data class Cycle(
    val id: Long,
    val suiteId: Long,
    val cycleNumber: Int,
    val startedAt: Long,
    val completedAt: Long?,
    val stats: CycleStats? = null
)
