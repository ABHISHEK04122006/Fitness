package com.example.fitness.feature.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.fitness.feature.progress.data.WorkoutProgressService
import com.example.fitness.feature.progress.model.WorkoutSessionRecord
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import com.example.fitness.core.components.CircularIconButton
import com.example.fitness.core.components.FitChip
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.components.NavigationTab
import com.example.fitness.core.components.PrimaryButton
import androidx.compose.foundation.layout.widthIn
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.navigation.BackHandler
import com.example.fitness.core.navigation.LocalNavigator
import fitness.shared.generated.resources.Res
import fitness.shared.generated.resources.workout_incline_bench
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import com.example.fitness.feature.workout.data.WeeklyWorkoutRepository
import com.example.fitness.feature.workout.model.DailyWorkoutPlan
import com.example.fitness.feature.workout.model.WorkoutPlanExercise
import com.example.fitness.feature.programs.data.ProgramRegistry
import com.example.fitness.feature.exercise.ui.ExerciseDetailModalSheet
import com.example.fitness.feature.exercise.media.FemaleExerciseMediaRegistry
import androidx.compose.runtime.rememberCoroutineScope
import com.example.fitness.core.util.TitleFormatter
import com.example.fitness.feature.workout.data.FavoriteWorkoutRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

private val detailTabs = listOf("Overview", "Exercises", "Muscles")

private data class MuscleActivation(
    val muscleName: String,
    val anatomicalName: String,
    val percentage: Float,
    val accentColor: Color
)

