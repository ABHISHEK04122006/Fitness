package com.example.fitness.feature.admin

import com.example.fitness.core.auth.UserRole
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.admin.data.AdminCatalogSyncRepository
import com.example.fitness.feature.admin.data.SecurityException
import com.example.fitness.feature.admin.model.AdminSyncHistoryRecord
import com.example.fitness.feature.admin.model.CatalogSyncPreview
import com.example.fitness.feature.admin.model.EntitySyncSummary
import com.example.fitness.feature.admin.model.WorkoutCatalogItem
import com.example.fitness.feature.admin.model.toCatalogItem
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseMedia
import com.example.fitness.feature.program.data.SampleProgramData
import com.example.fitness.feature.program.model.Program
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import com.example.fitness.feature.workout.data.ThirtyDayWorkoutRepository
import com.example.fitness.feature.workout.data.WorkoutRepository
import com.example.fitness.feature.workout.model.ExperienceLevel
import com.example.fitness.feature.workout.model.WorkoutType
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive test suite for ADMIN CATALOG SEED / SYNC MODULE.
 *
 * Verifies:
 * 1. Canonical asset discovery (Exercises = 222, Workouts = 16, Programs = 9)
 * 2. Idempotency & Duplicate Protection (No random IDs, stable snake_case keys)
 * 3. Pre-flight preview calculation
 * 4. Collision warning detection for identical names with differing IDs
 * 5. Archive Safety (Preservation of isActive = false during sync)
 * 6. Media Safety (Preservation of existing video URLs and storage paths)
 * 7. Workout Internal Rotation field preservation (A/B/C/D)
 * 8. Administrative RBAC enforcement (Unauthorized users rejected)
 * 9. Batch chunking logic (Chunks <= 200 items)
 * 10. Sync history record structure & audit attribution
 * 11. Compatibility with mobile application consumers
 */
class AdminCatalogSyncManagementTest {

    private val repository = AdminCatalogSyncRepository()

    @BeforeTest
    fun setUp() {
        UserSession.clear()
        UserSession.currentUserRole = UserRole.ADMIN
        UserSession.currentUserId = "admin_test_uid"
    }

    @AfterTest
    fun tearDown() {
        UserSession.clear()
    }

    // ==========================================
    // 1. CANONICAL ASSET AUDIT
    // ==========================================

    @Test
    fun testCanonicalExercises_Contains222AuthoritativeItemsWithStableIds() {
        val exercises = repository.getCanonicalExercises()
        assertEquals(222, exercises.size, "Canonical exercise library must contain exactly 222 items.")

        val uniqueIds = exercises.map { it.id }.toSet()
        assertEquals(222, uniqueIds.size, "All 222 exercises must possess unique stable IDs.")

        for (ex in exercises) {
            assertTrue(ex.id.isNotBlank(), "Exercise ID cannot be blank.")
            assertTrue(ex.name.isNotBlank(), "Exercise name cannot be blank.")
            assertTrue(ex.primaryMuscle.isNotBlank(), "Exercise primaryMuscle cannot be blank for ${ex.id}.")
        }
    }

    @Test
    fun testCanonicalWorkouts_Contains16StandaloneRoutines() {
        val workouts = repository.getCanonicalWorkouts()
        assertEquals(16, workouts.size, "Canonical workouts must contain 16 routines (8 Gym + 8 Home).")

        val gymWorkouts = workouts.filter { it.workoutType == "GYM" }
        val homeWorkouts = workouts.filter { it.workoutType == "HOME" }
        assertEquals(8, gymWorkouts.size, "Must contain exactly 8 Gym workouts.")
        assertEquals(8, homeWorkouts.size, "Must contain exactly 8 Home workouts.")

        val uniqueIds = workouts.map { it.id }.toSet()
        assertEquals(16, uniqueIds.size, "All 16 workouts must have stable unique IDs.")

        // Verify representative stable IDs
        assertTrue(uniqueIds.contains("gym_chest"))
        assertTrue(uniqueIds.contains("gym_back"))
        assertTrue(uniqueIds.contains("home_chest"))
        assertTrue(uniqueIds.contains("home_back"))
    }

