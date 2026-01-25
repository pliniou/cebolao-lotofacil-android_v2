package com.cebolao.lotofacil.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [DrawEntity::class, CheckRunEntity::class, UserGameEntity::class, DrawDetailsEntity::class], version = 7, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drawDao(): DrawDao
    abstract fun checkRunDao(): CheckRunDao
    abstract fun userGameDao(): UserGameDao
    abstract fun drawDetailsDao(): DrawDetailsDao
}
