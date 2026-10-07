package com.example.fitness.feature.exercise

import com.example.fitness.feature.exercise.data.ExerciseRepository
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseCategory
import com.example.fitness.feature.exercise.model.ExerciseDifficulty
import com.example.fitness.feature.exercise.model.ExerciseMedia
import com.example.fitness.feature.exercise.model.MovementPattern
import com.example.fitness.feature.workout.data.WorkoutFilterHelper
import com.example.fitness.feature.workout.data.WorkoutRepository
import com.example.fitness.feature.workout.model.WorkoutType
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * End-to-end integration test verifying complete Admin Exercise Management Lifecycle:
 * 1. Add Exercise -> Appears in Admin and User UI.
 * 2. Archive Exercise -> Disappears from User UI (catalog, workout details, filters), but appears in Admin (Archived filter).
 * 3. Unarchive Exercise -> Immediately reappears in User UI.
 * 4. Delete Exercise -> Permanently removed from DB, Admin UI, and User UI.
 * 5. Workout Dependencies -> Accurately counts containing workouts and removes cleanly without crashes.
 */
class AdminExerciseLifecycleIntegrationTest {

    private class InMemoryLifecycleExerciseRepository : ExerciseRepository {
        val store = mutableMapOf<String, Exercise>()

        override suspend fun getExercise(exerciseId: String): Exercise? {
            if (SampleExerciseData.isDeleted(exerciseId)) return null
            val ex = store[exerciseId] ?: SampleExerciseData.getById(exerciseId)
            return if (ex != null && SampleExerciseData.isDeleted(ex.id)) null else ex
        }

        override suspend fun getActiveExercises(): List<Exercise> {
            val all = store.values.filter { it.isActive && !SampleExerciseData.isDeleted(it.id) && !SampleExerciseData.isArchived(it.id) }
            SampleExerciseData.registerCanonicalExercises(all)
            return all
        }

        override suspend fun getExercisesForAdmin(status: String): List<Exercise> {
            val all = store.values.filter { !SampleExerciseData.isDeleted(it.id) }
            return when (status.uppercase()) {
                "ACTIVE" -> all.filter { it.isActive && !SampleExerciseData.isArchived(it.id) }
                "ARCHIVED" -> all.filter { !it.isActive || SampleExerciseData.isArchived(it.id) }
                else -> all
            }
        }

        override suspend fun getAllExercisesForAdmin(): List<Exercise> {
            return getExercisesForAdmin("ALL")
        }

        override suspend fun createExercise(exercise: Exercise): Result<Unit> {
            store[exercise.id] = exercise
            SampleExerciseData.updateCanonicalExercise(exercise)
            return Result.success(Unit)
        }

        override suspend fun updateExercise(exercise: Exercise): Result<Unit> {
            store[exercise.id] = exercise
            SampleExerciseData.updateCanonicalExercise(exercise)
            return Result.success(Unit)
        }

        override suspend fun archiveExercise(exerciseId: String): Result<Unit> {
            val ex = store[exerciseId] ?: SampleExerciseData.getById(exerciseId)
                ?: return Result.failure(NoSuchElementException())
            val archived = ex.copy(isActive = false, version = ex.version + 1)
            store[exerciseId] = archived
            SampleExerciseData.archiveCanonicalExercise(exerciseId)
            return Result.success(Unit)
        }

        override suspend fun restoreExercise(exerciseId: String): Result<Unit> {
            val ex = store[exerciseId] ?: SampleExerciseData.getById(exerciseId)
                ?: return Result.failure(NoSuchElementException())
            val restored = ex.copy(isActive = true, version = ex.version + 1)
            store[exerciseId] = restored
            SampleExerciseData.restoreCanonicalExercise(exerciseId)
            return Result.success(Unit)
        }

        override suspend fun deleteExercise(exerciseId: String): Result<Unit> {
            store.remove(exerciseId)
            SampleExerciseData.deleteCanonicalExercise(exerciseId)
            return Result.success(Unit)
        }

        override suspend fun updateExerciseMedia(exerciseId: String, media: ExerciseMedia): Result<Unit> {
            val ex = store[exerciseId] ?: return Result.failure(NoSuchElementException())
            store[exerciseId] = ex.copy(media = media, version = ex.version + 1)
            return Result.success(Unit)
        }
    }

    private lateinit var repository: InMemoryLifecycleExerciseRepository

