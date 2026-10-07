package com.example.fitness.feature.progress.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.progress.model.ExercisePerformanceHistoryItem
import com.example.fitness.feature.progress.model.ExercisePerformanceRecord
import com.example.fitness.feature.progress.model.WorkoutSessionRecord
import com.example.fitness.getCurrentDayOfWeekIndex
import com.example.fitness.getCurrentEpochMillis
import com.example.fitness.getCurrentWeekDayItems
import com.example.fitness.getCurrentYearMonthString
import com.example.fitness.getLocalDateString
import com.example.fitness.getRelativeDateDisplay
import com.example.fitness.platformLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout

enum class HistoryFilter {
    ALL,
    THIS_WEEK,
    THIS_MONTH
}

data class WorkoutWeeklyStats(
    val workoutsThisWeek: Int,
    val totalDurationMinutesThisWeek: Int,
    val exercisesCompletedThisWeek: Int,
    val setsCompletedThisWeek: Int
)

data class WorkoutMonthlyStats(
    val workoutsThisMonth: Int,
    val totalDurationMinutesThisMonth: Int,
    val exercisesCompletedThisMonth: Int,
    val setsCompletedThisMonth: Int,
    val consistencyPercentage: Int
)

/**
 * Detailed progress metrics for an active or completed training program.
 */
data class ProgramProgressStats(
    val programId: String,
    val programName: String,
    val currentDay: Int,
    val completedCount: Int,
    val remainingCount: Int,
    val totalDays: Int,
    val completionPercentage: Float,
    val isComplete: Boolean
)

object WorkoutProgressService {

    val workoutHistory = mutableStateListOf<WorkoutSessionRecord>()

    var historyRepository: WorkoutHistoryRepository = FirebaseWorkoutHistoryRepository()

    var isLoadingHistory by mutableStateOf(false)
    var lastLoadedUserId: String? = null
    var historyLoadError by mutableStateOf<String?>(null)

    // Process-level supervisor scope ensuring database writes survive screen disposal/navigation
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val syncMutex = Mutex()
    private val activeSyncJobs = com.example.fitness.createThreadSafeMap<String, Job>()

    /**
     * Resets local workout history state (primarily for testing and session clear).
     * Strictly does NOT delete persistent Firestore data.
     */
    fun clear() {
        try {
            serviceScope.coroutineContext.cancelChildren()
        } catch (_: Exception) {}
        workoutHistory.clear()
        lastLoadedUserId = null
        isLoadingHistory = false
        historyLoadError = null
    }

    /**
     * In-memory cache clearing alias for logout and session isolation.
     * Only clears volatile memory; does NOT delete remote Firestore documents.
     */
    fun clearCache() = clear()

    /**
     * Loads persistent workout history from Firestore for the specified authenticated user.
     * Merges remote records with any local pending offline records, deduplicates,
     * updates in-memory history, and restores UserSession streak and program progress.
     */
    suspend fun loadWorkoutHistory(userId: String, forceRefresh: Boolean = false): Result<List<WorkoutSessionRecord>> {
        val authUid = try { com.example.fitness.core.firebase.FitnessFirebase.auth.currentUser?.uid } catch (_: Exception) { null }
        val effectiveUid = userId.ifBlank { authUid ?: UserSession.currentUserId ?: "" }

        if (effectiveUid.isBlank()) {
            return Result.success(workoutHistory.toList())
        }

        if (!forceRefresh && (lastLoadedUserId == effectiveUid || isLoadingHistory)) {
            return Result.success(workoutHistory.toList())
        }

        isLoadingHistory = true
        historyLoadError = null
        platformLog("WorkoutHistoryDebug", "[PERSISTENCE_DIAGNOSTIC] LOAD_WORKOUT_HISTORY_START uid=$effectiveUid, forceRefresh=$forceRefresh")

        return try {
            val result = historyRepository.getWorkoutHistory(effectiveUid)
            if (result.isSuccess) {
                val remoteRecords = result.getOrNull() ?: emptyList()

                // Merge with any local pending offline records for this user
                val pendingRecords = workoutHistory.filter {
                    it.userId == effectiveUid && it.syncStatus != "synced"
                }

                val combined = mutableListOf<WorkoutSessionRecord>()
                val seenIds = mutableSetOf<String>()

                // Add pending records first
                for (p in pendingRecords) {
                    if (seenIds.add(p.sessionId)) {
                        combined.add(p)
                    }
                }


                // Add remote records
                for (r in remoteRecords) {
                    if (seenIds.add(r.sessionId)) {
                        combined.add(r)
                    }
                }

                // Sort newest first
                combined.sortByDescending { it.completedAt }

                workoutHistory.clear()
                workoutHistory.addAll(combined)
                lastLoadedUserId = effectiveUid // CRITICAL: Only mark as loaded on success!

                // Restore UserSession progress, completed days, streak, and metrics
                restoreUserSessionFromHistory(effectiveUid)

                println("[PERSISTENCE_DIAGNOSTIC] LOAD_WORKOUT_HISTORY_SUCCESS uid=$effectiveUid, remoteRecords=${remoteRecords.size}, combined=${combined.size}, streak=${UserSession.workoutStreak}, completedDays=${UserSession.completedDays.size}")

                Result.success(workoutHistory.toList())
            } else {
                val err = result.exceptionOrNull()?.message ?: "Failed to load workout history"
                historyLoadError = err
                // CRITICAL: DO NOT set lastLoadedUserId = effectiveUid on failure! Allow user/screens to retry.
                println("[PERSISTENCE_DIAGNOSTIC] LOAD_WORKOUT_HISTORY_FAILED uid=$effectiveUid, error=$err (lastLoadedUserId unchanged to allow retries)")
                // Even on remote fetch error, restore in-memory state if records exist
                restoreUserSessionFromHistory(effectiveUid)
                Result.failure(result.exceptionOrNull() ?: Exception(err))
            }
        } catch (e: Exception) {
            val err = e.message ?: "Failed to load workout history"
            historyLoadError = err
            // CRITICAL: DO NOT set lastLoadedUserId = effectiveUid on failure! Allow user/screens to retry.
            println("[PERSISTENCE_DIAGNOSTIC] LOAD_WORKOUT_HISTORY_EXCEPTION uid=$effectiveUid, error=$err (lastLoadedUserId unchanged to allow retries)")
            restoreUserSessionFromHistory(effectiveUid)
            Result.failure(e)
        } finally {
            isLoadingHistory = false
        }
    }

