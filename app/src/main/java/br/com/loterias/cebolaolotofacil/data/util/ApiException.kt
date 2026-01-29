package br.com.loterias.cebolaolotofacil.data.util

import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

/**
 * Sealed class for representing API call results
 * Supports success, error, and loading states
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception, val message: String? = null) : Result<Nothing>()
    data class Loading<T>(val data: T? = null) : Result<T>()

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
        is Loading -> data
    }

    fun exceptionOrNull(): Exception? = when (this) {
        is Error -> exception
        else -> null
    }

    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isLoading(): Boolean = this is Loading
}

/**
 * Custom exception for API-related errors
 */
sealed class ApiException(message: String?, cause: Throwable? = null) : Exception(message, cause) {
    
    data class NetworkException(val ioException: IOException) : 
        ApiException("Network error occurred: ${ioException.message}", ioException)
    
    data class ServerException(val code: Int, val body: String?) : 
        ApiException("Server error: $code - $body")
    
    data class HttpException(val httpException: retrofit2.HttpException) : 
        ApiException("HTTP error: ${httpException.code()}", httpException)
    
    data class TimeoutException(val timeoutCause: Throwable) : 
        ApiException("Request timeout", timeoutCause)
    
    data class ParseException(val parseException: Exception) : 
        ApiException("Failed to parse response: ${parseException.message}", parseException)
    
    class UnknownException(message: String?, cause: Throwable? = null) : 
        ApiException(message, cause)
}

/**
 * Extension function to convert throwables to ApiException
 */
fun Throwable.toApiException(): ApiException = when (this) {
    is IOException -> {
        Timber.e(this, "Network error")
        ApiException.NetworkException(this)
    }
    is HttpException -> {
        Timber.e(this, "HTTP error: ${this.code()}")
        ApiException.HttpException(this)
    }
    is ApiException -> this
    else -> {
        Timber.e(this, "Unknown error")
        ApiException.UnknownException(this.message, this)
    }
}

/**
 * Retry helper for network calls with exponential backoff
 */
suspend fun <T> retryWithExponentialBackoff(
    initialDelayMs: Long = 100,
    maxDelayMs: Long = 8000,
    maxRetries: Int = 3,
    block: suspend () -> T
): T {
    var delay = initialDelayMs
    var lastException: Exception? = null

    repeat(maxRetries) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            lastException = e
            Timber.w(e, "Attempt ${attempt + 1} failed, retrying in ${delay}ms")
            
            if (attempt < maxRetries - 1) {
                kotlinx.coroutines.delay(delay)
                delay = (delay * 2).coerceAtMost(maxDelayMs)
            }
        }
    }

    throw lastException ?: Exception("All retries failed")
}

/**
 * Timeout helper for network calls
 */
suspend fun <T> withTimeout(
    timeoutMs: Long = 30000,
    block: suspend () -> T
): T {
    return try {
        kotlinx.coroutines.withTimeoutOrNull(timeoutMs) {
            block()
        } ?: throw ApiException.TimeoutException(Exception("Request timeout after ${timeoutMs}ms"))
    } catch (e: Exception) {
        throw if (e is ApiException) e else ApiException.UnknownException(e.message, e)
    }
}
