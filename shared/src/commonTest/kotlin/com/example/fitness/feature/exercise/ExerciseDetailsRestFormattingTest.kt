package com.example.fitness.feature.exercise

import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.data.ThirtyDayExerciseLibrary
import com.example.fitness.feature.workout.data.WorkoutRepository
import com.example.fitness.feature.workout.model.CustomExerciseConfig
import com.example.fitness.feature.workout.model.ExperienceLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExerciseDetailsRestFormattingTest {

    @Test
    fun testRestMustBeSingleNumberAndNeverRangeOrConcatenated() {
        // Single duration strings
        assertEquals(30, CustomExerciseConfig.parseDefaultRest("30s"))
        assertEquals(45, CustomExerciseConfig.parseDefaultRest("45s"))
        assertEquals(60, CustomExerciseConfig.parseDefaultRest("60s"))
        assertEquals(90, CustomExerciseConfig.parseDefaultRest("90s"))
        assertEquals(120, CustomExerciseConfig.parseDefaultRest("120s"))

        // With words/spaces
        assertEquals(60, CustomExerciseConfig.parseDefaultRest("60s rest"))
        assertEquals(90, CustomExerciseConfig.parseDefaultRest("90 sec"))
        assertEquals(60, CustomExerciseConfig.parseDefaultRest("60"))

        // Prevent 6090s concatenation bug
        assertEquals(60, CustomExerciseConfig.parseDefaultRest("6090s"))
        assertEquals(60, CustomExerciseConfig.parseDefaultRest("6090"))

        // Prevent range strings from concatenating digits
        assertEquals(60, CustomExerciseConfig.parseDefaultRest("60-90 sec"))
        assertEquals(60, CustomExerciseConfig.parseDefaultRest("60–90s"))
        assertEquals(60, CustomExerciseConfig.parseDefaultRest("60 - 90s"))
        assertEquals(120, CustomExerciseConfig.parseDefaultRest("2-3 min"))
        assertEquals(120, CustomExerciseConfig.parseDefaultRest("2 min"))
    }

    @Test
    fun testThirtyDayExerciseLibraryRestValuesAreAllSingleDurations() {
        for (entry in ThirtyDayExerciseLibrary.ALL_ENTRIES) {
            val item = ThirtyDayExerciseLibrary.toExerciseItem(entry)
            val rest = item.restTime

            // Must end with 's' and be purely digits + 's'
            assertTrue(rest.endsWith("s"), "Rest time '$rest' for exercise '${entry.id}' must end with 's'")
            val digits = rest.removeSuffix("s")
            assertTrue(digits.all { it.isDigit() }, "Rest time '$rest' for exercise '${entry.id}' must be a single number")

            // Must never be 6090s or range
            assertFalse(rest.contains("6090"), "Rest time '$rest' for exercise '${entry.id}' must NOT contain 6090")
            assertFalse(rest.contains("-"), "Rest time '$rest' for exercise '${entry.id}' must NOT contain hyphen")
            assertFalse(rest.contains("–"), "Rest time '$rest' for exercise '${entry.id}' must NOT contain en-dash")
            assertFalse(rest.contains("sec"), "Rest time '$rest' for exercise '${entry.id}' must NOT contain 'sec'")

            // Ensure numeric rest interval is within realistic bounds (15s to 300s)
            val num = digits.toIntOrNull() ?: 0
            assertTrue(num in 15..300, "Rest duration $num for exercise '${entry.id}' must be between 15s and 300s")
        }
    }

    @Test
    fun testWorkoutRepositoryRestValuesAreAllSingleDurations() {
        val exercises = WorkoutRepository.fetchExercises("Chest", ExperienceLevel.NO_EXPERIENCE)
        assertTrue(exercises.isNotEmpty())

        for (ex in exercises) {
            val rest = ex.restTime
            assertTrue(rest.endsWith("s"), "Rest time '$rest' for exercise '${ex.id}' must end with 's'")
            val digits = rest.removeSuffix("s")
            assertTrue(digits.all { it.isDigit() }, "Rest time '$rest' for exercise '${ex.id}' must be a single number")
            assertFalse(rest.contains("6090"), "Rest time '$rest' must NOT contain 6090")
            assertFalse(rest.contains("-"), "Rest time '$rest' must NOT contain hyphen")
            assertFalse(rest.contains("–"), "Rest time '$rest' must NOT contain en-dash")
        }
    }

    @Test
    fun testCanonicalExercisesReflectTheirIndividualRestDurations() {
        // Machine Chest Press has defaultRestSeconds = 60
        val chestPress = SampleExerciseData.getById("machine_chest_press")
        assertEquals(60, chestPress?.defaultRestSeconds)

        // Barbell Bench Press has defaultRestSeconds = 90
        val benchPress = SampleExerciseData.getById("barbell_bench_press")
        assertEquals(90, benchPress?.defaultRestSeconds)

        // Super Admin or customized can be 45s or 120s
        assertEquals(45, CustomExerciseConfig.parseDefaultRest("45s"))
        assertEquals(120, CustomExerciseConfig.parseDefaultRest("120s"))
    }
}
