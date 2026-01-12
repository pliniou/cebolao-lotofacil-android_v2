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
 * Tipos de feedback háptico disponíveis
 */
enum class HapticFeedbackType {
    /** Feedback leve para interações simples (cliques, toggles) */
    LIGHT,

    /** Feedback médio para ações importantes (seleção, confirmação) */
    MEDIUM,

    /** Feedback forte para ações críticas (sucesso, erro) */
    HEAVY,

    /** Feedback para sucesso */
    SUCCESS,

    /** Feedback para erro */
    ERROR
}

/**
 * Gerenciador de feedback háptico
 * Fornece feedback tátil para melhorar a experiência do usuário
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
     * Executa feedback háptico
     */
    fun performHapticFeedback(type: HapticFeedbackType) {
        if (vibrator?.hasVibrator() != true) return

        val effect = when (type) {
            HapticFeedbackType.LIGHT -> VibrationEffect.createOneShot(10, 50)
            HapticFeedbackType.MEDIUM -> VibrationEffect.createOneShot(20, 100)
            HapticFeedbackType.HEAVY -> VibrationEffect.createOneShot(30, 150)
            HapticFeedbackType.SUCCESS -> createSuccessPattern()
            HapticFeedbackType.ERROR -> createErrorPattern()
        }
        vibrator.vibrate(effect)
    }

    private fun createSuccessPattern(): VibrationEffect {
        // Padrão: curto - pausa - curto (sucesso duplo-tap)
        return VibrationEffect.createWaveform(
            longArrayOf(0, 20, 50, 20),
            intArrayOf(0, 100, 0, 100),
            -1
        )
    }

    private fun createErrorPattern(): VibrationEffect {
        // Padrão: curto - curto - curto (erro triplo-tap)
        return VibrationEffect.createWaveform(
            longArrayOf(0, 30, 100, 30, 100, 30),
            intArrayOf(0, 150, 0, 150, 0, 150),
            -1
        )
    }
}

/**
 * Composable para lembrar uma instância de HapticFeedbackManager
 */
@Composable
fun rememberHapticFeedback(): HapticFeedbackManager {
    val context = LocalContext.current
    return remember { HapticFeedbackManager(context) }
}
