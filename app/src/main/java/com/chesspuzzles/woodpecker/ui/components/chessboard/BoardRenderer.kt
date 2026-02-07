package com.chesspuzzles.woodpecker.ui.components.chessboard

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.github.bhlangonijr.chesslib.Square

object BoardRenderer {

    val lightSquareColor = Color(0xFFF0D9B5)
    val darkSquareColor = Color(0xFFB58863)
    val selectedColor = Color(0x8014551E)
    val lastMoveColor = Color(0x80FFFF00)
    val legalMoveColor = Color(0x40000000)
    val checkColor = Color(0xCCFF0000)

    fun DrawScope.drawBoard(
        squareSize: Float,
        flipped: Boolean,
        selectedSquare: Square?,
        lastMoveFrom: Square?,
        lastMoveTo: Square?,
        legalMoveSquares: List<Square>,
        checkSquare: Square?
    ) {
        for (rank in 0 until 8) {
            for (file in 0 until 8) {
                val displayFile = if (flipped) 7 - file else file
                val displayRank = if (flipped) rank else 7 - rank

                val x = displayFile * squareSize
                val y = displayRank * squareSize

                val isLight = (file + rank) % 2 == 0
                val baseColor = if (isLight) lightSquareColor else darkSquareColor

                drawRect(
                    color = baseColor,
                    topLeft = Offset(x, y),
                    size = Size(squareSize, squareSize)
                )

                val square = Square.squareAt(file + rank * 8)

                // Last move highlight
                if (square == lastMoveFrom || square == lastMoveTo) {
                    drawRect(
                        color = lastMoveColor,
                        topLeft = Offset(x, y),
                        size = Size(squareSize, squareSize)
                    )
                }

                // Selected square highlight
                if (square == selectedSquare) {
                    drawRect(
                        color = selectedColor,
                        topLeft = Offset(x, y),
                        size = Size(squareSize, squareSize)
                    )
                }

                // Check highlight
                if (square == checkSquare) {
                    drawRect(
                        color = checkColor,
                        topLeft = Offset(x, y),
                        size = Size(squareSize, squareSize)
                    )
                }

                // Legal move indicators
                if (square in legalMoveSquares) {
                    val centerX = x + squareSize / 2
                    val centerY = y + squareSize / 2
                    val radius = squareSize * 0.15f
                    drawCircle(
                        color = legalMoveColor,
                        radius = radius,
                        center = Offset(centerX, centerY)
                    )
                }
            }
        }
    }

    fun squareToOffset(square: Square, squareSize: Float, flipped: Boolean): Offset {
        val file = square.file.ordinal
        val rank = square.rank.ordinal

        val displayFile = if (flipped) 7 - file else file
        val displayRank = if (flipped) rank else 7 - rank

        return Offset(
            x = displayFile * squareSize,
            y = displayRank * squareSize
        )
    }

    fun offsetToSquare(offset: Offset, squareSize: Float, flipped: Boolean): Square? {
        val col = (offset.x / squareSize).toInt()
        val row = (offset.y / squareSize).toInt()

        if (col !in 0..7 || row !in 0..7) return null

        val file = if (flipped) 7 - col else col
        val rank = if (flipped) row else 7 - row

        return try {
            Square.squareAt(file + rank * 8)
        } catch (e: Exception) {
            null
        }
    }
}
