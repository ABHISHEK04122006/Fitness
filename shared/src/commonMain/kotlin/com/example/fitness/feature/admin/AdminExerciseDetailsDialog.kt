package com.example.fitness.feature.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.fitness.core.auth.UserRole
import com.example.fitness.core.components.CircularIconButton
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.components.PrimaryButton
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.exercise.media.ExerciseMediaUploadRepository
import com.example.fitness.feature.exercise.media.ExerciseMediaValidator
import com.example.fitness.feature.exercise.media.CloudflareExerciseMediaUploadRepository
import com.example.fitness.feature.exercise.media.MediaUploadFile
import com.example.fitness.feature.exercise.media.MediaValidationResult
import com.example.fitness.feature.exercise.media.rememberMediaFilePickerLauncher
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseMedia
import com.example.fitness.feature.exercise.model.ExerciseMediaType
import androidx.compose.ui.draw.clip
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.media.FemaleExerciseMediaRegistry
import com.example.fitness.feature.workout.WorkoutVideoPlayer
import com.example.fitness.feature.workout.model.ExerciseItem
import fitness.shared.generated.resources.Res
import fitness.shared.generated.resources.gender_female
import fitness.shared.generated.resources.gym_video_thumb
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import com.example.fitness.feature.exercise.data.FirebaseExerciseRepository
import kotlinx.coroutines.launch

/**
 * Detailed modal dialog displaying complete exercise metadata, coaching cues,
 * anatomical targeting, training defaults, interactive media management, and audit trail.
 */
