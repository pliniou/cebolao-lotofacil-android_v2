package com.cebolao.lotofacil.domain.repository

import com.cebolao.lotofacil.domain.model.AppResult
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
     * Retorna um jogo pelo seu mask, ou null se não existir.
     */
    suspend fun getGame(mask: Long): AppResult<LotofacilGame?>

    /**
     * Salva ou atualiza um jogo.
     * Implementação deve tratar merge de campos não presentes no domínio (id, source, seed).
     */
    suspend fun saveGame(game: LotofacilGame): AppResult<Unit>

    /**
     * Adiciona jogos recém-gerados à lista de "unpinned".
     */
    suspend fun addGeneratedGames(newGames: List<LotofacilGame>): AppResult<Unit>

    /**
     * Limpa jogos não fixados.
     */
    suspend fun clearUnpinnedGames(): AppResult<Unit>

    /**
     * Remove um jogo.
     */
    suspend fun deleteGame(gameToDelete: LotofacilGame): AppResult<Unit>

    /**
     * Exporta os jogos para uma representação textual.
     */
    suspend fun exportGames(): AppResult<String>
}
