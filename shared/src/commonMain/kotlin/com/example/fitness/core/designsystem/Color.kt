package com.example.fitness.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Immutable
data class FitColors(
    val background: Color,
    val surface: Color,
    val surfaceSecondary: Color,
    val surfaceElevated: Color,
    val surfaceVariant: Color,
    val surfaceCard: Color,
    val primary: Color,
    val primaryDark: Color,
    val primaryGlow: Color,
    val accentCyan: Color,
    val accentAmber: Color,
    val accentCoral: Color,
    val accentPurple: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val borderSubtle: Color,
    val borderNeon: Color,
    val borderFocus: Color,
    val divider: Color,
    val error: Color,
    val success: Color,
    val warning: Color,
    val overlayDark: Color,
    val imageGradient: Brush,
    val cardGradient: Brush,
    val heroGradient: Brush,
    val neonGlowGradient: Brush
)

val FitDarkColors = FitColors(
    background = Color(0xFF000000),       // COMPLETE PURE OLED BLACK
    surface = Color(0xFF07090C),          // Deep obsidian black
    surfaceSecondary = Color(0xFF0B0E12), // Deep black card surface
    surfaceElevated = Color(0xFF10151B),  // Elevated black
    surfaceVariant = Color(0xFF141A22),   // Secondary container
    surfaceCard = Color(0xFF080B0E),      // True black card container
    primary = Color(0xFFA8FF3E),          // Vibrant Electric Neon Lime
    primaryDark = Color(0xFF0D2405),      // Deep Neon Olive
    primaryGlow = Color(0xFFC6FF50),      // Neon Lime Glow
    accentCyan = Color(0xFF00F0FF),       // Vibrant Electric Neon Cyan
    accentAmber = Color(0xFFFFD600),      // Vibrant Electric Neon Amber/Yellow
    accentCoral = Color(0xFFFF2D78),      // Vibrant Electric Neon Pink/Rose
    accentPurple = Color(0xFFB026FF),     // Vibrant Electric Neon Purple
    textPrimary = Color(0xFFFFFFFF),      // Pure Crisp White
    textSecondary = Color(0xFFA2B4C0),    // High-Legibility Silver Gray
    textMuted = Color(0xFF5A6B77),        // Subdued Gray
    borderSubtle = Color(0xFF151D24),     // Deep subtle border
    borderNeon = Color(0x40A8FF3E),       // Subtle glowing neon green border
    borderFocus = Color(0xFFA8FF3E),      // High-intensity neon green border
    divider = Color(0xFF10161C),          // Deep black divider
    error = Color(0xFFFF2D55),            // Neon Red
    success = Color(0xFFA8FF3E),          // Neon Green
    warning = Color(0xFFFFD600),          // Neon Amber
    overlayDark = Color(0xF0000000),      // Deep OLED Black Overlay
    imageGradient = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color(0x99000000),
            Color(0xFF000000)
        )
    ),
    cardGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0E1318),
            Color(0xFF06080A)
        )
    ),
    heroGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0x2EA8FF3E),
            Color(0xFF000000)
        )
    ),
    neonGlowGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0x00A8FF3E),
            Color(0x80A8FF3E),
            Color(0x00A8FF3E)
        )
    )
)

val LocalFitColors = staticCompositionLocalOf { FitDarkColors }
