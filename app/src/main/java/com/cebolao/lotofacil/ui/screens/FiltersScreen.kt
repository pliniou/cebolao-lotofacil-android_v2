package com.cebolao.lotofacil.ui.screens

import android.annotation.SuppressLint
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
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
import com.cebolao.lotofacil.navigation.GeneratedGamesRoute
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
import com.cebolao.lotofacil.presentation.viewmodel.FiltersUiEvent
import com.cebolao.lotofacil.presentation.viewmodel.FiltersScreenState
import com.cebolao.lotofacil.presentation.viewmodel.FiltersViewModel
import com.cebolao.lotofacil.presentation.viewmodel.NavigationEvent
import com.cebolao.lotofacil.presentation.viewmodel.GenerationUiState
import com.cebolao.lotofacil.domain.model.FilterPreset
import com.cebolao.lotofacil.domain.model.FilterPresets
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.collectLatest

data class FilterCategory(
    val titleRes: Int,
    val states: List<FilterState>
)

@Composable
fun FiltersScreen(navController: NavController, viewModel: FiltersViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val resources = LocalContext.current.resources
    val currentResources by rememberUpdatedState(resources)
    
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is NavigationEvent.NavigateToGeneratedGames -> {
                    navController.navigate(GeneratedGamesRoute) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                is NavigationEvent.ShowSnackbar -> {
                    val message = if (event.labelRes != null) {
                        currentResources.getString(
                            event.messageRes,
                            currentResources.getString(event.labelRes)
                        )
                    } else {
                        currentResources.getString(event.messageRes)
                    }
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    }

    FiltersScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        listState = listState
    )
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun FiltersScreenContent(
    uiState: FiltersScreenState,
    snackbarHostState: SnackbarHostState,
    onEvent: (FiltersUiEvent) -> Unit,
    listState: LazyListState
) {
    val configuration = LocalConfiguration.current
    val useColumnLayout = configuration.screenWidthDp >= 600

    // Local state for quantity and preset selection
    var quantity by rememberSaveable { mutableIntStateOf(10) }
    var selectedPreset by rememberSaveable { mutableStateOf<FilterPreset?>(null) }

    if (uiState.showResetDialog) {
        AppConfirmationDialog(
            title = R.string.filters_reset_dialog_title,
            message = R.string.filters_reset_dialog_message,
            confirmText = R.string.filters_reset_confirm,
            onConfirm = {
                selectedPreset = null
                onEvent(FiltersUiEvent.ConfirmResetFilters)
            },
            onDismiss = { onEvent(FiltersUiEvent.DismissResetDialog) },
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
            onDismissRequest = { onEvent(FiltersUiEvent.DismissFilterInfo) }
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

    AppScreen(
        title = stringResource(R.string.filters_title),
        subtitle = stringResource(R.string.filters_subtitle),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            TextButton(onClick = { onEvent(FiltersUiEvent.RequestResetFilters) }) {
                Text(stringResource(R.string.filters_reset_button_description))
            }
        },
        bottomBar = {
            GenerationActionsPanel(
                quantity = quantity,
                onQuantityChanged = { quantity = it.coerceIn(1, 50) },
                onGenerate = { onEvent(FiltersUiEvent.GenerateGames(quantity)) },
                isGenerating = uiState.generationState is GenerationUiState.Loading,
                modifier = Modifier.padding(bottom = Dimen.ItemSpacing)
            )
        }
    ) { innerPadding ->
        val categories = listOf(
            FilterCategory(R.string.filter_category_numeric, numericos),
            FilterCategory(R.string.filter_category_geometric, geometricos),
            FilterCategory(R.string.filter_category_math, matematicos),
            FilterCategory(R.string.filter_category_context, contextuais)
        )

        val categoryTitles = categories.map { stringResource(it.titleRes) }

        StandardPageLayout(
            scaffoldPadding = innerPadding,
            listState = listState
        ) {
            item(key = "preset_selector") {
                FilterPresetSelector(
                    selectedPreset = selectedPreset,
                    presets = FilterPresets.all,
                    onPresetSelected = {
                        selectedPreset = it
                        onEvent(FiltersUiEvent.ApplyPreset(it))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Dimen.ItemSpacing)
                )
            }

            if (useColumnLayout) {
                item(key = "filter_grid") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(0.5f)) {
                            categories.take(2).forEachIndexed { index, category ->
                                FilterGroupColumn(
                                    categoryTitles[index], category.states, uiState.lastDraw,
                                    onToggle = { t, e -> onEvent(FiltersUiEvent.ToggleFilter(t, e)) },
                                    onRangeAdjustment = { t, r -> onEvent(FiltersUiEvent.AdjustRange(t, r)) },
                                    onInfoRequest = { onEvent(FiltersUiEvent.ShowFilterInfo(it)) },
                                    modifier = if (index > 0) Modifier.padding(top = Dimen.ItemSpacing) else Modifier
                                )
                            }
                        }
                        
                        Column(modifier = Modifier.weight(0.5f)) {
                            categories.drop(2).forEachIndexed { index, category ->
                                val globalIndex = index + 2
                                FilterGroupColumn(
                                    categoryTitles[globalIndex], category.states, uiState.lastDraw,
                                    onToggle = { t, e -> onEvent(FiltersUiEvent.ToggleFilter(t, e)) },
                                    onRangeAdjustment = { t, r -> onEvent(FiltersUiEvent.AdjustRange(t, r)) },
                                    onInfoRequest = { onEvent(FiltersUiEvent.ShowFilterInfo(it)) },
                                    modifier = if (index > 0) Modifier.padding(top = Dimen.ItemSpacing) else Modifier
                                )
                            }
                        }
                    }
                }
            } else {
                categories.forEachIndexed { index, category ->
                    filterSection(
                        categoryTitles[index], category.states, uiState.lastDraw,
                        onToggle = { t, e -> onEvent(FiltersUiEvent.ToggleFilter(t, e)) },
                        onRangeAdjustment = { t, r -> onEvent(FiltersUiEvent.AdjustRange(t, r)) },
                        onInfoRequest = { onEvent(FiltersUiEvent.ShowFilterInfo(it)) }
                    )
                }
            }
        }
    }
}
