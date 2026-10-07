package com.example.fitness.feature.notifications.data

import com.example.fitness.core.coroutine.AppDispatchers
import com.example.fitness.core.firebase.FitnessFirebase
import com.example.fitness.feature.notifications.model.NotificationItem
import com.example.fitness.getCurrentEpochMillis
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Shared repository managing Firestore operations for in-app and FCM notifications.
 * Scoped strictly to: users/{uid}/notifications/{notificationId}
 */
class FirebaseNotificationRepository {

    private val firestore get() = FitnessFirebase.firestore

    private fun notificationsCollection(uid: String) =
        firestore.collection("users").document(uid).collection("notifications")

    private fun parseNotificationDoc(doc: DocumentSnapshot, defaultUid: String): NotificationItem? {
        return try {
            val id = doc.id
            val userId = try { doc.get<String>("userId") } catch (_: Exception) { defaultUid }
            val title = try { doc.get<String>("title") } catch (_: Exception) { "" }
            val message = try { doc.get<String>("message") } catch (_: Exception) { "" }
            val type = try { doc.get<String>("type") } catch (_: Exception) { NotificationItem.TYPE_GENERAL }
            val read = try { doc.get<Boolean>("read") } catch (_: Exception) { false }
            val createdAt = try {
                doc.get<Long>("createdAt")
            } catch (_: Exception) {
                try {
                    doc.get<Double>("createdAt").toLong()
                } catch (_: Exception) {
                    getCurrentEpochMillis()
                }
            }
            val actionType = try { doc.get<String?>("actionType") } catch (_: Exception) { null }
            val actionId = try { doc.get<String?>("actionId") } catch (_: Exception) { null }

            NotificationItem(
                id = id,
                userId = userId,
                title = title,
                message = message,
                type = type,
                read = read,
                createdAt = createdAt,
                actionType = actionType,
                actionId = actionId
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Real-time Flow emitting list of notifications for the given user, sorted newest first.
     * Updates automatically whenever documents in users/{uid}/notifications change.
     */
    fun getNotificationsFlow(uid: String): Flow<List<NotificationItem>> {
        if (uid.isBlank()) return flowOf(emptyList())
        return try {
            notificationsCollection(uid).snapshots.map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    parseNotificationDoc(doc, uid)
                }.sortedByDescending { it.createdAt }
            }.flowOn(AppDispatchers.Default)
        } catch (e: Exception) {
            println("[DEBUG] FirebaseNotificationRepository.getNotificationsFlow error: ${e.message}")
            flowOf(emptyList())
        }
    }

    /**
     * Fetches all notifications for the given user, sorted descending by createdAt (newest first).
     */
    suspend fun getNotifications(uid: String): List<NotificationItem> = withContext(AppDispatchers.IO) {
        if (uid.isBlank()) return@withContext emptyList()
        try {
            val snapshot = notificationsCollection(uid).get()
            withContext(AppDispatchers.Default) {
                snapshot.documents.mapNotNull { doc ->
                    parseNotificationDoc(doc, uid)
                }.sortedByDescending { it.createdAt }
            }
        } catch (e: Exception) {
            println("[DEBUG] FirebaseNotificationRepository.getNotifications error: ${e.message}")
            emptyList()
        }
    }

    /**
     * Marks a specific notification as read in users/{uid}/notifications/{notificationId}.
     */
    suspend fun markAsRead(uid: String, notificationId: String): Boolean = withContext(AppDispatchers.IO) {
        if (uid.isBlank() || notificationId.isBlank()) return@withContext false
        try {
            notificationsCollection(uid).document(notificationId).set(
                data = mapOf("read" to true),
                merge = true
            )
            true
        } catch (e: Exception) {
            println("[DEBUG] FirebaseNotificationRepository.markAsRead error: ${e.message}")
            false
        }
    }

    /**
     * Marks all unread notifications for this user as read.
     */
    suspend fun markAllAsRead(uid: String): Boolean = withContext(AppDispatchers.IO) {
        if (uid.isBlank()) return@withContext false
        try {
            val snapshot = notificationsCollection(uid).get()
            var success = true
            snapshot.documents.forEach { doc ->
                val isRead = try { doc.get<Boolean>("read") } catch (_: Exception) { false }
                if (!isRead) {
                    try {
                        doc.reference.set(mapOf("read" to true), merge = true)
                    } catch (e: Exception) {
                        success = false
                    }
                }
            }
            success
        } catch (e: Exception) {
            println("[DEBUG] FirebaseNotificationRepository.markAllAsRead error: ${e.message}")
            false
        }
    }

    /**
     * Calculates the count of unread notifications for this user.
     */
    suspend fun getUnreadCount(uid: String): Int = withContext(AppDispatchers.IO) {
        if (uid.isBlank()) return@withContext 0
        try {
            val snapshot = kotlinx.coroutines.withTimeoutOrNull(5_000L) {
                notificationsCollection(uid).get()
            } ?: return@withContext 0
            withContext(AppDispatchers.Default) {
                snapshot.documents.count { doc ->
                    try {
                        !doc.get<Boolean>("read")
                    } catch (_: Exception) {
                        true
                    }
                }
            }
        } catch (e: Exception) {
            println("[DEBUG] FirebaseNotificationRepository.getUnreadCount error: ${e.message}")
            0
        }
    }

    /**
     * Checks if a notification document already exists in users/{uid}/notifications/{notificationId}.
     */
    suspend fun notificationExists(uid: String, notificationId: String): Boolean = withContext(AppDispatchers.IO) {
        if (uid.isBlank() || notificationId.isBlank()) return@withContext false
        try {
            val doc = notificationsCollection(uid).document(notificationId).get()
            doc.exists
        } catch (e: Exception) {
            println("[DEBUG] FirebaseNotificationRepository.notificationExists error: ${e.message}")
            false
        }
    }

    /**
     * Persists notification only if it does not already exist with this notificationId.
     * Prevents duplicate Firestore documents between backend Cloud Functions and FCM data payload delivery.
     * Returns true if newly created, false if already exists or on error.
     */
    suspend fun saveNotificationIfAbsent(uid: String, notification: NotificationItem): Boolean = withContext(AppDispatchers.IO) {
        if (uid.isBlank() || notification.id.isBlank()) return@withContext false
        try {
            val docRef = notificationsCollection(uid).document(notification.id)
            val snapshot = docRef.get()
            if (snapshot.exists) {
                // Already created by backend / Cloud Function; avoid duplicate write or overwriting read status
                false
            } else {
                val data = mutableMapOf<String, Any>(
                    "id" to notification.id,
                    "userId" to uid,
                    "title" to notification.title,
                    "message" to notification.message,
                    "type" to notification.type,
                    "read" to notification.read,
                    "createdAt" to notification.createdAt
                )
                if (notification.actionType != null) data["actionType"] = notification.actionType
                if (notification.actionId != null) data["actionId"] = notification.actionId
                docRef.set(data, merge = false)
                true
            }
        } catch (e: Exception) {
            println("[DEBUG] FirebaseNotificationRepository.saveNotificationIfAbsent error: ${e.message}")
            false
        }
    }

    /**
     * Creates or updates a notification in users/{uid}/notifications/{notification.id}.
     * Intended for testing, development, and eventual backend Cloud Function payload alignment.
     */
    suspend fun createNotification(uid: String, notification: NotificationItem): Boolean = withContext(AppDispatchers.IO) {
        if (uid.isBlank() || notification.id.isBlank()) return@withContext false
        try {
            val docRef = notificationsCollection(uid).document(notification.id)
            val data = mutableMapOf<String, Any>(
                "id" to notification.id,
                "userId" to uid,
                "title" to notification.title,
                "message" to notification.message,
                "type" to notification.type,
                "read" to notification.read,
                "createdAt" to notification.createdAt
            )
            if (notification.actionType != null) data["actionType"] = notification.actionType
            if (notification.actionId != null) data["actionId"] = notification.actionId
            docRef.set(data, merge = true)
            true
        } catch (e: Exception) {
            println("[DEBUG] FirebaseNotificationRepository.createNotification error: ${e.message}")
            false
        }
    }

    /**
     * Development/testing helper for creating a test notification from the authenticated client.
     * Temporary mechanism for Firebase Free Spark Plan development/testing.
     * Production notification generation moves to Cloud Functions when upgrading to Blaze plan.
     *
     * Validates:
     * 1. Requires authenticated user (Firebase.auth.currentUser).
     * 2. Confirms target UID strictly matches caller's authenticated UID (prevents cross-user creation).
     * 3. Generates unique deterministic notificationId.
     * 4. Persists document to users/{uid}/notifications/{notificationId} matching exact NotificationItem schema.
     * 5. Returns the created NotificationItem, or null on failure.
     */
    suspend fun createLocalTestNotification(
        uid: String,
        title: String = "Today's Workout",
        message: String = "Chest + Triceps is waiting for you. 💪",
        type: String = NotificationItem.TYPE_WORKOUT_REMINDER,
        actionType: String? = "WORKOUT",
        actionId: String? = "day_14"
    ): NotificationItem? {
        val currentAuthUid = try { Firebase.auth.currentUser?.uid } catch (_: Exception) { null }
        if (currentAuthUid.isNullOrBlank() || currentAuthUid != uid) {
            println("[ERROR] FirebaseNotificationRepository.createLocalTestNotification: caller is unauthenticated or UID mismatch.")
            return null
        }

        val notificationId = "notif_${getCurrentEpochMillis()}_${kotlin.random.Random.nextInt(1000, 9999)}"
        val now = getCurrentEpochMillis()

        val item = NotificationItem(
            id = notificationId,
            userId = uid,
            title = title.trim(),
            message = message.trim(),
            type = type,
            read = false,
            createdAt = now,
            actionType = actionType,
            actionId = actionId
        )

        val success = saveNotificationIfAbsent(uid, item)
        return if (success) {
            item
        } else {
            null
        }
    }
}
