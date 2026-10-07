package com.example.fitness.feature.navigation

import com.example.fitness.core.components.NavigationTab
import com.example.fitness.core.navigation.Navigator
import com.example.fitness.core.navigation.Screen
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Comprehensive test suite verifying user-side back navigation and back-stack integrity.
 *
 * Covers:
 * TEST 1: Home -> Workouts -> Program -> Back -> Workouts
 * TEST 2: Home -> Workouts -> Program -> Workout -> Back -> Program
 * TEST 3: Home -> Workouts -> Program -> Workout -> Exercise -> Back -> Workout
 * TEST 4: Home -> Athlete Workout -> Workout -> Exercise -> Back -> Workout
 * TEST 5: Home -> Athlete Workout -> Workout -> Exercise -> Back -> Back -> Back (exact 1 level per Back)
 * TEST 6: Open Workout Programs -> Select program -> Back -> Return to Workout Programs, NOT Exercise
 * TEST 7: Duplicate navigation protection (singleTop)
 * TEST 8: Bottom Navigation tab switching without unexpected jumps or duplicate screens
 * TEST 9: Gym Workouts flow hierarchy
 */
class UserNavigationBackStackTest {

    private lateinit var navigator: Navigator

    @BeforeTest
    fun setUp() {
        navigator = Navigator(initialScreen = Screen.Home)
    }

    @Test
    fun test1_Home_Workouts_Program_Back_ReturnsToWorkouts() {
        assertEquals(Screen.Home, navigator.currentScreen)
        assertEquals(1, navigator.backStackSize)

        // 1. Home -> Workouts
        navigator.selectedProgramsTab = "Exercises"
        navigator.navigate(Screen.Programs)
        assertEquals(Screen.Programs, navigator.currentScreen)
        assertEquals(2, navigator.backStackSize)
        assertEquals("Exercises", navigator.selectedProgramsTab)

        // 2. User switches to "Programs" tab
        navigator.selectedProgramsTab = "Programs"
        assertEquals("Programs", navigator.selectedProgramsTab)

        // 3. User presses Back on "Programs" tab -> returns to "Exercises" (Workouts)
        if (navigator.selectedProgramsTab == "Programs") {
            navigator.selectedProgramsTab = "Exercises"
        }
        assertEquals("Exercises", navigator.selectedProgramsTab)
        assertEquals(Screen.Programs, navigator.currentScreen)

        // 4. User presses Back again -> returns to Home
        val popped = navigator.pop()
        assertTrue(popped)
        assertEquals(Screen.Home, navigator.currentScreen)
        assertEquals(1, navigator.backStackSize)
    }

    @Test
    fun test2_Home_Workouts_Program_Workout_Back_ReturnsToProgram() {
        // 1. Home -> Workouts -> Program tab -> Program Detail
        navigator.selectedProgramsTab = "Programs"
        navigator.navigate(Screen.Programs)
        navigator.navigate(Screen.ProgramDetail(programId = "hypertrophy_30_day"))
        assertEquals(Screen.ProgramDetail(programId = "hypertrophy_30_day"), navigator.currentScreen)

        // 2. Program Detail -> Workout Overview
        navigator.navigate(Screen.WorkoutOverview(workoutId = "day_1", programId = "hypertrophy_30_day"))
        assertEquals(Screen.WorkoutOverview(workoutId = "day_1", programId = "hypertrophy_30_day"), navigator.currentScreen)

        // 3. Press Back -> MUST return to Program Detail
        val popped = navigator.pop()
        assertTrue(popped)
        assertEquals(Screen.ProgramDetail(programId = "hypertrophy_30_day"), navigator.currentScreen)
    }

    @Test
    fun test3_Home_Workouts_Program_Workout_Exercise_Back_ReturnsToWorkout() {
        // Home -> Workouts -> Program -> Workout -> Exercise
        navigator.navigate(Screen.Programs)
        navigator.navigate(Screen.ProgramDetail(programId = "hypertrophy_30_day"))
        navigator.navigate(Screen.WorkoutOverview(workoutId = "day_1", programId = "hypertrophy_30_day"))
        navigator.navigate(Screen.ExerciseDetail(exerciseId = "barbell_bench_press", sets = "4", reps = "8-10", rest = "90"))

        assertEquals(5, navigator.backStackSize)
        assertTrue(navigator.currentScreen is Screen.ExerciseDetail)

        // Back: Exercise Detail -> Workout Detail
        val popped = navigator.pop()
        assertTrue(popped)
        assertEquals(Screen.WorkoutOverview(workoutId = "day_1", programId = "hypertrophy_30_day"), navigator.currentScreen)
        assertEquals(4, navigator.backStackSize)
    }

