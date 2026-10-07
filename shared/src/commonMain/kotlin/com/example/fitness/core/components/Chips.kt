package com.example.fitness.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.core.designsystem.FitCornerRadius
import com.example.fitness.core.designsystem.FitTheme

/**
 * Global standardized chip component for displaying workout muscles, equipment, badges, and tags.
 * Sizes naturally to fit its content without mid-word breaking, character splitting, clipping, or rigid width constraints.
 */
@Composable
fun FitChip(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    backgroundColor: Color = Color(0xFF141A1D),
    borderColor: Color = Color(0xFF1E262B),
    textColor: Color = Color(0xFFCBD5E1),
    fontSize: TextUnit = 12.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    shape: Shape = RoundedCornerShape(12.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 7.dp)
) {
    Surface(
        modifier = modifier.wrapContentSize(),
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = fontWeight,
                color = textColor,
                softWrap = false
            )
        }
    }
}

@Composable
fun CategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) FitTheme.colors.primary else FitTheme.colors.surfaceSecondary
    )
    val contentColor = if (isSelected) Color(0xFF000000) else FitTheme.colors.textSecondary
    val borderColor = if (isSelected) FitTheme.colors.primary else FitTheme.colors.borderSubtle

    Surface(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        shape = FitTheme.shapes.chip,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = FitTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(FitTheme.spacing.xs))
            }
            Text(
                text = text,
                style = FitTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}

@Composable
fun TagChip(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    backgroundColor: Color = FitTheme.colors.surfaceVariant,
    textColor: Color = FitTheme.colors.textSecondary
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(FitCornerRadius.sm),
        color = backgroundColor,
        border = BorderStroke(1.dp, FitTheme.colors.borderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = FitTheme.typography.labelSmall,
                color = textColor
            )
        }
    }
}

@Composable
fun DateSelectorItem(
    dayOfWeek: String,
    dayOfMonth: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) FitTheme.colors.primary else FitTheme.colors.surfaceSecondary
    )
    val textColor = if (isSelected) Color(0xFF000000) else FitTheme.colors.textSecondary
    val numberColor = if (isSelected) Color(0xFF000000) else FitTheme.colors.textPrimary
    val borderColor = if (isSelected) FitTheme.colors.primary else FitTheme.colors.borderSubtle

    Surface(
        onClick = onClick,
        modifier = modifier
            .width(52.dp)
            .height(72.dp),
        shape = RoundedCornerShape(FitCornerRadius.md),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(vertical = FitTheme.spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayOfWeek,
                style = FitTheme.typography.labelSmall,
                color = textColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dayOfMonth,
                style = FitTheme.typography.titleLarge,
                color = numberColor
            )
        }
    }
}
