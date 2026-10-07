package com.example.fitness.feature.admin

import com.example.fitness.feature.admin.data.AdminWorkoutCatalogProvider
import com.example.fitness.feature.exercise.data.FirebaseExerciseRepository
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.media.ExerciseMediaResolver
import com.example.fitness.feature.exercise.media.ResolvedExerciseMedia
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseCategory
import com.example.fitness.feature.exercise.model.ExerciseDifficulty
import com.example.fitness.feature.exercise.model.ExerciseMedia
import com.example.fitness.feature.exercise.model.ExerciseMediaType
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import com.example.fitness.feature.workout.model.ExerciseItem
import fitness.shared.generated.resources.Res
import fitness.shared.generated.resources.gym_video_thumb
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration test suite verifying the Single Source of Truth architecture:
 * Admin Exercise Management and User Exercise consumption share the exact same
 * live canonical exercise records and media.
 */
class AdminWorkoutExerciseIntegrationTest {

    @BeforeTest
    @AfterTest
    fun cleanup() {
        SampleExerciseData.resetOverridesForTesting()
    }

    @Test
    fun test01_canonicalExerciseSingleSourceOfTruth_AdminUpdatePropagatesToUserLookup() {
        val original = SampleExerciseData.getById("barbell_bench_press")
        assertNotNull(original, "Baseline barbell_bench_press must exist in canonical registry")

        val customInstruction = "Tuck elbows at 45 degrees and pause 1s at bottom."
        val updatedExercise = original.copy(
            instructions = listOf(customInstruction),
            formTips = listOf("Keep feet firmly planted on the floor"),
            version = original.version + 1
        )

        // Admin updates the canonical exercise
        SampleExerciseData.updateCanonicalExercise(updatedExercise)

        // User lookup via SampleExerciseData reflects the exact same updated record
        val userResolved = SampleExerciseData.getById("barbell_bench_press")
        assertNotNull(userResolved)
        assertEquals(listOf(customInstruction), userResolved.instructions)
        assertEquals(listOf("Keep feet firmly planted on the floor"), userResolved.formTips)
        assertEquals(original.version + 1, userResolved.version)
    }

    @Test
    fun test02_adminMediaUpdate_PropagatesToUserMediaResolverAndWorkoutPlan() {
        val exerciseId = "barbell_bench_press"
        val original = SampleExerciseData.getById(exerciseId)!!

        val testVideoUrl = "https://athgym-media.r2.dev/exercises/barbell_bench_press/video.mp4"
        val testThumbUrl = "https://athgym-media.r2.dev/exercises/barbell_bench_press/thumb.jpg"

        val updatedMedia = ExerciseMedia(
            mediaType = ExerciseMediaType.VIDEO.name,
            videoUrl = testVideoUrl,
            thumbnailUrl = testThumbUrl
        )

        val updatedExercise = original.copy(
            media = updatedMedia,
            version = original.version + 1
        )

        // Admin saves updated media
        SampleExerciseData.updateCanonicalExercise(updatedExercise)

        // User media resolver resolves the exact updated media
        val resolved = ExerciseMediaResolver.resolveFromId(exerciseId)
        assertTrue(resolved is ResolvedExerciseMedia.Video)
        assertEquals(testVideoUrl, resolved.url)
        assertEquals(testThumbUrl, resolved.posterUrl)

        // Workout resolver incorporates the updated media into User workout plan
        val workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("hypertrophy", "day_1")
        val benchInPlan = workout.exercises.find { it.id == exerciseId }
        if (benchInPlan != null) {
            assertEquals(testVideoUrl, benchInPlan.media.videoUrl)
            assertEquals(testThumbUrl, benchInPlan.media.thumbnailUrl)
        }
    }

    @Test
    fun test03_genderIsolation_MaleCatalogShowsOnlyMaleExercises() {
        runBlocking {
            val maleExercises = AdminWorkoutCatalogProvider.getUnifiedExercisesForGender("Male")
            assertTrue(maleExercises.isNotEmpty(), "Male catalog must have exercises")

            maleExercises.forEach { ex ->
                // Must never contain female-only exercises
                assertFalse(
                    ex.gender.equals("Female", ignoreCase = true),
                    "Male catalog must not contain female exercises: ${ex.id}"
                )
            }

            // Excludes female exclusive routines
            assertFalse(
                maleExercises.any { it.id == "pelvic_floor_activation" },
                "Male catalog must exclude female-exclusive pelvic_floor_activation"
            )
            assertFalse(
                maleExercises.any { it.id == "diaphragmatic_breathing" },
                "Male catalog must exclude female-exclusive diaphragmatic_breathing"
            )
        }
    }

