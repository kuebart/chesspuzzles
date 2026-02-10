package com.chesspuzzles.woodpecker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesspuzzles.woodpecker.data.repository.PuzzleRepository
import com.chesspuzzles.woodpecker.data.repository.SuiteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StatsUiState(
    val totalSolved: Int = 0,
    val trainingDays: Int = 0
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    puzzleRepository: PuzzleRepository,
    suiteRepository: SuiteRepository
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        puzzleRepository.observeTotalSolvedCount(),
        suiteRepository.observeTrainingDays()
    ) { totalSolved, trainingDays ->
        StatsUiState(
            totalSolved = totalSolved,
            trainingDays = trainingDays
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )
}
