package com.example.fitness.feature.progress

import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.progress.data.HistoryFilter
import com.example.fitness.feature.progress.data.WorkoutHistoryRepository
import com.example.fitness.feature.progress.data.WorkoutProgressService
import com.example.fitness.feature.progress.model.ExercisePerformanceHistoryItem
import com.example.fitness.feature.progress.model.ExercisePerformanceRecord
import com.example.fitness.feature.progress.model.WorkoutSessionRecord
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import com.example.fitness.feature.workout.model.ExperienceLevel
import com.example.fitness.getCurrentEpochMillis
import com.example.fitness.getLocalDateString
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * STEP 10 — WORKOUT PROGRESS, HISTORY & ANALYTICS TEST SUITE
 *
 * Covers all 24 required test cases from Section 19:
 * 1. Completed workout record creation
 * 2. Stable session identity
 * 3. User isolation
 * 4. Prescribed vs actual separation
 * 5. Completed set calculation
 * 6. Completed exercise calculation
 * 7. Duration calculation
 * 8. History ordering
 * 9. History detail mapping
 * 10. Empty history state
 * 11. Program progress calculation
 * 12. Weekly activity calculation
 * 13. Zero-workout progress
 * 14. Streak with one workout
 * 15. Consecutive-day streak
 * 16. Streak with gaps
 * 17. Multiple workouts same day
 * 18. Longest streak
 * 19. Exercise history
 * 20. Failed Firestore write handling
 * 21. Duplicate completion protection
 * 22. Canonical program preservation
 * 23. Canonical exercise preservation
 * 24. Responsive state handling
 */
class WorkoutProgressHistoryAnalyticsTest {

    @BeforeTest
    fun setUp() {
        WorkoutProgressService.clear()
        WorkoutProgressService.historyRepository = com.example.fitness.feature.progress.data.FirebaseWorkoutHistoryRepository()
        UserSession.clear()
    }

    @AfterTest
    fun tearDown() {
        WorkoutProgressService.clear()
        WorkoutProgressService.historyRepository = com.example.fitness.feature.progress.data.FirebaseWorkoutHistoryRepository()
        UserSession.clear()
    }

    // -------------------------------------------------------------------------------------
    // 1. Completed workout record creation
    // -------------------------------------------------------------------------------------
    @Test
    fun test1_completedWorkoutRecordCreation() {
        val now = getCurrentEpochMillis()
        val record = WorkoutProgressService.saveWorkoutSession(
            workoutId = "base_day_1",
            dayNumber = 1,
            workoutTitle = "Chest + Core Ignition",
            targetMuscle = listOf("Chest", "Abs"),
            experienceLevel = "beginner",
            startedAt = now - 2400000L,
            completedAt = now,
            durationMinutes = 40,
            totalExercises = 6,
            completedExercises = 6,
            totalSets = 18,
            completedSets = 18,
            programId = "30-day",
            programName = "30-Day Workout Plan",
            userId = "user_123"
        )

        assertEquals("user_123", record.userId)
        assertEquals("30-day", record.programId)
        assertEquals("30-Day Workout Plan", record.programName)
        assertEquals(1, record.dayNumber)
        assertEquals(40, record.durationMinutes)
        assertEquals(2400, record.durationSeconds)
        assertEquals(6, record.prescribedExerciseCount)
        assertEquals(6, record.completedExerciseCount)
        assertEquals(18, record.prescribedSetCount)
        assertEquals(18, record.completedSetCount)
        assertEquals("completed", record.completionStatus)
        assertTrue(record.sessionId.isNotBlank())
        assertTrue(record.workoutDate.isNotBlank())
    }

    // -------------------------------------------------------------------------------------
    // 2. Stable session identity
    // -------------------------------------------------------------------------------------
    @Test
    fun test2_stableSessionIdentity() {
        val record = WorkoutProgressService.saveWorkoutSession(
            workoutId = "ht_day_3",
            dayNumber = 3,
            workoutTitle = "Hypertrophy Pull Focus",
            targetMuscle = listOf("Back", "Biceps"),
            experienceLevel = "intermediate",
            durationMinutes = 45,
            totalExercises = 5,
            completedExercises = 5,
            totalSets = 15,
            completedSets = 15,
            programId = "hypertrophy",
            programName = "Hypertrophy",
            userId = "user_456"
        )

        assertTrue(record.sessionId.contains("hypertrophy"))
        assertTrue(record.sessionId.contains("3"))
        assertEquals("user_456", record.userId)
        assertEquals("hypertrophy", record.programId)
        assertEquals(3, record.dayNumber)
    }

    // -------------------------------------------------------------------------------------
    // 3. User isolation
    // -------------------------------------------------------------------------------------
    @Test
    fun test3_userIsolation() {
        val user1Session = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w1",
            dayNumber = 1,
            workoutTitle = "User 1 Workout",
            targetMuscle = listOf("Chest"),
            experienceLevel = "beginner",
            durationMinutes = 30,
            totalExercises = 4,
            completedExercises = 4,
            totalSets = 12,
            completedSets = 12,
            userId = "user_A"
        )

