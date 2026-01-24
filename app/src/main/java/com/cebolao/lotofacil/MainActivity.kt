package com.cebolao.lotofacil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.data.repository.THEME_MODE_DARK
import com.cebolao.lotofacil.data.repository.THEME_MODE_LIGHT
import com.cebolao.lotofacil.ui.screens.MainScreen
import com.cebolao.lotofacil.ui.theme.CebolaoLotofacilTheme
import com.cebolao.lotofacil.ui.util.SplashAnimationHelper
import com.cebolao.lotofacil.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splash = installSplashScreen()

        setupSplashScreen(splash)
        splash.setKeepOnScreenCondition { !mainViewModel.uiState.value.isReady }

        setContent {
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

            val darkTheme = when (uiState.themeMode) {
                THEME_MODE_DARK -> true
                THEME_MODE_LIGHT -> false
                else -> isSystemInDarkTheme()
            }

            CebolaoLotofacilTheme(
                darkTheme = darkTheme,
                accentPalette = uiState.accentPalette
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (uiState.isReady) {
                        MainScreen()
                    }
                }
            }
        }
    }

    private fun setupSplashScreen(splash: SplashScreen) {
        splash.setOnExitAnimationListener { provider ->
            SplashAnimationHelper.animateExit(provider)
        }
    }
}
