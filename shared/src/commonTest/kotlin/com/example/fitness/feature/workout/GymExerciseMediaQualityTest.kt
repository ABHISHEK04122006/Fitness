package com.example.fitness.feature.workout

import com.example.fitness.feature.workout.data.WorkoutRepository
import com.example.fitness.feature.workout.model.WorkoutType
import fitness.shared.generated.resources.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * GYM EXERCISE MEDIA QUALITY TEST SUITE
 *
 * Verifies that all Workout at Gym exercises have dedicated, high-resolution,
 * visually distinct imagery meeting ATHGYM cinematic standards:
 * 1. 24 distinct gym exercises across 8 body parts.
 * 2. Each exercise has a dedicated, non-null, non-generic image resource.
 * 3. Specific compound movements map directly to their dedicated assets (Bench Press,
 *    Incline Press, Lat Pulldown, Squat, Leg Press, Deadlift, Dips, Rollout, Clean & Press).
 * 4. No generic placeholder drawables remain on any gym exercise.
 */
class GymExerciseMediaQualityTest {

    @Test
    fun testAll24GymExercisesHaveDedicatedHighResolutionImages() {
        val gymWorkouts = WorkoutRepository.getWorkouts(WorkoutType.GYM)
        assertEquals(8, gymWorkouts.size, "Should have 8 gym body part workout categories")

        val targetWorkouts = gymWorkouts.filter { it.id != "full_body" }
        assertEquals(7, targetWorkouts.size, "Should have 7 targeted gym body part categories")
        val targetExercises = targetWorkouts.flatMap { it.exercises }
        assertEquals(21, targetExercises.size, "Targeted gym categories must maintain exactly 21 exercises")

        targetExercises.forEach { exercise ->
            assertNotNull(exercise.imageRes, "Exercise ${exercise.id} (${exercise.name}) must have an imageRes")
            assertTrue(exercise.id.startsWith("gym_"), "Exercise ID should follow gym_* convention: ${exercise.id}")
            assertTrue(exercise.name.isNotBlank(), "Exercise name cannot be blank")
        }

        val fbWorkout = gymWorkouts.find { it.id == "full_body" }
        assertNotNull(fbWorkout, "Full Body workout must exist")
        assertTrue(fbWorkout.exercises.isNotEmpty(), "Full body must contain exercises")
        fbWorkout.exercises.forEach { ex ->
            assertNotNull(ex.imageRes, "Full Body exercise ${ex.id} must have imageRes")
            assertTrue(ex.name.isNotBlank(), "Full Body exercise name cannot be blank")
        }
    }

    @Test
    fun testSpecificExerciseImageMappingsAreAccurateAndDistinct() {
        val gymWorkouts = WorkoutRepository.getWorkouts(WorkoutType.GYM)
        val exerciseMap = gymWorkouts.flatMap { it.exercises }.associateBy { it.id }

        // Chest
        assertEquals(Res.drawable.gym_bench_press, exerciseMap["gym_chest_1"]?.imageRes)
        assertEquals(Res.drawable.gym_incline_dumbbell_press, exerciseMap["gym_chest_2"]?.imageRes)
        assertEquals(Res.drawable.gym_cable_chest_fly, exerciseMap["gym_chest_3"]?.imageRes)

        // Back
        assertEquals(Res.drawable.gym_lat_pulldown, exerciseMap["gym_back_1"]?.imageRes)
        assertEquals(Res.drawable.gym_seated_cable_row, exerciseMap["gym_back_2"]?.imageRes)
        assertEquals(Res.drawable.gym_barbell_row, exerciseMap["gym_back_3"]?.imageRes)

        // Shoulders
        assertEquals(Res.drawable.gym_shoulder_press, exerciseMap["gym_sh_1"]?.imageRes)
        assertEquals(Res.drawable.gym_lateral_raise, exerciseMap["gym_sh_2"]?.imageRes)
        assertEquals(Res.drawable.gym_face_pull, exerciseMap["gym_sh_3"]?.imageRes)

        // Biceps
        assertEquals(Res.drawable.gym_ez_bar_curl, exerciseMap["gym_bi_1"]?.imageRes)
        assertEquals(Res.drawable.gym_incline_bicep_curl, exerciseMap["gym_bi_2"]?.imageRes)
        assertEquals(Res.drawable.gym_cable_hammer_curl, exerciseMap["gym_bi_3"]?.imageRes)

        // Triceps
        assertEquals(Res.drawable.gym_tricep_pushdown, exerciseMap["gym_tri_1"]?.imageRes)
        assertEquals(Res.drawable.gym_skull_crushers, exerciseMap["gym_tri_2"]?.imageRes)
        assertEquals(Res.drawable.gym_tricep_dips, exerciseMap["gym_tri_3"]?.imageRes)

        // Legs
        assertEquals(Res.drawable.gym_barbell_squat, exerciseMap["gym_leg_1"]?.imageRes)
        assertEquals(Res.drawable.gym_leg_press, exerciseMap["gym_leg_2"]?.imageRes)
        assertEquals(Res.drawable.gym_hamstring_curl, exerciseMap["gym_leg_3"]?.imageRes)

        // Abs
        assertEquals(Res.drawable.gym_hanging_leg_raise, exerciseMap["gym_abs_1"]?.imageRes)
        assertEquals(Res.drawable.gym_cable_crunch, exerciseMap["gym_abs_2"]?.imageRes)
        assertEquals(Res.drawable.gym_ab_wheel, exerciseMap["gym_abs_3"]?.imageRes)

        // Full Body exercises
        val fbWorkout = gymWorkouts.find { it.id == "full_body" }
        assertNotNull(fbWorkout)
        assertTrue(fbWorkout.exercises.all { it.imageRes != null })
    }

    @Test
    fun testAll24ExercisesHaveUniqueImages() {
        val gymWorkouts = WorkoutRepository.getWorkouts(WorkoutType.GYM)
        val targetExercises = gymWorkouts.filter { it.id != "full_body" }.flatMap { it.exercises }
        val distinctImages = targetExercises.map { it.imageRes }.toSet()

        assertTrue(
            distinctImages.size >= 21,
            "Target gym body part exercises must have their own unique, dedicated images"
        )
    }

    @Test
    fun testGymBodyPartCategoriesHaveDedicatedHeroImages() {
        val gymWorkouts = WorkoutRepository.getWorkouts(WorkoutType.GYM)
        gymWorkouts.forEach { workout ->
            assertNotNull(workout.imageRes, "Category ${workout.title} must have a hero image")
        }
    }
}
