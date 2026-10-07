package com.example.fitness.feature.workout

import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.progress.data.WorkoutProgressService
import com.example.fitness.feature.progress.model.ExercisePerformanceRecord
import com.example.fitness.feature.progress.model.WorkoutSessionRecord
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import com.example.fitness.feature.workout.model.ExperienceLevel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * WORKOUT ACTUAL COMPLETION COUNT TEST SUITE
 *
 * Verifies that workout and exercise completion counting strictly reflects actual completed user sets/exercises:
 * 1. Test 1: 5-exercise workout with 1 exercise completed -> partial, 1/5, isSessionFullyCompleted = false, no checkmark.
 * 2. Test 2: 5-exercise workout with all 5 exercises completed -> completed, 5/5, isSessionFullyCompleted = true, checkmark.
 * 3. Test 3: Opening/starting a workout and quitting with 0 exercises completed -> 0 completed, no checkmark.
 * 4. Test 4: Navigating to exercise / viewing detail / loading screen -> 0 completed, no checkmark.
 * 5. Test 5: Multiple workouts over days -> only fully completed days advance program completion count & get checkmarks.
 * 6. Test 6: Partial workout persistence & restore -> partial status preserved, no false checkmarks upon restore.
 */
class WorkoutActualCompletionCountTest {

    private val testUserId = "user_actual_test_123"

    @BeforeTest
    fun setUp() {
        WorkoutProgressService.clear()
        UserSession.clear()
        UserSession.currentUserId = testUserId
        UserSession.setActiveProgram("30-day")
    }

    private fun createSampleExerciseRecords(
        totalExercises: Int = 5,
        completedExercisesCount: Int = 1,
        prescribedSets: Int = 3
    ): List<ExercisePerformanceRecord> {
        return (1..totalExercises).map { i ->
            val isDone = i <= completedExercisesCount
            val actualSets = if (isDone) prescribedSets else 0
            ExercisePerformanceRecord(
                exerciseId = "ex_$i",
                exerciseName = "Exercise $i",
                prescribedSets = prescribedSets,
                prescribedReps = "10",
                completedSets = actualSets,
                completedReps = if (isDone) List(actualSets) { 10 } else emptyList(),
                weightUsed = null,
                restSeconds = 60,
                completed = isDone,
                notes = null
            )
        }
    }

    // -----------------------------------------------------------------------------------------
    // Test 1: 5-exercise workout with 1 exercise completed
    // -----------------------------------------------------------------------------------------
    @Test
    fun testFiveExerciseWorkoutWithOneExerciseCompleted() {
        val exercises = createSampleExerciseRecords(totalExercises = 5, completedExercisesCount = 1)
        val session = WorkoutProgressService.saveWorkoutSession(
            workoutId = "day_1",
            dayNumber = 1,
            workoutTitle = "Day 1 - Chest & Triceps",
            targetMuscle = listOf("Chest", "Triceps"),
            experienceLevel = "beginner",
            durationMinutes = 12,
            totalExercises = 5,
            completedExercises = 1,
            totalSets = 15,
            completedSets = 3,
            warmUpCompleted = true,
            mainWorkoutCompleted = false,
            coolDownCompleted = false,
            programId = "30-day",
            programName = "30-Day Workout Plan",
            userId = testUserId,
            exercises = exercises,
            sessionId = "session_test_1"
        )

        assertEquals(1, session.completedExercises, "Completed exercise count must be exactly 1")
        assertEquals(5, session.totalExercises, "Prescribed exercise count must be 5")
        assertEquals("partial", session.completionStatus, "Completion status must be 'partial'")
        assertFalse(WorkoutProgressService.isSessionFullyCompleted(session), "isSessionFullyCompleted must be false for partial workout")
        assertFalse(UserSession.isProgramDayCompleted("30-day", 1), "Program Day 1 must NOT receive a completion checkmark")
        assertFalse(UserSession.isDayCompleted(1), "Day 1 must NOT be marked completed in UserSession")
        assertEquals(0, UserSession.getProgramCompletedCount("30-day"), "Program completed count must remain 0")
    }

