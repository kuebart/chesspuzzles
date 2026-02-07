# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Workflow

**After every completed change: always commit and push.**
```bash
git add <changed files> && git commit -m "description" && git push
```
Do not wait for the user to ask — commit + push is the default after any working change.

## Build & Deploy

```bash
# Build debug APK (JAVA_HOME must point to JDK 17+)
JAVA_HOME="/c/Program Files/Java/jdk-18.0.1.1" ./gradlew assembleDebug

# Install on connected device
/c/android/platform-tools/adb.exe install -r app/build/outputs/apk/debug/app-debug.apk

# Launch app
/c/android/platform-tools/adb.exe shell am start -n com.chesspuzzles.woodpecker/.MainActivity

# Force stop + relaunch
/c/android/platform-tools/adb.exe shell am force-stop com.chesspuzzles.woodpecker
```

Android SDK is at `C:\android` (non-standard path, set in `local.properties`). No tests exist yet.

## Architecture

MVVM Android app using Jetpack Compose, Hilt DI, Room database. Single activity (`MainActivity`) with Compose Navigation. UI language is German.

**Data flow:** `Room DB` → `Repository` → `ViewModel (StateFlow)` → `Composable Screen`

### Key Layers

- **data/local/entity/** — Room entities: `PuzzleEntity`, `SuiteEntity`, `SuitePuzzleCrossRef`, `CycleEntity`, `PuzzleAttemptEntity`
- **data/local/dao/** — `PuzzleDao`, `SuiteDao`, `CycleDao`
- **data/repository/** — `PuzzleRepository`, `SuiteRepository` (entity ↔ domain mapping happens here)
- **data/csv/CsvImporter** — Imports `assets/puzzles.csv` (Lichess format) on first launch; skips if DB already has data. To re-import, uninstall the app.
- **domain/model/** — Domain models (`Puzzle`, `Suite`, `Cycle`, `CycleStats`, `PuzzleTheme` enum with 47 Lichess themes, German display names)
- **di/AppModule** — Hilt singleton module providing Room DB and DAOs
- **ui/navigation/NavGraph** — Routes: `home`, `create`, `suite/{suiteId}`, `training/{suiteId}/{cycleId}`, `summary/{cycleId}`

### Theme & Design (`ui/theme/`)

Premium dark chess design with Gold/Amber accent. Dynamic Color is disabled for brand consistency.

- **Color.kt** — Gold/Amber primary, warm dark surfaces, feedback colors (CorrectGreen, WrongRed), chart colors, board colors
- **Shape.kt** — Unified RoundedCornerShapes (8dp small, 12dp medium, 16dp large)
- **Type.kt** — Negative letter-spacing on headlines, all Material 3 text styles defined
- **Theme.kt** — Full dark/light ColorSchemes, status bar color synced to surface, no dynamic color

### Chess Board Component (`ui/components/chessboard/`)

Custom Canvas-based chess board — the most complex UI component:

- **BoardState** — Compose state holder wrapping `chesslib.Board`. Manages position, selection, legal moves, highlights. Exposes `boardVersion` (Int) that increments on every board change to trigger recomposition. Key methods: `loadFen()`, `makeMoveUci()`, `selectSquare()`, `tryMove()`.
- **BoardRenderer** — Pure drawing logic (`DrawScope` extension). Draws squares, highlights (selection, last move in amber, legal moves, check). Coordinates (a-h, 1-8) drawn outside the board on all four sides. Takes `margin` parameter for coordinate area. Square coloring: `(file + rank) % 2 != 0` = light (a1 is dark).
- **ChessBoard** — Main `@Composable`. Board wrapped in `Surface` with rounded corners (8dp) + shadow. Canvas uses `margin` (2.5% of width) for coordinates, `squareSize = (width - 2*margin) / 8`. In TrainingScreen the board gets full screen width (no horizontal padding). Uses `rememberTextMeasurer()` for coordinate labels.
- **PieceRenderer** — Maps `chesslib.Piece` enum to drawable resource IDs (`piece_wp.xml` through `piece_bk.xml`)
- **DragState** — Data class tracking drag-in-progress (piece, from square, current offset)

Piece drawables are Lichess cburnett SVGs converted to Android VectorDrawable format in `res/drawable/`. Black pieces require `fillType="evenOdd"` for correct rendering.

### Puzzle Solving Flow (TrainingViewModel)

1. Load FEN from puzzle, set position on `BoardState`
2. Auto-play opponent's first move (`moves[0]`) with 500ms delay
3. Enable board for user input
4. Compare user's UCI move against expected `moves[currentMoveIndex]`
5. Correct → play opponent response (`moves[currentMoveIndex+1]`), repeat until all moves done
6. Wrong → show correct move, display "Continue" button
7. Save `PuzzleAttemptEntity` with solved/failed + time
8. After last puzzle → complete cycle, navigate to summary

The FEN in the Lichess CSV is the position **before** the opponent's first move. The player always plays the side that moves second.

### Puzzle Data

`assets/puzzles.csv` uses the Lichess puzzle CSV format: `PuzzleId,FEN,Moves,Rating,RatingDeviation,Popularity,NbPlays,Themes`. CsvImporter reads columns by index: [0]=id, [1]=fen, [2]=moves, [3]=rating, [5]=popularity, [7]=themes.

### Dependencies

- **chesslib** (`com.github.bhlangonijr:chesslib:1.3.4` via JitPack) — FEN parsing, legal move generation, move validation. Core types: `Board`, `Piece`, `Square`, `Side`, `Move`.
- **Room** — Local persistence with 5 entities, 3 DAOs
- **Hilt** — DI with `@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`
- **Compose Navigation** — Type-safe routes with Long arguments for IDs
