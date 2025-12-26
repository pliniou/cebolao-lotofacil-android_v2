package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.domain.model.LotofacilGame
import org.junit.Assert.assertTrue
import org.junit.Test

class TicketValidatorTest {

    private val validator = TicketValidator()

    @Test
    fun `validate passes for valid game`() {
        val game = LotofacilGame.fromNumbers((1..15).toSet())
        assertTrue(validator.validate(game.numbers) is ValidationResult.Success)
    }

    @Test
    fun `validate fails for invalid game size`() {
        // TicketValidator checks size 15. We construct a set of 14 numbers.
        // Note: LotofacilGame.fromNumbers enforces 15, so we might need to bypass it or just test validator with raw set.
        // The validator takes Set<Int>, so we don't need LotofacilGame here.
        val numbers = (1..14).toSet()
        assertTrue(validator.validate(numbers) is ValidationResult.Error)
    }
}
