package com.example.fitness.feature.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.media.ExerciseMediaResolver
import com.example.fitness.feature.exercise.media.MediaLoadingState
import com.example.fitness.feature.exercise.media.ResolvedExerciseMedia
import com.example.fitness.feature.workout.model.ExerciseItem
import fitness.shared.generated.resources.Res
import fitness.shared.generated.resources.gym_video_thumb
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * Configuration contract for User exercise video playback.
 * Enforces:
 * - volume = 0
 * - isMuted = true
 * - autoPlay = true
 * - isLooping = true
 * - repeatMode = REPEAT_ONE
 * - controlsEnabled = false
 * - allowUserControl = false
 */
data class UserExerciseVideoConfig(
    val volume: Float = 0f,
    val isMuted: Boolean = true,
    val autoPlay: Boolean = true,
    val isLooping: Boolean = true,
    val repeatMode: String = "REPEAT_ONE",
    val controlsEnabled: Boolean = false,
    val allowUserControl: Boolean = false
) {
    companion object {
        val USER_DEFAULT = UserExerciseVideoConfig(
            volume = 0f,
            isMuted = true,
            autoPlay = true,
            isLooping = true,
            repeatMode = "REPEAT_ONE",
            controlsEnabled = false,
            allowUserControl = false
        )

        val ADMIN_PREVIEW = UserExerciseVideoConfig(
            volume = 1f,
            isMuted = false,
            autoPlay = false,
            isLooping = true,
            repeatMode = "REPEAT_ONE",
            controlsEnabled = true,
            allowUserControl = true
        )
    }
}

