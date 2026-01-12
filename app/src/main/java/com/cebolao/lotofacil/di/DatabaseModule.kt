package com.cebolao.lotofacil.di

import android.content.Context
import androidx.room.Room
import com.cebolao.lotofacil.data.local.db.AppDatabase
import com.cebolao.lotofacil.data.local.db.CheckRunDao
import com.cebolao.lotofacil.data.local.db.DrawDao
import com.cebolao.lotofacil.data.local.db.DrawDetailsDao
import com.cebolao.lotofacil.data.local.db.MIGRATION_1_6
import com.cebolao.lotofacil.data.local.db.MIGRATION_5_6
import com.cebolao.lotofacil.data.local.db.UserGameDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "lotofacil.db"
        )
        .addMigrations(MIGRATION_1_6, MIGRATION_5_6)
        .build()
    }

    @Provides
    fun provideDrawDao(database: AppDatabase): DrawDao = database.drawDao()

    @Provides
    fun provideCheckRunDao(database: AppDatabase): CheckRunDao = database.checkRunDao()

    @Provides
    fun provideUserGameDao(database: AppDatabase): UserGameDao = database.userGameDao()

    @Provides
    fun provideDrawDetailsDao(database: AppDatabase): DrawDetailsDao = database.drawDetailsDao()
}