    @BeforeTest
    fun setUp() {
        SampleExerciseData.resetOverridesForTesting()
        repository = InMemoryLifecycleExerciseRepository()
    }

    @AfterTest
    fun tearDown() {
        SampleExerciseData.resetOverridesForTesting()
    }

    @Test
    fun test01_AddExercise_AppearsInAdminAndUserCatalog() = runBlocking {
        val newExercise = Exercise(
            id = "custom_incline_cable_press",
            name = "Custom Incline Cable Press",
            primaryMuscle = "Chest",
            muscleGroups = listOf("Chest"),
            equipment = listOf("Cable Machine"),
            category = ExerciseCategory.STRENGTH.name,
            difficulty = ExerciseDifficulty.ADVANCED.name,
            movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
            defaultSets = 4,
            defaultReps = "10-12",
            defaultRestSeconds = 60,
            isActive = true
        )

        // 1. Create exercise
        val createResult = repository.createExercise(newExercise)
        assertTrue(createResult.isSuccess)

        // 2. Verify appears in Admin queries
        val adminAll = repository.getExercisesForAdmin("ALL")
        assertTrue(adminAll.any { it.id == newExercise.id })
        val adminActive = repository.getExercisesForAdmin("ACTIVE")
        assertTrue(adminActive.any { it.id == newExercise.id })
        val adminArchived = repository.getExercisesForAdmin("ARCHIVED")
        assertFalse(adminArchived.any { it.id == newExercise.id })

        // 3. Verify appears in User catalog & enriched Body Part Workout (Chest)
        val chestWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest")
        assertNotNull(chestWorkout)
        assertTrue(
            chestWorkout.exercises.any { it.id == newExercise.id },
            "Newly created active chest exercise must appear in User Chest workout"
        )

        // 4. Verify appears in User Filter & Search helper
        val allUserExercises = WorkoutFilterHelper.getAllExercises(WorkoutType.GYM)
        assertTrue(allUserExercises.any { it.id == newExercise.id })
    }

    @Test
    fun test02_ArchiveExercise_HidesFromUserUI_AndShowsInAdminArchived() = runBlocking {
        val exerciseId = "custom_incline_cable_press"
        val newExercise = Exercise(
            id = exerciseId,
            name = "Custom Incline Cable Press",
            primaryMuscle = "Chest",
            muscleGroups = listOf("Chest"),
            equipment = listOf("Cable Machine"),
            category = ExerciseCategory.STRENGTH.name,
            difficulty = ExerciseDifficulty.ADVANCED.name,
            movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
            isActive = true
        )
        repository.createExercise(newExercise)

        // Archive exercise
        val archiveResult = repository.archiveExercise(exerciseId)
        assertTrue(archiveResult.isSuccess)

        // 1. Verify hidden from User Active Exercises
        val activeList = repository.getActiveExercises()
        assertFalse(activeList.any { it.id == exerciseId }, "Archived exercise must not be returned by getActiveExercises")

        // 2. Verify hidden from User Workout routines
        val chestWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest")
        assertNotNull(chestWorkout)
        assertFalse(
            chestWorkout.exercises.any { it.id == exerciseId },
            "Archived exercise must be hidden from User Chest workout"
        )

        // 3. Verify hidden from User Filter & Search
        val allUserExercises = WorkoutFilterHelper.getAllExercises(WorkoutType.GYM)
        assertFalse(allUserExercises.any { it.id == exerciseId }, "Archived exercise must be hidden from user filters")

        // 4. Verify present in Admin ARCHIVED and ALL filters, but not ACTIVE
        val adminArchived = repository.getExercisesForAdmin("ARCHIVED")
        assertTrue(adminArchived.any { it.id == exerciseId }, "Archived exercise must appear in Admin ARCHIVED filter")
        val adminActive = repository.getExercisesForAdmin("ACTIVE")
        assertFalse(adminActive.any { it.id == exerciseId }, "Archived exercise must NOT appear in Admin ACTIVE filter")
        val adminAll = repository.getExercisesForAdmin("ALL")
        assertTrue(adminAll.any { it.id == exerciseId }, "Archived exercise must appear in Admin ALL filter")
    }

