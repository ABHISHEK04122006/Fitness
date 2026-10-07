package com.example.fitness.feature.program.model

import com.example.fitness.feature.exercise.model.ExerciseReference
import kotlinx.serialization.Serializable

/**
 * Represents a single training, rest, or recovery session day within a [Program].
 * Preserves canonical [dayNumber] ordering to determine workout sequence.
 *
 * @property dayNumber 1-based canonical ordering of the day (e.g. 1 through 30 for 30-Day Plan).
 * @property title Descriptive session title (e.g. "Chest & Triceps", "Active Recovery").
 * @property workoutType Type of session: "TRAINING", "REST", "RECOVERY", "WARMUP", "COOLDOWN", "CARDIO", "MOBILITY".
 * @property focus Target anatomical or performance focus areas (e.g. ["Chest", "Triceps"]).
 * @property isRestDay Indicates whether the day is a designated rest day (normally contains no exercises).
 * @property exercises Ordered list of [ExerciseReference] prescriptions executed during this day.
 * @property notes Coaching guidance or program day instructions.
 */
@Serializable
data class ProgramDay(
    val dayNumber: Int = 0,
    val title: String = "",
    val workoutType: String = "TRAINING",
    val focus: List<String> = emptyList(),
    val isRestDay: Boolean = false,
    val exercises: List<ExerciseReference> = emptyList(),
    val notes: String = "",
    val warmUp: List<String> = emptyList(),
    val coolDown: List<String> = emptyList()
)
