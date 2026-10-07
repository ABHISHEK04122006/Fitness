package com.example.fitness.feature.exercise

import com.example.fitness.feature.exercise.data.ExerciseRepository
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.media.ExerciseMediaResolver
import com.example.fitness.feature.exercise.media.ExerciseMediaValidator
import com.example.fitness.feature.exercise.media.ExerciseStorageClient
import com.example.fitness.feature.exercise.media.FirebaseExerciseMediaUploadRepository
import com.example.fitness.feature.exercise.media.FirebaseExerciseStorageContract
import com.example.fitness.feature.exercise.media.MediaUploadFile
import com.example.fitness.feature.exercise.media.MediaValidationResult
import com.example.fitness.feature.exercise.media.ResolvedExerciseMedia
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseMedia
import com.example.fitness.feature.exercise.model.ExerciseMediaType
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * STEP 6D — Comprehensive Exercise Media Asset Pipeline Unit Test Suite.
 *
 * Tests the real production media asset pipeline:
 * Exercise -> Firebase Storage (exercise-media/{exerciseId}/...) -> Download URL ->
 * Firestore (exercises/{exerciseId}.media) -> ExerciseMediaResolver -> WorkoutVideoPlayer.
 */
class ExerciseMediaPipelineTest {

    // Helper in-memory test repository
    private class InMemoryExerciseRepository(
        initialExercises: List<Exercise> = emptyList()
    ) : ExerciseRepository {
        val store = initialExercises.associateBy { it.id }.toMutableMap()

        override suspend fun getExercise(exerciseId: String): Exercise? = store[exerciseId]
        override suspend fun getActiveExercises(): List<Exercise> = store.values.filter { it.isActive }
        override suspend fun getAllExercisesForAdmin(): List<Exercise> = store.values.toList()

        override suspend fun createExercise(exercise: Exercise): Result<Unit> {
            store[exercise.id] = exercise
            return Result.success(Unit)
        }

        override suspend fun updateExercise(exercise: Exercise): Result<Unit> {
            store[exercise.id] = exercise
            return Result.success(Unit)
        }

        override suspend fun archiveExercise(exerciseId: String): Result<Unit> {
            val ex = store[exerciseId] ?: return Result.failure(NoSuchElementException())
            store[exerciseId] = ex.copy(isActive = false)
            return Result.success(Unit)
        }

        override suspend fun restoreExercise(exerciseId: String): Result<Unit> {
            val ex = store[exerciseId] ?: return Result.failure(NoSuchElementException())
            store[exerciseId] = ex.copy(isActive = true)
            return Result.success(Unit)
        }

        override suspend fun deleteExercise(exerciseId: String): Result<Unit> {
            store.remove(exerciseId) ?: return Result.failure(NoSuchElementException())
            return Result.success(Unit)
        }

        override suspend fun updateExerciseMedia(exerciseId: String, media: ExerciseMedia): Result<Unit> {
            val ex = store[exerciseId] ?: return Result.failure(NoSuchElementException())
            store[exerciseId] = ex.copy(media = media, version = ex.version + 1)
            return Result.success(Unit)
        }
    }

    // Helper test storage client
    private class TestExerciseStorageClient : ExerciseStorageClient {
        val uploadedFiles = mutableMapOf<String, MediaUploadFile>()
        val deletedPaths = mutableListOf<String>()
        var failUpload: Boolean = false
        var failDelete: Boolean = false

        override suspend fun upload(path: String, file: MediaUploadFile): Result<String> {
            if (failUpload) {
                return Result.failure(IllegalStateException("Simulated network upload failure"))
            }
            uploadedFiles[path] = file
            val encoded = path.replace("/", "%2F")
            return Result.success("https://firebasestorage.googleapis.com/v0/b/athgym.appspot.com/o/$encoded?alt=media")
        }

        override suspend fun delete(path: String): Result<Unit> {
            if (failDelete) {
                return Result.failure(IllegalStateException("Simulated delete failure"))
            }
            deletedPaths.add(path)
            uploadedFiles.remove(path)
            return Result.success(Unit)
        }
    }

