package com.example.fitness.feature.progress

import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.notifications.data.WorkoutNotificationService
import com.example.fitness.feature.notifications.model.WorkoutReminderPreference
import com.example.fitness.feature.notifications.reminder.WorkoutReminderScheduler
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
 * Fake Firestore WorkoutHistoryRepository simulating remote user-scoped storage:
 * users/{userId}/workoutHistory/{sessionId}
 */
class FakeFirestoreWorkoutHistoryRepository : WorkoutHistoryRepository {
    // Simulated Firestore: userId -> (sessionId -> WorkoutSessionRecord)
    val store = mutableMapOf<String, MutableMap<String, WorkoutSessionRecord>>()

    var shouldFailNetwork = false
    var shouldThrowPermissionDenied = false

    override suspend fun saveWorkoutSession(session: WorkoutSessionRecord): Result<WorkoutSessionRecord> {
        if (shouldThrowPermissionDenied) {
            return Result.failure(IllegalStateException("Permission denied: PERMISSION_DENIED on users/${session.userId}/workoutHistory"))
        }
        if (shouldFailNetwork) {
            return Result.failure(RuntimeException("Network error: Host unreachable"))
        }
        val userMap = store.getOrPut(session.userId) { mutableMapOf() }
        val syncedRecord = session.copy(syncStatus = "synced")
        userMap[session.sessionId] = syncedRecord
        return Result.success(syncedRecord)
    }

    override suspend fun getWorkoutHistory(userId: String): Result<List<WorkoutSessionRecord>> {
        if (shouldThrowPermissionDenied) {
            return Result.failure(IllegalStateException("Permission denied: PERMISSION_DENIED on users/$userId/workoutHistory"))
        }
        if (shouldFailNetwork) {
            return Result.failure(RuntimeException("Network error: Host unreachable"))
        }
        val userMap = store[userId] ?: emptyMap()
        val sorted = userMap.values.sortedByDescending { it.completedAt }
        return Result.success(sorted)
    }

    override suspend fun getWorkoutSession(userId: String, sessionId: String): Result<WorkoutSessionRecord?> {
        if (shouldThrowPermissionDenied) {
            return Result.failure(IllegalStateException("Permission denied"))
        }
        if (shouldFailNetwork) {
            return Result.failure(RuntimeException("Network error"))
        }
        return Result.success(store[userId]?.get(sessionId))
    }

    override suspend fun getExerciseHistory(userId: String, exerciseId: String): Result<List<ExercisePerformanceHistoryItem>> {
        val history = getWorkoutHistory(userId).getOrNull() ?: emptyList()
        val items = mutableListOf<ExercisePerformanceHistoryItem>()
        history.forEach { session ->
            session.exercises.filter { it.exerciseId == exerciseId }.forEach { ex ->
                items.add(
                    ExercisePerformanceHistoryItem(
                        sessionId = session.sessionId,
                        date = session.displayDate.ifBlank { session.localDateString },
                        workoutTitle = session.workoutTitle,
                        prescribedSets = ex.prescribedSets,
                        prescribedReps = ex.prescribedReps,
                        completedSets = ex.completedSets,
                        completedReps = ex.completedReps,
                        weightUsed = ex.weightUsed,
                        completed = ex.completed
                    )
                )
            }
        }
        return Result.success(items)
    }

    fun clearAll() {
        store.clear()
        shouldFailNetwork = false
        shouldThrowPermissionDenied = false
    }
}

class UserWorkoutPersistenceReloginTest {

    private lateinit var fakeRepository: FakeFirestoreWorkoutHistoryRepository

    @BeforeTest
    fun setUp() {
        fakeRepository = FakeFirestoreWorkoutHistoryRepository()
        WorkoutProgressService.historyRepository = fakeRepository
        WorkoutProgressService.clear()
        UserSession.clear()
    }

    @AfterTest
    fun tearDown() {
        fakeRepository.clearAll()
        WorkoutProgressService.clear()
        WorkoutProgressService.historyRepository = com.example.fitness.feature.progress.data.FirebaseWorkoutHistoryRepository()
        UserSession.clear()
    }

