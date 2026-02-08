package com.chesspuzzles.woodpecker.ui.strings

import com.chesspuzzles.woodpecker.domain.model.PuzzleTheme

data class AppStrings(
    // General
    val back: String,
    val cancel: String,
    val delete: String,
    val continueButton: String,

    // Settings
    val settingsTitle: String,
    val languageLabel: String,

    // HomeScreen
    val appTitle: String,
    val createSuite: String,
    val noSuitesYet: String,
    val noSuitesSubtitle: String,
    val puzzlesSolved: String,
    val trainingDays: String,
    val startTraining: String,
    val retryErrors: String, // "Fehler wiederholen" / "Retry errors"
    val puzzlesCount: String, // "puzzles"
    val ratingLabel: String, // "Rating"
    val cyclesCompleted: String, // "cycles completed"
    val lastAccuracy: String, // "Last:"
    val taskProgress: String, // "Aufgabe" / "Puzzle"

    // CreateSuiteScreen
    val createSuiteTitle: String,
    val suiteName: String,
    val suiteNamePlaceholder: String,
    val themes: String,
    val tacticalMotifs: String,
    val matingPatterns: String,
    val gamePhase: String,
    val ratingRange: String,
    val puzzleCount: String,
    val matchingPuzzlesAvailable: String, // "{count} matching puzzles available"
    val createSuiteButton: String,

    // SuiteDetailScreen
    val deleteSuiteTitle: String,
    val deleteSuiteConfirm: String,
    val suiteDefault: String,
    val progress: String,
    val cycles: String,
    val noCyclesYet: String,
    val cycle: String, // "Cycle"
    val correct: String, // "korrekt" / "correct"
    val time: String,
    val accuracy: String,

    // TrainingScreen
    val training: String,
    val previousPuzzle: String,
    val nextPuzzle: String,
    val correctLabel: String, // "correct"
    val wrongLabel: String, // "wrong"
    val reviewing: String,
    val toPlay: String, // "to play"
    val white: String,
    val black: String,
    val previousMove: String,
    val nextMove: String,
    val returnToCurrentPuzzle: String,
    val correctFeedback: String, // "Correct!"
    val incorrectFeedback: String, // "Incorrect"

    // CycleSummaryScreen
    val cycleSummary: String,
    val totalTime: String,
    val solved: String,
    val avgTime: String,
    val timePerCycle: String,
    val startNextCycle: String,
    val continueTraining: String,

    // Theme display names
    val themeDisplayNames: Map<PuzzleTheme, String>
)

