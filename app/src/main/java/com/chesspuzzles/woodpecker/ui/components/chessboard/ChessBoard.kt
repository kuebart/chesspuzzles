package com.chesspuzzles.woodpecker.ui.components.chessboard

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Square

@Composable
fun ChessBoard(
    boardState: BoardState,
    enabled: Boolean = true,
    onMoveAttempt: (from: Square, to: Square) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var dragState by remember { mutableStateOf(DragState()) }
    val textMeasurer = rememberTextMeasurer()
    val coordColor = MaterialTheme.colorScheme.onSurfaceVariant

    // Cache drawables
    val pieceDrawables = remember {
        val map = mutableMapOf<Piece, Drawable>()
        Piece.entries.forEach { piece ->
            if (piece != Piece.NONE) {
                PieceRenderer.getDrawable(context, piece)?.let { map[piece] = it }
            }
        }
        map
    }

    // Force recomposition on board version change
    val version = boardState.boardVersion

    val legalMoveSquares = remember(boardState.legalMovesFromSelected) {
        boardState.legalMovesFromSelected.map { it.to }
    }

    val checkSquare = remember(boardState.isCheck, version) {
        if (boardState.isCheck) {
            try {
                boardState.getKingSquare(boardState.getSideToMove())
            } catch (e: Exception) {
                null
            }
        } else null
    }

    val boardShape = RoundedCornerShape(8.dp)

    Surface(
        modifier = modifier,
        shape = boardShape,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
                .clip(boardShape)
                .then(
                    if (enabled) {
                        Modifier
                            .pointerInput(boardState.flipped, version) {
                                val margin = size.width * 0.025f
                                val squareSize = (size.width - 2 * margin) / 8f
                                detectTapGestures { offset ->
                                    val square = BoardRenderer.offsetToSquare(
                                        offset, squareSize, boardState.flipped, margin
                                    )
                                    if (square != null) {
                                        val selectedSq = boardState.selectedSquare
                                        if (selectedSq != null &&
                                            boardState.legalMovesFromSelected.any { it.to == square }
                                        ) {
                                            onMoveAttempt(selectedSq, square)
                                        } else {
                                            boardState.selectSquare(square)
                                        }
                                    }
                                }
                            }
                            .pointerInput(boardState.flipped, version) {
                                val margin = size.width * 0.025f
                                val squareSize = (size.width - 2 * margin) / 8f
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        val square = BoardRenderer.offsetToSquare(
                                            offset, squareSize, boardState.flipped, margin
                                        )
                                        if (square != null) {
                                            val piece = boardState.getPieceAt(square)
                                            if (piece != Piece.NONE &&
                                                piece.pieceSide == boardState.getSideToMove()
                                            ) {
                                                boardState.selectSquare(square)
                                                dragState = DragState(
                                                    isDragging = true,
                                                    piece = piece,
                                                    fromSquare = square,
                                                    currentOffset = offset
                                                )
                                            }
                                        }
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        dragState = dragState.copy(
                                            currentOffset = change.position
                                        )
                                    },
                                    onDragEnd = {
                                        if (dragState.isDragging && dragState.fromSquare != null) {
                                            val toSquare = BoardRenderer.offsetToSquare(
                                                dragState.currentOffset,
                                                squareSize,
                                                boardState.flipped,
                                                margin
                                            )
                                            if (toSquare != null && toSquare != dragState.fromSquare) {
                                                onMoveAttempt(dragState.fromSquare!!, toSquare)
                                            }
                                        }
                                        dragState = DragState()
                                        boardState.clearSelection()
                                    },
                                    onDragCancel = {
                                        dragState = DragState()
                                        boardState.clearSelection()
                                    }
                                )
                            }
                    } else {
                        Modifier
                    }
                )
        ) {
            val margin = size.width * 0.025f
            val squareSize = (size.width - 2 * margin) / 8f

            with(BoardRenderer) {
                drawBoard(
                    squareSize = squareSize,
                    flipped = boardState.flipped,
                    selectedSquare = boardState.selectedSquare,
                    lastMoveFrom = boardState.lastMoveFrom,
                    lastMoveTo = boardState.lastMoveTo,
                    legalMoveSquares = if (enabled) legalMoveSquares else emptyList(),
                    checkSquare = checkSquare,
                    textMeasurer = textMeasurer,
                    margin = margin,
                    coordColor = coordColor
                )
            }

            // Draw pieces
            for (sq in Square.entries) {
                if (sq == Square.NONE) continue

                // Skip dragged piece at its original position
                if (dragState.isDragging && sq == dragState.fromSquare) continue

                val piece = boardState.getPieceAt(sq)
                if (piece == Piece.NONE) continue

                val drawable = pieceDrawables[piece] ?: continue
                val offset = BoardRenderer.squareToOffset(sq, squareSize, boardState.flipped, margin)
                drawPiece(drawable, offset, squareSize)
            }

            // Draw dragged piece at finger position
            if (dragState.isDragging && dragState.piece != Piece.NONE) {
                val drawable = pieceDrawables[dragState.piece]
                if (drawable != null) {
                    val offset = Offset(
                        dragState.currentOffset.x - squareSize / 2,
                        dragState.currentOffset.y - squareSize / 2
                    )
                    drawPiece(drawable, offset, squareSize * 1.2f) // Slightly larger while dragging
                }
            }
        }
    }
}

private fun DrawScope.drawPiece(drawable: Drawable, offset: Offset, size: Float) {
    val bitmap = createBitmap(size.toInt().coerceAtLeast(1), size.toInt().coerceAtLeast(1))
    val canvas = android.graphics.Canvas(bitmap)
    drawable.setBounds(0, 0, size.toInt(), size.toInt())
    drawable.draw(canvas)
    drawImage(
        image = bitmap.asImageBitmap(),
        topLeft = offset
    )
}
