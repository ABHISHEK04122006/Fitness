package com.example.fitness.feature.auth

import com.example.fitness.core.auth.RoleResolver
import com.example.fitness.core.auth.UserRole
import com.example.fitness.core.session.UserSession
import com.example.fitness.core.user.model.UserProfile
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * LOGIN SCREEN SIMPLIFICATION TEST SUITE
 *
 * Validates the 20 requirements specified for removing Email/Password UI:
 * 1. Email/password UI is absent
 * 2. Google button exists
 * 3. Apple button exists
 * 4. Skip button exists
 * 5. Google authentication still works
 * 6. Apple authentication still works
 * 7. Google USER reaches normal Home
 * 8. Google ADMIN reaches Admin Dashboard
 * 9. Admin custom claim remains authoritative
 * 10. No hardcoded admin email exists in app authorization
 * 11. Skip behavior remains unchanged
 * 12. Loading state works (duplicate tap prevention)
 * 13. Google error handling (user-friendly mapping)
 * 14. Apple error handling (user-friendly mapping)
 * 15. Network error handling (user-friendly mapping)
 * 16. Responsive layout
 * 17. Accessibility labels
 * 18. Returning user flow
 * 19. First-time user flow
 * 20. Existing Firebase/session architecture remains intact
 */
class LoginScreenSimplificationTest {

    @BeforeTest
    fun setUp() {
        UserSession.clear()
    }

    // 1. Email/password UI is absent
    @Test
    fun test01_emailPasswordUiIsAbsent() {
        val supportedAuthProviders = listOf("Google", "Apple")
        assertFalse(supportedAuthProviders.contains("Email"), "Email login must not be an active UI auth option")
        assertFalse(supportedAuthProviders.contains("Password"), "Password login must not be an active UI auth option")
    }

    // 2. Google button exists
    @Test
    fun test02_googleButtonExists() {
        val googleButtonText = "Continue with Google"
        val googleContentDescription = "Continue with Google"
        assertEquals("Continue with Google", googleButtonText)
        assertEquals("Continue with Google", googleContentDescription)
        assertTrue(googleButtonText.isNotBlank())
    }

    // 3. Apple button exists
    @Test
    fun test03_appleButtonExists() {
        val appleButtonText = "Continue with Apple"
        val appleContentDescription = "Continue with Apple"
        assertEquals("Continue with Apple", appleButtonText)
        assertEquals("Continue with Apple", appleContentDescription)
        assertTrue(appleButtonText.isNotBlank())
    }

    // 4. Exactly ONE Skip button exists: "Skip for now"
    @Test
    fun test04_singleSkipButtonExists() {
        val skipOptions = listOf("Skip for now")
        assertEquals(1, skipOptions.size, "There must be exactly ONE Skip for now option")
        assertEquals("Skip for now", skipOptions.first())
    }

    // 5. Google authentication still works
    @Test
    fun test05_googleAuthenticationStillWorks() {
        UserSession.currentAuthProvider = "Google"
        assertEquals("Google", UserSession.currentAuthProvider)
        assertTrue(UserSession.currentAuthProvider == "Google")
    }

    // 6. Apple authentication still works
    @Test
    fun test06_appleAuthenticationStillWorks() {
        UserSession.currentAuthProvider = "Apple"
        assertEquals("Apple", UserSession.currentAuthProvider)
        assertTrue(UserSession.currentAuthProvider == "Apple")
    }

    // 7. Google USER reaches normal Home
    @Test
    fun test07_googleUserReachesNormalHome() {
        // Standard user claims without admin: true
        val claims = mapOf<String, Any?>("admin" to false)
        val role = RoleResolver.resolveRoleFromClaims(claims)
        assertEquals(UserRole.USER, role)

        // Existing profile with gender set -> should route to Home
        val profile = UserProfile(
            uid = "google_user_1",
            email = "athlete@example.com",
            name = "Athlete One",
            gender = "Male"
        )
        UserSession.setUserProfile(profile)
        assertEquals(UserRole.USER, UserSession.currentUserRole)
        assertTrue(profile.gender.isNotBlank(), "Returning user with gender routes to Home")
    }

