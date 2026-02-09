package com.chesspuzzles.woodpecker.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesspuzzles.woodpecker.data.repository.PuzzleRepository
import com.chesspuzzles.woodpecker.data.repository.SuiteRepository
import com.chesspuzzles.woodpecker.domain.model.Cycle
import com.chesspuzzles.woodpecker.domain.model.CycleStats
import com.chesspuzzles.woodpecker.domain.model.Suite
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CycleWithStats(
    val cycle: Cycle,
    val stats: CycleStats?,
    val progress: Int?
)

data class SuiteDetailUiState(
    val suite: Suite? = null,
    val cycles: List<CycleWithStats> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class SuiteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val suiteRepository: SuiteRepository,
    private val puzzleRepository: PuzzleRepository
) : ViewModel() {

    private val suiteId: Long = savedStateHandle["suiteId"]!!

    init {
        viewModelScope.launch {
            suiteRepository.touchSuiteAccess(suiteId)
        }
    }

    val uiState: StateFlow<SuiteDetailUiState> = combine(
        suiteRepository.observeSuiteById(suiteId),
        suiteRepository.observeCyclesForSuite(suiteId)
    ) { suite, cycles ->
        val cyclesWithStats = cycles.map { cycle ->
            val stats = suiteRepository.getCycleStats(cycle.id)
            val progress = if (cycle.completedAt == null) {
                suiteRepository.getAttemptCountForCycle(cycle.id)
            } else null
            CycleWithStats(cycle, stats, progress)
        }

        SuiteDetailUiState(
            suite = suite,
            cycles = cyclesWithStats,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SuiteDetailUiState()
    )

    fun deleteSuite(onDeleted: () -> Unit) {
        viewModelScope.launch {
            suiteRepository.deleteSuite(suiteId)
            onDeleted()
        }
    }

    suspend fun getOrCreateCycle(): Long {
        return suiteRepository.getOrCreateActiveCycle(suiteId)
    }

    fun renameSuite(name: String) {
        viewModelScope.launch {
            suiteRepository.renameSuite(suiteId, name)
        }
    }

    fun regenerateSuite(onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val suite = uiState.value.suite ?: return@launch
            val puzzles = puzzleRepository.findPuzzles(
                themes = suite.themes,
                ratingMin = suite.ratingMin,
                ratingMax = suite.ratingMax,
                limit = suite.puzzleCount
            )
            if (puzzles.isEmpty()) return@launch
            val newSuiteId = suiteRepository.createSuite(
                name = suite.name,
                themes = suite.themes,
                ratingMin = suite.ratingMin,
                ratingMax = suite.ratingMax,
                puzzleIds = puzzles.map { it.id }
            )
            onCreated(newSuiteId)
        }
    }
}
