package com.example.fitness.feature.exercise.media

import com.example.fitness.core.firebase.FitnessFirebase
import com.example.fitness.core.user.data.CloudflareProfilePhotoService
import com.example.fitness.feature.exercise.data.ExerciseRepository
import com.example.fitness.feature.exercise.data.FirebaseExerciseRepository
import com.example.fitness.feature.exercise.model.ExerciseMedia
import com.example.fitness.feature.exercise.model.ExerciseMediaType
import com.example.fitness.httpDeleteWithAuth
import com.example.fitness.httpPutBytesWithAuth

/**
 * Transport interface for exercise media HTTP operations to Cloudflare Worker R2.
 * Allows clean decoupling and mock injection for deterministic unit testing.
 */
interface ExerciseMediaHttpTransport {
    suspend fun put(url: String, bearerToken: String, mimeType: String, bytes: ByteArray): Pair<Int, String>
    suspend fun delete(url: String, bearerToken: String): Pair<Int, String>
}

/**
 * Default HTTP transport delegating to platform-specific HttpURLConnection implementations.
 */
object DefaultExerciseMediaHttpTransport : ExerciseMediaHttpTransport {
    override suspend fun put(url: String, bearerToken: String, mimeType: String, bytes: ByteArray): Pair<Int, String> =
        httpPutBytesWithAuth(url, bearerToken, mimeType, bytes)

    override suspend fun delete(url: String, bearerToken: String): Pair<Int, String> =
        httpDeleteWithAuth(url, bearerToken)
}

/**
 * Cloudflare Worker & R2 implementation of [ExerciseMediaUploadRepository].
 *
 * Architecture:
 * Admin Portal / App -> Cloudflare Worker -> Private R2 Bucket (athgym-media)
 *                    -> Firestore (exercises/{exerciseId}.media)
 *
 * Security:
 * - Admin authorization enforced by Cloudflare Worker using Firebase custom claim (admin == true).
 * - Client acquires fresh Firebase ID token via [tokenProvider] for each operation.
 * - Zero R2 secrets or Cloudflare credentials stored on client.
 * - Instructional exercise media served publicly via Cloudflare Worker HTTPS GET URL.
 */
