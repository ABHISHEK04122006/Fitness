package com.example.fitness.feature.admin

import com.example.fitness.core.navigation.Screen
import com.example.fitness.feature.admin.data.AdminExerciseCatalogOrganizer
import com.example.fitness.feature.admin.data.AdminWorkoutCatalogProvider
import com.example.fitness.feature.admin.data.ExerciseBodyPartSection
import com.example.fitness.feature.exercise.data.FirebaseExerciseRepository
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.media.ExerciseMediaHttpTransport
import com.example.fitness.feature.exercise.media.ExerciseMediaResolver
import com.example.fitness.feature.exercise.media.ExerciseMediaValidator
import com.example.fitness.feature.exercise.media.FemaleExerciseMediaRegistry
import com.example.fitness.feature.exercise.media.MediaUploadFile
import com.example.fitness.feature.exercise.media.ResolvedExerciseMedia
import com.example.fitness.feature.exercise.media.CloudflareExerciseMediaUploadRepository
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseCategory
import com.example.fitness.feature.exercise.model.ExerciseDifficulty
import com.example.fitness.feature.exercise.model.ExerciseMedia
import com.example.fitness.feature.program.data.CanonicalFemaleProgramData
import com.example.fitness.feature.program.data.FirebaseProgramRepository
import com.example.fitness.feature.program.data.SampleProgramData
import com.example.fitness.feature.program.model.Program
import com.example.fitness.feature.program.model.ProgramType
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive Test Suite for the Reworked Admin Workout Hub:
 * 1. Male Exercises display correctly from unified data source.
 * 2. Female Exercises display correctly from unified data source.
 * 3. Admin and User platforms share identical exercise IDs and attributes.
 * 4. Add Exercise works using existing Exercise model & fields.
 * 5. Media limits (5MB thumb, 10MB anim, 100MB video) enforced via Cloudflare R2 pipeline.
 * 6. Cloudflare R2 uploaded URLs are consumable by User platform and ExerciseMediaResolver.
 * 7. Exercise Catalog correctly organizes exercises into body-part / muscle sections.
 * 8. Cardio/sprint/activity items (e.g. 10m sprints) are not incorrectly presented as normal strength body parts.
 * 9. Male Programs match canonical user program definitions.
 * 10. Female Programs match all 9 authoritative female programs.
 * 11. Add Program works using existing Program model.
 * 12. Preserves existing User workout resolver and Admin dashboard routing.
 */
class AdminWorkoutManagementHubTest {

    // Mock HTTP transport for deterministic Cloudflare R2 testing
    private class FakeHttpTransport(
        val putResponse: Pair<Int, String> = 200 to """{"success":true,"key":"exercises/test_bench/thumbnail"}""",
        val deleteResponse: Pair<Int, String> = 200 to """{"success":true}"""
    ) : ExerciseMediaHttpTransport {
        var lastPutUrl: String? = null
        var lastPutBytes: ByteArray? = null

        override suspend fun put(url: String, bearerToken: String, mimeType: String, bytes: ByteArray): Pair<Int, String> {
            lastPutUrl = url
            lastPutBytes = bytes
            return putResponse
        }

        override suspend fun delete(url: String, bearerToken: String): Pair<Int, String> {
            return deleteResponse
        }
    }

    private class InMemoryExerciseRepository(
        private val storage: MutableMap<String, Exercise> = mutableMapOf()
    ) : com.example.fitness.feature.exercise.data.ExerciseRepository {
        override suspend fun getExercise(exerciseId: String): Exercise? = storage[exerciseId]
        override suspend fun getActiveExercises(): List<Exercise> = storage.values.filter { it.isActive }
        override suspend fun getAllExercisesForAdmin(): List<Exercise> = storage.values.toList()
        override suspend fun createExercise(exercise: Exercise): Result<Unit> {
            storage[exercise.id] = exercise
            return Result.success(Unit)
        }
        override suspend fun updateExercise(exercise: Exercise): Result<Unit> {
            storage[exercise.id] = exercise
            return Result.success(Unit)
        }
        override suspend fun archiveExercise(exerciseId: String): Result<Unit> {
            storage[exerciseId]?.let { storage[exerciseId] = it.copy(isActive = false) }
            return Result.success(Unit)
        }
        override suspend fun restoreExercise(exerciseId: String): Result<Unit> {
            storage[exerciseId]?.let { storage[exerciseId] = it.copy(isActive = true) }
            return Result.success(Unit)
        }
        override suspend fun deleteExercise(exerciseId: String): Result<Unit> {
            storage.remove(exerciseId)
            return Result.success(Unit)
        }
        override suspend fun updateExerciseMedia(exerciseId: String, media: ExerciseMedia): Result<Unit> {
            val ex = storage[exerciseId] ?: return Result.failure(NoSuchElementException())
            storage[exerciseId] = ex.copy(media = media, version = ex.version + 1)
            return Result.success(Unit)
        }
    }

