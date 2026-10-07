package com.example.fitness.feature.workout

import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.program.data.Canonical30DayProgramData
import com.example.fitness.feature.program.data.CanonicalAthletePerformanceProgramData
import com.example.fitness.feature.program.data.CanonicalHypertrophyProgramData
import com.example.fitness.feature.program.data.CanonicalStrengthProgramData
import com.example.fitness.feature.progress.data.WorkoutProgressService
import com.example.fitness.feature.programs.data.ProgramRegistry
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import com.example.fitness.feature.workout.data.ThirtyDayWorkoutRepository
import com.example.fitness.feature.workout.data.WeeklyWorkoutRepository
import com.example.fitness.feature.workout.model.ExperienceLevel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Step 6B Integration Tests:
 * Validates the end-to-end integration between Canonical Program Repositories
 * and the Workout Execution Flow across all 18 required scenarios.
 */
class CanonicalWorkoutExecutionIntegrationTest {

    @BeforeTest
    fun setUp() {
        WorkoutProgressService.clear()
        UserSession.clear()
        UserSession.experienceLevel = ExperienceLevel.BEGINNER
        UserSession.setActiveProgram("30-day")
    }

    @kotlin.test.AfterTest
    fun tearDown() {
        WorkoutProgressService.clear()
        UserSession.clear()
    }

    // -----------------------------------------------------------------------------------------
    // 1. Base program Day 1 resolves correctly.
    // -----------------------------------------------------------------------------------------
    @Test
    fun testBaseProgramDay1ResolvesCorrectly() {
        val day1 = CanonicalWorkoutResolver.resolveProgramDay("30-day", "day_1", ExperienceLevel.BEGINNER)
        assertEquals(1, day1.dayNumber)
        assertTrue(day1.title.contains("Day 1"))
        assertTrue(day1.focus.contains("Chest"))
        assertTrue(day1.focus.contains("Triceps"))
        assertFalse(day1.isRestDay)
        assertTrue(day1.exercises.isNotEmpty())

        val workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("30-day", "day_1", ExperienceLevel.BEGINNER)
        assertEquals("Day 1", workout.dayName)
        assertEquals("Mon", workout.dayOfWeek)
        assertEquals("Start Workout", workout.actionButtonText)
        assertFalse(workout.isRestDay)
        assertEquals(day1.exercises.size, workout.exercises.size)
    }

    // -----------------------------------------------------------------------------------------
    // 2. Base program Day 8 resolves correctly.
    // -----------------------------------------------------------------------------------------
    @Test
    fun testBaseProgramDay8ResolvesCorrectly() {
        val day8 = CanonicalWorkoutResolver.resolveProgramDay("30-day", "day_8", ExperienceLevel.BEGINNER)
        assertEquals(8, day8.dayNumber)
        assertTrue(day8.title.contains("Day 8"))
        assertTrue(day8.focus.contains("Chest"))
        assertTrue(day8.focus.contains("Triceps"))
        assertFalse(day8.isRestDay)

        val workout = ThirtyDayWorkoutRepository.getWorkoutByDayNumber(8, ExperienceLevel.BEGINNER)
        assertEquals(8, workout.dayNumber)
        assertEquals("Mon", workout.dayOfWeek)
        assertEquals(day8.exercises.size, workout.exercises.size)
    }

    // -----------------------------------------------------------------------------------------
    // 3. Base program Day 15 resolves correctly.
    // -----------------------------------------------------------------------------------------
    @Test
    fun testBaseProgramDay15ResolvesCorrectly() {
        val day15 = CanonicalWorkoutResolver.resolveProgramDay("30-day", "day_15", ExperienceLevel.ADVANCED)
        assertEquals(15, day15.dayNumber)
        assertTrue(day15.title.contains("Day 15"))
        assertTrue(day15.title.contains("Rotation B"))
        assertTrue(day15.focus.contains("Chest") || day15.focus.contains("Incline"))
        assertFalse(day15.isRestDay)
        assertTrue(day15.exercises.isNotEmpty())
    }

    // -----------------------------------------------------------------------------------------
    // 4. Base program Day 22 resolves correctly.
    // -----------------------------------------------------------------------------------------
    @Test
    fun testBaseProgramDay22ResolvesCorrectly() {
        val day22 = CanonicalWorkoutResolver.resolveProgramDay("30-day", "day_22", ExperienceLevel.EXPERT)
        assertEquals(22, day22.dayNumber)
        assertTrue(day22.title.contains("Day 22"))
        assertTrue(day22.title.contains("Rotation C"))
        assertFalse(day22.isRestDay)
        assertTrue(day22.exercises.isNotEmpty())
    }

