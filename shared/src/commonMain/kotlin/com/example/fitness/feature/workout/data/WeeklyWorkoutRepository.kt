package com.example.fitness.feature.workout.data

import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.program.data.Canonical30DayProgramData
import com.example.fitness.feature.program.model.ProgramDay
import com.example.fitness.feature.workout.model.DailyWorkoutPlan
import com.example.fitness.feature.workout.model.ExperienceLevel
import com.example.fitness.feature.workout.model.ThirtyDayWorkout
import com.example.fitness.feature.workout.model.WorkoutPlanExercise
import fitness.shared.generated.resources.Res
import fitness.shared.generated.resources.bodypart_back
import fitness.shared.generated.resources.bodypart_chest
import fitness.shared.generated.resources.goal_athlete
import fitness.shared.generated.resources.program_endurance
import fitness.shared.generated.resources.workout_gym
import fitness.shared.generated.resources.workout_home
import fitness.shared.generated.resources.workout_incline_bench
import org.jetbrains.compose.resources.DrawableResource

object WeeklyWorkoutRepository {

    private val dayOfWeekShortNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    private val dayOfWeekFullNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    fun toDailyWorkoutPlan(workout: ThirtyDayWorkout): DailyWorkoutPlan {
        return DailyWorkoutPlan(
            id = workout.id,
            dayOfWeek = workout.dayOfWeek,
            dayFullName = when (workout.dayOfWeek) {
                "Mon" -> "Monday"
                "Tue" -> "Tuesday"
                "Wed" -> "Wednesday"
                "Thu" -> "Thursday"
                "Fri" -> "Friday"
                "Sat" -> "Saturday"
                "Sun" -> "Sunday"
                else -> workout.dayName
            },
            title = workout.workoutTitle,
            duration = workout.duration,
            difficulty = workout.difficulty,
            target = workout.focus,
            calories = workout.calories,
            imageRes = workout.imageRes,
            isRestDay = workout.isRestDay,
            actionButtonText = workout.actionButtonText,
            bodyPartId = workout.bodyPartId,
            workoutType = workout.workoutType,
            equipment = workout.exercises.map { it.equipment }.distinct().joinToString(", "),
            about = "${workout.workoutTitle} session focusing on ${workout.focus}. Programmed according to the structured fitness plan for optimal progressive overload and recovery.",
            trainedMuscles = workout.focus.split(",").map { it.trim() },
            warmup = workout.warmUp.map { "${it.name} (${it.durationOrReps}) — ${it.instructions}" },
            exercises = workout.exercises.map {
                WorkoutPlanExercise(
                    id = it.id,
                    name = it.name,
                    sets = it.sets,
                    reps = it.reps,
                    weight = "Target Load",
                    rest = it.rest
                )
            },
            cooldown = workout.coolDown.map { "${it.name} (${it.durationOrReps}) — ${it.instructions}" }
        )
    }

    fun programDayToDailyWorkoutPlan(
        day: ProgramDay,
        dayIndex: Int,
        level: ExperienceLevel = UserSession.experienceLevel
    ): DailyWorkoutPlan {
        return CanonicalWorkoutResolver.toDailyWorkoutPlan(day, dayIndex, null, level)
    }

    fun getWorkoutForDay(
        dayIndex: Int,
        level: ExperienceLevel = UserSession.experienceLevel,
        workoutDays: String? = null
    ): DailyWorkoutPlan {
        return CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(null, dayIndex, level, workoutDays)
    }

    fun getWorkoutById(
        id: String,
        level: ExperienceLevel = UserSession.experienceLevel
    ): DailyWorkoutPlan? {
        return CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, id, level)
    }

    fun getAllWorkouts(
        level: ExperienceLevel = UserSession.experienceLevel,
        workoutDays: String? = null
    ): List<DailyWorkoutPlan> {
        return (0..6).map { getWorkoutForDay(it, level, workoutDays) }
    }
}
