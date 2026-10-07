package com.example.fitness.feature.program.model

import com.example.fitness.feature.workout.model.ExperienceLevel

/**
 * Controlled taxonomy of workout programs supported by the platform.
 * Relational identifiers remain stable and decoupled from human-readable display names.
 *
 * @property id Canonical lowercase identifier used for default program document IDs.
 * @property title Human-readable display name for presentation.
 */
enum class ProgramType(val id: String, val title: String) {
    THIRTY_DAY("thirty_day", "30-Day Workout Plan"),
    HYPERTROPHY("hypertrophy", "Hypertrophy Program"),
    STRENGTH("strength", "Strength Program"),
    ATHLETE_PERFORMANCE("athlete_performance", "Athlete Performance Program");

    companion object {
        fun fromString(value: String): ProgramType? =
            entries.firstOrNull {
                it.name.equals(value, ignoreCase = true) ||
                        it.id.equals(value, ignoreCase = true) ||
                        it.title.equals(value, ignoreCase = true)
            }
    }
}

/**
 * Controlled taxonomy for individual program day / session types.
 *
 * @property title Human-readable display label.
 */
enum class ProgramWorkoutType(val title: String) {
    TRAINING("Training"),
    REST("Rest"),
    RECOVERY("Active Recovery"),
    WARMUP("Warmup"),
    COOLDOWN("Cooldown"),
    CARDIO("Cardio Conditioning"),
    MOBILITY("Mobility & Flexibility");

    companion object {
        fun fromString(value: String): ProgramWorkoutType? =
            entries.firstOrNull {
                it.name.equals(value, ignoreCase = true) ||
                        it.title.equals(value, ignoreCase = true)
            }
    }
}

/**
 * Re-exports the project's existing [ExperienceLevel] terminology to ensure 100% compatibility:
 * - NO_EXPERIENCE
 * - BEGINNER
 * - ADVANCED
 * - EXPERT
 */
typealias ProgramExperienceLevel = ExperienceLevel
