package com.example.fitness.feature.workout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.example.fitness.core.navigation.BackHandler
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.ui.ExerciseDetailModalSheet
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.fitness.platformLog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.core.components.CircularIconButton
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.components.NavigationTab
import com.example.fitness.core.components.PrimaryButton
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.navigation.LocalNavigator
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.progress.data.WorkoutProgressService
import com.example.fitness.feature.progress.model.ExercisePerformanceRecord
import com.example.fitness.feature.progress.model.WorkoutSessionRecord
import com.example.fitness.feature.programs.data.ProgramRegistry
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import com.example.fitness.feature.workout.data.ThirtyDayWorkoutRepository
import com.example.fitness.feature.workout.model.CustomExerciseConfig
import com.example.fitness.feature.workout.model.ExerciseItem
import com.example.fitness.feature.workout.model.PlanExercise
import com.example.fitness.feature.workout.model.ThirtyDayWorkout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SessionPhase {
    WARM_UP,
    MAIN_WORKOUT,
    COOL_DOWN,
    COMPLETE
}

private fun PlanExercise.toExerciseItem(): ExerciseItem {
    val canonical = com.example.fitness.feature.exercise.data.SampleExerciseData.findCanonical(id, name)
    val effectiveMedia = if (canonical?.media?.videoUrl?.isNotBlank() == true) canonical.media else media
    val effectiveVideoUrl = when {
        effectiveMedia.videoUrl.isNotBlank() && !effectiveMedia.videoUrl.contains("fitness.app/demo", ignoreCase = true) -> effectiveMedia.videoUrl
        videoUrl.isNotBlank() && !videoUrl.contains("fitness.app/demo", ignoreCase = true) -> videoUrl
        else -> ""
    }
    return ExerciseItem(
        id = canonical?.id ?: id,
        name = name,
        sets = "$sets • $reps",
        duration = rest,
        description = instructions,
        imageRes = imageRes,
        videoDemoUrl = effectiveVideoUrl,
        tips = listOf(instructions, "Target: $targetMuscle", "Equipment: $equipment"),
        media = effectiveMedia.copy(videoUrl = effectiveVideoUrl)
    )
}

