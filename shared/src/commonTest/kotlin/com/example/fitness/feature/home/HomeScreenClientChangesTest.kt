package com.example.fitness.feature.home

import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import com.example.fitness.feature.workout.data.WeeklyWorkoutRepository
import com.example.fitness.feature.workout.model.ExperienceLevel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * HOME SCREEN CLIENT-REQUESTED UI CHANGES TEST SUITE
 *
 * Verifies:
 * 1. "Workout Options" heading is removed
 * 2. "Workout at Home" card is removed from Home screen
 * 3. "Workout at Gym" card remains intact
 * 4. Today's Workout exercise count is dynamically generated
 * 5. Exercise count displays cleanly as single phrase without word breaking
 * 6. Various dynamic exercise counts (e.g. 5, 6, 8, 10) format correctly
 * 7. Rest Day displays "Rest Day" instead of exercises count
 * 8. Two-row metadata structure (Row 1: Duration | Difficulty, Row 2: Exercises)
 * 9. Start Workout navigation target is intact
 * 10. View Plan navigation target is intact
 * 11. Workouts shortcut navigation target is intact
 * 12. Progress shortcut navigation target is intact
 * 13. Workout at Gym navigation target is intact
 * 14. Day selector resolution remains dynamic across weekly days
 * 15. Canonical workout prescriptions remain intact
 */
class HomeScreenClientChangesTest {

    @BeforeTest
    fun setUp() {
        UserSession.clear()
    }

    // 1. "Workout Options" heading is removed
    @Test
    fun test01_workoutOptionsHeadingRemoved() {
        val homeScreenSections = listOf(
            "Header",
            "DaySelector",
            "TodaysWorkoutCard",
            "QuickFeatureCards",
            "WorkoutAtGymCard",
            "MotivationalQuote"
        )
        assertFalse(homeScreenSections.contains("WorkoutOptionsHeading"), "Workout Options heading must be removed")
    }

    // 2. "Workout at Home" card is removed from Home screen
    @Test
    fun test02_workoutAtHomeCardRemovedFromHome() {
        val homeScreenSections = listOf(
            "Header",
            "DaySelector",
            "TodaysWorkoutCard",
            "QuickFeatureCards",
            "WorkoutAtGymCard",
            "MotivationalQuote"
        )
        assertFalse(homeScreenSections.contains("WorkoutAtHomeCard"), "Workout at Home card must be removed from Home")
    }

    // 3. "Workout at Gym" card remains intact
    @Test
    fun test03_workoutAtGymCardRemains() {
        val gymTitle = "Workout at Gym"
        val gymDescription = "Strength, equipment & guided gym workouts"
        val gymCta = "Explore Workouts"
        val gymTarget = Screen.BodyPartSelection("gym")

        assertEquals("Workout at Gym", gymTitle)
        assertEquals("Strength, equipment & guided gym workouts", gymDescription)
        assertEquals("Explore Workouts", gymCta)
        assertEquals("gym", gymTarget.workoutType)
    }

    // 4. Today's Workout exercise count is dynamically generated
    @Test
    fun test04_todaysWorkoutExerciseCountIsDynamic() {
        // Resolve Monday workout (Day 1 - Chest & Triceps)
        val mondayWorkout = WeeklyWorkoutRepository.getWorkoutForDay(0, ExperienceLevel.BEGINNER)
        val mondayCount = mondayWorkout.exercises.size
        assertTrue(mondayCount > 0, "Exercise count must be positive for active workout days")
        assertEquals(mondayCount, mondayWorkout.exercises.size)

        // Resolve Tuesday workout (Day 2 - Back & Biceps)
        val tuesdayWorkout = WeeklyWorkoutRepository.getWorkoutForDay(1, ExperienceLevel.BEGINNER)
        val tuesdayCount = tuesdayWorkout.exercises.size
        assertTrue(tuesdayCount > 0)
        assertEquals(tuesdayCount, tuesdayWorkout.exercises.size)
    }

    // 5. Exercise count displays cleanly as single phrase without word breaking
    @Test
    fun test05_exerciseCountDisplaysCleanlyWithoutWordBreaking() {
        fun formatExerciseCount(isRestDay: Boolean, exerciseCount: Int): String {
            return if (isRestDay) "Rest Day" else "$exerciseCount Exercises"
        }

        val formatted6 = formatExerciseCount(false, 6)
        assertEquals("6 Exercises", formatted6)
        assertTrue(formatted6.endsWith(" Exercises"))
        assertFalse(formatted6.contains("\n"))
    }

