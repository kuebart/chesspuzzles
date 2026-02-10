package com.chesspuzzles.woodpecker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chesspuzzles.woodpecker.data.local.entity.PuzzleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PuzzleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(puzzles: List<PuzzleEntity>)

    @Query("SELECT COUNT(*) FROM puzzles")
    suspend fun count(): Int

    @Query("SELECT * FROM puzzles WHERE id = :id")
    suspend fun getById(id: String): PuzzleEntity?

    @Query("""
        SELECT * FROM puzzles
        WHERE rating BETWEEN :minRating AND :maxRating
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getRandomByRating(minRating: Int, maxRating: Int, limit: Int): List<PuzzleEntity>

    @Query("""
        SELECT * FROM puzzles
        WHERE rating BETWEEN :minRating AND :maxRating
        AND (:themes = '' OR themes LIKE '%' || :themes || '%')
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getRandomByRatingAndTheme(
        minRating: Int,
        maxRating: Int,
        themes: String,
        limit: Int
    ): List<PuzzleEntity>

    @Query("""
        SELECT COUNT(*) FROM puzzles
        WHERE rating BETWEEN :minRating AND :maxRating
        AND (:themes = '' OR themes LIKE '%' || :themes || '%')
    """)
    suspend fun countByRatingAndTheme(minRating: Int, maxRating: Int, themes: String): Int

    @Query("""
        SELECT p.* FROM puzzles p
        INNER JOIN suite_puzzles sp ON p.id = sp.puzzleId
        WHERE sp.suiteId = :suiteId
        ORDER BY sp.orderIndex
    """)
    suspend fun getPuzzlesForSuite(suiteId: Long): List<PuzzleEntity>

    @Query("""
        SELECT p.* FROM puzzles p
        INNER JOIN suite_puzzles sp ON p.id = sp.puzzleId
        WHERE sp.suiteId = :suiteId
        ORDER BY sp.orderIndex
    """)
    fun observePuzzlesForSuite(suiteId: Long): Flow<List<PuzzleEntity>>

    @Query("SELECT COUNT(DISTINCT pa.puzzleId) FROM puzzle_attempts pa WHERE pa.solved = 1")
    fun observeTotalSolvedCount(): Flow<Int>

    @Query("""
        SELECT * FROM puzzles
        WHERE rating BETWEEN :minRating AND :maxRating
        AND (:themes = '' OR themes LIKE '%' || :themes || '%')
        AND id NOT IN (:excludeIds)
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getRandomByRatingAndThemeExcluding(
        minRating: Int,
        maxRating: Int,
        themes: String,
        limit: Int,
        excludeIds: List<String>
    ): List<PuzzleEntity>

    @Query("""
        SELECT * FROM puzzles
        WHERE rating BETWEEN :minRating AND :maxRating
        AND id NOT IN (:excludeIds)
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getRandomByRatingExcluding(
        minRating: Int,
        maxRating: Int,
        limit: Int,
        excludeIds: List<String>
    ): List<PuzzleEntity>

    @Query("SELECT DISTINCT themes FROM puzzles")
    suspend fun getAllThemeStrings(): List<String>
}
