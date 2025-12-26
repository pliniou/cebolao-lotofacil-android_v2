package com.cebolao.lotofacil.data.repository

import com.cebolao.lotofacil.data.local.db.UserGameDao
import com.cebolao.lotofacil.data.local.db.toLotofacilGame
import com.cebolao.lotofacil.data.local.db.toUserGameEntity
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

private const val TAG = "GameRepository"

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
        val entities = newGames.map { game ->
            // Verificar se já existe jogo com mesmo mask
            val existing = userGameDao.getGameByMask(game.mask)
            if (existing == null) {
                game.toUserGameEntity(
                    source = "generated",
                    seed = null, // Seed pode ser passado no futuro se necessário
                    json = json
                )
            } else {
                null // Já existe, não adicionar duplicado
            }
        }.filterNotNull()
        
        if (entities.isNotEmpty()) {
            userGameDao.insertAll(entities)
        }
    }

    override suspend fun clearUnpinnedGames() = withContext(ioDispatcher) {
        userGameDao.deleteAllUnpinned()
    }

    override suspend fun togglePinState(gameToToggle: LotofacilGame) = withContext(ioDispatcher) {
        val entity = userGameDao.getGameByMask(gameToToggle.mask)
        if (entity != null) {
            val updated = entity.copy(pinned = !entity.pinned)
            userGameDao.update(updated)
        } else {
            // Se não existe, criar como pinned/unpinned conforme o estado desejado
            val newEntity = gameToToggle.toUserGameEntity(
                source = "manual",
                pinned = !gameToToggle.isPinned,
                json = json
            )
            userGameDao.insert(newEntity)
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
