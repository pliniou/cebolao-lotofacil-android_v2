package com.cebolao.lotofacil.ui.components.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.cebolao.lotofacil.domain.model.CheckReport
import com.cebolao.lotofacil.domain.model.FinancialProjection
import com.cebolao.lotofacil.domain.model.GameComputedMetrics
import com.cebolao.lotofacil.ui.components.common.StandardInfoRow
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.GlassCard
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val ptBrLocale = Locale.forLanguageTag("pt-BR")
private val percentageFormat = DecimalFormat("0.00", DecimalFormatSymbols(ptBrLocale))

@Composable
fun FinancialPerformanceCard(
    report: CheckReport,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Dimen.Spacing16),
            verticalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)
        ) {
            Text(
                text = "Desempenho Financeiro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            StandardInfoRow(
                icon = AppIcons.Wallet,
                title = "Investimento",
                description = "R$ ${report.financialMetrics.investment}"
            )
            StandardInfoRow(
                icon = AppIcons.Paid,
                title = "Retorno",
                description = "R$ ${report.financialMetrics.revenue}"
            )
            StandardInfoRow(
                icon = if (report.financialMetrics.breakEven) AppIcons.TrendingUp else AppIcons.TrendingDown,
                title = "Resultado",
                description = "R$ ${report.financialMetrics.profit} (ROI: ${percentageFormat.format(report.financialMetrics.roi)}%)"
            )
        }
    }
}

@Composable
fun SimpleStatsCard(
    gameMetrics: GameComputedMetrics,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Dimen.CardContentPadding),
            verticalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)
        ) {
            Text(
                text = "Estatísticas do Jogo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Primeira linha: Soma, Pares, Ímpares
            StatsRow(
                items = listOf(
                    SimpleStatItem("Soma", gameMetrics.sum.toString(), MaterialTheme.colorScheme.primary),
                    SimpleStatItem("Pares", gameMetrics.evens.toString(), MaterialTheme.colorScheme.secondary),
                    SimpleStatItem("Ímpares", (15 - gameMetrics.evens).toString(), MaterialTheme.colorScheme.tertiary)
                )
            )
            
            // Segunda linha: Primos, Fibonacci, Moldura
            StatsRow(
                items = listOf(
                    SimpleStatItem("Primos", gameMetrics.primes.toString(), MaterialTheme.colorScheme.primary),
                    SimpleStatItem("Fibonacci", gameMetrics.fibonacci.toString(), MaterialTheme.colorScheme.secondary),
                    SimpleStatItem("Moldura", gameMetrics.frame.toString(), MaterialTheme.colorScheme.tertiary)
                )
            )
            
            // Terceira linha: Múltiplos de 3, Centro, Sequências
            StatsRow(
                items = listOf(
                    SimpleStatItem("Mult. 3", gameMetrics.multiplesOf3.toString(), MaterialTheme.colorScheme.primary),
                    SimpleStatItem("Centro", gameMetrics.center.toString(), MaterialTheme.colorScheme.secondary),
                    SimpleStatItem("Sequências", gameMetrics.sequences.toString(), MaterialTheme.colorScheme.tertiary)
                )
            )
        }
    }
}

@Composable
private fun StatsRow(
    items: List<SimpleStatItem>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        items.forEach { item ->
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = item.color
                )
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class SimpleStatItem(
    val label: String,
    val value: String,
    val color: androidx.compose.ui.graphics.Color
)

@Preview(showBackground = true)
@Composable
internal fun FinancialPerformanceCardPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(Dimen.Spacing16)) {
            FinancialPerformanceCard(
                report = CheckReport(
                    ticket = com.cebolao.lotofacil.domain.model.LotofacilGame.fromNumbers(emptySet()),
                    drawWindow = com.cebolao.lotofacil.domain.model.DrawWindow(1, 10, 10),
                    hits = emptyList(),
                    financialMetrics = FinancialProjection(
                        investment = "100.00",
                        revenue = "150.00",
                        profit = "50.00",
                        roi = 50.0f,
                        breakEven = true
                    ),
                    timestamp = 0L,
                    sourceHash = ""
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun SimpleStatsCardPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(Dimen.Spacing16)) {
            SimpleStatsCard(
                gameMetrics = GameComputedMetrics(
                    sum = 180,
                    evens = 7,
                    primes = 5,

                    fibonacci = 4,
                    frame = 8,
                    sequences = 3,
                    multiplesOf3 = 5,
                    center = 4,
                    repeated = 9
                )
            )
        }
    }
}