    /**
     * Authoritatively verifies whether a workout session was fully completed
     * based on actual recorded exercise performance records.
     * Partial sessions (e.g. only 1 of 5 exercises finished) return false.
     */
    fun isSessionFullyCompleted(session: WorkoutSessionRecord): Boolean {
        if (!session.completionStatus.equals("completed", ignoreCase = true)) {
            return false
        }
        val isRestDay = session.workoutTitle.contains("rest", ignoreCase = true) ||
                session.workoutType.contains("rest", ignoreCase = true)

        // Rest days / active recovery with 0 prescribed exercises
        if (session.prescribedExerciseCount == 0 && session.totalExercises == 0) {
            return isRestDay
        }
        if (session.mainWorkoutCompleted) {
            return true
        }
        // When granular exercise performance records exist, verify all were actually completed
        if (session.exercises.isNotEmpty()) {
            val actualCompleted = session.exercises.count { ex ->
                ex.completed && (if (ex.prescribedSets > 0) ex.completedSets >= ex.prescribedSets else ex.completedSets > 0)
            }
            val prescribed = if (session.prescribedExerciseCount > 0) session.prescribedExerciseCount else session.exercises.size
            return prescribed > 0 && actualCompleted >= prescribed
        }
        val prescribed = if (session.prescribedExerciseCount > 0) session.prescribedExerciseCount else session.totalExercises
        val completed = if (session.completedExerciseCount > 0) session.completedExerciseCount else session.completedExercises
        return prescribed > 0 && completed >= prescribed
    }

    /**
     * Reconstitutes UserSession state (streak, completed program days, total sets & reps)
     * strictly from the specified user's completed workout records.
     */
    fun restoreUserSessionFromHistory(userId: String) {
        val userSessions = if (userId.isBlank()) workoutHistory.toList() else workoutHistory.filter { it.userId == userId }

        // Clean slate in UserSession for the incoming user
        UserSession.resetProgramProgress()

        // Process chronological order (oldest to newest) to correctly advance currentDayNumber.
        // ONLY sessions that were ACTUALLY fully completed advance program day completion!
        val completedSessions = userSessions.filter { isSessionFullyCompleted(it) }.sortedBy { it.completedAt }
        for (session in completedSessions) {
            UserSession.markProgramDayCompleted(session.programId, session.dayNumber)
        }

        UserSession.totalSetsCompleted = userSessions.sumOf { it.completedSets }
        UserSession.totalRepsCompleted = userSessions.sumOf { it.completedSets * 10 }
        UserSession.workoutStreak = getCurrentStreak(userId)
    }

    /**
     * Retries syncing all pending or failed workout sessions for offline resilience.
     */
    suspend fun retryAllPendingSyncs(): Int {
        val pending = workoutHistory.filter { it.syncStatus != "synced" }
        var successCount = 0
        for (item in pending) {
            if (syncSessionToFirestore(item)) {
                successCount++
            }
        }
        return successCount
    }

