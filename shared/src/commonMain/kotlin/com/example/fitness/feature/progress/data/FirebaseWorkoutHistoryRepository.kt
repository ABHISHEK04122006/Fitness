package com.example.fitness.feature.progress.data

import com.example.fitness.core.firebase.FitnessFirebase
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.progress.model.ExercisePerformanceHistoryItem
import com.example.fitness.feature.progress.model.ExercisePerformanceRecord
import com.example.fitness.feature.progress.model.WorkoutSessionRecord
import com.example.fitness.core.coroutine.AppDispatchers
import com.example.fitness.platformLog
import dev.gitlive.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.withContext

/**
 * Production Firebase Firestore implementation of WorkoutHistoryRepository.
 * Strictly scoped to users/{userId}/workoutHistory/{sessionId}.
 * Employs typed kotlinx.serialization with GitLive Firestore.
 */
class FirebaseWorkoutHistoryRepository : WorkoutHistoryRepository {

    private val firestore get() = FitnessFirebase.firestore

    private fun historyCollection(userId: String) =
        firestore.collection("users").document(userId).collection("workoutHistory")

    override suspend fun saveWorkoutSession(session: WorkoutSessionRecord): Result<WorkoutSessionRecord> = withContext(AppDispatchers.IO) {
        val currentUser = try { FitnessFirebase.auth.currentUser } catch (e: Exception) { null }

        if (currentUser == null || currentUser.uid.isBlank()) {
            platformLog("WorkoutHistoryDebug", "[WorkoutHistoryDebug] AUTH USER NULL")
            val err = "Cannot persist workout history: Authenticated user is null or UID is blank"
            platformLog("WorkoutHistoryDebug", "[WorkoutHistoryDebug] Firestore write FAILED")
            platformLog("WorkoutHistoryDebug", "[WorkoutHistoryDebug] exception = IllegalStateException: $err")
            return@withContext Result.failure(IllegalStateException(err))
        }

        val authenticatedUid = currentUser.uid

        val stableSessionId = session.sessionId.ifBlank {
            session.id.ifBlank { "session_${com.example.fitness.getCurrentEpochMillis()}" }
        }

        val recordToSave = session.copy(
            id = stableSessionId,
            sessionId = stableSessionId,
            userId = authenticatedUid,
            syncStatus = "synced"
        )

        try {
            val docRef = historyCollection(authenticatedUid).document(stableSessionId)

            platformLog("WorkoutHistoryDebug", "[WorkoutHistoryDebug] attempting Firestore write")

            // Typed serialization via kotlinx.serialization in GitLive Firestore
            docRef.set(recordToSave)

            platformLog("WorkoutHistoryDebug", "[WorkoutHistoryDebug] Firestore write SUCCESS")
            Result.success(recordToSave)
        } catch (e: Exception) {
            val exClass = e::class.qualifiedName ?: e::class.simpleName ?: "Exception"
            val exMsg = e.message ?: "Unknown Firestore error"
            platformLog("WorkoutHistoryDebug", "[WorkoutHistoryDebug] Firestore write FAILED")
            platformLog("WorkoutHistoryDebug", "[WorkoutHistoryDebug] exception = $exClass: $exMsg")
            Result.failure(e)
        }
    }

    override suspend fun getWorkoutHistory(userId: String): Result<List<WorkoutSessionRecord>> = withContext(AppDispatchers.IO) {
        val currentUser = try { FitnessFirebase.auth.currentUser } catch (_: Exception) { null }
        val effectiveUid = if (currentUser != null && currentUser.uid.isNotBlank()) currentUser.uid else userId.ifBlank { UserSession.currentUserId ?: "" }

        if (effectiveUid.isBlank()) {
            platformLog("WorkoutHistoryDebug", "[WorkoutHistoryDebug] LOAD_SKIPPED reason=UID is blank")
            return@withContext Result.success(emptyList())
        }

        try {
            val snapshot = kotlinx.coroutines.withTimeoutOrNull(8_000L) {
                historyCollection(effectiveUid).get()
            }
            if (snapshot == null) {
                platformLog("WorkoutHistoryDebug", "[WorkoutHistoryDebug] FIRESTORE_READ_TIMEOUT uid=$effectiveUid after 8s")
                return@withContext Result.success(emptyList())
            }
            val records = withContext(AppDispatchers.Default) {
                snapshot.documents.mapNotNull { doc ->
                    parseWorkoutSessionDoc(doc, effectiveUid)
                }.sortedByDescending { it.completedAt }
            }

            Result.success(records)
        } catch (e: Exception) {
            val exClass = e::class.qualifiedName ?: e::class.simpleName ?: "Exception"
            val exMsg = e.message ?: "Unknown error"
            platformLog("WorkoutHistoryDebug", "[WorkoutHistoryDebug] FIRESTORE_READ_FAILED uid=$effectiveUid, exception=$exClass: $exMsg")
            Result.failure(e)
        }
    }

