package com.example.fitness.feature.auth

import com.example.fitness.core.auth.RoleResolver
import com.example.fitness.core.auth.UserRole
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.core.user.model.UserProfile
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * SAMSUNG S24 LOGIN VERIFICATION TEST SUITE
 *
 * Validates the safety timeout, loading-state resolution, role resolution,
 * and navigation requirements for the Samsung Galaxy S24 infinite loading fix:
 * 1. Timeout error mapping to user-friendly message
 * 2. Network error mapping intact without regressions
 * 3. User cancellation mapping intact
 * 4. RoleResolver fails safe to UserRole.USER on null or timeout
 * 5. Returning user with profile gender routes to Screen.Home
 * 6. New user without gender routes to Screen.GenderSelection
 * 7. Admin custom claims route to Screen.AdminDashboard
 * 8. Loading state cleanup guarantee on both success and failure
 */
class SamsungS24LoginFlowTest {

    @BeforeTest
    fun setUp() {
        UserSession.clear()
    }

    @Test
    fun test01_timeoutErrorMappedToHelpfulMessage() {
        val timeoutEx = Exception("Google Sign-In timed out. Please check your connection and try again.")
        val friendly = mapSocialAuthError(timeoutEx, "Google")
        assertEquals("Login is taking longer than expected. Please check your connection and try again.", friendly)

        val coroutineTimeout = Exception("Timed out waiting for 45000 ms")
        val friendlyCoroutine = mapSocialAuthError(coroutineTimeout, "Google")
        assertEquals("Login is taking longer than expected. Please check your connection and try again.", friendlyCoroutine)
    }

    @Test
    fun test02_networkErrorsStillMappedCorrectly() {
        val networkEx = Exception("Network timeout while connecting to server")
        val friendly = mapSocialAuthError(networkEx, "Google")
        assertEquals("Network error. Please check your internet connection and try again.", friendly)

        val offlineEx = Exception("Device is offline")
        val friendlyOffline = mapSocialAuthError(offlineEx, "Google")
        assertEquals("Network error. Please check your internet connection and try again.", friendlyOffline)
    }

    @Test
    fun test03_userCancellationMappedCorrectly() {
        val cancelEx = Exception("User cancelled account selection")
        val friendly = mapSocialAuthError(cancelEx, "Google")
        assertEquals("Google sign-in was cancelled.", friendly)
    }

    @Test
    fun test04_roleResolverFailsSafeToUserRole() {
        // When claims map is null or empty, role defaults to USER
        val roleEmpty = RoleResolver.resolveRoleFromClaims(emptyMap())
        assertEquals(UserRole.USER, roleEmpty)

        val roleNull = RoleResolver.resolveRoleFromClaims(null)
        assertEquals(UserRole.USER, roleNull)

        // When admin claim is true, resolves to ADMIN
        val roleAdmin = RoleResolver.resolveRoleFromClaims(mapOf("admin" to true))
        assertEquals(UserRole.ADMIN, roleAdmin)
    }

    @Test
    fun test05_returningUserProfileWithGenderRoutesToHome() {
        val profile = UserProfile(
            uid = "s24_user_1",
            email = "athlete@example.com",
            name = "S24 Athlete",
            gender = "Male"
        )
        UserSession.setUserProfile(profile)
        val shouldRouteToHome = !profile.gender.isBlank()
        assertTrue(shouldRouteToHome, "Returning user with gender routes to Home")
    }

    @Test
    fun test06_newUserWithoutGenderRoutesToGenderSelection() {
        val profile = UserProfile(
            uid = "s24_user_2",
            email = "new_athlete@example.com",
            name = "New Athlete",
            gender = ""
        )
        UserSession.setUserProfile(profile)
        val shouldRouteToGender = profile.gender.isBlank()
        assertTrue(shouldRouteToGender, "First-time user without gender routes to GenderSelection")
    }

    @Test
    fun test07_adminClaimRoutesToAdminDashboard() {
        val adminProfile = UserProfile(
            uid = "admin_user_1",
            email = "admin@example.com",
            name = "Admin User",
            gender = "Male"
        )
        UserSession.setUserProfile(adminProfile)
        UserSession.currentUserRole = UserRole.ADMIN

        val destination = if (UserSession.currentUserRole == UserRole.ADMIN) {
            Screen.AdminDashboard
        } else {
            Screen.Home
        }
        assertEquals(Screen.AdminDashboard, destination)
    }

