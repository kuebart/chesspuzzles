package com.chesspuzzles.woodpecker.ui.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chesspuzzles.woodpecker.domain.model.Cycle
import com.chesspuzzles.woodpecker.domain.model.CycleStats
import com.chesspuzzles.woodpecker.util.TimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SuiteDetailScreen(
    onBack: () -> Unit,
    onStartTraining: (suiteId: Long, cycleId: Long) -> Unit,
    onCycleClick: (cycleId: Long) -> Unit,
    viewModel: SuiteDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Suite") },
            text = { Text("Are you sure you want to delete this suite and all its cycles?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteSuite(onBack)
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.suite?.name ?: "Suite") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Suite")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val suite = uiState.suite ?: return@Scaffold

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Suite Info
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${suite.puzzleCount} puzzles",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Rating: ${suite.ratingMin} - ${suite.ratingMax}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (suite.themes.isNotEmpty()) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    suite.themes.forEach { theme ->
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(theme.displayName) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    val progress = uiState.activeCycleProgress
                    Button(
                        onClick = { viewModel.startTraining(onStartTraining) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        if (progress != null && progress > 0) {
                            Text("Continue ($progress/${suite.puzzleCount})")
                        } else {
                            Text("Start Training")
                        }
                    }
                    if (progress != null && progress > 0) {
                        LinearProgressIndicator(
                            progress = { progress.toFloat() / suite.puzzleCount },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                }

                // Progress chart
                val completedCycles = uiState.cycles.filter { it.second != null }
                if (completedCycles.size > 1) {
                    item {
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        ProgressChart(
                            cycleStats = completedCycles,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .padding(top = 8.dp)
                        )
                    }
                }

                // Cycles list
                item {
                    Text(
                        text = "Cycles",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (uiState.cycles.isEmpty()) {
                    item {
                        Text(
                            text = "No cycles yet. Start training!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }

                items(uiState.cycles.reversed()) { (cycle, stats) ->
                    CycleRow(
                        cycle = cycle,
                        stats = stats,
                        onClick = { onCycleClick(cycle.id) }
                    )
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun CycleRow(
    cycle: Cycle,
    stats: CycleStats?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Cycle ${cycle.cycleNumber}",
                    style = MaterialTheme.typography.titleSmall
                )
                if (cycle.completedAt == null) {
                    Text(
                        text = "In progress",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (stats != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = TimeFormatter.formatMs(stats.totalTimeMs),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Time",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${(stats.accuracy * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Accuracy",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressChart(
    cycleStats: List<Pair<Cycle, CycleStats?>>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val completedStats = cycleStats.mapNotNull { it.second }
        if (completedStats.size < 2) return@Canvas

        val maxTime = completedStats.maxOf { it.totalTimeMs }.toFloat()
        val minTime = completedStats.minOf { it.totalTimeMs }.toFloat()
        val range = (maxTime - minTime).coerceAtLeast(1f)

        val path = Path()
        val stepX = size.width / (completedStats.size - 1).coerceAtLeast(1)

        completedStats.forEachIndexed { index, stats ->
            val x = index * stepX
            val y = size.height - ((stats.totalTimeMs - minTime) / range) * size.height * 0.8f - size.height * 0.1f

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            // Draw point
            drawCircle(
                color = primaryColor,
                radius = 6f,
                center = Offset(x, y)
            )
        }

        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 3f)
        )
    }
}
