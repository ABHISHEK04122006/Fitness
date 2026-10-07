package com.example.fitness.feature.exercise.media

import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.program.data.CanonicalFemaleProgramData
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import fitness.shared.generated.resources.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * ATHGYM FEMALE EXERCISE MEDIA QUALITY TEST SUITE
 *
 * Verifies that:
 * 1. Every unique exercise used across all 9 female 30-day programs has a mapped, non-null female image.
 * 2. Female users receive dedicated female imagery.
 * 3. Male users strictly preserve existing male exercise imagery without cross-contamination.
 * 4. Multi-position instructional demonstration sets are structurally complete with non-blank titles and valid drawables.
 * 5. Canonical workout resolver correctly supplies female exercise images during female workout execution.
 */
class FemaleExerciseMediaQualityTest {

    @Test
    fun testAll70UniqueFemaleExercisesAreMappedToMedia() {
        val allFemalePrograms = CanonicalFemaleProgramData.ALL_PROGRAMS
        assertEquals(9, allFemalePrograms.size, "Must have exactly 9 female programs")

        val uniqueExerciseIds = allFemalePrograms.flatMap { program ->
            program.days.flatMap { day ->
                day.exercises.map { it.exerciseId }
            }
        }.toSet()

        assertTrue(uniqueExerciseIds.size >= 70, "Should have at least 70 unique exercise IDs across 9 programs")

        for (exerciseId in uniqueExerciseIds) {
            assertTrue(
                FemaleExerciseMediaRegistry.hasFemaleMedia(exerciseId),
                "Exercise '$exerciseId' must be registered in FemaleExerciseMediaRegistry"
            )
            val thumb = FemaleExerciseMediaRegistry.getThumbnailRes(exerciseId)
            assertNotNull(thumb, "Exercise '$exerciseId' must have a non-null thumbnail drawable")
        }
    }

    @Test
    fun testFemaleMediaResolutionForFemaleUser() {
        val gobletSquatThumb = FemaleExerciseMediaRegistry.resolveExerciseImage(
            exerciseId = "dumbbell_goblet_squat",
            isFemale = true,
            defaultFallback = Res.drawable.gym_bench_press
        )
        assertEquals(Res.drawable.female_goblet_squat, gobletSquatThumb)

        val hipThrustThumb = FemaleExerciseMediaRegistry.resolveExerciseImage(
            exerciseId = "hip_thrust",
            isFemale = true,
            defaultFallback = Res.drawable.gym_bench_press
        )
        assertEquals(Res.drawable.female_hip_thrust, hipThrustThumb)

        val deadliftThumb = FemaleExerciseMediaRegistry.resolveExerciseImage(
            exerciseId = "deadlift",
            isFemale = true,
            defaultFallback = Res.drawable.gym_bench_press
        )
        assertEquals(Res.drawable.female_deadlift, deadliftThumb)
    }

    @Test
    fun testMaleMediaPreservationForMaleUser() {
        val maleFallback = Res.drawable.gym_bench_press
        val resolvedForMale = FemaleExerciseMediaRegistry.resolveExerciseImage(
            exerciseId = "dumbbell_goblet_squat",
            isFemale = false,
            defaultFallback = maleFallback
        )
        assertEquals(maleFallback, resolvedForMale, "Male user must strictly receive the male fallback image")
    }

    @Test
    fun testNewlyGeneratedDedicatedFemaleImages() {
        assertEquals(
            Res.drawable.female_front_squat,
            FemaleExerciseMediaRegistry.resolveExerciseImage("front_squat", isFemale = true)
        )
        assertEquals(
            Res.drawable.female_air_squat,
            FemaleExerciseMediaRegistry.resolveExerciseImage("air_squat", isFemale = true)
        )
        assertEquals(
            Res.drawable.female_sumo_squat,
            FemaleExerciseMediaRegistry.resolveExerciseImage("sumo_squat", isFemale = true)
        )
        assertEquals(
            Res.drawable.female_leg_press,
            FemaleExerciseMediaRegistry.resolveExerciseImage("leg_press", isFemale = true)
        )
        assertEquals(
            Res.drawable.female_walking_lunge,
            FemaleExerciseMediaRegistry.resolveExerciseImage("walking_lunges", isFemale = true)
        )
        assertEquals(
            Res.drawable.female_reverse_lunge,
            FemaleExerciseMediaRegistry.resolveExerciseImage("reverse_lunge", isFemale = true)
        )
        assertEquals(
            Res.drawable.female_step_up,
            FemaleExerciseMediaRegistry.resolveExerciseImage("step_up", isFemale = true)
        )
        assertEquals(
            Res.drawable.female_single_leg_glute_bridge,
            FemaleExerciseMediaRegistry.resolveExerciseImage("single_leg_glute_bridge", isFemale = true)
        )
        assertEquals(
            Res.drawable.female_frog_pumps,
            FemaleExerciseMediaRegistry.resolveExerciseImage("frog_pumps", isFemale = true)
        )
        assertEquals(
            Res.drawable.female_cable_glute_kickback,
            FemaleExerciseMediaRegistry.resolveExerciseImage("cable_glute_kickback", isFemale = true)
        )
        assertEquals(
            Res.drawable.female_leg_extension,
            FemaleExerciseMediaRegistry.resolveExerciseImage("leg_extension", isFemale = true)
        )
        assertEquals(
            Res.drawable.female_lying_leg_curl,
            FemaleExerciseMediaRegistry.resolveExerciseImage("lying_leg_curl", isFemale = true)
        )
        assertEquals(
            Res.drawable.female_standing_calf_raise,
            FemaleExerciseMediaRegistry.resolveExerciseImage("standing_calf_raise", isFemale = true)
        )
    }

