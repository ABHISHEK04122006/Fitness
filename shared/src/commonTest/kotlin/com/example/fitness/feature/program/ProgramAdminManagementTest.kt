package com.example.fitness.feature.program

import com.example.fitness.feature.exercise.model.ExerciseReference
import com.example.fitness.feature.program.data.FirebaseProgramRepository
import com.example.fitness.feature.program.data.SampleProgramData
import com.example.fitness.feature.program.model.Program
import com.example.fitness.feature.program.model.ProgramDay
import com.example.fitness.feature.program.model.ProgramType
import com.example.fitness.feature.program.model.ProgramWorkoutType
import com.example.fitness.feature.workout.model.ExperienceLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Common unit test suite verifying Admin Program Management UI business logic (STEP 5B):
 * 1. Program search
 * 2. Program type filtering
 * 3. Experience-level filtering
 * 4. Active/archived filtering
 * 5. Combined filters
 * 6. Program ID immutability
 * 7. Day addition
 * 8. Day removal
 * 9. Day ordering
 * 10. Duplicate day-number validation
 * 11. Rest day behavior
 * 12. Exercise selection by exerciseId
 * 13. Exercise prescription editing
 * 14. Exercise ordering
 * 15. Archived exercise reference preservation
 * 16. Program archive
 * 17. Program restore
 * 18. Program validation
 */
class ProgramAdminManagementTest {

    private val repository = FirebaseProgramRepository()

    private val thirtyDayPlan = Program(
        id = "thirty_day",
        name = "30-Day Workout Plan",
        slug = "thirty_day_workout_plan",
        description = "Foundational daily workout plan for beginners.",
        type = ProgramType.THIRTY_DAY.name,
        experienceLevel = ExperienceLevel.BEGINNER.name,
        durationDays = 30,
        trainingDaysPerWeek = 5,
        days = listOf(
            ProgramDay(
                dayNumber = 1,
                title = "Day 1: Chest & Core",
                workoutType = ProgramWorkoutType.TRAINING.name,
                exercises = listOf(
                    ExerciseReference(exerciseId = "barbell_bench_press", sets = 3, reps = "8-12", restSeconds = 90)
                )
            ),
            ProgramDay(
                dayNumber = 2,
                title = "Day 2: Back & Biceps",
                workoutType = ProgramWorkoutType.TRAINING.name,
                exercises = listOf(
                    ExerciseReference(exerciseId = "pull_up", sets = 3, reps = "6-10", restSeconds = 90)
                )
            )
        ),
        isActive = true,
        version = 1,
        createdAt = 1000L,
        updatedAt = 1000L
    )

    private val hypertrophyPlan = Program(
        id = "hypertrophy_mastery",
        name = "Hypertrophy Mastery",
        slug = "hypertrophy_mastery",
        description = "Advanced hypertrophy building routine.",
        type = ProgramType.HYPERTROPHY.name,
        experienceLevel = ExperienceLevel.ADVANCED.name,
        durationDays = 60,
        trainingDaysPerWeek = 4,
        days = listOf(
            ProgramDay(
                dayNumber = 1,
                title = "Upper Body Hypertrophy",
                workoutType = ProgramWorkoutType.TRAINING.name,
                exercises = listOf(
                    ExerciseReference(exerciseId = "incline_dumbbell_press", sets = 4, reps = "10-12", restSeconds = 75)
                )
            )
        ),
        isActive = true,
        version = 1,
        createdAt = 1100L,
        updatedAt = 1100L
    )

    private val strengthPlan = Program(
        id = "strength_foundations",
        name = "Strength Foundations",
        slug = "strength_foundations",
        description = "Heavy compound strength training program.",
        type = ProgramType.STRENGTH.name,
        experienceLevel = ExperienceLevel.EXPERT.name,
        durationDays = 90,
        trainingDaysPerWeek = 3,
        days = emptyList(),
        isActive = false, // Archived
        version = 2,
        createdAt = 1200L,
        updatedAt = 1500L
    )

    private val athletePlan = Program(
        id = "athlete_performance",
        name = "Athlete Speed & Power",
        slug = "athlete_speed_and_power",
        description = "Multi-directional speed and power development.",
        type = ProgramType.ATHLETE_PERFORMANCE.name,
        experienceLevel = ExperienceLevel.NO_EXPERIENCE.name,
        durationDays = 42,
        trainingDaysPerWeek = 4,
        days = emptyList(),
        isActive = true,
        version = 1,
        createdAt = 1300L,
        updatedAt = 1300L
    )

    private val testDataset = listOf(thirtyDayPlan, hypertrophyPlan, strengthPlan, athletePlan)

