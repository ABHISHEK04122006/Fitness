package com.example.fitness.feature.profile

import com.example.fitness.core.components.ProfileImageMemoryCache
import com.example.fitness.core.components.calculateUserInitials
import com.example.fitness.core.session.UserSession
import com.example.fitness.core.user.model.UserProfile
import com.example.fitness.core.user.util.MobileNumberValidator
import com.example.fitness.core.user.util.ProfileCompletionCalculator
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProfileRealDataAndCompletionTest {

    @BeforeTest
    fun setUp() {
        UserSession.clear()
        ProfileImageMemoryCache.clear()
    }

    // ==========================================
    // 1. MobileNumberValidator Tests
    // ==========================================

    @Test
    fun testValidIndianMobileNumbers() {
        // Standard 10-digit starting with 6, 7, 8, 9
        assertEquals("+919876543210", MobileNumberValidator.normalize("9876543210"))
        assertEquals("+918123456789", MobileNumberValidator.normalize("8123456789"))
        assertEquals("+917012345678", MobileNumberValidator.normalize("7012345678"))
        assertEquals("+916987654321", MobileNumberValidator.normalize("6987654321"))

        assertTrue(MobileNumberValidator.isValid("9876543210"))
        assertTrue(MobileNumberValidator.isValid("8123456789"))
    }

    @Test
    fun testIndianMobileNumbersWithCountryCodesAndFormatting() {
        // With +91
        assertEquals("+919876543210", MobileNumberValidator.normalize("+91 98765 43210"))
        assertEquals("+919876543210", MobileNumberValidator.normalize("+91-98765-43210"))
        assertEquals("+919876543210", MobileNumberValidator.normalize("+919876543210"))

        // With 91 prefix without plus
        assertEquals("+919876543210", MobileNumberValidator.normalize("919876543210"))

        // With leading 0 (trunk code)
        assertEquals("+919876543210", MobileNumberValidator.normalize("09876543210"))
        assertEquals("+919876543210", MobileNumberValidator.normalize("0 98765 43210"))

        // With parentheses and spaces
        assertEquals("+919876543210", MobileNumberValidator.normalize("+91 (98765) 43210"))
    }

    @Test
    fun testInvalidIndianMobileNumbers() {
        // Starting with 1, 2, 3, 4, 5
        assertNull(MobileNumberValidator.normalize("5876543210"))
        assertNull(MobileNumberValidator.normalize("1234567890"))
        assertFalse(MobileNumberValidator.isValid("5876543210"))

        // Shorter than 10 digits
        assertNull(MobileNumberValidator.normalize("987654321"))
        assertFalse(MobileNumberValidator.isValid("987654321"))

        // Longer than 10 digits
        assertNull(MobileNumberValidator.normalize("98765432100"))
        assertFalse(MobileNumberValidator.isValid("98765432100"))

        // Contains alphabetic characters
        assertNull(MobileNumberValidator.normalize("98765abcde"))
        assertNull(MobileNumberValidator.normalize("phone12345"))

        // Other country codes
        assertNull(MobileNumberValidator.normalize("+14155552671"))
        assertNull(MobileNumberValidator.normalize("+447911123456"))

        // Blank, null, or empty
        assertNull(MobileNumberValidator.normalize(""))
        assertNull(MobileNumberValidator.normalize("   "))
        assertNull(MobileNumberValidator.normalize(null))
        assertFalse(MobileNumberValidator.isValid(null))
        assertFalse(MobileNumberValidator.isValid(""))
    }

    @Test
    fun testMobileNumberDisplayFormatting() {
        assertEquals("+91 98765 43210", MobileNumberValidator.formatForDisplay("+919876543210"))
        assertEquals("+91 98765 43210", MobileNumberValidator.formatForDisplay("9876543210"))
        assertEquals("+91 98765 43210", MobileNumberValidator.formatForDisplay("+91 9876543210"))

        // Null and blank formatting
        assertEquals("Not set", MobileNumberValidator.formatForDisplay(null))
        assertEquals("Not set", MobileNumberValidator.formatForDisplay(""))
        assertEquals("Not set", MobileNumberValidator.formatForDisplay("   "))
    }

    // ==========================================
    // 2. ProfileCompletionCalculator Tests
    // ==========================================

    @Test
    fun testProfileCompletionZeroPercentWhenEmptyOrPlaceholders() {
        val statsEmpty = ProfileCompletionCalculator.calculate(
            profileImageUrl = null,
            name = null,
            email = null,
            gender = null,
            mobileNumber = null
        )
        assertEquals(0, statsEmpty.percentage)
        assertEquals(0, statsEmpty.completedFieldCount)
        assertFalse(statsEmpty.isComplete)
        assertEquals("Profile 0% complete", statsEmpty.statusText)

        // Placeholders must NOT be counted as completed
        val statsPlaceholders = ProfileCompletionCalculator.calculate(
            profileImageUrl = "",
            name = "Athlete",
            email = "email not available",
            gender = "Not set",
            mobileNumber = ""
        )
        assertEquals(0, statsPlaceholders.percentage)
        assertEquals(0, statsPlaceholders.completedFieldCount)
        assertFalse(statsPlaceholders.hasPicture)
        assertFalse(statsPlaceholders.hasName)
        assertFalse(statsPlaceholders.hasEmail)
        assertFalse(statsPlaceholders.hasGender)
        assertFalse(statsPlaceholders.hasMobile)
    }

    @Test
    fun testProfileCompletionIncrementPerField() {
        // 1 field = 20%
        val stats1 = ProfileCompletionCalculator.calculate(
            profileImageUrl = null,
            name = "John Doe",
            email = "",
            gender = "",
            mobileNumber = ""
        )
        assertEquals(20, stats1.percentage)
        assertEquals(1, stats1.completedFieldCount)
        assertTrue(stats1.hasName)

        // 2 fields = 40%
        val stats2 = ProfileCompletionCalculator.calculate(
            profileImageUrl = null,
            name = "John Doe",
            email = "user@example.com",
            gender = "",
            mobileNumber = ""
        )
        assertEquals(40, stats2.percentage)
        assertEquals(2, stats2.completedFieldCount)
        assertTrue(stats2.hasName)
        assertTrue(stats2.hasEmail)

        // 3 fields = 60%
        val stats3 = ProfileCompletionCalculator.calculate(
            profileImageUrl = null,
            name = "John Doe",
            email = "user@example.com",
            gender = "Male",
            mobileNumber = ""
        )
        assertEquals(60, stats3.percentage)
        assertEquals(3, stats3.completedFieldCount)
        assertTrue(stats3.hasGender)

        // 4 fields = 80%
        val stats4 = ProfileCompletionCalculator.calculate(
            profileImageUrl = null,
            name = "John Doe",
            email = "user@example.com",
            gender = "Male",
            mobileNumber = "9876543210"
        )
        assertEquals(80, stats4.percentage)
        assertEquals(4, stats4.completedFieldCount)
        assertTrue(stats4.hasMobile)

        // 5 fields = 100%
        val stats5 = ProfileCompletionCalculator.calculate(
            profileImageUrl = "https://firebasestorage.googleapis.com/v0/b/app/o/profile.jpg",
            name = "John Doe",
            email = "user@example.com",
            gender = "Male",
            mobileNumber = "+919876543210"
        )
        assertEquals(100, stats5.percentage)
        assertEquals(5, stats5.completedFieldCount)
        assertTrue(stats5.isComplete)
        assertEquals("Profile complete", stats5.statusText)
        assertEquals(1.0f, stats5.progressFloat)
    }

    @Test
    fun testProfileCompletionWithInvalidMobileDoesNotCount() {
        val stats = ProfileCompletionCalculator.calculate(
            profileImageUrl = null,
            name = "John Doe",
            email = "user@example.com",
            gender = "Male",
            mobileNumber = "12345" // Invalid mobile number
        )
        assertEquals(60, stats.percentage)
        assertFalse(stats.hasMobile)
    }

    // ==========================================
    // 3. calculateUserInitials Tests
    // ==========================================

    @Test
    fun testInitialsGeneration() {
        // Two names
        assertEquals("JD", calculateUserInitials("John Doe"))
        // Single name
        assertEquals("A", calculateUserInitials("Alex"))
        // Multiple names
        assertEquals("JM", calculateUserInitials("Jane Mary Doe"))
        // Name with leading/trailing spaces
        assertEquals("JD", calculateUserInitials("  John   Doe  "))

        // Placeholder name falls back to email prefix initial
        assertEquals("T", calculateUserInitials("Athlete", "testuser@example.com"))
        assertEquals("K", calculateUserInitials("Name not set", "karan@domain.com"))

        // Empty fallback
        assertEquals("A", calculateUserInitials("", ""))
        assertEquals("A", calculateUserInitials(null, null))
    }

    // ==========================================
    // 4. UserSession Isolation & Cleanup Tests
    // ==========================================

    @Test
    fun testUserSessionClearsMobileAndProfileImageOnLogout() {
        // Setup User A
        val userAProfile = UserProfile(
            uid = "user_a_123",
            email = "usera@example.com",
            name = "User Alpha",
            gender = "Male",
            mobileNumber = "+919876543210",
            profileImageUrl = "https://storage.googleapis.com/user_a.png"
        )
        UserSession.setUserProfile(userAProfile)

        assertEquals("user_a_123", UserSession.currentUserId)
        assertEquals("+919876543210", UserSession.currentUserMobileNumber)
        assertEquals("https://storage.googleapis.com/user_a.png", UserSession.currentUserProfileImageUrl)

        // Simulate Logout
        UserSession.clear()

        // Verify complete isolation - nothing from User A remains
        assertNull(UserSession.currentUserId)
        assertNull(UserSession.currentUserEmail)
        assertNull(UserSession.currentUserName)
        assertNull(UserSession.currentUserGender)
        assertNull(UserSession.currentUserMobileNumber)
        assertNull(UserSession.currentUserProfileImageUrl)
        assertNull(UserSession.currentUserProfile)

        // Setup User B with no mobile or image
        val userBProfile = UserProfile(
            uid = "user_b_456",
            email = "userb@example.com",
            name = "User Beta",
            gender = "Female",
            mobileNumber = "",
            profileImageUrl = ""
        )
        UserSession.setUserProfile(userBProfile)

        assertEquals("user_b_456", UserSession.currentUserId)
        assertNull(UserSession.currentUserMobileNumber)
        assertNull(UserSession.currentUserProfileImageUrl)
    }
}
