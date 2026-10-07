package com.example.fitness.feature.exercise.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import com.example.fitness.getCurrentEpochMillis
import com.example.fitness.feature.progress.data.WorkoutProgressService
import com.example.fitness.feature.progress.model.ExercisePerformanceRecord
import com.example.fitness.feature.exercise.data.FavoriteExerciseRepository
import com.example.fitness.feature.exercise.data.FirebaseExerciseRepository
import com.example.fitness.feature.workout.data.WorkoutRepository
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fitness.core.components.CircularIconButton
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.components.PrimaryButton
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.navigation.LocalNavigator
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.workout.WorkoutVideoPlayer
import com.example.fitness.feature.workout.model.ExerciseItem
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.exercise.media.FemaleExerciseMediaRegistry
import fitness.shared.generated.resources.Res
import fitness.shared.generated.resources.gender_female
import fitness.shared.generated.resources.gym_video_thumb
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource

/**
 * Production-ready User Exercise Detail View.
 *
 * Implements strict canonical presentation order:
 * 1. MEDIA (via WorkoutVideoPlayer)
 * 2. Exercise Name
 * 3. Primary Muscle / Secondary Muscles / Muscle Groups
 * 4. Equipment
 * 5. Exercise Prescription (Sets, Reps, Rest - Customizable)
 * 6. Instructions (omitted if empty)
 * 7. Setup (omitted if empty)
 * 8. Breathing (omitted if empty)
 * 9. Form Tips (omitted if empty)
 * 10. Common Mistakes (omitted if empty)
 * 11. Interactive Set Tracker (Complete Set 1 of N -> Completion & Return)
 *
 * Preserves the ATHGYM visual language:
 * - Dark background (#000000 / #07090C)
 * - Neon green accent (FitTheme.colors.primary)
 * - White headings
 * - Gray secondary text (#8E9BA4 / #CBD5E1)
 * - Rounded cards (16dp-20dp)
 * - Subtle borders (#19232A)
 */
