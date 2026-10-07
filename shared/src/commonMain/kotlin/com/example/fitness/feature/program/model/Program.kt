package com.example.fitness.feature.program.model

import kotlinx.serialization.Serializable

/**
 * Foundational Program entity representing a workout program definition stored in Firestore under programs/{programId}.
 * Serves as the single source of truth across the 30-Day Workout Plan, Hypertrophy Program,
 * Strength Program, and Athlete Performance Program.
 *
 * @property id Stable unique document ID (e.g., "thirty_day", "hypertrophy", "strength", "athlete_performance").
 * @property name Human-readable program display name (e.g., "30-Day Workout Plan").
 * @property slug URL/ID-safe slug for search and deep linking.
 * @property description Overview and high-level training philosophy.
 * @property type Category of program matching [ProgramType] (e.g., "THIRTY_DAY", "HYPERTROPHY", "STRENGTH", "ATHLETE_PERFORMANCE").
 * @property experienceLevel Target athlete level (e.g., "NO_EXPERIENCE", "BEGINNER", "ADVANCED", "EXPERT").
 * @property durationDays Total program calendar duration in days (e.g., 30 for 30-Day Plan).
 * @property trainingDaysPerWeek Scheduled workout sessions per 7-day microcycle (e.g., 5).
 * @property days Ordered list of [ProgramDay] defining the program structure sorted by dayNumber.
 * @property isActive Status flag; inactive programs are soft-archived and hidden from user catalog.
 * @property createdAt Epoch timestamp when the program was created.
 * @property updatedAt Epoch timestamp when the program was last modified.
 * @property createdBy Authenticated UID of the admin creator.
 * @property updatedBy Authenticated UID of the admin modifier.
 * @property version Monotonically increasing revision counter for optimistic concurrency and auditability.
 */
@Serializable
data class Program(
    val id: String = "",
    val name: String = "",
    val slug: String = "",
    val description: String = "",

    val type: String = "",
    val experienceLevel: String = "",

    val durationDays: Int = 0,
    val trainingDaysPerWeek: Int = 0,

    val days: List<ProgramDay> = emptyList(),

    val isActive: Boolean = true,

    val createdAt: Long? = null,
    val updatedAt: Long? = null,

    val createdBy: String = "",
    val updatedBy: String = "",

    val version: Int = 1,
    val gender: String = ""
)
