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
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

private const val TAG = "HistoryRemoteDataSource"
private const val RETRY_AFTER_HEADER = "Retry-After"
private const val MAX_CONCURRENT_REQUESTS = 8
private const val RATE_LIMIT_HTTP_CODE = 429
private const val DEFAULT_MAX_RETRIES = 3
private const val DEFAULT_INITIAL_BACKOFF_MS = 1_000L
private const val DEFAULT_MAX_BACKOFF_MS = 30_000L

interface HistoryRemoteDataSource {
    suspend fun getLatestDraw(): LotofacilApiResult?
    suspend fun getDrawsInRange(range: IntRange): List<Draw>
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

        // Última tentativa: devolve o erro 429
        if (attempt == maxRetries) return result

        val retryAfterSeconds = exception.response()?.headers()?.get(RETRY_AFTER_HEADER)?.toLongOrNull()
        val sleepMs = (retryAfterSeconds?.coerceAtLeast(0)?.times(1_000L)) ?: backoff

        logger.warning(TAG, "HTTP 429 on $tag (attempt ${attempt + 1}/${maxRetries + 1}). Backing off ${sleepMs}ms.")

        delay(sleepMs)
        if (retryAfterSeconds == null) {
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

    private val dateFormat: ThreadLocal<SimpleDateFormat> =
        ThreadLocal.withInitial { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }

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

        // Mantém ordem do range (comportamento anterior do awaitAll() sobre map()).
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
            val numbers = apiResult.listaDezenas.mapNotNull { it.toIntOrNull() }.toSet()

            // Basic validation
            if (contest <= 0 || numbers.size != 15) return null

            val dateMillis = apiResult.dataApuracao
                ?.takeIf { it.isNotBlank() }
                ?.let { dateStr -> runCatching { dateFormat.get()?.parse(dateStr)?.time }.getOrNull() }

            Draw.fromNumbers(contest, numbers, dateMillis)
        }.getOrNull()
    }
}
