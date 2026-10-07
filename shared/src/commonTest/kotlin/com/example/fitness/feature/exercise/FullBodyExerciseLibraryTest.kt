package com.example.fitness.feature.exercise

import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.admin.data.AdminExerciseCatalogOrganizer
import com.example.fitness.feature.admin.data.AdminWorkoutCatalogProvider
import com.example.fitness.feature.admin.data.ExerciseBodyPartSection
import com.example.fitness.feature.exercise.data.FullBodyExerciseLibrary
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.model.ExerciseDifficulty
import com.example.fitness.feature.workout.data.WorkoutFilterHelper
import com.example.fitness.feature.workout.data.WorkoutFilterState
import com.example.fitness.feature.workout.data.WorkoutRepository
import com.example.fitness.feature.workout.model.ExperienceLevel
import com.example.fitness.feature.workout.model.WorkoutType
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FullBodyExerciseLibraryTest {

    @BeforeTest
    fun setUp() {
        SampleExerciseData.resetOverridesForTesting()
        UserSession.clear()
    }

    @AfterTest
    fun tearDown() {
        SampleExerciseData.resetOverridesForTesting()
        UserSession.clear()
    }

    // =========================================================================
    // 1. EXACT PRESCRIBED EXERCISE COUNTS (PDF Pages 2–5)
    // =========================================================================

    @Test
    fun testExactExerciseCountsPerLevel() {
        assertEquals(17, FullBodyExerciseLibrary.NO_EXPERIENCE_EXERCISES.size, "No Experience level must contain exactly 17 exercises (PDF Page 2)")
        assertEquals(21, FullBodyExerciseLibrary.BEGINNER_EXERCISES.size, "Beginner level must contain exactly 21 exercises (PDF Page 3)")
        assertEquals(26, FullBodyExerciseLibrary.ADVANCED_EXERCISES.size, "Advanced level must contain exactly 26 exercises (PDF Page 4)")
        assertEquals(26, FullBodyExerciseLibrary.EXPERT_EXERCISES.size, "Expert level must contain exactly 26 exercises (PDF Page 5)")
        assertEquals(90, FullBodyExerciseLibrary.ALL_FULL_BODY_EXERCISES.size, "Total canonical Full Body library must contain exactly 90 exercises")
    }

    @Test
    fun testGetExercisesForLevelMatchesExactCounts() {
        assertEquals(17, FullBodyExerciseLibrary.getExercisesForLevel(ExperienceLevel.NO_EXPERIENCE).size)
        assertEquals(21, FullBodyExerciseLibrary.getExercisesForLevel(ExperienceLevel.BEGINNER).size)
        assertEquals(26, FullBodyExerciseLibrary.getExercisesForLevel(ExperienceLevel.ADVANCED).size)
        assertEquals(26, FullBodyExerciseLibrary.getExercisesForLevel(ExperienceLevel.EXPERT).size)

        // ExerciseItem conversion parity
        assertEquals(17, FullBodyExerciseLibrary.getExerciseItemsForLevel(ExperienceLevel.NO_EXPERIENCE).size)
        assertEquals(21, FullBodyExerciseLibrary.getExerciseItemsForLevel(ExperienceLevel.BEGINNER).size)
        assertEquals(26, FullBodyExerciseLibrary.getExerciseItemsForLevel(ExperienceLevel.ADVANCED).size)
        assertEquals(26, FullBodyExerciseLibrary.getExerciseItemsForLevel(ExperienceLevel.EXPERT).size)
    }

    // =========================================================================
    // 2. STABLE UNIQUE IDS AND METADATA INTEGRITY
    // =========================================================================

    @Test
    fun testAllExercisesHaveUniqueStableIds() {
        val allIds = FullBodyExerciseLibrary.ALL_FULL_BODY_EXERCISES.map { it.id }
        val uniqueIds = allIds.toSet()
        assertEquals(90, uniqueIds.size, "All 90 Full Body exercise records must have unique IDs with 0 duplicates")

        for (ex in FullBodyExerciseLibrary.ALL_FULL_BODY_EXERCISES) {
            assertTrue(ex.id.startsWith("fb_"), "ID must start with fb_ prefix: ${ex.id}")
            assertTrue(ex.name.isNotBlank(), "Exercise name cannot be blank for ${ex.id}")
            assertTrue(ex.primaryMuscle.isNotBlank(), "Primary muscle cannot be blank for ${ex.id}")
            assertTrue(ex.equipment.isNotEmpty(), "Equipment cannot be empty for ${ex.id}")
            assertTrue(ex.defaultSets > 0, "Sets must be > 0 for ${ex.id}")
            assertTrue(ex.defaultReps.isNotBlank(), "Reps cannot be blank for ${ex.id}")
            assertTrue(ex.defaultRestSeconds > 0, "Rest seconds must be > 0 for ${ex.id}")
            assertEquals("FULL_BODY", ex.category.uppercase(), "Category must be FULL_BODY for ${ex.id}")
            assertTrue(ex.setupSteps.isNotEmpty(), "Setup steps cannot be empty for ${ex.id}")
            assertTrue(ex.executionSteps.isNotEmpty(), "Execution steps cannot be empty for ${ex.id}")
            assertTrue(ex.breathing.isNotBlank(), "Breathing cue cannot be blank for ${ex.id}")
            assertTrue(ex.commonMistakes.isNotEmpty(), "Common mistakes cannot be empty for ${ex.id}")
            assertTrue(ex.formTips.isNotEmpty(), "Form tips / safety notes cannot be empty for ${ex.id}")
        }
    }

    // =========================================================================
    // 3. EXPERIENCE LEVEL MAPPING AND FILTERING
    // =========================================================================

    @Test
    fun testExperienceLevelMappingAndFiltering() {
        val allExercises = WorkoutFilterHelper.getAllExercises(WorkoutType.GYM)

        // No Experience: exactly 17
        val noExpFilter = WorkoutFilterState(muscleGroup = "Full Body", difficulty = "No Experience")
        val noExpResult = WorkoutFilterHelper.filterExercises(allExercises, "", noExpFilter)
        assertEquals(17, noExpResult.size, "Full Body + No Experience filter must return exactly 17 exercises")
        assertTrue(noExpResult.all { it.bodyPart == "Full Body" })

        // Beginner: exactly 21
        val begFilter = WorkoutFilterState(muscleGroup = "Full Body", difficulty = "Beginner")
        val begResult = WorkoutFilterHelper.filterExercises(allExercises, "", begFilter)
        assertEquals(21, begResult.size, "Full Body + Beginner filter must return exactly 21 exercises")
        assertTrue(begResult.all { it.bodyPart == "Full Body" })

        // Advanced: exactly 26
        val advFilter = WorkoutFilterState(muscleGroup = "Full Body", difficulty = "Advanced")
        val advResult = WorkoutFilterHelper.filterExercises(allExercises, "", advFilter)
        assertEquals(26, advResult.size, "Full Body + Advanced filter must return exactly 26 exercises")
        assertTrue(advResult.all { it.bodyPart == "Full Body" })

        // Intermediate: strictly returns Intermediate exercises (0 in Gym baseline without guesses)
        val intFilter = WorkoutFilterState(muscleGroup = "Full Body", difficulty = "Intermediate")
        val intResult = WorkoutFilterHelper.filterExercises(allExercises, "", intFilter)
        assertEquals(0, intResult.size, "Full Body + Intermediate filter for Gym baseline must return 0 (no unassigned or guessed gym exercises)")

        // Expert: exactly 26
        val expFilter = WorkoutFilterState(muscleGroup = "Full Body", difficulty = "Expert")
        val expResult = WorkoutFilterHelper.filterExercises(allExercises, "", expFilter)
        assertEquals(26, expResult.size, "Full Body + Expert filter must return exactly 26 exercises")
        assertTrue(expResult.all { it.bodyPart == "Full Body" })

        // All difficulties: all 90 exercises
        val allDiffFilter = WorkoutFilterState(muscleGroup = "Full Body", difficulty = "All")
        val allDiffResult = WorkoutFilterHelper.filterExercises(allExercises, "", allDiffFilter)
        assertEquals(90, allDiffResult.size, "Full Body + All difficulties filter must return all 90 exercises")
    }

    // =========================================================================
    // 4. CATEGORY ISOLATION (NON-FULL BODY CATEGORIES REMAIN UNAFFECTED)
    // =========================================================================

    @Test
    fun testNonFullBodyCategoriesRemainUnaffected() {
        val allExercises = WorkoutFilterHelper.getAllExercises(WorkoutType.GYM)

        // Chest remains 3
        val chestResult = WorkoutFilterHelper.filterExercises(allExercises, "", WorkoutFilterState(muscleGroup = "Chest"))
        assertEquals(3, chestResult.size, "Chest category must strictly retain 3 exercises")
        assertTrue(chestResult.all { it.bodyPart == "Chest" && !it.id.startsWith("fb_") })

        // Back remains 3
        val backResult = WorkoutFilterHelper.filterExercises(allExercises, "", WorkoutFilterState(muscleGroup = "Back"))
        assertEquals(3, backResult.size, "Back category must strictly retain 3 exercises")
        assertTrue(backResult.all { it.bodyPart == "Back" && !it.id.startsWith("fb_") })

        // Shoulders remains 3
        val shResult = WorkoutFilterHelper.filterExercises(allExercises, "", WorkoutFilterState(muscleGroup = "Shoulders"))
        assertEquals(3, shResult.size, "Shoulders category must strictly retain 3 exercises")
        assertTrue(shResult.all { it.bodyPart == "Shoulders" && !it.id.startsWith("fb_") })

        // Biceps remains 3
        val biResult = WorkoutFilterHelper.filterExercises(allExercises, "", WorkoutFilterState(muscleGroup = "Biceps"))
        assertEquals(3, biResult.size, "Biceps category must strictly retain 3 exercises")
        assertTrue(biResult.all { it.bodyPart == "Biceps" && !it.id.startsWith("fb_") })

        // Triceps remains 3
        val triResult = WorkoutFilterHelper.filterExercises(allExercises, "", WorkoutFilterState(muscleGroup = "Triceps"))
        assertEquals(3, triResult.size, "Triceps category must strictly retain 3 exercises")
        assertTrue(triResult.all { it.bodyPart == "Triceps" && !it.id.startsWith("fb_") })

        // Legs remains 3
        val legResult = WorkoutFilterHelper.filterExercises(allExercises, "", WorkoutFilterState(muscleGroup = "Legs"))
        assertEquals(3, legResult.size, "Legs category must strictly retain 3 exercises without contamination from Full Body")
        assertTrue(legResult.all { it.bodyPart == "Legs" && !it.id.startsWith("fb_") })

        // Abs remains 3
        val absResult = WorkoutFilterHelper.filterExercises(allExercises, "", WorkoutFilterState(muscleGroup = "Abs"))
        assertEquals(3, absResult.size, "Abs category must strictly retain 3 exercises")
        assertTrue(absResult.all { (it.bodyPart == "Abs" || it.bodyPart == "Core") && !it.id.startsWith("fb_") })
    }

    // =========================================================================
    // 5. SEARCH AND FILTER INTEGRATION
    // =========================================================================

    @Test
    fun testSearchWorksTogetherWithFilters() {
        val allExercises = WorkoutFilterHelper.getAllExercises(WorkoutType.GYM)

        // Search "Leg Press" within Full Body
        val fbLegPress = WorkoutFilterHelper.filterExercises(
            allExercises,
            "Leg Press",
            WorkoutFilterState(muscleGroup = "Full Body")
        )
        assertTrue(fbLegPress.isNotEmpty())
        assertTrue(fbLegPress.all { it.bodyPart == "Full Body" })
        assertTrue(fbLegPress.any { it.name.equals("Leg Press", ignoreCase = true) })

        // Search "Leg Press" within Full Body + No Experience
        val noExpLegPress = WorkoutFilterHelper.filterExercises(
            allExercises,
            "Leg Press",
            WorkoutFilterState(muscleGroup = "Full Body", difficulty = "No Experience")
        )
        assertEquals(1, noExpLegPress.size)
        assertEquals("Leg Press", noExpLegPress.first().name)

        // Search "Leg Press" within Legs category returns only the gym leg press
        val legsLegPress = WorkoutFilterHelper.filterExercises(
            allExercises,
            "Leg Press",
            WorkoutFilterState(muscleGroup = "Legs")
        )
        assertEquals(1, legsLegPress.size)
        assertEquals("gym_leg_2", legsLegPress.first().id)
    }

    // =========================================================================
    // 6. WORKOUT ROUTINE DYNAMIC RESOLUTION BY EXPERIENCE LEVEL
    // =========================================================================

    @Test
    fun testWorkoutRepositoryDynamicFullBodyRoutine() {
        // No Experience
        val noExpWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "full_body", level = ExperienceLevel.NO_EXPERIENCE)
        assertNotNull(noExpWorkout)
        assertEquals(17, noExpWorkout.exercises.size, "Full Body workout for NO_EXPERIENCE must have 17 exercises")
        assertEquals("No Experience", noExpWorkout.difficulty)

        // Beginner
        val begWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "full_body", level = ExperienceLevel.BEGINNER)
        assertNotNull(begWorkout)
        assertEquals(21, begWorkout.exercises.size, "Full Body workout for BEGINNER must have 21 exercises")
        assertEquals("Beginner", begWorkout.difficulty)

        // Advanced
        val advWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "full_body", level = ExperienceLevel.ADVANCED)
        assertNotNull(advWorkout)
        assertEquals(26, advWorkout.exercises.size, "Full Body workout for ADVANCED must have 26 exercises")
        assertEquals("Advanced", advWorkout.difficulty)

        // Expert
        val expWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "full_body", level = ExperienceLevel.EXPERT)
        assertNotNull(expWorkout)
        assertEquals(26, expWorkout.exercises.size, "Full Body workout for EXPERT must have 26 exercises")
        assertEquals("Expert", expWorkout.difficulty)
    }

    // =========================================================================
    // 7. ADMIN PANEL AND CATALOG INTEGRATION
    // =========================================================================

    @Test
    fun testAdminCatalogClassificationAndManagement() = runBlocking {
        val exercises = AdminWorkoutCatalogProvider.getUnifiedExercisesForGender("All")
        val fbExercises = exercises.filter { AdminExerciseCatalogOrganizer.classify(it) == ExerciseBodyPartSection.FULL_BODY }

        assertTrue(fbExercises.size >= 90, "Admin catalog must contain all 90 canonical Full Body exercises")

        // Validate that full body exercises with target muscle Legs are NOT classified into LEGS section
        val legsExercises = exercises.filter { AdminExerciseCatalogOrganizer.classify(it) == ExerciseBodyPartSection.LEGS }
        assertTrue(legsExercises.none { it.id.startsWith("fb_") }, "Full Body exercises must never leak into Admin LEGS section")

        // Test editing an exercise idempotently via SampleExerciseData
        val targetEx = FullBodyExerciseLibrary.ALL_FULL_BODY_EXERCISES.first()
        val editedEx = targetEx.copy(description = "Updated custom instructions for admin verification")
        SampleExerciseData.updateCanonicalExercise(editedEx)

        val retrieved = SampleExerciseData.getById(targetEx.id)
        assertNotNull(retrieved)
        assertEquals("Updated custom instructions for admin verification", retrieved.description)
        assertEquals(targetEx.name, retrieved.name)
        assertEquals(targetEx.difficulty, retrieved.difficulty)
    }

    // =========================================================================
    // 8. EXPERIENCE LEVEL FILTERING IN USER FEED (4 LEVELS)
    // =========================================================================

    @Test
    fun testFullBodyFilteringByExperienceLevels() {
        val allUserFeed = WorkoutFilterHelper.getAllExercises()
        val fbOnly = allUserFeed.filter { it.bodyPart.equals("Full Body", ignoreCase = true) }
        assertTrue(fbOnly.size >= 90, "Total Full Body exercises must be at least 90 (found: ${fbOnly.size})")

        // 1. No Experience: must return ONLY No Experience exercises
        val noExpFilter = WorkoutFilterState(muscleGroup = "Full Body", difficulty = "No Experience", workoutType = "All")
        val noExpResults = WorkoutFilterHelper.filterExercises(allUserFeed, "", noExpFilter)
        assertEquals(17, noExpResults.size, "No Experience level must return exactly 17 exercises")
        assertTrue(noExpResults.all { it.difficulty.equals("NO_EXPERIENCE", ignoreCase = true) })
        assertTrue(noExpResults.none { it.difficulty.contains("advanced", ignoreCase = true) || it.difficulty.contains("expert", ignoreCase = true) })

        // 2. Beginner: must return ONLY Beginner exercises (21 gym + 1 home = 22)
        val begFilter = WorkoutFilterState(muscleGroup = "Full Body", difficulty = "Beginner", workoutType = "All")
        val begResults = WorkoutFilterHelper.filterExercises(allUserFeed, "", begFilter)
        assertEquals(22, begResults.size, "Beginner level must return 22 exercises (21 gym + 1 home)")
        assertTrue(begResults.all { it.difficulty.equals("BEGINNER", ignoreCase = true) })
        assertTrue(begResults.none { it.difficulty.contains("no_experience", ignoreCase = true) })

        // 3. Advanced: must return Advanced exercises (26 canonical exercises + 2 home exercises = 28)
        val advFilter = WorkoutFilterState(muscleGroup = "Full Body", difficulty = "Advanced", workoutType = "All")
        val advResults = WorkoutFilterHelper.filterExercises(allUserFeed, "", advFilter)
        assertEquals(28, advResults.size, "Advanced level must return 28 exercises")
        assertTrue(advResults.all { it.difficulty.equals("ADVANCED", ignoreCase = true) })
        assertTrue(advResults.none { it.difficulty.contains("expert", ignoreCase = true) })

        // 4. Expert: must return ONLY Expert exercises (26 canonical exercises)
        val expFilter = WorkoutFilterState(muscleGroup = "Full Body", difficulty = "Expert", workoutType = "All")
        val expResults = WorkoutFilterHelper.filterExercises(allUserFeed, "", expFilter)
        assertEquals(26, expResults.size, "Expert level must return exactly 26 exercises")
        assertTrue(expResults.all { it.difficulty.equals("EXPERT", ignoreCase = true) })
        assertTrue(expResults.none { it.difficulty.contains("advanced", ignoreCase = true) || it.difficulty.contains("beginner", ignoreCase = true) })
    }

    @Test
    fun testFullBodyFeedNeverShowsAllExercisesAtOnceWhenFilteringByLevel() {
        val allUserFeed = WorkoutFilterHelper.getAllExercises()
        val levels = listOf("No Experience", "Beginner", "Advanced", "Expert")

        for (level in levels) {
            val filter = WorkoutFilterState(muscleGroup = "Full Body", difficulty = level, workoutType = "All")
            val results = WorkoutFilterHelper.filterExercises(allUserFeed, "", filter)
            assertTrue(results.isNotEmpty(), "Level $level should have exercises")
            assertTrue(results.size < 90, "Level $level should show its subset, never all 90+ exercises together (was ${results.size})")
        }
    }

    @Test
    fun testSearchAndCategoryWorkSimultaneouslyWithDifficulty() {
        val allUserFeed = WorkoutFilterHelper.getAllExercises()

        // Search "squat" within No Experience
        val noExpSquats = WorkoutFilterHelper.filterExercises(
            allUserFeed,
            query = "squat",
            filters = WorkoutFilterState(muscleGroup = "Full Body", difficulty = "No Experience", workoutType = "All")
        )
        assertTrue(noExpSquats.isNotEmpty(), "Should find squats in No Experience")
        assertTrue(noExpSquats.all { it.name.contains("Squat", ignoreCase = true) })
        assertTrue(noExpSquats.all { it.difficulty.equals("NO_EXPERIENCE", ignoreCase = true) })

        // Search "deadlift" within Expert
        val expertDeadlifts = WorkoutFilterHelper.filterExercises(
            allUserFeed,
            query = "deadlift",
            filters = WorkoutFilterState(muscleGroup = "Full Body", difficulty = "Expert", workoutType = "All")
        )
        assertTrue(expertDeadlifts.isNotEmpty(), "Should find deadlift in Expert")
        assertTrue(expertDeadlifts.all { it.name.contains("Deadlift", ignoreCase = true) })
        assertTrue(expertDeadlifts.all { it.difficulty.equals("EXPERT", ignoreCase = true) })
    }

    @Test
    fun testAdminDifficultyUpdateReflectedInUserFeed() {
        val targetEx = FullBodyExerciseLibrary.ALL_FULL_BODY_EXERCISES.first { it.id == "fb_noexp_bodyweight_squat" }
        assertEquals("NO_EXPERIENCE", targetEx.difficulty)

        // Admin updates difficulty from NO_EXPERIENCE to EXPERT
        val updatedEx = targetEx.copy(difficulty = "EXPERT")
        SampleExerciseData.updateCanonicalExercise(updatedEx)

        // Feed refreshed
        val freshFeed = WorkoutFilterHelper.getAllExercises()
        val noExpResults = WorkoutFilterHelper.filterExercises(
            freshFeed, "", WorkoutFilterState(muscleGroup = "Full Body", difficulty = "No Experience")
        )
        val expResults = WorkoutFilterHelper.filterExercises(
            freshFeed, "", WorkoutFilterState(muscleGroup = "Full Body", difficulty = "Expert")
        )

        // Bodyweight Squat should now be in Expert and no longer in No Experience
        assertFalse(noExpResults.any { it.id == "fb_noexp_bodyweight_squat" }, "Moved exercise must no longer appear in No Experience")
        assertTrue(expResults.any { it.id == "fb_noexp_bodyweight_squat" }, "Moved exercise must now appear in Expert")
    }

    @Test
    fun testOtherBodyPartCategoriesRemainUnaffected() {
        val allUserFeed = WorkoutFilterHelper.getAllExercises()
        val chestExercises = WorkoutFilterHelper.filterExercises(
            allUserFeed, "", WorkoutFilterState(muscleGroup = "Chest", difficulty = "All")
        )
        assertTrue(chestExercises.isNotEmpty(), "Chest exercises must continue to work normally")
        assertTrue(chestExercises.all { it.bodyPart.equals("Chest", ignoreCase = true) })

        val backExercises = WorkoutFilterHelper.filterExercises(
            allUserFeed, "", WorkoutFilterState(muscleGroup = "Back", difficulty = "All")
        )
        assertTrue(backExercises.isNotEmpty(), "Back exercises must continue to work normally")
        assertTrue(backExercises.all { it.bodyPart.equals("Back", ignoreCase = true) })
    }
}