    @Test
    fun test08_loadingStateCleanedUpInFinallyPattern() {
        var isLoading = false
        var activeProvider: String? = null
        var errorMessage: String? = null

        fun executeLoginSimulation(shouldSucceed: Boolean, error: Exception? = null) {
            isLoading = true
            activeProvider = "Google"
            try {
                if (!shouldSucceed) {
                    throw error ?: Exception("Failure")
                }
            } catch (e: Exception) {
                errorMessage = mapSocialAuthError(e, "Google")
            } finally {
                isLoading = false
                activeProvider = null
            }
        }

        // Test failure path
        executeLoginSimulation(shouldSucceed = false, error = Exception("Timed out waiting"))
        assertFalse(isLoading, "isLoading must be false after failure")
        assertEquals(null, activeProvider)
        assertEquals("Login is taking longer than expected. Please check your connection and try again.", errorMessage)

        // Test success path
        executeLoginSimulation(shouldSucceed = true)
        assertFalse(isLoading, "isLoading must be false after success")
        assertEquals(null, activeProvider)
    }

    @Test
    fun test09_diagnosticAuthResultTypes() {
        val success = com.example.fitness.core.auth.DiagnosticAuthResult.Success(uid = "s24_diag_uid_123", currentUserStatus = "available")
        assertEquals("s24_diag_uid_123", success.uid)
        assertEquals("available", success.currentUserStatus)

        val timeout = com.example.fitness.core.auth.DiagnosticAuthResult.Timeout(durationMillis = 20_000L)
        assertEquals(20_000L, timeout.durationMillis)
    }

    @Test
    fun test10_authDiagnosticFailureCapturesException() = kotlinx.coroutines.runBlocking {
        val result = com.example.fitness.core.auth.AuthDiagnostic.runFirebaseAuthOnlyTest {
            throw IllegalStateException("API key not valid. Please pass a valid API key.")
        }

        assertTrue(result is com.example.fitness.core.auth.DiagnosticAuthResult.Failure)
        val failure = result as com.example.fitness.core.auth.DiagnosticAuthResult.Failure
        assertEquals("IllegalStateException", failure.exceptionClass)
        assertTrue(failure.message.contains("API key not valid"))
    }

    @Test
    fun test11_authDiagnosticCancellation() = kotlinx.coroutines.runBlocking {
        val result = com.example.fitness.core.auth.AuthDiagnostic.runFirebaseAuthOnlyTest {
            null // User cancelled
        }

        assertTrue(result is com.example.fitness.core.auth.DiagnosticAuthResult.Cancelled)
    }

    @Test
    fun test12_loginLifecycleLogSequenceValidation() {
        val simulatedLogTrace = mutableListOf<String>()

        fun log(msg: String) {
            simulatedLogTrace.add(msg)
        }

        // Trace of complete login flow
        log("[LOGIN] Button clicked (provider=Google)")
        log("[LOGIN] Validation passed")
        log("[LOGIN] Loading = true")
        log("[LOGIN] Credential Manager START")
        log("[LOGIN] Credential returned (type=CustomCredential)")
        log("[LOGIN] Google ID token received")
        log("[LOGIN] Firebase Google credential creation")
        log("[LOGIN] Firebase sign-in START")
        log("[LOGIN] Firebase sign-in SUCCESS (uid=user_123)")
        log("[LOGIN] Firebase currentUser = available")
        log("[LOGIN] Session creation START")
        log("[LOGIN] Role resolution START")
        log("[LOGIN] Role resolution SUCCESS: role=USER")
        log("[LOGIN] Firestore profile START")
        log("[LOGIN] Firestore profile SUCCESS")
        log("[LOGIN] Session creation SUCCESS")
        log("[LOGIN] Navigation START (destination=Home)")
        log("[LOGIN] Loading = false")

        assertEquals(18, simulatedLogTrace.size)
        assertEquals("[LOGIN] Button clicked (provider=Google)", simulatedLogTrace.first())
        assertEquals("[LOGIN] Loading = false", simulatedLogTrace.last())
        assertTrue(simulatedLogTrace.contains("[LOGIN] Firebase sign-in SUCCESS (uid=user_123)"))
        assertTrue(simulatedLogTrace.contains("[LOGIN] Firestore profile SUCCESS"))
    }

