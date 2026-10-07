package com.example.fitness.feature.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import com.example.fitness.core.components.CircularIconButton
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.components.NavigationTab
import com.example.fitness.core.components.PrimaryButton
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.navigation.LocalNavigator
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.exercise.data.FavoriteExerciseRepository
import com.example.fitness.feature.progress.data.WorkoutProgressService
import com.example.fitness.feature.progress.model.ExercisePerformanceRecord
import com.example.fitness.feature.workout.data.WorkoutRepository
import com.example.fitness.feature.workout.model.ExerciseItem
import com.example.fitness.feature.workout.model.ExperienceLevel
import com.example.fitness.feature.workout.model.WorkoutType
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
fun BodyPartWorkoutDetailScreen(
    workoutTypeString: String,
    bodyPartId: String,
    initialExerciseId: String? = null
) {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val workoutType = WorkoutType.fromString(workoutTypeString)
    val userGender = UserSession.currentUserGender
    val userLevel = UserSession.experienceLevel
    val targetLevel = remember(initialExerciseId, userLevel) {
        if (!initialExerciseId.isNullOrBlank()) {
            if (initialExerciseId.startsWith("fb_")) {
                val ex = com.example.fitness.feature.exercise.data.FullBodyExerciseLibrary.findById(initialExerciseId)
                when (ex?.difficulty) {
                    com.example.fitness.feature.exercise.model.ExerciseDifficulty.NO_EXPERIENCE.name -> ExperienceLevel.NO_EXPERIENCE
                    com.example.fitness.feature.exercise.model.ExerciseDifficulty.BEGINNER.name -> ExperienceLevel.BEGINNER
                    com.example.fitness.feature.exercise.model.ExerciseDifficulty.ADVANCED.name -> ExperienceLevel.ADVANCED
                    com.example.fitness.feature.exercise.model.ExerciseDifficulty.EXPERT.name -> ExperienceLevel.EXPERT
                    else -> userLevel
                }
            } else {
                com.example.fitness.feature.exercise.data.ThirtyDayExerciseLibrary.findById(initialExerciseId)?.level ?: userLevel
            }
        } else {
            userLevel
        }
    }
    val workout = remember(workoutType, bodyPartId, userGender, targetLevel, com.example.fitness.feature.exercise.data.SampleExerciseData.canonicalRevision) {
        WorkoutRepository.getWorkout(workoutType, bodyPartId, userGender, targetLevel)
            ?: WorkoutRepository.getWorkout(workoutType, bodyPartId)
            ?: WorkoutRepository.getWorkouts(workoutType, userGender, targetLevel).firstOrNull()
            ?: WorkoutRepository.getWorkouts(workoutType).first()
    }

    val initialIndex = remember(workout, initialExerciseId) {
        if (!initialExerciseId.isNullOrBlank()) {
            val cleanInitial = initialExerciseId.trim().lowercase()
            val canonicalInitial = com.example.fitness.feature.exercise.data.SampleExerciseData.resolveCanonicalId(initialExerciseId).trim().lowercase()
            val idx = workout.exercises.indexOfFirst {
                val itClean = it.id.trim().lowercase()
                val itCanonical = com.example.fitness.feature.exercise.data.SampleExerciseData.resolveCanonicalId(it.id).trim().lowercase()
                itClean == cleanInitial || itClean == canonicalInitial || itCanonical == cleanInitial || itCanonical == canonicalInitial
            }
            if (idx >= 0) idx else 0
        } else 0
    }
    var selectedExerciseIndex by remember(workout, initialExerciseId) { mutableIntStateOf(initialIndex) }
    val fallbackExercise = remember(workout) {
        ExerciseItem(
            id = "fallback",
            name = workout.title.ifBlank { "Exercise" },
            sets = "3 sets × 10 reps",
            duration = "10 min",
            description = workout.subtitle.ifBlank { "Follow proper form and control." },
            imageRes = workout.imageRes,
            videoDemoUrl = "",
            tips = listOf("Maintain steady form", "Breathe smoothly through each repetition")
        )
    }
    val rawExercise = workout.exercises.getOrElse(selectedExerciseIndex) {
        workout.exercises.firstOrNull() ?: fallbackExercise
    }
    val activeExercise = remember(rawExercise, com.example.fitness.feature.exercise.data.SampleExerciseData.canonicalRevision) {
        val canonical = com.example.fitness.feature.exercise.data.SampleExerciseData.findCanonical(rawExercise.id, rawExercise.name)
        val candidateVideo = when {
            canonical != null && canonical.media.videoUrl.isNotBlank() && !canonical.media.videoUrl.contains("fitness.app/demo", ignoreCase = true) -> canonical.media.videoUrl
            rawExercise.media.videoUrl.isNotBlank() && !rawExercise.media.videoUrl.contains("fitness.app/demo", ignoreCase = true) -> rawExercise.media.videoUrl
            rawExercise.videoDemoUrl.isNotBlank() && !rawExercise.videoDemoUrl.contains("fitness.app/demo", ignoreCase = true) -> rawExercise.videoDemoUrl
            else -> ""
        }
        if (candidateVideo.isNotBlank()) {
            rawExercise.copy(
                id = canonical?.id ?: rawExercise.id,
                media = (canonical?.media ?: rawExercise.media).copy(videoUrl = candidateVideo),
                videoDemoUrl = candidateVideo
            )
        } else {
            rawExercise.copy(
                id = canonical?.id ?: rawExercise.id,
                media = (canonical?.media ?: rawExercise.media).copy(videoUrl = ""),
                videoDemoUrl = ""
            )
        }
    }

    LaunchedEffect(activeExercise.id, activeExercise.name) {
        try {
            val canonical = com.example.fitness.feature.exercise.data.SampleExerciseData.findCanonical(activeExercise.id, activeExercise.name)
            val lookupId = canonical?.id ?: activeExercise.id
            if (lookupId.isNotBlank()) {
                val remote = com.example.fitness.feature.exercise.data.FirebaseExerciseRepository().getExercise(lookupId)
                if (remote != null) {
                    com.example.fitness.feature.exercise.data.SampleExerciseData.updateCanonicalExercise(remote)
                }
            }
        } catch (_: Exception) {}
    }

    val authUid = try { com.example.fitness.core.firebase.FitnessFirebase.auth.currentUser?.uid } catch (_: Exception) { null }
    val effectiveUid = if (!authUid.isNullOrBlank()) authUid else (UserSession.currentUserId ?: "")
    val isFavorite = UserSession.isExerciseFavorite(activeExercise.id)
    var isTogglingFavorite by remember { mutableStateOf(false) }
    var favoriteErrorMessage by remember { mutableStateOf<String?>(null) }

    var showCompletionDialog by remember { mutableStateOf(false) }
    var isSavingSession by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
        // Fetch active live exercises from Firestore to ensure newly created Admin exercises appear immediately
        try {
            com.example.fitness.feature.exercise.data.FirebaseExerciseRepository().getActiveExercises()
        } catch (_: Exception) {}

        // Maintain real-time snapshot listener so any new/modified/archived admin exercises reflect live
        try {
            com.example.fitness.feature.exercise.data.FirebaseExerciseRepository().observeActiveExercises().collect {
                // Emitted documents are registered into SampleExerciseData and bump canonicalRevision automatically
            }
        } catch (_: Exception) {}
    }

    LaunchedEffect(effectiveUid) {
        if (effectiveUid.isNotBlank() && UserSession.favoritesLoadedForUserId != effectiveUid) {
            try {
                val result = FavoriteExerciseRepository.getFavorites(effectiveUid)
                result.getOrNull()?.let { list ->
                    UserSession.syncFavorites(effectiveUid, list.map { it.exerciseId })
                }
            } catch (_: Exception) {
            }
        }
    }

    LaunchedEffect(favoriteErrorMessage) {
        if (favoriteErrorMessage != null) {
            kotlinx.coroutines.delay(3000)
            favoriteErrorMessage = null
        }
    }

    val topIndicatorText = "${workout.workoutType.badge} • ${workout.bodyPart.uppercase()}"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                // Top Header Bar: Back Button, Indicator Pill, Bookmark Action
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

                    // Indicator showing: "GYM • CHEST" or "HOME • CHEST"
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF07090C),
                        border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(FitTheme.colors.primary)
                            )
                            Spacer(modifier = Modifier.width(7.dp))
                            Text(
                                text = topIndicatorText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp,
                                color = Color.White
                            )
                        }
                    }

                    // Bookmark/Favorite Button
                    Surface(
                        onClick = {
                            if (isTogglingFavorite) return@Surface
                            val targetExerciseId = activeExercise.id
                            if (effectiveUid.isBlank()) {
                                favoriteErrorMessage = "Please sign in to save favorites."
                                return@Surface
                            }
                            val newFavoriteState = !isFavorite
                            // Immediate optimistic UI update
                            UserSession.setFavoriteLocal(targetExerciseId, newFavoriteState)
                            isTogglingFavorite = true
                            coroutineScope.launch {
                                try {
                                    val result = FavoriteExerciseRepository.setFavorite(
                                        userId = effectiveUid,
                                        exerciseId = targetExerciseId,
                                        isFavorite = newFavoriteState
                                    )
                                    if (result.isFailure) {
                                        // Revert optimistic update on backend failure
                                        UserSession.setFavoriteLocal(targetExerciseId, !newFavoriteState)
                                        favoriteErrorMessage = "Failed to update favorite. Please try again."
                                    }
                                } catch (e: Exception) {
                                    // Revert optimistic update on error
                                    UserSession.setFavoriteLocal(targetExerciseId, !newFavoriteState)
                                    favoriteErrorMessage = "Failed to update favorite. Please check connection."
                                } finally {
                                    isTogglingFavorite = false
                                }
                            }
                        },
                        shape = CircleShape,
                        color = Color(0xFF07090C),
                        border = BorderStroke(1.dp, Color(0xFF1B2329)),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isFavorite) FitnessIcons.HeartFilled else FitnessIcons.HeartOutline,
                                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (isFavorite) FitTheme.colors.primary else Color(0xFF8E9BA4),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 1. VIDEO PLAYER SECTION (Silent Loop, Full Fit, No Controls)
                WorkoutVideoPlayer(
                    exercise = activeExercise,
                    autoPlay = true,
                    interactiveControls = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (workout.exercises.isNotEmpty()) {
                    // Current Active Exercise Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Now Demonstrating:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF7A8B97)
                        )
                        Text(
                            text = "Exercise ${selectedExerciseIndex + 1} of ${workout.exercises.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FitTheme.colors.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Direct Quick Start & Customization Banner for Active Exercise
                    Surface(
                        onClick = {
                            val setsText = if (activeExercise.sets.contains("×")) {
                                activeExercise.sets.substringBefore("×").trim()
                            } else {
                                activeExercise.sets
                            }
                            val repsText = if (activeExercise.sets.contains("×")) {
                                activeExercise.sets.substringAfter("×").trim()
                            } else {
                                activeExercise.duration
                            }
                            navigator.navigate(
                                Screen.ExerciseDetail(
                                    exerciseId = activeExercise.id,
                                    sets = setsText,
                                    reps = repsText,
                                    rest = activeExercise.restTime
                                )
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF0C141B),
                        border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeExercise.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Customize Sets, Reps & Track Progress",
                                    fontSize = 11.sp,
                                    color = FitTheme.colors.primary
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = FitTheme.colors.primary,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = FitnessIcons.Play,
                                        contentDescription = "Start Exercise",
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 2. WORKOUT INFO SECTION
                Column {
                    Text(
                        text = "${workout.title} Workout",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = workout.subtitle,
                        fontSize = 14.sp,
                        color = Color(0xFF9BAAB4)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4 Stat Cards: Target, Equipment, Duration, Calories
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        WorkoutStatBadge(
                            label = "TARGET",
                            value = workout.bodyPart,
                            icon = FitnessIcons.Workout,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        WorkoutStatBadge(
                            label = "EQUIPMENT",
                            value = workout.equipment,
                            icon = FitnessIcons.Explore,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        WorkoutStatBadge(
                            label = "DURATION",
                            value = workout.duration,
                            icon = FitnessIcons.Clock,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        WorkoutStatBadge(
                            label = "EST. CALORIES",
                            value = workout.calories,
                            icon = FitnessIcons.Fire,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. HOW TO PERFORM SECTION
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF07090C),
                    border = BorderStroke(1.dp, Color(0xFF192229)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(FitTheme.colors.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "HOW TO PERFORM",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = FitTheme.colors.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = workout.howToPerform,
                            fontSize = 13.sp,
                            color = Color(0xFFC8D3DC),
                            lineHeight = 19.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Form Tips for Active Exercise
                        Text(
                            text = "Form Cues (${activeExercise.name}):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val formTips = if (activeExercise.tips.isNotEmpty()) {
                            activeExercise.tips
                        } else {
                            listOf(
                                "Keep your core tight throughout movement",
                                "Control the eccentric descent for 2 seconds",
                                "Exhale as you push or pull with explosive intent"
                            )
                        }

                        formTips.forEach { tip ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = FitnessIcons.Check,
                                    contentDescription = null,
                                    tint = FitTheme.colors.primary,
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tip,
                                    fontSize = 12.sp,
                                    color = Color(0xFFA2B4C0),
                                    lineHeight = 16.sp,
                                    modifier = Modifier.weight(1f),
                                    softWrap = true
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. EXERCISE ROUTINE SECTION
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Exercise Routine",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Tap any exercise to view full details & technique cues",
                            fontSize = 12.sp,
                            color = Color(0xFF7A8B97)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF141C22),
                        border = BorderStroke(1.dp, Color(0xFF22303A))
                    ) {
                        Text(
                            text = if (workout.exercises.size == 1) "1 Exercise" else "${workout.exercises.size} Exercises",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FitTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            if (workout.exercises.isEmpty()) {
                item {
                    EmptyWorkoutExercisesCard(
                        bodyPart = workout.bodyPart,
                        difficulty = workout.difficulty
                    )
                }
            } else {
                // Exercise Routine Cards List
                itemsIndexed(workout.exercises) { index, exercise ->
                    val isSelected = index == selectedExerciseIndex
                    ExerciseRoutineCard(
                        exercise = exercise,
                        index = index + 1,
                        isSelected = isSelected,
                        onClick = {
                            selectedExerciseIndex = index
                            val setsText = if (exercise.sets.contains("×")) {
                                exercise.sets.substringBefore("×").trim()
                            } else {
                                exercise.sets
                            }
                            val repsText = if (exercise.sets.contains("×")) {
                                exercise.sets.substringAfter("×").trim()
                            } else {
                                exercise.duration
                            }
                            val restText = exercise.restTime
                            navigator.navigate(
                                Screen.ExerciseDetail(
                                    exerciseId = exercise.id,
                                    sets = setsText,
                                    reps = repsText,
                                    rest = restText
                                )
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))

                // Action Button: Complete Workout Routine
                PrimaryButton(
                    text = if (workout.exercises.isEmpty()) "No Exercises Available" else if (isSavingSession) "Saving Progress..." else "Complete Workout",
                    enabled = workout.exercises.isNotEmpty() && !isSavingSession,
                    onClick = {
                        if (workout.exercises.isEmpty() || isSavingSession) return@PrimaryButton
                        isSavingSession = true

                        val performanceRecords = workout.exercises.map { ex ->
                            val setsCount = ex.sets.filter { it.isDigit() }.toIntOrNull() ?: 3
                            val durationDigits = ex.duration.filter { it.isDigit() }
                            val repNum = if (durationDigits.isNotEmpty()) durationDigits.take(2).toIntOrNull() ?: 10 else 10
                            val restSecs = com.example.fitness.feature.workout.model.CustomExerciseConfig.parseDefaultRest(ex.restTime)

                            ExercisePerformanceRecord(
                                exerciseId = ex.id,
                                exerciseName = ex.name,
                                prescribedSets = setsCount,
                                prescribedReps = ex.duration,
                                completedSets = setsCount,
                                completedReps = List(setsCount) { repNum },
                                weightUsed = null,
                                restSeconds = restSecs,
                                completed = true,
                                notes = null
                            )
                        }

                        val parsedMinutes = workout.duration.filter { it.isDigit() }.toIntOrNull() ?: 30
                        val totalPrescribedSets = performanceRecords.sumOf { it.prescribedSets }
                        val totalExCount = performanceRecords.size
                        val authUid = try { com.example.fitness.core.firebase.FitnessFirebase.auth.currentUser?.uid } catch (_: Exception) { null }
                        val effectiveUid = if (!authUid.isNullOrBlank()) authUid else (UserSession.currentUserId ?: "")
                        val muscleList = if (workout.targetMuscle.isNotBlank()) listOf(workout.targetMuscle) else listOf(workout.bodyPart)
                        val programId = workout.workoutType.name.lowercase()
                        val programName = workout.workoutType.title

                        coroutineScope.launch {
                            WorkoutProgressService.saveAndPersistWorkoutSession(
                                workoutId = workout.id,
                                dayNumber = 1,
                                workoutTitle = workout.title,
                                targetMuscle = muscleList,
                                experienceLevel = UserSession.experienceLevel.id,
                                durationMinutes = parsedMinutes,
                                totalExercises = totalExCount,
                                completedExercises = totalExCount,
                                totalSets = totalPrescribedSets,
                                completedSets = totalPrescribedSets,
                                warmUpCompleted = true,
                                mainWorkoutCompleted = true,
                                coolDownCompleted = true,
                                programId = programId,
                                programName = programName,
                                userId = effectiveUid,
                                exercises = performanceRecords,
                                sessionId = "session_${com.example.fitness.getCurrentEpochMillis()}_${programId}_${workout.id}",
                                scope = coroutineScope
                            )
                            isSavingSession = false
                            showCompletionDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Optional View Plan button
                Surface(
                    onClick = {
                        navigator.navigate(Screen.WorkoutOverview(workoutId = bodyPartId, programId = workoutTypeString))
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF07090C),
                    border = BorderStroke(1.dp, Color(0xFF1B2329)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "View Warm-Up & Full Plan >",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FitTheme.colors.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Workout Completion Celebration Dialog
        if (showCompletionDialog) {
            AlertDialog(
                onDismissRequest = { showCompletionDialog = false },
                containerColor = Color(0xFF07090C),
                shape = RoundedCornerShape(20.dp),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎉 Workout Completed!",
                            color = FitTheme.colors.primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Great job finishing your ${workout.title} workout!",
                            fontSize = 14.sp,
                            color = Color(0xFFD1D8DC),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0B1F08),
                            border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${UserSession.workoutStreak} Day Streak! 🔥",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FitTheme.colors.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "30-Day Progress: ${(UserSession.get30DayProgressPercentage() * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    color = Color(0xFF8E9BA4)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    PrimaryButton(
                        text = "Back to Home",
                        onClick = {
                            showCompletionDialog = false
                            navigator.switchTab(NavigationTab.HOME)
                        },
                        height = 44.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        }

        // Unobtrusive Favorite Error Banner
        AnimatedVisibility(
            visible = favoriteErrorMessage != null,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF261214),
                border = BorderStroke(1.dp, Color(0xFFFF453A).copy(alpha = 0.7f)),
                shadowElevation = 8.dp
            ) {
                Text(
                    text = favoriteErrorMessage ?: "",
                    color = Color(0xFFFF8A80),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun WorkoutStatBadge(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF07090C),
        border = BorderStroke(1.dp, Color(0xFF192229)),
        modifier = modifier.heightIn(min = 68.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF141C22),
                modifier = Modifier.size(34.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = FitTheme.colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
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
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 3,
                    softWrap = true
                )
            }
        }
    }
}

@Composable
private fun ExerciseRoutineCard(
    exercise: ExerciseItem,
    index: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1.0f)

    val borderColor by animateColorAsState(
        if (isSelected) FitTheme.colors.primary else Color(0xFF182229)
    )
    val cardBgColor by animateColorAsState(
        if (isSelected) Color(0xFF0E151A) else Color(0xFF07090C)
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(18.dp),
        color = cardBgColor,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail Image with Play Indicator Overlay
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                Image(
                    painter = painterResource(exercise.imageRes),
                    contentDescription = exercise.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark vignette
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF000000).copy(alpha = 0.35f))
                )

                // Play icon button
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) FitTheme.colors.primary else Color(0xFF000000).copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(26.dp)
                        .align(Alignment.Center)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) FitnessIcons.Pause else FitnessIcons.Play,
                            contentDescription = "Demo",
                            tint = if (isSelected) Color(0xFF000000) else Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Exercise Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = exercise.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    if (isSelected) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FitTheme.colors.primary
                        ) {
                            Text(
                                text = "PLAYING",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF000000),
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Sets & Reps
                Text(
                    text = exercise.sets,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF9BAAB4)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Rest Time & Duration Pills
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF141C22),
                        border = BorderStroke(0.6.dp, Color(0xFF22303A))
                    ) {
                        Text(
                            text = exercise.restTime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = FitTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "•   ${exercise.duration}",
                        fontSize = 11.sp,
                        color = Color(0xFF7A8B97)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyWorkoutExercisesCard(
    bodyPart: String,
    difficulty: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF0C141B),
        border = BorderStroke(1.dp, Color(0xFF1B2631)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF141E28),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = FitnessIcons.Workout,
                        contentDescription = null,
                        tint = Color(0xFF7A8B97),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "No exercises available for this level yet.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Admin can add or configure exercises for $bodyPart ($difficulty) in Exercise Management.",
                fontSize = 13.sp,
                color = Color(0xFF8E9BA4),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}
