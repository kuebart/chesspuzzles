package com.chesspuzzles.woodpecker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.chesspuzzles.woodpecker.data.local.converter.Converters
import com.chesspuzzles.woodpecker.data.local.dao.CycleDao
import com.chesspuzzles.woodpecker.data.local.dao.PuzzleDao
import com.chesspuzzles.woodpecker.data.local.dao.SuiteDao
import com.chesspuzzles.woodpecker.data.local.entity.CycleEntity
import com.chesspuzzles.woodpecker.data.local.entity.PuzzleAttemptEntity
import com.chesspuzzles.woodpecker.data.local.entity.PuzzleEntity
import com.chesspuzzles.woodpecker.data.local.entity.SuiteEntity
import com.chesspuzzles.woodpecker.data.local.entity.SuitePuzzleCrossRef

@Database(
    entities = [
        PuzzleEntity::class,
        SuiteEntity::class,
        SuitePuzzleCrossRef::class,
        CycleEntity::class,
        PuzzleAttemptEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun puzzleDao(): PuzzleDao
    abstract fun suiteDao(): SuiteDao
    abstract fun cycleDao(): CycleDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE suites ADD COLUMN lastAccessedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE suites SET lastAccessedAt = createdAt")
            }
        }
    }
}
