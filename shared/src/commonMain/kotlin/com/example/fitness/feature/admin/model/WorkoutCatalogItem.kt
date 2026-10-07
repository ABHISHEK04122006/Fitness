package com.example.fitness.feature.admin.model

import com.example.fitness.feature.workout.model.BodyPartWorkout
import com.example.fitness.feature.workout.model.ExerciseItem
import com.example.fitness.feature.workout.model.WorkoutType
import kotlinx.serialization.Serializable

/**
 * Canonical workout catalog item stored in Firestore "workouts/{workoutId}".
 *
 * Preserves stable IDs (e.g. "gym_chest", "home_chest"), exercise sequences,
 * sets/reps prescriptions, audit metadata, and internal rotation fields (A/B/C/D).
 */
@Serializable
data class WorkoutCatalogItem(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val bodyPart: String = "",
    val workoutType: String = "GYM", // "GYM" or "HOME"
    val equipment: String = "",
    val duration: String = "",
    val difficulty: String = "",
    val calories: String = "",
    val howToPerform: String = "",
    val exercises: List<WorkoutExerciseCatalogItem> = emptyList(),
    val targetMuscle: String = "",
    val rotation: String? = null, // Preserved internal Rotation A/B/C/D
    val mediaUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val createdBy: String = "",
    val updatedBy: String = "",
    val version: Int = 1
)

/**
 * Exercise item within a canonical workout routine.
 */
@Serializable
data class WorkoutExerciseCatalogItem(
    val exerciseId: String,
    val name: String,
    val sets: String,
    val reps: String = "",
    val duration: String = "",
    val restTime: String = "60s rest",
    val order: Int = 1,
    val description: String = "",
    val videoDemoUrl: String = "",
    val tips: List<String> = emptyList()
)

/**
 * Mappers to bridge the mobile UI's [BodyPartWorkout] to the canonical [WorkoutCatalogItem].
 */
fun BodyPartWorkout.toCatalogItem(): WorkoutCatalogItem {
    val prefix = if (this.workoutType == WorkoutType.HOME) "home" else "gym"
    val stableId = if (this.id.startsWith("gym_") || this.id.startsWith("home_")) {
        this.id.lowercase()
    } else {
        "${prefix}_${this.id.lowercase()}"
    }

    return WorkoutCatalogItem(
        id = stableId,
        title = this.title,
        subtitle = this.subtitle,
        bodyPart = this.bodyPart,
        workoutType = this.workoutType.name,
        equipment = this.equipment,
        duration = this.duration,
        difficulty = this.difficulty,
        calories = this.calories,
        howToPerform = this.howToPerform,
        targetMuscle = this.targetMuscle,
        exercises = this.exercises.mapIndexed { index, ex ->
            WorkoutExerciseCatalogItem(
                exerciseId = ex.id,
                name = ex.name,
                sets = ex.sets,
                duration = ex.duration,
                restTime = ex.restTime,
                order = index + 1,
                description = ex.description,
                videoDemoUrl = ex.videoDemoUrl,
                tips = ex.tips
            )
        },
        isActive = true,
        version = 1
    )
}
