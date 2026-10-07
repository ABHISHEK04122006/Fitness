package com.example.fitness.feature.exercise.media

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.fitness.feature.exercise.model.ExerciseMediaType

@Composable
actual fun rememberMediaFilePickerLauncher(
    onFileSelected: (MediaUploadFile) -> Unit,
    onError: (String) -> Unit
): MediaFilePickerLauncher {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        println("[ProfileImageDebug] STEP 3 URI received")
        try {
            val contentResolver = context.contentResolver
            var fileName = "media_asset"
            var sizeBytes = 0L

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex >= 0) fileName = cursor.getString(nameIndex) ?: fileName
                    if (sizeIndex >= 0) sizeBytes = cursor.getLong(sizeIndex)
                }
            }

            val mimeType = contentResolver.getType(uri) ?: when {
                fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
                fileName.endsWith(".png", true) -> "image/png"
                fileName.endsWith(".webp", true) -> "image/webp"
                fileName.endsWith(".json", true) -> "application/json"
                fileName.endsWith(".lottie", true) -> "application/json"
                fileName.endsWith(".zip", true) -> "application/zip"
                fileName.endsWith(".mp4", true) -> "video/mp4"
                fileName.endsWith(".webm", true) -> "video/webm"
                fileName.endsWith(".mov", true) -> "video/quicktime"
                else -> "application/octet-stream"
            }

            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes == null || bytes.isEmpty()) {
                println("[ProfileImageDebug] FAILED AT STEP 4")
                println("[ProfileImageDebug] exceptionClass = IOException")
                println("[ProfileImageDebug] message = Selected file is empty or could not be read")
                onError("Selected file is empty or could not be read.")
                return@rememberLauncherForActivityResult
            }
            println("[ProfileImageDebug] STEP 4 image bytes loaded")
            println("[ProfileImageDebug] imageBytesSize = ${bytes.size}")

            val actualSize = if (sizeBytes > 0) sizeBytes else bytes.size.toLong()

            val uploadFile = MediaUploadFile(
                fileName = fileName,
                mimeType = mimeType,
                sizeBytes = actualSize,
                bytes = bytes
            )
            onFileSelected(uploadFile)
        } catch (e: Exception) {
            println("[ProfileImageDebug] FAILED AT STEP 4")
            println("[ProfileImageDebug] exceptionClass = ${e::class.qualifiedName ?: e::class.simpleName}")
            println("[ProfileImageDebug] message = ${e.message}")
            e.printStackTrace()
            onError("Failed to read selected file: ${e.message ?: "Unknown error"}")
        }
    }

    return remember(launcher) {
        object : MediaFilePickerLauncher {
            override fun launch(targetType: ExerciseMediaType) {
                val inputMime = when (targetType) {
                    ExerciseMediaType.IMAGE -> "image/*"
                    ExerciseMediaType.ANIMATION -> "*/*"
                    ExerciseMediaType.VIDEO -> "video/*"
                    ExerciseMediaType.NONE -> "*/*"
                }
                launcher.launch(inputMime)
            }
        }
    }
}
