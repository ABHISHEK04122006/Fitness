package com.example.fitness.feature.workout.data

import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.media.FemaleExerciseMediaRegistry
import com.example.fitness.feature.exercise.model.ExerciseMedia
import com.example.fitness.feature.exercise.model.ExerciseReference
import com.example.fitness.feature.program.data.Canonical30DayProgramData
import com.example.fitness.feature.program.data.CanonicalAthletePerformanceProgramData
import com.example.fitness.feature.program.data.CanonicalFemaleProgramData
import com.example.fitness.feature.program.data.CanonicalHypertrophyProgramData
import com.example.fitness.feature.program.data.CanonicalStrengthProgramData
import com.example.fitness.feature.program.model.Program
import com.example.fitness.feature.program.model.ProgramDay
import com.example.fitness.feature.workout.model.CoolDownItem
import com.example.fitness.feature.workout.model.DailyWorkoutPlan
import com.example.fitness.feature.workout.model.ExperienceLevel
import com.example.fitness.feature.workout.model.PlanExercise
import com.example.fitness.feature.workout.model.ThirtyDayWorkout
import com.example.fitness.feature.workout.model.WarmUpItem
import com.example.fitness.feature.workout.model.WorkoutPlanExercise
import com.example.fitness.feature.workout.model.WorkoutType
import com.example.fitness.feature.workout.model.BodyPartWorkout
import com.example.fitness.feature.workout.model.ExerciseItem
import com.example.fitness.feature.exercise.data.FullBodyExerciseLibrary
import fitness.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource

/**
 * Unified canonical resolver connecting authoritative program repositories
 * (Base 30-Day across 4 levels, Hypertrophy, Strength, Athlete Performance, and 9 Female Programs)
 * to the complete workout execution flow.
 *
 * Single source of truth for:
 * (programId, dayIdentifier, experienceLevel) -> ProgramDay -> ThirtyDayWorkout / DailyWorkoutPlan
 */
object CanonicalWorkoutResolver {

    private val dayOfWeekShortNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    private val dayOfWeekFullNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    private val REGEX_DRILL_CLEAN = Regex("^[•■🔲\\-\\s—]+|[•■🔲\\-\\s—]+$")
    private val REGEX_PAGE_CLEAN = Regex("=== PAGE \\d+ ===.*")
    private val REGEX_DRILL_SPLIT = Regex("[\\r\\n\\t\\x7f•■🔲|]+|\\s{2,}")
    private val REGEX_DRILL_PARTS = Regex("(?<!\\d)\\s*[—–]\\s*(?!\\d)|\\s+-\\s+")

    private val thirtyDayWorkoutCache = com.example.fitness.createThreadSafeMap<String, ThirtyDayWorkout>()
    private val dailyWorkoutPlanCache = com.example.fitness.createThreadSafeMap<String, DailyWorkoutPlan>()

    fun clearWorkoutResolutionCache() {
        thirtyDayWorkoutCache.clear()
        dailyWorkoutPlanCache.clear()
    }

    /**
     * Resolves the canonical Program definition based on programId and experience level.
     */
    fun resolveProgram(
        programId: String?,
        level: ExperienceLevel = UserSession.experienceLevel
    ): Program {
        val normalized = programId?.lowercase()?.trim() ?: ""
        return when {
            normalized.startsWith("female_") || CanonicalFemaleProgramData.getById(normalized) != null -> {
                CanonicalFemaleProgramData.getById(normalized) ?: CanonicalFemaleProgramData.PROGRAM_FEMALE_WEIGHT_LOSS
            }
            normalized == "hypertrophy" || normalized.startsWith("hypertrophy") -> {
                CanonicalHypertrophyProgramData.PROGRAM_HYPERTROPHY
            }
            normalized == "strength" || normalized.startsWith("strength") -> {
                CanonicalStrengthProgramData.PROGRAM_STRENGTH
            }
            normalized == "athlete" || normalized == "athlete_performance" || normalized.startsWith("athlete") -> {
                CanonicalAthletePerformanceProgramData.PROGRAM_ATHLETE_PERFORMANCE
            }
            else -> {
                // Base 30-Day Workout Plan resolved by experience level
                Canonical30DayProgramData.getProgramForLevel(level)
            }
        }
    }

