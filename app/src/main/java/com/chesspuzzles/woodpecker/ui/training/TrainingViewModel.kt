package com.chesspuzzles.woodpecker.ui.training

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chesspuzzles.woodpecker.data.repository.PuzzleRepository
import com.chesspuzzles.woodpecker.data.repository.SuiteRepository
import com.chesspuzzles.woodpecker.domain.model.Puzzle
import com.chesspuzzles.woodpecker.ui.components.chessboard.BoardState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PuzzleResult {
    NONE, CORRECT, WRONG
}

data class TrainingUiState(
    val currentPuzzleIndex: Int = 0,
    val totalPuzzles: Int = 0,
    val isLoading: Boolean = true,
    val boardEnabled: Boolean = false,
    val result: PuzzleResult = PuzzleResult.NONE,
    val timerStartMs: Long = 0,
    val timerRunning: Boolean = false,
    val timerOffsetMs: Long = 0,
    val sideToPlay: String = "White",
    val isComplete: Boolean = false,
    val showContinueButton: Boolean = false,
    val currentPuzzleId: String = "",
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val moveHistoryIndex: Int = 0,
    val moveHistorySize: Int = 0,
    val displayedPuzzleIndex: Int = 0,
    val isReviewingPastPuzzle: Boolean = false
)

@HiltViewModel
class TrainingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val puzzleRepository: PuzzleRepository,
    private val suiteRepository: SuiteRepository
) : ViewModel() {

    private val suiteId: Long = savedStateHandle["suiteId"]!!
    private val cycleId: Long = savedStateHandle["cycleId"]!!

    private val _uiState = MutableStateFlow(TrainingUiState())
    val uiState: StateFlow<TrainingUiState> = _uiState.asStateFlow()

    val boardState = BoardState()

    private var puzzles: List<Puzzle> = emptyList()
    private var currentMoveIndex: Int = 0
    private var puzzleStartTime: Long = 0

    // Unified move history state
    private var puzzleBaseFen: String = ""
    private var moveHistory: MutableList<String> = mutableListOf()
    private var livePuzzleIndex: Int = 0
    private var savedLiveState: SavedLiveState? = null

    private data class SavedLiveState(
        val currentMoveIndex: Int,
        val moveHistory: List<String>,
        val moveHistoryIndex: Int,
        val puzzleStartTime: Long,
        val result: PuzzleResult,
        val showContinueButton: Boolean
    )

    init {
        loadPuzzles()
    }

    private fun pauseTimer() {
        val state = _uiState.value
        if (!state.timerRunning) return
        val elapsed = System.currentTimeMillis() - state.timerStartMs + state.timerOffsetMs
        _uiState.update {
            it.copy(timerRunning = false, timerOffsetMs = elapsed)
        }
    }

    private fun resumeTimer() {
        val state = _uiState.value
        if (state.timerRunning) return
        _uiState.update {
            it.copy(timerRunning = true, timerStartMs = System.currentTimeMillis())
        }
    }

    private fun loadPuzzles() {
        viewModelScope.launch {
            puzzles = puzzleRepository.getPuzzlesForSuite(suiteId)
            val alreadyAttempted = suiteRepository.getAttemptCountForCycle(cycleId)
            val startIndex = alreadyAttempted.coerceAtMost(puzzles.size - 1).coerceAtLeast(0)
            val timerOffset = if (alreadyAttempted > 0) {
                suiteRepository.getAccumulatedTimeForCycle(cycleId)
            } else 0L

            _uiState.update {
                it.copy(
                    totalPuzzles = puzzles.size,
                    currentPuzzleIndex = startIndex,
                    isLoading = false,
                    timerStartMs = System.currentTimeMillis(),
                    timerRunning = true,
                    timerOffsetMs = timerOffset
                )
            }

            livePuzzleIndex = startIndex

            if (puzzles.isNotEmpty() && startIndex < puzzles.size) {
                loadCurrentPuzzle()
            }
        }
    }

    private suspend fun loadCurrentPuzzle() {
        val index = _uiState.value.currentPuzzleIndex
        if (index >= puzzles.size) {
            completeCycle()
            return
        }

        val puzzle = puzzles[index]
        currentMoveIndex = 0
        puzzleStartTime = System.currentTimeMillis()
        livePuzzleIndex = index

        // Initialize move history
        puzzleBaseFen = puzzle.fen
        moveHistory.clear()

        // Load position
        boardState.loadFen(puzzle.fen)

        // Determine if board should be flipped (player plays the side that responds)
        val playerIsBlack = puzzle.fen.contains(" w ") // If white moves first in FEN, opponent is white, player is black
        boardState.flipped = playerIsBlack

        _uiState.update {
            it.copy(
                boardEnabled = false,
                result = PuzzleResult.NONE,
                sideToPlay = if (playerIsBlack) "Black" else "White",
                showContinueButton = false,
                currentPuzzleId = puzzle.id,
                displayedPuzzleIndex = index,
                isReviewingPastPuzzle = false,
                moveHistoryIndex = 0,
                moveHistorySize = 0
            )
        }

        // Play the opponent's first move with a delay
        delay(500)
        val opponentMove = puzzle.moves[0]
        boardState.makeMoveUci(opponentMove)
        currentMoveIndex = 1

        // Add opponent's first move to history
        moveHistory.add(opponentMove)

        _uiState.update {
            it.copy(
                boardEnabled = true,
                moveHistoryIndex = 1,
                moveHistorySize = 1
            )
        }
    }

    fun onUserMove(fromSquare: com.github.bhlangonijr.chesslib.Square, toSquare: com.github.bhlangonijr.chesslib.Square) {
        if (!_uiState.value.boardEnabled) return

        val puzzle = puzzles.getOrNull(_uiState.value.currentPuzzleIndex) ?: return
        val expectedMove = puzzle.moves.getOrNull(currentMoveIndex) ?: return

        // Build UCI string from squares
        val userUci = "${fromSquare.value().lowercase()}${toSquare.value().lowercase()}"

        // Check if the move matches (considering promotions - default to queen)
        val expectedFrom = expectedMove.substring(0, 2)
        val expectedTo = expectedMove.substring(2, 4)

        if (userUci == "$expectedFrom$expectedTo" || userUci == expectedMove) {
            // Correct move
            boardState.makeMoveUci(expectedMove)
            currentMoveIndex++
            moveHistory.add(expectedMove)

            _uiState.update {
                it.copy(
                    moveHistoryIndex = moveHistory.size,
                    moveHistorySize = moveHistory.size
                )
            }

            if (currentMoveIndex >= puzzle.moves.size) {
                // Puzzle solved!
                onPuzzleSolved()
            } else {
                // Play opponent's response
                viewModelScope.launch {
                    _uiState.update { it.copy(boardEnabled = false) }
                    delay(400)
                    val nextMove = puzzle.moves[currentMoveIndex]
                    boardState.makeMoveUci(nextMove)
                    currentMoveIndex++
                    moveHistory.add(nextMove)

                    _uiState.update {
                        it.copy(
                            boardEnabled = true,
                            moveHistoryIndex = moveHistory.size,
                            moveHistorySize = moveHistory.size
                        )
                    }
                }
            }
        } else {
            // Wrong move
            onPuzzleFailed()
        }
    }

    private fun onPuzzleSolved() {
        val timeMs = System.currentTimeMillis() - puzzleStartTime
        val puzzle = puzzles[_uiState.value.currentPuzzleIndex]

        viewModelScope.launch {
            suiteRepository.saveAttempt(
                cycleId = cycleId,
                puzzleId = puzzle.id,
                solved = true,
                timeMs = timeMs
            )

            pauseTimer()

            _uiState.update {
                it.copy(
                    boardEnabled = false,
                    result = PuzzleResult.CORRECT,
                    showContinueButton = true,
                    correctCount = it.correctCount + 1
                )
            }
        }
    }

    private fun onPuzzleFailed() {
        val timeMs = System.currentTimeMillis() - puzzleStartTime
        val puzzle = puzzles[_uiState.value.currentPuzzleIndex]

        viewModelScope.launch {
            suiteRepository.saveAttempt(
                cycleId = cycleId,
                puzzleId = puzzle.id,
                solved = false,
                timeMs = timeMs
            )

            // Append remaining solution moves to history
            val remainingMoves = puzzle.moves.subList(currentMoveIndex, puzzle.moves.size)
            moveHistory.addAll(remainingMoves)

            pauseTimer()

            _uiState.update {
                it.copy(
                    boardEnabled = false,
                    result = PuzzleResult.WRONG,
                    showContinueButton = true,
                    wrongCount = it.wrongCount + 1,
                    moveHistorySize = moveHistory.size
                    // moveHistoryIndex stays where it is — user can navigate forward to see solution
                )
            }
        }
    }

    fun onMoveHistoryBack() {
        val state = _uiState.value
        if (state.moveHistoryIndex <= 1) return

        val newIndex = state.moveHistoryIndex - 1
        reconstructBoard(newIndex)
        _uiState.update {
            it.copy(
                moveHistoryIndex = newIndex,
                boardEnabled = false
            )
        }
    }

    fun onMoveHistoryForward() {
        val state = _uiState.value
        if (state.moveHistoryIndex >= state.moveHistorySize) return

        val move = moveHistory[state.moveHistoryIndex]
        boardState.makeMoveUci(move)
        val newIndex = state.moveHistoryIndex + 1

        // Re-enable board only if we're back at the live end and puzzle is still active
        val atLiveEnd = newIndex == moveHistory.size
        val puzzleActive = state.result == PuzzleResult.NONE && !state.isReviewingPastPuzzle

        _uiState.update {
            it.copy(
                moveHistoryIndex = newIndex,
                boardEnabled = atLiveEnd && puzzleActive
            )
        }
    }

    fun onNavigateToPuzzle(index: Int) {
        if (index < 0 || index >= puzzles.size) return
        val state = _uiState.value

        // If currently on the live puzzle (not already reviewing), save state
        if (!state.isReviewingPastPuzzle) {
            savedLiveState = SavedLiveState(
                currentMoveIndex = currentMoveIndex,
                moveHistory = moveHistory.toList(),
                moveHistoryIndex = state.moveHistoryIndex,
                puzzleStartTime = puzzleStartTime,
                result = state.result,
                showContinueButton = state.showContinueButton
            )
        }

        // Load the target puzzle for review
        val puzzle = puzzles[index]
        puzzleBaseFen = puzzle.fen
        moveHistory = puzzle.moves.toMutableList()

        boardState.loadFen(puzzle.fen)

        val playerIsBlack = puzzle.fen.contains(" w ")
        boardState.flipped = playerIsBlack

        _uiState.update {
            it.copy(
                boardEnabled = false,
                isReviewingPastPuzzle = true,
                displayedPuzzleIndex = index,
                moveHistoryIndex = 0,
                moveHistorySize = puzzle.moves.size,
                currentPuzzleId = puzzle.id,
                sideToPlay = if (playerIsBlack) "Black" else "White",
                result = PuzzleResult.NONE,
                showContinueButton = false
            )
        }
    }

    fun onReturnToLivePuzzle() {
        val saved = savedLiveState ?: return

        // Restore live puzzle state
        val puzzle = puzzles[livePuzzleIndex]
        puzzleBaseFen = puzzle.fen
        moveHistory = saved.moveHistory.toMutableList()
        currentMoveIndex = saved.currentMoveIndex
        puzzleStartTime = saved.puzzleStartTime

        // Reconstruct board to the saved position
        reconstructBoard(saved.moveHistoryIndex)

        val playerIsBlack = puzzle.fen.contains(" w ")
        boardState.flipped = playerIsBlack

        val atLiveEnd = saved.moveHistoryIndex == moveHistory.size
        val puzzleActive = saved.result == PuzzleResult.NONE

        _uiState.update {
            it.copy(
                boardEnabled = atLiveEnd && puzzleActive,
                isReviewingPastPuzzle = false,
                displayedPuzzleIndex = livePuzzleIndex,
                moveHistoryIndex = saved.moveHistoryIndex,
                moveHistorySize = moveHistory.size,
                currentPuzzleId = puzzle.id,
                sideToPlay = if (playerIsBlack) "Black" else "White",
                result = saved.result,
                showContinueButton = saved.showContinueButton
            )
        }

        savedLiveState = null
    }

    private fun reconstructBoard(targetIndex: Int) {
        boardState.loadFen(puzzleBaseFen)
        for (i in 0 until targetIndex) {
            boardState.makeMoveUci(moveHistory[i])
        }
    }

    fun onContinue() {
        viewModelScope.launch {
            advanceToNext()
        }
    }

    private suspend fun advanceToNext() {
        val nextIndex = _uiState.value.currentPuzzleIndex + 1
        if (nextIndex >= puzzles.size) {
            completeCycle()
        } else {
            resumeTimer()
            _uiState.update { it.copy(currentPuzzleIndex = nextIndex) }
            loadCurrentPuzzle()
        }
    }

    private suspend fun completeCycle() {
        suiteRepository.completeCycle(cycleId)
        _uiState.update {
            it.copy(
                isComplete = true,
                timerRunning = false,
                boardEnabled = false
            )
        }
    }

    fun getCycleId(): Long = cycleId
}
