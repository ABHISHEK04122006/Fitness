package com.example.fitness.feature.admin.model

import com.example.fitness.feature.notifications.model.NotificationItem
import com.example.fitness.getCurrentEpochMillis

/**
 * Audience targets available to administrators in the notification composer.
 */
enum class AdminAudienceType(val label: String, val description: String) {
    SINGLE_USER("Single User", "Send directly to a specific registered athlete"),
    ALL_ATHLETES("All Athletes", "Broadcast to all registered athletes in the system"),
    GLOBAL_ANNOUNCEMENT("Global Announcement", "Publish to system announcements and all athlete feeds")
}

/**
 * Deep-link actions supported by the mobile notification handler.
 */
enum class AdminActionType(val label: String, val actionTypeKey: String?) {
    NONE("No Action", null),
    WORKOUT("Workout Overview", "WORKOUT"),
    PROGRAM("Program Detail", "PROGRAM"),
    PROFILE("Athlete Profile", "PROFILE")
}

/**
 * Request payload composed by the Admin Portal before dispatch.
 */
data class AdminNotificationRequest(
    val title: String,
    val message: String,
    val category: String = NotificationItem.TYPE_GENERAL,
    val audience: AdminAudienceType = AdminAudienceType.SINGLE_USER,
    val targetUid: String? = null,
    val targetName: String? = null,
    val targetEmail: String? = null,
    val targetUids: List<String> = emptyList(),
    val targetNames: List<String> = emptyList(),
    val actionType: String? = null,
    val actionId: String? = null
)

/**
 * Execution outcome returned by AdminNotificationRepository.
 */
data class AdminNotificationSendResult(
    val success: Boolean,
    val notificationId: String,
    val targetCount: Int = 1,
    val devicesFound: Int = 0,
    val messagesSent: Int = 0,
    val pushAccepted: Int = 0,
    val pushFailed: Int = 0,
    val status: String = "SUCCESS",
    val message: String? = null
)

/**
 * Persistent audit record stored in admin_notification_history/{notificationId}.
 */
data class AdminNotificationRecord(
    val id: String,
    val title: String,
    val message: String,
    val category: String = NotificationItem.TYPE_GENERAL,
    val audience: String = AdminAudienceType.SINGLE_USER.name,
    val targetUid: String? = null,
    val targetName: String? = null,
    val targetCount: Int = 1,
    val targetNames: List<String> = emptyList(),
    val actionType: String? = null,
    val actionId: String? = null,
    val createdAt: Long = getCurrentEpochMillis(),
    val createdBy: String,
    val status: String = "DELIVERED",
    val devicesFound: Int = 0,
    val messagesSent: Int = 0,
    val pushAccepted: Int = 0,
    val pushFailed: Int = 0,
    val pushStatus: String? = null,
    val statusMessage: String? = null
) {
    val categoryEmoji: String
        get() = when (category.uppercase().trim()) {
            NotificationItem.TYPE_WORKOUT_REMINDER, "WORKOUT" -> "💪"
            NotificationItem.TYPE_PROGRAM_MILESTONE, "PROGRAM_UPDATE" -> "🎯"
            NotificationItem.TYPE_SYSTEM, "SYSTEM_ANNOUNCEMENT" -> "⚙️"
            else -> "🔔"
        }

    val displayAudience: String
        get() = when {
            audience == "MULTI_USER" || (audience == AdminAudienceType.SINGLE_USER.name && targetCount > 1) -> {
                if (targetNames.isNotEmpty()) {
                    if (targetNames.size == 1 && targetCount == 1) {
                        "Single User: ${targetNames.first()}"
                    } else if (targetCount > 1) {
                        "${targetNames.first()} + ${targetCount - 1} others"
                    } else {
                        "Multiple Users • $targetCount recipients"
                    }
                } else {
                    "Multiple Users • $targetCount recipients"
                }
            }
            audience == AdminAudienceType.SINGLE_USER.name -> {
                val primaryName = targetName ?: targetNames.firstOrNull() ?: "Athlete"
                "Single User: $primaryName"
            }
            audience == AdminAudienceType.ALL_ATHLETES.name -> "All Athletes ($targetCount ${if (targetCount == 1) "athlete" else "athletes"})"
            audience == AdminAudienceType.GLOBAL_ANNOUNCEMENT.name -> "Global Broadcast ($targetCount ${if (targetCount == 1) "athlete" else "athletes"})"
            else -> audience
        }
}