    private fun createBaselineExercise(id: String = "barbell_bench_press", name: String = "Barbell Bench Press"): Exercise {
        return Exercise(
            id = id,
            name = name,
            slug = id,
            description = "Standard flat barbell bench press",
            primaryMuscle = "Chest",
            secondaryMuscles = listOf("Triceps", "Front Shoulders"),
            equipment = listOf("Barbell", "Flat Bench"),
            category = "Strength",
            instructions = listOf("Lie on bench", "Lower bar to chest", "Press up"),
            setupSteps = listOf("Set rack height", "Plant feet flat"),
            executionSteps = listOf("Unrack bar", "Lower with control", "Lock out elbows"),
            defaultSets = 4,
            defaultReps = "8-10",
            defaultRestSeconds = 90,
            media = ExerciseMedia(),
            isActive = true
        )
    }

    // 1. Valid thumbnail upload contract
    @Test
    fun testValidThumbnailUploadContract() = runBlocking {
        val exercise = createBaselineExercise("barbell_bench_press", "Barbell Bench Press")
        val repo = InMemoryExerciseRepository(listOf(exercise))
        val storage = TestExerciseStorageClient()
        val pipeline = FirebaseExerciseMediaUploadRepository(repo, storage)

        val thumbFile = MediaUploadFile(
            fileName = "bench_thumb.jpg",
            mimeType = "image/jpeg",
            sizeBytes = 200_000L,
            bytes = ByteArray(10)
        )

        val result = pipeline.uploadThumbnail("barbell_bench_press", thumbFile)
        assertTrue(result.isSuccess)

        val url = result.getOrThrow()
        assertTrue(url.contains("exercise-media%2Fbarbell_bench_press%2Fthumbnail"))

        // Verify Storage path
        val expectedPath = FirebaseExerciseStorageContract.getThumbnailPath("barbell_bench_press")
        assertEquals("exercise-media/barbell_bench_press/thumbnail", expectedPath)
        assertTrue(storage.uploadedFiles.containsKey(expectedPath))

        // Verify Firestore update
        val updated = repo.getExercise("barbell_bench_press")
        assertNotNull(updated)
        assertEquals(url, updated.media.thumbnailUrl)
        assertEquals(ExerciseMediaType.IMAGE.name, updated.media.mediaType)
        // Verify all core data preserved
        assertEquals("Barbell Bench Press", updated.name)
        assertEquals(4, updated.defaultSets)
        assertEquals(listOf("Barbell", "Flat Bench"), updated.equipment)
    }

    // 2. Valid animation upload contract
    @Test
    fun testValidAnimationUploadContract() = runBlocking {
        val exercise = createBaselineExercise("incline_dumbbell_press", "Incline Dumbbell Press")
        val repo = InMemoryExerciseRepository(listOf(exercise))
        val storage = TestExerciseStorageClient()
        val pipeline = FirebaseExerciseMediaUploadRepository(repo, storage)

        val animFile = MediaUploadFile(
            fileName = "incline_press.json",
            mimeType = "application/json",
            sizeBytes = 150_000L,
            bytes = ByteArray(10)
        )

        val result = pipeline.uploadAnimation("incline_dumbbell_press", animFile)
        assertTrue(result.isSuccess)

        val url = result.getOrThrow()
        assertTrue(url.contains("exercise-media%2Fincline_dumbbell_press%2Fanimation"))

        val expectedPath = FirebaseExerciseStorageContract.getAnimationPath("incline_dumbbell_press")
        assertEquals("exercise-media/incline_dumbbell_press/animation", expectedPath)
        assertTrue(storage.uploadedFiles.containsKey(expectedPath))

        val updated = repo.getExercise("incline_dumbbell_press")
        assertNotNull(updated)
        assertEquals(url, updated.media.animationUrl)
        assertEquals(ExerciseMediaType.ANIMATION.name, updated.media.mediaType)
    }

