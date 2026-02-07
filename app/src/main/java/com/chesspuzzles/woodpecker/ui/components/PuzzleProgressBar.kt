package com.chesspuzzles.woodpecker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PuzzleProgressBar(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$current / $total",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        LinearProgressIndicator(
            progress = { if (total > 0) current.toFloat() / total else 0f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}