class CloudflareExerciseMediaUploadRepository(
    private val exerciseRepository: ExerciseRepository = FirebaseExerciseRepository(),
    private val tokenProvider: suspend () -> String? = { FitnessFirebase.auth.currentUser?.getIdToken(true) },
    private val httpTransport: ExerciseMediaHttpTransport = DefaultExerciseMediaHttpTransport,
    private val workerBaseUrl: String = CloudflareProfilePhotoService.WORKER_BASE_URL
) : ExerciseMediaUploadRepository {

    override suspend fun uploadThumbnail(exerciseId: String, file: MediaUploadFile): Result<String> {
        return uploadInternal(
            exerciseId = exerciseId,
            targetType = ExerciseMediaType.IMAGE,
            file = file
        )
    }

    override suspend fun uploadAnimation(exerciseId: String, file: MediaUploadFile): Result<String> {
        return uploadInternal(
            exerciseId = exerciseId,
            targetType = ExerciseMediaType.ANIMATION,
            file = file
        )
    }

    override suspend fun uploadVideo(exerciseId: String, file: MediaUploadFile): Result<String> {
        return uploadInternal(
            exerciseId = exerciseId,
            targetType = ExerciseMediaType.VIDEO,
            file = file
        )
    }

    override suspend fun replaceMediaAsset(
        exerciseId: String,
        mediaType: ExerciseMediaType,
        newFile: MediaUploadFile
    ): Result<String> {
        return uploadInternal(
            exerciseId = exerciseId,
            targetType = mediaType,
            file = newFile
        )
    }

    override suspend fun removeMediaAsset(
        exerciseId: String,
        mediaType: ExerciseMediaType
    ): Result<Unit> {
        val stableId = exerciseId.trim()
        if (stableId.isBlank()) {
            return Result.failure(IllegalArgumentException("Exercise ID cannot be blank"))
        }

        val mediaTypeSlug = getMediaTypeSlug(mediaType)
            ?: return Result.failure(IllegalArgumentException("Cannot delete media with type NONE"))

        // 1. Verify exercise exists in repository
        val existingExercise = exerciseRepository.getExercise(stableId)
            ?: return Result.failure(NoSuchElementException("Exercise with ID '$stableId' not found"))

        // 2. Retrieve fresh Firebase ID token
        val token = tokenProvider()
        if (token.isNullOrBlank()) {
            return Result.failure(IllegalStateException("Authentication expired or no logged in user. Please sign in again."))
        }

        // 3. Prepare updated media model with target asset removed
        val currentMedia = existingExercise.media
        val updatedMedia = when (mediaType) {
            ExerciseMediaType.IMAGE -> currentMedia.copy(
                thumbnailUrl = "",
                mediaType = recalculateMediaType("", currentMedia.animationUrl, currentMedia.videoUrl).name
            )
            ExerciseMediaType.ANIMATION -> currentMedia.copy(
                animationUrl = "",
                mediaType = recalculateMediaType(currentMedia.thumbnailUrl, "", currentMedia.videoUrl).name
            )
            ExerciseMediaType.VIDEO -> currentMedia.copy(
                videoUrl = "",
                mediaType = recalculateMediaType(currentMedia.thumbnailUrl, currentMedia.animationUrl, "").name
            )
            ExerciseMediaType.NONE -> currentMedia
        }

        // 4. Update Firestore metadata (preserves all other exercise fields)
        val firestoreResult = exerciseRepository.updateExerciseMedia(stableId, updatedMedia)
        if (firestoreResult.isFailure) {
            return Result.failure(
                firestoreResult.exceptionOrNull()
                    ?: IllegalStateException("Failed to update Firestore media metadata for '$stableId'")
            )
        }

        // 5. Clean up asset via Cloudflare Worker DELETE endpoint (idempotent)
        val deleteUrl = "$workerBaseUrl/v1/admin/exercises/$stableId/media/$mediaTypeSlug"
        val (statusCode, responseBody) = httpTransport.delete(deleteUrl, token)

        return when (statusCode) {
            in 200..299, 404 -> Result.success(Unit)
            401 -> Result.failure(IllegalStateException("Unauthorized: Invalid or expired Firebase ID token."))
            403 -> Result.failure(IllegalStateException("Forbidden: Admin privileges required to delete exercise media."))
            else -> {
                println("[WARN] CloudflareExerciseMediaUploadRepository delete returned $statusCode: $responseBody")
                // Even if remote deletion had an error, Firestore document was already cleaned up
                Result.success(Unit)
            }
        }
    }

    private suspend fun uploadInternal(
        exerciseId: String,
        targetType: ExerciseMediaType,
        file: MediaUploadFile
    ): Result<String> {
        val stableId = exerciseId.trim()

        // Step 1: Strict input validation
        val validation = ExerciseMediaValidator.validate(stableId, targetType, file)
        if (validation is MediaValidationResult.Invalid) {
            return Result.failure(IllegalArgumentException("Validation failed: ${validation.reason}"))
        }

        val mediaTypeSlug = getMediaTypeSlug(targetType)
            ?: return Result.failure(IllegalArgumentException("Invalid target media type: $targetType"))

        // Step 2: Verify exercise exists in database before uploading
        val existingExercise = exerciseRepository.getExercise(stableId)
            ?: return Result.failure(NoSuchElementException("Exercise with ID '$stableId' not found"))

        // Step 3: Retrieve fresh Firebase ID token
        val token = tokenProvider()
        if (token.isNullOrBlank()) {
            return Result.failure(IllegalStateException("Authentication expired or no logged in user. Please sign in again."))
        }

        // Step 4: Upload to Cloudflare Worker R2 endpoint
        val uploadUrl = "$workerBaseUrl/v1/admin/exercises/$stableId/media/$mediaTypeSlug"
        val effectiveMime = when {
            file.mimeType.isNotBlank() -> file.mimeType
            targetType == ExerciseMediaType.IMAGE -> "image/jpeg"
            targetType == ExerciseMediaType.ANIMATION -> "application/json"
            targetType == ExerciseMediaType.VIDEO -> "video/mp4"
            else -> "application/octet-stream"
        }

        val (statusCode, responseBody) = httpTransport.put(
            url = uploadUrl,
            bearerToken = token,
            mimeType = effectiveMime,
            bytes = file.bytes
        )

        if (statusCode !in 200..299) {
            return when (statusCode) {
                401 -> Result.failure(IllegalStateException("Unauthorized: Invalid or expired Firebase ID token."))
                403 -> Result.failure(IllegalStateException("Forbidden: Admin privileges required to upload exercise media."))
                400 -> Result.failure(IllegalArgumentException("Bad request ($responseBody)"))
                413 -> Result.failure(IllegalArgumentException("Payload exceeds maximum size limit for $mediaTypeSlug"))
                else -> Result.failure(IllegalStateException("Upload failed with status $statusCode: $responseBody"))
            }
        }

        // Step 5: Construct the public Worker-served HTTPS URL
        val publicServedUrl = "$workerBaseUrl/v1/exercises/$stableId/media/$mediaTypeSlug"

        // Step 6: Build updated ExerciseMedia metadata
        val currentMedia = existingExercise.media
        val currentType = ExerciseMediaType.fromString(currentMedia.mediaType) ?: ExerciseMediaType.NONE
        val updatedMedia = when (targetType) {
            ExerciseMediaType.IMAGE -> currentMedia.copy(
                thumbnailUrl = publicServedUrl,
                mediaType = if (currentType == ExerciseMediaType.NONE) ExerciseMediaType.IMAGE.name else currentMedia.mediaType
            )
            ExerciseMediaType.ANIMATION -> currentMedia.copy(
                animationUrl = publicServedUrl,
                mediaType = ExerciseMediaType.ANIMATION.name
            )
            ExerciseMediaType.VIDEO -> currentMedia.copy(
                videoUrl = publicServedUrl,
                mediaType = ExerciseMediaType.VIDEO.name
            )
            ExerciseMediaType.NONE -> currentMedia
        }

        // Step 7: Update Firestore document exercises/{exerciseId}.media (preserves all other exercise fields)
        val firestoreUpdateResult = exerciseRepository.updateExerciseMedia(stableId, updatedMedia)
        if (firestoreUpdateResult.isFailure) {
            return Result.failure(
                firestoreUpdateResult.exceptionOrNull()
                    ?: IllegalStateException("Failed to sync media download URL to Firestore")
            )
        }

        return Result.success(publicServedUrl)
    }

    private fun getMediaTypeSlug(type: ExerciseMediaType): String? = when (type) {
        ExerciseMediaType.IMAGE -> "thumbnail"
        ExerciseMediaType.ANIMATION -> "animation"
        ExerciseMediaType.VIDEO -> "video"
        ExerciseMediaType.NONE -> null
    }

    private fun recalculateMediaType(
        thumbnailUrl: String,
        animationUrl: String,
        videoUrl: String
    ): ExerciseMediaType {
        return when {
            videoUrl.isNotBlank() -> ExerciseMediaType.VIDEO
            animationUrl.isNotBlank() -> ExerciseMediaType.ANIMATION
            thumbnailUrl.isNotBlank() -> ExerciseMediaType.IMAGE
            else -> ExerciseMediaType.NONE
        }
    }
}
