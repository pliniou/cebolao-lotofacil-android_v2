package br.com.loterias.cebolaolotofacil.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.loterias.cebolaolotofacil.data.local.entity.LotofacilResultEntity

/**
 * Room database for caching Lotofácil results locally
 */
@Database(
    entities = [LotofacilResultEntity::class],
    version = 1,
    exportSchema = true
)
abstract class LotofacilDatabase : RoomDatabase() {
    abstract fun lotofacilResultDao(): LotofacilResultDao

    companion object {
        const val DATABASE_NAME = "lotofacil.db"
    }
}