    /**
     * Resolves a canonical ProgramDay from program ID, day identifier (e.g. "day_8", "wednesday", "8"),
     * and experience level.
     */
    fun resolveProgramDay(
        programId: String?,
        dayIdentifier: String,
        level: ExperienceLevel = UserSession.experienceLevel
    ): ProgramDay {
        val program = resolveProgram(programId, level)
        val lower = dayIdentifier.lowercase().trim()

        // 1. Direct day number extraction (e.g. "day_8", "day8", "8", "hypertrophy_day_8")
        val numberMatch = Regex("\\b(\\d{1,2})\\b").find(lower)
            ?: Regex("(?:day|day_)?(\\d{1,2})").find(lower)
        val extractedNumber = numberMatch?.groupValues?.getOrNull(1)?.toIntOrNull()
        if (extractedNumber != null && extractedNumber in 1..30) {
            val matchingDay = program.days.find { it.dayNumber == extractedNumber }
            if (matchingDay != null) return matchingDay
        }

        // 2. Day of week mapping
        val dayOfWeekMap = mapOf(
            "monday" to 1, "mon" to 1,
            "tuesday" to 2, "tue" to 2,
            "wednesday" to 3, "wed" to 3,
            "thursday" to 4, "thu" to 4,
            "friday" to 5, "fri" to 5,
            "saturday" to 6, "sat" to 6,
            "sunday" to 7, "sun" to 7
        )
        val mappedDayNumber = dayOfWeekMap[lower]
        if (mappedDayNumber != null) {
            val matchingDay = program.days.find { it.dayNumber == mappedDayNumber }
            if (matchingDay != null) return matchingDay
        }

        // 3. Match against title substring
        val matchingByTitle = program.days.firstOrNull { it.title.lowercase().contains(lower) }
        if (matchingByTitle != null) return matchingByTitle

        // 4. Fallback to Day 1
        return program.days.first()
    }

    /**
     * Resolves a canonical ProgramDay by 1-based day number (1..30).
     */
    fun resolveProgramDayByNumber(
        programId: String?,
        dayNumber: Int,
        level: ExperienceLevel = UserSession.experienceLevel
    ): ProgramDay {
        val program = resolveProgram(programId, level)
        val safeDay = dayNumber.coerceIn(1, 30)
        return program.days.find { it.dayNumber == safeDay } ?: program.days.first()
    }

    /**
     * Converts a canonical ProgramDay into a ThirtyDayWorkout entity for workout execution screens.
     */
    fun toThirtyDayWorkout(
        programDay: ProgramDay,
        programId: String? = null,
        level: ExperienceLevel = UserSession.experienceLevel
    ): ThirtyDayWorkout {
        val dayIndex = (programDay.dayNumber - 1) % 7
        val shortDow = dayOfWeekShortNames[dayIndex]

        val (bodyPartId, imageRes, duration, calories, actionBtnText) = resolveDayAesthetics(programDay, programId)

        val difficulty = resolveDifficulty(programId, level)

        val warmUpItems = parseWarmUp(programDay.warmUp, programDay.dayNumber)
        val coolDownItems = parseCoolDown(programDay.coolDown, programDay.dayNumber)

        val isFemale = programId?.lowercase()?.startsWith("female_") == true ||
                UserSession.isFemale

        val planExercises = if (programDay.isRestDay) {
            emptyList()
        } else if (programDay.exercises.isNotEmpty()) {
            programDay.exercises.mapIndexed { index, ref ->
                mapExerciseReferenceToPlanExercise(ref, index, imageRes, isFemale)
            }
        } else {
            val selectedItems = resolveExerciseItemsForProgramDay(programDay, level, UserSession.currentUserGender)
            selectedItems.map { item ->
                val setParts = item.sets.split("×")
                val sets = setParts.getOrNull(0)?.trim() ?: item.sets
                val reps = setParts.getOrNull(1)?.trim() ?: "10-12 reps"
                val canonical = SampleExerciseData.getById(item.id) ?: SampleExerciseData.findCanonical(item.id, item.name)
                PlanExercise(
                    id = item.id,
                    name = item.name,
                    sets = sets,
                    reps = reps,
                    rest = item.restTime,
                    equipment = canonical?.equipment?.joinToString(", ") ?: "Gym Equipment",
                    targetMuscle = canonical?.primaryMuscle?.ifBlank { canonical.category } ?: programDay.focus.firstOrNull().orEmpty(),
                    videoUrl = item.videoDemoUrl,
                    instructions = item.description,
                    imageRes = item.imageRes,
                    media = item.media
                )
            }
        }

        val prefix = when {
            programId?.lowercase()?.startsWith("female_") == true -> "${programId.lowercase().trim()}_day_"
            programId?.lowercase()?.contains("hypertrophy") == true -> "hypertrophy_day_"
            programId?.lowercase()?.contains("strength") == true -> "strength_day_"
            programId?.lowercase()?.contains("athlete") == true -> "athlete_day_"
            else -> "day_"
        }

        val title = if (programDay.isRestDay) {
            "Rest / Recovery"
        } else {
            programDay.title.split("—").getOrNull(1)?.trim() ?: programDay.title
        }

        return ThirtyDayWorkout(
            id = "$prefix${programDay.dayNumber}",
            dayNumber = programDay.dayNumber,
            dayName = "Day ${programDay.dayNumber}",
            dayOfWeek = shortDow,
            workoutTitle = title,
            focus = programDay.focus.joinToString(", ").ifEmpty { "Active Recovery" },
            duration = duration,
            difficulty = difficulty,
            calories = calories,
            imageRes = imageRes,
            bodyPartId = bodyPartId,
            workoutType = "gym",
            isRestDay = programDay.isRestDay,
            actionButtonText = actionBtnText,
            warmUp = warmUpItems,
            exercises = planExercises,
            coolDown = coolDownItems,
            experienceLevel = level
        )
    }

