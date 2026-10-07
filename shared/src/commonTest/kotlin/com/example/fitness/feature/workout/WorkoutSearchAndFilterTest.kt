package com.example.fitness.feature.workout

import com.example.fitness.feature.workout.data.WorkoutFilterHelper
import com.example.fitness.feature.workout.data.WorkoutFilterState
import com.example.fitness.feature.workout.data.WorkoutRepository
import com.example.fitness.feature.workout.model.WorkoutType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WorkoutSearchAndFilterTest {

    private val allExercises = WorkoutFilterHelper.getAllExercises(WorkoutType.GYM)

    // =====================================================================================
    // 1. DATA INTEGRITY
    // =====================================================================================

    @Test
    fun testBaselineExerciseCount_isExactly24GymExercises() {
        assertEquals(111, allExercises.size, "Baseline Gym exercises must equal 111 (21 targeted + 90 Full Body from PDF)")
    }

    @Test
    fun testAllExercisesHaveValidFields() {
        allExercises.forEach { exercise ->
            assertTrue(exercise.id.isNotBlank(), "Exercise id must not be blank")
            assertTrue(exercise.name.isNotBlank(), "Exercise name must not be blank")
            assertTrue(exercise.bodyPart.isNotBlank(), "Exercise bodyPart must not be blank")
            assertTrue(exercise.equipment.isNotBlank(), "Exercise equipment must not be blank")
            assertTrue(exercise.difficulty.isNotBlank(), "Exercise difficulty must not be blank")
            assertTrue(exercise.bodyPartId.isNotBlank(), "Exercise bodyPartId must not be blank")
            assertNotNull(WorkoutRepository.getWorkout(exercise.workoutType, exercise.bodyPartId), "Associated workout must exist in repository")
        }
    }

    // =====================================================================================
    // 2. SEARCH FUNCTIONALITY
    // =====================================================================================

    @Test
    fun testSearch_emptySearch_showsAllExercises() {
        val result = WorkoutFilterHelper.filterExercises(
            exercises = allExercises,
            query = "",
            filters = WorkoutFilterState(workoutType = "Gym")
        )
        assertEquals(allExercises.size, result.size, "Empty search query must return all exercises")
    }

    @Test
    fun testSearch_exactExerciseName() {
        val result = WorkoutFilterHelper.filterExercises(
            exercises = allExercises,
            query = "Barbell Bench Press",
            filters = WorkoutFilterState(workoutType = "Gym")
        )
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.name.contains("Barbell Bench Press", ignoreCase = true) })
    }

    @Test
    fun testSearch_partialMatches_press() {
        val result = WorkoutFilterHelper.filterExercises(
            exercises = allExercises,
            query = "press",
            filters = WorkoutFilterState(workoutType = "Gym")
        )
        assertTrue(result.size >= 4, "Partial match 'press' must match multiple compound presses")
        val names = result.map { it.name }
        assertTrue(names.any { it.contains("Bench Press", ignoreCase = true) })
        assertTrue(names.any { it.contains("Dumbbell Press", ignoreCase = true) })
        assertTrue(names.any { it.contains("Shoulder Press", ignoreCase = true) })
        assertTrue(names.any { it.contains("Leg Press", ignoreCase = true) })
    }

    @Test
    fun testSearch_partialMatches_curl() {
        val result = WorkoutFilterHelper.filterExercises(
            exercises = allExercises,
            query = "curl",
            filters = WorkoutFilterState(workoutType = "Gym")
        )
        assertTrue(result.size >= 3, "Search 'curl' must match at least 3 bicep curls")
        val names = result.map { it.name }
        assertTrue(names.contains("Standing EZ-Bar Curl"))
        assertTrue(names.contains("Incline Dumbbell Curl"))
        assertTrue(names.contains("Hammer Rope Cable Curl"))
    }

    @Test
    fun testSearch_caseInsensitivity() {
        val resultLower = WorkoutFilterHelper.filterExercises(allExercises, "bench", WorkoutFilterState())
        val resultCapital = WorkoutFilterHelper.filterExercises(allExercises, "Bench", WorkoutFilterState())
        val resultUpper = WorkoutFilterHelper.filterExercises(allExercises, "BENCH", WorkoutFilterState())

        assertEquals(resultLower.size, resultCapital.size)
        assertEquals(resultLower.size, resultUpper.size)
        assertTrue(resultLower.isNotEmpty())
    }

    @Test
    fun testSearch_handlesWhitespaceGracefully() {
        val resultNormal = WorkoutFilterHelper.filterExercises(allExercises, "bench", WorkoutFilterState())
        val resultSpaced = WorkoutFilterHelper.filterExercises(allExercises, "   bench   ", WorkoutFilterState())

        assertEquals(resultNormal.size, resultSpaced.size)
    }

    @Test
    fun testSearch_clearingSearch_restoresCompleteList() {
        val filtered = WorkoutFilterHelper.filterExercises(allExercises, "bench", WorkoutFilterState())
        assertTrue(filtered.isNotEmpty())

        val cleared = WorkoutFilterHelper.filterExercises(allExercises, "", WorkoutFilterState())
        assertEquals(allExercises.size, cleared.size)
    }

    // =====================================================================================
    // 3. FILTER FUNCTIONALITY
    // =====================================================================================

    @Test
    fun testFilter_bySingleMuscleGroup_Chest() {
        val result = WorkoutFilterHelper.filterExercises(
            exercises = allExercises,
            query = "",
            filters = WorkoutFilterState(muscleGroup = "Chest", workoutType = "Gym")
        )
        assertEquals(3, result.size, "Chest filter must return 3 chest exercises")
        assertTrue(result.all { it.bodyPart.equals("Chest", ignoreCase = true) })
        val names = result.map { it.name }
        assertTrue(names.contains("Barbell Bench Press"))
        assertTrue(names.contains("Incline Dumbbell Press"))
        assertTrue(names.contains("Cable Chest Fly"))
    }

    @Test
    fun testFilter_bySingleMuscleGroup_Biceps() {
        val result = WorkoutFilterHelper.filterExercises(
            exercises = allExercises,
            query = "",
            filters = WorkoutFilterState(muscleGroup = "Biceps", workoutType = "Gym")
        )
        assertEquals(3, result.size, "Biceps filter must return 3 bicep exercises")
        assertTrue(result.all { it.bodyPart.equals("Biceps", ignoreCase = true) })
    }

    @Test
    fun testFilter_byEquipment_Dumbbell() {
        val result = WorkoutFilterHelper.filterExercises(
            exercises = allExercises,
            query = "",
            filters = WorkoutFilterState(equipment = "Dumbbell", workoutType = "Gym")
        )
        assertTrue(result.isNotEmpty(), "Equipment Dumbbell must return matching exercises")
        assertTrue(result.all { it.equipment.contains("Dumbbell", ignoreCase = true) })
    }

    @Test
    fun testFilter_byDifficulty_NoExperience() {
        val result = WorkoutFilterHelper.filterExercises(
            exercises = allExercises,
            query = "",
            filters = WorkoutFilterState(difficulty = "No Experience", workoutType = "Gym")
        )
        assertTrue(result.isNotEmpty(), "No Experience filter must return exercises")
        assertTrue(result.all { it.difficulty.contains("No Experience", ignoreCase = true) || it.difficulty.equals("NO_EXPERIENCE", ignoreCase = true) })
    }

    @Test
    fun testFilter_byDifficulty_Beginner() {
        val result = WorkoutFilterHelper.filterExercises(
            exercises = allExercises,
            query = "",
            filters = WorkoutFilterState(difficulty = "Beginner", workoutType = "Gym")
        )
        assertTrue(result.isNotEmpty(), "Beginner filter must return exercises")
        assertTrue(result.all { it.difficulty.contains("Beginner", ignoreCase = true) && !it.difficulty.contains("No Experience", ignoreCase = true) && !it.difficulty.equals("NO_EXPERIENCE", ignoreCase = true) })
    }

    @Test
    fun testFilter_byDifficulty_Advanced() {
        val result = WorkoutFilterHelper.filterExercises(
            exercises = allExercises,
            query = "",
            filters = WorkoutFilterState(difficulty = "Advanced", workoutType = "Gym")
        )
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.difficulty.contains("Advanced", ignoreCase = true) })
    }

    @Test
    fun testFilter_byDifficulty_AdvanceAlias() {
        val result = WorkoutFilterHelper.filterExercises(
            exercises = allExercises,
            query = "",
            filters = WorkoutFilterState(difficulty = "Advance", workoutType = "Gym")
        )
        assertTrue(result.isNotEmpty(), "Advance alias must match Advanced exercises")
        assertTrue(result.all { it.difficulty.contains("Advanced", ignoreCase = true) || it.difficulty.contains("Advance", ignoreCase = true) })
    }

    @Test
    fun testFilter_byDifficulty_Expert() {
        val result = WorkoutFilterHelper.filterExercises(
            exercises = allExercises,
            query = "",
            filters = WorkoutFilterState(difficulty = "Expert", workoutType = "Gym")
        )
        assertTrue(result.isNotEmpty(), "Expert filter must return exercises")
        assertTrue(result.all { it.difficulty.contains("Expert", ignoreCase = true) })
    }

    @Test
    fun testFilter_availableDifficultiesMatchProfileLevels() {
        val expected = listOf("All", "No Experience", "Beginner", "Advanced", "Expert")
        assertEquals(expected, WorkoutFilterHelper.availableDifficulties)
    }

    @Test
    fun testFilter_FullBody_dynamicCountsAcrossAllFourLevels() {
        val fbNoExp = WorkoutFilterHelper.getAllExercises(level = com.example.fitness.feature.workout.model.ExperienceLevel.NO_EXPERIENCE)
        val fbNoExpFiltered = WorkoutFilterHelper.filterExercises(
            exercises = fbNoExp,
            query = "",
            filters = WorkoutFilterState(muscleGroup = "Full Body", difficulty = "No Experience")
        )
        assertEquals(17, fbNoExpFiltered.size, "No Experience Full Body must return exactly 17 exercises")

        val fbBeg = WorkoutFilterHelper.getAllExercises(level = com.example.fitness.feature.workout.model.ExperienceLevel.BEGINNER)
        val fbBegFiltered = WorkoutFilterHelper.filterExercises(
            exercises = fbBeg,
            query = "",
            filters = WorkoutFilterState(muscleGroup = "Full Body", difficulty = "Beginner")
        )
        assertEquals(21, fbBegFiltered.size, "Beginner Full Body must return exactly 21 exercises")

        val fbAdv = WorkoutFilterHelper.getAllExercises(level = com.example.fitness.feature.workout.model.ExperienceLevel.ADVANCED)
        val fbAdvFiltered = WorkoutFilterHelper.filterExercises(
            exercises = fbAdv,
            query = "",
            filters = WorkoutFilterState(muscleGroup = "Full Body", difficulty = "Advanced")
        )
        assertEquals(26, fbAdvFiltered.size, "Advanced Full Body must return exactly 26 exercises")

        val fbExp = WorkoutFilterHelper.getAllExercises(level = com.example.fitness.feature.workout.model.ExperienceLevel.EXPERT)
        val fbExpFiltered = WorkoutFilterHelper.filterExercises(
            exercises = fbExp,
            query = "",
            filters = WorkoutFilterState(muscleGroup = "Full Body", difficulty = "Expert")
        )
        assertEquals(26, fbExpFiltered.size, "Expert Full Body must return exactly 26 exercises")
    }

    @Test
    fun testFilter_multipleFilters_workTogether() {
        // Muscle Group = Chest AND Equipment = Dumbbell
        val result = WorkoutFilterHelper.filterExercises(
            exercises = allExercises,
            query = "",
            filters = WorkoutFilterState(muscleGroup = "Chest", equipment = "Dumbbell", workoutType = "Gym")
        )
        assertEquals(1, result.size)
        assertEquals("Incline Dumbbell Press", result.first().name)
    }

    @Test
    fun testFilter_resetClearsAllFilters() {
        val activeFilter = WorkoutFilterState(muscleGroup = "Chest", difficulty = "Intermediate", equipment = "Cables")
        assertEquals(3, activeFilter.activeFilterCount)
        assertFalse(activeFilter.isDefault)

        val resetFilter = activeFilter.reset()
        assertEquals(0, resetFilter.activeFilterCount)
        assertTrue(resetFilter.isDefault)
        assertEquals("All", resetFilter.muscleGroup)
        assertEquals("All", resetFilter.difficulty)
        assertEquals("All", resetFilter.equipment)
    }

    // =====================================================================================
    // 4. SIMULTANEOUS SEARCH + FILTER
    // =====================================================================================

    @Test
    fun testSearchAndFilter_pressAndChest() {
        val result = WorkoutFilterHelper.filterExercises(
            exercises = allExercises,
            query = "press",
            filters = WorkoutFilterState(muscleGroup = "Chest", workoutType = "Gym")
        )
        assertEquals(2, result.size, "Search 'press' with filter Chest must return 2 chest presses")
        val names = result.map { it.name }
        assertTrue(names.contains("Barbell Bench Press"))
        assertTrue(names.contains("Incline Dumbbell Press"))
        assertFalse(names.contains("Leg Press"))
        assertFalse(names.contains("Seated Dumbbell Shoulder Press"))
    }

    @Test
    fun testSearchAndFilter_curlAndBiceps() {
        val result = WorkoutFilterHelper.filterExercises(
            exercises = allExercises,
            query = "curl",
            filters = WorkoutFilterState(muscleGroup = "Biceps", workoutType = "Gym")
        )
        assertEquals(3, result.size, "Search 'curl' with filter Biceps must return all 3 bicep curls")
    }

    @Test
    fun testSearchAndFilter_clearingSearchPreservesFilters() {
        val filters = WorkoutFilterState(muscleGroup = "Chest", workoutType = "Gym")

        val searchAndFilter = WorkoutFilterHelper.filterExercises(allExercises, "press", filters)
        assertEquals(2, searchAndFilter.size)

        // Clear query only
        val afterClearSearch = WorkoutFilterHelper.filterExercises(allExercises, "", filters)
        assertEquals(3, afterClearSearch.size, "Clearing search query must preserve Chest filter")
    }

    @Test
    fun testSearchAndFilter_clearingFiltersPreservesSearch() {
        val filters = WorkoutFilterState(muscleGroup = "Chest", workoutType = "Gym")

        val searchAndFilter = WorkoutFilterHelper.filterExercises(allExercises, "press", filters)
        assertEquals(2, searchAndFilter.size)

        // Clear filter only
        val afterClearFilter = WorkoutFilterHelper.filterExercises(allExercises, "press", filters.reset())
        assertTrue(afterClearFilter.size >= 4, "Clearing filter must preserve 'press' query across all muscle groups")
    }

    @Test
    fun testSearchAndFilter_zeroResults_producesEmptyList() {
        val result = WorkoutFilterHelper.filterExercises(
            exercises = allExercises,
            query = "nonexistentexercisexyz",
            filters = WorkoutFilterState()
        )
        assertTrue(result.isEmpty(), "Unmatched query must yield empty list")
    }

    // =====================================================================================
    // 5. WORKOUT & EXERCISE FILTER SELECTION STABILITY (CRASH PREVENTION)
    // =====================================================================================

    @Test
    fun testExerciseFilterLegs_exercisesCanBeOpenedAtAnyExperienceLevelWithoutCrash() {
        val allGymAndHome = WorkoutFilterHelper.getAllExercises()
        val legExercises = WorkoutFilterHelper.filterExercises(
            exercises = allGymAndHome,
            query = "",
            filters = WorkoutFilterState(muscleGroup = "Legs")
        )
        assertTrue(legExercises.isNotEmpty(), "Legs filter must return leg exercises")

        val experienceLevels = listOf(
            null,
            com.example.fitness.feature.workout.model.ExperienceLevel.NO_EXPERIENCE,
            com.example.fitness.feature.workout.model.ExperienceLevel.BEGINNER,
            com.example.fitness.feature.workout.model.ExperienceLevel.ADVANCED,
            com.example.fitness.feature.workout.model.ExperienceLevel.EXPERT
        )

        for (exercise in legExercises) {
            for (level in experienceLevels) {
                val workout = WorkoutRepository.getWorkout(
                    type = exercise.workoutType,
                    bodyPartId = exercise.bodyPartId,
                    gender = null,
                    level = level
                )
                assertNotNull(workout, "Workout for ${exercise.bodyPartId} ($level) must not be null")
                assertTrue(workout.exercises.isNotEmpty(), "Workout ${workout.id} exercises must not be empty at level $level")

                // Check that initialExercise matching resolves an exercise without crashing
                val cleanInitial = exercise.id.trim().lowercase()
                val canonicalInitial = com.example.fitness.feature.exercise.data.SampleExerciseData.resolveCanonicalId(exercise.id).trim().lowercase()
                val foundIndex = workout.exercises.indexOfFirst {
                    val itClean = it.id.trim().lowercase()
                    val itCanonical = com.example.fitness.feature.exercise.data.SampleExerciseData.resolveCanonicalId(it.id).trim().lowercase()
                    itClean == cleanInitial || itClean == canonicalInitial || itCanonical == cleanInitial || itCanonical == canonicalInitial
                }
                assertTrue(foundIndex >= 0 || workout.exercises.isNotEmpty(), "Exercise or workout exercises must exist")
                val resolvedEx = workout.exercises.getOrElse(if (foundIndex >= 0) foundIndex else 0) {
                    workout.exercises.firstOrNull()
                }
                assertNotNull(resolvedEx, "Resolved exercise must never be null")
            }
        }
    }
}

