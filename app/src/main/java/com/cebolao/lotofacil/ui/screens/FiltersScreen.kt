package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.FilterState
import com.cebolao.lotofacil.domain.model.FilterType
import com.cebolao.lotofacil.navigation.Screen
import com.cebolao.lotofacil.ui.components.common.AppConfirmationDialog
import com.cebolao.lotofacil.ui.components.filter.FilterGroupColumn
import com.cebolao.lotofacil.ui.components.filter.FilterPresetSelector
import com.cebolao.lotofacil.ui.components.filter.GenerationActionsPanel
import com.cebolao.lotofacil.ui.components.common.InfoDialog
import com.cebolao.lotofacil.ui.components.stats.InfoPoint
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.components.layout.StandardPageLayout
import com.cebolao.lotofacil.ui.components.filter.filterSection
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.filterIcon
import com.cebolao.lotofacil.viewmodels.FiltersViewModel
import com.cebolao.lotofacil.viewmodels.NavigationEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FiltersScreen(navController: NavController, viewModel: FiltersViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val useColumnLayout = configuration.screenWidthDp >= 600

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is NavigationEvent.NavigateToGeneratedGames -> {
                    navController.navigate(Screen.GeneratedGames.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                is NavigationEvent.ShowSnackbar -> {
                    val message = if (event.labelRes != null) {
                        context.getString(
                            event.messageRes,
                            context.getString(event.labelRes)
                        )
                    } else {
                        context.getString(event.messageRes)
                    }
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    }

    if (uiState.showResetDialog) {
        AppConfirmationDialog(
            title = R.string.filters_reset_dialog_title,
            message = R.string.filters_reset_dialog_message,
            confirmText = R.string.filters_reset_confirm,
            onConfirm = viewModel::confirmResetFilters,
            onDismiss = viewModel::dismissResetDialog,
            icon = Icons.Default.DeleteSweep
        )
    }

    uiState.filterInfoToShow?.let { type ->
        InfoDialog(
            dialogTitle = stringResource(
                R.string.filters_info_dialog_title_format,
                stringResource(type.titleRes)
            ),
            icon = type.filterIcon,
            onDismissRequest = viewModel::dismissFilterInfo
        ) {
            AppCard {
                InfoPoint(
                    title = stringResource(R.string.filters_info_button_description),
                    description = stringResource(type.descriptionRes)
                )
            }
        }
    }

    val allFilters: List<FilterState> = uiState.filterStates
    
    val numericos = remember(allFilters) {
        allFilters.filter {
            it.type in listOf(
                FilterType.SOMA_DEZENAS,
                FilterType.PARES,
                FilterType.PRIMOS,
                FilterType.SEQUENCIAS
            )
        }
    }
    
    val geometricos = remember(allFilters) {
        allFilters.filter {
            it.type in listOf(
                FilterType.MOLDURA,
                FilterType.CENTER
            )
        }
    }
    
    val matematicos = remember(allFilters) {
        allFilters.filter {
            it.type in listOf(
                FilterType.FIBONACCI,
                FilterType.MULTIPLES_OF_3
            )
        }
    }
    
    val contextuais = remember(allFilters) {
        allFilters.filter {
            it.type in listOf(
                FilterType.REPETIDAS_CONCURSO_ANTERIOR
            )
        }
    }

    val titleNumeric = stringResource(R.string.filters_group_numeric)
    val titleGeometric = stringResource(R.string.filters_group_geometric)
    val titleMath = stringResource(R.string.filters_group_math)
    val titleContext = stringResource(R.string.filters_group_context)

    AppScreen(
        title = stringResource(R.string.filters_title),
        subtitle = stringResource(R.string.filters_subtitle),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            TextButton(onClick = viewModel::requestResetFilters) {
                Text(stringResource(R.string.filters_reset_button_description))
            }
        },
        bottomBar = {
            GenerationActionsPanel(
                uiState.generationState,
                viewModel::generateGames,
                viewModel::cancelGeneration
            )
        }
    ) { innerPadding ->
        StandardPageLayout(scaffoldPadding = innerPadding) {
            item(key = "preset_selector") {
                FilterPresetSelector(
                    onPresetSelected = viewModel::applyPreset,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Dimen.Spacing8)
                )
            }

            if (useColumnLayout) {
                item(key = "filter_grid") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(0.5f)) {
                            FilterGroupColumn(
                                titleNumeric, numericos, uiState.lastDraw,
                                viewModel::onFilterToggle, viewModel::onRangeAdjust, viewModel::showFilterInfo
                            )
                            FilterGroupColumn(
                                titleGeometric, geometricos, uiState.lastDraw,
                                viewModel::onFilterToggle, viewModel::onRangeAdjust, viewModel::showFilterInfo,
                                modifier = Modifier.padding(top = Dimen.Spacing8)
                            )
                        }
                        
                        Column(modifier = Modifier.weight(0.5f)) {
                            FilterGroupColumn(
                                titleMath, matematicos, uiState.lastDraw,
                                viewModel::onFilterToggle, viewModel::onRangeAdjust, viewModel::showFilterInfo
                            )
                            FilterGroupColumn(
                                titleContext, contextuais, uiState.lastDraw,
                                viewModel::onFilterToggle, viewModel::onRangeAdjust, viewModel::showFilterInfo,
                                modifier = Modifier.padding(top = Dimen.Spacing8)
                            )
                        }
                    }
                }
            } else {
                filterSection(titleNumeric, numericos, uiState.lastDraw, viewModel::onFilterToggle, viewModel::onRangeAdjust, viewModel::showFilterInfo)
                filterSection(titleGeometric, geometricos, uiState.lastDraw, viewModel::onFilterToggle, viewModel::onRangeAdjust, viewModel::showFilterInfo)
                filterSection(titleMath, matematicos, uiState.lastDraw, viewModel::onFilterToggle, viewModel::onRangeAdjust, viewModel::showFilterInfo)
                filterSection(titleContext, contextuais, uiState.lastDraw, viewModel::onFilterToggle, viewModel::onRangeAdjust, viewModel::showFilterInfo)
            }
        }
    }
}