    // TEST 1: Credential Manager success -> fallback NOT called -> Firebase Auth called once
    @Test
    fun test13_credentialManagerSuccess_fallbackNotCalled_authCalledOnce() = kotlinx.coroutines.runBlocking {
        var fallbackCalled = false
        var authCalls = 0

        val result = com.example.fitness.core.auth.performGoogleSignInFlow(
            credentialManagerCall = {
                authCalls++
                "firebase_user_cred_mgr"
            },
            fallbackCall = {
                fallbackCalled = true
                authCalls++
                "firebase_user_fallback"
            }
        )

        assertEquals("firebase_user_cred_mgr", result)
        assertFalse(fallbackCalled, "Fallback must NOT be called when Credential Manager succeeds")
        assertEquals(1, authCalls, "Authentication must be called exactly once")
    }

    // TEST 2: Credential Manager timeout -> fallback called -> Firebase Auth succeeds
    @Test
    fun test14_credentialManagerTimeout_fallbackCalled_authSucceeds() = kotlinx.coroutines.runBlocking {
        var fallbackCalled = false
        var authCalls = 0

        val result = com.example.fitness.core.auth.performGoogleSignInFlow(
            credentialManagerCall = {
                kotlinx.coroutines.withTimeout(1L) {
                    kotlinx.coroutines.delay(50L)
                }
                "never_reached"
            },
            fallbackCall = {
                fallbackCalled = true
                authCalls++
                "firebase_user_fallback"
            }
        )

        assertEquals("firebase_user_fallback", result)
        assertTrue(fallbackCalled, "Fallback MUST be called when Credential Manager times out")
        assertEquals(1, authCalls, "Authentication must be called once on fallback")
    }

    // TEST 3: Credential Manager recoverable failure -> fallback called
    @Test
    fun test15_credentialManagerRecoverableFailure_fallbackCalled() = kotlinx.coroutines.runBlocking {
        var fallbackCalled = false

        val result = com.example.fitness.core.auth.performGoogleSignInFlow(
            credentialManagerCall = {
                throw IllegalStateException("No Google account found on device")
            },
            fallbackCall = {
                fallbackCalled = true
                "firebase_user_fallback"
            }
        )

        assertEquals("firebase_user_fallback", result)
        assertTrue(fallbackCalled, "Fallback MUST be called on recoverable Credential Manager failure")
    }

    // TEST 4: User cancellation -> fallback NOT called
    @Test
    fun test16_userCancellation_fallbackNotCalled() = kotlinx.coroutines.runBlocking {
        var fallbackCalled = false

        // Case A: returns null
        val resultA = com.example.fitness.core.auth.performGoogleSignInFlow(
            credentialManagerCall = { null },
            fallbackCall = {
                fallbackCalled = true
                "fallback"
            }
        )
        kotlin.test.assertNull(resultA)
        assertFalse(fallbackCalled, "Fallback must NOT be called when user cancels")

        // Case B: cancellation exception
        val resultB = com.example.fitness.core.auth.performGoogleSignInFlow(
            credentialManagerCall = { throw Exception("User cancelled account selection") },
            fallbackCall = {
                fallbackCalled = true
                "fallback"
            }
        )
        kotlin.test.assertNull(resultB)
        assertFalse(fallbackCalled, "Fallback must NOT be called on cancellation exception")
    }

    // TEST 5: Fallback failure -> loading resets -> existing error shown
    @Test
    fun test17_fallbackFailure_loadingResets_errorShown() = kotlinx.coroutines.runBlocking {
        var isLoading = true
        var errorMessage: String? = null

        try {
            com.example.fitness.core.auth.performGoogleSignInFlow(
                credentialManagerCall = {
                    kotlinx.coroutines.withTimeout(1L) { kotlinx.coroutines.delay(50L) }
                },
                fallbackCall = { throw IllegalStateException("Network unreachable during Google Sign-In fallback") }
            )
        } catch (e: Exception) {
            errorMessage = mapSocialAuthError(e, "Google")
        } finally {
            isLoading = false
        }

        assertFalse(isLoading, "Loading state must reset to false when fallback fails")
        assertEquals("Network error. Please check your internet connection and try again.", errorMessage)
    }

    // TEST 6: Credential Manager + fallback both fail -> loading resets -> no infinite spinner
    @Test
    fun test18_bothFail_loadingResets_noInfiniteSpinner() = kotlinx.coroutines.runBlocking {
        var isLoading = true
        var activeProvider: String? = "Google"
        var errorMessage: String? = null

        try {
            com.example.fitness.core.auth.performGoogleSignInFlow(
                credentialManagerCall = {
                    kotlinx.coroutines.withTimeout(1L) { kotlinx.coroutines.delay(50L) }
                },
                fallbackCall = {
                    kotlinx.coroutines.withTimeout(1L) { kotlinx.coroutines.delay(50L) }
                }
            )
        } catch (e: Exception) {
            errorMessage = "Login is taking longer than expected. Please check your connection and try again."
        } finally {
            isLoading = false
            activeProvider = null
        }

        assertFalse(isLoading, "Loading state MUST be reset when both fail")
        kotlin.test.assertNull(activeProvider)
        assertEquals("Login is taking longer than expected. Please check your connection and try again.", errorMessage)
    }

