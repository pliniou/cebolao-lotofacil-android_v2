package com.cebolao.lotofacil.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserGameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(game: UserGameEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<UserGameEntity>)

    @Update
    suspend fun update(game: UserGameEntity)

    @Query("DELETE FROM user_games WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM user_games WHERE numbersMask = :mask")
    suspend fun deleteByMask(mask: Long)

    @Query("SELECT * FROM user_games WHERE pinned = 1 ORDER BY createdAt DESC")
    fun getPinnedGames(): Flow<List<UserGameEntity>>

    @Query("SELECT * FROM user_games WHERE pinned = 0 ORDER BY createdAt DESC")
    fun getUnpinnedGames(): Flow<List<UserGameEntity>>

    @Query("SELECT * FROM user_games ORDER BY createdAt DESC")
    fun getAllGames(): Flow<List<UserGameEntity>>

    @Query("SELECT * FROM user_games WHERE id = :id")
    suspend fun getGameById(id: String): UserGameEntity?

    @Query("SELECT * FROM user_games WHERE numbersMask = :mask LIMIT 1")
    suspend fun getGameByMask(mask: Long): UserGameEntity?

    @Query("SELECT COUNT(*) FROM user_games")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM user_games WHERE pinned = 1")
    suspend fun countPinned(): Int

    @Query("SELECT COUNT(*) FROM user_games WHERE pinned = 0")
    suspend fun countUnpinned(): Int

    @Query("DELETE FROM user_games WHERE pinned = 0")
    suspend fun deleteAllUnpinned()

    @Query("DELETE FROM user_games")
    suspend fun deleteAll()
}
