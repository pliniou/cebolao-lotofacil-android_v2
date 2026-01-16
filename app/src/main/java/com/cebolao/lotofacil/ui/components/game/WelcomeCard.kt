package com.cebolao.lotofacil.ui.components.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.components.layout.CardVariant
import com.cebolao.lotofacil.ui.theme.Dimen
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Retorna o ícone apropriado baseado no horário do dia.
 */
@Suppress("MagicNumber")
private fun getGreetingIcon(hour: Int): ImageVector {
    return when (hour) {
        in 5..11 -> Icons.Filled.WbSunny
        in 12..17 -> Icons.Filled.WbTwilight
        else -> Icons.Filled.NightsStay
    }
}

/**
 * Card de boas-vindas com saudação dinâmica, data e frase motivacional.
 */
@Suppress("MagicNumber")
@Composable
fun WelcomeCard(
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    val currentTime = remember { LocalTime.now() }
    val currentDate = remember { LocalDate.now() }

    val greetingRes = when (currentTime.hour) {
        in 5..11 -> R.string.greeting_morning
        in 12..17 -> R.string.greeting_afternoon
        else -> R.string.greeting_night
    }

    val dateString = remember(currentDate) {
        val locale = Locale("pt", "BR")
        DateTimeFormatter
            .ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(locale)
            .format(currentDate)
    }

    val quotes = stringArrayResource(R.array.motivational_quotes)
    val randomQuote = remember(quotes) { quotes.randomOrNull().orEmpty() }

    AppCard(
        modifier = modifier.fillMaxWidth(),
        variant = CardVariant.Solid,
        contentPadding = Dimen.Spacing12
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing12, Alignment.CenterHorizontally)
            ) {
                Surface(
                    color = scheme.secondaryContainer,
                    contentColor = scheme.onSecondaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(
                        modifier = Modifier.padding(Dimen.Spacing4),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getGreetingIcon(currentTime.hour),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(Dimen.Spacing8)
                                .size(Dimen.IconMedium)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
                ) {
                        Text(
                            text = stringResource(
                                R.string.welcome_message_format,
                                stringResource(greetingRes)
                            ),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.labelLarge,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            if (randomQuote.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = scheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = randomQuote,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = scheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(Dimen.Spacing8)
                    )
                }
            }
        }
    }
}