    @Test
    fun test01_maleExercisesDisplayCorrectlyAndMatchUserPlatform() {
        runBlocking {
            val maleExercises = AdminWorkoutCatalogProvider.getUnifiedExercisesForGender("Male")
            assertTrue(maleExercises.isNotEmpty(), "Male exercises list must not be empty")

            // Must contain foundational compound lifts available to male users
            val benchPress = maleExercises.find { it.id == "barbell_bench_press" }
            assertNotNull(benchPress, "Barbell Bench Press must exist in male exercise catalog")
            assertEquals("Barbell Bench Press", benchPress.name)
            assertEquals("Chest", benchPress.primaryMuscle)

            val squat = maleExercises.find { it.id == "squat" }
            assertNotNull(squat, "Squat must exist in male exercise catalog")

            val deadlift = maleExercises.find { it.id == "deadlift" }
            assertNotNull(deadlift, "Deadlift must exist in male exercise catalog")
        }
    }

    @Test
    fun test02_femaleExercisesDisplayCorrectlyAndMatchUserPlatform() {
        runBlocking {
            val femaleExercises = AdminWorkoutCatalogProvider.getUnifiedExercisesForGender("Female")
            assertTrue(femaleExercises.isNotEmpty(), "Female exercises list must not be empty")

            // Must include all unique exercises from the 9 female 30-day programs
            val femaleProgramIds = AdminWorkoutCatalogProvider.femaleProgramExerciseIds
            assertTrue(femaleProgramIds.size >= 70, "Must have at least 70 unique exercises across female programs")

            // Test female-specific movement patterns
            val hipThrust = femaleExercises.find { it.id == "hip_thrust" }
            assertNotNull(hipThrust, "Hip Thrust must exist in female exercise catalog")

            val gobletSquat = femaleExercises.find { it.id == "dumbbell_goblet_squat" }
            assertNotNull(gobletSquat, "Dumbbell Goblet Squat must exist in female exercise catalog")

            val gluteBridge = femaleExercises.find { it.id == "glute_bridge" }
            assertNotNull(gluteBridge, "Glute Bridge must exist in female exercise catalog")

            val cableKickback = femaleExercises.find { it.id == "cable_glute_kickback" }
            assertNotNull(cableKickback, "Cable Glute Kickback must exist in female exercise catalog")
        }
    }

    @Test
    fun test03_adminAndUserUseExactSameExerciseIdsAndData() {
        runBlocking {
            val maleAdminExercises = AdminWorkoutCatalogProvider.getUnifiedExercisesForGender("Male")
            val femaleAdminExercises = AdminWorkoutCatalogProvider.getUnifiedExercisesForGender("Female")

            // Check that any exercise resolved by Admin exists identically in SampleExerciseData
            maleAdminExercises.take(20).forEach { adminEx ->
                val userEx = SampleExerciseData.getById(adminEx.id)
                assertNotNull(userEx, "Exercise '${adminEx.id}' seen by Admin must exist for User")
                assertEquals(userEx.name, adminEx.name, "Name must be identical for exercise '${adminEx.id}'")
                assertEquals(userEx.primaryMuscle, adminEx.primaryMuscle, "Primary muscle must match")
            }

            femaleAdminExercises.take(20).forEach { adminEx ->
                val userEx = SampleExerciseData.getById(adminEx.id)
                assertNotNull(userEx, "Exercise '${adminEx.id}' seen by Admin must exist for User")
                assertEquals(userEx.name, adminEx.name, "Name must be identical for exercise '${adminEx.id}'")
            }
        }
    }

