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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
    val isLoading: Boolean = true,
    val regenerateError: String? = null
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

    private val _regenerateError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SuiteDetailUiState> = combine(
        suiteRepository.observeSuiteById(suiteId),
        suiteRepository.observeCyclesForSuite(suiteId),
        _regenerateError
    ) { suite, cycles, regError ->
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
            isLoading = false,
            regenerateError = regError
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

    fun regenerateSuite(noPuzzlesMessage: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            _regenerateError.value = null
            val suite = uiState.value.suite ?: return@launch
            val existingIds = puzzleRepository.getPuzzlesForSuite(suiteId).map { it.id }
            val puzzles = puzzleRepository.findPuzzlesExcluding(
                themes = suite.themes,
                ratingMin = suite.ratingMin,
                ratingMax = suite.ratingMax,
                limit = suite.puzzleCount,
                excludeIds = existingIds
            )
            if (puzzles.isEmpty()) {
                _regenerateError.value = noPuzzlesMessage
                return@launch
            }
            val groupId = suiteRepository.ensureGroupId(suiteId)
            val nextVersion = suiteRepository.getNextVersionInGroup(groupId)
            val newSuiteId = suiteRepository.createSuite(
                name = suite.name,
                themes = suite.themes,
                ratingMin = suite.ratingMin,
                ratingMax = suite.ratingMax,
                puzzleIds = puzzles.map { it.id },
                version = nextVersion,
                groupId = groupId
            )
            onCreated(newSuiteId)
        }
    }

    fun clearRegenerateError() {
        _regenerateError.value = null
    }
}
