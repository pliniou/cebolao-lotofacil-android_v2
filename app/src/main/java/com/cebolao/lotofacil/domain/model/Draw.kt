package com.cebolao.lotofacil.domain.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * Representação pura de um sorteio da Lotofácil no domínio.
 */
@Stable
@Immutable
@ConsistentCopyVisibility
data class Draw private constructor(
    val contestNumber: Int,
    private val _mask: Long,
    val date: Long? = null
) : GameStatisticsProvider, BitmaskProvider {
    override val mask: Long get() = _mask
    override val numbers: Set<Int>
        get() = MaskUtils.toSet(_mask)

    companion object {
        fun fromNumbers(contestNumber: Int, numbers: Set<Int>, date: Long? = null): Draw {
            return Draw(contestNumber, MaskUtils.toMask(numbers), date)
        }

        fun fromMask(contestNumber: Int, mask: Long, date: Long? = null): Draw {
            return Draw(contestNumber, mask, date)
        }
    }
}
