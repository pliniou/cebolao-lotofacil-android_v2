package com.cebolao.lotofacil.ui.components.game

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Rule
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.components.common.AppDivider
import com.cebolao.lotofacil.ui.components.common.AppTable
import com.cebolao.lotofacil.ui.components.common.AppTableData
import com.cebolao.lotofacil.ui.components.common.AppTableStyle
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Motion

@Composable
fun GameRulesCard(
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        title = stringResource(R.string.game_rules_title),
        outlined = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimen.SpacingMedium)
        ) {
            GameRulesHeader(expanded = expanded, onToggle = { expanded = !expanded })
            
            if (expanded) {
                AppDivider()
                GameRulesContent()
            }
        }
    }
}

@Composable
private fun GameRulesHeader(
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                Modifier.semantics {
                    role = Role.Button
                    stateDescription = if (expanded)
                        "Contrair regras"
                    else
                        "Expandir regras"
                }
            )
            .clickable { onToggle() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)
        ) {
            Surface(
                color = scheme.primaryContainer,
                contentColor = scheme.onPrimaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Rule,
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
                    text = "Probabilidades e Bolões",
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

        val rotation by animateFloatAsState(
            targetValue = if (expanded) 180f else 0f,
            animationSpec = Motion.Spring.gentle(),
            label = "chevronRotation"
        )

        Icon(
            imageVector = Icons.Outlined.ExpandMore,
            contentDescription = null,
            tint = scheme.onSurfaceVariant,
            modifier = Modifier.rotate(rotation)
        )
    }
}

@Composable
private fun GameRulesContent() {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimen.SpacingMedium)
    ) {
        HowToPlaySection()
        AppDivider()
        PricesSection()
        AppDivider()
        ProbabilitiesSection()
        AppDivider()
        BolaoSection()
    }
}

@Composable
private fun HowToPlaySection() {
    RulesSection(
        title = stringResource(R.string.game_rules_how_to_play),
        icon = Icons.Outlined.Casino
    ) {
        Text(
            text = stringResource(R.string.game_rules_how_to_play_p1),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = stringResource(R.string.game_rules_how_to_play_p2),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PricesSection() {
    RulesSection(
        title = stringResource(R.string.game_rules_prices_title),
        icon = Icons.Outlined.MonetizationOn
    ) {
        AppTable(
            data = AppTableData(
                headers = listOf(
                    stringResource(R.string.table_header_numbers),
                    stringResource(R.string.table_header_value)
                ),
                rows = listOf(
                    listOf("15", "R$ 3,50"),
                    listOf("16", "R$ 56,00"),
                    listOf("17", "R$ 476,00"),
                    listOf("18", "R$ 2.856,00"),
                    listOf("19", "R$ 13.566,00"),
                    listOf("20", "R$ 54,264,00")
                ),
                weights = listOf(1f, 1f),
                textAligns = listOf(TextAlign.Center, TextAlign.End)
            ),
            style = AppTableStyle(showDividers = true)
        )
    }
}

@Composable
private fun ProbabilitiesSection() {
    RulesSection(
        title = "Probabilidades (1 em...)",
        icon = Icons.Outlined.Casino
    ) {
        ProbabilitiesTable()
    }
}

@Composable
private fun ProbabilitiesTable() {
    val scheme = MaterialTheme.colorScheme
    val probabilities = getProbabilitiesData()

    probabilities.forEachIndexed { index, (title, items) ->
        Column(Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.primary
            )
            items.forEach { item ->
                ProbabilityItemRow(item = item)
            }
        }
        if (index < probabilities.lastIndex) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = Dimen.Spacing8),
                color = scheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    }
}

private fun getProbabilitiesData(): List<Pair<String, List<String>>> {
    return listOf(
        "15 Números" to listOf(
            "15 acertos: 3.268.760",
            "14 acertos: 21.792",
            "13 acertos: 692",
            "12 acertos: 60",
            "11 acertos: 11"
        ),
        "16 Números" to listOf(
            "15 acertos: 204.298",
            "14 acertos: 3.027",
            "13 acertos: 162",
            "12 acertos: 21",
            "11 acertos: 6"
        ),
        "17 Números" to listOf(
            "15 acertos: 24.035",
            "14 acertos: 601",
            "13 acertos: 49",
            "12 acertos: 9",
            "11 acertos: 4"
        ),
        "18 Números" to listOf(
            "15 acertos: 4.006",
            "14 acertos: 153",
            "13 acertos: 18",
            "12 acertos: 5",
            "11 acertos: 3"
        ),
        "19 Números" to listOf(
            "15 acertos: 843",
            "14 acertos: 47",
            "13 acertos: 8",
            "12 acertos: 3,2",
            "11 acertos: 2,9"
        ),
        "20 Números" to listOf(
            "15 acertos: 211",
            "14 acertos: 17",
            "13 acertos: 4,2",
            "12 acertos: 2,6",
            "11 acertos: 0,9"
        )
    )
}

@Composable
private fun ProbabilityItemRow(item: String) {
    val scheme = MaterialTheme.colorScheme
    val parts = item.split(":")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimen.Spacing4),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = parts[0],
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant
        )
        Text(
            text = parts.getOrElse(1) { "" }.trim(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = scheme.onSurface
        )
    }
}

@Composable
private fun BolaoSection() {
    RulesSection(
        title = "Bolão Caixa",
        icon = Icons.Outlined.Groups
    ) {
        ScrollableBolaoTable()
    }
}

@Composable
private fun RulesSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = scheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.CardContentPadding),
            verticalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(Dimen.IconSmall)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
            }

            content()
        }
    }
}

@Composable
private fun ScrollableBolaoTable() {
    val scheme = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .background(scheme.surface, MaterialTheme.shapes.small)
            .padding(Dimen.Spacing8)
    ) {
        // Header
        Row(Modifier.padding(bottom = Dimen.Spacing8)) {
            listOf(
                "Números" to 60.dp,
                "Apostas" to 60.dp,
                "Min Cotas" to 70.dp,
                "Max Cotas" to 70.dp,
                "Min Cota" to 80.dp,
                "Min Bolão" to 80.dp,
                "Max Bolão" to 100.dp,
                "Max Jogos" to 70.dp
            ).forEach { (text, width) ->
                Text(
                    text = text,
                    modifier = Modifier.width(width),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = scheme.outlineVariant)

        // Data
        val data = listOf(
            listOf("15", "1", "2", "7", "R$ 4,50", "R$ 14,00", "R$ 35,00", "10"),
            listOf("16", "16", "2", "35", "R$ 4,50", "R$ 56,00", "R$ 560,00", "10"),
            listOf("17", "136", "2", "40", "R$ 11,90", "R$ 476,00", "R$ 4.760,00", "10"),
            listOf("18", "816", "2", "50", "R$ 57,12", "R$ 2.856,00", "R$ 28.560,00", "10"),
            listOf("19", "3.876", "2", "85", "R$ 159,60", "R$ 13.566,00", "R$ 122.094", "9"),
            listOf("20", "15.504", "2", "100", "R$ 542,64", "R$ 54.264,00", "R$ 217.056", "4")
        )

        data.forEach { row ->
            Row(Modifier.padding(vertical = Dimen.Spacing8)) {
                val widths = listOf(60.dp, 60.dp, 70.dp, 70.dp, 80.dp, 80.dp, 100.dp, 70.dp)
                row.forEachIndexed { index, cell ->
                    Text(
                        text = cell,
                        modifier = Modifier.width(widths[index]),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
            HorizontalDivider(thickness = 0.5.dp, color = scheme.outlineVariant.copy(alpha = 0.3f))
        }
    }
}
