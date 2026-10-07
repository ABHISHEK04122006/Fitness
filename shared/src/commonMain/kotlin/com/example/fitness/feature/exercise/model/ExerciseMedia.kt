package com.example.fitness.feature.exercise.model

import kotlinx.serialization.Serializable

/**
 * Media demonstration metadata for an exercise.
 *
 * @property thumbnailUrl Static image / poster frame URL.
 * @property animationUrl Lottie or vector animation URL.
 * @property videoUrl High-definition video demonstration URL.
 * @property mediaType Type of media present: "NONE", "IMAGE", "ANIMATION", "VIDEO".
 */
@Serializable
data class ExerciseMedia(
    val thumbnailUrl: String = "",
    val animationUrl: String = "",
    val videoUrl: String = "",
    val mediaType: String = "NONE"
)
