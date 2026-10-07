package com.example.fitness.feature.exercise.data

import kotlinx.serialization.Serializable

/**
 * Represents a bookmarked/favorited exercise document stored under
 * users/{userId}/favorites/{exerciseId}.
 */
@Serializable
data class FavoriteExerciseRecord(
    val exerciseId: String = "",
    val favoritedAt: Long = 0L
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to exerciseId,
        "exerciseId" to exerciseId,
        "type" to "EXERCISE",
        "favoritedAt" to favoritedAt
    )
}
