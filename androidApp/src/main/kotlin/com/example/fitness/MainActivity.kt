package com.example.fitness

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.fitness.core.auth.ActivityProvider
import com.example.fitness.core.firebase.initializeFirebase
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.notifications.FcmTokenManager
import com.example.fitness.notifications.FitnessFirebaseMessagingService

import kotlin.coroutines.resume

class MainActivity : ComponentActivity() {

    private var googleSignInContinuation: kotlin.coroutines.Continuation<Intent?>? = null

    private val googleSignInFallbackLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val cont = googleSignInContinuation
        googleSignInContinuation = null
        cont?.resume(result.data)
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        FcmTokenManager.syncTokenForCurrentUser(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ActivityProvider.currentActivity = this
        ActivityProvider.googleSignInLauncher = { intent ->
            kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
                googleSignInContinuation = continuation
                continuation.invokeOnCancellation {
                    googleSignInContinuation = null
                }
                googleSignInFallbackLauncher.launch(intent)
            }
        }
        initializeFirebase(applicationContext)

        createNotificationChannel()
        handleNotificationIntent(intent)

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        ActivityProvider.currentActivity = this
    }

    override fun onResume() {
        super.onResume()
        ActivityProvider.currentActivity = this

        val currentUid = FcmTokenManager.getCurrentUserId()
        if (!currentUid.isNullOrBlank()) {
            requestNotificationPermissionIfAppropriate()
            FcmTokenManager.syncToken(this, currentUid)
        }
    }

    override fun onDestroy() {
        if (ActivityProvider.currentActivity === this) {
            ActivityProvider.currentActivity = null
            ActivityProvider.googleSignInLauncher = null
        }
        super.onDestroy()
    }

    /**
     * Parses notification intent extras to route directly to NotificationScreen.
     */
    private fun handleNotificationIntent(intent: Intent?) {
        if (intent == null) return
        val navigateTo = intent.getStringExtra("navigate_to")
            ?: intent.getStringExtra("destination")
        val hasFcmPayload = intent.hasExtra("google.message_id") ||
                intent.hasExtra("gcm.message_id") ||
                intent.hasExtra("notification_id") ||
                intent.hasExtra("notificationId")
        if (navigateTo == "notifications" || hasFcmPayload) {
            println("[FCM] Notification opened: pendingScreen=Notifications")
            UserSession.pendingScreen = Screen.Notifications
        }
    }

    /**
     * Requests POST_NOTIFICATIONS permission on Android 13+ in a non-intrusive manner.
     */
    private fun requestNotificationPermissionIfAppropriate() {
        val currentUid = FcmTokenManager.getCurrentUserId()
        if (!currentUid.isNullOrBlank()) {
            FcmTokenManager.syncToken(this, currentUid)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                val prefs = getSharedPreferences("fitness_fcm_prefs", Context.MODE_PRIVATE)
                val hasPrompted = prefs.getBoolean("has_prompted_permission", false)
                if (!hasPrompted) {
                    prefs.edit().putBoolean("has_prompted_permission", true).apply()
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    /**
     * Creates the Android notification channel for ATHGYM with heads-up sound & vibration.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                .build()

            // Primary ATHGYM Notifications channel (HIGH importance for heads-up WhatsApp-style display)
            val athgymChannel = NotificationChannel(
                FitnessFirebaseMessagingService.CHANNEL_ID,
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
            notificationManager.createNotificationChannel(athgymChannel)

            // Legacy channel backward compatibility
            val legacyChannel = NotificationChannel(
                FitnessFirebaseMessagingService.LEGACY_CHANNEL_ID,
                "Fitness Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Workout reminders and fitness updates"
                enableLights(true)
                lightColor = android.graphics.Color.GREEN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(legacyChannel)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}