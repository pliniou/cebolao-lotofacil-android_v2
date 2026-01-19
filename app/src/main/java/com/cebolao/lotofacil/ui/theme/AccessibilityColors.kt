package com.cebolao.lotofacil.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.pow

/**
 * Utilitários de acessibilidade para garantir conformidade WCAG.
 * Contém funções para verificar e ajustar contraste de cores.
 */
object AccessibilityColors {
    
    /**
     * Contraste mínimo WCAG AA para texto normal (4.5:1)
     */
    const val WCAG_AA_NORMAL = 4.5f
    
    /**
     * Contraste mínimo WCAG AA para texto grande (3:1)
     */
    const val WCAG_AA_LARGE = 3.0f
    
    /**
     * Contraste mínimo WCAG AAA para texto normal (7:1)
     */
    const val WCAG_AAA_NORMAL = 7.0f
    
    /**
     * Calcula a taxa de contraste entre duas cores.
     * 
     * @param foreground Cor do primeiro plano (texto)
     * @param background Cor do fundo
     * @return Taxa de contraste (1.0 a 21.0)
     */
    fun contrastRatio(foreground: Color, background: Color): Float {
        val lumFg = foreground.luminance()
        val lumBg = background.luminance()
        
        val lighter = maxOf(lumFg, lumBg)
        val darker = minOf(lumFg, lumBg)
        
        return (lighter + 0.05f) / (darker + 0.05f)
    }
    
    /**
     * Verifica se o contraste entre duas cores atende um padrão WCAG.
     * 
     * @param foreground Cor do primeiro plano
     * @param background Cor do fundo
     * @param minRatio Taxa de contraste mínima desejada
     * @return true se o contraste for suficiente
     */
    fun meetsContrastRequirement(
        foreground: Color,
        background: Color,
        minRatio: Float = WCAG_AA_NORMAL
    ): Boolean {
        return contrastRatio(foreground, background) >= minRatio
    }
    
    /**
     * Ajusta a luminosidade de uma cor para garantir contraste mínimo.
     * 
     * @param color Cor a ser ajustada
     * @param background Cor de fundo de referência
     * @param minRatio Taxa de contraste mínima desejada
     * @param preferDarker Se true, tenta escurecer a cor primeiro; se false, tenta clarear
     * @return Cor ajustada com contraste adequado
     */
    fun ensureContrast(
        color: Color,
        background: Color,
        minRatio: Float = WCAG_AA_NORMAL,
        preferDarker: Boolean = background.luminance() > 0.5f
    ): Color {
        // Se já tem contraste suficiente, retorna a cor original
        if (meetsContrastRequirement(color, background, minRatio)) {
            return color
        }
        
        val bgLum = background.luminance()
        
        // Calcula a luminância alvo
        val targetLum = if (preferDarker) {
            // Tentar escurecer
            ((bgLum + 0.05f) / minRatio) - 0.05f
        } else {
            // Tentar clarear
            ((bgLum + 0.05f) * minRatio) - 0.05f
        }.coerceIn(0f, 1f)
        
        return adjustLuminance(color, targetLum)
    }
    
    /**
     * Ajusta a luminância de uma cor para um valor específico.
     * Mantém matiz e saturação aproximados.
     * 
     * @param color Cor original
     * @param targetLuminance Luminância alvo (0.0 a 1.0)
     * @return Cor com luminância ajustada
     */
    private fun adjustLuminance(color: Color, targetLuminance: Float): Color {
        val currentLum = color.luminance()
        
        if (currentLum == 0f) {
            // Cor preta - retorna branco ou cinza baseado no target
            val gray = targetLuminance.coerceIn(0f, 1f)
            return Color(gray, gray, gray, color.alpha)
        }
        
        // Ajusta os componentes RGB proporcionalmente
        val factor = (targetLuminance / currentLum).coerceIn(0f, 3f)
        
        return Color(
            red = (color.red * factor).coerceIn(0f, 1f),
            green = (color.green * factor).coerceIn(0f, 1f),
            blue = (color.blue * factor).coerceIn(0f, 1f),
            alpha = color.alpha
        )
    }
    
    /**
     * Retorna a melhor cor de conteúdo (preto ou branco) para um fundo específico.
     * 
     * @param background Cor de fundo
     * @return Color.Black ou Color.White, dependendo do que tem melhor contraste
     */
    fun bestContentColor(background: Color): Color {
        val contrastWithBlack = contrastRatio(Color.Black, background)
        val contrastWithWhite = contrastRatio(Color.White, background)
        
        return if (contrastWithBlack > contrastWithWhite) Color.Black else Color.White
    }
    
    /**
     * Verifica se uma cor é considerada "clara" (luminância > 0.5).
     */
    fun Color.isLight(): Boolean = this.luminance() > 0.5f
    
    /**
     * Verifica se uma cor é considerada "escura" (luminância <= 0.5).
     */
    fun Color.isDark(): Boolean = this.luminance() <= 0.5f
    
    /**
     * Retorna uma versão mais escura da cor.
     * 
     * @param factor Fator de escurecimento (0.0 a 1.0), onde 0.8 significa 20% mais escuro
     */
    fun Color.darken(factor: Float = 0.8f): Color {
        val clampedFactor = factor.coerceIn(0f, 1f)
        return Color(
            red = red * clampedFactor,
            green = green * clampedFactor,
            blue = blue * clampedFactor,
            alpha = alpha
        )
    }
    
    /**
     * Retorna uma versão mais clara da cor.
     * 
     * @param factor Fator de clareamento (1.0 a infinito), onde 1.2 significa 20% mais claro
     */
    fun Color.lighten(factor: Float = 1.2f): Color {
        val clampedFactor = maxOf(1f, factor)
        return Color(
            red = (red * clampedFactor).coerceIn(0f, 1f),
            green = (green * clampedFactor).coerceIn(0f, 1f),
            blue = (blue * clampedFactor).coerceIn(0f, 1f),
            alpha = alpha
        )
    }
}

/**
 * Extension function para garantir que esta cor tenha contraste suficiente com o fundo.
 */
fun Color.withMinimumContrast(
    background: Color,
    minRatio: Float = AccessibilityColors.WCAG_AA_NORMAL
): Color = AccessibilityColors.ensureContrast(this, background, minRatio)

/**
 * Extension function para obter a melhor cor de conteúdo para este fundo.
 */
fun Color.bestContentColor(): Color = AccessibilityColors.bestContentColor(this)
