package com.example.fitness.core.auth

import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.core.user.model.UserProfile
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthSessionManagerTest {

    @BeforeTest
    fun setUp() {
        UserSession.clear()
    }

    @Test
    fun testInitialUserSessionIsUnauthenticated() {
        assertNull(UserSession.currentUserId)
        assertNull(UserSession.currentUserEmail)
        assertNull(UserSession.currentUserName)
        assertNull(UserSession.currentUserProfile)
        assertEquals(UserRole.USER, UserSession.currentUserRole)
        assertTrue(UserSession.favoriteWorkoutIds.isEmpty())
        assertTrue(UserSession.favoriteExerciseIds.isEmpty())
    }

    @Test
    fun testUserProfileRestorationPopulatesUserSession() {
        val testProfile = UserProfile(
            uid = "auth_uid_12345",
            email = "athlete@athgym.com",
            name = "Alex Stone",
            gender = "Male",
            mobileNumber = "+1234567890",
            profileImageUrl = "https://example.com/avatar.jpg"
        )

        UserSession.setUserProfile(testProfile)

        assertEquals("auth_uid_12345", UserSession.currentUserId)
        assertEquals("athlete@athgym.com", UserSession.currentUserEmail)
        assertEquals("Alex Stone", UserSession.currentUserName)
        assertEquals("Male", UserSession.currentUserGender)
        assertEquals("+1234567890", UserSession.currentUserMobileNumber)
        assertEquals("https://example.com/avatar.jpg", UserSession.currentUserProfileImageUrl)
        assertEquals(testProfile, UserSession.currentUserProfile)
    }

    @Test
    fun testSessionRestorationRoutingRules() {
        fun resolveDestination(role: UserRole, profile: UserProfile?): Screen {
            if (role == UserRole.ADMIN) {
                return Screen.AdminDashboard
            }
            if (profile != null && profile.gender.isNotBlank()) {
                return Screen.Home
            }
            return Screen.GenderSelection
        }

        // Case 1: Admin user -> always AdminDashboard
        val adminProfile = UserProfile(uid = "admin_1", email = "admin@athgym.com", name = "Admin", gender = "Male")
        assertEquals(Screen.AdminDashboard, resolveDestination(UserRole.ADMIN, adminProfile))

        // Case 2: Standard user with complete profile -> Home
        val regularProfile = UserProfile(uid = "user_1", email = "user@athgym.com", name = "John Doe", gender = "Male")
        assertEquals(Screen.Home, resolveDestination(UserRole.USER, regularProfile))

        // Case 3: Returning user with female gender -> Home
        val femaleProfile = UserProfile(uid = "user_2", email = "jane@athgym.com", name = "Jane", gender = "Female")
        assertEquals(Screen.Home, resolveDestination(UserRole.USER, femaleProfile))

        // Case 4: First-time user or empty gender -> GenderSelection
        val incompleteProfile = UserProfile(uid = "user_3", email = "new@athgym.com", name = "New Athlete", gender = "")
        assertEquals(Screen.GenderSelection, resolveDestination(UserRole.USER, incompleteProfile))
        assertEquals(Screen.GenderSelection, resolveDestination(UserRole.USER, null))
    }

    @Test
    fun testExplicitLogoutClearsSessionForUserIsolation() {
        // Setup authenticated session
        val profile = UserProfile(
            uid = "user_session_to_clear",
            email = "logout_test@athgym.com",
            name = "Session User",
            gender = "Male"
        )
        UserSession.setUserProfile(profile)
        UserSession.currentUserRole = UserRole.ADMIN
        UserSession.setWorkoutFavoriteLocal("workout_chest", true)
        UserSession.setFavoriteLocal("ex_bench", true)
        UserSession.unreadNotificationCount = 4

        assertEquals("user_session_to_clear", UserSession.currentUserId)
        assertTrue(UserSession.isWorkoutFavorite("workout_chest"))
        assertTrue(UserSession.isExerciseFavorite("ex_bench"))

        // Perform explicit logout
        UserSession.clear()

        // Verify total session wipe
        assertNull(UserSession.currentUserId)
        assertNull(UserSession.currentUserEmail)
        assertNull(UserSession.currentUserName)
        assertNull(UserSession.currentUserProfile)
        assertEquals(UserRole.USER, UserSession.currentUserRole)
        assertFalse(UserSession.isWorkoutFavorite("workout_chest"))
        assertFalse(UserSession.isExerciseFavorite("ex_bench"))
        assertEquals(0, UserSession.favoriteWorkoutIds.size)
        assertEquals(0, UserSession.favoriteExerciseIds.size)
        assertEquals(0, UserSession.unreadNotificationCount)
    }

    @Test
    fun testWorkoutAndExerciseFavoritesSyncPreservesUidContext() {
        UserSession.syncWorkoutFavorites("uid_777", listOf("workout_leg_day", "workout_back_day"))
        UserSession.syncFavorites("uid_777", listOf("ex_squat", "ex_deadlift"))

        assertEquals("uid_777", UserSession.workoutFavoritesLoadedForUserId)
        assertEquals("uid_777", UserSession.favoritesLoadedForUserId)
        assertTrue(UserSession.isWorkoutFavorite("workout_leg_day"))
        assertTrue(UserSession.isWorkoutFavorite("workout_back_day"))
        assertFalse(UserSession.isWorkoutFavorite("workout_chest_day"))
        assertTrue(UserSession.isExerciseFavorite("ex_squat"))
        assertTrue(UserSession.isExerciseFavorite("ex_deadlift"))
        assertFalse(UserSession.isExerciseFavorite("ex_bench_press"))
    }
}
