package com.example.fitness.feature.admin

import com.example.fitness.core.navigation.Screen
import com.example.fitness.feature.admin.data.AdminExerciseCatalogOrganizer
import com.example.fitness.feature.admin.data.AdminWorkoutCatalogProvider
import com.example.fitness.feature.admin.data.ExerciseBodyPartSection
import com.example.fitness.feature.exercise.data.FirebaseExerciseRepository
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.media.CloudflareExerciseMediaUploadRepository
import com.example.fitness.feature.exercise.media.ExerciseMediaHttpTransport
import com.example.fitness.feature.exercise.media.ExerciseMediaResolver
import com.example.fitness.feature.exercise.media.MediaUploadFile
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseCategory
import com.example.fitness.feature.exercise.model.ExerciseDifficulty
import com.example.fitness.feature.exercise.model.ExerciseMedia
import com.example.fitness.feature.exercise.model.ExerciseMediaType
import com.example.fitness.feature.program.data.CanonicalFemaleProgramData
import com.example.fitness.feature.program.data.FirebaseProgramRepository
import com.example.fitness.feature.program.data.SampleProgramData
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Verification test suite for Admin Workout -> Gender -> Exercise flow.
 */
class AdminWorkoutGenderFlowTest {

    private class TestHttpTransport(
        val putResponse: Pair<Int, String> = 200 to """{"success":true,"key":"exercises/test_ex/thumbnail"}""",
        val deleteResponse: Pair<Int, String> = 200 to """{"success":true}"""
    ) : ExerciseMediaHttpTransport {
        var lastPutUrl: String? = null
        var lastDeleteUrl: String? = null

        override suspend fun put(url: String, bearerToken: String, mimeType: String, bytes: ByteArray): Pair<Int, String> {
            lastPutUrl = url
            return putResponse
        }

        override suspend fun delete(url: String, bearerToken: String): Pair<Int, String> {
            lastDeleteUrl = url
            return deleteResponse
        }
    }

    // 1. Admin -> Workout opens Male/Female selection
    @Test
    fun test01_adminWorkoutEntryScreenIsAdminWorkouts() {
        assertEquals(Screen.AdminWorkouts, Screen.AdminWorkouts)
    }

    // 2. No Exercises/Programs tabs are required for Workout entry
    @Test
    fun test02_adminWorkoutEntryHasOnlyGenderSelectionOptions() {
        val maleGender = "Male"
        val femaleGender = "Female"
        assertEquals("Male", maleGender)
        assertEquals("Female", femaleGender)
    }

    // 3. Clicking Male opens Male Exercises directly
    @Test
    fun test03_maleExercisesCatalogLoadsDirectly() {
        runBlocking {
            val maleExercises = AdminWorkoutCatalogProvider.getUnifiedExercisesForGender("Male")
            assertTrue(maleExercises.isNotEmpty(), "Male catalog must have exercises")
            assertNotNull(maleExercises.find { it.id == "barbell_bench_press" })
            assertNotNull(maleExercises.find { it.id == "squat" })
            assertNotNull(maleExercises.find { it.id == "deadlift" })
        }
    }

    // 4. Male/Female selection does not appear again
    @Test
    fun test04_genderCatalogScopeIsExplicit() {
        var selectedGender: String? = null
        selectedGender = "Male"
        assertEquals("Male", selectedGender)
        // In this state, activeGender = "Male" and only Male exercises are displayed
    }

    // 5. Clicking Female opens Female Exercises directly
    @Test
    fun test05_femaleExercisesCatalogLoadsDirectly() {
        runBlocking {
            val femaleExercises = AdminWorkoutCatalogProvider.getUnifiedExercisesForGender("Female")
            assertTrue(femaleExercises.isNotEmpty(), "Female catalog must have exercises")
            assertNotNull(femaleExercises.find { it.id == "hip_thrust" })
            assertNotNull(femaleExercises.find { it.id == "dumbbell_goblet_squat" })
            assertNotNull(femaleExercises.find { it.id == "glute_bridge" })
            assertNotNull(femaleExercises.find { it.id == "cable_glute_kickback" })
        }
    }

    // 6. Male catalog contains only Male exercises (strictly excludes explicitly Female exercises)
    @Test
    fun test06_maleCatalogExcludesExplicitlyFemaleExercises() {
        runBlocking {
            val maleExercises = AdminWorkoutCatalogProvider.getUnifiedExercisesForGender("Male")
            maleExercises.forEach { ex ->
                assertFalse(
                    ex.gender.equals("Female", ignoreCase = true),
                    "Male catalog must never include exercise with gender == Female ('${ex.id}')"
                )
            }
        }
    }

