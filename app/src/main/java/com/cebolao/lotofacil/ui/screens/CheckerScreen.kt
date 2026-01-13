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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.cebolao.lotofacil.domain.model.toCheckResult
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.components.common.AppConfirmationDialog
import com.cebolao.lotofacil.ui.components.common.MessageState
import com.cebolao.lotofacil.ui.components.stats.CheckResultCard
import com.cebolao.lotofacil.ui.components.game.NumberBallSize
import com.cebolao.lotofacil.ui.components.game.NumberGrid
import com.cebolao.lotofacil.ui.components.layout.StandardPageLayout
import com.cebolao.lotofacil.ui.components.stats.FinancialPerformanceCard
import com.cebolao.lotofacil.ui.components.stats.GameQualityCard
import com.cebolao.lotofacil.ui.components.stats.SimpleStatsCard
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Shapes
import com.cebolao.lotofacil.presentation.viewmodel.CheckerUiState
import com.cebolao.lotofacil.presentation.viewmodel.CheckerViewModel
import com.cebolao.lotofacil.presentation.viewmodel.CheckerUiEvent
import com.cebolao.lotofacil.presentation.viewmodel.CheckerEffect
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
    val currentContext by rememberUpdatedState(context)

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { effect ->
            when (effect) {
                is CheckerEffect.ShowMessage -> snackbarHostState.showSnackbar(message = currentContext.getString(effect.messageResId))
                is CheckerEffect.RequestSaveConfirmation -> { /* Handle dialog state if needed or show snackbar */ }
                is CheckerEffect.RequestReplaceConfirmation -> { /* Handle dialog state */ }
            }
        }
    }

    CheckerScreenContent(
        uiState = uiState,
        selectedNumbers = selectedNumbers,
        isGameComplete = isGameComplete,
        gameScore = gameScore,
        isHeatmapEnabled = isHeatmapEnabled,
        heatmapIntensities = heatmapIntensities,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun CheckerScreenContent(
    uiState: CheckerUiState,
    selectedNumbers: Set<Int>,
    isGameComplete: Boolean,
    gameScore: com.cebolao.lotofacil.domain.model.GameScore?,
    isHeatmapEnabled: Boolean,
    heatmapIntensities: Map<Int, Float>,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: (() -> Unit)?,
    onEvent: (CheckerUiEvent) -> Unit
) {
    var showClearDialog by rememberSaveable { mutableStateOf(false) }

    if (showClearDialog) {
        AppConfirmationDialog(
            title = R.string.games_clear_dialog_title,
            message = R.string.games_clear_dialog_message,
            confirmText = R.string.games_clear_confirm,
            onConfirm = {
                onEvent(CheckerUiEvent.ClearNumbers)
                showClearDialog = false
            },
            onDismiss = { showClearDialog = false },
            icon = AppIcons.Delete
        )
    }

    val heatmapColors = remember(isHeatmapEnabled, heatmapIntensities) {
        if (isHeatmapEnabled) {
            heatmapIntensities.mapValues { (_, intensity) ->
                getHeatmapColor(intensity)
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
                    onSave = { onEvent(CheckerUiEvent.RequestSave) },
                    onCheck = { onEvent(CheckerUiEvent.CheckGame) }
                )
            }
        }
    ) { innerPadding ->
        StandardPageLayout(scaffoldPadding = innerPadding) {
            item(key = "counter") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimen.ItemSpacing),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
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
                            fontWeight = FontWeight.SemiBold
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

            // Grid Header with Heatmap Toggle
            item(key = "grid_header") {
                Row(
                   modifier = Modifier
                       .fillMaxWidth()
                       .padding(horizontal = Dimen.Spacing16, vertical = Dimen.Spacing8),
                   horizontalArrangement = Arrangement.End,
                   verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onEvent(CheckerUiEvent.ToggleHeatmap) }
                    ) {
                        Icon(
                            imageVector = if (isHeatmapEnabled) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = stringResource(
                                if (isHeatmapEnabled) R.string.checker_hide_frequency 
                                else R.string.checker_show_frequency
                            ),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(Dimen.SpacingTiny))
                        Text(if (isHeatmapEnabled) "Ocultar Frequência" else "Ver Frequência")
                    }
                }
            }

            // Grid centralizado
            item(key = "grid") {
                NumberGrid(
                    selectedNumbers = selectedNumbers,
                    onNumberClick = { onEvent(CheckerUiEvent.ToggleNumber(it)) },
                    maxSelection = GameConstants.GAME_SIZE,
                    sizeVariant = NumberBallSize.Medium,
                    horizontalArrangement = Arrangement.spacedBy(
                        Dimen.Spacing4,
                        Alignment.CenterHorizontally
                    ),
                    heatmapColors = heatmapColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Termômetro de Qualidade (Novo)
            if (gameScore != null) {
                item(key = "quality_meter") {
                    GameQualityCard(
                        score = gameScore,
                        modifier = Modifier.padding(top = Dimen.ItemSpacing),
                    )
                }
            }

            // Card de instruções (Idle)
            // Show only if no game score and no results (pure idle)
            if (uiState is CheckerUiState.Idle && gameScore == null) {
                item(key = "instructions") {
                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Dimen.ItemSpacing),
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
                CheckerResultSection(uiState)
            }
        }
    }
}

// Helper for Color Lerp
private fun getHeatmapColor(intensity: Float): androidx.compose.ui.graphics.Color {
    // 0.0 (Cold/Blue) -> 0.5 (Yellow) -> 1.0 (Hot/Red)
    val cold = androidx.compose.ui.graphics.Color(0xFF2196F3) // Blue
    val mid = androidx.compose.ui.graphics.Color(0xFFFFEB3B) // Yellow
    val hot = androidx.compose.ui.graphics.Color(0xFFF44336) // Red
    
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
                    Icon(AppIcons.Save, contentDescription = stringResource(R.string.general_save))
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
                    Icon(AppIcons.Success, contentDescription = stringResource(R.string.checker_check_button))
                    Spacer(Modifier.width(Dimen.SpacingTiny))
                    Text(stringResource(R.string.checker_check_button))
                }
            }
        }
    }
}

@Composable
private fun CheckerResultSection(state: CheckerUiState) {
    Column(
        modifier = Modifier.padding(top = Dimen.ItemSpacing),
        verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
    ) {
        when (state) {
            is CheckerUiState.Success -> {
                Text(
                    text = stringResource(R.string.checker_performance_analysis),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = Dimen.ItemSpacing),
                    color = MaterialTheme.colorScheme.onSurface
                )
                // New Financial Card
                FinancialPerformanceCard(
                    report = state.report,
                    modifier = Modifier.fillMaxWidth()
                )
                
                CheckResultCard(state.report.toCheckResult())
                SimpleStatsCard(
                    gameMetrics = state.metrics,
                    modifier = Modifier.fillMaxWidth()
                )
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
                    icon = AppIcons.Success,
                    title = stringResource(R.string.general_error_title),
                    message = stringResource(state.messageResId),
                    iconTint = MaterialTheme.colorScheme.error
                )
            }

            CheckerUiState.Idle -> Unit
        }
    }
}