    // 3. Valid video upload contract
    @Test
    fun testValidVideoUploadContract() = runBlocking {
        val exercise = createBaselineExercise("barbell_deadlift", "Barbell Deadlift")
        val repo = InMemoryExerciseRepository(listOf(exercise))
        val storage = TestExerciseStorageClient()
        val pipeline = FirebaseExerciseMediaUploadRepository(repo, storage)

        val videoFile = MediaUploadFile(
            fileName = "deadlift_1080p.mp4",
            mimeType = "video/mp4",
            sizeBytes = 15_000_000L,
            bytes = ByteArray(100)
        )

        val result = pipeline.uploadVideo("barbell_deadlift", videoFile)
        assertTrue(result.isSuccess)

        val url = result.getOrThrow()
        assertTrue(url.contains("exercise-media%2Fbarbell_deadlift%2Fvideo"))

        val expectedPath = FirebaseExerciseStorageContract.getVideoPath("barbell_deadlift")
        assertEquals("exercise-media/barbell_deadlift/video", expectedPath)
        assertTrue(storage.uploadedFiles.containsKey(expectedPath))

        val updated = repo.getExercise("barbell_deadlift")
        assertNotNull(updated)
        assertEquals(url, updated.media.videoUrl)
        assertEquals(ExerciseMediaType.VIDEO.name, updated.media.mediaType)
    }

    // 4. Invalid file rejection (unsupported extension, 0 bytes, oversized)
    @Test
    fun testInvalidFileRejections() = runBlocking {
        val exercise = createBaselineExercise("pull_up", "Pull-up")
        val repo = InMemoryExerciseRepository(listOf(exercise))
        val storage = TestExerciseStorageClient()
        val pipeline = FirebaseExerciseMediaUploadRepository(repo, storage)

        // 4a. Unsupported extension (.exe)
        val exeFile = MediaUploadFile(
            fileName = "malicious.exe",
            mimeType = "application/x-msdownload",
            sizeBytes = 1000L
        )
        val resExe = pipeline.uploadThumbnail("pull_up", exeFile)
        assertTrue(resExe.isFailure)
        assertFalse(storage.uploadedFiles.containsKey("exercise-media/pull_up/thumbnail"))

        // 4b. Zero bytes file
        val zeroBytesFile = MediaUploadFile(
            fileName = "empty.mp4",
            mimeType = "video/mp4",
            sizeBytes = 0L
        )
        val resZero = pipeline.uploadVideo("pull_up", zeroBytesFile)
        assertTrue(resZero.isFailure)

        // 4c. Oversized thumbnail (>5MB)
        val oversizedThumb = MediaUploadFile(
            fileName = "giant.jpg",
            mimeType = "image/jpeg",
            sizeBytes = 6 * 1024 * 1024L
        )
        val resOversized = pipeline.uploadThumbnail("pull_up", oversizedThumb)
        assertTrue(resOversized.isFailure)

        // 4d. Oversized animation (>10MB)
        val oversizedAnim = MediaUploadFile(
            fileName = "giant.json",
            mimeType = "application/json",
            sizeBytes = 11 * 1024 * 1024L
        )
        val resAnim = pipeline.uploadAnimation("pull_up", oversizedAnim)
        assertTrue(resAnim.isFailure)

        // 4e. Oversized video (>100MB)
        val oversizedVideo = MediaUploadFile(
            fileName = "giant.mp4",
            mimeType = "video/mp4",
            sizeBytes = 101 * 1024 * 1024L
        )
        val resVideo = pipeline.uploadVideo("pull_up", oversizedVideo)
        assertTrue(resVideo.isFailure)

        // Verify storage remains empty and Firestore untouched
        assertTrue(storage.uploadedFiles.isEmpty())
        val current = repo.getExercise("pull_up")
        assertNotNull(current)
        assertEquals("", current.media.thumbnailUrl)
        assertEquals("", current.media.videoUrl)
    }

