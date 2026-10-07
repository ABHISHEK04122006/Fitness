package com.example.fitness.feature.exercise.audit

import com.example.fitness.feature.exercise.data.ExerciseRepository
import com.example.fitness.feature.exercise.media.ExerciseMediaUploadRepository
import com.example.fitness.feature.exercise.media.ExerciseMediaValidator
import com.example.fitness.feature.exercise.media.MediaUploadFile
import com.example.fitness.feature.exercise.media.MediaValidationResult
import com.example.fitness.feature.exercise.model.ExerciseMediaType

/**
 * Descriptor for a single exercise media asset scheduled for bulk import.
 *
 * @property exerciseId The target exercise ID (e.g. "barbell_bench_press").
 * @property mediaType The type of media asset (IMAGE, ANIMATION, VIDEO).
 * @property file The media payload encapsulated in a [MediaUploadFile].
 */
data class ExerciseMediaImportItem(
    val exerciseId: String,
    val mediaType: ExerciseMediaType,
    val file: MediaUploadFile
)

/**
 * Result outcome of an individual asset import operation.
 */
sealed class ExerciseMediaImportOutcome {
    data class Success(
        val exerciseId: String,
        val mediaType: ExerciseMediaType,
        val downloadUrl: String
    ) : ExerciseMediaImportOutcome()

    data class Failure(
        val exerciseId: String,
        val mediaType: ExerciseMediaType,
        val reason: String,
        val cause: Throwable? = null
    ) : ExerciseMediaImportOutcome()
}

/**
 * Summary report produced after executing a batch media import.
 */
data class BulkImportReport(
    val totalProcessed: Int,
    val successCount: Int,
    val failureCount: Int,
    val outcomes: List<ExerciseMediaImportOutcome>
)

/**
 * Metadata-driven importer for safe bulk media population.
 *
 * Guarantees:
 * 1. Pre-validation of exercise ID and verification of catalog existence.
 * 2. Strict file format and size validation via [ExerciseMediaValidator].
 * 3. Prevention of duplicate or conflicting asset specifications within the same batch.
 * 4. Safe upload sequencing routing through [ExerciseMediaUploadRepository].
 * 5. Complete isolation: Failed asset uploads report per-item errors without corrupting other assets.
 * 6. Non-destructive: Never modifies instructions, biomechanics, sets, reps, or non-media attributes.
 */
