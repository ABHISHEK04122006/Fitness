package com.example.fitness.feature.goals

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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.core.components.CircularIconButton
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.components.PrimaryButton
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.navigation.LocalNavigator
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.workout.model.ExperienceLevel
import fitness.shared.generated.resources.Res
import fitness.shared.generated.resources.goal_athlete
import fitness.shared.generated.resources.goal_general
import fitness.shared.generated.resources.program_strength
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

import androidx.compose.runtime.rememberCoroutineScope
import com.example.fitness.core.firebase.FitnessFirebase
import com.example.fitness.core.user.data.FirebaseUserRepository
import kotlinx.coroutines.launch

@Composable
fun GoalSelectionScreen(
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val userRepository = remember { FirebaseUserRepository() }
    val initialGoal = remember {
        when (UserSession.activeProgramId) {
            "athlete" -> "athlete"
            "strength", "hypertrophy" -> "strength"
            else -> "general"
        }
    }
    val initialLevel = remember {
        UserSession.experienceLevel.toUserFacingLevel()
    }
    var selectedGoal by remember { mutableStateOf(initialGoal) }
    var selectedDays by remember { mutableStateOf(UserSession.weeklyCommitment) }
    var selectedLevel by remember { mutableStateOf(initialLevel) }
    var visible by remember { mutableStateOf(false) }

    fun navigateToProgram(goal: String, levelStr: String) {
        val targetLevel = ExperienceLevel.fromUserFacingOption(levelStr)
        val targetProgramId = when (goal) {
            "general" -> "30-day"
            "athlete" -> "athlete"
            "strength" -> "strength"
            else -> "30-day"
        }
        UserSession.experienceLevel = targetLevel
        UserSession.setActiveProgram(targetProgramId)
        UserSession.weeklyCommitment = selectedDays
        UserSession.updateWorkoutPreferences(
            gender = UserSession.currentUserGender ?: "Male",
            workoutDays = selectedDays,
            fitnessLevel = levelStr
        )
        val currentUid = UserSession.currentUserId ?: FitnessFirebase.auth.currentUser?.uid
        if (!currentUid.isNullOrBlank()) {
            coroutineScope.launch {
                try {
                    userRepository.updateWorkoutPreferences(
                        uid = currentUid,
                        gender = UserSession.currentUserGender ?: "Male",
                        workoutDays = selectedDays,
                        fitnessLevel = levelStr
                    )
                } catch (_: Exception) {}
            }
        }
        navigator.navigate(Screen.ProgramDetail(programId = targetProgramId, level = targetLevel))
    }

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 90.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

            // Back Button & Step Indicator
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF07090C),
                        border = BorderStroke(1.dp, Color(0x33A8FF3E))
                    ) {
                        Text(
                            text = "Step 2 of 2",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FitTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        onClick = { navigator.resetRoot(Screen.Home) },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0C1015),
                        border = BorderStroke(1.dp, Color(0x40A8FF3E))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Skip",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = FitTheme.colors.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Header
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                        slideInVertically(initialOffsetY = { -30 })
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "What's Your Goal?",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Choose a path that fits your lifestyle & ambitions.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF8E9BA4),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Goal Option 1: General Fitness
            GoalCard(
                title = "General Fitness",
                subtitle = "Get stronger, healthier and fitter.",
                bullets = listOf(
                    "Build functional muscle",
                    "Lose body fat & lean out",
                    "Improve cardiovascular health",
                    "Tailored for all experience levels"
                ),
                tag = "Most Popular",
                tagColor = FitTheme.colors.accentCyan,
                imageRes = Res.drawable.goal_general,
                isSelected = selectedGoal == "general",
                onClick = {
                    if (selectedGoal == "general") {
                        navigateToProgram("general", selectedLevel)
                    } else {
                        selectedGoal = "general"
                    }
                },
                onArrowClick = {
                    selectedGoal = "general"
                    navigateToProgram("general", selectedLevel)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Goal Option 2: Athlete Training
            GoalCard(
                title = "Athlete Training",
                subtitle = "Train like an athlete. Perform at your peak.",
                bullets = listOf(
                    "Sport-specific agility drills",
                    "Explosive power & sprint speed",
                    "High-output metabolic conditioning",
                    "Dynamic mobility & recovery protocols"
                ),
                tag = "Performance",
                tagColor = FitTheme.colors.primary,
                imageRes = Res.drawable.goal_athlete,
                isSelected = selectedGoal == "athlete",
                onClick = {
                    if (selectedGoal == "athlete") {
                        navigateToProgram("athlete", selectedLevel)
                    } else {
                        selectedGoal = "athlete"
                    }
                },
                onArrowClick = {
                    selectedGoal = "athlete"
                    navigateToProgram("athlete", selectedLevel)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Goal Option 3: Hypertrophy & Strength
            GoalCard(
                title = "Hypertrophy & Strength",
                subtitle = "Max muscle mass & heavy compound lifts.",
                bullets = listOf(
                    "Progressive overload periodization",
                    "Squat, bench & deadlift mastery",
                    "Targeted muscle group splits",
                    "Weekly volume progression"
                ),
                tag = "Heavy Strength",
                tagColor = FitTheme.colors.accentAmber,
                imageRes = Res.drawable.program_strength,
                isSelected = selectedGoal == "strength",
                onClick = {
                    if (selectedGoal == "strength") {
                        navigateToProgram("strength", selectedLevel)
                    } else {
                        selectedGoal = "strength"
                    }
                },
                onArrowClick = {
                    selectedGoal = "strength"
                    navigateToProgram("strength", selectedLevel)
                }
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Training Frequency Selector
            Text(
                text = "Weekly Commitment",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val daysOptions = listOf("3 Days", "4 Days", "5 Days", "6 Days")
                daysOptions.forEach { days ->
                    val isSelected = days == selectedDays
                    val bg by animateColorAsState(if (isSelected) FitTheme.colors.primary else Color(0xFF07090C))
                    val textCol by animateColorAsState(if (isSelected) Color(0xFF000000) else Color(0xFF8E9BA4))

                    Surface(
                        onClick = { selectedDays = days },
                        shape = RoundedCornerShape(14.dp),
                        color = bg,
                        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFF161E24)),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = days, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textCol)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Experience Level Selector
            Text(
                text = "Experience Level",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val levels = listOf("No Experience", "Beginner", "Advanced", "Expert")
                levels.chunked(2).forEach { rowLevels ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowLevels.forEach { level ->
                            val isSelected = level == selectedLevel
                            val bg by animateColorAsState(if (isSelected) FitTheme.colors.primary else Color(0xFF07090C))
                            val textCol by animateColorAsState(if (isSelected) Color(0xFF000000) else Color(0xFF8E9BA4))

                            Surface(
                                onClick = { selectedLevel = level },
                                shape = RoundedCornerShape(14.dp),
                                color = bg,
                                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFF161E24)),
                                modifier = Modifier.weight(1f).height(42.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = level,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textCol,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }
        }

        // Sticky Bottom "Continue to Dashboard" Button
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            color = Color(0xFF000000).copy(alpha = 0.96f),
            shadowElevation = 16.dp
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(FitTheme.colors.neonGlowGradient)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        PrimaryButton(
                            text = "Continue to Dashboard",
                            onClick = {
                                val targetLevel = ExperienceLevel.fromUserFacingOption(selectedLevel)
                                val targetProgramId = when (selectedGoal) {
                                    "general" -> "30-day"
                                    "athlete" -> "athlete"
                                    "strength" -> "strength"
                                    else -> "30-day"
                                }
                                UserSession.experienceLevel = targetLevel
                                UserSession.setActiveProgram(targetProgramId)
                                UserSession.weeklyCommitment = selectedDays
                                UserSession.updateWorkoutPreferences(
                                    gender = UserSession.currentUserGender ?: "Male",
                                    workoutDays = selectedDays,
                                    fitnessLevel = selectedLevel
                                )
                                val currentUid = UserSession.currentUserId ?: FitnessFirebase.auth.currentUser?.uid
                                if (!currentUid.isNullOrBlank()) {
                                    coroutineScope.launch {
                                        try {
                                            userRepository.updateWorkoutPreferences(
                                                uid = currentUid,
                                                gender = UserSession.currentUserGender ?: "Male",
                                                workoutDays = selectedDays,
                                                fitnessLevel = selectedLevel
                                            )
                                        } catch (_: Exception) {}
                                    }
                                }
                                navigator.resetRoot(Screen.Home)
                            },
                            height = 54.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalCard(
    title: String,
    subtitle: String,
    bullets: List<String>,
    tag: String,
    tagColor: Color,
    imageRes: DrawableResource,
    isSelected: Boolean,
    onClick: () -> Unit,
    onArrowClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMedium)
    )

    val borderColor by animateColorAsState(
        if (isSelected) FitTheme.colors.primary else Color(0xFF161E24)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF07090C),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .heightIn(min = 210.dp)
        ) {
            // Left Image Section
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .fillMaxHeight()
            ) {
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                0.0f to Color.Transparent,
                                0.7f to Color(0x6607090C),
                                1.0f to Color(0xFF07090C)
                            )
                        )
                )
            }

            // Right Content Section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 10.dp, top = 14.dp, end = 12.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            // Badge Tag
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = tagColor.copy(alpha = 0.15f),
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tagColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = subtitle,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = Color(0xFF8E9BA4)
                            )
                        }

                        // Circular White/Neon Arrow Button
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) FitTheme.colors.primary else Color.White)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onArrowClick
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FitnessIcons.ChevronRight,
                                contentDescription = "Open $title",
                                tint = Color(0xFF000000),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bullets with Checkmarks
                    Column(
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        bullets.forEach { bullet ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(15.dp)
                                        .clip(CircleShape)
                                        .background(FitTheme.colors.primary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = FitnessIcons.Check,
                                        contentDescription = null,
                                        tint = FitTheme.colors.primary,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(7.dp))
                                Text(
                                    text = bullet,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFCBD5E1),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