    // 5. Stable exercise ID preservation
    @Test
    fun testStableExerciseIdPreservation() = runBlocking {
        val repo = InMemoryExerciseRepository()
        val storage = TestExerciseStorageClient()
        val pipeline = FirebaseExerciseMediaUploadRepository(repo, storage)

        val validFile = MediaUploadFile(
            fileName = "thumb.png",
            mimeType = "image/png",
            sizeBytes = 50_000L
        )

        // Invalid IDs with special characters or spaces rejected by validator
        val resInvalid = pipeline.uploadThumbnail("invalid id with spaces!", validFile)
        assertTrue(resInvalid.isFailure)

        val resBlank = pipeline.uploadThumbnail("   ", validFile)
        assertTrue(resBlank.isFailure)

        // Valid slug ID preserved
        val validSlug = "barbell_curl_2"
        repo.createExercise(createBaselineExercise(validSlug, "Barbell Curl 2"))
        val resValid = pipeline.uploadThumbnail(validSlug, validFile)
        assertTrue(resValid.isSuccess)
        assertEquals("exercise-media/barbell_curl_2/thumbnail", storage.uploadedFiles.keys.first())
    }

    // 6. Correct Storage path generation matching contract
    @Test
    fun testCorrectStoragePathGeneration() {
        val exerciseId = "lateral_raise"
        assertEquals(
            "exercise-media/lateral_raise/thumbnail",
            FirebaseExerciseStorageContract.getThumbnailPath(exerciseId)
        )
        assertEquals(
            "exercise-media/lateral_raise/animation",
            FirebaseExerciseStorageContract.getAnimationPath(exerciseId)
        )
        assertEquals(
            "exercise-media/lateral_raise/video",
            FirebaseExerciseStorageContract.getVideoPath(exerciseId)
        )
        assertEquals(
            "exercise-media/lateral_raise/thumbnail",
            FirebaseExerciseStorageContract.getStoragePathForType(exerciseId, ExerciseMediaType.IMAGE)
        )
        assertEquals(
            "exercise-media/lateral_raise/animation",
            FirebaseExerciseStorageContract.getStoragePathForType(exerciseId, ExerciseMediaType.ANIMATION)
        )
        assertEquals(
            "exercise-media/lateral_raise/video",
            FirebaseExerciseStorageContract.getStoragePathForType(exerciseId, ExerciseMediaType.VIDEO)
        )
    }

    // 7. Successful URL -> Firestore metadata mapping
    @Test
    fun testSuccessfulUrlToFirestoreMetadataMapping() = runBlocking {
        val exercise = createBaselineExercise("standing_overhead_press", "Overhead Press")
        val repo = InMemoryExerciseRepository(listOf(exercise))
        val storage = TestExerciseStorageClient()
        val pipeline = FirebaseExerciseMediaUploadRepository(repo, storage)

        val file = MediaUploadFile(
            fileName = "ohp.webp",
            mimeType = "image/webp",
            sizeBytes = 120_000L
        )

        val result = pipeline.uploadThumbnail("standing_overhead_press", file)
        assertTrue(result.isSuccess)
        val returnedUrl = result.getOrThrow()

        val saved = repo.getExercise("standing_overhead_press")
        assertNotNull(saved)
        assertEquals(returnedUrl, saved.media.thumbnailUrl)
        assertEquals("Chest", saved.primaryMuscle)
        assertEquals(4, saved.defaultSets)
        assertEquals(listOf("Barbell", "Flat Bench"), saved.equipment)
    }

