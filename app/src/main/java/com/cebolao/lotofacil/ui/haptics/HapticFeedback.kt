package com.cebolao.lotofacil.ui.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Available haptic feedback types
 */
enum class HapticFeedbackType {
    /** Light feedback for simple interactions (clicks, toggles) */
    LIGHT,

    /** Medium feedback for confirmations */
    MEDIUM,

    /** Strong feedback for important actions */
    STRONG,

    /** Success feedback pattern */
    SUCCESS,

    /** Error feedback pattern */
    ERROR
}

/**
 * Haptic feedback manager
 * Provides tactile feedback to improve user experience
 */
class HapticFeedbackManager(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Performs haptic feedback
     */
    fun performHapticFeedback(type: HapticFeedbackType) {
        if (vibrator?.hasVibrator() != true) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ (API 29+) - Use Predefined Effects
            val effect = when (type) {
                HapticFeedbackType.LIGHT -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                HapticFeedbackType.MEDIUM -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                HapticFeedbackType.STRONG -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                HapticFeedbackType.SUCCESS -> createSuccessPattern()
                HapticFeedbackType.ERROR -> createErrorPattern()
            }
            vibrator.vibrate(effect)
        } else {
            // Android 8.0 - 9.0 (API 26-28) - Fallback to OneShot/Waveform
            val effect = when (type) {
                HapticFeedbackType.LIGHT -> VibrationEffect.createOneShot(10, 50) 
                HapticFeedbackType.MEDIUM -> VibrationEffect.createOneShot(20, 100)
                HapticFeedbackType.STRONG -> VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
                HapticFeedbackType.SUCCESS -> createSuccessPattern()
                HapticFeedbackType.ERROR -> createErrorPattern()
            }
            vibrator.vibrate(effect)
        }
    }

    private fun createSuccessPattern(): VibrationEffect {
        // Pattern: short - pause - short (double-tap success)
        return VibrationEffect.createWaveform(
            longArrayOf(0, 20, 50, 20),
            intArrayOf(0, 100, 0, 100),
            -1
        )
    }

    private fun createErrorPattern(): VibrationEffect {
        // Pattern: short - short - short (triple-tap error)
        return VibrationEffect.createWaveform(
            longArrayOf(0, 30, 100, 30, 100, 30),
            intArrayOf(0, 150, 0, 150, 0, 150),
            -1
        )
    }
}

/**
 * Composable to remember a HapticFeedbackManager instance
 */
@Composable
fun rememberHapticFeedback(): HapticFeedbackManager {
    val context = LocalContext.current
    return remember { HapticFeedbackManager(context) }
}