    // -----------------------------------------------------------------------------------------
    // Test 2: 5-exercise workout with all 5 exercises completed
    // -----------------------------------------------------------------------------------------
    @Test
    fun testFiveExerciseWorkoutWithAllFiveExercisesCompleted() {
        val exercises = createSampleExerciseRecords(totalExercises = 5, completedExercisesCount = 5)
        val session = WorkoutProgressService.saveWorkoutSession(
            workoutId = "day_1",
            dayNumber = 1,
            workoutTitle = "Day 1 - Chest & Triceps",
            targetMuscle = listOf("Chest", "Triceps"),
            experienceLevel = "beginner",
            durationMinutes = 45,
            totalExercises = 5,
            completedExercises = 5,
            totalSets = 15,
            completedSets = 15,
            warmUpCompleted = true,
            mainWorkoutCompleted = true,
            coolDownCompleted = true,
            programId = "30-day",
            programName = "30-Day Workout Plan",
            userId = testUserId,
            exercises = exercises,
            sessionId = "session_test_2"
        )

        assertEquals(5, session.completedExercises, "Completed exercise count must be 5")
        assertEquals(5, session.totalExercises, "Prescribed exercise count must be 5")
        assertEquals("completed", session.completionStatus, "Completion status must be 'completed'")
        assertTrue(WorkoutProgressService.isSessionFullyCompleted(session), "isSessionFullyCompleted must be true for full workout")
        assertTrue(UserSession.isProgramDayCompleted("30-day", 1), "Program Day 1 MUST receive a completion checkmark")
        assertTrue(UserSession.isDayCompleted(1), "Day 1 MUST be marked completed in UserSession")
        assertEquals(1, UserSession.getProgramCompletedCount("30-day"), "Program completed count must be 1")
    }

    // -----------------------------------------------------------------------------------------
    // Test 3: Opening/starting a workout and quitting with 0 exercises completed
    // -----------------------------------------------------------------------------------------
    @Test
    fun testOpenWorkoutAndQuitWithZeroExercisesCompleted() {
        val exercises = createSampleExerciseRecords(totalExercises = 5, completedExercisesCount = 0)
        val session = WorkoutProgressService.saveWorkoutSession(
            workoutId = "day_1",
            dayNumber = 1,
            workoutTitle = "Day 1 - Chest & Triceps",
            targetMuscle = listOf("Chest", "Triceps"),
            experienceLevel = "beginner",
            durationMinutes = 1,
            totalExercises = 5,
            completedExercises = 0,
            totalSets = 15,
            completedSets = 0,
            warmUpCompleted = false,
            mainWorkoutCompleted = false,
            coolDownCompleted = false,
            programId = "30-day",
            programName = "30-Day Workout Plan",
            userId = testUserId,
            exercises = exercises,
            sessionId = "session_test_3"
        )

        assertEquals(0, session.completedExercises, "Completed exercise count must be 0")
        assertEquals("partial", session.completionStatus, "Completion status must be 'partial'")
        assertFalse(WorkoutProgressService.isSessionFullyCompleted(session), "isSessionFullyCompleted must be false")
        assertFalse(UserSession.isProgramDayCompleted("30-day", 1), "Program Day 1 must NOT receive a completion checkmark")
        assertFalse(UserSession.isDayCompleted(1), "Day 1 must NOT be marked completed")
        assertEquals(0, UserSession.getProgramCompletedCount("30-day"), "Program completed count must be 0")
    }

    // -----------------------------------------------------------------------------------------
    // Test 4: Navigating to exercise, viewing detail, or loading screens does NOT complete anything
    // -----------------------------------------------------------------------------------------
    @Test
    fun testNavigatingDetailViewingDoesNotIncrementCompletion() {
        // Resolve canonical plan and day representations
        val plan = CanonicalWorkoutResolver.resolveDailyWorkoutPlan("30-day", "day_1", ExperienceLevel.BEGINNER)
        val day = CanonicalWorkoutResolver.resolveProgramDay("30-day", "day_1", ExperienceLevel.BEGINNER)
        val thirtyDay = CanonicalWorkoutResolver.resolveThirtyDayWorkout("30-day", "day_1", ExperienceLevel.BEGINNER)

        assertTrue(plan.exercises.isNotEmpty(), "Plan exercises must be resolved")
        assertTrue(day.exercises.isNotEmpty(), "Day exercises must be resolved")
        assertTrue(thirtyDay.exercises.isNotEmpty(), "ThirtyDay exercises must be resolved")

        // Inspecting/loading must not mutate UserSession progress or checkmarks
        assertFalse(UserSession.isProgramDayCompleted("30-day", 1), "Opening/viewing workout must NOT mark day completed")
        assertFalse(UserSession.isDayCompleted(1), "Opening/viewing workout must NOT mark day completed")
        assertEquals(0, UserSession.completedDays.size, "Completed days list must remain empty")
        assertEquals(0, UserSession.totalSetsCompleted, "Total sets completed must remain 0")
        assertEquals(0, UserSession.totalRepsCompleted, "Total reps completed must remain 0")
        assertEquals(0, UserSession.getProgramCompletedCount("30-day"), "Program completed count must remain 0")
    }