    // 8. Safe replacement workflow
    @Test
    fun testSafeReplacementWorkflow() = runBlocking {
        val existingMedia = ExerciseMedia(
            thumbnailUrl = "https://firebasestorage.googleapis.com/v0/b/athgym.appspot.com/o/exercise-media%2Fsquat%2Fthumbnail?alt=media",
            mediaType = ExerciseMediaType.IMAGE.name
        )
        val exercise = createBaselineExercise("barbell_squat", "Barbell Squat").copy(media = existingMedia)
        val repo = InMemoryExerciseRepository(listOf(exercise))
        val storage = TestExerciseStorageClient()
        val pipeline = FirebaseExerciseMediaUploadRepository(repo, storage)

        val newThumbnail = MediaUploadFile(
            fileName = "new_squat_thumb.jpg",
            mimeType = "image/jpeg",
            sizeBytes = 350_000L
        )

        // Replace thumbnail
        val replaceResult = pipeline.replaceMediaAsset("barbell_squat", ExerciseMediaType.IMAGE, newThumbnail)
        assertTrue(replaceResult.isSuccess)
        val newUrl = replaceResult.getOrThrow()

        val updated = repo.getExercise("barbell_squat")
        assertNotNull(updated)
        assertEquals(newUrl, updated.media.thumbnailUrl)

        // Replace with video
        val newVideo = MediaUploadFile(
            fileName = "squat_hd.mp4",
            mimeType = "video/mp4",
            sizeBytes = 25_000_000L
        )
        val replaceVideoResult = pipeline.replaceMediaAsset("barbell_squat", ExerciseMediaType.VIDEO, newVideo)
        assertTrue(replaceVideoResult.isSuccess)

        val finalState = repo.getExercise("barbell_squat")
        assertNotNull(finalState)
        assertEquals(replaceVideoResult.getOrThrow(), finalState.media.videoUrl)
        assertEquals(newUrl, finalState.media.thumbnailUrl) // Poster thumbnail preserved!
        assertEquals(ExerciseMediaType.VIDEO.name, finalState.media.mediaType)
    }

    // 9. Failed upload does not corrupt Firestore metadata
    @Test
    fun testFailedUploadDoesNotCorruptFirestoreMetadata() = runBlocking {
        val baselineMedia = ExerciseMedia(
            thumbnailUrl = "https://assets.athgym.com/initial_thumb.jpg",
            mediaType = ExerciseMediaType.IMAGE.name
        )
        val exercise = createBaselineExercise("leg_press", "Leg Press").copy(media = baselineMedia)
        val repo = InMemoryExerciseRepository(listOf(exercise))
        val storage = TestExerciseStorageClient().apply {
            failUpload = true // Storage throws network/permission failure
        }
        val pipeline = FirebaseExerciseMediaUploadRepository(repo, storage)

        val newVideo = MediaUploadFile(
            fileName = "leg_press.mp4",
            mimeType = "video/mp4",
            sizeBytes = 10_000_000L
        )

        val result = pipeline.uploadVideo("leg_press", newVideo)
        assertTrue(result.isFailure)

        // Verify Firestore metadata is completely uncorrupted
        val current = repo.getExercise("leg_press")
        assertNotNull(current)
        assertEquals("https://assets.athgym.com/initial_thumb.jpg", current.media.thumbnailUrl)
        assertEquals("", current.media.videoUrl)
        assertEquals(ExerciseMediaType.IMAGE.name, current.media.mediaType)
    }

    // 10. Existing media remains intact if replacement fails
    @Test
    fun testExistingMediaRemainsIntactIfReplacementFails() = runBlocking {
        val originalMedia = ExerciseMedia(
            thumbnailUrl = "https://storage.athgym.com/valid_thumb.png",
            videoUrl = "https://storage.athgym.com/valid_video.mp4",
            mediaType = ExerciseMediaType.VIDEO.name
        )
        val exercise = createBaselineExercise("cable_flye", "Cable Flye").copy(media = originalMedia)
        val repo = InMemoryExerciseRepository(listOf(exercise))
        val storage = TestExerciseStorageClient().apply {
            failUpload = true
        }
        val pipeline = FirebaseExerciseMediaUploadRepository(repo, storage)

        val replacementVideo = MediaUploadFile(
            fileName = "corrupt_video.mp4",
            mimeType = "video/mp4",
            sizeBytes = 20_000_000L
        )

        val result = pipeline.replaceMediaAsset("cable_flye", ExerciseMediaType.VIDEO, replacementVideo)
        assertTrue(result.isFailure)

        val afterFail = repo.getExercise("cable_flye")
        assertNotNull(afterFail)
        assertEquals("https://storage.athgym.com/valid_thumb.png", afterFail.media.thumbnailUrl)
        assertEquals("https://storage.athgym.com/valid_video.mp4", afterFail.media.videoUrl)
        assertEquals(ExerciseMediaType.VIDEO.name, afterFail.media.mediaType)
    }

