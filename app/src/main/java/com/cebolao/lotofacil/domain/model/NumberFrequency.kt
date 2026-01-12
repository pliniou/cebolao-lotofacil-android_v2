package com.cebolao.lotofacil.domain.model

import android.annotation.SuppressLint
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Immutable
@Serializable
data class NumberFrequency(
    val number: Int,
    val frequency: Int
)
