package com.example.fitness.feature.workout

import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.progress.data.WorkoutProgressService
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import com.example.fitness.feature.workout.model.ExperienceLevel
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * ATHGYM ACTIVE WORKOUT SESSION & TIMER CONTROL TEST SUITE
 *
 * Verifies that the workout execution flow and timer strictly conform to:
 * - CASE A: Opening Today's Workout -> Timer = 00:00, Button = START EXERCISE, no start timestamp.
 * - CASE B: Tap START EXERCISE -> Timer immediately starts, first exercise activity starts, state IN_PROGRESS.
 * - CASE C: Open workout -> wait 30 seconds without pressing button -> Timer still 00:00, no timestamp.
 * - CASE D: START EXERCISE -> leave screen -> return -> state & elapsed time preserved, does not restart from 0.
 * - CASE E: START EXERCISE -> complete workout -> completion, Progress, and history persistence preserved.
 * - Back button behavior: clean exit before START EXERCISE; confirmation dialog / state retention after starting.
 */
private class FakeTestWorkoutHistoryRepository : com.example.fitness.feature.progress.data.WorkoutHistoryRepository {
    val store = mutableMapOf<String, MutableMap<String, com.example.fitness.feature.progress.model.WorkoutSessionRecord>>()

    override suspend fun saveWorkoutSession(session: com.example.fitness.feature.progress.model.WorkoutSessionRecord): Result<com.example.fitness.feature.progress.model.WorkoutSessionRecord> {
        val userMap = store.getOrPut(session.userId) { mutableMapOf() }
        val synced = session.copy(syncStatus = "synced")
        userMap[session.sessionId] = synced
        return Result.success(synced)
    }

    override suspend fun getWorkoutHistory(userId: String): Result<List<com.example.fitness.feature.progress.model.WorkoutSessionRecord>> {
        val userMap = store[userId] ?: emptyMap()
        return Result.success(userMap.values.sortedByDescending { it.completedAt })
    }

    override suspend fun getWorkoutSession(userId: String, sessionId: String): Result<com.example.fitness.feature.progress.model.WorkoutSessionRecord?> {
        return Result.success(store[userId]?.get(sessionId))
    }

    override suspend fun getExerciseHistory(userId: String, exerciseId: String): Result<List<com.example.fitness.feature.progress.model.ExercisePerformanceHistoryItem>> {
        return Result.success(emptyList())
    }
}

class ActiveWorkoutSessionTimerTest {

    private lateinit var fakeRepository: FakeTestWorkoutHistoryRepository

    @BeforeTest
    fun setUp() {
        fakeRepository = FakeTestWorkoutHistoryRepository()
        WorkoutProgressService.historyRepository = fakeRepository
        WorkoutProgressService.clear()
        UserSession.clear()
        UserSession.currentUserId = "timer_test_user"
        UserSession.setActiveProgram("hypertrophy")
        ActiveWorkoutSessionManager.resetSession()
    }

    @AfterTest
    fun tearDown() {
        ActiveWorkoutSessionManager.resetSession()
        WorkoutProgressService.clear()
        WorkoutProgressService.historyRepository = com.example.fitness.feature.progress.data.FirebaseWorkoutHistoryRepository()
        UserSession.clear()
    }

    /**
     * CASE A:
     * Today's Workout -> Open
     * Expected:
     * - Timer = 00:00
     * - Workout session NOT_STARTED
     * - No workout start timestamp yet
     * - Current phase is WARM_UP
     */
    @Test
    fun testCaseA_openWorkout_timerRemainsZero_notStarted_noStartTimestamp() {
        val workoutId = "day_1"
        val programId = "hypertrophy"
        val initialPhase = SessionPhase.WARM_UP

        ActiveWorkoutSessionManager.prepareSession(workoutId, programId, initialPhase)

        assertEquals(WorkoutExecutionState.NOT_STARTED, ActiveWorkoutSessionManager.executionState)
        assertEquals(0, ActiveWorkoutSessionManager.totalSecondsElapsed)
        assertEquals("00:00", ActiveWorkoutSessionManager.formattedTime)
        assertNull(ActiveWorkoutSessionManager.workoutStartedAt, "Start timestamp must be null before START EXERCISE is pressed")
        assertEquals(SessionPhase.WARM_UP, ActiveWorkoutSessionManager.currentPhase)
        assertEquals(workoutId, ActiveWorkoutSessionManager.activeWorkoutId)
        assertEquals(programId, ActiveWorkoutSessionManager.activeProgramId)
        assertNotNull(ActiveWorkoutSessionManager.stableSessionId)
    }

