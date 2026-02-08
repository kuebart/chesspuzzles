package com.chesspuzzles.woodpecker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.chesspuzzles.woodpecker.data.local.entity.CycleEntity
import com.chesspuzzles.woodpecker.data.local.entity.PuzzleAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleDao {

    @Insert
    suspend fun insertCycle(cycle: CycleEntity): Long

    @Update
    suspend fun updateCycle(cycle: CycleEntity)

    @Query("SELECT * FROM cycles WHERE id = :cycleId")
    suspend fun getCycleById(cycleId: Long): CycleEntity?

    @Query("SELECT * FROM cycles WHERE suiteId = :suiteId ORDER BY cycleNumber ASC")
    fun observeCyclesForSuite(suiteId: Long): Flow<List<CycleEntity>>

    @Query("SELECT * FROM cycles WHERE suiteId = :suiteId ORDER BY cycleNumber ASC")
    suspend fun getCyclesForSuite(suiteId: Long): List<CycleEntity>

    @Query("SELECT MAX(cycleNumber) FROM cycles WHERE suiteId = :suiteId")
    suspend fun getMaxCycleNumber(suiteId: Long): Int?

    @Query("SELECT * FROM cycles WHERE suiteId = :suiteId AND completedAt IS NULL LIMIT 1")
    suspend fun getActiveCycle(suiteId: Long): CycleEntity?

    @Insert
    suspend fun insertAttempt(attempt: PuzzleAttemptEntity)

    @Query("SELECT * FROM puzzle_attempts WHERE cycleId = :cycleId ORDER BY attemptedAt ASC")
    suspend fun getAttemptsForCycle(cycleId: Long): List<PuzzleAttemptEntity>

    @Query("SELECT * FROM puzzle_attempts WHERE cycleId = :cycleId ORDER BY attemptedAt ASC")
    fun observeAttemptsForCycle(cycleId: Long): Flow<List<PuzzleAttemptEntity>>

    @Query("SELECT COUNT(*) FROM puzzle_attempts WHERE cycleId = :cycleId")
    suspend fun getAttemptCountForCycle(cycleId: Long): Int

    @Query("SELECT COUNT(*) FROM puzzle_attempts WHERE cycleId = :cycleId AND solved = 1")
    suspend fun getSolvedCountForCycle(cycleId: Long): Int

    @Query("SELECT SUM(timeMs) FROM puzzle_attempts WHERE cycleId = :cycleId")
    suspend fun getTotalTimeForCycle(cycleId: Long): Long?

    @Query("SELECT COUNT(DISTINCT DATE(attemptedAt / 1000, 'unixepoch')) FROM puzzle_attempts")
    fun observeTrainingDays(): Flow<Int>

}