    /**
     * Completes and persists a workout session, strictly awaiting the remote Firestore write
     * while keeping the persistence job anchored to the process-level serviceScope so it survives screen disposal.
     */
    suspend fun saveAndPersistWorkoutSession(
        workoutId: String,
        dayNumber: Int,
        workoutTitle: String,
        targetMuscle: List<String>,
        experienceLevel: String,
        startedAt: Long = getCurrentEpochMillis() - 2520000L,
        completedAt: Long = getCurrentEpochMillis(),
        durationMinutes: Int,
        totalExercises: Int,
        completedExercises: Int,
        totalSets: Int,
        completedSets: Int,
        warmUpCompleted: Boolean = true,
        mainWorkoutCompleted: Boolean = true,
        coolDownCompleted: Boolean = true,
        programId: String = "30-day",
        programName: String = "30-Day Workout Plan",
        userId: String = UserSession.currentUserId ?: "",
        exercises: List<ExercisePerformanceRecord> = emptyList(),
        totalVolume: Double = 0.0,
        sessionId: String? = null,
        scope: CoroutineScope? = null
    ): Result<WorkoutSessionRecord> {
        val localDate = getLocalDateString(completedAt)
        val displayDate = getRelativeDateDisplay(completedAt)
        val effectiveSessionId = sessionId ?: "session_${completedAt}_${programId}_$dayNumber"

        val authUid = try { com.example.fitness.core.firebase.FitnessFirebase.auth.currentUser?.uid } catch (_: Exception) { null }
        val effectiveUid = if (!authUid.isNullOrBlank()) authUid else userId.ifBlank { UserSession.currentUserId ?: "" }

        // Deduplication check: if identical session already successfully synced, return it
        val existing = workoutHistory.find {
            it.id == effectiveSessionId || it.sessionId == effectiveSessionId
        }
        if (existing != null && existing.syncStatus == "synced") {
            return Result.success(existing)
        }

        val actualCompletedExercises = if (exercises.isNotEmpty()) exercises.count { it.completed } else completedExercises
        val actualPrescribedExercises = if (exercises.isNotEmpty()) exercises.size else totalExercises
        val actualCompletedSets = if (exercises.isNotEmpty()) exercises.sumOf { it.completedSets } else completedSets
        val actualPrescribedSets = if (exercises.isNotEmpty()) exercises.sumOf { it.prescribedSets } else totalSets

        val isRestDay = workoutTitle.contains("rest", ignoreCase = true)
        val isFullyComplete = (totalExercises == 0 && exercises.isEmpty() && isRestDay) ||
            (actualPrescribedExercises > 0 && actualCompletedExercises >= actualPrescribedExercises)
        val calculatedStatus = if (isFullyComplete) "completed" else "partial"

        // 1. Build WorkoutSessionRecord
        val record = WorkoutSessionRecord(
            id = effectiveSessionId,
            sessionId = effectiveSessionId,
            userId = effectiveUid,
            workoutId = workoutId,
            dayNumber = dayNumber,
            workoutTitle = workoutTitle,
            targetMuscle = targetMuscle,
            experienceLevel = experienceLevel,
            startedAt = startedAt,
            completedAt = completedAt,
            durationMinutes = durationMinutes.coerceAtLeast(1),
            durationSeconds = durationMinutes.coerceAtLeast(1) * 60,
            totalExercises = actualPrescribedExercises,
            completedExercises = actualCompletedExercises,
            prescribedExerciseCount = actualPrescribedExercises,
            completedExerciseCount = actualCompletedExercises,
            totalSets = actualPrescribedSets,
            completedSets = actualCompletedSets,
            prescribedSetCount = actualPrescribedSets,
            completedSetCount = actualCompletedSets,
            warmUpCompleted = warmUpCompleted,
            mainWorkoutCompleted = mainWorkoutCompleted && isFullyComplete,
            coolDownCompleted = coolDownCompleted,
            completionStatus = calculatedStatus,
            localDateString = localDate,
            workoutDate = localDate,
            displayDate = displayDate,
            programId = programId,
            programName = programName,
            exercises = exercises,
            totalVolume = totalVolume,
            syncStatus = "pending_sync"
        )

        // Add locally first
        val existingIndex = workoutHistory.indexOfFirst { it.sessionId == effectiveSessionId }
        if (existingIndex != -1) {
            workoutHistory[existingIndex] = record
        } else {
            workoutHistory.add(0, record)
        }

        // 2. Persist the record via serviceScope (survives screen disposal / navigation)
        val persistDeferred = serviceScope.async {
            historyRepository.saveWorkoutSession(record)
        }
        syncMutex.withLock {
            activeSyncJobs[record.sessionId] = persistDeferred
        }

        val writeResult = try {
            persistDeferred.await()
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            syncMutex.withLock {
                activeSyncJobs.remove(record.sessionId)
            }
        }

        return if (writeResult.isSuccess) {
            val savedRecord = writeResult.getOrNull() ?: record.copy(syncStatus = "synced")
            val index = workoutHistory.indexOfFirst { it.sessionId == savedRecord.sessionId }
            if (index != -1) {
                workoutHistory[index] = savedRecord
            }

            // 3. Update progress: Only mark program day completed if the workout was ACTUALLY fully completed!
            if (isSessionFullyCompleted(savedRecord)) {
                UserSession.markProgramDayCompleted(programId, dayNumber)
                UserSession.workoutStreak = getCurrentStreak(effectiveUid)
            }
            UserSession.totalSetsCompleted += actualCompletedSets
            UserSession.totalRepsCompleted += (actualCompletedSets * 10)

            // 4. Trigger notifications
            try {
                com.example.fitness.feature.notifications.data.WorkoutNotificationService.processWorkoutCompletion(
                    savedRecord,
                    serviceScope
                )
            } catch (e: Exception) {
                platformLog("WorkoutHistoryDebug", "[DEBUG] Notification trigger error: ${e.message}")
            }

            Result.success(savedRecord)
        } else {
            val err = writeResult.exceptionOrNull() ?: IllegalStateException("Firestore persistence failed")
            platformLog("WorkoutHistoryDebug", "[WorkoutHistoryDebug] Persistence failed: ${err.message}")

            val failedRecord = record.copy(syncStatus = "failed_sync")
            val index = workoutHistory.indexOfFirst { it.sessionId == failedRecord.sessionId }
            if (index != -1) {
                workoutHistory[index] = failedRecord
            }

            // Still update user session progress for local offline usage (only if fully completed)
            if (isSessionFullyCompleted(failedRecord)) {
                UserSession.markProgramDayCompleted(programId, dayNumber)
                UserSession.workoutStreak = getCurrentStreak(effectiveUid)
            }
            UserSession.totalSetsCompleted += actualCompletedSets
            UserSession.totalRepsCompleted += (actualCompletedSets * 10)

            Result.failure(err)
        }
    }

