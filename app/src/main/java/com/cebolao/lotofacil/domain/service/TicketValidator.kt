package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.domain.GameConstants
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resultado de validação de ticket
 */
sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

/**
 * Validador de tickets da Lotofácil.
 * Valida: tamanho = 15, números únicos, range 1-25
 */
@Singleton
class TicketValidator @Inject constructor() {

    /**
     * Valida um conjunto de números como ticket válido
     */
    fun validate(numbers: Set<Int>): ValidationResult {
        // Validação de tamanho
        if (numbers.size != GameConstants.GAME_SIZE) {
            return ValidationResult.Error(
                "O ticket deve conter exatamente ${GameConstants.GAME_SIZE} números. " +
                        "Foram encontrados ${numbers.size} números."
            )
        }

        // Validação de números únicos (Set já garante unicidade, mas verificamos se há duplicatas na entrada)
        // Como recebemos Set<Int>, não há duplicatas, mas validamos o tamanho acima

        // Validação de range
        val outOfRange = numbers.filter { it !in GameConstants.NUMBER_RANGE }
        if (outOfRange.isNotEmpty()) {
            return ValidationResult.Error(
                "Números fora do range válido (${GameConstants.MIN_NUMBER}-${GameConstants.MAX_NUMBER}): " +
                        outOfRange.joinToString(", ")
            )
        }

        return ValidationResult.Success
    }

    /**
     * Valida um conjunto de números e retorna mensagem de erro se inválido
     */
    fun validateOrThrow(numbers: Set<Int>) {
        when (val result = validate(numbers)) {
            is ValidationResult.Error -> throw IllegalArgumentException(result.message)
            is ValidationResult.Success -> { /* válido */ }
        }
    }
}

