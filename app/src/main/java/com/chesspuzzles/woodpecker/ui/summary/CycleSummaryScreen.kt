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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chesspuzzles.woodpecker.ui.components.AppBackground
import com.chesspuzzles.woodpecker.ui.strings.LocalStrings
import com.chesspuzzles.woodpecker.ui.theme.ChartGold
import com.chesspuzzles.woodpecker.ui.theme.ChartGoldFill
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

                    // Cycle comparison chart
                    if (uiState.allCycleStats.size > 1) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = strings.timePerCycle,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CycleLineChart(
                            cycleStats = uiState.allCycleStats,
                            currentCycleId = viewModel.getCycleId(),
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
                    Text(if (uiState.isLatestCycle) strings.startNextCycle else strings.continueTraining)
                }

                if (uiState.failedCount > 0 && uiState.isLatestCycle) {
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
private fun CycleLineChart(
    cycleStats: List<Pair<com.chesspuzzles.woodpecker.domain.model.Cycle, com.chesspuzzles.woodpecker.domain.model.CycleStats>>,
    currentCycleId: Long,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (cycleStats.size < 2) return@Canvas

        val topPadding = 12f
        val bottomPadding = 12f
        val chartHeight = size.height - topPadding - bottomPadding
        val n = cycleStats.size

        // Time line (gold) — scale
        val maxTime = cycleStats.maxOf { it.second.totalTimeMs }.toFloat()
        val minTime = cycleStats.minOf { it.second.totalTimeMs }.toFloat()
        val timeRange = (maxTime - minTime).coerceAtLeast(1f)

        // Errors line (red) — scale
        val errors = cycleStats.map { it.second.totalPuzzles - it.second.solvedCount }
        val maxErrors = errors.max().toFloat()
        val minErrors = errors.min().toFloat()
        val errorRange = (maxErrors - minErrors).coerceAtLeast(1f)

        // Compute points
        val timePoints = mutableListOf<Offset>()
        val errorPoints = mutableListOf<Offset>()

        cycleStats.forEachIndexed { index, (_, stats) ->
            val x = if (n == 1) size.width / 2 else index * size.width / (n - 1).toFloat()

            val timeY = topPadding + chartHeight * 0.9f -
                    ((stats.totalTimeMs - minTime) / timeRange) * chartHeight * 0.8f
            timePoints.add(Offset(x, timeY))

            val errCount = (stats.totalPuzzles - stats.solvedCount).toFloat()
            val errorY = topPadding + chartHeight * 0.9f -
                    ((errCount - minErrors) / errorRange) * chartHeight * 0.8f
            errorPoints.add(Offset(x, errorY))
        }

        // Draw time fill
        val timeFillPath = Path().apply {
            moveTo(timePoints.first().x, topPadding + chartHeight)
            timePoints.forEach { lineTo(it.x, it.y) }
            lineTo(timePoints.last().x, topPadding + chartHeight)
            close()
        }
        drawPath(timeFillPath, color = ChartGoldFill, style = Fill)

        // Draw time line
        val timeLinePath = Path().apply {
            timePoints.forEachIndexed { i, p ->
                if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
            }
        }
        drawPath(
            timeLinePath,
            color = ChartGold,
            style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw error fill
        val errorFillPath = Path().apply {
            moveTo(errorPoints.first().x, topPadding + chartHeight)
            errorPoints.forEach { lineTo(it.x, it.y) }
            lineTo(errorPoints.last().x, topPadding + chartHeight)
            close()
        }
        drawPath(errorFillPath, color = WrongRed.copy(alpha = 0.15f), style = Fill)

        // Draw error line
        val errorLinePath = Path().apply {
            errorPoints.forEachIndexed { i, p ->
                if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
            }
        }
        drawPath(
            errorLinePath,
            color = WrongRed,
            style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw dots — highlight current cycle
        cycleStats.forEachIndexed { index, (cycle, _) ->
            val isCurrent = cycle.id == currentCycleId
            val radius = if (isCurrent) 7f else 4f

            // Time dot
            drawCircle(color = ChartGold, radius = radius, center = timePoints[index])

            // Error dot
            drawCircle(color = WrongRed, radius = radius, center = errorPoints[index])
        }
    }
}