    /**
     * Saves a completed workout session with full prescribed vs. actual exercise metrics.
     * Prevents duplicate completion records when called repeatedly with identical session parameters.
     */
    fun saveWorkoutSession(

        workoutId: String,
        dayNumber: Int,
        workoutTitle: String,
        targetMuscle: List<String>,
        experienceLevel: String,
        startedAt: Long = getCurrentEpochMillis() - 2520000L,
        completedAt: Long = getCurrentEpochMillis(),
        durationMinutes: Int,
        totalExercises: Int,
        completedExercises: Int,
        totalSets: Int,
        completedSets: Int,
        warmUpCompleted: Boolean = true,
        mainWorkoutCompleted: Boolean = true,
        coolDownCompleted: Boolean = true,
        programId: String = "30-day",
        programName: String = "30-Day Workout Plan",
        userId: String = UserSession.currentUserId ?: "",
        exercises: List<ExercisePerformanceRecord> = emptyList(),
        totalVolume: Double = 0.0,
        sessionId: String? = null,
        scope: CoroutineScope? = null
    ): WorkoutSessionRecord {
        val localDate = getLocalDateString(completedAt)
        val displayDate = getRelativeDateDisplay(completedAt)
        val effectiveSessionId = sessionId ?: "session_${completedAt}_${programId}_$dayNumber"

        // Deduplication guard: prevent duplicate recording on repeated Complete actions or double-tap
        val existing = workoutHistory.find {
            it.id == effectiveSessionId || it.sessionId == effectiveSessionId ||
                (it.userId == userId && it.programId == programId && it.dayNumber == dayNumber && it.workoutDate == localDate && kotlin.math.abs(it.completedAt - completedAt) < 60000L)
        }
        if (existing != null) {
            return existing
        }



        val actualCompletedExercises = if (exercises.isNotEmpty()) exercises.count { it.completed } else completedExercises
        val actualPrescribedExercises = if (exercises.isNotEmpty()) exercises.size else totalExercises
        val actualCompletedSets = if (exercises.isNotEmpty()) exercises.sumOf { it.completedSets } else completedSets
        val actualPrescribedSets = if (exercises.isNotEmpty()) exercises.sumOf { it.prescribedSets } else totalSets

        val isFullyComplete = (totalExercises == 0 && exercises.isEmpty()) ||
            (actualPrescribedExercises > 0 && actualCompletedExercises >= actualPrescribedExercises)
        val calculatedStatus = if (isFullyComplete) "completed" else "partial"

        val record = WorkoutSessionRecord(
            id = effectiveSessionId,
            sessionId = effectiveSessionId,
            userId = userId,
            workoutId = workoutId,
            dayNumber = dayNumber,
            workoutTitle = workoutTitle,
            targetMuscle = targetMuscle,
            experienceLevel = experienceLevel,
            startedAt = startedAt,
            completedAt = completedAt,
            durationMinutes = durationMinutes.coerceAtLeast(1),
            durationSeconds = durationMinutes.coerceAtLeast(1) * 60,
            totalExercises = actualPrescribedExercises,
            completedExercises = actualCompletedExercises,
            prescribedExerciseCount = actualPrescribedExercises,
            completedExerciseCount = actualCompletedExercises,
            totalSets = actualPrescribedSets,
            completedSets = actualCompletedSets,
            prescribedSetCount = actualPrescribedSets,
            completedSetCount = actualCompletedSets,
            warmUpCompleted = warmUpCompleted,
            mainWorkoutCompleted = mainWorkoutCompleted && isFullyComplete,
            coolDownCompleted = coolDownCompleted,
            completionStatus = calculatedStatus,
            localDateString = localDate,
            workoutDate = localDate,
            displayDate = displayDate,
            programId = programId,
            programName = programName,
            exercises = exercises,
            totalVolume = totalVolume,
            syncStatus = "pending_sync"
        )

        workoutHistory.add(0, record) // newest first

        // Update UserSession metrics only if the workout was ACTUALLY fully completed!
        if (isSessionFullyCompleted(record)) {
            UserSession.markProgramDayCompleted(programId, dayNumber)
            UserSession.workoutStreak = getCurrentStreak(userId)
        }
        UserSession.totalSetsCompleted += actualCompletedSets
        UserSession.totalRepsCompleted += (actualCompletedSets * 10)

        // Trigger smart workout notifications (completion, streak milestones, program milestones)
        try {
            com.example.fitness.feature.notifications.data.WorkoutNotificationService.processWorkoutCompletion(record, scope ?: serviceScope)
        } catch (e: Exception) {
            println("[DEBUG] Notification trigger error: ${e.message}")
        }

        // Reliable background persistence to Firestore via detached serviceScope.
        // Screen navigation/disposal will NOT cancel this persistent write.
        val authUid = try { com.example.fitness.core.firebase.FitnessFirebase.auth.currentUser?.uid } catch (_: Exception) { null }
        val targetUid = userId.ifBlank { authUid ?: UserSession.currentUserId ?: "" }

        if (targetUid.isNotBlank()) {
            val syncJob = serviceScope.launch {
                try {
                    syncSessionToFirestore(record.copy(userId = targetUid))
                } finally {
                    syncMutex.withLock {
                        activeSyncJobs.remove(record.sessionId)
                    }
                }
            }
            serviceScope.launch {
                syncMutex.withLock {
                    if (syncJob.isActive) {
                        activeSyncJobs[record.sessionId] = syncJob
                    }
                }
            }
        }

        return record
    }

