package com.cebolao.lotofacil.data.local.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "draws",
    indices = [
        Index(value = ["contestNumber"], unique = true),
        Index(value = ["date"])
    ]
)

data class DrawEntity(
    @PrimaryKey val contestNumber: Int,
    val numbers: String,
    val date: Long?
)
