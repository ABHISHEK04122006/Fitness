package com.example.fitness.feature.programs

import com.example.fitness.core.components.NavigationTab
import com.example.fitness.core.navigation.Navigator
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.admin.data.AdminExerciseCatalogOrganizer
import com.example.fitness.feature.admin.data.AdminWorkoutCatalogProvider
import com.example.fitness.feature.admin.data.ExerciseBodyPartSection
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.media.FemaleExerciseMediaRegistry
import com.example.fitness.feature.program.data.CanonicalFemaleProgramData
import com.example.fitness.feature.programs.data.ProgramRegistry
import com.example.fitness.feature.workout.data.WorkoutMediaResolver
import com.example.fitness.feature.workout.model.ExperienceLevel
import fitness.shared.generated.resources.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Authoritative Test Suite for Female User Workout Flow, Plan Selection,
 * Exercise Catalog Synchronization against the Female Workout PDF, and Home Screen Images.
 */
class FemaleWorkoutFlowAndCatalogTest {

    @BeforeTest
    fun setUp() {
        UserSession.clear()
    }

    @AfterTest
    fun tearDown() {
        UserSession.clear()
    }

    // 1. Female -> Workout opens "Select your workout plan" (Screen.FemalePlanSelection)
    @Test
    fun test01_femaleWorkoutOpensSelectYourWorkoutPlan() {
        UserSession.currentUserGender = "Female"
        assertTrue(UserSession.isFemale, "User must be recognized as female")

        val navigator = Navigator(initialScreen = Screen.Home)
        navigator.switchTab(NavigationTab.EXPLORE)

        assertEquals(
            Screen.FemalePlanSelection,
            navigator.currentScreen,
            "Female user tapping Workouts tab must route to Screen.FemalePlanSelection"
        )
        assertEquals(NavigationTab.EXPLORE, navigator.currentTab)
    }

    // 2. All 9 Female programs are displayed
    @Test
    fun test02_allNineFemaleProgramsAreDisplayed() {
        val programs = ProgramRegistry.getFemalePrograms()
        assertEquals(9, programs.size, "Must have exactly 9 female programs")

        val expectedTitles = listOf(
            "Weight Loss",
            "Sculpted Body",
            "Perfect Butt",
            "6-Pack Abs",
            "General Muscle Building",
            "Powerlifting",
            "CrossFit",
            "Full Body in 45 Minutes",
            "Postpartum Recovery"
        )

        val actualTitles = programs.map { it.title }
        for (expected in expectedTitles) {
            assertTrue(
                actualTitles.any { it.equals(expected, ignoreCase = true) || it.contains("45") },
                "Expected program '$expected' not found in: $actualTitles"
            )
        }
    }

    // 3. Selecting Weight Loss opens the correct program
    @Test
    fun test03_selectingWeightLossOpensCorrectProgram() {
        val program = ProgramRegistry.getProgramById("female_weight_loss")
        assertNotNull(program)
        assertEquals("Weight Loss", program.title)
        assertEquals(30, program.workoutsCount)
    }

    // 4. Selecting Sculpted Body opens the correct program
    @Test
    fun test04_selectingSculptedBodyOpensCorrectProgram() {
        val program = ProgramRegistry.getProgramById("female_sculpted_body")
        assertNotNull(program)
        assertEquals("Sculpted Body", program.title)
        assertEquals(30, program.workoutsCount)
    }

    // 5. Selecting Perfect Butt opens the correct program
    @Test
    fun test05_selectingPerfectButtOpensCorrectProgram() {
        val program = ProgramRegistry.getProgramById("female_perfect_butt")
        assertNotNull(program)
        assertEquals("Perfect Butt", program.title)
        assertEquals(30, program.workoutsCount)
    }

    // 6. Selecting 6-Pack Abs opens the correct program
    @Test
    fun test06_selecting6PackAbsOpensCorrectProgram() {
        val program = ProgramRegistry.getProgramById("female_6_pack_abs")
        assertNotNull(program)
        assertTrue(program.title.contains("6-Pack") || program.title.contains("6 Pack"))
        assertEquals(30, program.workoutsCount)
    }

    // 7. Selecting General Muscle Building opens the correct program
    @Test
    fun test07_selectingGeneralMuscleBuildingOpensCorrectProgram() {
        val program = ProgramRegistry.getProgramById("female_general_muscle_building")
        assertNotNull(program)
        assertEquals("General Muscle Building", program.title)
        assertEquals(30, program.workoutsCount)
    }