@Composable
fun ActiveWorkoutSessionScreen(
    workoutId: String = "mon",
    programId: String = "",
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current
    val effectiveProgramId = if (programId.isNotEmpty()) programId else UserSession.scheduledProgramId
    val workout: ThirtyDayWorkout = remember(workoutId, effectiveProgramId, UserSession.experienceLevel, com.example.fitness.feature.exercise.data.SampleExerciseData.canonicalRevision) {
        CanonicalWorkoutResolver.resolveThirtyDayWorkout(effectiveProgramId, workoutId, UserSession.experienceLevel)
    }

    val initialPhase = if (workout.isRestDay) SessionPhase.COOL_DOWN else SessionPhase.WARM_UP

    // Prepare or restore session state in ActiveWorkoutSessionManager synchronously
    remember(workout.id, effectiveProgramId) {
        ActiveWorkoutSessionManager.prepareSession(workout.id, effectiveProgramId, initialPhase)
        true
    }

    val currentPhase = ActiveWorkoutSessionManager.currentPhase
    val currentExerciseIndex = ActiveWorkoutSessionManager.currentExerciseIndex
    val currentSetIndex = ActiveWorkoutSessionManager.currentSetIndex
    var detailExerciseId by remember(workout.id) { mutableStateOf<String?>(null) }

    // Rest timer state
    var isResting by remember(workout.id) { mutableStateOf(false) }
    var restSecondsLeft by remember(workout.id) { mutableIntStateOf(45) }

    // Coroutine scope for background sync
    val coroutineScope = rememberCoroutineScope()
    var completedSessionRecord by remember { mutableStateOf<WorkoutSessionRecord?>(null) }
    var isSavingSession by remember { mutableStateOf(false) }
    val stableSessionId = ActiveWorkoutSessionManager.stableSessionId
        ?: remember(workout.id) { "session_${com.example.fitness.getCurrentEpochMillis()}_${effectiveProgramId}_${workout.dayNumber}" }

    // Quit confirmation dialog
    var showQuitDialog by remember { mutableStateOf(false) }

    fun handleBack() {
        if (detailExerciseId != null) {
            detailExerciseId = null
        } else if (showQuitDialog) {
            showQuitDialog = false
        } else if (ActiveWorkoutSessionManager.executionState == WorkoutExecutionState.NOT_STARTED) {
            ActiveWorkoutSessionManager.resetSession()
            navigator.pop()
        } else if (ActiveWorkoutSessionManager.executionState == WorkoutExecutionState.COMPLETED) {
            ActiveWorkoutSessionManager.resetSession()
            navigator.switchTab(NavigationTab.HOME)
        } else {
            showQuitDialog = true
        }
    }

    // Intercept system Back button
    BackHandler(enabled = true) {
        handleBack()
    }

    val completedSetsByExercise = ActiveWorkoutSessionManager.completedSetsByExercise
    val completedRepsByExercise = ActiveWorkoutSessionManager.completedRepsByExercise
    val customExerciseConfigs = ActiveWorkoutSessionManager.customExerciseConfigs

    val resolvedProgramName = remember(effectiveProgramId) {
        when (effectiveProgramId.lowercase().trim()) {
            "hypertrophy" -> "Hypertrophy"
            "strength" -> "Strength"
            "athlete", "athlete_performance" -> "Athlete Performance"
            else -> "30-Day Workout Plan"
        }
    }

    fun buildPerformanceRecords(): List<ExercisePerformanceRecord> {
        if (workout.isRestDay) return emptyList()
        return workout.exercises.map { ex ->
            val config = customExerciseConfigs[ex.id] ?: CustomExerciseConfig.fromPlanExercise(ex)
            val setsCount = config.sets
            val repStr = config.displayReps
            val repNum = config.activeRepsOrDurationCount
            val restSecs = config.restSeconds
            val actualSets = completedSetsByExercise[ex.id] ?: 0
            val actualReps = completedRepsByExercise[ex.id]?.toList() ?: if (actualSets > 0) List(actualSets) { repNum } else emptyList()
            val isExCompleted = actualSets >= setsCount && setsCount > 0

            ExercisePerformanceRecord(
                exerciseId = ex.id,
                exerciseName = ex.name,
                prescribedSets = setsCount,
                prescribedReps = repStr,
                completedSets = actualSets,
                completedReps = actualReps,
                weightUsed = null,
                restSeconds = restSecs,
                completed = isExCompleted,
                notes = null
            )
        }
    }

    // Rest interval countdown
    LaunchedEffect(isResting, restSecondsLeft) {
        if (isResting && restSecondsLeft > 0) {
            delay(1000)
            restSecondsLeft--
        } else if (isResting && restSecondsLeft <= 0) {
            isResting = false
            restSecondsLeft = 45
        }
    }

    LaunchedEffect(Unit) {
        com.example.fitness.core.perf.AppPerfTracker.finish("TODAYS_WORKOUT_TO_WORKOUT", "Workout screen rendered")
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
                .navigationBarsPadding()
        ) {
            // Top Navigation & Step Indicator Bar (unless complete)
            if (currentPhase != SessionPhase.COMPLETE) {
                WorkoutSessionTopBar(
                    currentPhase = currentPhase,
                    onBackClick = { handleBack() }
                )
            }

            // Phase Content with Smooth Slide Transitions
            AnimatedContent(
                targetState = currentPhase,
                transitionSpec = {
                    (slideInHorizontally(
                        animationSpec = tween(320, easing = FastOutSlowInEasing),
                        initialOffsetX = { width -> (width * 0.35f).toInt() }
                    ) + fadeIn(animationSpec = tween(280))) togetherWith
                            (slideOutHorizontally(
                                animationSpec = tween(260, easing = FastOutSlowInEasing),
                                targetOffsetX = { width -> -(width * 0.25f).toInt() }
                            ) + fadeOut(animationSpec = tween(200)))
                },
                modifier = Modifier.weight(1f)
            ) { phase ->
                when (phase) {
                    SessionPhase.WARM_UP -> {
                        WarmUpPhaseContent(
                            workout = workout,
                            onStartExercise = {
                                ActiveWorkoutSessionManager.startExercise(initialPhase)
                            },
                            onViewDetail = { id -> detailExerciseId = id }
                        )
                    }

                    SessionPhase.MAIN_WORKOUT -> {
                        val currentExercise = workout.exercises.getOrNull(currentExerciseIndex)
                        val currentExId = currentExercise?.id ?: ""
                        val currentConfig = if (currentExercise != null) {
                            customExerciseConfigs.getOrPut(currentExId) {
                                CustomExerciseConfig.fromPlanExercise(currentExercise)
                            }
                        } else null

                        val totalSetsForCurrentExercise = currentConfig?.sets
                            ?: (currentExercise?.sets?.filter { it.isDigit() }?.toIntOrNull() ?: 3)

                        if (currentExercise != null && currentConfig != null) {
                            MainWorkoutPhaseContent(
                                workout = workout,
                                currentExerciseIndex = currentExerciseIndex,
                                currentSet = currentSetIndex,
                                totalSets = totalSetsForCurrentExercise,
                                customConfig = currentConfig,
                                onUpdateConfig = { updated ->
                                    customExerciseConfigs[updated.exerciseId] = updated
                                    if (currentSetIndex > updated.sets) {
                                        ActiveWorkoutSessionManager.currentSetIndex = updated.sets
                                    }
                                    if (isResting) {
                                        restSecondsLeft = updated.restSeconds
                                    }
                                },
                                onResetConfig = {
                                    val defaultCfg = CustomExerciseConfig.fromPlanExercise(currentExercise)
                                    customExerciseConfigs[currentExercise.id] = defaultCfg
                                    if (currentSetIndex > defaultCfg.sets) {
                                        ActiveWorkoutSessionManager.currentSetIndex = defaultCfg.sets
                                    }
                                    if (isResting) {
                                        restSecondsLeft = defaultCfg.restSeconds
                                    }
                                },
                                isResting = isResting,
                                restSecondsLeft = restSecondsLeft,
                                onSkipRest = { isResting = false },
                                onCompleteSet = {
                                    val currentEx = workout.exercises.getOrNull(currentExerciseIndex)
                                    val exId = currentEx?.id ?: ""
                                    val effectiveConfig = if (currentEx != null) {
                                        customExerciseConfigs[exId] ?: CustomExerciseConfig.fromPlanExercise(currentEx)
                                    } else null
                                    val effectiveReps = effectiveConfig?.activeRepsOrDurationCount ?: 10

                                    if (exId.isNotEmpty()) {
                                        val setsDone = (completedSetsByExercise[exId] ?: 0) + 1
                                        completedSetsByExercise[exId] = setsDone
                                        val repsList = completedRepsByExercise.getOrPut(exId) { mutableListOf() }
                                        repsList.add(effectiveReps)
                                    }

                                    if (currentSetIndex < totalSetsForCurrentExercise) {
                                        ActiveWorkoutSessionManager.currentSetIndex++
                                        isResting = true
                                        val effectiveRest = effectiveConfig?.restSeconds
                                            ?: com.example.fitness.feature.workout.model.CustomExerciseConfig.parseDefaultRest(currentEx?.rest ?: "60s")
                                        restSecondsLeft = effectiveRest
                                        UserSession.totalSetsCompleted += 1
                                        UserSession.totalRepsCompleted += effectiveReps
                                    } else {
                                        // Move to next exercise or finish
                                        UserSession.totalSetsCompleted += 1
                                        UserSession.totalRepsCompleted += effectiveReps
                                        if (currentExerciseIndex < workout.exercises.size - 1) {
                                            ActiveWorkoutSessionManager.currentExerciseIndex++
                                            ActiveWorkoutSessionManager.currentSetIndex = 1
                                            isResting = false
                                        } else {
                                            // Main workout finished -> Move to Cool-down
                                            ActiveWorkoutSessionManager.currentPhase = SessionPhase.COOL_DOWN
                                        }
                                    }
                                },
                                onViewDetail = { id -> detailExerciseId = id }
                            )
                        }
                    }

                    SessionPhase.COOL_DOWN -> {
                        CoolDownPhaseContent(
                            workout = workout,
                            isSaving = isSavingSession,
                            onCompleteCoolDown = {
                                if (currentPhase == SessionPhase.COMPLETE || isSavingSession) return@CoolDownPhaseContent

                                val performanceRecords = buildPerformanceRecords()
                                val totalPrescribedSets = if (workout.isRestDay) 0 else workout.exercises.sumOf { ex ->
                                    val config = customExerciseConfigs[ex.id] ?: CustomExerciseConfig.fromPlanExercise(ex)
                                    config.sets
                                }
                                val totalExCount = if (workout.isRestDay) 0 else workout.exercises.size
                                val actuallyCompletedExCount = if (workout.isRestDay) 0 else performanceRecords.count { it.completed }
                                val actuallyCompletedSets = if (workout.isRestDay) 0 else performanceRecords.sumOf { it.completedSets }

                                val currentUser = try { com.example.fitness.core.firebase.FitnessFirebase.auth.currentUser } catch (_: Exception) { null }
                                val authUid = currentUser?.uid ?: com.example.fitness.core.session.UserSession.currentUserId ?: ""

                                platformLog("WorkoutHistoryDebug", "[WorkoutHistoryDebug] Workout completion triggered")
                                platformLog("WorkoutHistoryDebug", "[WorkoutHistoryDebug] sessionId = $stableSessionId")
                                platformLog("WorkoutHistoryDebug", "[WorkoutHistoryDebug] authenticatedUid = $authUid")
                                platformLog("WorkoutHistoryDebug", "[WorkoutHistoryDebug] programId = $effectiveProgramId")
                                platformLog("WorkoutHistoryDebug", "[WorkoutHistoryDebug] dayNumber = ${workout.dayNumber}")
                                platformLog("WorkoutHistoryDebug", "[WorkoutHistoryDebug] prescribedExerciseCount = $totalExCount")
                                platformLog("WorkoutHistoryDebug", "[WorkoutHistoryDebug] completedExerciseCount = $actuallyCompletedExCount")

                                val sessionStartTime = ActiveWorkoutSessionManager.workoutStartedAt
                                    ?: (com.example.fitness.getCurrentEpochMillis() - (ActiveWorkoutSessionManager.totalSecondsElapsed * 1000L))

                                isSavingSession = true
                                coroutineScope.launch {
                                    val result = WorkoutProgressService.saveAndPersistWorkoutSession(
                                        workoutId = workout.id,
                                        dayNumber = workout.dayNumber,
                                        workoutTitle = workout.workoutTitle,
                                        targetMuscle = workout.focus.split(",").map { it.trim() },
                                        experienceLevel = UserSession.experienceLevel.id,
                                        startedAt = sessionStartTime,
                                        completedAt = com.example.fitness.getCurrentEpochMillis(),
                                        durationMinutes = (ActiveWorkoutSessionManager.totalSecondsElapsed / 60).coerceAtLeast(1),
                                        totalExercises = totalExCount,
                                        completedExercises = actuallyCompletedExCount,
                                        totalSets = totalPrescribedSets,
                                        completedSets = actuallyCompletedSets,
                                        warmUpCompleted = !workout.isRestDay,
                                        mainWorkoutCompleted = if (workout.isRestDay) true else (totalExCount > 0 && actuallyCompletedExCount >= totalExCount),
                                        coolDownCompleted = true,
                                        programId = effectiveProgramId,
                                        programName = resolvedProgramName,
                                        userId = authUid,
                                        exercises = performanceRecords,
                                        sessionId = stableSessionId,
                                        scope = coroutineScope
                                    )
                                    isSavingSession = false
                                    completedSessionRecord = result.getOrNull()
                                        ?: WorkoutProgressService.workoutHistory.find { it.sessionId == stableSessionId }
                                    ActiveWorkoutSessionManager.completeWorkout()
                                }
                            },
                            onViewDetail = { id -> detailExerciseId = id }
                        )
                    }


                    SessionPhase.COMPLETE -> {
                        val liveSessionRecord = WorkoutProgressService.workoutHistory.find { it.sessionId == completedSessionRecord?.sessionId } ?: completedSessionRecord
                        WorkoutCompletePhaseContent(
                            workout = workout,
                            totalTime = ActiveWorkoutSessionManager.formattedTime,
                            sessionRecord = liveSessionRecord,
                            onRetrySync = {
                                completedSessionRecord?.let { rec ->
                                    coroutineScope.launch {
                                        val success = WorkoutProgressService.retryFailedSync(rec.sessionId)
                                        if (success) {
                                            completedSessionRecord = rec.copy(syncStatus = "synced")
                                        }
                                    }
                                }
                            },
                            onViewProgress = {
                                ActiveWorkoutSessionManager.resetSession()
                                navigator.switchTab(NavigationTab.PROGRESS)
                            },
                            onBackToHome = {
                                ActiveWorkoutSessionManager.resetSession()
                                navigator.switchTab(NavigationTab.HOME)
                            }
                        )
                    }
                }
            }
        }

        // Quit Confirmation Dialog
        if (showQuitDialog) {
            val anyProgressRecorded = completedSetsByExercise.values.any { it > 0 }
            AlertDialog(
                onDismissRequest = { showQuitDialog = false },
                containerColor = Color(0xFF07090C),
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "Leave Workout?",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = if (anyProgressRecorded) {
                            "You have completed exercise sets in this workout. Would you like to save your progress before leaving?"
                        } else {
                            "If you quit now, no workout progress will be recorded."
                        },
                        color = Color(0xFF9BAAB4),
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (anyProgressRecorded) {
                            PrimaryButton(
                                text = "Save & Exit",
                                onClick = {
                                    showQuitDialog = false
                                    val performanceRecords = buildPerformanceRecords()
                                    val totalExCount = if (workout.isRestDay) 0 else workout.exercises.size
                                    val actuallyCompletedExCount = if (workout.isRestDay) 0 else performanceRecords.count { it.completed }
                                    val totalPrescribedSets = if (workout.isRestDay) 0 else workout.exercises.sumOf { ex ->
                                        ex.sets.filter { it.isDigit() }.toIntOrNull() ?: 3
                                    }
                                    val actuallyCompletedSets = if (workout.isRestDay) 0 else performanceRecords.sumOf { it.completedSets }
                                    val currentUser = try { com.example.fitness.core.firebase.FitnessFirebase.auth.currentUser } catch (_: Exception) { null }
                                    val authUid = currentUser?.uid ?: com.example.fitness.core.session.UserSession.currentUserId ?: ""

                                    val sessionStartTime = ActiveWorkoutSessionManager.workoutStartedAt
                                        ?: (com.example.fitness.getCurrentEpochMillis() - (ActiveWorkoutSessionManager.totalSecondsElapsed * 1000L))

                                    coroutineScope.launch {
                                        WorkoutProgressService.saveAndPersistWorkoutSession(
                                            workoutId = workout.id,
                                            dayNumber = workout.dayNumber,
                                            workoutTitle = workout.workoutTitle,
                                            targetMuscle = workout.focus.split(",").map { it.trim() },
                                            experienceLevel = UserSession.experienceLevel.id,
                                            startedAt = sessionStartTime,
                                            completedAt = com.example.fitness.getCurrentEpochMillis(),
                                            durationMinutes = (ActiveWorkoutSessionManager.totalSecondsElapsed / 60).coerceAtLeast(1),
                                            totalExercises = totalExCount,
                                            completedExercises = actuallyCompletedExCount,
                                            totalSets = totalPrescribedSets,
                                            completedSets = actuallyCompletedSets,
                                            warmUpCompleted = currentPhase != SessionPhase.WARM_UP,
                                            mainWorkoutCompleted = false,
                                            coolDownCompleted = false,
                                            programId = effectiveProgramId,
                                            programName = resolvedProgramName,
                                            userId = authUid,
                                            exercises = performanceRecords,
                                            sessionId = stableSessionId,
                                            scope = coroutineScope
                                        )
                                        ActiveWorkoutSessionManager.completeWorkout()
                                        navigator.pop()
                                    }
                                },
                                height = 42.dp
                            )
                        }
                        PrimaryButton(
                            text = "Resume",
                            onClick = { showQuitDialog = false },
                            height = 42.dp,
                            modifier = Modifier.width(90.dp)
                        )
                    }
                },
                dismissButton = {
                    Surface(
                        onClick = {
                            showQuitDialog = false
                            ActiveWorkoutSessionManager.resetSession()
                            navigator.pop()
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1B2329),
                        modifier = Modifier.height(42.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 14.dp)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (anyProgressRecorded) "Discard" else "Quit",
                                color = Color(0xFFFF5252),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        }

        // Exercise Detail Inspection Modal Sheet (non-blocking)
        if (detailExerciseId != null) {
            val inspectedExercise = workout.exercises.find { it.id == detailExerciseId }
            ExerciseDetailModalSheet(
                exerciseId = detailExerciseId!!,
                prescribedSets = inspectedExercise?.sets ?: "",
                prescribedReps = inspectedExercise?.reps ?: "",
                prescribedRest = inspectedExercise?.rest ?: "",
                onDismiss = { detailExerciseId = null }
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// 1. TOP BAR COMPONENT
// -----------------------------------------------------------------------------------------

@Composable
private fun WorkoutSessionTopBar(
    currentPhase: SessionPhase,
    formattedTimeProvider: () -> String = { ActiveWorkoutSessionManager.formattedTime },
    onBackClick: () -> Unit
) {
    val phaseTitle = when (currentPhase) {
        SessionPhase.WARM_UP -> "1. WARM-UP"
        SessionPhase.MAIN_WORKOUT -> "2. MAIN WORKOUT"
        SessionPhase.COOL_DOWN -> "3. COOL-DOWN"
        SessionPhase.COMPLETE -> "COMPLETE"
    }

    val phaseProgress = when (currentPhase) {
        SessionPhase.WARM_UP -> 0.25f
        SessionPhase.MAIN_WORKOUT -> 0.65f
        SessionPhase.COOL_DOWN -> 0.90f
        SessionPhase.COMPLETE -> 1.0f
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularIconButton(
                icon = FitnessIcons.ArrowBack,
                onClick = onBackClick,
                contentDescription = "Back"
            )

            // Phase indicator badge
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0D1E0B),
                border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.5f))
            ) {
                Text(
                    text = phaseTitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = FitTheme.colors.primary,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
            }

            // Elapsed Timer Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F1519),
                border = BorderStroke(1.dp, Color(0xFF1F2930))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = FitnessIcons.Clock,
                        contentDescription = null,
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedTimeProvider(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Phase Progress Bar
        LinearProgressIndicator(
            progress = { phaseProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = FitTheme.colors.primary,
            trackColor = Color(0xFF141C22)
        )
    }
}

// -----------------------------------------------------------------------------------------
// 2. WARM-UP PHASE CONTENT
// -----------------------------------------------------------------------------------------

@Composable
private fun WarmUpPhaseContent(
    workout: ThirtyDayWorkout,
    onStartExercise: () -> Unit,
    onViewDetail: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Warm-Up Routine",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Prepare your body for ${workout.workoutTitle} • ${workout.duration}",
            fontSize = 13.sp,
            color = Color(0xFF8E9BA4)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Duration & Prep Banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF07090C),
            border = BorderStroke(1.dp, Color(0x33A8FF3E)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF141F10),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "🔥", fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Warm-Up Duration: ~5 - 7 Mins",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Activates heart rate, joint fluid & muscle elasticity",
                        fontSize = 12.sp,
                        color = Color(0xFF8E9BA4)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "EXERCISES (${workout.warmUp.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = FitTheme.colors.primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Exercise list
        workout.warmUp.forEachIndexed { index, warmItem ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF07090C),
                border = BorderStroke(1.dp, Color(0xFF19232A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF121B21),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = FitTheme.colors.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = warmItem.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF152028),
                                    modifier = Modifier.widthIn(max = 135.dp)
                                ) {
                                    Text(
                                        text = warmItem.durationOrReps,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = FitTheme.colors.primary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                CircularIconButton(
                                    icon = FitnessIcons.Info,
                                    onClick = { onViewDetail(warmItem.id) },
                                    contentDescription = "View warm-up exercise details",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = warmItem.instructions,
                            fontSize = 12.sp,
                            color = Color(0xFF8E9BA4),
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Big action button: [START EXERCISE]
        PrimaryButton(
            text = "START EXERCISE",
            onClick = onStartExercise,
            height = 54.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// -----------------------------------------------------------------------------------------
// 3. MAIN WORKOUT PHASE CONTENT
// -----------------------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MainWorkoutPhaseContent(
    workout: ThirtyDayWorkout,
    currentExerciseIndex: Int,
    currentSet: Int,
    totalSets: Int,
    customConfig: CustomExerciseConfig,
    onUpdateConfig: (CustomExerciseConfig) -> Unit,
    onResetConfig: () -> Unit,
    isResting: Boolean,
    restSecondsLeft: Int,
    onSkipRest: () -> Unit,
    onCompleteSet: () -> Unit,
    onViewDetail: (String) -> Unit
) {
    val exercise = workout.exercises.getOrElse(currentExerciseIndex) {
        workout.exercises.firstOrNull() ?: PlanExercise(
            id = "fallback",
            name = workout.workoutTitle.ifBlank { "Exercise" },
            sets = "3 Sets",
            reps = "10-12 Reps",
            rest = "60s",
            equipment = "Bodyweight",
            targetMuscle = workout.focus,
            videoUrl = "",
            instructions = "Perform each movement with smooth control.",
            imageRes = workout.imageRes
        )
    }
    val canonical = remember(exercise.id, com.example.fitness.feature.exercise.data.SampleExerciseData.canonicalRevision) {
        com.example.fitness.feature.exercise.data.SampleExerciseData.findCanonical(exercise.id, exercise.name)
    }

    var showCustomizeDialog by remember(exercise.id) { mutableStateOf(false) }

    if (showCustomizeDialog) {
        ExerciseCustomizationDialog(
            exercise = exercise,
            config = customConfig,
            onSave = { updated ->
                onUpdateConfig(updated)
                showCustomizeDialog = false
            },
            onReset = {
                onResetConfig()
                showCustomizeDialog = false
            },
            onDismiss = { showCustomizeDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        // Top Header: Exercise Index & Guide action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EXERCISE ${currentExerciseIndex + 1} OF ${workout.exercises.size}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = FitTheme.colors.primary
            )

            Surface(
                onClick = { onViewDetail(exercise.id) },
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF131D24),
                border = BorderStroke(1.dp, Color(0xFF22313C)),
                modifier = Modifier.semantics { contentDescription = "View exercise guide" }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = FitnessIcons.Info,
                        contentDescription = null,
                        tint = FitTheme.colors.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Guide",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FitTheme.colors.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 1. Video Player Hero
        WorkoutVideoPlayer(
            exercise = exercise.toExerciseItem(),
            autoPlay = true,
            interactiveControls = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Exercise Title
        Text(
            text = exercise.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Target Muscles
        val primaryMuscle = canonical?.primaryMuscle?.ifBlank { exercise.targetMuscle } ?: exercise.targetMuscle
        val muscleGroups = canonical?.muscleGroups ?: emptyList()
        val secondaryMuscles = canonical?.secondaryMuscles ?: emptyList()

        if (primaryMuscle.isNotBlank() || muscleGroups.isNotEmpty() || secondaryMuscles.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (primaryMuscle.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F1E0C),
                        border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.7f))
                    ) {
                        Text(
                            text = "Primary: $primaryMuscle",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            softWrap = true
                        )
                    }
                }
                muscleGroups.forEach { group ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10171D),
                        border = BorderStroke(1.dp, Color(0xFF22303C))
                    ) {
                        Text(
                            text = group,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            softWrap = true
                        )
                    }
                }
                secondaryMuscles.forEach { sec ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0D1318),
                        border = BorderStroke(1.dp, Color(0xFF1E2830))
                    ) {
                        Text(
                            text = "+ $sec",
                            fontSize = 11.sp,
                            color = Color(0xFF8E9BA4),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            softWrap = true
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 4. Equipment
        val equipmentList = canonical?.equipment?.ifEmpty { null }
            ?: exercise.equipment.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (equipmentList.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "EQUIPMENT:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8E9BA4),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    equipmentList.forEach { eq ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF141C22),
                            border = BorderStroke(0.8.dp, Color(0xFF23303A))
                        ) {
                            Text(
                                text = eq,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                softWrap = true
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 5. Targets & Sets / Reps / Rest Badges Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "TARGETS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF8E9BA4)
                )
                if (customConfig.isCustomized) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = FitTheme.colors.primary.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, FitTheme.colors.primary.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "CUSTOMIZED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FitTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Surface(
                onClick = { showCustomizeDialog = true },
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF101915),
                border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.6f)),
                modifier = Modifier.semantics { contentDescription = "Customize Sets Reps and Rest" }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✎",
                        fontSize = 11.sp,
                        color = FitTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Customize",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FitTheme.colors.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                onClick = { showCustomizeDialog = true },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF07090C),
                border = BorderStroke(1.dp, if (customConfig.isCustomized) FitTheme.colors.primary.copy(alpha = 0.5f) else Color(0xFF19232A)),
                modifier = Modifier.weight(1f).semantics { contentDescription = "Customize Sets" }
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "SETS", fontSize = 10.sp, color = Color(0xFF8E9BA4), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "✎", fontSize = 9.sp, color = FitTheme.colors.primary.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = customConfig.displaySets, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }

            Surface(
                onClick = { showCustomizeDialog = true },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF07090C),
                border = BorderStroke(1.dp, if (customConfig.isCustomized) FitTheme.colors.primary.copy(alpha = 0.5f) else Color(0xFF19232A)),
                modifier = Modifier.weight(1f).semantics { contentDescription = "Customize Reps or Duration" }
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (customConfig.isDurationBased) "DURATION" else "REPS",
                            fontSize = 10.sp,
                            color = Color(0xFF8E9BA4),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "✎", fontSize = 9.sp, color = FitTheme.colors.primary.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = customConfig.displayReps, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }

            Surface(
                onClick = { showCustomizeDialog = true },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF07090C),
                border = BorderStroke(1.dp, if (customConfig.isCustomized) FitTheme.colors.primary.copy(alpha = 0.5f) else Color(0xFF19232A)),
                modifier = Modifier.weight(1f).semantics { contentDescription = "Customize Rest" }
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "REST", fontSize = 10.sp, color = Color(0xFF8E9BA4), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "✎", fontSize = 9.sp, color = FitTheme.colors.primary.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = customConfig.displayRest, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = FitTheme.colors.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Rest Interval Banner (when user is resting between sets)
        AnimatedVisibility(
            visible = isResting,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -10 }),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0B1F08),
                border = BorderStroke(1.dp, FitTheme.colors.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⏳", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Resting: ${restSecondsLeft}s",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = FitTheme.colors.primary
                            )
                            Text(
                                text = "Catch your breath for next set",
                                fontSize = 11.sp,
                                color = Color(0xFF8E9BA4)
                            )
                        }
                    }

                    Surface(
                        onClick = onSkipRest,
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF142E0C),
                        border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "Skip Rest",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Set Completion Tracker
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF07090C),
            border = BorderStroke(1.dp, Color(0xFF19232A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SET TRACKER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF8E9BA4)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..totalSets).forEach { setNum ->
                        val isFinished = setNum < currentSet
                        val isCurrent = setNum == currentSet

                        val bg = when {
                            isFinished -> FitTheme.colors.primary
                            isCurrent -> Color(0xFF162D0F)
                            else -> Color(0xFF11181D)
                        }

                        val textColor = when {
                            isFinished -> Color(0xFF000000)
                            isCurrent -> FitTheme.colors.primary
                            else -> Color(0xFF6E7E8A)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(bg)
                                    .border(
                                        1.dp,
                                        if (isCurrent) FitTheme.colors.primary else Color.Transparent,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isFinished) {
                                    Icon(
                                        imageVector = FitnessIcons.Check,
                                        contentDescription = "Done",
                                        tint = Color(0xFF000000),
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Text(
                                        text = "$setNum",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Set $setNum",
                                fontSize = 10.sp,
                                color = if (isCurrent) FitTheme.colors.primary else Color(0xFF7A8B97)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Instructions (if non-empty)
        val instructionsList = canonical?.instructions ?: if (exercise.instructions.isNotBlank()) listOf(exercise.instructions) else emptyList()
        if (instructionsList.isNotEmpty()) {
            GuidanceSectionCard(title = "INSTRUCTIONS", accentColor = FitTheme.colors.primary) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    instructionsList.forEach { ins ->
                        Text(text = "• $ins", fontSize = 13.sp, color = Color(0xFFCAD7E0), lineHeight = 18.sp, softWrap = true)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 7. Setup (if non-empty)
        if (canonical?.setupSteps?.isNotEmpty() == true) {
            GuidanceSectionCard(title = "SETUP", accentColor = Color(0xFF38BDF8)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    canonical.setupSteps.forEachIndexed { idx, s ->
                        Text(text = "${idx + 1}. $s", fontSize = 13.sp, color = Color(0xFFCAD7E0), lineHeight = 18.sp, softWrap = true)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 8. Execution (if non-empty)
        if (canonical?.executionSteps?.isNotEmpty() == true) {
            GuidanceSectionCard(title = "EXECUTION", accentColor = FitTheme.colors.primary) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    canonical.executionSteps.forEachIndexed { idx, s ->
                        Text(text = "${idx + 1}. $s", fontSize = 13.sp, color = Color(0xFFCAD7E0), lineHeight = 18.sp, softWrap = true)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 9. Breathing (if non-empty)
        if (!canonical?.breathing.isNullOrBlank()) {
            GuidanceSectionCard(title = "BREATHING", accentColor = Color(0xFFA78BFA)) {
                Text(text = "🫁 ${canonical.breathing}", fontSize = 13.sp, color = Color(0xFFCAD7E0), lineHeight = 18.sp, softWrap = true)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 10. Form Tips (if non-empty)
        if (canonical?.formTips?.isNotEmpty() == true) {
            GuidanceSectionCard(title = "FORM TIPS", accentColor = FitTheme.colors.primary) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    canonical.formTips.forEach { tip ->
                        Text(text = "✓ $tip", fontSize = 13.sp, color = Color(0xFFCAD7E0), lineHeight = 18.sp, softWrap = true)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 11. Common Mistakes (if non-empty)
        if (canonical?.commonMistakes?.isNotEmpty() == true) {
            GuidanceSectionCard(title = "COMMON MISTAKES", accentColor = Color(0xFFFF6B6B)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    canonical.commonMistakes.forEach { mis ->
                        Text(text = "⚠ $mis", fontSize = 13.sp, color = Color(0xFFE2E8F0), lineHeight = 18.sp, softWrap = true)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Complete Set / Next Action Button
        val isLastExercise = currentExerciseIndex == workout.exercises.size - 1
        val isLastSet = currentSet == totalSets
        val buttonText = when {
            isLastSet && isLastExercise -> "Complete Workout & Cool-Down >"
            isLastSet -> "Complete Exercise (${currentExerciseIndex + 1}/${workout.exercises.size}) >"
            else -> "Complete Set $currentSet of $totalSets"
        }

        PrimaryButton(
            text = buttonText,
            onClick = onCompleteSet,
            height = 54.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun GuidanceSectionCard(
    title: String,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF07090C),
        border = BorderStroke(1.dp, Color(0xFF19232A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    letterSpacing = 0.8.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

// -----------------------------------------------------------------------------------------
// EXERCISE CUSTOMIZATION DIALOG & STEPPERS
// -----------------------------------------------------------------------------------------

@Composable
private fun ExerciseCustomizationDialog(
    exercise: PlanExercise,
    config: CustomExerciseConfig,
    onSave: (CustomExerciseConfig) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var tempSets by remember(config) { mutableIntStateOf(config.sets) }
    val isDuration = config.isDurationBased
    var tempDuration by remember(config) {
        mutableIntStateOf(config.durationSeconds ?: CustomExerciseConfig.parseDefaultRepsOrDuration(config.rawRepsText, true))
    }
    var tempReps by remember(config) {
        mutableIntStateOf(config.repsCount ?: CustomExerciseConfig.parseDefaultRepsOrDuration(config.rawRepsText, false))
    }
    var tempRest by remember(config) { mutableIntStateOf(config.restSeconds) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color(0xFF0C1014),
        titleContentColor = Color.White,
        textContentColor = Color(0xFFCBD5E1),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CUSTOMIZE EXERCISE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = FitTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = exercise.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                    CircularIconButton(
                        icon = FitnessIcons.Close,
                        contentDescription = "Close",
                        onClick = onDismiss,
                        size = 32.dp,
                        backgroundColor = Color(0xFF161E24),
                        tint = Color.White
                    )
                }

                // Stepper 1: SETS
                CustomizationStepper(
                    title = "SETS",
                    subtitle = "1 – 20 sets",
                    displayValue = "$tempSets Sets",
                    canDecrement = tempSets > 1,
                    canIncrement = tempSets < 20,
                    onDecrement = { if (tempSets > 1) tempSets-- },
                    onIncrement = { if (tempSets < 20) tempSets++ },
                    presets = listOf(2, 3, 4, 5, 6),
                    selectedPreset = tempSets,
                    onSelectPreset = { tempSets = it }
                )

                // Stepper 2: DURATION or REPS
                if (isDuration) {
                    CustomizationStepper(
                        title = "DURATION",
                        subtitle = "5 – 600 seconds",
                        displayValue = "$tempDuration sec",
                        canDecrement = tempDuration > 5,
                        canIncrement = tempDuration < 600,
                        onDecrement = {
                            val step = if (tempDuration > 30) 15 else 5
                            tempDuration = (tempDuration - step).coerceAtLeast(5)
                        },
                        onIncrement = {
                            val step = if (tempDuration >= 30) 15 else 5
                            tempDuration = (tempDuration + step).coerceAtMost(600)
                        },
                        presets = listOf(30, 45, 60, 75, 90),
                        presetLabel = { "${it}s" },
                        selectedPreset = tempDuration,
                        onSelectPreset = { tempDuration = it }
                    )
                } else {
                    CustomizationStepper(
                        title = "REPS",
                        subtitle = "1 – 100 reps",
                        displayValue = "$tempReps Reps",
                        canDecrement = tempReps > 1,
                        canIncrement = tempReps < 100,
                        onDecrement = { if (tempReps > 1) tempReps-- },
                        onIncrement = { if (tempReps < 100) tempReps++ },
                        presets = listOf(6, 8, 10, 12, 15, 20),
                        selectedPreset = tempReps,
                        onSelectPreset = { tempReps = it }
                    )
                }

                // Stepper 3: REST TIME
                CustomizationStepper(
                    title = "REST TIME",
                    subtitle = "5 – 300 seconds",
                    displayValue = "${tempRest}s",
                    valueColor = FitTheme.colors.primary,
                    canDecrement = tempRest > 15,
                    canIncrement = tempRest < 300,
                    onDecrement = { tempRest = (tempRest - 15).coerceAtLeast(5) },
                    onIncrement = { tempRest = (tempRest + 15).coerceAtMost(300) },
                    presets = listOf(30, 45, 60, 90, 120, 150),
                    presetLabel = { "${it}s" },
                    selectedPreset = tempRest,
                    onSelectPreset = { tempRest = it }
                )

                // Actions: Reset to Default & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = {
                            onReset()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF161E24),
                        border = BorderStroke(1.dp, Color(0xFF26333D)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Reset Default",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }

                    PrimaryButton(
                        text = "Save Changes",
                        onClick = {
                            val updated = config.copy(
                                sets = tempSets,
                                repsCount = if (!isDuration) tempReps else null,
                                durationSeconds = if (isDuration) tempDuration else null,
                                restSeconds = tempRest,
                                isCustomized = true
                            )
                            onSave(updated)
                            onDismiss()
                        },
                        height = 44.dp,
                        modifier = Modifier.weight(1.3f)
                    )
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun CustomizationStepper(
    title: String,
    subtitle: String,
    displayValue: String,
    valueColor: Color = Color.White,
    canDecrement: Boolean,
    canIncrement: Boolean,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    presets: List<Int>,
    presetLabel: (Int) -> String = { it.toString() },
    selectedPreset: Int,
    onSelectPreset: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF080C0F), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF162028), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF8E9BA4), letterSpacing = 0.5.sp)
            Text(text = subtitle, fontSize = 10.sp, color = Color(0xFF5E6D77))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Stepper row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // [-] button
            Surface(
                onClick = onDecrement,
                enabled = canDecrement,
                shape = CircleShape,
                color = if (canDecrement) Color(0xFF152028) else Color(0xFF0F1418),
                border = BorderStroke(1.dp, if (canDecrement) Color(0xFF283642) else Color(0xFF1A2228)),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "−", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (canDecrement) Color.White else Color(0xFF4A5560))
                }
            }

            // Central display
            Text(
                text = displayValue,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
                textAlign = TextAlign.Center
            )

            // [+] button
            Surface(
                onClick = onIncrement,
                enabled = canIncrement,
                shape = CircleShape,
                color = if (canIncrement) Color(0xFF152028) else Color(0xFF0F1418),
                border = BorderStroke(1.dp, if (canIncrement) FitTheme.colors.primary.copy(alpha = 0.6f) else Color(0xFF1A2228)),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (canIncrement) FitTheme.colors.primary else Color(0xFF4A5560))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Preset quick chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presets.forEach { p ->
                val isSel = p == selectedPreset
                Surface(
                    onClick = { onSelectPreset(p) },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSel) FitTheme.colors.primary else Color(0xFF121920),
                    border = BorderStroke(1.dp, if (isSel) FitTheme.colors.primary else Color(0xFF1E2A34)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = presetLabel(p),
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSel) Color.Black else Color(0xFFCBD5E1),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 4. COOL-DOWN PHASE CONTENT
// -----------------------------------------------------------------------------------------

@Composable
private fun CoolDownPhaseContent(
    workout: ThirtyDayWorkout,
    isSaving: Boolean = false,
    onCompleteCoolDown: () -> Unit,
    onViewDetail: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (workout.isRestDay) "Active Recovery Session" else "Cool-Down & Recovery",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (workout.isRestDay) "Active recovery and joint restoration" else "Stretch and restore ${workout.focus}",
            fontSize = 13.sp,
            color = Color(0xFF8E9BA4)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Recovery Banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF07090C),
            border = BorderStroke(1.dp, Color(0x3338BDF8)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF0C2430),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "🧘", fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Duration: ~5 Mins Recovery",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Reduces muscle stiffness and lowers cortisol levels",
                        fontSize = 12.sp,
                        color = Color(0xFF8E9BA4)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "STRETCHING MOVEMENTS (${workout.coolDown.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color(0xFF38BDF8)
        )

        Spacer(modifier = Modifier.height(10.dp))

        workout.coolDown.forEachIndexed { index, cdItem ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF07090C),
                border = BorderStroke(1.dp, Color(0xFF19232A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF121B21),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = cdItem.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF152028),
                                    modifier = Modifier.widthIn(max = 135.dp)
                                ) {
                                    Text(
                                        text = cdItem.durationOrReps,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF38BDF8),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                CircularIconButton(
                                    icon = FitnessIcons.Info,
                                    onClick = { onViewDetail(cdItem.id) },
                                    contentDescription = "View cool-down exercise details",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = cdItem.instructions,
                            fontSize = 12.sp,
                            color = Color(0xFF8E9BA4),
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Complete button
        PrimaryButton(
            text = if (isSaving) "Saving Workout..." else if (workout.isRestDay) "Complete Recovery" else "Complete Workout",
            onClick = onCompleteCoolDown,
            enabled = !isSaving,
            isLoading = isSaving,
            height = 54.dp,
            modifier = Modifier.fillMaxWidth()
        )

    }
}

// -----------------------------------------------------------------------------------------
// 5. WORKOUT COMPLETE PHASE CONTENT
// -----------------------------------------------------------------------------------------

@Composable
private fun WorkoutCompletePhaseContent(
    workout: ThirtyDayWorkout,
    totalTime: String,
    sessionRecord: WorkoutSessionRecord? = null,
    onRetrySync: () -> Unit = {},
    onViewProgress: () -> Unit,
    onBackToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Offline / Sync Status Notification Banner
        if (sessionRecord?.syncStatus == "failed_sync") {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E100C),
                border = BorderStroke(1.dp, Color(0xFF552211)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cloud Sync Pending",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF8866)
                        )
                        Text(
                            text = "Session is safely saved locally.",
                            fontSize = 11.sp,
                            color = Color(0xFFB0A8A4)
                        )
                    }
                    Surface(
                        onClick = onRetrySync,
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF3B1A12)
                    ) {
                        Text(
                            text = "Retry Sync",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF8866),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // Celebratory Badge
        Surface(
            shape = CircleShape,
            color = Color(0xFF0F260A),
            border = BorderStroke(2.dp, FitTheme.colors.primary),
            modifier = Modifier.size(90.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "🏆", fontSize = 42.sp)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "WORKOUT COMPLETE!",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = FitTheme.colors.primary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "You finished Day ${workout.dayNumber}: ${workout.workoutTitle}",
            fontSize = 14.sp,
            color = Color(0xFFCAD7E0),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 4 Summary Metrics Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryMetricBadge(label = "TIME", value = totalTime, icon = "⏱️", modifier = Modifier.weight(1f))
            SummaryMetricBadge(label = "STREAK", value = "${UserSession.workoutStreak}d 🔥", icon = "🔥", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryMetricBadge(label = "SETS", value = "${workout.exercises.size * 3}", icon = "💪", modifier = Modifier.weight(1f))
            SummaryMetricBadge(label = "30-DAY PLAN", value = "${(UserSession.get30DayProgressPercentage() * 100).toInt()}%", icon = "📈", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Primary: View Progress
        PrimaryButton(
            text = "View Progress & Analytics",
            onClick = onViewProgress,
            height = 54.dp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Secondary: Back to Home
        Surface(
            onClick = onBackToHome,
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF07090C),
            border = BorderStroke(1.dp, Color(0xFF1B2329)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Back to Home",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFCBD5E1)
                )
            }
        }
    }
}

@Composable
private fun SummaryMetricBadge(
    label: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF07090C),
        border = BorderStroke(1.dp, Color(0xFF19232A)),
        modifier = modifier.height(78.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7A8B97),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}
