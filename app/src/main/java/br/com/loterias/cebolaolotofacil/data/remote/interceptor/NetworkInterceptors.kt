package br.com.loterias.cebolaolotofacil.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException

/**
 * OkHttp interceptor for handling HTTP 429 (Too Many Requests) with automatic retry
 */
class RetryOnHttp429Interceptor(private val maxRetries: Int = 3) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        var retryCount = 0

        do {
            try {
                response = chain.proceed(request)

                // Retry on 429 (Too Many Requests)
                if (response.code == 429 && retryCount < maxRetries) {
                    val retryAfter = response.header("Retry-After")?.toLongOrNull() ?: (1000L * (retryCount + 1))
                    
                    Timber.w("HTTP 429 - Rate limited. Retrying after ${retryAfter}ms (attempt ${retryCount + 1}/$maxRetries)")
                    response.close()
                    
                    Thread.sleep(retryAfter)
                    retryCount++
                } else {
                    return response
                }
            } catch (e: IOException) {
                exception = e
                if (retryCount >= maxRetries) {
                    throw e
                }
                Timber.w(e, "Network error, retrying (attempt ${retryCount + 1}/$maxRetries)")
                retryCount++
            }
        } while (retryCount <= maxRetries)

        return response ?: throw exception ?: IOException("Unknown network error")
    }
}

/**
 * Interceptor for adding common headers and logging requests
 */
class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val requestBuilder = originalRequest.newBuilder()
            .header("User-Agent", "CebolaoLotofacil/1.2.0")
            .header("Accept", "application/json")
            .header("Connection", "keep-alive")

        val newRequest = requestBuilder.build()
        
        Timber.d("→ ${newRequest.method} ${newRequest.url}")
        
        return chain.proceed(newRequest)
    }
}

/**
 * Interceptor for logging response details
 */
class ResponseLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val startTime = System.currentTimeMillis()
        val response = chain.proceed(chain.request())
        val duration = System.currentTimeMillis() - startTime

        val logMessage = when {
            response.isSuccessful -> "← ${response.code} (${duration}ms)"
            else -> "← ERROR ${response.code} (${duration}ms)"
        }

        Timber.d(logMessage)
        return response
    }
}
