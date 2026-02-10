package com.chesspuzzles.woodpecker.ui.detail

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chesspuzzles.woodpecker.ui.components.AppBackground
import com.chesspuzzles.woodpecker.ui.components.CycleLineChart
import com.chesspuzzles.woodpecker.ui.strings.LocalStrings
import com.chesspuzzles.woodpecker.ui.theme.CorrectGreen
import com.chesspuzzles.woodpecker.ui.theme.SuiteColors
import com.chesspuzzles.woodpecker.ui.theme.WrongRed
import com.chesspuzzles.woodpecker.util.TimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SuiteDetailScreen(
    onBack: () -> Unit,
    onContinueCycle: (suiteId: Long, cycleId: Long) -> Unit,
    onStartTraining: (suiteId: Long, cycleId: Long) -> Unit = onContinueCycle,
    onNavigateToSuite: (suiteId: Long) -> Unit,
    viewModel: SuiteDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showResetCycleDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text(strings.renameSuite) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text(strings.suiteName) }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            viewModel.renameSuite(renameText.trim())
                            showRenameDialog = false
                        }
                    }
                ) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    if (uiState.regenerateError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearRegenerateError() },
            title = { Text(strings.regenerateSuite) },
            text = { Text(uiState.regenerateError!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearRegenerateError() }) {
                    Text(strings.continueButton)
                }
            }
        )
    }

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

    if (showResetCycleDialog) {
        AlertDialog(
            onDismissRequest = { showResetCycleDialog = false },
            title = { Text(strings.resetCycle) },
            text = { Text(strings.resetCycleConfirm) },
            confirmButton = {
                TextButton(onClick = {
                    showResetCycleDialog = false
                    viewModel.resetActiveCycle()
                }) {
                    Text(strings.resetCycle, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetCycleDialog = false }) {
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
                    IconButton(onClick = {
                        renameText = uiState.suite?.name ?: ""
                        showRenameDialog = true
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = strings.renameSuite)
                    }
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
            val completedCycles = uiState.cycles
                .filter { it.cycle.completedAt != null && it.stats != null }
                .map { Pair(it.cycle, it.stats!!) }
            val hasActiveCycle = uiState.cycles.any { it.cycle.completedAt == null }
            val suiteAccentColor = SuiteColors[(suite.id % SuiteColors.size).toInt()]

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // 1. Chart + stat cards at top (like summary screen)
                if (completedCycles.isNotEmpty()) {
                    item {
                        var selectedIndex by remember { mutableIntStateOf(completedCycles.size - 1) }
                        val selectedCycle = completedCycles[selectedIndex]
                        val selectedStats = selectedCycle.second

                        // Compute deltas from previous cycle
                        val prevStats = if (selectedIndex > 0) completedCycles[selectedIndex - 1].second else null
                        val timeDelta = prevStats?.let { selectedStats.totalTimeMs - it.totalTimeMs }
                        val accuracyDelta = prevStats?.let { selectedStats.accuracy - it.accuracy }

                        Text(
                            text = "${strings.cycle} ${selectedCycle.first.cycleNumber}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stat cards row 1: Time + Accuracy
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = strings.totalTime,
                                value = TimeFormatter.formatMs(selectedStats.totalTimeMs),
                                delta = timeDelta?.let { TimeFormatter.formatDelta(it) },
                                deltaPositive = timeDelta?.let { it < 0 },
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = strings.accuracy,
                                value = "${(selectedStats.accuracy * 100).toInt()}%",
                                delta = accuracyDelta?.let {
                                    "${if (it >= 0) "+" else ""}${(it * 100).toInt()}%"
                                },
                                deltaPositive = accuracyDelta?.let { it >= 0 },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stat cards row 2: Solved + Avg Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = strings.solved,
                                value = "${selectedStats.solvedCount} / ${selectedStats.totalPuzzles}",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = strings.avgTime,
                                value = TimeFormatter.formatMsShort(selectedStats.averageTimeMs),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        CycleLineChart(
                            cycleStats = completedCycles,
                            highlightCycleId = selectedCycle.first.id,
                            onCycleClick = { cycleId ->
                                val index = completedCycles.indexOfFirst { it.first.id == cycleId }
                                if (index >= 0) selectedIndex = index
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                        )
                    }
                } else {
                    item {
                        Text(
                            text = strings.noCyclesYet,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }

                // 2. Start/continue training button
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
                            val progress = active.progress ?: 0
                            if (progress > 0) {
                                Text("${strings.continueCycle} (${progress + 1}/${suite.puzzleCount})")
                            } else {
                                Text(strings.startCycle)
                            }
                        } else {
                            Text(strings.startTraining)
                        }
                    }

                    if (hasActiveCycle) {
                        TextButton(
                            onClick = { showResetCycleDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                strings.resetCycle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 3. Suite info
                item {
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

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            OutlinedButton(
                onClick = {
                    viewModel.regenerateSuite(strings.regenerateNoPuzzles) { newSuiteId ->
                        onNavigateToSuite(newSuiteId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(strings.regenerateSuite)
            }
            }
        }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    delta: String? = null,
    deltaPositive: Boolean? = null,
    modifier: Modifier = Modifier
) {
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
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (delta != null) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (deltaPositive) {
                        true -> CorrectGreen.copy(alpha = 0.15f)
                        false -> WrongRed.copy(alpha = 0.15f)
                        null -> androidx.compose.ui.graphics.Color.Transparent
                    },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = delta,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = when (deltaPositive) {
                            true -> CorrectGreen
                            false -> WrongRed
                            null -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