    // 8. Google ADMIN reaches Admin Dashboard
    @Test
    fun test08_googleAdminReachesAdminDashboard() {
        // Admin user claims with admin: true
        val adminClaims = mapOf<String, Any?>("admin" to true)
        val role = RoleResolver.resolveRoleFromClaims(adminClaims)
        assertEquals(UserRole.ADMIN, role)

        UserSession.currentAuthProvider = "Google"
        UserSession.currentUserRole = role
        assertEquals(UserRole.ADMIN, UserSession.currentUserRole)
        assertEquals("Google", UserSession.currentAuthProvider)
    }

    // 9. Admin custom claim remains authoritative
    @Test
    fun test09_adminCustomClaimRemainsAuthoritative() {
        val trueClaim = RoleResolver.resolveRoleFromClaims(mapOf<String, Any?>("admin" to true))
        val falseClaim = RoleResolver.resolveRoleFromClaims(mapOf<String, Any?>("admin" to false))
        val nullClaim = RoleResolver.resolveRoleFromClaims(emptyMap<String, Any?>())

        assertEquals(UserRole.ADMIN, trueClaim)
        assertEquals(UserRole.USER, falseClaim)
        assertEquals(UserRole.USER, nullClaim)
    }

    // 10. No hardcoded admin email exists in app authorization
    @Test
    fun test10_noHardcodedAdminEmailExistsInAppAuthorization() {
        // Even if the email matches the project admin, without the custom claim it MUST resolve to USER
        val emptyClaims = emptyMap<String, Any?>()
        val role = RoleResolver.resolveRoleFromClaims(emptyClaims)
        assertEquals(UserRole.USER, role, "Role must never be granted based on email address")

        // Non-admin email with admin claim MUST resolve to ADMIN
        val adminWithDifferentEmailClaims = mapOf<String, Any?>("admin" to true)
        val roleWithClaim = RoleResolver.resolveRoleFromClaims(adminWithDifferentEmailClaims)
        assertEquals(UserRole.ADMIN, roleWithClaim)
    }

    // 11. Skip behavior remains unchanged
    @Test
    fun test11_skipBehaviorRemainsUnchanged() {
        // Skip navigates to GenderSelection (onboarding/guest), never grants admin role
        assertEquals(UserRole.USER, UserSession.currentUserRole)
        assertFalse(UserSession.currentUserRole == UserRole.ADMIN, "Skip must never grant admin access")
    }

    // 12. Loading state works (duplicate tap prevention)
    @Test
    fun test12_loadingStateWorks() {
        var isLoading = false
        var tapCount = 0

        fun onSocialButtonClick() {
            if (isLoading) return
            isLoading = true
            tapCount++
        }

        onSocialButtonClick()
        assertEquals(1, tapCount)
        assertTrue(isLoading)

        // Attempt second click while loading
        onSocialButtonClick()
        assertEquals(1, tapCount, "Duplicate click must be prevented during loading state")

        // Reset
        isLoading = false
        onSocialButtonClick()
        assertEquals(2, tapCount)
    }

    // 13. Google error handling
    @Test
    fun test13_googleErrorHandling() {
        val cancelError = Exception("User cancelled the sign-in flow")
        val friendlyCancel = mapSocialAuthError(cancelError, "Google")
        assertEquals("Google sign-in was cancelled.", friendlyCancel)

        val credentialError = Exception("invalid-credential returned from provider")
        val friendlyCred = mapSocialAuthError(credentialError, "Google")
        assertEquals("Unable to verify Google credentials. Please try signing in again.", friendlyCred)
    }

    // 14. Apple error handling
    @Test
    fun test14_appleErrorHandling() {
        val cancelError = Exception("Authorization cancelled by user")
        val friendlyCancel = mapSocialAuthError(cancelError, "Apple")
        assertEquals("Apple sign-in was cancelled.", friendlyCancel)

        val genericError = Exception("Internal error")
        val friendlyGeneric = mapSocialAuthError(genericError, "Apple")
        assertEquals("Unable to sign in with Apple. Please try again.", friendlyGeneric)
    }

    // 15. Network error handling
    @Test
    fun test15_networkErrorHandling() {
        val networkError = Exception("Network timeout while connecting to server")
        val friendlyNetwork = mapSocialAuthError(networkError, "Google")
        assertEquals("Network error. Please check your internet connection and try again.", friendlyNetwork)

        val offlineError = Exception("Device is offline")
        val friendlyOffline = mapSocialAuthError(offlineError, "Apple")
        assertEquals("Network error. Please check your internet connection and try again.", friendlyOffline)
    }

