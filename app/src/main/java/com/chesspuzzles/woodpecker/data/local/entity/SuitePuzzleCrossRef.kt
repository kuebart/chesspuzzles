package com.chesspuzzles.woodpecker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "suite_puzzles",
    primaryKeys = ["suiteId", "puzzleId"],
    foreignKeys = [
        ForeignKey(
            entity = SuiteEntity::class,
            parentColumns = ["id"],
            childColumns = ["suiteId"],
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
        Index("suiteId"),
        Index("puzzleId")
    ]
)
data class SuitePuzzleCrossRef(
    val suiteId: Long,
    val puzzleId: String,
    val orderIndex: Int
)
