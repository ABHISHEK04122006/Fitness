package com.example.fitness.feature.exercise.data

import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseMedia

/**
 * Repository interface for managing exercises in Firestore under exercises/{exerciseId}.
 * Provides centralized exercise definitions used across all workout programs and admin management.
 */
interface ExerciseRepository {

    /**
     * Retrieves a single exercise by its stable document ID.
     * Returns null if not found or inaccessible.
     */
    suspend fun getExercise(exerciseId: String): Exercise?

    /**
     * Retrieves all active exercises (isActive == true).
     * Used by standard application flows and workout builders.
     */
    suspend fun getActiveExercises(): List<Exercise>

    /**
     * Retrieves all exercises including archived/inactive ones.
     * Restricted to admin operations.
     */
    suspend fun getAllExercisesForAdmin(): List<Exercise>

    /**
     * Creates a new exercise document with a stable unique ID.
     * Initializes createdAt, updatedAt, createdBy, updatedBy, and version = 1.
     */
    suspend fun createExercise(exercise: Exercise): Result<Unit>

    /**
     * Updates an existing exercise document.
     * Preserves createdAt and createdBy, updates updatedAt and updatedBy, increments version.
     */
    suspend fun updateExercise(exercise: Exercise): Result<Unit>

    /**
     * Soft-deletes / archives an exercise by setting isActive = false.
     * Preserves existing references while hiding the exercise from active selection.
     */
    suspend fun archiveExercise(exerciseId: String): Result<Unit>

    /**
     * Restores an archived exercise by setting isActive = true.
     * Preserves createdAt and createdBy, updates updatedAt and updatedBy, increments version.
     */
    suspend fun restoreExercise(exerciseId: String): Result<Unit>

    /**
     * Permanently deletes an exercise document from Firestore and client catalog.
     * Hard delete operation restricted to admin operations.
     */
    suspend fun deleteExercise(exerciseId: String): Result<Unit>

    /**
     * Retrieves exercises filtered by active/archived status for admin management.
     * status = "ALL", "ACTIVE", or "ARCHIVED".
     */
    suspend fun getExercisesForAdmin(status: String = "ALL"): List<Exercise> = getAllExercisesForAdmin()

    /**
     * Partially updates only the media demonstration metadata for an existing exercise.
     * Preserves all other exercise metadata (names, instructions, sets/reps, biomechanics).
     */
    suspend fun updateExerciseMedia(exerciseId: String, media: ExerciseMedia): Result<Unit>
}
