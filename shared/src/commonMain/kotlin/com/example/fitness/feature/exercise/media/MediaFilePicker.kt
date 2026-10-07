package com.example.fitness.feature.exercise.media

import androidx.compose.runtime.Composable
import com.example.fitness.feature.exercise.model.ExerciseMediaType

/**
 * Cross-platform launcher abstraction for opening native file pickers.
 */
interface MediaFilePickerLauncher {
    /**
     * Triggers the platform file picker filtered for the specified media type.
     */
    fun launch(targetType: ExerciseMediaType)
}

/**
 * Creates and remembers a platform-specific [MediaFilePickerLauncher].
 *
 * @param onFileSelected Callback invoked when an asset is chosen and read into a [MediaUploadFile].
 * @param onError Callback invoked when file picking or streaming fails.
 */
@Composable
expect fun rememberMediaFilePickerLauncher(
    onFileSelected: (MediaUploadFile) -> Unit,
    onError: (String) -> Unit = {}
): MediaFilePickerLauncher
