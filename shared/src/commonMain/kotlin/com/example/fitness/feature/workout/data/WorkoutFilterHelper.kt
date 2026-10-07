package com.example.fitness.feature.workout.data

import com.example.fitness.feature.workout.model.WorkoutType
import com.example.fitness.feature.workout.model.ExperienceLevel
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.exercise.media.FemaleExerciseMediaRegistry
import org.jetbrains.compose.resources.DrawableResource

/**
 * Enriched exercise item with full workout context for searching and filtering.
 * Sourced directly from [WorkoutRepository].
 */
data class WorkoutExerciseEntry(
    val id: String,
    val name: String,
    val bodyPart: String,
    val workoutType: WorkoutType,
    val difficulty: String,
    val equipment: String,
    val sets: String,
    val duration: String,
    val description: String,
    val imageRes: DrawableResource,
    val videoDemoUrl: String,
    val tips: List<String> = emptyList(),
    val restTime: String = "60s rest",
    val bodyPartId: String = "",
    val targetMuscle: String = ""
)

/**
 * Filter state representing user selections in the Workout filter dialog/sheet.
 */
data class WorkoutFilterState(
    val muscleGroup: String = "All",
    val difficulty: String = "All",
    val equipment: String = "All",
    val workoutType: String = "Gym"
) {
    val activeFilterCount: Int
        get() {
            var count = 0
            if (muscleGroup != "All") count++
            if (difficulty != "All") count++
            if (equipment != "All") count++
            if (workoutType != "Gym") count++
            return count
        }

    val isDefault: Boolean
        get() = activeFilterCount == 0

    fun reset(): WorkoutFilterState = WorkoutFilterState()
}

/**
 * Centralized, reusable filtering helper for workouts and exercises.
 */
object WorkoutFilterHelper {

    val availableMuscleGroups: List<String> = listOf(
        "All", "Chest", "Back", "Shoulders", "Biceps", "Triceps", "Legs", "Abs", "Full Body"
    )

    val availableDifficulties: List<String> = listOf(
        "All", "No Experience", "Beginner", "Advanced", "Expert"
    )

    val availableEquipments: List<String> = listOf(
        "All", "Barbell", "Dumbbell", "Cables", "Machine", "Bodyweight"
    )

    val availableWorkoutTypes: List<String> = listOf(
        "Gym", "Home", "All"
    )

    /**
     * Loads all exercise entries from [WorkoutRepository] for the given [workoutType],
     * or all workouts if null.
     */
    fun getAllExercises(
        workoutType: WorkoutType? = null,
        gender: String? = null,
        level: ExperienceLevel? = null
    ): List<WorkoutExerciseEntry> {
        val workouts = when (workoutType) {
            WorkoutType.GYM -> WorkoutRepository.getWorkouts(WorkoutType.GYM, gender, level)
            WorkoutType.HOME -> WorkoutRepository.getWorkouts(WorkoutType.HOME, gender, level)
            null -> WorkoutRepository.getWorkouts(WorkoutType.GYM, gender, level) + WorkoutRepository.getWorkouts(WorkoutType.HOME, gender, level)
        }

        val isFemale = UserSession.isFemale
        return workouts.flatMap { workout ->
            workout.exercises.map { ex ->
                val resolvedImage = if (isFemale) {
                    FemaleExerciseMediaRegistry.resolveExerciseImage(ex.id, isFemale = true, defaultFallback = ex.imageRes)
                } else {
                    ex.imageRes
                }

                val canonical = com.example.fitness.feature.exercise.data.SampleExerciseData.findCanonical(ex.id, ex.name)
                    ?: com.example.fitness.feature.exercise.data.SampleExerciseData.getById(ex.id)

                val fbEx = if (ex.id.startsWith("fb_")) {
                    com.example.fitness.feature.exercise.data.FullBodyExerciseLibrary.findById(ex.id)
                } else null

                val thirtyDayEx = if (fbEx == null && !ex.id.startsWith("fb_")) {
                    com.example.fitness.feature.exercise.data.ThirtyDayExerciseLibrary.findById(ex.id)
                } else null

                val resolvedDiff = canonical?.difficulty?.ifBlank { null }
                    ?: fbEx?.difficulty
                    ?: thirtyDayEx?.difficulty
                    ?: workout.difficulty
                val resolvedEquipment = if (fbEx != null && fbEx.equipment.isNotEmpty()) {
                    fbEx.equipment.joinToString(", ")
                } else if (thirtyDayEx != null && thirtyDayEx.equipment.isNotEmpty()) {
                    thirtyDayEx.equipment.joinToString(", ")
                } else if (canonical != null && canonical.equipment.isNotEmpty()) {
                    canonical.equipment.joinToString(", ")
                } else {
                    workout.equipment
                }
                val resolvedMuscle = if (fbEx != null && fbEx.primaryMuscle.isNotBlank()) {
                    fbEx.primaryMuscle
                } else if (thirtyDayEx != null && thirtyDayEx.targetMuscle.isNotBlank()) {
                    thirtyDayEx.targetMuscle
                } else if (canonical != null && canonical.primaryMuscle.isNotBlank()) {
                    canonical.primaryMuscle
                } else {
                    workout.bodyPart
                }

                WorkoutExerciseEntry(
                    id = ex.id,
                    name = ex.name,
                    bodyPart = workout.bodyPart,
                    workoutType = workout.workoutType,
                    difficulty = resolvedDiff,
                    equipment = resolvedEquipment,
                    sets = ex.sets,
                    duration = ex.duration,
                    description = ex.description,
                    imageRes = resolvedImage,
                    videoDemoUrl = ex.videoDemoUrl,
                    tips = ex.tips,
                    restTime = ex.restTime,
                    bodyPartId = workout.id,
                    targetMuscle = resolvedMuscle
                )
            }
        }
    }