    @Test
    fun test04_genderIsolation_FemaleCatalogShowsOnlyFemaleExercises() {
        runBlocking {
            val femaleExercises = AdminWorkoutCatalogProvider.getUnifiedExercisesForGender("Female")
            assertTrue(femaleExercises.isNotEmpty(), "Female catalog must have exercises")

            femaleExercises.forEach { ex ->
                // Must never contain male-only exercises
                assertFalse(
                    ex.gender.equals("Male", ignoreCase = true),
                    "Female catalog must not contain male exercises: ${ex.id}"
                )
            }

            // Must include female canonical exercises
            assertTrue(
                femaleExercises.any { it.id == "hip_thrust" },
                "Female catalog must include hip_thrust"
            )
            assertTrue(
                femaleExercises.any { it.id == "cable_glute_kickback" },
                "Female catalog must include cable_glute_kickback"
            )
        }
    }

    @Test
    fun test05_newlyCreatedMaleExercise_AppearsInMaleOnly() {
        runBlocking {
            val newMaleEx = Exercise(
                id = "custom_male_bench_variation",
                name = "Custom Male Bench Variation",
                primaryMuscle = "Chest",
                gender = "Male",
                category = ExerciseCategory.STRENGTH.name,
                difficulty = ExerciseDifficulty.ADVANCED.name
            )

            // Register canonical exercise
            SampleExerciseData.updateCanonicalExercise(newMaleEx)

            val maleList = AdminWorkoutCatalogProvider.getUnifiedExercisesForGender("Male")
            val femaleList = AdminWorkoutCatalogProvider.getUnifiedExercisesForGender("Female")

            assertTrue(
                maleList.any { it.id == "custom_male_bench_variation" },
                "New Male exercise must appear in Male catalog"
            )
            assertFalse(
                femaleList.any { it.id == "custom_male_bench_variation" },
                "New Male exercise must NOT appear in Female catalog"
            )
        }
    }

    @Test
    fun test06_newlyCreatedFemaleExercise_AppearsInFemaleOnly() {
        runBlocking {
            val newFemaleEx = Exercise(
                id = "custom_female_glute_exercise",
                name = "Custom Female Glute Exercise",
                primaryMuscle = "Glutes",
                gender = "Female",
                category = ExerciseCategory.STRENGTH.name,
                difficulty = ExerciseDifficulty.ADVANCED.name
            )

            // Register canonical exercise
            SampleExerciseData.updateCanonicalExercise(newFemaleEx)

            val maleList = AdminWorkoutCatalogProvider.getUnifiedExercisesForGender("Male")
            val femaleList = AdminWorkoutCatalogProvider.getUnifiedExercisesForGender("Female")

            assertTrue(
                femaleList.any { it.id == "custom_female_glute_exercise" },
                "New Female exercise must appear in Female catalog"
            )
            assertFalse(
                maleList.any { it.id == "custom_female_glute_exercise" },
                "New Female exercise must NOT appear in Male catalog"
            )
        }
    }

    @Test
    fun test07_registerCanonicalExercises_SyncsBatchToBothAdminAndUser() {
        val ex1 = Exercise(id = "batch_ex_1", name = "Batch Exercise One", primaryMuscle = "Back", gender = "Both")
        val ex2 = Exercise(id = "batch_ex_2", name = "Batch Exercise Two", primaryMuscle = "Legs", gender = "Both")

        SampleExerciseData.registerCanonicalExercises(listOf(ex1, ex2))

        assertEquals("Batch Exercise One", SampleExerciseData.getById("batch_ex_1")?.name)
        assertEquals("Batch Exercise Two", SampleExerciseData.getById("batch_ex_2")?.name)

        runBlocking {
            val maleList = AdminWorkoutCatalogProvider.getUnifiedExercisesForGender("Male")
            val femaleList = AdminWorkoutCatalogProvider.getUnifiedExercisesForGender("Female")

            assertTrue(maleList.any { it.id == "batch_ex_1" })
            assertTrue(femaleList.any { it.id == "batch_ex_1" })
        }
    }

