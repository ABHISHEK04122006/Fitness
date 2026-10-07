package com.example.fitness.feature.exercise.media

import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseMedia
import com.example.fitness.feature.workout.model.ExerciseItem

/**
 * Clean, production-ready sealed representation of resolved exercise media.
 * Follows strict priority order:
 * 1. Animation (animationUrl)
 * 2. Video (videoUrl)
 * 3. Thumbnail (thumbnailUrl)
 * 4. Empty (safe fallback state)
 */
sealed class ResolvedExerciseMedia {
    data class Animation(
        val url: String,
        val posterUrl: String? = null
    ) : ResolvedExerciseMedia()

    data class Video(
        val url: String,
        val posterUrl: String? = null,
        val localResName: String? = null
    ) : ResolvedExerciseMedia()

    data class Thumbnail(
        val url: String
    ) : ResolvedExerciseMedia()

    data object Empty : ResolvedExerciseMedia()
}

/**
 * Loading states for exercise media presentation components.
 */
enum class MediaLoadingState {
    LOADING,
    SUCCESS,
    EMPTY,
    ERROR
}

/**
 * Production Media Resolver providing a single, unified resolution path:
 * Exercise ID -> Exercise -> ExerciseMedia -> available media
 *
 * Enforces strict priority:
 * 1. animationUrl
 * 2. videoUrl
 * 3. thumbnailUrl
 * 4. Safe Empty state
 *
 * Guarantees zero media fabrication and safe empty state fallbacks.
 */
object ExerciseMediaResolver {

    /**
     * Resolves media directly from an [ExerciseMedia] model instance.
     * Enforces strict priority:
     * 1. animationUrl
     * 2. videoUrl
     * 3. thumbnailUrl
     * 4. Empty
     */
    fun resolve(media: ExerciseMedia?): ResolvedExerciseMedia {
        if (media == null) return ResolvedExerciseMedia.Empty

        val anim = media.animationUrl.trim()
        val video = media.videoUrl.trim()
        val thumb = media.thumbnailUrl.trim()

        return when {
            anim.isNotEmpty() -> ResolvedExerciseMedia.Animation(
                url = anim,
                posterUrl = thumb.ifEmpty { null }
            )
            video.isNotEmpty() -> ResolvedExerciseMedia.Video(
                url = video,
                posterUrl = thumb.ifEmpty { null },
                localResName = null
            )
            thumb.isNotEmpty() -> ResolvedExerciseMedia.Thumbnail(
                url = thumb
            )
            else -> ResolvedExerciseMedia.Empty
        }
    }

    /**
     * Resolves media from an [Exercise] entity.
     * Prioritizes canonical videoUrl over other media types.
     */
    fun resolve(exercise: Exercise?): ResolvedExerciseMedia {
        if (exercise == null) return ResolvedExerciseMedia.Empty
        val media = exercise.media
        val video = media.videoUrl.trim()
        if (video.isNotEmpty()) {
            return ResolvedExerciseMedia.Video(
                url = video,
                posterUrl = media.thumbnailUrl.trim().ifEmpty { null },
                localResName = null
            )
        }
        val canonical = SampleExerciseData.findCanonical(exercise.id, exercise.name)
        val canonicalVideo = canonical?.media?.videoUrl?.trim() ?: ""
        if (canonicalVideo.isNotEmpty()) {
            return ResolvedExerciseMedia.Video(
                url = canonicalVideo,
                posterUrl = canonical?.media?.thumbnailUrl?.trim()?.ifEmpty { null }
                    ?: media.thumbnailUrl.trim().ifEmpty { null },
                localResName = null
            )
        }
        val directMedia = resolve(media)
        if (directMedia !is ResolvedExerciseMedia.Empty) {
            return directMedia
        }
        return resolve(canonical?.media)
    }

    /**
     * Resolves media from an [ExerciseItem] presentation model.
     * Enforces strict priority:
     * 1. Direct canonical [exerciseItem.media.videoUrl]
     * 2. Canonical catalog lookup by stable ID or name: [SampleExerciseData.findCanonical]
     * 3. Non-video direct media (animation or thumbnail)
     * 4. Fallback/demo video ([exerciseItem.videoDemoUrl]) ONLY when canonical videoUrl is missing
     * 5. Catalog fallback for non-video assets
     * 6. Safe Empty
     */
    fun resolve(exerciseItem: ExerciseItem?): ResolvedExerciseMedia {
        if (exerciseItem == null) return ResolvedExerciseMedia.Empty

        // Priority 1: Direct canonical videoUrl on exerciseItem.media
        val directVideoUrl = exerciseItem.media.videoUrl.trim()
        if (directVideoUrl.isNotEmpty()) {
            return ResolvedExerciseMedia.Video(
                url = directVideoUrl,
                posterUrl = exerciseItem.media.thumbnailUrl.trim().ifEmpty { null },
                localResName = null
            )
        }

        // Priority 2: Canonical catalog lookup by stable exercise ID or name
        val canonical = SampleExerciseData.findCanonical(exerciseItem.id, exerciseItem.name)
        val canonicalVideo = canonical?.media?.videoUrl?.trim() ?: ""
        if (canonicalVideo.isNotEmpty()) {
            val canonicalPoster = canonical?.media?.thumbnailUrl?.trim()?.ifEmpty { null }
                ?: exerciseItem.media.thumbnailUrl.trim().ifEmpty { null }
            return ResolvedExerciseMedia.Video(
                url = canonicalVideo,
                posterUrl = canonicalPoster,
                localResName = null
            )
        }

        // Priority 3: Non-video direct media (animation or thumbnail)
        val fromDirectMedia = resolve(exerciseItem.media)
        if (fromDirectMedia !is ResolvedExerciseMedia.Empty) {
            return fromDirectMedia
        }

        // Priority 4: Existing fallback/demo video ONLY when canonical videoUrl is missing
        val fallbackDemoVideo = exerciseItem.videoDemoUrl.trim()
        if (fallbackDemoVideo.isNotEmpty()) {
            return ResolvedExerciseMedia.Video(
                url = fallbackDemoVideo,
                posterUrl = exerciseItem.media.thumbnailUrl.trim().ifEmpty { null },
                localResName = null
            )
        }

        // Priority 5: Catalog fallback for non-video assets
        if (canonical != null) {
            val fromCanonical = resolve(canonical.media)
            if (fromCanonical !is ResolvedExerciseMedia.Empty) {
                return fromCanonical
            }
        }
        if (exerciseItem.id.isNotBlank()) {
            return resolveFromId(exerciseItem.id)
        }

        return ResolvedExerciseMedia.Empty
    }

    /**
     * Resolves media from a stable [exerciseId] using the canonical exercise catalog.
     */
    fun resolveFromId(exerciseId: String): ResolvedExerciseMedia {
        val cleanId = exerciseId.trim()
        if (cleanId.isEmpty()) return ResolvedExerciseMedia.Empty

        val exercise = SampleExerciseData.findCanonical(cleanId)
        return resolve(exercise?.media)
    }
}
