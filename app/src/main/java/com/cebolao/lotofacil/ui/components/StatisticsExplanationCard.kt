package com.cebolao.lotofacil.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Motion

@Composable
fun StatisticsExplanationCard(
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme

    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        outlined = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimen.SpacingMedium)
        ) {
            // Header clicável (flat, sem “peso” excessivo)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)
                ) {
                    Surface(
                        color = scheme.secondaryContainer,
                        contentColor = scheme.onSecondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(Dimen.Spacing8)
                                .size(Dimen.IconMedium)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
                    ) {
                        Text(
                            text = stringResource(R.string.edu_stats_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onSurface,
                        )
                        Text(
                            text = if (expanded)
                                stringResource(R.string.tap_to_collapse)
                            else
                                stringResource(R.string.tap_to_expand),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }

                // Ícone com rotação animada
                val rotation by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    animationSpec = Motion.Spring.gentle(),
                    label = "chevronRotation"
                )

                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = if (expanded)
                        stringResource(R.string.tap_to_collapse)
                    else
                        stringResource(R.string.tap_to_expand),
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotation)
                )
            }

            if (expanded) {
                AppDivider()

                // Seção 1: Distribuição Normal
                EduSectionCard(
                    title = stringResource(R.string.edu_section_normal_dist),
                    icon = Icons.Outlined.School
                ) {
                    EduBullet(text = stringResource(R.string.edu_normal_dist_intro))
                    EduBullet(text = stringResource(R.string.edu_normal_dist_history))
                    EduBullet(text = stringResource(R.string.edu_normal_dist_application))
                    EduBullet(text = stringResource(R.string.edu_normal_dist_intuition))
                }

                AppDivider()

                // Seção 2: Filtros (explicação geral + lista de filtros do app)
                EduSectionCard(
                    title = stringResource(R.string.edu_section_filters),
                    icon = Icons.Outlined.Info
                ) {
                    EduBullet(text = stringResource(R.string.edu_filters_desc))

                    EduBullet(
                        title = stringResource(R.string.edu_hot_cold_title),
                        text = stringResource(R.string.edu_hot_cold_desc)
                    )

                    Spacer(Modifier.padding(top = Dimen.Spacing8))

                    FilterExplainerList()
                }
            }
        }
    }
}

@Composable
private fun EduSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceVariant,
        contentColor = scheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.CardContentPadding),
            verticalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)
        ) {
            TitleWithIcon(
                text = title,
                iconVector = icon,
                tint = scheme.primary
            )

            content()
        }
    }
}

@Composable
private fun EduBullet(
    text: String,
    title: String? = null
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )

        Spacer(Modifier.width(Dimen.SpacingShort))

        Column(
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.primary
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )
        }
    }
}

private data class FilterInfo(
    @param:StringRes @field:StringRes val titleRes: Int,
    @param:StringRes @field:StringRes val descRes: Int
)

@Composable
private fun FilterExplainerList() {
    val scheme = MaterialTheme.colorScheme

    val filters = remember {
        listOf(
            FilterInfo(R.string.filter_soma_title, R.string.filter_soma_desc),
            FilterInfo(R.string.filter_pares_title, R.string.filter_pares_desc),
            FilterInfo(R.string.filter_primos_title, R.string.filter_primos_desc),
            FilterInfo(R.string.filter_moldura_title, R.string.filter_moldura_desc),
            FilterInfo(R.string.filter_retrato_title, R.string.filter_retrato_desc),
            FilterInfo(R.string.filter_fibonacci_title, R.string.filter_fibonacci_desc),
            FilterInfo(R.string.filter_multiplos3_title, R.string.filter_multiplos3_desc),
            FilterInfo(R.string.filter_repetidas_title, R.string.filter_repetidas_desc),
            FilterInfo(R.string.filter_sequencias_title, R.string.filter_sequencias_desc),
            FilterInfo(R.string.filter_linhas_title, R.string.filter_linhas_desc),
            FilterInfo(R.string.filter_colunas_title, R.string.filter_colunas_desc),
            FilterInfo(R.string.filter_quadrantes_title, R.string.filter_quadrantes_desc)
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)
    ) {
        filters.forEach { item ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = scheme.surface,
                contentColor = scheme.onSurface,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimen.SpacingMedium),
                    verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
                ) {
                    Text(
                        text = stringResource(item.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = stringResource(item.descRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
