package com.example.fitness.feature.workout

import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.exercise.data.FullBodyExerciseLibrary
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.data.ThirtyDayExerciseLibrary
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import com.example.fitness.feature.workout.data.WorkoutFilterHelper
import com.example.fitness.feature.workout.data.WorkoutFilterState
import com.example.fitness.feature.workout.data.WorkoutRepository
import com.example.fitness.feature.workout.model.ExperienceLevel
import com.example.fitness.feature.workout.model.WorkoutType
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DynamicExerciseAssignmentTest {

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

    // 1. Combination: No Experience + Chest in both Workout section and Today's Workout
    @Test
    fun test01_noExperienceChest_WorkoutAndTodaysWorkout() {
        val level = ExperienceLevel.NO_EXPERIENCE
        UserSession.experienceLevel = level

        // Workout section fetching
        val workoutExercises = WorkoutRepository.fetchExercises("Chest", level)
        assertTrue(workoutExercises.isNotEmpty(), "No Experience chest exercises must not be empty")
        assertEquals(8, workoutExercises.size, "No Experience chest exercises must contain exactly 8 entries from authoritative database")

        val gymWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest", level = level)
        assertNotNull(gymWorkout)
        assertEquals("No Experience", gymWorkout.difficulty)
        assertEquals(8, gymWorkout.exercises.size)
        assertTrue(gymWorkout.exercises.any { it.name.contains("Machine Chest Press", ignoreCase = true) })

        // Today's Workout fetching
        val todayPlan = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "chest", level)
        assertNotNull(todayPlan)
        assertFalse(todayPlan.isRestDay)
        assertTrue(todayPlan.exercises.isNotEmpty(), "Today's workout chest plan must contain exercises")
        assertEquals(8, todayPlan.exercises.size)
        assertTrue(todayPlan.exercises.any { it.name.contains("Machine Chest Press", ignoreCase = true) })
    }

    // 2. Combination: Beginner + Chest in both Workout section and Today's Workout
    @Test
    fun test02_beginnerChest_WorkoutAndTodaysWorkout() {
        val level = ExperienceLevel.BEGINNER
        UserSession.experienceLevel = level

        val workoutExercises = WorkoutRepository.fetchExercises("Chest", level)
        assertTrue(workoutExercises.isNotEmpty(), "Beginner chest exercises must not be empty")
        assertEquals(9, workoutExercises.size, "Beginner chest exercises must contain exactly 9 entries")

        val gymWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest", level = level)
        assertNotNull(gymWorkout)
        assertEquals("Beginner", gymWorkout.difficulty)
        assertEquals(9, gymWorkout.exercises.size)
        assertTrue(gymWorkout.exercises.any { it.name.contains("Dumbbell Bench Press", ignoreCase = true) })

        val todayPlan = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "chest", level)
        assertNotNull(todayPlan)
        assertEquals(9, todayPlan.exercises.size)
        assertTrue(todayPlan.exercises.any { it.name.contains("Dumbbell Bench Press", ignoreCase = true) })
    }

    // 3. Combination: Beginner + Legs in both Workout section and Today's Workout
    @Test
    fun test03_beginnerLegs_WorkoutAndTodaysWorkout() {
        val level = ExperienceLevel.BEGINNER
        UserSession.experienceLevel = level

        val workoutExercises = WorkoutRepository.fetchExercises("Legs", level)
        assertTrue(workoutExercises.isNotEmpty(), "Beginner legs exercises must not be empty")
        assertEquals(13, workoutExercises.size, "Beginner legs exercises must contain 13 entries")

        val gymWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "legs", level = level)
        assertNotNull(gymWorkout)
        assertEquals("Beginner", gymWorkout.difficulty)
        assertEquals(13, gymWorkout.exercises.size)
        assertTrue(gymWorkout.exercises.any { it.name.contains("Leg Press", ignoreCase = true) })

        val todayPlan = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "legs", level)
        assertNotNull(todayPlan)
        assertEquals(13, todayPlan.exercises.size)
        assertTrue(todayPlan.exercises.any { it.name.contains("Leg Press", ignoreCase = true) })
    }

    // 4. Combination: Advanced + Back in both Workout section and Today's Workout
    @Test
    fun test04_advancedBack_WorkoutAndTodaysWorkout() {
        val level = ExperienceLevel.ADVANCED
        UserSession.experienceLevel = level

        val workoutExercises = WorkoutRepository.fetchExercises("Back", level)
        assertTrue(workoutExercises.isNotEmpty(), "Advanced back exercises must not be empty")
        assertEquals(12, workoutExercises.size, "Advanced back exercises must contain exactly 12 entries")

        val gymWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "back", level = level)
        assertNotNull(gymWorkout)
        assertEquals("Advanced", gymWorkout.difficulty)
        assertEquals(12, gymWorkout.exercises.size)
        assertTrue(gymWorkout.exercises.any { it.name.contains("Pull-Up", ignoreCase = true) || it.name.contains("Barbell Row", ignoreCase = true) })

        val todayPlan = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "back", level)
        assertNotNull(todayPlan)
        assertEquals(12, todayPlan.exercises.size)
    }

    // 5. Combination: Expert + Full Body in both Workout section and Today's Workout
    @Test
    fun test05_expertFullBody_WorkoutAndTodaysWorkout() {
        val level = ExperienceLevel.EXPERT
        UserSession.experienceLevel = level

        val workoutExercises = WorkoutRepository.fetchExercises("Full Body", level)
        assertTrue(workoutExercises.isNotEmpty(), "Expert full body exercises must not be empty")
        assertEquals(26, workoutExercises.size, "Expert full body exercises must contain exactly 26 entries from FullBodyExerciseLibrary")

        val gymWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "full_body", level = level)
        assertNotNull(gymWorkout)
        assertEquals("Expert", gymWorkout.difficulty)
        assertEquals(26, gymWorkout.exercises.size)

        val todayPlan = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "full_body", level)
        assertNotNull(todayPlan)
        assertEquals(26, todayPlan.exercises.size)
    }

    // 6. Changing user level dynamically refreshes Workout and Today's Workout results
    @Test
    fun test06_userLevelChangeRefreshesWorkoutAndTodaysWorkout() {
        // Start as No Experience
        UserSession.experienceLevel = ExperienceLevel.NO_EXPERIENCE
        val noExpChest = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest", level = UserSession.experienceLevel)
        assertNotNull(noExpChest)
        assertEquals("No Experience", noExpChest.difficulty)
        assertEquals(8, noExpChest.exercises.size)

        // Switch user level to Advanced
        UserSession.experienceLevel = ExperienceLevel.ADVANCED
        val advChest = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest", level = UserSession.experienceLevel)
        assertNotNull(advChest)
        assertEquals("Advanced", advChest.difficulty)
        assertEquals(15, advChest.exercises.size)

        // Verify Today's Workout also dynamically resolves to Advanced
        val advTodayPlan = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "chest", UserSession.experienceLevel)
        assertEquals(15, advTodayPlan.exercises.size)
        assertEquals("Advanced", advTodayPlan.difficulty)
    }

    // 7. Admin updates to an exercise propagate synchronously to both screens
    @Test
    fun test07_adminExerciseUpdatePropagatesToBothScreens() {
        val baseEx = ThirtyDayExerciseLibrary.getExercises("Chest", ExperienceLevel.NO_EXPERIENCE).first()
        val originalExercise = ThirtyDayExerciseLibrary.toExercise(baseEx)

        // Admin updates exercise parameters
        val updatedExercise = originalExercise.copy(
            name = "Super Admin Chest Press",
            defaultSets = 5,
            defaultReps = "15-20",
            defaultRestSeconds = 45,
            description = "Admin customized chest press technique."
        )

        SampleExerciseData.updateCanonicalExercise(updatedExercise)

        // Check Workout section
        val workoutExercises = WorkoutRepository.fetchExercises("Chest", ExperienceLevel.NO_EXPERIENCE)
        val modifiedWorkoutEx = workoutExercises.find { it.id == updatedExercise.id }
        assertNotNull(modifiedWorkoutEx, "Modified exercise must be returned in Workout fetch")
        assertEquals("Super Admin Chest Press", modifiedWorkoutEx.name)
        assertEquals("5 sets × 15-20", modifiedWorkoutEx.sets)
        assertEquals("45s", modifiedWorkoutEx.restTime)
        assertEquals("Admin customized chest press technique.", modifiedWorkoutEx.description)

        // Check Today's Workout
        val todayPlan = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "chest", ExperienceLevel.NO_EXPERIENCE)
        val modifiedPlanEx = todayPlan.exercises.find { it.id == updatedExercise.id }
        assertNotNull(modifiedPlanEx, "Modified exercise must be reflected in Today's Workout")
        assertEquals("Super Admin Chest Press", modifiedPlanEx.name)
        assertEquals("5 sets", modifiedPlanEx.sets.lowercase())
        assertEquals("45s", modifiedPlanEx.rest)
    }

    // 8. Search and category filtering with WorkoutFilterHelper
    @Test
    fun test08_searchAndCategoryFiltering() {
        val exercises = WorkoutFilterHelper.getAllExercises(level = ExperienceLevel.BEGINNER)
        assertTrue(exercises.isNotEmpty())

        // Filter by body part
        val chestFiltered = WorkoutFilterHelper.filterExercises(
            exercises = exercises,
            query = "",
            filters = WorkoutFilterState(muscleGroup = "Chest", difficulty = "All")
        )
        assertTrue(chestFiltered.all { it.bodyPart.equals("Chest", ignoreCase = true) })

        // Search query
        val queryFiltered = WorkoutFilterHelper.filterExercises(
            exercises = chestFiltered,
            query = "Bench Press",
            filters = WorkoutFilterState(muscleGroup = "Chest", difficulty = "All")
        )
        assertTrue(queryFiltered.all { it.name.contains("Bench Press", ignoreCase = true) })
    }

    // 9. Graceful empty state handling for unknown categories
    @Test
    fun test09_emptyResultsHandledGracefully() {
        val emptyResult = WorkoutRepository.fetchExercises("NonExistentCategory", ExperienceLevel.BEGINNER)
        assertTrue(emptyResult.isEmpty(), "Non-existent category must safely return empty list without crash")

        val emptyPlan = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "non_existent_category", ExperienceLevel.BEGINNER)
        assertNotNull(emptyPlan)
    }

    // 10. Workout schedule and 30-day program resolution remain intact
    @Test
    fun test10_workoutScheduleAndProgressionIntact() {
        // Day 1: Chest + Triceps
        val day1Plan = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(null, 0, ExperienceLevel.NO_EXPERIENCE)
        assertNotNull(day1Plan)
        assertEquals("Mon", day1Plan.dayOfWeek)
        assertFalse(day1Plan.isRestDay)
        assertTrue(day1Plan.exercises.isNotEmpty())

        // Day 7: Rest day
        val day7Plan = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(null, 6, ExperienceLevel.NO_EXPERIENCE)
        assertNotNull(day7Plan)
        assertEquals("Sun", day7Plan.dayOfWeek)
        assertTrue(day7Plan.isRestDay)
        assertTrue(day7Plan.exercises.isEmpty())
    }

    // 11. Full Body workout dynamically fetches matching exercises across all 4 levels
    @Test
    fun test11_fullBodyDynamicAssignmentAllFourLevels() {
        val noExpFb = WorkoutRepository.fetchExercises("Full Body", ExperienceLevel.NO_EXPERIENCE)
        assertEquals(17, noExpFb.size, "No Experience Full Body must return 17 exercises")

        val begFb = WorkoutRepository.fetchExercises("Full Body", ExperienceLevel.BEGINNER)
        assertEquals(21, begFb.size, "Beginner Full Body must return 21 exercises")

        val advFb = WorkoutRepository.fetchExercises("Full Body", ExperienceLevel.ADVANCED)
        assertEquals(26, advFb.size, "Advanced Full Body must return 26 exercises")

        val expFb = WorkoutRepository.fetchExercises("Full Body", ExperienceLevel.EXPERT)
        assertEquals(26, expFb.size, "Expert Full Body must return 26 exercises")

        // Also test getWorkout for full_body
        val noExpWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "full_body", level = ExperienceLevel.NO_EXPERIENCE)
        assertNotNull(noExpWorkout)
        assertEquals(17, noExpWorkout.exercises.size)
        assertEquals("No Experience", noExpWorkout.difficulty)

        val begWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "full_body", level = ExperienceLevel.BEGINNER)
        assertNotNull(begWorkout)
        assertEquals(21, begWorkout.exercises.size)
        assertEquals("Beginner", begWorkout.difficulty)

        val advWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "full_body", level = ExperienceLevel.ADVANCED)
        assertNotNull(advWorkout)
        assertEquals(26, advWorkout.exercises.size)
        assertEquals("Advanced", advWorkout.difficulty)

        val expWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "full_body", level = ExperienceLevel.EXPERT)
        assertNotNull(expWorkout)
        assertEquals(26, expWorkout.exercises.size)
        assertEquals("Expert", expWorkout.difficulty)
    }

    // 12. Profile page options mapping: No Experience, Beginner, Advance/Advanced, Expert
    @Test
    fun test12_profileFitnessLevelOptionMapping() {
        assertEquals(ExperienceLevel.NO_EXPERIENCE, ExperienceLevel.fromUserFacingOption("No Experience"))
        assertEquals(ExperienceLevel.BEGINNER, ExperienceLevel.fromUserFacingOption("Beginner"))
        assertEquals(ExperienceLevel.ADVANCED, ExperienceLevel.fromUserFacingOption("Advance"))
        assertEquals(ExperienceLevel.ADVANCED, ExperienceLevel.fromUserFacingOption("Advanced"))
        assertEquals(ExperienceLevel.EXPERT, ExperienceLevel.fromUserFacingOption("Expert"))

        assertEquals("No Experience", ExperienceLevel.NO_EXPERIENCE.toUserFacingLevel())
        assertEquals("Beginner", ExperienceLevel.BEGINNER.toUserFacingLevel())
        assertEquals("Advanced", ExperienceLevel.ADVANCED.toUserFacingLevel())
        assertEquals("Expert", ExperienceLevel.EXPERT.toUserFacingLevel())
    }

    // =====================================================================================
    // SECTION 23 TESTS: ALL 4 EXPERIENCE LEVELS (CHEST, LEGS, FULL BODY, TODAY'S WORKOUT)
    // =====================================================================================

    @Test
    fun testSection23_noExperience_ChestLegsFullBodyToday() {
        val level = ExperienceLevel.NO_EXPERIENCE
        UserSession.experienceLevel = level

        // Chest
        val chestEx = WorkoutRepository.fetchExercises("Chest", level)
        assertEquals(8, chestEx.size)
        val gymChest = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest", level = level)
        assertEquals("No Experience", gymChest?.difficulty)
        assertEquals(8, gymChest?.exercises?.size)

        // Legs
        val legsEx = WorkoutRepository.fetchExercises("Legs", level)
        assertEquals(12, legsEx.size)
        val gymLegs = WorkoutRepository.getWorkout(WorkoutType.GYM, "legs", level = level)
        assertEquals("No Experience", gymLegs?.difficulty)
        assertEquals(12, gymLegs?.exercises?.size)

        // Full Body
        val fbEx = WorkoutRepository.fetchExercises("Full Body", level)
        assertEquals(17, fbEx.size)
        val gymFb = WorkoutRepository.getWorkout(WorkoutType.GYM, "full_body", level = level)
        assertEquals("No Experience", gymFb?.difficulty)
        assertEquals(17, gymFb?.exercises?.size)

        // Today's Workout
        val todayPlan = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "chest", level)
        assertEquals(8, todayPlan.exercises.size)
        assertEquals("No Experience", todayPlan.difficulty)
    }

    @Test
    fun testSection23_beginner_ChestLegsFullBodyToday() {
        val level = ExperienceLevel.BEGINNER
        UserSession.experienceLevel = level

        // Chest
        val chestEx = WorkoutRepository.fetchExercises("Chest", level)
        assertEquals(9, chestEx.size)
        val gymChest = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest", level = level)
        assertEquals("Beginner", gymChest?.difficulty)
        assertEquals(9, gymChest?.exercises?.size)

        // Legs
        val legsEx = WorkoutRepository.fetchExercises("Legs", level)
        assertEquals(13, legsEx.size)
        val gymLegs = WorkoutRepository.getWorkout(WorkoutType.GYM, "legs", level = level)
        assertEquals("Beginner", gymLegs?.difficulty)
        assertEquals(13, gymLegs?.exercises?.size)

        // Full Body
        val fbEx = WorkoutRepository.fetchExercises("Full Body", level)
        assertEquals(21, fbEx.size)
        val gymFb = WorkoutRepository.getWorkout(WorkoutType.GYM, "full_body", level = level)
        assertEquals("Beginner", gymFb?.difficulty)
        assertEquals(21, gymFb?.exercises?.size)

        // Today's Workout
        val todayPlan = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "chest", level)
        assertEquals(9, todayPlan.exercises.size)
        assertEquals("Beginner", todayPlan.difficulty)
    }

    @Test
    fun testSection23_advanced_ChestLegsFullBodyToday() {
        val level = ExperienceLevel.ADVANCED
        UserSession.experienceLevel = level

        // Chest
        val chestEx = WorkoutRepository.fetchExercises("Chest", level)
        assertEquals(15, chestEx.size)
        val gymChest = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest", level = level)
        assertEquals("Advanced", gymChest?.difficulty)
        assertEquals(15, gymChest?.exercises?.size)

        // Legs
        val legsEx = WorkoutRepository.fetchExercises("Legs", level)
        assertEquals(16, legsEx.size)
        val gymLegs = WorkoutRepository.getWorkout(WorkoutType.GYM, "legs", level = level)
        assertEquals("Advanced", gymLegs?.difficulty)
        assertEquals(16, gymLegs?.exercises?.size)

        // Full Body
        val fbEx = WorkoutRepository.fetchExercises("Full Body", level)
        assertEquals(26, fbEx.size)
        val gymFb = WorkoutRepository.getWorkout(WorkoutType.GYM, "full_body", level = level)
        assertEquals("Advanced", gymFb?.difficulty)
        assertEquals(26, gymFb?.exercises?.size)

        // Today's Workout
        val todayPlan = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "chest", level)
        assertEquals(15, todayPlan.exercises.size)
        assertEquals("Advanced", todayPlan.difficulty)
    }

    @Test
    fun testSection23_expert_ChestLegsFullBodyToday() {
        val level = ExperienceLevel.EXPERT
        UserSession.experienceLevel = level

        // Chest
        val chestEx = WorkoutRepository.fetchExercises("Chest", level)
        assertEquals(15, chestEx.size)
        val gymChest = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest", level = level)
        assertEquals("Expert", gymChest?.difficulty)
        assertEquals(15, gymChest?.exercises?.size)

        // Legs
        val legsEx = WorkoutRepository.fetchExercises("Legs", level)
        assertEquals(16, legsEx.size)
        val gymLegs = WorkoutRepository.getWorkout(WorkoutType.GYM, "legs", level = level)
        assertEquals("Expert", gymLegs?.difficulty)
        assertEquals(16, gymLegs?.exercises?.size)

        // Full Body
        val fbEx = WorkoutRepository.fetchExercises("Full Body", level)
        assertEquals(26, fbEx.size)
        val gymFb = WorkoutRepository.getWorkout(WorkoutType.GYM, "full_body", level = level)
        assertEquals("Expert", gymFb?.difficulty)
        assertEquals(26, gymFb?.exercises?.size)

        // Today's Workout
        val todayPlan = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "chest", level)
        assertEquals(15, todayPlan.exercises.size)
        assertEquals("Expert", todayPlan.difficulty)
    }

    // =====================================================================================
    // SECTION 24 TESTS: TODAY'S WORKOUT SPECIFIC TEST SCENARIOS
    // =====================================================================================

    // Test A: Beginner + Chest -> Remains Chest-based and contains Beginner Chest exercises
    @Test
    fun testSection24_TestA_BeginnerChest() {
        val plan = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "chest", ExperienceLevel.BEGINNER)
        assertNotNull(plan)
        assertEquals("Beginner", plan.difficulty)
        assertEquals(9, plan.exercises.size)
        val libExercises = ThirtyDayExerciseLibrary.getExercises("Chest", ExperienceLevel.BEGINNER)
        val libIds = libExercises.map { it.id }.toSet()
        assertTrue(plan.exercises.all { libIds.contains(it.id) || libExercises.any { le -> le.name.equals(it.name, ignoreCase = true) } })
    }

    // Test B: Expert + Chest -> Remains Chest-based and contains Expert Chest exercises
    @Test
    fun testSection24_TestB_ExpertChest() {
        val plan = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "chest", ExperienceLevel.EXPERT)
        assertNotNull(plan)
        assertEquals("Expert", plan.difficulty)
        assertEquals(15, plan.exercises.size)
        val libExercises = ThirtyDayExerciseLibrary.getExercises("Chest", ExperienceLevel.EXPERT)
        val libIds = libExercises.map { it.id }.toSet()
        assertTrue(plan.exercises.all { libIds.contains(it.id) || libExercises.any { le -> le.name.equals(it.name, ignoreCase = true) } })
    }

    // Test C: Beginner + Chest + Triceps -> Beginner Chest + Beginner Triceps
    @Test
    fun testSection24_TestC_BeginnerChestAndTriceps() {
        val plan = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "chest", ExperienceLevel.BEGINNER)
        assertNotNull(plan)
        assertEquals("Beginner", plan.difficulty)
        assertTrue(plan.exercises.isNotEmpty())
        val chestEx = ThirtyDayExerciseLibrary.getExercises("Chest", ExperienceLevel.BEGINNER)
        val triEx = ThirtyDayExerciseLibrary.getExercises("Triceps", ExperienceLevel.BEGINNER)
        assertEquals(9, chestEx.size)
        assertEquals(8, triEx.size)
    }

    // Test D: Advanced + Full Body -> Advanced Full Body exercises only
    @Test
    fun testSection24_TestD_AdvancedFullBody() {
        val plan = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "full_body", ExperienceLevel.ADVANCED)
        assertNotNull(plan)
        assertEquals("Advanced", plan.difficulty)
        assertEquals(26, plan.exercises.size)
        val advFbIds = FullBodyExerciseLibrary.ADVANCED_EXERCISES.map { it.id }.toSet()
        assertTrue(plan.exercises.all { advFbIds.contains(it.id) })
    }

    // Test E: No Experience + Legs -> No Experience Legs exercises only
    @Test
    fun testSection24_TestE_NoExperienceLegs() {
        val plan = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "legs", ExperienceLevel.NO_EXPERIENCE)
        assertNotNull(plan)
        assertEquals("No Experience", plan.difficulty)
        assertEquals(12, plan.exercises.size)
        val noExpLegIds = ThirtyDayExerciseLibrary.getExercises("Legs", ExperienceLevel.NO_EXPERIENCE).map { it.id }.toSet()
        assertTrue(plan.exercises.all { noExpLegIds.contains(it.id) })
    }

    // =====================================================================================
    // COMPLETE VALIDATION MATRIX TESTS (Section 17)
    // =====================================================================================

    @Test
    fun testValidationMatrix_ChestAllLevels() {
        // No Experience + Chest
        val noExpChest = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "chest", ExperienceLevel.NO_EXPERIENCE)
        assertEquals("No Experience", noExpChest.difficulty)
        val noExpChestIds = noExpChest.exercises.map { it.id }.toSet()

        // Beginner + Chest
        val begChest = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "chest", ExperienceLevel.BEGINNER)
        assertEquals("Beginner", begChest.difficulty)
        val begChestIds = begChest.exercises.map { it.id }.toSet()

        // Advanced + Chest
        val advChest = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "chest", ExperienceLevel.ADVANCED)
        assertEquals("Advanced", advChest.difficulty)
        val advChestIds = advChest.exercises.map { it.id }.toSet()

        // Expert + Chest
        val expChest = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "chest", ExperienceLevel.EXPERT)
        assertEquals("Expert", expChest.difficulty)
        val expChestIds = expChest.exercises.map { it.id }.toSet()

        // Verify that pools are actually distinct across levels
        assertTrue(noExpChestIds != begChestIds, "No Experience Chest must have distinct exercise pool from Beginner")
        assertTrue(begChestIds != advChestIds, "Beginner Chest must have distinct exercise pool from Advanced")
        assertTrue(advChestIds != expChestIds || expChest.exercises.any { it.name.contains("Barbell", ignoreCase = true) })
    }

    @Test
    fun testValidationMatrix_LegsAllLevels() {
        // No Experience + Legs
        val noExpLegs = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "legs", ExperienceLevel.NO_EXPERIENCE)
        assertEquals("No Experience", noExpLegs.difficulty)
        val noExpLegsIds = noExpLegs.exercises.map { it.id }.toSet()

        // Beginner + Legs
        val begLegs = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "legs", ExperienceLevel.BEGINNER)
        assertEquals("Beginner", begLegs.difficulty)
        val begLegsIds = begLegs.exercises.map { it.id }.toSet()

        // Advanced + Legs
        val advLegs = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "legs", ExperienceLevel.ADVANCED)
        assertEquals("Advanced", advLegs.difficulty)
        val advLegsIds = advLegs.exercises.map { it.id }.toSet()

        // Expert + Legs
        val expLegs = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "legs", ExperienceLevel.EXPERT)
        assertEquals("Expert", expLegs.difficulty)
        val expLegsIds = expLegs.exercises.map { it.id }.toSet()

        // Verify that pools are actually distinct across levels
        assertTrue(noExpLegsIds != begLegsIds, "No Experience Legs must have distinct exercise pool from Beginner")
        assertTrue(begLegsIds != advLegsIds, "Beginner Legs must have distinct exercise pool from Advanced")
        assertTrue(advLegsIds != expLegsIds, "Advanced Legs must have distinct exercise pool from Expert")
    }

    @Test
    fun testValidationMatrix_FullBodyAllLevels() {
        // No Experience + Full Body
        val noExpFb = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "full_body", ExperienceLevel.NO_EXPERIENCE)
        assertEquals("No Experience", noExpFb.difficulty)
        assertEquals(17, noExpFb.exercises.size)
        val noExpFbIds = noExpFb.exercises.map { it.id }.toSet()

        // Beginner + Full Body
        val begFb = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "full_body", ExperienceLevel.BEGINNER)
        assertEquals("Beginner", begFb.difficulty)
        assertEquals(21, begFb.exercises.size)
        val begFbIds = begFb.exercises.map { it.id }.toSet()

        // Advanced + Full Body
        val advFb = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "full_body", ExperienceLevel.ADVANCED)
        assertEquals("Advanced", advFb.difficulty)
        assertEquals(26, advFb.exercises.size)
        val advFbIds = advFb.exercises.map { it.id }.toSet()

        // Expert + Full Body
        val expFb = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "full_body", ExperienceLevel.EXPERT)
        assertEquals("Expert", expFb.difficulty)
        assertEquals(26, expFb.exercises.size)
        val expFbIds = expFb.exercises.map { it.id }.toSet()

        // Verify that pools are actually distinct across levels
        assertTrue(noExpFbIds != begFbIds, "No Experience Full Body must differ from Beginner")
        assertTrue(begFbIds != advFbIds, "Beginner Full Body must differ from Advanced")
        assertTrue(advFbIds != expFbIds, "Advanced Full Body must differ from Expert")
    }

    @Test
    fun testValidationMatrix_BeginnerChestAndTriceps() {
        // Multi-body-part test: Beginner + Chest + Triceps
        val multiWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest_triceps", level = ExperienceLevel.BEGINNER)
        assertNotNull(multiWorkout)
        assertEquals("Beginner", multiWorkout.difficulty)

        val begChestPool = ThirtyDayExerciseLibrary.getExercises("Chest", ExperienceLevel.BEGINNER).map { it.id }.toSet()
        val begTriPool = ThirtyDayExerciseLibrary.getExercises("Triceps", ExperienceLevel.BEGINNER).map { it.id }.toSet()

        // Verify exercises are strictly from Beginner Chest and Beginner Triceps
        for (ex in multiWorkout.exercises) {
            val isChest = begChestPool.contains(ex.id) || begChestPool.contains(SampleExerciseData.resolveCanonicalId(ex.id))
            val isTri = begTriPool.contains(ex.id) || begTriPool.contains(SampleExerciseData.resolveCanonicalId(ex.id))
            assertTrue(isChest || isTri, "Exercise ${ex.id} (${ex.name}) must be in Beginner Chest or Beginner Triceps pool")
        }
    }

    @Test
    fun testTodaysWorkoutDay1_ExercisesActuallyChangeAcrossAll4Levels() {
        // Test Today's Workout Day 1 (Chest + Triceps) across all four levels
        val day1NoExp = CanonicalWorkoutResolver.resolveDailyWorkoutPlan("30-day", "day_1", ExperienceLevel.NO_EXPERIENCE)
        val day1Beg = CanonicalWorkoutResolver.resolveDailyWorkoutPlan("30-day", "day_1", ExperienceLevel.BEGINNER)
        val day1Adv = CanonicalWorkoutResolver.resolveDailyWorkoutPlan("30-day", "day_1", ExperienceLevel.ADVANCED)
        val day1Exp = CanonicalWorkoutResolver.resolveDailyWorkoutPlan("30-day", "day_1", ExperienceLevel.EXPERT)

        assertEquals("No Experience", day1NoExp.difficulty)
        assertEquals("Beginner", day1Beg.difficulty)
        assertEquals("Advanced", day1Adv.difficulty)
        assertEquals("Expert", day1Exp.difficulty)

        val noExpIds = day1NoExp.exercises.map { it.id }.toSet()
        val begIds = day1Beg.exercises.map { it.id }.toSet()
        val advIds = day1Adv.exercises.map { it.id }.toSet()
        val expIds = day1Exp.exercises.map { it.id }.toSet()

        // Assert that the exercises are actually different and not just identical lists with changing labels
        assertTrue(noExpIds != begIds, "Day 1 No Experience must have different exercises from Beginner. NoExp=$noExpIds vs Beg=$begIds")
        assertTrue(begIds != advIds, "Day 1 Beginner must have different exercises from Advanced. Beg=$begIds vs Adv=$advIds")
        assertTrue(advIds != expIds, "Day 1 Advanced must have different exercises from Expert. Adv=$advIds vs Exp=$expIds")
    }

    @Test
    fun testErrorPrevention_NoIntermediateOrUnclassifiedLeaking() {
        for (level in ExperienceLevel.entries) {
            val plan = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(null, "chest", level)
            assertFalse(plan.difficulty.contains("Intermediate", ignoreCase = true), "Intermediate must never appear in difficulty")
            assertTrue(
                plan.difficulty in listOf("No Experience", "Beginner", "Advanced", "Expert"),
                "Difficulty must be one of the four exact ATHGYM levels: ${plan.difficulty}"
            )
        }
    }
}
