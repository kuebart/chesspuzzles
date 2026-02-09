package com.chesspuzzles.woodpecker.ui.components.chessboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move

class BoardState {
    var board: Board by mutableStateOf(Board())
        private set

    var flipped: Boolean by mutableStateOf(false)

    var selectedSquare: Square? by mutableStateOf(null)
        private set

    var legalMovesFromSelected: List<Move> by mutableStateOf(emptyList())
        private set

    var lastMoveFrom: Square? by mutableStateOf(null)
        private set

    var lastMoveTo: Square? by mutableStateOf(null)
        private set

    var isCheck: Boolean by mutableStateOf(false)
        private set

    // Increment to trigger recomposition after board changes
    var boardVersion: Int by mutableIntStateOf(0)
        private set

    var wrongMoveFrom: Square? by mutableStateOf(null)
        private set

    var wrongMoveTo: Square? by mutableStateOf(null)
        private set

    var animatingPiece: Piece? by mutableStateOf(null)
        private set

    var animateFromSquare: Square? by mutableStateOf(null)
        private set

    var animateToSquare: Square? by mutableStateOf(null)
        private set

    fun loadFen(fen: String) {
        board = Board()
        board.loadFromFen(fen)
        selectedSquare = null
        legalMovesFromSelected = emptyList()
        lastMoveFrom = null
        lastMoveTo = null
        wrongMoveFrom = null
        wrongMoveTo = null
        animatingPiece = null
        animateFromSquare = null
        animateToSquare = null
        isCheck = board.isKingAttacked
        boardVersion++
    }

    fun selectSquare(square: Square) {
        val piece = board.getPiece(square)
        if (piece != Piece.NONE && piece.pieceSide == board.sideToMove) {
            selectedSquare = square
            legalMovesFromSelected = getLegalMovesFrom(square)
        } else {
            clearSelection()
        }
    }

    fun tryMove(from: Square, to: Square): Boolean {
        val legalMoves = getLegalMovesFrom(from)
        val move = legalMoves.find { it.to == to }
        if (move != null) {
            makeMove(move)
            return true
        }
        return false
    }

    fun makeMove(move: Move) {
        lastMoveFrom = move.from
        lastMoveTo = move.to
        board.doMove(move)
        selectedSquare = null
        legalMovesFromSelected = emptyList()
        isCheck = board.isKingAttacked
        boardVersion++
    }

    fun makeMoveUci(uci: String): Boolean {
        if (uci.length < 4) return false
        val from = try { Square.valueOf(uci.substring(0, 2).uppercase()) } catch (e: Exception) { return false }
        val to = try { Square.valueOf(uci.substring(2, 4).uppercase()) } catch (e: Exception) { return false }

        val legalMoves = board.legalMoves()
        val legalMove = if (uci.length >= 5) {
            val promChar = uci[4].lowercaseChar()
            legalMoves.find { move ->
                move.from == from && move.to == to &&
                    move.promotion != Piece.NONE &&
                    when (promChar) {
                        'q' -> move.promotion == Piece.WHITE_QUEEN || move.promotion == Piece.BLACK_QUEEN
                        'r' -> move.promotion == Piece.WHITE_ROOK || move.promotion == Piece.BLACK_ROOK
                        'b' -> move.promotion == Piece.WHITE_BISHOP || move.promotion == Piece.BLACK_BISHOP
                        'n' -> move.promotion == Piece.WHITE_KNIGHT || move.promotion == Piece.BLACK_KNIGHT
                        else -> true
                    }
            }
        } else {
            legalMoves.find { it.from == from && it.to == to }
        } ?: return false

        makeMove(legalMove)
        return true
    }

    fun setWrongMove(from: Square, to: Square) {
        wrongMoveFrom = from
        wrongMoveTo = to
    }

    fun clearWrongMove() {
        wrongMoveFrom = null
        wrongMoveTo = null
    }

    fun setAnimatingMove(from: Square, to: Square, piece: Piece) {
        animatingPiece = piece
        animateFromSquare = from
        animateToSquare = to
    }

    fun clearAnimation() {
        animatingPiece = null
        animateFromSquare = null
        animateToSquare = null
    }

    fun clearSelection() {
        selectedSquare = null
        legalMovesFromSelected = emptyList()
    }

    fun getLegalMovesFrom(square: Square): List<Move> {
        return board.legalMoves().filter { it.from == square }
    }

    fun getPieceAt(square: Square): Piece = board.getPiece(square)

    fun getSideToMove(): Side = board.sideToMove

    fun getKingSquare(side: Side): Square {
        return Square.entries.first { sq ->
            val piece = board.getPiece(sq)
            piece == if (side == Side.WHITE) Piece.WHITE_KING else Piece.BLACK_KING
        }
    }
}
