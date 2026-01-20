package com.cebolao.lotofacil.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.NumberFormat
import java.util.Locale

object Formatters {
    private val appLocale = Locale.forLanguageTag("$LOCALE_LANGUAGE-$LOCALE_COUNTRY")
    fun formatCurrency(value: Number): String {
        return NumberFormat.getCurrencyInstance(appLocale).format(value)
    }

    fun formatPercentage(value: Float): String {
        return String.format(appLocale, "%.1f%%", value)
    }

    fun formatDate(date: java.time.LocalDate): String {
        return java.time.format.DateTimeFormatter
            .ofLocalizedDate(java.time.format.FormatStyle.MEDIUM)
            .withLocale(appLocale)
            .format(date)
    }

    fun getLocale(): Locale = appLocale
}

@Composable
fun rememberCurrencyFormatter(): NumberFormat {
    return remember {
        NumberFormat.getCurrencyInstance(Formatters.getLocale())
    }
}