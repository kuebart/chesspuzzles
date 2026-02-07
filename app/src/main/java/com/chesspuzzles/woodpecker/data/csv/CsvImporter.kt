package com.chesspuzzles.woodpecker.data.csv

import android.content.Context
import android.util.Log
import com.chesspuzzles.woodpecker.data.local.AppDatabase
import com.chesspuzzles.woodpecker.data.local.entity.PuzzleEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvImporter @Inject constructor(
    private val db: AppDatabase,
    @ApplicationContext private val context: Context
) {
    suspend fun importIfNeeded() {
        if (db.puzzleDao().count() > 0) return

        Log.d(TAG, "Starting CSV import...")
        var imported = 0

        context.assets.open("puzzles.csv").bufferedReader().useLines { lines ->
            lines.drop(1) // Skip header
                .chunked(1000)
                .forEach { batch ->
                    val entities = batch.mapNotNull { line ->
                        try {
                            parseLine(line)
                        } catch (e: Exception) {
                            Log.w(TAG, "Skipping malformed line: ${e.message}")
                            null
                        }
                    }
                    db.puzzleDao().insertAll(entities)
                    imported += entities.size
                    Log.d(TAG, "Imported $imported puzzles...")
                }
        }

        Log.d(TAG, "CSV import complete: $imported puzzles imported")
    }

    private fun parseLine(line: String): PuzzleEntity {
        val cols = line.split(",")
        return PuzzleEntity(
            id = cols[0],
            fen = cols[1],
            moves = cols[2],
            rating = cols[3].toInt(),
            themes = cols.getOrElse(7) { "" },
            popularity = cols.getOrElse(5) { "0" }.toInt()
        )
    }

    companion object {
        private const val TAG = "CsvImporter"
    }
}
