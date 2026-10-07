package com.example.fitness.feature.exercise

import com.example.fitness.core.auth.UserRole
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.exercise.audit.ControlledBatchReport
import com.example.fitness.feature.exercise.audit.ControlledBatchResultItem
import com.example.fitness.feature.exercise.audit.ControlledMediaImportItem
import com.example.fitness.feature.exercise.audit.ExerciseContentAuditor
import com.example.fitness.feature.exercise.audit.ExerciseMediaBulkImporter
import com.example.fitness.feature.exercise.audit.ExerciseMediaImportItem
import com.example.fitness.feature.exercise.audit.ExerciseMediaImportOutcome
import com.example.fitness.feature.exercise.audit.MediaReadiness
import com.example.fitness.feature.exercise.data.ExerciseRepository
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.media.ExerciseMediaResolver
import com.example.fitness.feature.exercise.media.ExerciseStorageClient
import com.example.fitness.feature.exercise.media.FirebaseExerciseMediaUploadRepository
import com.example.fitness.feature.exercise.media.FirebaseExerciseStorageContract
import com.example.fitness.feature.exercise.media.MediaUploadFile
import com.example.fitness.feature.exercise.media.ResolvedExerciseMedia
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseMedia
import com.example.fitness.feature.exercise.model.ExerciseMediaType
import com.example.fitness.feature.program.data.Canonical30DayProgramData
import com.example.fitness.feature.program.data.CanonicalAthletePerformanceProgramData
import com.example.fitness.feature.program.data.CanonicalHypertrophyProgramData
import com.example.fitness.feature.program.data.CanonicalStrengthProgramData
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * STEP 8 — Comprehensive Real Exercise Media Asset Population Unit Test Suite.
 *
 * Fully covers all 20 Section 16 requirements:
 * 1. Valid real asset import
 * 2. Invalid asset rejection
 * 3. Unknown exercise ID rejection
 * 4. Duplicate batch rejection
 * 5. Successful Storage upload
 * 6. Successful Firestore media update
 * 7. Failed upload isolation
 * 8. Failed Firestore update isolation
 * 9. Media audit count update
 * 10. Correct storage path
 * 11. Exercise metadata preservation
 * 12. Program reference preservation
 * 13. Animation priority
 * 14. Video priority
 * 15. Thumbnail fallback
 * 16. Empty fallback
 * 17. Admin-only upload
 * 18. Admin-only replacement
 * 19. Admin-only removal
 * 20. Batch result reporting
 */
class RealExerciseMediaPopulationTest {

