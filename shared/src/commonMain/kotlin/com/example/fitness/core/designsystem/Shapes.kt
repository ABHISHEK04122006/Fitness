package com.example.fitness.core.designsystem

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape

@Immutable
data class FitShapes(
    val card: Shape = RoundedCornerShape(FitCornerRadius.card),
    val cardLarge: Shape = RoundedCornerShape(FitCornerRadius.cardLarge),
    val cardSmall: Shape = RoundedCornerShape(FitCornerRadius.md),
    val button: Shape = RoundedCornerShape(FitCornerRadius.button),
    val buttonPill: Shape = RoundedCornerShape(FitCornerRadius.buttonPill),
    val chip: Shape = RoundedCornerShape(FitCornerRadius.buttonPill),
    val bottomSheet: Shape = RoundedCornerShape(topStart = FitCornerRadius.cardLarge, topEnd = FitCornerRadius.cardLarge),
    val circle: Shape = CircleShape
)

val LocalFitShapes = staticCompositionLocalOf { FitShapes() }
