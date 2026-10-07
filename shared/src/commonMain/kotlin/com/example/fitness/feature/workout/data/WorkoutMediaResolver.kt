package com.example.fitness.feature.workout.data

import com.example.fitness.feature.exercise.media.FemaleExerciseMediaRegistry
import com.example.fitness.feature.workout.model.DailyWorkoutPlan
import fitness.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource

/**
 * Authoritative Central Media Resolver for ATHGYM Workout & Training Imagery.
 *
 * Implements strict gender-aware media selection:
 * - Female users receive dedicated female athletic & strength imagery.
 * - Male users receive authoritative male athletic & strength imagery.
 * - Loading / unauthenticated / unselected states receive a neutral gym placeholder (goal_general),
 *   guaranteeing a user is never shown the opposite gender's imagery.
 */
object WorkoutMediaResolver {

    val NEUTRAL_PLACEHOLDER: DrawableResource = Res.drawable.goal_general

    /**
     * Resolves dedicated imagery for the Athlete Workout entry card.
     */
    fun resolveAthleteWorkoutImage(gender: String?): DrawableResource {
        val normalized = gender?.trim()?.lowercase()
        return when (normalized) {
            "female" -> Res.drawable.female_athlete
            "male" -> Res.drawable.goal_athlete
            else -> NEUTRAL_PLACEHOLDER
        }
    }

    /**
     * Resolves dedicated imagery for the Gym Workouts entry card.
     */
    fun resolveGymWorkoutImage(gender: String?): DrawableResource {
        val normalized = gender?.trim()?.lowercase()
        return when (normalized) {
            "female" -> Res.drawable.female_gym
            "male" -> Res.drawable.workout_gym
            else -> NEUTRAL_PLACEHOLDER
        }
    }

    /**
     * Resolves dedicated imagery for Today's Workout thumbnail on the Home Screen.
     */
    fun resolveTodayWorkoutImage(
        workout: DailyWorkoutPlan?,
        gender: String?,
        fallbackCycleDay: Int = 1
    ): DrawableResource {
        val normalized = gender?.trim()?.lowercase()
        return when (normalized) {
            "female" -> {
                // 1. Try first exercise thumbnail if female media is registered
                val firstExerciseId = workout?.exercises?.firstOrNull()?.id
                if (firstExerciseId != null) {
                    val thumb = FemaleExerciseMediaRegistry.getThumbnailRes(firstExerciseId)
                    if (thumb != null) return thumb
                }
                // 2. Day/bodypart-specific female image
                resolveFemaleDayImage(workout?.bodyPartId, fallbackCycleDay, workout?.title)
            }
            "male" -> {
                // Male day/bodypart-specific image based on actual workout
                resolveMaleDayImage(workout?.bodyPartId, fallbackCycleDay, workout?.title)
            }
            else -> NEUTRAL_PLACEHOLDER
        }
    }

    private fun resolveFemaleDayImage(bodyPartId: String?, cycleDay: Int, title: String? = null): DrawableResource {
        val titleLower = title?.lowercase() ?: ""
        val bodyLower = bodyPartId?.lowercase()?.trim() ?: ""

        val effectiveBodyPart = when {
            titleLower.contains("leg") || titleLower.contains("squat") -> "legs"
            titleLower.contains("shoulder") || titleLower.contains("overhead") -> "shoulders"
            titleLower.contains("chest") || titleLower.contains("bench") || titleLower.contains("push") -> "chest"
            titleLower.contains("back") || titleLower.contains("pull") || titleLower.contains("row") -> "back"
            titleLower.contains("arm") || titleLower.contains("bicep") || titleLower.contains("curl") -> "biceps"
            titleLower.contains("abs") || titleLower.contains("core") -> "abs"
            else -> bodyLower
        }

        return when (effectiveBodyPart) {
            "chest" -> Res.drawable.female_dumbbell_bench_press
            "back" -> Res.drawable.female_lat_pulldown
            "legs" -> Res.drawable.female_barbell_squat
            "shoulders" -> Res.drawable.female_kettlebell_swing
            "biceps", "arms" -> Res.drawable.female_hip_thrust
            "abs", "core" -> Res.drawable.female_romanian_deadlift
            "full_body" -> Res.drawable.female_glute_bridge
            else -> when (((cycleDay - 1).mod(7)) + 1) {
                1 -> Res.drawable.female_dumbbell_bench_press
                2 -> Res.drawable.female_lat_pulldown
                3 -> Res.drawable.female_barbell_squat
                4 -> Res.drawable.female_kettlebell_swing
                5 -> Res.drawable.female_hip_thrust
                6 -> Res.drawable.female_romanian_deadlift
                else -> Res.drawable.female_glute_bridge
            }
        }
    }

    private fun resolveMaleDayImage(bodyPartId: String?, cycleDay: Int, title: String? = null): DrawableResource {
        val titleLower = title?.lowercase() ?: ""
        val bodyLower = bodyPartId?.lowercase()?.trim() ?: ""

        val effectiveBodyPart = when {
            titleLower.contains("shoulder") || titleLower.contains("overhead") -> "shoulders"
            titleLower.contains("leg") || titleLower.contains("squat") -> "legs"
            titleLower.contains("chest") || titleLower.contains("bench") || titleLower.contains("push") -> "chest"
            titleLower.contains("back") || titleLower.contains("pull") || titleLower.contains("row") -> "back"
            titleLower.contains("arm") || titleLower.contains("bicep") || titleLower.contains("curl") -> "biceps"
            titleLower.contains("abs") || titleLower.contains("core") -> "abs"
            else -> bodyLower
        }

        return when (effectiveBodyPart) {
            "chest" -> Res.drawable.bodypart_chest
            "back" -> Res.drawable.bodypart_back
            "legs" -> Res.drawable.gym_barbell_squat
            "shoulders" -> Res.drawable.gym_shoulder_press
            "biceps", "arms" -> Res.drawable.workout_incline_bench
            "abs", "core" -> Res.drawable.workout_home
            "full_body" -> Res.drawable.program_endurance
            else -> when (((cycleDay - 1).mod(7)) + 1) {
                1 -> Res.drawable.bodypart_chest
                2 -> Res.drawable.bodypart_back
                3 -> Res.drawable.gym_barbell_squat
                4 -> Res.drawable.gym_shoulder_press
                5 -> Res.drawable.workout_incline_bench
                6 -> Res.drawable.workout_home
                else -> Res.drawable.program_endurance
            }
        }
    }
}
