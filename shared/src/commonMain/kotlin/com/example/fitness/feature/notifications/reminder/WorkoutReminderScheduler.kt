package com.example.fitness.feature.notifications.reminder

/**
 * Common platform abstraction for local workout reminder scheduling.
 *
 * Android implementation uses AlarmManager.
 * iOS implementation uses UNUserNotificationCenter.
 */
expect object WorkoutReminderScheduler {
    /**
     * Schedules a daily recurring local workout reminder at [hour]:[minute] in local time.
     * Cancels any existing reminder before scheduling to prevent duplicate alarms.
     *
     * @param hour Hour of day (0..23)
     * @param minute Minute of hour (0..59)
     * @param title Title of notification
     * @param message Body text of notification
     * @return true if successfully scheduled, false on failure
     */
    fun schedule(
        hour: Int,
        minute: Int,
        title: String = "Workout Reminder",
        message: String = "Time to crush your workout today! 💪"
    ): Boolean

    /**
     * Cancels any active scheduled workout reminder.
     *
     * @return true if successfully cancelled, false on failure
     */
    fun cancel(): Boolean

    /**
     * Cancels and reschedules the workout reminder at the new [hour]:[minute].
     *
     * @param hour Hour of day (0..23)
     * @param minute Minute of hour (0..59)
     * @param title Title of notification
     * @param message Body text of notification
     * @return true if successfully rescheduled, false on failure
     */
    fun reschedule(
        hour: Int,
        minute: Int,
        title: String = "Workout Reminder",
        message: String = "Time to crush your workout today! 💪"
    ): Boolean
}