    // -----------------------------------------------------------------------------------------
    // 5. Base program Day 29 resolves correctly.
    // -----------------------------------------------------------------------------------------
    @Test
    fun testBaseProgramDay29ResolvesCorrectly() {
        val day29 = CanonicalWorkoutResolver.resolveProgramDay("30-day", "day_29", ExperienceLevel.NO_EXPERIENCE)
        assertEquals(29, day29.dayNumber)
        assertTrue(day29.title.contains("Day 29"))
        assertTrue(day29.title.contains("Rotation D"))
        assertFalse(day29.isRestDay)
        assertTrue(day29.exercises.isNotEmpty())
    }

    // -----------------------------------------------------------------------------------------
    // 6. Each experience level resolves its own prescriptions.
    // -----------------------------------------------------------------------------------------
    @Test
    fun testEachExperienceLevelResolvesOwnPrescriptions() {
        val day1NoExp = CanonicalWorkoutResolver.resolveProgramDay("30-day", "1", ExperienceLevel.NO_EXPERIENCE)
        val day1Beg = CanonicalWorkoutResolver.resolveProgramDay("30-day", "1", ExperienceLevel.BEGINNER)
        val day1Adv = CanonicalWorkoutResolver.resolveProgramDay("30-day", "1", ExperienceLevel.ADVANCED)
        val day1Exp = CanonicalWorkoutResolver.resolveProgramDay("30-day", "1", ExperienceLevel.EXPERT)

        // No experience: machine press focus, 2 sets
        assertEquals("machine_chest_press", day1NoExp.exercises[0].exerciseId)
        assertEquals(2, day1NoExp.exercises[0].sets)

        // Beginner: dumbbell press, 3 sets
        assertEquals("dumbbell_bench_press", day1Beg.exercises[0].exerciseId)
        assertEquals(3, day1Beg.exercises[0].sets)

        // Advanced: barbell press, 4 sets
        assertEquals("barbell_bench_press", day1Adv.exercises[0].exerciseId)
        assertEquals(4, day1Adv.exercises[0].sets)

        // Expert: barbell bench press, 4 sets with 5-8 reps
        assertEquals("barbell_bench_press", day1Exp.exercises[0].exerciseId)
        assertEquals("5-8", day1Exp.exercises[0].reps)
        assertEquals(7, day1Exp.exercises.size)
    }

    // -----------------------------------------------------------------------------------------
    // 7. Rest days resolve as rest days.
    // -----------------------------------------------------------------------------------------
    @Test
    fun testRestDaysResolveAsRestDays() {
        val restDays = listOf(7, 14, 21, 28)
        for (dayNum in restDays) {
            val day = CanonicalWorkoutResolver.resolveProgramDayByNumber("30-day", dayNum, ExperienceLevel.BEGINNER)
            assertTrue(day.isRestDay, "Day $dayNum must be designated as a rest day")

            val workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("30-day", "day_$dayNum", ExperienceLevel.BEGINNER)
            assertTrue(workout.isRestDay, "Workout for Day $dayNum must be marked isRestDay = true")
            assertEquals("Start Recovery", workout.actionButtonText)
            assertEquals("Rest / Recovery", workout.workoutTitle)
        }
    }

    // -----------------------------------------------------------------------------------------
    // 8. Rest days contain no resistance working sets.
    // -----------------------------------------------------------------------------------------
    @Test
    fun testRestDaysContainNoResistanceWorkingSets() {
        val restDays = listOf(7, 14, 21, 28)
        for (dayNum in restDays) {
            val day = CanonicalWorkoutResolver.resolveProgramDayByNumber("30-day", dayNum, ExperienceLevel.ADVANCED)
            assertTrue(day.exercises.isEmpty(), "Canonical ProgramDay $dayNum exercises must be empty")

            val workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("30-day", "day_$dayNum", ExperienceLevel.ADVANCED)
            assertTrue(workout.exercises.isEmpty(), "ThirtyDayWorkout $dayNum exercises must be empty")

            val plan = CanonicalWorkoutResolver.resolveDailyWorkoutPlan("30-day", "day_$dayNum", ExperienceLevel.ADVANCED)
            assertTrue(plan.exercises.isEmpty(), "DailyWorkoutPlan $dayNum exercises must be empty")
        }
    }