val DeStrings = AppStrings(
    // General
    back = "Zurück",
    cancel = "Abbrechen",
    delete = "Löschen",
    continueButton = "Weiter",

    // Settings
    settingsTitle = "Einstellungen",
    languageLabel = "Sprache",

    // HomeScreen
    appTitle = "Woodpecker",
    createSuite = "Suite erstellen",
    noSuitesYet = "Noch keine Suites",
    noSuitesSubtitle = "Erstelle deine erste Puzzle-Suite um mit dem Training zu beginnen",
    puzzlesSolved = "Gelöste Aufgaben",
    trainingDays = "Trainingstage",
    startTraining = "Training starten",
    retryErrors = "Fehler wiederholen",
    puzzlesCount = "Aufgaben",
    ratingLabel = "Rating",
    cyclesCompleted = "Zyklen abgeschlossen",
    lastAccuracy = "Letzter:",
    taskProgress = "Aufgabe",

    // CreateSuiteScreen
    createSuiteTitle = "Suite erstellen",
    suiteName = "Suite Name",
    suiteNamePlaceholder = "Suite ...",
    themes = "Themen",
    tacticalMotifs = "TAKTISCHE MOTIVE",
    matingPatterns = "MATTMUSTER",
    gamePhase = "SPIELPHASE",
    ratingRange = "Rating-Bereich",
    puzzleCount = "Aufgabenanzahl",
    matchingPuzzlesAvailable = "passende Aufgaben verfügbar",
    createSuiteButton = "Suite erstellen",

    // SuiteDetailScreen
    deleteSuiteTitle = "Suite löschen",
    deleteSuiteConfirm = "Bist du sicher, dass du diese Suite und alle Zyklen löschen möchtest?",
    suiteDefault = "Suite",
    progress = "Fortschritt",
    cycles = "Zyklen",
    noCyclesYet = "Noch keine Zyklen.",
    cycle = "Zyklus",
    correct = "korrekt",
    time = "Zeit",
    accuracy = "Genauigkeit",

    // TrainingScreen
    training = "Training",
    previousPuzzle = "Vorherige Aufgabe",
    nextPuzzle = "Nächste Aufgabe",
    correctLabel = "richtig",
    wrongLabel = "falsch",
    reviewing = "Rückblick",
    toPlay = "am Zug",
    white = "Weiß",
    black = "Schwarz",
    previousMove = "Vorheriger Zug",
    nextMove = "Nächster Zug",
    returnToCurrentPuzzle = "Zurück zur aktuellen Aufgabe",
    correctFeedback = "Richtig!",
    incorrectFeedback = "Falsch",

    // CycleSummaryScreen
    cycleSummary = "Zusammenfassung",
    totalTime = "Gesamtzeit",
    solved = "Gelöst",
    avgTime = "Ø Zeit",
    timePerCycle = "Zeit pro Zyklus",
    startNextCycle = "Nächsten Zyklus starten",
    continueTraining = "Training fortsetzen",

    // Theme display names
    themeDisplayNames = mapOf(
        PuzzleTheme.MATE_IN_1 to "Matt in 1",
        PuzzleTheme.MATE_IN_2 to "Matt in 2",
        PuzzleTheme.MATE_IN_3 to "Matt in 3",
        PuzzleTheme.MATE_IN_4 to "Matt in 4",
        PuzzleTheme.MATE_IN_5 to "Matt in 5+",
        PuzzleTheme.FORK to "Gabel",
        PuzzleTheme.PIN to "Fesselung",
        PuzzleTheme.SKEWER to "Spieß",
        PuzzleTheme.DISCOVERED_ATTACK to "Abzugsangriff",
        PuzzleTheme.DOUBLE_CHECK to "Doppelschach",
        PuzzleTheme.SACRIFICE to "Opfer",
        PuzzleTheme.DEFLECTION to "Ablenkung",
        PuzzleTheme.DECOY to "Hinlenkung",
        PuzzleTheme.INTERFERENCE to "Verstellung",
        PuzzleTheme.OVERLOADING to "Überlastung",
        PuzzleTheme.ZUGZWANG to "Zugzwang",
        PuzzleTheme.TRAPPED_PIECE to "Gefangene Figur",
        PuzzleTheme.EXPOSED_KING to "Exponierter König",
        PuzzleTheme.HANGING_PIECE to "Hängende Figur",
        PuzzleTheme.BACK_RANK_MATE to "Grundreihenmatt",
        PuzzleTheme.SMOTHERED_MATE to "Ersticktes Matt",
        PuzzleTheme.ARABIAN_MATE to "Arabisches Matt",
        PuzzleTheme.ANASTASIA_MATE to "Anastasia-Matt",
        PuzzleTheme.HOOK_MATE to "Hakenmatt",
        PuzzleTheme.QUIET_MOVE to "Stiller Zug",
        PuzzleTheme.X_RAY_ATTACK to "Röntgenangriff",
        PuzzleTheme.CLEARANCE to "Räumung",
        PuzzleTheme.INTERMEZZO to "Zwischenzug",
        PuzzleTheme.CASTLING to "Rochade",
        PuzzleTheme.EN_PASSANT to "En Passant",
        PuzzleTheme.PROMOTION to "Umwandlung",
        PuzzleTheme.UNDER_PROMOTION to "Unterverwandlung",
        PuzzleTheme.EQUALITY to "Ausgleich",
        PuzzleTheme.ADVANTAGE to "Vorteil",
        PuzzleTheme.CRUSHING to "Vernichtend",
        PuzzleTheme.OPENING to "Eröffnung",
        PuzzleTheme.MIDDLEGAME to "Mittelspiel",
        PuzzleTheme.ENDGAME to "Endspiel",
        PuzzleTheme.PAWN_ENDGAME to "Bauernendspiel",
        PuzzleTheme.ROOK_ENDGAME to "Turmendspiel",
        PuzzleTheme.BISHOP_ENDGAME to "Läuferendspiel",
        PuzzleTheme.KNIGHT_ENDGAME to "Springerendspiel",
        PuzzleTheme.QUEEN_ENDGAME to "Damenendspiel",
        PuzzleTheme.QUEEN_ROOK_ENDGAME to "Dame+Turm-Endspiel",
        PuzzleTheme.SHORT to "Kurz",
        PuzzleTheme.LONG to "Lang",
        PuzzleTheme.ONE_MOVE to "Ein Zug",
        PuzzleTheme.VERY_LONG to "Sehr lang",
        PuzzleTheme.MASTER to "Meister",
        PuzzleTheme.MASTER_VS_MASTER to "Meister vs Meister",
        PuzzleTheme.SUPER_GM to "Super-GM"
    )
)

