package com.cebolao.lotofacil.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawDao {
    @Query("SELECT * FROM draws ORDER BY contestNumber DESC")
    fun getAllDraws(): Flow<List<DrawEntity>>
    
    @Query("SELECT * FROM draws ORDER BY contestNumber DESC")
    suspend fun getAllDrawsSnapshot(): List<DrawEntity>

    @Query("SELECT * FROM draws ORDER BY contestNumber DESC LIMIT 1")
    fun getLastDraw(): Flow<DrawEntity?>

    @Query("SELECT * FROM draws ORDER BY contestNumber DESC LIMIT 1")
    suspend fun getLastDrawSnapshot(): DrawEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(draws: List<DrawEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(draw: DrawEntity)

    @Query("DELETE FROM draws")
    suspend fun clearAll()
    
    @Query("SELECT COUNT(*) FROM draws")
    suspend fun count(): Int
}
