package com.example.fitness.feature.workout.model

import org.jetbrains.compose.resources.DrawableResource

data class WorkoutPlanExercise(
    val id: String,
    val name: String,
    val sets: String,
    val reps: String,
    val weight: String,
    val rest: String
)

data class DailyWorkoutPlan(
    val id: String,
    val dayOfWeek: String,
    val dayFullName: String,
    val title: String,
    val duration: String,
    val difficulty: String,
    val target: String,
    val calories: String,
    val imageRes: DrawableResource,
    val isRestDay: Boolean = false,
    val actionButtonText: String = "Start Workout",
    val bodyPartId: String,
    val workoutType: String = "gym",
    val equipment: String = "Dumbbells, Barbell",
    val about: String = "",
    val trainedMuscles: List<String> = emptyList(),
    val warmup: List<String> = emptyList(),
    val exercises: List<WorkoutPlanExercise> = emptyList(),
    val cooldown: List<String> = emptyList()
)
