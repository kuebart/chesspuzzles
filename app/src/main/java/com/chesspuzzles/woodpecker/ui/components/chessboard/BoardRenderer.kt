package com.chesspuzzles.woodpecker.ui.components.chessboard

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.github.bhlangonijr.chesslib.Square

object BoardRenderer {

    val lightSquareColor = Color(0xFFF0D9B5)
    val darkSquareColor = Color(0xFFB58863)
    val selectedColor = Color(0x8014551E)
    val lastMoveColor = Color(0x50FFA000)
    val legalMoveColor = Color(0x40000000)
    val wrongMoveColor = Color(0x60EF5350)
    val checkColor = Color(0xCCFF0000)

    fun DrawScope.drawBoard(
        squareSize: Float,
        flipped: Boolean,
        selectedSquare: Square?,
        lastMoveFrom: Square?,
        lastMoveTo: Square?,
        wrongMoveFrom: Square? = null,
        wrongMoveTo: Square? = null,
        legalMoveSquares: List<Square>,
        checkSquare: Square?,
        textMeasurer: TextMeasurer? = null,
        margin: Float = 0f,
        coordColor: Color = Color(0xFFCAC4D0)
    ) {
        val files = "abcdefgh"
        val ranks = "12345678"

        for (rank in 0 until 8) {
            for (file in 0 until 8) {
                val displayFile = if (flipped) 7 - file else file
                val displayRank = if (flipped) rank else 7 - rank

                val x = margin + displayFile * squareSize
                val y = margin + displayRank * squareSize

                // a1 is dark: (file + rank) even means dark
                val isLight = (file + rank) % 2 != 0
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

                // Wrong move highlight (overrides last move)
                if (square == wrongMoveFrom || square == wrongMoveTo) {
                    drawRect(
                        color = wrongMoveColor,
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

        // Draw coordinates outside the board on all four sides
        if (textMeasurer != null && margin > 0f) {
            val coordFontSize = (margin * 0.38f).coerceAtLeast(1f)
            val style = TextStyle(
                fontSize = coordFontSize.sp,
                fontWeight = FontWeight.Medium,
                color = coordColor
            )

            for (i in 0 until 8) {
                // File labels (a-h): top and bottom
                val fileIndex = if (flipped) 7 - i else i
                val fileLabel = files[fileIndex].toString()
                val fileResult = textMeasurer.measure(text = fileLabel, style = style)
                val fileCenterX = margin + i * squareSize + squareSize / 2 - fileResult.size.width / 2

                // Top
                drawText(
                    textLayoutResult = fileResult,
                    topLeft = Offset(fileCenterX, (margin - fileResult.size.height) / 2)
                )
                // Bottom
                drawText(
                    textLayoutResult = fileResult,
                    topLeft = Offset(fileCenterX, margin + 8 * squareSize + (margin - fileResult.size.height) / 2)
                )

                // Rank labels (1-8): left and right
                val rankIndex = if (flipped) i else 7 - i
                val rankLabel = ranks[rankIndex].toString()
                val rankResult = textMeasurer.measure(text = rankLabel, style = style)
                val rankCenterY = margin + i * squareSize + squareSize / 2 - rankResult.size.height / 2

                // Left
                drawText(
                    textLayoutResult = rankResult,
                    topLeft = Offset((margin - rankResult.size.width) / 2, rankCenterY)
                )
                // Right
                drawText(
                    textLayoutResult = rankResult,
                    topLeft = Offset(margin + 8 * squareSize + (margin - rankResult.size.width) / 2, rankCenterY)
                )
            }
        }
    }

    fun squareToOffset(square: Square, squareSize: Float, flipped: Boolean, margin: Float = 0f): Offset {
        val file = square.file.ordinal
        val rank = square.rank.ordinal

        val displayFile = if (flipped) 7 - file else file
        val displayRank = if (flipped) rank else 7 - rank

        return Offset(
            x = margin + displayFile * squareSize,
            y = margin + displayRank * squareSize
        )
    }

    fun offsetToSquare(offset: Offset, squareSize: Float, flipped: Boolean, margin: Float = 0f): Square? {
        val col = ((offset.x - margin) / squareSize).toInt()
        val row = ((offset.y - margin) / squareSize).toInt()

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
