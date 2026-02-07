package com.chesspuzzles.woodpecker.domain.model

enum class PuzzleTheme(val displayName: String) {
    MATE_IN_1("Mate in 1"),
    MATE_IN_2("Mate in 2"),
    MATE_IN_3("Mate in 3"),
    MATE_IN_4("Mate in 4"),
    MATE_IN_5("Mate in 5+"),
    FORK("Fork"),
    PIN("Pin"),
    SKEWER("Skewer"),
    DISCOVERED_ATTACK("Discovered Attack"),
    DOUBLE_CHECK("Double Check"),
    SACRIFICE("Sacrifice"),
    DEFLECTION("Deflection"),
    DECOY("Decoy"),
    INTERFERENCE("Interference"),
    OVERLOADING("Overloading"),
    ZUGZWANG("Zugzwang"),
    TRAPPED_PIECE("Trapped Piece"),
    EXPOSED_KING("Exposed King"),
    HANGING_PIECE("Hanging Piece"),
    BACK_RANK_MATE("Back Rank Mate"),
    SMOTHERED_MATE("Smothered Mate"),
    ARABIAN_MATE("Arabian Mate"),
    ANASTASIA_MATE("Anastasia Mate"),
    HOOK_MATE("Hook Mate"),
    QUIET_MOVE("Quiet Move"),
    X_RAY_ATTACK("X-Ray Attack"),
    CLEARANCE("Clearance"),
    INTERMEZZO("Intermezzo"),
    CASTLING("Castling"),
    EN_PASSANT("En Passant"),
    PROMOTION("Promotion"),
    UNDER_PROMOTION("Under Promotion"),
    EQUALITY("Equality"),
    ADVANTAGE("Advantage"),
    CRUSHING("Crushing"),
    OPENING("Opening"),
    MIDDLEGAME("Middlegame"),
    ENDGAME("Endgame"),
    PAWN_ENDGAME("Pawn Endgame"),
    ROOK_ENDGAME("Rook Endgame"),
    BISHOP_ENDGAME("Bishop Endgame"),
    KNIGHT_ENDGAME("Knight Endgame"),
    QUEEN_ENDGAME("Queen Endgame"),
    QUEEN_ROOK_ENDGAME("Queen+Rook Endgame"),
    SHORT("Short"),
    LONG("Long"),
    ONE_MOVE("One Move"),
    VERY_LONG("Very Long"),
    MASTER("Master"),
    MASTER_VS_MASTER("Master vs Master"),
    SUPER_GM("Super GM");

    val csvKey: String
        get() = name.lowercase().replace("_", "")
            .replace("matein1", "mateIn1")
            .replace("matein2", "mateIn2")
            .replace("matein3", "mateIn3")
            .replace("matein4", "mateIn4")
            .replace("matein5", "mateIn5")
            .replace("discoveredattack", "discoveredAttack")
            .replace("doublecheck", "doubleCheck")
            .replace("trappedpiece", "trappedPiece")
            .replace("exposedking", "exposedKing")
            .replace("hangingpiece", "hangingPiece")
            .replace("backrankmate", "backRankMate")
            .replace("smotheredmate", "smotheredMate")
            .replace("arabianmate", "arabianMate")
            .replace("anastasiamate", "anastasiaMate")
            .replace("hookmate", "hookMate")
            .replace("quietmove", "quietMove")
            .replace("xrayattack", "xRayAttack")
            .replace("underpromotion", "underPromotion")
            .replace("enpassant", "enPassant")
            .replace("pawnendgame", "pawnEndgame")
            .replace("rookendgame", "rookEndgame")
            .replace("bishopendgame", "bishopEndgame")
            .replace("knightendgame", "knightEndgame")
            .replace("queenendgame", "queenEndgame")
            .replace("queenrookendgame", "queenRookEndgame")
            .replace("onemove", "oneMove")
            .replace("verylong", "veryLong")
            .replace("mastervsmaster", "masterVsMaster")
            .replace("supergm", "superGM");

    companion object {
        private val csvKeyMap: Map<String, PuzzleTheme> by lazy {
            entries.associateBy { it.csvKey }
        }

        fun fromCsvKey(key: String): PuzzleTheme? = csvKeyMap[key]
    }
}