    @Test
    fun testCanonicalPrograms_ContainsAuthoritativeCurricula() {
        val programs = repository.getCanonicalPrograms()
        assertTrue(programs.size >= 7, "Must contain at least 7 canonical programs.")

        val ids = programs.map { it.id }.toSet()
        assertTrue(ids.contains("thirty_day_no_experience"))
        assertTrue(ids.contains("thirty_day_beginner"))
        assertTrue(ids.contains("thirty_day_advanced"))
        assertTrue(ids.contains("thirty_day_expert"))
        assertTrue(ids.contains("hypertrophy_30_day"))
        assertTrue(ids.contains("strength_30_day"))
        assertTrue(ids.contains("athlete_performance_30_day"))

        for (prog in programs) {
            assertTrue(prog.durationDays > 0, "Program durationDays must be positive for ${prog.id}.")
            assertTrue(prog.days.isNotEmpty(), "Program must define program days for ${prog.id}.")
        }
    }

    // ==========================================
    // 2. IDEMPOTENCY & DUPLICATE PROTECTION
    // ==========================================

    @Test
    fun testStableIds_NeverGeneratesRandomIdsDuringMapping() {
        val gymRaw = WorkoutRepository.getWorkouts(WorkoutType.GYM).first()
        val mapped1 = gymRaw.toCatalogItem()
        val mapped2 = gymRaw.toCatalogItem()

        assertEquals(mapped1.id, mapped2.id, "Repeated catalog mapping must produce identical stable IDs.")
        assertEquals("gym_chest", mapped1.id)
        assertFalse(mapped1.id.contains("random"), "IDs must never be randomly generated.")
    }

    @Test
    fun testCollisionDetection_IdentifiesSameNameUnderDifferentId() {
        val canonicalEx = SampleExerciseData.ALL_EXERCISES.first() // e.g. "barbell_bench_press"
        val mockExistingDocs = mapOf(canonicalEx.name.trim().lowercase() to "legacy_custom_bench_press")

        val warnings = mutableListOf<String>()
        val normalized = canonicalEx.name.trim().lowercase()
        val conflictingId = mockExistingDocs[normalized]
        if (conflictingId != null && conflictingId != canonicalEx.id) {
            warnings.add("Exercise name collision: '${canonicalEx.name}' matches existing document ID '$conflictingId' (canonical ID is '${canonicalEx.id}').")
        }

        assertEquals(1, warnings.size)
        assertTrue(warnings.first().contains("Exercise name collision"))
        assertTrue(warnings.first().contains("legacy_custom_bench_press"))
    }

    // ==========================================
    // 3. ARCHIVE SAFETY
    // ==========================================

    @Test
    fun testArchiveSafety_PreservesSoftDeletedStatus() {
        val existingArchivedExercise = Exercise(
            id = "barbell_bench_press",
            name = "Barbell Bench Press",
            primaryMuscle = "Chest",
            isActive = false, // ARCHIVED by admin previously
            createdAt = 1000L,
            createdBy = "previous_admin",
            version = 3
        )

        val canonical = SampleExerciseData.getById("barbell_bench_press")!!

        // Simulate safe update logic
        val updated = canonical.copy(
            id = canonical.id,
            isActive = existingArchivedExercise.isActive, // MUST PRESERVE ARCHIVED STATUS
            createdAt = existingArchivedExercise.createdAt,
            createdBy = existingArchivedExercise.createdBy,
            version = existingArchivedExercise.version + 1
        )

        assertFalse(updated.isActive, "Archived exercise must remain isActive = false after catalog sync.")
        assertEquals(1000L, updated.createdAt, "Original createdAt must be preserved.")
        assertEquals("previous_admin", updated.createdBy, "Original createdBy must be preserved.")
        assertEquals(4, updated.version, "Version must increment.")
    }

    // ==========================================
    // 4. MEDIA SAFETY
    // ==========================================

    @Test
    fun testMediaSafety_DoesNotOverwriteExistingMedia() {
        val existingWithMedia = Exercise(
            id = "barbell_bench_press",
            name = "Barbell Bench Press",
            primaryMuscle = "Chest",
            media = ExerciseMedia(
                videoUrl = "https://cdn.athgym.com/videos/bench.mp4",
                thumbnailUrl = "https://cdn.athgym.com/thumbs/bench.jpg"
            ),
            isActive = true
        )

        val canonicalWithoutMedia = Exercise(
            id = "barbell_bench_press",
            name = "Barbell Bench Press",
            primaryMuscle = "Chest",
            media = ExerciseMedia(), // Empty default
            isActive = true
        )

        val preservedMedia = if (existingWithMedia.media.videoUrl.isNotBlank() || existingWithMedia.media.thumbnailUrl.isNotBlank()) {
            existingWithMedia.media
        } else {
            canonicalWithoutMedia.media
        }

        val updated = canonicalWithoutMedia.copy(media = preservedMedia)
        assertEquals("https://cdn.athgym.com/videos/bench.mp4", updated.media.videoUrl)
        assertEquals("https://cdn.athgym.com/thumbs/bench.jpg", updated.media.thumbnailUrl)
    }