fun getUserVideoPlayerConfig(interactiveControls: Boolean = false): UserExerciseVideoConfig {
    return if (interactiveControls) {
        UserExerciseVideoConfig.ADMIN_PREVIEW
    } else {
        UserExerciseVideoConfig.USER_DEFAULT
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutVideoPlayer(
    exercise: ExerciseItem,
    autoPlay: Boolean = true,
    interactiveControls: Boolean = false,
    modifier: Modifier = Modifier
) {
    val canonicalRevision = SampleExerciseData.canonicalRevision

    // 1. Lazy media resolution via unified pipeline with reactive revision key
    val resolvedMedia = remember(exercise.id, exercise.videoDemoUrl, exercise.media.videoUrl, exercise.media.thumbnailUrl, canonicalRevision) {
        ExerciseMediaResolver.resolve(exercise)
    }

    val mediaKey = remember(exercise.id, resolvedMedia) {
        when (resolvedMedia) {
            is ResolvedExerciseMedia.Video -> "video_${exercise.id}_${resolvedMedia.url}_${resolvedMedia.localResName}"
            is ResolvedExerciseMedia.Animation -> "anim_${exercise.id}_${resolvedMedia.url}"
            is ResolvedExerciseMedia.Thumbnail -> "thumb_${exercise.id}_${resolvedMedia.url}"
            is ResolvedExerciseMedia.Empty -> "empty_${exercise.id}"
        }
    }

    var mediaLoadingState by remember(mediaKey) {
        mutableStateOf(
            when (resolvedMedia) {
                is ResolvedExerciseMedia.Empty -> MediaLoadingState.EMPTY
                is ResolvedExerciseMedia.Video -> MediaLoadingState.LOADING
                is ResolvedExerciseMedia.Animation -> MediaLoadingState.SUCCESS
                is ResolvedExerciseMedia.Thumbnail -> MediaLoadingState.SUCCESS
            }
        )
    }

    var mediaErrorMessage by remember(mediaKey) { mutableStateOf<String?>(null) }

    val videoConfig = remember(interactiveControls) {
        getUserVideoPlayerConfig(interactiveControls)
    }

    // Real video playback states keyed on stable mediaKey to prevent teardown on recomposition
    var totalDurationMs by remember(mediaKey) { mutableLongStateOf(10050L) }
    var currentPositionMs by remember(mediaKey) { mutableLongStateOf(0L) }
    var pendingSeekMs by remember(mediaKey) { mutableStateOf<Long?>(null) }
    var isPlaying by remember(mediaKey, autoPlay) { mutableStateOf(autoPlay) }
    var isPlaybackEnded by remember(mediaKey) { mutableStateOf(false) }
    var hasStartedPlaying by remember(mediaKey) { mutableStateOf(false) }
    var isMuted by remember(interactiveControls) { mutableStateOf(videoConfig.isMuted) }
    var isFullscreen by remember { mutableStateOf(false) }
    var showControls by remember(interactiveControls) { mutableStateOf(interactiveControls) }

    // Auto-hide controls after 3.5s of inactivity when playing (only for interactive admin preview)
    LaunchedEffect(showControls, isPlaying, interactiveControls) {
        if (interactiveControls && showControls && isPlaying) {
            delay(3500L)
            showControls = false
        }
    }

    val progress = if (totalDurationMs > 0) {
        (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    // Fullscreen Dialog Handler (only available in interactiveControls mode)
    if (isFullscreen && interactiveControls) {
        Dialog(
            onDismissRequest = { isFullscreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF000000))
            ) {
                RenderMediaContent(
                    exercise = exercise,
                    resolvedMedia = resolvedMedia,
                    mediaLoadingState = mediaLoadingState,
                    mediaErrorMessage = mediaErrorMessage,
                    isPlaying = isPlaying,
                    hasStartedPlaying = hasStartedPlaying,
                    isPlaybackEnded = isPlaybackEnded,
                    isMuted = isMuted,
                    isLooping = videoConfig.isLooping,
                    isFullscreen = true,
                    interactiveControls = true,
                    showControls = showControls,
                    currentPositionMs = currentPositionMs,
                    totalDurationMs = totalDurationMs,
                    pendingSeekMs = pendingSeekMs,
                    progress = progress,
                    onProgressUpdate = { cur, tot ->
                        if (mediaLoadingState != MediaLoadingState.SUCCESS) {
                            mediaLoadingState = MediaLoadingState.SUCCESS
                            com.example.fitness.core.perf.AppPerfTracker.finish("EXERCISE_DETAIL_TO_MEDIA_READY", "Demonstration media ready")
                        }
                        if (!hasStartedPlaying) {
                            hasStartedPlaying = true
                        }
                        currentPositionMs = cur
                        if (tot > 0) totalDurationMs = tot
                    },
                    onTogglePlay = {
                        if (isPlaybackEnded) {
                            isPlaybackEnded = false
                            currentPositionMs = 0L
                            pendingSeekMs = 0L
                            isPlaying = true
                            hasStartedPlaying = true
                            showControls = true
                        } else {
                            isPlaying = !isPlaying
                            if (isPlaying) hasStartedPlaying = true
                            showControls = true
                        }
                    },
                    onPlaybackEnded = {
                        isPlaybackEnded = true
                        isPlaying = false
                        showControls = true
                    },
                    onToggleMute = { isMuted = !isMuted },
                    onToggleFullscreen = { isFullscreen = false },
                    onSeek = { newProgress ->
                        val targetMs = (newProgress * totalDurationMs).toLong()
                        currentPositionMs = targetMs
                        pendingSeekMs = targetMs
                    },
                    onSurfaceClick = { showControls = !showControls },
                    onError = { err ->
                        mediaErrorMessage = err
                        mediaLoadingState = MediaLoadingState.ERROR
                    },
                    onRetry = {
                        mediaErrorMessage = null
                        mediaLoadingState = MediaLoadingState.LOADING
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    } else {
        // Inline container (Standard 270dp hero)
        RenderMediaContent(
            exercise = exercise,
            resolvedMedia = resolvedMedia,
            mediaLoadingState = mediaLoadingState,
            mediaErrorMessage = mediaErrorMessage,
            isPlaying = isPlaying,
            hasStartedPlaying = hasStartedPlaying,
            isPlaybackEnded = isPlaybackEnded,
            isMuted = isMuted,
            isLooping = videoConfig.isLooping,
            isFullscreen = false,
            interactiveControls = interactiveControls,
            showControls = if (interactiveControls) showControls else false,
            currentPositionMs = currentPositionMs,
            totalDurationMs = totalDurationMs,
            pendingSeekMs = pendingSeekMs,
            progress = progress,
            onProgressUpdate = { cur, tot ->
                if (mediaLoadingState != MediaLoadingState.SUCCESS) {
                    mediaLoadingState = MediaLoadingState.SUCCESS
                    com.example.fitness.core.perf.AppPerfTracker.finish("EXERCISE_DETAIL_TO_MEDIA_READY", "Demonstration media ready")
                }
                if (!hasStartedPlaying) {
                    hasStartedPlaying = true
                }
                if (interactiveControls) {
                    currentPositionMs = cur
                    if (tot > 0) totalDurationMs = tot
                }
            },
            onTogglePlay = {
                if (interactiveControls) {
                    if (isPlaybackEnded) {
                        isPlaybackEnded = false
                        currentPositionMs = 0L
                        pendingSeekMs = 0L
                        isPlaying = true
                        hasStartedPlaying = true
                        showControls = true
                    } else {
                        isPlaying = !isPlaying
                        if (isPlaying) hasStartedPlaying = true
                        showControls = true
                    }
                }
            },
            onPlaybackEnded = {
                if (interactiveControls) {
                    isPlaybackEnded = true
                    isPlaying = false
                    showControls = true
                } else {
                    // Continuous infinite loop: never pause, never mark ended, never show controls
                    isPlaybackEnded = false
                    isPlaying = true
                    showControls = false
                }
            },
            onToggleMute = {
                if (interactiveControls) {
                    isMuted = !isMuted
                }
            },
            onToggleFullscreen = {
                if (interactiveControls) {
                    isFullscreen = true
                }
            },
            onSeek = { newProgress ->
                if (interactiveControls) {
                    val targetMs = (newProgress * totalDurationMs).toLong()
                    currentPositionMs = targetMs
                    pendingSeekMs = targetMs
                }
            },
            onSurfaceClick = {
                if (interactiveControls) {
                    showControls = !showControls
                }
            },
            onError = { err ->
                mediaErrorMessage = err
                mediaLoadingState = MediaLoadingState.ERROR
            },
            onRetry = {
                mediaErrorMessage = null
                mediaLoadingState = MediaLoadingState.LOADING
            },
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .heightIn(min = 180.dp, max = 270.dp)
                .clip(RoundedCornerShape(22.dp))
        )
    }
}

@Composable
private fun RenderMediaContent(
    exercise: ExerciseItem,
    resolvedMedia: ResolvedExerciseMedia,
    mediaLoadingState: MediaLoadingState,
    mediaErrorMessage: String?,
    isPlaying: Boolean,
    hasStartedPlaying: Boolean,
    isPlaybackEnded: Boolean,
    isMuted: Boolean,
    isLooping: Boolean = true,
    isFullscreen: Boolean,
    interactiveControls: Boolean = false,
    showControls: Boolean,
    currentPositionMs: Long,
    totalDurationMs: Long,
    pendingSeekMs: Long?,
    progress: Float,
    onProgressUpdate: (currentMs: Long, totalMs: Long) -> Unit,
    onTogglePlay: () -> Unit,
    onPlaybackEnded: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onSeek: (Float) -> Unit,
    onSurfaceClick: () -> Unit,
    onError: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        mediaLoadingState == MediaLoadingState.ERROR -> {
            ErrorMediaSurface(
                exercise = exercise,
                errorMessage = mediaErrorMessage,
                onRetry = onRetry,
                isFullscreen = isFullscreen,
                modifier = modifier
            )
        }
        resolvedMedia is ResolvedExerciseMedia.Empty || mediaLoadingState == MediaLoadingState.EMPTY -> {
            EmptyMediaSurface(
                exercise = exercise,
                isFullscreen = isFullscreen,
                interactiveControls = interactiveControls,
                modifier = modifier
            )
        }
        resolvedMedia is ResolvedExerciseMedia.Animation -> {
            AnimationMediaSurface(
                exercise = exercise,
                animation = resolvedMedia,
                isFullscreen = isFullscreen,
                modifier = modifier
            )
        }
        resolvedMedia is ResolvedExerciseMedia.Thumbnail -> {
            ThumbnailMediaSurface(
                exercise = exercise,
                thumbnail = resolvedMedia,
                isFullscreen = isFullscreen,
                interactiveControls = interactiveControls,
                modifier = modifier
            )
        }
        resolvedMedia is ResolvedExerciseMedia.Video -> {
            VideoPlayerSurface(
                exercise = exercise,
                videoMedia = resolvedMedia,
                isLoading = mediaLoadingState == MediaLoadingState.LOADING,
                isPlaying = isPlaying,
                hasStartedPlaying = hasStartedPlaying,
                isPlaybackEnded = isPlaybackEnded,
                isMuted = isMuted,
                isLooping = isLooping,
                isFullscreen = isFullscreen,
                interactiveControls = interactiveControls,
                showControls = showControls,
                currentPositionMs = currentPositionMs,
                totalDurationMs = totalDurationMs,
                pendingSeekMs = pendingSeekMs,
                progress = progress,
                onProgressUpdate = onProgressUpdate,
                onTogglePlay = onTogglePlay,
                onPlaybackEnded = onPlaybackEnded,
                onToggleMute = onToggleMute,
                onToggleFullscreen = onToggleFullscreen,
                onSeek = onSeek,
                onSurfaceClick = onSurfaceClick,
                onError = onError,
                modifier = modifier
            )
        }
    }
}

/**
 * High-contrast, clean athletic placeholder when no media is present.
 * Adheres strictly to:
 * - No broken video players
 * - No broken image icons
 * - No raw URLs
 * - No "undefined" or "null"
 * - Responsive at 320dp - 430dp and tablet
 * - Natural text wrapping
 */
@Composable
private fun EmptyMediaSurface(
    exercise: ExerciseItem,
    isFullscreen: Boolean,
    interactiveControls: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .background(Color(0xFF07090C))
            .semantics { contentDescription = "Exercise media placeholder" },
        shape = if (isFullscreen) RoundedCornerShape(0.dp) else RoundedCornerShape(22.dp),
        color = Color(0xFF07090C),
        border = if (isFullscreen) null else BorderStroke(1.2.dp, Color(0xFF1E2830))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // High-Resolution Exercise Imagery Backdrop
            Image(
                painter = painterResource(exercise.imageRes),
                contentDescription = exercise.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (interactiveControls) {
                // Cinematic dark gradient scrim for high contrast and crystal-clear text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color(0xFF07090C).copy(alpha = 0.78f),
                                    0.35f to Color(0xFF07090C).copy(alpha = 0.45f),
                                    0.70f to Color(0xFF07090C).copy(alpha = 0.72f),
                                    1.0f to Color(0xFF07090C).copy(alpha = 0.96f)
                                )
                            )
                        )
                )

            // Top Status Badge (Demonstration Queued)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF000000).copy(alpha = 0.85f),
                    border = BorderStroke(0.8.dp, Color(0xFF32414C))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                            text = "ATHGYM GUIDE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF000000).copy(alpha = 0.70f)
                ) {
                    Text(
                        text = "CANONICAL PRESCRIPTION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFA2B4C0),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }

            // Center: Athletic Exercise Target & Guidance Focus
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF10161C),
                    border = BorderStroke(1.2.dp, Color(0xFF22303A)),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FitnessIcons.Workout,
                            contentDescription = "Exercise media placeholder",
                            tint = FitTheme.colors.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Technique Demonstration Queued",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Perform with strict form, tempo & prescribed rest intervals below.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF8E9BA4),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom Overlay Details
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF000000).copy(alpha = 0.85f),
                                Color(0xFF000000).copy(alpha = 0.98f)
                            )
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = exercise.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = exercise.sets.ifBlank { "Prescribed sets" },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FitTheme.colors.primary
                    )
                    Text(
                        text = exercise.duration.ifBlank { "Form Focus" },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8E9BA4)
                    )
                }
            }
        }
    }
}
}

