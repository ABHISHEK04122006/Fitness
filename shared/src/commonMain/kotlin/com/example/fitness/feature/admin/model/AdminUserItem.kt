package com.example.fitness.feature.admin.model

import com.example.fitness.core.auth.UserRole

data class AdminUserItem(
    val uid: String,
    val name: String,
    val email: String,
    val gender: String,
    val role: UserRole = UserRole.USER,
    val notificationCount: Int? = null,
    val deviceCount: Int? = null,
    val rawRoleField: String? = null,
    val authProvider: String = "Email"
) {
    val displayName: String
        get() = name.takeIf { it.isNotBlank() } ?: "Athlete"

    val firstName: String
        get() = displayName.trim().split(" ").firstOrNull().orEmpty()

    val lastName: String
        get() {
            val parts = displayName.trim().split(" ").filter { it.isNotBlank() }
            return if (parts.size > 1) parts.drop(1).joinToString(" ") else ""
        }

    val displayEmail: String
        get() = email.takeIf { it.isNotBlank() } ?: "No email registered"

    val displayGender: String
        get() = gender.takeIf { it.isNotBlank() } ?: "Not specified"

    val displayProvider: String
        get() = when (authProvider.lowercase().trim()) {
            "google" -> "Google"
            "apple" -> "Apple"
            else -> "Email"
        }

    val truncatedUid: String
        get() = if (uid.length > 10) "${uid.take(6)}...${uid.takeLast(4)}" else uid
}