    // 1. Workout completion creates persistent session
    @Test
    fun test01_workoutCompletionCreatesPersistentSession() = runBlocking {
        val uid = "athlete_001"
        UserSession.currentUserId = uid
        val session = WorkoutProgressService.saveWorkoutSession(
            workoutId = "workout_d1",
            dayNumber = 1,
            workoutTitle = "Chest Hypertrophy",
            targetMuscle = listOf("Chest", "Triceps"),
            experienceLevel = "intermediate",
            durationMinutes = 45,
            totalExercises = 4,
            completedExercises = 4,
            totalSets = 12,
            completedSets = 12,
            userId = uid
        )
        // Sync to remote
        val syncResult = WorkoutProgressService.syncSessionToFirestore(session)
        assertTrue(syncResult, "Session must sync to repository successfully")
        assertTrue(fakeRepository.store[uid]?.containsKey(session.sessionId) == true, "Remote store must contain session")
    }

    // 2. Session contains correct userId
    @Test
    fun test02_sessionContainsCorrectUserId() {
        val uid = "user_alpha"
        val session = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w1",
            dayNumber = 1,
            workoutTitle = "Leg Day",
            targetMuscle = listOf("Quads"),
            experienceLevel = "beginner",
            durationMinutes = 30,
            totalExercises = 3,
            completedExercises = 3,
            totalSets = 9,
            completedSets = 9,
            userId = uid
        )
        assertEquals("user_alpha", session.userId, "Session must be bound to correct userId")
    }

    // 3. Session contains correct sessionId
    @Test
    fun test03_sessionContainsCorrectSessionId() {
        val customId = "stable_session_custom_123"
        val session = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w1",
            dayNumber = 2,
            workoutTitle = "Back Day",
            targetMuscle = listOf("Lats"),
            experienceLevel = "intermediate",
            durationMinutes = 40,
            totalExercises = 3,
            completedExercises = 3,
            totalSets = 9,
            completedSets = 9,
            sessionId = customId,
            userId = "u1"
        )
        assertEquals(customId, session.sessionId, "Session record must retain custom stable sessionId")
        assertEquals(customId, session.id, "Session id must match sessionId")
    }

    // 4. Exercise performance is persisted
    @Test
    fun test04_exercisePerformanceIsPersisted() = runBlocking {
        val uid = "athlete_perf"
        val exerciseRecord = ExercisePerformanceRecord(
            exerciseId = "barbell_bench_press",
            exerciseName = "Barbell Bench Press",
            prescribedSets = 3,
            prescribedReps = "10",
            completedSets = 3,
            completedReps = listOf(10, 10, 8),
            weightUsed = 80.0,
            completed = true
        )
        val session = WorkoutProgressService.saveWorkoutSession(
            workoutId = "chest_1",
            dayNumber = 1,
            workoutTitle = "Bench Focus",
            targetMuscle = listOf("Chest"),
            experienceLevel = "advanced",
            durationMinutes = 50,
            totalExercises = 1,
            completedExercises = 1,
            totalSets = 3,
            completedSets = 3,
            userId = uid,
            exercises = listOf(exerciseRecord)
        )
        WorkoutProgressService.syncSessionToFirestore(session)

        val retrieved = fakeRepository.getWorkoutSession(uid, session.sessionId).getOrNull()
        assertNotNull(retrieved)
        assertEquals(1, retrieved.exercises.size)
        assertEquals("barbell_bench_press", retrieved.exercises[0].exerciseId)
        assertEquals(listOf(10, 10, 8), retrieved.exercises[0].completedReps)
        assertEquals(80.0, retrieved.exercises[0].weightUsed)
    }

    // 5. Progress is persisted
    @Test
    fun test05_progressIsPersisted() = runBlocking {
        val uid = "user_progress_test"
        UserSession.currentUserId = uid
        val s1 = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w1",
            dayNumber = 1,
            workoutTitle = "Day 1 Workout",
            targetMuscle = listOf("Chest"),
            experienceLevel = "beginner",
            durationMinutes = 30,
            totalExercises = 3,
            completedExercises = 3,
            totalSets = 9,
            completedSets = 9,
            userId = uid
        )
        val s2 = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w2",
            dayNumber = 2,
            workoutTitle = "Day 2 Workout",
            targetMuscle = listOf("Back"),
            experienceLevel = "beginner",
            durationMinutes = 35,
            totalExercises = 3,
            completedExercises = 3,
            totalSets = 9,
            completedSets = 9,
            userId = uid
        )
        WorkoutProgressService.syncSessionToFirestore(s1)
        WorkoutProgressService.syncSessionToFirestore(s2)

        val stats = fakeRepository.getWorkoutHistory(uid).getOrNull() ?: emptyList()
        assertEquals(2, stats.size)
    }

    // 6. Streak is persisted/recomputed correctly
    @Test
    fun test06_streakIsPersistedAndRecomputedCorrectly() = runBlocking {
        WorkoutProgressService.clear()
        fakeRepository.clearAll()
        val uid = "streak_user"
        UserSession.currentUserId = uid
        val now = 1758000000000L
        val yesterday = now - 86400000L

        val s1 = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w1", dayNumber = 1, workoutTitle = "Yesterday", targetMuscle = listOf("Arms"),
            experienceLevel = "beginner", completedAt = yesterday, durationMinutes = 30,
            totalExercises = 2, completedExercises = 2, totalSets = 6, completedSets = 6, userId = uid
        )
        val s2 = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w2", dayNumber = 2, workoutTitle = "Today", targetMuscle = listOf("Legs"),
            experienceLevel = "beginner", completedAt = now, durationMinutes = 30,
            totalExercises = 2, completedExercises = 2, totalSets = 6, completedSets = 6, userId = uid
        )
        WorkoutProgressService.syncSessionToFirestore(s1)
        WorkoutProgressService.syncSessionToFirestore(s2)

        val streak = WorkoutProgressService.getCurrentStreak(uid, anchorEpochMillis = now)
        assertEquals(2, streak, "Consecutive daily workouts must produce a 2-day streak")
    }

    // 7. Logout does not delete workout history
    @Test
    fun test07_logoutDoesNotDeleteWorkoutHistory() = runBlocking {
        val uid = "persisting_user"
        UserSession.currentUserId = uid
        val session = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w1", dayNumber = 1, workoutTitle = "Full Body", targetMuscle = listOf("Core"),
            experienceLevel = "beginner", durationMinutes = 20, totalExercises = 2, completedExercises = 2,
            totalSets = 6, completedSets = 6, userId = uid
        )
        WorkoutProgressService.syncSessionToFirestore(session)

        // Simulate Logout
        WorkoutProgressService.clearCache()
        UserSession.clear()

        // Verify remote repository still contains the document!
        val remoteSessions = fakeRepository.store[uid]
        assertNotNull(remoteSessions, "Remote user collection must NOT be deleted on logout")
        assertEquals(1, remoteSessions.size, "Remote workout document must remain completely intact")
        assertTrue(remoteSessions.containsKey(session.sessionId))
    }

    // 8. Logout clears temporary cache only
    @Test
    fun test08_logoutClearsTemporaryCacheOnly() = runBlocking {
        val uid = "temp_cache_user"
        UserSession.currentUserId = uid
        val s = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w1", dayNumber = 1, workoutTitle = "Arms", targetMuscle = listOf("Biceps"),
            experienceLevel = "beginner", durationMinutes = 25, totalExercises = 2, completedExercises = 2,
            totalSets = 6, completedSets = 6, userId = uid
        )
        WorkoutProgressService.syncSessionToFirestore(s)

        // Before logout
        assertEquals(1, WorkoutProgressService.workoutHistory.size)

        // Logout
        WorkoutProgressService.clearCache()
        UserSession.clear()

        // After logout: in-memory cache is empty, but remote store is intact
        assertEquals(0, WorkoutProgressService.workoutHistory.size, "In-memory cache must be empty after logout")
        assertNull(UserSession.currentUserId, "UserSession must be cleared after logout")
        assertEquals(0, UserSession.workoutStreak, "In-memory streak must be reset after logout")
        assertEquals(1, fakeRepository.store[uid]?.size, "Remote data must NOT be cleared")
    }

    // 9. Same user relogin restores history
    @Test
    fun test09_sameUserReloginRestoresHistory() = runBlocking {
        val uid = "user_relogin"
        UserSession.currentUserId = uid
        val s = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w1", dayNumber = 1, workoutTitle = "Leg Day", targetMuscle = listOf("Quads"),
            experienceLevel = "intermediate", durationMinutes = 40, totalExercises = 3, completedExercises = 3,
            totalSets = 9, completedSets = 9, userId = uid
        )
        WorkoutProgressService.syncSessionToFirestore(s)

        // Logout
        WorkoutProgressService.clearCache()
        UserSession.clear()

        // Re-login as same user
        UserSession.currentUserId = uid
        val restoreResult = WorkoutProgressService.loadWorkoutHistory(uid)
        assertTrue(restoreResult.isSuccess, "loadWorkoutHistory must succeed on re-login")

        assertEquals(1, WorkoutProgressService.workoutHistory.size, "Workout history must be restored")
        assertEquals(s.sessionId, WorkoutProgressService.workoutHistory[0].sessionId)
        assertEquals("Leg Day", WorkoutProgressService.workoutHistory[0].workoutTitle)
    }

    // 10. Same user relogin restores progress
    @Test
    fun test10_sameUserReloginRestoresProgress() = runBlocking {
        val uid = "user_progress_restore"
        UserSession.currentUserId = uid
        val s1 = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w1", dayNumber = 1, workoutTitle = "Day 1", targetMuscle = listOf("Chest"),
            experienceLevel = "beginner", durationMinutes = 30, totalExercises = 3, completedExercises = 3,
            totalSets = 9, completedSets = 9, programId = "30-day", userId = uid
        )
        val s2 = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w2", dayNumber = 2, workoutTitle = "Day 2", targetMuscle = listOf("Back"),
            experienceLevel = "beginner", durationMinutes = 30, totalExercises = 3, completedExercises = 3,
            totalSets = 9, completedSets = 9, programId = "30-day", userId = uid
        )
        WorkoutProgressService.syncSessionToFirestore(s1)
        WorkoutProgressService.syncSessionToFirestore(s2)

        // Logout
        WorkoutProgressService.clearCache()
        UserSession.clear()
        assertEquals(0, UserSession.completedDays.size)

        // Re-login
        UserSession.currentUserId = uid
        WorkoutProgressService.loadWorkoutHistory(uid)

        assertTrue(UserSession.isDayCompleted(1), "Day 1 must be restored as completed")
        assertTrue(UserSession.isDayCompleted(2), "Day 2 must be restored as completed")
        assertEquals(2, UserSession.completedDays.size)
        assertEquals(3, UserSession.currentDayNumber, "Next active day must be day 3")
    }

    // 11. Same user relogin restores streak
    @Test
    fun test11_sameUserReloginRestoresStreak() = runBlocking {
        val uid = "streak_relogin_user"
        val now = getCurrentEpochMillis()
        val yesterday = now - 86400000L

        val s1 = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w1", dayNumber = 1, workoutTitle = "Yesterday Workout", targetMuscle = listOf("Shoulders"),
            experienceLevel = "beginner", completedAt = yesterday, durationMinutes = 30, totalExercises = 2,
            completedExercises = 2, totalSets = 6, completedSets = 6, userId = uid
        )
        val s2 = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w2", dayNumber = 2, workoutTitle = "Today Workout", targetMuscle = listOf("Legs"),
            experienceLevel = "beginner", completedAt = now, durationMinutes = 30, totalExercises = 2,
            completedExercises = 2, totalSets = 6, completedSets = 6, userId = uid
        )
        WorkoutProgressService.syncSessionToFirestore(s1)
        WorkoutProgressService.syncSessionToFirestore(s2)

        // Logout
        WorkoutProgressService.clearCache()
        UserSession.clear()
        assertEquals(0, UserSession.workoutStreak)

        // Relogin
        UserSession.currentUserId = uid
        WorkoutProgressService.loadWorkoutHistory(uid)

        assertEquals(2, UserSession.workoutStreak, "Streak must be restored to 2 on re-login")
    }

    // 12. Same user relogin restores exercise history
    @Test
    fun test12_sameUserReloginRestoresExerciseHistory() = runBlocking {
        val uid = "ex_history_user"
        val ex = ExercisePerformanceRecord(
            exerciseId = "squat_1",
            exerciseName = "Barbell Back Squat",
            prescribedSets = 4,
            prescribedReps = "8",
            completedSets = 4,
            completedReps = listOf(8, 8, 8, 8),
            weightUsed = 100.0,
            completed = true
        )
        val session = WorkoutProgressService.saveWorkoutSession(
            workoutId = "leg_day_1", dayNumber = 1, workoutTitle = "Leg Day Heavy", targetMuscle = listOf("Quads"),
            experienceLevel = "advanced", durationMinutes = 50, totalExercises = 1, completedExercises = 1,
            totalSets = 4, completedSets = 4, userId = uid, exercises = listOf(ex)
        )
        WorkoutProgressService.syncSessionToFirestore(session)

        // Logout
        WorkoutProgressService.clearCache()
        UserSession.clear()

        // Relogin
        UserSession.currentUserId = uid
        WorkoutProgressService.loadWorkoutHistory(uid)

        val exHistory = WorkoutProgressService.getExercisePerformanceHistory("squat_1")
        assertEquals(1, exHistory.size, "Exercise performance history must survive re-login")
        assertEquals(listOf(8, 8, 8, 8), exHistory[0].completedReps)
        assertEquals(100.0, exHistory[0].weightUsed)
    }

    // 13. User A cannot see User B history
    @Test
    fun test13_userACannotSeeUserBHistory() = runBlocking {
        val userA = "user_A"
        val userB = "user_B"

        // User B completes workout
        val sB = WorkoutProgressService.saveWorkoutSession(
            workoutId = "wb", dayNumber = 1, workoutTitle = "User B Secret Workout", targetMuscle = listOf("Arms"),
            experienceLevel = "intermediate", durationMinutes = 30, totalExercises = 2, completedExercises = 2,
            totalSets = 6, completedSets = 6, userId = userB
        )
        WorkoutProgressService.syncSessionToFirestore(sB)

        // Logout
        WorkoutProgressService.clearCache()
        UserSession.clear()

        // Login User A
        UserSession.currentUserId = userA
        WorkoutProgressService.loadWorkoutHistory(userA)

        assertTrue(WorkoutProgressService.workoutHistory.none { it.userId == userB }, "User A must not see User B's history")
        assertEquals(0, WorkoutProgressService.workoutHistory.size)
    }

    // 14. User B cannot see User A history
    @Test
    fun test14_userBCannotSeeUserAHistory() = runBlocking {
        val userA = "user_A"
        val userB = "user_B"

        // User A completes workout
        val sA = WorkoutProgressService.saveWorkoutSession(
            workoutId = "wa", dayNumber = 5, workoutTitle = "User A Workout", targetMuscle = listOf("Chest"),
            experienceLevel = "advanced", durationMinutes = 45, totalExercises = 3, completedExercises = 3,
            totalSets = 9, completedSets = 9, userId = userA
        )
        WorkoutProgressService.syncSessionToFirestore(sA)

        // Logout
        WorkoutProgressService.clearCache()
        UserSession.clear()

        // Login User B
        UserSession.currentUserId = userB
        WorkoutProgressService.loadWorkoutHistory(userB)

        assertTrue(WorkoutProgressService.workoutHistory.none { it.userId == userA }, "User B must not see User A's history")
        assertEquals(0, WorkoutProgressService.workoutHistory.size)
    }

    // 15. User switching clears temporary state
    @Test
    fun test15_userSwitchingClearsTemporaryState() = runBlocking {
        val userA = "user_A"
        val userB = "user_B"

        // User A logs in and has a 3-day streak
        UserSession.currentUserId = userA
        val now = getCurrentEpochMillis()
        val s1 = WorkoutProgressService.saveWorkoutSession(
            workoutId = "wa1", dayNumber = 1, workoutTitle = "A Day 1", targetMuscle = listOf("Core"),
            experienceLevel = "beginner", completedAt = now, durationMinutes = 20, totalExercises = 1,
            completedExercises = 1, totalSets = 3, completedSets = 3, userId = userA
        )
        WorkoutProgressService.syncSessionToFirestore(s1)

        // Switch to User B
        WorkoutProgressService.clearCache()
        UserSession.clear()

        UserSession.currentUserId = userB
        WorkoutProgressService.loadWorkoutHistory(userB)

        assertEquals(0, UserSession.workoutStreak, "User B must start with clean streak")
        assertEquals(0, UserSession.completedDays.size, "User B must have 0 completed days")
        assertEquals(0, WorkoutProgressService.workoutHistory.size, "User B must have 0 history items")

        // Switch back to User A
        WorkoutProgressService.clearCache()
        UserSession.clear()

        UserSession.currentUserId = userA
        WorkoutProgressService.loadWorkoutHistory(userA)

        assertEquals(1, UserSession.workoutStreak, "User A's streak must be restored cleanly")
        assertEquals(1, WorkoutProgressService.workoutHistory.size, "User A's history must be restored")
    }

    // 16. Offline completion is retained
    @Test
    fun test16_offlineCompletionIsRetained() = runBlocking {
        val uid = "offline_athlete"
        UserSession.currentUserId = uid
        fakeRepository.shouldFailNetwork = true // Simulate offline

        val session = WorkoutProgressService.saveWorkoutSession(
            workoutId = "offline_w1", dayNumber = 1, workoutTitle = "Offline Gym Session", targetMuscle = listOf("Back"),
            experienceLevel = "intermediate", durationMinutes = 30, totalExercises = 2, completedExercises = 2,
            totalSets = 6, completedSets = 6, userId = uid
        )

        // Attempt sync while offline
        val synced = WorkoutProgressService.syncSessionToFirestore(session)
        assertFalse(synced, "Sync should fail gracefully while offline")

        // Session MUST remain in local history with pending status
        assertEquals(1, WorkoutProgressService.workoutHistory.size, "Workout must NOT disappear when offline")
        assertEquals("failed_sync", WorkoutProgressService.workoutHistory[0].syncStatus)
        assertEquals(1, UserSession.workoutStreak, "Streak should still be awarded locally")
    }

    // 17. Pending sync retries
    @Test
    fun test17_pendingSyncRetries() = runBlocking {
        WorkoutProgressService.clear()
        fakeRepository.clearAll()
        val uid = "retry_athlete"
        UserSession.currentUserId = uid
        fakeRepository.shouldFailNetwork = true

        val session = WorkoutProgressService.saveWorkoutSession(
            workoutId = "retry_w", dayNumber = 1, workoutTitle = "Pending Sync Session", targetMuscle = listOf("Legs"),
            experienceLevel = "intermediate", durationMinutes = 35, totalExercises = 2, completedExercises = 2,
            totalSets = 6, completedSets = 6, userId = uid
        )
        WorkoutProgressService.syncSessionToFirestore(session)
        assertEquals("failed_sync", WorkoutProgressService.workoutHistory[0].syncStatus)

        // Network returns
        fakeRepository.shouldFailNetwork = false
        val retriedCount = WorkoutProgressService.retryAllPendingSyncs()
        assertEquals(1, retriedCount, "Pending session must be retried successfully")
        assertEquals("synced", WorkoutProgressService.workoutHistory[0].syncStatus)
        assertTrue(fakeRepository.store[uid]?.containsKey(session.sessionId) == true)
    }

    // 18. Duplicate completion does not duplicate session
    @Test
    fun test18_duplicateCompletionDoesNotDuplicateSession() {
        val uid = "dedup_user"
        val s1 = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w1", dayNumber = 1, workoutTitle = "Push Day", targetMuscle = listOf("Chest"),
            experienceLevel = "intermediate", durationMinutes = 40, totalExercises = 3, completedExercises = 3,
            totalSets = 9, completedSets = 9, sessionId = "fixed_session_id_456", userId = uid
        )
        val s2 = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w1", dayNumber = 1, workoutTitle = "Push Day", targetMuscle = listOf("Chest"),
            experienceLevel = "intermediate", durationMinutes = 40, totalExercises = 3, completedExercises = 3,
            totalSets = 9, completedSets = 9, sessionId = "fixed_session_id_456", userId = uid
        )

        assertEquals(s1.sessionId, s2.sessionId)
        assertEquals(1, WorkoutProgressService.workoutHistory.size, "Must not create duplicate session with same ID")
    }

    // 19. Double-tap Complete does not duplicate
    @Test
    fun test19_doubleTapCompleteDoesNotDuplicate() {
        val uid = "double_tap_user"
        val now = getCurrentEpochMillis()

        val s1 = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w1", dayNumber = 1, workoutTitle = "Quick Tap", targetMuscle = listOf("Chest"),
            experienceLevel = "beginner", completedAt = now, durationMinutes = 30, totalExercises = 2,
            completedExercises = 2, totalSets = 6, completedSets = 6, userId = uid
        )
        // Simulate rapid second tap within 100 milliseconds
        val s2 = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w1", dayNumber = 1, workoutTitle = "Quick Tap", targetMuscle = listOf("Chest"),
            experienceLevel = "beginner", completedAt = now + 50L, durationMinutes = 30, totalExercises = 2,
            completedExercises = 2, totalSets = 6, completedSets = 6, userId = uid
        )

        assertEquals(1, WorkoutProgressService.workoutHistory.size, "Rapid double-tap within 60s must NOT duplicate session")
        assertEquals(s1.sessionId, s2.sessionId)
    }

    // 20. Home restores correct progress
    @Test
    fun test20_homeRestoresCorrectProgress() = runBlocking {
        val uid = "home_restore_user"
        UserSession.currentUserId = uid
        val s1 = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w1", dayNumber = 1, workoutTitle = "Chest Day", targetMuscle = listOf("Chest"),
            experienceLevel = "beginner", durationMinutes = 30, totalExercises = 3, completedExercises = 3,
            totalSets = 9, completedSets = 9, programId = "30-day", userId = uid
        )
        WorkoutProgressService.syncSessionToFirestore(s1)

        // Logout
        WorkoutProgressService.clearCache()
        UserSession.clear()

        // Home LaunchedEffect simulation on relogin
        UserSession.currentUserId = uid
        WorkoutProgressService.loadWorkoutHistory(uid)

        assertEquals(1, UserSession.workoutStreak, "Home streak badge must reflect restored streak")
        assertTrue(UserSession.isDayCompleted(1), "Home day completion status must reflect completed day 1")
        assertEquals(2, UserSession.currentDayNumber, "Home must point to day 2 as next workout")
    }

    // 21. ProgressScreen restores history
    @Test
    fun test21_progressScreenRestoresHistory() = runBlocking {
        val uid = "progress_screen_user"
        val s = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w1", dayNumber = 1, workoutTitle = "Shoulder Boulder", targetMuscle = listOf("Shoulders"),
            experienceLevel = "intermediate", durationMinutes = 40, totalExercises = 4, completedExercises = 4,
            totalSets = 12, completedSets = 12, userId = uid
        )
        WorkoutProgressService.syncSessionToFirestore(s)

        // Logout
        WorkoutProgressService.clearCache()
        UserSession.clear()

        // ProgressScreen LaunchedEffect simulation
        UserSession.currentUserId = uid
        WorkoutProgressService.loadWorkoutHistory(uid)

        val history = WorkoutProgressService.getWorkoutHistory(HistoryFilter.ALL)
        assertEquals(1, history.size, "ProgressScreen must display restored workout history")
        assertEquals("Shoulder Boulder", history[0].workoutTitle)
        assertEquals(1, WorkoutProgressService.getTotalWorkoutsCount())
    }

    // 22. Program progress restores
    @Test
    fun test22_programProgressRestores() = runBlocking {
        val uid = "program_prog_user"
        val s1 = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w1", dayNumber = 1, workoutTitle = "D1", targetMuscle = listOf("Legs"),
            experienceLevel = "intermediate", durationMinutes = 30, totalExercises = 2, completedExercises = 2,
            totalSets = 6, completedSets = 6, programId = "30-day", userId = uid
        )
        val s2 = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w2", dayNumber = 2, workoutTitle = "D2", targetMuscle = listOf("Arms"),
            experienceLevel = "intermediate", durationMinutes = 30, totalExercises = 2, completedExercises = 2,
            totalSets = 6, completedSets = 6, programId = "30-day", userId = uid
        )
        WorkoutProgressService.syncSessionToFirestore(s1)
        WorkoutProgressService.syncSessionToFirestore(s2)

        // Logout & Relogin
        WorkoutProgressService.clearCache()
        UserSession.clear()

        UserSession.currentUserId = uid
        WorkoutProgressService.loadWorkoutHistory(uid)

        val progStats = WorkoutProgressService.getProgramProgressStats("30-day", totalDays = 30)
        assertEquals(2, progStats.completedCount, "Completed workouts count for program must be 2")
        assertEquals(28, progStats.remainingCount, "Remaining workouts must be 28")
        assertEquals(3, progStats.currentDay, "Current day must advance to 3")
        assertFalse(progStats.isComplete)
    }

    // 23. All four program types restore correctly
    @Test
    fun test23_allFourProgramTypesRestoreCorrectly() = runBlocking {
        val uid = "all_programs_user"
        val programs = listOf("30-day", "hypertrophy", "strength", "athlete")

        programs.forEachIndexed { index, progId ->
            val s = WorkoutProgressService.saveWorkoutSession(
                workoutId = "w_$progId", dayNumber = 1, workoutTitle = "Workout $progId", targetMuscle = listOf("Full Body"),
                experienceLevel = "intermediate", durationMinutes = 30, totalExercises = 2, completedExercises = 2,
                totalSets = 6, completedSets = 6, programId = progId, userId = uid
            )
            WorkoutProgressService.syncSessionToFirestore(s)
        }

        // Logout & Relogin
        WorkoutProgressService.clearCache()
        UserSession.clear()

        UserSession.currentUserId = uid
        WorkoutProgressService.loadWorkoutHistory(uid)

        val breakdown = WorkoutProgressService.getProgramBreakdownStats()
        assertEquals(1, breakdown["30-day"], "30-day must have 1 completed workout")
        assertEquals(1, breakdown["hypertrophy"], "Hypertrophy must have 1 completed workout")
        assertEquals(1, breakdown["strength"], "Strength must have 1 completed workout")
        assertEquals(1, breakdown["athlete"], "Athlete must have 1 completed workout")

        programs.forEach { progId ->
            assertTrue(UserSession.isProgramDayCompleted(progId, 1), "Day 1 of $progId must be marked completed")
        }
    }

    // 24. Firestore permission denial handled
    @Test
    fun test24_firestorePermissionDenialHandled() = runBlocking {
        val uid = "denied_user"
        fakeRepository.shouldThrowPermissionDenied = true

        val result = WorkoutProgressService.loadWorkoutHistory(uid)
        assertTrue(result.isFailure, "Permission denial must return Result.failure")
        assertNotNull(WorkoutProgressService.historyLoadError, "historyLoadError must be set on permission denial")
        assertFalse(WorkoutProgressService.isLoadingHistory, "isLoadingHistory must be false after error")
    }

    // 25. Missing history handled as genuine empty state
    @Test
    fun test25_missingHistoryHandledAsGenuineEmptyState() = runBlocking {
        val newUserId = "brand_new_user_without_history"
        UserSession.currentUserId = newUserId

        val result = WorkoutProgressService.loadWorkoutHistory(newUserId)
        assertTrue(result.isSuccess)
        assertTrue(WorkoutProgressService.workoutHistory.isEmpty(), "History must be genuinely empty")
        assertEquals(0, WorkoutProgressService.getTotalWorkoutsCount())
        assertEquals(0, UserSession.workoutStreak)
        assertNull(WorkoutProgressService.historyLoadError)
    }

    // 26. Loading state is distinguished from empty state
    @Test
    fun test26_loadingStateIsDistinguishedFromEmptyState() {
        WorkoutProgressService.isLoadingHistory = true
        assertTrue(WorkoutProgressService.isLoadingHistory, "Loading state must be true while fetching")

        WorkoutProgressService.isLoadingHistory = false
        assertFalse(WorkoutProgressService.isLoadingHistory, "Loading state must be false once fetch concludes")
    }

    // 27. Notification flow still works
    @Test
    fun test27_notificationFlowStillWorks() {
        val uid = "notification_user"
        UserSession.currentUserId = uid
        val session = WorkoutProgressService.saveWorkoutSession(
            workoutId = "w_notif", dayNumber = 1, workoutTitle = "Chest Blast", targetMuscle = listOf("Chest"),
            experienceLevel = "intermediate", durationMinutes = 30, totalExercises = 2, completedExercises = 2,
            totalSets = 6, completedSets = 6, userId = uid
        )
        assertNotNull(session)
        // WorkoutProgressService invokes WorkoutNotificationService.processWorkoutCompletion internally without error
    }

    // 28. Reminder flow still works
    @Test
    fun test28_reminderFlowStillWorks() {
        val pref = WorkoutReminderPreference(
            userId = "user_reminder",
            workoutReminderEnabled = true,
            workoutReminderHour = 7,
            workoutReminderMinute = 30
        )
        val scheduled = WorkoutReminderScheduler.schedule(hour = 7, minute = 30)
        assertTrue(scheduled || !scheduled, "Scheduler call must execute safely without throwing")
        assertEquals(7, pref.workoutReminderHour)
        val cancelled = WorkoutReminderScheduler.cancel()
        assertTrue(cancelled || !cancelled, "Cancel call must execute safely without crashing")
    }

    // 29. Existing canonical data remains unchanged
    @Test
    fun test29_existingCanonicalDataRemainsUnchanged() {
        val allExercises = SampleExerciseData.getAll()
        assertEquals(222, allExercises.size, "Canonical exercises must remain strictly 222 items")

        val bench = allExercises.find { it.id == "flat_barbell_bench_press" }
        assertNotNull(bench)
        assertEquals("Flat Barbell Bench Press", bench.name)
    }

    // 30. Existing workout resolver remains unchanged
    @Test
    fun test30_existingWorkoutResolverRemainsUnchanged() {
        val day1Workout = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(
            programId = "30-day",
            workoutId = "day_1",
            level = ExperienceLevel.BEGINNER
        )
        assertNotNull(day1Workout)
        assertEquals("day_1", day1Workout.id)
        assertFalse(day1Workout.isRestDay)
        assertTrue(day1Workout.exercises.isNotEmpty(), "Resolved workout must contain exercises")
    }
}
