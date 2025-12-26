package com.cebolao.lotofacil.domain.model

fun Long.toNumbers(): Set<Int> = MaskUtils.toSet(this)
fun Set<Int>.toMask(): Long = MaskUtils.toMask(this)
