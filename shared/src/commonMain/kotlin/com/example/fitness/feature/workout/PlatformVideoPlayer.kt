package com.example.fitness.feature.workout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Cross-platform native video player composable.
 * Renders hardware-accelerated video playback on Android and fallback/AVPlayer on iOS.
 *
 * @param videoResName The name of the raw video resource (e.g. "gym" for res/raw/gym.mp4)
 * @param videoUrl Optional network URL or streaming link (MP4, HLS)
 * @param isPlaying Whether the video is currently playing
 * @param isMuted Whether the audio should be muted
 * @param isFullscreen Whether the player is currently rendered in fullscreen mode
 * @param seekPositionMs Optional millisecond position to seek to
 * @param onProgressUpdate Callback invoked periodically with (currentPositionMs, totalDurationMs)
 * @param onPlaybackEnded Callback invoked when video finishes playing
 * @param onError Callback invoked when video playback fails (graceful degradation)
 * @param modifier Compose modifier for styling, sizing, and clipping
 */
@Composable
expect fun PlatformVideoPlayer(
    videoResName: String = "",
    videoUrl: String = "",
    isPlaying: Boolean,
    isMuted: Boolean,
    isLooping: Boolean = true,
    isFullscreen: Boolean = false,
    seekPositionMs: Long? = null,
    onProgressUpdate: (currentMs: Long, totalMs: Long) -> Unit = { _, _ -> },
    onPlaybackEnded: () -> Unit = {},
    onError: (String) -> Unit = {},
    modifier: Modifier = Modifier
)