    // -----------------------------------------------------------------------------------------
    // Test 5: Multiple workouts over days - only fully completed days advance program
    // -----------------------------------------------------------------------------------------
    @Test
    fun testMultipleWorkoutsOverDaysOnlyFullyCompletedAdvanceProgram() {
        // Day 1: Fully completed (5/5)
        WorkoutProgressService.saveWorkoutSession(
            workoutId = "day_1",
            dayNumber = 1,
            workoutTitle = "Day 1",
            targetMuscle = listOf("Chest"),
            experienceLevel = "beginner",
            durationMinutes = 40,
            totalExercises = 5,
            completedExercises = 5,
            totalSets = 15,
            completedSets = 15,
            warmUpCompleted = true,
            mainWorkoutCompleted = true,
            coolDownCompleted = true,
            programId = "30-day",
            programName = "30-Day Workout Plan",
            userId = testUserId,
            exercises = createSampleExerciseRecords(5, 5),
            sessionId = "session_d1"
        )

        // Day 2: Partial (1/5)
        WorkoutProgressService.saveWorkoutSession(
            workoutId = "day_2",
            dayNumber = 2,
            workoutTitle = "Day 2",
            targetMuscle = listOf("Back"),
            experienceLevel = "beginner",
            durationMinutes = 10,
            totalExercises = 5,
            completedExercises = 1,
            totalSets = 15,
            completedSets = 3,
            warmUpCompleted = true,
            mainWorkoutCompleted = false,
            coolDownCompleted = false,
            programId = "30-day",
            programName = "30-Day Workout Plan",
            userId = testUserId,
            exercises = createSampleExerciseRecords(5, 1),
            sessionId = "session_d2"
        )

        // Day 3: Partial (2/5)
        WorkoutProgressService.saveWorkoutSession(
            workoutId = "day_3",
            dayNumber = 3,
            workoutTitle = "Day 3",
            targetMuscle = listOf("Legs"),
            experienceLevel = "beginner",
            durationMinutes = 15,
            totalExercises = 5,
            completedExercises = 2,
            totalSets = 15,
            completedSets = 6,
            warmUpCompleted = true,
            mainWorkoutCompleted = false,
            coolDownCompleted = false,
            programId = "30-day",
            programName = "30-Day Workout Plan",
            userId = testUserId,
            exercises = createSampleExerciseRecords(5, 2),
            sessionId = "session_d3"
        )

        // Day 4: Fully completed (5/5)
        WorkoutProgressService.saveWorkoutSession(
            workoutId = "day_4",
            dayNumber = 4,
            workoutTitle = "Day 4",
            targetMuscle = listOf("Shoulders"),
            experienceLevel = "beginner",
            durationMinutes = 42,
            totalExercises = 5,
            completedExercises = 5,
            totalSets = 15,
            completedSets = 15,
            warmUpCompleted = true,
            mainWorkoutCompleted = true,
            coolDownCompleted = true,
            programId = "30-day",
            programName = "30-Day Workout Plan",
            userId = testUserId,
            exercises = createSampleExerciseRecords(5, 5),
            sessionId = "session_d4"
        )

        // Assert individual day completion status
        assertTrue(UserSession.isProgramDayCompleted("30-day", 1), "Day 1 was fully completed and must have checkmark")
        assertFalse(UserSession.isProgramDayCompleted("30-day", 2), "Day 2 was partial and must NOT have checkmark")
        assertFalse(UserSession.isProgramDayCompleted("30-day", 3), "Day 3 was partial and must NOT have checkmark")
        assertTrue(UserSession.isProgramDayCompleted("30-day", 4), "Day 4 was fully completed and must have checkmark")

        // Assert total program completion count
        assertEquals(2, UserSession.getProgramCompletedCount("30-day"), "Program completed count must be exactly 2")
        val stats = WorkoutProgressService.getProgramProgressStats("30-day", 30)
        assertEquals(2, stats.completedCount, "ProgramProgressStats completedCount must be exactly 2")
    }

