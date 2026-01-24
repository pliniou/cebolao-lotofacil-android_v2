package com.cebolao.lotofacil.domain.model

/**
 * Domain-level error types. Extend as needed.
 */
sealed interface AppError {
    data class Network(val cause: Throwable? = null) : AppError
    data class Database(val cause: Throwable? = null) : AppError
    data class NotFound(val message: String? = null) : AppError
    data class Validation(val message: String? = null) : AppError
    data class Unknown(val cause: Throwable? = null) : AppError
}