    /**
     * Converts a canonical ProgramDay into a DailyWorkoutPlan entity for Home and Overview screens.
     */
    fun toDailyWorkoutPlan(
        programDay: ProgramDay,
        dayIndex: Int = (programDay.dayNumber - 1) % 7,
        programId: String? = null,
        level: ExperienceLevel = UserSession.experienceLevel
    ): DailyWorkoutPlan {
        val safeIndex = dayIndex.coerceIn(0, 6)
        val shortDow = dayOfWeekShortNames[safeIndex]
        val fullDow = dayOfWeekFullNames[safeIndex]

        val (bodyPartId, imageRes, duration, calories, actionBtnText) = resolveDayAesthetics(programDay, programId)
        val difficulty = resolveDifficulty(programId, level)

        val isFemale = programId?.lowercase()?.startsWith("female_") == true || UserSession.isFemale
        val planExercises = if (programDay.isRestDay) {
            emptyList()
        } else if (programDay.exercises.isNotEmpty()) {
            programDay.exercises.mapIndexed { index, ref ->
                val planEx = mapExerciseReferenceToPlanExercise(ref, index, imageRes, isFemale)
                val setParts = planEx.sets.split("×")
                val sets = setParts.getOrNull(0)?.trim() ?: planEx.sets
                val reps = setParts.getOrNull(1)?.trim() ?: planEx.reps
                WorkoutPlanExercise(
                    id = planEx.id,
                    name = planEx.name,
                    sets = sets,
                    reps = reps,
                    weight = "Target Load",
                    rest = "${com.example.fitness.feature.workout.model.CustomExerciseConfig.parseDefaultRest(planEx.rest)}s"
                )
            }
        } else {
            val selectedItems = resolveExerciseItemsForProgramDay(programDay, level, UserSession.currentUserGender)
            selectedItems.map { item ->
                val setParts = item.sets.split("×")
                val sets = setParts.getOrNull(0)?.trim() ?: item.sets
                val reps = setParts.getOrNull(1)?.trim() ?: "10-12 reps"
                WorkoutPlanExercise(
                    id = item.id,
                    name = item.name,
                    sets = sets,
                    reps = reps,
                    weight = "Target Load",
                    rest = "${com.example.fitness.feature.workout.model.CustomExerciseConfig.parseDefaultRest(item.restTime)}s"
                )
            }
        }

        val equipment = if (programDay.isRestDay) {
            "Bodyweight / Mat"
        } else {
            planExercises.flatMap { ex ->
                SampleExerciseData.getById(ex.id)?.equipment ?: emptyList()
            }.distinct().ifEmpty { listOf("Gym Equipment") }.joinToString(", ")
        }

        val focusString = programDay.focus.joinToString(", ").ifEmpty { "Active Recovery" }
        val title = if (programDay.isRestDay) "Rest / Recovery" else programDay.title.split("—").getOrNull(1)?.trim() ?: programDay.title

        val prefix = when {
            programId?.lowercase()?.startsWith("female_") == true -> "${programId.lowercase().trim()}_day_"
            programId?.lowercase()?.contains("hypertrophy") == true -> "hypertrophy_day_"
            programId?.lowercase()?.contains("strength") == true -> "strength_day_"
            programId?.lowercase()?.contains("athlete") == true -> "athlete_day_"
            else -> "day_"
        }

        return DailyWorkoutPlan(
            id = "$prefix${programDay.dayNumber}",
            dayOfWeek = shortDow,
            dayFullName = fullDow,
            title = title,
            duration = duration,
            difficulty = difficulty,
            target = focusString,
            calories = calories,
            imageRes = imageRes,
            isRestDay = programDay.isRestDay,
            actionButtonText = actionBtnText,
            bodyPartId = bodyPartId,
            workoutType = "gym",
            equipment = equipment,
            about = "${programDay.title}. ${programDay.notes}",
            trainedMuscles = programDay.focus,
            warmup = programDay.warmUp,
            exercises = planExercises,
            cooldown = programDay.coolDown
        )
    }

