package com.cebolao.lotofacil.domain.repository

import com.cebolao.lotofacil.domain.model.LotofacilGame
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow

interface GameRepository {
    /**
     * Jogos gerados ainda não fixados.
     */
    val unpinnedGames: StateFlow<ImmutableList<LotofacilGame>>

    /**
     * Jogos fixados (pinned).
     */
    val pinnedGames: StateFlow<ImmutableList<LotofacilGame>>

    /**
     * Adiciona jogos recém-gerados à lista de "unpinned".
     */
    suspend fun addGeneratedGames(newGames: List<LotofacilGame>)

    /**
     * Limpa jogos não fixados.
     */
    suspend fun clearUnpinnedGames()

    /**
     * Alterna estado de pin/unpin do jogo.
     */
    suspend fun togglePinState(gameToToggle: LotofacilGame)

    /**
     * Remove um jogo (pinned ou unpinned, conforme regra da implementação).
     */
    suspend fun deleteGame(gameToDelete: LotofacilGame)

    /**
     * Exporta os jogos para uma representação textual.
     */
    suspend fun exportGames(): String
}
