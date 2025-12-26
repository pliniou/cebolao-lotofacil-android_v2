package com.cebolao.lotofacil.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.dp

/**
 * Sistema de Motion Design seguindo Material Design 3.
 * Fornece specs de animação consistentes e performáticas para toda a aplicação.
 */
object Motion {
    // --- DURATIONS (ms) ---
    /**
     * Durações padrão para animações.
     * Baseadas nas guidelines do Material Design 3.
     */
    object Duration {
        /** Animações rápidas - feedback imediato (150ms) */
        const val FAST = 150
        /** Animações padrão - transições de estado (300ms) */
        const val MEDIUM = 300
        /** Animações de entrada de elementos (350ms) */
        const val ENTER = 350
        /** Animações de saída de elementos (250ms) */
        const val EXIT = 250
        /** STAGGER delay para listas (50ms) */
        const val STAGGER = 50
        /** Splash exit duration (500ms) */
        const val SPLASH = 500
    }
    
    // --- EASING CURVES ---
    /**
     * Curvas de easing seguindo M3 motion system.
     * - Standard: Para maioria das animações
     * - Emphasized: Para animações que precisam de mais atenção
     * - Linear: Para rotações contínuas e progress indicators
     */
    object Easing {
        /** Curva padrão M3 - entrada e saída suaves */
        val Standard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        /** Desaceleração enfatizada - para elementos entrando */
        val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
        /** Aceleração enfatizada - para elementos saindo */
        val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
    }
    
    // --- SPRING SPECS ---
    /**
     * Especificações de spring pré-configuradas.
     * Springs são preferidos para animações naturais e responsivas.
     */
    object Spring {
        /** Spring suave - para animações gentis e naturais */
        fun <T> gentle() = spring<T>(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        )
        
        /** Spring rápido - para feedback de interação */
        fun <T> snappy() = spring<T>(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        )
    }
    
    // --- TWEEN SPECS ---
    /**
     * Especificações de tween pré-configuradas.
     * Tweens são usados quando precisamos de durações previsíveis.
     */
    object Tween {
        /** Tween rápido para micro-interações */
        fun <T> fast() = tween<T>(Duration.FAST, easing = Easing.Standard)
        /** Tween médio para transições padrão */
        fun <T> medium() = tween<T>(Duration.MEDIUM, easing = Easing.Standard)
        /** Tween de entrada com desaceleração */
        fun <T> enter() = tween<T>(Duration.ENTER, easing = Easing.EmphasizedDecelerate)
        /** Tween de saída com aceleração */
        fun <T> exit() = tween<T>(Duration.EXIT, easing = Easing.EmphasizedAccelerate)
    }
    
    // --- OFFSET SPECS ---
    /**
     * Offsets pré-definidos para animações de slide e fade.
     */
    object Offset {
        /** Offset para slide de baixo para cima */
        val SlideUp = 24.dp
        /** Offset para slide de cima para baixo */
        val SlideDown = 24.dp
        /** Escala inicial para animações de zoom */
        const val SCALE = 0.92f
        /** Escala de pressão (press feedback) */
        const val PRESSSCALE = 0.96f
        /** Escala de seleção (selected feedback) */
        const val SELECTSCALE = 1.05f
    }
}

// --- ANIMATION UTILITIES ---
/**
 * Calcula o delay de stagger para animações em lista.
 * @param index Índice do item na lista
 * @param baseDelay Delay base em ms
 * @param maxDelay Delay máximo total em ms
 * @return Delay calculado em ms
 */
fun staggerDelay(
    index: Int,
    baseDelay: Int = Motion.Duration.STAGGER,
    maxDelay: Int = 500
): Int {
    return (index * baseDelay).coerceAtMost(maxDelay)
}
