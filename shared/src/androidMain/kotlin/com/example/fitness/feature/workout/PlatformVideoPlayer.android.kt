package com.example.fitness.feature.workout

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
actual fun PlatformVideoPlayer(
    videoResName: String,
    videoUrl: String,
    isPlaying: Boolean,
    isMuted: Boolean,
    isLooping: Boolean,
    isFullscreen: Boolean,
    seekPositionMs: Long?,
    onProgressUpdate: (currentMs: Long, totalMs: Long) -> Unit,
    onPlaybackEnded: () -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val currentOnProgressUpdate by rememberUpdatedState(onProgressUpdate)
    val currentOnPlaybackEnded by rememberUpdatedState(onPlaybackEnded)
    val currentOnError by rememberUpdatedState(onError)

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPrepared by remember { mutableStateOf(false) }
    var surfaceTextureRef by remember { mutableStateOf<SurfaceTexture?>(null) }
    var textureViewRef by remember { mutableStateOf<TextureView?>(null) }

    var hasAttemptedFallback by remember(videoResName, videoUrl) { mutableStateOf(false) }

    // Resolve video URI: Network URL takes priority if provided, else raw resource
    val videoUri = remember(videoResName, videoUrl) {
        val cleanUrl = videoUrl.trim()
        val effectiveRes = videoResName.trim()
        if (cleanUrl.startsWith("http://", ignoreCase = true) || cleanUrl.startsWith("https://", ignoreCase = true)) {
            if (cleanUrl.contains("fitness.app/demo", ignoreCase = true)) {
                if (effectiveRes.isNotBlank()) {
                    val resId = context.resources.getIdentifier(effectiveRes, "raw", context.packageName)
                    if (resId != 0) {
                        Uri.parse("android.resource://${context.packageName}/$resId")
                    } else {
                        Uri.parse("android.resource://${context.packageName}/raw/$effectiveRes")
                    }
                } else {
                    null
                }
            } else {
                Uri.parse(cleanUrl)
            }
        } else if (cleanUrl.startsWith("android.resource://")) {
            val targetRes = cleanUrl.removePrefix("android.resource://").trim()
            val resNameToUse = if (targetRes.isNotBlank() && !targetRes.contains("/")) targetRes else effectiveRes
            if (resNameToUse.isNotBlank()) {
                val resId = context.resources.getIdentifier(resNameToUse, "raw", context.packageName)
                if (resId != 0) {
                    Uri.parse("android.resource://${context.packageName}/$resId")
                } else {
                    Uri.parse("android.resource://${context.packageName}/raw/$resNameToUse")
                }
            } else {
                null
            }
        } else if (effectiveRes.isNotBlank()) {
            val resId = context.resources.getIdentifier(effectiveRes, "raw", context.packageName)
            if (resId != 0) {
                Uri.parse("android.resource://${context.packageName}/$resId")
            } else {
                Uri.parse("android.resource://${context.packageName}/raw/$effectiveRes")
            }
        } else {
            null
        }
    }

    // Adjust TextureView matrix to CenterCrop: full fit on the video block without letterboxing or black bars
    fun adjustAspectRatio(
        textureView: TextureView,
        videoWidth: Int,
        videoHeight: Int
    ) {
        val viewWidth = textureView.width
        val viewHeight = textureView.height
        if (viewWidth <= 0 || viewHeight <= 0 || videoWidth <= 0 || videoHeight <= 0) return

        val scaleX: Float
        val scaleY: Float

        val videoRatio = videoWidth.toFloat() / videoHeight.toFloat()
        val viewRatio = viewWidth.toFloat() / viewHeight.toFloat()

        if (viewRatio > videoRatio) {
            // Container is wider than video: fill width (1.0f), expand height to fully cover container
            scaleX = 1.0f
            scaleY = (viewRatio / videoRatio)
        } else {
            // Container is taller than video: fill height (1.0f), expand width to fully cover container
            scaleY = 1.0f
            scaleX = (videoRatio / viewRatio)
        }

        val matrix = Matrix()
        // Scale around center
        matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
        textureView.setTransform(matrix)
    }

    // Manage MediaPlayer lifecycle
    DisposableEffect(videoUri) {
        val uri = videoUri
        if (uri == null) {
            onDispose {}
        } else {
            val player = MediaPlayer()
            mediaPlayer = player

            try {
                val cleanUrl = videoUrl.trim()
                if (cleanUrl.startsWith("http://", ignoreCase = true) || cleanUrl.startsWith("https://", ignoreCase = true)) {
                    val headers = mapOf(
                        "User-Agent" to "ATHGYM-Android/1.0",
                        "Accept" to "*/*"
                    )
                    player.setDataSource(context, uri, headers)
                } else {
                    player.setDataSource(context, uri)
                }
                player.isLooping = isLooping
                player.setOnErrorListener { _, what, extra ->
                    if (!hasAttemptedFallback && videoResName.isNotBlank()) {
                        hasAttemptedFallback = true
                        try {
                            val fallbackRes = videoResName.trim()
                            val resId = context.resources.getIdentifier(fallbackRes, "raw", context.packageName)
                            val fallbackUri = if (resId != 0) {
                                Uri.parse("android.resource://${context.packageName}/$resId")
                            } else {
                                Uri.parse("android.resource://${context.packageName}/raw/$fallbackRes")
                            }
                            player.reset()
                            player.setDataSource(context, fallbackUri)
                            player.isLooping = isLooping
                            val currentSurfaceTexture = surfaceTextureRef ?: textureViewRef?.takeIf { it.isAvailable }?.surfaceTexture
                            if (currentSurfaceTexture != null) {
                                player.setSurface(Surface(currentSurfaceTexture))
                            }
                            player.prepareAsync()
                            return@setOnErrorListener true
                        } catch (_: Exception) {}
                    }
                    currentOnError("MediaPlayer error: what=$what, extra=$extra")
                    true
                }
                player.setOnPreparedListener { mp ->
                    isPrepared = true
                    val vol = if (isMuted) 0f else 1f
                    mp.setVolume(vol, vol)
                    textureViewRef?.let { tv ->
                        adjustAspectRatio(tv, mp.videoWidth.takeIf { it > 0 } ?: 368, mp.videoHeight.takeIf { it > 0 } ?: 384)
                    }
                    if (isPlaying) {
                        mp.start()
                    }
                    try {
                        val duration = mp.duration.toLong()
                        currentOnProgressUpdate(0L, if (duration > 0) duration else 10000L)
                    } catch (_: Exception) {}
                }
                player.setOnCompletionListener { mp ->
                    if (isLooping) {
                        try {
                            mp.seekTo(0)
                            mp.start()
                        } catch (_: Exception) {}
                    }
                    currentOnPlaybackEnded()
                }
                player.setOnVideoSizeChangedListener { mp, width, height ->
                    textureViewRef?.let { tv ->
                        adjustAspectRatio(tv, width, height)
                    }
                }
                val currentSurfaceTexture = surfaceTextureRef ?: textureViewRef?.takeIf { it.isAvailable }?.surfaceTexture
                if (currentSurfaceTexture != null) {
                    try {
                        player.setSurface(Surface(currentSurfaceTexture))
                    } catch (_: Exception) {}
                }
                player.prepareAsync()
            } catch (e: Exception) {
                currentOnError(e.message ?: "Failed to initialize player")
            }

            onDispose {
                try {
                    isPrepared = false
                    player.stop()
                    player.reset()
                    player.release()
                } catch (e: Exception) {
                    // Ignore cleanup errors
                }
                mediaPlayer = null
            }
        }
    }

    // Observe Activity lifecycle to pause playback on ON_PAUSE/ON_STOP and resume on ON_RESUME
    DisposableEffect(context) {
        val lifecycleOwner = generateSequence(context) { (it as? android.content.ContextWrapper)?.baseContext }
            .filterIsInstance<androidx.lifecycle.LifecycleOwner>()
            .firstOrNull()

        if (lifecycleOwner != null) {
            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                when (event) {
                    androidx.lifecycle.Lifecycle.Event.ON_PAUSE,
                    androidx.lifecycle.Lifecycle.Event.ON_STOP -> {
                        try {
                            if (mediaPlayer?.isPlaying == true) {
                                mediaPlayer?.pause()
                            }
                        } catch (_: Exception) {}
                    }
                    androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {
                        try {
                            if (isPlaying && isPrepared && mediaPlayer?.isPlaying == false) {
                                mediaPlayer?.start()
                            }
                        } catch (_: Exception) {}
                    }
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        } else {
            onDispose {}
        }
    }

    // Play/Pause sync
    LaunchedEffect(isPlaying, isPrepared) {
        val player = mediaPlayer
        if (player != null && isPrepared) {
            try {
                if (isPlaying && !player.isPlaying) {
                    player.start()
                } else if (!isPlaying && player.isPlaying) {
                    player.pause()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Mute/Unmute sync
    LaunchedEffect(isMuted, isPrepared) {
        val player = mediaPlayer
        if (player != null && isPrepared) {
            try {
                val vol = if (isMuted) 0f else 1f
                player.setVolume(vol, vol)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Seeking sync
    LaunchedEffect(seekPositionMs) {
        val player = mediaPlayer
        if (player != null && isPrepared && seekPositionMs != null) {
            try {
                player.seekTo(seekPositionMs.toInt())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Real-time progress update loop
    LaunchedEffect(isPlaying, isPrepared) {
        while (isActive && isPlaying && isPrepared) {
            val player = mediaPlayer
            if (player != null) {
                try {
                    val currentPos = player.currentPosition.toLong()
                    val duration = player.duration.toLong()
                    if (duration > 0) {
                        currentOnProgressUpdate(currentPos, duration)
                    }
                } catch (e: Exception) {
                    // Ignore position read error during seek
                }
            }
            delay(250L)
        }
    }

    Box(
        modifier = modifier.background(Color(0xFF07090C)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    textureViewRef = this
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                            surfaceTextureRef = surface
                            mediaPlayer?.setSurface(Surface(surface))
                            mediaPlayer?.let { mp ->
                                adjustAspectRatio(
                                    this@apply,
                                    mp.videoWidth.takeIf { it > 0 } ?: 368,
                                    mp.videoHeight.takeIf { it > 0 } ?: 384
                                )
                            }
                        }

                        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                            mediaPlayer?.let { mp ->
                                adjustAspectRatio(
                                    this@apply,
                                    mp.videoWidth.takeIf { it > 0 } ?: 368,
                                    mp.videoHeight.takeIf { it > 0 } ?: 384
                                )
                            }
                        }

                        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                            surfaceTextureRef = null
                            mediaPlayer?.setSurface(null)
                            return true
                        }

                        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                    }
                }
            },
            update = { tv ->
                textureViewRef = tv
                if (tv.isAvailable && tv.surfaceTexture != null) {
                    surfaceTextureRef = tv.surfaceTexture
                    try {
                        mediaPlayer?.setSurface(Surface(tv.surfaceTexture))
                    } catch (_: Exception) {}
                }
                mediaPlayer?.let { mp ->
                    adjustAspectRatio(
                        tv,
                        mp.videoWidth.takeIf { it > 0 } ?: 368,
                        mp.videoHeight.takeIf { it > 0 } ?: 384
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