class ExerciseMediaBulkImporter(
    private val exerciseRepository: ExerciseRepository,
    private val uploadRepository: ExerciseMediaUploadRepository
) {

    /**
     * Executes a batch of media import items.
     */
    suspend fun importBatch(items: List<ExerciseMediaImportItem>): BulkImportReport {
        val outcomes = mutableListOf<ExerciseMediaImportOutcome>()
        val seenKeys = mutableSetOf<Pair<String, ExerciseMediaType>>()

        for (item in items) {
            val cleanId = item.exerciseId.trim()

            // 1. Duplicate detection within the batch
            val key = cleanId to item.mediaType
            if (seenKeys.contains(key)) {
                outcomes.add(
                    ExerciseMediaImportOutcome.Failure(
                        exerciseId = cleanId,
                        mediaType = item.mediaType,
                        reason = "Duplicate import item detected for exercise '$cleanId' and type '${item.mediaType}'."
                    )
                )
                continue
            }
            seenKeys.add(key)

            // 2. Validate target media type
            if (item.mediaType == ExerciseMediaType.NONE) {
                outcomes.add(
                    ExerciseMediaImportOutcome.Failure(
                        exerciseId = cleanId,
                        mediaType = item.mediaType,
                        reason = "Cannot import media with type NONE."
                    )
                )
                continue
            }

            // 3. Validate exercise ID format
            val idValidation = ExerciseMediaValidator.validateExerciseId(cleanId)
            if (idValidation is MediaValidationResult.Invalid) {
                outcomes.add(
                    ExerciseMediaImportOutcome.Failure(
                        exerciseId = cleanId,
                        mediaType = item.mediaType,
                        reason = idValidation.reason
                    )
                )
                continue
            }

            // 4. Verify exercise exists in the catalog/repository
            val exercise = exerciseRepository.getExercise(cleanId)
            if (exercise == null) {
                outcomes.add(
                    ExerciseMediaImportOutcome.Failure(
                        exerciseId = cleanId,
                        mediaType = item.mediaType,
                        reason = "Exercise with ID '$cleanId' does not exist in catalog."
                    )
                )
                continue
            }

            // 5. Validate file payload using ExerciseMediaValidator
            val fileValidation = ExerciseMediaValidator.validateFile(item.file, item.mediaType)
            if (fileValidation is MediaValidationResult.Invalid) {
                outcomes.add(
                    ExerciseMediaImportOutcome.Failure(
                        exerciseId = cleanId,
                        mediaType = item.mediaType,
                        reason = fileValidation.reason
                    )
                )
                continue
            }

            // 6. Execute upload via ExerciseMediaUploadRepository
            val uploadResult = when (item.mediaType) {
                ExerciseMediaType.IMAGE -> uploadRepository.uploadThumbnail(cleanId, item.file)
                ExerciseMediaType.ANIMATION -> uploadRepository.uploadAnimation(cleanId, item.file)
                ExerciseMediaType.VIDEO -> uploadRepository.uploadVideo(cleanId, item.file)
                ExerciseMediaType.NONE -> Result.failure(IllegalArgumentException("Invalid target media type"))
            }

            if (uploadResult.isSuccess) {
                outcomes.add(
                    ExerciseMediaImportOutcome.Success(
                        exerciseId = cleanId,
                        mediaType = item.mediaType,
                        downloadUrl = uploadResult.getOrThrow()
                    )
                )
            } else {
                outcomes.add(
                    ExerciseMediaImportOutcome.Failure(
                        exerciseId = cleanId,
                        mediaType = item.mediaType,
                        reason = uploadResult.exceptionOrNull()?.message ?: "Upload failed",
                        cause = uploadResult.exceptionOrNull()
                    )
                )
            }
        }

        return BulkImportReport(
            totalProcessed = items.size,
            successCount = outcomes.count { it is ExerciseMediaImportOutcome.Success },
            failureCount = outcomes.count { it is ExerciseMediaImportOutcome.Failure },
            outcomes = outcomes
        )
    }

    /**
     * Executes a controlled pilot batch of media assets (max [MAX_PILOT_BATCH_SIZE] items).
     * Strictly enforces the Real Media Source Policy:
     * - Only authorized project-owned media is uploaded.
     * - If no authorized asset exists (localFile == null or !isAuthorized), reports failure:
     *   "No authorized media asset available." without performing storage or database writes.
     * - Authorized files are processed via [importBatch] ensuring safe replacement, format validation,
     *   and duplicate detection.
     */
    suspend fun executeControlledBatch(items: List<ControlledMediaImportItem>): ControlledBatchReport {
        val cappedItems = items.take(MAX_PILOT_BATCH_SIZE)
        val results = mutableListOf<ControlledBatchResultItem>()
        val authorizedToImport = mutableListOf<ExerciseMediaImportItem>()

        for (item in cappedItems) {
            val cleanId = item.exerciseId.trim()
            val slotName = when (item.mediaType) {
                ExerciseMediaType.IMAGE -> "thumbnail"
                ExerciseMediaType.ANIMATION -> "animation"
                ExerciseMediaType.VIDEO -> "video"
                ExerciseMediaType.NONE -> "none"
            }
            val expectedStoragePath = "exercise-media/$cleanId/$slotName"
            val exercise = exerciseRepository.getExercise(cleanId)
            val displayName = exercise?.name ?: cleanId

            if (item.localFile == null || !item.isAuthorized) {
                results.add(
                    ControlledBatchResultItem(
                        exerciseId = cleanId,
                        exerciseName = displayName,
                        mediaType = item.mediaType,
                        isSuccess = false,
                        storagePath = expectedStoragePath,
                        firestoreStatus = "Unchanged",
                        statusMessage = "FAILED: No authorized media asset available.",
                        failureReason = "No authorized media asset available."
                    )
                )
            } else {
                authorizedToImport.add(
                    ExerciseMediaImportItem(
                        exerciseId = cleanId,
                        mediaType = item.mediaType,
                        file = item.localFile
                    )
                )
            }
        }

        if (authorizedToImport.isNotEmpty()) {
            val report = importBatch(authorizedToImport)
            for (outcome in report.outcomes) {
                val cleanId = when (outcome) {
                    is ExerciseMediaImportOutcome.Success -> outcome.exerciseId
                    is ExerciseMediaImportOutcome.Failure -> outcome.exerciseId
                }
                val mType = when (outcome) {
                    is ExerciseMediaImportOutcome.Success -> outcome.mediaType
                    is ExerciseMediaImportOutcome.Failure -> outcome.mediaType
                }
                val slotName = when (mType) {
                    ExerciseMediaType.IMAGE -> "thumbnail"
                    ExerciseMediaType.ANIMATION -> "animation"
                    ExerciseMediaType.VIDEO -> "video"
                    ExerciseMediaType.NONE -> "none"
                }
                val expectedStoragePath = "exercise-media/$cleanId/$slotName"
                val exercise = exerciseRepository.getExercise(cleanId)
                val displayName = exercise?.name ?: cleanId

                when (outcome) {
                    is ExerciseMediaImportOutcome.Success -> {
                        results.add(
                            ControlledBatchResultItem(
                                exerciseId = cleanId,
                                exerciseName = displayName,
                                mediaType = mType,
                                isSuccess = true,
                                storagePath = expectedStoragePath,
                                firestoreStatus = "Updated",
                                statusMessage = "SUCCESS",
                                failureReason = null
                            )
                        )
                    }
                    is ExerciseMediaImportOutcome.Failure -> {
                        results.add(
                            ControlledBatchResultItem(
                                exerciseId = cleanId,
                                exerciseName = displayName,
                                mediaType = mType,
                                isSuccess = false,
                                storagePath = expectedStoragePath,
                                firestoreStatus = if (outcome.reason.contains("Firestore", ignoreCase = true)) "Failed" else "Unchanged",
                                statusMessage = "FAILED: ${outcome.reason}",
                                failureReason = outcome.reason
                            )
                        )
                    }
                }
            }
        }

        val successCount = results.count { it.isSuccess }
        val failureCount = results.count { !it.isSuccess }
        val hasAuthorizedAssets = results.any { it.isSuccess || it.failureReason != "No authorized media asset available." }

        return ControlledBatchReport(
            totalProcessed = results.size,
            successCount = successCount,
            failureCount = failureCount,
            results = results,
            hasAuthorizedAssets = hasAuthorizedAssets
        )
    }

    companion object {
        const val MAX_PILOT_BATCH_SIZE = 10

        /**
         * Generates a sample pilot batch definition for 5–10 exercises.
         * Default instances have localFile = null and isAuthorized = false, reflecting
         * the current absence of authorized real media assets in the repository.
         */
        fun createPilotBatchPlan(exercises: List<com.example.fitness.feature.exercise.model.Exercise>): List<ControlledMediaImportItem> {
            val pilotExercises = exercises.take(6)
            return pilotExercises.map { exercise ->
                ControlledMediaImportItem(
                    exerciseId = exercise.id,
                    mediaType = ExerciseMediaType.VIDEO,
                    localFile = null,
                    optionalFilename = "${exercise.id}.mp4",
                    isAuthorized = false
                )
            }
        }
    }
}

