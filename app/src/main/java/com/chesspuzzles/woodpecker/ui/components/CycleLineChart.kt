package com.chesspuzzles.woodpecker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.chesspuzzles.woodpecker.domain.model.Cycle
import com.chesspuzzles.woodpecker.domain.model.CycleStats
import com.chesspuzzles.woodpecker.ui.strings.LocalStrings
import com.chesspuzzles.woodpecker.ui.theme.ChartGold
import com.chesspuzzles.woodpecker.ui.theme.ChartGoldFill
import com.chesspuzzles.woodpecker.ui.theme.WrongRed
import kotlin.math.abs

@Composable
fun CycleLineChart(
    cycleStats: List<Pair<Cycle, CycleStats>>,
    modifier: Modifier = Modifier,
    highlightCycleId: Long? = null,
    onCycleClick: ((Long) -> Unit)? = null
) {
    // Store x positions for tap detection
    val xPositions = remember { mutableStateOf(listOf<Float>()) }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .then(
                    if (onCycleClick != null && cycleStats.size >= 2) {
                        Modifier.pointerInput(cycleStats) {
                            detectTapGestures { offset ->
                                val positions = xPositions.value
                                if (positions.isNotEmpty()) {
                                    val closestIndex = positions.indices.minByOrNull {
                                        abs(positions[it] - offset.x)
                                    } ?: return@detectTapGestures
                                    onCycleClick(cycleStats[closestIndex].first.id)
                                }
                            }
                        }
                    } else Modifier
                )
        ) {
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
                val x = index * size.width / (n - 1).toFloat()

                val timeY = topPadding + chartHeight * 0.9f -
                        ((stats.totalTimeMs - minTime) / timeRange) * chartHeight * 0.8f
                timePoints.add(Offset(x, timeY))

                val errCount = (stats.totalPuzzles - stats.solvedCount).toFloat()
                val errorY = topPadding + chartHeight * 0.9f -
                        ((errCount - minErrors) / errorRange) * chartHeight * 0.8f
                errorPoints.add(Offset(x, errorY))
            }

            // Store x positions for tap detection
            xPositions.value = timePoints.map { it.x }

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

            // Highlight index
            val highlightIndex = if (highlightCycleId != null) {
                cycleStats.indexOfFirst { it.first.id == highlightCycleId }
            } else -1

            // Vertical line for highlighted cycle
            if (highlightCycleId != null) {
                if (highlightIndex >= 0) {
                    val x = timePoints[highlightIndex].x
                    drawLine(
                        color = ChartGold.copy(alpha = 0.5f),
                        start = Offset(x, topPadding),
                        end = Offset(x, topPadding + chartHeight),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                    )
                }
            }

            // Draw dots
            cycleStats.forEachIndexed { index, _ ->
                val isHighlighted = index == highlightIndex
                val radius = if (isHighlighted) 7f else 4f
                drawCircle(color = ChartGold, radius = radius, center = timePoints[index])
                drawCircle(color = WrongRed, radius = radius, center = errorPoints[index])
            }
        }

        // Legend
        Spacer(modifier = Modifier.height(8.dp))
        ChartLegend()
    }
}

@Composable
private fun ChartLegend() {
    val strings = LocalStrings.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time legend
        Box(
            modifier = Modifier
                .size(10.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(color = ChartGold, radius = size.minDimension / 2)
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = strings.chartLegendTime,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Errors legend
        Box(
            modifier = Modifier
                .size(10.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(color = WrongRed, radius = size.minDimension / 2)
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = strings.chartLegendErrors,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
