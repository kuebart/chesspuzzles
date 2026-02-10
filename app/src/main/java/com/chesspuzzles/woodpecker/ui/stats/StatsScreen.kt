package com.chesspuzzles.woodpecker.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chesspuzzles.woodpecker.ui.components.AppBackground
import com.chesspuzzles.woodpecker.ui.strings.LocalStrings
import com.chesspuzzles.woodpecker.util.TimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalStrings.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.statisticsTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        AppBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Row 1: Solved total + Solved today
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        value = "${uiState.totalSolved}",
                        label = strings.puzzlesSolved,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = "${uiState.solvedToday}",
                        label = strings.statsSolvedToday,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: Total time + Avg per puzzle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        value = TimeFormatter.formatMs(uiState.totalTrainingTimeMs),
                        label = strings.statsTotalTime,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = TimeFormatter.formatMsShort(uiState.avgTimePerPuzzle),
                        label = strings.statsAvgTimePerPuzzle,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3: Avg accuracy + Best accuracy
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        value = if (uiState.completedCycles > 0)
                            "${(uiState.averageAccuracy * 100).toInt()}%" else "–",
                        label = strings.statsAvgAccuracy,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = if (uiState.completedCycles > 0)
                            "${(uiState.bestAccuracy * 100).toInt()}%" else "–",
                        label = strings.statsBestAccuracy,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 4: Cycles + Suites
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        value = "${uiState.completedCycles}",
                        label = strings.statsCompletedCycles,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = "${uiState.suiteCount}",
                        label = strings.statsSuites,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 5: Training days + Streak
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        value = "${uiState.trainingDays}",
                        label = strings.trainingDays,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = if (uiState.currentStreak > 0)
                            "${uiState.currentStreak} ${strings.statsDays}" else "–",
                        label = strings.statsCurrentStreak,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
