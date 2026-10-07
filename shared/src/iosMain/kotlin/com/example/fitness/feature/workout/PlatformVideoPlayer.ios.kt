package com.example.fitness.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07090C)),
        contentAlignment = Alignment.Center
    ) {
        // Fallback for iOS compilation target
    }
}
