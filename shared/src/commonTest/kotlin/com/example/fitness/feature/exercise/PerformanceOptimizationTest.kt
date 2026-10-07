package com.example.fitness.feature.exercise

import com.example.fitness.core.perf.AppPerfTracker
import com.example.fitness.feature.exercise.data.FirebaseExerciseRepository
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseMedia
import com.example.fitness.feature.progress.data.WorkoutProgressService
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Performance Optimization Test Suite for ATHGYM.
 * Validates the in-memory caching, startup acceleration, canonical revision stability,
 * and performance tracking milestones.
 */
class PerformanceOptimizationTest {

    @BeforeTest
    fun setUp() {
        FirebaseExerciseRepository.clearCache()
        SampleExerciseData.resetOverridesForTesting()
        WorkoutProgressService.clear()
        AppPerfTracker.reset()
    }

    @AfterTest
    fun tearDown() {
        FirebaseExerciseRepository.clearCache()
        SampleExerciseData.resetOverridesForTesting()
        WorkoutProgressService.clear()
        AppPerfTracker.reset()
    }

    @Test
    fun test01_appPerfTrackerRecordsLatency() {
        AppPerfTracker.start("TEST_STARTUP")
        val elapsed = AppPerfTracker.getElapsed("TEST_STARTUP")
        assertNotNull(elapsed)
        assertTrue(elapsed >= 0L)

        val recorded = AppPerfTracker.record("TEST_STARTUP", "MILESTONE_1")
        assertTrue(recorded >= 0L)

        val finished = AppPerfTracker.finish("TEST_STARTUP", "COMPLETE")
        assertTrue(finished >= 0L)
        assertNull(AppPerfTracker.getElapsed("TEST_STARTUP"))
    }

    @Test
    fun test02_firebaseExerciseRepositoryInMemCacheHits() = runBlocking {
        val testEx = Exercise(
            id = "bench_press_custom",
            name = "Bench Press Custom",
            media = ExerciseMedia(videoUrl = "https://cdn.example.com/bench.mp4")
        )

        // Cache the exercise directly
        FirebaseExerciseRepository.cacheExercise(testEx)

        // Repository should return the cached instance immediately without remote queries
        val repo = FirebaseExerciseRepository()
        val retrieved = repo.getExercise("bench_press_custom")

        assertNotNull(retrieved)
        assertEquals("bench_press_custom", retrieved.id)
        assertEquals("https://cdn.example.com/bench.mp4", retrieved.media.videoUrl)
    }

    @Test
    fun test03_sampleExerciseDataWithVideoReturnsImmediately() = runBlocking {
        val canonicalEx = Exercise(
            id = "incline_press_canon",
            name = "Incline Press Canon",
            media = ExerciseMedia(videoUrl = "https://cdn.example.com/incline.mp4")
        )
        SampleExerciseData.updateCanonicalExercise(canonicalEx)

        val repo = FirebaseExerciseRepository()
        val retrieved = repo.getExercise("incline_press_canon")

        assertNotNull(retrieved)
        assertEquals("https://cdn.example.com/incline.mp4", retrieved.media.videoUrl)
    }

    @Test
    fun test04_cacheInvalidationOnArchiveAndDelete() = runBlocking {
        val testEx = Exercise(
            id = "deadlift_test",
            name = "Deadlift Test",
            media = ExerciseMedia(videoUrl = "https://cdn.example.com/deadlift.mp4")
        )
        FirebaseExerciseRepository.cacheExercise(testEx)

        // Invalidate cache
        FirebaseExerciseRepository.invalidateCache("deadlift_test")

        // Deleted or non-cached without canonical should return null or fallback
        SampleExerciseData.deleteCanonicalExercise("deadlift_test")
        val repo = FirebaseExerciseRepository()
        val retrieved = repo.getExercise("deadlift_test")
        assertNull(retrieved)
    }

    @Test
    fun test05_canonicalRevisionStabilityOnUnchangedRegistration() {
        val testEx = Exercise(
            id = "squat_stable",
            name = "Squat Stable",
            media = ExerciseMedia(videoUrl = "https://cdn.example.com/squat.mp4")
        )
        SampleExerciseData.updateCanonicalExercise(testEx)
        val rev1 = SampleExerciseData.canonicalRevision

        // Register the exact same exercise again
        SampleExerciseData.registerCanonicalExercises(listOf(testEx))
        val rev2 = SampleExerciseData.canonicalRevision

        // Revision should not increment when there are no actual updates!
        assertEquals(rev1, rev2, "Canonical revision must not increment for redundant exercise registrations")
    }

    @Test
    fun test06_workoutHistoryCachedEvenWhenEmpty() = runBlocking {
        WorkoutProgressService.lastLoadedUserId = "user_123"
        // workoutHistory is empty

        // Calling loadWorkoutHistory with forceRefresh = false should return success immediately
        val result = WorkoutProgressService.loadWorkoutHistory("user_123", forceRefresh = false)
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }
}
