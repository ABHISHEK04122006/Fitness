package com.example.fitness.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fitness.core.designsystem.FitTheme

@Composable
fun FitnessCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = FitTheme.shapes.card,
    backgroundColor: Color = FitTheme.colors.surface,
    borderColor: Color = FitTheme.colors.borderSubtle,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed && onClick != null) 0.985f else 1.0f)

    Surface(
        modifier = modifier
            .scale(scale)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        content()
    }
}

@Composable
fun ImagePlaceholderBox(
    modifier: Modifier = Modifier,
    height: Dp = FitTheme.sizes.cardImageHeight,
    shape: Shape = FitTheme.shapes.card,
    icon: ImageVector = FitnessIcons.Workout,
    gradientColors: List<Color> = listOf(
        Color(0xFF141D22),
        Color(0xFF0A0E11),
        Color(0xFF000000)
    ),
    content: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(gradientColors)
            )
    ) {
        // Subtle decorative athletic watermark
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0x15FFFFFF),
            modifier = Modifier
                .size(height * 0.85f)
                .align(Alignment.CenterEnd)
                .padding(end = FitTheme.spacing.lg)
        )
        // Dark gradient overlay towards bottom for text legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FitTheme.colors.imageGradient)
        )

        content()
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    highlight: Boolean = false,
    icon: ImageVector? = null
) {
    FitnessCard(
        modifier = modifier,
        backgroundColor = if (highlight) Color(0xFF162312) else FitTheme.colors.surfaceSecondary,
        borderColor = if (highlight) FitTheme.colors.primary.copy(alpha = 0.4f) else FitTheme.colors.borderSubtle
    ) {
        Column(
            modifier = Modifier.padding(FitTheme.spacing.lg),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (highlight) FitTheme.colors.primary else FitTheme.colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(FitTheme.spacing.xs))
                }
                Text(
                    text = value,
                    style = FitTheme.typography.metricLarge,
                    color = if (highlight) FitTheme.colors.primary else FitTheme.colors.textPrimary
                )
                if (unit != null) {
                    Spacer(modifier = Modifier.width(FitTheme.spacing.xxs))
                    Text(
                        text = unit,
                        style = FitTheme.typography.titleSmall,
                        color = FitTheme.colors.textSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(FitTheme.spacing.xs))
            Text(
                text = label,
                style = FitTheme.typography.bodySmall,
                color = FitTheme.colors.textSecondary
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FitnessCard(
        modifier = modifier,
        onClick = onClick,
        shape = FitTheme.shapes.cardSmall,
        backgroundColor = FitTheme.colors.surfaceSecondary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = FitTheme.spacing.lg, horizontal = FitTheme.spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222B30)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = FitTheme.colors.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(FitTheme.spacing.sm))
            Text(
                text = title,
                style = FitTheme.typography.labelMedium,
                color = FitTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