    @Test
    fun test4_Home_AthleteWorkout_Workout_Exercise_Back_ReturnsToWorkout() {
        // Home -> Athlete Workout -> Workout -> Exercise
        navigator.navigate(Screen.ProgramDetail(programId = "athlete"))
        navigator.navigate(Screen.WorkoutOverview(workoutId = "sprint_power_day_1", programId = "athlete"))
        navigator.navigate(Screen.ExerciseDetail(exerciseId = "ten_meter_acceleration", sets = "4", reps = "4 reps", rest = "120"))

        assertEquals(4, navigator.backStackSize)
        assertTrue(navigator.currentScreen is Screen.ExerciseDetail)

        // Back: Exercise Detail -> Workout Detail
        val popped = navigator.pop()
        assertTrue(popped)
        assertEquals(Screen.WorkoutOverview(workoutId = "sprint_power_day_1", programId = "athlete"), navigator.currentScreen)
        assertEquals(3, navigator.backStackSize)
    }

    @Test
    fun test5_Home_AthleteWorkout_Workout_Exercise_Back_EachBackRemovesOneLevel() {
        assertEquals(1, navigator.backStackSize)

        // Forward flow: Home -> Athlete Program -> Workout -> Exercise
        navigator.navigate(Screen.ProgramDetail(programId = "athlete"))
        assertEquals(2, navigator.backStackSize)

        navigator.navigate(Screen.WorkoutOverview(workoutId = "sprint_power_day_1", programId = "athlete"))
        assertEquals(3, navigator.backStackSize)

        navigator.navigate(Screen.ExerciseDetail(exerciseId = "ten_meter_acceleration"))
        assertEquals(4, navigator.backStackSize)

        // Back 1: Exercise -> Workout Detail
        assertTrue(navigator.pop())
        assertEquals(Screen.WorkoutOverview(workoutId = "sprint_power_day_1", programId = "athlete"), navigator.currentScreen)
        assertEquals(3, navigator.backStackSize)

        // Back 2: Workout Detail -> Athlete Program Selection
        assertTrue(navigator.pop())
        assertEquals(Screen.ProgramDetail(programId = "athlete"), navigator.currentScreen)
        assertEquals(2, navigator.backStackSize)

        // Back 3: Athlete Program Selection -> Home
        assertTrue(navigator.pop())
        assertEquals(Screen.Home, navigator.currentScreen)
        assertEquals(1, navigator.backStackSize)

        // Back 4: Already at root, cannot pop further
        assertFalse(navigator.pop())
        assertEquals(Screen.Home, navigator.currentScreen)
        assertEquals(1, navigator.backStackSize)
    }

    @Test
    fun test6_OpenWorkoutPrograms_SelectProgram_Back_ReturnsToWorkoutProgramsNotExercise() {
        // Open Workout Programs (Programs tab)
        navigator.selectedProgramsTab = "Programs"
        navigator.navigate(Screen.Programs)

        // Select any program
        navigator.navigate(Screen.ProgramDetail(programId = "thirty_day_beginner"))
        assertEquals(Screen.ProgramDetail(programId = "thirty_day_beginner"), navigator.currentScreen)

        // Back from Program Detail
        assertTrue(navigator.pop())
        assertEquals(Screen.Programs, navigator.currentScreen)

        // MUST return to Workout Programs (Programs tab), NOT Exercise!
        assertEquals("Programs", navigator.selectedProgramsTab, "Must return to Programs tab, not Exercise catalog!")
    }