    // -----------------------------------------------------------------------------------------
    // 9. Warm-up is separate from working sets.
    // -----------------------------------------------------------------------------------------
    @Test
    fun testWarmUpIsSeparateFromWorkingSets() {
        val workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("30-day", "day_1", ExperienceLevel.BEGINNER)
        assertTrue(workout.warmUp.isNotEmpty(), "Warm-up must be populated")
        assertTrue(workout.exercises.isNotEmpty(), "Main exercises must be populated")

        // Warm-up items must not bleed into resistance working sets
        val warmUpNames = workout.warmUp.map { it.name }
        val exerciseIds = workout.exercises.map { it.id }
        for (wName in warmUpNames) {
            assertFalse(exerciseIds.contains(wName), "Warm-up $wName must not be an exerciseId in working sets")
        }
    }

    // -----------------------------------------------------------------------------------------
    // 10. Cool-down is separate from working sets.
    // -----------------------------------------------------------------------------------------
    @Test
    fun testCoolDownIsSeparateFromWorkingSets() {
        val workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("30-day", "day_1", ExperienceLevel.BEGINNER)
        assertTrue(workout.coolDown.isNotEmpty(), "Cool-down must be populated")
        val coolDownNames = workout.coolDown.map { it.name }
        val exerciseIds = workout.exercises.map { it.id }
        for (cdName in coolDownNames) {
            assertFalse(exerciseIds.contains(cdName), "Cool-down $cdName must not be an exerciseId in working sets")
        }
    }

    // -----------------------------------------------------------------------------------------
    // 11. ExerciseReference resolves to a valid Exercise.
    // -----------------------------------------------------------------------------------------
    @Test
    fun testExerciseReferenceResolvesToValidExercise() {
        val allPrograms = Canonical30DayProgramData.ALL_CANONICAL_PROGRAMS +
                listOf(
                    CanonicalHypertrophyProgramData.PROGRAM_HYPERTROPHY,
                    CanonicalStrengthProgramData.PROGRAM_STRENGTH,
                    CanonicalAthletePerformanceProgramData.PROGRAM_ATHLETE_PERFORMANCE
                )

        for (prog in allPrograms) {
            for (day in prog.days) {
                for (ref in day.exercises) {
                    val entity = SampleExerciseData.getById(ref.exerciseId)
                    assertNotNull(entity, "ExerciseReference '${ref.exerciseId}' in '${prog.id}' Day ${day.dayNumber} must exist in SampleExerciseData")
                    assertTrue(entity.name.isNotBlank(), "Exercise name must not be blank for ${ref.exerciseId}")
                }
            }
        }
    }

    // -----------------------------------------------------------------------------------------
    // 12. Stable exercise IDs are preserved.
    // -----------------------------------------------------------------------------------------
    @Test
    fun testStableExerciseIdsArePreserved() {
        val canonicalIds = listOf(
            "barbell_bench_press",
            "incline_dumbbell_press",
            "cable_fly",
            "rope_pushdown",
            "deadlift",
            "bent_over_barbell_row",
            "squat",
            "barbell_overhead_press",
            "barbell_bicep_curl",
            "skull_crushers"
        )
        for (id in canonicalIds) {
            val ex = SampleExerciseData.getById(id)
            assertNotNull(ex, "Canonical exercise '$id' must exist")
            assertEquals(id, ex.id)
        }
    }

    // -----------------------------------------------------------------------------------------
    // 13. User performance does not overwrite canonical prescriptions.
    // -----------------------------------------------------------------------------------------
    @Test
    fun testUserPerformanceDoesNotOverwriteCanonicalPrescriptions() {
        val day1Before = CanonicalWorkoutResolver.resolveProgramDay("30-day", "1", ExperienceLevel.BEGINNER)
        val originalPrescription = day1Before.exercises.first()
        val originalSets = originalPrescription.sets
        val originalReps = originalPrescription.reps

        // Simulate user logging progress
        WorkoutProgressService.saveWorkoutSession(
            workoutId = "day_1",
            dayNumber = 1,
            workoutTitle = "Chest + Triceps",
            targetMuscle = listOf("Chest", "Triceps"),
            experienceLevel = "beginner",
            durationMinutes = 45,
            totalExercises = 5,
            completedExercises = 5,
            totalSets = 15,
            completedSets = 15,
            programId = "30-day"
        )

        // Verify canonical prescription is intact
        val day1After = CanonicalWorkoutResolver.resolveProgramDay("30-day", "1", ExperienceLevel.BEGINNER)
        val afterPrescription = day1After.exercises.first()
        assertEquals(originalSets, afterPrescription.sets, "Prescribed sets must not change after user logs workout")
        assertEquals(originalReps, afterPrescription.reps, "Prescribed reps must not change after user logs workout")
    }

