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
        is SocketTimeoutException -> AppError.Network(cause = this)
        is IOException -> AppError.Network(cause = this)
        is HttpException -> {
            when (code()) {
                404 -> AppError.NotFound(message = this.message)
                in 500..599 -> AppError.Network(cause = this)
                else -> AppError.Unknown(cause = this)
            }
        }
        is IllegalArgumentException -> AppError.Validation(message = this.message)
        is IllegalStateException -> AppError.Validation(message = this.message)
        else -> AppError.Unknown(cause = this)
    }
}

@StringRes
fun AppError.toUserMessageRes(): Int {
    return when (this) {
        is AppError.Network -> R.string.home_sync_failed_message
        is AppError.NotFound -> R.string.home_sync_failed_message
        is AppError.Database -> R.string.error_load_data_failed
        is AppError.Validation -> R.string.error_load_data_failed
        is AppError.Unknown -> R.string.error_load_data_failed
    }
}
