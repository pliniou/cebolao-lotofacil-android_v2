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

    fun getLocale(): Locale = appLocale
}

@Composable
fun rememberCurrencyFormatter(): NumberFormat {
    return remember {
        NumberFormat.getCurrencyInstance(Formatters.getLocale())
    }
}