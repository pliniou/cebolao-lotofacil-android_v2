package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.CheckReport
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Checks the last draw against the provided numbers. */
class CheckLastGameUseCase @Inject constructor(
    private val checkGameUseCase: CheckGameUseCase
) {
    suspend operator fun invoke(numbers: List<Int>): AppResult<CheckReport> =
        when (val result = checkGameUseCase(numbers.toSet()).first()) {
            is AppResult.Success -> AppResult.Success(result.value)
            is AppResult.Failure -> result
        }
}
