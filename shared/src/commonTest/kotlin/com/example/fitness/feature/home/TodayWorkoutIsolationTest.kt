package com.example.fitness.feature.home

import com.example.fitness.core.navigation.Navigator
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.programs.data.ProgramRegistry
import com.example.fitness.feature.progress.data.WorkoutProgressService
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import com.example.fitness.feature.workout.model.ExperienceLevel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * TEST SUITE: TODAY'S WORKOUT ISOLATION FROM ATHLETE WORKOUT
 *
 * Verifies that browsing or navigating into Athlete Workout (or any program/exercise)
 * NEVER contaminates, mutates, or replaces the user's scheduled Today's Workout on HomeScreen.
 *
 * Covers:
 * - Test A: Home -> Open Athlete Workout -> Open any category -> Back -> Unchanged
 * - Test B: Home -> Open Athlete Workout -> Open Acceleration & Sprint -> Back -> Unchanged
 * - Test C: Home -> Open normal workout -> Open exercise -> Back -> Unchanged
 * - Test D: Selected day change is the only trigger for daily schedule updates
 * - Test E: Exact Workout IDs verification before/during/after Athlete navigation
 * - Test F: Dynamic determination across different programs and genders (no hardcoding)
 */
class TodayWorkoutIsolationTest {

    private lateinit var navigator: Navigator

    @BeforeTest
    fun setUp() {
        WorkoutProgressService.clear()
        WorkoutProgressService.historyRepository = com.example.fitness.feature.progress.data.FirebaseWorkoutHistoryRepository()
        UserSession.clear()
        UserSession.currentUserId = "test_user_isolation"
        navigator = Navigator()
        navigator.navigate(Screen.Home)
    }

    @AfterTest
    fun tearDown() {
        WorkoutProgressService.clear()
        WorkoutProgressService.historyRepository = com.example.fitness.feature.progress.data.FirebaseWorkoutHistoryRepository()
        UserSession.clear()
    }

    // Test A: Home -> Open Athlete Workout -> Open any Athlete Training category -> Press Back -> Unchanged
    @Test
    fun testA_athleteWorkoutBrowsingPreservesTodayWorkout() {
        UserSession.weeklyCommitment = "4 Days"
        UserSession.experienceLevel = ExperienceLevel.ADVANCED
        val selectedDayIndex = 0 // Monday

        // 1. Initial Home Today's Workout
        val beforeWorkout = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = UserSession.scheduledProgramId,
            dayIndex = selectedDayIndex,
            level = UserSession.experienceLevel,
            workoutDays = UserSession.weeklyCommitment
        )

        assertEquals("Chest + Triceps (Rotation A)", beforeWorkout.title)
        assertEquals("45 min", beforeWorkout.duration)
        assertEquals("Advanced", beforeWorkout.difficulty)
        assertEquals(6, beforeWorkout.exercises.size)
        assertEquals("day_1", beforeWorkout.id)

        // 2. Open Athlete Workout
        navigator.navigate(
            Screen.ProgramDetail(
                programId = "athlete",
                level = if (UserSession.experienceLevel == ExperienceLevel.EXPERT) ExperienceLevel.ADVANCED else UserSession.experienceLevel
            )
        )
        assertEquals("athlete", (navigator.currentScreen as Screen.ProgramDetail).programId)

        // 3. User browses Athlete Training category
        val athleteProgram = ProgramRegistry.getProgramById("athlete")
        assertEquals("Performance", athleteProgram?.category)

        // 4. User presses Back until Home
        navigator.pop()
        assertEquals(Screen.Home, navigator.currentScreen)

