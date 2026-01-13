package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cebolao.lotofacil.navigation.AppNavHost
import com.cebolao.lotofacil.ui.components.layout.AppBottomBar
import com.cebolao.lotofacil.presentation.viewmodel.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val startDestinationState by viewModel.startDestination.collectAsStateWithLifecycle()
    val startDestination = startDestinationState.destination // This needs to be Any, check MainViewModel

    Scaffold(
        bottomBar = { AppBottomBar(navController, navBackStackEntry?.destination) }
    ) { innerPadding ->
        if (!uiState.isReady) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            AppNavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding),
                mainViewModel = viewModel
            )
        }
    }
}
