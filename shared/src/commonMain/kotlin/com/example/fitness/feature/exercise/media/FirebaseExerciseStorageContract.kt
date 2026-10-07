package com.example.fitness.feature.exercise.media

import com.example.fitness.feature.exercise.model.ExerciseMediaType

/**
 * Architectural specification and path contract for Firebase Storage exercise media.
 *
 * Expected Firebase Storage structure:
 * exercise-media/
 *     {exerciseId}/
 *         thumbnail
 *         animation
 *         video
 *
 * This contract establishes consistent object hierarchy for future asset uploads
 * without fabricating URLs or storing local credentials.
 */
object FirebaseExerciseStorageContract {

    /**
     * Root storage bucket directory for all exercise media demonstrations.
     */
    const val ROOT_COLLECTION = "exercise-media"

    /**
     * Standardized file keys within an exercise media directory.
     */
    const val THUMBNAIL_NAME = "thumbnail"
    const val ANIMATION_NAME = "animation"
    const val VIDEO_NAME = "video"

    /**
     * Resolves the base Storage directory for an exercise.
     * e.g. "exercise-media/barbell_bench_press"
     */
    fun getExerciseDirectory(exerciseId: String): String {
        return "$ROOT_COLLECTION/${exerciseId.trim()}"
    }

    /**
     * Resolves the canonical Storage path for a thumbnail image.
     * e.g. "exercise-media/barbell_bench_press/thumbnail"
     */
    fun getThumbnailPath(exerciseId: String): String {
        return "${getExerciseDirectory(exerciseId)}/$THUMBNAIL_NAME"
    }

    /**
     * Resolves the canonical Storage path for a vector/Lottie animation.
     * e.g. "exercise-media/barbell_bench_press/animation"
     */
    fun getAnimationPath(exerciseId: String): String {
        return "${getExerciseDirectory(exerciseId)}/$ANIMATION_NAME"
    }

    /**
     * Resolves the canonical Storage path for an HD video demonstration.
     * e.g. "exercise-media/barbell_bench_press/video"
     */
    fun getVideoPath(exerciseId: String): String {
        return "${getExerciseDirectory(exerciseId)}/$VIDEO_NAME"
    }

    /**
     * Resolves the canonical Storage path for a given [ExerciseMediaType].
     */
    fun getStoragePathForType(exerciseId: String, mediaType: ExerciseMediaType): String {
        return when (mediaType) {
            ExerciseMediaType.IMAGE -> getThumbnailPath(exerciseId)
            ExerciseMediaType.ANIMATION -> getAnimationPath(exerciseId)
            ExerciseMediaType.VIDEO -> getVideoPath(exerciseId)
            ExerciseMediaType.NONE -> getExerciseDirectory(exerciseId)
        }
    }
}
