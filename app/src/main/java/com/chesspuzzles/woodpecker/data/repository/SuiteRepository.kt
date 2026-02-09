package com.chesspuzzles.woodpecker.data.repository

import com.chesspuzzles.woodpecker.data.local.dao.CycleDao
import com.chesspuzzles.woodpecker.data.local.dao.SuiteDao
import com.chesspuzzles.woodpecker.data.local.entity.CycleEntity
import com.chesspuzzles.woodpecker.data.local.entity.PuzzleAttemptEntity
import com.chesspuzzles.woodpecker.data.local.entity.SuiteEntity
import com.chesspuzzles.woodpecker.domain.model.Cycle
import com.chesspuzzles.woodpecker.domain.model.CycleStats
import com.chesspuzzles.woodpecker.domain.model.PuzzleTheme
import com.chesspuzzles.woodpecker.domain.model.Suite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuiteRepository @Inject constructor(
    private val suiteDao: SuiteDao,
    private val cycleDao: CycleDao
) {

    fun observeAllSuites(): Flow<List<Suite>> =
        suiteDao.observeAllSuites().map { suites ->
            suites.map { it.toDomain() }
        }

    suspend fun getSuiteById(suiteId: Long): Suite? =
        suiteDao.getSuiteById(suiteId)?.toDomain()

    suspend fun getSuiteCount(): Int = suiteDao.getSuiteCount()

    fun observeSuiteById(suiteId: Long): Flow<Suite?> =
        suiteDao.observeSuiteById(suiteId).map { it?.toDomain() }

    suspend fun createSuite(
        name: String,
        themes: List<PuzzleTheme>,
        ratingMin: Int,
        ratingMax: Int,
        puzzleIds: List<String>
    ): Long {
        val suite = SuiteEntity(
            name = name,
            createdAt = System.currentTimeMillis(),
            themes = themes.joinToString(",") { it.csvKey },
            ratingMin = ratingMin,
            ratingMax = ratingMax,
            puzzleCount = puzzleIds.size
        )
        return suiteDao.createSuiteWithPuzzles(suite, puzzleIds)
    }

    suspend fun deleteSuite(suiteId: Long) {
        suiteDao.deleteSuite(suiteId)
    }

    suspend fun renameSuite(suiteId: Long, name: String) {
        suiteDao.updateSuiteName(suiteId, name)
    }

    suspend fun touchSuiteAccess(suiteId: Long) {
        suiteDao.updateLastAccessedAt(suiteId, System.currentTimeMillis())
    }

    fun observeCyclesForSuite(suiteId: Long): Flow<List<Cycle>> =
        cycleDao.observeCyclesForSuite(suiteId).map { cycles ->
            cycles.map { it.toDomain() }
        }

    suspend fun getCyclesForSuite(suiteId: Long): List<Cycle> =
        cycleDao.getCyclesForSuite(suiteId).map { it.toDomain() }

    suspend fun startNewCycle(suiteId: Long): Long {
        val active = cycleDao.getActiveCycle(suiteId)
        if (active != null) return active.id
        val maxNumber = cycleDao.getMaxCycleNumber(suiteId) ?: 0
        val cycle = CycleEntity(
            suiteId = suiteId,
            cycleNumber = maxNumber + 1,
            startedAt = System.currentTimeMillis()
        )
        return cycleDao.insertCycle(cycle)
    }

    suspend fun getActiveCycle(suiteId: Long): Cycle? =
        cycleDao.getActiveCycle(suiteId)?.toDomain()

    suspend fun getOrCreateActiveCycle(suiteId: Long): Long {
        val active = cycleDao.getActiveCycle(suiteId)
        return active?.id ?: startNewCycle(suiteId)
    }

    suspend fun completeCycle(cycleId: Long) {
        val cycle = cycleDao.getCycleById(cycleId) ?: return
        cycleDao.updateCycle(cycle.copy(completedAt = System.currentTimeMillis()))
    }

    suspend fun saveAttempt(
        cycleId: Long,
        puzzleId: String,
        solved: Boolean,
        timeMs: Long
    ) {
        cycleDao.insertAttempt(
            PuzzleAttemptEntity(
                cycleId = cycleId,
                puzzleId = puzzleId,
                solved = solved,
                timeMs = timeMs,
                attemptedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getAttemptCountForCycle(cycleId: Long): Int =
        cycleDao.getAttemptCountForCycle(cycleId)

    suspend fun getAccumulatedTimeForCycle(cycleId: Long): Long =
        cycleDao.getTotalTimeForCycle(cycleId) ?: 0L

    private fun latestAttemptsPerPuzzle(
        allAttempts: List<PuzzleAttemptEntity>
    ): List<PuzzleAttemptEntity> =
        allAttempts.groupBy { it.puzzleId }
            .map { (_, attempts) -> attempts.maxByOrNull { it.attemptedAt }!! }

    suspend fun getCycleStats(cycleId: Long): CycleStats? {
        val cycle = cycleDao.getCycleById(cycleId) ?: return null
        val allAttempts = cycleDao.getAttemptsForCycle(cycleId)
        if (allAttempts.isEmpty()) return null

        val attempts = latestAttemptsPerPuzzle(allAttempts)
        val solvedCount = attempts.count { it.solved }
        val totalTime = attempts.sumOf { it.timeMs }
        val accuracy = solvedCount.toFloat() / attempts.size

        var prevTotalTime: Long? = null
        var prevAccuracy: Float? = null

        if (cycle.cycleNumber > 1) {
            val prevCycles = cycleDao.getCyclesForSuite(cycle.suiteId)
            val prevCycle = prevCycles.find { it.cycleNumber == cycle.cycleNumber - 1 }
            if (prevCycle != null) {
                val prevAllAttempts = cycleDao.getAttemptsForCycle(prevCycle.id)
                if (prevAllAttempts.isNotEmpty()) {
                    val prevAttempts = latestAttemptsPerPuzzle(prevAllAttempts)
                    prevTotalTime = prevAttempts.sumOf { it.timeMs }
                    prevAccuracy = prevAttempts.count { it.solved }.toFloat() / prevAttempts.size
                }
            }
        }

        return CycleStats(
            totalPuzzles = attempts.size,
            solvedCount = solvedCount,
            totalTimeMs = totalTime,
            averageTimeMs = totalTime / attempts.size,
            accuracy = accuracy,
            previousCycleTotalTimeMs = prevTotalTime,
            previousCycleAccuracy = prevAccuracy
        )
    }

    suspend fun getCycleStatsForAllCycles(suiteId: Long): List<Pair<Cycle, CycleStats>> {
        val cycles = cycleDao.getCyclesForSuite(suiteId)
        return cycles.mapNotNull { cycle ->
            val stats = getCycleStats(cycle.id)
            if (stats != null) Pair(cycle.toDomain(), stats) else null
        }
    }

    suspend fun getCurrentlyFailedPuzzleIdsForCycle(cycleId: Long): List<String> {
        val attempts = cycleDao.getAttemptsForCycle(cycleId)
        return latestAttemptsPerPuzzle(attempts)
            .filter { !it.solved }
            .map { it.puzzleId }
    }

    suspend fun getCycleProgress(cycleId: Long): Pair<Int, Int> {
        val attempts = cycleDao.getAttemptsForCycle(cycleId)
        val latest = latestAttemptsPerPuzzle(attempts)
        return Pair(latest.count { it.solved }, latest.count { !it.solved })
    }

    suspend fun getLastCompletedCycleFailedCount(suiteId: Long): Int {
        val cycles = cycleDao.getCyclesForSuite(suiteId)
        val lastCompleted = cycles.lastOrNull { it.completedAt != null } ?: return 0
        return getCurrentlyFailedPuzzleIdsForCycle(lastCompleted.id).size
    }

    suspend fun getFailedCountForCycle(cycleId: Long): Int {
        return getCurrentlyFailedPuzzleIdsForCycle(cycleId).size
    }

    suspend fun getLastCompletedCycleId(suiteId: Long): Long? {
        val cycles = cycleDao.getCyclesForSuite(suiteId)
        return cycles.lastOrNull { it.completedAt != null }?.id
    }

    fun observeTrainingDays(): Flow<Int> = cycleDao.observeTrainingDays()

    private fun SuiteEntity.toDomain() = Suite(
        id = id,
        name = name,
        createdAt = createdAt,
        themes = themes.split(",").filter { it.isNotBlank() }.mapNotNull { PuzzleTheme.fromCsvKey(it.trim()) },
        ratingMin = ratingMin,
        ratingMax = ratingMax,
        puzzleCount = puzzleCount,
        lastAccessedAt = lastAccessedAt
    )

    private fun CycleEntity.toDomain() = Cycle(
        id = id,
        suiteId = suiteId,
        cycleNumber = cycleNumber,
        startedAt = startedAt,
        completedAt = completedAt
    )
}