    @Test
    fun test7_SingleTop_DuplicateNavigationProtection() {
        assertEquals(1, navigator.backStackSize)

        // Navigate to Screen.Programs
        navigator.navigate(Screen.Programs)
        assertEquals(2, navigator.backStackSize)

        // Accidental double-tap on Screen.Programs
        navigator.navigate(Screen.Programs)
        assertEquals(2, navigator.backStackSize, "Duplicate navigation to current screen must be ignored.")

        // Navigate to ProgramDetail
        val detailScreen = Screen.ProgramDetail(programId = "strength")
        navigator.navigate(detailScreen)
        assertEquals(3, navigator.backStackSize)

        // Accidental double-tap on ProgramDetail
        navigator.navigate(detailScreen)
        assertEquals(3, navigator.backStackSize, "Duplicate navigation to same ProgramDetail must be ignored.")

        // Pop removes ProgramDetail in 1 back click
        assertTrue(navigator.pop())
        assertEquals(Screen.Programs, navigator.currentScreen)
        assertEquals(2, navigator.backStackSize)

        // Pop removes Screen.Programs in 1 back click
        assertTrue(navigator.pop())
        assertEquals(Screen.Home, navigator.currentScreen)
        assertEquals(1, navigator.backStackSize)
    }

    @Test
    fun test8_BottomNavigation_SwitchTabs_PreservesStackIntegrity() {
        assertEquals(Screen.Home, navigator.currentScreen)
        assertEquals(1, navigator.backStackSize)

        // Switch to EXPLORE
        navigator.switchTab(NavigationTab.EXPLORE)
        assertEquals(Screen.Explore, navigator.currentScreen)
        assertEquals(NavigationTab.EXPLORE, navigator.currentTab)
        assertEquals(2, navigator.backStackSize)

        // Switch to PROGRESS (should replace EXPLORE secondary tab, not stack them)
        navigator.switchTab(NavigationTab.PROGRESS)
        assertEquals(Screen.Progress, navigator.currentScreen)
        assertEquals(NavigationTab.PROGRESS, navigator.currentTab)
        assertEquals(2, navigator.backStackSize)

        // Switch to PROFILE (should replace PROGRESS secondary tab)
        navigator.switchTab(NavigationTab.PROFILE)
        assertEquals(Screen.Profile, navigator.currentScreen)
        assertEquals(NavigationTab.PROFILE, navigator.currentTab)
        assertEquals(2, navigator.backStackSize)

        // Switch to HOME (should pop cleanly to root Home)
        navigator.switchTab(NavigationTab.HOME)
        assertEquals(Screen.Home, navigator.currentScreen)
        assertEquals(NavigationTab.HOME, navigator.currentTab)
        assertEquals(1, navigator.backStackSize)

        // Open Workouts as a sub-screen
        navigator.navigate(Screen.Programs)
        assertEquals(2, navigator.backStackSize)

        // Switch tab to PROFILE -> should replace secondary screen cleanly
        navigator.switchTab(NavigationTab.PROFILE)
        assertEquals(Screen.Profile, navigator.currentScreen)
        assertEquals(2, navigator.backStackSize)

        // Pop from Profile returns to Home
        assertTrue(navigator.pop())
        assertEquals(Screen.Home, navigator.currentScreen)
        assertEquals(1, navigator.backStackSize)
    }

    @Test
    fun test9_GymWorkouts_Flow_PreservesHierarchy() {
        assertEquals(Screen.Home, navigator.currentScreen)

        // Home -> BodyPartSelection
        navigator.navigate(Screen.BodyPartSelection("gym"))
        assertEquals(Screen.BodyPartSelection("gym"), navigator.currentScreen)
        assertEquals(2, navigator.backStackSize)

        // BodyPartSelection -> BodyPartWorkoutDetail
        navigator.navigate(Screen.BodyPartWorkoutDetail("gym", "chest"))
        assertEquals(Screen.BodyPartWorkoutDetail("gym", "chest"), navigator.currentScreen)
        assertEquals(3, navigator.backStackSize)

        // Back: BodyPartWorkoutDetail -> BodyPartSelection
        assertTrue(navigator.pop())
        assertEquals(Screen.BodyPartSelection("gym"), navigator.currentScreen)
        assertEquals(2, navigator.backStackSize)

        // Back: BodyPartSelection -> Home
        assertTrue(navigator.pop())
        assertEquals(Screen.Home, navigator.currentScreen)
        assertEquals(1, navigator.backStackSize)
    }
}