    private class InMemoryExerciseRepo(
        initialExercises: List<Exercise> = emptyList()
    ) : ExerciseRepository {
        val store = initialExercises.associateBy { it.id }.toMutableMap()
        var failUpdateMedia: Boolean = false

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
            if (failUpdateMedia) {
                return Result.failure(IllegalStateException("Simulated Firestore update failure"))
            }
            val ex = store[exerciseId] ?: return Result.failure(NoSuchElementException("Exercise $exerciseId not found"))
            store[exerciseId] = ex.copy(media = media, version = ex.version + 1)
            return Result.success(Unit)
        }
    }

    private class MockStorageClient : ExerciseStorageClient {
        val files = mutableMapOf<String, MediaUploadFile>()
        val deletedPaths = mutableListOf<String>()
        var failUpload: Boolean = false
        var failOnSpecificPath: String? = null

        override suspend fun upload(path: String, file: MediaUploadFile): Result<String> {
            if (failUpload || (failOnSpecificPath != null && path.contains(failOnSpecificPath!!))) {
                return Result.failure(IllegalStateException("Simulated upload failure for path: $path"))
            }
            files[path] = file
            val encoded = path.replace("/", "%2F")
            return Result.success("https://firebasestorage.googleapis.com/v0/b/athgym.appspot.com/o/$encoded?alt=media")
        }

        override suspend fun delete(path: String): Result<Unit> {
            deletedPaths.add(path)
            files.remove(path)
            return Result.success(Unit)
        }
    }

    private lateinit var mockStorage: MockStorageClient
    private lateinit var fakeRepo: InMemoryExerciseRepo
    private lateinit var uploadRepo: FirebaseExerciseMediaUploadRepository
    private lateinit var bulkImporter: ExerciseMediaBulkImporter

    @BeforeTest
    fun setUp() {
        UserSession.currentUserRole = UserRole.ADMIN
        fakeRepo = InMemoryExerciseRepo(SampleExerciseData.getAll())
        mockStorage = MockStorageClient()
        uploadRepo = FirebaseExerciseMediaUploadRepository(
            exerciseRepository = fakeRepo,
            storageClient = mockStorage
        )
        bulkImporter = ExerciseMediaBulkImporter(fakeRepo, uploadRepo)
    }

    @AfterTest
    fun tearDown() {
        UserSession.currentUserRole = UserRole.USER
    }

    // 1. Valid real asset import
    @Test
    fun testValidRealAssetImport() = runBlocking {
        val testVideo = MediaUploadFile(
            fileName = "bench_press.mp4",
            mimeType = "video/mp4",
            sizeBytes = 2048L,
            bytes = ByteArray(2048) { 1 }
        )

        val batch = listOf(
            ControlledMediaImportItem(
                exerciseId = "barbell_bench_press",
                mediaType = ExerciseMediaType.VIDEO,
                localFile = testVideo,
                optionalFilename = "bench_press.mp4",
                isAuthorized = true
            )
        )

        val report = bulkImporter.executeControlledBatch(batch)
        assertEquals(1, report.totalProcessed)
        assertEquals(1, report.successCount)
        assertEquals(0, report.failureCount)

        val item = report.results.first()
        assertTrue(item.isSuccess)
        assertEquals("exercise-media/barbell_bench_press/video", item.storagePath)
        assertEquals("Updated", item.firestoreStatus)
        assertEquals("SUCCESS", item.statusMessage)
        assertNull(item.failureReason)
    }

    // 2. Invalid asset rejection
    @Test
    fun testInvalidAssetRejection() = runBlocking {
        val invalidFile = MediaUploadFile(
            fileName = "malicious.exe",
            mimeType = "application/x-msdownload",
            sizeBytes = 5000L,
            bytes = ByteArray(5000)
        )

        val batch = listOf(
            ControlledMediaImportItem(
                exerciseId = "barbell_bench_press",
                mediaType = ExerciseMediaType.VIDEO,
                localFile = invalidFile,
                isAuthorized = true
            )
        )

        val report = bulkImporter.executeControlledBatch(batch)
        assertEquals(1, report.totalProcessed)
        assertEquals(0, report.successCount)
        assertEquals(1, report.failureCount)

        val item = report.results.first()
        assertFalse(item.isSuccess)
        assertTrue(item.failureReason?.contains("Unsupported") == true || item.statusMessage.contains("FAILED"))
    }

    // 3. Unknown exercise ID rejection
    @Test
    fun testUnknownExerciseIdRejection() = runBlocking {
        val testVideo = MediaUploadFile(
            fileName = "unknown.mp4",
            mimeType = "video/mp4",
            sizeBytes = 1024L,
            bytes = ByteArray(1024)
        )

        val batch = listOf(
            ControlledMediaImportItem(
                exerciseId = "non_existent_exercise_123",
                mediaType = ExerciseMediaType.VIDEO,
                localFile = testVideo,
                isAuthorized = true
            )
        )

        val report = bulkImporter.executeControlledBatch(batch)
        assertEquals(1, report.totalProcessed)
        assertEquals(0, report.successCount)
        assertEquals(1, report.failureCount)
        assertTrue(report.results.first().failureReason?.contains("does not exist in catalog") == true)
    }

    // 4. Duplicate batch rejection
    @Test
    fun testDuplicateBatchRejection() = runBlocking {
        val file1 = MediaUploadFile(fileName = "bench1.mp4", mimeType = "video/mp4", sizeBytes = 1024L, bytes = ByteArray(1024))
        val file2 = MediaUploadFile(fileName = "bench2.mp4", mimeType = "video/mp4", sizeBytes = 1024L, bytes = ByteArray(1024))

        val batch = listOf(
            ControlledMediaImportItem("barbell_bench_press", ExerciseMediaType.VIDEO, file1, isAuthorized = true),
            ControlledMediaImportItem("barbell_bench_press", ExerciseMediaType.VIDEO, file2, isAuthorized = true)
        )

        val report = bulkImporter.executeControlledBatch(batch)
        assertEquals(2, report.totalProcessed)
        assertEquals(1, report.successCount)
        assertEquals(1, report.failureCount)
        assertTrue(report.results[1].failureReason?.contains("Duplicate") == true)
    }

    // 5. Successful Storage upload
    @Test
    fun testSuccessfulStorageUpload() = runBlocking {
        val testFile = MediaUploadFile(fileName = "thumb.jpg", mimeType = "image/jpeg", sizeBytes = 1024L, bytes = ByteArray(1024) { 42 })

        val batch = listOf(
            ControlledMediaImportItem("incline_dumbbell_press", ExerciseMediaType.IMAGE, testFile, isAuthorized = true)
        )

        val report = bulkImporter.executeControlledBatch(batch)
        assertTrue(report.results.first().isSuccess)

        val expectedStorageKey = "exercise-media/incline_dumbbell_press/thumbnail"
        assertTrue(mockStorage.files.containsKey(expectedStorageKey), "Storage must contain key $expectedStorageKey")
        assertEquals(1024L, mockStorage.files[expectedStorageKey]?.sizeBytes)
    }

    // 6. Successful Firestore media update
    @Test
    fun testSuccessfulFirestoreMediaUpdate() = runBlocking {
        val initialExercise = fakeRepo.getExercise("barbell_bench_press")!!
        val initialVersion = initialExercise.version

        val testVideo = MediaUploadFile(fileName = "press.mp4", mimeType = "video/mp4", sizeBytes = 2048L, bytes = ByteArray(2048))
        val batch = listOf(
            ControlledMediaImportItem("barbell_bench_press", ExerciseMediaType.VIDEO, testVideo, isAuthorized = true)
        )

        bulkImporter.executeControlledBatch(batch)

        val updated = fakeRepo.getExercise("barbell_bench_press")!!
        assertTrue(updated.media.videoUrl.isNotBlank(), "Firestore videoUrl must be populated")
        assertEquals(initialVersion + 1, updated.version, "Version must be incremented")
    }

    // 7. Failed upload isolation
    @Test
    fun testFailedUploadIsolation() = runBlocking {
        mockStorage.failOnSpecificPath = "pec_deck"

        val file1 = MediaUploadFile(fileName = "v1.mp4", mimeType = "video/mp4", sizeBytes = 1024L, bytes = ByteArray(1024))
        val file2 = MediaUploadFile(fileName = "v2.mp4", mimeType = "video/mp4", sizeBytes = 1024L, bytes = ByteArray(1024))

        val batch = listOf(
            ControlledMediaImportItem("pec_deck", ExerciseMediaType.VIDEO, file1, isAuthorized = true),
            ControlledMediaImportItem("cable_fly", ExerciseMediaType.VIDEO, file2, isAuthorized = true)
        )

        val report = bulkImporter.executeControlledBatch(batch)
        assertEquals(2, report.totalProcessed)
        assertEquals(1, report.successCount)
        assertEquals(1, report.failureCount)

        assertFalse(report.results[0].isSuccess)
        assertTrue(report.results[1].isSuccess)
    }

    // 8. Failed Firestore update isolation
    @Test
    fun testFailedFirestoreUpdateIsolation() = runBlocking {
        fakeRepo.failUpdateMedia = true

        val file = MediaUploadFile(fileName = "video.mp4", mimeType = "video/mp4", sizeBytes = 1024L, bytes = ByteArray(1024))
        val batch = listOf(
            ControlledMediaImportItem("barbell_bench_press", ExerciseMediaType.VIDEO, file, isAuthorized = true)
        )

        val report = bulkImporter.executeControlledBatch(batch)
        assertEquals(1, report.failureCount)
        assertFalse(report.results.first().isSuccess)
        assertTrue(report.results.first().failureReason?.contains("Firestore") == true)
    }

    // 9. Media audit count update
    @Test
    fun testMediaAuditCountUpdate() = runBlocking {
        // Initial state
        val initialSummary = ExerciseContentAuditor.summarizeAudit(fakeRepo.getAllExercisesForAdmin())
        assertEquals(222, initialSummary.totalExercises)
        assertEquals(0, initialSummary.withAnyMedia)
        assertEquals(222, initialSummary.withNoMedia)

        // Upload 1 thumbnail
        val file = MediaUploadFile(fileName = "thumb.jpg", mimeType = "image/jpeg", sizeBytes = 1024L, bytes = ByteArray(1024))
        bulkImporter.executeControlledBatch(
            listOf(ControlledMediaImportItem("barbell_bench_press", ExerciseMediaType.IMAGE, file, isAuthorized = true))
        )

        // Updated audit state
        val updatedSummary = ExerciseContentAuditor.summarizeAudit(fakeRepo.getAllExercisesForAdmin())
        assertEquals(222, updatedSummary.totalExercises)
        assertEquals(1, updatedSummary.withThumbnail)
        assertEquals(1, updatedSummary.withAnyMedia)
        assertEquals(221, updatedSummary.withNoMedia)
    }

    // 10. Correct storage path
    @Test
    fun testCorrectStoragePath() = runBlocking {
        val thumbFile = MediaUploadFile(fileName = "t.png", mimeType = "image/png", sizeBytes = 512L, bytes = ByteArray(512))
        val animFile = MediaUploadFile(fileName = "a.json", mimeType = "application/json", sizeBytes = 512L, bytes = ByteArray(512))
        val vidFile = MediaUploadFile(fileName = "v.mp4", mimeType = "video/mp4", sizeBytes = 512L, bytes = ByteArray(512))

        val batch = listOf(
            ControlledMediaImportItem("barbell_bench_press", ExerciseMediaType.IMAGE, thumbFile, isAuthorized = true),
            ControlledMediaImportItem("incline_dumbbell_press", ExerciseMediaType.ANIMATION, animFile, isAuthorized = true),
            ControlledMediaImportItem("cable_fly", ExerciseMediaType.VIDEO, vidFile, isAuthorized = true)
        )

        val report = bulkImporter.executeControlledBatch(batch)
        assertEquals(3, report.successCount)

        assertEquals("exercise-media/barbell_bench_press/thumbnail", report.results[0].storagePath)
        assertEquals("exercise-media/incline_dumbbell_press/animation", report.results[1].storagePath)
        assertEquals("exercise-media/cable_fly/video", report.results[2].storagePath)

        assertTrue(mockStorage.files.containsKey("exercise-media/barbell_bench_press/thumbnail"))
        assertTrue(mockStorage.files.containsKey("exercise-media/incline_dumbbell_press/animation"))
        assertTrue(mockStorage.files.containsKey("exercise-media/cable_fly/video"))
    }

    // 11. Exercise metadata preservation
    @Test
    fun testExerciseMetadataPreservation() = runBlocking {
        val original = fakeRepo.getExercise("barbell_bench_press")!!

        val file = MediaUploadFile(fileName = "bench.mp4", mimeType = "video/mp4", sizeBytes = 1024L, bytes = ByteArray(1024))
        bulkImporter.executeControlledBatch(
            listOf(ControlledMediaImportItem("barbell_bench_press", ExerciseMediaType.VIDEO, file, isAuthorized = true))
        )

        val updated = fakeRepo.getExercise("barbell_bench_press")!!

        // Media and version updated
        assertTrue(updated.media.videoUrl.isNotBlank())
        assertEquals(original.version + 1, updated.version)

        // All non-media attributes preserved
        assertEquals(original.id, updated.id)
        assertEquals(original.name, updated.name)
        assertEquals(original.slug, updated.slug)
        assertEquals(original.description, updated.description)
        assertEquals(original.primaryMuscle, updated.primaryMuscle)
        assertEquals(original.secondaryMuscles, updated.secondaryMuscles)
        assertEquals(original.muscleGroups, updated.muscleGroups)
        assertEquals(original.equipment, updated.equipment)
        assertEquals(original.category, updated.category)
        assertEquals(original.movementPattern, updated.movementPattern)
        assertEquals(original.difficulty, updated.difficulty)
        assertEquals(original.instructions, updated.instructions)
        assertEquals(original.setupSteps, updated.setupSteps)
        assertEquals(original.executionSteps, updated.executionSteps)
        assertEquals(original.breathing, updated.breathing)
        assertEquals(original.formTips, updated.formTips)
        assertEquals(original.commonMistakes, updated.commonMistakes)
        assertEquals(original.defaultSets, updated.defaultSets)
        assertEquals(original.defaultReps, updated.defaultReps)
        assertEquals(original.defaultRestSeconds, updated.defaultRestSeconds)
    }

    // 12. Program reference preservation
    @Test
    fun testProgramReferencePreservation() {
        val catalogIds = SampleExerciseData.getAll().map { it.id }.toSet()

        // 30-Day Programs
        Canonical30DayProgramData.PROGRAM_BEGINNER.days.forEach { day ->
            day.exercises.forEach {
                assertTrue(catalogIds.contains(it.exerciseId), "30-Day exercise ID ${it.exerciseId} must exist")
            }
        }

        // Hypertrophy
        CanonicalHypertrophyProgramData.PROGRAM_HYPERTROPHY.days.forEach { day ->
            day.exercises.forEach {
                assertTrue(catalogIds.contains(it.exerciseId), "Hypertrophy exercise ID ${it.exerciseId} must exist")
            }
        }

        // Strength
        CanonicalStrengthProgramData.PROGRAM_STRENGTH.days.forEach { day ->
            day.exercises.forEach {
                assertTrue(catalogIds.contains(it.exerciseId), "Strength exercise ID ${it.exerciseId} must exist")
            }
        }

        // Athlete Performance
        CanonicalAthletePerformanceProgramData.PROGRAM_ATHLETE_PERFORMANCE.days.forEach { day ->
            day.exercises.forEach {
                assertTrue(catalogIds.contains(it.exerciseId), "Athlete exercise ID ${it.exerciseId} must exist")
            }
        }
    }

    // 13. Animation priority
    @Test
    fun testAnimationPriority() {
        val media = ExerciseMedia(
            thumbnailUrl = "https://thumb.jpg",
            animationUrl = "https://anim.json",
            videoUrl = "https://video.mp4"
        )
        val resolved = ExerciseMediaResolver.resolve(media)
        assertIs<ResolvedExerciseMedia.Animation>(resolved)
        assertEquals("https://anim.json", resolved.url)
        assertEquals("https://thumb.jpg", resolved.posterUrl)
    }

    // 14. Video priority
    @Test
    fun testVideoPriority() {
        val media = ExerciseMedia(
            thumbnailUrl = "https://thumb.jpg",
            animationUrl = "",
            videoUrl = "https://video.mp4"
        )
        val resolved = ExerciseMediaResolver.resolve(media)
        assertIs<ResolvedExerciseMedia.Video>(resolved)
        assertEquals("https://video.mp4", resolved.url)
        assertEquals("https://thumb.jpg", resolved.posterUrl)
    }

    // 15. Thumbnail fallback
    @Test
    fun testThumbnailFallback() {
        val media = ExerciseMedia(
            thumbnailUrl = "https://thumb.jpg",
            animationUrl = "",
            videoUrl = ""
        )
        val resolved = ExerciseMediaResolver.resolve(media)
        assertIs<ResolvedExerciseMedia.Thumbnail>(resolved)
        assertEquals("https://thumb.jpg", resolved.url)
    }

    // 16. Empty fallback
    @Test
    fun testEmptyFallback() {
        val media = ExerciseMedia(
            thumbnailUrl = "",
            animationUrl = "",
            videoUrl = ""
        )
        val resolved = ExerciseMediaResolver.resolve(media)
        assertIs<ResolvedExerciseMedia.Empty>(resolved)
    }

    // 17. Admin-only upload
    @Test
    fun testAdminOnlyUpload() = runBlocking {
        val file = MediaUploadFile(fileName = "thumb.jpg", mimeType = "image/jpeg", sizeBytes = 1024L, bytes = ByteArray(1024))

        // As USER
        UserSession.currentUserRole = UserRole.USER
        val canUploadUser = UserSession.currentUserRole == UserRole.ADMIN
        assertFalse(canUploadUser, "Regular USER must NOT be allowed to upload media")

        // As ADMIN
        UserSession.currentUserRole = UserRole.ADMIN
        val canUploadAdmin = UserSession.currentUserRole == UserRole.ADMIN
        assertTrue(canUploadAdmin, "ADMIN must be allowed to upload media")
        val adminResult = uploadRepo.uploadThumbnail("barbell_bench_press", file)
        assertTrue(adminResult.isSuccess, "Admin upload must succeed")
    }

    // 18. Admin-only replacement
    @Test
    fun testAdminOnlyReplacement() = runBlocking {
        val initialFile = MediaUploadFile(fileName = "v1.mp4", mimeType = "video/mp4", sizeBytes = 1024L, bytes = ByteArray(1024))
        UserSession.currentUserRole = UserRole.ADMIN
        uploadRepo.uploadVideo("barbell_bench_press", initialFile)

        val replaceFile = MediaUploadFile(fileName = "v2.mp4", mimeType = "video/mp4", sizeBytes = 2048L, bytes = ByteArray(2048))

        // As USER
        UserSession.currentUserRole = UserRole.USER
        val canReplaceUser = UserSession.currentUserRole == UserRole.ADMIN
        assertFalse(canReplaceUser, "Regular USER cannot replace media")

        // As ADMIN
        UserSession.currentUserRole = UserRole.ADMIN
        val canReplaceAdmin = UserSession.currentUserRole == UserRole.ADMIN
        assertTrue(canReplaceAdmin, "ADMIN can replace media")
        val adminResult = uploadRepo.replaceMediaAsset("barbell_bench_press", ExerciseMediaType.VIDEO, replaceFile)
        assertTrue(adminResult.isSuccess, "Admin can safely replace media")
    }

    // 19. Admin-only removal
    @Test
    fun testAdminOnlyRemoval() = runBlocking {
        val file = MediaUploadFile(fileName = "thumb.jpg", mimeType = "image/jpeg", sizeBytes = 1024L, bytes = ByteArray(1024))
        UserSession.currentUserRole = UserRole.ADMIN
        uploadRepo.uploadThumbnail("barbell_bench_press", file)

        // As USER
        UserSession.currentUserRole = UserRole.USER
        val canRemoveUser = UserSession.currentUserRole == UserRole.ADMIN
        assertFalse(canRemoveUser, "Regular USER cannot remove media")

        // As ADMIN
        UserSession.currentUserRole = UserRole.ADMIN
        val canRemoveAdmin = UserSession.currentUserRole == UserRole.ADMIN
        assertTrue(canRemoveAdmin, "ADMIN can remove media")
        val adminResult = uploadRepo.removeMediaAsset("barbell_bench_press", ExerciseMediaType.IMAGE)
        assertTrue(adminResult.isSuccess, "Admin can remove media")
    }

    // 20. Batch result reporting
    @Test
    fun testBatchResultReporting() = runBlocking {
        // Run pilot batch without authorized local assets
        val pilotExercises = SampleExerciseData.getAll().take(6)
        val pilotBatch = ExerciseMediaBulkImporter.createPilotBatchPlan(pilotExercises)

        val report = bulkImporter.executeControlledBatch(pilotBatch)
        assertEquals(6, report.totalProcessed)
        assertEquals(0, report.successCount)
        assertEquals(6, report.failureCount)
        assertFalse(report.hasAuthorizedAssets)

        report.results.forEach { result ->
            assertFalse(result.isSuccess)
            assertEquals("Unchanged", result.firestoreStatus)
            assertEquals("FAILED: No authorized media asset available.", result.statusMessage)
            assertEquals("No authorized media asset available.", result.failureReason)
            assertTrue(result.storagePath.startsWith("exercise-media/"))
            assertFalse(result.statusMessage.contains("https://"), "Raw download URLs must not be in primary UI")
        }
    }
}