    /**
     * Awaits completion of all currently in-flight workout session persistence jobs
     * to eliminate save/logout race conditions.
     */
    suspend fun awaitPendingSyncs(timeoutMillis: Long = 5000L): Boolean {
        val jobsToWait = syncMutex.withLock {
            activeSyncJobs.values.toList()
        }
        if (jobsToWait.isEmpty()) return true

        println("[PERSISTENCE_DIAGNOSTIC] AWAIT_PENDING_SYNCS_START inFlightJobs=${jobsToWait.size}, timeoutMillis=$timeoutMillis")

        return try {
            withTimeout(timeoutMillis) {
                jobsToWait.forEach { job ->
                    if (job.isActive) {
                        job.join()
                    }
                }
                println("[PERSISTENCE_DIAGNOSTIC] AWAIT_PENDING_SYNCS_SUCCESS all jobs completed")
                true
            }
        } catch (e: Exception) {
            println("[PERSISTENCE_DIAGNOSTIC] AWAIT_PENDING_SYNCS_TIMEOUT_OR_FAILED error=${e.message}")
            false
        }
    }

    suspend fun syncSessionToFirestore(record: WorkoutSessionRecord): Boolean {
        val authUid = try { com.example.fitness.core.firebase.FitnessFirebase.auth.currentUser?.uid } catch (_: Exception) { null }
        val effectiveUid = record.userId.ifBlank { authUid ?: UserSession.currentUserId ?: "" }

        val recordToSync = if (record.userId != effectiveUid) record.copy(userId = effectiveUid) else record

        return try {
            println("[PERSISTENCE_DIAGNOSTIC] SYNC_SESSION_START sessionId=${recordToSync.sessionId}, uid=$effectiveUid")
            val result = historyRepository.saveWorkoutSession(recordToSync)
            if (result.isSuccess) {
                val index = workoutHistory.indexOfFirst { it.sessionId == recordToSync.sessionId }
                if (index != -1) {
                    workoutHistory[index] = workoutHistory[index].copy(userId = effectiveUid, syncStatus = "synced")
                }
                println("[PERSISTENCE_DIAGNOSTIC] SYNC_SESSION_SUCCESS sessionId=${recordToSync.sessionId}, uid=$effectiveUid, syncStatus=synced")
                true
            } else {
                val index = workoutHistory.indexOfFirst { it.sessionId == recordToSync.sessionId }
                if (index != -1) {
                    workoutHistory[index] = workoutHistory[index].copy(syncStatus = "failed_sync")
                }
                println("[PERSISTENCE_DIAGNOSTIC] SYNC_SESSION_FAILED sessionId=${recordToSync.sessionId}, uid=$effectiveUid, error=${result.exceptionOrNull()?.message}")
                false
            }
        } catch (e: Exception) {
            val index = workoutHistory.indexOfFirst { it.sessionId == recordToSync.sessionId }
            if (index != -1) {
                workoutHistory[index] = workoutHistory[index].copy(syncStatus = "failed_sync")
            }
            println("[PERSISTENCE_DIAGNOSTIC] SYNC_SESSION_EXCEPTION sessionId=${recordToSync.sessionId}, uid=$effectiveUid, error=${e.message}")
            false
        }
    }

