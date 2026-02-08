package com.chesspuzzles.woodpecker.ui.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesspuzzles.woodpecker.data.repository.SuiteRepository
import com.chesspuzzles.woodpecker.domain.model.Cycle
import com.chesspuzzles.woodpecker.domain.model.CycleStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CycleSummaryUiState(
    val cycleNumber: Int = 0,
    val stats: CycleStats? = null,
    val allCycleStats: List<Pair<Cycle, CycleStats>> = emptyList(),
    val suiteId: Long = 0,
    val isLoading: Boolean = true,
    val failedCount: Int = 0,
    val isLatestCycle: Boolean = false
)

@HiltViewModel
class CycleSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val suiteRepository: SuiteRepository
) : ViewModel() {

    private val cycleId: Long = savedStateHandle["cycleId"]!!

    private val _uiState = MutableStateFlow(CycleSummaryUiState())
    val uiState: StateFlow<CycleSummaryUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val cycles = suiteRepository.getCyclesForSuite(0) // We need to find suite from cycle
            // Get cycle info to find suiteId
            val stats = suiteRepository.getCycleStats(cycleId)

            // Find the cycle to get suiteId and cycleNumber
            // We'll iterate through all suites to find it
            val allSuites = suiteRepository.observeAllSuites()
            allSuites.collect { suites ->
                for (suite in suites) {
                    val suiteCycles = suiteRepository.getCyclesForSuite(suite.id)
                    val cycle = suiteCycles.find { it.id == cycleId }
                    if (cycle != null) {
                        val allCycleStats = suiteRepository.getCycleStatsForAllCycles(suite.id)
                        val failedCount = suiteRepository.getFailedCountForCycle(cycleId)
                        val latestCompleted = suiteCycles.filter { it.completedAt != null }.maxByOrNull { it.cycleNumber }
                        _uiState.update {
                            it.copy(
                                cycleNumber = cycle.cycleNumber,
                                stats = stats,
                                allCycleStats = allCycleStats,
                                suiteId = suite.id,
                                isLoading = false,
                                failedCount = failedCount,
                                isLatestCycle = latestCompleted?.id == cycleId
                            )
                        }
                        return@collect
                    }
                }
            }
        }
    }

    fun startNextCycle(onStarted: (suiteId: Long, cycleId: Long) -> Unit) {
        viewModelScope.launch {
            val suiteId = _uiState.value.suiteId
            val newCycleId = suiteRepository.getOrCreateActiveCycle(suiteId)
            onStarted(suiteId, newCycleId)
        }
    }

    fun startRetryTraining(onStarted: (suiteId: Long, cycleId: Long) -> Unit) {
        viewModelScope.launch {
            onStarted(_uiState.value.suiteId, cycleId)
        }
    }
}
