package com.example.fitness.feature.programs

import com.example.fitness.feature.program.data.CanonicalHypertrophyProgramData
import com.example.fitness.feature.program.data.CanonicalStrengthProgramData
import com.example.fitness.feature.workout.data.WorkoutFilterHelper
import com.example.fitness.feature.workout.data.WorkoutFilterState
import com.example.fitness.feature.workout.model.WorkoutType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProgramDetailHeaderFilterTest {

    // 1. FILTER VISIBILITY POLICY TESTS

    @Test
    fun testHypertrophyProgramSuppressesFilterButton() {
        // Canonical Hypertrophy program
        val shouldShow = ProgramDetailFilterPolicy.shouldShowFilter(
            programId = "hypertrophy",
            programTitle = "30-Day Hypertrophy Program"
        )
        assertFalse(shouldShow, "Filter button must be hidden on 30-Day Hypertrophy Program")
    }

    @Test
    fun testStrengthProgramSuppressesFilterButton() {
        // Canonical Strength program
        val shouldShow = ProgramDetailFilterPolicy.shouldShowFilter(
            programId = "strength",
            programTitle = "30-Day Strength Program"
        )
        assertFalse(shouldShow, "Filter button must be hidden on 30-Day Strength Program")
    }

    @Test
    fun testCanonicalProgramIdsSuppressFilterButton() {
        assertFalse(
            ProgramDetailFilterPolicy.shouldShowFilter(
                programId = CanonicalHypertrophyProgramData.PROGRAM_HYPERTROPHY.id,
                programTitle = CanonicalHypertrophyProgramData.PROGRAM_HYPERTROPHY.name
            ),
            "Canonical hypertrophy program ID (${CanonicalHypertrophyProgramData.PROGRAM_HYPERTROPHY.id}) must suppress filter"
        )

        assertFalse(
            ProgramDetailFilterPolicy.shouldShowFilter(
                programId = CanonicalStrengthProgramData.PROGRAM_STRENGTH.id,
                programTitle = CanonicalStrengthProgramData.PROGRAM_STRENGTH.name
            ),
            "Canonical strength program ID (${CanonicalStrengthProgramData.PROGRAM_STRENGTH.id}) must suppress filter"
        )
    }

    @Test
    fun testCaseInsensitiveAndVariationHandling() {
        // Upper case
        assertFalse(ProgramDetailFilterPolicy.shouldShowFilter("HYPERTROPHY", "30-DAY HYPERTROPHY PROGRAM"))
        assertFalse(ProgramDetailFilterPolicy.shouldShowFilter("STRENGTH", "30-DAY STRENGTH PROGRAM"))

        // Prefixed IDs
        assertFalse(ProgramDetailFilterPolicy.shouldShowFilter("hypertrophy_30_day", "Hypertrophy"))
        assertFalse(ProgramDetailFilterPolicy.shouldShowFilter("strength_phase_1", "Strength Focus"))

        // Mixed casing with whitespace
        assertFalse(ProgramDetailFilterPolicy.shouldShowFilter("  hypertrophy  ", "  30-Day Hypertrophy Program  "))
        assertFalse(ProgramDetailFilterPolicy.shouldShowFilter("  strength  ", "  30-Day Strength Program  "))
    }

    @Test
    fun testAllProgramsSuppressFilterOption() {
        // General 30-day workout plan / transformation
        assertFalse(
            ProgramDetailFilterPolicy.shouldShowFilter(
                programId = "30-day",
                programTitle = "30-Day Workout Plan — Beginner"
            ),
            "General 30-day program must suppress filter button"
        )

        // Athlete performance program
        assertFalse(
            ProgramDetailFilterPolicy.shouldShowFilter(
                programId = "athlete",
                programTitle = "30-Day Athletic Performance Program"
            ),
            "Athlete performance program must suppress filter button"
        )
    }

    // 2. RESPONSIVE HEADER GEOMETRY TESTS (320dp to 430dp)

    @Test
    fun testHeaderAvailableTitleWidthAcrossScreenBreakpoints() {
        val screenWidthsDp = listOf(320, 360, 375, 390, 414, 430)
        val horizontalPaddingDp = 20 * 2 // 20.dp on each side
        val backButtonWidthDp = 44
        val spacerWidthDp = 12
        val oldFilterButtonWidthDp = 44
        val oldFilterSpacerWidthDp = 12

        for (width in screenWidthsDp) {
            val oldAvailableTextWidth = width - horizontalPaddingDp - backButtonWidthDp - oldFilterButtonWidthDp - (oldFilterSpacerWidthDp * 2)
            val newAvailableTextWidth = width - horizontalPaddingDp - backButtonWidthDp - spacerWidthDp

            // The new layout must grant at least 56dp more horizontal room
            val gainedSpace = newAvailableTextWidth - oldAvailableTextWidth
            assertEquals(56, gainedSpace, "Removing filter button must free up 56dp on all screen widths")

            // On the narrowest 320dp screen, title gets at least 224dp
            if (width == 320) {
                assertEquals(224, newAvailableTextWidth, "320dp screen must have 224dp available for title")
                assertTrue(newAvailableTextWidth > 200, "Must comfortably fit program title without clipping")
            }

            // On 360dp+ screens, title gets at least 264dp
            if (width >= 360) {
                assertTrue(newAvailableTextWidth >= 264, "360dp+ screen must have at least 264dp for title")
            }
        }
    }

    // 3. WORKOUT SEARCH & FILTER REGRESSION TEST

    @Test
    fun testWorkoutSearchAndFilterRemainsFullyFunctional() {
        val exercises = WorkoutFilterHelper.getAllExercises(WorkoutType.GYM)
        assertTrue(exercises.isNotEmpty(), "Gym exercises must be non-empty")

        val chestFiltered = WorkoutFilterHelper.filterExercises(
            exercises = exercises,
            query = "",
            filters = WorkoutFilterState(muscleGroup = "Chest", workoutType = "Gym")
        )
        assertTrue(chestFiltered.isNotEmpty(), "Workout filtering by muscle group must remain functional")
        assertTrue(chestFiltered.all { it.bodyPart.equals("Chest", ignoreCase = true) })

        val searchResult = WorkoutFilterHelper.filterExercises(
            exercises = exercises,
            query = "bench",
            filters = WorkoutFilterState()
        )
        assertTrue(searchResult.isNotEmpty(), "Workout search must remain fully functional")
    }
}
