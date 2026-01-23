package com.cebolao.lotofacil.data.datasource

import com.cebolao.lotofacil.domain.util.Logger
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset

fun retryAfterHeaderToDelayMs(header: String?, nowMs: Long = System.currentTimeMillis()): Long {
    if (header == null) return 0L

    // Try parsing as seconds
    header.toLongOrNull()?.let { seconds ->
        return seconds * 1000L
    }

    // Try parsing as HTTP Date
    try {
        val date = DateTimeFormatter.RFC_1123_DATE_TIME
            .withZone(ZoneOffset.UTC)
            .parse(header, Instant::from)
        val delay = date.toEpochMilli() - nowMs
        return delay.coerceAtLeast(0L)
    } catch (e: Exception) {
        // Fallback or ignore
    }

    return 0L
}

suspend fun <T> retryOnHttp429(
    logger: Logger,
    tag: String,
    maxRetries: Int = 3,
    initialBackoffMs: Long = 1000,
    maxBackoffMs: Long = 30000,
    block: suspend () -> T
): Result<T> {
    var currentBackoff = initialBackoffMs
    var attempts = 0
    while (true) {
        try {
            return Result.success(block())
        } catch (e: HttpException) {
            if (e.code() == 429) {
                attempts++
                if (attempts > maxRetries) {
                    return Result.failure(e)
                }

                val retryAfter = e.response()?.headers()?.get("Retry-After")
                val delayMs = retryAfterHeaderToDelayMs(retryAfter)
                
                // Use the larger of the calculated delay or the exponential backoff (if we were doing exponential, but here we seem to just rely on 429 header or backoff?)
                // The test passed explicit backoff params. Usually, if Retry-After is present, we honor it.
                // If not, we might use exponential backoff.
                // Let's implement a simple logic: if Retry-After is > 0, use it. Else use currentBackoff.
                
                val finalDelay = if (delayMs > 0) delayMs else currentBackoff
                
                logger.warning(tag, "HTTP 429 Too Many Requests. Retrying in ${finalDelay}ms (attempt $attempts/$maxRetries)")
                delay(finalDelay)

                // Exponential backoff for next time if we fallback to it? 
                // The prompt didn't specify exact logic but let's assume standard behavior.
                if (delayMs <= 0) {
                     currentBackoff = (currentBackoff * 2).coerceAtMost(maxBackoffMs)
                }
            } else {
                 return Result.failure(e)
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