    @Test
    fun test08_uploadedVideoUrl_PrecedenceOverDemoFallback_AndFallbackWhenMissing() {
        val exerciseId = "barbell_bench_press"
        val original = SampleExerciseData.getById(exerciseId)!!
        val uploadedVideoUrl = "https://athgym-media.example.workers.dev/v1/exercises/$exerciseId/media/video"
        val demoFallbackUrl = "https://fitness.app/demo/bench_press.mp4"

        // 1. When videoUrl exists in canonical exercise:
        val canonicalWithVideo = original.copy(
            media = ExerciseMedia(
                mediaType = ExerciseMediaType.VIDEO.name,
                videoUrl = uploadedVideoUrl
            )
        )
        SampleExerciseData.updateCanonicalExercise(canonicalWithVideo)

        // User repository / canonical lookup returns the updated exercise
        val userFetchedExercise = SampleExerciseData.getById(exerciseId)
        assertNotNull(userFetchedExercise)
        assertEquals(uploadedVideoUrl, userFetchedExercise.media.videoUrl)

        // ExerciseItem configured with both canonical ID and a legacy demo fallback URL
        val exerciseItemWithDemo = ExerciseItem(
            id = exerciseId,
            name = original.name,
            sets = "3 sets",
            duration = "60s",
            description = "Barbell Bench Press description",
            imageRes = Res.drawable.gym_video_thumb,
            videoDemoUrl = demoFallbackUrl
        )

        // Resolver MUST prioritize the uploaded video URL and NOT select the demo video
        val resolvedWithCanonical = ExerciseMediaResolver.resolve(exerciseItemWithDemo)
        assertTrue(resolvedWithCanonical is ResolvedExerciseMedia.Video)
        assertEquals(uploadedVideoUrl, resolvedWithCanonical.url)
        assertFalse(
            resolvedWithCanonical.url == demoFallbackUrl,
            "Demo video must NOT be selected when canonical videoUrl is present"
        )

        // Also test direct Exercise resolution
        val resolvedFromExercise = ExerciseMediaResolver.resolve(userFetchedExercise)
        assertTrue(resolvedFromExercise is ResolvedExerciseMedia.Video)
        assertEquals(uploadedVideoUrl, resolvedFromExercise.url)

        // 2. When videoUrl is missing / blank:
        val canonicalWithoutVideo = original.copy(
            media = ExerciseMedia(
                mediaType = ExerciseMediaType.IMAGE.name,
                videoUrl = ""
            )
        )
        SampleExerciseData.updateCanonicalExercise(canonicalWithoutVideo)

        val exerciseItemWithoutCanonicalVideo = ExerciseItem(
            id = exerciseId,
            name = original.name,
            sets = "3 sets",
            duration = "60s",
            description = "Barbell Bench Press description",
            imageRes = Res.drawable.gym_video_thumb,
            videoDemoUrl = demoFallbackUrl
        )

        // Demo fallback IS selected when canonical videoUrl is missing
        val resolvedFallback = ExerciseMediaResolver.resolve(exerciseItemWithoutCanonicalVideo)
        assertTrue(resolvedFallback is ResolvedExerciseMedia.Video)
        assertEquals(
            demoFallbackUrl,
            resolvedFallback.url,
            "Existing demo fallback video MUST be selected when canonical videoUrl is missing"
        )

        // 3. Test CanonicalWorkoutResolver propagates uploaded videoUrl to PlanExercise
        SampleExerciseData.updateCanonicalExercise(canonicalWithVideo)
        val workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("hypertrophy", "day_1")
        val benchInPlan = workout.exercises.find { it.id == exerciseId }
        assertNotNull(benchInPlan, "barbell_bench_press should be in workout plan")
        assertEquals(uploadedVideoUrl, benchInPlan.videoUrl)
    }

