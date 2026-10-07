package com.example.fitness.notifications

import android.content.Context
import com.example.fitness.core.auth.ActivityProvider
import com.example.fitness.core.firebase.FitnessFirebase
import com.example.fitness.core.session.UserSession
import com.google.firebase.messaging.FirebaseMessaging
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Manages Android device ID generation, FCM registration token retrieval,
 * and Firestore device document synchronization under users/{uid}/fcmTokens/{deviceId}
 * and users/{uid}/devices/{deviceId}.
 *
 * Implements strict security: never logs full FCM tokens or credentials.
 */
object FcmTokenManager {

    private const val PREFS_NAME = "fitness_fcm_prefs"
    private const val KEY_DEVICE_ID = "device_id"
    private const val KEY_CACHED_TOKEN = "cached_fcm_token"

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Gets current authenticated user ID from Firebase or UserSession.
     */
    fun getCurrentUserId(): String? {
        return try {
            Firebase.auth.currentUser?.uid ?: UserSession.currentUserId
        } catch (_: Exception) {
            UserSession.currentUserId
        }
    }

    /**
     * Helper to safely format truncated token for debug logging.
     * Never exposes full FCM tokens in logs or analytics.
     */
    fun safeTokenSnippet(token: String): String {
        return if (token.length > 10) {
            "${token.take(6)}...${token.takeLast(4)}"
        } else {
            "***"
        }
    }

    /**
     * Synchronizes device token for current user if authenticated.
     */
    fun syncTokenForCurrentUser(context: Context? = null) {
        val uid = getCurrentUserId()
        if (!uid.isNullOrBlank()) {
            val ctx = context ?: ActivityProvider.currentActivity?.applicationContext
            if (ctx != null) {
                syncToken(ctx, uid)
            }
        }
    }

    /**
     * Retrieves or generates a stable installation-scoped device identifier.
     * Persisted in SharedPreferences so it remains stable across app restarts.
     */
    fun getDeviceId(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existingId = prefs.getString(KEY_DEVICE_ID, null)
        if (!existingId.isNullOrBlank()) {
            return existingId
        }
        val newId = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_DEVICE_ID, newId).apply()
        return newId
    }

    /**
     * Retrieves the current FCM token from Firebase and synchronizes it to Firestore.
     */
    fun syncToken(context: Context, uid: String) {
        if (uid.isBlank()) return
        val appContext = context.applicationContext

        println("[FCM] Token registration started")

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                println("[WARN] [FCM] Failed to obtain FCM registration token: ${task.exception?.message}")
                return@addOnCompleteListener
            }
            val token = task.result ?: return@addOnCompleteListener
            if (token.isBlank()) return@addOnCompleteListener

            println("[FCM] Token registered: ${safeTokenSnippet(token)}")
            saveDeviceToken(appContext, uid, token)
        }
    }

    /**
     * Stores the device registration document under both:
     * - users/{uid}/fcmTokens/{deviceId} (Modern normalized subcollection)
     * - users/{uid}/devices/{deviceId} (Legacy compatibility)
     *
     * Note: Never logs sensitive raw tokens.
     */
    fun saveDeviceToken(context: Context, uid: String, token: String) {
        if (uid.isBlank() || token.isBlank()) return
        val appContext = context.applicationContext
        val deviceId = getDeviceId(appContext)

        // Cache token locally
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CACHED_TOKEN, token).apply()

        scope.launch {
            val now = System.currentTimeMillis()
            val data = mapOf(
                "token" to token,
                "platform" to "android",
                "updatedAt" to now,
                "appVersion" to "1.0",
                "deviceId" to deviceId
            )

            // 1. Attempt write to normalized fcmTokens subcollection (safely handle permission differences)
            try {
                FitnessFirebase.firestore
                    .collection("users")
                    .document(uid)
                    .collection("fcmTokens")
                    .document(deviceId)
                    .set(data, merge = true)
                println("[FCM] Token synced to fcmTokens")
            } catch (e: Exception) {
                println("[DEBUG] FcmTokenManager: fcmTokens subcollection sync notice: ${e.message}")
            }

            // 2. Write to primary devices subcollection (confirmed allowed by deployed Firestore rules)
            try {
                FitnessFirebase.firestore
                    .collection("users")
                    .document(uid)
                    .collection("devices")
                    .document(deviceId)
                    .set(data, merge = true)
                println("[FCM] Token successfully registered in devices subcollection")
            } catch (e: Exception) {
                println("[WARN] [FCM] failed to sync device token to devices: ${e.message}")
            }
        }
    }

    /**
     * Removes device documents from both fcmTokens and devices on logout
     * to safely isolate push notifications between user accounts.
     */
    fun clearDeviceToken(context: Context, uid: String) {
        if (uid.isBlank()) return
        val appContext = context.applicationContext
        val deviceId = getDeviceId(appContext)

        scope.launch {
            try {
                FitnessFirebase.firestore
                    .collection("users")
                    .document(uid)
                    .collection("fcmTokens")
                    .document(deviceId)
                    .delete()
            } catch (_: Exception) {}

            try {
                FitnessFirebase.firestore
                    .collection("users")
                    .document(uid)
                    .collection("devices")
                    .document(deviceId)
                    .delete()
                println("[FCM] Device token cleared for user $uid")
            } catch (e: Exception) {
                println("[DEBUG] FcmTokenManager: failed to delete device token on logout: ${e.message}")
            }
        }
    }
}
