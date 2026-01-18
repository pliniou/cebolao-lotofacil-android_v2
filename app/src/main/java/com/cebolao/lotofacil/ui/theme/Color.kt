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
val BrandLaranja = Color(0xFFF8971D)

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

val SuccessColor = SuccessBase
val WarningColor = WarningBase
val ErrorColor = ErrorBase

// --- GLASSMORPHISM TOKENS ---
val GlassSurfaceLight = Color.White.copy(alpha = 0.72f)
val GlassSurfaceDark = Slate900.copy(alpha = 0.65f)

object Alpha {
    const val DIVIDER = 0.75f
    const val DIVIDER_SUBTLE = 0.5f
    const val DISABLED = 0.38f
    const val OVERLAY = 0.12f
    const val SCRIM = 0.32f
}
