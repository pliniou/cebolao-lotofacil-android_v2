package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.ui.components.AppCard
import com.cebolao.lotofacil.ui.components.AppConfirmationDialog
import com.cebolao.lotofacil.domain.model.toCheckResult
import com.cebolao.lotofacil.ui.components.CheckResultCard
import com.cebolao.lotofacil.ui.components.FinancialPerformanceCard
import com.cebolao.lotofacil.ui.components.GameQualityCard
import com.cebolao.lotofacil.ui.components.MessageState
import com.cebolao.lotofacil.ui.components.NumberBallSize
import com.cebolao.lotofacil.ui.components.NumberGrid
import com.cebolao.lotofacil.ui.components.SimpleStatsCard
import com.cebolao.lotofacil.ui.components.StandardPageLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Shapes
import com.cebolao.lotofacil.viewmodels.CheckerUiState
import com.cebolao.lotofacil.viewmodels.CheckerViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CheckerScreen(
    viewModel: CheckerViewModel = hiltViewModel(),
    onNavigateBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedNumbers by viewModel.selectedNumbers.collectAsStateWithLifecycle()
    val isGameComplete by viewModel.isGameComplete.collectAsStateWithLifecycle()
    val gameScore by viewModel.gameScore.collectAsStateWithLifecycle()
    
    // Heatmap states
    val isHeatmapEnabled by viewModel.heatmapEnabled.collectAsStateWithLifecycle()
    val heatmapIntensities by viewModel.heatmapIntensities.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { msgId ->
            snackbarHostState.showSnackbar(message = context.getString(msgId))
        }
    }

    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AppConfirmationDialog(
            title = R.string.games_clear_dialog_title,
            message = R.string.games_clear_dialog_message,
            confirmText = R.string.games_clear_confirm,
            onConfirm = {
                viewModel.clearNumbers()
                showClearDialog = false
            },
            onDismiss = { showClearDialog = false },
            icon = AppIcons.Delete
        )
    }

    val scheme = MaterialTheme.colorScheme
    val coldColor = scheme.inversePrimary
    val midColor = scheme.tertiary
    val hotColor = scheme.error

    val heatmapColors = remember(isHeatmapEnabled, heatmapIntensities, coldColor, midColor, hotColor) {
        if (isHeatmapEnabled) {
            heatmapIntensities.mapValues { (_, intensity) ->
                getHeatmapColor(intensity, coldColor, midColor, hotColor)
            }
        } else {
            null
        }
    }

    AppScreen(
        title = stringResource(R.string.checker_title),
        subtitle = stringResource(R.string.checker_subtitle),
        navigationIcon = if (onNavigateBack != null) {
            {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = AppIcons.ArrowBack,
                        contentDescription = stringResource(R.string.general_back)
                    )
                }
            }
        } else null,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            IconButton(
                onClick = { showClearDialog = true },
                enabled = selectedNumbers.isNotEmpty()
            ) {
                Icon(
                    imageVector = AppIcons.Delete,
                    contentDescription = stringResource(R.string.checker_clear_button_description)
                )
            }
        },
        bottomBar = {
            if (isGameComplete) {
                CheckerBottomBar(
                    onSave = viewModel::saveGame,
                    onCheck = viewModel::checkGame
                )
            }
        }
    ) { innerPadding ->
        StandardPageLayout(scaffoldPadding = innerPadding) {
            item(key = "counter") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimen.Spacing8),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
                ) {
                    val scheme = MaterialTheme.colorScheme
                    val isComplete = selectedNumbers.size == GameConstants.GAME_SIZE

                    Surface(
                        color = if (isComplete) scheme.primaryContainer else scheme.surfaceVariant,
                        contentColor = if (isComplete) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                        shape = CircleShape,
                        tonalElevation = Dimen.Elevation.None,
                        shadowElevation = 0.dp
                    ) {
                        Text(
                            text = "${selectedNumbers.size}/${GameConstants.GAME_SIZE}",
                            modifier = Modifier.padding(
                                horizontal = Dimen.Spacing16,
                                vertical = Dimen.Spacing8
                            ),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = stringResource(
                            R.string.checker_selection_instruction,
                            GameConstants.GAME_SIZE
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Grid centralizado
            item(key = "grid") {
                NumberGrid(
                    selectedNumbers = selectedNumbers,
                    onNumberClick = viewModel::toggleNumber,
                    maxSelection = GameConstants.GAME_SIZE,
                    sizeVariant = NumberBallSize.Medium,
                    horizontalArrangement = Arrangement.spacedBy(
                        Dimen.Spacing4,
                        Alignment.CenterHorizontally
                    ),
                    heatmapColors = heatmapColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimen.Spacing16)
                )
            }
            


            // Card de instruções (Idle)
            // Show only if no game score and no results (pure idle)
            if (uiState is CheckerUiState.Idle && gameScore == null) {
                item(key = "instructions") {
                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Dimen.Spacing8),
                        outlined = false,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        contentPadding = Dimen.CardContentPadding
                    ) {
                        MessageState(
                            icon = AppIcons.Success,
                            title = stringResource(R.string.checker_how_it_works_title),
                            message = stringResource(R.string.checker_how_it_works_desc)
                        )
                    }
                }
            }

            // Resultados
            item(key = "results") {
                CheckerResultSection(uiState, gameScore)
            }
        }
    }
}

