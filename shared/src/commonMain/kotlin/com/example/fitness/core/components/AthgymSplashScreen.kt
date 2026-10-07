package com.example.fitness.core.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.core.designsystem.FitTheme
import fitness.shared.generated.resources.Res
import fitness.shared.generated.resources.app_logo
import org.jetbrains.compose.resources.painterResource

/**
 * Branded ATHGYM startup/splash screen.
 * Displayed seamlessly while Firebase Authentication and user profile are resolved on cold start,
 * preventing any login flicker or layout jump.
 */
@Composable
fun AthgymSplashScreen(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Official ATHGYM Brand Logo with breathing animation
            Image(
                painter = painterResource(Res.drawable.app_logo),
                contentDescription = "ATHGYM Logo",
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulseScale)
                    .alpha(pulseAlpha)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Subtitle / Tagline
            Text(
                text = "STRONGER EVERYDAY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8E9BA4),
                letterSpacing = 3.5.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Subtle Neon Spinner
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = FitTheme.colors.primary,
                strokeWidth = 2.5.dp,
                trackColor = Color(0xFF141A1F)
            )
        }
    }
}
