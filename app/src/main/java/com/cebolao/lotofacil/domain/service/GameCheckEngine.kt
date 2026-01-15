package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.domain.model.CheckResult
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.model.MaskUtils
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

/**
 * Engine otimizado para verificar jogos contra historico usando bitmasks.
 * Operacoes de intersecao sao O(1) usando bitwise em vez de O(n) com Sets.
 */
@Singleton
class GameCheckEngine @Inject constructor() {

    /**
     * Calcula o resultado da verificacao de um jogo contra o historico de sorteios.
     * Usa bitmasks para otimizar operacoes de intersecao.
     */
    fun checkGame(game: LotofacilGame, history: List<Draw>): CheckResult {
        if (history.isEmpty()) {
            return CheckResult(
                scoreCounts = persistentMapOf(),
                lastHitContest = null,
                lastHitScore = null,
                lastCheckedContest = 0,
                recentHits = persistentListOf()
            )
        }

        val gameMask = game.mask
        val scoreCountsMutable = HashMap<Int, Int>()
        var lastHitContest: Int? = null
        var lastHitScore: Int? = null

        val recentWindow = min(RECENT_HITS_WINDOW, history.size)
        val recentBuffer = ArrayList<Pair<Int, Int>>(recentWindow)

        for ((index, draw) in history.withIndex()) {
            val hits = MaskUtils.intersectCount(gameMask, draw.mask)
            val contestNumber = draw.contestNumber

            if (hits >= GameConstants.MIN_PRIZE_SCORE) {
                scoreCountsMutable[hits] = (scoreCountsMutable[hits] ?: 0) + 1
                if (lastHitContest == null) {
                    lastHitContest = contestNumber
                    lastHitScore = hits
                }
            }

            if (index < recentWindow) {
                recentBuffer.add(contestNumber to hits)
            }
        }

        val scoreCounts: ImmutableMap<Int, Int> = scoreCountsMutable.toImmutableMap()
        val recentHits: ImmutableList<Pair<Int, Int>> = recentBuffer.asReversed().toImmutableList()

        return CheckResult(
            scoreCounts = scoreCounts,
            lastHitContest = lastHitContest,
            lastHitScore = lastHitScore,
            lastCheckedContest = history.firstOrNull()?.contestNumber ?: 0,
            recentHits = recentHits
        )
    }

    private companion object {
        const val RECENT_HITS_WINDOW = 100
    }
}
