package com.cebolao.lotofacil.ui.components.stats

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.FilterType
import com.cebolao.lotofacil.ui.components.common.AppDivider
import com.cebolao.lotofacil.ui.components.layout.AppCard
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
            verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
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
                    horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
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

                // Icon with animated rotation
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

                // Section 1: Normal Distribution
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

                // Section 2: Filters (general explanation + list of app filters)
                EduSectionCard(
                    title = stringResource(R.string.edu_section_filters),
                    icon = Icons.Outlined.Info
                ) {
                    EduBullet(text = stringResource(R.string.edu_filters_desc))

                    EduBullet(
                        title = stringResource(R.string.edu_hot_cold_title),
                        text = stringResource(R.string.edu_hot_cold_desc)
                    )

                    Spacer(Modifier.padding(top = Dimen.ItemSpacing))

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
        color = scheme.surfaceContainerLow,
        contentColor = scheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.CardContentPadding),
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
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

        Spacer(Modifier.width(Dimen.Spacing4))

        Column(
            verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
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

@Composable
private fun FilterExplainerList() {
    val scheme = MaterialTheme.colorScheme
    val filters = FilterType.entries

    Column(
        verticalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)
    ) {
        filters.forEach { filter ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = scheme.surfaceContainerHigh,
                contentColor = scheme.onSurface,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimen.CardContentPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
                ) {
                    Text(
                        text = stringResource(filter.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = stringResource(filter.descriptionRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview
@Composable
internal fun StatisticsExplanationCardPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(Dimen.Spacing16)) {
            StatisticsExplanationCard()
        }
    }
}
