package com.chesspuzzles.woodpecker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "puzzles")
data class PuzzleEntity(
    @PrimaryKey val id: String,
    val fen: String,
    val moves: String,
    val rating: Int,
    val themes: String,
    val popularity: Int
)
