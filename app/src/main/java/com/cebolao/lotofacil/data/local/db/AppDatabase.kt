package com.cebolao.lotofacil.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DrawEntity::class, CheckRunEntity::class, UserGameEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drawDao(): DrawDao
    abstract fun checkRunDao(): CheckRunDao
    abstract fun userGameDao(): UserGameDao
}
