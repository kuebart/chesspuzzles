package com.chesspuzzles.woodpecker.ui.components.chessboard

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.chesspuzzles.woodpecker.R
import com.github.bhlangonijr.chesslib.Piece

object PieceRenderer {

    private val pieceDrawableMap = mapOf(
        Piece.WHITE_PAWN to R.drawable.piece_wp,
        Piece.WHITE_KNIGHT to R.drawable.piece_wn,
        Piece.WHITE_BISHOP to R.drawable.piece_wb,
        Piece.WHITE_ROOK to R.drawable.piece_wr,
        Piece.WHITE_QUEEN to R.drawable.piece_wq,
        Piece.WHITE_KING to R.drawable.piece_wk,
        Piece.BLACK_PAWN to R.drawable.piece_bp,
        Piece.BLACK_KNIGHT to R.drawable.piece_bn,
        Piece.BLACK_BISHOP to R.drawable.piece_bb,
        Piece.BLACK_ROOK to R.drawable.piece_br,
        Piece.BLACK_QUEEN to R.drawable.piece_bq,
        Piece.BLACK_KING to R.drawable.piece_bk,
    )

    fun getDrawable(context: Context, piece: Piece): Drawable? {
        val resId = pieceDrawableMap[piece] ?: return null
        return ContextCompat.getDrawable(context, resId)
    }

    fun getDrawableRes(piece: Piece): Int? = pieceDrawableMap[piece]
}