    override suspend fun getWorkoutSession(userId: String, sessionId: String): Result<WorkoutSessionRecord?> = withContext(AppDispatchers.IO) {
        val authUid = try { FitnessFirebase.auth.currentUser?.uid } catch (_: Exception) { null }
        val effectiveUid = if (!authUid.isNullOrBlank()) authUid else userId.ifBlank { UserSession.currentUserId ?: "" }

        if (effectiveUid.isBlank() || sessionId.isBlank()) return@withContext Result.success(null)

        try {
            val doc = historyCollection(effectiveUid).document(sessionId).get()
            if (!doc.exists) {
                Result.success(null)
            } else {
                val record = withContext(AppDispatchers.Default) {
                    parseWorkoutSessionDoc(doc, effectiveUid)
                }
                Result.success(record)
            }
        } catch (e: Exception) {
            println("[PERSISTENCE_DIAGNOSTIC] FIRESTORE_READ_SESSION_FAILED uid=$effectiveUid, sessionId=$sessionId, error=${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getExerciseHistory(userId: String, exerciseId: String): Result<List<ExercisePerformanceHistoryItem>> = withContext(AppDispatchers.Default) {
        val historyResult = getWorkoutHistory(userId)
        if (historyResult.isFailure) return@withContext Result.failure(historyResult.exceptionOrNull()!!)

        val sessions = historyResult.getOrNull() ?: emptyList()
        val items = mutableListOf<ExercisePerformanceHistoryItem>()

        sessions.forEach { session ->
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

        Result.success(items)
    }

    private fun parseWorkoutSessionDoc(doc: DocumentSnapshot, defaultUserId: String): WorkoutSessionRecord? {
        if (!doc.exists) return null

        // 1. Primary path: GitLive typed data deserialization
        try {
            val parsed = doc.data<WorkoutSessionRecord>()
            return sanitizeAndReconcileSessionRecord(parsed, doc.id, defaultUserId)
        } catch (e: Exception) {
            println("[PERSISTENCE_DIAGNOSTIC] TYPED_PARSE_FALLBACK docId=${doc.id}, notice=${e.message}")
        }

        // 2. Resilient fallback path
        return try {
            val sessionId = try { doc.get<String>("sessionId") } catch (_: Exception) { doc.id }
            val userId = try { doc.get<String>("userId") } catch (_: Exception) { defaultUserId }
            val workoutId = try { doc.get<String>("workoutId") } catch (_: Exception) { "" }
            val dayNumber = try { doc.get<Int>("dayNumber") } catch (_: Exception) { 1 }
            val workoutTitle = try { doc.get<String>("workoutTitle") } catch (_: Exception) { "Workout" }
            val targetMuscle = try { doc.get<List<String>>("targetMuscle") } catch (_: Exception) { emptyList() }
            val experienceLevel = try { doc.get<String>("experienceLevel") } catch (_: Exception) { "beginner" }
            val startedAt = try { doc.get<Long>("startedAt") } catch (_: Exception) { 0L }
            val completedAt = try { doc.get<Long>("completedAt") } catch (_: Exception) { 0L }
            val durationMinutes = try { doc.get<Int>("durationMinutes") } catch (_: Exception) { 1 }
            val totalExercises = try { doc.get<Int>("totalExercises") } catch (_: Exception) { 0 }
            val completedExercises = try { doc.get<Int>("completedExercises") } catch (_: Exception) { 0 }
            val totalSets = try { doc.get<Int>("totalSets") } catch (_: Exception) { 0 }
            val completedSets = try { doc.get<Int>("completedSets") } catch (_: Exception) { 0 }
            val warmUpCompleted = try { doc.get<Boolean>("warmUpCompleted") } catch (_: Exception) { true }
            val mainWorkoutCompleted = try { doc.get<Boolean>("mainWorkoutCompleted") } catch (_: Exception) { false }
            val coolDownCompleted = try { doc.get<Boolean>("coolDownCompleted") } catch (_: Exception) { true }
            val completionStatus = try { doc.get<String>("completionStatus") } catch (_: Exception) { "partial" }
            val localDateString = try { doc.get<String>("localDateString") } catch (_: Exception) { "" }
            val displayDate = try { doc.get<String>("displayDate") } catch (_: Exception) { "" }
            val programId = try { doc.get<String>("programId") } catch (_: Exception) { "30-day" }
            val programName = try { doc.get<String>("programName") } catch (_: Exception) { "30-Day Workout Plan" }
            val workoutType = try { doc.get<String>("workoutType") } catch (_: Exception) { "Strength" }
            val durationSeconds = try { doc.get<Int>("durationSeconds") } catch (_: Exception) { durationMinutes * 60 }
            val prescribedExerciseCount = try { doc.get<Int>("prescribedExerciseCount") } catch (_: Exception) { totalExercises }
            val completedExerciseCount = try { doc.get<Int>("completedExerciseCount") } catch (_: Exception) { completedExercises }
            val prescribedSetCount = try { doc.get<Int>("prescribedSetCount") } catch (_: Exception) { totalSets }
            val completedSetCount = try { doc.get<Int>("completedSetCount") } catch (_: Exception) { completedSets }
            val totalVolume = try { doc.get<Double>("totalVolume") } catch (_: Exception) { 0.0 }

            val parsedExercises = try {
                doc.get<List<ExercisePerformanceRecord>>("exercises")
            } catch (_: Exception) {
                emptyList()
            }

            val rawRecord = WorkoutSessionRecord(
                id = sessionId,
                workoutId = workoutId,
                dayNumber = dayNumber,
                workoutTitle = workoutTitle,
                targetMuscle = targetMuscle,
                experienceLevel = experienceLevel,
                startedAt = startedAt,
                completedAt = completedAt,
                durationMinutes = durationMinutes,
                totalExercises = totalExercises,
                completedExercises = completedExercises,
                totalSets = totalSets,
                completedSets = completedSets,
                warmUpCompleted = warmUpCompleted,
                mainWorkoutCompleted = mainWorkoutCompleted,
                coolDownCompleted = coolDownCompleted,
                completionStatus = completionStatus,
                localDateString = localDateString,
                displayDate = displayDate,
                programId = programId,
                programName = programName,
                sessionId = sessionId,
                userId = userId,
                workoutDate = localDateString,
                workoutType = workoutType,
                durationSeconds = durationSeconds,
                prescribedExerciseCount = prescribedExerciseCount,
                completedExerciseCount = completedExerciseCount,
                prescribedSetCount = prescribedSetCount,
                completedSetCount = completedSetCount,
                totalVolume = totalVolume,
                exercises = parsedExercises,
                syncStatus = "synced"
            )
            sanitizeAndReconcileSessionRecord(rawRecord, doc.id, defaultUserId)
        } catch (e: Exception) {
            println("[PERSISTENCE_DIAGNOSTIC] PARSE_FAILED docId=${doc.id}, error=${e.message}")
            null
        }
    }

    internal companion object {
        fun sanitizeAndReconcileSessionRecord(
            record: WorkoutSessionRecord,
            fallbackId: String = "",
            defaultUserId: String = ""
        ): WorkoutSessionRecord {
            val sanitizedExercises = record.exercises.map { ex ->
                val actuallyCompleted = ex.completed && (if (ex.prescribedSets > 0) ex.completedSets >= ex.prescribedSets else ex.completedSets > 0)
                ex.copy(completed = actuallyCompleted)
            }

            val actualCompletedExercises = if (sanitizedExercises.isNotEmpty()) {
                sanitizedExercises.count { it.completed }
            } else {
                record.completedExerciseCount.coerceAtLeast(0)
            }

            val actualPrescribedExercises = if (sanitizedExercises.isNotEmpty()) {
                sanitizedExercises.size
            } else {
                if (record.prescribedExerciseCount > 0) record.prescribedExerciseCount else record.totalExercises
            }

            val actualCompletedSets = if (sanitizedExercises.isNotEmpty()) {
                sanitizedExercises.sumOf { it.completedSets }
            } else {
                if (record.completedSetCount > 0) record.completedSetCount else record.completedSets
            }

            val actualPrescribedSets = if (sanitizedExercises.isNotEmpty()) {
                sanitizedExercises.sumOf { it.prescribedSets }
            } else {
                if (record.prescribedSetCount > 0) record.prescribedSetCount else record.totalSets
            }

            val isRestDay = record.workoutTitle.contains("rest", ignoreCase = true) ||
                    (actualPrescribedExercises == 0 && (record.workoutType.contains("rest", ignoreCase = true) || record.workoutTitle.contains("rest", ignoreCase = true)))

            val effectiveCompletionStatus = when {
                isRestDay -> record.completionStatus.ifBlank { "completed" }
                actualPrescribedExercises > 0 && actualCompletedExercises >= actualPrescribedExercises -> "completed"
                actualCompletedExercises > 0 -> "partial"
                else -> if (record.completionStatus == "completed" && actualPrescribedExercises > 0) "partial" else record.completionStatus.ifBlank { "partial" }
            }

            val effectiveMainWorkoutCompleted = when {
                isRestDay -> true
                actualPrescribedExercises > 0 -> actualCompletedExercises >= actualPrescribedExercises
                else -> false
            }

            val effectiveId = record.id.ifBlank { fallbackId }
            val effectiveSessionId = record.sessionId.ifBlank { effectiveId }
            val effectiveUserId = record.userId.ifBlank { defaultUserId }

            return record.copy(
                id = effectiveId,
                sessionId = effectiveSessionId,
                userId = effectiveUserId,
                totalExercises = actualPrescribedExercises,
                completedExercises = actualCompletedExercises,
                prescribedExerciseCount = actualPrescribedExercises,
                completedExerciseCount = actualCompletedExercises,
                totalSets = actualPrescribedSets,
                completedSets = actualCompletedSets,
                prescribedSetCount = actualPrescribedSets,
                completedSetCount = actualCompletedSets,
                mainWorkoutCompleted = effectiveMainWorkoutCompleted,
                completionStatus = effectiveCompletionStatus,
                exercises = sanitizedExercises,
                syncStatus = "synced"
            )
        }
    }
}