    fun isBodyPartWorkout(workoutId: String): Boolean {
        val lower = workoutId.trim().lowercase()
        val knownBodyParts = setOf("legs", "chest", "back", "shoulders", "biceps", "triceps", "abs", "core", "full_body", "full body")
        if (knownBodyParts.contains(lower) || lower.startsWith("gym_") || lower.startsWith("home_")) return true
        val stripped = lower.removePrefix("gym_").removePrefix("home_")
        val parts = stripped.split("+", "_", ",", "&").map { it.trim() }.filter { it.isNotEmpty() }
        return parts.size > 1 && parts.all { knownBodyParts.contains(it) }
    }

    private fun bodyPartToThirtyDayWorkout(workout: BodyPartWorkout, level: ExperienceLevel): ThirtyDayWorkout {
        val planExercises = workout.exercises.map { ex ->
            val setParts = ex.sets.split("×")
            val sets = setParts.getOrNull(0)?.trim() ?: ex.sets
            val reps = setParts.getOrNull(1)?.trim() ?: "10-12 reps"
            PlanExercise(
                id = ex.id,
                name = ex.name,
                sets = sets,
                reps = reps,
                rest = "${com.example.fitness.feature.workout.model.CustomExerciseConfig.parseDefaultRest(ex.restTime)}s",
                equipment = workout.equipment,
                targetMuscle = workout.targetMuscle.ifBlank { workout.bodyPart },
                videoUrl = ex.videoDemoUrl,
                instructions = ex.description,
                imageRes = ex.imageRes,
                media = ex.media
            )
        }

        return ThirtyDayWorkout(
            id = workout.id,
            dayNumber = 1,
            dayName = workout.title,
            dayOfWeek = "Day 1",
            workoutTitle = workout.title,
            focus = workout.targetMuscle.ifBlank { workout.subtitle },
            duration = workout.duration,
            difficulty = workout.difficulty,
            calories = workout.calories,
            imageRes = workout.imageRes,
            bodyPartId = workout.id,
            workoutType = workout.workoutType.name.lowercase(),
            isRestDay = false,
            actionButtonText = "Start Workout",
            warmUp = emptyList(),
            exercises = planExercises,
            coolDown = emptyList(),
            experienceLevel = level
        )
    }

    private fun resolveBodyPartWorkout(programId: String?, workoutId: String, level: ExperienceLevel): BodyPartWorkout? {
        val lower = workoutId.trim().lowercase()
        val progLower = programId?.trim()?.lowercase() ?: ""
        val isExplicitBodyPart = isBodyPartWorkout(workoutId) || progLower == "gym" || progLower == "home"
        if (!isExplicitBodyPart) return null

        val type = if (progLower == "home" || lower.startsWith("home_")) WorkoutType.HOME else WorkoutType.GYM
        val bodyPartKey = lower.removePrefix("gym_").removePrefix("home_")
        val gender = UserSession.currentUserGender

        return WorkoutRepository.getWorkout(type, bodyPartKey, gender, level)
            ?: WorkoutRepository.getWorkouts(type, gender, level).find { it.id.equals(bodyPartKey, ignoreCase = true) }
    }