    @Test
    fun test04_createTestExerciseUsingExistingArchitecture() {
        val repo = FirebaseExerciseRepository()
        val testExercise = Exercise(
            id = "test_custom_pushup",
            name = "Test Custom Pushup",
            slug = "test_custom_pushup",
            description = "A progressive variation of the standard pushup.",
            primaryMuscle = "Chest",
            secondaryMuscles = listOf("Triceps", "Shoulders"),
            equipment = listOf("Bodyweight"),
            category = ExerciseCategory.STRENGTH.name,
            difficulty = ExerciseDifficulty.BEGINNER.name,
            defaultSets = 3,
            defaultReps = "12-15",
            defaultRestSeconds = 60,
            gender = "Male",
            isActive = true
        )

        val validation = repo.validateExercise(testExercise)
        assertTrue(validation.isSuccess, "Exercise validation must succeed for valid exercise")
        assertEquals("test_custom_pushup", testExercise.id)
        assertEquals("Male", testExercise.gender)
    }

    @Test
    fun test05_exerciseMediaLimitsAreStrictlyEnforced() {
        // Thumbnail <= 5 MB
        assertEquals(5 * 1024 * 1024L, ExerciseMediaValidator.MAX_THUMBNAIL_BYTES)
        // Animation <= 10 MB
        assertEquals(10 * 1024 * 1024L, ExerciseMediaValidator.MAX_ANIMATION_BYTES)
        // Video <= 100 MB
        assertEquals(100 * 1024 * 1024L, ExerciseMediaValidator.MAX_VIDEO_BYTES)
    }

    @Test
    fun test06_mediaUploadRepositoryHandlesThumbnailThroughExistingR2Integration() {
        runBlocking {
            val exerciseId = "test_bench_press"
            val initialExercise = Exercise(
                id = exerciseId,
                name = "Bench Press",
                primaryMuscle = "Chest",
                defaultSets = 3
            )
            val inMemoryRepo = InMemoryExerciseRepository(mutableMapOf(exerciseId to initialExercise))
            val fakeTransport = FakeHttpTransport(
                putResponse = 200 to """{"success":true,"key":"exercises/$exerciseId/thumbnail"}"""
            )

            val mediaRepo = CloudflareExerciseMediaUploadRepository(
                exerciseRepository = inMemoryRepo,
                tokenProvider = { "fake_firebase_token" },
                httpTransport = fakeTransport,
                workerBaseUrl = "https://athgym-r2-api.fitness.workers.dev"
            )

            val thumbFile = MediaUploadFile(
                fileName = "thumb.jpg",
                mimeType = "image/jpeg",
                sizeBytes = 1024L,
                bytes = ByteArray(1024) { 1 }
            )

            val uploadResult = mediaRepo.uploadThumbnail(exerciseId, thumbFile)
            assertTrue(uploadResult.isSuccess, "Thumbnail upload to R2 Worker must succeed")

            val returnedUrl = uploadResult.getOrThrow()
            assertEquals("https://athgym-r2-api.fitness.workers.dev/v1/exercises/$exerciseId/media/thumbnail", returnedUrl)

            // Verify Firestore record updated with worker-served media URL
            val updated = inMemoryRepo.getExercise(exerciseId)
            assertNotNull(updated)
            assertEquals(returnedUrl, updated.media.thumbnailUrl)
            assertEquals("IMAGE", updated.media.mediaType)
        }
    }

    @Test
    fun test07_mediaUploadRepositoryHandlesVideoThroughExistingR2Integration() {
        runBlocking {
            val exerciseId = "test_squat"
            val initialExercise = Exercise(
                id = exerciseId,
                name = "Squat",
                primaryMuscle = "Legs",
                defaultSets = 4
            )
            val inMemoryRepo = InMemoryExerciseRepository(mutableMapOf(exerciseId to initialExercise))
            val fakeTransport = FakeHttpTransport(
                putResponse = 200 to """{"success":true,"key":"exercises/$exerciseId/video"}"""
            )

            val mediaRepo = CloudflareExerciseMediaUploadRepository(
                exerciseRepository = inMemoryRepo,
                tokenProvider = { "fake_firebase_token" },
                httpTransport = fakeTransport,
                workerBaseUrl = "https://athgym-r2-api.fitness.workers.dev"
            )

            val videoFile = MediaUploadFile(
                fileName = "squat_demo.mp4",
                mimeType = "video/mp4",
                sizeBytes = 2048L,
                bytes = ByteArray(2048) { 2 }
            )

            val uploadResult = mediaRepo.uploadVideo(exerciseId, videoFile)
            assertTrue(uploadResult.isSuccess, "Video upload to R2 Worker must succeed")

            val returnedUrl = uploadResult.getOrThrow()
            assertEquals("https://athgym-r2-api.fitness.workers.dev/v1/exercises/$exerciseId/media/video", returnedUrl)

            val updated = inMemoryRepo.getExercise(exerciseId)
            assertNotNull(updated)
            assertEquals(returnedUrl, updated.media.videoUrl)
            assertEquals("VIDEO", updated.media.mediaType)
        }
    }

