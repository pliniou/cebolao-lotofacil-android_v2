package com.cebolao.lotofacil.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.dp

/**
 * Motion Design system following Material Design 3.
 * Provides consistent and performant animation specs throughout the application.
 */
object Motion {
    // --- DURATIONS (ms) ---
    /**
     * Standard animation durations.
     * Based on Material Design 3 guidelines.
     */
    object Duration {
        /** Fast animations - immediate feedback (150ms) */
        const val FAST = 150
        /** Standard animations - state transitions (300ms) */
        const val MEDIUM = 300
        /** Element entry animations (350ms) */
        const val ENTER = 350
        /** Element exit animations (250ms) */
        const val EXIT = 250
        /** STAGGER delay for lists (50ms) */
        const val STAGGER = 50
        /** Splash exit duration (500ms) */
        const val SPLASH = 500
    }
    
    // --- EASING CURVES ---
    /**
     * Easing curves following M3 motion system.
     * - Standard: For most animations
     * - Emphasized: For animations needing more attention
     * - Linear: For continuous rotations and progress indicators
     */
    object Easing {
        /** Standard M3 curve - smooth entry and exit */
        val Standard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        /** Emphasized deceleration - for entering elements */
        val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
        /** Emphasized acceleration - for exiting elements */
        val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
    }
    
    // --- SPRING SPECS ---
    /**
     * Pre-configured spring specifications.
     * Springs are preferred for natural and responsive animations.
     */
    object Spring {
        /** Gentle spring - for smooth and natural animations */
        fun <T> gentle() = spring<T>(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        )
        
        /** Snappy spring - for interaction feedback */
        fun <T> snappy() = spring<T>(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        )
    }
    
    // --- TWEEN SPECS ---
    /**
     * Pre-configured tween specifications.
     * Tweens are used when predictable durations are needed.
     */
    object Tween {
        /** Fast tween for micro-interactions */
        fun <T> fast(reduceMotion: Boolean = false) =
            tween<T>(if (reduceMotion) 0 else Duration.FAST, easing = Easing.Standard)
        /** Medium tween for standard transitions */
        fun <T> medium(reduceMotion: Boolean = false) =
            tween<T>(if (reduceMotion) 0 else Duration.MEDIUM, easing = Easing.Standard)
        /** Entry tween with deceleration */
        fun <T> enter(reduceMotion: Boolean = false) =
            tween<T>(if (reduceMotion) 0 else Duration.ENTER, easing = Easing.EmphasizedDecelerate)
        /** Exit tween with acceleration */
        fun <T> exit(reduceMotion: Boolean = false) =
            tween<T>(if (reduceMotion) 0 else Duration.EXIT, easing = Easing.EmphasizedAccelerate)
    }
    
    // --- OFFSET SPECS ---
    /**
     * Pre-defined offsets for slide and fade animations.
     */
    object Offset {
        /** Offset for slide from bottom to top */
        val SlideUp = 24.dp
        /** Offset for slide from top to bottom */
        val SlideDown = 24.dp
        /** Initial scale for zoom animations */
        const val SCALE = 0.92f
        /** Press scale (press feedback) */
        const val PRESSSCALE = 0.96f
        /** Selection scale (selected feedback) */
        const val SELECTSCALE = 1.05f
    }
}

// --- ANIMATION UTILITIES ---
/**
 * Calculates stagger delay for list animations.
 * @param index Item index in the list
 * @param baseDelay Base delay in ms
 * @param maxDelay Maximum total delay in ms
 * @return Calculated delay in ms
 */
fun staggerDelay(
    index: Int,
    baseDelay: Int = Motion.Duration.STAGGER,
    maxDelay: Int = 500
): Int {
    return (index * baseDelay).coerceAtMost(maxDelay)
}