    /**
     * Resolves a ThirtyDayWorkout given programId and workoutId.
     */
    fun resolveThirtyDayWorkout(
        programId: String?,
        workoutId: String,
        level: ExperienceLevel = UserSession.experienceLevel
    ): ThirtyDayWorkout {
        val cacheKey = "${programId ?: ""}|$workoutId|$level|${UserSession.currentUserGender ?: ""}|${SampleExerciseData.canonicalRevision}"
        thirtyDayWorkoutCache[cacheKey]?.let { return it }

        val bpWorkout = resolveBodyPartWorkout(programId, workoutId, level)
        val result = if (bpWorkout != null) {
            bodyPartToThirtyDayWorkout(bpWorkout, level)
        } else {
            val programDay = resolveProgramDay(programId, workoutId, level)
            toThirtyDayWorkout(programDay, programId, level)
        }
        thirtyDayWorkoutCache[cacheKey] = result
        return result
    }

    /**
     * Resolves a DailyWorkoutPlan given programId and workoutId.
     */
    fun resolveDailyWorkoutPlan(
        programId: String?,
        workoutId: String,
        level: ExperienceLevel = UserSession.experienceLevel
    ): DailyWorkoutPlan {
        val cacheKey = "plan|${programId ?: ""}|$workoutId|$level|${UserSession.currentUserGender ?: ""}|${SampleExerciseData.canonicalRevision}"
        dailyWorkoutPlanCache[cacheKey]?.let { return it }

        val bpWorkout = resolveBodyPartWorkout(programId, workoutId, level)
        val result = if (bpWorkout != null) {
            val thirtyDay = bodyPartToThirtyDayWorkout(bpWorkout, level)
            WeeklyWorkoutRepository.toDailyWorkoutPlan(thirtyDay)
        } else {
            val programDay = resolveProgramDay(programId, workoutId, level)
            toDailyWorkoutPlan(programDay, (programDay.dayNumber - 1) % 7, programId, level)
        }
        dailyWorkoutPlanCache[cacheKey] = result
        return result
    }

    /**
     * Resolves DailyWorkoutPlan for day of week index (0=Mon .. 6=Sun) for given program,
     * taking into account the user's weekly commitment/workout frequency (4 Days, 5 Days, 6 Days).
     */
    fun resolveDailyWorkoutPlanForDayIndex(
        programId: String?,
        dayIndex: Int,
        level: ExperienceLevel = UserSession.experienceLevel,
        workoutDays: String? = null
    ): DailyWorkoutPlan {
        val safeIndex = dayIndex.coerceIn(0, 6)
        val cacheKey = "dayIndex|${programId ?: ""}|$safeIndex|$level|${workoutDays ?: ""}|${UserSession.currentUserGender ?: ""}|${SampleExerciseData.canonicalRevision}"
        dailyWorkoutPlanCache[cacheKey]?.let { return it }

        val targetDayNum = if (workoutDays == null) {
            safeIndex + 1
        } else {
            val daysCount = when {
                workoutDays.contains("4") -> 4
                workoutDays.contains("5") -> 5
                workoutDays.contains("6") -> 6
                else -> 6
            }
            when (daysCount) {
                4 -> when (safeIndex) {
                    0 -> 1 // Monday: Day 1 Workout
                    1 -> 2 // Tuesday: Day 2 Workout
                    2 -> 7 // Wednesday: Rest / Recovery
                    3 -> 3 // Thursday: Day 3 Workout
                    4 -> 4 // Friday: Day 4 Workout
                    5 -> 7 // Saturday: Rest / Recovery
                    6 -> 7 // Sunday: Rest / Recovery
                    else -> 7
                }
                5 -> when (safeIndex) {
                    0 -> 1 // Monday: Day 1 Workout
                    1 -> 2 // Tuesday: Day 2 Workout
                    2 -> 3 // Wednesday: Day 3 Workout
                    3 -> 4 // Thursday: Day 4 Workout
                    4 -> 5 // Friday: Day 5 Workout
                    5 -> 7 // Saturday: Rest / Recovery
                    6 -> 7 // Sunday: Rest / Recovery
                    else -> 7
                }
                else -> safeIndex + 1 // 6 Days: Mon..Sat = Days 1..6, Sun = Day 7 (Rest)
            }
        }
        val programDay = resolveProgramDayByNumber(programId, targetDayNum, level)
        val result = toDailyWorkoutPlan(programDay, safeIndex, programId, level)
        dailyWorkoutPlanCache[cacheKey] = result
        return result
    }