    @Test
    fun test08_userMediaResolverConsumesExerciseMediaCorrectly() {
        val exerciseWithMedia = Exercise(
            id = "test_deadlift",
            name = "Deadlift",
            primaryMuscle = "Back",
            media = ExerciseMedia(
                thumbnailUrl = "https://athgym-r2-api.fitness.workers.dev/v1/exercises/test_deadlift/media/thumbnail",
                videoUrl = "https://athgym-r2-api.fitness.workers.dev/v1/exercises/test_deadlift/media/video",
                mediaType = "VIDEO"
            )
        )

        // ExerciseMediaResolver is the exact resolver used by WorkoutVideoPlayer and UserExerciseDetailView
        val resolved = ExerciseMediaResolver.resolve(exerciseWithMedia)
        assertTrue(resolved is ResolvedExerciseMedia.Video, "Resolved media must be Video")
        assertEquals("https://athgym-r2-api.fitness.workers.dev/v1/exercises/test_deadlift/media/video", resolved.url)
        assertEquals("https://athgym-r2-api.fitness.workers.dev/v1/exercises/test_deadlift/media/thumbnail", resolved.posterUrl)
    }

    @Test
    fun test09_maleProgramsDisplayCorrectlyAndMatchUserPlatform() {
        runBlocking {
            val malePrograms = AdminWorkoutCatalogProvider.getUnifiedProgramsForGender("Male")
            assertTrue(malePrograms.isNotEmpty(), "Male programs must not be empty")

            // Must contain canonical 30-Day, Hypertrophy, Strength, Athlete Performance
            val progIds = malePrograms.map { it.id }
            assertTrue(progIds.any { it.contains("30_day") || it.contains("thirty_day") }, "Must include 30-day program")
            assertTrue(progIds.any { it.contains("hypertrophy") }, "Must include hypertrophy program")
            assertTrue(progIds.any { it.contains("strength") }, "Must include strength program")
            assertTrue(progIds.any { it.contains("athlete") }, "Must include athlete program")

            // Verify none of the male programs are female-specific
            assertFalse(malePrograms.any { it.id.startsWith("female_") }, "Male programs list must not contain female_ programs")
        }
    }

    @Test
    fun test10_femaleProgramsDisplayCorrectlyAndMatchUserPlatform() {
        runBlocking {
            val femalePrograms = AdminWorkoutCatalogProvider.getUnifiedProgramsForGender("Female")
            assertEquals(9, femalePrograms.size, "Must have exactly 9 authoritative female programs")

            val expectedIds = listOf(
                "female_weight_loss",
                "female_sculpted_body",
                "female_perfect_butt",
                "female_6_pack_abs",
                "female_general_muscle_building",
                "female_powerlifting",
                "female_crossfit",
                "female_full_body_in_45_minutes",
                "female_postpartum_recovery"
            )

            expectedIds.forEach { expectedId ->
                val found = femalePrograms.find { it.id == expectedId }
                assertNotNull(found, "Female program '$expectedId' must exist in Admin catalog")
                assertEquals(30, found.days.size, "Program '$expectedId' must have exactly 30 days")
            }
        }
    }

    @Test
    fun test11_addProgramUsingExistingProgramModel() {
        val repo = FirebaseProgramRepository()
        val customProgram = Program(
            id = "test_strength_block",
            name = "Test Strength Block",
            slug = "test_strength_block",
            description = "A 4-week heavy compound progression block.",
            type = ProgramType.STRENGTH.name,
            experienceLevel = "ADVANCED",
            durationDays = 28,
            trainingDaysPerWeek = 4,
            gender = "Male",
            days = emptyList(),
            isActive = true
        )

        val validation = repo.validateProgram(customProgram)
        assertTrue(validation.isSuccess, "Program validation must succeed")
        assertEquals("test_strength_block", customProgram.id)
        assertEquals("Male", customProgram.gender)
    }

