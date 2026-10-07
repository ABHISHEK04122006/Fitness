package com.example.fitness.feature.exercise.data

import com.example.fitness.core.coroutine.AppDispatchers
import com.example.fitness.core.firebase.FitnessFirebase
import com.example.fitness.getCurrentEpochMillis
import com.example.fitness.platformLog
import kotlinx.coroutines.withContext

/**
 * Repository for managing user exercise favorites/bookmarks in Firestore.
 * Persisted under users/{userId}/favorites/{exerciseId}.
 */
object FavoriteExerciseRepository {

    private val firestore get() = FitnessFirebase.firestore

    private fun favoritesCollection(userId: String) =
        firestore.collection("users").document(userId).collection("favorites")

    /**
     * Loads all favorited exercises for [userId].
     * Ignores workout favorites stored in the same subcollection.
     */
    suspend fun getFavorites(userId: String): Result<List<FavoriteExerciseRecord>> = withContext(AppDispatchers.IO) {
        if (userId.isBlank()) return@withContext Result.success(emptyList())

        try {
            val snapshot = kotlinx.coroutines.withTimeoutOrNull(5_000L) {
                favoritesCollection(userId).get()
            }
            if (snapshot == null) {
                platformLog("FavoriteExerciseRepo", "getFavorites TIMED OUT for uid=$userId after 5s")
                return@withContext Result.success(emptyList())
            }
            val favorites = snapshot.documents.mapNotNull { doc ->
                val type = try { doc.get<String>("type") } catch (_: Exception) { "" }
                val exId = try { doc.get<String>("exerciseId") } catch (_: Exception) { "" }
                val wId = try { doc.get<String>("workoutId") } catch (_: Exception) { "" }

                // Discard documents that are purely workout favorites
                if (type.equals("WORKOUT", ignoreCase = true) || (exId.isBlank() && wId.isNotBlank())) {
                    return@mapNotNull null
                }

                val id = if (exId.isNotBlank()) exId else doc.id
                val favoritedAt = try {
                    doc.get<Long>("favoritedAt")
                } catch (_: Exception) {
                    try { doc.get<Double>("favoritedAt").toLong() } catch (_: Exception) { 0L }
                }

                FavoriteExerciseRecord(
                    exerciseId = id,
                    favoritedAt = favoritedAt
                )
            }
            Result.success(favorites)
        } catch (e: Exception) {
            val exMsg = e.message ?: "Unknown Firestore error"
            platformLog("FavoriteExerciseRepo", "getFavorites FAILED for uid=$userId: $exMsg")
            Result.failure(e)
        }
    }

    /**
     * Checks if a specific exercise is favorited by [userId].
     */
    suspend fun isFavorite(userId: String, exerciseId: String): Result<Boolean> = withContext(AppDispatchers.IO) {
        if (userId.isBlank() || exerciseId.isBlank()) return@withContext Result.success(false)

        try {
            val doc = favoritesCollection(userId).document(exerciseId).get()
            Result.success(doc.exists)
        } catch (e: Exception) {
            val exMsg = e.message ?: "Unknown Firestore error"
            platformLog("FavoriteExerciseRepo", "isFavorite FAILED for uid=$userId, exerciseId=$exerciseId: $exMsg")
            Result.failure(e)
        }
    }

    /**
     * Adds or removes a favorite exercise document for [userId].
     * Uses record.toMap() to ensure safe cross-platform Firestore persistence.
     */
    suspend fun setFavorite(userId: String, exerciseId: String, isFavorite: Boolean): Result<Unit> = withContext(AppDispatchers.IO) {
        if (userId.isBlank()) {
            return@withContext Result.failure(IllegalStateException("User ID cannot be blank"))
        }
        if (exerciseId.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Exercise ID cannot be blank"))
        }

        try {
            val docRef = favoritesCollection(userId).document(exerciseId)
            if (isFavorite) {
                val record = FavoriteExerciseRecord(
                    exerciseId = exerciseId,
                    favoritedAt = getCurrentEpochMillis()
                )
                docRef.set(record.toMap())
            } else {
                docRef.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            val exMsg = e.message ?: "Unknown Firestore error"
            platformLog("FavoriteExerciseRepo", "setFavorite FAILED for uid=$userId, exerciseId=$exerciseId, isFavorite=$isFavorite: $exMsg")
            Result.failure(e)
        }
    }
}