    /**
     * CASE B:
     * Today's Workout -> Open -> START EXERCISE
     * Expected:
     * - Timer immediately starts (transitions to IN_PROGRESS)
     * - Start timestamp recorded
     * - Transitions to MAIN_WORKOUT
     * - First workout activity / exercises start
     */
    @Test
    fun testCaseB_openWorkout_startExercise_startsTimerAndFirstActivity() {
        val workoutId = "day_1"
        val programId = "hypertrophy"

        ActiveWorkoutSessionManager.prepareSession(workoutId, programId, SessionPhase.WARM_UP)
        assertEquals(WorkoutExecutionState.NOT_STARTED, ActiveWorkoutSessionManager.executionState)

        // User taps "START EXERCISE"
        val beforeStart = com.example.fitness.getCurrentEpochMillis()
        ActiveWorkoutSessionManager.startExercise(SessionPhase.WARM_UP)
        val afterStart = com.example.fitness.getCurrentEpochMillis()

        assertEquals(WorkoutExecutionState.IN_PROGRESS, ActiveWorkoutSessionManager.executionState)
        assertNotNull(ActiveWorkoutSessionManager.workoutStartedAt)
        assertTrue(ActiveWorkoutSessionManager.workoutStartedAt!! in beforeStart..afterStart)

        // Phase transitioned from WARM_UP to MAIN_WORKOUT
        assertEquals(SessionPhase.MAIN_WORKOUT, ActiveWorkoutSessionManager.currentPhase)
        assertEquals(0, ActiveWorkoutSessionManager.currentExerciseIndex)
        assertEquals(1, ActiveWorkoutSessionManager.currentSetIndex)

        // Timer ticks while IN_PROGRESS
        ActiveWorkoutSessionManager.tickTimer()
        assertEquals(1, ActiveWorkoutSessionManager.totalSecondsElapsed)
        assertEquals("00:01", ActiveWorkoutSessionManager.formattedTime)

        ActiveWorkoutSessionManager.tickTimer()
        assertEquals(2, ActiveWorkoutSessionManager.totalSecondsElapsed)
        assertEquals("00:02", ActiveWorkoutSessionManager.formattedTime)
    }

    /**
     * CASE C:
     * Open workout -> wait 30 seconds without pressing button
     * Expected:
     * - Timer still = 00:00
     * - No session start timestamp
     * - State remains NOT_STARTED
     */
    @Test
    fun testCaseC_openWorkout_waitWithoutPressingButton_timerStaysAtZero() {
        val workoutId = "day_1"
        val programId = "hypertrophy"

        ActiveWorkoutSessionManager.prepareSession(workoutId, programId, SessionPhase.WARM_UP)

        // Simulate 30 seconds passing or tick attempts while NOT_STARTED
        repeat(30) {
            ActiveWorkoutSessionManager.tickTimer()
        }

        assertEquals(WorkoutExecutionState.NOT_STARTED, ActiveWorkoutSessionManager.executionState)
        assertEquals(0, ActiveWorkoutSessionManager.totalSecondsElapsed, "Timer must remain at 00:00 if not started")
        assertEquals("00:00", ActiveWorkoutSessionManager.formattedTime)
        assertNull(ActiveWorkoutSessionManager.workoutStartedAt, "Start timestamp must not be recorded while not started")
    }

