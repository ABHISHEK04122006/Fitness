package com.example.fitness.feature.program

import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.program.data.Canonical30DayProgramData
import com.example.fitness.feature.program.data.CanonicalAthletePerformanceProgramData
import com.example.fitness.feature.program.data.CanonicalHypertrophyProgramData
import com.example.fitness.feature.program.data.CanonicalStrengthProgramData
import com.example.fitness.feature.program.model.ProgramType
import com.example.fitness.feature.program.model.ProgramWorkoutType
import com.example.fitness.feature.workout.data.WeeklyWorkoutRepository
import com.example.fitness.feature.workout.model.ExperienceLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Canonical30DayPlanTest {

    // 1. Exactly 4 canonical 30-day program variants exist
    @Test
    fun testExactlyFourCanonicalProgramsExist() {
        val programs = Canonical30DayProgramData.ALL_CANONICAL_PROGRAMS
        assertEquals(4, programs.size)
        val expectedLevels = setOf(
            ExperienceLevel.NO_EXPERIENCE.name,
            ExperienceLevel.BEGINNER.name,
            ExperienceLevel.ADVANCED.name,
            ExperienceLevel.EXPERT.name
        )
        val actualLevels = programs.map { it.experienceLevel }.toSet()
        assertEquals(expectedLevels, actualLevels)
    }

    // 2. Each program contains exactly 30 days
    @Test
    fun testEachProgramContainsExactlyThirtyDays() {
        for (program in Canonical30DayProgramData.ALL_CANONICAL_PROGRAMS) {
            assertEquals(30, program.durationDays)
            assertEquals(30, program.days.size, "Program ${program.id} must have exactly 30 days")
        }
    }

    // 3. Day numbers are 1–30
    @Test
    fun testDayNumbersAreSequentialOneToThirty() {
        for (program in Canonical30DayProgramData.ALL_CANONICAL_PROGRAMS) {
            val dayNumbers = program.days.map { it.dayNumber }
            assertEquals((1..30).toList(), dayNumbers)
        }
    }

    // 4. Day 7/14/21/28 are rest/recovery days
    @Test
    fun testDays7_14_21_28AreRestDays() {
        val restDays = setOf(7, 14, 21, 28)
        for (program in Canonical30DayProgramData.ALL_CANONICAL_PROGRAMS) {
            for (day in program.days) {
                if (day.dayNumber in restDays) {
                    assertTrue(day.isRestDay, "Day ${day.dayNumber} must be designated as a rest day")
                    assertEquals(ProgramWorkoutType.REST.name, day.workoutType)
                    assertTrue(day.exercises.isEmpty(), "Rest day ${day.dayNumber} must not have resistance training exercises")
                    assertTrue(day.coolDown.isNotEmpty(), "Rest day ${day.dayNumber} must include cool-down/recovery routines")
                } else {
                    assertFalse(day.isRestDay, "Day ${day.dayNumber} is a training day")
                    assertEquals(ProgramWorkoutType.TRAINING.name, day.workoutType)
                    assertTrue(day.exercises.isNotEmpty(), "Training day ${day.dayNumber} must have exercises")
                }
            }
        }
    }

    // 5. Day 1 is Chest + Triceps
    @Test
    fun testDay1IsChestAndTriceps() {
        for (program in Canonical30DayProgramData.ALL_CANONICAL_PROGRAMS) {
            val day1 = program.days.first { it.dayNumber == 1 }
            assertTrue(day1.title.contains("Chest + Triceps"))
            assertTrue(day1.focus.contains("Chest"))
            assertTrue(day1.focus.contains("Triceps"))
        }
    }

    // 6. Day 2 is Back + Biceps
    @Test
    fun testDay2IsBackAndBiceps() {
        for (program in Canonical30DayProgramData.ALL_CANONICAL_PROGRAMS) {
            val day2 = program.days.first { it.dayNumber == 2 }
            assertTrue(day2.title.contains("Back + Biceps"))
            assertTrue(day2.focus.contains("Back"))
            assertTrue(day2.focus.contains("Biceps"))
        }
    }

    // 7. Day 3 is Legs
    @Test
    fun testDay3IsLegs() {
        for (program in Canonical30DayProgramData.ALL_CANONICAL_PROGRAMS) {
            val day3 = program.days.first { it.dayNumber == 3 }
            assertTrue(day3.title.contains("Legs"))
            assertTrue(day3.focus.contains("Quadriceps") || day3.focus.contains("Hamstrings"))
        }
    }

    // 8. Day 4 is Shoulders
    @Test
    fun testDay4IsShoulders() {
        for (program in Canonical30DayProgramData.ALL_CANONICAL_PROGRAMS) {
            val day4 = program.days.first { it.dayNumber == 4 }
            assertTrue(day4.title.contains("Shoulders"))
            assertTrue(day4.focus.contains("Shoulders"))
        }
    }

    // 9. Day 5 is Arms
    @Test
    fun testDay5IsArms() {
        for (program in Canonical30DayProgramData.ALL_CANONICAL_PROGRAMS) {
            val day5 = program.days.first { it.dayNumber == 5 }
            assertTrue(day5.title.contains("Arms"))
            assertTrue(day5.focus.contains("Biceps") || day5.focus.contains("Triceps"))
        }
    }

    // 10. Day 6 is Core
    @Test
    fun testDay6IsCore() {
        for (program in Canonical30DayProgramData.ALL_CANONICAL_PROGRAMS) {
            val day6 = program.days.first { it.dayNumber == 6 }
            assertTrue(day6.title.contains("Core"))
            assertTrue(day6.focus.contains("Abs"))
        }
    }

    // 11. Day 15 uses Rotation B
    @Test
    fun testDay15UsesRotationB() {
        for (program in Canonical30DayProgramData.ALL_CANONICAL_PROGRAMS) {
            val day15 = program.days.first { it.dayNumber == 15 }
            assertTrue(day15.title.contains("Rotation B"))
            assertTrue(day15.title.contains("Chest + Triceps"))
        }
    }

    // 12. Day 22 uses Rotation C
    @Test
    fun testDay22UsesRotationC() {
        for (program in Canonical30DayProgramData.ALL_CANONICAL_PROGRAMS) {
            val day22 = program.days.first { it.dayNumber == 22 }
            assertTrue(day22.title.contains("Rotation C"))
            assertTrue(day22.title.contains("Chest + Triceps"))
        }
    }

    // 13. Day 29 uses Rotation D
    @Test
    fun testDay29UsesRotationD() {
        for (program in Canonical30DayProgramData.ALL_CANONICAL_PROGRAMS) {
            val day29 = program.days.first { it.dayNumber == 29 }
            assertTrue(day29.title.contains("Rotation D"))
            assertTrue(day29.title.contains("Chest + Triceps"))
            val day30 = program.days.first { it.dayNumber == 30 }
            assertTrue(day30.title.contains("Rotation D"))
            assertTrue(day30.title.contains("Back + Biceps"))
        }
    }

    // 14. Experience levels are mapped correctly
    @Test
    fun testExperienceLevelsMappedCorrectly() {
        assertEquals("thirty_day_no_experience", Canonical30DayProgramData.getProgramForLevel(ExperienceLevel.NO_EXPERIENCE).id)
        assertEquals("thirty_day_beginner", Canonical30DayProgramData.getProgramForLevel(ExperienceLevel.BEGINNER).id)
        assertEquals("thirty_day_advanced", Canonical30DayProgramData.getProgramForLevel(ExperienceLevel.ADVANCED).id)
        assertEquals("thirty_day_expert", Canonical30DayProgramData.getProgramForLevel(ExperienceLevel.EXPERT).id)
    }

    // 15. Sets/reps match the source data for Day 1
    @Test
    fun testDay1SetsAndRepsMatchSourceData() {
        // NO_EXPERIENCE
        val noExpDay1 = Canonical30DayProgramData.PROGRAM_NO_EXPERIENCE.days.first { it.dayNumber == 1 }
        assertEquals(5, noExpDay1.exercises.size)
        assertEquals(listOf("machine_chest_press", "incline_machine_press", "pec_deck", "rope_triceps_pushdown", "overhead_cable_extension"), noExpDay1.exercises.map { it.exerciseId })
        noExpDay1.exercises.forEach {
            assertEquals(2, it.sets)
            assertEquals("12-15", it.reps)
        }

        // BEGINNER
        val begDay1 = Canonical30DayProgramData.PROGRAM_BEGINNER.days.first { it.dayNumber == 1 }
        assertEquals(5, begDay1.exercises.size)
        assertEquals(listOf("dumbbell_bench_press", "incline_dumbbell_press", "cable_fly", "rope_pushdown", "overhead_dumbbell_extension"), begDay1.exercises.map { it.exerciseId })
        assertEquals(3, begDay1.exercises[0].sets)
        assertEquals("10-12", begDay1.exercises[0].reps)
        assertEquals("12-15", begDay1.exercises[2].reps) // Cable fly 12-15

        // ADVANCED
        val advDay1 = Canonical30DayProgramData.PROGRAM_ADVANCED.days.first { it.dayNumber == 1 }
        assertEquals(6, advDay1.exercises.size)
        assertEquals(listOf("barbell_bench_press", "incline_dumbbell_press", "chest_dips", "cable_fly", "close_grip_bench_press", "rope_pushdown"), advDay1.exercises.map { it.exerciseId })
        assertEquals(4, advDay1.exercises[0].sets)
        assertEquals("6-10", advDay1.exercises[0].reps)
        assertEquals("8-12", advDay1.exercises[1].reps)
        assertEquals("8-12", advDay1.exercises[2].reps)
        assertEquals("12-15", advDay1.exercises[3].reps)
        assertEquals("8-10", advDay1.exercises[4].reps)
        assertEquals("10-15", advDay1.exercises[5].reps)

        // EXPERT
        val expDay1 = Canonical30DayProgramData.PROGRAM_EXPERT.days.first { it.dayNumber == 1 }
        assertEquals(7, expDay1.exercises.size)
        assertEquals(listOf("barbell_bench_press", "incline_barbell_press", "decline_dumbbell_press", "weighted_dips", "skull_crushers", "overhead_cable_extension", "rope_pushdown"), expDay1.exercises.map { it.exerciseId })
        assertEquals(4, expDay1.exercises[0].sets)
        assertEquals("5-8", expDay1.exercises[0].reps)
        assertEquals(4, expDay1.exercises[1].sets)
        assertEquals("6-10", expDay1.exercises[1].reps)
        assertEquals("8-12", expDay1.exercises[2].reps)
        assertEquals("6-10", expDay1.exercises[3].reps)
        assertEquals("8-12", expDay1.exercises[4].reps)
        assertEquals("10-12", expDay1.exercises[5].reps)
        assertEquals("12-15", expDay1.exercises[6].reps)
    }

    // 16. No duplicate exercise IDs are created in SampleExerciseData
    @Test
    fun testNoDuplicateExerciseIds() {
        val allIds = SampleExerciseData.INITIAL_EXERCISES.map { it.id }
        val uniqueIds = allIds.toSet()
        assertEquals(allIds.size, uniqueIds.size, "Exercise IDs in SampleExerciseData must be strictly unique")
    }

    // 17. Exercise references are nonblank
    @Test
    fun testExerciseReferencesAreNonBlank() {
        for (program in Canonical30DayProgramData.ALL_CANONICAL_PROGRAMS) {
            for (day in program.days) {
                for (ref in day.exercises) {
                    assertTrue(ref.exerciseId.isNotBlank(), "exerciseId must not be blank on Day ${day.dayNumber}")
                    assertTrue(ref.sets > 0, "sets must be > 0 on Day ${day.dayNumber}")
                    assertTrue(ref.reps.isNotBlank(), "reps must not be blank on Day ${day.dayNumber}")
                    assertTrue(ref.restSeconds >= 0, "restSeconds must be >= 0 on Day ${day.dayNumber}")
                }
            }
        }
    }

    // 18. Program validation passes
    @Test
    fun testProgramValidationPasses() {
        for (program in Canonical30DayProgramData.ALL_CANONICAL_PROGRAMS) {
            assertTrue(program.id.isNotBlank())
            assertTrue(program.name.isNotBlank())
            assertEquals(ProgramType.THIRTY_DAY.name, program.type)
            assertEquals(30, program.durationDays)
            assertEquals(6, program.trainingDaysPerWeek)
            assertTrue(program.isActive)
            assertEquals(30, program.days.size)
        }
    }

    // 19. Warm-up and cool-down mapping validation
    @Test
    fun testWarmupAndCooldownMappedAccurately() {
        val program = Canonical30DayProgramData.PROGRAM_BEGINNER
        val day1 = program.days.first { it.dayNumber == 1 }
        assertEquals(Canonical30DayProgramData.WARMUP_CHEST_TRICEPS, day1.warmUp)
        assertEquals(Canonical30DayProgramData.COOLDOWN_UPPER_BODY, day1.coolDown)

        val day3 = program.days.first { it.dayNumber == 3 } // Legs
        assertEquals(Canonical30DayProgramData.WARMUP_LEGS, day3.warmUp)
        assertEquals(Canonical30DayProgramData.COOLDOWN_LOWER_BODY, day3.coolDown)

        val day6 = program.days.first { it.dayNumber == 6 } // Core
        assertEquals(Canonical30DayProgramData.WARMUP_CORE, day6.warmUp)
        assertEquals(Canonical30DayProgramData.COOLDOWN_CORE, day6.coolDown)

        val day7 = program.days.first { it.dayNumber == 7 } // Rest
        assertTrue(day7.warmUp.isEmpty())
        assertEquals(Canonical30DayProgramData.COOLDOWN_GENERAL, day7.coolDown)
    }

    // 20. Home Screen WeeklyWorkoutRepository integration
    @Test
    fun testHomeScreenWorkoutResolutionIntegration() {
        // Wednesday (dayIndex 2) should resolve to Day 3 (Legs)
        val wednesdayWorkout = WeeklyWorkoutRepository.getWorkoutForDay(2, ExperienceLevel.BEGINNER)
        assertEquals("Wednesday", wednesdayWorkout.dayFullName)
        assertTrue(wednesdayWorkout.title.contains("Legs"))
        assertEquals("legs", wednesdayWorkout.bodyPartId)
        assertFalse(wednesdayWorkout.isRestDay)
        assertTrue(wednesdayWorkout.exercises.isNotEmpty())

        // Sunday (dayIndex 6) should resolve to Day 7 (Rest / Recovery)
        val sundayWorkout = WeeklyWorkoutRepository.getWorkoutForDay(6, ExperienceLevel.BEGINNER)
        assertEquals("Sunday", sundayWorkout.dayFullName)
        assertTrue(sundayWorkout.isRestDay)
        assertEquals("Rest / Recovery", sundayWorkout.title)
        assertTrue(sundayWorkout.exercises.isEmpty())
        assertTrue(sundayWorkout.cooldown.isNotEmpty())

        // Level change dynamically alters prescriptions
        val noExpMonday = WeeklyWorkoutRepository.getWorkoutForDay(0, ExperienceLevel.NO_EXPERIENCE)
        val expertMonday = WeeklyWorkoutRepository.getWorkoutForDay(0, ExperienceLevel.EXPERT)

        assertEquals("No Experience", noExpMonday.difficulty)
        assertEquals("Expert", expertMonday.difficulty)
        assertEquals("Machine Chest Press", noExpMonday.exercises.first().name)
        assertEquals("Barbell Bench Press", expertMonday.exercises.first().name)
    }

    // 21. Hypertrophy Program contains 30 days, correct split, and Day 30 performance check
    @Test
    fun testHypertrophyProgramStructure() {
        val program = CanonicalHypertrophyProgramData.PROGRAM_HYPERTROPHY
        assertEquals(30, program.durationDays)
        assertEquals(30, program.days.size)
        assertEquals(ProgramType.HYPERTROPHY.name, program.type)

        val restDays = setOf(7, 14, 21, 28)
        for (day in program.days) {
            if (day.dayNumber in restDays) {
                assertTrue(day.isRestDay, "Hypertrophy Day ${day.dayNumber} must be a rest day")
                assertTrue(day.exercises.isEmpty(), "Hypertrophy rest day must not have resistance exercises")
            } else {
                assertFalse(day.isRestDay, "Hypertrophy Day ${day.dayNumber} must be a training day")
                assertTrue(day.exercises.isNotEmpty(), "Hypertrophy training day must have exercises")
                for (ex in day.exercises) {
                    assertNotNull(SampleExerciseData.getById(ex.exerciseId), "Exercise ${ex.exerciseId} must exist in registry")
                }
            }
        }

        val day30 = program.days.first { it.dayNumber == 30 }
        assertTrue(day30.notes.contains("Controlled rep-max check"))
    }

    // 22. Strength Program contains 30 days, compound lifts, and Day 30 test
    @Test
    fun testStrengthProgramStructure() {
        val program = CanonicalStrengthProgramData.PROGRAM_STRENGTH
        assertEquals(30, program.durationDays)
        assertEquals(30, program.days.size)
        assertEquals(ProgramType.STRENGTH.name, program.type)

        val restDays = setOf(7, 14, 21, 28)
        for (day in program.days) {
            if (day.dayNumber in restDays) {
                assertTrue(day.isRestDay, "Strength Day ${day.dayNumber} must be a rest day")
                assertTrue(day.exercises.isEmpty(), "Strength rest day must not have resistance exercises")
            } else {
                assertFalse(day.isRestDay, "Strength Day ${day.dayNumber} must be a training day")
                assertTrue(day.exercises.isNotEmpty(), "Strength training day must have exercises")
                for (ex in day.exercises) {
                    assertNotNull(SampleExerciseData.getById(ex.exerciseId), "Exercise ${ex.exerciseId} must exist in registry")
                }
            }
        }

        val day30 = program.days.first { it.dayNumber == 30 }
        assertTrue(day30.notes.contains("Strength Performance Test"))
    }

    // 23. Athlete Performance Program contains 30 days, multi-modal focus, and coaching notes
    @Test
    fun testAthletePerformanceProgramStructure() {
        val program = CanonicalAthletePerformanceProgramData.PROGRAM_ATHLETE_PERFORMANCE
        assertEquals(30, program.durationDays)
        assertEquals(30, program.days.size)
        assertEquals(ProgramType.ATHLETE_PERFORMANCE.name, program.type)

        val restDays = setOf(7, 14, 21, 28)
        for (day in program.days) {
            if (day.dayNumber in restDays) {
                assertTrue(day.isRestDay, "Athlete Perf Day ${day.dayNumber} must be a recovery day")
                assertTrue(day.exercises.isEmpty(), "Athlete Perf recovery day must not have resistance exercises")
            } else {
                assertFalse(day.isRestDay, "Athlete Perf Day ${day.dayNumber} must be a training day")
                assertTrue(day.exercises.isNotEmpty(), "Athlete Perf training day must have exercises")
                for (ex in day.exercises) {
                    assertNotNull(SampleExerciseData.getById(ex.exerciseId), "Exercise ${ex.exerciseId} must exist in registry")
                }
            }
        }

        // Day 11 Hang power clean note
        val day11 = program.days.first { it.dayNumber == 11 }
        assertTrue(day11.notes.contains("Hang power clean requires appropriate technical proficiency"))

        // Day 30 Performance test
        val day30 = program.days.first { it.dayNumber == 30 }
        assertTrue(day30.notes.contains("Athlete Performance Test"))
    }

    // 24. Centralized Exercise Registry completeness and stable ID resolution
    @Test
    fun testCentralizedExerciseRegistryIntegrity() {
        assertTrue(SampleExerciseData.ALL_EXERCISES.size >= 200, "Must have all authoritative exercises registered")
        val ids = SampleExerciseData.ALL_EXERCISES.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "All exercise IDs must be strictly unique")
        for (ex in SampleExerciseData.ALL_EXERCISES) {
            assertTrue(ex.id.isNotBlank())
            assertTrue(ex.name.isNotBlank())
            assertTrue(ex.slug.isNotBlank())
            assertTrue(ex.primaryMuscle.isNotBlank())
            assertTrue(ex.category.isNotBlank())
            assertTrue(ex.movementPattern.isNotBlank())
            assertTrue(ex.difficulty.isNotBlank())
            assertTrue(ex.isActive)
        }
    }
}