/**
 * Controlled media import item representing an asset planned or queued for pilot or bulk population.
 *
 * @property exerciseId The target exercise ID (e.g. "barbell_bench_press").
 * @property mediaType Target media type (IMAGE, ANIMATION, VIDEO).
 * @property localFile Local binary payload if available, or null if no local asset exists.
 * @property optionalFilename Optional local filename for display or logging.
 * @property isAuthorized Explicit authorization flag affirming the asset is team-created or legally licensed.
 */
data class ControlledMediaImportItem(
    val exerciseId: String,
    val mediaType: ExerciseMediaType,
    val localFile: MediaUploadFile? = null,
    val optionalFilename: String? = null,
    val isAuthorized: Boolean = false
)

/**
 * Per-asset result record for a controlled batch import operation.
 * Omits raw Firebase URLs from the primary user-facing result.
 *
 * @property exerciseId The target exercise ID.
 * @property exerciseName Display name of the exercise.
 * @property mediaType The slot processed (IMAGE, ANIMATION, VIDEO).
 * @property isSuccess Whether the upload and Firestore update succeeded.
 * @property storagePath Canonical storage path (e.g. "exercise-media/{exerciseId}/{slot}").
 * @property firestoreStatus Firestore synchronization status ("Updated", "Unchanged", "Failed").
 * @property statusMessage Human-readable status message ("SUCCESS" or "FAILED: <reason>").
 * @property failureReason Detailed failure reason if failed.
 */
data class ControlledBatchResultItem(
    val exerciseId: String,
    val exerciseName: String,
    val mediaType: ExerciseMediaType,
    val isSuccess: Boolean,
    val storagePath: String,
    val firestoreStatus: String,
    val statusMessage: String,
    val failureReason: String? = null
)

/**
 * Summary report for a controlled batch population workflow.
 */
data class ControlledBatchReport(
    val totalProcessed: Int,
    val successCount: Int,
    val failureCount: Int,
    val results: List<ControlledBatchResultItem>,
    val hasAuthorizedAssets: Boolean
)