    /**
     * CASE D:
     * START EXERCISE -> leave screen -> return
     * Expected:
     * - Existing session behavior is preserved
     * - Timer does not restart from zero accidentally
     * - Exercise and set progress preserved
     */
    @Test
    fun testCaseD_startExercise_leaveScreen_return_preservesSessionAndTimer() {
        val workoutId = "day_1"
        val programId = "hypertrophy"

        ActiveWorkoutSessionManager.prepareSession(workoutId, programId, SessionPhase.WARM_UP)
        ActiveWorkoutSessionManager.startExercise(SessionPhase.WARM_UP)

        // Simulate 45 seconds of active workout
        repeat(45) {
            ActiveWorkoutSessionManager.tickTimer()
        }
        val originalStartedAt = ActiveWorkoutSessionManager.workoutStartedAt
        val originalSessionId = ActiveWorkoutSessionManager.stableSessionId

        // User advances to exercise 1, set 2, completes sets
        ActiveWorkoutSessionManager.currentExerciseIndex = 1
        ActiveWorkoutSessionManager.currentSetIndex = 2
        ActiveWorkoutSessionManager.completedSetsByExercise["ex_1"] = 3
        ActiveWorkoutSessionManager.completedRepsByExercise["ex_1"] = mutableListOf(10, 10, 10)

        assertEquals(45, ActiveWorkoutSessionManager.totalSecondsElapsed)
        assertEquals("00:45", ActiveWorkoutSessionManager.formattedTime)

        // User leaves screen and returns: screen re-initialization calls prepareSession
        ActiveWorkoutSessionManager.prepareSession(workoutId, programId, SessionPhase.WARM_UP)

        // Must PRESERVE running session
        assertEquals(WorkoutExecutionState.IN_PROGRESS, ActiveWorkoutSessionManager.executionState)
        assertEquals(45, ActiveWorkoutSessionManager.totalSecondsElapsed, "Timer must NOT restart from zero when returning")
        assertEquals("00:45", ActiveWorkoutSessionManager.formattedTime)
        assertEquals(originalStartedAt, ActiveWorkoutSessionManager.workoutStartedAt)
        assertEquals(originalSessionId, ActiveWorkoutSessionManager.stableSessionId)
        assertEquals(SessionPhase.MAIN_WORKOUT, ActiveWorkoutSessionManager.currentPhase)
        assertEquals(1, ActiveWorkoutSessionManager.currentExerciseIndex)
        assertEquals(2, ActiveWorkoutSessionManager.currentSetIndex)
        assertEquals(3, ActiveWorkoutSessionManager.completedSetsByExercise["ex_1"])

        // Timer continues from where it was
        ActiveWorkoutSessionManager.tickTimer()
        assertEquals(46, ActiveWorkoutSessionManager.totalSecondsElapsed)
        assertEquals("00:46", ActiveWorkoutSessionManager.formattedTime)
    }

