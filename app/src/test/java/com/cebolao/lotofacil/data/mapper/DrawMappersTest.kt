package com.cebolao.lotofacil.data.mapper

import com.cebolao.lotofacil.data.local.db.DrawEntity
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.model.MaskUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class DrawMappersTest {

    @Test
    fun `DrawEntity toDraw parses csv without allocations heavy split`() {
        val entity = DrawEntity(
            contestNumber = 123,
            numbers = " 1, 2 ,03,25 ",
            date = 1700000000000L
        )

        val draw = entity.toDraw()
        val expectedMask = MaskUtils.toMask(setOf(1, 2, 3, 25))

        assertEquals(123, draw.contestNumber)
        assertEquals(expectedMask, draw.mask)
        assertEquals(entity.date, draw.date)
    }

    @Test
    fun `Draw toEntity emits canonical sorted csv`() {
        val draw = Draw.fromNumbers(
            contestNumber = 1,
            numbers = setOf(25, 5, 1),
            date = null
        )

        val entity = draw.toEntity()
        assertEquals("1,5,25", entity.numbers)
    }
}