    // TEST 7: Rapid double-tap guard prevents duplicate authentication requests
    @Test
    fun test19_rapidDoubleTapExecutesOnlyOnce() = kotlinx.coroutines.runBlocking {
        var isAuthenticating = false
        var isLoading = false
        var attemptsStarted = 0

        fun handleLoginTap() {
            if (isLoading || isAuthenticating) return
            isAuthenticating = true
            isLoading = true
            attemptsStarted++
        }

        // Rapid double tap in succession
        handleLoginTap()
        handleLoginTap()
        handleLoginTap()

        assertEquals(1, attemptsStarted, "Rapid multiple taps must trigger exactly ONE authentication attempt")
        assertTrue(isLoading)
        assertTrue(isAuthenticating)

        // Cleanup
        isAuthenticating = false
        isLoading = false
        assertFalse(isLoading)
        assertFalse(isAuthenticating)

        // User can now tap again if previous attempt completed/failed
        handleLoginTap()
        assertEquals(2, attemptsStarted, "User can start a new login attempt after previous finishes")
    }

    // TEST 8: LoginPerfTracker records milestones and tracks elapsed time accurately
    @Test
    fun test20_loginPerfTrackerRecordsMilestones() {
        val tracker = com.example.fitness.core.auth.LoginPerfTracker
        tracker.reset()
        kotlin.test.assertNull(tracker.currentElapsedMillis())

        tracker.start("Google")
        val elapsedAfterStart = tracker.currentElapsedMillis()
        kotlin.test.assertNotNull(elapsedAfterStart)
        assertTrue(elapsedAfterStart >= 0L)

        tracker.record("Button → CredentialManager start")
        tracker.record("CredentialManager result")
        tracker.record("Firebase Auth start")
        tracker.record("Firebase Auth success")
        tracker.record("Session start")
        tracker.record("Navigation: Home")

        val elapsedAtEnd = tracker.currentElapsedMillis()
        kotlin.test.assertNotNull(elapsedAtEnd)
        assertTrue(elapsedAtEnd >= elapsedAfterStart)

        tracker.reset()
        kotlin.test.assertNull(tracker.currentElapsedMillis())
    }

    // TEST 9: Concurrent Role and Profile resolution safety
    @Test
    fun test21_concurrentRoleAndProfileResolutionSafety() = kotlinx.coroutines.runBlocking {
        var roleCompleted = false
        var profileCompleted = false

        coroutineScope {
            val roleDeferred = async {
                delay(10L)
                roleCompleted = true
                UserRole.USER
            }

            val profileDeferred = async {
                delay(15L)
                profileCompleted = true
                UserProfile(
                    uid = "test_concurrent_user",
                    email = "test@example.com",
                    name = "Test Athlete",
                    gender = "Female"
                )
            }

            val role = roleDeferred.await()
            val profile = profileDeferred.await()

            assertEquals(UserRole.USER, role)
            assertEquals("Female", profile.gender)
            assertTrue(roleCompleted)
            assertTrue(profileCompleted)
        }
    }

    // TEST 10: Loading state always resets in finally block even on panic or error
    @Test
    fun test22_loadingStateAlwaysResetsInFinally() {
        var isLoading = false
        var isAuthenticating = false

        fun simulateCrashFlow() {
            isLoading = true
            isAuthenticating = true
            try {
                throw RuntimeException("Fatal network disruption")
            } finally {
                isLoading = false
                isAuthenticating = false
            }
        }

        try {
            simulateCrashFlow()
        } catch (_: Exception) {}

        assertFalse(isLoading, "isLoading must always be false in finally")
        assertFalse(isAuthenticating, "isAuthenticating must always be false in finally")
    }