    /**
     * Filters a list of [WorkoutExerciseEntry] according to search query and active filters.
     *
     * Conceptually:
     * All Exercises -> Search Filter -> Category/Level/Equipment/WorkoutType Filters -> Displayed Exercises
     */
    fun filterExercises(
        exercises: List<WorkoutExerciseEntry>,
        query: String,
        filters: WorkoutFilterState
    ): List<WorkoutExerciseEntry> {
        val trimmedQuery = query.trim().lowercase()

        return exercises.filter { exercise ->
            // 1. Search Query filter (matches name, targetMuscle, description)
            val matchesSearch = if (trimmedQuery.isEmpty()) {
                true
            } else {
                exercise.name.lowercase().contains(trimmedQuery) ||
                        exercise.targetMuscle.lowercase().contains(trimmedQuery) ||
                        exercise.description.lowercase().contains(trimmedQuery)
            }

            // 2. Muscle Group filter
            val matchesMuscle = if (filters.muscleGroup == "All") {
                true
            } else {
                exercise.bodyPart.equals(filters.muscleGroup, ignoreCase = true) ||
                        (filters.muscleGroup.equals("Abs", ignoreCase = true) &&
                                (exercise.bodyPart.contains("Abs", ignoreCase = true) || exercise.bodyPart.contains("Core", ignoreCase = true)))
            }

            // 3. Difficulty / Level filter
            val matchesDifficulty = if (filters.difficulty == "All") {
                true
            } else {
                val target = filters.difficulty.trim().lowercase()
                val diff = exercise.difficulty.trim().lowercase()
                when {
                    target == "no experience" || target == "no_experience" -> {
                        diff.contains("no_experience") || diff.contains("no experience") || diff.contains("noexp")
                    }
                    target == "beginner" -> {
                        // Strict beginner check - does not mix with no_experience
                        (diff.contains("beginner") || diff.contains("beg")) && !diff.contains("no_experience") && !diff.contains("no experience") && !diff.contains("noexp")
                    }
                    target == "advanced" || target == "advance" -> {
                        diff.contains("advanced") || diff.contains("advance") || diff.contains("adv")
                    }
                    target == "expert" -> {
                        diff.contains("expert") || (diff.contains("exp") && !diff.contains("experience"))
                    }
                    target == "intermediate" || target == "inter." || target == "inter" -> {
                        diff.contains("intermediate") || diff.contains("inter.") || diff.contains("inter")
                    }
                    else -> {
                        val targetLevel = ExperienceLevel.fromString(filters.difficulty)
                        WorkoutRepository.isLevelCompatible(exercise.difficulty, targetLevel)
                    }
                }
            }

            // 4. Equipment filter: prioritize specific exercise equipment from name/description/equipment
            val matchesEquipment = if (filters.equipment == "All") {
                true
            } else {
                val filterEquip = filters.equipment.lowercase().removeSuffix("s")
                val nameLower = exercise.name.lowercase()
                val descLower = exercise.description.lowercase()
                val equipLower = exercise.equipment.lowercase()

                if (nameLower.contains(filterEquip) || descLower.contains(filterEquip)) {
                    true
                } else if (nameLower.contains("barbell") || nameLower.contains("cable") || nameLower.contains("dumbbell")) {
                    false
                } else {
                    equipLower.contains(filterEquip)
                }
            }

            // 5. Workout Type filter
            val matchesWorkoutType = when (filters.workoutType.lowercase()) {
                "gym" -> exercise.workoutType == WorkoutType.GYM
                "home" -> exercise.workoutType == WorkoutType.HOME
                else -> true
            }

            matchesSearch && matchesMuscle && matchesDifficulty && matchesEquipment && matchesWorkoutType
        }
    }
}
