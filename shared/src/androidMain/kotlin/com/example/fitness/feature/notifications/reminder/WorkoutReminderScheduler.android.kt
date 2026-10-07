package com.example.fitness.feature.notifications.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.fitness.core.auth.ActivityProvider
import java.util.Calendar

/**
 * Android implementation of WorkoutReminderScheduler using AlarmManager.
 */
actual object WorkoutReminderScheduler {

    private const val REQUEST_CODE = 9001

    private fun getContext(): Context? {
        return ActivityProvider.currentActivity?.applicationContext
    }

    actual fun schedule(
        hour: Int,
        minute: Int,
        title: String,
        message: String
    ): Boolean {
        val context = getContext() ?: return false
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false

        return try {
            // Cancel existing to prevent duplicate alarms
            cancel()

            val intent = Intent(context, WorkoutReminderReceiver::class.java).apply {
                putExtra(WorkoutReminderReceiver.EXTRA_TITLE, title)
                putExtra(WorkoutReminderReceiver.EXTRA_MESSAGE, message)
                putExtra(WorkoutReminderReceiver.EXTRA_HOUR, hour)
                putExtra(WorkoutReminderReceiver.EXTRA_MINUTE, minute)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val triggerAtMillis = calendar.timeInMillis

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
            true
        } catch (e: Exception) {
            println("[ERROR] WorkoutReminderScheduler.schedule failed: ${e.message}")
            false
        }
    }

    actual fun cancel(): Boolean {
        val context = getContext() ?: return false
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false

        return try {
            val intent = Intent(context, WorkoutReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
            true
        } catch (e: Exception) {
            println("[ERROR] WorkoutReminderScheduler.cancel failed: ${e.message}")
            false
        }
    }

    actual fun reschedule(
        hour: Int,
        minute: Int,
        title: String,
        message: String
    ): Boolean {
        cancel()
        return schedule(hour, minute, title, message)
    }
}
