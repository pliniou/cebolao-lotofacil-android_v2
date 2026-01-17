package com.cebolao.lotofacil.util

import androidx.annotation.StringRes
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.AppError
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Fase 3: converte Throwable -> AppError (tipado) e AppError -> mensagem (UI).
 */
fun Throwable.toAppError(): AppError {
    return when (this) {
        is SocketTimeoutException -> AppError.Timeout(
            message = this.message,
            cause = this
        )
        is IOException -> AppError.Network(
            message = this.message,
            cause = this
        )
        is HttpException -> {
            when (code()) {
                429 -> {
                    AppError.RateLimited(
                        message = this.message,
                        cause = this
                    )
                }
                404 -> AppError.NotFound(
                    message = this.message,
                    cause = this
                )
                in 500..599 -> AppError.Network(
                    message = this.message,
                    cause = this
                )
                else -> AppError.Unknown(
                    message = this.message,
                    cause = this
                )
            }
        }
        is IllegalArgumentException -> AppError.Validation(
            message = this.message,
            cause = this
        )
        is IllegalStateException -> AppError.Validation(
            message = this.message,
            cause = this
        )
        is SecurityException -> AppError.Security(
            message = this.message,
            cause = this
        )
        is UnsupportedOperationException -> AppError.Unsupported(
            message = this.message,
            cause = this
        )
        else -> AppError.Unknown(
            message = this.message,
            cause = this
        )
    }
}

@StringRes
fun AppError.toUserMessageRes(): Int {
    return when (this) {
        is AppError.RateLimited -> R.string.home_sync_failed_message
        is AppError.Timeout -> R.string.home_sync_failed_message
        is AppError.Network -> R.string.home_sync_failed_message
        is AppError.NotFound -> R.string.home_sync_failed_message
        is AppError.Parse -> R.string.error_load_data_failed
        is AppError.Database -> R.string.error_load_data_failed
        is AppError.Validation -> R.string.error_load_data_failed
        is AppError.Security -> R.string.error_load_data_failed
        is AppError.Unsupported -> R.string.error_load_data_failed
        is AppError.Unknown -> R.string.error_load_data_failed
    }
}