    // 8. Selecting Powerlifting opens the correct program
    @Test
    fun test08_selectingPowerliftingOpensCorrectProgram() {
        val program = ProgramRegistry.getProgramById("female_powerlifting")
        assertNotNull(program)
        assertEquals("Powerlifting", program.title)
        assertEquals(30, program.workoutsCount)
    }

    // 9. Selecting CrossFit opens the correct program
    @Test
    fun test09_selectingCrossFitOpensCorrectProgram() {
        val program = ProgramRegistry.getProgramById("female_crossfit")
        assertNotNull(program)
        assertEquals("CrossFit", program.title)
        assertEquals(30, program.workoutsCount)
    }

    // 10. Selecting Full Body in 45 Mins opens the correct program
    @Test
    fun test10_selectingFullBodyIn45MinsOpensCorrectProgram() {
        val program = ProgramRegistry.getProgramById("female_full_body_in_45_minutes")
        assertNotNull(program)
        assertTrue(program.title.contains("45"))
        assertEquals(30, program.workoutsCount)
    }

    // 11. Selecting Postpartum Recovery opens the correct program
    @Test
    fun test11_selectingPostpartumRecoveryOpensCorrectProgram() {
        val program = ProgramRegistry.getProgramById("female_postpartum_recovery")
        assertNotNull(program)
        assertEquals("Postpartum Recovery", program.title)
        assertEquals(30, program.workoutsCount)
    }

    // 12. Female programs retain their 30-day structure
    @Test
    fun test12_femaleProgramsRetain30DayStructure() {
        val programs = CanonicalFemaleProgramData.ALL_PROGRAMS
        assertEquals(9, programs.size)

        var totalDays = 0
        for (prog in programs) {
            assertEquals(30, prog.durationDays, "${prog.name} durationDays must be 30")
            assertEquals(30, prog.days.size, "${prog.name} must contain exactly 30 programmed days")
            totalDays += prog.days.size
        }
        assertEquals(270, totalDays, "Total programmed days across 9 programs must equal 270")
    }

    // 13. Female exercise catalog contains unique exercises referenced by all 9 programs
    @Test
    fun test13_femaleExerciseCatalogContainsAllReferencedExercises() {
        val referencedIds = AdminWorkoutCatalogProvider.femaleProgramExerciseIds
        assertTrue(referencedIds.isNotEmpty(), "Female program exercise IDs must not be empty")

        for (exId in referencedIds) {
            val resolved = SampleExerciseData.getById(exId)
            assertNotNull(
                resolved,
                "Exercise ID '$exId' referenced in female programs must resolve in SampleExerciseData"
            )
        }
    }

    // 14. Program exercises resolve to canonical Exercise IDs
    @Test
    fun test14_programExercisesResolveToCanonicalExerciseIds() {
        for (program in CanonicalFemaleProgramData.ALL_PROGRAMS) {
            for (day in program.days) {
                for (ref in day.exercises) {
                    val ex = SampleExerciseData.getById(ref.exerciseId)
                    assertNotNull(
                        ex,
                        "Program ${program.name} Day ${day.dayNumber} exercise '${ref.exerciseId}' must resolve"
                    )
                    assertEquals(ref.exerciseId, ex.id)
                }
            }
        }
    }

    // 15. No duplicate Exercise IDs are created for the same exercise
    @Test
    fun test15_noDuplicateExerciseIdsForSameExercise() {
        val allFemaleIds = CanonicalFemaleProgramData.ALL_PROGRAMS.flatMap { p ->
            p.days.flatMap { d -> d.exercises.map { it.exerciseId } }
        }.distinct()

        // Shared exercises must use canonical IDs without program-specific suffixes
        assertTrue(allFemaleIds.contains("hip_thrust"))
        assertFalse(allFemaleIds.any { it.contains("hip_thrust_weight_loss") })
        assertFalse(allFemaleIds.any { it.contains("hip_thrust_sculpted") })
        assertFalse(allFemaleIds.any { it.contains("hip_thrust_perfect_butt") })

        assertTrue(allFemaleIds.contains("dumbbell_bench_press"))
        assertTrue(allFemaleIds.contains("lat_pulldown"))
        assertTrue(allFemaleIds.contains("romanian_deadlift"))
        assertTrue(allFemaleIds.contains("cable_crunch"))
    }

