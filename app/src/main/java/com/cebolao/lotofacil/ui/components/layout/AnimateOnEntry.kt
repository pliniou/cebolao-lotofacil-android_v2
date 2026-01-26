package com.cebolao.lotofacil.ui.components.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.cebolao.lotofacil.ui.theme.Motion
import com.cebolao.lotofacil.ui.util.MotionPreferences
import kotlinx.coroutines.delay

/**
 * Variantes de animação de entrada disponíveis.
 */
enum class EntryAnimation {
    /** Slide de baixo + fade (padrão) */
    SlideUp,
    /** Fade simples */
    Fade,
    /** SCALE + fade (zoom in) */
    Scale,
    /** Slide de cima + fade */
    SlideDown
}

/**
 * Componente que anima a entrada de seu conteúdo com animações elegantes.
 *
 * @param modifier Modifier para o container
 * @param delayMillis Delay antes de iniciar a animação
 * @param animation Tipo de animação de entrada
 * @param content Conteúdo a ser animado
 */
@Composable
fun AnimateOnEntry(
    modifier: Modifier = Modifier,
    delayMillis: Long = 0,
    animation: EntryAnimation = EntryAnimation.SlideUp,
    content: @Composable () -> Unit
) {
    // Saves state through configuration changes to avoid involuntary reanimation
    var isVisible by rememberSaveable { mutableStateOf(false) }
    val prefersReducedMotion = MotionPreferences.rememberPrefersReducedMotion()
    val effectiveDelay = if (prefersReducedMotion) 0L else delayMillis

    LaunchedEffect(Unit) {
        if (effectiveDelay > 0) delay(effectiveDelay)
        isVisible = true
    }

    val enterTransition = if (prefersReducedMotion) {
        EnterTransition.None
    } else {
        when (animation) {
            EntryAnimation.SlideUp -> slideInVertically(
                initialOffsetY = { Motion.Offset.SlideUp.value.toInt() },
                animationSpec = Motion.Spring.gentle()
            ) + fadeIn(animationSpec = Motion.Tween.enter())

            EntryAnimation.Fade -> fadeIn(animationSpec = Motion.Tween.medium())

            EntryAnimation.Scale -> scaleIn(
                initialScale = Motion.Offset.SCALE,
                animationSpec = Motion.Spring.snappy()
            ) + fadeIn(animationSpec = Motion.Tween.fast())

            EntryAnimation.SlideDown -> slideInVertically(
                initialOffsetY = { -Motion.Offset.SlideDown.value.toInt() },
                animationSpec = Motion.Spring.gentle()
            ) + fadeIn(animationSpec = Motion.Tween.enter())
        }
    }

    val exitTransition = if (prefersReducedMotion) {
        ExitTransition.None
    } else {
        when (animation) {
            EntryAnimation.SlideUp -> slideOutVertically(
                targetOffsetY = { Motion.Offset.SlideUp.value.toInt() },
                animationSpec = Motion.Tween.exit()
            ) + fadeOut(animationSpec = Motion.Tween.fast())

            EntryAnimation.Fade -> fadeOut(animationSpec = Motion.Tween.fast())

            EntryAnimation.Scale -> scaleOut(
                targetScale = Motion.Offset.SCALE,
                animationSpec = Motion.Tween.exit()
            ) + fadeOut(animationSpec = Motion.Tween.fast())

            EntryAnimation.SlideDown -> slideOutVertically(
                targetOffsetY = { -Motion.Offset.SlideDown.value.toInt() },
                animationSpec = Motion.Tween.exit()
            ) + fadeOut(animationSpec = Motion.Tween.fast())
        }
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = enterTransition,
        exit = exitTransition
    ) {
        content()
    }
}
