package com.chesspuzzles.woodpecker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesspuzzles.woodpecker.data.preferences.AppPreferences
import com.chesspuzzles.woodpecker.data.repository.PuzzleRepository
import com.chesspuzzles.woodpecker.data.repository.SuiteRepository
import com.chesspuzzles.woodpecker.domain.model.Suite
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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
    val isLoading: Boolean = true,
    val sortOrder: String = AppPreferences.SORT_LAST_ACCESS
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val suiteRepository: SuiteRepository,
    private val puzzleRepository: PuzzleRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(appPreferences.getSuiteSortOrder())

    val uiState: StateFlow<HomeUiState> = combine(
        suiteRepository.observeAllSuites(),
        puzzleRepository.observeTotalSolvedCount(),
        suiteRepository.observeTrainingDays(),
        _sortOrder
    ) { suites, totalSolved, trainingDays, sortOrder ->
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

        val sortedSuites = when (sortOrder) {
            AppPreferences.SORT_NAME -> enrichedSuites.sortedBy { it.name.lowercase() }
            AppPreferences.SORT_CREATED -> enrichedSuites.sortedByDescending { it.createdAt }
            else -> enrichedSuites.sortedByDescending { it.lastAccessedAt }
        }

        HomeUiState(
            suites = sortedSuites,
            totalSolved = totalSolved,
            trainingDays = trainingDays,
            isLoading = false,
            sortOrder = sortOrder
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun setSortOrder(sort: String) {
        appPreferences.setSuiteSortOrder(sort)
        _sortOrder.value = sort
    }

    suspend fun getOrCreateCycleForSuite(suiteId: Long): Long {
        return suiteRepository.getOrCreateActiveCycle(suiteId)
    }

    suspend fun startRetryForSuite(suiteId: Long): Long? {
        return suiteRepository.getLastCompletedCycleId(suiteId)
    }
}
