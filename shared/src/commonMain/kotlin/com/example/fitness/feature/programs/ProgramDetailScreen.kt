package com.example.fitness.feature.programs

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.fitness.core.components.NavigationTab
import com.example.fitness.core.components.PrimaryButton
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.navigation.BackHandler
import com.example.fitness.core.navigation.LocalNavigator
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.core.util.TitleFormatter
import com.example.fitness.feature.programs.data.ProgramRegistry
import com.example.fitness.feature.programs.model.ProgramScheduleItem
import com.example.fitness.feature.programs.model.WorkoutProgram
import com.example.fitness.feature.progress.data.WorkoutProgressService
import com.example.fitness.core.firebase.FitnessFirebase
import com.example.fitness.feature.workout.model.ExperienceLevel
import org.jetbrains.compose.resources.painterResource

/**
 * Policy governing the display of header action controls for Program Detail screens.
 * Suppresses the non-functional filter icon on Program Detail screens as programs are fixed sequential schedules.
 */
object ProgramDetailFilterPolicy {
    fun shouldShowFilter(programId: String = "", programTitle: String = ""): Boolean = false
}

@Composable
fun ProgramDetailScreen(
    programId: String,
    experienceLevel: ExperienceLevel? = null,
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current
    val effectiveLevel = experienceLevel ?: UserSession.experienceLevel
    val isLevelAvailable = remember(programId, effectiveLevel) {
        ProgramRegistry.isProgramLevelAvailable(programId, effectiveLevel)
    }
    val program: WorkoutProgram = remember(programId, effectiveLevel) {
        ProgramRegistry.getProgramById(programId, effectiveLevel) ?: ProgramRegistry.getAllPrograms().first()
    }

    val showFilter = remember(program.id, program.title) {
        ProgramDetailFilterPolicy.shouldShowFilter(program.id, program.title)
    }

    var visible by remember { mutableStateOf(false) }

    val authUid = try { FitnessFirebase.auth.currentUser?.uid } catch (_: Exception) { null }
    val effectiveUid = authUid ?: UserSession.currentUserId ?: ""

    LaunchedEffect(effectiveUid) {
        visible = true
        if (effectiveUid.isNotBlank() && WorkoutProgressService.workoutHistory.isEmpty()) {
            WorkoutProgressService.loadWorkoutHistory(effectiveUid)
        }
    }

    // Program-specific progress metrics
    val completedCount = UserSession.getProgramCompletedCount(program.id)
    val totalWorkouts = program.workoutsCount
    val remainingCount = (totalWorkouts - completedCount).coerceAtLeast(0)
    val progressPercent = UserSession.getProgramProgressPercentage(program.id, totalWorkouts)
    val currentDay = UserSession.getProgramCurrentDay(program.id)

    val currentScheduleItem = program.schedule.find { it.dayNumber == currentDay }
        ?: program.schedule.firstOrNull()

    BackHandler(enabled = true) {
        if (!navigator.pop()) {
            navigator.switchTab(NavigationTab.HOME)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000)),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 840.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp)
        ) {
            // 1. Top Bar & Hero Section
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularIconButton(
                            icon = FitnessIcons.ArrowBack,
                            onClick = {
                                if (!navigator.pop()) {
                                    navigator.switchTab(NavigationTab.HOME)
                                }
                            },
                            contentDescription = "Back"
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = program.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (showFilter) {
                            Spacer(modifier = Modifier.width(12.dp))
                            CircularIconButton(
                                icon = FitnessIcons.Filter,
                                onClick = { },
                                contentDescription = "Options"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hero Banner Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        Image(
                            painter = painterResource(program.imageRes),
                            contentDescription = program.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color(0x80000000),
                                            Color(0xFF07090C)
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = FitTheme.colors.primary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Text(
                                    text = program.category.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF000000),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Text(
                                text = program.title,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = program.subtitle,
                                fontSize = 13.sp,
                                color = Color(0xFFC0D0DC)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Metadata Badges Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProgramMetaPill(
                            label = "DURATION",
                            value = program.duration,
                            modifier = Modifier.weight(1f)
                        )
                        ProgramMetaPill(
                            label = "LEVEL",
                            value = effectiveLevel.title,
                            modifier = Modifier.weight(1f)
                        )
                        ProgramMetaPill(
                            label = "WORKOUTS",
                            value = "${program.workoutsCount} Days",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Description & Goal
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF07090C),
                        border = BorderStroke(1.dp, Color(0xFF161E24)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Training Goal: ${program.goal}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = FitTheme.colors.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = program.description,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = Color(0xFF8E9BA4)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isLevelAvailable) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF140F08),
                            border = BorderStroke(1.dp, Color(0x66FFB020)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0x33FFB020),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "!",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFFFFB020)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Plan Not Available for ${effectiveLevel.title}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${program.title} does not currently have a dedicated 30-day program for ${effectiveLevel.title} level. Please choose an available level to access workouts.",
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = Color(0xFFC0D0DC),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                PrimaryButton(
                                    text = "Choose Another Level",
                                    onClick = { navigator.pop() },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        // Program Progress Card
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF07090C),
                            border = BorderStroke(1.dp, Color(0x33A8FF3E)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Program Progress",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Day $currentDay / $totalWorkouts",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = FitTheme.colors.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                LinearProgressIndicator(
                                    progress = { progressPercent },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = FitTheme.colors.primary,
                                    trackColor = Color(0xFF161E24)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Completed: $completedCount",
                                        fontSize = 12.sp,
                                        color = Color(0xFFA2B4C0)
                                    )
                                    Text(
                                        text = "Remaining: $remainingCount",
                                        fontSize = 12.sp,
                                        color = Color(0xFFA2B4C0)
                                    )
                                    Text(
                                        text = "${(progressPercent * 100).toInt()}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FitTheme.colors.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Primary Action Button: Start / Continue Program
                        val buttonText = if (completedCount == 0 && currentDay == 1) {
                            "Start Program"
                        } else {
                            "Continue Program (Day $currentDay)"
                        }

                        PrimaryButton(
                            text = buttonText,
                            onClick = {
                                currentScheduleItem?.let {
                                    navigator.navigate(
                                        Screen.ActiveWorkoutSession(
                                            workoutId = it.workoutId,
                                            programId = program.id
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Schedule Section Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Workout Schedule",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${program.schedule.size} Days",
                                fontSize = 13.sp,
                                color = Color(0xFF8E9BA4)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // 2. Schedule Day Items
            if (isLevelAvailable) {
                itemsIndexed(program.schedule) { index, scheduleItem ->
                val isCompleted = UserSession.isProgramDayCompleted(program.id, scheduleItem.dayNumber)
                val isCurrent = scheduleItem.dayNumber == currentDay

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                            slideInVertically(
                                initialOffsetY = { 20 * (index % 10 + 1) },
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)
                            )
                ) {
                    ScheduleDayCard(
                        item = scheduleItem,
                        isCompleted = isCompleted,
                        isCurrent = isCurrent,
                        onClick = {
                            navigator.navigate(
                                Screen.WorkoutOverview(
                                    workoutId = scheduleItem.workoutId,
                                    programId = program.id
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}
}

@Composable
private fun ProgramMetaPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF07090C),
        border = BorderStroke(1.dp, Color(0xFF161E24)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF5A6B77),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                softWrap = true
            )
        }
    }
}

@Composable
private fun ScheduleDayCard(
    item: ProgramScheduleItem,
    isCompleted: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1.0f)

    val borderColor = when {
        isCurrent -> FitTheme.colors.primary
        isCompleted -> Color(0x44A8FF3E)
        else -> Color(0xFF161E24)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF07090C),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Day Number Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isCurrent) FitTheme.colors.primary else Color(0xFF121920))
                    .border(
                        1.dp,
                        if (isCompleted) FitTheme.colors.primary else Color(0xFF1E2830),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = FitnessIcons.Check,
                        contentDescription = "Completed",
                        tint = if (isCurrent) Color(0xFF000000) else FitTheme.colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "D${item.dayNumber}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrent) Color(0xFF000000) else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = TitleFormatter.stripRotation(item.dayTitle),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    softWrap = true
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = item.focus,
                    fontSize = 12.sp,
                    color = Color(0xFF8E9BA4),
                    maxLines = 2,
                    softWrap = true
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.duration,
                        fontSize = 11.sp,
                        color = Color(0xFFA2B4C0)
                    )
                    Text(
                        text = "  •  ",
                        fontSize = 11.sp,
                        color = Color(0xFF4A555D)
                    )
                    Text(
                        text = "${item.exerciseCount} exercises",
                        fontSize = 11.sp,
                        color = Color(0xFFA2B4C0)
                    )
                }

                if (item.isRestDay) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0C1924),
                        border = BorderStroke(1.dp, Color(0x5538BDF8))
                    ) {
                        Text(
                            text = "Rest Day",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right arrow or status badge
            if (isCurrent) {
                Surface(
                    shape = CircleShape,
                    color = FitTheme.colors.primary,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = FitnessIcons.ChevronRight,
                            contentDescription = "Current",
                            tint = Color(0xFF000000),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Icon(
                    imageVector = FitnessIcons.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF3E4A53),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
