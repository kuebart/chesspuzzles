package com.chesspuzzles.woodpecker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesspuzzles.woodpecker.data.local.dao.CycleDao
import com.chesspuzzles.woodpecker.data.local.dao.SuiteDao
import com.chesspuzzles.woodpecker.data.repository.PuzzleRepository
import com.chesspuzzles.woodpecker.data.repository.SuiteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class StatsUiState(
    val totalSolved: Int = 0,
    val trainingDays: Int = 0,
    val completedCycles: Int = 0,
    val totalTrainingTimeMs: Long = 0,
    val averageAccuracy: Float = 0f,
    val bestAccuracy: Float = 0f,
    val avgTimePerPuzzle: Long = 0,
    val currentStreak: Int = 0,
    val solvedToday: Int = 0,
    val suiteCount: Int = 0
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val puzzleRepository: PuzzleRepository,
    private val suiteRepository: SuiteRepository,
    private val cycleDao: CycleDao,
    private val suiteDao: SuiteDao
) : ViewModel() {

    private val _extraStats = MutableStateFlow(StatsUiState())

    val uiState: StateFlow<StatsUiState> = combine(
        puzzleRepository.observeTotalSolvedCount(),
        suiteRepository.observeTrainingDays(),
        _extraStats
    ) { totalSolved, trainingDays, extra ->
        extra.copy(
            totalSolved = totalSolved,
            trainingDays = trainingDays
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )

    init {
        loadExtraStats()
    }

    private fun loadExtraStats() {
        viewModelScope.launch {
            val completedCycles = cycleDao.getCompletedCycleCount()
            val totalTime = cycleDao.getTotalTrainingTime() ?: 0L
            val avgTime = cycleDao.getAverageTimePerPuzzle() ?: 0L
            val solvedToday = cycleDao.getPuzzlesSolvedToday()
            val suiteCount = suiteDao.getSuiteCount()

            // Calculate average and best accuracy across all completed cycles
            val allSuites = suiteRepository.getAllSuiteIds()
            var totalAccuracy = 0f
            var bestAccuracy = 0f
            var cycleCount = 0
            for (suiteId in allSuites) {
                val cycleStats = suiteRepository.getCycleStatsForAllCycles(suiteId)
                for ((_, stats) in cycleStats) {
                    totalAccuracy += stats.accuracy
                    if (stats.accuracy > bestAccuracy) bestAccuracy = stats.accuracy
                    cycleCount++
                }
            }
            val avgAccuracy = if (cycleCount > 0) totalAccuracy / cycleCount else 0f

            // Calculate current streak
            val dates = cycleDao.getAllTrainingDates()
            val streak = calculateStreak(dates)

            _extraStats.value = StatsUiState(
                completedCycles = completedCycles,
                totalTrainingTimeMs = totalTime,
                averageAccuracy = avgAccuracy,
                bestAccuracy = bestAccuracy,
                avgTimePerPuzzle = avgTime,
                currentStreak = streak,
                solvedToday = solvedToday,
                suiteCount = suiteCount
            )
        }
    }

    private fun calculateStreak(dateStrings: List<String>): Int {
        if (dateStrings.isEmpty()) return 0
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()
        val dates = dateStrings.mapNotNull {
            try { LocalDate.parse(it, formatter) } catch (_: Exception) { null }
        }.sortedDescending()

        if (dates.isEmpty()) return 0

        // Streak must include today or yesterday
        val first = dates[0]
        if (first != today && first != today.minusDays(1)) return 0

        var streak = 1
        for (i in 1 until dates.size) {
            if (dates[i] == dates[i - 1].minusDays(1)) {
                streak++
            } else if (dates[i] != dates[i - 1]) {
                break
            }
        }
        return streak
    }
}
