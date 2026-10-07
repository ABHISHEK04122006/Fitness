package com.example.fitness.feature.notifications.reminder

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

/**
 * iOS native implementation of WorkoutReminderScheduler using UNUserNotificationCenter.
 */
actual object WorkoutReminderScheduler {

    private const val REMINDER_IDENTIFIER = "com.example.fitness.workout_reminder"

    actual fun schedule(
        hour: Int,
        minute: Int,
        title: String,
        message: String
    ): Boolean {
        return try {
            val center = UNUserNotificationCenter.currentNotificationCenter()

            // Remove existing to prevent duplicates
            cancel()

            val content = UNMutableNotificationContent().apply {
                setTitle(title)
                setBody(message)
                setSound(UNNotificationSound.defaultSound())
            }

            val dateComponents = NSDateComponents().apply {
                setHour(hour.toLong())
                setMinute(minute.toLong())
            }

            val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                dateComponents = dateComponents,
                repeats = true
            )

            val request = UNNotificationRequest.requestWithIdentifier(
                identifier = REMINDER_IDENTIFIER,
                content = content,
                trigger = trigger
            )

            center.addNotificationRequest(request) { error ->
                if (error != null) {
                    println("[ERROR] iOS WorkoutReminderScheduler.schedule failed: ${error.localizedDescription}")
                }
            }
            true
        } catch (e: Exception) {
            println("[ERROR] iOS WorkoutReminderScheduler.schedule exception: ${e.message}")
            false
        }
    }

    actual fun cancel(): Boolean {
        return try {
            val center = UNUserNotificationCenter.currentNotificationCenter()
            center.removePendingNotificationRequestsWithIdentifiers(listOf(REMINDER_IDENTIFIER))
            true
        } catch (e: Exception) {
            println("[ERROR] iOS WorkoutReminderScheduler.cancel exception: ${e.message}")
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
