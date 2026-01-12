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
        val errorMsg = when {
            numbers.size != GameConstants.GAME_SIZE -> {
                "O ticket deve conter exatamente ${GameConstants.GAME_SIZE} números. " +
                        "Foram encontrados ${numbers.size} números."
            }
            numbers.any { it !in GameConstants.NUMBER_RANGE } -> {
                val outOfRange = numbers.filter { it !in GameConstants.NUMBER_RANGE }
                "Números fora do range válido (${GameConstants.MIN_NUMBER}-${GameConstants.MAX_NUMBER}): " +
                        outOfRange.joinToString(", ")
            }
            numbers.count { it in GameConstants.PRIMOS } !in 
                GameConstants.AnalysisRanges.PRIMES_ACCEPTABLE -> {
                val primeCount = numbers.count { it in GameConstants.PRIMOS }
                "Quantidade de números primos fora do intervalo aceitável: $primeCount"
            }
            numbers.count { it in GameConstants.MULTIPLOS_DE_3 } !in 
                GameConstants.AnalysisRanges.MULTIPLES_OF_3_ACCEPTABLE -> {
                val multiplesOf3Count = numbers.count { it in GameConstants.MULTIPLOS_DE_3 }
                "Quantidade de múltiplos de 3 fora do intervalo aceitável: $multiplesOf3Count"
            }
            else -> null
        }

        return errorMsg?.let { ValidationResult.Error(it) } ?: ValidationResult.Success
    }

}
