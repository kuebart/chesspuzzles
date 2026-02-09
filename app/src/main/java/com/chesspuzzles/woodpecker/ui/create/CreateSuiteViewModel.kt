package com.chesspuzzles.woodpecker.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesspuzzles.woodpecker.data.repository.PuzzleRepository
import com.chesspuzzles.woodpecker.data.repository.SuiteRepository
import com.chesspuzzles.woodpecker.domain.model.PuzzleTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateSuiteUiState(
    val name: String = "",
    val selectedThemes: Set<PuzzleTheme> = emptySet(),
    val ratingMin: Int = 1000,
    val ratingMax: Int = 2000,
    val puzzleCount: Int = 200,
    val matchingPuzzleCount: Int = 0,
    val isCreating: Boolean = false,
    val error: String? = null,
    val noPuzzlesFound: Boolean = false
)

@HiltViewModel
class CreateSuiteViewModel @Inject constructor(
    private val puzzleRepository: PuzzleRepository,
    private val suiteRepository: SuiteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateSuiteUiState())
    val uiState: StateFlow<CreateSuiteUiState> = _uiState.asStateFlow()

    private var countJob: Job? = null

    init {
        updateMatchingCount()
    }

    fun setName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun toggleTheme(theme: PuzzleTheme) {
        _uiState.update { state ->
            val newThemes = if (theme in state.selectedThemes) {
                state.selectedThemes - theme
            } else {
                state.selectedThemes + theme
            }
            state.copy(selectedThemes = newThemes)
        }
        updateMatchingCount()
    }

    fun setRatingRange(min: Int, max: Int) {
        _uiState.update { it.copy(ratingMin = min, ratingMax = max) }
        updateMatchingCount()
    }

    fun setPuzzleCount(count: Int) {
        _uiState.update { it.copy(puzzleCount = count) }
    }

    private fun updateMatchingCount() {
        countJob?.cancel()
        countJob = viewModelScope.launch {
            delay(300) // Debounce
            val state = _uiState.value
            val count = puzzleRepository.countMatchingPuzzles(
                themes = state.selectedThemes.toList(),
                ratingMin = state.ratingMin,
                ratingMax = state.ratingMax
            )
            _uiState.update { it.copy(matchingPuzzleCount = count) }
        }
    }

    private suspend fun generateAutoName(): String {
        val count = suiteRepository.getSuiteCount()
        return "Suite ${count + 1}"
    }

    fun createSuite(onSuccess: (Long) -> Unit) {
        val state = _uiState.value

        _uiState.update { it.copy(isCreating = true, error = null, noPuzzlesFound = false) }

        viewModelScope.launch {
            try {
                val name = state.name.ifBlank { generateAutoName() }

                val puzzles = puzzleRepository.findPuzzles(
                    themes = state.selectedThemes.toList(),
                    ratingMin = state.ratingMin,
                    ratingMax = state.ratingMax,
                    limit = state.puzzleCount
                )

                if (puzzles.isEmpty()) {
                    _uiState.update { it.copy(isCreating = false, noPuzzlesFound = true) }
                    return@launch
                }

                val suiteId = suiteRepository.createSuite(
                    name = name,
                    themes = state.selectedThemes.toList(),
                    ratingMin = state.ratingMin,
                    ratingMax = state.ratingMax,
                    puzzleIds = puzzles.map { it.id }
                )

                _uiState.update { it.copy(isCreating = false) }
                onSuccess(suiteId)
            } catch (e: Exception) {
                _uiState.update { it.copy(isCreating = false, error = e.message) }
            }
        }
    }
}
