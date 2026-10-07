package com.example.fitness.core.user.model

/**
 * Represents the persistent user profile stored in Firestore under users/{uid}.
 * Initial fields focus on essential identity and onboarding information:
 * - uid: Firebase Authentication UID
 * - email: Primary account email address
 * - name: User's display name or registration full name
 * - gender: Selected gender ("Male", "Female", etc.)
 *
 * Additional fitness attributes (age, height, weight, goal, experience level)
 * can be added here in future profile extensions.
 */
data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val gender: String = "",
    val mobileNumber: String = "",
    val profileImageUrl: String = "",
    val workoutDays: String = "",
    val fitnessLevel: String = ""
) {
    /**
     * Extracts first name from full name string.
     */
    val firstName: String
        get() = name.trim().split(" ").firstOrNull().orEmpty()

    /**
     * Extracts last name (if present) from full name string.
     */
    val lastName: String
        get() {
            val parts = name.trim().split(" ").filter { it.isNotBlank() }
            return if (parts.size > 1) parts.drop(1).joinToString(" ") else ""
        }

    companion object {
        fun isValidName(name: String): Boolean = name.trim().isNotBlank()
    }
}
