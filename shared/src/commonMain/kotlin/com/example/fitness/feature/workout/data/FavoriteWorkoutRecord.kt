package com.example.fitness.feature.workout.data

import kotlinx.serialization.Serializable

/**
 * Represents a user-favorited workout persisted under
 * users/{userId}/favorites/{workoutId}.
 */
@Serializable
data class FavoriteWorkoutRecord(
    val id: String = "",
    val workoutId: String = "",
    val programId: String = "",
    val title: String = "",
    val favoritedAt: Long = 0L
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to (id.ifBlank { workoutId }),
        "workoutId" to (workoutId.ifBlank { id }),
        "type" to "WORKOUT",
        "programId" to programId,
        "title" to title,
        "favoritedAt" to favoritedAt
    )
}
