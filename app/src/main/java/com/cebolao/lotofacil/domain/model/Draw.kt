package com.cebolao.lotofacil.domain.model

/**
 * Representação pura de um sorteio da Lotofácil no domínio.
 */
@ConsistentCopyVisibility
data class Draw internal constructor(
    val contestNumber: Int,
    private val maskValue: Long,
    val date: Long? = null
) : GameStatisticsProvider, BitmaskProvider {
    override val mask: Long get() = maskValue
    override val numbers: Set<Int>
        get() = MaskUtils.toSet(maskValue)

    companion object {
        fun fromNumbers(contestNumber: Int, numbers: Set<Int>, date: Long? = null): Draw {
            return Draw(contestNumber, MaskUtils.toMask(numbers), date)
        }
    }
}
