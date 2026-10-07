package com.example.fitness.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.components.UserProfileAvatar
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.navigation.LocalNavigator
import com.example.fitness.core.navigation.Screen
import fitness.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

import com.example.fitness.core.session.UserSession
import com.example.fitness.core.user.data.FirebaseUserRepository
import com.example.fitness.feature.notifications.data.FirebaseNotificationRepository
import com.example.fitness.getCurrentDayOfWeekIndex
import com.example.fitness.getCurrentWeekDayItems
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import com.example.fitness.feature.workout.data.WeeklyWorkoutRepository
import com.example.fitness.feature.workout.data.WorkoutMediaResolver
import com.example.fitness.feature.workout.model.ExperienceLevel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import com.example.fitness.feature.progress.data.WorkoutProgressService

internal fun extractFirstName(fullName: String?): String {
    return fullName
        ?.trim()
        ?.split(Regex("\\s+"))
        ?.firstOrNull()
        ?.takeIf { it.isNotBlank() }
        ?: "Athlete"
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current
    val userRepository = remember { FirebaseUserRepository() }
    val notificationRepository = remember { FirebaseNotificationRepository() }
    val currentDayIndex = remember { getCurrentDayOfWeekIndex() }
    var selectedDayIndex by remember { mutableStateOf(currentDayIndex) }
    val weekDays = remember { getCurrentWeekDayItems() }
    val currentGender = UserSession.currentUserGender ?: UserSession.currentUserProfile?.gender
    val scheduledProgram = UserSession.scheduledProgramId
    val selectedWorkout = remember(selectedDayIndex, scheduledProgram, UserSession.experienceLevel, currentGender, UserSession.weeklyCommitment) {
        CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = scheduledProgram,
            dayIndex = selectedDayIndex,
            level = UserSession.experienceLevel,
            workoutDays = UserSession.weeklyCommitment
        )
    }
    val todayWorkoutImage = remember(selectedWorkout, currentGender, selectedDayIndex) {
        WorkoutMediaResolver.resolveTodayWorkoutImage(
            workout = selectedWorkout,
            gender = currentGender,
            fallbackCycleDay = selectedDayIndex + 1
        )
    }
    val athleteWorkoutImage = remember(currentGender) {
        WorkoutMediaResolver.resolveAthleteWorkoutImage(currentGender)
    }
    val gymWorkoutImage = remember(currentGender) {
        WorkoutMediaResolver.resolveGymWorkoutImage(currentGender)
    }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        com.example.fitness.core.auth.LoginPerfTracker.record("Home/Admin usable")
        com.example.fitness.core.auth.LoginPerfTracker.logSummary()
        com.example.fitness.core.perf.AppPerfTracker.finish("SPLASH_TO_HOME", "Home screen interactive")
        com.example.fitness.core.perf.AppPerfTracker.finish("APP_STARTUP", "First usable screen rendered")
        com.example.fitness.core.perf.AppPerfTracker.finish("ADMIN_TO_USER_APP", "User App Home rendered")
        val currentUser = Firebase.auth.currentUser
        val effectiveUid = currentUser?.uid ?: UserSession.currentUserId
        if (!effectiveUid.isNullOrBlank()) {
            if (currentUser != null && (UserSession.currentUserId != currentUser.uid || UserSession.currentUserProfile == null)) {
                UserSession.setAuthenticatedUser(currentUser)
                UserSession.updateRoleFromUser(currentUser)
                val profile = userRepository.getUserProfile(currentUser.uid)
                if (profile != null) {
                    UserSession.setUserProfile(profile)
                }
            }
            if (WorkoutProgressService.lastLoadedUserId != effectiveUid && !WorkoutProgressService.isLoadingHistory) {
                try {
                    WorkoutProgressService.loadWorkoutHistory(effectiveUid)
                } catch (_: Exception) {
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 840.dp)
                .padding(horizontal = 20.dp)
                .padding(bottom = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Top Header: Greeting, Streak Badge, Bell, Profile Avatar
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                        slideInVertically(initialOffsetY = { -20 })
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        val rawFullName = UserSession.currentUserName?.takeIf { it.isNotBlank() }
                            ?: UserSession.currentUserProfile?.name?.takeIf { it.isNotBlank() }
                            ?: Firebase.auth.currentUser?.displayName?.takeIf { it.isNotBlank() }

                        val firstName = extractFirstName(rawFullName)

                        Text(
                            text = "👋 Hi, $firstName",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Let's make progress today.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF8E9BA4)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Streak Badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0B2105),
                            border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "${UserSession.workoutStreak}d 🔥",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FitTheme.colors.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        // Notification Bell with unread dot
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF14191C))
                                .border(1.dp, Color(0xFF222B2F), CircleShape)
                                .clickable { navigator.navigate(Screen.Notifications) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FitnessIcons.Bell,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            if (UserSession.unreadNotificationCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 9.dp, end = 9.dp)
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(FitTheme.colors.primary)
                                )
                            }
                        }

                        // Dynamic User Profile Avatar
                        UserProfileAvatar(
                            imageUrl = UserSession.currentUserProfileImageUrl ?: UserSession.currentUserProfile?.profileImageUrl,
                            name = UserSession.currentUserName ?: UserSession.currentUserProfile?.name,
                            email = UserSession.currentUserEmail ?: UserSession.currentUserProfile?.email,
                            size = 42.dp,
                            borderWidth = 1.5.dp,
                            onClick = { navigator.navigate(Screen.Profile) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Weekly Days Selector (Mon-Sun horizontal selector)
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(spring(stiffness = Spring.StiffnessLow))
            ) {
                val weeklyActivityStatus = WorkoutProgressService.getWeeklyDayActivityStatus()
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val isCompact = maxWidth < 360.dp
                    val itemSpacing = if (isCompact) 4.dp else 6.dp
                    val dayFontSize = if (isCompact) 10.sp else 11.sp
                    val dateFontSize = if (isCompact) 15.sp else 16.sp

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(itemSpacing)
                    ) {
                        weekDays.forEachIndexed { index, item ->
                            val isSelected = index == selectedDayIndex
                            val interactionSource = remember { MutableInteractionSource() }
                            val isPressed by interactionSource.collectIsPressedAsState()
                            val scale by animateFloatAsState(if (isPressed) 0.92f else 1.0f)

                            val bgColor by animateColorAsState(
                                if (isSelected) FitTheme.colors.primary else Color(0xFF07090C)
                            )
                            val textColor by animateColorAsState(
                                if (isSelected) Color(0xFF000000) else Color(0xFF8E9BA4)
                            )
                            val numberColor by animateColorAsState(
                                if (isSelected) Color(0xFF000000) else Color.White
                            )

                            Surface(
                                modifier = Modifier
                                    .scale(scale)
                                    .weight(1f)
                                    .height(66.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = null,
                                        onClick = { selectedDayIndex = index }
                                    ),
                                shape = RoundedCornerShape(16.dp),
                                color = bgColor,
                                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFF161E24))
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = item.dayOfWeek,
                                        fontSize = dayFontSize,
                                        fontWeight = FontWeight.Medium,
                                        color = textColor,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "${item.dayOfMonth}",
                                        fontSize = dateFontSize,
                                        fontWeight = FontWeight.Bold,
                                        color = numberColor,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    // Small dot for completed workouts
                                    if (item.completed || weeklyActivityStatus.getOrElse(index) { false }) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 3.dp)
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color(0xFF000000) else FitTheme.colors.primary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Today's Workout Section
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                        slideInVertically(initialOffsetY = { 30 })
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Workout",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "View Plan >",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FitTheme.colors.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    com.example.fitness.core.perf.AppPerfTracker.start("HOME_TO_TODAYS_WORKOUT")
                                    navigator.navigate(
                                        Screen.WorkoutOverview(
                                            workoutId = selectedWorkout.id,
                                            programId = scheduledProgram
                                        )
                                    )
                                }
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Workout Banner Card (Matching desired reference with top split and full-width Start Workout button)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .clickable {
                                com.example.fitness.core.perf.AppPerfTracker.start("HOME_TO_TODAYS_WORKOUT")
                                navigator.navigate(
                                    Screen.ActiveWorkoutSession(
                                        workoutId = selectedWorkout.id,
                                        programId = scheduledProgram
                                    )
                                )
                            },
                        shape = RoundedCornerShape(22.dp),
                        color = Color(0xFF07090C),
                        border = BorderStroke(1.dp, Color(0x33A8FF3E))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Top split: Left Workout Image + Right Info Details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Workout Thumbnail (rounded corners, exercise-specific)
                                Box(
                                    modifier = Modifier
                                        .weight(0.42f)
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                ) {
                                    Image(
                                        painter = painterResource(todayWorkoutImage),
                                        contentDescription = selectedWorkout.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                // Right Details
                                Column(
                                    modifier = Modifier.weight(0.58f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = selectedWorkout.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        softWrap = true
                                    )

                                    // Row 1: Duration | Difficulty
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = FitnessIcons.Clock,
                                            contentDescription = null,
                                            tint = Color(0xFFA2B4C0),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = selectedWorkout.duration,
                                            fontSize = 12.sp,
                                            color = Color(0xFFA2B4C0)
                                        )
                                        Text(
                                            text = " | ",
                                            fontSize = 12.sp,
                                            color = Color(0xFF4A555D)
                                        )
                                        Text(
                                            text = selectedWorkout.difficulty,
                                            fontSize = 12.sp,
                                            color = Color(0xFFA2B4C0)
                                        )
                                    }

                                    // Row 2: Exercise Count / Rest Day
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = FitnessIcons.Workout,
                                            contentDescription = null,
                                            tint = if (selectedWorkout.isRestDay) Color(0xFF38BDF8) else FitTheme.colors.primary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = if (selectedWorkout.isRestDay) "Rest Day" else "${selectedWorkout.exercises.size} Exercises",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (selectedWorkout.isRestDay) Color(0xFF38BDF8) else FitTheme.colors.primary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Bottom: Full-Width Start Workout Action Button
                            Surface(
                                onClick = {
                                    navigator.navigate(
                                        Screen.ActiveWorkoutSession(
                                            workoutId = selectedWorkout.id,
                                            programId = scheduledProgram
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = FitTheme.colors.primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = selectedWorkout.actionButtonText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF000000)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Athlete Workout Card (Dedicated Athletic Training Entry Point)
            WorkoutPromoCard(
                title = "Athlete Workout",
                description = "Train for performance, speed, strength and endurance.",
                ctaText = "Explore Athlete Training",
                imageRes = athleteWorkoutImage,
                features = listOf(
                    Pair(FitnessIcons.Clock, "Sprint & Speed Mechanics"),
                    Pair(FitnessIcons.FlameOutline, "Agility & Explosive Power"),
                    Pair(FitnessIcons.BarChart, "Conditioning & Endurance"),
                    Pair(FitnessIcons.ArmFlex, "Joint Mobility & Recovery")
                ),
                onClick = {
                    navigator.navigate(
                        Screen.ProgramDetail(
                            programId = "athlete",
                            level = if (UserSession.experienceLevel == ExperienceLevel.EXPERT) ExperienceLevel.ADVANCED else UserSession.experienceLevel
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Workout at Gym Card (Athgym Journey Card)
            WorkoutPromoCard(
                title = "Gym Workouts",
                description = "Build muscle, get stronger and become the best version of you.",
                ctaText = "Explore Gym Workouts",
                imageRes = gymWorkoutImage,
                features = listOf(
                    Pair(FitnessIcons.ArmFlex, "Strength Training"),
                    Pair(FitnessIcons.BarChart, "Muscle Building"),
                    Pair(FitnessIcons.FlameOutline, "Fat Loss"),
                    Pair(FitnessIcons.Target, "Custom Plans")
                ),
                onClick = { navigator.navigate(Screen.BodyPartSelection("gym")) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 6. Motivational Quote
            Text(
                text = "“Discipline today, results tomorrow.”",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontStyle = FontStyle.Italic,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Workout Promo Card (Shared reusable card for Athlete Workout & Gym Workouts)
// ---------------------------------------------------------------------------

@Composable
private fun WorkoutPromoCard(
    title: String,
    description: String,
    ctaText: String,
    imageRes: org.jetbrains.compose.resources.DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    features: List<Pair<ImageVector, String>> = emptyList()
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
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF07090C),
        border = BorderStroke(1.dp, Color(0xFF1E2830)),
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // 1. Background Image - strictly uses matchParentSize so it NEVER determines card height
            Image(
                painter = painterResource(imageRes),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                alignment = Alignment.CenterEnd,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(24.dp))
            )

            // 2. Cinematic Dark Gradient Overlay (dark on left for 100% text readability, clear on right for athlete/lifter)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0.0f to Color(0xFF07090C).copy(alpha = 0.96f),
                                0.48f to Color(0xFF07090C).copy(alpha = 0.90f),
                                0.72f to Color(0xFF07090C).copy(alpha = 0.55f),
                                0.90f to Color.Transparent
                            )
                        )
                    )
            )

            // 3. Subtle vertical vignette at bottom
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.70f to Color.Transparent,
                                1.0f to Color(0xFF07090C).copy(alpha = 0.50f)
                            )
                        )
                    )
            )

            // 4. Content Column - NORMAL LAYOUT FLOW, solely defines the card's natural height
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    // Title
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = true
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Description / Subtitle
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = Color(0xFF9EABB8),
                        lineHeight = 16.sp,
                        maxLines = 2,
                        softWrap = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 Feature Bullets
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        features.forEach { (featIcon, featText) ->
                            GymFeatureItem(icon = featIcon, text = featText)
                        }
                    }
                }

                // Intentional, dedicated vertical gap before CTA button (24dp)
                Spacer(modifier = Modifier.height(24.dp))

                // Full-width Neon CTA Button - normal child of Column, placed at the bottom
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = FitTheme.colors.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = ctaText,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = FitnessIcons.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF000000),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// Retain WorkoutOptionCard alias for backwards compatibility
@Composable
private fun WorkoutOptionCard(
    title: String,
    description: String,
    ctaText: String,
    imageRes: org.jetbrains.compose.resources.DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    features: List<Pair<ImageVector, String>> = emptyList()
) {
    WorkoutPromoCard(
        title = title,
        description = description,
        ctaText = ctaText,
        imageRes = imageRes,
        onClick = onClick,
        modifier = modifier,
        features = features
    )
}

@Composable
private fun GymFeatureItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFB0BDC9),
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = text,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFD4DFE8),
            lineHeight = 15.sp,
            maxLines = 2,
            softWrap = true
        )
    }
}