    // 11. Existing ExerciseMediaResolver continues working seamlessly
    @Test
    fun testExistingExerciseMediaResolverContinuesWorking() {
        // Empty media -> Empty
        val emptyResolved = ExerciseMediaResolver.resolve(ExerciseMedia())
        assertIs<ResolvedExerciseMedia.Empty>(emptyResolved)

        // Thumbnail only -> Thumbnail
        val thumbMedia = ExerciseMedia(
            thumbnailUrl = "https://storage.athgym.com/thumb.jpg",
            mediaType = ExerciseMediaType.IMAGE.name
        )
        val thumbResolved = ExerciseMediaResolver.resolve(thumbMedia)
        assertIs<ResolvedExerciseMedia.Thumbnail>(thumbResolved)
        assertEquals("https://storage.athgym.com/thumb.jpg", thumbResolved.url)

        // Video + Thumbnail -> Video with posterUrl
        val videoMedia = ExerciseMedia(
            thumbnailUrl = "https://storage.athgym.com/thumb.jpg",
            videoUrl = "https://storage.athgym.com/video.mp4",
            mediaType = ExerciseMediaType.VIDEO.name
        )
        val videoResolved = ExerciseMediaResolver.resolve(videoMedia)
        assertIs<ResolvedExerciseMedia.Video>(videoResolved)
        assertEquals("https://storage.athgym.com/video.mp4", videoResolved.url)
        assertEquals("https://storage.athgym.com/thumb.jpg", videoResolved.posterUrl)

        // Animation priority
        val animMedia = ExerciseMedia(
            thumbnailUrl = "https://storage.athgym.com/thumb.jpg",
            videoUrl = "https://storage.athgym.com/video.mp4",
            animationUrl = "https://storage.athgym.com/anim.json",
            mediaType = ExerciseMediaType.ANIMATION.name
        )
        val animResolved = ExerciseMediaResolver.resolve(animMedia)
        assertIs<ResolvedExerciseMedia.Animation>(animResolved)
        assertEquals("https://storage.athgym.com/anim.json", animResolved.url)
        assertEquals("https://storage.athgym.com/thumb.jpg", animResolved.posterUrl)
    }

    // 12. Existing canonical workout data remains unchanged (all 222 exercises intact)
    @Test
    fun testCanonicalWorkoutDataRemainsUnchanged() {
        val exercises = SampleExerciseData.getAll()
        assertEquals(222, exercises.size)

        // Verify no fake URLs or third-party video links were injected into canonical catalog
        exercises.forEach { ex ->
            assertTrue(ex.id.isNotBlank(), "Exercise ID must be non-blank")
            assertTrue(ex.name.isNotBlank(), "Exercise name must be non-blank")
            assertEquals("", ex.media.videoUrl, "Canonical exercise ${ex.id} must not contain fabricated video URL")
            assertEquals("", ex.media.animationUrl, "Canonical exercise ${ex.id} must not contain fabricated animation URL")
            assertEquals("", ex.media.thumbnailUrl, "Canonical exercise ${ex.id} must not contain fabricated thumbnail URL")
        }
    }

    // 13. Existing workout execution flows remain unchanged
    @Test
    fun testExistingWorkoutExecutionFlowsRemainsUnchanged() {
        val programDay = CanonicalWorkoutResolver.resolveProgramDayByNumber(
            programId = "30_day_beginner",
            dayNumber = 1
        )
        assertNotNull(programDay)
        val workout = CanonicalWorkoutResolver.toThirtyDayWorkout(
            programDay = programDay,
            programId = "30_day_beginner"
        )
        assertNotNull(workout)
        assertTrue(workout.workoutTitle.isNotBlank())
        assertTrue(workout.exercises.isNotEmpty())

        workout.exercises.forEach { planEx ->
            assertTrue(planEx.id.isNotBlank())
            assertTrue(planEx.name.isNotBlank())
            val resolved = ExerciseMediaResolver.resolve(planEx.media)
            assertNotNull(resolved)
        }
    }
}
