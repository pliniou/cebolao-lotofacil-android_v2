package com.cebolao.lotofacil.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckRunDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(checkRun: CheckRunEntity): Long

    @Query("SELECT * FROM check_runs ORDER BY createdAt DESC")
    fun getAllCheckRuns(): Flow<List<CheckRunEntity>>

    @Query("SELECT * FROM check_runs ORDER BY createdAt DESC")
    suspend fun getAllCheckRunsSnapshot(): List<CheckRunEntity>

    @Query("SELECT * FROM check_runs WHERE id = :id")
    suspend fun getCheckRunById(id: Long): CheckRunEntity?

    @Query("SELECT * FROM check_runs WHERE sourceHash = :hash ORDER BY createdAt DESC LIMIT 1")
    suspend fun getCheckRunByHash(hash: String): CheckRunEntity?

    @Query("SELECT * FROM check_runs WHERE lotteryId = :lotteryId ORDER BY createdAt DESC")
    fun getCheckRunsByLottery(lotteryId: String): Flow<List<CheckRunEntity>>

    @Query("SELECT * FROM check_runs ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentCheckRuns(limit: Int): List<CheckRunEntity>

    @Query("DELETE FROM check_runs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM check_runs")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM check_runs")
    suspend fun count(): Int
}
