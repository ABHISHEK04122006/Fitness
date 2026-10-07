package com.example.fitness.feature.programs.data

import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.program.data.Canonical30DayProgramData
import com.example.fitness.feature.program.data.CanonicalAthletePerformanceProgramData
import com.example.fitness.feature.program.data.CanonicalFemaleProgramData
import com.example.fitness.feature.program.data.CanonicalHypertrophyProgramData
import com.example.fitness.feature.program.data.CanonicalStrengthProgramData
import com.example.fitness.feature.program.model.Program
import com.example.fitness.feature.programs.model.ProgramScheduleItem
import com.example.fitness.feature.programs.model.WorkoutProgram
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import com.example.fitness.feature.workout.model.ExperienceLevel
import com.example.fitness.feature.workout.model.ThirtyDayWorkout
import fitness.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource

/**
 * Program Registry serving the UI program explorer and workout overview/session routing.
 * Sourced exclusively from canonical Step 6A program definitions.
 */
object ProgramRegistry {

    private fun buildWorkoutProgram(
        program: Program,
        programKey: String,
        subtitle: String,
        goal: String,
        category: String,
        imageRes: DrawableResource
    ): WorkoutProgram {
        return WorkoutProgram(
            id = programKey,
            title = program.name,
            subtitle = subtitle,
            description = program.description,
            duration = "${program.durationDays} Days",
            goal = goal,
            difficulty = program.experienceLevel,
            workoutsCount = program.days.size,
            focus = program.days.flatMap { it.focus }.distinct().take(6).joinToString(", "),
            category = category,
            imageRes = imageRes,
            schedule = program.days.map { day ->
                val workoutId = "${programKey}_day_${day.dayNumber}"
                val title = if (day.isRestDay) {
                    "Rest / Recovery"
                } else {
                    day.title.split("—").getOrNull(1)?.trim() ?: day.title
                }
                ProgramScheduleItem(
                    dayNumber = day.dayNumber,
                    dayTitle = title,
                    focus = day.focus.joinToString(", ").ifEmpty { "Active Recovery" },
                    workoutId = workoutId,
                    duration = if (day.isRestDay) "20-30 min" else "45-60 min",
                    difficulty = if (day.isRestDay) "Low" else "High",
                    exerciseCount = day.exercises.size,
                    isRestDay = day.isRestDay
                )
            }
        )
    }

    fun getThirtyDayPlanProgram(level: ExperienceLevel = UserSession.experienceLevel): WorkoutProgram {
        val canonical = Canonical30DayProgramData.getProgramForLevel(level)
        return buildWorkoutProgram(
            program = canonical,
            programKey = "30-day",
            subtitle = "Structured fitness foundation with progressive overload.",
            goal = "General Fitness & Muscle Tone",
            category = "General",
            imageRes = Res.drawable.bodypart_chest
        )
    }

    val hypertrophyProgram: WorkoutProgram
        get() = buildWorkoutProgram(
            program = CanonicalHypertrophyProgramData.PROGRAM_HYPERTROPHY,
            programKey = "hypertrophy",
            subtitle = "Build muscle through structured hypertrophy-focused training.",
            goal = "Muscle Growth & Hypertrophy",
            category = "Muscle Growth",
            imageRes = Res.drawable.workout_incline_bench
        )

    val strengthProgram: WorkoutProgram
        get() = buildWorkoutProgram(
            program = CanonicalStrengthProgramData.PROGRAM_STRENGTH,
            programKey = "strength",
            subtitle = "Build maximum strength on primary compound movements.",
            goal = "Pure Strength & Power",
            category = "Strength",
            imageRes = Res.drawable.workout_gym
        )

    val athletePerformanceProgram: WorkoutProgram
        get() = buildWorkoutProgram(
            program = CanonicalAthletePerformanceProgramData.PROGRAM_ATHLETE_PERFORMANCE,
            programKey = "athlete",
            subtitle = "Multi-modal athletic conditioning, agility, power, and sprint speed.",
            goal = "Athletic Performance & Speed",
            category = "Performance",
            imageRes = Res.drawable.goal_athlete
        )

