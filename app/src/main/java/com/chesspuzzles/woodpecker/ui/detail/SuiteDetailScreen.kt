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
import androidx.compose.ui.text.style.TextAlign
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

                }

                // Progress chart with cycle stats
                val completedCycles = uiState.cycles
                    .filter { it.stats != null }
                    .map { Pair(it.cycle, it.stats!!) }
                if (completedCycles.size > 1) {
                    item {
                        var selectedIndex by remember { mutableIntStateOf(completedCycles.size - 1) }
                        val selectedCycle = completedCycles[selectedIndex]
                        val selectedStats = selectedCycle.second

                        Text(
                            text = strings.progress,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
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
                                .padding(top = 8.dp)
                        )

                        // Compute deltas from previous cycle in list
                        val prevStats = if (selectedIndex > 0) completedCycles[selectedIndex - 1].second else null
                        val timeDelta = prevStats?.let { selectedStats.totalTimeMs - it.totalTimeMs }
                        val accuracyDelta = prevStats?.let { selectedStats.accuracy - it.accuracy }
                        val errorsDelta = prevStats?.let {
                            (selectedStats.totalPuzzles - selectedStats.solvedCount) - (it.totalPuzzles - it.solvedCount)
                        }

                        // Selected cycle stats
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${strings.cycle} ${selectedCycle.first.cycleNumber}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = TimeFormatter.formatMs(selectedStats.totalTimeMs),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = strings.time,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = timeDelta?.let { TimeFormatter.formatDelta(it) } ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (timeDelta != null && timeDelta < 0) CorrectGreen
                                               else if (timeDelta != null && timeDelta > 0) WrongRed
                                               else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${(selectedStats.accuracy * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = strings.accuracy,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = accuracyDelta?.let {
                                            "${if (it >= 0) "+" else ""}${(it * 100).toInt()}%"
                                        } ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (accuracyDelta != null && accuracyDelta > 0) CorrectGreen
                                               else if (accuracyDelta != null && accuracyDelta < 0) WrongRed
                                               else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${selectedStats.totalPuzzles - selectedStats.solvedCount}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = strings.chartLegendErrors,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = errorsDelta?.let {
                                            "${if (it >= 0) "+" else ""}$it"
                                        } ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (errorsDelta != null && errorsDelta < 0) CorrectGreen
                                               else if (errorsDelta != null && errorsDelta > 0) WrongRed
                                               else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                } else if (completedCycles.isEmpty()) {
                    item {
                        Text(
                            text = strings.noCyclesYet,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.regenerateSuite(strings.regenerateNoPuzzles) { newSuiteId ->
                                onNavigateToSuite(newSuiteId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(strings.regenerateSuite)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
        }
    }
}


