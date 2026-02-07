package com.chesspuzzles.woodpecker.util

object TimeFormatter {

    fun formatMs(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%d:%02d", minutes, seconds)
        }
    }

    fun formatMsShort(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return when {
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }

    fun formatDelta(deltaMs: Long): String {
        val prefix = if (deltaMs < 0) "-" else "+"
        return "$prefix${formatMsShort(kotlin.math.abs(deltaMs))}"
    }
}