    suspend fun retryFailedSync(sessionId: String): Boolean {
        val record = workoutHistory.find { it.sessionId == sessionId } ?: return false
        return syncSessionToFirestore(record)
    }

    fun getWorkoutHistory(filter: HistoryFilter = HistoryFilter.ALL): List<WorkoutSessionRecord> {
        val currentYearMonth = getCurrentYearMonthString()

        return when (filter) {
            HistoryFilter.ALL -> workoutHistory
            HistoryFilter.THIS_WEEK -> {
                val currentWeekDayStrings = getWeeklyLocalDateStrings()
                workoutHistory.filter { currentWeekDayStrings.contains(it.localDateString) }
            }
            HistoryFilter.THIS_MONTH -> {
                workoutHistory.filter { it.localDateString.startsWith(currentYearMonth) }
            }
        }
    }

    fun getTotalWorkoutsCount(): Int = workoutHistory.size

    fun getCompletedWorkoutCount(): Int = workoutHistory.count { isSessionFullyCompleted(it) }

    fun getTotalWorkoutTimeMinutes(): Int = workoutHistory.sumOf { it.durationMinutes }

    fun getTotalWorkoutTimeFormatted(): String {
        val totalMinutes = getTotalWorkoutTimeMinutes()
        if (totalMinutes <= 0) return "0 min"
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    }

    fun getWorkoutsCountByProgram(programId: String): Int {
        val normalized = programId.lowercase().trim()
        return workoutHistory.count { it.programId.lowercase().trim() == normalized }
    }

    fun getProgramBreakdownStats(): Map<String, Int> {
        return mapOf(
            "30-day" to getWorkoutsCountByProgram("30-day"),
            "hypertrophy" to getWorkoutsCountByProgram("hypertrophy"),
            "strength" to getWorkoutsCountByProgram("strength"),
            "athlete" to getWorkoutsCountByProgram("athlete")
        )
    }

    /**
     * Calculates deterministic program progress including current day, completed count,
     * remaining workouts, and completion percentage.
     */
    fun getProgramProgressStats(
        programId: String,
        totalDays: Int = 30,
        trackCalendarDays: Boolean = false
    ): ProgramProgressStats {
        val normalized = when {
            programId.lowercase().trim().startsWith("female_") -> programId.lowercase().trim()
            programId.lowercase().trim() == "hypertrophy" -> "hypertrophy"
            programId.lowercase().trim() == "strength" -> "strength"
            programId.lowercase().trim() in listOf("athlete", "athlete_performance") -> "athlete"
            else -> "30-day"
        }

        val authUid = try { com.example.fitness.core.firebase.FitnessFirebase.auth.currentUser?.uid } catch (_: Exception) { null }
        val effectiveUid = authUid ?: UserSession.currentUserId ?: ""

        val userScopedHistory = if (effectiveUid.isNotBlank()) {
            workoutHistory.filter { it.userId == effectiveUid }
        } else {
            workoutHistory.toList()
        }

        val matchingSessions = userScopedHistory
            .filter {
                val p = it.programId.lowercase().trim()
                (p == normalized || (normalized == "athlete" && p == "athlete_performance")) &&
                    isSessionFullyCompleted(it)
            }

        val completedCount = if (trackCalendarDays) {
            matchingSessions
                .map { session ->
                    session.workoutDate.ifBlank {
                        session.localDateString.ifBlank {
                            if (session.completedAt > 0L) getLocalDateString(session.completedAt) else "day_${session.dayNumber}"
                        }
                    }
                }
                .filter { it.isNotBlank() }
                .distinct()
                .size
        } else {
            matchingSessions
                .map { it.dayNumber }
                .distinct()
                .size
        }

        val completedDaysForProgram = matchingSessions
            .map { it.dayNumber }
            .distinct()

        val remainingCount = (totalDays - completedCount).coerceAtLeast(0)
        val percentage = (completedCount.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)

        // Keep UserSession aligned with history
        completedDaysForProgram.forEach { dayNum ->
            UserSession.markProgramDayCompleted(normalized, dayNum)
        }

        val sessionCurrentDay = UserSession.getProgramCurrentDay(normalized)
        val derivedCurrentDay = if (completedCount > 0) {
            if (trackCalendarDays) {
                completedCount
            } else {
                val firstMissing = (1..totalDays).firstOrNull { !completedDaysForProgram.contains(it) } ?: totalDays
                if (firstMissing > 1) {
                    firstMissing
                } else {
                    val maxDay = completedDaysForProgram.maxOrNull() ?: 0
                    maxOf(completedCount + 1, (maxDay + 1).coerceAtMost(totalDays))
                }
            }
        } else {
            1
        }
        val currentDay = if (trackCalendarDays) {
            if (completedCount > 0) completedCount else 1
        } else {
            maxOf(sessionCurrentDay, derivedCurrentDay).coerceIn(1, totalDays)
        }
        val progName = when {
            normalized.startsWith("female_") -> com.example.fitness.feature.program.data.CanonicalFemaleProgramData.getById(normalized)?.name ?: "Female Program"
            normalized == "hypertrophy" -> "Hypertrophy"
            normalized == "strength" -> "Strength"
            normalized == "athlete" -> "Athlete Performance"
            else -> "30-Day Workout Plan"
        }

        return ProgramProgressStats(
            programId = normalized,
            programName = progName,
            currentDay = currentDay,
            completedCount = completedCount,
            remainingCount = remainingCount,
            totalDays = totalDays,
            completionPercentage = percentage,
            isComplete = completedCount >= totalDays
        )
    }