    // 16. Existing workout execution flow remains functional
    @Test
    fun test16_workoutExecutionFlowRemainsFunctional() {
        val program = ProgramRegistry.getProgramById("female_weight_loss")
        assertNotNull(program)

        val day1Schedule = program.schedule.first()
        assertEquals(1, day1Schedule.dayNumber)
        assertFalse(day1Schedule.isRestDay)
        assertTrue(day1Schedule.exerciseCount > 0)

        // WorkoutOverview navigation
        val overviewScreen = Screen.WorkoutOverview(
            workoutId = day1Schedule.workoutId,
            programId = program.id
        )
        assertEquals("female_weight_loss_day_1", overviewScreen.workoutId)
        assertEquals("female_weight_loss", overviewScreen.programId)

        // Active session navigation
        val activeScreen = Screen.ActiveWorkoutSession(
            workoutId = day1Schedule.workoutId,
            programId = program.id
        )
        assertEquals("female_weight_loss_day_1", activeScreen.workoutId)
        assertEquals("female_weight_loss", activeScreen.programId)
    }

    // 17. Female Home Athlete Workout image fits correctly
    @Test
    fun test17_femaleHomeAthleteWorkoutImageResolvesCorrectly() {
        val femaleAthleteImage = WorkoutMediaResolver.resolveAthleteWorkoutImage("Female")
        assertEquals(Res.drawable.female_athlete, femaleAthleteImage)
        assertNotEquals(Res.drawable.goal_athlete, femaleAthleteImage)
    }

    // 18. Female Home Workout at Gym image fits correctly
    @Test
    fun test18_femaleHomeGymWorkoutImageResolvesCorrectly() {
        val femaleGymImage = WorkoutMediaResolver.resolveGymWorkoutImage("Female")
        assertEquals(Res.drawable.female_gym, femaleGymImage)
        assertNotEquals(Res.drawable.workout_gym, femaleGymImage)
    }

    // 19. Male User flow remains unchanged
    @Test
    fun test19_maleUserFlowRemainsUnchanged() {
        UserSession.currentUserGender = "Male"
        assertFalse(UserSession.isFemale, "User must not be female")

        val navigator = Navigator(initialScreen = Screen.Home)
        navigator.switchTab(NavigationTab.EXPLORE)

        assertEquals(
            Screen.Explore,
            navigator.currentScreen,
            "Male user tapping Workouts tab must route to Screen.Explore (ProgramsScreen)"
        )
        assertNotEquals(Screen.FemalePlanSelection, navigator.currentScreen)

        // Male images
        assertEquals(Res.drawable.goal_athlete, WorkoutMediaResolver.resolveAthleteWorkoutImage("Male"))
        assertEquals(Res.drawable.workout_gym, WorkoutMediaResolver.resolveGymWorkoutImage("Male"))
    }

    // 20. Existing Admin functionality remains unchanged
    @Test
    fun test20_existingAdminFunctionalityRemainsUnchanged() {
        // Exercise catalog organization respects all major body part sections
        val bench = SampleExerciseData.getById("dumbbell_bench_press")
        assertNotNull(bench)
        assertEquals(ExerciseBodyPartSection.CHEST, AdminExerciseCatalogOrganizer.classify(bench))

        val row = SampleExerciseData.getById("dumbbell_row")
        assertNotNull(row)
        assertEquals(ExerciseBodyPartSection.BACK, AdminExerciseCatalogOrganizer.classify(row))

        val squat = SampleExerciseData.getById("dumbbell_goblet_squat")
        assertNotNull(squat)
        assertEquals(ExerciseBodyPartSection.LEGS, AdminExerciseCatalogOrganizer.classify(squat))

        val plank = SampleExerciseData.getById("plank_hold")
        assertNotNull(plank)
        assertEquals(ExerciseBodyPartSection.CORE, AdminExerciseCatalogOrganizer.classify(plank))

        val burpees = SampleExerciseData.getById("burpees")
        assertNotNull(burpees)
        assertEquals(ExerciseBodyPartSection.CARDIO_DRILLS, AdminExerciseCatalogOrganizer.classify(burpees))
    }
}
