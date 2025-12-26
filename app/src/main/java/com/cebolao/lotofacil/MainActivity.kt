package com.cebolao.lotofacil

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.data.repository.THEME_MODE_DARK
import com.cebolao.lotofacil.data.repository.THEME_MODE_LIGHT
import com.cebolao.lotofacil.ui.screens.MainScreen
import com.cebolao.lotofacil.ui.theme.CebolaoLotofacilTheme
import com.cebolao.lotofacil.ui.theme.Motion
import com.cebolao.lotofacil.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val SPLASH_ICON_SCALE_TARGET = 0.5f

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setupSplashScreen(splash)
        splash.setKeepOnScreenCondition { !mainViewModel.uiState.value.isReady }

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
    }

    private fun setupSplashScreen(splash: SplashScreen) {
        splash.setOnExitAnimationListener { provider ->
            animateSplashExit(provider)
        }
    }

    private fun animateSplashExit(provider: SplashScreenViewProvider) {
        val duration = Motion.Duration.SPLASH.toLong()

        val fadeOut = ObjectAnimator.ofFloat(provider.view, View.ALPHA, 1f, 0f)
        val icon = provider.iconView
        val scaleX = ObjectAnimator.ofFloat(icon, View.SCALE_X, 1f, SPLASH_ICON_SCALE_TARGET)
        val scaleY = ObjectAnimator.ofFloat(icon, View.SCALE_Y, 1f, SPLASH_ICON_SCALE_TARGET)
        val fadeIcon = ObjectAnimator.ofFloat(icon, View.ALPHA, 1f, 0f)

        AnimatorSet().apply {
            interpolator = AnticipateInterpolator()
            this.duration = duration
            playTogether(fadeOut, scaleX, scaleY, fadeIcon)
            doOnEnd { provider.remove() }
            start()
        }
    }
}