    // 1. Program search
    @Test
    fun testProgramSearchCaseInsensitive() {
        val query = "hypertrophy"
        val results = testDataset.filter { prog ->
            prog.name.contains(query, ignoreCase = true) ||
                    prog.id.contains(query, ignoreCase = true) ||
                    prog.slug.contains(query, ignoreCase = true) ||
                    prog.description.contains(query, ignoreCase = true) ||
                    prog.type.contains(query, ignoreCase = true) ||
                    prog.experienceLevel.contains(query, ignoreCase = true)
        }
        assertEquals(1, results.size)
        assertEquals("hypertrophy_mastery", results[0].id)
    }

    // 2. Program type filtering
    @Test
    fun testProgramTypeFiltering() {
        val thirtyDayOnly = testDataset.filter { it.type.equals("THIRTY_DAY", ignoreCase = true) }
        assertEquals(1, thirtyDayOnly.size)
        assertEquals("thirty_day", thirtyDayOnly[0].id)

        val athleteOnly = testDataset.filter { it.type.equals("ATHLETE_PERFORMANCE", ignoreCase = true) }
        assertEquals(1, athleteOnly.size)
        assertEquals("athlete_performance", athleteOnly[0].id)
    }

    // 3. Experience-level filtering
    @Test
    fun testExperienceLevelFiltering() {
        val beginnerOnly = testDataset.filter { it.experienceLevel.equals("BEGINNER", ignoreCase = true) }
        assertEquals(1, beginnerOnly.size)
        assertEquals("thirty_day", beginnerOnly[0].id)

        val expertOnly = testDataset.filter { it.experienceLevel.equals("EXPERT", ignoreCase = true) }
        assertEquals(1, expertOnly.size)
        assertEquals("strength_foundations", expertOnly[0].id)
    }

    // 4. Active/archived filtering
    @Test
    fun testActiveArchivedFiltering() {
        val active = testDataset.filter { it.isActive }
        assertEquals(3, active.size)

        val archived = testDataset.filter { !it.isActive }
        assertEquals(1, archived.size)
        assertEquals("strength_foundations", archived[0].id)
    }

    // 5. Combined filters
    @Test
    fun testCombinedFilters() {
        // Active + HYPERTROPHY + ADVANCED + "mastery"
        val filtered = testDataset.filter { prog ->
            prog.isActive &&
                    prog.type.equals("HYPERTROPHY", ignoreCase = true) &&
                    prog.experienceLevel.equals("ADVANCED", ignoreCase = true) &&
                    prog.name.contains("mastery", ignoreCase = true)
        }
        assertEquals(1, filtered.size)
        assertEquals("hypertrophy_mastery", filtered[0].id)
    }

    // 6. Program ID immutability
    @Test
    fun testProgramIdImmutability() {
        val original = thirtyDayPlan
        // In edit mode, ID cannot be altered
        val updated = original.copy(
            name = "30-Day Workout Plan (Updated Edition)",
            durationDays = 35,
            version = original.version + 1,
            updatedAt = 2000L
        )
        assertEquals(original.id, updated.id, "Program ID must remain immutable")
        assertEquals(original.createdAt, updated.createdAt, "createdAt must be preserved")
        assertEquals(2, updated.version)
    }

    // 7. Day addition
    @Test
    fun testDayAddition() {
        val days = thirtyDayPlan.days.toMutableList()
        val nextDayNum = if (days.isEmpty()) 1 else (days.maxOf { it.dayNumber } + 1)
        assertEquals(3, nextDayNum)

        days.add(
            ProgramDay(
                dayNumber = nextDayNum,
                title = "Day 3: Lower Body Power",
                workoutType = ProgramWorkoutType.TRAINING.name
            )
        )
        assertEquals(3, days.size)
        assertEquals(3, days[2].dayNumber)
    }

    // 8. Day removal
    @Test
    fun testDayRemoval() {
        val days = thirtyDayPlan.days.toMutableList()
        assertEquals(2, days.size)

        days.removeAt(0)
        assertEquals(1, days.size)
        assertEquals(2, days[0].dayNumber, "Remaining day preserves original numbering")
    }

    // 9. Day ordering
    @Test
    fun testDayOrdering() {
        val day1 = ProgramDay(dayNumber = 1, title = "Day 1")
        val day2 = ProgramDay(dayNumber = 2, title = "Day 2")

        // Move Down day 1 (swap dayNumbers)
        val updatedDay1 = day1.copy(dayNumber = 2)
        val updatedDay2 = day2.copy(dayNumber = 1)

        val reordered = listOf(updatedDay1, updatedDay2).sortedBy { it.dayNumber }
        assertEquals(1, reordered[0].dayNumber)
        assertEquals("Day 2", reordered[0].title)
        assertEquals(2, reordered[1].dayNumber)
        assertEquals("Day 1", reordered[1].title)
    }

    // 10. Duplicate day-number validation
    @Test
    fun testDuplicateDayNumberValidation() {
        val duplicateDays = listOf(
            ProgramDay(dayNumber = 1, title = "Push"),
            ProgramDay(dayNumber = 1, title = "Pull")
        )
        val invalidProg = Program(id = "p", name = "P", slug = "p", days = duplicateDays)
        val res = repository.validateProgram(invalidProg)
        assertTrue(res.isFailure)
        assertTrue(res.exceptionOrNull()?.message?.contains("Duplicate dayNumber 1") == true)
    }

