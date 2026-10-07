package com.example.fitness.feature.notifications.model

import com.example.fitness.getCurrentEpochMillis

/**
 * Represents an in-app and future FCM push notification item.
 *
 * @param id Unique document ID in Firestore: users/{userId}/notifications/{id}
 * @param userId Firebase UID of the notification recipient
 * @param title Bold header text of the notification
 * @param message Descriptive body text
 * @param type Category/topic (e.g., WORKOUT_REMINDER, WORKOUT_COMPLETED, STREAK, PROGRESS, SYSTEM, GENERAL)
 * @param read Whether the user has marked or opened this notification
 * @param createdAt Epoch timestamp in milliseconds when the notification was created
 * @param actionType Optional payload category for deep linking (e.g., WORKOUT, PROGRAM, PROFILE)
 * @param actionId Optional identifier payload for deep linking (e.g., workoutId, programId)
 */
data class NotificationItem(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String = TYPE_GENERAL,
    val read: Boolean = false,
    val createdAt: Long = getCurrentEpochMillis(),
    val actionType: String? = null,
    val actionId: String? = null
) {
    companion object {
        const val TYPE_WORKOUT_REMINDER = "WORKOUT_REMINDER"
        const val TYPE_WORKOUT_COMPLETED = "WORKOUT_COMPLETED"
        const val TYPE_STREAK = "STREAK"
        const val TYPE_STREAK_MILESTONE = "STREAK_MILESTONE"
        const val TYPE_PROGRESS = "PROGRESS"
        const val TYPE_PROGRAM_MILESTONE = "PROGRAM_MILESTONE"
        const val TYPE_SYSTEM = "SYSTEM"
        const val TYPE_GENERAL = "GENERAL"
    }
}