/**
 * Safe error fallback card if media playback fails.
 * Guarantees workout execution continues seamlessly.
 */
@Composable
private fun ErrorMediaSurface(
    exercise: ExerciseItem,
    errorMessage: String?,
    onRetry: () -> Unit,
    isFullscreen: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.background(Color(0xFF07090C)),
        shape = if (isFullscreen) RoundedCornerShape(0.dp) else RoundedCornerShape(22.dp),
        color = Color(0xFF07090C),
        border = if (isFullscreen) null else BorderStroke(1.2.dp, Color(0xFF332018))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1E1410),
                    border = BorderStroke(1.2.dp, Color(0xFF5A3018)),
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = FitnessIcons.Close,
                            contentDescription = "Media Error",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Demonstration Playback Unavailable",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Your workout continues normally. Follow the prescribed sets and technique instructions below.",
                    fontSize = 11.sp,
                    color = Color(0xFF8E9BA4),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    softWrap = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    onClick = onRetry,
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF161E24),
                    border = BorderStroke(1.dp, Color(0xFF2B3A42)),
                    modifier = Modifier.semantics { contentDescription = "Retry playback" }
                ) {
                    Text(
                        text = "Retry Playback",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FitTheme.colors.primary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/**
 * Surface for vector or animated exercise demonstrations.
 */
@Composable
private fun AnimationMediaSurface(
    exercise: ExerciseItem,
    animation: ResolvedExerciseMedia.Animation,
    isFullscreen: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.background(Color(0xFF07090C)),
        shape = if (isFullscreen) RoundedCornerShape(0.dp) else RoundedCornerShape(22.dp),
        color = Color(0xFF07090C),
        border = if (isFullscreen) null else BorderStroke(1.2.dp, Color(0xFF1E2830))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Poster frame fallback if available
            if (animation.posterUrl != null) {
                Image(
                    painter = painterResource(exercise.imageRes),
                    contentDescription = exercise.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Top Badge
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF000000).copy(alpha = 0.85f),
                    border = BorderStroke(0.8.dp, Color(0xFF32414C))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                            text = "ATHGYM ANIMATION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Center Animation Indicator
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF12191F),
                    border = BorderStroke(1.5.dp, Color(0xFF22303A)),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = FitnessIcons.Play,
                            contentDescription = "Animated Movement",
                            tint = FitTheme.colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Vector Demonstration",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Surface for static exercise thumbnail frames.
 */
@Composable
private fun ThumbnailMediaSurface(
    exercise: ExerciseItem,
    thumbnail: ResolvedExerciseMedia.Thumbnail,
    isFullscreen: Boolean,
    interactiveControls: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.background(Color(0xFF07090C)),
        shape = if (isFullscreen) RoundedCornerShape(0.dp) else RoundedCornerShape(22.dp),
        color = Color(0xFF07090C),
        border = if (isFullscreen) null else BorderStroke(1.2.dp, Color(0xFF1E2830))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(exercise.imageRes),
                contentDescription = exercise.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (interactiveControls) {
                // Cinematic Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color(0xFF000000).copy(alpha = 0.70f),
                                    0.35f to Color(0xFF000000).copy(alpha = 0.15f),
                                    0.70f to Color(0xFF000000).copy(alpha = 0.40f),
                                    1.0f to Color(0xFF000000).copy(alpha = 0.90f)
                                )
                            )
                        )
                )

                // Top Badge
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF000000).copy(alpha = 0.85f),
                        border = BorderStroke(0.8.dp, Color(0xFF32414C))
                    ) {
                        Text(
                            text = "POSTER PREVIEW",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Bottom title
                Text(
                    text = exercise.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = true,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                )
            }
        }
    }
}

