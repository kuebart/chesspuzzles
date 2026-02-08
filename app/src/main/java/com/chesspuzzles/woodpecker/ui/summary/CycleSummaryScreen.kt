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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chesspuzzles.woodpecker.ui.strings.LocalStrings
import com.chesspuzzles.woodpecker.ui.theme.ChartBarDefault
import com.chesspuzzles.woodpecker.ui.theme.ChartGold
import com.chesspuzzles.woodpecker.ui.theme.CorrectGreen
import com.chesspuzzles.woodpecker.ui.theme.WrongRed
import com.chesspuzzles.woodpecker.util.TimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleSummaryScreen(
    onBack: () -> Unit,
    onStartNextCycle: (suiteId: Long, cycleId: Long) -> Unit,
    onStartRetry: (suiteId: Long, cycleId: Long) -> Unit,
    viewModel: CycleSummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalStrings.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.cycleSummary) },
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
                    text = "${strings.cycle} ${uiState.cycleNumber}",
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
                            title = strings.totalTime,
                            value = TimeFormatter.formatMs(stats.totalTimeMs),
                            delta = stats.timeDeltaMs?.let { TimeFormatter.formatDelta(it) },
                            deltaPositive = stats.timeDeltaMs?.let { it < 0 },
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = strings.accuracy,
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
                            title = strings.solved,
                            value = "${stats.solvedCount} / ${stats.totalPuzzles}",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = strings.avgTime,
                            value = TimeFormatter.formatMsShort(stats.averageTimeMs),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Cycle time comparison chart
                    if (uiState.allCycleStats.size > 1) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = strings.timePerCycle,
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
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 4.dp)
                    )
                    Text(strings.startNextCycle)
                }

                if (uiState.failedCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.startRetryTraining(onStartRetry) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 4.dp)
                        )
                        Text("${strings.retryErrors} (${uiState.failedCount})")
                    }
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
                        null -> Color.Transparent
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

@Composable
private fun CycleBarChart(
    cycleStats: List<Pair<com.chesspuzzles.woodpecker.domain.model.Cycle, com.chesspuzzles.woodpecker.domain.model.CycleStats>>,
    modifier: Modifier = Modifier
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = modifier) {
        if (cycleStats.isEmpty()) return@Canvas

        val maxTime = cycleStats.maxOf { it.second.totalTimeMs }.toFloat()
        val bottomPadding = 24f
        val chartHeight = size.height - bottomPadding
        val barWidth = size.width / (cycleStats.size * 2f)
        val spacing = barWidth
        val cornerRadius = CornerRadius(barWidth * 0.2f, barWidth * 0.2f)

        cycleStats.forEachIndexed { index, (_, stats) ->
            val barHeight = (stats.totalTimeMs / maxTime) * (chartHeight * 0.85f)
            val x = index * (barWidth + spacing) + spacing / 2
            val isLast = index == cycleStats.lastIndex
            val barColor = if (isLast) ChartGold else ChartBarDefault

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, chartHeight - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = cornerRadius
            )
        }
    }
}