@Composable
fun WorkoutOverviewScreen(
    workoutId: String = "friday",
    programId: String = "",
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current
    val effectiveProgramId = if (programId.isNotEmpty()) programId else UserSession.scheduledProgramId
    val workoutPlan = remember(workoutId, effectiveProgramId, UserSession.experienceLevel, com.example.fitness.feature.exercise.data.SampleExerciseData.canonicalRevision) {
        CanonicalWorkoutResolver.resolveDailyWorkoutPlan(effectiveProgramId, workoutId, UserSession.experienceLevel)
    }

    val displayTitle = remember(workoutPlan.title) {
        TitleFormatter.stripRotation(workoutPlan.title)
    }

    var selectedTab by remember { mutableStateOf("Overview") }
    var showActiveWorkoutDialog by remember { mutableStateOf(false) }
    var showFullTitleDialog by remember { mutableStateOf(false) }
    var isPlayingPreview by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    var detailExerciseId by remember { mutableStateOf<String?>(null) }
    val authUid = try { com.example.fitness.core.firebase.FitnessFirebase.auth.currentUser?.uid } catch (_: Exception) { null }
    val effectiveUid = authUid ?: UserSession.currentUserId ?: ""

    val latestHistorySession = remember(workoutPlan.id, effectiveProgramId, effectiveUid, WorkoutProgressService.workoutHistory.size) {
        WorkoutProgressService.workoutHistory.firstOrNull {
            (effectiveUid.isBlank() || it.userId == effectiveUid) &&
            it.programId.equals(effectiveProgramId, ignoreCase = true) &&
            (it.workoutId.equals(workoutPlan.id, ignoreCase = true) || it.workoutId.equals(workoutId, ignoreCase = true))
        }
    }
    val completedExercises = remember(workoutPlan.id, latestHistorySession?.sessionId, effectiveUid) {
        latestHistorySession?.exercises?.filter { it.completed }?.map { it.exerciseId }?.toSet() ?: emptySet()
    }

    // Favorite management
    val isFavorite = UserSession.isWorkoutFavorite(workoutPlan.id) || UserSession.isWorkoutFavorite(workoutId)
    var heartTargetScale by remember { mutableStateOf(1f) }
    val heartScale by animateFloatAsState(
        targetValue = heartTargetScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )
    var isTogglingFavorite by remember { mutableStateOf(false) }
    var favoriteErrorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        visible = true
        com.example.fitness.core.perf.AppPerfTracker.finish("HOME_TO_TODAYS_WORKOUT", "Today's Workout screen rendered")
    }

    LaunchedEffect(effectiveUid) {
        if (effectiveUid.isNotBlank() && UserSession.workoutFavoritesLoadedForUserId != effectiveUid) {
            val favsResult = FavoriteWorkoutRepository.getFavorites(effectiveUid)
            if (favsResult.isSuccess) {
                val ids = favsResult.getOrNull()?.map { it.workoutId } ?: emptyList()
                UserSession.syncWorkoutFavorites(effectiveUid, ids)
            }
        }
    }

    LaunchedEffect(favoriteErrorMessage) {
        if (favoriteErrorMessage != null) {
            delay(3500)
            favoriteErrorMessage = null
        }
    }

    fun onToggleFavorite() {
        if (isTogglingFavorite) return
        val currentUserId = effectiveUid
        if (currentUserId.isBlank()) {
            favoriteErrorMessage = "Please log in to save favorites"
            return
        }
        val targetWorkoutId = workoutPlan.id
        val newFavoriteState = !isFavorite
        heartTargetScale = 1.25f
        UserSession.setWorkoutFavoriteLocal(targetWorkoutId, newFavoriteState)
        if (workoutId.isNotBlank() && workoutId != targetWorkoutId) {
            UserSession.setWorkoutFavoriteLocal(workoutId, newFavoriteState)
        }
        isTogglingFavorite = true

        coroutineScope.launch {
            delay(150)
            heartTargetScale = 1.0f
            val result = FavoriteWorkoutRepository.setFavorite(
                userId = currentUserId,
                workoutId = targetWorkoutId,
                programId = effectiveProgramId,
                title = displayTitle,
                isFavorite = newFavoriteState
            )
            if (result.isFailure) {
                UserSession.setWorkoutFavoriteLocal(targetWorkoutId, !newFavoriteState)
                if (workoutId.isNotBlank() && workoutId != targetWorkoutId) {
                    UserSession.setWorkoutFavoriteLocal(workoutId, !newFavoriteState)
                }
                favoriteErrorMessage = "Failed to update favorite. Please check your connection."
            }
            isTogglingFavorite = false
        }
    }

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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 840.dp)
                .statusBarsPadding()
        ) {
            // Pinned Top Bar: Back Button, 1-Line Workout Title (flexible), and Heart Button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF000000),
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularIconButton(
                        icon = FitnessIcons.ArrowBack,
                        onClick = {
                            if (!navigator.pop()) {
                                navigator.switchTab(NavigationTab.HOME)
                            }
                        },
                        contentDescription = "Back",
                        tint = Color.White,
                        backgroundColor = Color(0xFF07090C),
                        borderColor = Color(0xFF1B2329)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                showFullTitleDialog = true
                            },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = displayTitle,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    CircularIconButton(
                        icon = if (isFavorite) FitnessIcons.HeartFilled else FitnessIcons.HeartOutline,
                        onClick = { onToggleFavorite() },
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) FitTheme.colors.primary else Color(0xFF8E9BA4),
                        backgroundColor = Color(0xFF07090C),
                        borderColor = Color(0xFF1B2329),
                        modifier = Modifier.scale(heartScale)
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 100.dp) // Leave space for sticky button
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                // Segmented Tabs: Overview, Exercises, Muscles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    detailTabs.forEach { tab ->
                        val isSelected = tab == selectedTab
                        val bgColor by animateColorAsState(
                            if (isSelected) FitTheme.colors.primary else Color(0xFF07090C)
                        )
                        val textColor by animateColorAsState(
                            if (isSelected) Color(0xFF000000) else Color(0xFF8E9BA4)
                        )

                        Surface(
                            onClick = { selectedTab = tab },
                            shape = RoundedCornerShape(20.dp),
                            color = bgColor,
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFF161E24)),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = textColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // DYNAMIC CONTENT BASED ON SELECTED TAB:
                when (selectedTab) {
                    "Overview" -> {
                        OverviewTabContent(
                            workoutPlan = workoutPlan,
                            isPlayingPreview = isPlayingPreview,
                            onTogglePlay = { isPlayingPreview = !isPlayingPreview }
                        )
                    }
                    "Exercises" -> {
                        ExercisesTabContent(
                            workoutPlan = workoutPlan,
                            completedIds = completedExercises.toList(),
                            onViewDetail = { id ->
                                val inspectedExercise = workoutPlan.exercises.find { it.id == id }
                                com.example.fitness.core.perf.AppPerfTracker.start("WORKOUT_TO_EXERCISE_DETAIL")
                                navigator.navigate(
                                    Screen.ExerciseDetail(
                                        exerciseId = id,
                                        sets = inspectedExercise?.sets ?: "",
                                        reps = inspectedExercise?.reps ?: "",
                                        rest = inspectedExercise?.rest ?: ""
                                    )
                                )
                            }
                        )
                    }
                    "Muscles" -> {
                        MusclesTabContent(workoutPlan = workoutPlan)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Sticky Bottom Bar: Full-Width Start Workout Button
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
                        .widthIn(max = 840.dp)
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
                            text = workoutPlan.actionButtonText,
                            onClick = {
                                com.example.fitness.core.perf.AppPerfTracker.start("TODAYS_WORKOUT_TO_WORKOUT")
                                navigator.navigate(
                                    Screen.ActiveWorkoutSession(
                                        workoutId = workoutPlan.id,
                                        programId = effectiveProgramId
                                    )
                                )
                            },
                            height = 54.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Interactive Active Workout Dialog Modal
        if (showActiveWorkoutDialog) {
            ActiveWorkoutSessionDialog(
                workoutTitle = displayTitle,
                onDismiss = { showActiveWorkoutDialog = false }
            )
        }

        // Full Workout Title Dialog Modal
        if (showFullTitleDialog) {
            AlertDialog(
                onDismissRequest = { showFullTitleDialog = false },
                confirmButton = {
                    PrimaryButton(
                        text = "Close",
                        onClick = { showFullTitleDialog = false },
                        height = 42.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                containerColor = Color(0xFF07090C),
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 8.dp,
                title = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(FitTheme.colors.primary.copy(alpha = 0.15f))
                            .border(1.dp, FitTheme.colors.primary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "WORKOUT DETAIL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitTheme.colors.primary,
                            letterSpacing = 1.sp
                        )
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = displayTitle,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 26.sp
                        )

                        val targetOrMuscles = if (workoutPlan.target.isNotBlank()) {
                            workoutPlan.target
                        } else {
                            workoutPlan.trainedMuscles.joinToString(", ")
                        }
                        if (targetOrMuscles.isNotBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "FOCUS / TARGET",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF8E9BA4),
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = targetOrMuscles,
                                fontSize = 14.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 20.sp
                            )
                        }

                        if (workoutPlan.duration.isNotBlank() || workoutPlan.difficulty.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (workoutPlan.duration.isNotBlank()) {
                                    Text(
                                        text = "• ${workoutPlan.duration}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF8E9BA4)
                                    )
                                }
                                if (workoutPlan.difficulty.isNotBlank()) {
                                    Text(
                                        text = "• ${workoutPlan.difficulty}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF8E9BA4)
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }

        // Intercept Android hardware Back when modal sheet is displayed
        BackHandler(enabled = detailExerciseId != null) {
            detailExerciseId = null
        }

        // Exercise Detail Inspection Modal Sheet
        if (detailExerciseId != null) {
            val inspectedExercise = workoutPlan.exercises.find { it.id == detailExerciseId }
            ExerciseDetailModalSheet(
                exerciseId = detailExerciseId!!,
                prescribedSets = inspectedExercise?.sets ?: "",
                prescribedReps = inspectedExercise?.reps ?: "",
                prescribedRest = inspectedExercise?.rest ?: "",
                onDismiss = { detailExerciseId = null }
            )
        }

        // Transient Favorite Error Toast/Banner
        AnimatedVisibility(
            visible = favoriteErrorMessage != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 64.dp, start = 20.dp, end = 20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E1114),
                border = BorderStroke(1.dp, Color(0xFFFF453A).copy(alpha = 0.6f)),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = favoriteErrorMessage ?: "",
                        color = Color(0xFFFFB3B8),
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DISMISS",
                        color = FitTheme.colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { favoriteErrorMessage = null }
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewTabContent(
    workoutPlan: DailyWorkoutPlan,
    isPlayingPreview: Boolean,
    onTogglePlay: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column {
        // Video Hero Preview Player Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(24.dp))
                .clickable { onTogglePlay() },
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF14191C),
            border = BorderStroke(1.dp, Color(0xFF1E272B))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(workoutPlan.imageRes),
                    contentDescription = TitleFormatter.stripRotation(workoutPlan.title),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.0f to Color(0x33000000),
                                0.6f to Color(0x66090D0F),
                                1.0f to Color(0xCC090D0F)
                            )
                        )
                )

                // Centered Interactive Play Button
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .scale(if (isPlayingPreview) 1.0f else pulseScale)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0x8014191C))
                        .border(1.5.dp, Color.White.copy(alpha = 0.7f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlayingPreview) FitnessIcons.Pause else FitnessIcons.Play,
                        contentDescription = if (isPlayingPreview) "Pause Preview" else "Play Preview",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(start = if (isPlayingPreview) 0.dp else 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Quick Stats Row
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF14191C))
                .border(1.dp, Color(0xFF1F282D), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = FitnessIcons.Clock, contentDescription = null, tint = Color(0xFF8E9BA4), modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = workoutPlan.duration, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = FitnessIcons.Difficulty, contentDescription = null, tint = Color(0xFF8E9BA4), modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = workoutPlan.difficulty, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = FitnessIcons.Workout, contentDescription = null, tint = Color(0xFF8E9BA4), modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = workoutPlan.equipment, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // About This Workout
        Text(text = "About This Workout", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = TitleFormatter.stripRotation(workoutPlan.about),
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = Color(0xFF8E9BA4)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // What You'll Train
        Text(text = "What You'll Train", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            workoutPlan.trainedMuscles.forEach { muscle ->
                FitChip(
                    text = muscle,
                    backgroundColor = Color(0xFF141A1D),
                    borderColor = Color(0xFF1E262B),
                    textColor = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Equipment Required
        Text(text = "Equipment Required", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val equipments = workoutPlan.equipment.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            equipments.forEach { eq ->
                FitChip(
                    text = eq,
                    backgroundColor = Color(0xFF14191C),
                    borderColor = Color(0xFF222B2F),
                    textColor = Color(0xFFA6B4BC),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ExercisesTabContent(
    workoutPlan: DailyWorkoutPlan,
    completedIds: List<String>,
    onViewDetail: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. Warm-up Section
        if (workoutPlan.warmup.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(FitTheme.colors.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "1. WARM-UP ROUTINE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = FitTheme.colors.primary
                    )
                }

                workoutPlan.warmup.forEach { warmupItem ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF0C1114),
                        border = BorderStroke(0.8.dp, Color(0xFF1B252C)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = FitnessIcons.Check,
                                contentDescription = null,
                                tint = FitTheme.colors.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = warmupItem,
                                fontSize = 13.sp,
                                color = Color(0xFFCAD7E0),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                                softWrap = true
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (workoutPlan.isRestDay) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF0C1418),
                border = BorderStroke(1.dp, Color(0xFF1E2D38)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Active Recovery & Restoration",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = FitTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No resistance-training working sets scheduled for today. Focus on tissue regeneration, joint decompression, hydration, and the prescribed recovery routine below.",
                        fontSize = 13.sp,
                        color = Color(0xFF8E9BA4),
                        lineHeight = 18.sp
                    )
                }
            }
        } else if (workoutPlan.exercises.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF0C141B),
                border = BorderStroke(1.dp, Color(0xFF1B2631)),
                modifier = Modifier.fillMaxWidth()
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
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = FitnessIcons.Workout,
                                contentDescription = null,
                                tint = Color(0xFF7A8B97),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No exercises available for this level yet.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Admin can add or configure exercises for this category in Exercise Management.",
                        fontSize = 13.sp,
                        color = Color(0xFF8E9BA4),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            // 2. Main Exercises Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(FitTheme.colors.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "2. MAIN EXERCISES (${workoutPlan.exercises.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }
                Text(
                    text = "${completedIds.size}/${workoutPlan.exercises.size} Completed",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FitTheme.colors.primary
                )
            }

            workoutPlan.exercises.forEachIndexed { index, ex ->
                val isDone = completedIds.contains(ex.id)

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isDone) Color(0xFF101E13) else Color(0xFF14191C),
                    border = BorderStroke(1.dp, if (isDone) FitTheme.colors.primary.copy(alpha = 0.5f) else Color(0xFF1E272B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewDetail(ex.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Checkbox Circle
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isDone) FitTheme.colors.primary else Color(0xFF1E262B))
                                .border(1.dp, if (isDone) FitTheme.colors.primary else Color(0xFF2C3840), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(
                                    imageVector = FitnessIcons.Check,
                                    contentDescription = "Completed",
                                    tint = Color(0xFF000000),
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(text = "${index + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8E9BA4))
                            }
                        }

                        // Exercise Thumbnail (if female media available)
                        val femaleThumb = if (UserSession.isFemale) FemaleExerciseMediaRegistry.getThumbnailRes(ex.id) else null
                        if (femaleThumb != null) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            ) {
                                Image(
                                    painter = painterResource(femaleThumb),
                                    contentDescription = ex.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ex.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDone) Color(0xFFA6FF54) else Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${ex.sets} • ${ex.reps} • ${ex.weight}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFCBD5E1),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1B2327)
                        ) {
                            Text(
                                text = ex.rest,
                                fontSize = 11.sp,
                                color = Color(0xFF8E9BA4),
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        CircularIconButton(
                            icon = FitnessIcons.Info,
                            onClick = { onViewDetail(ex.id) },
                            contentDescription = "View exercise details",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        // 3. Cool-down Section
        if (workoutPlan.cooldown.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF38BDF8))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "3. COOL-DOWN & RECOVERY",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF38BDF8)
                    )
                }

                workoutPlan.cooldown.forEach { cdItem ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF0C1114),
                        border = BorderStroke(0.8.dp, Color(0xFF1B252C)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🧘", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = cdItem,
                                fontSize = 13.sp,
                                color = Color(0xFFCAD7E0),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                                softWrap = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MusclesTabContent(workoutPlan: DailyWorkoutPlan) {
    val accentColors = listOf(Color(0xFFA8FF3E), Color(0xFF38BDF8), Color(0xFFFBBF24), Color(0xFFFB7185))
    val muscleData = workoutPlan.trainedMuscles.mapIndexed { index, muscleName ->
        MuscleActivation(
            muscleName = muscleName,
            anatomicalName = when (muscleName.lowercase()) {
                "chest" -> "Pectoralis Major & Minor"
                "triceps" -> "Long, Lateral & Medial Head"
                "back", "lats" -> "Latissimus Dorsi & Rhomboids"
                "biceps" -> "Biceps Brachii"
                "shoulders", "delts", "front delts", "rear delts" -> "Anterior & Lateral Deltoids"
                "quads" -> "Quadriceps Femoris"
                "hamstrings" -> "Biceps Femoris & Semitendinosus"
                "glutes" -> "Gluteus Maximus & Medius"
                "abs", "core" -> "Rectus Abdominis & Transverse Abdominis"
                "mobility" -> "Full Body Fascial Chains"
                else -> "Target Muscle Group"
            },
            percentage = 0.85f - (index * 0.12f).coerceAtMost(0.4f),
            accentColor = accentColors[index % accentColors.size]
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Recovery Status Banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF122214),
            border = BorderStroke(1.dp, Color(0xFF1B401E)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "⚡", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Muscle Recovery: 100% Ready",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = FitTheme.colors.primary
                    )
                    Text(
                        text = "Prime neuromuscular readiness for ${TitleFormatter.stripRotation(workoutPlan.title)}.",
                        fontSize = 12.sp,
                        color = Color(0xFF8E9BA4)
                    )
                }
            }
        }

        Text(
            text = "Target Muscle Engagement",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        muscleData.forEach { item ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF14191C),
                border = BorderStroke(1.dp, Color(0xFF1E262B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.muscleName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = item.anatomicalName, fontSize = 12.sp, color = Color(0xFF8E9BA4), softWrap = true)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${(item.percentage * 100).toInt()}%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = item.accentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { item.percentage },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = item.accentColor,
                        trackColor = Color(0xFF1E262B)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveWorkoutSessionDialog(
    workoutTitle: String,
    onDismiss: () -> Unit
) {
    var secondsElapsed by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(true) }
    var currentSet by remember { mutableStateOf(1) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)
            secondsElapsed++
        }
    }

    val minutes = secondsElapsed / 60
    val seconds = secondsElapsed % 60
    val formattedTime = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF07090C),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔥 Live Session",
                    color = FitTheme.colors.primary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0D2405),
                    border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Set $currentSet of 4",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FitTheme.colors.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formattedTime,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Current: Incline Dumbbell Press",
                    fontSize = 13.sp,
                    color = Color(0xFF8E9BA4)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Set selector dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..4).forEach { setNum ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (setNum <= currentSet) FitTheme.colors.primary else Color(0xFF161E24))
                                .clickable { currentSet = setNum },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$setNum",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (setNum <= currentSet) Color(0xFF000000) else Color.White
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = if (isRunning) "Pause" else "Resume",
                onClick = { isRunning = !isRunning },
                height = 42.dp,
                modifier = Modifier.width(110.dp)
            )
        },
        dismissButton = {
            Surface(
                onClick = {
                    onDismiss()
                },
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF222B2F),
                modifier = Modifier.height(42.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Finish",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    )
}
