package com.cebolao.lotofacil.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// --- NEUTRAL PALETTE (Deep Slate System) ---
val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)
val Slate200 = Color(0xFFE2E8F0)
val Slate300 = Color(0xFFCBD5E1)
val Slate600 = Color(0xFF475569)
val Slate700 = Color(0xFF334155)
val Slate800 = Color(0xFF1E293B)
val Slate900 = Color(0xFF0F172A)
val Slate950 = Color(0xFF020617)

// --- PREMIUM NEUTRALS (Rich darks) ---
val Obsidian = Color(0xFF050510)
val Charcoal = Color(0xFF10121B)
val Gunmetal = Color(0xFF1A1D2D)

// --- BRAND ACCENTS (Vibrant & Neon-ish) ---
val BrandAzul = Color(0xFF2962FF)
val BrandRoxo = Color(0xFF6200EA)
val BrandVerde = Color(0xFF00C853)
val BrandAmarelo = Color(0xFFFFD600)
val BrandRosa = Color(0xFFD500F9)
val BrandLaranja = Color(0xFFFF6D00)

// --- GRADIENT BRUSHES ---
val GradientAzul = Brush.linearGradient(listOf(Color(0xFF2962FF), Color(0xFF0091EA)))

val GradientPremiumDark = Brush.verticalGradient(
    listOf(Obsidian, Slate950)
)
val GradientPremiumLight = Brush.verticalGradient(
    listOf(Slate50, Slate100)
)

// --- SEMANTIC ROLES: LIGHT MODE ---
val LightBackground = Slate50
val LightSurface1 = Color.White
val LightSurface2 = Slate50
val LightSurface3 = Slate100
val LightOutline = Slate200
val LightOutlineVariant = Slate100
val LightTextPrimary = Slate900
val LightTextSecondary = Slate600

// --- SEMANTIC ROLES: DARK MODE ---
val DarkBackground = Obsidian
val DarkSurface1 = Charcoal
val DarkSurface2 = Gunmetal
val DarkSurface3 = Slate800
val DarkOutline = Slate800
val DarkOutlineVariant = Slate700.copy(alpha = 0.3f)
val DarkTextPrimary = Color.White
val DarkTextSecondary = Slate300

// --- STATUS COLORS ---
val SuccessBase = Color(0xFF00E676)
val WarningBase = Color(0xFFFFAB00)
val ErrorBase = Color(0xFFFF1744)

// Legacy aliases (maintained for compatibility)
val SuccessColor = SuccessBase
val ErrorColor = ErrorBase

val ErrorLight = Color(0xFFFF8A80)
val ErrorDark = Color(0xFFD50000)

// --- ACCENT COLOR VARIANTS ---
val BrandAzulLight = Color(0xFF82B1FF)

val BrandRoxoLight = Color(0xFFB388FF)

val BrandVerdeLight = Color(0xFFB9F6CA)

val BrandAmareloLight = Color(0xFFFFE57F)

val BrandRosaLight = Color(0xFFFF80AB)

val BrandLaranjaLight = Color(0xFFFFCC80)

// --- GLASSMORPHISM TOKENS ---
val GlassSurfaceLight = Color.White.copy(alpha = 0.65f)
val GlassSurfaceDark = Color(0xFF1E202E).copy(alpha = 0.60f)

// --- OVERLAY & INTERACTION STATES ---
object Alpha {
    const val DIVIDER = 0.75f
    const val DIVIDER_SUBTLE = 0.5f
    const val DISABLED = 0.38f
    const val SCRIM = 0.40f
}
