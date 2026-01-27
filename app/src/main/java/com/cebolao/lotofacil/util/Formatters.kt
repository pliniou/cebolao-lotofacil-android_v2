package com.cebolao.lotofacil.util

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object Formatters {
    private val appLocale = Locale.forLanguageTag("$LOCALE_LANGUAGE-$LOCALE_COUNTRY")
    private val localizedDateFormatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(appLocale)
    private val apiDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", appLocale)

    fun formatCurrency(value: Number): String {
        return NumberFormat.getCurrencyInstance(appLocale).format(value)
    }

    fun formatPercentage(value: Float): String {
        return String.format(appLocale, "%.1f%%", value)
    }

    fun formatDate(date: LocalDate): String {
        return localizedDateFormatter.format(date)
    }

    fun formatDateMillis(timestamp: Long, style: FormatStyle = FormatStyle.MEDIUM): String {
        val formatter = if (style == FormatStyle.MEDIUM) {
            localizedDateFormatter
        } else {
            DateTimeFormatter.ofLocalizedDate(style).withLocale(appLocale)
        }
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(formatter)
    }

    fun parseApiDate(dateText: String): LocalDate? {
        return runCatching { LocalDate.parse(dateText, apiDateFormatter) }.getOrNull()
    }

}
