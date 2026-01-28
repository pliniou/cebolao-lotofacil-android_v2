package br.com.loterias.cebolaolotofacil.data

import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class LotofacilRepositoryImplTest {
    @Test
    fun `should emit mock result`() = runTest {
        val repository = LotofacilRepositoryImpl()
        val results = repository.getRecentResults().first()
        assertEquals(3041, results.first().concurso)
    }
}
