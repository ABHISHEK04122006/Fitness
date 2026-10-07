package com.example.fitness.feature.progress.data

import com.example.fitness.feature.progress.model.ExercisePerformanceHistoryItem
import com.example.fitness.feature.progress.model.WorkoutSessionRecord

/**
 * Repository interface for managing user-scoped workout history and progress records.
 * All operations are strictly bound to the authenticated user's scope.
 */
interface WorkoutHistoryRepository {
    /**
     * Persists a completed workout session to storage.
     * Must be idempotent and prevent duplicate entries for the same sessionId.
     */
    suspend fun saveWorkoutSession(session: WorkoutSessionRecord): Result<WorkoutSessionRecord>

    /**
     * Retrieves all workout history records for the specified user, ordered newest first.
     */
    suspend fun getWorkoutHistory(userId: String): Result<List<WorkoutSessionRecord>>

    /**
     * Retrieves a single workout session record by ID for the specified user.
     */
    suspend fun getWorkoutSession(userId: String, sessionId: String): Result<WorkoutSessionRecord?>

    /**
     * Retrieves historical performance entries for a specific exercise across all completed sessions.
     */
    suspend fun getExerciseHistory(userId: String, exerciseId: String): Result<List<ExercisePerformanceHistoryItem>>
}