        // 5. Verify Today's Workout on Home is STILL Chest + Triceps (Rotation A)
        val afterWorkout = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = UserSession.scheduledProgramId,
            dayIndex = selectedDayIndex,
            level = UserSession.experienceLevel,
            workoutDays = UserSession.weeklyCommitment
        )

        assertEquals("Chest + Triceps (Rotation A)", afterWorkout.title)
        assertEquals("45 min", afterWorkout.duration)
        assertEquals("Advanced", afterWorkout.difficulty)
        assertEquals(6, afterWorkout.exercises.size)
        assertEquals("day_1", afterWorkout.id)
        assertEquals(beforeWorkout.id, afterWorkout.id)
    }

    // Test B: Home -> Open Athlete Workout -> Open Acceleration & Sprint -> Back -> Unchanged
    @Test
    fun testB_openingAccelerationAndSprintPreservesTodayWorkout() {
        UserSession.weeklyCommitment = "4 Days"
        UserSession.experienceLevel = ExperienceLevel.ADVANCED
        val selectedDayIndex = 0 // Monday

        // 1. Initial Home Today's Workout
        val beforeWorkout = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = UserSession.scheduledProgramId,
            dayIndex = selectedDayIndex,
            level = UserSession.experienceLevel,
            workoutDays = UserSession.weeklyCommitment
        )
        assertEquals("Chest + Triceps (Rotation A)", beforeWorkout.title)
        assertEquals("Advanced", beforeWorkout.difficulty)
        assertEquals(6, beforeWorkout.exercises.size)
        assertEquals("day_1", beforeWorkout.id)

        // 2. Navigate to Athlete Workout
        navigator.navigate(Screen.ProgramDetail(programId = "athlete"))

        // 3. Open Acceleration & Sprint (Day 1 of Athlete program)
        val athleteDay1 = CanonicalWorkoutResolver.resolveDailyWorkoutPlan("athlete", "athlete_day_1", UserSession.experienceLevel)
        assertTrue(athleteDay1.title.contains("Acceleration & Sprint"), "Athlete Day 1 title must be Acceleration & Sprint")
        assertEquals("Challenging", athleteDay1.difficulty)
        assertEquals(4, athleteDay1.exercises.size)
        assertEquals("athlete_day_1", athleteDay1.id)

        // Navigate to WorkoutOverview for Acceleration & Sprint
        navigator.navigate(Screen.WorkoutOverview(workoutId = athleteDay1.id, programId = "athlete"))
        assertEquals("athlete_day_1", (navigator.currentScreen as Screen.WorkoutOverview).workoutId)

        // 4. Back to ProgramDetail, then Back to Home
        navigator.pop() // Back to ProgramDetail
        navigator.pop() // Back to Home
        assertEquals(Screen.Home, navigator.currentScreen)

        // 5. Today's Workout must STILL be Chest + Triceps (Rotation A), NOT Acceleration & Sprint
        val afterWorkout = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = UserSession.scheduledProgramId,
            dayIndex = selectedDayIndex,
            level = UserSession.experienceLevel,
            workoutDays = UserSession.weeklyCommitment
        )

        assertEquals("Chest + Triceps (Rotation A)", afterWorkout.title)
        assertNotEquals("Acceleration & Sprint", afterWorkout.title)
        assertEquals("day_1", afterWorkout.id)
        assertNotEquals("athlete_day_1", afterWorkout.id)
        assertEquals(6, afterWorkout.exercises.size)
        assertEquals("Advanced", afterWorkout.difficulty)
    }

    // Test C: Home -> Open normal Workout section -> Open exercise/workout -> Back -> Unchanged
    @Test
    fun testC_openingNormalWorkoutSectionPreservesTodayWorkout() {
        UserSession.weeklyCommitment = "4 Days"
        UserSession.experienceLevel = ExperienceLevel.ADVANCED
        val selectedDayIndex = 0

        val beforeWorkout = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = UserSession.scheduledProgramId,
            dayIndex = selectedDayIndex,
            level = UserSession.experienceLevel,
            workoutDays = UserSession.weeklyCommitment
        )

        // Navigate to Today's Workout Overview
        navigator.navigate(Screen.WorkoutOverview(workoutId = beforeWorkout.id, programId = UserSession.scheduledProgramId))

        // Navigate to exercise detail
        navigator.navigate(Screen.ExerciseDetail(exerciseId = beforeWorkout.exercises.first().id))

        // Back to Overview, then Back to Home
        navigator.pop()
        navigator.pop()
        assertEquals(Screen.Home, navigator.currentScreen)

        val afterWorkout = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = UserSession.scheduledProgramId,
            dayIndex = selectedDayIndex,
            level = UserSession.experienceLevel,
            workoutDays = UserSession.weeklyCommitment
        )

        assertEquals(beforeWorkout.id, afterWorkout.id)
        assertEquals(beforeWorkout.title, afterWorkout.title)
        assertEquals(beforeWorkout.exercises.size, afterWorkout.exercises.size)
    }

    // Test D: Change the selected day -> Today's Workout changes only because the user changed the scheduled day
    @Test
    fun testD_todayWorkoutChangesOnlyWhenDayChanges() {
        UserSession.weeklyCommitment = "4 Days"
        UserSession.experienceLevel = ExperienceLevel.ADVANCED

        // Monday (Day 0) -> Day 1 Workout
        val mondayWorkout = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = UserSession.scheduledProgramId,
            dayIndex = 0,
            level = UserSession.experienceLevel,
            workoutDays = UserSession.weeklyCommitment
        )
        assertEquals("Chest + Triceps (Rotation A)", mondayWorkout.title)
        assertEquals("day_1", mondayWorkout.id)

        // Tuesday (Day 1) -> Day 2 Workout
        val tuesdayWorkout = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = UserSession.scheduledProgramId,
            dayIndex = 1,
            level = UserSession.experienceLevel,
            workoutDays = UserSession.weeklyCommitment
        )
        assertEquals("Back + Biceps (Rotation A)", tuesdayWorkout.title)
        assertEquals("day_2", tuesdayWorkout.id)

        // Wednesday (Day 2) -> Rest Day in 4-day split
        val wednesdayWorkout = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = UserSession.scheduledProgramId,
            dayIndex = 2,
            level = UserSession.experienceLevel,
            workoutDays = UserSession.weeklyCommitment
        )
        assertTrue(wednesdayWorkout.isRestDay)
        assertEquals("Rest / Recovery", wednesdayWorkout.title)

        // Switch back to Monday -> Identical to original Monday workout
        val mondayWorkoutAgain = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = UserSession.scheduledProgramId,
            dayIndex = 0,
            level = UserSession.experienceLevel,
            workoutDays = UserSession.weeklyCommitment
        )
        assertEquals(mondayWorkout.id, mondayWorkoutAgain.id)
        assertEquals(mondayWorkout.title, mondayWorkoutAgain.title)
    }

    // Test E: Exact data verification before/during/after Athlete navigation
    @Test
    fun testE_exactDataVerificationBeforeDuringAfterAthleteNavigation() {
        UserSession.weeklyCommitment = "4 Days"
        UserSession.experienceLevel = ExperienceLevel.ADVANCED
        val selectedDayIndex = 0

        // Today's Workout ID before entering Athlete Workout
        val todayWorkoutBefore = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = UserSession.scheduledProgramId,
            dayIndex = selectedDayIndex,
            level = UserSession.experienceLevel,
            workoutDays = UserSession.weeklyCommitment
        )
        val todayWorkoutIdBefore = todayWorkoutBefore.id
        assertEquals("day_1", todayWorkoutIdBefore)
        assertEquals("Chest + Triceps (Rotation A)", todayWorkoutBefore.title)

        // Athlete Workout ID
        val athleteDay1 = CanonicalWorkoutResolver.resolveDailyWorkoutPlan("athlete", "athlete_day_1", UserSession.experienceLevel)
        val athleteWorkoutId = athleteDay1.id
        assertEquals("athlete_day_1", athleteWorkoutId)
        assertEquals("Acceleration & Sprint", athleteDay1.title)

        // Selected navigation route
        val athleteNavRoute = Screen.ProgramDetail(
            programId = "athlete",
            level = if (UserSession.experienceLevel == ExperienceLevel.EXPERT) ExperienceLevel.ADVANCED else UserSession.experienceLevel
        )
        navigator.navigate(athleteNavRoute)
        assertEquals("athlete", (navigator.currentScreen as Screen.ProgramDetail).programId)

        // Return to Home
        navigator.pop()
        assertEquals(Screen.Home, navigator.currentScreen)

        // Today's Workout ID after returning to Home
        val todayWorkoutAfter = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = UserSession.scheduledProgramId,
            dayIndex = selectedDayIndex,
            level = UserSession.experienceLevel,
            workoutDays = UserSession.weeklyCommitment
        )
        val todayWorkoutIdAfter = todayWorkoutAfter.id

        // The Today's Workout ID must remain identical before and after the Athlete Workout navigation
        assertEquals(todayWorkoutIdBefore, todayWorkoutIdAfter)
        assertEquals("day_1", todayWorkoutIdAfter)
        assertNotEquals(athleteWorkoutId, todayWorkoutIdAfter)
    }

    // Test F: Dynamic determination across different programs and genders (no hardcoding)
    @Test
    fun testF_dynamicDeterminationPreservedAcrossPrograms() {
        // 1. Female Weight Loss Program
        UserSession.currentUserGender = "Female"
        UserSession.setActiveProgram("female_weight_loss")
        UserSession.weeklyCommitment = "4 Days"
        UserSession.experienceLevel = ExperienceLevel.BEGINNER

        val femaleDay0 = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = UserSession.scheduledProgramId,
            dayIndex = 0,
            level = UserSession.experienceLevel,
            workoutDays = UserSession.weeklyCommitment
        )
        assertTrue(femaleDay0.id.startsWith("female_weight_loss_day_"), "Must resolve female program dynamically")

        // Explore Athlete Workout and return
        navigator.navigate(Screen.ProgramDetail("athlete"))
        navigator.pop()

        val femaleDay0After = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = UserSession.scheduledProgramId,
            dayIndex = 0,
            level = UserSession.experienceLevel,
            workoutDays = UserSession.weeklyCommitment
        )
        assertEquals(femaleDay0.id, femaleDay0After.id, "Female program must remain unchanged after Athlete Workout")

        // 2. Hypertrophy Program
        UserSession.currentUserGender = "Male"
        UserSession.setActiveProgram("hypertrophy")

        val hypertrophyDay0 = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = UserSession.scheduledProgramId,
            dayIndex = 0,
            level = UserSession.experienceLevel,
            workoutDays = UserSession.weeklyCommitment
        )
        assertTrue(hypertrophyDay0.id.startsWith("hypertrophy_day_"), "Must resolve hypertrophy program dynamically")

        // Explore Athlete Workout and return
        navigator.navigate(Screen.ProgramDetail("athlete"))
        navigator.pop()

        val hypertrophyDay0After = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = UserSession.scheduledProgramId,
            dayIndex = 0,
            level = UserSession.experienceLevel,
            workoutDays = UserSession.weeklyCommitment
        )
        assertEquals(hypertrophyDay0.id, hypertrophyDay0After.id, "Hypertrophy program must remain unchanged after Athlete Workout")
    }
}
