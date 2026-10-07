package com.example.fitness.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private val MaterialDarkColors = darkColorScheme(
    primary = Color(0xFFA8FF3E),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF0D2405),
    onPrimaryContainer = Color(0xFFA8FF3E),
    secondary = Color(0xFF00F0FF),
    onSecondary = Color(0xFF000000),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF07090C),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF0B0E12),
    onSurfaceVariant = Color(0xFFA2B4C0),
    outline = Color(0xFF151D24),
    error = Color(0xFFFF2D55),
    onError = Color(0xFFFFFFFF)
)

@Composable
fun FitTheme(
    colors: FitColors = FitDarkColors,
    typography: FitTypography = DefaultFitTypography,
    shapes: FitShapes = FitShapes(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalFitColors provides colors,
        LocalFitTypography provides typography,
        LocalFitShapes provides shapes
    ) {
        MaterialTheme(
            colorScheme = MaterialDarkColors,
            content = content
        )
    }
}

object FitTheme {
    val colors: FitColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFitColors.current

    val typography: FitTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalFitTypography.current

    val shapes: FitShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalFitShapes.current

    val spacing: FitSpacing
        get() = FitSpacing

    val sizes: FitSizes
        get() = FitSizes
}
