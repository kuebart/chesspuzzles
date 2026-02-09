package com.chesspuzzles.woodpecker.ui.detail

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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chesspuzzles.woodpecker.domain.model.Cycle
import com.chesspuzzles.woodpecker.domain.model.CycleStats
import com.chesspuzzles.woodpecker.ui.components.AppBackground
import com.chesspuzzles.woodpecker.ui.components.CycleLineChart
import com.chesspuzzles.woodpecker.ui.strings.LocalStrings
import com.chesspuzzles.woodpecker.ui.theme.SuiteColors
import com.chesspuzzles.woodpecker.util.TimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SuiteDetailScreen(
    onBack: () -> Unit,
    onCycleClick: (cycleId: Long) -> Unit,
    onContinueCycle: (suiteId: Long, cycleId: Long) -> Unit,
    onStartTraining: (suiteId: Long, cycleId: Long) -> Unit = onContinueCycle,
    viewModel: SuiteDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(strings.deleteSuiteTitle) },
            text = { Text(strings.deleteSuiteConfirm) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteSuite(onBack)
                }) {
                    Text(strings.delete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.suite?.name ?: strings.suiteDefault) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = strings.deleteSuiteTitle)
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
            val suite = uiState.suite ?: return@AppBackground

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
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${suite.puzzleCount} ${strings.puzzlesCount}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${strings.ratingLabel}: ${suite.ratingMin} - ${suite.ratingMax}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (suite.themes.isNotEmpty()) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    suite.themes.forEach { theme ->
                                        val themeName = strings.themeDisplayNames[theme] ?: theme.displayName
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(themeName) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Start Training button
                val hasActiveCycle = uiState.cycles.any { it.cycle.completedAt == null }
                val suiteAccentColor = SuiteColors[(suite.id % SuiteColors.size).toInt()]
                item {
                    Button(
                        onClick = {
                            if (hasActiveCycle) {
                                val active = uiState.cycles.first { it.cycle.completedAt == null }
                                onContinueCycle(suite.id, active.cycle.id)
                            } else {
                                scope.launch {
                                    val cycleId = viewModel.getOrCreateCycle()
                                    onStartTraining(suite.id, cycleId)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = suiteAccentColor)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        if (hasActiveCycle) {
                            val active = uiState.cycles.first { it.cycle.completedAt == null }
                            Text("${strings.continueCycle} (${(active.progress ?: 0) + 1}/${suite.puzzleCount})")
                        } else {
                            Text(strings.startTraining)
                        }
                    }
                }

                // Progress chart
                val completedCycles = uiState.cycles
                    .filter { it.stats != null }
                    .map { Pair(it.cycle, it.stats!!) }
                if (completedCycles.size > 1) {
                    item {
                        Text(
                            text = strings.progress,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        CycleLineChart(
                            cycleStats = completedCycles,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                                .padding(top = 8.dp)
                        )
                    }
                }

                // Cycles list
                item {
                    Text(
                        text = strings.cycles,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (uiState.cycles.isEmpty()) {
                    item {
                        Text(
                            text = strings.noCyclesYet,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }

                items(uiState.cycles.reversed()) { item ->
                    val cycle = item.cycle
                    val stats = item.stats
                    val progress = item.progress
                    CycleRow(
                        cycle = cycle,
                        stats = stats,
                        progress = progress,
                        totalPuzzles = suite.puzzleCount,
                        onClick = {
                            if (cycle.completedAt == null) {
                                onContinueCycle(suite.id, cycle.id)
                            } else {
                                onCycleClick(cycle.id)
                            }
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
        }
    }
}

@Composable
private fun CycleRow(
    cycle: Cycle,
    stats: CycleStats?,
    progress: Int?,
    totalPuzzles: Int,
    onClick: () -> Unit
) {
    val strings = LocalStrings.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
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
                    text = "${strings.cycle} ${cycle.cycleNumber}",
                    style = MaterialTheme.typography.titleSmall
                )
                if (cycle.completedAt == null && progress != null) {
                    Text(
                        text = "${strings.taskProgress} ${progress + 1} / $totalPuzzles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (stats != null) {
                    Text(
                        text = "${(stats.accuracy * 100).toInt()}% ${strings.correct}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            text = strings.time,
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
                            text = strings.accuracy,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

