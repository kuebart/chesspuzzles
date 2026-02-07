package com.chesspuzzles.woodpecker.data.repository

import com.chesspuzzles.woodpecker.data.local.dao.PuzzleDao
import com.chesspuzzles.woodpecker.data.local.entity.PuzzleEntity
import com.chesspuzzles.woodpecker.domain.model.Puzzle
import com.chesspuzzles.woodpecker.domain.model.PuzzleTheme

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PuzzleRepository @Inject constructor(
    private val puzzleDao: PuzzleDao
) {

    suspend fun getPuzzleCount(): Int = puzzleDao.count()

    suspend fun getPuzzleById(id: String): Puzzle? =
        puzzleDao.getById(id)?.toDomain()

    suspend fun getPuzzlesForSuite(suiteId: Long): List<Puzzle> =
        puzzleDao.getPuzzlesForSuite(suiteId).map { it.toDomain() }

    suspend fun findPuzzles(
        themes: List<PuzzleTheme>,
        ratingMin: Int,
        ratingMax: Int,
        limit: Int
    ): List<PuzzleEntity> {
        return if (themes.isEmpty()) {
            puzzleDao.getRandomByRating(ratingMin, ratingMax, limit)
        } else {
            val allResults = mutableListOf<PuzzleEntity>()
            for (theme in themes) {
                val results = puzzleDao.getRandomByRatingAndTheme(
                    ratingMin, ratingMax, theme.csvKey, limit
                )
                allResults.addAll(results)
            }
            allResults.distinctBy { it.id }.shuffled().take(limit)
        }
    }

    suspend fun countMatchingPuzzles(
        themes: List<PuzzleTheme>,
        ratingMin: Int,
        ratingMax: Int
    ): Int {
        return if (themes.isEmpty()) {
            puzzleDao.countByRatingAndTheme(ratingMin, ratingMax, "")
        } else {
            var total = 0
            for (theme in themes) {
                total += puzzleDao.countByRatingAndTheme(ratingMin, ratingMax, theme.csvKey)
            }
            total
        }
    }


    fun observeTotalSolvedCount(): Flow<Int> = puzzleDao.observeTotalSolvedCount()

    private fun PuzzleEntity.toDomain() = Puzzle(
        id = id,
        fen = fen,
        moves = moves.split(" "),
        rating = rating,
        themes = themes.split(",").mapNotNull { PuzzleTheme.fromCsvKey(it.trim()) },
        popularity = popularity
    )
}
