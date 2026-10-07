package com.example.fitness.feature.notifications.data

import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.notifications.model.NotificationItem
import com.example.fitness.feature.progress.data.WorkoutProgressService
import com.example.fitness.feature.progress.model.WorkoutSessionRecord
import com.example.fitness.getCurrentEpochMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Service managing smart workout event notifications:
 * - WORKOUT_COMPLETED on every completed workout session
 * - STREAK_MILESTONE on reaching 3, 7, 14, or 30 days
 * - PROGRAM_MILESTONE on completing day 7, 14, 21, or 30 of a program
 *
 * Enforces strict deterministic deduplication to prevent duplicate alerts.
 * Compatible with Firebase Spark plan (client-side event generation, zero Cloud Functions).
 */
object WorkoutNotificationService {

    // Meaningful streak milestone thresholds
    val STREAK_MILESTONES = setOf(3, 7, 14, 30)

    // Meaningful program day milestone thresholds
    val PROGRAM_MILESTONES = setOf(7, 14, 21, 30)

    // In-memory store for offline fallback and fast retrieval: userId -> List<NotificationItem>
    private val localNotifications = mutableMapOf<String, MutableList<NotificationItem>>()

    // In-memory deduplication set: set of processed notification IDs
    private val processedNotificationIds = mutableSetOf<String>()

    private val notificationRepository = FirebaseNotificationRepository()

    /**
     * Processes a completed workout session and generates all applicable milestone notifications.
     * Returns the list of newly created notifications.
     */
    fun processWorkoutCompletion(
        session: WorkoutSessionRecord,
        scope: CoroutineScope? = null
    ): List<NotificationItem> {
        val userId = session.userId
        if (userId.isBlank()) return emptyList()

        val newlyCreated = mutableListOf<NotificationItem>()
        val userList = localNotifications.getOrPut(userId) { mutableListOf() }
        val now = getCurrentEpochMillis()

        // 1. WORKOUT_COMPLETED Notification
        val completionNotifId = "notif_complete_${session.sessionId}"
        if (!processedNotificationIds.contains(completionNotifId) && userList.none { it.id == completionNotifId }) {
            processedNotificationIds.add(completionNotifId)
            val completionItem = NotificationItem(
                id = completionNotifId,
                userId = userId,
                title = "Workout Complete",
                message = "${session.workoutTitle} completed. Keep going! 💪",
                type = NotificationItem.TYPE_WORKOUT_COMPLETED,
                read = false,
                createdAt = now,
                actionType = "WORKOUT",
                actionId = session.workoutId
            )
            userList.add(0, completionItem)
            newlyCreated.add(completionItem)
        }

        // 2. STREAK_MILESTONE Notification
        val currentStreak = WorkoutProgressService.getCurrentStreak(userId)
        if (STREAK_MILESTONES.contains(currentStreak)) {
            val streakNotifId = "notif_streak_${userId}_${currentStreak}_${session.workoutDate}"
            if (!processedNotificationIds.contains(streakNotifId) && userList.none { it.id == streakNotifId }) {
                processedNotificationIds.add(streakNotifId)
                val streakItem = NotificationItem(
                    id = streakNotifId,
                    userId = userId,
                    title = "$currentStreak Day Streak 🔥",
                    message = "You've completed workouts for $currentStreak consecutive days.",
                    type = NotificationItem.TYPE_STREAK_MILESTONE,
                    read = false,
                    createdAt = now,
                    actionType = "PROGRESS",
                    actionId = null
                )
                userList.add(0, streakItem)
                newlyCreated.add(streakItem)
            }
        }

        // 3. PROGRAM_MILESTONE Notification
        if (PROGRAM_MILESTONES.contains(session.dayNumber) && session.completionStatus.equals("completed", ignoreCase = true)) {
            val programNotifId = "notif_prog_${userId}_${session.programId}_day_${session.dayNumber}"
            if (!processedNotificationIds.contains(programNotifId) && userList.none { it.id == programNotifId }) {
                processedNotificationIds.add(programNotifId)
                val progItem = NotificationItem(
                    id = programNotifId,
                    userId = userId,
                    title = "Program Progress",
                    message = "You've completed Day ${session.dayNumber} of your program.",
                    type = NotificationItem.TYPE_PROGRAM_MILESTONE,
                    read = false,
                    createdAt = now,
                    actionType = "PROGRAM",
                    actionId = session.programId
                )
                userList.add(0, progItem)
                newlyCreated.add(progItem)
            }
        }

        // Update UserSession unread count
        UserSession.unreadNotificationCount = userList.count { !it.read }

        // Background asynchronous persistence to Firestore without blocking caller
        if (scope != null && newlyCreated.isNotEmpty()) {
            scope.launch {
                newlyCreated.forEach { item ->
                    try {
                        notificationRepository.saveNotificationIfAbsent(userId, item)
                    } catch (e: Exception) {
                        println("[DEBUG] WorkoutNotificationService sync error: ${e.message}")
                    }
                }
            }
        }

        return newlyCreated
    }

    /**
     * Gets all in-app notifications for [userId], newest first.
     */
    fun getNotifications(userId: String): List<NotificationItem> {
        return localNotifications[userId]?.sortedByDescending { it.createdAt } ?: emptyList()
    }

    /**
     * Marks a specific notification as read.
     */
    fun markAsRead(userId: String, notificationId: String, scope: CoroutineScope? = null): Boolean {
        val userList = localNotifications[userId] ?: return false
        val index = userList.indexOfFirst { it.id == notificationId }
        if (index != -1) {
            userList[index] = userList[index].copy(read = true)
            UserSession.unreadNotificationCount = userList.count { !it.read }

            if (scope != null) {
                scope.launch {
                    try {
                        notificationRepository.markAsRead(userId, notificationId)
                    } catch (e: Exception) {
                        println("[DEBUG] WorkoutNotificationService.markAsRead error: ${e.message}")
                    }
                }
            }
            return true
        }
        return false
    }

    /**
     * Marks all notifications for [userId] as read.
     */
    fun markAllAsRead(userId: String, scope: CoroutineScope? = null): Boolean {
        val userList = localNotifications[userId] ?: return false
        localNotifications[userId] = userList.map { it.copy(read = true) }.toMutableList()
        UserSession.unreadNotificationCount = 0

        if (scope != null) {
            scope.launch {
                try {
                    notificationRepository.markAllAsRead(userId)
                } catch (e: Exception) {
                    println("[DEBUG] WorkoutNotificationService.markAllAsRead error: ${e.message}")
                }
            }
        }
        return true
    }

    /**
     * Returns unread count for [userId].
     */
    fun getUnreadCount(userId: String): Int {
        return localNotifications[userId]?.count { !it.read } ?: 0
    }

    /**
     * Injects or creates a notification directly (e.g. for testing or external events).
     */
    fun addNotification(item: NotificationItem): Boolean {
        val userList = localNotifications.getOrPut(item.userId) { mutableListOf() }
        if (processedNotificationIds.contains(item.id) || userList.any { it.id == item.id }) {
            return false
        }
        processedNotificationIds.add(item.id)
        userList.add(0, item)
        UserSession.unreadNotificationCount = userList.count { !it.read }
        return true
    }

    /**
     * Clears all local state for testing and logout.
     */
    fun clear() {
        localNotifications.clear()
        processedNotificationIds.clear()
    }

    /**
     * In-memory cache clearing alias for logout and session isolation.
     */
    fun clearCache() = clear()
}