val EnStrings = AppStrings(
    // General
    back = "Back",
    cancel = "Cancel",
    delete = "Delete",
    continueButton = "Continue",

    // Settings
    settingsTitle = "Settings",
    languageLabel = "Language",

    // HomeScreen
    appTitle = "Woodpecker",
    createSuite = "Create Suite",
    noSuitesYet = "No suites yet",
    noSuitesSubtitle = "Create your first puzzle suite to start training",
    puzzlesSolved = "Puzzles Solved",
    trainingDays = "Training Days",
    startTraining = "Start Training",
    retryErrors = "Retry errors",
    puzzlesCount = "puzzles",
    ratingLabel = "Rating",
    cyclesCompleted = "cycles completed",
    lastAccuracy = "Last:",
    taskProgress = "Puzzle",

    // CreateSuiteScreen
    createSuiteTitle = "Create Suite",
    suiteName = "Suite Name",
    suiteNamePlaceholder = "Suite ...",
    themes = "Themes",
    tacticalMotifs = "TACTICAL MOTIFS",
    matingPatterns = "MATING PATTERNS",
    gamePhase = "GAME PHASE",
    ratingRange = "Rating Range",
    puzzleCount = "Puzzle Count",
    matchingPuzzlesAvailable = "matching puzzles available",
    createSuiteButton = "Create Suite",

    // SuiteDetailScreen
    deleteSuiteTitle = "Delete Suite",
    deleteSuiteConfirm = "Are you sure you want to delete this suite and all its cycles?",
    suiteDefault = "Suite",
    progress = "Progress",
    cycles = "Cycles",
    noCyclesYet = "No cycles yet.",
    cycle = "Cycle",
    correct = "correct",
    time = "Time",
    accuracy = "Accuracy",

    // TrainingScreen
    training = "Training",
    previousPuzzle = "Previous puzzle",
    nextPuzzle = "Next puzzle",
    correctLabel = "correct",
    wrongLabel = "wrong",
    reviewing = "Reviewing",
    toPlay = "to play",
    white = "White",
    black = "Black",
    previousMove = "Previous move",
    nextMove = "Next move",
    returnToCurrentPuzzle = "Return to current puzzle",
    correctFeedback = "Correct!",
    incorrectFeedback = "Incorrect",

    // CycleSummaryScreen
    cycleSummary = "Cycle Summary",
    totalTime = "Total Time",
    solved = "Solved",
    avgTime = "Avg Time",
    timePerCycle = "Time per Cycle",
    startNextCycle = "Start Next Cycle",
    continueTraining = "Continue Training",

    // Theme display names
    themeDisplayNames = mapOf(
        PuzzleTheme.MATE_IN_1 to "Mate in 1",
        PuzzleTheme.MATE_IN_2 to "Mate in 2",
        PuzzleTheme.MATE_IN_3 to "Mate in 3",
        PuzzleTheme.MATE_IN_4 to "Mate in 4",
        PuzzleTheme.MATE_IN_5 to "Mate in 5+",
        PuzzleTheme.FORK to "Fork",
        PuzzleTheme.PIN to "Pin",
        PuzzleTheme.SKEWER to "Skewer",
        PuzzleTheme.DISCOVERED_ATTACK to "Discovered Attack",
        PuzzleTheme.DOUBLE_CHECK to "Double Check",
        PuzzleTheme.SACRIFICE to "Sacrifice",
        PuzzleTheme.DEFLECTION to "Deflection",
        PuzzleTheme.DECOY to "Decoy",
        PuzzleTheme.INTERFERENCE to "Interference",
        PuzzleTheme.OVERLOADING to "Overloading",
        PuzzleTheme.ZUGZWANG to "Zugzwang",
        PuzzleTheme.TRAPPED_PIECE to "Trapped Piece",
        PuzzleTheme.EXPOSED_KING to "Exposed King",
        PuzzleTheme.HANGING_PIECE to "Hanging Piece",
        PuzzleTheme.BACK_RANK_MATE to "Back Rank Mate",
        PuzzleTheme.SMOTHERED_MATE to "Smothered Mate",
        PuzzleTheme.ARABIAN_MATE to "Arabian Mate",
        PuzzleTheme.ANASTASIA_MATE to "Anastasia Mate",
        PuzzleTheme.HOOK_MATE to "Hook Mate",
        PuzzleTheme.QUIET_MOVE to "Quiet Move",
        PuzzleTheme.X_RAY_ATTACK to "X-Ray Attack",
        PuzzleTheme.CLEARANCE to "Clearance",
        PuzzleTheme.INTERMEZZO to "Intermezzo",
        PuzzleTheme.CASTLING to "Castling",
        PuzzleTheme.EN_PASSANT to "En Passant",
        PuzzleTheme.PROMOTION to "Promotion",
        PuzzleTheme.UNDER_PROMOTION to "Underpromotion",
        PuzzleTheme.EQUALITY to "Equality",
        PuzzleTheme.ADVANTAGE to "Advantage",
        PuzzleTheme.CRUSHING to "Crushing",
        PuzzleTheme.OPENING to "Opening",
        PuzzleTheme.MIDDLEGAME to "Middlegame",
        PuzzleTheme.ENDGAME to "Endgame",
        PuzzleTheme.PAWN_ENDGAME to "Pawn Endgame",
        PuzzleTheme.ROOK_ENDGAME to "Rook Endgame",
        PuzzleTheme.BISHOP_ENDGAME to "Bishop Endgame",
        PuzzleTheme.KNIGHT_ENDGAME to "Knight Endgame",
        PuzzleTheme.QUEEN_ENDGAME to "Queen Endgame",
        PuzzleTheme.QUEEN_ROOK_ENDGAME to "Queen+Rook Endgame",
        PuzzleTheme.SHORT to "Short",
        PuzzleTheme.LONG to "Long",
        PuzzleTheme.ONE_MOVE to "One Move",
        PuzzleTheme.VERY_LONG to "Very Long",
        PuzzleTheme.MASTER to "Master",
        PuzzleTheme.MASTER_VS_MASTER to "Master vs Master",
        PuzzleTheme.SUPER_GM to "Super GM"
    )
)
