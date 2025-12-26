package com.cebolao.lotofacil.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cebolao.lotofacil.data.datasource.HistoryLocalDataSource
import com.cebolao.lotofacil.data.datasource.HistoryRemoteDataSource
import com.cebolao.lotofacil.domain.model.Draw
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class HistoryRepositoryImplTest {

    private val localDataSource: HistoryLocalDataSource = mockk()
    private val remoteDataSource: HistoryRemoteDataSource = mockk()
    
    // We are testing Repository logic, effectively unit testing but using Android runner for consistency 
    // or if we needed Context (not needed here but good practice for integration layer).
    
    @Test
    fun getHistoryReturnsLocalDataIfAvailable() = runTest {
        val draw = Draw.fromNumbers(1, (1..15).toSet(), Date().time)
        val localHistory = listOf(draw)
        
        coEvery { localDataSource.getLocalHistory() } returns localHistory
        
        val repository = HistoryRepositoryImpl(localDataSource, remoteDataSource, this)
        
        val result = repository.getHistory()
        assertEquals(1, result.size)
        assertEquals(draw.contestNumber, result.first().contestNumber)
    }
}
