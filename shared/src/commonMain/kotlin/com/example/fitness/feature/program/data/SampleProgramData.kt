package com.example.fitness.feature.program.data

import com.example.fitness.feature.exercise.model.ExerciseReference
import com.example.fitness.feature.program.model.Program
import com.example.fitness.feature.program.model.ProgramDay
import com.example.fitness.feature.program.model.ProgramType
import com.example.fitness.feature.program.model.ProgramWorkoutType
import com.example.fitness.feature.workout.model.ExperienceLevel

/**
 * Central program registry for Athgym / Fitness.
 * Provides canonical, authoritative programs from the Athgym PDF specifications:
 * 1. 30-Day Workout Plan (No Experience, Beginner, Advanced, Expert)
 * 2. 30-Day Hypertrophy Program
 * 3. 30-Day Strength Program
 * 4. 30-Day Athlete Performance Program
 *
 * Preserves backward-compatible demonstrators for existing unit tests.
 */
object SampleProgramData {

    val THIRTY_DAY_DEMONSTRATOR = Program(
        id = "thirty_day",
        name = "30-Day Workout Plan",
        slug = "thirty_day_workout_plan",
        description = "Structured foundational 30-day progressive training program.",
        type = ProgramType.THIRTY_DAY.name,
        experienceLevel = ExperienceLevel.BEGINNER.name,
        durationDays = 30,
        trainingDaysPerWeek = 5,
        days = (1..30).map { dayNum ->
            val isRest = (dayNum % 7 == 0)
            ProgramDay(
                dayNumber = dayNum,
                title = if (isRest) "Day $dayNum: Rest & Recovery" else "Day $dayNum: Training Session",
                workoutType = if (isRest) ProgramWorkoutType.REST.name else ProgramWorkoutType.TRAINING.name,
                focus = if (isRest) listOf("Recovery") else listOf("General Fitness"),
                isRestDay = isRest,
                exercises = if (isRest) {
                    emptyList()
                } else {
                    listOf(
                        ExerciseReference(
                            exerciseId = "barbell_bench_press",
                            sets = 3,
                            reps = "8-12",
                            restSeconds = 90
                        )
                    )
                },
                notes = if (isRest) "Hydrate and rest." else ""
            )
        },
        isActive = true,
        version = 1
    )

    val HYPERTROPHY_DEMONSTRATOR = Program(
        id = "hypertrophy",
        name = "Hypertrophy Program",
        slug = "hypertrophy_program",
        description = "Muscle-group focused hypertrophy training architecture.",
        type = ProgramType.HYPERTROPHY.name,
        experienceLevel = ExperienceLevel.ADVANCED.name,
        durationDays = 56, // 8 weeks
        trainingDaysPerWeek = 4,
        days = listOf(
            ProgramDay(
                dayNumber = 1,
                title = "Upper Body Hypertrophy",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Chest", "Back"),
                isRestDay = false,
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "barbell_bench_press",
                        sets = 4,
                        reps = "8-10",
                        restSeconds = 120
                    )
                )
            ),
            ProgramDay(
                dayNumber = 2,
                title = "Rest & Recovery",
                workoutType = ProgramWorkoutType.REST.name,
                focus = listOf("Rest"),
                isRestDay = true,
                exercises = emptyList()
            )
        ),
        isActive = true,
        version = 1
    )

    val INITIAL_PROGRAMS: List<Program> = listOf(
        Canonical30DayProgramData.PROGRAM_NO_EXPERIENCE,
        Canonical30DayProgramData.PROGRAM_BEGINNER,
        Canonical30DayProgramData.PROGRAM_ADVANCED,
        Canonical30DayProgramData.PROGRAM_EXPERT,
        CanonicalHypertrophyProgramData.PROGRAM_HYPERTROPHY,
        CanonicalStrengthProgramData.PROGRAM_STRENGTH,
        CanonicalAthletePerformanceProgramData.PROGRAM_ATHLETE_PERFORMANCE,
        THIRTY_DAY_DEMONSTRATOR,
        HYPERTROPHY_DEMONSTRATOR
    )
}