    private fun mapExerciseReferenceToPlanExercise(
        ref: ExerciseReference,
        index: Int,
        defaultImage: DrawableResource,
        isFemale: Boolean = false
    ): PlanExercise {
        val cleanId = ref.exerciseId.trim().lowercase()
        val aliasId = SampleExerciseData.resolveCanonicalId(ref.exerciseId).lowercase()
        val exercise = SampleExerciseData.getById(ref.exerciseId)
            ?: SampleExerciseData.findCanonical(ref.exerciseId)

        val isExplicitOverride = SampleExerciseData.getLiveOverrides().any { it.id.equals(exercise?.id, ignoreCase = true) }
        val displayName = if (isExplicitOverride && exercise?.name?.isNotBlank() == true) {
            exercise.name
        } else {
            exercise?.name ?: ref.exerciseId.replace('_', ' ')
                .split(" ")
                .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
        }

        val equipmentStr = exercise?.equipment?.joinToString(", ")?.ifEmpty { "Gym Equipment" } ?: "Gym Equipment"
        val targetMuscleStr = exercise?.primaryMuscle?.ifEmpty { "Full Body" } ?: "Full Body"
        val instructionsStr = exercise?.instructions?.joinToString(" ")?.ifEmpty {
            "Perform with controlled form and tempo."
        } ?: "Perform with controlled form and tempo."

        val formattedReps = if (ref.reps.endsWith("reps", ignoreCase = true) ||
            ref.reps.contains("m") ||
            ref.reps.contains("sec")
        ) {
            ref.reps
        } else {
            "${ref.reps} reps"
        }

        val displaySets = if (isExplicitOverride && exercise != null && exercise.defaultSets > 0) {
            "${exercise.defaultSets} Sets"
        } else {
            "${ref.sets} Sets"
        }

        val displayReps = if (isExplicitOverride && exercise != null && exercise.defaultReps.isNotBlank()) {
            if (exercise.defaultReps.endsWith("reps", ignoreCase = true) || exercise.defaultReps.contains("m") || exercise.defaultReps.contains("sec")) {
                exercise.defaultReps
            } else {
                "${exercise.defaultReps} reps"
            }
        } else {
            formattedReps
        }

        val displayRest = if (isExplicitOverride && exercise != null && exercise.defaultRestSeconds > 0) {
            "${exercise.defaultRestSeconds}s"
        } else if (ref.restSeconds > 0) {
            "${ref.restSeconds}s"
        } else if (exercise != null && exercise.defaultRestSeconds > 0) {
            "${exercise.defaultRestSeconds}s"
        } else {
            "60s"
        }

        val resolvedImage = if (isFemale) {
            FemaleExerciseMediaRegistry.resolveExerciseImage(ref.exerciseId, isFemale = true, defaultFallback = defaultImage)
        } else {
            defaultImage
        }

        return PlanExercise(
            id = exercise?.id ?: ref.exerciseId,
            name = displayName,
            sets = displaySets,
            reps = displayReps,
            rest = displayRest,
            equipment = equipmentStr,
            targetMuscle = targetMuscleStr,
            videoUrl = exercise?.media?.videoUrl?.ifBlank { "" } ?: "",
            instructions = instructionsStr,
            imageRes = resolvedImage,
            media = exercise?.media ?: ExerciseMedia()
        )
    }

    private fun cleanDrillText(str: String): String {
        return str.replace(REGEX_DRILL_CLEAN, "").trim()
    }

    fun parseWarmUp(rawItems: List<String>, dayNumber: Int): List<WarmUpItem> {
        val result = mutableListOf<WarmUpItem>()
        var counter = 1
        for (itemStr in rawItems) {
            val cleaned = itemStr.replace(REGEX_PAGE_CLEAN, "").trim()
            if (cleaned.isBlank()) continue

            val subItems = cleaned
                .split(REGEX_DRILL_SPLIT)
                .map { it.trim().removePrefix("-").removePrefix("—").trim() }
                .filter { it.isNotBlank() }

            for (sub in subItems) {
                val parts = sub.split(REGEX_DRILL_PARTS, limit = 2)
                val rawName = cleanDrillText(parts[0])
                var durationOrReps = if (parts.size > 1) cleanDrillText(parts[1]) else "Active"
                var instructions = "Perform with controlled form and steady breathing."

                if (durationOrReps.length > 35) {
                    instructions = durationOrReps
                    durationOrReps = durationOrReps.take(30).trimEnd() + "…"
                }

                if (rawName.isNotBlank()) {
                    result.add(
                        WarmUpItem(
                            id = "warmup_${dayNumber}_$counter",
                            name = rawName,
                            durationOrReps = durationOrReps.ifBlank { "Active" },
                            instructions = instructions
                        )
                    )
                    counter++
                }
            }
        }
        return result
    }