private enum class PrescriptionCustomizationTarget {
    SETS,
    REPS,
    REST
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserExerciseDetailContent(
    exercise: Exercise,
    prescribedSets: String = "",
    prescribedReps: String = "",
    prescribedRest: String = "",
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current
    val handleBack = {
        if (onNavigateBack != null) {
            onNavigateBack()
        } else {
            navigator.pop()
        }
    }

    // Resolve initial prescription numbers
    val initialSets = remember(prescribedSets, exercise.defaultSets) {
        val parsed = prescribedSets.filter { it.isDigit() }.toIntOrNull()
        if (parsed != null && parsed > 0) parsed
        else if (exercise.defaultSets > 0) exercise.defaultSets
        else 3
    }

    val initialReps = remember(prescribedReps, exercise.defaultReps) {
        if (prescribedReps.isNotBlank()) prescribedReps
        else if (exercise.defaultReps.isNotBlank()) exercise.defaultReps
        else "10–12"
    }

    val initialRestSeconds = remember(prescribedRest, exercise.defaultRestSeconds) {
        val parsed = if (prescribedRest.isNotBlank()) {
            com.example.fitness.feature.workout.model.CustomExerciseConfig.parseDefaultRest(prescribedRest)
        } else null
        if (parsed != null && parsed in 15..300) {
            parsed
        } else if (exercise.defaultRestSeconds in 15..300) {
            exercise.defaultRestSeconds
        } else {
            60
        }
    }

    // User customized workout values
    var customSets by remember(initialSets) { mutableIntStateOf(initialSets) }
    var customReps by remember(initialReps) { mutableStateOf(initialReps) }
    var customRestSeconds by remember(initialRestSeconds) { mutableIntStateOf(initialRestSeconds) }

    // Set completion tracking
    var completedSetsCount by remember(customSets) { mutableIntStateOf(0) }
    var activeCustomizationTarget by remember { mutableStateOf<PrescriptionCustomizationTarget?>(null) }
    var showCompletionDialog by remember { mutableStateOf(false) }

    val setsDisplay = "$customSets sets"
    val repsDisplay = customReps
    val restDisplay = "${customRestSeconds}s"

    val isFemale = UserSession.isFemale
    val canonicalRevision = SampleExerciseData.canonicalRevision

    // Record workout completion when all sets are finished
    LaunchedEffect(completedSetsCount, customSets) {
        if (completedSetsCount >= customSets && customSets > 0) {
            showCompletionDialog = true
            try {
                val repNum = customReps.filter { it.isDigit() }.take(2).toIntOrNull() ?: 10
                val perfRecord = ExercisePerformanceRecord(
                    exerciseId = exercise.id,
                    exerciseName = exercise.name,
                    prescribedSets = customSets,
                    prescribedReps = customReps,
                    completedSets = customSets,
                    completedReps = List(customSets) { repNum },
                    weightUsed = null,
                    restSeconds = customRestSeconds,
                    completed = true,
                    notes = null
                )
                val authUid = try { com.example.fitness.core.firebase.FitnessFirebase.auth.currentUser?.uid } catch (_: Exception) { null }
                val effectiveUid = if (!authUid.isNullOrBlank()) authUid else (UserSession.currentUserId ?: "")
                WorkoutProgressService.saveWorkoutSession(
                    workoutId = "ex_${exercise.id}",
                    dayNumber = 1,
                    workoutTitle = exercise.name,
                    targetMuscle = if (exercise.primaryMuscle.isNotBlank()) listOf(exercise.primaryMuscle) else emptyList(),
                    experienceLevel = "all",
                    completedAt = getCurrentEpochMillis(),
                    durationMinutes = maxOf(1, ((customSets * 45) + (customSets * customRestSeconds)) / 60),
                    totalExercises = 1,
                    completedExercises = 1,
                    totalSets = customSets,
                    completedSets = customSets,
                    exercises = listOf(perfRecord),
                    programId = "single_exercise",
                    programName = "Individual Exercise Workout",
                    userId = effectiveUid
                )
            } catch (_: Exception) { }
        }
    }

    // Adapt to presentation model for the unified video player
    val exerciseItem = remember(exercise.id, exercise.name, exercise.media.videoUrl, exercise.media.thumbnailUrl, isFemale, canonicalRevision, setsDisplay, restDisplay) {
        val canonical = SampleExerciseData.findCanonical(exercise.id, exercise.name)
            ?: SampleExerciseData.getById(exercise.id)
            ?: SampleExerciseData.getById(SampleExerciseData.resolveCanonicalId(exercise.id))
        val canonicalMedia = canonical?.media
        val liveMedia = when {
            canonicalMedia != null && canonicalMedia.videoUrl.isNotBlank() && !canonicalMedia.videoUrl.contains("fitness.app/demo", ignoreCase = true) -> canonicalMedia
            exercise.media.videoUrl.isNotBlank() && !exercise.media.videoUrl.contains("fitness.app/demo", ignoreCase = true) -> exercise.media
            canonicalMedia != null && (canonicalMedia.animationUrl.isNotBlank() || canonicalMedia.thumbnailUrl.isNotBlank()) -> canonicalMedia
            else -> exercise.media
        }

        val repoItem = WorkoutRepository.findExerciseItem(exercise.id)
            ?: WorkoutRepository.findExerciseItem(exercise.name)

        val resolvedImage = if (isFemale) {
            FemaleExerciseMediaRegistry.resolveExerciseImage(exercise.id, isFemale = true, defaultFallback = Res.drawable.gender_female)
        } else {
            repoItem?.imageRes ?: Res.drawable.gym_video_thumb
        }

        val effectiveVideoUrl = when {
            liveMedia.videoUrl.isNotBlank() && !liveMedia.videoUrl.contains("fitness.app/demo", ignoreCase = true) -> liveMedia.videoUrl
            exercise.media.videoUrl.isNotBlank() && !exercise.media.videoUrl.contains("fitness.app/demo", ignoreCase = true) -> exercise.media.videoUrl
            canonical?.media?.videoUrl?.isNotBlank() == true && !canonical.media.videoUrl.contains("fitness.app/demo", ignoreCase = true) -> canonical.media.videoUrl
            !repoItem?.videoDemoUrl.isNullOrBlank() && !repoItem.videoDemoUrl.contains("fitness.app/demo", ignoreCase = true) -> repoItem.videoDemoUrl
            else -> ""
        }

        ExerciseItem(
            id = canonical?.id ?: exercise.id,
            name = exercise.name,
            sets = setsDisplay,
            duration = restDisplay,
            description = exercise.instructions.firstOrNull() ?: exercise.description,
            imageRes = resolvedImage,
            videoDemoUrl = effectiveVideoUrl,
            tips = if (exercise.formTips.isNotEmpty()) exercise.formTips else (repoItem?.tips ?: emptyList()),
            media = liveMedia.copy(
                videoUrl = effectiveVideoUrl
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 720.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 1. MEDIA (Video or Clean Neutral Fallback: Full Fit, No Controls, Silent Loop)
        WorkoutVideoPlayer(
            exercise = exerciseItem,
            autoPlay = true,
            interactiveControls = false,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .heightIn(min = 180.dp, max = 280.dp)
                .clip(RoundedCornerShape(20.dp))
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 2. EXERCISE NAME
        Text(
            text = exercise.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            maxLines = 3,
            softWrap = true,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. TARGET MUSCLES (Primary Muscle, Muscle Groups, Secondary Muscles)
        val hasPrimary = exercise.primaryMuscle.isNotBlank()
        val hasGroups = exercise.muscleGroups.isNotEmpty()
        val hasSecondary = exercise.secondaryMuscles.isNotEmpty()

        if (hasPrimary || hasGroups || hasSecondary) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary Muscle Badge
                if (hasPrimary) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F1E0C),
                        border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.8f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                                text = "Primary: ${exercise.primaryMuscle}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = FitTheme.colors.primary,
                                softWrap = true
                            )
                        }
                    }
                }

                // Muscle Groups
                exercise.muscleGroups.forEach { group ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF10171D),
                        border = BorderStroke(1.dp, Color(0xFF22303C))
                    ) {
                        Text(
                            text = group,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            softWrap = true
                        )
                    }
                }

                // Secondary Muscles
                exercise.secondaryMuscles.forEach { muscle ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0D1318),
                        border = BorderStroke(1.dp, Color(0xFF1E2830))
                    ) {
                        Text(
                            text = "+ $muscle",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF8E9BA4),
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            softWrap = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // 4. EQUIPMENT
        if (exercise.equipment.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EQUIPMENT:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF8E9BA4)
                )
                Spacer(modifier = Modifier.width(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    exercise.equipment.forEach { eq ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF141C22),
                            border = BorderStroke(0.8.dp, Color(0xFF23303A))
                        ) {
                            Text(
                                text = eq,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                softWrap = true
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 4b. INSTRUCTIONAL MOVEMENT POSITIONS (Female Form Demonstration)
        val instructionalPositions = remember(exercise.id, isFemale) {
            if (isFemale) FemaleExerciseMediaRegistry.getInstructionalPositions(exercise.id) else emptyList()
        }
        if (instructionalPositions.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "TECHNIQUE PHASES & FORM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFFE5A93C)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    instructionalPositions.forEach { frame ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0C1014),
                            border = BorderStroke(1.dp, Color(0xFF22303C)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                ) {
                                    Image(
                                        painter = painterResource(frame.imageRes),
                                        contentDescription = frame.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = frame.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = frame.subtitle,
                                    fontSize = 10.sp,
                                    color = Color(0xFF8E9BA4),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // 5. EXERCISE PRESCRIPTION (Sets, Reps, Rest - Interactive & Customizable)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PRESCRIPTION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = FitTheme.colors.primary
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF10171F),
                border = BorderStroke(0.8.dp, FitTheme.colors.primary.copy(alpha = 0.5f)),
                modifier = Modifier.clickable { activeCustomizationTarget = PrescriptionCustomizationTarget.SETS }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✎ Customize",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
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
            // SETS
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF07090C),
                border = BorderStroke(1.dp, Color(0xFF19232A)),
                modifier = Modifier
                    .weight(1f)
                    .clickable { activeCustomizationTarget = PrescriptionCustomizationTarget.SETS }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "SETS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color(0xFF8E9BA4)
                        )
                        Text(
                            text = "✎",
                            fontSize = 9.sp,
                            color = FitTheme.colors.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = setsDisplay,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        softWrap = true
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Edit",
                        fontSize = 9.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // REPS
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF07090C),
                border = BorderStroke(1.dp, Color(0xFF19232A)),
                modifier = Modifier
                    .weight(1f)
                    .clickable { activeCustomizationTarget = PrescriptionCustomizationTarget.REPS }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "REPS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color(0xFF8E9BA4)
                        )
                        Text(
                            text = "✎",
                            fontSize = 9.sp,
                            color = FitTheme.colors.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = repsDisplay,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        softWrap = true
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Edit",
                        fontSize = 9.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // REST
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF07090C),
                border = BorderStroke(1.dp, Color(0xFF19232A)),
                modifier = Modifier
                    .weight(1f)
                    .clickable { activeCustomizationTarget = PrescriptionCustomizationTarget.REST }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "REST",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color(0xFF8E9BA4)
                        )
                        Text(
                            text = "✎",
                            fontSize = 9.sp,
                            color = FitTheme.colors.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = restDisplay,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        softWrap = true
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Edit",
                        fontSize = 9.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 6. INSTRUCTIONS (Display canonical content if non-empty)
        if (exercise.instructions.isNotEmpty()) {
            GuidanceCard(
                title = "INSTRUCTIONS",
                accentColor = FitTheme.colors.primary
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    exercise.instructions.forEach { instruction ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = FitTheme.colors.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = instruction,
                                fontSize = 13.sp,
                                color = Color(0xFFCAD7E0),
                                lineHeight = 19.sp,
                                softWrap = true
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 7. SETUP (Numbered setup steps if non-empty)
        if (exercise.setupSteps.isNotEmpty()) {
            GuidanceCard(
                title = "SETUP",
                accentColor = Color(0xFF38BDF8)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    exercise.setupSteps.forEachIndexed { idx, step ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF0F2633),
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${idx + 1}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = step,
                                fontSize = 13.sp,
                                color = Color(0xFFCAD7E0),
                                lineHeight = 19.sp,
                                modifier = Modifier.weight(1f),
                                softWrap = true
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }



        // 9. BREATHING (Breathing cues if non-empty)
        if (exercise.breathing.isNotBlank()) {
            GuidanceCard(
                title = "BREATHING",
                accentColor = Color(0xFFA78BFA)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🫁",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = exercise.breathing,
                        fontSize = 13.sp,
                        color = Color(0xFFCAD7E0),
                        lineHeight = 19.sp,
                        softWrap = true
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 10. FORM TIPS (Tips if non-empty)
        if (exercise.formTips.isNotEmpty()) {
            GuidanceCard(
                title = "FORM TIPS",
                accentColor = FitTheme.colors.primary
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    exercise.formTips.forEach { tip ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = FitnessIcons.Check,
                                contentDescription = null,
                                tint = FitTheme.colors.primary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tip,
                                fontSize = 13.sp,
                                color = Color(0xFFCAD7E0),
                                lineHeight = 19.sp,
                                modifier = Modifier.weight(1f),
                                softWrap = true
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 11. COMMON MISTAKES (Mistakes if non-empty)
        if (exercise.commonMistakes.isNotEmpty()) {
            GuidanceCard(
                title = "COMMON MISTAKES",
                accentColor = Color(0xFFFF6B6B)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    exercise.commonMistakes.forEach { mistake ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = FitnessIcons.Close,
                                contentDescription = null,
                                tint = Color(0xFFFF6B6B),
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = mistake,
                                fontSize = 13.sp,
                                color = Color(0xFFE2E8F0),
                                lineHeight = 19.sp,
                                modifier = Modifier.weight(1f),
                                softWrap = true
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 11. INTERACTIVE SET COMPLETION TRACKER
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF090E13),
            border = BorderStroke(1.dp, if (completedSetsCount >= customSets) FitTheme.colors.primary else Color(0xFF1C2730)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SET PROGRESS TRACKER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = FitTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (completedSetsCount >= customSets) "All Sets Done! 🎉" else "Set ${completedSetsCount + 1} of $customSets Active",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (completedSetsCount >= customSets) FitTheme.colors.primary.copy(alpha = 0.2f) else Color(0xFF141C22),
                        border = BorderStroke(1.dp, if (completedSetsCount >= customSets) FitTheme.colors.primary else Color(0xFF24323D))
                    ) {
                        Text(
                            text = "$completedSetsCount / $customSets Done",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (completedSetsCount >= customSets) FitTheme.colors.primary else Color(0xFF8E9BA4),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Set Status Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (setIndex in 1..customSets) {
                        val isDone = setIndex <= completedSetsCount
                        val isCurrent = setIndex == completedSetsCount + 1

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = when {
                                isDone -> FitTheme.colors.primary.copy(alpha = 0.16f)
                                isCurrent -> Color(0xFF141F28)
                                else -> Color(0xFF090D11)
                            },
                            border = BorderStroke(
                                width = 1.dp,
                                color = when {
                                // Completed
                                isDone -> FitTheme.colors.primary
                                // Current active set
                                isCurrent -> FitTheme.colors.primary.copy(alpha = 0.8f)
                                else -> Color(0xFF1F2B36)
                            }),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    completedSetsCount = if (setIndex == completedSetsCount && isDone) {
                                        setIndex - 1
                                    } else {
                                        setIndex
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isDone) "✓" else "$setIndex",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDone) FitTheme.colors.primary else if (isCurrent) Color.White else Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Set $setIndex",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDone) FitTheme.colors.primary else if (isCurrent) Color.White else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Button: Complete Set X of Y / Next Exercise
                if (completedSetsCount < customSets) {
                    PrimaryButton(
                        text = "Complete Set ${completedSetsCount + 1} of $customSets",
                        onClick = {
                            completedSetsCount++
                        },
                        height = 52.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Complete Set ${completedSetsCount + 1} of $customSets" }
                    )
                } else {
                    PrimaryButton(
                        text = "✓ Completed Exercise — Next Exercise",
                        onClick = {
                            showCompletionDialog = true
                        },
                        height = 52.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Completed Exercise — Next Exercise" }
                    )
                }

                if (completedSetsCount > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Reset Sets",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B),
                        modifier = Modifier
                            .clickable { completedSetsCount = 0 }
                            .padding(4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }

    // Modal Customization Dialog
    if (activeCustomizationTarget != null) {
        PrescriptionCustomizationDialog(
            initialTarget = activeCustomizationTarget!!,
            currentSets = customSets,
            currentReps = customReps,
            currentRestSeconds = customRestSeconds,
            onSave = { newSets, newReps, newRest ->
                customSets = newSets
                customReps = newReps
                customRestSeconds = newRest
                if (completedSetsCount > newSets) {
                    completedSetsCount = newSets
                }
                activeCustomizationTarget = null
            },
            onDismiss = {
                activeCustomizationTarget = null
            }
        )
    }

    // Exercise Completion Celebration Dialog
    if (showCompletionDialog) {
        ExerciseCelebrationDialog(
            exerciseName = exercise.name,
            setsCount = customSets,
            reps = customReps,
            restSeconds = customRestSeconds,
            onBackToWorkout = {
                showCompletionDialog = false
                handleBack()
            },
            onDismiss = {
                showCompletionDialog = false
            }
        )
    }
}

/**
 * Interactive Dialog to customize Sets, Reps, and Rest periods.
 */
@Composable
private fun PrescriptionCustomizationDialog(
    initialTarget: PrescriptionCustomizationTarget,
    currentSets: Int,
    currentReps: String,
    currentRestSeconds: Int,
    onSave: (sets: Int, reps: String, restSeconds: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableStateOf(initialTarget) }
    var tempSets by remember { mutableIntStateOf(currentSets) }
    var tempReps by remember { mutableStateOf(currentReps) }
    var tempRest by remember { mutableIntStateOf(currentRestSeconds) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0C1116),
                border = BorderStroke(1.dp, Color(0xFF22303C)),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 440.dp)
                    .clickable(enabled = false) {}
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CUSTOMIZE EXERCISE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = FitTheme.colors.primary
                        )
                        CircularIconButton(
                            icon = FitnessIcons.Close,
                            onClick = onDismiss,
                            contentDescription = "Close",
                            size = 32.dp,
                            iconSize = 16.dp,
                            backgroundColor = Color(0xFF141C22),
                            borderColor = Color(0xFF23303A)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tab Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF07090C), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PrescriptionCustomizationTarget.values().forEach { target ->
                            val isSelected = target == activeTab
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) FitTheme.colors.primary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { activeTab = target }
                            ) {
                                Text(
                                    text = target.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else Color(0xFF8E9BA4),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Content for active tab
                    when (activeTab) {
                        PrescriptionCustomizationTarget.SETS -> {
                            Text(
                                text = "Target Sets",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF8E9BA4)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            // Stepper
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF162029),
                                    border = BorderStroke(1.dp, Color(0xFF2A3A48)),
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clickable { if (tempSets > 1) tempSets-- }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("-", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }

                                Text(
                                    text = "$tempSets sets",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF162029),
                                    border = BorderStroke(1.dp, Color(0xFF2A3A48)),
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clickable { if (tempSets < 12) tempSets++ }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Presets
                            Text(
                                text = "Quick Select",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(2, 3, 4, 5, 6).forEach { num ->
                                    val isChosen = tempSets == num
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isChosen) FitTheme.colors.primary.copy(alpha = 0.2f) else Color(0xFF141C22),
                                        border = BorderStroke(1.dp, if (isChosen) FitTheme.colors.primary else Color(0xFF23303A)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { tempSets = num }
                                    ) {
                                        Text(
                                            text = "$num",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isChosen) FitTheme.colors.primary else Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        PrescriptionCustomizationTarget.REPS -> {
                            Text(
                                text = "Target Reps / Range",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF8E9BA4)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = tempReps,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Rep range presets
                            Text(
                                text = "Common Rep Ranges",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf("6–8", "8–10", "10–12", "12–15", "15–20", "To Failure").forEach { range ->
                                    val isChosen = tempReps == range
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isChosen) FitTheme.colors.primary.copy(alpha = 0.2f) else Color(0xFF141C22),
                                        border = BorderStroke(1.dp, if (isChosen) FitTheme.colors.primary else Color(0xFF23303A)),
                                        modifier = Modifier.clickable { tempReps = range }
                                    ) {
                                        Text(
                                            text = range,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isChosen) FitTheme.colors.primary else Color.White,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        PrescriptionCustomizationTarget.REST -> {
                            Text(
                                text = "Rest Duration (Seconds)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF8E9BA4)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            // Stepper +/- 15s
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF162029),
                                    border = BorderStroke(1.dp, Color(0xFF2A3A48)),
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clickable { if (tempRest >= 30) tempRest -= 15 }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("-15", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }

                                Text(
                                    text = "${tempRest}s",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FitTheme.colors.primary
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF162029),
                                    border = BorderStroke(1.dp, Color(0xFF2A3A48)),
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clickable { if (tempRest <= 300) tempRest += 15 }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("+15", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Rest presets
                            Text(
                                text = "Standard Intervals",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(30, 45, 60, 90, 120, 180).forEach { secs ->
                                    val isChosen = tempRest == secs
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isChosen) FitTheme.colors.primary.copy(alpha = 0.2f) else Color(0xFF141C22),
                                        border = BorderStroke(1.dp, if (isChosen) FitTheme.colors.primary else Color(0xFF23303A)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { tempRest = secs }
                                    ) {
                                        Text(
                                            text = "${secs}s",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isChosen) FitTheme.colors.primary else Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Save / Apply Button
                    PrimaryButton(
                        text = "Save & Apply",
                        onClick = {
                            onSave(tempSets, tempReps, tempRest)
                        },
                        height = 48.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Exercise Completion Celebration Dialog when all customized sets are finished.
 */
@Composable
private fun ExerciseCelebrationDialog(
    exerciseName: String,
    setsCount: Int,
    reps: String,
    restSeconds: Int,
    onBackToWorkout: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF090E13),
                border = BorderStroke(1.5.dp, FitTheme.colors.primary),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 440.dp)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Celebration badge
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(FitTheme.colors.primary.copy(alpha = 0.15f))
                            .border(2.dp, FitTheme.colors.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎉",
                            fontSize = 36.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Exercise Completed!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Completed $exerciseName!\nNow move to the next exercise.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFCAD7E0),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Summary Stats Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF10171F),
                        border = BorderStroke(1.dp, Color(0xFF1E2B37)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("SETS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8E9BA4))
                                Spacer(modifier = Modifier.height(3.dp))
                                Text("$setsCount / $setsCount", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = FitTheme.colors.primary)
                            }
                            Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color(0xFF22303C)))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("REPS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8E9BA4))
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(reps, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                            Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color(0xFF22303C)))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("REST", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8E9BA4))
                                Spacer(modifier = Modifier.height(3.dp))
                                Text("${restSeconds}s", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryButton(
                        text = "Back to Workout Page",
                        onClick = onBackToWorkout,
                        height = 52.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Back to Workout Page" }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Stay on this page",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8E9BA4),
                        modifier = Modifier
                            .clickable(onClick = onDismiss)
                            .padding(6.dp)
                    )
                }
            }
        }
    }
}

/**
 * Reusable rounded card container for guidance sections.
 */
@Composable
private fun GuidanceCard(
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
        Column(modifier = Modifier.padding(16.dp)) {
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
                    letterSpacing = 1.sp,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

/**
 * Fullscreen / modal bottom sheet allowing users to view complete canonical exercise
 * details without breaking workout session timers, set tracking, or state.
 */
@Composable
fun ExerciseDetailModalSheet(
    exerciseId: String,
    prescribedSets: String = "",
    prescribedReps: String = "",
    prescribedRest: String = "",
    onDismiss: () -> Unit
) {
    val canonicalRevision = SampleExerciseData.canonicalRevision
    val resolvedCanonicalId = remember(exerciseId) {
        SampleExerciseData.resolveCanonicalId(exerciseId)
    }
    val exerciseName = remember(exerciseId) {
        WorkoutRepository.findExerciseItem(exerciseId)?.name
            ?: (if (resolvedCanonicalId.isNotBlank()) WorkoutRepository.findExerciseItem(resolvedCanonicalId)?.name else null)
            ?: exerciseId.replace('_', ' ')
    }
    var exercise by remember(exerciseId, canonicalRevision) {
        val canonical = SampleExerciseData.findCanonical(resolvedCanonicalId, exerciseName)
            ?: SampleExerciseData.findCanonical(exerciseId, exerciseName)
            ?: SampleExerciseData.getById(resolvedCanonicalId)
            ?: SampleExerciseData.getById(exerciseId)
        if (canonical != null) {
            mutableStateOf(canonical)
        } else {
            val repoItem = WorkoutRepository.findExerciseItem(exerciseId)
                ?: WorkoutRepository.findExerciseItem(exerciseName)
            if (repoItem != null) {
                val effectiveVideo = if (!repoItem.videoDemoUrl.contains("fitness.app/demo", ignoreCase = true)) {
                    repoItem.videoDemoUrl.ifBlank { repoItem.media.videoUrl }
                } else {
                    repoItem.media.videoUrl
                }
                mutableStateOf(
                    Exercise(
                        id = repoItem.id,
                        name = repoItem.name,
                        description = repoItem.description,
                        instructions = listOf(repoItem.description).filter { it.isNotBlank() },
                        formTips = repoItem.tips,
                        media = repoItem.media.copy(videoUrl = effectiveVideo)
                    )
                )
            } else {
                mutableStateOf(
                    Exercise(
                        id = exerciseId,
                        name = exerciseName.split(' ')
                            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                    )
                )
            }
        }
    }

    LaunchedEffect(exerciseId) {
        com.example.fitness.core.perf.AppPerfTracker.finish("WORKOUT_TO_EXERCISE_DETAIL", "Exercise Details modal sheet ready")
        val hasValidVideo = exercise.media.videoUrl.isNotBlank() &&
            !exercise.media.videoUrl.contains("fitness.app/demo", ignoreCase = true)
        if (hasValidVideo) {
            // Already resolved and available from Today's Workout / canonical in-memory repository!
            return@LaunchedEffect
        }
        try {
            val lookupId = resolvedCanonicalId.ifBlank { exerciseId }
            val remote = FirebaseExerciseRepository().getExercise(lookupId)
                ?: if (lookupId != exerciseId) FirebaseExerciseRepository().getExercise(exerciseId) else null
            if (remote != null) {
                val canonicalEx = SampleExerciseData.getById(lookupId) ?: SampleExerciseData.getById(exerciseId)
                val preservedVideoUrl = when {
                    remote.media.videoUrl.isNotBlank() -> remote.media.videoUrl
                    exercise.media.videoUrl.isNotBlank() -> exercise.media.videoUrl
                    canonicalEx?.media?.videoUrl?.isNotBlank() == true -> canonicalEx.media.videoUrl
                    else -> ""
                }
                val mergedRemote = remote.copy(
                    media = remote.media.copy(videoUrl = preservedVideoUrl)
                )
                // Only update state if data actually changed to avoid tearing down the video player!
                if (mergedRemote.media.videoUrl != exercise.media.videoUrl ||
                    mergedRemote.description != exercise.description ||
                    mergedRemote.instructions != exercise.instructions ||
                    mergedRemote.formTips != exercise.formTips
                ) {
                    exercise = mergedRemote
                }
                if (preservedVideoUrl.isNotBlank()) {
                    SampleExerciseData.updateCanonicalExercise(mergedRemote)
                }
            }
        } catch (_: Exception) { }
    }

    val authUid = try { com.example.fitness.core.firebase.FitnessFirebase.auth.currentUser?.uid } catch (_: Exception) { null }
    val effectiveUid = if (!authUid.isNullOrBlank()) authUid else (UserSession.currentUserId ?: "")
    val isFavorite = UserSession.isExerciseFavorite(exercise.id)
    val coroutineScope = rememberCoroutineScope()
    var isTogglingFavorite by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF000000))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularIconButton(
                        icon = FitnessIcons.ArrowBack,
                        onClick = onDismiss,
                        contentDescription = "Close exercise details"
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F161C),
                        border = BorderStroke(1.dp, Color(0xFF22303A))
                    ) {
                        Text(
                            text = "EXERCISE GUIDE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = FitTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    // Heart Favorite Action
                    CircularIconButton(
                        icon = if (isFavorite) FitnessIcons.HeartFilled else FitnessIcons.HeartOutline,
                        onClick = {
                            if (isTogglingFavorite) return@CircularIconButton
                            val targetExerciseId = exercise.id
                            if (effectiveUid.isBlank()) return@CircularIconButton
                            val newFavoriteState = !isFavorite
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
                                        UserSession.setFavoriteLocal(targetExerciseId, !newFavoriteState)
                                    }
                                } catch (_: Exception) {
                                    UserSession.setFavoriteLocal(targetExerciseId, !newFavoriteState)
                                } finally {
                                    isTogglingFavorite = false
                                }
                            }
                        },
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) FitTheme.colors.primary else Color(0xFF8E9BA4),
                        backgroundColor = Color(0xFF07090C),
                        borderColor = Color(0xFF1B2329)
                    )
                }

                // Scrollable Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    UserExerciseDetailContent(
                        exercise = exercise,
                        prescribedSets = prescribedSets,
                        prescribedReps = prescribedReps,
                        prescribedRest = prescribedRest,
                        onNavigateBack = onDismiss,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Bottom Dismiss Action
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    color = Color.Transparent
                ) {
                    PrimaryButton(
                        text = "Return to Workout",
                        onClick = onDismiss,
                        height = 50.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Return to Workout" }
                    )
                }
            }
        }
    }
}

/**
 * Standalone Exercise Detail Screen for direct navigation from Explore or Catalog.
 */
@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    prescribedSets: String = "",
    prescribedReps: String = "",
    prescribedRest: String = "",
    onBack: (() -> Unit)? = null
) {
    val navigator = LocalNavigator.current
    val canonicalRevision = SampleExerciseData.canonicalRevision
    val resolvedCanonicalId = remember(exerciseId) {
        SampleExerciseData.resolveCanonicalId(exerciseId)
    }
    val exerciseName = remember(exerciseId) {
        WorkoutRepository.findExerciseItem(exerciseId)?.name
            ?: (if (resolvedCanonicalId.isNotBlank()) WorkoutRepository.findExerciseItem(resolvedCanonicalId)?.name else null)
            ?: exerciseId.replace('_', ' ')
    }
    var exercise by remember(exerciseId, canonicalRevision) {
        val canonical = SampleExerciseData.findCanonical(resolvedCanonicalId, exerciseName)
            ?: SampleExerciseData.findCanonical(exerciseId, exerciseName)
            ?: SampleExerciseData.getById(resolvedCanonicalId)
            ?: SampleExerciseData.getById(exerciseId)
        if (canonical != null) {
            mutableStateOf(canonical)
        } else {
            val repoItem = WorkoutRepository.findExerciseItem(exerciseId)
                ?: WorkoutRepository.findExerciseItem(exerciseName)
            if (repoItem != null) {
                val effectiveVideo = if (!repoItem.videoDemoUrl.contains("fitness.app/demo", ignoreCase = true)) {
                    repoItem.videoDemoUrl.ifBlank { repoItem.media.videoUrl }
                } else {
                    repoItem.media.videoUrl
                }
                mutableStateOf(
                    Exercise(
                        id = repoItem.id,
                        name = repoItem.name,
                        description = repoItem.description,
                        instructions = listOf(repoItem.description).filter { it.isNotBlank() },
                        formTips = repoItem.tips,
                        media = repoItem.media.copy(videoUrl = effectiveVideo)
                    )
                )
            } else {
                mutableStateOf(
                    Exercise(
                        id = exerciseId,
                        name = exerciseName.split(' ')
                            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                    )
                )
            }
        }
    }

    LaunchedEffect(exerciseId) {
        com.example.fitness.core.perf.AppPerfTracker.finish("WORKOUT_TO_EXERCISE_DETAIL", "Exercise Details screen ready")
        val hasValidVideo = exercise.media.videoUrl.isNotBlank() &&
            !exercise.media.videoUrl.contains("fitness.app/demo", ignoreCase = true)
        if (hasValidVideo) {
            // Already resolved and available from Today's Workout / canonical in-memory repository!
            return@LaunchedEffect
        }
        try {
            val lookupId = resolvedCanonicalId.ifBlank { exerciseId }
            val remote = FirebaseExerciseRepository().getExercise(lookupId)
                ?: if (lookupId != exerciseId) FirebaseExerciseRepository().getExercise(exerciseId) else null
            if (remote != null) {
                val canonicalEx = SampleExerciseData.getById(lookupId) ?: SampleExerciseData.getById(exerciseId)
                val preservedVideoUrl = when {
                    remote.media.videoUrl.isNotBlank() -> remote.media.videoUrl
                    exercise.media.videoUrl.isNotBlank() -> exercise.media.videoUrl
                    canonicalEx?.media?.videoUrl?.isNotBlank() == true -> canonicalEx.media.videoUrl
                    else -> ""
                }
                val mergedRemote = remote.copy(
                    media = remote.media.copy(videoUrl = preservedVideoUrl)
                )
                // Only update state if data actually changed to avoid tearing down the video player!
                if (mergedRemote.media.videoUrl != exercise.media.videoUrl ||
                    mergedRemote.description != exercise.description ||
                    mergedRemote.instructions != exercise.instructions ||
                    mergedRemote.formTips != exercise.formTips
                ) {
                    exercise = mergedRemote
                }
                if (preservedVideoUrl.isNotBlank()) {
                    SampleExerciseData.updateCanonicalExercise(mergedRemote)
                }
            }
        } catch (_: Exception) { }
    }

    val authUid = try { com.example.fitness.core.firebase.FitnessFirebase.auth.currentUser?.uid } catch (_: Exception) { null }
    val effectiveUid = if (!authUid.isNullOrBlank()) authUid else (UserSession.currentUserId ?: "")
    val isFavorite = UserSession.isExerciseFavorite(exercise.id)
    val coroutineScope = rememberCoroutineScope()
    var isTogglingFavorite by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularIconButton(
                    icon = FitnessIcons.ArrowBack,
                    onClick = {
                        if (onBack != null) onBack() else navigator.pop()
                    },
                    contentDescription = "Back"
                )

                Text(
                    text = "Exercise Details",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Heart Favorite Action
                CircularIconButton(
                    icon = if (isFavorite) FitnessIcons.HeartFilled else FitnessIcons.HeartOutline,
                    onClick = {
                        if (isTogglingFavorite) return@CircularIconButton
                        val targetExerciseId = exercise.id
                        if (effectiveUid.isBlank()) return@CircularIconButton
                        val newFavoriteState = !isFavorite
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
                                    UserSession.setFavoriteLocal(targetExerciseId, !newFavoriteState)
                                }
                            } catch (_: Exception) {
                                UserSession.setFavoriteLocal(targetExerciseId, !newFavoriteState)
                            } finally {
                                isTogglingFavorite = false
                            }
                        }
                    },
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) FitTheme.colors.primary else Color(0xFF8E9BA4),
                    backgroundColor = Color(0xFF07090C),
                    borderColor = Color(0xFF1B2329)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                UserExerciseDetailContent(
                    exercise = exercise,
                    prescribedSets = prescribedSets,
                    prescribedReps = prescribedReps,
                    prescribedRest = prescribedRest,
                    onNavigateBack = {
                        if (onBack != null) onBack() else navigator.pop()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