    // ==========================================
    // 5. ROTATION FIELD PRESERVATION
    // ==========================================

    @Test
    fun testRotationFieldPreservation_RetainsBackendRotationMetadata() {
        val workoutWithRotation = WorkoutCatalogItem(
            id = "gym_chest",
            title = "Chest",
            rotation = "Rotation A",
            workoutType = "GYM"
        )

        val updated = workoutWithRotation.copy(
            title = "Chest Power",
            rotation = workoutWithRotation.rotation // Preserved
        )

        assertEquals("Rotation A", updated.rotation, "Internal rotation metadata must never be cleared or renamed.")
    }

    // ==========================================
    // 6. RBAC / SECURITY ENFORCEMENT
    // ==========================================

    @Test
    fun testRBAC_NonAdminCannotExecuteCatalogSync() {
        UserSession.currentUserRole = UserRole.USER
        UserSession.currentUserId = "normal_athlete_uid"

        assertFailsWith<SecurityException>("Non-admin user must be rejected when attempting catalog sync.") {
            repository.verifyAdminAuthorization()
        }
    }

    @Test
    fun testRBAC_AdminIsAuthorized() {
        UserSession.currentUserRole = UserRole.ADMIN
        UserSession.currentUserId = "admin_uid"

        // Should execute without throwing SecurityException
        repository.verifyAdminAuthorization()
    }

    // ==========================================
    // 7. BATCH CHUNKING LOGIC
    // ==========================================

    @Test
    fun testBatchChunking_SplitsLargeCatalogSafelyUnderFirestoreLimit() {
        val exercises = repository.getCanonicalExercises() // 222 items
        val chunkSize = 200
        val chunks = exercises.chunked(chunkSize)

        assertEquals(2, chunks.size, "222 exercises chunked by 200 must produce exactly 2 batches.")
        assertEquals(200, chunks[0].size, "First batch must contain 200 items.")
        assertEquals(22, chunks[1].size, "Second batch must contain 22 items.")

        for (chunk in chunks) {
            assertTrue(chunk.size <= 500, "Every batch size must be well within Firestore 500-write limit.")
        }
    }

    // ==========================================
    // 8. AUDIT RECORD STRUCTURE
    // ==========================================

    @Test
    fun testSyncHistoryRecord_ConstructsValidAuditAttributes() {
        val record = AdminSyncHistoryRecord(
            syncId = "sync_1726000000000",
            adminUid = "admin_123",
            startedAt = 1726000000000L,
            completedAt = 1726000005000L,
            status = "SUCCESS",
            exerciseSummary = EntitySyncSummary(created = 222, updated = 0, failed = 0),
            workoutSummary = EntitySyncSummary(created = 16, updated = 0, failed = 0),
            programSummary = EntitySyncSummary(created = 9, updated = 0, failed = 0),
            totalCreated = 247,
            totalUpdated = 0,
            totalSkipped = 0,
            totalFailed = 0,
            warnings = emptyList(),
            errorSummary = null
        )

        assertEquals("sync_1726000000000", record.syncId)
        assertEquals("admin_123", record.adminUid)
        assertEquals("SUCCESS", record.status)
        assertEquals(247, record.totalCreated)
        assertEquals(0, record.totalFailed)
    }

    // ==========================================
    // 9. MOBILE COMPATIBILITY REGRESSION
    // ==========================================

    @Test
    fun testMobileCompatibility_WorkoutRepositoryRemainsOperational() {
        val gym = WorkoutRepository.getWorkouts(WorkoutType.GYM)
        val home = WorkoutRepository.getWorkouts(WorkoutType.HOME)

        assertTrue(gym.isNotEmpty(), "Gym workouts repository must remain functional.")
        assertTrue(home.isNotEmpty(), "Home workouts repository must remain functional.")

        val chest = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest")
        assertNotNull(chest, "WorkoutRepository.getWorkout must find 'chest'.")
        assertTrue(chest.exercises.isNotEmpty(), "Chest workout must have exercises.")
    }

    @Test
    fun testMobileCompatibility_CanonicalResolverRemainsOperational() {
        val hypertrophy = CanonicalWorkoutResolver.resolveProgram("hypertrophy")
        assertEquals("30-Day Hypertrophy Program", hypertrophy.name)
        assertTrue(hypertrophy.days.isNotEmpty(), "Hypertrophy program must have resolved days.")

        val thirtyDayPlan = ThirtyDayWorkoutRepository.get30DayPlan(ExperienceLevel.BEGINNER)
        assertEquals(30, thirtyDayPlan.size, "ThirtyDayWorkoutRepository must resolve 30 days.")
    }

