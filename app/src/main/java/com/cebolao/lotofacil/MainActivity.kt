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
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.data.repository.THEME_MODE_DARK
import com.cebolao.lotofacil.data.repository.THEME_MODE_LIGHT
import com.cebolao.lotofacil.ui.screens.MainScreen
import com.cebolao.lotofacil.ui.theme.CebolaoLotofacilTheme
import com.cebolao.lotofacil.ui.util.SplashAnimationHelper
import com.cebolao.lotofacil.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d(TAG, "onCreate called")
        val splash = installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setupSplashScreen(splash)
        splash.setKeepOnScreenCondition { !mainViewModel.uiState.value.isReady }

        try {
            setContent {
                val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
                val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()
                val accentPalette by mainViewModel.accentPalette.collectAsStateWithLifecycle()

                val darkTheme = when (themeMode) {
                    THEME_MODE_DARK -> true
                    THEME_MODE_LIGHT -> false
                    else -> isSystemInDarkTheme()
                }

                CebolaoLotofacilTheme(
                    darkTheme = darkTheme,
                    accentPalette = accentPalette
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
        } catch (e: IllegalStateException) {
            android.util.Log.e(TAG, "Illegal state in setContent", e)
            throw e
        } catch (e: RuntimeException) {
            android.util.Log.e(TAG, "Runtime error in setContent", e)
            throw e
        }
    }

    override fun onStart() {
        super.onStart()
        android.util.Log.d(TAG, "onStart called")
    }

    override fun onResume() {
        super.onResume()
        android.util.Log.d(TAG, "onResume called")
    }

    override fun onPause() {
        super.onPause()
        android.util.Log.d(TAG, "onPause called")
    }

    override fun onStop() {
        super.onStop()
        android.util.Log.d(TAG, "onStop called")
    }

    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d(TAG, "onDestroy called")
    }

    private fun setupSplashScreen(splash: SplashScreen) {
        splash.setOnExitAnimationListener { provider ->
            SplashAnimationHelper.animateExit(provider)
        }
    }
}
