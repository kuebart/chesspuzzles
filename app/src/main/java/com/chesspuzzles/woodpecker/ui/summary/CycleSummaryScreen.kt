package com.chesspuzzles.woodpecker.ui.summary

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chesspuzzles.woodpecker.ui.theme.CorrectGreen
import com.chesspuzzles.woodpecker.ui.theme.WrongRed
import com.chesspuzzles.woodpecker.util.TimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleSummaryScreen(
    onBack: () -> Unit,
    onStartNextCycle: (suiteId: Long, cycleId: Long) -> Unit,
    viewModel: CycleSummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cycle Summary") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Cycle ${uiState.cycleNumber}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                uiState.stats?.let { stats ->
                    // Main stats cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Total Time",
                            value = TimeFormatter.formatMs(stats.totalTimeMs),
                            delta = stats.timeDeltaMs?.let { TimeFormatter.formatDelta(it) },
                            deltaPositive = stats.timeDeltaMs?.let { it < 0 }, // Less time is better
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Accuracy",
                            value = "${(stats.accuracy * 100).toInt()}%",
                            delta = stats.accuracyDelta?.let {
                                "${if (it >= 0) "+" else ""}${(it * 100).toInt()}%"
                            },
                            deltaPositive = stats.accuracyDelta?.let { it >= 0 },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Solved",
                            value = "${stats.solvedCount} / ${stats.totalPuzzles}",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Avg Time",
                            value = TimeFormatter.formatMsShort(stats.averageTimeMs),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Cycle time comparison chart
                    if (uiState.allCycleStats.size > 1) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Time per Cycle",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CycleBarChart(
                            cycleStats = uiState.allCycleStats,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.startNextCycle(onStartNextCycle) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Next Cycle")
                }

                Spacer(modifier = Modifier.height(32.dp))
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
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Text(
                    text = delta,
                    style = MaterialTheme.typography.bodySmall,
                    color = when (deltaPositive) {
                        true -> CorrectGreen
                        false -> WrongRed
                        null -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun CycleBarChart(
    cycleStats: List<Pair<com.chesspuzzles.woodpecker.domain.model.Cycle, com.chesspuzzles.woodpecker.domain.model.CycleStats>>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        if (cycleStats.isEmpty()) return@Canvas

        val maxTime = cycleStats.maxOf { it.second.totalTimeMs }.toFloat()
        val barWidth = size.width / (cycleStats.size * 2f)
        val spacing = barWidth

        cycleStats.forEachIndexed { index, (_, stats) ->
            val barHeight = (stats.totalTimeMs / maxTime) * (size.height * 0.85f)
            val x = index * (barWidth + spacing) + spacing / 2

            drawRect(
                color = primaryColor,
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barWidth, barHeight)
            )
        }
    }
}
