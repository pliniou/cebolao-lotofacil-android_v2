package com.cebolao.lotofacil.data.repository

import com.cebolao.lotofacil.data.local.db.UserGameDao
import com.cebolao.lotofacil.data.mapper.toLotofacilGame
import com.cebolao.lotofacil.data.mapper.toUserGameEntity
import com.cebolao.lotofacil.di.ApplicationScope
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.repository.GameRepository
import com.cebolao.lotofacil.domain.util.Logger
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
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
    private val json: Json,
    private val logger: Logger
) : GameRepository {

    private companion object {
        const val TAG = "GameRepository"
    }

    override val unpinnedGames: StateFlow<ImmutableList<LotofacilGame>> = userGameDao
        .getUnpinnedGames()
        .map { entities -> entities.map { it.toLotofacilGame() }.toImmutableList() }
        .catch { e ->
            logger.error(TAG, "Failed to observe unpinned games", e)
            emit(persistentListOf())
        }
        .flowOn(ioDispatcher)
        .stateIn(scope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), persistentListOf())

    override val pinnedGames: StateFlow<ImmutableList<LotofacilGame>> = userGameDao
        .getPinnedGames()
        .map { entities -> entities.map { it.toLotofacilGame() }.toImmutableList() }
        .catch { e ->
            logger.error(TAG, "Failed to observe pinned games", e)
            emit(persistentListOf())
        }
        .flowOn(ioDispatcher)
        .stateIn(scope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), persistentListOf())

    override suspend fun addGeneratedGames(newGames: List<LotofacilGame>) {
        if (newGames.isEmpty()) return
        withContext(ioDispatcher) {
            runCatching {
                val inputMasks = newGames.map { it.mask }
                val existingMasks = userGameDao.getExistingMasks(inputMasks).toSet()
                val gamesToInsert = newGames.filter { it.mask !in existingMasks }
                if (gamesToInsert.isNotEmpty()) {
                    val entities = gamesToInsert.map { game ->
                        game.toUserGameEntity(source = "generated", seed = null, json = json)
                    }
                    userGameDao.insertAll(entities)
                }
            }.onFailure { e ->
                logger.error(TAG, "Failed to insert generated games", e)
            }
        }
    }

    override suspend fun clearUnpinnedGames() {
        withContext(ioDispatcher) {
            runCatching { userGameDao.deleteAllUnpinned() }
                .onFailure { e -> logger.error(TAG, "Failed to clear unpinned games", e) }
        }
    }

    override suspend fun getGame(mask: Long): LotofacilGame? = withContext(ioDispatcher) {
        runCatching { userGameDao.getGameByMask(mask)?.toLotofacilGame() }
            .onFailure { e -> logger.error(TAG, "Failed to fetch game mask=$mask", e) }
            .getOrNull()
    }

    override suspend fun saveGame(game: LotofacilGame) {
        withContext(ioDispatcher) {
            runCatching {
                val existing = userGameDao.getGameByMask(game.mask)
                if (existing != null) {
                    val updated = existing.copy(pinned = game.isPinned)
                    userGameDao.update(updated)
                } else {
                    userGameDao.insert(game.toUserGameEntity(source = "manual", json = json))
                }
            }.onFailure { e -> logger.error(TAG, "Failed to save game mask=${game.mask}", e) }
        }
    }

    override suspend fun deleteGame(gameToDelete: LotofacilGame) {
        withContext(ioDispatcher) {
            runCatching { userGameDao.deleteByMask(gameToDelete.mask) }
                .onFailure { e -> logger.error(TAG, "Failed to delete game mask=${gameToDelete.mask}", e) }
        }
    }

    override suspend fun exportGames(): String = withContext(ioDispatcher) {
        runCatching {
            val allEntities = userGameDao.getAllGames().first()
            val allGames = allEntities.map { it.toLotofacilGame() }
            json.encodeToString(allGames)
        }.onFailure { e ->
            logger.error(TAG, "Failed to export games", e)
        }.getOrDefault("[]")
    }
}
