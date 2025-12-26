package com.cebolao.lotofacil.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "draws")
data class DrawEntity(
    @PrimaryKey val contestNumber: Int,
    val numbers: String,
    val date: Long?
)
