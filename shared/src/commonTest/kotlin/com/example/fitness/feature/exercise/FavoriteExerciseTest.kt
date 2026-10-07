package com.example.fitness.feature.exercise

import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.exercise.data.FavoriteExerciseRecord
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FavoriteExerciseTest {

    @BeforeTest
    fun setup() {
        UserSession.clear()
    }

    @Test
    fun testFavoriteExercisePerExerciseIsolation() {
        val benchPressId = "gym_chest_1"
        val bicepCurlId = "gym_bi_1"
        val squatId = "gym_leg_1"

        // Initially none are favorited
        assertFalse(UserSession.isExerciseFavorite(benchPressId))
        assertFalse(UserSession.isExerciseFavorite(bicepCurlId))
        assertFalse(UserSession.isExerciseFavorite(squatId))

        // Favorite Bench Press
        UserSession.setFavoriteLocal(benchPressId, true)

        // Bench Press is ON, others remain OFF
        assertTrue(UserSession.isExerciseFavorite(benchPressId))
        assertFalse(UserSession.isExerciseFavorite(bicepCurlId))
        assertFalse(UserSession.isExerciseFavorite(squatId))

        // Favorite Squat
        UserSession.setFavoriteLocal(squatId, true)
        assertTrue(UserSession.isExerciseFavorite(benchPressId))
        assertFalse(UserSession.isExerciseFavorite(bicepCurlId))
        assertTrue(UserSession.isExerciseFavorite(squatId))

        // Unfavorite Bench Press
        UserSession.setFavoriteLocal(benchPressId, false)
        assertFalse(UserSession.isExerciseFavorite(benchPressId))
        assertFalse(UserSession.isExerciseFavorite(bicepCurlId))
        assertTrue(UserSession.isExerciseFavorite(squatId))
    }

    @Test
    fun testFavoriteUserSessionIsolationAndLogout() {
        val userAUid = "user_A_123"
        val userBUid = "user_B_456"

        // User A syncs favorites
        UserSession.syncFavorites(userAUid, listOf("gym_chest_1", "gym_chest_2"))
        assertEquals(userAUid, UserSession.favoritesLoadedForUserId)
        assertTrue(UserSession.isExerciseFavorite("gym_chest_1"))
        assertTrue(UserSession.isExerciseFavorite("gym_chest_2"))
        assertFalse(UserSession.isExerciseFavorite("gym_chest_3"))

        // User A logs out
        UserSession.clear()

        // Cache must be wiped
        assertEquals(null, UserSession.favoritesLoadedForUserId)
        assertFalse(UserSession.isExerciseFavorite("gym_chest_1"))
        assertFalse(UserSession.isExerciseFavorite("gym_chest_2"))
        assertTrue(UserSession.favoriteExerciseIds.isEmpty())

        // User B logs in with different favorites
        UserSession.syncFavorites(userBUid, listOf("gym_chest_3"))
        assertEquals(userBUid, UserSession.favoritesLoadedForUserId)
        assertFalse(UserSession.isExerciseFavorite("gym_chest_1"))
        assertFalse(UserSession.isExerciseFavorite("gym_chest_2"))
        assertTrue(UserSession.isExerciseFavorite("gym_chest_3"))
    }

    @Test
    fun testOptimisticRollbackBehavior() {
        val exerciseId = "gym_chest_1"

        // Initially false
        assertFalse(UserSession.isExerciseFavorite(exerciseId))

        // Optimistic update to true
        val targetState = true
        UserSession.setFavoriteLocal(exerciseId, targetState)
        assertTrue(UserSession.isExerciseFavorite(exerciseId))

        // Simulate backend failure -> rollback to false
        UserSession.setFavoriteLocal(exerciseId, !targetState)
        assertFalse(UserSession.isExerciseFavorite(exerciseId))
    }

    @Test
    fun testFavoriteExerciseRecordModel() {
        val record = FavoriteExerciseRecord(
            exerciseId = "gym_chest_1",
            favoritedAt = 1718000000000L
        )

        assertEquals("gym_chest_1", record.exerciseId)
        assertEquals(1718000000000L, record.favoritedAt)

        val map = record.toMap()
        assertEquals("gym_chest_1", map["exerciseId"])
        assertEquals(1718000000000L, map["favoritedAt"])
    }
}