@Composable
fun AdminExerciseDetailsDialog(
    exercise: Exercise,
    onDismiss: () -> Unit,
    onEdit: (Exercise) -> Unit,
    onExerciseUpdated: ((Exercise) -> Unit)? = null,
    mediaUploadRepository: ExerciseMediaUploadRepository = remember { CloudflareExerciseMediaUploadRepository() }
) {
    var currentExercise by remember(exercise.id, exercise.media.videoUrl) {
        mutableStateOf(
            SampleExerciseData.getById(exercise.id) ?: exercise
        )
    }
    val coroutineScope = rememberCoroutineScope()
    val isAdmin = UserSession.currentUserRole == UserRole.ADMIN

    // Presentation model for the Admin Video Preview player
    val previewExerciseItem = remember(currentExercise.id, currentExercise.media.videoUrl, currentExercise.media) {
        val isFemale = currentExercise.gender.equals("Female", ignoreCase = true)
        val fallbackThumb = if (isFemale) {
            FemaleExerciseMediaRegistry.resolveExerciseImage(
                currentExercise.id,
                isFemale = true,
                defaultFallback = Res.drawable.gender_female
            )
        } else {
            Res.drawable.gym_video_thumb
        }

        ExerciseItem(
            id = currentExercise.id,
            name = currentExercise.name,
            sets = "${currentExercise.defaultSets} sets",
            duration = "${currentExercise.defaultRestSeconds}s",
            description = currentExercise.instructions.firstOrNull() ?: currentExercise.description,
            imageRes = fallbackThumb,
            // Explicit canonical priority: direct uploaded videoUrl takes precedence; demo only used when media.videoUrl is blank
            videoDemoUrl = currentExercise.media.videoUrl,
            media = currentExercise.media
        )
    }

    // Media upload & operation states
    var isUploading by remember { mutableStateOf(false) }
    var activeUploadingSlot by remember { mutableStateOf<ExerciseMediaType?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isStatusError by remember { mutableStateOf(false) }

    var pendingTargetType by remember { mutableStateOf<ExerciseMediaType?>(null) }
    var mediaTypeToRemove by remember { mutableStateOf<ExerciseMediaType?>(null) }

    // Multiplatform file picker launcher
    val filePickerLauncher = rememberMediaFilePickerLauncher(
        onFileSelected = { selectedFile ->
            val target = pendingTargetType ?: return@rememberMediaFilePickerLauncher
            pendingTargetType = null

            // Pre-upload validation
            val validation = ExerciseMediaValidator.validate(currentExercise.id, target, selectedFile)
            if (validation is MediaValidationResult.Invalid) {
                statusMessage = validation.reason
                isStatusError = true
                return@rememberMediaFilePickerLauncher
            }

            // Safe upload workflow
            isUploading = true
            activeUploadingSlot = target
            statusMessage = "Uploading ${target.name.lowercase()}..."
            isStatusError = false

            coroutineScope.launch {
                try {
                    val hasExisting = when (target) {
                        ExerciseMediaType.IMAGE -> currentExercise.media.thumbnailUrl.isNotBlank()
                        ExerciseMediaType.ANIMATION -> currentExercise.media.animationUrl.isNotBlank()
                        ExerciseMediaType.VIDEO -> currentExercise.media.videoUrl.isNotBlank()
                        ExerciseMediaType.NONE -> false
                    }

                    val uploadResult = if (hasExisting) {
                        mediaUploadRepository.replaceMediaAsset(
                            exerciseId = currentExercise.id,
                            mediaType = target,
                            newFile = selectedFile
                        )
                    } else {
                        when (target) {
                            ExerciseMediaType.IMAGE -> mediaUploadRepository.uploadThumbnail(currentExercise.id, selectedFile)
                            ExerciseMediaType.ANIMATION -> mediaUploadRepository.uploadAnimation(currentExercise.id, selectedFile)
                            ExerciseMediaType.VIDEO -> mediaUploadRepository.uploadVideo(currentExercise.id, selectedFile)
                            ExerciseMediaType.NONE -> Result.failure(IllegalArgumentException("Invalid target"))
                        }
                    }

                    if (uploadResult.isSuccess) {
                        val downloadUrl = uploadResult.getOrThrow()
                        val oldMedia = currentExercise.media
                        val updatedMedia = when (target) {
                            ExerciseMediaType.IMAGE -> oldMedia.copy(
                                thumbnailUrl = downloadUrl,
                                mediaType = if (oldMedia.mediaType == ExerciseMediaType.NONE.name) ExerciseMediaType.IMAGE.name else oldMedia.mediaType
                            )
                            ExerciseMediaType.ANIMATION -> oldMedia.copy(
                                animationUrl = downloadUrl,
                                mediaType = ExerciseMediaType.ANIMATION.name
                            )
                            ExerciseMediaType.VIDEO -> oldMedia.copy(
                                videoUrl = downloadUrl,
                                mediaType = ExerciseMediaType.VIDEO.name
                            )
                            ExerciseMediaType.NONE -> oldMedia
                        }
                        val updatedExercise = currentExercise.copy(
                            media = updatedMedia,
                            version = currentExercise.version + 1
                        )
                        SampleExerciseData.updateCanonicalExercise(updatedExercise)
                        currentExercise = updatedExercise
                        onExerciseUpdated?.invoke(updatedExercise)
                        statusMessage = "Media uploaded successfully."
                        isStatusError = false
                    } else {
                        statusMessage = "Upload failed: ${uploadResult.exceptionOrNull()?.message ?: "Unknown error"}"
                        isStatusError = true
                    }
                } catch (e: Exception) {
                    statusMessage = "Upload failed: ${e.message ?: "Unknown error"}"
                    isStatusError = true
                } finally {
                    isUploading = false
                    activeUploadingSlot = null
                }
            }
        },
        onError = { error ->
            statusMessage = error
            isStatusError = true
            pendingTargetType = null
        }
    )

    fun triggerUpload(targetType: ExerciseMediaType) {
        if (!isAdmin || isUploading) return
        statusMessage = null
        pendingTargetType = targetType
        filePickerLauncher.launch(targetType)
    }

    fun confirmRemoveMedia(targetType: ExerciseMediaType) {
        if (!isAdmin || isUploading) return
        isUploading = true
        activeUploadingSlot = targetType
        statusMessage = "Removing ${targetType.name.lowercase()}..."
        isStatusError = false

        coroutineScope.launch {
            try {
                val result = mediaUploadRepository.removeMediaAsset(currentExercise.id, targetType)
                if (result.isSuccess) {
                    val oldMedia = currentExercise.media
                    val newThumb = if (targetType == ExerciseMediaType.IMAGE) "" else oldMedia.thumbnailUrl
                    val newAnim = if (targetType == ExerciseMediaType.ANIMATION) "" else oldMedia.animationUrl
                    val newVideo = if (targetType == ExerciseMediaType.VIDEO) "" else oldMedia.videoUrl
                    val newType = when {
                        newVideo.isNotBlank() -> ExerciseMediaType.VIDEO.name
                        newAnim.isNotBlank() -> ExerciseMediaType.ANIMATION.name
                        newThumb.isNotBlank() -> ExerciseMediaType.IMAGE.name
                        else -> ExerciseMediaType.NONE.name
                    }
                    val updatedMedia = oldMedia.copy(
                        thumbnailUrl = newThumb,
                        animationUrl = newAnim,
                        videoUrl = newVideo,
                        mediaType = newType
                    )
                    val updatedExercise = currentExercise.copy(
                        media = updatedMedia,
                        version = currentExercise.version + 1
                    )
                    SampleExerciseData.updateCanonicalExercise(updatedExercise)
                    currentExercise = updatedExercise
                    onExerciseUpdated?.invoke(updatedExercise)
                    statusMessage = "Media removed successfully."
                    isStatusError = false
                } else {
                    statusMessage = "Failed to remove media: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
                    isStatusError = true
                }
            } catch (e: Exception) {
                statusMessage = "Failed to remove media: ${e.message ?: "Unknown error"}"
                isStatusError = true
            } finally {
                isUploading = false
                activeUploadingSlot = null
                mediaTypeToRemove = null
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0D1117),
            border = BorderStroke(1.dp, Color(0xFF1B2228)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                // Top Action Bar with Title and Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (currentExercise.isActive) {
                                    FitTheme.colors.primary.copy(alpha = 0.15f)
                                } else {
                                    Color(0xFFFF5252).copy(alpha = 0.15f)
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (currentExercise.isActive) {
                                        FitTheme.colors.primary.copy(alpha = 0.4f)
                                    } else {
                                        Color(0xFFFF5252).copy(alpha = 0.4f)
                                    }
                                )
                            ) {
                                Text(
                                    text = if (currentExercise.isActive) "ACTIVE" else "ARCHIVED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentExercise.isActive) FitTheme.colors.primary else Color(0xFFFF5252),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            if (currentExercise.category.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF161E24)
                                ) {
                                    Text(
                                        text = currentExercise.category.replace('_', ' '),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF8E9BA4),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (currentExercise.difficulty.isNotBlank()) {
                                val formattedDiff = when (currentExercise.difficulty.trim().uppercase()) {
                                    "NO_EXPERIENCE", "NO EXPERIENCE", "NOEXPERIENCE" -> "No Experience"
                                    "BEGINNER" -> "Beginner"
                                    "ADVANCED" -> "Advanced"
                                    "EXPERT" -> "Expert"
                                    else -> currentExercise.difficulty.replace('_', ' ').lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF161E24)
                                ) {
                                    Text(
                                        text = formattedDiff,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF8E9BA4),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = currentExercise.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    CircularIconButton(
                        icon = FitnessIcons.Close,
                        contentDescription = "Close",
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status Banner (if active feedback exists)
                if (statusMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isStatusError) Color(0xFFFF5252).copy(alpha = 0.12f) else FitTheme.colors.primary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, if (isStatusError) Color(0xFFFF5252).copy(alpha = 0.4f) else FitTheme.colors.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isUploading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = FitTheme.colors.primary
                                    )
                                }
                                Text(
                                    text = statusMessage ?: "",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isStatusError) Color(0xFFFF8A80) else FitTheme.colors.primary
                                )
                            }
                            if (!isUploading) {
                                Icon(
                                    imageVector = FitnessIcons.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color(0xFF8E9BA4),
                                    modifier = Modifier.size(16.dp).clickable { statusMessage = null }
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // INTERACTIVE VIDEO PREVIEW SECTION
                // ==========================================
                SectionHeader(title = "VIDEO PREVIEW")
                WorkoutVideoPlayer(
                    exercise = previewExerciseItem,
                    autoPlay = false,
                    interactiveControls = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Identification & Description
                SectionHeader(title = "IDENTIFICATION & DESCRIPTION")
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF101416),
                    border = BorderStroke(1.dp, Color(0xFF1B2228)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        DetailItemRow(label = "Unique ID", value = currentExercise.id)
                        Spacer(modifier = Modifier.height(6.dp))
                        DetailItemRow(label = "Slug", value = currentExercise.slug)
                        Spacer(modifier = Modifier.height(6.dp))
                        DetailItemRow(
                            label = "Description",
                            value = currentExercise.description.ifBlank { "No description provided." }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Muscle & Anatomical Targeting
                SectionHeader(title = "ANATOMICAL TARGETING")
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF101416),
                    border = BorderStroke(1.dp, Color(0xFF1B2228)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        DetailItemRow(label = "Primary Muscle", value = currentExercise.primaryMuscle.ifBlank { "Not specified" })
                        if (currentExercise.secondaryMuscles.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            DetailItemRow(label = "Secondary Muscles", value = currentExercise.secondaryMuscles.joinToString(", "))
                        }
                        if (currentExercise.muscleGroups.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            DetailItemRow(label = "Muscle Groups", value = currentExercise.muscleGroups.joinToString(", "))
                        }
                        if (currentExercise.movementPattern.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            DetailItemRow(label = "Movement Pattern", value = currentExercise.movementPattern)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Equipment
                SectionHeader(title = "EQUIPMENT")
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF101416),
                    border = BorderStroke(1.dp, Color(0xFF1B2228)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (currentExercise.equipment.isNotEmpty()) {
                            Text(
                                text = currentExercise.equipment.joinToString(" • "),
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Bodyweight / No equipment required",
                                fontSize = 13.sp,
                                color = Color(0xFF8E9BA4)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Coaching & Biomechanical Guidance
                SectionHeader(title = "COACHING & BIOMECHANICAL GUIDANCE")
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF101416),
                    border = BorderStroke(1.dp, Color(0xFF1B2228)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (currentExercise.instructions.isNotEmpty()) {
                            NumberedListSection(title = "Instructions", items = currentExercise.instructions)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        if (currentExercise.setupSteps.isNotEmpty()) {
                            NumberedListSection(title = "Setup Steps", items = currentExercise.setupSteps)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        if (currentExercise.executionSteps.isNotEmpty()) {
                            NumberedListSection(title = "Execution Steps", items = currentExercise.executionSteps)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        if (currentExercise.breathing.isNotBlank()) {
                            DetailItemRow(label = "Breathing Cues", value = currentExercise.breathing)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        if (currentExercise.formTips.isNotEmpty()) {
                            NumberedListSection(title = "Form Tips", items = currentExercise.formTips)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        if (currentExercise.commonMistakes.isNotEmpty()) {
                            NumberedListSection(title = "Common Mistakes", items = currentExercise.commonMistakes)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        if (currentExercise.instructions.isEmpty() && currentExercise.setupSteps.isEmpty() &&
                            currentExercise.executionSteps.isEmpty() && currentExercise.formTips.isEmpty() &&
                            currentExercise.commonMistakes.isEmpty() && currentExercise.breathing.isBlank()
                        ) {
                            Text(
                                text = "No coaching instructions configured yet. Add steps or form cues via Edit.",
                                fontSize = 11.sp,
                                color = Color(0xFF5A6E7B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Training Defaults
                SectionHeader(title = "TRAINING DEFAULTS")
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF101416),
                    border = BorderStroke(1.dp, Color(0xFF1B2228)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DEFAULT SETS", fontSize = 10.sp, color = Color(0xFF8E9BA4), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${currentExercise.defaultSets}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DEFAULT REPS", fontSize = 10.sp, color = Color(0xFF8E9BA4), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(currentExercise.defaultReps.ifBlank { "N/A" }, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("REST PERIOD", fontSize = 10.sp, color = Color(0xFF8E9BA4), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${currentExercise.defaultRestSeconds}s", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ==========================================
                // MEDIA MANAGEMENT SECTION (Step 6E)
                // ==========================================
                SectionHeader(title = "MEDIA")
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF101416),
                    border = BorderStroke(1.dp, Color(0xFF1B2228)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // VIDEO SLOT (File Upload via Cloudflare R2)
                        MediaSlotCard(
                            slotName = "VIDEO",
                            isUploaded = currentExercise.media.videoUrl.isNotBlank(),
                            formatHint = "Allowed: MP4, WEBM, MOV • Max 100 MB",
                            statusDescription = if (currentExercise.media.videoUrl.isNotBlank()) "Cloud HD Video Synced ✓" else "Not uploaded",
                            isUploadingThisSlot = isUploading && activeUploadingSlot == ExerciseMediaType.VIDEO,
                            isAnyUploading = isUploading,
                            isAdmin = isAdmin,
                            onPreview = {
                                coroutineScope.launch {
                                    scrollState.animateScrollTo(0)
                                }
                            },
                            onUpload = { triggerUpload(ExerciseMediaType.VIDEO) },
                            onReplace = { triggerUpload(ExerciseMediaType.VIDEO) },
                            onRemove = { mediaTypeToRemove = ExerciseMediaType.VIDEO }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // DIRECT VIDEO URL CONTROLLER (Admin has full control to paste/type/save video URLs)
                        var directVideoUrlInput by remember(currentExercise.media.videoUrl) {
                            mutableStateOf(currentExercise.media.videoUrl)
                        }
                        var isSavingDirectUrl by remember { mutableStateOf(false) }

                        Text(
                            text = "CUSTOM VIDEO URL (MP4 / STREAMING)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFBAC5D0),
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF161E24),
                                border = BorderStroke(1.dp, Color(0xFF26333D)),
                                modifier = Modifier.weight(1f)
                            ) {
                                BasicTextField(
                                    value = directVideoUrlInput,
                                    onValueChange = { directVideoUrlInput = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontFamily = FitTheme.typography.bodyMedium.fontFamily
                                    ),
                                    cursorBrush = SolidColor(FitTheme.colors.primary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    decorationBox = { innerTextField ->
                                        if (directVideoUrlInput.isEmpty()) {
                                            Text(
                                                text = "https://... or CDN video URL",
                                                color = Color(0xFF5A6E7B),
                                                fontSize = 12.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }

                            // Save Button
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSavingDirectUrl) FitTheme.colors.primary.copy(alpha = 0.5f) else FitTheme.colors.primary,
                                modifier = Modifier
                                    .clickable(enabled = !isSavingDirectUrl && !isUploading) {
                                        val cleanUrl = directVideoUrlInput.trim()
                                        val updatedMedia = currentExercise.media.copy(
                                            videoUrl = cleanUrl,
                                            mediaType = if (cleanUrl.isNotBlank()) ExerciseMediaType.VIDEO.name else ExerciseMediaType.NONE.name
                                        )
                                        val updatedExercise = currentExercise.copy(
                                            media = updatedMedia,
                                            version = currentExercise.version + 1
                                        )
                                        isSavingDirectUrl = true
                                        statusMessage = "Saving video URL..."
                                        isStatusError = false
                                        coroutineScope.launch {
                                            try {
                                                val repo = FirebaseExerciseRepository()
                                                val res = repo.updateExercise(updatedExercise)
                                                if (res.isSuccess) {
                                                    SampleExerciseData.updateCanonicalExercise(updatedExercise)
                                                    currentExercise = updatedExercise
                                                    onExerciseUpdated?.invoke(updatedExercise)
                                                    statusMessage = if (cleanUrl.isNotBlank()) "Video URL saved successfully." else "Video URL cleared."
                                                    isStatusError = false
                                                } else {
                                                    statusMessage = "Failed to save URL: ${res.exceptionOrNull()?.message ?: "Unknown error"}"
                                                    isStatusError = true
                                                }
                                            } catch (e: Exception) {
                                                statusMessage = "Failed to save URL: ${e.message}"
                                                isStatusError = true
                                            } finally {
                                                isSavingDirectUrl = false
                                            }
                                        }
                                    }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    if (isSavingDirectUrl) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.Black
                                        )
                                    } else {
                                        Text(
                                            text = "Save URL",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }

                            // Clear button if URL is present
                            if (currentExercise.media.videoUrl.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFF5252).copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f)),
                                    modifier = Modifier.clickable(enabled = !isSavingDirectUrl && !isUploading) {
                                        directVideoUrlInput = ""
                                        val updatedMedia = currentExercise.media.copy(
                                            videoUrl = "",
                                            mediaType = ExerciseMediaType.NONE.name
                                        )
                                        val updatedExercise = currentExercise.copy(
                                            media = updatedMedia,
                                            version = currentExercise.version + 1
                                        )
                                        isSavingDirectUrl = true
                                        statusMessage = "Clearing video..."
                                        isStatusError = false
                                        coroutineScope.launch {
                                            try {
                                                val repo = FirebaseExerciseRepository()
                                                val res = repo.updateExercise(updatedExercise)
                                                if (res.isSuccess) {
                                                    SampleExerciseData.updateCanonicalExercise(updatedExercise)
                                                    currentExercise = updatedExercise
                                                    onExerciseUpdated?.invoke(updatedExercise)
                                                    statusMessage = "Video cleared successfully."
                                                    isStatusError = false
                                                } else {
                                                    statusMessage = "Failed to clear: ${res.exceptionOrNull()?.message ?: "Unknown error"}"
                                                    isStatusError = true
                                                }
                                            } catch (e: Exception) {
                                                statusMessage = "Failed to clear: ${e.message}"
                                                isStatusError = true
                                            } finally {
                                                isSavingDirectUrl = false
                                            }
                                        }
                                    }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = "Clear",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF8A80)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Audit Trail
                SectionHeader(title = "AUDIT & VERSION CONTROL")
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF101416),
                    border = BorderStroke(1.dp, Color(0xFF1B2228)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        DetailItemRow(label = "Version", value = "v${currentExercise.version}")
                        Spacer(modifier = Modifier.height(6.dp))
                        DetailItemRow(
                            label = "Created At",
                            value = currentExercise.createdAt?.let { "$it ms" } ?: "Not available"
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        DetailItemRow(
                            label = "Created By",
                            value = currentExercise.createdBy.ifBlank { "System" }.take(16)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        DetailItemRow(
                            label = "Updated At",
                            value = currentExercise.updatedAt?.let { "$it ms" } ?: "Not available"
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        DetailItemRow(
                            label = "Updated By",
                            value = currentExercise.updatedBy.ifBlank { "System" }.take(16)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF161E24),
                        border = BorderStroke(1.dp, Color(0xFF2B3A42)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clickable { onDismiss() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Close",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    PrimaryButton(
                        text = "Edit Exercise",
                        onClick = {
                            onDismiss()
                            onEdit(currentExercise)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    )
                }
            }
        }
    }

    // Remove Confirmation Dialog
    mediaTypeToRemove?.let { targetType ->
        Dialog(onDismissRequest = { if (!isUploading) mediaTypeToRemove = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0D1117),
                border = BorderStroke(1.dp, Color(0xFF2B3A42)),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Remove ${targetType.name.lowercase().replaceFirstChar { it.uppercase() }}?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Are you sure you want to remove this media asset? This will clear the cloud asset reference. All coaching instructions, muscle groups, equipment, sets, and reps will remain untouched.",
                        fontSize = 13.sp,
                        color = Color(0xFF8E9BA4),
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF161E24),
                            border = BorderStroke(1.dp, Color(0xFF2B3A42)),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clickable {
                                    if (!isUploading) mediaTypeToRemove = null
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Cancel",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFF5252).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFFFF5252)),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clickable {
                                    if (!isUploading) confirmRemoveMedia(targetType)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isUploading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFFFF5252)
                                    )
                                } else {
                                    Text(
                                        text = "Remove",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF5252)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaSlotCard(
    slotName: String,
    isUploaded: Boolean,
    formatHint: String,
    statusDescription: String,
    isUploadingThisSlot: Boolean,
    isAnyUploading: Boolean,
    isAdmin: Boolean,
    onPreview: (() -> Unit)? = null,
    onUpload: () -> Unit,
    onReplace: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF161E24),
        border = BorderStroke(
            1.dp,
            if (isUploaded) FitTheme.colors.primary.copy(alpha = 0.35f) else Color(0xFF242E35)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Slot Header & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = slotName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false
                )

                Surface(
                    shape = RoundedCornerShape(5.dp),
                    color = if (isUploaded) FitTheme.colors.primary.copy(alpha = 0.15f) else Color(0xFF242E35),
                    border = BorderStroke(
                        1.dp,
                        if (isUploaded) FitTheme.colors.primary.copy(alpha = 0.5f) else Color(0xFF3B4852)
                    )
                ) {
                    Text(
                        text = if (isUploaded) "UPLOADED" else "NOT UPLOADED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUploaded) FitTheme.colors.primary else Color(0xFF8E9BA4),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatHint,
                fontSize = 10.sp,
                color = Color(0xFF5A6E7B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Body & Action Buttons
            if (isUploaded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = statusDescription,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = FitTheme.colors.primary,
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isAdmin) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Preview Button (scroll to top video player)
                            if (onPreview != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = FitTheme.colors.primary.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .clickable(enabled = !isAnyUploading) { onPreview() }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Preview",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = FitTheme.colors.primary
                                        )
                                    }
                                }
                            }

                            // Replace Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1B252C),
                                border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .clickable(enabled = !isAnyUploading) { onReplace() }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    if (isUploadingThisSlot) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            strokeWidth = 2.dp,
                                            color = FitTheme.colors.primary
                                        )
                                    } else {
                                        Text(
                                            text = "Replace",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = FitTheme.colors.primary
                                        )
                                    }
                                }
                            }

                            // Remove Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFF5252).copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .clickable(enabled = !isAnyUploading) { onRemove() }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Remove",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF8A80)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Empty",
                        fontSize = 11.sp,
                        color = Color(0xFF5A6E7B)
                    )

                    if (isAdmin) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FitTheme.colors.primary.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clickable(enabled = !isAnyUploading) { onUpload() }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                if (isUploadingThisSlot) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 2.dp,
                                        color = FitTheme.colors.primary
                                    )
                                } else {
                                    Text(
                                        text = "Upload",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FitTheme.colors.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF5A6E7B),
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun DetailItemRow(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF8E9BA4),
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
private fun NumberedListSection(
    title: String,
    items: List<String>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = FitTheme.colors.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "${index + 1}. ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8E9BA4)
                )
                Text(
                    text = item,
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
