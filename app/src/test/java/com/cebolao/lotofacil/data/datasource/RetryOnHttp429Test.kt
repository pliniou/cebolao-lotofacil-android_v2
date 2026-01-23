package com.cebolao.lotofacil.data.datasource

import com.cebolao.lotofacil.domain.util.NoOpLogger
import okhttp3.Headers
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset

class RetryOnHttp429Test {

    @Test
    fun `retryAfterHeaderToDelayMs parses seconds`() {
        assertEquals(0L, retryAfterHeaderToDelayMs("0", nowMs = 0L))
        assertEquals(5_000L, retryAfterHeaderToDelayMs("5", nowMs = 0L))
    }

    @Test
    fun `retryAfterHeaderToDelayMs parses RFC1123 date`() {
        val now = Instant.parse("2015-10-21T07:27:55Z").toEpochMilli()
        val header = DateTimeFormatter.RFC_1123_DATE_TIME
            .withZone(ZoneOffset.UTC)
            .format(Instant.ofEpochMilli(now + 5_000L))

        assertEquals(5_000L, retryAfterHeaderToDelayMs(header, nowMs = now))
    }

    @Test
    fun `retryOnHttp429 retries and eventually succeeds`() {
        val logger = NoOpLogger()
        val tooManyRequests = http429(retryAfter = "0")

        var attempts = 0
        val result = kotlinx.coroutines.runBlocking {
            retryOnHttp429(
                logger = logger,
                tag = "test",
                maxRetries = 3,
                initialBackoffMs = 0,
                maxBackoffMs = 0
            ) {
                attempts++
                if (attempts < 3) throw tooManyRequests
                "ok"
            }
        }

        assertTrue(result.isSuccess)
        assertEquals("ok", result.getOrNull())
        assertEquals(3, attempts)
    }

    private fun http429(retryAfter: String? = null): HttpException {
        val headers = if (retryAfter == null) {
            Headers.headersOf()
        } else {
            Headers.headersOf("Retry-After", retryAfter)
        }

        val raw = okhttp3.Response.Builder()
            .request(Request.Builder().url("https://example.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(429)
            .message("Too Many Requests")
            .headers(headers)
            .build()

        val errorBody = "{}".toResponseBody(null)
        val response = Response.error<Unit>(errorBody, raw)
        return HttpException(response)
    }
}
