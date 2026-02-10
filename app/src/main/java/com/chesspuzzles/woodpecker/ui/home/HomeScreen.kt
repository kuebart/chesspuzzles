package com.chesspuzzles.woodpecker.ui.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chesspuzzles.woodpecker.data.preferences.AppPreferences
import com.chesspuzzles.woodpecker.domain.model.Suite
import com.chesspuzzles.woodpecker.ui.components.AppBackground
import com.chesspuzzles.woodpecker.ui.strings.LocalStrings
import com.chesspuzzles.woodpecker.ui.theme.SuiteColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onCreateSuite: () -> Unit,
    onSuiteClick: (Long) -> Unit,
    onStartTraining: (suiteId: Long, cycleId: Long) -> Unit,
    onStartRetry: (suiteId: Long, cycleId: Long) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStats: () -> Unit,
    appPreferences: AppPreferences,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current

    var showInfoDialog by remember { mutableStateOf(appPreferences.shouldShowInfoBox()) }
    var doNotShowAgain by remember { mutableStateOf(false) }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = {
                if (doNotShowAgain) appPreferences.setInfoBoxDismissed()
                showInfoDialog = false
            },
            title = { Text(strings.infoTitle) },
            text = {
                Column {
                    Text(strings.infoText)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { doNotShowAgain = !doNotShowAgain }
                    ) {
                        Checkbox(
                            checked = doNotShowAgain,
                            onCheckedChange = { doNotShowAgain = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.infoDoNotShowAgain,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (doNotShowAgain) appPreferences.setInfoBoxDismissed()
                    showInfoDialog = false
                }) {
                    Text(strings.infoDismiss)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.appTitle,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = onOpenStats) {
                        Icon(
                            Icons.Outlined.BarChart,
                            contentDescription = strings.statisticsTitle,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = strings.settingsTitle,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateSuite,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.createSuite)
            }
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (uiState.suites.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = strings.noSuitesYet,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = strings.noSuitesSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                items(uiState.suites, key = { it.id }) { suite ->
                    val accentColor = SuiteColors[(suite.id % SuiteColors.size).toInt()]
                    SuiteCard(
                        suite = suite,
                        accentColor = accentColor,
                        onClick = { onSuiteClick(suite.id) },
                        onStartTraining = {
                            scope.launch {
                                val cycleId = viewModel.getOrCreateCycleForSuite(suite.id)
                                onStartTraining(suite.id, cycleId)
                            }
                        },
                        onStartRetry = {
                            scope.launch {
                                val cycleId = viewModel.startRetryForSuite(suite.id) ?: return@launch
                                onStartRetry(suite.id, cycleId)
                            }
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SuiteCard(
    suite: Suite,
    accentColor: Color,
    onClick: () -> Unit,
    onStartTraining: () -> Unit,
    onStartRetry: () -> Unit
) {
    val strings = LocalStrings.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .drawBehind {
                drawRect(
                    color = accentColor,
                    topLeft = Offset.Zero,
                    size = Size(4.dp.toPx(), size.height)
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = suite.name,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onStartTraining,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 4.dp)
                )
                if (suite.activeCycleProgress != null && suite.activeCycleProgress > 0) {
                    Text("${strings.continueCycle} (${suite.activeCycleProgress!! + 1}/${suite.puzzleCount})")
                } else if (suite.activeCycleProgress != null) {
                    Text(strings.startCycle)
                } else {
                    Text(strings.startTraining)
                }
            }

            if (suite.activeCycleProgress != null && suite.activeCycleProgress > 0) {
                LinearProgressIndicator(
                    progress = { suite.activeCycleProgress.toFloat() / suite.puzzleCount },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    color = accentColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }

            if (suite.failedPuzzleCount > 0 && (suite.activeCycleProgress == null || suite.activeCycleProgress == 0)) {
                OutlinedButton(
                    onClick = onStartRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("${strings.retryErrors} (${suite.failedPuzzleCount})")
                }
            }

            if (suite.themes.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    suite.themes.take(5).forEach { theme ->
                        val themeName = strings.themeDisplayNames[theme] ?: theme.displayName
                        AssistChip(
                            onClick = {},
                            label = { Text(themeName, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                    if (suite.themes.size > 5) {
                        AssistChip(
                            onClick = {},
                            label = { Text("+${suite.themes.size - 5}", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${suite.puzzleCount} ${strings.puzzlesCount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${strings.ratingLabel} ${suite.ratingMin}-${suite.ratingMax}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${suite.cycleCount} ${strings.cyclesCompleted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                suite.lastCycleAccuracy?.let { accuracy ->
                    Text(
                        text = "${strings.lastAccuracy} ${(accuracy * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = accentColor
                    )
                }
            }
        }
    }
}
