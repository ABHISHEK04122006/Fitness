package com.example.fitness.feature.workout.data

import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.program.data.Canonical30DayProgramData
import com.example.fitness.feature.workout.model.ExperienceLevel
import com.example.fitness.feature.workout.model.ThirtyDayWorkout

/**
 * 30-Day Workout Plan repository.
 * Backed exclusively by Canonical30DayProgramData and CanonicalWorkoutResolver.
 * Eliminates legacy synthetic 6-day template generators.
 */
object ThirtyDayWorkoutRepository {

    fun get30DayPlan(level: ExperienceLevel = UserSession.experienceLevel): List<ThirtyDayWorkout> {
        val program = Canonical30DayProgramData.getProgramForLevel(level)
        return program.days.map { CanonicalWorkoutResolver.toThirtyDayWorkout(it, "30-day", level) }
    }

    fun getWorkoutForDayOfWeek(dayIndex: Int, level: ExperienceLevel = UserSession.experienceLevel): ThirtyDayWorkout {
        val safeIndex = dayIndex.coerceIn(0, 6)
        val targetDayNum = safeIndex + 1
        return getWorkoutByDayNumber(targetDayNum, level)
    }

    fun getWorkoutByDayNumber(dayNumber: Int, level: ExperienceLevel = UserSession.experienceLevel): ThirtyDayWorkout {
        val programDay = CanonicalWorkoutResolver.resolveProgramDayByNumber("30-day", dayNumber, level)
        return CanonicalWorkoutResolver.toThirtyDayWorkout(programDay, "30-day", level)
    }

    fun getWorkoutById(id: String, level: ExperienceLevel = UserSession.experienceLevel): ThirtyDayWorkout {
        return CanonicalWorkoutResolver.resolveThirtyDayWorkout("30-day", id, level)
    }
}
