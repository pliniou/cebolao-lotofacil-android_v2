package com.cebolao.lotofacil.domain.util

/**
 * Logging abstraction to avoid Android framework dependencies in domain layer.
 * Implementations can use platform-specific logging (Android Log, Timber, etc.)
 */
interface Logger {
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun warning(tag: String, message: String, throwable: Throwable? = null)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}

/**
 * No-op logger for testing or when logging is disabled
 */
class NoOpLogger : Logger {
    override fun debug(tag: String, message: String) = Unit
    override fun info(tag: String, message: String) = Unit
    override fun warning(tag: String, message: String, throwable: Throwable?) = Unit
    override fun error(tag: String, message: String, throwable: Throwable?) = Unit
}
