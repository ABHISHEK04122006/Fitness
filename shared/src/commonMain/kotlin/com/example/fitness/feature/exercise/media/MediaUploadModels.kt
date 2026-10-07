package com.example.fitness.feature.exercise.media

import com.example.fitness.feature.exercise.model.ExerciseMedia
import com.example.fitness.feature.exercise.model.ExerciseMediaType

/**
 * Cross-platform representation of an exercise media asset to be uploaded.
 *
 * @property fileName Name of the file with extension (e.g. "bench_press.mp4").
 * @property mimeType Standard MIME type (e.g. "video/mp4", "image/jpeg").
 * @property sizeBytes Size of the file payload in bytes.
 * @property bytes The raw byte payload of the asset.
 */
data class MediaUploadFile(
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val bytes: ByteArray = ByteArray(0)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MediaUploadFile) return false
        if (fileName != other.fileName) return false
        if (mimeType != other.mimeType) return false
        if (sizeBytes != other.sizeBytes) return false
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + sizeBytes.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}

/**
 * Result of pre-upload file validation.
 */
sealed class MediaValidationResult {
    data object Valid : MediaValidationResult()
    data class Invalid(val reason: String) : MediaValidationResult()

    val isValid: Boolean get() = this is Valid
}

/**
 * Result of a media asset upload or replacement operation.
 */
sealed class MediaUploadResult {
    data class Success(
        val exerciseId: String,
        val mediaType: ExerciseMediaType,
        val downloadUrl: String,
        val updatedMedia: ExerciseMedia
    ) : MediaUploadResult()

    data class Failure(
        val exerciseId: String,
        val errorMessage: String,
        val cause: Throwable? = null
    ) : MediaUploadResult()
}
