package com.example.fitness.feature.exercise.model

import kotlinx.serialization.Serializable

/**
 * Foundational Exercise entity representing an exercise definition stored in Firestore under exercises/{exerciseId}.
 * Serves as the single source of truth across all workout programs (30-Day, Hypertrophy, Strength, Athlete Performance).
 */
@Serializable
data class Exercise(
    val id: String = "",
    val name: String = "",
    val slug: String = "",
    val description: String = "",

    val primaryMuscle: String = "",
    val secondaryMuscles: List<String> = emptyList(),
    val muscleGroups: List<String> = emptyList(),

    val equipment: List<String> = emptyList(),

    val category: String = "",
    val movementPattern: String = "",
    val difficulty: String = "",

    val instructions: List<String> = emptyList(),
    val setupSteps: List<String> = emptyList(),
    val executionSteps: List<String> = emptyList(),

    val breathing: String = "",

    val formTips: List<String> = emptyList(),
    val commonMistakes: List<String> = emptyList(),

    val defaultSets: Int = 3,
    val defaultReps: String = "",
    val defaultRestSeconds: Int = 90,

    val media: ExerciseMedia = ExerciseMedia(),

    val isActive: Boolean = true,

    val createdAt: Long? = null,
    val updatedAt: Long? = null,

    val createdBy: String = "",
    val updatedBy: String = "",

    val version: Int = 1,
    val gender: String = ""
)
