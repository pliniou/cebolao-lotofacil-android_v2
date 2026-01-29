package br.com.loterias.cebolaolotofacil.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for Lotofácil result caching
 */
@Entity(tableName = "lotofacil_results")
data class LotofacilResultEntity(
    @PrimaryKey
    @ColumnInfo(name = "concurso")
    val concurso: Int,

    @ColumnInfo(name = "dezenas")
    val dezenas: String, // Stored as comma-separated string

    @ColumnInfo(name = "data")
    val data: String,

    @ColumnInfo(name = "valor_acumulado")
    val valorAcumulado: Double? = null,

    @ColumnInfo(name = "valor_premio_principal")
    val valorPremioPrincipal: Double? = null,

    @ColumnInfo(name = "ganhadores")
    val ganhadores: Int? = null,

    @ColumnInfo(name = "data_proximo_sorteio")
    val dataProximoSorteio: String? = null,

    @ColumnInfo(name = "sync_timestamp")
    val syncTimestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false
)
