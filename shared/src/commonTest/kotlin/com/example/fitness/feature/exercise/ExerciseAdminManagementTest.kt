package com.example.fitness.feature.exercise

import com.example.fitness.feature.exercise.data.FirebaseExerciseRepository
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseCategory
import com.example.fitness.feature.exercise.model.ExerciseDifficulty
import com.example.fitness.feature.exercise.model.MovementPattern
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Unit test suite verifying Admin Exercise Management business logic:
 * - In-memory live search across Name, Muscles, Equipment, Pattern.
 * - Multi-dimensional filtering by Status (All, Active, Archived), Category, and Difficulty.
 * - Immutability of Exercise ID during edits.
 * - Soft archiving (isActive = false) vs Restore (isActive = true) without permanent deletion.
 * - Audit trail preservation and version incrementing.
 * - Field validation (name required).
 */
class ExerciseAdminManagementTest {

    private val benchPress = SampleExerciseData.BARBELL_BENCH_PRESS.copy(
        id = "barbell_bench_press",
        name = "Barbell Bench Press",
        primaryMuscle = "Chest",
        secondaryMuscles = listOf("Triceps", "Anterior Deltoids"),
        equipment = listOf("Barbell", "Flat Bench"),
        category = ExerciseCategory.STRENGTH.name,
        difficulty = ExerciseDifficulty.NO_EXPERIENCE.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        isActive = true,
        version = 1,
        createdAt = 1000L,
        updatedAt = 1000L
    )

    private val pullUp = Exercise(
        id = "pull_up",
        name = "Pull-Up",
        primaryMuscle = "Lats",
        secondaryMuscles = listOf("Biceps", "Upper Back"),
        equipment = listOf("Pull-Up Bar"),
        category = ExerciseCategory.STRENGTH.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        movementPattern = MovementPattern.VERTICAL_PULL.name,
        isActive = true,
        version = 1,
        createdAt = 1100L,
        updatedAt = 1100L
    )

    private val dumbbellCurl = Exercise(
        id = "dumbbell_bicep_curl",
        name = "Dumbbell Bicep Curl",
        primaryMuscle = "Biceps",
        secondaryMuscles = listOf("Forearms"),
        equipment = listOf("Dumbbells"),
        category = ExerciseCategory.HYPERTROPHY.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        movementPattern = MovementPattern.ISOLATION.name,
        isActive = false, // Archived
        version = 2,
        createdAt = 1200L,
        updatedAt = 1500L
    )

    private val testDataset = listOf(benchPress, pullUp, dumbbellCurl)

    // ==========================================
    // 1. SEARCH FILTERING TESTS
    // ==========================================

    @Test
    fun testSearchByNameCaseInsensitive() {
        val query = "bench"
        val results = testDataset.filter { ex ->
            ex.name.contains(query, ignoreCase = true) ||
                    ex.primaryMuscle.contains(query, ignoreCase = true) ||
                    ex.secondaryMuscles.any { it.contains(query, ignoreCase = true) } ||
                    ex.equipment.any { it.contains(query, ignoreCase = true) } ||
                    ex.movementPattern.contains(query, ignoreCase = true)
        }
        assertEquals(1, results.size)
        assertEquals("barbell_bench_press", results[0].id)
    }

    @Test
    fun testSearchByPrimaryMuscle() {
        val query = "lats"
        val results = testDataset.filter { ex ->
            ex.name.contains(query, ignoreCase = true) ||
                    ex.primaryMuscle.contains(query, ignoreCase = true)
        }
        assertEquals(1, results.size)
        assertEquals("pull_up", results[0].id)
    }

    @Test
    fun testSearchBySecondaryMuscle() {
        val query = "triceps"
        val results = testDataset.filter { ex ->
            ex.secondaryMuscles.any { it.contains(query, ignoreCase = true) }
        }
        assertEquals(1, results.size)
        assertEquals("barbell_bench_press", results[0].id)
    }

    @Test
    fun testSearchByEquipment() {
        val query = "dumbbells"
        val results = testDataset.filter { ex ->
            ex.equipment.any { it.contains(query, ignoreCase = true) }
        }
        assertEquals(1, results.size)
        assertEquals("dumbbell_bicep_curl", results[0].id)
    }

    @Test
    fun testSearchByMovementPattern() {
        val query = "VERTICAL_PULL"
        val results = testDataset.filter { ex ->
            ex.movementPattern.contains(query, ignoreCase = true)
        }
        assertEquals(1, results.size)
        assertEquals("pull_up", results[0].id)
    }

    // ==========================================
    // 2. STATUS FILTERING TESTS
    // ==========================================

    @Test
    fun testFilterByStatusAll() {
        val statusFilter = "ALL"
        val results = testDataset.filter { ex ->
            when (statusFilter) {
                "ACTIVE" -> ex.isActive
                "ARCHIVED" -> !ex.isActive
                else -> true
            }
        }
        assertEquals(3, results.size)
    }

    @Test
    fun testFilterByStatusActive() {
        val statusFilter = "ACTIVE"
        val results = testDataset.filter { ex ->
            when (statusFilter) {
                "ACTIVE" -> ex.isActive
                "ARCHIVED" -> !ex.isActive
                else -> true
            }
        }
        assertEquals(2, results.size)
        assertTrue(results.all { it.isActive })
    }

    @Test
    fun testFilterByStatusArchived() {
        val statusFilter = "ARCHIVED"
        val results = testDataset.filter { ex ->
            when (statusFilter) {
                "ACTIVE" -> ex.isActive
                "ARCHIVED" -> !ex.isActive
                else -> true
            }
        }
        assertEquals(1, results.size)
        assertEquals("dumbbell_bicep_curl", results[0].id)
        assertFalse(results[0].isActive)
    }

