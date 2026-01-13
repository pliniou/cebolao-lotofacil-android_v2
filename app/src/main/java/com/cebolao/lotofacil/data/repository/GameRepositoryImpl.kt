package com.cebolao.lotofacil.data.repository

import com.cebolao.lotofacil.data.local.db.UserGameDao
import com.cebolao.lotofacil.data.mapper.toLotofacilGame
import com.cebolao.lotofacil.data.mapper.toUserGameEntity
import com.cebolao.lotofacil.di.ApplicationScope
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.repository.GameRepository
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val userGameDao: UserGameDao,
    @param:ApplicationScope private val scope: CoroutineScope,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val json: Json
) : GameRepository {

    override val unpinnedGames: StateFlow<ImmutableList<LotofacilGame>> = userGameDao
        .getUnpinnedGames()
        .map { entities ->
            entities.map { it.toLotofacilGame() }.toImmutableList()
        }
        .flowOn(ioDispatcher)
        .stateIn(scope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), persistentListOf())

    override val pinnedGames: StateFlow<ImmutableList<LotofacilGame>> = userGameDao
        .getPinnedGames()
        .map { entities ->
            entities.map { it.toLotofacilGame() }.toImmutableList()
        }
        .flowOn(ioDispatcher)
        .stateIn(scope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), persistentListOf())

    override suspend fun addGeneratedGames(newGames: List<LotofacilGame>) = withContext(ioDispatcher) {
        if (newGames.isEmpty()) return@withContext

        // 1. Gather all masks from input
        val inputMasks = newGames.map { it.mask }

        // 2. Fetch existing masks from DB in one query
        val existingMasks = userGameDao.getExistingMasks(inputMasks).toSet()

        // 3. Filter out games that already exist
        val gamesToInsert = newGames.filter { game ->
            game.mask !in existingMasks
        }

        // 4. Transform to entities and bulk insert
        if (gamesToInsert.isNotEmpty()) {
            val entities = gamesToInsert.map { game ->
                game.toUserGameEntity(
                    source = "generated",
                    seed = null,
                    json = json
                )
            }
            userGameDao.insertAll(entities)
        }
    }

    override suspend fun clearUnpinnedGames() = withContext(ioDispatcher) {
        userGameDao.deleteAllUnpinned()
    }

    override suspend fun getGame(mask: Long): LotofacilGame? = withContext(ioDispatcher) {
        userGameDao.getGameByMask(mask)?.toLotofacilGame()
    }

    override suspend fun saveGame(game: LotofacilGame) = withContext(ioDispatcher) {
        val existing = userGameDao.getGameByMask(game.mask)
        if (existing != null) {
            // Merge logic: preserve ID, source, seed, etc.
            // Only update fields managed by Domain (pinned state, mainly)
            val updated = existing.copy(
                pinned = game.isPinned
                // Note: timestamp in existing is likely creation time.
                // If game.creationTimestamp is different, deciding which to keep is tricky.
                // Usually creation time shouldn't change.
            )
            userGameDao.update(updated)
        } else {
            // New game: use default source "manual" unless we want to param it.
            // Domain layer creating a game usually means Manual or Generated.
            // If it came from generator, it should have been added via addGeneratedGames?
            // If it's a manual toggle of a ghost game, it becomes manual.
            userGameDao.insert(
                game.toUserGameEntity(
                    source = "manual",
                    json = json
                )
            )
        }
        Unit
    }

    override suspend fun deleteGame(gameToDelete: LotofacilGame) = withContext(ioDispatcher) {
        userGameDao.deleteByMask(gameToDelete.mask)
    }

    override suspend fun exportGames(): String = withContext(ioDispatcher) {
        val allEntities = userGameDao.getAllGames().first()
        val allGames = allEntities.map { it.toLotofacilGame() }
        json.encodeToString(allGames)
    }
}
