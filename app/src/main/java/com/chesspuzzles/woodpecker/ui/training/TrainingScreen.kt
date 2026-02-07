package com.chesspuzzles.woodpecker.ui.training

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chesspuzzles.woodpecker.ui.components.PuzzleProgressBar
import com.chesspuzzles.woodpecker.ui.components.TimerDisplay
import com.chesspuzzles.woodpecker.ui.components.chessboard.ChessBoard
import com.chesspuzzles.woodpecker.ui.theme.CorrectGreen
import com.chesspuzzles.woodpecker.ui.theme.WrongRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingScreen(
    onBack: () -> Unit,
    onCycleComplete: (cycleId: Long) -> Unit,
    viewModel: TrainingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onCycleComplete(viewModel.getCycleId())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Training") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Progress + Puzzle Navigation + Timer Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.onNavigateToPuzzle(uiState.displayedPuzzleIndex - 1)
                            },
                            enabled = uiState.displayedPuzzleIndex > 0,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous puzzle"
                            )
                        }
                        PuzzleProgressBar(
                            current = uiState.displayedPuzzleIndex + 1,
                            total = uiState.totalPuzzles,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                if (uiState.isReviewingPastPuzzle &&
                                    uiState.displayedPuzzleIndex + 1 == uiState.currentPuzzleIndex
                                ) {
                                    viewModel.onReturnToLivePuzzle()
                                } else {
                                    viewModel.onNavigateToPuzzle(uiState.displayedPuzzleIndex + 1)
                                }
                            },
                            enabled = uiState.displayedPuzzleIndex < uiState.currentPuzzleIndex,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next puzzle"
                            )
                        }
                    }
                    TimerDisplay(
                        startTimeMs = uiState.timerStartMs,
                        running = uiState.timerRunning,
                        modifier = Modifier.padding(start = 16.dp),
                        offsetMs = uiState.timerOffsetMs
                    )
                }

                // Correct / Wrong counter
                if (uiState.correctCount > 0 || uiState.wrongCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${uiState.correctCount} correct",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CorrectGreen
                        )
                        Text(
                            text = " / ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${uiState.wrongCount} wrong",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WrongRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Side to play / Review indicator + Puzzle ID
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.isReviewingPastPuzzle) {
                        Text(
                            text = "Reviewing",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "${uiState.sideToPlay} to play",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (uiState.currentPuzzleId.isNotEmpty()) {
                        Text(
                            text = "Lichess #${uiState.currentPuzzleId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chess Board
                ChessBoard(
                    boardState = viewModel.boardState,
                    enabled = uiState.boardEnabled,
                    onMoveAttempt = { from, to ->
                        viewModel.onUserMove(from, to)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Move History Navigation (always visible when there are moves)
                if (uiState.moveHistorySize > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = viewModel::onMoveHistoryBack,
                            enabled = uiState.moveHistoryIndex > 0
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous move"
                            )
                        }
                        Text(
                            text = "Move ${uiState.moveHistoryIndex} / ${uiState.moveHistorySize}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        IconButton(
                            onClick = viewModel::onMoveHistoryForward,
                            enabled = uiState.moveHistoryIndex < uiState.moveHistorySize
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next move"
                            )
                        }
                    }
                }

                // Review mode: return to current puzzle button
                if (uiState.isReviewingPastPuzzle) {
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(onClick = viewModel::onReturnToLivePuzzle) {
                        Text("Return to current puzzle")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Feedback area
                when (uiState.result) {
                    PuzzleResult.CORRECT -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    CorrectGreen.copy(alpha = 0.2f),
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Correct!",
                                style = MaterialTheme.typography.titleLarge,
                                color = CorrectGreen
                            )
                        }
                    }
                    PuzzleResult.WRONG -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        WrongRed.copy(alpha = 0.2f),
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Incorrect",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = WrongRed
                                )
                            }
                            if (uiState.showContinueButton) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = viewModel::onContinueAfterWrong) {
                                    Text("Continue")
                                }
                            }
                        }
                    }
                    PuzzleResult.NONE -> {
                        // Empty space placeholder
                        Spacer(modifier = Modifier.height(56.dp))
                    }
                }
            }
        }
    }
}
