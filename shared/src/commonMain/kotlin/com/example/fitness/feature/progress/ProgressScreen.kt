package com.example.fitness.feature.progress

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.components.PrimaryButton
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.navigation.LocalNavigator
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.progress.data.HistoryFilter
import com.example.fitness.feature.progress.data.WorkoutProgressService
import com.example.fitness.feature.progress.model.WorkoutSessionRecord
import kotlinx.coroutines.launch


@Composable
fun ProgressScreen(
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val authUid = try { com.example.fitness.core.firebase.FitnessFirebase.auth.currentUser?.uid } catch (_: Exception) { null }
    val effectiveUserId = if (!authUid.isNullOrBlank()) authUid else UserSession.currentUserId

    LaunchedEffect(effectiveUserId) {
        if (!effectiveUserId.isNullOrBlank()) {
            try {
                WorkoutProgressService.loadWorkoutHistory(effectiveUserId, forceRefresh = true)
            } catch (_: Exception) {
            }
        }
    }

    var selectedTimeframe by remember { mutableStateOf("Weekly") }
    var selectedHistoryFilter by remember { mutableStateOf(HistoryFilter.ALL) }
    var selectedSessionForDetail by remember { mutableStateOf<WorkoutSessionRecord?>(null) }
    var selectedExerciseForHistory by remember { mutableStateOf<String?>(null) }

    val totalWorkouts = WorkoutProgressService.getTotalWorkoutsCount()
    val currentStreak = WorkoutProgressService.getCurrentStreak()
    val longestStreak = WorkoutProgressService.getLongestStreak()
    val totalTimeFormatted = WorkoutProgressService.getTotalWorkoutTimeFormatted()

    val weeklyStats = WorkoutProgressService.getWeeklyStats()
    val monthlyStats = WorkoutProgressService.getMonthlyStats()
    val weeklyActivityStatus = WorkoutProgressService.getWeeklyDayActivityStatus()

    val historyList = WorkoutProgressService.getWorkoutHistory(selectedHistoryFilter)


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
                .padding(bottom = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

        // Title & Timeframe Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Analytics & Progress",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Real-time statistics & workout history",
                    fontSize = 13.sp,
                    color = Color(0xFF8E9BA4)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Timeframe Selector: Weekly vs Monthly
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF07090C),
                border = BorderStroke(1.dp, Color(0xFF161E24))
            ) {
                Row(modifier = Modifier.padding(3.dp)) {
                    listOf("Weekly", "Monthly").forEach { tf ->
                        val isSel = tf == selectedTimeframe
                        val bg by animateColorAsState(if (isSel) FitTheme.colors.primary else Color.Transparent)
                        val textCol by animateColorAsState(if (isSel) Color(0xFF000000) else Color(0xFF8E9BA4))

                        Surface(
                            onClick = { selectedTimeframe = tf },
                            shape = RoundedCornerShape(9.dp),
                            color = bg
                        ) {
                            Text(
                                text = tf,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textCol,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Check if user has zero completed workouts
        if (totalWorkouts == 0) {
            // EMPTY STATE
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF07090C),
                border = BorderStroke(1.dp, Color(0x33A8FF3E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF141F10),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "🏋️", fontSize = 28.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No Completed Workouts Yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Complete your first workout to start tracking your streak, volume, and history.",
                        fontSize = 13.sp,
                        color = Color(0xFF8E9BA4),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    PrimaryButton(
                        text = "Start Workout",
                        onClick = { navigator.navigate(Screen.Home) },
                        height = 46.dp,
                        modifier = Modifier.width(180.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Top 3 Dynamic Metric Cards (Weekly / Monthly stats)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val countText = if (selectedTimeframe == "Weekly") "${weeklyStats.workoutsThisWeek}" else "${monthlyStats.workoutsThisMonth}"
            val countSub = if (selectedTimeframe == "Weekly") "This Week" else "This Month"

            val durationText = if (selectedTimeframe == "Weekly") "${weeklyStats.totalDurationMinutesThisWeek}m" else "${monthlyStats.totalDurationMinutesThisMonth}m"

            val setsCountText = if (selectedTimeframe == "Weekly") "${weeklyStats.setsCompletedThisWeek}" else "${monthlyStats.setsCompletedThisMonth}"

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF07090C),
                border = BorderStroke(1.dp, Color(0x33A8FF3E)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)) {
                    Text(text = "Workouts", fontSize = 11.sp, color = Color(0xFF8E9BA4), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = countText, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = countSub, fontSize = 10.sp, color = FitTheme.colors.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF07090C),
                border = BorderStroke(1.dp, Color(0xFF1E2830)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)) {
                    Text(text = "Duration", fontSize = 11.sp, color = Color(0xFF8E9BA4), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = durationText, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = "Trained time", fontSize = 10.sp, color = FitTheme.colors.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF07090C),
                border = BorderStroke(1.dp, Color(0x33FFD600)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)) {
                    Text(text = "Sets", fontSize = 11.sp, color = Color(0xFF8E9BA4), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = setsCountText, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = FitTheme.colors.accentAmber, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = "Completed sets", fontSize = 10.sp, color = FitTheme.colors.accentAmber, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Streak & Total Summary Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF07090C),
                border = BorderStroke(1.dp, Color(0xFF161E24)),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🔥", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "Current Streak", fontSize = 10.sp, color = Color(0xFF7A8B97), fontWeight = FontWeight.Bold)
                        Text(text = "$currentStreak Days", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF07090C),
                border = BorderStroke(1.dp, Color(0xFF161E24)),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🏆", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "Longest Streak", fontSize = 10.sp, color = Color(0xFF7A8B97), fontWeight = FontWeight.Bold)
                        Text(text = "$longestStreak Days", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Active Program Progress Summary Card
        val activeProgStats = WorkoutProgressService.getProgramProgressStats(
            programId = UserSession.activeProgramId,
            trackCalendarDays = true
        )
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF07090C),
            border = BorderStroke(1.dp, Color(0x33A8FF3E)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = activeProgStats.programName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val displayDay = if (activeProgStats.completedCount > 0) {
                            activeProgStats.completedCount
                        } else {
                            activeProgStats.currentDay
                        }
                        Text(
                            text = "DAY $displayDay / ${activeProgStats.totalDays}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FitTheme.colors.primary
                        )
                    }
                    Text(
                        text = "${(activeProgStats.completionPercentage * 100).toInt()}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FitTheme.colors.primary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF161E24))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(activeProgStats.completionPercentage.coerceAtLeast(0.01f))
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(FitTheme.colors.primary, Color(0xFF88FF00))
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Completed: ${activeProgStats.completedCount}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        text = "Remaining: ${activeProgStats.remainingCount}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8E9BA4)
                    )
                    Text(
                        text = "Total Time: $totalTimeFormatted",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = FitTheme.colors.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Activity Volume Bar Graph Card (Dynamically driven by Weekly Activity)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF07090C),
            border = BorderStroke(1.dp, Color(0x33A8FF3E)),
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Weekly Workout Activity",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    val activeDaysCount = weeklyActivityStatus.count { it }
                    Text(
                        text = if (activeDaysCount == 1) "1 Day Active" else "$activeDaysCount Days Active",
                        fontSize = 12.sp,
                        color = FitTheme.colors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Volume Bar Visualization
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val days = listOf("M", "T", "W", "T", "F", "S", "S")

                    days.forEachIndexed { i, d ->
                        val hasWorkoutOnDay = weeklyActivityStatus.getOrElse(i) { false }
                        val barHeight = if (hasWorkoutOnDay) 85.dp else 24.dp

                        val barBrush = if (hasWorkoutOnDay)
                            Brush.verticalGradient(
                                listOf(
                                    FitTheme.colors.primary,
                                    FitTheme.colors.primary.copy(alpha = 0.75f)
                                )
                            )
                        else
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF222B32),
                                    Color(0xFF14191C)
                                )
                            )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(barHeight)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(barBrush),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                if (hasWorkoutOnDay) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF000000))
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = d,
                                fontSize = 11.sp,
                                fontWeight = if (hasWorkoutOnDay) FontWeight.Bold else FontWeight.Normal,
                                color = if (hasWorkoutOnDay) FitTheme.colors.primary else Color(0xFF6E7E8A)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // PROGRESS BY PROGRAM SECTION
        Text(
            text = "Progress by Program",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(12.dp))

        val programStats = listOf(
            Triple("30-Day Workout Plan", WorkoutProgressService.getProgramProgressStats("30-day", trackCalendarDays = true).completedCount, Color(0xFFA8FF3E)),
            Triple("Hypertrophy", WorkoutProgressService.getProgramProgressStats("hypertrophy", trackCalendarDays = true).completedCount, Color(0xFF88FF00)),
            Triple("Strength", WorkoutProgressService.getProgramProgressStats("strength", trackCalendarDays = true).completedCount, Color(0xFFFF5252)),
            Triple("Athlete Performance", WorkoutProgressService.getProgramProgressStats("athlete", trackCalendarDays = true).completedCount, Color(0xFFFFD600))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            programStats.take(2).forEach { (progName, count, accentColor) ->
                ProgramStatSmallCard(
                    programName = progName,
                    completedCount = count,
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            programStats.drop(2).forEach { (progName, count, accentColor) ->
                ProgramStatSmallCard(
                    programName = progName,
                    completedCount = count,
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // WORKOUT HISTORY SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Workout History",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // History Filter Pills: All / This Week / This Month
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    HistoryFilter.ALL to "All",
                    HistoryFilter.THIS_WEEK to "Week",
                    HistoryFilter.THIS_MONTH to "Month"
                ).forEach { (filterOption, label) ->
                    val isSel = selectedHistoryFilter == filterOption
                    Surface(
                        onClick = { selectedHistoryFilter = filterOption },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSel) FitTheme.colors.primary else Color(0xFF0D1216),
                        border = if (isSel) null else BorderStroke(1.dp, Color(0xFF19232A))
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color(0xFF000000) else Color(0xFF8E9BA4),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (WorkoutProgressService.isLoadingHistory) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF07090C),
                border = BorderStroke(1.dp, Color(0xFF161E24)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = FitTheme.colors.primary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Loading workout history...",
                        fontSize = 13.sp,
                        color = Color(0xFF8E9BA4)
                    )
                }
            }
        } else if (WorkoutProgressService.historyLoadError != null && historyList.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF14080A),
                border = BorderStroke(1.dp, Color(0xFF42151B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Unable to load workout history.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF5252)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap below to retry syncing your records.",
                        fontSize = 12.sp,
                        color = Color(0xFF8E9BA4)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        onClick = {
                            if (!effectiveUserId.isNullOrBlank()) {
                                coroutineScope.launch {
                                    WorkoutProgressService.loadWorkoutHistory(effectiveUserId, forceRefresh = true)
                                }
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = FitTheme.colors.primary
                    ) {
                        Text(
                            text = "Retry",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        } else if (historyList.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF07090C),
                border = BorderStroke(1.dp, Color(0xFF161E24)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (totalWorkouts == 0) "No workouts recorded yet. Complete a workout to see your progress!" else "No workouts recorded in this timeframe.",
                        fontSize = 12.sp,
                        color = Color(0xFF7A8B97)
                    )
                }
            }
        } else {
            historyList.forEach { session ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF07090C),
                    border = BorderStroke(1.dp, Color(0xFF161E24)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable { selectedSessionForDetail = session }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = session.workoutTitle,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF141D22)
                                ) {
                                    Text(
                                        text = session.programName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = FitTheme.colors.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(text = session.displayDate, fontSize = 12.sp, color = Color(0xFF8E9BA4))
                                Text(text = " • ", fontSize = 12.sp, color = Color(0xFF4A555D))
                                Text(text = "${session.durationMinutes} min", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                                Text(text = " • ", fontSize = 12.sp, color = Color(0xFF4A555D))
                                Text(text = "${session.completedExercises} ex", fontSize = 12.sp, color = Color(0xFF8E9BA4))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0B1F08),
                            border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "Completed ✓",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = FitTheme.colors.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
        }
    }

    // WORKOUT HISTORY DETAIL MODAL
    if (selectedSessionForDetail != null) {
        val session = selectedSessionForDetail!!
        AlertDialog(
            onDismissRequest = { selectedSessionForDetail = null },
            containerColor = Color(0xFF07090C),
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = session.workoutTitle.uppercase(),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0F260B),
                        border = BorderStroke(1.dp, FitTheme.colors.primary)
                    ) {
                        Text(
                            text = "Completed",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${session.displayDate} • ${session.durationMinutes} min",
                        fontSize = 13.sp,
                        color = FitTheme.colors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Target Muscle: ${session.targetMuscle.joinToString(", ")}",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Completion percentage
                    val completionPct = if (session.prescribedExerciseCount > 0) {
                        ((session.completedExerciseCount.toFloat() / session.prescribedExerciseCount) * 100).toInt().coerceIn(0, 100)
                    } else if (session.totalExercises > 0) {
                        ((session.completedExercises.toFloat() / session.totalExercises) * 100).toInt().coerceIn(0, 100)
                    } else 100

                    Text(
                        text = "Completion: $completionPct%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FitTheme.colors.primary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Breakdown Cards
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F1519),
                        border = BorderStroke(1.dp, Color(0xFF1C262E)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Training Program", fontSize = 12.sp, color = Color(0xFF8E9BA4))
                                Text(text = session.programName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FitTheme.colors.primary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Exercises Completed", fontSize = 12.sp, color = Color(0xFF8E9BA4))
                                Text(text = "${session.completedExercises} / ${session.totalExercises}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Sets Completed", fontSize = 12.sp, color = Color(0xFF8E9BA4))
                                Text(text = "${session.completedSets} / ${session.totalSets}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Exercises Breakdown: Prescribed vs. Actual
                    Text(
                        text = "EXERCISES (${session.completedExerciseCount} / ${session.prescribedExerciseCount})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF8E9BA4)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (session.exercises.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0F1519),
                            border = BorderStroke(1.dp, Color(0xFF1C262E)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${session.completedExercises} exercises completed in full.",
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        session.exercises.forEach { ex ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0F1519),
                                border = BorderStroke(1.dp, Color(0xFF1C262E)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = ex.exerciseName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            onClick = { selectedExerciseForHistory = ex.exerciseId },
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF142410)
                                        ) {
                                            Text(
                                                text = "History",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = FitTheme.colors.primary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Prescribed: ${ex.prescribedSets} × ${ex.prescribedReps}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF8E9BA4)
                                        )
                                        val actualRepsText = if (ex.completedReps.isNotEmpty()) ex.completedReps.joinToString(", ") else ex.prescribedReps
                                        Text(
                                            text = "Actual: ${ex.completedSets} × $actualRepsText",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFFCBD5E1)
                                        )
                                    }
                                    if (ex.weightUsed != null) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Weight: ${ex.weightUsed} kg",
                                            fontSize = 11.sp,
                                            color = FitTheme.colors.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (ex.completed) "✓ Completed" else "Incomplete",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (ex.completed) FitTheme.colors.primary else Color(0xFFFF5252)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Phases Checkmarks
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PhaseStatusChip("Warm-Up", session.warmUpCompleted)
                        PhaseStatusChip("Main", session.mainWorkoutCompleted)
                        PhaseStatusChip("Cool-Down", session.coolDownCompleted)
                    }
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = "Close",
                    onClick = { selectedSessionForDetail = null },
                    height = 42.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }

    // EXERCISE PERFORMANCE HISTORY MODAL
    if (selectedExerciseForHistory != null) {
        val exerciseId = selectedExerciseForHistory!!
        val exerciseHistoryItems = WorkoutProgressService.getExercisePerformanceHistory(exerciseId)
        val exerciseName = SampleExerciseData.getById(exerciseId)?.name ?: exerciseId

        AlertDialog(
            onDismissRequest = { selectedExerciseForHistory = null },
            containerColor = Color(0xFF07090C),
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = exerciseName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Historical Performance",
                        fontSize = 12.sp,
                        color = FitTheme.colors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (exerciseHistoryItems.isEmpty()) {
                        Text(
                            text = "No previous sessions recorded for this exercise.",
                            fontSize = 12.sp,
                            color = Color(0xFF8E9BA4)
                        )
                    } else {
                        exerciseHistoryItems.forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0F1519),
                                border = BorderStroke(1.dp, Color(0xFF1C262E)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = item.date,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = item.workoutTitle,
                                            fontSize = 11.sp,
                                            color = FitTheme.colors.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Prescribed: ${item.prescribedSets} × ${item.prescribedReps}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF8E9BA4)
                                    )
                                    val actReps = if (item.completedReps.isNotEmpty()) item.completedReps.joinToString(", ") else item.prescribedReps
                                    Text(
                                        text = "Actual: ${item.completedSets} × $actReps",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFCBD5E1)
                                    )
                                    if (item.weightUsed != null) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Weight: ${item.weightUsed} kg",
                                            fontSize = 11.sp,
                                            color = FitTheme.colors.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = "Close",
                    onClick = { selectedExerciseForHistory = null },
                    height = 42.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}

@Composable
private fun PhaseStatusChip(name: String, completed: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (completed) Color(0xFF0F260B) else Color(0xFF181F24),
        border = BorderStroke(1.dp, if (completed) FitTheme.colors.primary.copy(alpha = 0.5f) else Color(0xFF222C33))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = if (completed) "✓ " else "• ", fontSize = 11.sp, color = if (completed) FitTheme.colors.primary else Color(0xFF8E9BA4), fontWeight = FontWeight.Bold)
            Text(text = name, fontSize = 11.sp, color = if (completed) Color.White else Color(0xFF8E9BA4), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ProgramStatSmallCard(
    programName: String,
    completedCount: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF07090C),
        border = BorderStroke(1.dp, Color(0xFF161E24)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = programName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                softWrap = true
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$completedCount completed",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (completedCount.toFloat() / 30f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = accentColor,
                trackColor = Color(0xFF161E24)
            )
        }
    }
}

