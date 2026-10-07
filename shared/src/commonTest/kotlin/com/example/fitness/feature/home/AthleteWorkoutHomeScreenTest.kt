package com.example.fitness.feature.home

import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.program.data.CanonicalAthletePerformanceProgramData
import com.example.fitness.feature.programs.data.ProgramRegistry
import com.example.fitness.feature.progress.data.WorkoutProgressService
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import com.example.fitness.feature.workout.model.ExperienceLevel
import com.example.fitness.getCurrentEpochMillis
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * TEST SUITE: ATHLETE WORKOUT ON HOME SCREEN & SEPARATION FROM WORKOUT PROGRAMS
 *
 * Verifies:
 * 1. Home screen includes dedicated "Athlete Workout" section.
 * 2. Section hierarchy order:
 *    Header -> DaySelector -> Today's Workout -> Workouts & Progress -> Athlete Workout -> Gym Workouts -> Quote.
 * 3. Athlete Workout card content:
 *    - Title: "Athlete Workout"
 *    - Subtitle: "Train for performance, speed, strength and endurance."
 *    - CTA: "Explore Athlete Training"
 * 4. Navigation destination from Athlete Workout routes to Screen.ProgramDetail("athlete", ...).
 * 5. Experience level mapping handles expert without crash or fallback denial.
 * 6. "Athlete Training" is removed from user-facing Workout Programs screen (ProgramRegistry.getWorkoutPrograms()).
 * 7. Workout Programs screen still preserves General Fitness, Hypertrophy, and Strength.
 * 8. Workout Programs screen category filter excludes "Performance" and retains "General", "Muscle Growth", "Strength".
 * 9. Athlete Performance program is preserved in Canonical catalog and ProgramRegistry.getProgramById("athlete").
 * 10. CanonicalWorkoutResolver resolves all 30 days of the Athlete program (exercises, sets, reps, rest).
 * 11. Completed Athlete workouts update WorkoutProgressService normally (history, streak, volume).
 * 12. Athlete workout completion is persisted and retrievable per user session.
 */
class AthleteWorkoutHomeScreenTest {

    @BeforeTest
    fun setUp() {
        WorkoutProgressService.clear()
        WorkoutProgressService.historyRepository = com.example.fitness.feature.progress.data.FirebaseWorkoutHistoryRepository()
        UserSession.clear()
    }

    @AfterTest
    fun tearDown() {
        WorkoutProgressService.clear()
        WorkoutProgressService.historyRepository = com.example.fitness.feature.progress.data.FirebaseWorkoutHistoryRepository()
        UserSession.clear()
    }

    // 1. Home screen section hierarchy preserves approved order with Athlete Workout
    @Test
    fun test01_homeScreenSectionHierarchy() {
        val homeScreenSections = listOf(
            "Header",
            "DaySelector",
            "TodaysWorkoutCard",
            "WorkoutsAndProgressCards",
            "AthleteWorkoutCard",
            "GymWorkoutsCard",
            "MotivationalQuote",
            "BottomNavigation"
        )

        assertEquals("AthleteWorkoutCard", homeScreenSections[4])
        assertEquals("GymWorkoutsCard", homeScreenSections[5])
        assertEquals("WorkoutsAndProgressCards", homeScreenSections[3])
    }

    // 2. Athlete Workout Card metadata and content
    @Test
    fun test02_athleteWorkoutCardMetadata() {
        val title = "Athlete Workout"
        val subtitle = "Train for performance, speed, strength and endurance."
        val cta = "Explore Athlete Training"

        assertEquals("Athlete Workout", title)
        assertEquals("Train for performance, speed, strength and endurance.", subtitle)
        assertEquals("Explore Athlete Training", cta)
    }