    // -----------------------------------------------------------------------------------------
    // Test 6: Partial workout persistence & restore
    // -----------------------------------------------------------------------------------------
    @Test
    fun testPartialWorkoutPersistenceAndRestore() {
        // Save a partial workout for Day 1
        WorkoutProgressService.saveWorkoutSession(
            workoutId = "day_1",
            dayNumber = 1,
            workoutTitle = "Day 1 - Chest & Triceps",
            targetMuscle = listOf("Chest"),
            experienceLevel = "beginner",
            durationMinutes = 10,
            totalExercises = 5,
            completedExercises = 1,
            totalSets = 15,
            completedSets = 3,
            warmUpCompleted = true,
            mainWorkoutCompleted = false,
            coolDownCompleted = false,
            programId = "30-day",
            programName = "30-Day Workout Plan",
            userId = testUserId,
            exercises = createSampleExerciseRecords(5, 1),
            sessionId = "session_partial_restore"
        )

        // Simulate new app launch or screen reload: reset memory state and restore from history
        UserSession.resetProgramProgress()
        assertEquals(0, UserSession.getProgramCompletedCount("30-day"), "Memory should be reset before restore")

        WorkoutProgressService.restoreUserSessionFromHistory(testUserId)

        // Verify that after restoring, the partial workout does NOT mark Day 1 completed
        assertFalse(UserSession.isProgramDayCompleted("30-day", 1), "Partial workout must NOT mark Day 1 completed upon restore")
        assertFalse(UserSession.isDayCompleted(1), "Partial workout must NOT mark Day 1 completed in UserSession")
        assertEquals(0, UserSession.getProgramCompletedCount("30-day"), "Completed program count must remain 0 after restoring partial session")
        assertEquals(3, UserSession.totalSetsCompleted, "Total sets completed should be restored (3 sets)")
    }

    // -----------------------------------------------------------------------------------------
    // Test 7: Firestore reconciliation sanitizes false completed flags & zero-set completed exercises
    // -----------------------------------------------------------------------------------------
    @Test
    fun testFirestoreReconciliationSanitizesCorruptedCompletedFlags() {
        // Exercise 1: actually completed (3 of 3 sets)
        // Exercise 2: false positive: completed = true, but completedSets = 0
        // Exercise 3: partial: completed = true, but completedSets = 1 of 3
        // Exercise 4 & 5: not completed
        val exercises = listOf(
            ExercisePerformanceRecord("ex_1", "Bench Press", 3, "10", 3, listOf(10, 10, 10), null, 60, true),
            ExercisePerformanceRecord("ex_2", "Incline DB Press", 3, "10", 0, emptyList(), null, 60, true),
            ExercisePerformanceRecord("ex_3", "Dips", 3, "10", 1, listOf(10), null, 60, true),
            ExercisePerformanceRecord("ex_4", "Pushups", 3, "10", 0, emptyList(), null, 60, false),
            ExercisePerformanceRecord("ex_5", "Cable Flyes", 3, "10", 0, emptyList(), null, 60, false)
        )

        val corruptedRecord = WorkoutSessionRecord(
            id = "session_corrupt_1",
            sessionId = "session_corrupt_1",
            userId = testUserId,
            workoutId = "day_1",
            dayNumber = 1,
            workoutTitle = "Day 1 - Chest",
            durationMinutes = 15,
            totalExercises = 5,
            completedExercises = 3, // Corrupted: falsely claimed 3
            prescribedExerciseCount = 5,
            completedExerciseCount = 3,
            mainWorkoutCompleted = true, // Corrupted: falsely true
            completionStatus = "completed", // Corrupted: falsely "completed"
            exercises = exercises
        )

        val sanitized = com.example.fitness.feature.progress.data.FirebaseWorkoutHistoryRepository.sanitizeAndReconcileSessionRecord(
            record = corruptedRecord,
            fallbackId = "session_corrupt_1",
            defaultUserId = testUserId
        )

        // Only Exercise 1 should remain completed!
        assertEquals(1, sanitized.completedExercises, "Only 1 exercise actually completed all sets")
        assertEquals(1, sanitized.completedExerciseCount, "Only 1 exercise actually completed all sets")
        assertEquals(5, sanitized.totalExercises, "Total exercises must be 5")
        assertFalse(sanitized.mainWorkoutCompleted, "mainWorkoutCompleted must be reconciled to false")
        assertEquals("partial", sanitized.completionStatus, "completionStatus must be reconciled to partial")
        assertFalse(WorkoutProgressService.isSessionFullyCompleted(sanitized), "isSessionFullyCompleted must be false")

        // Check each exercise's sanitized completed flag
        assertTrue(sanitized.exercises[0].completed, "Exercise 1 must be completed")
        assertFalse(sanitized.exercises[1].completed, "Exercise 2 (0 sets done) must be sanitized to false")
        assertFalse(sanitized.exercises[2].completed, "Exercise 3 (1/3 sets done) must be sanitized to false")
        assertFalse(sanitized.exercises[3].completed, "Exercise 4 must be false")
        assertFalse(sanitized.exercises[4].completed, "Exercise 5 must be false")
    }

