package com.cebolao.lotofacil.ui.util

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreenViewProvider
import com.cebolao.lotofacil.ui.theme.Motion

object SplashAnimationHelper {
    private const val SPLASH_ICON_SCALE_TARGET = 0.5f

    /**
     * Executes the standard exit animation for the splash screen.
     */
    fun animateExit(provider: SplashScreenViewProvider) {
        val duration = Motion.Duration.SPLASH.toLong()
        val set = AnimatorSet().apply {
            interpolator = AnticipateInterpolator()
            this.duration = duration
            
            // Fade out the container (always present)
            val fadeOutContainer = ObjectAnimator.ofFloat(provider.view, View.ALPHA, 1f, 0f)

            // Icon animations
            val iconView = provider.iconView
            val scaleX = ObjectAnimator.ofFloat(iconView, View.SCALE_X, 1f, SPLASH_ICON_SCALE_TARGET)
            val scaleY = ObjectAnimator.ofFloat(iconView, View.SCALE_Y, 1f, SPLASH_ICON_SCALE_TARGET)
            val fadeIcon = ObjectAnimator.ofFloat(iconView, View.ALPHA, 1f, 0f)
            
            playTogether(fadeOutContainer, scaleX, scaleY, fadeIcon)
            doOnEnd { provider.remove() }
        }
        set.start()
    }
}