    /**
     * Regression Test: Section 11 & Section 2
     *
     * Given:
     *   exercise.media.videoUrl = uploadedVideoUrl
     *   exercise.videoDemoUrl = demoVideoUrl
     *
     * Admin preview resolver / player source MUST resolve to:
     *   uploadedVideoUrl
     * and MUST NOT resolve to:
     *   demoVideoUrl.
     *
     * Also test:
     *   If media.videoUrl is blank -> existing demo fallback is allowed.
     */
    @Test
    fun testAdminPreviewResolverPrioritizesUploadedVideoOverDemoVideo() {
        val exerciseId = "barbell_bench_press"
        val uploadedVideoUrl = "https://pub-worker.r2.dev/exercise-media/barbell_bench_press/video/unique_test_video_1.mp4"
        val demoVideoUrl = "https://fitness.app/demo/bench_press.mp4"

        // 1. Prepare exercise with uploaded video
        val baseExercise = SampleExerciseData.getById(exerciseId)!!
        val updatedWithUpload = baseExercise.copy(
            media = baseExercise.media.copy(
                videoUrl = uploadedVideoUrl,
                mediaType = ExerciseMediaType.VIDEO.name
            )
        )

        // Simulate upload completion: immediate in-memory canonical update
        SampleExerciseData.updateCanonicalExercise(updatedWithUpload)

        // Construct Admin preview presentation model (same pattern used in AdminExerciseDetailsDialog)
        val adminPreviewItem = ExerciseItem(
            id = updatedWithUpload.id,
            name = updatedWithUpload.name,
            sets = "${updatedWithUpload.defaultSets} sets",
            duration = "${updatedWithUpload.defaultRestSeconds}s",
            description = updatedWithUpload.description,
            imageRes = Res.drawable.gym_video_thumb,
            videoDemoUrl = demoVideoUrl, // Even if legacy demo video is present
            media = updatedWithUpload.media
        )

        // Resolve Admin player media source
        val resolvedAdminMedia = ExerciseMediaResolver.resolve(adminPreviewItem)

        // MUST resolve to uploadedVideoUrl
        assertTrue(resolvedAdminMedia is ResolvedExerciseMedia.Video)
        assertEquals(uploadedVideoUrl, resolvedAdminMedia.url)
        assertFalse(
            resolvedAdminMedia.url == demoVideoUrl,
            "Admin preview MUST NOT resolve to demoVideoUrl when canonical media.videoUrl is present"
        )

        // Also test direct Exercise resolution for Admin
        val resolvedDirect = ExerciseMediaResolver.resolve(updatedWithUpload)
        assertTrue(resolvedDirect is ResolvedExerciseMedia.Video)
        assertEquals(uploadedVideoUrl, resolvedDirect.url)

        // 2. When media.videoUrl is blank:
        val exerciseWithBlankVideo = baseExercise.copy(
            media = baseExercise.media.copy(
                videoUrl = "",
                mediaType = ExerciseMediaType.IMAGE.name
            )
        )
        SampleExerciseData.updateCanonicalExercise(exerciseWithBlankVideo)

        val adminPreviewItemBlank = ExerciseItem(
            id = exerciseWithBlankVideo.id,
            name = exerciseWithBlankVideo.name,
            sets = "3 sets",
            duration = "60s",
            description = exerciseWithBlankVideo.description,
            imageRes = Res.drawable.gym_video_thumb,
            videoDemoUrl = demoVideoUrl,
            media = exerciseWithBlankVideo.media
        )

        val resolvedBlankFallback = ExerciseMediaResolver.resolve(adminPreviewItemBlank)
        assertTrue(resolvedBlankFallback is ResolvedExerciseMedia.Video)
        assertEquals(
            demoVideoUrl,
            resolvedBlankFallback.url,
            "Existing demo fallback is allowed ONLY when media.videoUrl is blank"
        )
    }

    /**
     * Regression Test: Section 8 (Replacement Test)
     *
     * Upload Video #1 -> Admin preview resolves to Video #1.
     * Upload Video #2 (Replacement) -> Admin preview immediately resolves to Video #2.
     * Must NOT resolve to Video #1 or demo video.
     */
    @Test
    fun testAdminVideoReplacementWorkflowUpdatesPreviewToVideo2() {
        val exerciseId = "barbell_bench_press"
        val video1Url = "https://pub-worker.r2.dev/exercise-media/barbell_bench_press/video/video_1_movement_a.mp4"
        val video2Url = "https://pub-worker.r2.dev/exercise-media/barbell_bench_press/video/video_2_movement_b.mp4"
        val demoVideoUrl = "https://fitness.app/demo/bench_press.mp4"

        val baseExercise = SampleExerciseData.getById(exerciseId)!!

        // Step 1: Upload Video #1
        val exVideo1 = baseExercise.copy(
            media = baseExercise.media.copy(
                videoUrl = video1Url,
                mediaType = ExerciseMediaType.VIDEO.name
            ),
            version = baseExercise.version + 1
        )
        SampleExerciseData.updateCanonicalExercise(exVideo1)

        val adminItem1 = ExerciseItem(
            id = exVideo1.id,
            name = exVideo1.name,
            sets = "3 sets",
            duration = "60s",
            description = exVideo1.description,
            imageRes = Res.drawable.gym_video_thumb,
            videoDemoUrl = exVideo1.media.videoUrl,
            media = exVideo1.media
        )
        val resolved1 = ExerciseMediaResolver.resolve(adminItem1)
        assertTrue(resolved1 is ResolvedExerciseMedia.Video)
        assertEquals(video1Url, resolved1.url)

        // Step 2: Upload Video #2 (Replacement)
        val exVideo2 = exVideo1.copy(
            media = exVideo1.media.copy(
                videoUrl = video2Url,
                mediaType = ExerciseMediaType.VIDEO.name
            ),
            version = exVideo1.version + 1
        )
        SampleExerciseData.updateCanonicalExercise(exVideo2)

        val adminItem2 = ExerciseItem(
            id = exVideo2.id,
            name = exVideo2.name,
            sets = "3 sets",
            duration = "60s",
            description = exVideo2.description,
            imageRes = Res.drawable.gym_video_thumb,
            videoDemoUrl = exVideo2.media.videoUrl,
            media = exVideo2.media
        )
        val resolved2 = ExerciseMediaResolver.resolve(adminItem2)
        assertTrue(resolved2 is ResolvedExerciseMedia.Video)
        assertEquals(video2Url, resolved2.url, "Admin preview MUST resolve to Video #2 after replacement")
        assertFalse(resolved2.url == video1Url, "Admin preview MUST NOT play old Video #1")
        assertFalse(resolved2.url == demoVideoUrl, "Admin preview MUST NOT play demo video")

        // Step 3: User pipeline also consumes Video #2
        val userFetched = SampleExerciseData.getById(exerciseId)
        assertNotNull(userFetched)
        assertEquals(video2Url, userFetched.media.videoUrl)
        val resolvedUser = ExerciseMediaResolver.resolve(userFetched)
        assertTrue(resolvedUser is ResolvedExerciseMedia.Video)
        assertEquals(video2Url, resolvedUser.url)
    }

