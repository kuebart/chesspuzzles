package com.chesspuzzles.woodpecker.ui.components

import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chesspuzzles.woodpecker.domain.model.PuzzleTheme

@Composable
fun ThemeChip(
    theme: PuzzleTheme,
    selected: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = { onToggle(!selected) },
        label = { Text(theme.displayName) },
        modifier = modifier
    )
}