    // -----------------------------------------------------------------------------------------
    // 14. Hypertrophy program resolves correctly.
    // -----------------------------------------------------------------------------------------
    @Test
    fun testHypertrophyProgramResolvesCorrectly() {
        val program = CanonicalWorkoutResolver.resolveProgram("hypertrophy")
        assertEquals(CanonicalHypertrophyProgramData.PROGRAM_HYPERTROPHY.id, program.id)
        assertEquals(30, program.days.size)

        val day1Workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("hypertrophy", "hypertrophy_day_1")
        assertEquals(1, day1Workout.dayNumber)
        assertTrue(day1Workout.workoutTitle.contains("Chest + Triceps"))
        assertEquals("barbell_bench_press", day1Workout.exercises[0].id)
        assertEquals("4 Sets", day1Workout.exercises[0].sets)
    }

    // -----------------------------------------------------------------------------------------
    // 15. Strength program resolves correctly.
    // -----------------------------------------------------------------------------------------
    @Test
    fun testStrengthProgramResolvesCorrectly() {
        val program = CanonicalWorkoutResolver.resolveProgram("strength")
        assertEquals(CanonicalStrengthProgramData.PROGRAM_STRENGTH.id, program.id)
        assertEquals(30, program.days.size)

        val day1Workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("strength", "strength_day_1")
        assertEquals(1, day1Workout.dayNumber)
        assertEquals("barbell_bench_press", day1Workout.exercises[0].id)
        assertEquals("5 Sets", day1Workout.exercises[0].sets)
        assertEquals("5 reps", day1Workout.exercises[0].reps)
    }

    // -----------------------------------------------------------------------------------------
    // 16. Athlete Performance program resolves correctly.
    // -----------------------------------------------------------------------------------------
    @Test
    fun testAthletePerformanceProgramResolvesCorrectly() {
        val program = CanonicalWorkoutResolver.resolveProgram("athlete")
        assertEquals(CanonicalAthletePerformanceProgramData.PROGRAM_ATHLETE_PERFORMANCE.id, program.id)
        assertEquals(30, program.days.size)

        val day1Workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("athlete", "athlete_day_1")
        assertEquals(1, day1Workout.dayNumber)
        assertTrue(day1Workout.workoutTitle.contains("Acceleration & Sprint"))
        assertEquals("ten_meter_acceleration", day1Workout.exercises[0].id)
    }

    // -----------------------------------------------------------------------------------------
    // 17. Workout completion creates the correct history identity.
    // -----------------------------------------------------------------------------------------
    @Test
    fun testWorkoutCompletionCreatesCorrectHistoryIdentity() {
        val initialHistorySize = WorkoutProgressService.workoutHistory.size

        val record = WorkoutProgressService.saveWorkoutSession(
            workoutId = "athlete_day_2",
            dayNumber = 2,
            workoutTitle = "Day 2 — Strength & Structural Power",
            targetMuscle = listOf("Strength & Structural Power"),
            experienceLevel = "advanced",
            durationMinutes = 50,
            totalExercises = 6,
            completedExercises = 6,
            totalSets = 19,
            completedSets = 19,
            programId = "athlete",
            programName = "Athlete Performance"
        )

        assertEquals("athlete", record.programId)
        assertEquals(2, record.dayNumber)
        assertEquals("athlete_day_2", record.workoutId)
        assertEquals(19, record.totalSets)
        assertEquals(19, record.completedSets)
        assertEquals("completed", record.completionStatus)
        assertTrue(WorkoutProgressService.workoutHistory.size > initialHistorySize)
        assertTrue(UserSession.isProgramDayCompleted("athlete", 2))
    }

