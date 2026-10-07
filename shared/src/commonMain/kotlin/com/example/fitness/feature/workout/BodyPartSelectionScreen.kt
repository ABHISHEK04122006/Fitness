package com.example.fitness.feature.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.core.components.CircularIconButton
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.navigation.LocalNavigator
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.workout.data.WorkoutRepository
import com.example.fitness.feature.workout.model.BodyPartWorkout
import com.example.fitness.feature.workout.model.WorkoutType
import org.jetbrains.compose.resources.painterResource

@Composable
fun BodyPartSelectionScreen(workoutTypeString: String) {
    val navigator = LocalNavigator.current
    val workoutType = WorkoutType.fromString(workoutTypeString)
    val userGender = UserSession.currentUserGender
    val userLevel = UserSession.experienceLevel
    val workouts = remember(workoutType, userGender, userLevel, com.example.fitness.feature.exercise.data.SampleExerciseData.canonicalRevision) {
        WorkoutRepository.getWorkouts(workoutType, userGender, userLevel)
    }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
        try {
            com.example.fitness.feature.exercise.data.FirebaseExerciseRepository().getActiveExercises()
        } catch (_: Exception) {}
    }

    val screenTitle = if (workoutType == WorkoutType.GYM) "Gym Workouts" else "Home Workouts"
    val badgeText = if (workoutType == WorkoutType.GYM) "GYM TRAINING" else "HOME FITNESS"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .padding(horizontal = 20.dp)
        ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            // Top Header: Back Button & Category Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularIconButton(
                    icon = FitnessIcons.ArrowBack,
                    onClick = { navigator.pop() },
                    contentDescription = "Back"
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF07090C),
                    border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(FitTheme.colors.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = badgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = FitTheme.colors.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Screen Title & Subtitle
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                        slideInVertically(initialOffsetY = { 20 })
            ) {
                Column {
                    Text(
                        text = screenTitle,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Choose the body part you want to train",
                        fontSize = 14.sp,
                        color = Color(0xFF9BAAB4),
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
        }

        // 8 Body-Part Cards
        itemsIndexed(workouts) { index, item ->
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                        slideInVertically(
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            initialOffsetY = { (index + 1) * 20 }
                        )
            ) {
                BodyPartCard(
                    workout = item,
                    onClick = {
                        navigator.navigate(
                            Screen.BodyPartWorkoutDetail(
                                workoutType = workoutTypeString,
                                bodyPartId = item.id
                            )
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    }
}

@Composable
private fun BodyPartCard(
    workout: BodyPartWorkout,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f)
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF07090C),
        border = BorderStroke(1.dp, Color(0xFF1B2329)),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 146.dp)
            .scale(scale)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Body Part Image
            Image(
                painter = painterResource(workout.imageRes),
                contentDescription = workout.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
            )

            // Cinematic Horizontal Gradient Overlay (dark left for high-contrast text, clear right for image visibility)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0.0f to Color(0xFF000000).copy(alpha = 0.94f),
                                0.50f to Color(0xFF000000).copy(alpha = 0.82f),
                                0.75f to Color(0xFF000000).copy(alpha = 0.45f),
                                1.0f to Color(0xFF000000).copy(alpha = 0.20f)
                            )
                        )
                    )
            )

            // Subtle vertical vignette
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.70f to Color.Transparent,
                                1.0f to Color(0xFF000000).copy(alpha = 0.55f)
                            )
                        )
                    )
            )

            // Content inside Card
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Title
                        Text(
                            text = workout.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Subtitle
                        Text(
                            text = workout.subtitle,
                            fontSize = 12.sp,
                            color = Color(0xFF9BAAB4),
                            maxLines = 2,
                            lineHeight = 16.sp
                        )
                    }

                    // Metadata Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF141C22),
                            border = BorderStroke(0.8.dp, Color(0xFF22303A))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = FitnessIcons.Clock,
                                    contentDescription = null,
                                    tint = FitTheme.colors.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = workout.duration,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFD1D8DC)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF141C22),
                            border = BorderStroke(0.8.dp, Color(0xFF22303A))
                        ) {
                            Text(
                                text = "${workout.exercises.size} Exercises",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = FitTheme.colors.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Chevron Action Button
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF161F25),
                    border = BorderStroke(1.dp, Color(0xFF25333D)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FitnessIcons.ChevronRight,
                            contentDescription = "View",
                            tint = FitTheme.colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