    /**
     * Verifies that when an Admin uploads a video for an exercise, all User screens
     * (BodyPartWorkoutDetailScreen via WorkoutRepository, ThirtyDayWorkout, and UserExerciseDetailView)
     * immediately resolve and play the exact uploaded video URL without falling back to demo videos.
     */
    @Test
    fun testAdminUploadDirectlyReflectedInUserScreensAndLegacyIds() {
        val canonicalId = "barbell_bench_press"
        val legacyId = "gym_chest_1"
        val uploadedVideoUrl = "https://athgym-r2-api.workers.dev/v1/exercises/$canonicalId/media/video"

        val initialRevision = SampleExerciseData.canonicalRevision
        val baseExercise = SampleExerciseData.getById(canonicalId)!!

        // Admin uploads a new video
        val updatedExercise = baseExercise.copy(
            media = ExerciseMedia(
                videoUrl = uploadedVideoUrl,
                mediaType = ExerciseMediaType.VIDEO.name
            ),
            version = baseExercise.version + 1
        )
        SampleExerciseData.updateCanonicalExercise(updatedExercise)

        // 1. Revision counter incremented for Compose reactivity
        assertTrue(SampleExerciseData.canonicalRevision > initialRevision, "canonicalRevision must increment on upload")

        // 2. User queries via legacy ID gym_chest_1 resolve to the uploaded video
        val userLegacyExercise = SampleExerciseData.getById(legacyId)
        assertNotNull(userLegacyExercise, "Legacy ID gym_chest_1 must resolve canonical exercise")
        assertEquals(uploadedVideoUrl, userLegacyExercise.media.videoUrl)

        // 3. User queries via exercise name resolve to the uploaded video
        val userByName = SampleExerciseData.findByName("Barbell Bench Press")
        assertNotNull(userByName)
        assertEquals(uploadedVideoUrl, userByName.media.videoUrl)

        // 4. WorkoutRepository.getWorkout (used by BodyPartWorkoutDetailScreen) reflects uploaded video
        val gymChestWorkout = com.example.fitness.feature.workout.data.WorkoutRepository.getWorkout(
            com.example.fitness.feature.workout.model.WorkoutType.GYM,
            "chest"
        )
        assertNotNull(gymChestWorkout)
        val firstExercise = gymChestWorkout.exercises.first()
        assertEquals(uploadedVideoUrl, firstExercise.media.videoUrl, "User WorkoutRepository must have uploaded video")
        assertEquals(uploadedVideoUrl, firstExercise.videoDemoUrl, "User videoDemoUrl must match uploaded video")

        // 5. ExerciseMediaResolver resolves to the uploaded video, NOT demo video
        val resolved = ExerciseMediaResolver.resolve(firstExercise)
        assertTrue(resolved is ResolvedExerciseMedia.Video)
        assertEquals(uploadedVideoUrl, resolved.url, "User player must play uploaded video, not demo")
        assertFalse(resolved.url.contains("demo"), "Must NOT play demo video")

        // 6. User looking up via FirebaseExerciseRepository resolves canonical ID and uploaded video
        runBlocking {
            val repoExercise = FirebaseExerciseRepository().getExercise(legacyId)
            assertNotNull(repoExercise)
            assertEquals(uploadedVideoUrl, repoExercise.media.videoUrl)
        }
    }
}

