package br.com.loterias.cebolaolotofacil.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.loterias.cebolaolotofacil.presentation.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    val state = viewModel.uiState.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Cebolão Lotofácil") })
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorScreen(message = state.error)
            else -> ResultList(results = state.results, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorScreen(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun ResultList(results: List<br.com.loterias.cebolaolotofacil.domain.model.LotofacilResult>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        results.forEach { result ->
            Text("Concurso ${result.concurso} - ${result.data}")
            Text(result.dezenas.joinToString(" - "), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
        }
    }
}
