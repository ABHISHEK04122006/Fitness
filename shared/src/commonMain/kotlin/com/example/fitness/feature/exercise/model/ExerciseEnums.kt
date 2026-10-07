package com.example.fitness.feature.exercise.model

/**
 * Standardized difficulty levels for exercises.
 */
enum class ExerciseDifficulty(val displayName: String) {
    NO_EXPERIENCE("No Experience"),
    BEGINNER("Beginner"),
    ADVANCED("Advanced"),
    EXPERT("Expert");

    companion object {
        fun fromString(value: String): ExerciseDifficulty? {
            val trimmed = value.trim()
            if (trimmed.equals("no_experience", ignoreCase = true) ||
                trimmed.equals("no experience", ignoreCase = true) ||
                trimmed.equals("noExperience", ignoreCase = true) ||
                trimmed.equals("none", ignoreCase = true)
            ) {
                return NO_EXPERIENCE
            }
            if (trimmed.equals("beginner", ignoreCase = true) ||
                trimmed.equals("beg", ignoreCase = true) ||
                trimmed.equals("intermediate", ignoreCase = true)
            ) {
                return BEGINNER
            }
            if (trimmed.equals("advanced", ignoreCase = true) ||
                trimmed.equals("advance", ignoreCase = true) ||
                trimmed.equals("adv", ignoreCase = true)
            ) {
                return ADVANCED
            }
            if (trimmed.equals("expert", ignoreCase = true) ||
                trimmed.equals("exp", ignoreCase = true)
            ) {
                return EXPERT
            }
            return entries.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
        }
    }
}

/**
 * Primary categories an exercise can be tagged with.
 * An exercise can be used across multiple programs regardless of category.
 */
enum class ExerciseCategory {
    STRENGTH,
    HYPERTROPHY,
    ATHLETE_PERFORMANCE,
    CORE,
    MOBILITY,
    WARMUP,
    COOLDOWN,
    CARDIO;

    companion object {
        fun fromString(value: String): ExerciseCategory? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}

/**
 * Foundational biomechanical movement patterns.
 */
enum class MovementPattern {
    HORIZONTAL_PUSH,
    HORIZONTAL_PULL,
    VERTICAL_PUSH,
    VERTICAL_PULL,
    SQUAT,
    HINGE,
    LUNGE,
    CARRY,
    ROTATION,
    ANTI_ROTATION,
    FLEXION,
    EXTENSION,
    ISOLATION,
    LOCOMOTION;

    companion object {
        fun fromString(value: String): MovementPattern? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}

/**
 * Types of media attached to an exercise demonstration.
 */
enum class ExerciseMediaType {
    NONE,
    IMAGE,
    ANIMATION,
    VIDEO;

    companion object {
        fun fromString(value: String): ExerciseMediaType? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}
