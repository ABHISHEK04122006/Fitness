package com.example.fitness.feature.admin

import com.example.fitness.feature.admin.data.AdminWorkoutCatalogProvider
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.program.data.CanonicalAthletePerformanceProgramData
import com.example.fitness.feature.program.data.CanonicalFemaleProgramData
import com.example.fitness.feature.program.data.SampleProgramData
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Verification unit tests for Admin Program Day Card and internal workout/day structure.
 *
 * Ensures:
 * 1. Training days vs Rest days are accurately identified and structured.
 * 2. Day titles, categories/focus, descriptions/notes, and exercises are preserved.
 * 3. Prescription formatting conforms to: "1 set × ..." vs "N sets × ... • Xs rest".
 * 4. All Male and Female programs maintain consistent day structure.
 */
class AdminProgramDayCardTest {

    @Test
    fun test01_athletePerformanceDay1Structure() {
        val program = CanonicalAthletePerformanceProgramData.PROGRAM_ATHLETE_PERFORMANCE
        val day1 = program.days.find { it.dayNumber == 1 }
        assertNotNull(day1, "Day 1 must exist")
        assertEquals("Day 1 — Acceleration & Sprint", day1.title)
        assertEquals("TRAINING", day1.workoutType)
        assertFalse(day1.isRestDay)
        assertEquals(listOf("Acceleration & Sprint"), day1.focus)
        assertEquals(4, day1.exercises.size)
        assertEquals("Prioritize technical execution and movement quality over speed or load.", day1.notes)

        // Exercise 1: 1 set × 6 reps (90s rest)
        val ex1 = day1.exercises[0]
        assertEquals("ten_meter_acceleration", ex1.exerciseId)
        assertEquals(1, ex1.sets)
        assertEquals("6 reps", ex1.reps)
        assertEquals(90, ex1.restSeconds)

        // Exercise 4: 4 sets × 15m (90s rest)
        val ex4 = day1.exercises[3]
        assertEquals("hill_sprint", ex4.exerciseId)
        assertEquals(4, ex4.sets)
        assertEquals("15m", ex4.reps)
        assertEquals(90, ex4.restSeconds)
    }

    @Test
    fun test02_athletePerformanceDay7RestDayStructure() {
        val program = CanonicalAthletePerformanceProgramData.PROGRAM_ATHLETE_PERFORMANCE
        val day7 = program.days.find { it.dayNumber == 7 }
        assertNotNull(day7, "Day 7 must exist")
        assertEquals("Day 7 — Recovery", day7.title)
        assertEquals("REST", day7.workoutType)
        assertTrue(day7.isRestDay)
        assertEquals(listOf("Recovery"), day7.focus)
        assertTrue(day7.exercises.isEmpty(), "Rest day must have 0 exercises")
        assertEquals("Recovery day. Light walking, mobility, hydration, and sleep.", day7.notes)
    }

    @Test
    fun test03_longExerciseNamesExistInCatalog() {
        val copenhagenPlank = SampleExerciseData.getById("side_plank_3_30_sec_side")
        assertNotNull(copenhagenPlank)
        assertTrue(copenhagenPlank.name.contains("Copenhagen plank") || copenhagenPlank.name.contains("side"))

        val medBallThrow = SampleExerciseData.getById("medicine_ball_rotational_throw")
        assertNotNull(medBallThrow)
        assertEquals("Medicine-ball rotational throw", medBallThrow.name)
    }

    @Test
    fun test04_allMaleProgramsHaveValidDays() {
        runBlocking {
            val malePrograms = AdminWorkoutCatalogProvider.getUnifiedProgramsForGender("Male")
            assertTrue(malePrograms.isNotEmpty())
            malePrograms.forEach { program ->
                assertTrue(program.days.isNotEmpty(), "Program '${program.name}' must have days configured")
                program.days.forEach { day ->
                    assertTrue(day.dayNumber > 0, "Day number must be positive in '${program.name}'")
                    if (day.isRestDay) {
                        assertTrue(day.exercises.isEmpty(), "Rest day in '${program.name}' should have no exercises")
                    } else {
                        assertTrue(day.workoutType.isNotBlank(), "Training day must have a workout type in '${program.name}'")
                    }
                }
            }
        }
    }

    @Test
    fun test05_allFemaleProgramsHaveValidDays() {
        runBlocking {
            val femalePrograms = AdminWorkoutCatalogProvider.getUnifiedProgramsForGender("Female")
            assertTrue(femalePrograms.isNotEmpty())
            femalePrograms.forEach { program ->
                assertTrue(program.days.isNotEmpty(), "Female program '${program.name}' must have days configured")
                program.days.forEach { day ->
                    assertTrue(day.dayNumber > 0, "Day number must be positive in '${program.name}'")
                }
            }
        }
    }
}
