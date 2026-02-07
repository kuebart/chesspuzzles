package com.chesspuzzles.woodpecker.ui.components.chessboard

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Square

data class DragState(
    val isDragging: Boolean = false,
    val piece: Piece = Piece.NONE,
    val fromSquare: Square? = null,
    val currentOffset: Offset = Offset.Zero
)

suspend fun PointerInputScope.handleTap(
    squareSize: Float,
    flipped: Boolean,
    onSquareTapped: (Square) -> Unit
) {
    detectTapGestures { offset ->
        val square = BoardRenderer.offsetToSquare(offset, squareSize, flipped)
        if (square != null) {
            onSquareTapped(square)
        }
    }
}

suspend fun PointerInputScope.handleDrag(
    squareSize: Float,
    flipped: Boolean,
    boardState: BoardState,
    onDragStateChanged: (DragState) -> Unit,
    onMoveAttempt: (Square, Square) -> Unit
) {
    detectDragGestures(
        onDragStart = { offset ->
            val square = BoardRenderer.offsetToSquare(offset, squareSize, flipped)
            if (square != null) {
                val piece = boardState.getPieceAt(square)
                if (piece != Piece.NONE && piece.pieceSide == boardState.getSideToMove()) {
                    boardState.selectSquare(square)
                    onDragStateChanged(
                        DragState(
                            isDragging = true,
                            piece = piece,
                            fromSquare = square,
                            currentOffset = offset
                        )
                    )
                }
            }
        },
        onDrag = { change, _ ->
            change.consume()
            onDragStateChanged(
                DragState(
                    isDragging = true,
                    piece = boardState.getPieceAt(boardState.selectedSquare ?: Square.NONE),
                    fromSquare = boardState.selectedSquare,
                    currentOffset = change.position
                )
            )
        },
        onDragEnd = {
            val dragState = DragState() // Will be replaced
            onDragStateChanged(DragState())
        },
        onDragCancel = {
            boardState.clearSelection()
            onDragStateChanged(DragState())
        }
    )
}
