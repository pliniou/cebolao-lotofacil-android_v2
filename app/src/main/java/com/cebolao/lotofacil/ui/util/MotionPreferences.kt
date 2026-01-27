package com.cebolao.lotofacil.ui.util

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Utility functions for handling Reduced Motion accessibility preference.
 */
object MotionPreferences {

    /**
     * Checks if the user has enabled "Remove animations" (Reduced Motion) in system settings.
     */
    @Composable
    fun rememberPrefersReducedMotion(): Boolean {
        val context = LocalContext.current
        return remember(context) {
            val resolver = context.contentResolver
            runCatching {
                val scale = Settings.Global.getFloat(
                    resolver,
                    Settings.Global.TRANSITION_ANIMATION_SCALE,
                    1f
                )
                val animatorScale = Settings.Global.getFloat(
                    resolver,
                    Settings.Global.ANIMATOR_DURATION_SCALE,
                    1f
                )
                scale == 0f || animatorScale == 0f
            }.getOrDefault(false)
        }
    }

}
