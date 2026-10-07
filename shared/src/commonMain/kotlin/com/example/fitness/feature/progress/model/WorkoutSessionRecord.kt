package com.example.fitness.feature.progress.model

import kotlinx.serialization.Serializable

/**
 * Captures the actual execution performance of a single exercise within a workout session,
 * strictly maintaining separation between canonical prescribed values and actual user results.
 */
@Serializable
data class ExercisePerformanceRecord(
    val exerciseId: String = "",
    val exerciseName: String = "",
    val prescribedSets: Int = 0,
    val prescribedReps: String = "",
    val completedSets: Int = 0,
    val completedReps: List<Int> = emptyList(),
    val weightUsed: Double? = null, // Nullable: strictly no fake weight data
    val restSeconds: Int = 60,
    val completed: Boolean = false,
    val notes: String? = null
)

/**
 * Historical performance entry for a specific exercise across multiple workout sessions.
 */
@Serializable
data class ExercisePerformanceHistoryItem(
    val sessionId: String = "",
    val date: String = "",
    val workoutTitle: String = "",
    val prescribedSets: Int = 0,
    val prescribedReps: String = "",
    val completedSets: Int = 0,
    val completedReps: List<Int> = emptyList(),
    val weightUsed: Double? = null,
    val completed: Boolean = false
)

/**
 * Canonical record of a completed workout session, storing both summary metrics and
 * detailed per-exercise performance records.
 */
@Serializable
data class WorkoutSessionRecord(
    val id: String = "",
    val workoutId: String = "",
    val dayNumber: Int = 1,
    val workoutTitle: String = "",
    val targetMuscle: List<String> = emptyList(),
    val experienceLevel: String = "beginner",
    val startedAt: Long = 0L,
    val completedAt: Long = 0L,
    val durationMinutes: Int = 1,
    val totalExercises: Int = 0,
    val completedExercises: Int = 0,
    val totalSets: Int = 0,
    val completedSets: Int = 0,
    val warmUpCompleted: Boolean = true,
    val mainWorkoutCompleted: Boolean = false,
    val coolDownCompleted: Boolean = true,
    val completionStatus: String = "partial",
    val localDateString: String = "", // "YYYY-MM-DD"
    val displayDate: String = "",      // "Today", "Yesterday", "Sep 13"
    val programId: String = "30-day",
    val programName: String = "30-Day Workout Plan",
    // Section 2 canonical progress fields
    val sessionId: String = id,
    val userId: String = "",
    val workoutDate: String = localDateString,
    val workoutType: String = if (targetMuscle.isNotEmpty()) targetMuscle.joinToString(", ") else "Strength",
    val durationSeconds: Int = durationMinutes * 60,
    val prescribedExerciseCount: Int = totalExercises,
    val completedExerciseCount: Int = completedExercises,
    val prescribedSetCount: Int = totalSets,
    val completedSetCount: Int = completedSets,
    val totalVolume: Double = 0.0,
    val exercises: List<ExercisePerformanceRecord> = emptyList(),
    val syncStatus: String = "synced"
)