    /**
     * Extracts chronological exercise performance history for an exercise across all completed sessions.
     */
    fun getExercisePerformanceHistory(exerciseId: String): List<ExercisePerformanceHistoryItem> {
        val items = mutableListOf<ExercisePerformanceHistoryItem>()
        workoutHistory.forEach { session ->
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
        return items
    }

    /**
     * Deterministic streak calculation based on unique completed workout dates.
     * Multiple workouts on the same date count as 1 qualifying day.
     */
    fun getCurrentStreak(anchorEpochMillis: Long = getCurrentEpochMillis()): Int {
        return calculateStreak(workoutHistory, anchorEpochMillis)
    }

    /**
     * Deterministic streak calculation scoped to a specific user.
     */
    fun getCurrentStreak(userId: String, anchorEpochMillis: Long = getCurrentEpochMillis()): Int {
        val filtered = if (userId.isBlank()) workoutHistory else workoutHistory.filter { it.userId == userId }
        return calculateStreak(filtered, anchorEpochMillis)
    }

    private fun calculateStreak(history: List<WorkoutSessionRecord>, anchorEpochMillis: Long): Int {
        if (history.isEmpty()) return 0

        val uniqueDates = history
            .filter { isSessionFullyCompleted(it) }
            .map { it.localDateString }
            .distinct()
            .toSet()

        if (uniqueDates.isEmpty()) return 0

        val todayStr = getLocalDateString(anchorEpochMillis)
        val todayEpochDays = parseLocalDateToEpochDays(todayStr)
        if (todayEpochDays == null) {
            val yesterdayStr = getLocalDateString(anchorEpochMillis - 86400000L)
            val hasWorkoutToday = uniqueDates.contains(todayStr)
            val hasWorkoutYesterday = uniqueDates.contains(yesterdayStr)
            if (!hasWorkoutToday && !hasWorkoutYesterday) return 0
            var streak = 0
            var checkEpoch = if (hasWorkoutToday) anchorEpochMillis else anchorEpochMillis - 86400000L
            while (true) {
                val checkDateStr = getLocalDateString(checkEpoch)
                if (uniqueDates.contains(checkDateStr)) {
                    streak++
                    checkEpoch -= 86400000L
                } else {
                    break
                }
            }
            return streak
        }

        val yesterdayStr = formatEpochDaysToLocalDate(todayEpochDays - 1)
        val hasWorkoutToday = uniqueDates.contains(todayStr)
        val hasWorkoutYesterday = uniqueDates.contains(yesterdayStr)

        if (!hasWorkoutToday && !hasWorkoutYesterday) {
            return 0
        }

        var streak = 0
        var checkEpochDays = if (hasWorkoutToday) todayEpochDays else todayEpochDays - 1

        while (true) {
            val checkDateStr = formatEpochDaysToLocalDate(checkEpochDays)
            if (uniqueDates.contains(checkDateStr)) {
                streak++
                checkEpochDays--
            } else {
                break
            }
        }

        return streak
    }

    /**
     * Deterministically calculates the longest streak across all recorded completed sessions.
     * Multi-workout days count as 1 day; month and leap-year boundaries are fully respected.
     */
    fun getLongestStreak(): Int {
        val uniqueDays = workoutHistory
            .filter { isSessionFullyCompleted(it) }
            .map { it.localDateString }
            .distinct()
            .mapNotNull { parseLocalDateToEpochDays(it) }
            .distinct()
            .sorted()

        if (uniqueDays.isEmpty()) return 0
        if (uniqueDays.size == 1) return 1

        var maxStreak = 1
        var currentStreak = 1

        for (i in 1 until uniqueDays.size) {
            val prev = uniqueDays[i - 1]
            val curr = uniqueDays[i]

            if (curr - prev == 1L) {
                currentStreak++
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak
                }
            } else {
                currentStreak = 1
            }
        }

        return maxOf(maxStreak, getCurrentStreak())
    }

