package com.example.fitness.core.user.util

/**
 * Result model representing profile completion metrics.
 */
data class ProfileCompletionStats(
    val percentage: Int,
    val completedFieldCount: Int,
    val totalFields: Int = 5,
    val hasPicture: Boolean,
    val hasName: Boolean,
    val hasEmail: Boolean,
    val hasGender: Boolean,
    val hasMobile: Boolean
) {
    val isComplete: Boolean get() = percentage == 100
    val progressFloat: Float get() = (percentage / 100f).coerceIn(0f, 1f)
    val statusText: String get() = if (isComplete) "Profile complete" else "Profile $percentage% complete"
}

/**
 * Dynamically calculates Profile Completion Percentage based strictly on real authenticated data.
 *
 * Core Fields (Total = 5, each field = 20%):
 * 1. Profile Picture: Real profileImageUrl exists
 * 2. Full Name: Valid real name exists (not blank, not placeholder)
 * 3. Email: Real Firebase Auth email exists (contains '@')
 * 4. Gender: Real Firestore gender value exists (not 'Not set')
 * 5. Mobile Number: Valid Indian mobile number exists
 */
object ProfileCompletionCalculator {

    private val placeholderNames = setOf("name not set", "athlete", "fitness user", "user")
    private val placeholderGenders = setOf("not set", "")

    fun calculate(
        profileImageUrl: String?,
        name: String?,
        email: String?,
        gender: String?,
        mobileNumber: String?
    ): ProfileCompletionStats {
        val hasPicture = !profileImageUrl.isNullOrBlank()

        val cleanName = name?.trim().orEmpty()
        val hasName = cleanName.isNotBlank() && !placeholderNames.contains(cleanName.lowercase())

        val cleanEmail = email?.trim().orEmpty()
        val hasEmail = cleanEmail.isNotBlank() &&
                cleanEmail.contains("@") &&
                !cleanEmail.equals("email not available", ignoreCase = true)

        val cleanGender = gender?.trim().orEmpty()
        val hasGender = cleanGender.isNotBlank() && !placeholderGenders.contains(cleanGender.lowercase())

        val hasMobile = !mobileNumber.isNullOrBlank() && MobileNumberValidator.isValid(mobileNumber)

        var count = 0
        if (hasPicture) count++
        if (hasName) count++
        if (hasEmail) count++
        if (hasGender) count++
        if (hasMobile) count++

        val percentage = count * 20

        return ProfileCompletionStats(
            percentage = percentage,
            completedFieldCount = count,
            totalFields = 5,
            hasPicture = hasPicture,
            hasName = hasName,
            hasEmail = hasEmail,
            hasGender = hasGender,
            hasMobile = hasMobile
        )
    }
}
