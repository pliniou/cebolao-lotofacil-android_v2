package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.presentation.viewmodel.ResultsUiState
import com.cebolao.lotofacil.presentation.viewmodel.ResultsViewModel
import com.cebolao.lotofacil.ui.components.game.NumberBall
import com.cebolao.lotofacil.ui.components.game.NumberBallSize
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.components.layout.CardVariant
import com.cebolao.lotofacil.ui.components.layout.StandardPageLayout
import com.cebolao.lotofacil.ui.screens.AppScreen
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.util.Formatters
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ResultsScreen(
    viewModel: ResultsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AppScreen(
        title = stringResource(R.string.results_title), // Need to add string
        navigationIcon = {
            androidx.compose.material3.IconButton(onClick = onNavigateBack) {
                androidx.compose.material3.Icon(
                    imageVector = com.cebolao.lotofacil.ui.theme.AppIcons.ArrowBack,
                    contentDescription = stringResource(R.string.general_back)
                )
            }
        }
    ) { innerPadding ->
        StandardPageLayout(scaffoldPadding = innerPadding) {
            when (val state = uiState) {
                is ResultsUiState.Loading -> {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(Dimen.Spacing16),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is ResultsUiState.Error -> {
                    item {
                        Text(
                            text = "Error loading results", // Placeholder
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is ResultsUiState.Success -> {
                    items(
                        items = state.draws.sortedByDescending { it.contestNumber },
                        key = { it.contestNumber }
                    ) { draw ->
                        DrawListItem(draw = draw)
                    }
                }
            }
        }
    }
}

@Composable
fun DrawListItem(draw: Draw) {
    AppCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = Dimen.Spacing8),
        variant = CardVariant.Solid,
        contentPadding = Dimen.Spacing12
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Concurso ${draw.contestNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                val dateText = rememberDrawDate(draw.date ?: 0L)
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Balls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                draw.numbers.sorted().forEach { number ->
                    NumberBall(
                        number = number,
                        sizeVariant = NumberBallSize.Small,
                        modifier = Modifier.size(24.dp) // Force small size for dense list
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberDrawDate(timestamp: Long): String {
    return androidx.compose.runtime.remember(timestamp) {
        val formatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())
        formatter.format(Date(timestamp))
    }
}
