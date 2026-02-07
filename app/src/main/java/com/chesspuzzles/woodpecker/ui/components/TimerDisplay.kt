package com.chesspuzzles.woodpecker.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.chesspuzzles.woodpecker.util.TimeFormatter
import kotlinx.coroutines.delay

@Composable
fun TimerDisplay(
    startTimeMs: Long,
    running: Boolean,
    modifier: Modifier = Modifier,
    offsetMs: Long = 0
) {
    var elapsed by remember { mutableLongStateOf(0L) }

    LaunchedEffect(running, startTimeMs) {
        if (running && startTimeMs > 0) {
            while (true) {
                elapsed = System.currentTimeMillis() - startTimeMs + offsetMs
                delay(100)
            }
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(18.dp)
                .padding(end = 4.dp)
        )
        Text(
            text = TimeFormatter.formatMs(elapsed),
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
