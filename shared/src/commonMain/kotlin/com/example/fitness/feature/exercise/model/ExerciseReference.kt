package com.example.fitness.feature.exercise.model

import kotlinx.serialization.Serializable

/**
 * Lightweight reference to an [Exercise] used by workouts, programs, and session logs.
 * Centralizes exercise definitions by referencing [exerciseId] instead of copying exercise details.
 *
 * @property exerciseId The stable document ID referencing exercises/{exerciseId}.
 * @property sets Target set count for this workout configuration.
 * @property reps Prescribed repetition range or target (e.g., "8-10", "12-15", "AMRAP").
 * @property restSeconds Prescribed rest interval in seconds between sets.
 */
@Serializable
data class ExerciseReference(
    val exerciseId: String = "",
    val sets: Int = 3,
    val reps: String = "",
    val restSeconds: Int = 90,
    val notes: String = ""
)
