package com.example.fitness.feature.workout

import com.example.fitness.core.session.UserSession
import com.example.fitness.core.util.TitleFormatter
import com.example.fitness.feature.program.data.Canonical30DayProgramData
import com.example.fitness.feature.workout.data.FavoriteWorkoutRecord
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkoutDetailFavoriteAndRotationTest {

    @BeforeTest
    fun setUp() {
        UserSession.clear()
    }

    @Test
    fun testStripRotationFromParentheses() {
        assertEquals("Chest + Triceps", TitleFormatter.stripRotation("Chest + Triceps (Rotation A)"))
        assertEquals("Back + Biceps", TitleFormatter.stripRotation("Back + Biceps (Rotation B)"))
        assertEquals("Legs", TitleFormatter.stripRotation("Legs (Rotation C)"))
        assertEquals("Shoulders", TitleFormatter.stripRotation("Shoulders (Rotation D)"))
    }

    @Test
    fun testStripRotationFromDaySchedules() {
        assertEquals("Day 1 — Chest + Triceps", TitleFormatter.stripRotation("Day 1 — Chest + Triceps (Rotation A)"))
        assertEquals("Day 15 — Chest + Triceps", TitleFormatter.stripRotation("Day 15 — Chest + Triceps (Rotation B)"))
        assertEquals("Day 22 — Chest + Triceps", TitleFormatter.stripRotation("Day 22 — Chest + Triceps (Rotation C)"))
        assertEquals("Day 29 — Chest + Triceps", TitleFormatter.stripRotation("Day 29 — Chest + Triceps (Rotation D)"))
    }

    @Test
    fun testStripRotationWithoutParentheses() {
        assertEquals("Chest + Triceps", TitleFormatter.stripRotation("Chest + Triceps Rotation A"))
        assertEquals("Day 1 — Chest + Triceps", TitleFormatter.stripRotation("Day 1 — Chest + Triceps Rotation B"))
    }

    @Test
    fun testPreserveLegitimateRotationalExercises() {
        // Exercise names and descriptions containing "rotation" must NOT be corrupted
        assertEquals("Thoracic Spine Rotations", TitleFormatter.stripRotation("Thoracic Spine Rotations"))
        assertEquals("Rotational Medicine Ball Wall Slams", TitleFormatter.stripRotation("Rotational Medicine Ball Wall Slams"))
        assertEquals("Cuban Rotations", TitleFormatter.stripRotation("Cuban Rotations"))
        assertEquals("90/90 Hip Mobility Rotations", TitleFormatter.stripRotation("90/90 Hip Mobility Rotations"))
        assertEquals("Full Body Blast", TitleFormatter.stripRotation("Full Body Blast"))
    }

    @Test
    fun testStripRotationEdgeCases() {
        assertEquals("", TitleFormatter.stripRotation(null))
        assertEquals("", TitleFormatter.stripRotation(""))
        assertEquals("", TitleFormatter.stripRotation("   "))
    }

    @Test
    fun testUserSessionWorkoutFavoritesLocalToggle() {
        assertFalse(UserSession.isWorkoutFavorite("day_1"))

        UserSession.setWorkoutFavoriteLocal("day_1", true)
        assertTrue(UserSession.isWorkoutFavorite("day_1"))

        // Duplicate add is idempotent
        UserSession.setWorkoutFavoriteLocal("day_1", true)
        assertTrue(UserSession.isWorkoutFavorite("day_1"))
        assertEquals(1, UserSession.favoriteWorkoutIds.size)

        UserSession.setWorkoutFavoriteLocal("day_1", false)
        assertFalse(UserSession.isWorkoutFavorite("day_1"))
        assertEquals(0, UserSession.favoriteWorkoutIds.size)
    }

    @Test
    fun testUserSessionSyncWorkoutFavorites() {
        assertNull(UserSession.workoutFavoritesLoadedForUserId)

        val initialFavorites = listOf("day_1", "day_4", "day_8")
        UserSession.syncWorkoutFavorites("user_abc", initialFavorites)

        assertEquals("user_abc", UserSession.workoutFavoritesLoadedForUserId)
        assertTrue(UserSession.isWorkoutFavorite("day_1"))
        assertTrue(UserSession.isWorkoutFavorite("day_4"))
        assertTrue(UserSession.isWorkoutFavorite("day_8"))
        assertFalse(UserSession.isWorkoutFavorite("day_2"))
    }

    @Test
    fun testUserSessionClearWipesWorkoutFavoritesForUserIsolation() {
        UserSession.syncWorkoutFavorites("user_abc", listOf("day_1", "day_2"))
        assertTrue(UserSession.isWorkoutFavorite("day_1"))
        assertEquals("user_abc", UserSession.workoutFavoritesLoadedForUserId)

        // User logout / clear
        UserSession.clear()

        assertFalse(UserSession.isWorkoutFavorite("day_1"))
        assertNull(UserSession.workoutFavoritesLoadedForUserId)
        assertEquals(0, UserSession.favoriteWorkoutIds.size)
    }

    @Test
    fun testFavoriteWorkoutRecordModel() {
        val record = FavoriteWorkoutRecord(
            id = "day_1",
            workoutId = "day_1",
            programId = "30-day",
            title = "Chest + Triceps",
            favoritedAt = 1700000000000L
        )
        assertEquals("day_1", record.id)
        assertEquals("day_1", record.workoutId)
        assertEquals("30-day", record.programId)
        assertEquals("Chest + Triceps", record.title)
        assertEquals(1700000000000L, record.favoritedAt)
    }

    @Test
    fun testInternalCanonical30DayProgramDataRetainsRotationIntegrity() {
        val day1 = Canonical30DayProgramData.PROGRAM_BEGINNER.days.first { it.dayNumber == 1 }
        val day15 = Canonical30DayProgramData.PROGRAM_BEGINNER.days.first { it.dayNumber == 15 }

        // Internal data models retain rotation markers for progression/overload logic
        assertTrue(day1.title.contains("Rotation A"))
        assertTrue(day15.title.contains("Rotation B"))

        // Presentation layer cleanly strips it
        val day1Display = TitleFormatter.stripRotation(day1.title)
        val day15Display = TitleFormatter.stripRotation(day15.title)

        assertEquals("Day 1 — Chest + Triceps", day1Display)
        assertEquals("Day 15 — Chest + Triceps", day15Display)
        assertFalse(day1Display.contains("Rotation"))
        assertFalse(day15Display.contains("Rotation"))
    }
}
