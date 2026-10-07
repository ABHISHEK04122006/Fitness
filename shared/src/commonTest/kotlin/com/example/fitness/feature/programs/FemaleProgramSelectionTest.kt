package com.example.fitness.feature.programs

import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.program.data.CanonicalFemaleProgramData
import com.example.fitness.feature.programs.data.ProgramRegistry
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import com.example.fitness.feature.workout.model.ExperienceLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FemaleProgramSelectionTest {

    @Test
    fun testAllNineFemaleProgramsExistWithExactNames() {
        val expectedPrograms = listOf(
            "Weight Loss" to "female_weight_loss",
            "Sculpted Body" to "female_sculpted_body",
            "Perfect Butt" to "female_perfect_butt",
            "6-Pack Abs" to "female_6_pack_abs",
            "General Muscle Building" to "female_general_muscle_building",
            "Powerlifting" to "female_powerlifting",
            "CrossFit" to "female_crossfit",
            "Full Body in 45 Minutes" to "female_full_body_in_45_minutes",
            "Postpartum Recovery" to "female_postpartum_recovery"
        )

        val femalePrograms = ProgramRegistry.getFemalePrograms()
        assertEquals(9, femalePrograms.size, "Expected exactly 9 female programs")

        expectedPrograms.forEach { (expectedName, expectedId) ->
            val found = femalePrograms.find { it.id == expectedId }
            assertNotNull(found, "Expected to find program with id $expectedId")
            assertEquals(expectedName, found.title, "Program title should match authoritative PDF")
            assertEquals(30, found.workoutsCount, "Program $expectedName should have 30 workouts")
            assertEquals(30, found.schedule.size, "Schedule for $expectedName should contain 30 items")
        }
    }

    @Test
    fun testTotalProgrammedDaysEquals270() {
        val allFemalePrograms = CanonicalFemaleProgramData.ALL_PROGRAMS
        assertEquals(9, allFemalePrograms.size, "Expected 9 canonical programs")

        var totalDays = 0
        allFemalePrograms.forEach { prog ->
            assertEquals(30, prog.days.size, "${prog.name} must have exactly 30 days")
            totalDays += prog.days.size

            // Ensure 1-based sequential days from 1 to 30
            prog.days.forEachIndexed { index, day ->
                assertEquals(index + 1, day.dayNumber, "${prog.name} day sequence must be 1..30")
            }
        }

        assertEquals(270, totalDays, "Total programmed days must equal 270 (9 programs × 30 days)")
    }

    @Test
    fun testWeightLossDayOneAndDayThirtyFidelity() {
        val prog = CanonicalFemaleProgramData.getById("female_weight_loss")
        assertNotNull(prog)

        val day1 = prog.days.find { it.dayNumber == 1 }
        assertNotNull(day1)
        assertTrue(day1.title.contains("Full Body + Cardio"))
        assertFalse(day1.isRestDay)
        assertTrue(day1.exercises.size >= 5)

        val day30 = prog.days.find { it.dayNumber == 30 }
        assertNotNull(day30)
        assertTrue(day30.title.contains("Lower Body"))
        assertFalse(day30.isRestDay)
    }

    @Test
    fun testPerfectButtDayOneAndDayThirtyFidelity() {
        val prog = CanonicalFemaleProgramData.getById("female_perfect_butt")
        assertNotNull(prog)

        val day1 = prog.days.find { it.dayNumber == 1 }
        assertNotNull(day1)
        assertTrue(day1.title.contains("Glute Strength"))

        val day30 = prog.days.find { it.dayNumber == 30 }
        assertNotNull(day30)
        assertTrue(day30.title.contains("Upper Body"))
    }

    @Test
    fun testPostpartumRecoveryFidelityAndProgression() {
        val prog = CanonicalFemaleProgramData.getById("female_postpartum_recovery")
        assertNotNull(prog)
        assertEquals("Postpartum Recovery", prog.name)

        val day1 = prog.days.find { it.dayNumber == 1 }
        assertNotNull(day1)
        assertTrue(day1.title.contains("Breathing + Core"))

        val day7 = prog.days.find { it.dayNumber == 7 }
        assertNotNull(day7)
        assertTrue(day7.isRestDay || day7.title.contains("Rest"))

        val day30 = prog.days.find { it.dayNumber == 30 }
        assertNotNull(day30)
        assertTrue(day30.title.contains("Lower Body"))
        assertTrue(day30.notes.contains("Consolidate technique") || day30.notes.isNotBlank())
    }

    @Test
    fun testCanonicalWorkoutResolverForFemaleWorkouts() {
        val workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("female_weight_loss", "day_1")
        assertNotNull(workout)
        assertEquals(1, workout.dayNumber)
        assertTrue(workout.workoutTitle.contains("Full Body"))
        assertTrue(workout.exercises.isNotEmpty(), "Workout should have exercises")

        val day30Workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("female_powerlifting", "30")
        assertNotNull(day30Workout)
        assertEquals(30, day30Workout.dayNumber)
    }

    @Test
    fun testGenderLogicPreservation() {
        val originalProfile = UserSession.currentUserProfile
        val originalGender = UserSession.currentUserGender
        try {
            UserSession.currentUserProfile = null
            UserSession.currentUserGender = "Female"
            assertTrue(UserSession.isFemale, "User with Female gender must be recognized as female")

            UserSession.currentUserGender = "Male"
            assertFalse(UserSession.isFemale, "User with Male gender must not be recognized as female")

            UserSession.currentUserGender = null
            assertFalse(UserSession.isFemale, "User with unspecified gender must default to non-female")
        } finally {
            UserSession.currentUserProfile = originalProfile
            UserSession.currentUserGender = originalGender
        }
    }

    @Test
    fun testProgramRegistryDoesNotCrossWirePrograms() {
        val perfectButt = ProgramRegistry.getProgramById("female_perfect_butt")
        assertNotNull(perfectButt)
        assertEquals("Perfect Butt", perfectButt.title)

        val sculptedBody = ProgramRegistry.getProgramById("female_sculpted_body")
        assertNotNull(sculptedBody)
        assertEquals("Sculpted Body", sculptedBody.title)

        val powerlifting = ProgramRegistry.getProgramById("female_powerlifting")
        assertNotNull(powerlifting)
        assertEquals("Powerlifting", powerlifting.title)
    }

    @Test
    fun testMaleCatalogProgramsRemainUntouched() {
        val male30Day = ProgramRegistry.getProgramById("30-day")
        assertNotNull(male30Day)

        val hypertrophy = ProgramRegistry.getProgramById("hypertrophy")
        assertNotNull(hypertrophy)
        assertEquals("30-Day Hypertrophy Program", hypertrophy.title)

        val strength = ProgramRegistry.getProgramById("strength")
        assertNotNull(strength)
        assertEquals("30-Day Strength Program", strength.title)

        val athlete = ProgramRegistry.getProgramById("athlete")
        assertNotNull(athlete)
        assertEquals("30-Day Athlete Performance Program", athlete.title)
    }
}
