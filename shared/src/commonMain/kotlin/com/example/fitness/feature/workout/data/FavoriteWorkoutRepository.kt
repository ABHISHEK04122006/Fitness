package com.example.fitness.feature.workout.data

import com.example.fitness.core.coroutine.AppDispatchers
import com.example.fitness.core.firebase.FitnessFirebase
import com.example.fitness.getCurrentEpochMillis
import com.example.fitness.platformLog
import kotlinx.coroutines.withContext

/**
 * Repository for persisting and managing user-specific workout favorites in Firestore.
 * Stored under users/{userId}/favorites/{workoutId}.
 */
object FavoriteWorkoutRepository {

    private val firestore get() = FitnessFirebase.firestore

    private fun favoritesCollection(userId: String) =
        firestore.collection("users").document(userId).collection("favorites")

    /**
     * Loads all favorite workout records for [userId].
     * Filters out exercise-only bookmarks stored in the same subcollection.
     */
    suspend fun getFavorites(userId: String): Result<List<FavoriteWorkoutRecord>> = withContext(AppDispatchers.IO) {
        if (userId.isBlank()) return@withContext Result.success(emptyList())

        try {
            val snapshot = kotlinx.coroutines.withTimeoutOrNull(5_000L) {
                favoritesCollection(userId).get()
            }
            if (snapshot == null) {
                platformLog("FavoriteWorkoutRepo", "getFavorites TIMED OUT for uid=$userId after 5s")
                return@withContext Result.success(emptyList())
            }
            val favorites = snapshot.documents.mapNotNull { doc ->
                val type = try { doc.get<String>("type") } catch (_: Exception) { "" }
                val wId = try { doc.get<String>("workoutId") } catch (_: Exception) { "" }
                val exId = try { doc.get<String>("exerciseId") } catch (_: Exception) { "" }

                // Discard documents that are purely exercise favorites
                if (type.equals("EXERCISE", ignoreCase = true) || (wId.isBlank() && exId.isNotBlank())) {
                    return@mapNotNull null
                }

                val docId = doc.id
                val id = if (wId.isNotBlank()) wId else docId
                val pId = try { doc.get<String>("programId") } catch (_: Exception) { "" }
                val title = try { doc.get<String>("title") } catch (_: Exception) { "" }
                val favoritedAt = try {
                    doc.get<Long>("favoritedAt")
                } catch (_: Exception) {
                    try { doc.get<Double>("favoritedAt").toLong() } catch (_: Exception) { 0L }
                }

                FavoriteWorkoutRecord(
                    id = id,
                    workoutId = id,
                    programId = pId,
                    title = title,
                    favoritedAt = favoritedAt
                )
            }
            Result.success(favorites)
        } catch (e: Exception) {
            val exMsg = e.message ?: "Unknown Firestore error"
            platformLog("FavoriteWorkoutRepo", "getFavorites FAILED for uid=$userId: $exMsg")
            Result.failure(e)
        }
    }

    /**
     * Checks if a specific workout is favorited by [userId].
     */
    suspend fun isFavorite(userId: String, workoutId: String): Result<Boolean> = withContext(AppDispatchers.IO) {
        if (userId.isBlank() || workoutId.isBlank()) return@withContext Result.success(false)

        try {
            val doc = favoritesCollection(userId).document(workoutId).get()
            Result.success(doc.exists)
        } catch (e: Exception) {
            val exMsg = e.message ?: "Unknown Firestore error"
            platformLog("FavoriteWorkoutRepo", "isFavorite FAILED for uid=$userId, workoutId=$workoutId: $exMsg")
            Result.failure(e)
        }
    }

    /**
     * Adds or removes a workout favorite document for [userId].
     * Uses record.toMap() to guarantee rock-solid cross-platform Firestore document creation.
     */
    suspend fun setFavorite(
        userId: String,
        workoutId: String,
        programId: String = "",
        title: String = "",
        isFavorite: Boolean
    ): Result<Unit> = withContext(AppDispatchers.IO) {
        if (userId.isBlank()) {
            return@withContext Result.failure(IllegalStateException("User ID cannot be blank"))
        }
        if (workoutId.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Workout ID cannot be blank"))
        }

        try {
            val docRef = favoritesCollection(userId).document(workoutId)
            if (isFavorite) {
                val record = FavoriteWorkoutRecord(
                    id = workoutId,
                    workoutId = workoutId,
                    programId = programId,
                    title = title,
                    favoritedAt = getCurrentEpochMillis()
                )
                docRef.set(record.toMap())
            } else {
                docRef.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            val exMsg = e.message ?: "Unknown Firestore error"
            platformLog("FavoriteWorkoutRepo", "setFavorite FAILED for uid=$userId, workoutId=$workoutId, isFavorite=$isFavorite: $exMsg")
            Result.failure(e)
        }
    }
}
