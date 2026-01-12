package com.cebolao.lotofacil.domain.model

/** Marca que o provedor expõe uma máscara de bits interna para otimizações */
interface BitmaskProvider {
    val mask: Long
}