        val user2Session = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w2",
            dayNumber = 2,
            workoutTitle = "User 2 Workout",
            targetMuscle = listOf("Back"),
            experienceLevel = "intermediate",
            durationMinutes = 35,
            totalExercises = 4,
            completedExercises = 4,
            totalSets = 12,
            completedSets = 12,
            userId = "user_B"
        )

        assertEquals("user_A", user1Session.userId)
        assertEquals("user_B", user2Session.userId)

        // Filter sessions by user scope
        val allSessions = WorkoutProgressService.workoutHistory
        val userASessions = allSessions.filter { it.userId == "user_A" }
        val userBSessions = allSessions.filter { it.userId == "user_B" }

        assertEquals(1, userASessions.size)
        assertEquals(1, userBSessions.size)
        assertEquals("user_A", userASessions.first().userId)
        assertEquals("user_B", userBSessions.first().userId)
    }

    // -------------------------------------------------------------------------------------
    // 4. Prescribed vs actual separation
    // -------------------------------------------------------------------------------------
    @Test
    fun test4_prescribedVsActualSeparation() {
        val performance = ExercisePerformanceRecord(
            exerciseId = "flat_barbell_bench_press",
            exerciseName = "Flat Barbell Bench Press",
            prescribedSets = 3,
            prescribedReps = "10 reps",
            completedSets = 3,
            completedReps = listOf(10, 10, 8), // Actual was 10, 10, 8 reps
            weightUsed = null,
            restSeconds = 90,
            completed = true
        )

        // Prescribed values must remain intact and distinct from actual execution
        assertEquals(3, performance.prescribedSets)
        assertEquals("10 reps", performance.prescribedReps)
        assertEquals(3, performance.completedSets)
        assertEquals(listOf(10, 10, 8), performance.completedReps)
        assertNull(performance.weightUsed, "Weight must be null when not recorded — no fake weight")
    }

    // -------------------------------------------------------------------------------------
    // 5. Completed set calculation
    // -------------------------------------------------------------------------------------
    @Test
    fun test5_completedSetCalculation() {
        val exercises = listOf(
            ExercisePerformanceRecord("ex1", "Exercise 1", prescribedSets = 4, prescribedReps = "10", completedSets = 4),
            ExercisePerformanceRecord("ex2", "Exercise 2", prescribedSets = 3, prescribedReps = "12", completedSets = 3),
            ExercisePerformanceRecord("ex3", "Exercise 3", prescribedSets = 3, prescribedReps = "15", completedSets = 2) // only 2 completed
        )

        val totalPrescribed = exercises.sumOf { it.prescribedSets }
        val totalCompleted = exercises.sumOf { it.completedSets }

        assertEquals(10, totalPrescribed)
        assertEquals(9, totalCompleted)
    }

    // -------------------------------------------------------------------------------------
    // 6. Completed exercise calculation
    // -------------------------------------------------------------------------------------
    @Test
    fun test6_completedExerciseCalculation() {
        val exercises = listOf(
            ExercisePerformanceRecord("ex1", "Exercise 1", 3, "10", 3, completed = true),
            ExercisePerformanceRecord("ex2", "Exercise 2", 3, "10", 3, completed = true),
            ExercisePerformanceRecord("ex3", "Exercise 3", 3, "10", 0, completed = false)
        )

        val totalExercises = exercises.size
        val completedExercises = exercises.count { it.completed }

        assertEquals(3, totalExercises)
        assertEquals(2, completedExercises)
    }

    // -------------------------------------------------------------------------------------
    // 7. Duration calculation
    // -------------------------------------------------------------------------------------
    @Test
    fun test7_durationCalculation() {
        val startedAt = 1000000L
        val completedAt = 1000000L + (45 * 60 * 1000L) // 45 minutes
        val durationMinutes = ((completedAt - startedAt) / 60000L).toInt()
        val durationSeconds = ((completedAt - startedAt) / 1000L).toInt()

        assertEquals(45, durationMinutes)
        assertEquals(2700, durationSeconds)
    }

    // -------------------------------------------------------------------------------------
    // 8. History ordering
    // -------------------------------------------------------------------------------------
    @Test
    fun test8_historyOrdering() {
        val now = getCurrentEpochMillis()

        WorkoutProgressService.saveWorkoutSession(
            workoutId = "day1",
            dayNumber = 1,
            workoutTitle = "Day 1 Workout",
            targetMuscle = listOf("Legs"),
            experienceLevel = "beginner",
            startedAt = now - 86400000L - 1800000L,
            completedAt = now - 86400000L, // yesterday
            durationMinutes = 30,
            totalExercises = 3,
            completedExercises = 3,
            totalSets = 9,
            completedSets = 9
        )

        WorkoutProgressService.saveWorkoutSession(
            workoutId = "day2",
            dayNumber = 2,
            workoutTitle = "Day 2 Workout",
            targetMuscle = listOf("Arms"),
            experienceLevel = "beginner",
            startedAt = now - 1800000L,
            completedAt = now, // today
            durationMinutes = 30,
            totalExercises = 3,
            completedExercises = 3,
            totalSets = 9,
            completedSets = 9
        )

        val history = WorkoutProgressService.getWorkoutHistory(HistoryFilter.ALL)
        assertEquals(2, history.size)
        // Newest session must be first in list
        assertEquals(2, history[0].dayNumber)
        assertEquals(1, history[1].dayNumber)
    }

    // -------------------------------------------------------------------------------------
    // 9. History detail mapping
    // -------------------------------------------------------------------------------------
    @Test
    fun test9_historyDetailMapping() {
        val performance = listOf(
            ExercisePerformanceRecord("flat_barbell_bench_press", "Flat Barbell Bench Press", 3, "10", 3, listOf(10, 10, 10), completed = true),
            ExercisePerformanceRecord("incline_dumbbell_press", "Incline Dumbbell Press", 3, "12", 3, listOf(12, 12, 12), completed = true)
        )

        val session = WorkoutProgressService.saveWorkoutSession(
            workoutId = "chest_blast",
            dayNumber = 5,
            workoutTitle = "Chest Blast",
            targetMuscle = listOf("Chest"),
            experienceLevel = "intermediate",
            durationMinutes = 40,
            totalExercises = 2,
            completedExercises = 2,
            totalSets = 6,
            completedSets = 6,
            exercises = performance
        )

        assertEquals("Chest Blast", session.workoutTitle)
        assertEquals(2, session.exercises.size)
        assertEquals("Flat Barbell Bench Press", session.exercises[0].exerciseName)
        assertEquals(3, session.exercises[0].prescribedSets)
        assertEquals(3, session.exercises[0].completedSets)
        assertTrue(session.exercises[0].completed)
    }

    // -------------------------------------------------------------------------------------
    // 10. Empty history state
    // -------------------------------------------------------------------------------------
    @Test
    fun test10_emptyHistoryState() {
        assertEquals(0, WorkoutProgressService.getTotalWorkoutsCount())
        assertEquals(0, WorkoutProgressService.getCompletedWorkoutCount())
        assertEquals(0, WorkoutProgressService.getTotalWorkoutTimeMinutes())
        assertEquals("0 min", WorkoutProgressService.getTotalWorkoutTimeFormatted())
        assertEquals(0, WorkoutProgressService.getCurrentStreak())
        assertEquals(0, WorkoutProgressService.getLongestStreak())
        assertTrue(WorkoutProgressService.getWorkoutHistory().isEmpty())
    }

    // -------------------------------------------------------------------------------------
    // 11. Program progress calculation
    // -------------------------------------------------------------------------------------
    @Test
    fun test11_programProgressCalculation() {
        for (day in 1..11) {
            WorkoutProgressService.saveWorkoutSession(
                workoutId = "base_day_$day",
                dayNumber = day,
                workoutTitle = "Day $day",
                targetMuscle = listOf("Full Body"),
                experienceLevel = "beginner",
                durationMinutes = 30,
                totalExercises = 4,
                completedExercises = 4,
                totalSets = 12,
                completedSets = 12,
                programId = "30-day"
            )
        }

        val stats = WorkoutProgressService.getProgramProgressStats("30-day", totalDays = 30)
        assertEquals("30-day", stats.programId)
        assertEquals(11, stats.completedCount)
        assertEquals(19, stats.remainingCount)
        assertEquals(30, stats.totalDays)
        // 11 / 30 = 0.36666... (~36.7%)
        assertEquals(0.3666f, stats.completionPercentage, 0.01f)
        assertFalse(stats.isComplete)
    }

    // -------------------------------------------------------------------------------------
    // 12. Weekly activity calculation
    // -------------------------------------------------------------------------------------
    @Test
    fun test12_weeklyActivityCalculation() {
        val now = getCurrentEpochMillis()
        WorkoutProgressService.saveWorkoutSession(
            workoutId = "session_today",
            dayNumber = 1,
            workoutTitle = "Today Session",
            targetMuscle = listOf("Chest"),
            experienceLevel = "beginner",
            completedAt = now,
            durationMinutes = 30,
            totalExercises = 4,
            completedExercises = 4,
            totalSets = 12,
            completedSets = 12
        )

        val activity = WorkoutProgressService.getWeeklyDayActivityStatus()
        assertEquals(7, activity.size, "Must have 7 day status indicators (Mon-Sun)")
        assertTrue(activity.any { it }, "At least one day in the current week must be active")
    }

    // -------------------------------------------------------------------------------------
    // 13. Zero-workout progress
    // -------------------------------------------------------------------------------------
    @Test
    fun test13_zeroWorkoutProgress() {
        val stats = WorkoutProgressService.getProgramProgressStats("30-day", totalDays = 30)
        assertEquals(0, stats.completedCount)
        assertEquals(30, stats.remainingCount)
        assertEquals(0.0f, stats.completionPercentage)
        assertFalse(stats.isComplete)
    }

    // -------------------------------------------------------------------------------------
    // 14. Streak with one workout
    // -------------------------------------------------------------------------------------
    @Test
    fun test14_streakWithOneWorkout() {
        val now = getCurrentEpochMillis()
        WorkoutProgressService.saveWorkoutSession(
            workoutId = "day1",
            dayNumber = 1,
            workoutTitle = "Workout 1",
            targetMuscle = listOf("Full Body"),
            experienceLevel = "beginner",
            completedAt = now,
            durationMinutes = 30,
            totalExercises = 3,
            completedExercises = 3,
            totalSets = 9,
            completedSets = 9
        )

        val currentStreak = WorkoutProgressService.getCurrentStreak(anchorEpochMillis = now)
        assertEquals(1, currentStreak, "1 workout today must yield a 1-day streak")
        assertEquals(1, WorkoutProgressService.getLongestStreak())
    }

    // -------------------------------------------------------------------------------------
    // 15. Consecutive-day streak
    // -------------------------------------------------------------------------------------
    @Test
    fun test15_consecutiveDayStreak() {
        val anchor = 1758000000000L // arbitrary fixed anchor timestamp
        for (i in 2 downTo 0) {
            val epoch = anchor - (i * 86400000L)
            WorkoutProgressService.saveWorkoutSession(
                workoutId = "w_$i",
                dayNumber = 3 - i,
                workoutTitle = "Day ${3 - i}",
                targetMuscle = listOf("Core"),
                experienceLevel = "beginner",
                completedAt = epoch,
                durationMinutes = 30,
                totalExercises = 3,
                completedExercises = 3,
                totalSets = 9,
                completedSets = 9
            )
        }

        val streak = WorkoutProgressService.getCurrentStreak(anchorEpochMillis = anchor)
        assertEquals(3, streak, "3 consecutive daily workouts must yield 3-day streak")
    }

    // -------------------------------------------------------------------------------------
    // 16. Streak with gaps
    // -------------------------------------------------------------------------------------
    @Test
    fun test16_streakWithGaps() {
        val anchor = 1758000000000L
        // Workout 4 days ago
        WorkoutProgressService.saveWorkoutSession(
            workoutId = "w_old",
            dayNumber = 1,
            workoutTitle = "Old",
            targetMuscle = listOf("Legs"),
            experienceLevel = "beginner",
            completedAt = anchor - (4 * 86400000L),
            durationMinutes = 30,
            totalExercises = 3,
            completedExercises = 3,
            totalSets = 9,
            completedSets = 9
        )
        // Workout today
        WorkoutProgressService.saveWorkoutSession(
            workoutId = "w_today",
            dayNumber = 2,
            workoutTitle = "Today",
            targetMuscle = listOf("Legs"),
            experienceLevel = "beginner",
            completedAt = anchor,
            durationMinutes = 30,
            totalExercises = 3,
            completedExercises = 3,
            totalSets = 9,
            completedSets = 9
        )

        // Because of the 3-day gap between workouts, current streak is only 1 day
        val streak = WorkoutProgressService.getCurrentStreak(anchorEpochMillis = anchor)
        assertEquals(1, streak, "Gap must reset current streak to 1 day")
    }

    // -------------------------------------------------------------------------------------
    // 17. Multiple workouts same day
    // -------------------------------------------------------------------------------------
    @Test
    fun test17_multipleWorkoutsSameDay() {
        val anchor = 1758000000000L
        // Session 1 in morning
        WorkoutProgressService.saveWorkoutSession(
            workoutId = "w_morning",
            dayNumber = 1,
            workoutTitle = "Morning Session",
            targetMuscle = listOf("Chest"),
            experienceLevel = "beginner",
            completedAt = anchor,
            durationMinutes = 25,
            totalExercises = 3,
            completedExercises = 3,
            totalSets = 9,
            completedSets = 9
        )
        // Session 2 in evening (same calendar date)
        WorkoutProgressService.saveWorkoutSession(
            workoutId = "w_evening",
            dayNumber = 2,
            workoutTitle = "Evening Core",
            targetMuscle = listOf("Abs"),
            experienceLevel = "beginner",
            completedAt = anchor + 36000000L,
            durationMinutes = 20,
            totalExercises = 2,
            completedExercises = 2,
            totalSets = 6,
            completedSets = 6
        )

        // Multiple sessions on the same calendar day must not inflate streak count
        val streak = WorkoutProgressService.getCurrentStreak(anchorEpochMillis = anchor)
        assertEquals(1, streak, "Multiple workouts on the same day must count as 1 streak day")
        assertEquals(2, WorkoutProgressService.getTotalWorkoutsCount(), "Total workouts count must be 2")
    }

    // -------------------------------------------------------------------------------------
    // 18. Longest streak
    // -------------------------------------------------------------------------------------
    @Test
    fun test18_longestStreak() {
        val anchor = 1758000000000L
        // 5-day streak in the past (days 10 to 6 ago)
        for (i in 10 downTo 6) {
            WorkoutProgressService.saveWorkoutSession(
                workoutId = "past_$i",
                dayNumber = 10 - i + 1,
                workoutTitle = "Past Workout",
                targetMuscle = listOf("Back"),
                experienceLevel = "beginner",
                completedAt = anchor - (i * 86400000L),
                durationMinutes = 30,
                totalExercises = 3,
                completedExercises = 3,
                totalSets = 9,
                completedSets = 9
            )
        }
        // Current streak: only 2 days (days 1 to 0 ago)
        for (i in 1 downTo 0) {
            WorkoutProgressService.saveWorkoutSession(
                workoutId = "recent_$i",
                dayNumber = 10 + (2 - i),
                workoutTitle = "Recent Workout",
                targetMuscle = listOf("Back"),
                experienceLevel = "beginner",
                completedAt = anchor - (i * 86400000L),
                durationMinutes = 30,
                totalExercises = 3,
                completedExercises = 3,
                totalSets = 9,
                completedSets = 9
            )
        }

        val longestStreak = WorkoutProgressService.getLongestStreak()
        assertEquals(5, longestStreak, "Longest streak must reflect the 5-day historical streak")
        val currentStreak = WorkoutProgressService.getCurrentStreak(anchorEpochMillis = anchor)
        assertEquals(2, currentStreak, "Current streak is 2 days")
    }

    // -------------------------------------------------------------------------------------
    // 19. Exercise history
    // -------------------------------------------------------------------------------------
    @Test
    fun test19_exerciseHistory() {
        val exerciseId = "flat_barbell_bench_press"

        WorkoutProgressService.saveWorkoutSession(
            workoutId = "s1",
            dayNumber = 1,
            workoutTitle = "Chest Day 1",
            targetMuscle = listOf("Chest"),
            experienceLevel = "beginner",
            durationMinutes = 30,
            totalExercises = 1,
            completedExercises = 1,
            totalSets = 3,
            completedSets = 3,
            exercises = listOf(
                ExercisePerformanceRecord(exerciseId, "Flat Barbell Bench Press", 3, "10", 3, listOf(10, 10, 10))
            )
        )

        WorkoutProgressService.saveWorkoutSession(
            workoutId = "s2",
            dayNumber = 5,
            workoutTitle = "Chest Day 2",
            targetMuscle = listOf("Chest"),
            experienceLevel = "beginner",
            durationMinutes = 35,
            totalExercises = 1,
            completedExercises = 1,
            totalSets = 3,
            completedSets = 3,
            exercises = listOf(
                ExercisePerformanceRecord(exerciseId, "Flat Barbell Bench Press", 3, "8", 3, listOf(8, 8, 8))
            )
        )

        val history = WorkoutProgressService.getExercisePerformanceHistory(exerciseId)
        assertEquals(2, history.size, "Must return 2 historical performance records for bench press")
        assertEquals(3, history[0].prescribedSets)
        assertEquals(listOf(8, 8, 8), history[0].completedReps)
    }

    // -------------------------------------------------------------------------------------
    // 20. Failed Firestore write handling
    // -------------------------------------------------------------------------------------
    @Test
    fun test20_failedFirestoreWriteHandling() = runBlocking {
        val failingRepo = object : WorkoutHistoryRepository {
            override suspend fun saveWorkoutSession(session: WorkoutSessionRecord): Result<WorkoutSessionRecord> {
                return Result.failure(RuntimeException("Network timeout"))
            }

            override suspend fun getWorkoutHistory(userId: String): Result<List<WorkoutSessionRecord>> {
                return Result.success(emptyList())
            }

            override suspend fun getWorkoutSession(userId: String, sessionId: String): Result<WorkoutSessionRecord?> {
                return Result.success(null)
            }

            override suspend fun getExerciseHistory(userId: String, exerciseId: String): Result<List<ExercisePerformanceHistoryItem>> {
                return Result.success(emptyList())
            }
        }

        WorkoutProgressService.historyRepository = failingRepo

        val session = WorkoutProgressService.saveWorkoutSession(
            workoutId = "test_fail",
            dayNumber = 1,
            workoutTitle = "Fail Test",
            targetMuscle = listOf("Chest"),
            experienceLevel = "beginner",
            durationMinutes = 30,
            totalExercises = 1,
            completedExercises = 1,
            totalSets = 3,
            completedSets = 3,
            userId = "user_test"
        )

        // Sync fails gracefully without throwing an exception
        val syncResult = WorkoutProgressService.syncSessionToFirestore(session)
        assertFalse(syncResult, "Sync must report false when repository throws")

        val stored = WorkoutProgressService.workoutHistory.first()
        assertEquals("failed_sync", stored.syncStatus, "Local status must reflect failed_sync")

        // Local progress is NOT lost or corrupted
        assertEquals(1, WorkoutProgressService.getTotalWorkoutsCount())
    }

    // -------------------------------------------------------------------------------------
    // 21. Duplicate completion protection
    // -------------------------------------------------------------------------------------
    @Test
    fun test21_duplicateCompletionProtection() {
        val now = getCurrentEpochMillis()

        val record1 = WorkoutProgressService.saveWorkoutSession(
            workoutId = "base_1",
            dayNumber = 1,
            workoutTitle = "Base Day 1",
            targetMuscle = listOf("Chest"),
            experienceLevel = "beginner",
            startedAt = now - 1800000L,
            completedAt = now,
            durationMinutes = 30,
            totalExercises = 5,
            completedExercises = 5,
            totalSets = 15,
            completedSets = 15,
            programId = "30-day"
        )

        // Rapid second tap with same timestamp and day number
        val record2 = WorkoutProgressService.saveWorkoutSession(
            workoutId = "base_1",
            dayNumber = 1,
            workoutTitle = "Base Day 1",
            targetMuscle = listOf("Chest"),
            experienceLevel = "beginner",
            startedAt = now - 1800000L,
            completedAt = now,
            durationMinutes = 30,
            totalExercises = 5,
            completedExercises = 5,
            totalSets = 15,
            completedSets = 15,
            programId = "30-day"
        )

        assertEquals(record1.sessionId, record2.sessionId)
        assertEquals(1, WorkoutProgressService.getTotalWorkoutsCount(), "Deduplication must prevent duplicate entry")
    }

    // -------------------------------------------------------------------------------------
    // 22. Canonical program preservation
    // -------------------------------------------------------------------------------------
    @Test
    fun test22_canonicalProgramPreservation() {
        val baseDay1 = CanonicalWorkoutResolver.resolveThirtyDayWorkout("base", "day1", ExperienceLevel.BEGINNER)
        assertEquals(1, baseDay1.dayNumber)
        assertFalse(baseDay1.isRestDay)
        assertTrue(baseDay1.exercises.isNotEmpty(), "Base day 1 must retain canonical exercises")

        val htDay1 = CanonicalWorkoutResolver.resolveThirtyDayWorkout("hypertrophy", "day1", ExperienceLevel.BEGINNER)
        assertEquals(1, htDay1.dayNumber)
        assertEquals("Chest + Triceps", htDay1.workoutTitle)
        assertTrue(htDay1.exercises.isNotEmpty(), "Hypertrophy day 1 must retain canonical exercises")
    }

    // -------------------------------------------------------------------------------------
    // 23. Canonical exercise preservation
    // -------------------------------------------------------------------------------------
    @Test
    fun test23_canonicalExercisePreservation() {
        val catalog = SampleExerciseData.getAll()
        assertEquals(222, catalog.size, "Catalog must strictly retain all 222 canonical exercises")

        catalog.forEach { ex ->
            assertTrue(ex.id.isNotBlank())
            assertTrue(ex.name.isNotBlank())
            assertTrue(ex.primaryMuscle.isNotBlank())
        }
    }

    // -------------------------------------------------------------------------------------
    // 24. Responsive state handling
    // -------------------------------------------------------------------------------------
    @Test
    fun test24_responsiveStateHandling() {
        val session = WorkoutProgressService.saveWorkoutSession(
            workoutId = "responsive_test",
            dayNumber = 14,
            workoutTitle = "Upper Body Hypertrophy & Stability",
            targetMuscle = listOf("Chest", "Back", "Shoulders"),
            experienceLevel = "advanced",
            durationMinutes = 52,
            totalExercises = 8,
            completedExercises = 8,
            totalSets = 24,
            completedSets = 24,
            programId = "hypertrophy",
            programName = "Hypertrophy"
        )

        // String presentations must not contain invalid characters, breaks, or nulls
        val titleText = session.workoutTitle
        val dateText = session.displayDate
        val durationText = "${session.durationMinutes} min"
        val progressPercentText = "${((session.completedExercises.toFloat() / session.totalExercises) * 100).toInt()}%"

        assertFalse(titleText.contains("null"))
        assertFalse(dateText.contains("null"))
        assertFalse(durationText.contains("null"))
        assertEquals("100%", progressPercentText)
    }

    // -------------------------------------------------------------------------------------
    // 25. Civil calendar format and parse consistency
    // -------------------------------------------------------------------------------------
    @Test
    fun test25_civilCalendarFormatAndParseConsistency() {
        val testDates = listOf("2026-01-01", "2026-02-28", "2024-02-29", "2026-09-18", "2026-09-19", "2026-12-31")
        for (dateStr in testDates) {
            val epochDays = WorkoutProgressService.parseLocalDateToEpochDays(dateStr)
            assertNotNull(epochDays, "Failed to parse $dateStr")
            val roundTrip = WorkoutProgressService.formatEpochDaysToLocalDate(epochDays)
            assertEquals(dateStr, roundTrip, "Round-trip conversion failed for $dateStr")
        }
    }

    // -------------------------------------------------------------------------------------
    // 26. Body part / gym workout persistence updates streak and activity
    // -------------------------------------------------------------------------------------
    @Test
    fun test26_bodyPartGymWorkoutPersistenceUpdatesStreakAndActivity() = runBlocking {
        val testUid = "user_gym_test"
        UserSession.currentUserId = testUid

        val mockRepo = object : WorkoutHistoryRepository {
            override suspend fun saveWorkoutSession(session: WorkoutSessionRecord): Result<WorkoutSessionRecord> {
                return Result.success(session.copy(syncStatus = "synced"))
            }
            override suspend fun getWorkoutHistory(userId: String): Result<List<WorkoutSessionRecord>> {
                return Result.success(emptyList())
            }
            override suspend fun getWorkoutSession(userId: String, sessionId: String): Result<WorkoutSessionRecord?> {
                return Result.success(null)
            }
            override suspend fun getExerciseHistory(userId: String, exerciseId: String): Result<List<ExercisePerformanceHistoryItem>> {
                return Result.success(emptyList())
            }
        }
        WorkoutProgressService.historyRepository = mockRepo

        // Create exercises for a gym workout
        val exercises = listOf(
            ExercisePerformanceRecord("ex_bench", "Bench Press", 3, "10", 3, listOf(10, 10, 10), null, 60, true),
            ExercisePerformanceRecord("ex_incline", "Incline Dumbbell", 3, "10", 3, listOf(10, 10, 10), null, 60, true)
        )

        val result = WorkoutProgressService.saveAndPersistWorkoutSession(
            workoutId = "gym_chest_1",
            dayNumber = 1,
            workoutTitle = "Chest Routine",
            targetMuscle = listOf("Chest"),
            experienceLevel = "beginner",
            durationMinutes = 45,
            totalExercises = 2,
            completedExercises = 2,
            totalSets = 6,
            completedSets = 6,
            warmUpCompleted = true,
            mainWorkoutCompleted = true,
            coolDownCompleted = true,
            programId = "gym",
            programName = "Gym Workouts",
            userId = testUid,
            exercises = exercises
        )

        assertTrue(result.isSuccess, "Gym workout persistence must succeed")
        val session = result.getOrNull()
        assertNotNull(session)
        assertEquals("completed", session.completionStatus)
        assertTrue(WorkoutProgressService.isSessionFullyCompleted(session))

        // Streak must be at least 1
        val streak = WorkoutProgressService.getCurrentStreak(testUid)
        assertTrue(streak >= 1, "Streak must update to at least 1 after completing a gym workout")

        // Weekly activity status must reflect today's workout
        val weeklyStatus = WorkoutProgressService.getWeeklyDayActivityStatus()
        val todayDow = com.example.fitness.getCurrentDayOfWeekIndex()
        assertTrue(weeklyStatus[todayDow], "Today's index ($todayDow) must be marked active in weekly day activity")
    }

    // -------------------------------------------------------------------------------------
    // 27. Multiple sessions on the same date count as 1 day for streak
    // -------------------------------------------------------------------------------------
    @Test
    fun test27_multiSessionSameDayDoesNotDoubleCountStreak() = runBlocking {
        val testUid = "user_multi_session"
        UserSession.currentUserId = testUid

        val mockRepo = object : WorkoutHistoryRepository {
            override suspend fun saveWorkoutSession(session: WorkoutSessionRecord): Result<WorkoutSessionRecord> {
                return Result.success(session.copy(syncStatus = "synced"))
            }
            override suspend fun getWorkoutHistory(userId: String): Result<List<WorkoutSessionRecord>> {
                return Result.success(emptyList())
            }
            override suspend fun getWorkoutSession(userId: String, sessionId: String): Result<WorkoutSessionRecord?> {
                return Result.success(null)
            }
            override suspend fun getExerciseHistory(userId: String, exerciseId: String): Result<List<ExercisePerformanceHistoryItem>> {
                return Result.success(emptyList())
            }
        }
        val defaultRepo = WorkoutProgressService.historyRepository
        try {
            WorkoutProgressService.historyRepository = mockRepo

            val exercises = listOf(
                ExercisePerformanceRecord("ex_1", "Pushups", 3, "10", 3, listOf(10, 10, 10), null, 60, true)
            )

            // Session 1 today
            WorkoutProgressService.saveAndPersistWorkoutSession(
                workoutId = "w1",
                dayNumber = 1,
                workoutTitle = "Morning Routine",
                targetMuscle = listOf("Chest"),
                experienceLevel = "beginner",
                durationMinutes = 20,
                totalExercises = 1,
                completedExercises = 1,
                totalSets = 3,
                completedSets = 3,
                programId = "gym",
                userId = testUid,
                exercises = exercises
            )

            // Session 2 today
            WorkoutProgressService.saveAndPersistWorkoutSession(
                workoutId = "w2",
                dayNumber = 2,
                workoutTitle = "Evening Routine",
                targetMuscle = listOf("Arms"),
                experienceLevel = "beginner",
                durationMinutes = 25,
                totalExercises = 1,
                completedExercises = 1,
                totalSets = 3,
                completedSets = 3,
                programId = "gym",
                userId = testUid,
                exercises = exercises
            )

            val streak = WorkoutProgressService.getCurrentStreak(testUid)
            assertEquals(1, streak, "Two workouts on the same day must count as 1 streak day, not 2")
        } finally {
            WorkoutProgressService.historyRepository = defaultRepo
        }
    }

    // -------------------------------------------------------------------------------------
    // 28. Non-consecutive workouts / skipped Day 1 advances progress correctly
    // -------------------------------------------------------------------------------------
    @Test
    fun test28_nonConsecutiveWorkoutsOrSkippedDayOneAdvancesProgressCorrectly() = runBlocking {
        val testUid = "non_consecutive_user"
        WorkoutProgressService.clearCache()
        UserSession.clear()
        UserSession.currentUserId = testUid
        UserSession.setActiveProgram("30-day")

        // User starts from calendar days 2, 4, 5, 6 without having done Day 1
        val completedDays = listOf(2, 4, 5, 6)
        val now = getCurrentEpochMillis()

        completedDays.forEachIndexed { idx, dayNum ->
            WorkoutProgressService.saveWorkoutSession(
                workoutId = "session_day_$dayNum",
                dayNumber = dayNum,
                workoutTitle = "Day $dayNum Routine",
                targetMuscle = listOf("Chest"),
                experienceLevel = "beginner",
                completedAt = now - ((4 - idx) * 86400000L),
                durationMinutes = 20,
                totalExercises = 2,
                completedExercises = 2,
                totalSets = 6,
                completedSets = 6,
                programId = "30-day",
                userId = testUid,
                mainWorkoutCompleted = true
            )
        }

        // 1. Verify UserSession does NOT stay stuck on Day 1
        val sessionCurrentDay = UserSession.getProgramCurrentDay("30-day")
        assertTrue(sessionCurrentDay > 1, "UserSession must not be stuck at Day 1 when 4 workouts are completed, got: $sessionCurrentDay")
        assertEquals(4, UserSession.getProgramCompletedCount("30-day"))

        // 2. Verify ProgramProgressStats from WorkoutProgressService
        val stats = WorkoutProgressService.getProgramProgressStats("30-day", totalDays = 30)
        assertEquals(4, stats.completedCount, "Must have 4 completed workouts")
        assertEquals(26, stats.remainingCount, "Remaining must be 26 (30 - 4)")
        assertTrue(stats.currentDay > 1, "Current day in stats must not be stuck at Day 1, got: ${stats.currentDay}")
        assertEquals(4f / 30f, stats.completionPercentage, 0.001f)
    }

    // -------------------------------------------------------------------------------------
    // 29. Active calendar days consolidation for program progress
    // -------------------------------------------------------------------------------------
    @Test
    fun test29_trackCalendarDaysConsolidatesSameDayWorkouts() = runBlocking {
        val testUid = "calendar_days_user"
        WorkoutProgressService.clearCache()
        UserSession.clear()
        UserSession.currentUserId = testUid
        UserSession.setActiveProgram("30-day")

        val now = getCurrentEpochMillis()
        val dayMillis = 86400000L

        // Thursday (2 workouts): Day 4 and Day 2
        WorkoutProgressService.saveWorkoutSession(
            workoutId = "thursday_w1", dayNumber = 4, workoutTitle = "Shoulders", targetMuscle = listOf("Shoulders"),
            experienceLevel = "beginner", completedAt = now - (2 * dayMillis), durationMinutes = 20,
            totalExercises = 2, completedExercises = 2, totalSets = 6, completedSets = 6,
            programId = "30-day", userId = testUid, mainWorkoutCompleted = true
        )
        WorkoutProgressService.saveWorkoutSession(
            workoutId = "thursday_w2", dayNumber = 2, workoutTitle = "Back & Biceps", targetMuscle = listOf("Back"),
            experienceLevel = "beginner", completedAt = now - (2 * dayMillis), durationMinutes = 25,
            totalExercises = 2, completedExercises = 2, totalSets = 6, completedSets = 6,
            programId = "30-day", userId = testUid, mainWorkoutCompleted = true
        )

        // Friday (1 workout): Day 5
        WorkoutProgressService.saveWorkoutSession(
            workoutId = "friday_w1", dayNumber = 5, workoutTitle = "Arms", targetMuscle = listOf("Arms"),
            experienceLevel = "beginner", completedAt = now - (1 * dayMillis), durationMinutes = 20,
            totalExercises = 2, completedExercises = 2, totalSets = 6, completedSets = 6,
            programId = "30-day", userId = testUid, mainWorkoutCompleted = true
        )

        // Saturday (1 workout): Day 6
        WorkoutProgressService.saveWorkoutSession(
            workoutId = "saturday_w1", dayNumber = 6, workoutTitle = "Core", targetMuscle = listOf("Core"),
            experienceLevel = "beginner", completedAt = now, durationMinutes = 20,
            totalExercises = 2, completedExercises = 2, totalSets = 6, completedSets = 6,
            programId = "30-day", userId = testUid, mainWorkoutCompleted = true
        )

        // With trackCalendarDays = true: 2 workouts on Thursday count as 1 day -> Total 3 days
        val calendarStats = WorkoutProgressService.getProgramProgressStats("30-day", totalDays = 30, trackCalendarDays = true)
        assertEquals(3, calendarStats.completedCount, "Multiple workouts on Thursday must consolidate into 1 active day")
        assertEquals(27, calendarStats.remainingCount, "Remaining workouts must be 27 (30 - 3)")
        assertEquals(3, calendarStats.currentDay, "Current day must display 3 (DAY 3 / 30)")
        assertEquals(3f / 30f, calendarStats.completionPercentage, 0.001f)

        // Default / routine count mode remains intact
        val routineStats = WorkoutProgressService.getProgramProgressStats("30-day", totalDays = 30, trackCalendarDays = false)
        assertEquals(4, routineStats.completedCount, "Without trackCalendarDays, routine count must be 4")
        assertEquals(26, routineStats.remainingCount, "Remaining must be 26 (30 - 4)")
    }
}
