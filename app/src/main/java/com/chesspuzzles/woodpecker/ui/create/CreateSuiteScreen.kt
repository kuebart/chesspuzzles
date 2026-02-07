package com.chesspuzzles.woodpecker.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chesspuzzles.woodpecker.domain.model.PuzzleTheme
import com.chesspuzzles.woodpecker.ui.components.ThemeChip

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateSuiteScreen(
    onBack: () -> Unit,
    onSuiteCreated: (Long) -> Unit,
    viewModel: CreateSuiteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Suite") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::setName,
                label = { Text("Suite Name") },
                placeholder = { Text("Suite ...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Theme Selection
            Text(
                text = "Themes",
                style = MaterialTheme.typography.titleMedium
            )

            val tacticalThemes = listOf(
                PuzzleTheme.FORK, PuzzleTheme.PIN, PuzzleTheme.SKEWER,
                PuzzleTheme.DISCOVERED_ATTACK, PuzzleTheme.DOUBLE_CHECK,
                PuzzleTheme.SACRIFICE, PuzzleTheme.DEFLECTION, PuzzleTheme.DECOY,
                PuzzleTheme.INTERFERENCE, PuzzleTheme.OVERLOADING,
                PuzzleTheme.TRAPPED_PIECE, PuzzleTheme.HANGING_PIECE,
                PuzzleTheme.ZUGZWANG, PuzzleTheme.QUIET_MOVE,
                PuzzleTheme.X_RAY_ATTACK, PuzzleTheme.CLEARANCE,
                PuzzleTheme.INTERMEZZO
            )

            val mateThemes = listOf(
                PuzzleTheme.MATE_IN_1, PuzzleTheme.MATE_IN_2, PuzzleTheme.MATE_IN_3,
                PuzzleTheme.BACK_RANK_MATE, PuzzleTheme.SMOTHERED_MATE,
                PuzzleTheme.ARABIAN_MATE, PuzzleTheme.HOOK_MATE
            )

            val phaseThemes = listOf(
                PuzzleTheme.OPENING, PuzzleTheme.MIDDLEGAME, PuzzleTheme.ENDGAME,
                PuzzleTheme.PAWN_ENDGAME, PuzzleTheme.ROOK_ENDGAME
            )

            // Tactical Motifs
            Text(
                text = "TAKTISCHE MOTIVE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tacticalThemes.forEach { theme ->
                    ThemeChip(
                        theme = theme,
                        selected = theme in uiState.selectedThemes,
                        onToggle = { viewModel.toggleTheme(theme) }
                    )
                }
            }

            // Mating Patterns
            Text(
                text = "MATTMUSTER",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                mateThemes.forEach { theme ->
                    ThemeChip(
                        theme = theme,
                        selected = theme in uiState.selectedThemes,
                        onToggle = { viewModel.toggleTheme(theme) }
                    )
                }
            }

            // Game Phase
            Text(
                text = "SPIELPHASE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                phaseThemes.forEach { theme ->
                    ThemeChip(
                        theme = theme,
                        selected = theme in uiState.selectedThemes,
                        onToggle = { viewModel.toggleTheme(theme) }
                    )
                }
            }

            // Rating Range
            Text(
                text = "Rating Range: ${uiState.ratingMin} - ${uiState.ratingMax}",
                style = MaterialTheme.typography.titleMedium
            )

            RangeSlider(
                value = uiState.ratingMin.toFloat()..uiState.ratingMax.toFloat(),
                onValueChange = { range ->
                    viewModel.setRatingRange(range.start.toInt(), range.endInclusive.toInt())
                },
                valueRange = 400f..2800f,
                steps = 23,
                modifier = Modifier.fillMaxWidth()
            )

            // Puzzle Count
            Text(
                text = "Puzzle Count: ${uiState.puzzleCount}",
                style = MaterialTheme.typography.titleMedium
            )

            Slider(
                value = uiState.puzzleCount.toFloat(),
                onValueChange = { viewModel.setPuzzleCount(it.toInt()) },
                valueRange = 50f..1000f,
                steps = 18,
                modifier = Modifier.fillMaxWidth()
            )

            // Matching count preview
            Text(
                text = "${uiState.matchingPuzzleCount} matching puzzles available",
                style = MaterialTheme.typography.bodyMedium,
                color = if (uiState.matchingPuzzleCount >= uiState.puzzleCount) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            uiState.error?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = { viewModel.createSuite(onSuiteCreated) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isCreating
            ) {
                if (uiState.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text("Create Suite")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
