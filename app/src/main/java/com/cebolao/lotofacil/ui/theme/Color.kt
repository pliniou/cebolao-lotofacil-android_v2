package com.cebolao.lotofacil.ui.theme

import androidx.compose.ui.graphics.Color

// --- NEUTRAL PALETTE (Slate System) ---
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

// --- BRAND ACCENTS ---
val BrandAzul = Color(0xFF0055FF)
val BrandRoxo = Color(0xFF651FFF)
val BrandVerde = Color(0xFF10B981)
val BrandAmarelo = Color(0xFFFBBF24)
val BrandRosa = Color(0xFFEC4899)
val BrandLaranja = Color(0xFFF97316)  // Coral - distinct from yellow

// --- SEMANTIC ROLES: LIGHT MODE ---
val LightBackground = Slate50
val LightSurface1 = Color.White
val LightSurface2 = Slate50
val LightSurface3 = Slate100
val LightOutline = Slate200
val LightOutlineVariant = Slate100
val LightTextPrimary = Slate900
val LightTextSecondary = Slate600
val LightTextTertiary = Slate400

// --- SEMANTIC ROLES: DARK MODE ---
val DarkBackground = Slate950
val DarkSurface1 = Slate900
val DarkSurface2 = Slate800
val DarkSurface3 = Slate700
val DarkOutline = Slate800
val DarkOutlineVariant = Slate700.copy(alpha = 0.5f)
val DarkTextPrimary = Color.White
val DarkTextSecondary = Slate400
val DarkTextTertiary = Slate500

// --- STATUS COLORS ---
val SuccessBase = Color(0xFF22C55E)
val WarningBase = Color(0xFFF59E0B)
val ErrorBase = Color(0xFFEF4444)
val InfoBase = Color(0xFF3B82F6)

// Legacy aliases - mantidos para compatibilidade
val SuccessColor = SuccessBase
val WarningColor = WarningBase
val ErrorColor = ErrorBase

// --- STATUS COLOR VARIANTS ---
// Success variants
val SuccessLight = Color(0xFFDCFCE7)
val SuccessDark = Color(0xFF16A34A)

// Warning variants
val WarningLight = Color(0xFFFEF3C7)
val WarningDark = Color(0xFFD97706)

// Error variants
val ErrorLight = Color(0xFFFEE2E2)
val ErrorDark = Color(0xFFDC2626)

// Info variants
val InfoLight = Color(0xFFDBEAFE)
val InfoDark = Color(0xFF1E40AF)

// --- ACCENT COLOR VARIANTS ---
// Azul variants
val BrandAzulLight = Color(0xFF3377FF)
val BrandAzulDark = Color(0xFF0044CC)

// Roxo variants
val BrandRoxoLight = Color(0xFF7C3AED)
val BrandRoxoDark = Color(0xFF5B21B6)

// Verde variants
val BrandVerdeLight = Color(0xFF34D399)
val BrandVerdeDark = Color(0xFF059669)

// Amarelo variants
val BrandAmareloLight = Color(0xFFFCD34D)
val BrandAmareloDark = Color(0xFFF59E0B)

// Rosa variants
val BrandRosaLight = Color(0xFFF472B6)
val BrandRosaDark = Color(0xFFDB2777)

// Laranja variants
val BrandLaranjaLight = Color(0xFFFDBA74)  // Orange-300
val BrandLaranjaDark = Color(0xFFEA580C)

// --- GLASSMORPHISM TOKENS ---
val GlassSurfaceLight = Color.White.copy(alpha = 0.72f)
val GlassSurfaceDark = Slate900.copy(alpha = 0.65f)

// --- OVERLAY & INTERACTION STATES ---
object Alpha {
    const val DIVIDER = 0.75f
    const val DIVIDER_SUBTLE = 0.5f
    const val DISABLED = 0.38f
    const val OVERLAY = 0.12f
    const val SCRIM = 0.32f
    const val HOVER = 0.08f
    const val PRESSED = 0.12f
    const val FOCUS = 0.12f
    const val DRAG = 0.16f
}
