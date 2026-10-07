package com.example.fitness.feature.exercise.audit

import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseMedia

/**
 * High-level media readiness status of an exercise.
 */
enum class MediaReadiness {
    NO_MEDIA,
    PARTIAL,
    COMPLETE
}

/**
 * Detailed breakdown of which media combinations are populated for an exercise.
 */
enum class DetailedMediaStatus {
    NO_MEDIA,
    THUMBNAIL_ONLY,
    ANIMATION_ONLY,
    VIDEO_ONLY,
    THUMBNAIL_ANIMATION,
    THUMBNAIL_VIDEO,
    ANIMATION_VIDEO,
    ALL_MEDIA
}

/**
 * Audit record for an individual exercise, evaluating its media completeness.
 *
 * @property exercise The underlying exercise definition.
 * @property hasThumbnail Whether a non-blank thumbnail URL is configured.
 * @property hasAnimation Whether a non-blank animation URL is configured.
 * @property hasVideo Whether a non-blank video URL is configured.
 * @property mediaCount Count of populated media assets (0 to 3).
 * @property readiness High-level readiness category (NO_MEDIA, PARTIAL, COMPLETE).
 * @property detailedStatus Detailed multi-slot configuration status.
 */
data class ExerciseMediaAuditItem(
    val exercise: Exercise,
    val hasThumbnail: Boolean,
    val hasAnimation: Boolean,
    val hasVideo: Boolean,
    val mediaCount: Int,
    val readiness: MediaReadiness,
    val detailedStatus: DetailedMediaStatus
)

/**
 * Aggregate metrics summarizing media completeness across a collection of exercises.
 */
data class ContentAuditSummary(
    val totalExercises: Int,
    val withThumbnail: Int,
    val withAnimation: Int,
    val withVideo: Int,
    val withAnyMedia: Int,
    val withNoMedia: Int,
    val completeMedia: Int,
    val partialMedia: Int
)

/**
 * Content auditor utility for calculating completeness metrics and readiness statuses.
 */
object ExerciseContentAuditor {

    /**
     * Audits an individual exercise, computing slot booleans, media count (0..3),
     * high-level readiness, and detailed combination status.
     */
    fun auditExercise(exercise: Exercise): ExerciseMediaAuditItem {
        val hasThumb = exercise.media.thumbnailUrl.isNotBlank()
        val hasAnim = exercise.media.animationUrl.isNotBlank()
        val hasVid = exercise.media.videoUrl.isNotBlank()

        val count = (if (hasThumb) 1 else 0) + (if (hasAnim) 1 else 0) + (if (hasVid) 1 else 0)

        val readiness = when (count) {
            0 -> MediaReadiness.NO_MEDIA
            3 -> MediaReadiness.COMPLETE
            else -> MediaReadiness.PARTIAL
        }

        val detailed = when {
            count == 0 -> DetailedMediaStatus.NO_MEDIA
            count == 3 -> DetailedMediaStatus.ALL_MEDIA
            hasThumb && hasAnim -> DetailedMediaStatus.THUMBNAIL_ANIMATION
            hasThumb && hasVid -> DetailedMediaStatus.THUMBNAIL_VIDEO
            hasAnim && hasVid -> DetailedMediaStatus.ANIMATION_VIDEO
            hasThumb -> DetailedMediaStatus.THUMBNAIL_ONLY
            hasAnim -> DetailedMediaStatus.ANIMATION_ONLY
            hasVid -> DetailedMediaStatus.VIDEO_ONLY
            else -> DetailedMediaStatus.NO_MEDIA
        }

        return ExerciseMediaAuditItem(
            exercise = exercise,
            hasThumbnail = hasThumb,
            hasAnimation = hasAnim,
            hasVideo = hasVid,
            mediaCount = count,
            readiness = readiness,
            detailedStatus = detailed
        )
    }

    /**
     * Aggregates audit results across an entire collection of exercises.
     */
    fun summarizeAudit(exercises: List<Exercise>): ContentAuditSummary {
        val audited = exercises.map { auditExercise(it) }
        return ContentAuditSummary(
            totalExercises = audited.size,
            withThumbnail = audited.count { it.hasThumbnail },
            withAnimation = audited.count { it.hasAnimation },
            withVideo = audited.count { it.hasVideo },
            withAnyMedia = audited.count { it.mediaCount > 0 },
            withNoMedia = audited.count { it.mediaCount == 0 },
            completeMedia = audited.count { it.readiness == MediaReadiness.COMPLETE },
            partialMedia = audited.count { it.readiness == MediaReadiness.PARTIAL }
        )
    }
}

/**
 * Filter options for Exercise Content & Media Audits.
 */
enum class ContentAuditFilter(val title: String) {
    ALL("All"),
    NO_MEDIA("No Media"),
    THUMBNAIL_MISSING("Thumbnail Missing"),
    ANIMATION_MISSING("Animation Missing"),
    VIDEO_MISSING("Video Missing"),
    COMPLETE("Complete")
}
