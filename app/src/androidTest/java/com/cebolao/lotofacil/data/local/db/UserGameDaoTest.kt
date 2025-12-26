package com.cebolao.lotofacil.data.local.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cebolao.lotofacil.domain.model.LotofacilGame
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserGameDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: UserGameDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.userGameDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetGames() = runBlocking {
        val game = LotofacilGame.fromNumbers((1..15).toSet())
        val entity = game.toUserGameEntity(json = Json)

        // Inserir
        dao.insert(entity)

        // Consultar todos os jogos
        val games = dao.getAllGames().first()
        assertEquals(1, games.size)
        // Checar números
        val savedGame = games.first()
        assertEquals(game.numbers, savedGame.toLotofacilGame().numbers)
    }
}
