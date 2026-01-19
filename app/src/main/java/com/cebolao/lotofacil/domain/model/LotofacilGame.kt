package com.cebolao.lotofacil.domain.model

import com.cebolao.lotofacil.domain.GameConstants
import kotlinx.serialization.Serializable

@Serializable
@ConsistentCopyVisibility
data class LotofacilGame internal constructor(
    private val _mask: Long,
    val isPinned: Boolean = false,
    val creationTimestamp: Long = System.currentTimeMillis()
) : GameStatisticsProvider, BitmaskProvider {
    override val mask: Long get() = _mask

    override val numbers: Set<Int>
        get() = MaskUtils.toSet(_mask)

    init {
        require(MaskUtils.popcount(_mask) == GameConstants.GAME_SIZE) {
            "Um jogo deve ter ${GameConstants.GAME_SIZE} números."
        }
        for (n in numbers) require(n in GameConstants.NUMBER_RANGE)
    }

    companion object {
        fun fromNumbers(numbers: Set<Int>, isPinned: Boolean = false): LotofacilGame {
            val mask = MaskUtils.toMask(numbers)
            return LotofacilGame(mask, isPinned)
        }

        fun fromMask(mask: Long, isPinned: Boolean = false, timestamp: Long = System.currentTimeMillis()): LotofacilGame {
            return LotofacilGame(mask, isPinned, timestamp)
        }
    }

    override fun repeatedFrom(lastDraw: Set<Int>?): Int {
        val base = lastDraw ?: return 0
        val lastDrawMask = MaskUtils.toMask(base)
        return MaskUtils.intersectCount(_mask, lastDrawMask)
    }
}
