package com.chesspuzzles.woodpecker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "puzzle_attempts",
    foreignKeys = [
        ForeignKey(
            entity = CycleEntity::class,
            parentColumns = ["id"],
            childColumns = ["cycleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PuzzleEntity::class,
            parentColumns = ["id"],
            childColumns = ["puzzleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("cycleId"),
        Index("puzzleId")
    ]
)
data class PuzzleAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cycleId: Long,
    val puzzleId: String,
    val solved: Boolean,
    val timeMs: Long,
    val attemptedAt: Long
)