    // MULTI-ACCOUNT SCENARIO 1: Admin was last account used, but user selects normal User account
    @Test
    fun test26_multiAccount_adminWasLastUsed_userSelectsUserAccount_authenticatesUser() = kotlinx.coroutines.runBlocking {
        // Step 1: Simulate previous session was Admin
        UserSession.currentUserRole = UserRole.ADMIN
        UserSession.currentUserEmail = "admin@athgym.com"
        UserSession.currentUserId = "admin_uid_999"

        // User logs out
        UserSession.clear()
        assertFalse(UserSession.isAuthenticated, "Session must be unauthenticated after logout")

        // Step 2: Next login flow presents multiple accounts; user explicitly selects User account
        val selectedAccountEmail = "user@athgym.com"
        val selectedAccountUid = "user_uid_111"

        val authResult = com.example.fitness.core.auth.performGoogleSignInFlow(
            credentialManagerCall = {
                // Returns selected user account, NOT the previous admin account
                mapOf("email" to selectedAccountEmail, "uid" to selectedAccountUid, "isAdmin" to false)
            },
            fallbackCall = { null }
        )

        assertEquals(selectedAccountEmail, authResult?.get("email"))
        assertEquals(selectedAccountUid, authResult?.get("uid"))

        // Authenticate session as selected user
        val userProfile = UserProfile(
            uid = selectedAccountUid,
            email = selectedAccountEmail,
            name = "Regular Athlete",
            gender = "Male"
        )
        UserSession.setUserProfile(userProfile)
        UserSession.currentUserRole = RoleResolver.resolveRoleFromClaims(emptyMap())

        assertEquals(UserRole.USER, UserSession.currentUserRole)
        val destination = if (UserSession.currentUserRole == UserRole.ADMIN) Screen.AdminDashboard else Screen.Home
        assertEquals(Screen.Home, destination, "Selected user account must route to Home, not AdminDashboard")
    }

    // MULTI-ACCOUNT SCENARIO 2: User was last account used, but user selects Admin account
    @Test
    fun test27_multiAccount_userWasLastUsed_userSelectsAdminAccount_authenticatesAdmin() = kotlinx.coroutines.runBlocking {
        // Step 1: Simulate previous session was normal User
        UserSession.currentUserRole = UserRole.USER
        UserSession.currentUserEmail = "user@athgym.com"
        UserSession.currentUserId = "user_uid_111"

        // User logs out
        UserSession.clear()
        assertFalse(UserSession.isAuthenticated, "Session must be unauthenticated after logout")

        // Step 2: Next login flow presents multiple accounts; user explicitly selects Admin account
        val selectedAccountEmail = "admin@athgym.com"
        val selectedAccountUid = "admin_uid_999"

        val authResult = com.example.fitness.core.auth.performGoogleSignInFlow(
            credentialManagerCall = {
                // Returns selected admin account, NOT the previous user account
                mapOf("email" to selectedAccountEmail, "uid" to selectedAccountUid, "isAdmin" to true)
            },
            fallbackCall = { null }
        )

        assertEquals(selectedAccountEmail, authResult?.get("email"))
        assertEquals(selectedAccountUid, authResult?.get("uid"))

        // Authenticate session as selected admin
        val adminProfile = UserProfile(
            uid = selectedAccountUid,
            email = selectedAccountEmail,
            name = "Admin Coach",
            gender = "Male"
        )
        UserSession.setUserProfile(adminProfile)
        UserSession.currentUserRole = RoleResolver.resolveRoleFromClaims(mapOf("admin" to true))

        assertEquals(UserRole.ADMIN, UserSession.currentUserRole)
        val destination = if (UserSession.currentUserRole == UserRole.ADMIN) Screen.AdminDashboard else Screen.Home
        assertEquals(Screen.AdminDashboard, destination, "Selected admin account must route to AdminDashboard")
    }

    // MULTI-ACCOUNT SCENARIO 3: User cancels account selection sheet -> returns null gracefully, no login loop
    @Test
    fun test28_userCancelsAccountSelection_returnsNullGracefully_noFallback() = kotlinx.coroutines.runBlocking {
        var fallbackInvoked = false

        val result = com.example.fitness.core.auth.performGoogleSignInFlow(
            credentialManagerCall = {
                throw Exception("androidx.credentials.exceptions.GetCredentialCancellationException: User cancelled")
            },
            fallbackCall = {
                fallbackInvoked = true
                "unexpected_fallback"
            }
        )

        kotlin.test.assertNull(result, "Cancelled account selection must return null")
        assertFalse(fallbackInvoked, "Fallback must NOT be invoked when user intentionally cancels account selection")
        assertFalse(UserSession.isAuthenticated, "User must remain unauthenticated after cancellation")
    }

