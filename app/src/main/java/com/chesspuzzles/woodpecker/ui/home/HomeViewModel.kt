package com.chesspuzzles.woodpecker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesspuzzles.woodpecker.data.preferences.AppPreferences
import com.chesspuzzles.woodpecker.data.repository.PuzzleRepository
import com.chesspuzzles.woodpecker.data.repository.SuiteRepository
import com.chesspuzzles.woodpecker.domain.model.Suite
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val suites: List<Suite> = emptyList(),
    val totalSolved: Int = 0,
    val trainingDays: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val suiteRepository: SuiteRepository,
    private val puzzleRepository: PuzzleRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        suiteRepository.observeAllSuites(),
        puzzleRepository.observeTotalSolvedCount(),
        suiteRepository.observeTrainingDays()
    ) { suites, totalSolved, trainingDays ->
        // Enrich suites with cycle info
        val enrichedSuites = suites.map { suite ->
            val cycles = suiteRepository.getCyclesForSuite(suite.id)
            val lastCompleted = cycles.lastOrNull { it.completedAt != null }
            val lastAccuracy = if (lastCompleted != null) {
                suiteRepository.getCycleStats(lastCompleted.id)?.accuracy
            } else null

            val activeCycle = suiteRepository.getActiveCycle(suite.id)
            val activeCycleProgress = if (activeCycle != null) {
                suiteRepository.getAttemptCountForCycle(activeCycle.id)
            } else null

            val failedCount = suiteRepository.getLastCompletedCycleFailedCount(suite.id)

            suite.copy(
                cycleCount = cycles.count { it.completedAt != null },
                lastCycleAccuracy = lastAccuracy,
                activeCycleProgress = activeCycleProgress,
                failedPuzzleCount = failedCount
            )
        }

        val sortOrder = appPreferences.getSuiteSortOrder()
        val reversed = appPreferences.isSuiteSortReversed()

        val sortedSuites = when (sortOrder) {
            AppPreferences.SORT_NAME ->
                if (reversed) enrichedSuites.sortedByDescending { it.name.lowercase() }
                else enrichedSuites.sortedBy { it.name.lowercase() }
            AppPreferences.SORT_CREATED ->
                if (reversed) enrichedSuites.sortedBy { it.createdAt }
                else enrichedSuites.sortedByDescending { it.createdAt }
            else ->
                if (reversed) enrichedSuites.sortedBy { it.lastAccessedAt }
                else enrichedSuites.sortedByDescending { it.lastAccessedAt }
        }

        HomeUiState(
            suites = sortedSuites,
            totalSolved = totalSolved,
            trainingDays = trainingDays,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    suspend fun getOrCreateCycleForSuite(suiteId: Long): Long {
        return suiteRepository.getOrCreateActiveCycle(suiteId)
    }

    suspend fun startRetryForSuite(suiteId: Long): Long? {
        return suiteRepository.getLastCompletedCycleId(suiteId)
    }
}
