package com.example.fitness.feature.exercise.media

import com.example.fitness.feature.exercise.model.ExerciseMediaType

/**
 * Pre-upload validator for exercise media assets.
 * Enforces file size limits, MIME/extension constraints, non-empty payloads,
 * and stable exercise ID validation before any network upload begins.
 */
object ExerciseMediaValidator {

    const val MAX_THUMBNAIL_BYTES: Long = 5 * 1024 * 1024L       // 5 MB
    const val MAX_ANIMATION_BYTES: Long = 10 * 1024 * 1024L     // 10 MB
    const val MAX_VIDEO_BYTES: Long = 100 * 1024 * 1024L        // 100 MB

    private val SUPPORTED_IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")
    private val SUPPORTED_IMAGE_MIMES = setOf("image/jpeg", "image/png", "image/webp")

    private val SUPPORTED_ANIMATION_EXTENSIONS = setOf("json", "lottie", "zip")
    private val SUPPORTED_ANIMATION_MIMES = setOf("application/json", "application/zip", "application/octet-stream")

    private val SUPPORTED_VIDEO_EXTENSIONS = setOf("mp4", "webm", "mov")
    private val SUPPORTED_VIDEO_MIMES = setOf("video/mp4", "video/webm", "video/quicktime")

    /**
     * Validates an exercise document ID.
     */
    fun validateExerciseId(exerciseId: String): MediaValidationResult {
        val trimmed = exerciseId.trim()
        if (trimmed.isBlank()) {
            return MediaValidationResult.Invalid("Exercise ID cannot be blank.")
        }
        if (!trimmed.matches(Regex("^[a-z0-9_\\-]+$"))) {
            return MediaValidationResult.Invalid(
                "Exercise ID '$trimmed' contains invalid characters. Must be lowercase alphanumeric with underscores or hyphens."
            )
        }
        return MediaValidationResult.Valid
    }

    /**
     * Validates a media upload file against target media type constraints.
     */
    fun validateFile(
        file: MediaUploadFile,
        targetType: ExerciseMediaType
    ): MediaValidationResult {
        // 1. Media type validation
        if (targetType == ExerciseMediaType.NONE) {
            return MediaValidationResult.Invalid("Cannot upload media with type NONE.")
        }

        // 2. Non-empty check
        if (file.sizeBytes <= 0L) {
            return MediaValidationResult.Invalid("File payload cannot be empty (0 bytes).")
        }

        // 3. File extension extraction
        val extension = file.fileName.substringAfterLast('.', "").lowercase()
        if (extension.isBlank()) {
            return MediaValidationResult.Invalid("File must have a valid extension.")
        }

        val mime = file.mimeType.lowercase().trim()

        // 4. Type-specific validations
        return when (targetType) {
            ExerciseMediaType.IMAGE -> {
                if (file.sizeBytes > MAX_THUMBNAIL_BYTES) {
                    return MediaValidationResult.Invalid(
                        "Thumbnail file size (${file.sizeBytes} bytes) exceeds maximum limit of $MAX_THUMBNAIL_BYTES bytes (5 MB)."
                    )
                }
                if (extension !in SUPPORTED_IMAGE_EXTENSIONS && mime !in SUPPORTED_IMAGE_MIMES) {
                    return MediaValidationResult.Invalid(
                        "Unsupported image format: '.$extension' ($mime). Supported formats: jpg, jpeg, png, webp."
                    )
                }
                MediaValidationResult.Valid
            }
            ExerciseMediaType.ANIMATION -> {
                if (file.sizeBytes > MAX_ANIMATION_BYTES) {
                    return MediaValidationResult.Invalid(
                        "Animation file size (${file.sizeBytes} bytes) exceeds maximum limit of $MAX_ANIMATION_BYTES bytes (10 MB)."
                    )
                }
                if (extension !in SUPPORTED_ANIMATION_EXTENSIONS && mime !in SUPPORTED_ANIMATION_MIMES) {
                    return MediaValidationResult.Invalid(
                        "Unsupported animation format: '.$extension' ($mime). Supported formats: json, lottie, zip."
                    )
                }
                MediaValidationResult.Valid
            }
            ExerciseMediaType.VIDEO -> {
                if (file.sizeBytes > MAX_VIDEO_BYTES) {
                    return MediaValidationResult.Invalid(
                        "Video file size (${file.sizeBytes} bytes) exceeds maximum limit of $MAX_VIDEO_BYTES bytes (100 MB)."
                    )
                }
                if (extension !in SUPPORTED_VIDEO_EXTENSIONS && mime !in SUPPORTED_VIDEO_MIMES) {
                    return MediaValidationResult.Invalid(
                        "Unsupported video format: '.$extension' ($mime). Supported formats: mp4, webm, mov."
                    )
                }
                MediaValidationResult.Valid
            }
            ExerciseMediaType.NONE -> MediaValidationResult.Invalid("Invalid target media type.")
        }
    }

    /**
     * Complete validation combining exercise ID, target media type, and file payload.
     */
    fun validate(
        exerciseId: String,
        targetType: ExerciseMediaType,
        file: MediaUploadFile
    ): MediaValidationResult {
        val idResult = validateExerciseId(exerciseId)
        if (!idResult.isValid) return idResult

        return validateFile(file, targetType)
    }
}