    @Test
    fun test03_UnarchiveRestoreExercise_ReappearsInUserUI() = runBlocking {
        val exerciseId = "custom_incline_cable_press"
        val newExercise = Exercise(
            id = exerciseId,
            name = "Custom Incline Cable Press",
            primaryMuscle = "Chest",
            muscleGroups = listOf("Chest"),
            equipment = listOf("Cable Machine"),
            category = ExerciseCategory.STRENGTH.name,
            difficulty = ExerciseDifficulty.ADVANCED.name,
            movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
            isActive = true
        )
        repository.createExercise(newExercise)
        repository.archiveExercise(exerciseId)

        // Restore exercise
        val restoreResult = repository.restoreExercise(exerciseId)
        assertTrue(restoreResult.isSuccess)

        // 1. Verify immediately active again
        val activeList = repository.getActiveExercises()
        assertTrue(activeList.any { it.id == exerciseId }, "Restored exercise must reappear in getActiveExercises")

        // 2. Verify reappears in User workout
        val chestWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest")
        assertNotNull(chestWorkout)
        assertTrue(
            chestWorkout.exercises.any { it.id == exerciseId },
            "Restored exercise must reappear in User workout"
        )

        // 3. Verify reappears in User Filter & Search
        val allUserExercises = WorkoutFilterHelper.getAllExercises(WorkoutType.GYM)
        assertTrue(allUserExercises.any { it.id == exerciseId }, "Restored exercise must reappear in user filters")
    }

    @Test
    fun test04_DeleteExercise_PermanentlyRemovesFromDatabaseAdminAndUserUI() = runBlocking {
        val exerciseId = "custom_incline_cable_press"
        val newExercise = Exercise(
            id = exerciseId,
            name = "Custom Incline Cable Press",
            primaryMuscle = "Chest",
            muscleGroups = listOf("Chest"),
            equipment = listOf("Cable Machine"),
            category = ExerciseCategory.STRENGTH.name,
            difficulty = ExerciseDifficulty.ADVANCED.name,
            movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
            isActive = true
        )
        repository.createExercise(newExercise)

        // Permanently delete exercise
        val deleteResult = repository.deleteExercise(exerciseId)
        assertTrue(deleteResult.isSuccess)

        // 1. Verify removed from DB / Repository
        assertNull(repository.getExercise(exerciseId), "Deleted exercise must return null from getExercise")
        assertFalse(repository.getAllExercisesForAdmin().any { it.id == exerciseId }, "Deleted exercise must not appear in Admin list")
        assertFalse(repository.getActiveExercises().any { it.id == exerciseId }, "Deleted exercise must not appear in Active list")

        // 2. Verify removed from SampleExerciseData
        assertTrue(SampleExerciseData.isDeleted(exerciseId), "SampleExerciseData must record tombstone")
        assertNull(SampleExerciseData.getById(exerciseId), "SampleExerciseData.getById must return null")

        // 3. Verify removed from User workout routines
        val chestWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest")
        assertNotNull(chestWorkout)
        assertFalse(
            chestWorkout.exercises.any { it.id == exerciseId },
            "Deleted exercise must not appear in User workout"
        )

        // 4. Verify removed from User Filter & Search
        val allUserExercises = WorkoutFilterHelper.getAllExercises(WorkoutType.GYM)
        assertFalse(allUserExercises.any { it.id == exerciseId }, "Deleted exercise must not appear in user filters")
    }

    @Test
    fun test05_WorkoutDependencyCheck_DetectsUsageCountAndSafelyRemoves() = runBlocking {
        // Barbell bench press is a baseline exercise in Gym Chest workout
        val benchUsage = WorkoutRepository.countWorkoutsContainingExercise("barbell_bench_press")
        assertTrue(benchUsage >= 1, "Barbell bench press must be detected in at least 1 workout")

        // Non-existent exercise has 0 usage
        val nonExistentUsage = WorkoutRepository.countWorkoutsContainingExercise("non_existent_exercise_123")
        assertEquals(0, nonExistentUsage)

        // When a baseline exercise is deleted, enrichWorkout safely omits it without crashing
        val targetId = "barbell_bench_press"
        SampleExerciseData.deleteCanonicalExercise(targetId)

        val chestWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest")
        assertNotNull(chestWorkout)
        assertFalse(
            chestWorkout.exercises.any { it.id == targetId || it.id == "gym_chest_1" },
            "Deleted baseline exercise must be omitted from workout without crashing"
        )
        // Other exercises in the workout must remain intact
        assertTrue(chestWorkout.exercises.isNotEmpty(), "Workout must still contain other exercises")
    }
}
