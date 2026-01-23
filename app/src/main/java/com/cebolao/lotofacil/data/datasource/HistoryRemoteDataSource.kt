package com.cebolao.lotofacil.data.datasource

import com.cebolao.lotofacil.data.network.ApiService
import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.util.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
import java.time.ZonedDateTime
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.random.Random

private const val TAG = "HistoryRemoteDataSource"
private const val RETRY_AFTER_HEADER = "Retry-After"
private const val MAX_CONCURRENT_REQUESTS = 8
private const val RATE_LIMIT_HTTP_CODE = 429
private const val DEFAULT_MAX_RETRIES = 3
private const val DEFAULT_INITIAL_BACKOFF_MS = 1_000L
private const val DEFAULT_MAX_BACKOFF_MS = 30_000L
private const val DEFAULT_JITTER_RATIO = 0.1

interface HistoryRemoteDataSource {
    suspend fun getLatestDraw(): LotofacilApiResult?
    suspend fun getDrawsInRange(range: IntRange): List<Draw>
}

internal fun retryAfterHeaderToDelayMs(
    headerValue: String?,
    nowMs: Long = System.currentTimeMillis()
): Long? {
    val raw = headerValue?.trim().orEmpty()
    if (raw.isBlank()) return null

    raw.toLongOrNull()?.let { seconds ->
        return (seconds.coerceAtLeast(0) * 1_000L)
    }

    // Retry-After can also be an HTTP-date per RFC 7231.
    return runCatching {
        val now = Instant.ofEpochMilli(nowMs)
        val retryAt = ZonedDateTime.parse(raw, RFC_1123_DATE_TIME).toInstant()
        (retryAt.toEpochMilli() - now.toEpochMilli()).coerceAtLeast(0)
    }.getOrNull()
}

internal suspend fun <T> retryOnHttp429(
    logger: Logger,
    tag: String,
    maxRetries: Int = DEFAULT_MAX_RETRIES,
    initialBackoffMs: Long = DEFAULT_INITIAL_BACKOFF_MS,
    maxBackoffMs: Long = DEFAULT_MAX_BACKOFF_MS,
    block: suspend () -> T
): Result<T> {
    var backoff = initialBackoffMs

    repeat(maxRetries + 1) { attempt ->
        val result = runCatching { block() }
        val exception = result.exceptionOrNull()

        if (exception !is HttpException || exception.code() != RATE_LIMIT_HTTP_CODE) {
            return result
        }

        // Last attempt: return the 429 error.
        if (attempt == maxRetries) return result

        val retryAfter = exception.response()?.headers()?.get(RETRY_AFTER_HEADER)
        val sleepMs = retryAfterHeaderToDelayMs(retryAfter) ?: backoff
        val jitterWindowMs = (sleepMs * DEFAULT_JITTER_RATIO).toLong().coerceAtLeast(0L)
        val jitterMs = if (jitterWindowMs == 0L) 0L else Random.nextLong(0L, jitterWindowMs + 1)
        val sleepWithJitterMs = (sleepMs + jitterMs).coerceAtMost(maxBackoffMs)

        logger.warning(
            TAG,
            "HTTP 429 on $tag (attempt ${attempt + 1}/${maxRetries + 1}). Backing off ${sleepWithJitterMs}ms."
        )

        delay(sleepWithJitterMs)
        if (retryAfter == null) {
            backoff = (backoff * 2).coerceAtMost(maxBackoffMs)
        }
    }

    return Result.failure(IllegalArgumentException("Unreachable retry loop state for $tag"))
}

@Singleton
class HistoryRemoteDataSourceImpl @Inject constructor(
    private val apiService: ApiService,
    private val logger: Logger,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : HistoryRemoteDataSource {

    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))

    override suspend fun getLatestDraw(): LotofacilApiResult? = withContext(ioDispatcher) {
        retryOnHttp429(logger, tag = "getLatestDraw") {
            apiService.getLatestResult()
        }.onFailure { e ->
            logger.error(TAG, "Failed to fetch latest draw: ${e.message}", e)
        }.getOrNull()
    }

    override suspend fun getDrawsInRange(range: IntRange): List<Draw> = withContext(ioDispatcher) {
        if (range.isEmpty()) return@withContext emptyList()

        val first = range.first
        val size = (range.last - range.first + 1).coerceAtLeast(0)
        if (size == 0) return@withContext emptyList()

        // Keep range order (previous behavior of awaitAll() over map()).
        val output = arrayOfNulls<Draw>(size)
        val nextIndex = AtomicInteger(0)
        val workers = min(MAX_CONCURRENT_REQUESTS, size)

        coroutineScope {
            List(workers) {
                async {
                    while (true) {
                        val idx = nextIndex.getAndIncrement()
                        if (idx >= size) break

                        val contestNumber = first + idx
                        val draw = retryOnHttp429(logger, tag = "getResultByContest($contestNumber)") {
                            apiService.getResultByContest(contestNumber)
                        }.getOrNull()?.let { apiResultToDraw(it) }

                        output[idx] = draw
                    }
                }
            }.awaitAll()
        }

        output.filterNotNull()
    }

    private fun apiResultToDraw(apiResult: LotofacilApiResult): Draw? {
        return runCatching {
            val contest = apiResult.numero
            val mask = stringsToMask(apiResult.listaDezenas)
            if (contest <= 0 || java.lang.Long.bitCount(mask) != 15) return null

            val dateMillis = apiResult.dataApuracao
                ?.takeIf { it.isNotBlank() }
                ?.let { dateStr ->
                    runCatching {
                        LocalDate.parse(dateStr, dateFormatter)
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                    }.getOrNull()
                }

            Draw(contest, mask, dateMillis)
        }.getOrNull()
    }

    private fun stringsToMask(numbers: List<String>): Long {
        var mask = 0L
        for (s in numbers) {
            val n = s.toIntOrNull() ?: continue
            val idx = n - 1
            if (idx in 0..63) mask = mask or (1L shl idx)
        }
        return mask
    }
}