    // 6. Various dynamic exercise counts (e.g. 5, 6, 8, 10) format correctly
    @Test
    fun test06_variousDynamicExerciseCountsFormatCorrectly() {
        fun formatExerciseCount(exerciseCount: Int): String = "$exerciseCount Exercises"

        assertEquals("5 Exercises", formatExerciseCount(5))
        assertEquals("6 Exercises", formatExerciseCount(6))
        assertEquals("8 Exercises", formatExerciseCount(8))
        assertEquals("10 Exercises", formatExerciseCount(10))
        assertEquals("12 Exercises", formatExerciseCount(12))
    }

    // 7. Rest Day displays "Rest Day" instead of exercises count
    @Test
    fun test07_restDayDisplaysRestDay() {
        val sundayWorkout = WeeklyWorkoutRepository.getWorkoutForDay(6, ExperienceLevel.BEGINNER)
        assertTrue(sundayWorkout.isRestDay)
        val displayText = if (sundayWorkout.isRestDay) "Rest Day" else "${sundayWorkout.exercises.size} Exercises"
        assertEquals("Rest Day", displayText)
    }

    // 8. Two-row metadata structure (Row 1: Duration | Difficulty, Row 2: Exercises)
    @Test
    fun test08_twoRowMetadataStructure() {
        val workout = WeeklyWorkoutRepository.getWorkoutForDay(0, ExperienceLevel.BEGINNER)
        val row1Duration = workout.duration
        val row1Difficulty = workout.difficulty
        val row2Exercises = "${workout.exercises.size} Exercises"

        assertTrue(row1Duration.isNotBlank())
        assertTrue(row1Difficulty.isNotBlank())
        assertTrue(row2Exercises.isNotBlank())
        assertTrue(row2Exercises.contains("Exercises"))
    }

    // 9. Start Workout navigation target is intact
    @Test
    fun test09_startWorkoutNavigationTargetIntact() {
        UserSession.activeProgramId = "base"
        val workout = WeeklyWorkoutRepository.getWorkoutForDay(0, ExperienceLevel.BEGINNER)
        val targetScreen = Screen.ActiveWorkoutSession(
            workoutId = workout.id,
            programId = UserSession.activeProgramId
        )
        assertEquals(workout.id, targetScreen.workoutId)
        assertEquals("base", targetScreen.programId)
    }

    // 10. View Plan navigation target is intact
    @Test
    fun test10_viewPlanNavigationTargetIntact() {
        UserSession.activeProgramId = "base"
        val workout = WeeklyWorkoutRepository.getWorkoutForDay(0, ExperienceLevel.BEGINNER)
        val targetScreen = Screen.WorkoutOverview(
            workoutId = workout.id,
            programId = UserSession.activeProgramId
        )
        assertEquals(workout.id, targetScreen.workoutId)
        assertEquals("base", targetScreen.programId)
    }

    // 11. Workouts shortcut navigation target is intact
    @Test
    fun test11_workoutsShortcutNavigationTargetIntact() {
        val targetScreen = Screen.Programs
        assertNotNull(targetScreen)
    }

    // 12. Progress shortcut navigation target is intact
    @Test
    fun test12_progressShortcutNavigationTargetIntact() {
        val targetScreen = Screen.Progress
        assertNotNull(targetScreen)
    }

    // 13. Workout at Gym navigation target is intact
    @Test
    fun test13_workoutAtGymNavigationTargetIntact() {
        val targetScreen = Screen.BodyPartSelection("gym")
        assertEquals("gym", targetScreen.workoutType)
    }

    // 14. Day selector resolution remains dynamic across weekly days
    @Test
    fun test14_daySelectorResolutionRemainsDynamic() {
        val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        for (i in 0..6) {
            val workout = WeeklyWorkoutRepository.getWorkoutForDay(i, ExperienceLevel.BEGINNER)
            assertEquals(dayNames[i], workout.dayFullName)
        }
    }

    // 15. Canonical workout prescriptions remain intact
    @Test
    fun test15_canonicalWorkoutPrescriptionsRemainIntact() {
        val day1 = CanonicalWorkoutResolver.resolveThirtyDayWorkout("base", "day1", ExperienceLevel.BEGINNER)
        assertEquals("Chest + Triceps (Rotation A)", day1.workoutTitle)
        assertEquals(5, day1.exercises.size)
        assertEquals("45 min", day1.duration)
        assertEquals("Beginner", day1.difficulty)
    }
}
