package com.chesspuzzles.woodpecker.di

import android.content.Context
import androidx.room.Room
import com.chesspuzzles.woodpecker.data.local.AppDatabase
import com.chesspuzzles.woodpecker.data.local.dao.CycleDao
import com.chesspuzzles.woodpecker.data.local.dao.PuzzleDao
import com.chesspuzzles.woodpecker.data.local.dao.SuiteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "woodpecker.db"
        ).addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3).build()
    }

    @Provides
    fun providePuzzleDao(db: AppDatabase): PuzzleDao = db.puzzleDao()

    @Provides
    fun provideSuiteDao(db: AppDatabase): SuiteDao = db.suiteDao()

    @Provides
    fun provideCycleDao(db: AppDatabase): CycleDao = db.cycleDao()

}