    fun parseCoolDown(rawItems: List<String>, dayNumber: Int): List<CoolDownItem> {
        val result = mutableListOf<CoolDownItem>()
        var counter = 1
        for (itemStr in rawItems) {
            val cleaned = itemStr.replace(REGEX_PAGE_CLEAN, "").trim()
            if (cleaned.isBlank()) continue

            val subItems = cleaned
                .split(REGEX_DRILL_SPLIT)
                .map { it.trim().removePrefix("-").removePrefix("—").trim() }
                .filter { it.isNotBlank() }

            for (sub in subItems) {
                val parts = sub.split(REGEX_DRILL_PARTS, limit = 2)
                val rawName = cleanDrillText(parts[0])
                var durationOrReps = if (parts.size > 1) cleanDrillText(parts[1]) else "Gentle Stretch"
                var instructions = "Hold gently without bouncing; focus on deep exhalations."

                if (durationOrReps.length > 35) {
                    instructions = durationOrReps
                    durationOrReps = durationOrReps.take(30).trimEnd() + "…"
                }

                if (rawName.isNotBlank()) {
                    result.add(
                        CoolDownItem(
                            id = "cooldown_${dayNumber}_$counter",
                            name = rawName,
                            durationOrReps = durationOrReps.ifBlank { "Gentle Stretch" },
                            instructions = instructions
                        )
                    )
                    counter++
                }
            }
        }
        return result
    }

    private data class DayAesthetics(
        val bodyPartId: String,
        val imageRes: DrawableResource,
        val duration: String,
        val calories: String,
        val actionButtonText: String
    )

    private fun resolveDayAesthetics(day: ProgramDay, programId: String? = null): DayAesthetics {
        val isFemale = programId?.lowercase()?.startsWith("female_") == true || UserSession.isFemale
        val isMale = UserSession.currentUserGender?.equals("Male", ignoreCase = true) == true ||
                (!isFemale && UserSession.currentUserProfile?.gender?.equals("Male", ignoreCase = true) == true)

        if (day.isRestDay) {
            val restImage = when {
                isFemale -> Res.drawable.female_glute_bridge
                isMale -> Res.drawable.program_endurance
                else -> Res.drawable.goal_general
            }
            return DayAesthetics(
                bodyPartId = "full_body",
                imageRes = restImage,
                duration = "30 min",
                calories = "~160 kcal",
                actionButtonText = "Start Recovery"
            )
        }

        val cycleDay = ((day.dayNumber - 1) % 7) + 1
        val image = when {
            isFemale -> when (cycleDay) {
                1 -> Res.drawable.female_dumbbell_bench_press
                2 -> Res.drawable.female_lat_pulldown
                3 -> Res.drawable.female_barbell_squat
                4 -> Res.drawable.female_kettlebell_swing
                5 -> Res.drawable.female_hip_thrust
                6 -> Res.drawable.female_romanian_deadlift
                else -> Res.drawable.female_glute_bridge
            }
            isMale -> when (cycleDay) {
                1 -> Res.drawable.bodypart_chest
                2 -> Res.drawable.bodypart_back
                3 -> Res.drawable.gym_barbell_squat
                4 -> Res.drawable.gym_shoulder_press
                5 -> Res.drawable.workout_incline_bench
                6 -> Res.drawable.workout_home
                else -> Res.drawable.program_endurance
            }
            else -> Res.drawable.goal_general
        }

        return when (cycleDay) {
            1 -> DayAesthetics("chest", image, "45 min", "~380 kcal", "Start Workout")
            2 -> DayAesthetics("back", image, "45 min", "~390 kcal", "Start Workout")
            3 -> DayAesthetics("legs", image, "50 min", "~460 kcal", "Start Workout")
            4 -> DayAesthetics("shoulders", image, "40 min", "~340 kcal", "Start Workout")
            5 -> DayAesthetics("biceps", image, "45 min", "~360 kcal", "Start Workout")
            6 -> DayAesthetics("abs", image, "35 min", "~300 kcal", "Start Workout")
            else -> DayAesthetics("full_body", image, "30 min", "~160 kcal", "Start Recovery")
        }
    }

