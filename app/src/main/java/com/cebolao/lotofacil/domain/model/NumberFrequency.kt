package com.cebolao.lotofacil.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class NumberFrequency(
    val number: Int,
    val frequency: Int
)
