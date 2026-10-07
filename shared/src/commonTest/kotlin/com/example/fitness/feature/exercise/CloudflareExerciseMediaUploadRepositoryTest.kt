package com.example.fitness.feature.exercise

import com.example.fitness.feature.exercise.data.ExerciseRepository
import com.example.fitness.feature.exercise.media.CloudflareExerciseMediaUploadRepository
import com.example.fitness.feature.exercise.media.ExerciseMediaHttpTransport
import com.example.fitness.feature.exercise.media.MediaUploadFile
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseMedia
import com.example.fitness.feature.exercise.model.ExerciseMediaType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class CloudflareExerciseMediaUploadRepositoryTest {

    private class FakeExerciseRepository(
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

    private class FakeMediaHttpTransport : ExerciseMediaHttpTransport {
        var putStatusCode: Int = 200
        var putResponseBody: String = """{"success":true}"""
        var lastPutUrl: String? = null
        var lastPutToken: String? = null
        var lastPutMimeType: String? = null
        var lastPutBytes: ByteArray? = null

        var deleteStatusCode: Int = 200
        var deleteResponseBody: String = """{"success":true}"""
        var lastDeleteUrl: String? = null
        var lastDeleteToken: String? = null

        override suspend fun put(
            url: String,
            bearerToken: String,
            mimeType: String,
            bytes: ByteArray
        ): Pair<Int, String> {
            lastPutUrl = url
            lastPutToken = bearerToken
            lastPutMimeType = mimeType
            lastPutBytes = bytes
            return Pair(putStatusCode, putResponseBody)
        }

        override suspend fun delete(url: String, bearerToken: String): Pair<Int, String> {
            lastDeleteUrl = url
            lastDeleteToken = bearerToken
            return Pair(deleteStatusCode, deleteResponseBody)
        }
    }

    private lateinit var fakeRepo: FakeExerciseRepository
    private lateinit var fakeTransport: FakeMediaHttpTransport
    private var mockToken: String? = "admin_valid_token"
    private lateinit var uploadRepo: CloudflareExerciseMediaUploadRepository

    private val sampleExercise = Exercise(
        id = "bench_press",
        slug = "bench-press",
        name = "Barbell Bench Press",
        category = "Chest",
        difficulty = "Intermediate",
        instructions = listOf("Lie on bench", "Lower bar to mid-chest", "Press bar upward"),
        primaryMuscle = "Pectoralis Major",
        secondaryMuscles = listOf("Anterior Deltoid", "Triceps Brachii"),
        media = ExerciseMedia(
            thumbnailUrl = "",
            animationUrl = "",
            videoUrl = "",
            mediaType = ExerciseMediaType.NONE.name
        ),
        version = 1
    )

    @BeforeTest
    fun setUp() {
        fakeRepo = FakeExerciseRepository(listOf(sampleExercise))
        fakeTransport = FakeMediaHttpTransport()
        mockToken = "admin_valid_token"
        uploadRepo = CloudflareExerciseMediaUploadRepository(
            exerciseRepository = fakeRepo,
            tokenProvider = { mockToken },
            httpTransport = fakeTransport,
            workerBaseUrl = "https://athgym-r2-api.prayasingale0909.workers.dev"
        )
    }

    // 1. Successful thumbnail upload
    @Test
    fun testSuccessfulThumbnailUpload_UpdatesFirestoreAndReturnsWorkerUrl() = runBlocking {
        val file = MediaUploadFile(
            fileName = "bench.jpg",
            mimeType = "image/jpeg",
            sizeBytes = 1024,
            bytes = ByteArray(1024) { 1 }
        )

        val result = uploadRepo.uploadThumbnail("bench_press", file)
        assertTrue(result.isSuccess)

        val expectedUrl = "https://athgym-r2-api.prayasingale0909.workers.dev/v1/exercises/bench_press/media/thumbnail"
        assertEquals(expectedUrl, result.getOrThrow())

        // Verify HTTP call
        assertEquals(
            "https://athgym-r2-api.prayasingale0909.workers.dev/v1/admin/exercises/bench_press/media/thumbnail",
            fakeTransport.lastPutUrl
        )
        assertEquals("admin_valid_token", fakeTransport.lastPutToken)
        assertEquals("image/jpeg", fakeTransport.lastPutMimeType)

        // Verify Firestore update
        val updatedExercise = fakeRepo.getExercise("bench_press")
        assertNotNull(updatedExercise)
        assertEquals(expectedUrl, updatedExercise.media.thumbnailUrl)
        assertEquals(ExerciseMediaType.IMAGE.name, updatedExercise.media.mediaType)
        assertEquals(2, updatedExercise.version)
    }

    // 2. Successful animation upload
    @Test
    fun testSuccessfulAnimationUpload_UpdatesFirestoreAndReturnsWorkerUrl() = runBlocking {
        val file = MediaUploadFile(
            fileName = "bench_anim.json",
            mimeType = "application/json",
            sizeBytes = 2048,
            bytes = ByteArray(2048) { 2 }
        )

        val result = uploadRepo.uploadAnimation("bench_press", file)
        assertTrue(result.isSuccess)

        val expectedUrl = "https://athgym-r2-api.prayasingale0909.workers.dev/v1/exercises/bench_press/media/animation"
        assertEquals(expectedUrl, result.getOrThrow())

        val updatedExercise = fakeRepo.getExercise("bench_press")
        assertNotNull(updatedExercise)
        assertEquals(expectedUrl, updatedExercise.media.animationUrl)
        assertEquals(ExerciseMediaType.ANIMATION.name, updatedExercise.media.mediaType)
    }

    // 3. Successful video upload
    @Test
    fun testSuccessfulVideoUpload_UpdatesFirestoreAndReturnsWorkerUrl() = runBlocking {
        val file = MediaUploadFile(
            fileName = "bench_demo.mp4",
            mimeType = "video/mp4",
            sizeBytes = 1048576,
            bytes = ByteArray(1048576) { 3 }
        )

        val result = uploadRepo.uploadVideo("bench_press", file)
        assertTrue(result.isSuccess)

        val expectedUrl = "https://athgym-r2-api.prayasingale0909.workers.dev/v1/exercises/bench_press/media/video"
        assertEquals(expectedUrl, result.getOrThrow())

        val updatedExercise = fakeRepo.getExercise("bench_press")
        assertNotNull(updatedExercise)
        assertEquals(expectedUrl, updatedExercise.media.videoUrl)
        assertEquals(ExerciseMediaType.VIDEO.name, updatedExercise.media.mediaType)
    }

    // 4. Successful delete
    @Test
    fun testSuccessfulDelete_UpdatesFirestoreAndCallsWorkerDelete() = runBlocking {
        // Pre-populate exercise with existing video
        val existingWithVideo = sampleExercise.copy(
            media = ExerciseMedia(
                thumbnailUrl = "https://athgym-r2-api.prayasingale0909.workers.dev/v1/exercises/bench_press/media/thumbnail",
                videoUrl = "https://athgym-r2-api.prayasingale0909.workers.dev/v1/exercises/bench_press/media/video",
                mediaType = ExerciseMediaType.VIDEO.name
            )
        )
        fakeRepo.store["bench_press"] = existingWithVideo

        val result = uploadRepo.removeMediaAsset("bench_press", ExerciseMediaType.VIDEO)
        assertTrue(result.isSuccess)

        // Verify HTTP DELETE called
        assertEquals(
            "https://athgym-r2-api.prayasingale0909.workers.dev/v1/admin/exercises/bench_press/media/video",
            fakeTransport.lastDeleteUrl
        )
        assertEquals("admin_valid_token", fakeTransport.lastDeleteToken)

        // Verify Firestore update cleared videoUrl and recalculated mediaType to IMAGE
        val updated = fakeRepo.getExercise("bench_press")
        assertNotNull(updated)
        assertEquals("", updated.media.videoUrl)
        assertEquals(
            "https://athgym-r2-api.prayasingale0909.workers.dev/v1/exercises/bench_press/media/thumbnail",
            updated.media.thumbnailUrl
        )
        assertEquals(ExerciseMediaType.IMAGE.name, updated.media.mediaType)
    }

    // 5. Missing or expired Firebase token
    @Test
    fun testMissingOrExpiredFirebaseToken_UploadFailsWithoutCallingHttp() = runBlocking {
        mockToken = null // No token

        val file = MediaUploadFile(
            fileName = "bench.jpg",
            mimeType = "image/jpeg",
            sizeBytes = 1024,
            bytes = ByteArray(1024)
        )

        val result = uploadRepo.uploadThumbnail("bench_press", file)
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull()?.message?.contains("Authentication expired") == true)

        // Verify no HTTP request was made
        assertEquals(null, fakeTransport.lastPutUrl)
    }

    // 6. Non-admin rejection (HTTP 403)
    @Test
    fun testNonAdminRejection_ReturnsFailure() = runBlocking {
        fakeTransport.putStatusCode = 403
        fakeTransport.putResponseBody = "Forbidden: Admin privileges required"

        val file = MediaUploadFile(
            fileName = "bench.mp4",
            mimeType = "video/mp4",
            sizeBytes = 2048,
            bytes = ByteArray(2048)
        )

        val result = uploadRepo.uploadVideo("bench_press", file)
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull()?.message?.contains("Admin privileges required") == true)

        // Verify Firestore was NOT updated
        val exercise = fakeRepo.getExercise("bench_press")
        assertNotNull(exercise)
        assertEquals("", exercise.media.videoUrl)
    }

    // 7. Non-admin delete rejection (HTTP 403)
    @Test
    fun testNonAdminDeleteRejection_ReturnsFailure() = runBlocking {
        fakeTransport.deleteStatusCode = 403
        fakeTransport.deleteResponseBody = "Forbidden: Admin privileges required"

        val result = uploadRepo.removeMediaAsset("bench_press", ExerciseMediaType.IMAGE)
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull()?.message?.contains("Admin privileges required") == true)
    }

    // 8. Preserving other exercise fields
    @Test
    fun testPreservingOtherExerciseFields_OnlyMediaAndVersionAreModified() = runBlocking {
        val file = MediaUploadFile(
            fileName = "bench.jpg",
            mimeType = "image/jpeg",
            sizeBytes = 1024,
            bytes = ByteArray(1024)
        )

        val result = uploadRepo.uploadThumbnail("bench_press", file)
        assertTrue(result.isSuccess)

        val updated = fakeRepo.getExercise("bench_press")
        assertNotNull(updated)

        // Verify every other exercise field is preserved exactly
        assertEquals("bench_press", updated.id)
        assertEquals("bench-press", updated.slug)
        assertEquals("Barbell Bench Press", updated.name)
        assertEquals("Chest", updated.category)
        assertEquals("Intermediate", updated.difficulty)
        assertEquals(sampleExercise.instructions, updated.instructions)
        assertEquals(sampleExercise.primaryMuscle, updated.primaryMuscle)
        assertEquals(sampleExercise.secondaryMuscles, updated.secondaryMuscles)
        assertEquals(sampleExercise.version + 1, updated.version)
    }

    // 9. Pre-upload validation: file size limit enforcement
    @Test
    fun testThumbnailExceedingLimit_FailsPreUploadValidation() = runBlocking {
        val oversizedFile = MediaUploadFile(
            fileName = "huge.jpg",
            mimeType = "image/jpeg",
            sizeBytes = 6 * 1024 * 1024L, // 6 MB > 5 MB
            bytes = ByteArray(10)
        )

        val result = uploadRepo.uploadThumbnail("bench_press", oversizedFile)
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull()?.message?.contains("exceeds maximum limit") == true)
        assertEquals(null, fakeTransport.lastPutUrl)
    }
}
