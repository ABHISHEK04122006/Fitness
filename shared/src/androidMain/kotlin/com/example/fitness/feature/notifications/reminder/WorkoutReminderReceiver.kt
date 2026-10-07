package com.example.fitness.feature.notifications.reminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.fitness.notifications.FitnessFirebaseMessagingService

/**
 * Android BroadcastReceiver handling scheduled workout reminder alarms.
 * Posts a local heads-up notification and reschedules the next day's alarm.
 */
class WorkoutReminderReceiver : BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_ID = 1001
        const val EXTRA_TITLE = "extra_reminder_title"
        const val EXTRA_MESSAGE = "extra_reminder_message"
        const val EXTRA_HOUR = "extra_reminder_hour"
        const val EXTRA_MINUTE = "extra_reminder_minute"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        val title = intent?.getStringExtra(EXTRA_TITLE) ?: "Workout Reminder"
        val message = intent?.getStringExtra(EXTRA_MESSAGE) ?: "Time to crush your workout today! 💪"
        val hour = intent?.getIntExtra(EXTRA_HOUR, 8) ?: 8
        val minute = intent?.getIntExtra(EXTRA_MINUTE, 0) ?: 0

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigate_to", "workout")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, FitnessFirebaseMessagingService.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(NOTIFICATION_ID, notification)

        // Reschedule for tomorrow at the same local time
        WorkoutReminderScheduler.schedule(hour, minute, title, message)
    }
}