    // -----------------------------------------------------------------------------------------
    // Test 8: Firestore reconciliation preserves legitimate full workout
    // -----------------------------------------------------------------------------------------
    @Test
    fun testFirestoreReconciliationPreservesLegitimateFullWorkout() {
        val exercises = (1..5).map { i ->
            ExercisePerformanceRecord("ex_$i", "Exercise $i", 3, "10", 3, listOf(10, 10, 10), null, 60, true)
        }

        val legitimateRecord = WorkoutSessionRecord(
            id = "session_full_1",
            sessionId = "session_full_1",
            userId = testUserId,
            workoutId = "day_1",
            dayNumber = 1,
            workoutTitle = "Day 1 - Chest",
            durationMinutes = 45,
            totalExercises = 5,
            completedExercises = 5,
            prescribedExerciseCount = 5,
            completedExerciseCount = 5,
            mainWorkoutCompleted = true,
            completionStatus = "completed",
            exercises = exercises
        )

        val sanitized = com.example.fitness.feature.progress.data.FirebaseWorkoutHistoryRepository.sanitizeAndReconcileSessionRecord(
            record = legitimateRecord,
            fallbackId = "session_full_1",
            defaultUserId = testUserId
        )

        assertEquals(5, sanitized.completedExercises)
        assertTrue(sanitized.mainWorkoutCompleted)
        assertEquals("completed", sanitized.completionStatus)
        assertTrue(WorkoutProgressService.isSessionFullyCompleted(sanitized))
    }

    // -----------------------------------------------------------------------------------------
    // Test 9: Firestore reconciliation preserves Rest Day completion
    // -----------------------------------------------------------------------------------------
    @Test
    fun testFirestoreReconciliationPreservesRestDays() {
        val restDayRecord = WorkoutSessionRecord(
            id = "session_rest_1",
            sessionId = "session_rest_1",
            userId = testUserId,
            workoutId = "day_4",
            dayNumber = 4,
            workoutTitle = "Day 4 - Rest & Recovery",
            durationMinutes = 15,
            totalExercises = 0,
            completedExercises = 0,
            prescribedExerciseCount = 0,
            completedExerciseCount = 0,
            mainWorkoutCompleted = true,
            completionStatus = "completed",
            exercises = emptyList()
        )

        val sanitized = com.example.fitness.feature.progress.data.FirebaseWorkoutHistoryRepository.sanitizeAndReconcileSessionRecord(
            record = restDayRecord,
            fallbackId = "session_rest_1",
            defaultUserId = testUserId
        )

        assertEquals(0, sanitized.completedExercises)
        assertTrue(sanitized.mainWorkoutCompleted, "Rest days should have mainWorkoutCompleted = true")
        assertEquals("completed", sanitized.completionStatus, "Rest day completion status should remain completed")
        assertTrue(WorkoutProgressService.isSessionFullyCompleted(sanitized), "isSessionFullyCompleted must be true for rest day")
    }

    // -----------------------------------------------------------------------------------------
    // Test 10: Weekday items default completed to false (no auto-completion of past days)
    // -----------------------------------------------------------------------------------------
    @Test
    fun testCurrentWeekDayItemsDefaultsCompletedToFalse() {
        val weekItems = com.example.fitness.getCurrentWeekDayItems()
        assertEquals(7, weekItems.size, "Must produce 7 days of the week")
        assertTrue(weekItems.none { it.completed }, "No weekday should be automatically marked completed without actual workout data")
    }
}