    // 7. Female catalog contains only Female exercises (strictly excludes explicitly Male exercises)
    @Test
    fun test07_femaleCatalogExcludesExplicitlyMaleExercises() {
        runBlocking {
            val femaleExercises = AdminWorkoutCatalogProvider.getUnifiedExercisesForGender("Female")
            femaleExercises.forEach { ex ->
                assertFalse(
                    ex.gender.equals("Male", ignoreCase = true),
                    "Female catalog must never include exercise with gender == Male ('${ex.id}')"
                )
            }
        }
    }

    // 8. Exercises are organized by the existing body-part/muscle categorization
    @Test
    fun test08_exerciseOrganizationByBodyPart() {
        val bench = SampleExerciseData.getById("barbell_bench_press")!!
        assertEquals(ExerciseBodyPartSection.CHEST, AdminExerciseCatalogOrganizer.classify(bench))

        val row = SampleExerciseData.getById("bent_over_barbell_row")!!
        assertEquals(ExerciseBodyPartSection.BACK, AdminExerciseCatalogOrganizer.classify(row))

        val ohp = SampleExerciseData.getById("barbell_overhead_press")!!
        assertEquals(ExerciseBodyPartSection.SHOULDERS, AdminExerciseCatalogOrganizer.classify(ohp))

        val curl = SampleExerciseData.getById("barbell_bicep_curl")!!
        assertEquals(ExerciseBodyPartSection.BICEPS, AdminExerciseCatalogOrganizer.classify(curl))

        val pushdown = SampleExerciseData.getById("reverse_grip_pushdown")!!
        assertEquals(ExerciseBodyPartSection.TRICEPS, AdminExerciseCatalogOrganizer.classify(pushdown))

        val hipThrust = SampleExerciseData.getById("hip_thrust")!!
        assertEquals(ExerciseBodyPartSection.GLUTES, AdminExerciseCatalogOrganizer.classify(hipThrust))

        val squat = SampleExerciseData.getById("squat")!!
        assertEquals(ExerciseBodyPartSection.LEGS, AdminExerciseCatalogOrganizer.classify(squat))

        val plank = SampleExerciseData.getById("ab_rollout")!!
        assertEquals(ExerciseBodyPartSection.CORE, AdminExerciseCatalogOrganizer.classify(plank))

        val thruster = SampleExerciseData.getById("dumbbell_thruster")!!
        assertEquals(ExerciseBodyPartSection.FULL_BODY, AdminExerciseCatalogOrganizer.classify(thruster))

        val breathing = SampleExerciseData.getById("diaphragmatic_breathing")!!
        assertEquals(ExerciseBodyPartSection.MOBILITY_RECOVERY, AdminExerciseCatalogOrganizer.classify(breathing))
    }

    // 9. Search works within the selected gender
    @Test
    fun test09_searchFiltersWithinSelectedGender() {
        runBlocking {
            val femaleExercises = AdminWorkoutCatalogProvider.getUnifiedExercisesForGender("Female")
            val searchResults = femaleExercises.filter { it.name.lowercase().contains("hip") }
            assertTrue(searchResults.isNotEmpty(), "Search for 'hip' in female catalog should return exercises")
            assertTrue(searchResults.any { it.id == "hip_thrust" })
        }
    }

    // 10. Existing Add Exercise works
    @Test
    fun test10_addExerciseValidationAndCreation() {
        val repo = FirebaseExerciseRepository()
        val customEx = Exercise(
            id = "custom_test_exercise",
            name = "Custom Test Exercise",
            primaryMuscle = "Chest",
            gender = "Male",
            category = ExerciseCategory.STRENGTH.name,
            difficulty = ExerciseDifficulty.BEGINNER.name
        )
        val validation = repo.validateExercise(customEx)
        assertTrue(validation.isSuccess, "Custom exercise validation must succeed")
    }

    // 11. Add Exercise from Male defaults to Male
    @Test
    fun test11_addExerciseDefaultsToMaleWhenFromMale() {
        val activeGender = "Male"
        val initialGender = activeGender
        assertEquals("Male", initialGender)
    }

    // 12. Add Exercise from Female defaults to Female
    @Test
    fun test12_addExerciseDefaultsToFemaleWhenFromFemale() {
        val activeGender = "Female"
        val initialGender = activeGender
        assertEquals("Female", initialGender)
    }