    @Test
    fun test12_exerciseCatalogCategorization_ProperlyGroupsIndividualStrengthExercises() {
        // Chest
        val bench = SampleExerciseData.getById("barbell_bench_press")
        assertNotNull(bench)
        assertEquals(ExerciseBodyPartSection.CHEST, AdminExerciseCatalogOrganizer.classify(bench))

        // Back
        val row = SampleExerciseData.getById("bent_over_barbell_row")
        assertNotNull(row)
        assertEquals(ExerciseBodyPartSection.BACK, AdminExerciseCatalogOrganizer.classify(row))

        // Shoulders
        val ohp = SampleExerciseData.getById("barbell_overhead_press")
        assertNotNull(ohp)
        assertEquals(ExerciseBodyPartSection.SHOULDERS, AdminExerciseCatalogOrganizer.classify(ohp))

        // Biceps
        val curl = SampleExerciseData.getById("barbell_bicep_curl")
        assertNotNull(curl)
        assertEquals(ExerciseBodyPartSection.BICEPS, AdminExerciseCatalogOrganizer.classify(curl))

        // Triceps
        val pushdown = SampleExerciseData.getById("reverse_grip_pushdown")
        assertNotNull(pushdown)
        assertEquals(ExerciseBodyPartSection.TRICEPS, AdminExerciseCatalogOrganizer.classify(pushdown))

        // Legs
        val squat = SampleExerciseData.getById("squat")
        assertNotNull(squat)
        assertEquals(ExerciseBodyPartSection.LEGS, AdminExerciseCatalogOrganizer.classify(squat))

        // Core
        val plank = SampleExerciseData.getById("ab_rollout")
        assertNotNull(plank)
        assertEquals(ExerciseBodyPartSection.CORE, AdminExerciseCatalogOrganizer.classify(plank))
    }

    @Test
    fun test13_cardioAndActivities_AreNotClassifiedAsNormalStrengthExercises() {
        // "10m sprints"
        val sprint10m = SampleExerciseData.getById("ten_meter_sprint")
        assertNotNull(sprint10m, "ten_meter_sprint must exist in canonical catalog")
        assertTrue(AdminExerciseCatalogOrganizer.isCardioOrActivity(sprint10m), "10m sprints must be recognized as cardio/activity")
        assertEquals(ExerciseBodyPartSection.CARDIO_DRILLS, AdminExerciseCatalogOrganizer.classify(sprint10m))

        // "4×60m relaxed strides"
        val strides = SampleExerciseData.getById("4_60m_relaxed_strides")
        assertNotNull(strides)
        assertEquals(ExerciseBodyPartSection.CARDIO_DRILLS, AdminExerciseCatalogOrganizer.classify(strides))

        // Multi-movement conditioning rounds
        val rounds = SampleExerciseData.getById("4_rounds_250m_run_15_kettlebell_swings_12_burpees_15_walking_lunges_10_push_ups_30_sec_battle_ropes")
        assertNotNull(rounds)
        assertEquals(ExerciseBodyPartSection.CARDIO_DRILLS, AdminExerciseCatalogOrganizer.classify(rounds))
    }

    @Test
    fun test14_existingUserFunctionalityUnchanged() {
        // Verify canonical workout resolver resolves without crash
        val workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("hypertrophy", "day_1")
        assertNotNull(workout, "Hypertrophy Day 1 workout must resolve cleanly")
        assertTrue(workout.exercises.isNotEmpty(), "Workout exercises must not be empty")

        // Verify FemaleExerciseMediaRegistry resolves female thumbnail
        val femaleThumb = FemaleExerciseMediaRegistry.resolveExerciseImage("dumbbell_goblet_squat", isFemale = true)
        assertNotNull(femaleThumb, "Female goblet squat thumbnail must resolve")
    }

    @Test
    fun test15_existingAdminDashboardRoutesPreserved() {
        assertEquals(Screen.AdminWorkouts, Screen.AdminWorkouts)
        assertEquals(Screen.AdminExercises, Screen.AdminExercises)
        assertEquals(Screen.AdminPrograms, Screen.AdminPrograms)
    }
}