    /**
     * CASE E:
     * START EXERCISE -> complete workout
     * Expected:
     * - Existing completion, Progress, and workout history behavior remains unchanged
     * - WorkoutProgressService saves the exact startedAt timestamp and duration
     * - ActiveWorkoutSessionManager transitions to COMPLETED and timer stops
     */
    @Test
    fun testCaseE_startExercise_completeWorkout_persistsHistoryAndProgress() = runBlocking {
        val workoutId = "day_1"
        val programId = "hypertrophy"
        val workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout(programId, workoutId, ExperienceLevel.BEGINNER)

        ActiveWorkoutSessionManager.prepareSession(workoutId, programId, SessionPhase.WARM_UP)
        ActiveWorkoutSessionManager.startExercise(SessionPhase.WARM_UP)

        val recordedStart = ActiveWorkoutSessionManager.workoutStartedAt!!

        // Simulate 20 minutes (1200 seconds)
        repeat(1200) {
            ActiveWorkoutSessionManager.tickTimer()
        }
        assertEquals(1200, ActiveWorkoutSessionManager.totalSecondsElapsed)
        assertEquals("20:00", ActiveWorkoutSessionManager.formattedTime)

        // Persist session to history
        val durationMins = (ActiveWorkoutSessionManager.totalSecondsElapsed / 60).coerceAtLeast(1)
        val result = WorkoutProgressService.saveAndPersistWorkoutSession(
            workoutId = workout.id,
            dayNumber = workout.dayNumber,
            workoutTitle = workout.workoutTitle,
            targetMuscle = workout.focus.split(",").map { it.trim() },
            experienceLevel = UserSession.experienceLevel.id,
            startedAt = recordedStart,
            completedAt = com.example.fitness.getCurrentEpochMillis(),
            durationMinutes = durationMins,
            totalExercises = workout.exercises.size,
            completedExercises = workout.exercises.size,
            totalSets = workout.exercises.size * 3,
            completedSets = workout.exercises.size * 3,
            warmUpCompleted = true,
            mainWorkoutCompleted = true,
            coolDownCompleted = true,
            programId = programId,
            programName = "Hypertrophy",
            userId = "timer_test_user",
            exercises = emptyList(),
            sessionId = ActiveWorkoutSessionManager.stableSessionId!!
        )

        assertTrue(result.isSuccess)
        ActiveWorkoutSessionManager.completeWorkout()

        assertEquals(WorkoutExecutionState.COMPLETED, ActiveWorkoutSessionManager.executionState)
        assertEquals(SessionPhase.COMPLETE, ActiveWorkoutSessionManager.currentPhase)

        // Timer does NOT tick after completion
        ActiveWorkoutSessionManager.tickTimer()
        assertEquals(1200, ActiveWorkoutSessionManager.totalSecondsElapsed)

        // Verify history record persisted with accurate start time
        val savedSession = WorkoutProgressService.workoutHistory.find { it.sessionId == ActiveWorkoutSessionManager.stableSessionId }
        assertNotNull(savedSession)
        assertEquals(recordedStart, savedSession.startedAt)
        assertEquals(20, savedSession.durationMinutes)
        assertEquals(1, WorkoutProgressService.workoutHistory.size)
    }

    /**
     * BACK BUTTON / EXIT:
     * Before START EXERCISE:
     * - Workout is treated as not started
     * - No active timer/session running
     * - Clean exit resets session
     */
    @Test
    fun testBackButton_beforeStartExercise_cleanReset() {
        val workoutId = "day_1"
        val programId = "hypertrophy"

        ActiveWorkoutSessionManager.prepareSession(workoutId, programId, SessionPhase.WARM_UP)
        assertEquals(WorkoutExecutionState.NOT_STARTED, ActiveWorkoutSessionManager.executionState)

        // System back pressed before starting
        ActiveWorkoutSessionManager.resetSession()

        assertEquals(WorkoutExecutionState.NOT_STARTED, ActiveWorkoutSessionManager.executionState)
        assertEquals(0, ActiveWorkoutSessionManager.totalSecondsElapsed)
        assertNull(ActiveWorkoutSessionManager.workoutStartedAt)
        assertNull(ActiveWorkoutSessionManager.activeWorkoutId)
        assertTrue(WorkoutProgressService.workoutHistory.isEmpty(), "No history record should be created")
    }

    /**
     * TIMER PAUSE AND RESUME RULE:
     * IN_PROGRESS -> PAUSED (timer stops)
     * PAUSED -> IN_PROGRESS (timer resumes)
     */
    @Test
    fun testTimerPauseAndResumeRules() {
        ActiveWorkoutSessionManager.prepareSession("day_1", "hypertrophy", SessionPhase.WARM_UP)
        ActiveWorkoutSessionManager.startExercise(SessionPhase.WARM_UP)

        ActiveWorkoutSessionManager.tickTimer()
        ActiveWorkoutSessionManager.tickTimer()
        assertEquals(2, ActiveWorkoutSessionManager.totalSecondsElapsed)

        ActiveWorkoutSessionManager.pauseWorkout()
        assertEquals(WorkoutExecutionState.PAUSED, ActiveWorkoutSessionManager.executionState)

        // Timer does not tick while paused
        ActiveWorkoutSessionManager.tickTimer()
        assertEquals(2, ActiveWorkoutSessionManager.totalSecondsElapsed)

        ActiveWorkoutSessionManager.resumeWorkout()
        assertEquals(WorkoutExecutionState.IN_PROGRESS, ActiveWorkoutSessionManager.executionState)

        ActiveWorkoutSessionManager.tickTimer()
        assertEquals(3, ActiveWorkoutSessionManager.totalSecondsElapsed)
    }
}
