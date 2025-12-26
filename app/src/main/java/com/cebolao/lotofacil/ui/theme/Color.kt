package com.cebolao.lotofacil.ui.theme

import androidx.compose.ui.graphics.Color

val FlatWhite = Color(0xFFFFFFFF)

// --- NEUTRAL GRAYS (Slate System) ---
val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)
val Slate200 = Color(0xFFE2E8F0)
val Slate300 = Color(0xFFCBD5E1)
val Slate400 = Color(0xFF94A3B8)
val Slate500 = Color(0xFF64748B)
val Slate600 = Color(0xFF475569)
val Slate700 = Color(0xFF334155)
val Slate800 = Color(0xFF1E293B)
val Slate900 = Color(0xFF0F172A)
val Slate950 = Color(0xFF020617)

// --- BRAND COLORS ---
val VividBlue = Color(0xFF0066FF)
val VividPurple = Color(0xFF7C3AED)
val VividPink = Color(0xFFEC4899)
val VividAmber = Color(0xFFFFAB00)
val VividOrange = Color(0xFFFF6B35)
val VividGreen = Color(0xFF10B981)

// Corporate Identity
val BrandAzulCaixa = Color(0xFF005CA9) // Azul Institucional
val BrandLaranjaCaixa = Color(0xFFF8971D) // Laranja Institucional

val BrandAzul = BrandAzulCaixa
val BrandRoxo = VividPurple
val BrandVerde = VividGreen
val BrandAmarelo = VividAmber
val BrandRosa = VividPink
val BrandLaranja = BrandLaranjaCaixa

// --- SEMANTIC COLORS ---
val SemanticSuccess = Color(0xFF00C853)
val SemanticError = Color(0xFFFF3B30)
val ErrorColor = SemanticError
val SuccessColor = SemanticSuccess

// --- BACKGROUND LAYERS (Theme Independent Tokens) ---
val DarkBackground = Slate950
val DarkSurface = Slate900
val DarkSurfaceElevated = Slate800
val DarkSurfaceHighlight = Slate700
val LightBackground = Slate50
val LightSurface = FlatWhite
val LightSurfaceElevated = Slate100
val LightSurfaceHighlight = Slate200

// --- TEXT COLORS ---
val TextPrimaryDark = FlatWhite
val TextSecondaryDark = Slate400
val TextTertiaryDark = Slate500
val TextPrimaryLight = Slate900
val TextSecondaryLight = Slate600
val TextTertiaryLight = Slate500

// Legacy Aliases (to be removed iteratively or kept for compat)
val TextPrimary = TextPrimaryDark
val TextSecondary = TextSecondaryDark
val TextTertiary = TextTertiaryDark

object Alpha {
    const val MEDIUM = 0.60f
    const val DISABLED = 0.38f
    const val DIVIDER = 0.12f
    const val SCRIM = 0.32f
    const val GLASS = 0.75f
}

val GlassSurfaceDark = Slate900.copy(alpha = Alpha.GLASS)
