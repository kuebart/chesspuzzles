package com.chesspuzzles.woodpecker.domain.model

enum class PuzzleTheme(val displayName: String) {
    MATE_IN_1("Matt in 1"),
    MATE_IN_2("Matt in 2"),
    MATE_IN_3("Matt in 3"),
    MATE_IN_4("Matt in 4"),
    MATE_IN_5("Matt in 5+"),
    FORK("Gabel"),
    PIN("Fesselung"),
    SKEWER("Spie\u00df"),
    DISCOVERED_ATTACK("Abzugsangriff"),
    DOUBLE_CHECK("Doppelschach"),
    SACRIFICE("Opfer"),
    DEFLECTION("Ablenkung"),
    DECOY("Hinlenkung"),
    INTERFERENCE("Verstellung"),
    OVERLOADING("\u00dcberlastung"),
    ZUGZWANG("Zugzwang"),
    TRAPPED_PIECE("Gefangene Figur"),
    EXPOSED_KING("Exponierter K\u00f6nig"),
    HANGING_PIECE("H\u00e4ngende Figur"),
    BACK_RANK_MATE("Grundreihenmatt"),
    SMOTHERED_MATE("Ersticktes Matt"),
    ARABIAN_MATE("Arabisches Matt"),
    ANASTASIA_MATE("Anastasia-Matt"),
    HOOK_MATE("Hakenmatt"),
    QUIET_MOVE("Stiller Zug"),
    X_RAY_ATTACK("R\u00f6ntgenangriff"),
    CLEARANCE("R\u00e4umung"),
    INTERMEZZO("Zwischenzug"),
    CASTLING("Rochade"),
    EN_PASSANT("En Passant"),
    PROMOTION("Umwandlung"),
    UNDER_PROMOTION("Unterverwandlung"),
    EQUALITY("Ausgleich"),
    ADVANTAGE("Vorteil"),
    CRUSHING("Vernichtend"),
    OPENING("Er\u00f6ffnung"),
    MIDDLEGAME("Mittelspiel"),
    ENDGAME("Endspiel"),
    PAWN_ENDGAME("Bauernendspiel"),
    ROOK_ENDGAME("Turmendspiel"),
    BISHOP_ENDGAME("L\u00e4uferendspiel"),
    KNIGHT_ENDGAME("Springerendspiel"),
    QUEEN_ENDGAME("Damenendspiel"),
    QUEEN_ROOK_ENDGAME("Dame+Turm-Endspiel"),
    SHORT("Kurz"),
    LONG("Lang"),
    ONE_MOVE("Ein Zug"),
    VERY_LONG("Sehr lang"),
    MASTER("Meister"),
    MASTER_VS_MASTER("Meister vs Meister"),
    SUPER_GM("Super-GM");

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
