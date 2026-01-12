package com.cebolao.lotofacil.ui.components.stats

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.domain.model.CheckReport
import com.cebolao.lotofacil.domain.model.FinancialProjection
import com.cebolao.lotofacil.domain.model.GameComputedMetrics
import com.cebolao.lotofacil.ui.components.common.StandardInfoRow
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen

@SuppressLint("DefaultLocale")
@Composable
fun FinancialPerformanceCard(
    report: CheckReport,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier,
        title = "Desempenho Financeiro",
        outlined = true
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)) {
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
                description = "R$ ${report.financialMetrics.profit} (ROI: ${String.format("%.2f", report.financialMetrics.roi)}%)"
            )
        }
    }
}

@Composable
fun SimpleStatsCard(
    gameMetrics: GameComputedMetrics,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier,
        title = "Estatísticas Básicas",
        outlined = true
    ) {
         Column(verticalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Soma: ${gameMetrics.sum}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Pares: ${gameMetrics.evens}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Ímpares: ${15 - gameMetrics.evens}", style = MaterialTheme.typography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Primos: ${gameMetrics.primes}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Fibonacci: ${gameMetrics.fibonacci}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Moldura: ${gameMetrics.frame}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FinancialPerformanceCardPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
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
private fun SimpleStatsCardPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
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
