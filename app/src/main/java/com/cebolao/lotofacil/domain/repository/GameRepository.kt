package com.cebolao.lotofacil.domain.repository

import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.LotofacilGame
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow

interface GameRepository {
    /**
     * Generated games not yet pinned.
     */
    val unpinnedGames: StateFlow<ImmutableList<LotofacilGame>>

    /**
     * Pinned games.
     */
    val pinnedGames: StateFlow<ImmutableList<LotofacilGame>>

    /**
     * Returns a game by its mask, or null if it doesn't exist.
     */
    suspend fun getGame(mask: Long): AppResult<LotofacilGame?>

    /**
     * Saves or updates a game.
     * Implementation should handle merging of fields not present in domain (id, source, seed).
     */
    suspend fun saveGame(game: LotofacilGame): AppResult<Unit>

    /**
     * Adds newly generated games to the "unpinned" list.
     */
    suspend fun addGeneratedGames(newGames: List<LotofacilGame>): AppResult<Unit>

    /**
     * Clears unpinned games.
     */
    suspend fun clearUnpinnedGames(): AppResult<Unit>

    /**
     * Removes a game.
     */
    suspend fun deleteGame(gameToDelete: LotofacilGame): AppResult<Unit>

    /**
     * Exports games to a text representation.
     */
    suspend fun exportGames(): AppResult<String>
}
