package com.example.fitness.feature.workout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.fitness.getCurrentEpochMillis
import com.example.fitness.feature.workout.model.CustomExerciseConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class WorkoutExecutionState {
    NOT_STARTED,
    IN_PROGRESS,
    PAUSED,
    COMPLETED
}

object ActiveWorkoutSessionManager {
    private val sessionScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var timerJob: Job? = null

    var executionState by mutableStateOf(WorkoutExecutionState.NOT_STARTED)
        private set

    var activeWorkoutId by mutableStateOf<String?>(null)
        private set

    var activeProgramId by mutableStateOf<String?>(null)
        private set

    var stableSessionId by mutableStateOf<String?>(null)
        private set

    var workoutStartedAt by mutableStateOf<Long?>(null)
        private set

    var totalSecondsElapsed by mutableIntStateOf(0)
        private set

    var currentPhase by mutableStateOf(SessionPhase.WARM_UP)
    var currentExerciseIndex by mutableIntStateOf(0)
    var currentSetIndex by mutableIntStateOf(1)

    val completedSetsByExercise = mutableStateMapOf<String, Int>()
    val completedRepsByExercise = mutableStateMapOf<String, MutableList<Int>>()
    val customExerciseConfigs = mutableStateMapOf<String, CustomExerciseConfig>()

    val formattedTime: String
        get() {
            val minutes = totalSecondsElapsed / 60
            val seconds = totalSecondsElapsed % 60
            return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        }

    /**
     * Prepares or loads the workout session when navigating to the workout screen.
     * If an active session for the same workout is already IN_PROGRESS or PAUSED,
     * it preserves all state and does NOT restart the timer (Requirement 5 & Case D).
     * Otherwise, prepares a fresh NOT_STARTED session with timer at 00:00.
     */
    fun prepareSession(workoutId: String, programId: String, initialPhase: SessionPhase) {
        if (activeWorkoutId == workoutId &&
            activeProgramId == programId &&
            (executionState == WorkoutExecutionState.IN_PROGRESS || executionState == WorkoutExecutionState.PAUSED)
        ) {
            // Preserve existing running/paused session
            return
        }

        stopTimer()
        activeWorkoutId = workoutId
        activeProgramId = programId
        executionState = WorkoutExecutionState.NOT_STARTED
        workoutStartedAt = null
        totalSecondsElapsed = 0
        stableSessionId = "session_${getCurrentEpochMillis()}_${programId}_$workoutId"
        currentPhase = initialPhase
        currentExerciseIndex = 0
        currentSetIndex = 1
        completedSetsByExercise.clear()
        completedRepsByExercise.clear()
        customExerciseConfigs.clear()
    }

    /**
     * Triggered strictly when user taps "START EXERCISE".
     * Transitions NOT_STARTED -> IN_PROGRESS, records start timestamp,
     * begins timer, and starts the workout.
     */
    fun startExercise(initialPhase: SessionPhase = SessionPhase.WARM_UP) {
        if (executionState == WorkoutExecutionState.NOT_STARTED) {
            workoutStartedAt = getCurrentEpochMillis()
            executionState = WorkoutExecutionState.IN_PROGRESS
            if (currentPhase == SessionPhase.WARM_UP) {
                currentPhase = SessionPhase.MAIN_WORKOUT
            }
            startTimer()
        } else if (executionState == WorkoutExecutionState.PAUSED) {
            resumeWorkout()
        }
    }

    fun pauseWorkout() {
        if (executionState == WorkoutExecutionState.IN_PROGRESS) {
            executionState = WorkoutExecutionState.PAUSED
            stopTimer()
        }
    }

    fun resumeWorkout() {
        if (executionState == WorkoutExecutionState.PAUSED) {
            executionState = WorkoutExecutionState.IN_PROGRESS
            startTimer()
        }
    }

    fun tickTimer() {
        if (executionState == WorkoutExecutionState.IN_PROGRESS) {
            totalSecondsElapsed++
        }
    }

    fun completeWorkout() {
        executionState = WorkoutExecutionState.COMPLETED
        currentPhase = SessionPhase.COMPLETE
        stopTimer()
    }

    /**
     * Resets session (e.g. user pressed back before starting, or discarded session).
     */
    fun resetSession() {
        stopTimer()
        executionState = WorkoutExecutionState.NOT_STARTED
        activeWorkoutId = null
        activeProgramId = null
        workoutStartedAt = null
        totalSecondsElapsed = 0
        stableSessionId = null
        currentPhase = SessionPhase.WARM_UP
        currentExerciseIndex = 0
        currentSetIndex = 1
        completedSetsByExercise.clear()
        completedRepsByExercise.clear()
        customExerciseConfigs.clear()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = sessionScope.launch {
            while (executionState == WorkoutExecutionState.IN_PROGRESS) {
                delay(1000)
                if (executionState == WorkoutExecutionState.IN_PROGRESS) {
                    totalSecondsElapsed++
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }
}