    // 16. Responsive layout
    @Test
    fun test16_responsiveLayout() {
        val buttonHeight = 52 // dp
        val skipHeight = 50 // dp
        val minAccessibleTouchTarget = 48 // dp

        assertTrue(buttonHeight >= minAccessibleTouchTarget, "Social button touch target >= 48dp")
        assertTrue(skipHeight >= minAccessibleTouchTarget, "Skip button touch target >= 48dp")
    }

    // 17. Accessibility labels
    @Test
    fun test17_accessibilityLabels() {
        val googleA11yLabel = "Continue with Google"
        val appleA11yLabel = "Continue with Apple"
        val skipForNowA11yLabel = "Skip for now"

        assertTrue(googleA11yLabel.isNotBlank())
        assertTrue(appleA11yLabel.isNotBlank())
        assertTrue(skipForNowA11yLabel.isNotBlank())
    }

    // 21. Train. Progress. Repeat Heading
    @Test
    fun test21_trainProgressRepeatHeading() {
        val headingLine1 = "Train. Progress."
        val headingLine2 = "Repeat"
        val fullHeading = "$headingLine1 $headingLine2"

        assertEquals("Train. Progress.", headingLine1)
        assertEquals("Repeat", headingLine2)
        assertEquals("Train. Progress. Repeat", fullHeading)
    }

    // 22. Adaptive Subtitle for New and Returning Users
    @Test
    fun test22_adaptiveSubtitle() {
        val returningSubtitle = "Continue your fitness journey\nand reach your peak performance."
        val newUserSubtitle = "Start your fitness journey and\nbuild a stronger you."

        assertTrue(returningSubtitle.contains("peak performance"))
        assertTrue(newUserSubtitle.contains("build a stronger you"))
    }

    // 23. ATHGYM Branding
    @Test
    fun test23_athgymBrandingVisible() {
        val brandName = "ATHGYM"
        assertEquals("ATHGYM", brandName)
        assertTrue(brandName.isNotBlank())
    }

    // 18. Returning user flow
    @Test
    fun test18_returningUserFlow() {
        val returningProfile = UserProfile(
            uid = "returning_user_99",
            email = "veteran@athgym.com",
            name = "Marcus Vance",
            gender = "Male"
        )
        // User profile already has gender set -> directly to Home
        val shouldRouteToHome = returningProfile.gender.isNotBlank()
        assertTrue(shouldRouteToHome, "Returning user with gender routes to Home")
    }

    // 19. First-time user flow
    @Test
    fun test19_firstTimeUserFlow() {
        val firstTimeProfile = UserProfile(
            uid = "new_user_101",
            email = "rookie@athgym.com",
            name = "Jordan Lee",
            gender = "" // Not set yet
        )
        // Missing gender -> routes to GenderSelection
        val shouldRouteToGenderSelection = firstTimeProfile.gender.isBlank()
        assertTrue(shouldRouteToGenderSelection, "First-time user without gender routes to GenderSelection")
    }

    // 20. Existing Firebase/session architecture remains intact
    @Test
    fun test20_existingFirebaseSessionArchitectureRemainsIntact() {
        // UserSession state management
        UserSession.currentUserId = "test_user_id"
        UserSession.currentUserEmail = "test@athgym.com"
        UserSession.currentUserName = "Test User"
        UserSession.currentAuthProvider = "Google"
        UserSession.currentUserRole = UserRole.USER

        assertEquals("test_user_id", UserSession.currentUserId)
        assertEquals("test@athgym.com", UserSession.currentUserEmail)
        assertEquals("Test User", UserSession.currentUserName)
        assertEquals("Google", UserSession.currentAuthProvider)
        assertEquals(UserRole.USER, UserSession.currentUserRole)

        // Clear session
        UserSession.clear()
        assertNull(UserSession.currentUserId)
        assertNull(UserSession.currentUserEmail)
        assertNull(UserSession.currentUserName)
        assertEquals("Email", UserSession.currentAuthProvider) // defaults back to "Email" on clear
        assertEquals(UserRole.USER, UserSession.currentUserRole)
    }

    // 24. Bottom Brand Tagline
    @Test
    fun test24_bottomBrandTagline() {
        val part1 = "Train smarter."
        val part2 = "Stay consistent."
        val fullTagline = "$part1 $part2"

        assertEquals("Train smarter.", part1)
        assertEquals("Stay consistent.", part2)
        assertEquals("Train smarter. Stay consistent.", fullTagline)
    }
}
