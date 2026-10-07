package com.example.fitness.feature.goals

import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.program.data.Canonical30DayProgramData
import com.example.fitness.feature.program.data.CanonicalAthletePerformanceProgramData
import com.example.fitness.feature.program.data.CanonicalStrengthProgramData
import com.example.fitness.feature.programs.data.ProgramRegistry
import com.example.fitness.feature.workout.model.ExperienceLevel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProgramSelectionNavigationTest {

    @BeforeTest
    fun setUp() {
        UserSession.clear()
        UserSession.resetProgramProgress()
        UserSession.experienceLevel = ExperienceLevel.NO_EXPERIENCE
        UserSession.weeklyCommitment = "4 Days"
    }

    // 1. Default Experience Level is Beginner (NO_EXPERIENCE)
    @Test
    fun testDefaultExperienceLevelIsBeginner() {
        assertEquals(ExperienceLevel.NO_EXPERIENCE, UserSession.experienceLevel)
        val defaultLevelString = UserSession.experienceLevel.toUserFacingLevel()
        assertEquals("No Experience", defaultLevelString)
    }

    // 2. General Fitness resolution for all 4 experience levels
    @Test
    fun testGeneralFitnessResolvesCorrectPlansPerLevel() {
        // Beginner user-facing level (maps to NO_EXPERIENCE)
        val beginnerProg = ProgramRegistry.getProgramById("general", ExperienceLevel.NO_EXPERIENCE)
        assertNotNull(beginnerProg)
        assertEquals("30-day", beginnerProg.id)
        assertEquals(30, beginnerProg.workoutsCount)
        assertEquals(Canonical30DayProgramData.PROGRAM_NO_EXPERIENCE.name, beginnerProg.title)

        // Intermediate user-facing level (maps to BEGINNER)
        val intermediateProg = ProgramRegistry.getProgramById("30-day", ExperienceLevel.BEGINNER)
        assertNotNull(intermediateProg)
        assertEquals("30-day", intermediateProg.id)
        assertEquals(Canonical30DayProgramData.PROGRAM_BEGINNER.name, intermediateProg.title)

        // Advanced user-facing level (maps to ADVANCED)
        val advancedProg = ProgramRegistry.getProgramById("30-day", ExperienceLevel.ADVANCED)
        assertNotNull(advancedProg)
        assertEquals("30-day", advancedProg.id)
        assertEquals(Canonical30DayProgramData.PROGRAM_ADVANCED.name, advancedProg.title)

        // Expert level
        val expertProg = ProgramRegistry.getProgramById("general_fitness", ExperienceLevel.EXPERT)
        assertNotNull(expertProg)
        assertEquals("30-day", expertProg.id)
        assertEquals(Canonical30DayProgramData.PROGRAM_EXPERT.name, expertProg.title)

        // Availability must be true for all 4 levels in General Fitness
        assertTrue(ProgramRegistry.isProgramLevelAvailable("30-day", ExperienceLevel.NO_EXPERIENCE))
        assertTrue(ProgramRegistry.isProgramLevelAvailable("30-day", ExperienceLevel.BEGINNER))
        assertTrue(ProgramRegistry.isProgramLevelAvailable("general", ExperienceLevel.ADVANCED))
        assertTrue(ProgramRegistry.isProgramLevelAvailable("general_fitness", ExperienceLevel.EXPERT))
    }

    // 3. Athlete Training Program opens athlete plan and NOT General Fitness
    @Test
    fun testAthleteTrainingResolvesAthleteProgramAndNotGeneralFitness() {
        val athleteProg = ProgramRegistry.getProgramById("athlete", ExperienceLevel.BEGINNER)
        assertNotNull(athleteProg)
        assertEquals("athlete", athleteProg.id)
        assertEquals(30, athleteProg.workoutsCount)
        assertEquals(CanonicalAthletePerformanceProgramData.PROGRAM_ATHLETE_PERFORMANCE.name, athleteProg.title)

        // Must NOT match General Fitness
        val generalProg = ProgramRegistry.getProgramById("30-day", ExperienceLevel.BEGINNER)
        assertNotNull(generalProg)
        assertNotEquals(generalProg.title, athleteProg.title)
        assertNotEquals(generalProg.id, athleteProg.id)

        // Also test aliases: "athlete_training", "athlete_performance"
        val athleteAlias1 = ProgramRegistry.getProgramById("athlete_training")
        val athleteAlias2 = ProgramRegistry.getProgramById("athlete_performance")
        assertEquals(athleteProg.id, athleteAlias1?.id)
        assertEquals(athleteProg.id, athleteAlias2?.id)
    }

    // 4. Hypertrophy & Strength Program opens strength plan and NOT General Fitness
    @Test
    fun testStrengthResolvesStrengthProgramAndNotGeneralFitness() {
        val strengthProg = ProgramRegistry.getProgramById("strength", ExperienceLevel.BEGINNER)
        assertNotNull(strengthProg)
        assertEquals("strength", strengthProg.id)
        assertEquals(30, strengthProg.workoutsCount)
        assertEquals(CanonicalStrengthProgramData.PROGRAM_STRENGTH.name, strengthProg.title)

        // Must NOT match General Fitness or Athlete
        val generalProg = ProgramRegistry.getProgramById("30-day", ExperienceLevel.BEGINNER)
        val athleteProg = ProgramRegistry.getProgramById("athlete", ExperienceLevel.BEGINNER)
        assertNotNull(generalProg)
        assertNotNull(athleteProg)
        assertNotEquals(generalProg.title, strengthProg.title)
        assertNotEquals(athleteProg.title, strengthProg.title)

        // Also test alias: "hypertrophy_strength"
        val strengthAlias = ProgramRegistry.getProgramById("hypertrophy_strength")
        assertEquals("strength", strengthAlias?.id)
    }

    // 5. No Silent Fallback for Unsupported Levels (Athlete Training + Expert)
    @Test
    fun testAthleteTrainingExpertLevelIsNotAvailable() {
        assertTrue(ProgramRegistry.isProgramLevelAvailable("athlete", ExperienceLevel.BEGINNER))
        assertTrue(ProgramRegistry.isProgramLevelAvailable("athlete", ExperienceLevel.ADVANCED))
        assertFalse(
            ProgramRegistry.isProgramLevelAvailable("athlete", ExperienceLevel.EXPERT),
            "Athlete Training must not silently fall back when Expert level is selected"
        )
    }

    // 6. Program Isolation: Schedules and workouts differ between programs
    @Test
    fun testProgramSchedulesAreCompletelyIsolated() {
        val generalProg = ProgramRegistry.getProgramById("30-day", ExperienceLevel.BEGINNER)!!
        val athleteProg = ProgramRegistry.getProgramById("athlete", ExperienceLevel.BEGINNER)!!
        val strengthProg = ProgramRegistry.getProgramById("strength", ExperienceLevel.BEGINNER)!!

        // Day 1 schedule comparison
        val generalDay1 = generalProg.schedule.first { it.dayNumber == 1 }
        val athleteDay1 = athleteProg.schedule.first { it.dayNumber == 1 }
        val strengthDay1 = strengthProg.schedule.first { it.dayNumber == 1 }

        assertNotEquals(generalDay1.workoutId, athleteDay1.workoutId)
        assertNotEquals(athleteDay1.workoutId, strengthDay1.workoutId)
        assertNotEquals(generalDay1.workoutId, strengthDay1.workoutId)
    }

    // 7. UserSession Persistence across Program Selections
    @Test
    fun testUserSessionStateRetention() {
        // User selects Athlete Training, 5 Days commitment, Intermediate level
        UserSession.setActiveProgram("athlete")
        UserSession.weeklyCommitment = "5 Days"
        UserSession.experienceLevel = ExperienceLevel.ADVANCED

        assertEquals("athlete", UserSession.activeProgramId)
        assertEquals("5 Days", UserSession.weeklyCommitment)
        assertEquals(ExperienceLevel.ADVANCED, UserSession.experienceLevel)

        // Mark Athlete Day 1 completed
        UserSession.markProgramDayCompleted("athlete", 1)
        assertTrue(UserSession.isProgramDayCompleted("athlete", 1))
        // Verify General Fitness Day 1 remains incomplete (program isolation)
        assertFalse(UserSession.isProgramDayCompleted("30-day", 1))
    }

    // 8. Navigation typed Screen.ProgramDetail carries programId and level
    @Test
    fun testScreenProgramDetailDataClass() {
        val navScreen = Screen.ProgramDetail(programId = "athlete", level = ExperienceLevel.ADVANCED)
        assertEquals("athlete", navScreen.programId)
        assertEquals(ExperienceLevel.ADVANCED, navScreen.level)

        val navScreenDefaultLevel = Screen.ProgramDetail(programId = "strength")
        assertEquals("strength", navScreenDefaultLevel.programId)
        assertEquals(null, navScreenDefaultLevel.level)
    }

    // 9. Program normalization logic
    @Test
    fun testProgramNormalization() {
        assertEquals("30-day", UserSession.normalizeProgramId("30-day"))
        assertEquals("30-day", UserSession.normalizeProgramId("general"))
        assertEquals("30-day", UserSession.normalizeProgramId("general_fitness"))
        assertEquals("athlete", UserSession.normalizeProgramId("athlete"))
        assertEquals("athlete", UserSession.normalizeProgramId("athlete_training"))
        assertEquals("athlete", UserSession.normalizeProgramId("athlete_performance"))
        assertEquals("strength", UserSession.normalizeProgramId("strength"))
        assertEquals("strength", UserSession.normalizeProgramId("hypertrophy_strength"))
    }
}