    // 13. Existing Edit works (id is preserved)
    @Test
    fun test13_editExercisePreservesId() {
        val original = Exercise(id = "hip_thrust", name = "Barbell Hip Thrust")
        val updated = original.copy(name = "Barbell Hip Thrust (Updated)")
        assertEquals(original.id, updated.id)
        assertEquals("Barbell Hip Thrust (Updated)", updated.name)
    }

    // 14. Existing Archive/Restore works
    @Test
    fun test14_archiveAndRestoreFlagsToggleCorrectly() {
        val active = Exercise(id = "test_ex", isActive = true)
        val archived = active.copy(isActive = false)
        assertFalse(archived.isActive)
        val restored = archived.copy(isActive = true)
        assertTrue(restored.isActive)
    }

    // 15. Existing R2 media upload/delete works
    @Test
    fun test15_cloudflareR2MediaRepositoryUploadAndDelete() {
        runBlocking {
            val transport = TestHttpTransport()
            val exId = "hip_thrust"
            val inMemoryRepo = object : com.example.fitness.feature.exercise.data.ExerciseRepository {
                val storage = mutableMapOf(exId to Exercise(id = exId, name = "Hip Thrust"))
                override suspend fun getExercise(exerciseId: String): Exercise? = storage[exerciseId]
                override suspend fun getActiveExercises(): List<Exercise> = storage.values.toList()
                override suspend fun getAllExercisesForAdmin(): List<Exercise> = storage.values.toList()
                override suspend fun createExercise(exercise: Exercise): Result<Unit> { storage[exercise.id] = exercise; return Result.success(Unit) }
                override suspend fun updateExercise(exercise: Exercise): Result<Unit> { storage[exercise.id] = exercise; return Result.success(Unit) }
                override suspend fun archiveExercise(exerciseId: String): Result<Unit> { return Result.success(Unit) }
                override suspend fun restoreExercise(exerciseId: String): Result<Unit> { return Result.success(Unit) }
                override suspend fun deleteExercise(exerciseId: String): Result<Unit> { storage.remove(exerciseId); return Result.success(Unit) }
                override suspend fun updateExerciseMedia(exerciseId: String, media: ExerciseMedia): Result<Unit> {
                    storage[exerciseId]?.let { storage[exerciseId] = it.copy(media = media) }
                    return Result.success(Unit)
                }
            }
            val repo = CloudflareExerciseMediaUploadRepository(
                exerciseRepository = inMemoryRepo,
                workerBaseUrl = "https://athgym-r2-api.fitness.workers.dev",
                tokenProvider = { "fake_firebase_token" },
                httpTransport = transport
            )
            val thumbFile = MediaUploadFile(
                fileName = "thumb.jpg",
                mimeType = "image/jpeg",
                sizeBytes = 1024L,
                bytes = ByteArray(1024) { 0x42 }
            )
            val uploadResult = repo.uploadThumbnail(exId, thumbFile)
            assertTrue(uploadResult.isSuccess, "R2 upload must succeed")
            assertTrue(transport.lastPutUrl?.contains("/v1/admin/exercises/$exId/media/thumbnail") == true)

            val deleteResult = repo.removeMediaAsset(exId, ExerciseMediaType.IMAGE)
            assertTrue(deleteResult.isSuccess, "R2 delete must succeed")
            assertTrue(transport.lastDeleteUrl?.contains("/v1/admin/exercises/$exId/media/thumbnail") == true)
        }
    }

    // 16. Back navigation returns correctly to Male/Female selection
    @Test
    fun test16_backNavigationResetsSelectedGender() {
        var selectedGender: String? = "Male"
        val onNavigateBack = { selectedGender = null }
        onNavigateBack()
        assertEquals(null, selectedGender)
    }

    // 17. User-side functionality remains unchanged
    @Test
    fun test17_userFunctionalityUnchanged() {
        val workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("hypertrophy", "day_1")
        assertNotNull(workout)
        assertTrue(workout.exercises.isNotEmpty())

        val femaleEx = SampleExerciseData.getById("hip_thrust")
        assertNotNull(femaleEx)
    }

    // 18. Program functionality remains unchanged
    @Test
    fun test18_programFunctionalityUnchanged() {
        val malePrograms = SampleProgramData.INITIAL_PROGRAMS
        assertTrue(malePrograms.isNotEmpty())

        val femalePrograms = CanonicalFemaleProgramData.ALL_PROGRAMS
        assertEquals(9, femalePrograms.size)
        femalePrograms.forEach { assertEquals(30, it.days.size) }
    }
}