    fun getFemalePrograms(): List<WorkoutProgram> {
        return CanonicalFemaleProgramData.ALL_PROGRAMS.map { program ->
            val imageRes = when (program.id) {
                "female_weight_loss" -> Res.drawable.female_walking_lunge
                "female_sculpted_body" -> Res.drawable.female_hip_thrust
                "female_perfect_butt" -> Res.drawable.female_frog_pumps
                "female_6_pack_abs" -> Res.drawable.female_romanian_deadlift
                "female_general_muscle_building" -> Res.drawable.female_dumbbell_bench_press
                "female_powerlifting" -> Res.drawable.female_barbell_squat
                "female_crossfit" -> Res.drawable.female_kettlebell_swing
                "female_full_body_in_45_minutes", "female_full_body_45" -> Res.drawable.female_goblet_squat
                "female_postpartum_recovery" -> Res.drawable.female_glute_bridge
                else -> Res.drawable.gender_female
            }
            buildWorkoutProgram(
                program = program,
                programKey = program.id,
                subtitle = "30-Day progressive plan from the AthGym female curriculum.",
                goal = program.description.split("Progression:").getOrNull(0)?.trim() ?: program.description,
                category = "Female Workout Plans",
                imageRes = imageRes
            )
        }
    }

    fun getAllPrograms(): List<WorkoutProgram> {
        return listOf(
            getThirtyDayPlanProgram(),
            hypertrophyProgram,
            strengthProgram,
            athletePerformanceProgram
        )
    }

    /**
     * Standard programs selectable within the Workout Programs explorer screen.
     * Athlete Training is dedicated to the Home screen entry point.
     */
    fun getWorkoutPrograms(level: ExperienceLevel = UserSession.experienceLevel): List<WorkoutProgram> {
        return listOf(
            getThirtyDayPlanProgram(level),
            hypertrophyProgram,
            strengthProgram
        )
    }

    fun getProgramById(id: String, level: ExperienceLevel = UserSession.experienceLevel): WorkoutProgram? {
        val normalized = id.lowercase().trim()
        val femaleProg = getFemalePrograms().find {
            it.id.equals(normalized, ignoreCase = true) ||
            it.id.removePrefix("female_").equals(normalized.removePrefix("female_"), ignoreCase = true) ||
            (normalized.contains("full_body") && it.id.contains("full_body")) ||
            (normalized.contains("6_pack") && it.id.contains("6_pack")) ||
            (normalized.contains("6pack") && it.id.contains("6_pack"))
        }
        if (femaleProg != null) return femaleProg

        return when {
            normalized == "30-day" || normalized == "30_day" || normalized == "general" || normalized == "general_fitness" || normalized.startsWith("30") -> {
                getThirtyDayPlanProgram(level)
            }
            normalized == "hypertrophy_strength" || normalized == "strength" || normalized.startsWith("strength") -> strengthProgram
            normalized == "hypertrophy" || normalized.startsWith("hypertrophy") -> hypertrophyProgram
            normalized == "athlete" || normalized == "athlete_performance" || normalized == "athlete_training" || normalized.startsWith("athlete") -> athletePerformanceProgram
            else -> getAllPrograms().find { it.id.equals(normalized, ignoreCase = true) }
        }
    }

    /**
     * Checks whether a specific (programId, level) combination has a defined 30-day plan.
     * Prevents silent fallbacks (e.g. Athlete Training + Expert).
     */
    fun isProgramLevelAvailable(programId: String, level: ExperienceLevel): Boolean {
        val normalized = programId.lowercase().trim()
        if (normalized.startsWith("female_")) return true

        return when {
            // General Fitness (30-day) supports all 4 levels in Canonical30DayProgramData
            normalized == "30-day" || normalized == "30_day" || normalized == "general" || normalized == "general_fitness" || normalized.startsWith("30") -> true
            // Athlete Training is designed for Beginner / Intermediate / Advanced (Expert plan not available)
            normalized == "athlete" || normalized == "athlete_performance" || normalized == "athlete_training" || normalized.startsWith("athlete") -> {
                level != ExperienceLevel.EXPERT
            }
            // Hypertrophy / Strength programs are 30-day compound splits designed for Intermediate / Advanced / Expert
            normalized == "strength" || normalized == "hypertrophy" || normalized == "hypertrophy_strength" || normalized.startsWith("strength") || normalized.startsWith("hypertrophy") -> {
                true
            }
            else -> true
        }
    }

    fun getWorkoutById(workoutId: String, programId: String? = null): ThirtyDayWorkout? {
        return try {
            CanonicalWorkoutResolver.resolveThirtyDayWorkout(programId, workoutId)
        } catch (_: Exception) {
            null
        }
    }
}
