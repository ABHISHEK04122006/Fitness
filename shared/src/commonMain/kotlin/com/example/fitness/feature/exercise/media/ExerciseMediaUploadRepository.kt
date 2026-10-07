package com.example.fitness.feature.exercise.media

import com.example.fitness.feature.exercise.model.ExerciseMediaType

/**
 * Service contract for managing exercise media uploads, replacements, and removals.
 * Connects exercise assets to Firebase Storage and syncs metadata to Firestore exercises/{exerciseId}.
 */
interface ExerciseMediaUploadRepository {

    /**
     * Uploads a thumbnail image for an exercise.
     * Validates file, uploads to exercise-media/{exerciseId}/thumbnail,
     * and updates Firestore media.thumbnailUrl and mediaType.
     */
    suspend fun uploadThumbnail(exerciseId: String, file: MediaUploadFile): Result<String>

    /**
     * Uploads a vector/Lottie animation for an exercise.
     * Validates file, uploads to exercise-media/{exerciseId}/animation,
     * and updates Firestore media.animationUrl and mediaType.
     */
    suspend fun uploadAnimation(exerciseId: String, file: MediaUploadFile): Result<String>

    /**
     * Uploads an HD demonstration video for an exercise.
     * Validates file, uploads to exercise-media/{exerciseId}/video,
     * and updates Firestore media.videoUrl and mediaType.
     */
    suspend fun uploadVideo(exerciseId: String, file: MediaUploadFile): Result<String>

    /**
     * Replaces an existing media asset safely:
     * 1. Validates the new asset.
     * 2. Uploads the new asset to Storage.
     * 3. Retrieves download URL.
     * 4. Updates Firestore exercises/{exerciseId}.media metadata.
     * 5. Deletes old asset in Storage only after Firestore update succeeds.
     */
    suspend fun replaceMediaAsset(
        exerciseId: String,
        mediaType: ExerciseMediaType,
        newFile: MediaUploadFile
    ): Result<String>

    /**
     * Removes a media asset for an exercise and clears its URL in Firestore.
     */
    suspend fun removeMediaAsset(
        exerciseId: String,
        mediaType: ExerciseMediaType
    ): Result<Unit>
}