    /**
     * Converts a "YYYY-MM-DD" local date string into days since Unix epoch (1970-01-01)
     * using standard Gregorian civil day arithmetic.
     */
    fun parseLocalDateToEpochDays(dateStr: String): Long? {
        return try {
            val parts = dateStr.split("-").map { it.toInt() }
            if (parts.size != 3) return null
            var y = parts[0]
            val m = parts[1]
            val d = parts[2]
            y -= if (m <= 2) 1 else 0
            val era = (if (y >= 0) y else y - 399) / 400
            val yoe = y - era * 400
            val doy = (153 * (if (m > 2) m - 3 else m + 9) + 2) / 5 + d - 1
            val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
            era * 146097L + doe - 719468L
        } catch (_: Exception) {
            null
        }
    }

    fun getWeeklyStats(): WorkoutWeeklyStats {
        val weekDates = getWeeklyLocalDateStrings()
        val weekSessions = workoutHistory.filter { weekDates.contains(it.localDateString) }

        return WorkoutWeeklyStats(
            workoutsThisWeek = weekSessions.size,
            totalDurationMinutesThisWeek = weekSessions.sumOf { it.durationMinutes },
            exercisesCompletedThisWeek = weekSessions.sumOf { it.completedExercises },
            setsCompletedThisWeek = weekSessions.sumOf { it.completedSets }
        )
    }

    fun getMonthlyStats(): WorkoutMonthlyStats {
        val currentYearMonth = getCurrentYearMonthString()
        val monthSessions = workoutHistory.filter { it.localDateString.startsWith(currentYearMonth) }

        val uniqueDaysTrained = monthSessions.map { it.localDateString }.distinct().size
        val dayOfMonth = getCurrentWeekDayItems().find { it.isToday }?.dayOfMonth ?: 15
        val consistency = ((uniqueDaysTrained.toFloat() / dayOfMonth.coerceAtLeast(1)) * 100).toInt().coerceIn(0, 100)

        return WorkoutMonthlyStats(
            workoutsThisMonth = monthSessions.size,
            totalDurationMinutesThisMonth = monthSessions.sumOf { it.durationMinutes },
            exercisesCompletedThisMonth = monthSessions.sumOf { it.completedExercises },
            setsCompletedThisMonth = monthSessions.sumOf { it.completedSets },
            consistencyPercentage = consistency
        )
    }

    /**
     * Returns Monday-to-Sunday activity booleans for the current week based on actual completed sessions.
     */
    fun getWeeklyDayActivityStatus(): List<Boolean> {
        val weekDates = getWeeklyLocalDateStrings()
        val completedDatesSet = workoutHistory
            .filter { isSessionFullyCompleted(it) }
            .map { it.localDateString }
            .toSet()

        return weekDates.map { dateStr ->
            completedDatesSet.contains(dateStr)
        }
    }

    fun formatEpochDaysToLocalDate(epochDays: Long): String {
        var z = epochDays + 719468L
        val era = (if (z >= 0) z else z - 146096L) / 146097L
        val doe = z - era * 146097L
        val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
        var y = yoe + era * 400
        val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
        val mp = (5 * doy + 2) / 153
        val d = doy - (153 * mp + 2) / 5 + 1
        val m = mp + (if (mp < 10) 3 else -9)
        y += (if (m <= 2) 1 else 0)
        val yStr = y.toString().padStart(4, '0')
        val mStr = m.toString().padStart(2, '0')
        val dStr = d.toString().padStart(2, '0')
        return "$yStr-$mStr-$dStr"
    }

    private fun getWeeklyLocalDateStrings(): List<String> {
        val todayDow = getCurrentDayOfWeekIndex()
        val todayStr = getLocalDateString(getCurrentEpochMillis())
        val todayEpochDays = parseLocalDateToEpochDays(todayStr)
        if (todayEpochDays == null) {
            val todayMillis = getCurrentEpochMillis()
            return (0..6).map { dayIndex ->
                val offsetDays = dayIndex - todayDow
                val dayMillis = todayMillis + (offsetDays * 86400000L)
                getLocalDateString(dayMillis)
            }
        }

        return (0..6).map { dayIndex ->
            val offsetDays = (dayIndex - todayDow).toLong()
            formatEpochDaysToLocalDate(todayEpochDays + offsetDays)
        }
    }
}