// Helper for Color Lerp
private fun getHeatmapColor(
    intensity: Float,
    cold: androidx.compose.ui.graphics.Color,
    mid: androidx.compose.ui.graphics.Color,
    hot: androidx.compose.ui.graphics.Color
): androidx.compose.ui.graphics.Color {
    // 0.0 (Cold) -> 0.5 (Mid) -> 1.0 (Hot)
    return if (intensity < 0.5f) {
        androidx.compose.ui.graphics.lerp(cold, mid, intensity * 2f)
    } else {
        androidx.compose.ui.graphics.lerp(mid, hot, (intensity - 0.5f) * 2f)
    }
}

@Composable
private fun CheckerBottomBar(
    onSave: () -> Unit,
    onCheck: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        tonalElevation = Dimen.Elevation.None,
        shadowElevation = 0.dp,
        color = scheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .fillMaxWidth()
        ) {
            HorizontalDivider(
                thickness = Dimen.Border.Hairline,
                color = scheme.outlineVariant.copy(alpha = 0.65f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimen.Spacing8)
                    .padding(bottom = Dimen.Spacing4),
                horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
            ) {
                OutlinedButton(
                    onClick = onSave,
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimen.ActionButtonHeight),
                    shape = Shapes.medium
                ) {
                    Icon(AppIcons.Save, contentDescription = null)
                    Spacer(Modifier.width(Dimen.SpacingTiny))
                    Text(stringResource(R.string.general_save))
                }

                // Ação primária: tonal (flat e moderno)
                FilledTonalButton(
                    onClick = onCheck,
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimen.ActionButtonHeight),
                        shape = Shapes.medium,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = scheme.secondaryContainer,
                            contentColor = scheme.onSecondaryContainer
                        )
                    ) {
                        Icon(AppIcons.Success, contentDescription = null)
                        Spacer(Modifier.width(Dimen.SpacingTiny))
                        Text(stringResource(R.string.checker_check_button))
                    }
                }
            }
        }
    }

@Composable
private fun CheckerResultSection(state: CheckerUiState, gameScore: com.cebolao.lotofacil.domain.model.GameScore?) {
    Column(
        modifier = Modifier.padding(top = Dimen.Spacing16),
        verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
    ) {
        when (state) {
            is CheckerUiState.Success -> {
                // Termômetro de Qualidade
                gameScore?.let { score ->
                    GameQualityCard(
                        score = score,
                        modifier = Modifier.padding(bottom = Dimen.Spacing8)
                    )
                }

                Text(
                    text = stringResource(R.string.checker_performance_analysis),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = Dimen.Spacing4),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Informações de janela e timestamp
                CheckReportInfoCard(
                    drawWindow = state.report.drawWindow,
                    timestamp = state.report.timestamp,
                    isApproximate = state.report.financialMetrics.isApproximate,
                    disclaimer = state.report.financialMetrics.disclaimer
                )
                
                // Financial Card
                FinancialPerformanceCard(
                    projection = state.report.financialMetrics
                )
                
                // CheckResult Card
                CheckResultCard(state.report.toCheckResult())
                
                // Stats Card with explicit mapping
                val metrics = state.gameMetrics
                val statsList = remember(metrics) {
                    kotlinx.collections.immutable.persistentListOf(
                        R.string.stat_sum to metrics.sum,
                        R.string.stat_evens to metrics.evens,
                        R.string.stat_primes to metrics.primes,
                        R.string.stat_frame to metrics.frame,
                        R.string.stat_fibonacci to metrics.fibonacci,
                        R.string.stat_repeated to metrics.repeated,
                        R.string.stat_sequences to metrics.sequences,
                        R.string.stat_quadrants to metrics.quadrants
                    )
                }
                
                // Converting ResId keys to String for the generic card
                val resolvedStats = statsList.map { (key, value) ->
                    stringResource(key) to value.toString()
                }.toImmutableList()

                SimpleStatsCard(resolvedStats)
            }

            is CheckerUiState.Loading -> {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    outlined = true,
                    color = MaterialTheme.colorScheme.surface,
                    contentPadding = Dimen.CardContentPadding
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)) {
                        Text(
                            text = stringResource(R.string.general_loading),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            is CheckerUiState.Error -> {
                MessageState(
                    icon = AppIcons.Success, // Placeholder icon
                    title = stringResource(R.string.general_error_title),
                    message = stringResource(state.messageResId),
                    iconTint = MaterialTheme.colorScheme.error
                )
            }

            CheckerUiState.Idle -> Unit
        }
    }
}

@Composable
private fun CheckReportInfoCard(
    drawWindow: com.cebolao.lotofacil.domain.model.DrawWindow,
    timestamp: Long,
    isApproximate: Boolean,
    disclaimer: String?,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")) }
    val dateStr = dateFormat.format(Date(timestamp))
    
    AppCard(
        modifier = modifier.fillMaxWidth(),
        outlined = true,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(Dimen.Spacing16),
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
        ) {

            Text(
                text = stringResource(R.string.checker_info_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.checker_info_window_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${drawWindow.firstContest} - ${drawWindow.lastContest} (${drawWindow.totalDraws} concursos)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.checker_info_date_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            if (isApproximate && disclaimer != null) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = Dimen.Spacing8),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
                Text(
                    text = "⚠️ $disclaimer",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