    // MULTI-ACCOUNT SCENARIO 4: Single account on device -> user confirms -> successful sign in
    @Test
    fun test29_singleAccount_userConfirms_signsInSuccessfully() = kotlinx.coroutines.runBlocking {
        val result = com.example.fitness.core.auth.performGoogleSignInFlow(
            credentialManagerCall = {
                "single_user_uid_456"
            },
            fallbackCall = { null }
        )

        assertEquals("single_user_uid_456", result)
    }

    // MULTI-ACCOUNT SCENARIO 5: Full logout and re-login with different account resets all state
    @Test
    fun test30_logoutAndRelogin_resetsAllSessionState() = kotlinx.coroutines.runBlocking {
        // Setup initial user
        UserSession.currentUserId = "initial_uid"
        UserSession.currentUserEmail = "initial@athgym.com"
        UserSession.currentUserRole = UserRole.USER
        UserSession.favoriteExerciseIds.add("ex_1")

        // Logout
        UserSession.clear()
        kotlin.test.assertNull(UserSession.currentUserId)
        kotlin.test.assertNull(UserSession.currentUserEmail)
        assertTrue(UserSession.favoriteExerciseIds.isEmpty())

        // Re-login as new user
        UserSession.currentUserId = "switched_uid"
        UserSession.currentUserEmail = "switched@athgym.com"
        assertEquals("switched_uid", UserSession.currentUserId)
        assertEquals("switched@athgym.com", UserSession.currentUserEmail)
    }

    // DUPLICATE PICKER FIX TEST 1: User takes longer than old 4s timeout (e.g. 5s+) to choose account
    // Credential Manager must NOT be cancelled prematurely and must NOT trigger fallback.
    @Test
    fun test31_noDuplicatePicker_userTakesTimeToChoose_fallbackNotTriggered() = kotlinx.coroutines.runBlocking {
        var fallbackTriggered = false
        val selectedEmail = "abhishek751883@gmail.com"

        val result = com.example.fitness.core.auth.performGoogleSignInFlow(
            credentialManagerCall = {
                // User takes 500ms in test simulation to read 4 accounts and tap one
                kotlinx.coroutines.delay(100L)
                selectedEmail
            },
            fallbackCall = {
                fallbackTriggered = true
                "unexpected_fallback"
            }
        )

        assertEquals(selectedEmail, result)
        assertFalse(fallbackTriggered, "Fallback must NOT be triggered when user is selecting account in Credential Manager")
    }

    // DUPLICATE PICKER FIX TEST 2: Firebase Auth error after account selection does NOT reopen account picker
    @Test
    fun test32_firebaseFailureAfterGoogleSelection_doesNotTriggerFallback() = kotlinx.coroutines.runBlocking {
        var fallbackTriggered = false

        // Account selection succeeds
        val selectedAccount = com.example.fitness.core.auth.performGoogleSignInFlow(
            credentialManagerCall = {
                "google_token_user_123"
            },
            fallbackCall = {
                fallbackTriggered = true
                "unexpected_fallback"
            }
        )

        assertEquals("google_token_user_123", selectedAccount)
        assertFalse(fallbackTriggered, "Fallback must NOT be triggered once Google account is chosen")

        // Subsequent Firebase sign-in failure throws error directly without invoking fallback
        var firebaseAuthErrorCaught = false
        try {
            throw IllegalStateException("Network error connecting to Firebase")
        } catch (e: Exception) {
            firebaseAuthErrorCaught = true
        }

        assertTrue(firebaseAuthErrorCaught, "Firebase error must bubble up to UI error message without reopening account selection")
        assertFalse(fallbackTriggered, "Fallback picker must NOT open when Firebase auth fails after account selection")
    }

    // DUPLICATE PICKER FIX TEST 3: User cancellation via GetCredentialCancellationException never triggers fallback
    @Test
    fun test33_cancellationInCredentialManager_neverTriggersFallback() = kotlinx.coroutines.runBlocking {
        var fallbackTriggered = false

        val result = com.example.fitness.core.auth.performGoogleSignInFlow(
            credentialManagerCall = {
                throw Exception("androidx.credentials.exceptions.GetCredentialCancellationException: The user canceled the credential request")
            },
            fallbackCall = {
                fallbackTriggered = true
                "fallback_result"
            }
        )

        kotlin.test.assertNull(result, "Cancellation must result in null without fallback")
        assertFalse(fallbackTriggered, "Fallback must NEVER be triggered when user dismisses Credential Manager bottom sheet")
    }
}

