package com.chesspuzzles.woodpecker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.chesspuzzles.woodpecker.data.local.entity.SuiteEntity
import com.chesspuzzles.woodpecker.data.local.entity.SuitePuzzleCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface SuiteDao {

    @Insert
    suspend fun insertSuite(suite: SuiteEntity): Long

    @Insert
    suspend fun insertSuitePuzzles(crossRefs: List<SuitePuzzleCrossRef>)

    @Query("SELECT * FROM suites ORDER BY createdAt DESC")
    fun observeAllSuites(): Flow<List<SuiteEntity>>

    @Query("SELECT * FROM suites WHERE id = :suiteId")
    suspend fun getSuiteById(suiteId: Long): SuiteEntity?

    @Query("SELECT * FROM suites WHERE id = :suiteId")
    fun observeSuiteById(suiteId: Long): Flow<SuiteEntity?>

    @Query("DELETE FROM suites WHERE id = :suiteId")
    suspend fun deleteSuite(suiteId: Long)

    @Query("UPDATE suites SET name = :name WHERE id = :suiteId")
    suspend fun updateSuiteName(suiteId: Long, name: String)

    @Query("UPDATE suites SET lastAccessedAt = :timestamp WHERE id = :suiteId")
    suspend fun updateLastAccessedAt(suiteId: Long, timestamp: Long)

    @Query("UPDATE suites SET groupId = :groupId WHERE id = :suiteId")
    suspend fun updateGroupId(suiteId: Long, groupId: Long)

    @Query("SELECT MAX(version) FROM suites WHERE groupId = :groupId")
    suspend fun getMaxVersionInGroup(groupId: Long): Int?

    @Query("UPDATE suites SET name = :name WHERE groupId = :groupId")
    suspend fun updateGroupName(groupId: Long, name: String)

    @Query("SELECT COUNT(*) FROM suites")
    fun observeSuiteCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM suites")
    suspend fun getSuiteCount(): Int

    @Query("SELECT id FROM suites")
    suspend fun getAllSuiteIds(): List<Long>

    @Transaction
    suspend fun createSuiteWithPuzzles(suite: SuiteEntity, puzzleIds: List<String>): Long {
        val suiteId = insertSuite(suite)
        val crossRefs = puzzleIds.mapIndexed { index, puzzleId ->
            SuitePuzzleCrossRef(
                suiteId = suiteId,
                puzzleId = puzzleId,
                orderIndex = index
            )
        }
        insertSuitePuzzles(crossRefs)
        return suiteId
    }
}
