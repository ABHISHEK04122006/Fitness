package com.example.fitness.feature.welcome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.components.PrimaryButton
import com.example.fitness.core.components.SecondaryButton
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.navigation.LocalNavigator
import com.example.fitness.core.navigation.Screen
import fitness.shared.generated.resources.Res
import fitness.shared.generated.resources.welcome_hero
import org.jetbrains.compose.resources.painterResource

@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        // Hero Background Athlete Image
        Image(
            painter = painterResource(Res.drawable.welcome_hero),
            contentDescription = "Fitness Athlete",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Multi-stop Vignette Gradient for High Legibility & Depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0x22000000),
                        0.30f to Color(0x55000000),
                        0.50f to Color(0xB3000000),
                        0.70f to Color(0xF2000000),
                        0.90f to Color(0xFF000000),
                        1.0f to Color(0xFF000000)
                    )
                )
        )

        // Screen Foreground Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                val isCompact = maxWidth < 360.dp
                val headlineSize = if (isCompact) 36.sp else 44.sp
                val headlineLineHeight = if (isCompact) 40.sp else 48.sp
                val horizontalPad = if (isCompact) 16.dp else 22.dp

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = horizontalPad, vertical = 18.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.Start
                ) {
                    // Discipline Tags
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                                slideInVertically(initialOffsetY = { 30 })
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            val disciplines = listOf("Strength", "HIIT", "Mobility", "Endurance")
                            disciplines.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF07090C).copy(alpha = 0.9f),
                                    border = BorderStroke(1.dp, Color(0x40A8FF3E))
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFA6B8C4),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                                slideInVertically(
                                    initialOffsetY = { 50 },
                                    animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)
                                )
                    ) {
                        Column {
                            Text(
                                text = "Stronger",
                                fontSize = headlineSize,
                                lineHeight = headlineLineHeight,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Every Day",
                                fontSize = headlineSize,
                                lineHeight = headlineLineHeight,
                                fontWeight = FontWeight.ExtraBold,
                                color = FitTheme.colors.primary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Workouts for everyone.\nTraining for what's next.",
                                fontSize = 14.5.sp,
                                lineHeight = 21.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFFA2B4C0)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Social Proof Bar
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                                slideInVertically(initialOffsetY = { 60 })
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF07090C).copy(alpha = 0.95f))
                                .border(1.dp, Color(0x33A8FF3E), RoundedCornerShape(14.dp))
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "🔥", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "250k+ Athletes",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Box(modifier = Modifier.size(1.dp, 16.dp).background(Color(0xFF263339)))

                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "⭐", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "4.9/5 Rating",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Box(modifier = Modifier.size(1.dp, 16.dp).background(Color(0xFF263339)))

                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "💪", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "1,200+ Workouts",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                        slideInVertically(
                            initialOffsetY = { 70 },
                            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)
                        )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PrimaryButton(
                        text = "Get Started",
                        onClick = { navigator.navigate(Screen.Auth(isSignUp = true)) },
                        height = 56.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SecondaryButton(
                        text = "Sign In",
                        onClick = { navigator.navigate(Screen.Auth(isSignUp = false)) },
                        height = 56.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Build Habits",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6E7E8A)
                        )
                        Text(
                            text = "  |  ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF3B464E)
                        )
                        Text(
                            text = "Track Progress",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6E7E8A)
                        )
                        Text(
                            text = "  |  ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF3B464E)
                        )
                        Text(
                            text = "Be Your Best",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6E7E8A)
                        )
                    }
                }
            }
        }
    }
}
}
}
