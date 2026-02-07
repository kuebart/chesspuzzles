package com.chesspuzzles.woodpecker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cycles",
    foreignKeys = [
        ForeignKey(
            entity = SuiteEntity::class,
            parentColumns = ["id"],
            childColumns = ["suiteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("suiteId")]
)
data class CycleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val suiteId: Long,
    val cycleNumber: Int,
    val startedAt: Long,
    val completedAt: Long? = null
)
