package com.example.fitness.feature.notifications.data

import com.example.fitness.core.firebase.FitnessFirebase
import com.example.fitness.feature.notifications.model.WorkoutReminderPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * Repository for managing user workout reminder preferences.
 * Persisted in Firestore under users/{userId}/settings/notifications.
 */
class WorkoutReminderRepository {

    // Local in-memory cache for offline resilience and fast synchronous access
    private val memoryCache = mutableMapOf<String, WorkoutReminderPreference>()

    private val firestore get() = FitnessFirebase.firestore

    private fun settingsDoc(userId: String) =
        firestore.collection("users").document(userId).collection("settings").document("notifications")

    /**
     * Retrieves the workout reminder preference for [userId].
     * Returns default preference if document does not exist or user is offline.
     */
    suspend fun getPreference(userId: String): WorkoutReminderPreference {
        if (userId.isBlank()) return WorkoutReminderPreference()

        memoryCache[userId]?.let { return it }

        return try {
            val doc = settingsDoc(userId).get()
            if (doc.exists) {
                val data = doc.data<Map<String, Any?>>()
                val pref = WorkoutReminderPreference.fromMap(userId, data)
                memoryCache[userId] = pref
                pref
            } else {
                val defaultPref = WorkoutReminderPreference(userId = userId)
                memoryCache[userId] = defaultPref
                defaultPref
            }
        } catch (e: Exception) {
            println("[DEBUG] WorkoutReminderRepository.getPreference error: ${e.message}")
            memoryCache[userId] ?: WorkoutReminderPreference(userId = userId)
        }
    }

    /**
     * Persists the workout reminder preference for [userId] in Firestore and memory cache.
     */
    suspend fun savePreference(preference: WorkoutReminderPreference): Boolean {
        if (preference.userId.isBlank()) return false
        memoryCache[preference.userId] = preference

        return try {
            settingsDoc(preference.userId).set(
                data = preference.toMap(),
                merge = true
            )
            true
        } catch (e: Exception) {
            println("[DEBUG] WorkoutReminderRepository.savePreference error: ${e.message}")
            // Memory cache updated, so local functionality continues
            false
        }
    }

    /**
     * Emits the preference flow for reactive UI bindings.
     */
    fun getPreferenceFlow(userId: String): Flow<WorkoutReminderPreference> {
        if (userId.isBlank()) return flowOf(WorkoutReminderPreference())
        return flow {
            emit(getPreference(userId))
        }
    }

    fun clearCache() {
        memoryCache.clear()
    }
}