    // -----------------------------------------------------------------------------------------
    // 18. Existing repositories and ProgramRegistry continue to pass.
    // -----------------------------------------------------------------------------------------
    @Test
    fun testExistingRepositoriesAndRegistryContinueToPass() {
        val dailyPlan = WeeklyWorkoutRepository.getWorkoutForDay(0, ExperienceLevel.BEGINNER)
        assertEquals("Day 1", dailyPlan.id.replace("day_", "Day "))
        assertEquals("Mon", dailyPlan.dayOfWeek)
        assertFalse(dailyPlan.isRestDay)

        val all30Days = ThirtyDayWorkoutRepository.get30DayPlan(ExperienceLevel.BEGINNER)
        assertEquals(30, all30Days.size)

        val allPrograms = ProgramRegistry.getAllPrograms()
        assertEquals(4, allPrograms.size)
        assertNotNull(ProgramRegistry.getProgramById("30-day"))
        assertNotNull(ProgramRegistry.getProgramById("hypertrophy"))
        assertNotNull(ProgramRegistry.getProgramById("strength"))
        assertNotNull(ProgramRegistry.getProgramById("athlete"))
    }

    // -----------------------------------------------------------------------------------------
    // 19. Athlete Day 3 warm-up resolves with distinct, clean items and no corrupted characters.
    // -----------------------------------------------------------------------------------------
    @Test
    fun testAthleteDay3WarmUpResolvesCleanly() {
        val workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("athlete", "day_3", ExperienceLevel.ADVANCED)
        assertEquals(7, workout.warmUp.size)

        assertEquals("Easy jog", workout.warmUp[0].name)
        assertEquals("3 min", workout.warmUp[0].durationOrReps)

        assertEquals("Lateral shuffle", workout.warmUp[1].name)
        assertEquals("2×15m", workout.warmUp[1].durationOrReps)

        assertEquals("Carioca", workout.warmUp[2].name)
        assertEquals("2×15m", workout.warmUp[2].durationOrReps)

        assertEquals("Leg swings", workout.warmUp[3].name)
        assertEquals("10/leg", workout.warmUp[3].durationOrReps)

        assertEquals("Hip openers", workout.warmUp[4].name)
        assertEquals("10/side", workout.warmUp[4].durationOrReps)

        assertEquals("A-skips", workout.warmUp[5].name)
        assertEquals("2×15m", workout.warmUp[5].durationOrReps)

        assertEquals("Deceleration drill", workout.warmUp[6].name)
        assertEquals("3×10m", workout.warmUp[6].durationOrReps)

        // Ensure none contain box characters or control characters
        workout.warmUp.forEach { item ->
            assertFalse(item.name.contains("\u007F") || item.name.contains("🔲"))
            assertFalse(item.durationOrReps.contains("\u007F") || item.durationOrReps.contains("🔲"))
            assertTrue(item.durationOrReps.length <= 35)
        }
    }

    // -----------------------------------------------------------------------------------------
    // 20. Parser defensively cleans raw multi-drill strings and page markers.
    // -----------------------------------------------------------------------------------------
    @Test
    fun testDefensiveWarmUpAndCoolDownParsing() {
        val rawWarmUp = listOf("Jog — 3 min \u007F Lateral shuffle — 2×15m   Carioca — 2×15m")
        val parsedWarmUp = CanonicalWorkoutResolver.parseWarmUp(rawWarmUp, 1)
        assertEquals(3, parsedWarmUp.size)
        assertEquals("Jog", parsedWarmUp[0].name)
        assertEquals("3 min", parsedWarmUp[0].durationOrReps)
        assertEquals("Lateral shuffle", parsedWarmUp[1].name)
        assertEquals("2×15m", parsedWarmUp[1].durationOrReps)
        assertEquals("Carioca", parsedWarmUp[2].name)
        assertEquals("2×15m", parsedWarmUp[2].durationOrReps)

        val rawCoolDown = listOf("5–10 min relevant mobility/stretching === PAGE 12 === Athgym Goal Selection")
        val parsedCoolDown = CanonicalWorkoutResolver.parseCoolDown(rawCoolDown, 1)
        assertEquals(1, parsedCoolDown.size)
        assertEquals("5–10 min relevant mobility/stretching", parsedCoolDown[0].name)
        assertFalse(parsedCoolDown[0].name.contains("PAGE 12"))
        assertFalse(parsedCoolDown[0].instructions.contains("PAGE 12"))
    }
}
