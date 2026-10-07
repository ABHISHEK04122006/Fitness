package com.example.fitness.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.notifications.data.FirebaseNotificationRepository
import com.example.fitness.feature.notifications.model.NotificationItem
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Android FirebaseMessagingService handling FCM token refresh and incoming pushes.
 * Accurately parses ATHGYM data and notification payloads.
 * Strictly adheres to security standards: never logs full tokens or credentials.
 */
class FitnessFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "athgym_notifications"
        const val LEGACY_CHANNEL_ID = "fitness_notifications_channel"
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    /**
     * Called when a new FCM registration token is generated or refreshed.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val safeSnippet = FcmTokenManager.safeTokenSnippet(token)
        println("[FCM] Token refreshed: $safeSnippet")

        val currentUid = try { Firebase.auth.currentUser?.uid } catch (_: Exception) { null }
            ?: UserSession.currentUserId
        if (!currentUid.isNullOrBlank()) {
            FcmTokenManager.saveDeviceToken(this, currentUid, token)
        }
    }

    /**
     * Called when an incoming FCM message is received while the app is in foreground.
     * Note: Background messages containing notification payloads are rendered directly
     * by Android Google Play Services in the notification tray.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        println("[FCM] Message received: id=${remoteMessage.messageId}")

        // 1. Prioritize explicit data payload keys, fallback to notification payload
        val title = remoteMessage.data["title"]?.takeIf { it.isNotBlank() }
            ?: remoteMessage.notification?.title?.takeIf { it.isNotBlank() }
            ?: "ATHGYM"

        val message = remoteMessage.data["message"]?.takeIf { it.isNotBlank() }
            ?: remoteMessage.data["body"]?.takeIf { it.isNotBlank() }
            ?: remoteMessage.notification?.body?.takeIf { it.isNotBlank() }
            ?: ""

        // If notificationId is supplied in data payload, use it instead of generating a duplicate record
        val notificationId = remoteMessage.data["notificationId"]?.takeIf { it.isNotBlank() }
            ?: remoteMessage.data["notification_id"]?.takeIf { it.isNotBlank() }
            ?: remoteMessage.data["id"]?.takeIf { it.isNotBlank() }
            ?: remoteMessage.messageId?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()

        val actionType = remoteMessage.data["actionType"]?.takeIf { it.isNotBlank() }
        val actionId = remoteMessage.data["actionId"]?.takeIf { it.isNotBlank() }
        val type = remoteMessage.data["type"]?.takeIf { it.isNotBlank() } ?: NotificationItem.TYPE_GENERAL
        val navigateTo = remoteMessage.data["navigate_to"]?.takeIf { it.isNotBlank() }
            ?: remoteMessage.data["destination"]?.takeIf { it.isNotBlank() }
            ?: "notifications"

        // 2. Post Android heads-up system notification
        showSystemNotification(
            notificationId = notificationId,
            title = title,
            message = message,
            actionType = actionType,
            actionId = actionId,
            navigateTo = navigateTo
        )

        // 3. Persist to Firestore if user is authenticated and message has content.
        // saveNotificationIfAbsent guarantees that if Cloud Functions / trusted backend
        // already created this notificationId, no duplicate or overwrite occurs.
        val currentUid = try { Firebase.auth.currentUser?.uid } catch (_: Exception) { null }
            ?: UserSession.currentUserId
        if (!currentUid.isNullOrBlank() && (title.isNotBlank() || message.isNotBlank())) {
            serviceScope.launch {
                try {
                    val repo = FirebaseNotificationRepository()
                    val wasCreated = repo.saveNotificationIfAbsent(
                        uid = currentUid,
                        notification = NotificationItem(
                            id = notificationId,
                            userId = currentUid,
                            title = title,
                            message = message,
                            type = type,
                            read = false,
                            createdAt = System.currentTimeMillis(),
                            actionType = actionType,
                            actionId = actionId
                        )
                    )
                    if (wasCreated) {
                        UserSession.unreadNotificationCount += 1
                    }
                } catch (e: Exception) {
                    println("[DEBUG] FitnessFirebaseMessagingService: Firestore notification sync error: ${e.message}")
                }
            }
        }
    }

    private fun ensureNotificationChannel(notificationManager: NotificationManager) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel == null) {
                val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .build()

                val channel = android.app.NotificationChannel(
                    CHANNEL_ID,
                    "ATHGYM Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "ATHGYM announcements, workout reminders, and gym updates"
                    enableLights(true)
                    lightColor = android.graphics.Color.GREEN
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 250, 250, 250)
                    setSound(soundUri, audioAttributes)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private fun showSystemNotification(
        notificationId: String,
        title: String,
        message: String,
        actionType: String?,
        actionId: String?,
        navigateTo: String
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureNotificationChannel(notificationManager)

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: Intent()
        launchIntent.apply {
            setClassName(packageName, "com.example.fitness.MainActivity")
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", navigateTo)
            putExtra("destination", navigateTo)
            putExtra("notification_id", notificationId)
            putExtra("notificationId", notificationId)
            putExtra("action_type", actionType)
            putExtra("action_id", actionId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Prefer monochrome status bar icon
        val iconResId = resources.getIdentifier("ic_notification", "drawable", packageName)
        val smallIcon = if (iconResId != 0) iconResId else {
            if (applicationInfo.icon != 0) applicationInfo.icon else android.R.drawable.ic_dialog_info
        }

        val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setColor(0xFFA8FF3E.toInt()) // ATHGYM Neon Green
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setContentIntent(pendingIntent)

        notificationManager.notify(notificationId.hashCode(), notificationBuilder.build())
    }
}
