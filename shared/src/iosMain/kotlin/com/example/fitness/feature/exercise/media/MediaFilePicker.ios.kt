package com.example.fitness.feature.exercise.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.fitness.feature.exercise.model.ExerciseMediaType

@Composable
actual fun rememberMediaFilePickerLauncher(
    onFileSelected: (MediaUploadFile) -> Unit,
    onError: (String) -> Unit
): MediaFilePickerLauncher {
    return remember {
        object : MediaFilePickerLauncher {
            override fun launch(targetType: ExerciseMediaType) {
                // Provides interface contract on iOS. Real native picking on iOS can be wired to UIDocumentPickerViewController.
            }
        }
    }
}
