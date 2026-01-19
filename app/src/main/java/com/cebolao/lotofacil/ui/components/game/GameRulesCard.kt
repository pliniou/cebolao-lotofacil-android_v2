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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.components.common.AppDivider
import com.cebolao.lotofacil.ui.components.common.AppTable
import com.cebolao.lotofacil.ui.components.common.AppTableData
import com.cebolao.lotofacil.ui.components.common.AppTableStyle
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Motion
import com.cebolao.lotofacil.domain.model.FinancialCalculator
import com.cebolao.lotofacil.util.Formatters

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
    val expandedState = stringResource(R.string.state_expanded)
    val collapsedState = stringResource(R.string.state_collapsed)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                Modifier.semantics {
                    role = Role.Button
                    stateDescription = if (expanded)
                        expandedState
                    else
                        collapsedState
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
                    text = stringResource(R.string.game_rules_probabilities_and_pools),
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
        Column(verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)) {
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
}

@Composable
private fun PricesSection() {
    RulesSection(
        title = stringResource(R.string.game_rules_prices_title),
        icon = Icons.Outlined.MonetizationOn
    ) {
        val formatter = Formatters
        // Generate prices for 15 to 20 numbers
        val priceRows = (15..20).map { n ->
            val cost = FinancialCalculator.getGameCost(n)
            listOf(n.toString(), formatter.formatCurrency(cost))
        }

        AppTable(
            data = AppTableData(
                headers = listOf(
                    stringResource(R.string.table_header_numbers),
                    stringResource(R.string.table_header_value)
                ),
                rows = priceRows,
                weights = listOf(1f, 1f),
                textAligns = listOf(TextAlign.Center, TextAlign.End)
            ),
            style = AppTableStyle(showDividers = true, rowStyle = MaterialTheme.typography.bodyMedium)
        )
    }
}

@Composable
private fun ProbabilitiesSection() {
    RulesSection(
        title = stringResource(R.string.game_rules_probabilities_title),
        icon = Icons.Outlined.Casino
    ) {
        ProbabilitiesTable()
    }
}

@Composable
private fun ProbabilitiesTable() {
    val scheme = MaterialTheme.colorScheme
    val probabilities = getProbabilitiesData()

    Column(verticalArrangement = Arrangement.spacedBy(Dimen.Spacing16)) {
        probabilities.forEachIndexed { index, data ->
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)) {
                Text(
                    text = pluralStringResource(R.plurals.about_prob_n_numbers, data.numbers, data.numbers),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary
                )
                
                AppTable(
                    data = AppTableData(
                        headers = emptyList(), // No headers for nested tables
                        rows = data.items.map {
                            listOf(
                                pluralStringResource(R.plurals.about_prob_hits_format, it.hits, it.hits),
                                it.probability
                            )
                        },
                        weights = listOf(1f, 1.2f),
                        textAligns = listOf(TextAlign.Start, TextAlign.End)
                    ),
                    style = AppTableStyle(showDividers = false, rowStyle = MaterialTheme.typography.bodySmall)
                )
            }
            if (index < probabilities.lastIndex) {
                AppDivider()
            }
        }
    }
}

private data class ProbData(val numbers: Int, val items: List<HitProb>)
private data class HitProb(val hits: Int, val probability: String)

private fun getProbabilitiesData(): List<ProbData> {
    return listOf(
        ProbData(15, listOf(
            HitProb(15, "3.268.760"),
            HitProb(14, "21.792"),
            HitProb(13, "692"),
            HitProb(12, "60"),
            HitProb(11, "11")
        )),
        ProbData(16, listOf(
            HitProb(15, "204.298"),
            HitProb(14, "3.027"),
            HitProb(13, "162"),
            HitProb(12, "21"),
            HitProb(11, "6")
        )),
        ProbData(17, listOf(
            HitProb(15, "24.035"),
            HitProb(14, "601"),
            HitProb(13, "49"),
            HitProb(12, "9"),
            HitProb(11, "4")
        )),
        ProbData(18, listOf(
            HitProb(15, "4.006"),
            HitProb(14, "153"),
            HitProb(13, "18"),
            HitProb(12, "5"),
            HitProb(11, "3")
        )),
        ProbData(19, listOf(
            HitProb(15, "843"),
            HitProb(14, "47"),
            HitProb(13, "8"),
            HitProb(12, "3,2"),
            HitProb(11, "2,9")
        )),
        ProbData(20, listOf(
            HitProb(15, "211"),
            HitProb(14, "17"),
            HitProb(13, "4,2"),
            HitProb(12, "2,6"),
            HitProb(11, "0,9")
        ))
    )
}

@Composable
private fun BolaoSection() {
    RulesSection(
        title = stringResource(R.string.game_rules_pool_caixa_title),
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
    val columns = listOf(
        stringResource(R.string.game_rules_pool_col_numbers) to Dimen.ControlWidthMedium,
        stringResource(R.string.game_rules_pool_col_bets) to Dimen.ControlWidthMedium,
        stringResource(R.string.game_rules_pool_col_min_quotas) to Dimen.TableColumnWidthMedium,
        stringResource(R.string.game_rules_pool_col_max_quotas) to Dimen.TableColumnWidthMedium,
        stringResource(R.string.game_rules_pool_col_min_quota) to Dimen.TableColumnWidthLarge,
        stringResource(R.string.game_rules_pool_col_min_pool) to Dimen.TableColumnWidthLarge,
        stringResource(R.string.game_rules_pool_col_max_pool) to Dimen.TableColumnWidthXLarge,
        stringResource(R.string.game_rules_pool_col_max_games) to Dimen.TableColumnWidthMedium
    )
    val columnWidths = columns.map { it.second }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .background(scheme.surface, MaterialTheme.shapes.small)
            .padding(Dimen.Spacing8)
    ) {
        // Header
        Row(Modifier.padding(bottom = Dimen.Spacing8)) {
            columns.forEach { (text, width) ->
                Text(
                    text = text,
                    modifier = Modifier.width(width),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
            }
        }

        HorizontalDivider(thickness = Dimen.Border.Hairline, color = scheme.outlineVariant)

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
                row.forEachIndexed { index, cell ->
                    Text(
                        text = cell,
                        modifier = Modifier.width(columnWidths[index]),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
            HorizontalDivider(thickness = Dimen.Border.Hairline, color = scheme.outlineVariant.copy(alpha = 0.3f))
        }
    }
}
