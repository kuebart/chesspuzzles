package com.chesspuzzles.woodpecker.ui.training

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
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
            val hp = Modifier.padding(horizontal = 16.dp)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Timer
                TimerDisplay(
                    startTimeMs = uiState.timerStartMs,
                    running = uiState.timerRunning,
                    offsetMs = uiState.timerOffsetMs
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Puzzle Navigation (centered)
                Row(
                    modifier = Modifier.fillMaxWidth().then(hp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            viewModel.onNavigateToPuzzle(uiState.displayedPuzzleIndex - 1)
                        },
                        enabled = uiState.displayedPuzzleIndex > 0
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous puzzle",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    PuzzleProgressBar(
                        current = uiState.displayedPuzzleIndex + 1,
                        total = uiState.totalPuzzles,
                        modifier = Modifier.weight(1f, fill = false)
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
                        enabled = uiState.displayedPuzzleIndex < uiState.currentPuzzleIndex
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next puzzle",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Correct / Wrong counter
                if (uiState.correctCount > 0 || uiState.wrongCount > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().then(hp),
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

                Spacer(modifier = Modifier.height(16.dp))

                // Side to play / Review indicator + Puzzle ID
                Row(
                    modifier = Modifier.fillMaxWidth().then(hp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.isReviewingPastPuzzle) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Reviewing",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = "${uiState.sideToPlay} to play",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (uiState.currentPuzzleId.isNotEmpty()) {
                        Text(
                            text = "Lichess #${uiState.currentPuzzleId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Chess Board - full width, no horizontal padding
                ChessBoard(
                    boardState = viewModel.boardState,
                    enabled = uiState.boardEnabled,
                    onMoveAttempt = { from, to ->
                        viewModel.onUserMove(from, to)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Move History Navigation (visible when user has made moves)
                if (uiState.moveHistorySize > 1) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().then(hp)
                    ) {
                        IconButton(
                            onClick = viewModel::onMoveHistoryBack,
                            enabled = uiState.moveHistoryIndex > 1
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous move",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = "${uiState.moveHistoryIndex / 2} / ${uiState.moveHistorySize / 2}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        IconButton(
                            onClick = viewModel::onMoveHistoryForward,
                            enabled = uiState.moveHistoryIndex < uiState.moveHistorySize
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next move",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // Review mode: return to current puzzle button
                if (uiState.isReviewingPastPuzzle) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = viewModel::onReturnToLivePuzzle) {
                        Text("Return to current puzzle")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Feedback area
                when (uiState.result) {
                    PuzzleResult.CORRECT -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().then(hp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = CorrectGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text(
                                text = "Correct!",
                                style = MaterialTheme.typography.titleMedium,
                                color = CorrectGreen
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Button(onClick = viewModel::onContinue) {
                                Text("Continue")
                            }
                        }
                    }
                    PuzzleResult.WRONG -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().then(hp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = WrongRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text(
                                text = "Incorrect",
                                style = MaterialTheme.typography.titleMedium,
                                color = WrongRed
                            )
                            if (uiState.showContinueButton) {
                                Spacer(modifier = Modifier.weight(1f))
                                Button(onClick = viewModel::onContinue) {
                                    Text("Continue")
                                }
                            }
                        }
                    }
                    PuzzleResult.NONE -> {
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
        }
    }
}