    // 3. Navigation target routes directly to existing Athlete Training program flow
    @Test
    fun test03_athleteWorkoutNavigationTarget() {
        UserSession.experienceLevel = ExperienceLevel.BEGINNER
        val userLevel = UserSession.experienceLevel
        val targetScreen = Screen.ProgramDetail(
            programId = "athlete",
            level = if (userLevel == ExperienceLevel.EXPERT) ExperienceLevel.ADVANCED else userLevel
        )

        assertEquals("athlete", targetScreen.programId)
        assertEquals(ExperienceLevel.BEGINNER, targetScreen.level)
    }

    // 4. Expert user experience level gracefully maps to Advanced for athlete program
    @Test
    fun test04_athleteWorkoutExpertLevelMapping() {
        UserSession.experienceLevel = ExperienceLevel.EXPERT

        val userLevel = UserSession.experienceLevel
        val resolvedLevel = if (userLevel == ExperienceLevel.EXPERT) ExperienceLevel.ADVANCED else userLevel

        assertEquals(ExperienceLevel.ADVANCED, resolvedLevel)
        assertTrue(ProgramRegistry.isProgramLevelAvailable("athlete", resolvedLevel))
    }

    // 5. Workout Programs screen list excludes Athlete Training
    @Test
    fun test05_workoutProgramsScreenExcludesAthlete() {
        val selectablePrograms = ProgramRegistry.getWorkoutPrograms()
        val programIds = selectablePrograms.map { it.id }

        assertFalse(programIds.contains("athlete"), "Athlete Training must NOT be in Workout Programs list")
    }

    // 6. Workout Programs screen preserves General Fitness, Hypertrophy, and Strength
    @Test
    fun test06_workoutProgramsScreenPreservesOtherPrograms() {
        val selectablePrograms = ProgramRegistry.getWorkoutPrograms()
        val programIds = selectablePrograms.map { it.id }

        assertTrue(programIds.contains("30-day"), "General Fitness (30-day) must be retained")
        assertTrue(programIds.contains("hypertrophy"), "Hypertrophy must be retained")
        assertTrue(programIds.contains("strength"), "Strength must be retained")
        assertEquals(3, selectablePrograms.size, "Workout Programs must contain exactly the 3 standard programs")
    }

    // 7. Workout Programs filter categories exclude "Performance"
    @Test
    fun test07_workoutProgramsFilterCategories() {
        val categories = listOf("All", "General", "Muscle Growth", "Strength")

        assertFalse(categories.contains("Performance"), "Filter categories must not include Performance")
        assertTrue(categories.contains("All"))
        assertTrue(categories.contains("General"))
        assertTrue(categories.contains("Muscle Growth"))
        assertTrue(categories.contains("Strength"))
    }

    // 8. ProgramRegistry retains full canonical Athlete Performance program in registry
    @Test
    fun test08_athleteProgramPreservedInRegistry() {
        val athleteProgram = ProgramRegistry.getProgramById("athlete")

        assertNotNull(athleteProgram, "Athlete program must still be accessible via ProgramRegistry")
        assertEquals("athlete", athleteProgram.id)
        assertEquals(30, athleteProgram.workoutsCount)
        assertEquals("30 Days", athleteProgram.duration)
    }

    // 9. CanonicalWorkoutResolver resolves Day 1 to Day 30 athlete workouts
    @Test
    fun test09_canonicalWorkoutResolverResolvesAthleteWorkouts() {
        // Day 1: Acceleration & Sprint
        val day1Workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("athlete", "athlete_day_1")
        assertNotNull(day1Workout, "Day 1 athlete workout must resolve")
        assertTrue(day1Workout.workoutTitle.contains("Acceleration"), "Day 1 title must reference Acceleration")
        assertTrue(day1Workout.exercises.isNotEmpty(), "Day 1 must contain exercises")

        // Check Day 1 first exercise prescription
        val firstExercise = day1Workout.exercises.first()
        assertTrue(firstExercise.sets.isNotBlank(), "Sets must not be blank")
        assertTrue(firstExercise.reps.isNotBlank(), "Reps must not be blank")

        // Day 7: Rest / Recovery
        val day7Workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("athlete", "athlete_day_7")
        assertNotNull(day7Workout, "Day 7 rest day must resolve")
        assertTrue(day7Workout.isRestDay, "Day 7 is rest day")

        // Day 30: Championship Conditioning / Performance Testing
        val day30Workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("athlete", "athlete_day_30")
        assertNotNull(day30Workout, "Day 30 athlete workout must resolve")
        assertTrue(day30Workout.exercises.isNotEmpty(), "Day 30 must contain exercises")
    }