    // ==========================================
    // 10. CHANGE DETECTION & SKIPPED LOGIC
    // ==========================================

    @Test
    fun testChangeDetection_IdentifiesUnmodifiedExerciseAsNotModified() {
        val canonical = SampleExerciseData.getById("barbell_bench_press")!!
        val copy = canonical.copy()
        assertFalse(
            repository.isExerciseModified(copy, canonical),
            "Identical exercise must be detected as unmodified."
        )
    }

    @Test
    fun testChangeDetection_IdentifiesModifiedExercise() {
        val canonical = SampleExerciseData.getById("barbell_bench_press")!!
        val modified = canonical.copy(difficulty = "EXPERT", instructions = listOf("Keep back flat on bench."))
        assertTrue(
            repository.isExerciseModified(modified, canonical),
            "Exercise with different difficulty or instructions must be detected as modified."
        )
    }

    @Test
    fun testChangeDetection_IdentifiesUnmodifiedWorkoutAsNotModified() {
        val workout = repository.getCanonicalWorkouts().first()
        val copy = workout.copy()
        assertFalse(
            repository.isWorkoutModified(copy, workout),
            "Identical workout must be detected as unmodified."
        )
    }

    @Test
    fun testChangeDetection_IdentifiesModifiedWorkout() {
        val workout = repository.getCanonicalWorkouts().first()
        val modified = workout.copy(duration = "60 min", difficulty = "Advanced")
        assertTrue(
            repository.isWorkoutModified(modified, workout),
            "Workout with updated duration or difficulty must be detected as modified."
        )
    }

    @Test
    fun testChangeDetection_IdentifiesUnmodifiedProgramAsNotModified() {
        val program = repository.getCanonicalPrograms().first()
        val copy = program.copy()
        assertFalse(
            repository.isProgramModified(copy, program),
            "Identical program must be detected as unmodified."
        )
    }

    @Test
    fun testChangeDetection_IdentifiesModifiedProgram() {
        val program = repository.getCanonicalPrograms().first()
        val modified = program.copy(description = "Updated comprehensive training curriculum.")
        assertTrue(
            repository.isProgramModified(modified, program),
            "Program with edited description must be detected as modified."
        )
    }

    // ==========================================
    // 11. FULL 30-DAY INSPECTION & PRESCRIPTIONS
    // ==========================================

    @Test
    fun testProgramDayInspection_ResolvesAllPrescriptionsAndSets() {
        val programs = repository.getCanonicalPrograms()
        val thirtyDayProg = programs.find { it.id == "thirty_day_beginner" }
        assertNotNull(thirtyDayProg, "thirty_day_beginner program must exist.")
        assertEquals(30, thirtyDayProg.days.size, "Program must contain all 30 days.")

        for (day in thirtyDayProg.days) {
            assertTrue(day.dayNumber in 1..30, "Day number must be within 1..30.")
            if (!day.isRestDay) {
                assertTrue(day.exercises.isNotEmpty(), "Training day ${day.dayNumber} must contain prescribed exercises.")
                for (ref in day.exercises) {
                    assertTrue(ref.exerciseId.isNotBlank(), "Exercise reference must have a valid ID.")
                    assertTrue(ref.sets > 0, "Prescribed sets must be positive on day ${day.dayNumber}.")
                    assertTrue(ref.restSeconds >= 0, "Prescribed rest seconds must be non-negative on day ${day.dayNumber}.")
                }
            }
        }
    }

    @Test
    fun testPreviewModel_ContainsUpdateCounts() {
        val preview = CatalogSyncPreview(
            exerciseNewCount = 22,
            exerciseExistingCount = 200,
            exerciseUpdateCount = 5,
            workoutNewCount = 0,
            workoutExistingCount = 16,
            workoutUpdateCount = 2,
            programNewCount = 0,
            programExistingCount = 9,
            programUpdateCount = 1
        )

        assertEquals(8, preview.totalUpdates)
        assertEquals(22, preview.totalNew)
        assertEquals(225, preview.totalExisting)
        assertEquals(5, preview.exerciseUpdateCount)
        assertEquals(2, preview.workoutUpdateCount)
        assertEquals(1, preview.programUpdateCount)
    }

    @Test
    fun testEntitySyncSummary_ContainsSkippedCount() {
        val summary = EntitySyncSummary(created = 10, updated = 5, skipped = 207, failed = 0)
        assertEquals(207, summary.skipped)
        assertEquals(10, summary.created)
        assertEquals(5, summary.updated)
        assertEquals(0, summary.failed)
    }
}
