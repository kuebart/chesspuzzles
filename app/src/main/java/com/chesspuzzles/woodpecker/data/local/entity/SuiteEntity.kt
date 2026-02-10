package com.chesspuzzles.woodpecker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suites")
data class SuiteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
    val themes: String,
    val ratingMin: Int,
    val ratingMax: Int,
    val puzzleCount: Int,
    val lastAccessedAt: Long = 0,
    val version: Int = 1,
    val groupId: Long = 0
)