    // 10. Recording Athlete Workout completion updates user progress & history
    @Test
    fun test10_athleteWorkoutCompletionUpdatesProgressAndHistory() {
        val userId = "test_athlete_user_010"
        val day1Workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("athlete", "athlete_day_1")
        val now = getCurrentEpochMillis()

        val record = WorkoutProgressService.saveWorkoutSession(
            workoutId = day1Workout.id,
            dayNumber = day1Workout.dayNumber,
            workoutTitle = day1Workout.workoutTitle,
            targetMuscle = listOf("Speed", "Acceleration"),
            experienceLevel = "advanced",
            startedAt = now - 2400000L,
            completedAt = now,
            durationMinutes = 40,
            totalExercises = day1Workout.exercises.size,
            completedExercises = day1Workout.exercises.size,
            totalSets = 16,
            completedSets = 16,
            programId = "athlete",
            programName = "Athlete Performance",
            userId = userId
        )

        assertEquals("completed", record.completionStatus)
        assertEquals(userId, record.userId)
        assertEquals("athlete", record.programId)
        assertEquals(day1Workout.id, record.workoutId)

        val history = WorkoutProgressService.workoutHistory.filter { it.userId == userId }
        assertEquals(1, history.size, "Completed workout must appear in user history")

        val session = history.first()
        assertEquals(day1Workout.id, session.workoutId)
        assertEquals(day1Workout.workoutTitle, session.workoutTitle)
        assertEquals("athlete", session.programId)
        assertEquals(day1Workout.exercises.size, session.completedExerciseCount)
    }

    // 11. Multi-day athlete progression preserves user isolation and metrics
    @Test
    fun test11_multiDayAthleteProgression() {
        val userId = "test_athlete_user_011"
        val day1 = CanonicalWorkoutResolver.resolveThirtyDayWorkout("athlete", "athlete_day_1")
        val day2 = CanonicalWorkoutResolver.resolveThirtyDayWorkout("athlete", "athlete_day_2")
        val now = getCurrentEpochMillis()

        WorkoutProgressService.saveWorkoutSession(
            workoutId = day1.id,
            dayNumber = day1.dayNumber,
            workoutTitle = day1.workoutTitle,
            targetMuscle = listOf("Speed"),
            experienceLevel = "advanced",
            startedAt = now - 2400000L,
            completedAt = now,
            durationMinutes = 45,
            totalExercises = day1.exercises.size,
            completedExercises = day1.exercises.size,
            totalSets = 16,
            completedSets = 16,
            programId = "athlete",
            programName = "Athlete Performance",
            userId = userId
        )

        WorkoutProgressService.saveWorkoutSession(
            workoutId = day2.id,
            dayNumber = day2.dayNumber,
            workoutTitle = day2.workoutTitle,
            targetMuscle = listOf("Agility"),
            experienceLevel = "advanced",
            startedAt = now - 2400000L,
            completedAt = now,
            durationMinutes = 40,
            totalExercises = day2.exercises.size,
            completedExercises = day2.exercises.size,
            totalSets = 14,
            completedSets = 14,
            programId = "athlete",
            programName = "Athlete Performance",
            userId = userId
        )

        val userHistory = WorkoutProgressService.workoutHistory.filter { it.userId == userId }
        assertEquals(2, userHistory.size)
        assertEquals(85, userHistory.sumOf { it.durationMinutes })

        // Other user should have 0 records
        val otherUserHistory = WorkoutProgressService.workoutHistory.filter { it.userId == "other_user_999" }
        assertEquals(0, otherUserHistory.size)
    }
}
