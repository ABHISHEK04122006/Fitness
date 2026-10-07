package com.example.fitness.feature.notifications.model

import kotlinx.serialization.Serializable

/**
 * Represents user-controlled workout reminder settings.
 *
 * @param userId Authenticated user identifier
 * @param workoutReminderEnabled Whether local daily workout reminders are active
 * @param workoutReminderHour Hour of day (0-23) in user's local device time
 * @param workoutReminderMinute Minute of hour (0-59) in user's local device time
 */
@Serializable
data class WorkoutReminderPreference(
    val userId: String = "",
    val workoutReminderEnabled: Boolean = false,
    val workoutReminderHour: Int = 8,
    val workoutReminderMinute: Int = 0
) {
    init {
        require(workoutReminderHour in 0..23) {
            "workoutReminderHour must be between 0 and 23, got $workoutReminderHour"
        }
        require(workoutReminderMinute in 0..59) {
            "workoutReminderMinute must be between 0 and 59, got $workoutReminderMinute"
        }
    }

    /**
     * Formats reminder time for user-facing display (e.g., "8:00 AM", "7:30 PM").
     */
    fun formattedTime(): String {
        val h = workoutReminderHour
        val m = workoutReminderMinute
        val amPm = if (h < 12) "AM" else "PM"
        val displayHour = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        val displayMin = if (m < 10) "0$m" else "$m"
        return "$displayHour:$displayMin $amPm"
    }

    fun toMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "workoutReminderEnabled" to workoutReminderEnabled,
        "workoutReminderHour" to workoutReminderHour,
        "workoutReminderMinute" to workoutReminderMinute
    )

    companion object {
        fun fromMap(userId: String, map: Map<String, Any?>): WorkoutReminderPreference {
            val enabled = map["workoutReminderEnabled"] as? Boolean ?: false
            val hour = (map["workoutReminderHour"] as? Number)?.toInt()?.coerceIn(0, 23) ?: 8
            val minute = (map["workoutReminderMinute"] as? Number)?.toInt()?.coerceIn(0, 59) ?: 0
            return WorkoutReminderPreference(
                userId = userId,
                workoutReminderEnabled = enabled,
                workoutReminderHour = hour,
                workoutReminderMinute = minute
            )
        }
    }
}
