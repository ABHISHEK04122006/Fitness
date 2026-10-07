package com.example.fitness.feature.workout.model

import com.example.fitness.feature.exercise.model.ExerciseMedia
import org.jetbrains.compose.resources.DrawableResource

enum class ExperienceLevel(val id: String, val title: String) {
    NO_EXPERIENCE("noExperience", "No Experience"),
    BEGINNER("beginner", "Beginner"),
    ADVANCED("advanced", "Advanced"),
    EXPERT("expert", "Expert");

    fun toUserFacingLevel(): String = when (this) {
        NO_EXPERIENCE -> "No Experience"
        BEGINNER -> "Beginner"
        ADVANCED -> "Advanced"
        EXPERT -> "Expert"
    }

    companion object {
        fun fromUserFacingOption(value: String): ExperienceLevel {
            val trimmed = value.trim()
            return when {
                trimmed.equals("noExperience", ignoreCase = true) ||
                    trimmed.equals("no experience", ignoreCase = true) ||
                    trimmed.equals("no_experience", ignoreCase = true) ||
                    trimmed.equals("none", ignoreCase = true) -> NO_EXPERIENCE
                trimmed.equals("beginner", ignoreCase = true) -> BEGINNER
                trimmed.equals("intermediate", ignoreCase = true) -> BEGINNER
                trimmed.equals("advance", ignoreCase = true) ||
                    trimmed.equals("advanced", ignoreCase = true) -> ADVANCED
                trimmed.equals("expert", ignoreCase = true) -> EXPERT
                else -> fromString(value)
            }
        }

        fun fromString(value: String): ExperienceLevel {
            val trimmed = value.trim()
            if (trimmed.equals("noExperience", ignoreCase = true) ||
                trimmed.equals("no experience", ignoreCase = true) ||
                trimmed.equals("no_experience", ignoreCase = true) ||
                trimmed.equals("none", ignoreCase = true)) {
                return NO_EXPERIENCE
            }
            if (trimmed.equals("beginner", ignoreCase = true)) {
                return BEGINNER
            }
            if (trimmed.equals("intermediate", ignoreCase = true)) {
                return BEGINNER
            }
            if (trimmed.equals("advance", ignoreCase = true) ||
                trimmed.equals("advanced", ignoreCase = true)) {
                return ADVANCED
            }
            if (trimmed.equals("expert", ignoreCase = true)) {
                return EXPERT
            }
            return entries.find {
                it.id.equals(trimmed, ignoreCase = true) || it.title.equals(trimmed, ignoreCase = true)
            } ?: NO_EXPERIENCE
        }
    }
}

data class WarmUpItem(
    val id: String,
    val name: String,
    val durationOrReps: String,
    val instructions: String
)

data class CoolDownItem(
    val id: String,
    val name: String,
    val durationOrReps: String,
    val instructions: String
)

data class PlanExercise(
    val id: String,
    val name: String,
    val sets: String,
    val reps: String,
    val rest: String,
    val equipment: String,
    val targetMuscle: String,
    val videoUrl: String,
    val instructions: String,
    val imageRes: DrawableResource,
    val media: ExerciseMedia = ExerciseMedia()
)

data class ThirtyDayWorkout(
    val id: String = "day1",
    val dayNumber: Int,
    val dayName: String,
    val dayOfWeek: String,
    val workoutTitle: String,
    val focus: String,
    val duration: String,
    val difficulty: String,
    val calories: String,
    val imageRes: DrawableResource,
    val bodyPartId: String,
    val workoutType: String = "gym",
    val isRestDay: Boolean = false,
    val actionButtonText: String = "Start Workout",
    val warmUp: List<WarmUpItem>,
    val exercises: List<PlanExercise>,
    val coolDown: List<CoolDownItem>,
    val experienceLevel: ExperienceLevel = ExperienceLevel.BEGINNER
)