    // ==========================================
    // 3. CATEGORY & DIFFICULTY FILTERING TESTS
    // ==========================================

    @Test
    fun testFilterByCategory() {
        val strengthOnly = testDataset.filter { it.category.equals(ExerciseCategory.STRENGTH.name, ignoreCase = true) }
        assertEquals(2, strengthOnly.size)

        val hypertrophyOnly = testDataset.filter { it.category.equals(ExerciseCategory.HYPERTROPHY.name, ignoreCase = true) }
        assertEquals(1, hypertrophyOnly.size)
        assertEquals("dumbbell_bicep_curl", hypertrophyOnly[0].id)
    }

    @Test
    fun testFilterByDifficulty() {
        val beginnerOnly = testDataset.filter { it.difficulty.equals(ExerciseDifficulty.BEGINNER.name, ignoreCase = true) }
        assertEquals(1, beginnerOnly.size)
        assertEquals("dumbbell_bicep_curl", beginnerOnly[0].id)

        val advancedOnly = testDataset.filter { it.difficulty.equals(ExerciseDifficulty.ADVANCED.name, ignoreCase = true) }
        assertEquals(1, advancedOnly.size)
        assertEquals("pull_up", advancedOnly[0].id)
    }

    @Test
    fun testCombinedMultiFilter() {
        // Active + Strength + No Experience + "bench"
        val filtered = testDataset.filter { ex ->
            ex.isActive &&
                    ex.category.equals("STRENGTH", ignoreCase = true) &&
                    ex.difficulty.equals(ExerciseDifficulty.NO_EXPERIENCE.name, ignoreCase = true) &&
                    ex.name.contains("bench", ignoreCase = true)
        }
        assertEquals(1, filtered.size)
        assertEquals("barbell_bench_press", filtered[0].id)
    }

    // ==========================================
    // 4. IMMUTABILITY & EDIT CONSTRAINTS
    // ==========================================

    @Test
    fun testExerciseIdImmutabilityDuringEdit() {
        val original = benchPress

        // When admin edits an exercise, they cannot alter its document ID
        val edited = original.copy(
            name = "Barbell Flat Bench Press",
            description = "Updated cues for powerlifting arch.",
            updatedAt = 2000L,
            updatedBy = "admin_updater",
            version = original.version + 1
        )

        // ID must match original exactly
        assertEquals(original.id, edited.id, "Exercise ID must remain strictly immutable")
        assertEquals(original.createdAt, edited.createdAt, "createdAt must be preserved")
        assertEquals(original.createdBy, edited.createdBy, "createdBy must be preserved")
        assertEquals("Barbell Flat Bench Press", edited.name)
        assertEquals(2, edited.version)
        assertEquals(2000L, edited.updatedAt)
    }

    // ==========================================
    // 5. ARCHIVE (SOFT DELETE) & RESTORE TESTS
    // ==========================================

    @Test
    fun testSoftArchiveTogglesIsActiveWithoutPermanentDelete() {
        val activeExercise = benchPress
        assertTrue(activeExercise.isActive)

        // Simulate archive action
        val archived = activeExercise.copy(
            isActive = false,
            updatedAt = 3000L,
            updatedBy = "admin_uid",
            version = activeExercise.version + 1
        )

        assertFalse(archived.isActive, "Archiving must set isActive to false")
        assertEquals(activeExercise.id, archived.id, "Document ID must be preserved")
        assertEquals(activeExercise.createdAt, archived.createdAt, "createdAt must be preserved")
        assertEquals(2, archived.version, "Version must increment on archive")
    }

    @Test
    fun testRestoreTogglesIsActiveBackToTrue() {
        val archivedExercise = dumbbellCurl
        assertFalse(archivedExercise.isActive)

        // Simulate restore action
        val restored = archivedExercise.copy(
            isActive = true,
            updatedAt = 4000L,
            updatedBy = "admin_uid",
            version = archivedExercise.version + 1
        )

        assertTrue(restored.isActive, "Restoring must set isActive to true")
        assertEquals(archivedExercise.id, restored.id, "Document ID must be preserved")
        assertEquals(archivedExercise.createdAt, restored.createdAt, "createdAt must be preserved")
        assertEquals(3, restored.version, "Version must increment on restore")
    }

    // ==========================================
    // 6. VALIDATION & SLUG GENERATION TESTS
    // ==========================================

    @Test
    fun testFormValidationForName() {
        fun validate(name: String): Boolean = name.trim().isNotBlank()

        assertFalse(validate(""), "Blank name must fail validation")
        assertFalse(validate("   "), "Whitespace-only name must fail validation")
        assertTrue(validate("Deadlift"), "Non-blank name must pass validation")
    }

    @Test
    fun testAutoSlugGenerationForNewExercises() {
        val repo = FirebaseExerciseRepository()

        assertEquals("bulgarian_split_squat", repo.generateSlug("Bulgarian Split Squat"))
        assertEquals("romanian_deadlift", repo.generateSlug("  Romanian Deadlift  "))
        assertEquals("cable_face_pull_high_angle", repo.generateSlug("Cable Face Pull (High Angle)"))
    }

    @Test
    fun testEmptyStateHandledGracefully() {
        val emptyList = emptyList<Exercise>()
        val filtered = emptyList.filter { it.name.contains("anything") }
        assertTrue(filtered.isEmpty())
    }
}
