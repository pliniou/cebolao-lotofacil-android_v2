package com.cebolao.lotofacil.ui.util

import android.provider.Settings
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.cebolao.lotofacil.ui.theme.Motion

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

    /**
     * Returns a duration that respects the reduced motion setting.
     * If reduced motion is enabled, returns 0. Otherwise returns the original duration.
     */
    @Composable
    fun adaptiveDuration(millis: Int): Int {
        val prefersReduced = rememberPrefersReducedMotion()
        return if (prefersReduced) 0 else millis
    }

    /**
     * Returns a spring spec that respects the reduced motion setting.
     * If reduced motion is enabled, returns a stiff spring (nearly instant).
     * Otherwise returns the original spec (or a gentle default).
     */
    @Composable
    fun <T> adaptiveSpring(
        defaultSpec: SpringSpec<T> = Motion.Spring.gentle()
    ): SpringSpec<T> {
        val prefersReduced = rememberPrefersReducedMotion()
        return if (prefersReduced) {
            spring(stiffness = 10_000f, visibilityThreshold = defaultSpec.visibilityThreshold)
        } else {
            defaultSpec
        }
    }
}