    private fun resolveDifficulty(programId: String?, level: ExperienceLevel): String {
        val norm = programId?.lowercase()?.trim() ?: ""
        if (norm.startsWith("female_")) return "Beginner"
        if (norm == "hypertrophy") return "Advanced"
        if (norm == "strength") return "Advanced"
        if (norm == "athlete") return "Challenging"
        return when (level) {
            ExperienceLevel.NO_EXPERIENCE -> "No Experience"
            ExperienceLevel.BEGINNER -> "Beginner"
            ExperienceLevel.ADVANCED -> "Advanced"
            ExperienceLevel.EXPERT -> "Expert"
        }
    }

    /**
     * Dynamically resolves the level-specific exercise items for a scheduled ProgramDay.
     * Rule: User Experience Level + Scheduled Workout Body Part(s) = Correct Level-Specific Exercises
     */
    fun resolveExerciseItemsForProgramDay(
        programDay: ProgramDay,
        level: ExperienceLevel,
        gender: String? = UserSession.currentUserGender
    ): List<ExerciseItem> {
        if (programDay.isRestDay) return emptyList()

        val scheduledFocus = if (programDay.focus.isNotEmpty()) {
            programDay.focus
        } else {
            val titleFocus = programDay.title.split("—").getOrNull(1)?.split("(")?.getOrNull(0)?.trim()
            val parsed = titleFocus?.split("+", "&", ",")?.map { it.trim() }?.filter { it.isNotEmpty() }
            if (!parsed.isNullOrEmpty()) parsed else listOf("Full Body")
        }

        val totalTemplateCount = programDay.exercises.size.coerceAtLeast(5)
        val selectedItems = mutableListOf<ExerciseItem>()
        val seenExerciseIds = mutableSetOf<String>()

        for (bp in scheduledFocus) {
            val pool = WorkoutRepository.fetchExercises(bp, level, gender)
            if (pool.isEmpty()) continue

            // Determine target exercise count for this body part
            val templateCountForBp = programDay.exercises.count { ref ->
                val ex = SampleExerciseData.getById(ref.exerciseId) ?: SampleExerciseData.findCanonical(ref.exerciseId)
                ex != null && WorkoutRepository.matchesBodyPart(ex, bp.lowercase(), bp)
            }
            val targetCount = if (templateCountForBp > 0) {
                templateCountForBp
            } else {
                (totalTemplateCount / scheduledFocus.size).coerceAtLeast(1)
            }

            val pickedForBp = mutableListOf<ExerciseItem>()

            // 1. Prioritize template exercises that legitimately belong to this level-specific pool
            for (ref in programDay.exercises) {
                if (pickedForBp.size >= targetCount) break
                val refClean = ref.exerciseId.trim().lowercase()
                val refCanon = SampleExerciseData.resolveCanonicalId(ref.exerciseId).lowercase()
                val match = pool.find { item ->
                    val itemId = item.id.trim().lowercase()
                    val itemCanon = SampleExerciseData.resolveCanonicalId(item.id).lowercase()
                    itemId == refClean || itemId == refCanon || itemCanon == refClean || itemCanon == refCanon
                }
                if (match != null) {
                    val matchClean = match.id.trim().lowercase()
                    if (!seenExerciseIds.contains(matchClean)) {
                        seenExerciseIds.add(matchClean)
                        pickedForBp.add(match)
                    }
                }
            }

            // 2. If more exercises are needed to fulfill targetCount, select deterministically from remaining pool
            if (pickedForBp.size < targetCount) {
                val remaining = pool.filter { item -> !seenExerciseIds.contains(item.id.trim().lowercase()) }
                val needed = targetCount - pickedForBp.size
                val offset = (programDay.dayNumber - 1).coerceAtLeast(0)
                for (i in 0 until needed) {
                    if (remaining.isNotEmpty()) {
                        val item = remaining[(offset + i) % remaining.size]
                        val itemClean = item.id.trim().lowercase()
                        if (!seenExerciseIds.contains(itemClean)) {
                            seenExerciseIds.add(itemClean)
                            pickedForBp.add(item)
                        }
                    }
                }
            }

            selectedItems.addAll(pickedForBp)
        }

        if (selectedItems.isEmpty()) {
            val fbPool = FullBodyExerciseLibrary.getExerciseItemsForLevel(level)
            return fbPool.take(totalTemplateCount)
        }

        return selectedItems
    }
}
