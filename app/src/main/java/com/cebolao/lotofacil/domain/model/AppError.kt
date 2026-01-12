package com.cebolao.lotofacil.domain.model

sealed class AppError(
    open val cause: Throwable? = null,
    open val message: String? = null
) {
    data class Network(
        override val cause: Throwable? = null,
        override val message: String? = null
    ) : AppError(cause, message)
    
    data class RateLimited(
        override val cause: Throwable? = null,
        override val message: String? = null
    ) : AppError(cause, message)
    
    data class Timeout(
        override val cause: Throwable? = null,
        override val message: String? = null
    ) : AppError(cause, message)
    
    data class NotFound(
        override val cause: Throwable? = null,
        override val message: String? = null
    ) : AppError(cause, message)
    
    data class Parse(
        override val cause: Throwable? = null,
        override val message: String? = null
    ) : AppError(cause, message)
    
    data class Database(
        override val cause: Throwable? = null,
        override val message: String? = null
    ) : AppError(cause, message)
    
    data class Validation(
        override val message: String? = null,
        override val cause: Throwable? = null
    ) : AppError(cause, message)
    
    data class Security(
        override val cause: Throwable? = null,
        override val message: String? = null
    ) : AppError(cause, message)
    
    data class Unsupported(
        override val cause: Throwable? = null,
        override val message: String? = null
    ) : AppError(cause, message)
    
    data class Unknown(
        override val cause: Throwable? = null,
        override val message: String? = null
    ) : AppError(cause, message)
}
