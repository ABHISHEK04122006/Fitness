package com.example.fitness.feature.exercise

import com.example.fitness.feature.exercise.data.FirebaseExerciseRepository
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseCategory
import com.example.fitness.feature.exercise.model.ExerciseDifficulty
import com.example.fitness.feature.exercise.model.ExerciseMedia
import com.example.fitness.feature.exercise.model.ExerciseMediaType
import com.example.fitness.feature.exercise.model.ExerciseReference
import com.example.fitness.feature.exercise.model.MovementPattern
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExerciseModelTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Test
    fun testExerciseDefaultValues() {
        val exercise = Exercise()

        assertEquals("", exercise.id)
        assertEquals("", exercise.name)
        assertEquals("", exercise.slug)
        assertEquals("", exercise.description)
        assertEquals("", exercise.primaryMuscle)
        assertTrue(exercise.secondaryMuscles.isEmpty())
        assertTrue(exercise.muscleGroups.isEmpty())
        assertTrue(exercise.equipment.isEmpty())
        assertEquals("", exercise.category)
        assertEquals("", exercise.movementPattern)
        assertEquals("", exercise.difficulty)
        assertTrue(exercise.instructions.isEmpty())
        assertTrue(exercise.setupSteps.isEmpty())
        assertTrue(exercise.executionSteps.isEmpty())
        assertEquals("", exercise.breathing)
        assertTrue(exercise.formTips.isEmpty())
        assertTrue(exercise.commonMistakes.isEmpty())
        assertEquals(3, exercise.defaultSets)
        assertEquals("", exercise.defaultReps)
        assertEquals(90, exercise.defaultRestSeconds)
        assertEquals(ExerciseMedia(), exercise.media)
        assertTrue(exercise.isActive)
        assertNull(exercise.createdAt)
        assertNull(exercise.updatedAt)
        assertEquals("", exercise.createdBy)
        assertEquals("", exercise.updatedBy)
        assertEquals(1, exercise.version)
    }

    @Test
    fun testExerciseMediaDefaultValues() {
        val media = ExerciseMedia()

        assertEquals("", media.thumbnailUrl)
        assertEquals("", media.animationUrl)
        assertEquals("", media.videoUrl)
        assertEquals("NONE", media.mediaType)
    }

    @Test
    fun testExerciseJsonSerializationRoundTrip() {
        val sample = SampleExerciseData.BARBELL_BENCH_PRESS.copy(
            createdAt = 1700000000000L,
            updatedAt = 1700000000000L,
            createdBy = "admin_uid_123",
            updatedBy = "admin_uid_123",
            media = ExerciseMedia(
                thumbnailUrl = "https://storage.fitness.app/exercises/bench_thumb.jpg",
                videoUrl = "https://storage.fitness.app/exercises/bench.mp4",
                mediaType = ExerciseMediaType.VIDEO.name
            )
        )

        val encoded = json.encodeToString(Exercise.serializer(), sample)
        assertTrue(encoded.contains("\"id\":\"barbell_bench_press\""))
        assertTrue(encoded.contains("\"category\":\"STRENGTH\""))
        assertTrue(encoded.contains("\"movementPattern\":\"HORIZONTAL_PUSH\""))

        val decoded = json.decodeFromString(Exercise.serializer(), encoded)
        assertEquals(sample.id, decoded.id)
        assertEquals(sample.name, decoded.name)
        assertEquals(sample.primaryMuscle, decoded.primaryMuscle)
        assertEquals(sample.secondaryMuscles, decoded.secondaryMuscles)
        assertEquals(sample.equipment, decoded.equipment)
        assertEquals(sample.defaultSets, decoded.defaultSets)
        assertEquals(sample.defaultRestSeconds, decoded.defaultRestSeconds)
        assertEquals(sample.media.videoUrl, decoded.media.videoUrl)
        assertEquals("VIDEO", decoded.media.mediaType)
        assertEquals(sample.version, decoded.version)
        assertEquals(sample.createdAt, decoded.createdAt)
        assertEquals(sample.createdBy, decoded.createdBy)
    }

    @Test
    fun testStableExerciseIdHandlingAndSlugGeneration() {
        val repo = FirebaseExerciseRepository()

        // 1. Normal clean name
        val slug1 = repo.generateSlug("Barbell Bench Press")
        assertEquals("barbell_bench_press", slug1)

        // 2. Name with special characters, symbols, and extra whitespace
        val slug2 = repo.generateSlug("  Incline Dumbbell Fly (30° Angle) — Advanced!  ")
        assertEquals("incline_dumbbell_fly_30_angle_advanced", slug2)

        // 3. Name with punctuation and hyphens
        val slug3 = repo.generateSlug("Push-Up: Close-Grip")
        assertEquals("pushup_closegrip", slug3)

        // 4. Verification that an existing ID remains stable
        val existingExercise = SampleExerciseData.BARBELL_BENCH_PRESS
        assertEquals("barbell_bench_press", existingExercise.id)

        // Even if display name changes, document ID stays stable
        val renamedExercise = existingExercise.copy(name = "Flat Barbell Bench Press (Updated)")
        assertEquals("barbell_bench_press", renamedExercise.id)
        assertEquals("Flat Barbell Bench Press (Updated)", renamedExercise.name)
    }

    @Test
    fun testExerciseReferenceStructure() {
        val ref = ExerciseReference(
            exerciseId = "barbell_bench_press",
            sets = 4,
            reps = "8-10",
            restSeconds = 120
        )

        assertEquals("barbell_bench_press", ref.exerciseId)
        assertEquals(4, ref.sets)
        assertEquals("8-10", ref.reps)
        assertEquals(120, ref.restSeconds)

        // Verify JSON serialization of reference
        val encoded = json.encodeToString(ExerciseReference.serializer(), ref)
        assertTrue(encoded.contains("\"exerciseId\":\"barbell_bench_press\""))
        assertTrue(encoded.contains("\"sets\":4"))
        assertTrue(encoded.contains("\"reps\":\"8-10\""))
        assertTrue(encoded.contains("\"restSeconds\":120"))

        val decoded = json.decodeFromString(ExerciseReference.serializer(), encoded)
        assertEquals(ref, decoded)
    }

    @Test
    fun testArchiveBehaviorSoftDelete() {
        val exercise = SampleExerciseData.SQUAT.copy(
            isActive = true,
            version = 1,
            createdAt = 1690000000000L,
            createdBy = "admin_1"
        )
        assertTrue(exercise.isActive)

        // Archiving sets isActive = false, updates timestamp and increments version
        val archivedTime = 1690005000000L
        val archived = exercise.copy(
            isActive = false,
            updatedAt = archivedTime,
            updatedBy = "admin_archiver",
            version = exercise.version + 1
        )

        assertFalse(archived.isActive, "Archived exercise must have isActive = false")
        assertEquals(2, archived.version, "Version must increment on archiving")
        assertEquals(exercise.createdAt, archived.createdAt, "createdAt must be preserved")
        assertEquals(exercise.createdBy, archived.createdBy, "createdBy must be preserved")
        assertEquals(archivedTime, archived.updatedAt)
        assertEquals("admin_archiver", archived.updatedBy)
        assertEquals(exercise.id, archived.id, "Document ID must remain stable")
    }

    @Test
    fun testVersionIncrementAndAuditPreservationOnUpdate() {
        val original = Exercise(
            id = "deadlift",
            name = "Conventional Deadlift",
            primaryMuscle = "Hamstrings",
            isActive = true,
            createdAt = 1000L,
            updatedAt = 1000L,
            createdBy = "creator_user",
            updatedBy = "creator_user",
            version = 1
        )

        // Simulate update
        val updated = original.copy(
            name = "Deadlift (Standard Stance)",
            secondaryMuscles = listOf("Glutes", "Erector Spinae"),
            updatedAt = 2500L,
            updatedBy = "updater_user",
            version = original.version + 1
        )

        assertEquals("deadlift", updated.id)
        assertEquals(1000L, updated.createdAt, "Original createdAt must be preserved")
        assertEquals("creator_user", updated.createdBy, "Original createdBy must be preserved")
        assertEquals(2500L, updated.updatedAt)
        assertEquals("updater_user", updated.updatedBy)
        assertEquals(2, updated.version, "Version must increment on update")
        assertTrue(updated.isActive)
    }

    @Test
    fun testEnumsCompletenessAndParsing() {
        // Difficulty - ATHGYM has exactly 4 canonical levels
        assertEquals(4, ExerciseDifficulty.entries.size)
        assertEquals(ExerciseDifficulty.NO_EXPERIENCE, ExerciseDifficulty.fromString("NO_EXPERIENCE"))
        assertEquals(ExerciseDifficulty.NO_EXPERIENCE, ExerciseDifficulty.fromString("No Experience"))
        assertEquals(ExerciseDifficulty.BEGINNER, ExerciseDifficulty.fromString("BEGINNER"))
        assertEquals(ExerciseDifficulty.BEGINNER, ExerciseDifficulty.fromString("intermediate"))
        assertEquals(ExerciseDifficulty.ADVANCED, ExerciseDifficulty.fromString("advanced"))
        assertEquals(ExerciseDifficulty.EXPERT, ExerciseDifficulty.fromString("EXPERT"))
        assertNull(ExerciseDifficulty.fromString("UNKNOWN"))

        // Category
        assertEquals(8, ExerciseCategory.entries.size)
        assertEquals(ExerciseCategory.STRENGTH, ExerciseCategory.fromString("strength"))
        assertEquals(ExerciseCategory.HYPERTROPHY, ExerciseCategory.fromString("hypertrophy"))
        assertEquals(ExerciseCategory.ATHLETE_PERFORMANCE, ExerciseCategory.fromString("ATHLETE_PERFORMANCE"))
        assertEquals(ExerciseCategory.CORE, ExerciseCategory.fromString("core"))
        assertEquals(ExerciseCategory.MOBILITY, ExerciseCategory.fromString("mobility"))
        assertEquals(ExerciseCategory.WARMUP, ExerciseCategory.fromString("warmup"))
        assertEquals(ExerciseCategory.COOLDOWN, ExerciseCategory.fromString("cooldown"))
        assertEquals(ExerciseCategory.CARDIO, ExerciseCategory.fromString("cardio"))

        // Movement Pattern
        assertEquals(14, MovementPattern.entries.size)
        val expectedPatterns = listOf(
            "HORIZONTAL_PUSH", "HORIZONTAL_PULL", "VERTICAL_PUSH", "VERTICAL_PULL",
            "SQUAT", "HINGE", "LUNGE", "CARRY", "ROTATION", "ANTI_ROTATION",
            "FLEXION", "EXTENSION", "ISOLATION", "LOCOMOTION"
        )
        expectedPatterns.forEach { pattern ->
            assertNotNull(MovementPattern.fromString(pattern), "MovementPattern $pattern should be valid")
        }

        // Media Type
        assertEquals(4, ExerciseMediaType.entries.size)
        assertEquals(ExerciseMediaType.NONE, ExerciseMediaType.fromString("none"))
        assertEquals(ExerciseMediaType.IMAGE, ExerciseMediaType.fromString("image"))
        assertEquals(ExerciseMediaType.ANIMATION, ExerciseMediaType.fromString("animation"))
        assertEquals(ExerciseMediaType.VIDEO, ExerciseMediaType.fromString("video"))
    }

    @Test
    fun testSampleExercisesMeetFoundationCriteria() {
        val samples = SampleExerciseData.INITIAL_EXERCISES
        assertEquals(3, samples.size)

        samples.forEach { ex ->
            assertTrue(ex.id.isNotBlank(), "Sample exercise ID must not be blank")
            assertTrue(ex.name.isNotBlank(), "Sample exercise name must not be blank")
            assertTrue(ex.primaryMuscle.isNotBlank(), "Primary muscle must not be blank")
            assertTrue(ex.category.isNotBlank(), "Category must not be blank")
            assertTrue(ex.movementPattern.isNotBlank(), "Movement pattern must not be blank")
            assertTrue(ex.difficulty.isNotBlank(), "Difficulty must not be blank")
            assertTrue(ex.isActive, "Sample exercise must default to active")
            assertEquals(1, ex.version)
            // Foundation constraint: no invented instructions
            assertTrue(ex.instructions.isEmpty(), "Instructions must be empty until authoritative source provided")
            assertTrue(ex.formTips.isEmpty(), "Form tips must be empty until authoritative source provided")
            assertTrue(ex.commonMistakes.isEmpty(), "Common mistakes must be empty until authoritative source provided")
        }
    }
}
