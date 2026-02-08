package com.chesspuzzles.woodpecker.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesspuzzles.woodpecker.data.repository.SuiteRepository
import com.chesspuzzles.woodpecker.domain.model.Cycle
import com.chesspuzzles.woodpecker.domain.model.CycleStats
import com.chesspuzzles.woodpecker.domain.model.Suite
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SuiteDetailUiState(
    val suite: Suite? = null,
    val cycles: List<Triple<Cycle, CycleStats?, Int?>> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class SuiteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val suiteRepository: SuiteRepository
) : ViewModel() {

    private val suiteId: Long = savedStateHandle["suiteId"]!!

    private val _uiState = MutableStateFlow(SuiteDetailUiState())
    val uiState: StateFlow<SuiteDetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val suite = suiteRepository.getSuiteById(suiteId)
            val cycles = suiteRepository.getCyclesForSuite(suiteId)
            val cyclesWithStats = cycles.map { cycle ->
                val stats = suiteRepository.getCycleStats(cycle.id)
                val progress = if (cycle.completedAt == null) {
                    suiteRepository.getAttemptCountForCycle(cycle.id)
                } else null
                Triple(cycle, stats, progress)
            }

            _uiState.update {
                it.copy(
                    suite = suite,
                    cycles = cyclesWithStats,
                    isLoading = false
                )
            }
        }
    }

    fun deleteSuite(onDeleted: () -> Unit) {
        viewModelScope.launch {
            suiteRepository.deleteSuite(suiteId)
            onDeleted()
        }
    }

}
