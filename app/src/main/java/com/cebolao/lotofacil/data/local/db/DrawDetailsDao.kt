package com.cebolao.lotofacil.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DrawDetailsDao {
    @Query("SELECT * FROM draw_details WHERE contestNumber = :contestNumber")
    suspend fun getDrawDetails(contestNumber: Int): DrawDetailsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetails(details: DrawDetailsEntity)
}