    @Test
    fun testExpandedInstructionalSets() {
        val legPressPositions = FemaleExerciseMediaRegistry.getInstructionalPositions("leg_press")
        assertTrue(legPressPositions.isNotEmpty(), "Leg Press must have instructional frames")
        assertEquals(Res.drawable.female_leg_press, legPressPositions.first().imageRes)

        val frontSquatPositions = FemaleExerciseMediaRegistry.getInstructionalPositions("front_squat")
        assertEquals(2, frontSquatPositions.size, "Front Squat must have start and end frames")
        assertEquals(Res.drawable.female_front_squat, frontSquatPositions.last().imageRes)

        val calfRaisePositions = FemaleExerciseMediaRegistry.getInstructionalPositions("standing_calf_raise")
        assertTrue(calfRaisePositions.isNotEmpty(), "Calf raise must have instructional frames")
        assertEquals(Res.drawable.female_standing_calf_raise, calfRaisePositions.first().imageRes)
    }

    @Test
    fun testInstructionalPositionsStructure() {
        val gobletSquatPositions = FemaleExerciseMediaRegistry.getInstructionalPositions("dumbbell_goblet_squat")
        assertTrue(gobletSquatPositions.isNotEmpty(), "Goblet Squat must have instructional movement positions")
        assertEquals(2, gobletSquatPositions.size, "Goblet Squat should have 2 positions (start and end)")

        val startFrame = gobletSquatPositions[0]
        assertEquals("start", startFrame.phaseId)
        assertTrue(startFrame.title.isNotBlank())
        assertEquals(Res.drawable.female_goblet_squat_start, startFrame.imageRes)

        val endFrame = gobletSquatPositions[1]
        assertEquals("end", endFrame.phaseId)
        assertTrue(endFrame.title.isNotBlank())
        assertEquals(Res.drawable.female_goblet_squat, endFrame.imageRes)
    }

    @Test
    fun testCanonicalWorkoutResolverSuppliesFemaleImages() {
        val originalProfile = UserSession.currentUserProfile
        val originalGender = UserSession.currentUserGender
        try {
            UserSession.currentUserProfile = null
            UserSession.currentUserGender = "Female"

            val workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("female_weight_loss", "1")
            assertNotNull(workout)
            assertTrue(workout.exercises.isNotEmpty(), "Workout should contain exercises")

            // First exercise is Goblet Squat
            val firstEx = workout.exercises.first()
            assertEquals("dumbbell_goblet_squat", firstEx.id)
            assertEquals(
                Res.drawable.female_goblet_squat,
                firstEx.imageRes,
                "Goblet Squat in female workout must resolve to female_goblet_squat"
            )
        } finally {
            UserSession.currentUserProfile = originalProfile
            UserSession.currentUserGender = originalGender
        }
    }

    @Test
    fun testMaleWorkoutRetainsMaleAesthetics() {
        val originalProfile = UserSession.currentUserProfile
        val originalGender = UserSession.currentUserGender
        try {
            UserSession.currentUserProfile = null
            UserSession.currentUserGender = "Male"

            val maleWorkout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("hypertrophy", "1")
            assertNotNull(maleWorkout)
            assertFalse(
                maleWorkout.imageRes == Res.drawable.gender_female,
                "Male workout hero image must not be gender_female"
            )
        } finally {
            UserSession.currentUserProfile = originalProfile
            UserSession.currentUserGender = originalGender
        }
    }
}
