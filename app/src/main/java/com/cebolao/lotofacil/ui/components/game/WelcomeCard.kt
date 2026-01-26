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
import com.cebolao.lotofacil.ui.model.UiNextDrawInfo
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.components.layout.CardVariant
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.util.Formatters
import java.time.LocalDate
import java.time.LocalTime

private const val MORNING_START_HOUR = 5
private const val MORNING_END_HOUR = 11
private const val AFTERNOON_START_HOUR = 12
private const val AFTERNOON_END_HOUR = 17

/**
 * Retorna o ícone apropriado baseado no horário do dia.
 */
private fun getGreetingIcon(hour: Int): ImageVector {
    return when (hour) {
        in MORNING_START_HOUR..MORNING_END_HOUR -> Icons.Filled.WbSunny
        in AFTERNOON_START_HOUR..AFTERNOON_END_HOUR -> Icons.Filled.WbTwilight
        else -> Icons.Filled.NightsStay
    }
}

/**
 * Card de boas-vindas com saudação dinâmica, data e frase motivacional.
 */
@Composable
fun WelcomeCard(
    modifier: Modifier = Modifier,
    nextDrawInfo: UiNextDrawInfo? = null
) {
    val scheme = MaterialTheme.colorScheme

    val currentTime = remember { LocalTime.now() }
    val currentDate = remember { LocalDate.now() }

    val greetingRes = when (currentTime.hour) {
        in MORNING_START_HOUR..MORNING_END_HOUR -> R.string.greeting_morning
        in AFTERNOON_START_HOUR..AFTERNOON_END_HOUR -> R.string.greeting_afternoon
        else -> R.string.greeting_night
    }

    val dateString = remember(currentDate) { Formatters.formatDate(currentDate) }

    val quotes = stringArrayResource(R.array.motivational_quotes)
    val randomQuote = remember(quotes) { quotes.randomOrNull().orEmpty() }
    val contestNumber = nextDrawInfo?.contestNumber ?: 0
    val contestDate = nextDrawInfo?.formattedDate.orEmpty()
    val hasNextDraw = contestNumber > 0 && contestDate.isNotBlank()
    val isDrawToday = hasNextDraw && nextDrawInfo?.drawDate == currentDate

    AppCard(
        modifier = modifier.fillMaxWidth(),
        variant = CardVariant.Solid,
        outlined = true,
        contentPadding = Dimen.Spacing16
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

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (isDrawToday) scheme.primaryContainer else scheme.surfaceContainerHigh,
                contentColor = if (isDrawToday) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimen.Spacing8),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
                ) {
                    Icon(
                        imageVector = if (isDrawToday) AppIcons.Check else AppIcons.Info,
                        contentDescription = null,
                        modifier = Modifier.size(Dimen.IconSmall)
                    )
                    Text(
                        text = when {
                            !hasNextDraw -> stringResource(R.string.welcome_draw_info_unavailable)
                            isDrawToday -> stringResource(
                                R.string.welcome_draw_today_format,
                                contestNumber,
                                contestDate
                            )
                            else -> stringResource(
                                R.string.welcome_next_draw_format,
                                contestNumber,
                                contestDate
                            )
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isDrawToday) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isDrawToday) scheme.onPrimaryContainer else scheme.onSurfaceVariant
                    )
                }
            }

            if (randomQuote.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = scheme.surfaceContainerHigh,
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