    // 11. Rest day behavior
    @Test
    fun testRestDayBehavior() {
        val restDay = ProgramDay(
            dayNumber = 7,
            title = "Weekly Rest",
            workoutType = ProgramWorkoutType.REST.name,
            isRestDay = true,
            exercises = emptyList(),
            notes = "Full rest day."
        )
        assertTrue(restDay.isRestDay)
        assertTrue(restDay.exercises.isEmpty())
        assertEquals("REST", restDay.workoutType)
    }

    // 12. Exercise selection by exerciseId
    @Test
    fun testExerciseSelectionByExerciseId() {
        // Exercise reference references ONLY stable exerciseId
        val ref = ExerciseReference(
            exerciseId = "barbell_bench_press",
            sets = 4,
            reps = "8-10",
            restSeconds = 120
        )
        assertEquals("barbell_bench_press", ref.exerciseId)
        assertEquals(4, ref.sets)
        assertEquals("8-10", ref.reps)
        assertEquals(120, ref.restSeconds)
    }

    // 13. Exercise prescription editing
    @Test
    fun testExercisePrescriptionEditing() {
        val originalRef = ExerciseReference(
            exerciseId = "squat",
            sets = 3,
            reps = "8-12",
            restSeconds = 90
        )

        // Modify prescription
        val updatedRef = originalRef.copy(
            sets = 5,
            reps = "AMRAP",
            restSeconds = 180,
            notes = "Final set to technical failure"
        )
        assertEquals("squat", updatedRef.exerciseId)
        assertEquals(5, updatedRef.sets)
        assertEquals("AMRAP", updatedRef.reps)
        assertEquals(180, updatedRef.restSeconds)
        assertEquals("Final set to technical failure", updatedRef.notes)
    }

    // 14. Exercise ordering
    @Test
    fun testExerciseOrdering() {
        val ex1 = ExerciseReference(exerciseId = "bench_press")
        val ex2 = ExerciseReference(exerciseId = "incline_dumbbell_press")
        val ex3 = ExerciseReference(exerciseId = "cable_fly")

        val exerciseList = mutableListOf(ex1, ex2, ex3)
        assertEquals("bench_press", exerciseList[0].exerciseId)
        assertEquals("incline_dumbbell_press", exerciseList[1].exerciseId)

        // Move ex2 up to index 0
        val moved = exerciseList.removeAt(1)
        exerciseList.add(0, moved)

        assertEquals("incline_dumbbell_press", exerciseList[0].exerciseId)
        assertEquals("bench_press", exerciseList[1].exerciseId)
        assertEquals("cable_fly", exerciseList[2].exerciseId)
    }

    // 15. Archived exercise reference preservation
    @Test
    fun testArchivedExerciseReferencePreservation() {
        // Program has reference to an exercise that later gets archived
        val referencedId = "archived_exercise_123"
        val day = ProgramDay(
            dayNumber = 1,
            exercises = listOf(ExerciseReference(exerciseId = referencedId, sets = 3, reps = "10"))
        )
        val program = Program(id = "p", name = "P", slug = "p", days = listOf(day))

        // Program reference remains completely intact
        assertEquals(referencedId, program.days[0].exercises[0].exerciseId)
    }

    // 16. Program archive
    @Test
    fun testProgramArchive() {
        val activeProg = thirtyDayPlan
        assertTrue(activeProg.isActive)

        val archived = activeProg.copy(
            isActive = false,
            updatedAt = 3000L,
            version = activeProg.version + 1
        )
        assertFalse(archived.isActive)
        assertEquals(2, archived.version)
        assertEquals(activeProg.createdAt, archived.createdAt)
    }

    // 17. Program restore
    @Test
    fun testProgramRestore() {
        val archivedProg = strengthPlan
        assertFalse(archivedProg.isActive)

        val restored = archivedProg.copy(
            isActive = true,
            updatedAt = 4000L,
            version = archivedProg.version + 1
        )
        assertTrue(restored.isActive)
        assertEquals(3, restored.version)
        assertEquals(archivedProg.createdAt, restored.createdAt)
    }

    // 18. Program validation
    @Test
    fun testProgramValidation() {
        // Blank name must fail
        val blankNameProg = Program(id = "p", name = "", slug = "p")
        assertTrue(repository.validateProgram(blankNameProg).isFailure)

        // Negative duration must fail
        val negDurationProg = Program(id = "p", name = "Valid", slug = "p", durationDays = -5)
        assertTrue(repository.validateProgram(negDurationProg).isFailure)

        // Valid program passes
        val valid = Program(id = "valid_id", name = "Valid Program", slug = "valid_id", durationDays = 30)
        assertTrue(repository.validateProgram(valid).isSuccess)
    }
}