/**
 * Interactive video player surface for video demonstrations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoPlayerSurface(
    exercise: ExerciseItem,
    videoMedia: ResolvedExerciseMedia.Video,
    isLoading: Boolean,
    isPlaying: Boolean,
    hasStartedPlaying: Boolean,
    isPlaybackEnded: Boolean,
    isMuted: Boolean,
    isLooping: Boolean = true,
    isFullscreen: Boolean,
    interactiveControls: Boolean = false,
    showControls: Boolean,
    currentPositionMs: Long,
    totalDurationMs: Long,
    pendingSeekMs: Long?,
    progress: Float,
    onProgressUpdate: (currentMs: Long, totalMs: Long) -> Unit,
    onTogglePlay: () -> Unit,
    onPlaybackEnded: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onSeek: (Float) -> Unit,
    onSurfaceClick: () -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val centralIcon = when {
        isPlaybackEnded -> FitnessIcons.Replay
        isPlaying -> FitnessIcons.Pause
        else -> FitnessIcons.Play
    }
    val centralDesc = when {
        isPlaybackEnded -> "Replay"
        isPlaying -> "Pause"
        else -> "Play"
    }

    Surface(
        modifier = modifier
            .background(Color(0xFF07090C))
            .then(
                if (interactiveControls) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSurfaceClick
                    )
                } else {
                    Modifier
                }
            ),
        shape = if (isFullscreen) RoundedCornerShape(0.dp) else RoundedCornerShape(22.dp),
        color = Color(0xFF07090C),
        border = if (isFullscreen) null else BorderStroke(1.2.dp, Color(0xFF1E2830))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(if (isFullscreen) RoundedCornerShape(0.dp) else RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Layer 1: Hardware-accelerated Platform Video Player
            PlatformVideoPlayer(
                videoResName = videoMedia.localResName ?: "",
                videoUrl = videoMedia.url,
                isPlaying = isPlaying,
                isMuted = isMuted,
                isLooping = isLooping,
                isFullscreen = isFullscreen,
                seekPositionMs = pendingSeekMs,
                onProgressUpdate = onProgressUpdate,
                onPlaybackEnded = onPlaybackEnded,
                onError = onError,
                modifier = Modifier.fillMaxSize()
            )

            // Layer 2: Poster frame before playback starts
            if (!hasStartedPlaying) {
                Image(
                    painter = painterResource(exercise.imageRes),
                    contentDescription = exercise.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF07090C))
                )
            }

            // Layer 3: Loading Indicator
            if (isLoading && !hasStartedPlaying) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    CircularProgressIndicator(
                        color = FitTheme.colors.primary,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Loading demonstration...",
                        fontSize = 11.sp,
                        color = Color(0xFF8E9BA4)
                    )
                }
            }

            // Layer 4-7: Interactive Overlays & Controls (Strictly Admin Preview only)
            if (interactiveControls) {
                // Layer 4: Cinematic Gradient Overlay for high-contrast controls
                Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color(0xFF000000).copy(alpha = 0.70f),
                                0.30f to Color(0xFF000000).copy(alpha = 0.15f),
                                0.65f to Color(0xFF000000).copy(alpha = 0.35f),
                                1.0f to Color(0xFF000000).copy(alpha = 0.90f)
                            )
                        )
                    )
            )

            // Layer 5: Top Badge
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF000000).copy(alpha = 0.80f),
                    border = BorderStroke(0.6.dp, Color(0xFF32414C))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) FitTheme.colors.primary else Color(0xFFFF4B4B))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val isUploadedVideo = videoMedia.url.contains("workers.dev") || !videoMedia.url.contains("demo")
                        val badgeText = when {
                            isPlaying && isUploadedVideo -> "PLAYING VIDEO"
                            isPlaying -> "PLAYING DEMO"
                            isUploadedVideo -> "EXERCISE VIDEO"
                            else -> "DEMO VIDEO"
                        }
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF000000).copy(alpha = 0.70f)
                ) {
                    Text(
                        text = "1080p • 60fps",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFA2B4C0),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }

            // Layer 6: Central Play / Pause Action Button
            AnimatedVisibility(
                visible = showControls || !isPlaying || isPlaybackEnded,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Surface(
                    onClick = onTogglePlay,
                    shape = CircleShape,
                    color = if (isPlaying) Color(0xFF000000).copy(alpha = 0.65f) else FitTheme.colors.primary,
                    border = BorderStroke(
                        1.5.dp,
                        if (isPlaying) Color(0xFF4A5D6B) else FitTheme.colors.primary
                    ),
                    modifier = Modifier
                        .size(54.dp)
                        .semantics { contentDescription = centralDesc }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = centralIcon,
                            contentDescription = centralDesc,
                            tint = if (isPlaying) Color.White else Color(0xFF000000),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Layer 7: Bottom Video Controls Panel
            AnimatedVisibility(
                visible = showControls || !isPlaying,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF000000).copy(alpha = 0.85f),
                                    Color(0xFF000000).copy(alpha = 0.98f)
                                )
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = exercise.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = true
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Seeker Progress Bar
                    Slider(
                        value = progress,
                        onValueChange = onSeek,
                        colors = SliderDefaults.colors(
                            thumbColor = FitTheme.colors.primary,
                            activeTrackColor = FitTheme.colors.primary,
                            inactiveTrackColor = Color(0xFF28343D)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                    )

                    // Control Row: Play/Pause, Timestamp, Mute, Fullscreen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Play/Pause & Real-time Timestamp
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                onClick = onTogglePlay,
                                shape = CircleShape,
                                color = Color(0xFF161F25),
                                modifier = Modifier
                                    .size(44.dp)
                                    .semantics { contentDescription = centralDesc }
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = centralIcon,
                                        contentDescription = centralDesc,
                                        tint = FitTheme.colors.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "${formatMs(currentPositionMs)} / ${formatMs(totalDurationMs)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFC5D1D9)
                            )
                        }

                        // Right: Sound Mute & Fullscreen
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = onToggleMute,
                                shape = CircleShape,
                                color = Color(0xFF161F25),
                                modifier = Modifier
                                    .size(44.dp)
                                    .semantics { contentDescription = if (isMuted) "Unmute" else "Mute" }
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isMuted) FitnessIcons.VolumeOff else FitnessIcons.VolumeUp,
                                        contentDescription = if (isMuted) "Unmute" else "Mute",
                                        tint = if (isMuted) Color(0xFFFF5252) else Color.White,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }

                            Surface(
                                onClick = onToggleFullscreen,
                                shape = CircleShape,
                                color = Color(0xFF161F25),
                                modifier = Modifier
                                    .size(44.dp)
                                    .semantics { contentDescription = if (isFullscreen) "Exit Fullscreen" else "Fullscreen" }
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isFullscreen) FitnessIcons.FullscreenExit else FitnessIcons.Fullscreen,
                                        contentDescription = if (isFullscreen) "Exit Fullscreen" else "Fullscreen",
                                        tint = Color.White,
                                        modifier = Modifier.size(17.dp)
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
}

private fun formatMs(millis: Long): String {
    val totalSeconds = (millis / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
