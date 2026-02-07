package com.chesspuzzles.woodpecker.domain.model

data class Puzzle(
    val id: String,
    val fen: String,
    val moves: List<String>,
    val rating: Int,
    val themes: List<PuzzleTheme>,
    val popularity: Int
)
