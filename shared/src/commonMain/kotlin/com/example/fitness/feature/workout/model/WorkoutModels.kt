package com.example.fitness.feature.workout.model

import com.example.fitness.feature.exercise.model.ExerciseMedia
import fitness.shared.generated.resources.Res
import org.jetbrains.compose.resources.DrawableResource

enum class WorkoutType(val title: String, val badge: String) {
    GYM("Gym Workouts", "GYM"),
    HOME("Home Workouts", "HOME");

    companion object {
        fun fromString(value: String): WorkoutType {
            return if (value.equals("home", ignoreCase = true)) HOME else GYM
        }
    }
}

data class ExerciseItem(
    val id: String,
    val name: String,
    val sets: String,
    val duration: String,
    val description: String,
    val imageRes: DrawableResource,
    val videoDemoUrl: String,
    val tips: List<String> = emptyList(),
    val restTime: String = "60s rest",
    val media: ExerciseMedia = ExerciseMedia()
)

data class BodyPartWorkout(
    val id: String,
    val title: String,
    val subtitle: String,
    val bodyPart: String,
    val workoutType: WorkoutType,
    val imageRes: DrawableResource,
    val equipment: String,
    val duration: String,
    val difficulty: String,
    val calories: String,
    val howToPerform: String,
    val exercises: List<ExerciseItem>,
    val targetMuscle: String = "",
    val commonMistakes: List<String> = emptyList()
)
