package com.chesspuzzles.woodpecker.util

object UciParser {

    data class UciMove(
        val from: String,
        val to: String,
        val promotion: Char? = null
    ) {
        val uci: String
            get() = "$from$to${promotion?.toString() ?: ""}"
    }

    fun parse(uci: String): UciMove {
        require(uci.length in 4..5) { "Invalid UCI move: $uci" }
        return UciMove(
            from = uci.substring(0, 2),
            to = uci.substring(2, 4),
            promotion = if (uci.length == 5) uci[4] else null
        )
    }

    fun parseAll(movesString: String): List<UciMove> =
        movesString.trim().split("\\s+".toRegex()).map { parse(it) }

    fun squareToFileRank(square: String): Pair<Int, Int> {
        require(square.length == 2) { "Invalid square: $square" }
        val file = square[0] - 'a'
        val rank = square[1] - '1'
        return Pair(file, rank)
    }

    fun fileRankToSquare(file: Int, rank: Int): String {
        return "${('a' + file)}${('1' + rank)}"
    }
}
