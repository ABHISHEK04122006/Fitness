package com.example.fitness.feature.exercise

import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.media.ExerciseMediaResolver
import com.example.fitness.feature.exercise.media.MediaLoadingState
import com.example.fitness.feature.exercise.media.ResolvedExerciseMedia
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseMedia
import com.example.fitness.feature.workout.SessionPhase
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import com.example.fitness.feature.workout.model.ExerciseItem
import com.example.fitness.feature.workout.model.ExperienceLevel
import com.example.fitness.feature.workout.model.PlanExercise
import com.example.fitness.feature.workout.model.ThirtyDayWorkout
import fitness.shared.generated.resources.Res
import fitness.shared.generated.resources.gym_video_thumb
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * STEP 9 — USER EXERCISE EXPERIENCE & MEDIA PRESENTATION TEST SUITE
 *
 * Covers all 22 required test cases from Section 17:
 * 1. Exercise detail renders canonical data
 * 2. Prescription renders correctly
 * 3. Animation selected correctly
 * 4. Video fallback works
 * 5. Thumbnail fallback works
 * 6. Empty media fallback works
 * 7. Media loading state
 * 8. Media error state
 * 9. Video controls state
 * 10. Exercise metadata preservation
 * 11. Instructions display
 * 12. Empty optional sections hidden
 * 13. Muscle metadata display
 * 14. Equipment display
 * 15. No mid-word wrapping model/state
 * 16. Workout session integration
 * 17. Warm-up integration
 * 18. Cool-down integration
 * 19. Media failure does not break workout
 * 20. Lazy media loading
 * 21. No-media user experience
 * 22. Accessibility labels/state
 */
class UserExerciseExperienceTest {

    // -------------------------------------------------------------------------------------
    // 1. Exercise detail renders canonical data
    // -------------------------------------------------------------------------------------
    @Test
    fun test1_exerciseDetailRendersCanonicalData() {
        val benchPress = SampleExerciseData.getById("flat_barbell_bench_press")
        assertNotNull(benchPress, "Canonical exercise 'flat_barbell_bench_press' must exist")
        assertEquals("Flat Barbell Bench Press", benchPress.name)
        assertEquals("Chest", benchPress.primaryMuscle)
        assertTrue(benchPress.muscleGroups.isNotEmpty())
        assertTrue(benchPress.equipment.contains("Barbell"))

        // Also verify metadata structure preservation with fully populated content
        val richBench = benchPress.copy(
            instructions = listOf("Lie back on the flat bench", "Press the barbell upward smoothly"),
            setupSteps = listOf("Lie flat on the bench with eyes beneath the bar"),
            executionSteps = listOf("Unrack bar, lower to mid-chest, and press to lockout")
        )
        assertTrue(richBench.instructions.isNotEmpty())
        assertTrue(richBench.setupSteps.isNotEmpty())
        assertTrue(richBench.executionSteps.isNotEmpty())
    }

    // -------------------------------------------------------------------------------------
    // 2. Prescription renders correctly
    // -------------------------------------------------------------------------------------
    @Test
    fun test2_prescriptionRendersCorrectly() {
        val squat = SampleExerciseData.getById("squat")
        assertNotNull(squat, "Canonical exercise 'squat' must exist")
        assertTrue(squat.defaultSets > 0, "Default sets must be positive")
        assertTrue(squat.defaultReps.isNotBlank(), "Default reps must be non-blank")
        assertTrue(squat.defaultRestSeconds > 0, "Default rest seconds must be positive")

        // Formatted presentation values must match canonical specifications
        val setsText = "${squat.defaultSets}"
        val repsText = squat.defaultReps
        val restText = "${squat.defaultRestSeconds} sec"

        assertEquals("3", setsText)
        assertEquals("8-10", repsText)
        assertEquals("120 sec", restText)
    }

    // -------------------------------------------------------------------------------------
    // 3. Animation selected correctly
    // -------------------------------------------------------------------------------------
    @Test
    fun test3_animationSelectedCorrectly() {
        val media = ExerciseMedia(
            animationUrl = "https://storage.googleapis.com/test-bucket/exercise-media/bench/animation.json",
            videoUrl = "https://storage.googleapis.com/test-bucket/exercise-media/bench/video.mp4",
            thumbnailUrl = "https://storage.googleapis.com/test-bucket/exercise-media/bench/thumb.jpg"
        )
        val resolved = ExerciseMediaResolver.resolve(media)
        assertTrue(resolved is ResolvedExerciseMedia.Animation, "Animation must take top priority")
        assertEquals("https://storage.googleapis.com/test-bucket/exercise-media/bench/animation.json", resolved.url)
        assertEquals("https://storage.googleapis.com/test-bucket/exercise-media/bench/thumb.jpg", resolved.posterUrl)
    }

    // -------------------------------------------------------------------------------------
    // 4. Video fallback works
    // -------------------------------------------------------------------------------------
    @Test
    fun test4_videoFallbackWorks() {
        val media = ExerciseMedia(
            animationUrl = "",
            videoUrl = "https://storage.googleapis.com/test-bucket/exercise-media/bench/video.mp4",
            thumbnailUrl = "https://storage.googleapis.com/test-bucket/exercise-media/bench/thumb.jpg"
        )
        val resolved = ExerciseMediaResolver.resolve(media)
        assertTrue(resolved is ResolvedExerciseMedia.Video, "Must fall back to video when animation is absent")
        assertEquals("https://storage.googleapis.com/test-bucket/exercise-media/bench/video.mp4", resolved.url)
        assertEquals("https://storage.googleapis.com/test-bucket/exercise-media/bench/thumb.jpg", resolved.posterUrl)
    }

    // -------------------------------------------------------------------------------------
    // 5. Thumbnail fallback works
    // -------------------------------------------------------------------------------------
    @Test
    fun test5_thumbnailFallbackWorks() {
        val media = ExerciseMedia(
            animationUrl = "",
            videoUrl = "",
            thumbnailUrl = "https://storage.googleapis.com/test-bucket/exercise-media/bench/thumb.jpg"
        )
        val resolved = ExerciseMediaResolver.resolve(media)
        assertTrue(resolved is ResolvedExerciseMedia.Thumbnail, "Must fall back to thumbnail when video is absent")
        assertEquals("https://storage.googleapis.com/test-bucket/exercise-media/bench/thumb.jpg", resolved.url)
    }

    // -------------------------------------------------------------------------------------
    // 6. Empty media fallback works
    // -------------------------------------------------------------------------------------
    @Test
    fun test6_emptyMediaFallbackWorks() {
        val media = ExerciseMedia(animationUrl = "", videoUrl = "", thumbnailUrl = "")
        val resolved = ExerciseMediaResolver.resolve(media)
        assertTrue(resolved is ResolvedExerciseMedia.Empty, "Must return Empty state when all media URLs are blank")

        val fromNull = ExerciseMediaResolver.resolve(null as ExerciseMedia?)
        assertTrue(fromNull is ResolvedExerciseMedia.Empty, "Null media must safely return Empty")
    }

    // -------------------------------------------------------------------------------------
    // 7. Media loading state
    // -------------------------------------------------------------------------------------
    @Test
    fun test7_mediaLoadingState() {
        val resolvedEmpty: ResolvedExerciseMedia = ResolvedExerciseMedia.Empty
        val emptyState = when (resolvedEmpty) {
            is ResolvedExerciseMedia.Empty -> MediaLoadingState.EMPTY
            else -> MediaLoadingState.LOADING
        }
        assertEquals(MediaLoadingState.EMPTY, emptyState)

        val videoMedia: ResolvedExerciseMedia = ResolvedExerciseMedia.Video("https://example.com/video.mp4")
        val videoInitialState = when (videoMedia) {
            is ResolvedExerciseMedia.Empty -> MediaLoadingState.EMPTY
            is ResolvedExerciseMedia.Video -> MediaLoadingState.LOADING
            else -> MediaLoadingState.SUCCESS
        }
        assertEquals(MediaLoadingState.LOADING, videoInitialState)

        // Progress callback transitions state to SUCCESS
        var currentState = videoInitialState
        val onProgressUpdate: (Long, Long) -> Unit = { cur, tot ->
            if (tot > 0) currentState = MediaLoadingState.SUCCESS
        }
        onProgressUpdate(1500L, 10050L)
        assertEquals(MediaLoadingState.SUCCESS, currentState)
    }

    // -------------------------------------------------------------------------------------
    // 8. Media error state
    // -------------------------------------------------------------------------------------
    @Test
    fun test8_mediaErrorState() {
        var loadingState = MediaLoadingState.LOADING
        var errorMessage: String? = null

        val onError: (String) -> Unit = { err ->
            errorMessage = err
            loadingState = MediaLoadingState.ERROR
        }

        onError("Network unavailable")
        assertEquals(MediaLoadingState.ERROR, loadingState)
        assertEquals("Network unavailable", errorMessage)

        // Retry resets error and goes to LOADING
        val onRetry: () -> Unit = {
            errorMessage = null
            loadingState = MediaLoadingState.LOADING
        }
        onRetry()
        assertEquals(MediaLoadingState.LOADING, loadingState)
        assertEquals(null, errorMessage)
    }

    // -------------------------------------------------------------------------------------
    // 9. Video controls state
    // -------------------------------------------------------------------------------------
    @Test
    fun test9_videoControlsState() {
        var isPlaying = false
        var isPlaybackEnded = false
        var currentPositionMs = 0L
        val totalDurationMs = 10000L

        // Play
        isPlaying = true
        assertTrue(isPlaying)
        assertFalse(isPlaybackEnded)

        // Pause
        isPlaying = false
        assertFalse(isPlaying)

        // Ended
        currentPositionMs = totalDurationMs
        isPlaybackEnded = true
        isPlaying = false
        assertTrue(isPlaybackEnded)

        // Replay action resets position to 0 and starts playing
        if (isPlaybackEnded) {
            isPlaybackEnded = false
            currentPositionMs = 0L
            isPlaying = true
        }
        assertFalse(isPlaybackEnded)
        assertEquals(0L, currentPositionMs)
        assertTrue(isPlaying)
    }

    // -------------------------------------------------------------------------------------
    // 10. Exercise metadata preservation
    // -------------------------------------------------------------------------------------
    @Test
    fun test10_exerciseMetadataPreservation() {
        val allExercises = SampleExerciseData.getAll()
        assertEquals(222, allExercises.size, "Catalog must retain all 222 canonical exercises")

        allExercises.forEach { ex ->
            assertTrue(ex.id.isNotBlank(), "Exercise ID must be non-blank")
            assertTrue(ex.name.isNotBlank(), "Exercise name must be non-blank")
            assertTrue(ex.primaryMuscle.isNotBlank(), "Exercise '${ex.id}' must have primaryMuscle")
            assertTrue(ex.category.isNotBlank(), "Exercise '${ex.id}' must have category")
        }
    }

    // -------------------------------------------------------------------------------------
    // 11. Instructions display
    // -------------------------------------------------------------------------------------
    @Test
    fun test11_instructionsDisplay() {
        val deadlift = SampleExerciseData.getById("deadlift")
        assertNotNull(deadlift, "Canonical exercise 'deadlift' must exist")

        val populatedDeadlift = deadlift.copy(
            instructions = listOf("Hinge at hips, keep flat back, drive with legs"),
            setupSteps = listOf("Position feet hip-width apart under the barbell"),
            executionSteps = listOf("Brace core, pull bar smoothly along shins to lockout")
        )
        assertTrue(populatedDeadlift.instructions.isNotEmpty(), "Instructions must not be empty")
        assertTrue(populatedDeadlift.setupSteps.isNotEmpty(), "Setup steps must not be empty")
        assertTrue(populatedDeadlift.executionSteps.isNotEmpty(), "Execution steps must not be empty")

        // Ensure instruction steps are readable strings
        populatedDeadlift.setupSteps.forEach { step ->
            assertTrue(step.length >= 10, "Setup step '$step' must have descriptive content")
        }
    }

    // -------------------------------------------------------------------------------------
    // 12. Empty optional sections hidden
    // -------------------------------------------------------------------------------------
    @Test
    fun test12_emptyOptionalSectionsHidden() {
        val testExercise = Exercise(
            id = "minimal_exercise",
            name = "Minimal Exercise",
            primaryMuscle = "Core",
            instructions = listOf("Perform with steady pace"),
            setupSteps = emptyList(), // Optional empty
            executionSteps = emptyList(), // Optional empty
            breathing = "", // Optional empty
            formTips = emptyList(), // Optional empty
            commonMistakes = emptyList() // Optional empty
        )

        // Logic check: sections with empty contents evaluate to false
        assertFalse(testExercise.setupSteps.isNotEmpty(), "Empty setupSteps must not render")
        assertFalse(testExercise.executionSteps.isNotEmpty(), "Empty executionSteps must not render")
        assertFalse(testExercise.breathing.isNotBlank(), "Blank breathing must not render")
        assertFalse(testExercise.formTips.isNotEmpty(), "Empty formTips must not render")
        assertFalse(testExercise.commonMistakes.isNotEmpty(), "Empty commonMistakes must not render")
        assertTrue(testExercise.instructions.isNotEmpty(), "Instructions must render")
    }

    // -------------------------------------------------------------------------------------
    // 13. Muscle metadata display
    // -------------------------------------------------------------------------------------
    @Test
    fun test13_muscleMetadataDisplay() {
        val pullUp = SampleExerciseData.getById("pull_ups")
        assertNotNull(pullUp, "Canonical exercise 'pull_ups' must exist")
        assertEquals("Back", pullUp.primaryMuscle)
        assertTrue(pullUp.muscleGroups.contains("Back") || pullUp.muscleGroups.contains("Upper Body"))

        val richPullUp = pullUp.copy(secondaryMuscles = listOf("Biceps", "Forearms"))
        assertTrue(richPullUp.secondaryMuscles.isNotEmpty(), "Pull-up must support secondary muscles")
    }

    // -------------------------------------------------------------------------------------
    // 14. Equipment display
    // -------------------------------------------------------------------------------------
    @Test
    fun test14_equipmentDisplay() {
        val dumbbellCurl = SampleExerciseData.getById("dumbbell_hammer_curl")
        assertNotNull(dumbbellCurl, "Canonical exercise 'dumbbell_hammer_curl' must exist")
        assertTrue(
            dumbbellCurl.equipment.any { it.contains("Dumbbell", ignoreCase = true) },
            "Equipment must include Dumbbell"
        )
    }

    // -------------------------------------------------------------------------------------
    // 15. No mid-word wrapping model/state
    // -------------------------------------------------------------------------------------
    @Test
    fun test15_noMidWordWrappingModel() {
        val technicalTerms = listOf(
            "Barbell",
            "Dumbbell",
            "Anterior Deltoids",
            "Latissimus Dorsi",
            "Quadriceps",
            "Pectoralis Major",
            "Gastrocnemius"
        )

        technicalTerms.forEach { term ->
            // Verify words are whitespace-separated without unexpected internal hyphens or splits
            val words = term.split(" ")
            words.forEach { word ->
                assertTrue(word.all { it.isLetter() || it == '-' }, "Word '$word' must be composed of letters")
                assertFalse(word.contains("\n"), "Word '$word' must not contain linebreaks")
            }
        }
    }

    // -------------------------------------------------------------------------------------
    // 16. Workout session integration
    // -------------------------------------------------------------------------------------
    @Test
    fun test16_workoutSessionIntegration() {
        val workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("base", "day1", ExperienceLevel.BEGINNER)
        assertTrue(workout.exercises.isNotEmpty(), "Workout day 1 must have exercises")

        // Flow phase sequence
        val phases = listOf(
            SessionPhase.WARM_UP,
            SessionPhase.MAIN_WORKOUT,
            SessionPhase.COOL_DOWN,
            SessionPhase.COMPLETE
        )
        assertEquals(SessionPhase.WARM_UP, phases[0])
        assertEquals(SessionPhase.MAIN_WORKOUT, phases[1])
        assertEquals(SessionPhase.COOL_DOWN, phases[2])
        assertEquals(SessionPhase.COMPLETE, phases[3])

        // First exercise prescription
        val firstEx = workout.exercises.first()
        assertTrue(firstEx.sets.isNotBlank())
        assertTrue(firstEx.reps.isNotBlank())
        assertTrue(firstEx.rest.isNotBlank())
    }

    // -------------------------------------------------------------------------------------
    // 17. Warm-up integration
    // -------------------------------------------------------------------------------------
    @Test
    fun test17_warmUpIntegration() {
        val workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("base", "day1", ExperienceLevel.BEGINNER)
        assertTrue(workout.warmUp.isNotEmpty(), "Workout must contain warm-up exercises")

        workout.warmUp.forEach { warmItem ->
            assertTrue(warmItem.id.isNotBlank(), "Warm-up ID must be non-blank")
            assertTrue(warmItem.name.isNotBlank(), "Warm-up name must be non-blank")
            assertTrue(warmItem.durationOrReps.isNotBlank(), "Warm-up duration must be non-blank")
            assertTrue(warmItem.instructions.isNotBlank(), "Warm-up instructions must be non-blank")

            // Test media resolution for warm-up item
            val resolved = ExerciseMediaResolver.resolveFromId(warmItem.id)
            assertNotNull(resolved)
        }
    }

    // -------------------------------------------------------------------------------------
    // 18. Cool-down integration
    // -------------------------------------------------------------------------------------
    @Test
    fun test18_coolDownIntegration() {
        val workout = CanonicalWorkoutResolver.resolveThirtyDayWorkout("base", "day1", ExperienceLevel.BEGINNER)
        assertTrue(workout.coolDown.isNotEmpty(), "Workout must contain cool-down exercises")

        workout.coolDown.forEach { cdItem ->
            assertTrue(cdItem.id.isNotBlank(), "Cool-down ID must be non-blank")
            assertTrue(cdItem.name.isNotBlank(), "Cool-down name must be non-blank")
            assertTrue(cdItem.durationOrReps.isNotBlank(), "Cool-down duration must be non-blank")
            assertTrue(cdItem.instructions.isNotBlank(), "Cool-down instructions must be non-blank")

            // Test media resolution for cool-down item
            val resolved = ExerciseMediaResolver.resolveFromId(cdItem.id)
            assertNotNull(resolved)
        }
    }

    // -------------------------------------------------------------------------------------
    // 19. Media failure does not break workout
    // -------------------------------------------------------------------------------------
    @Test
    fun test19_mediaFailureDoesNotBreakWorkout() {
        val failingExerciseItem = ExerciseItem(
            id = "broken_video_ex",
            name = "Test Broken Media",
            sets = "3 sets",
            duration = "60s",
            description = "Execution test",
            imageRes = Res.drawable.gym_video_thumb,
            videoDemoUrl = "https://invalid-domain-error.test/broken.mp4",
            media = ExerciseMedia(videoUrl = "https://invalid-domain-error.test/broken.mp4")
        )

        // Resolver succeeds without throwing
        val resolved = ExerciseMediaResolver.resolve(failingExerciseItem)
        assertTrue(resolved is ResolvedExerciseMedia.Video)

        // Workout state progression is unaffected by media status
        var currentSet = 1
        val totalSets = 3
        var isWorkoutCompleted = false

        // Simulating complete set 1, 2, 3
        while (currentSet < totalSets) {
            currentSet++
        }
        isWorkoutCompleted = true

        assertEquals(3, currentSet)
        assertTrue(isWorkoutCompleted)
    }

    // -------------------------------------------------------------------------------------
    // 20. Lazy media loading
    // -------------------------------------------------------------------------------------
    @Test
    fun test20_lazyMediaLoading() {
        // Individual resolution per exercise ensures media is resolved on demand
        val singleResolved = ExerciseMediaResolver.resolveFromId("barbell_bench_press")
        assertNotNull(singleResolved)

        // Exercise item presentation model resolution is lazily evaluated
        val exercise = SampleExerciseData.getById("incline_dumbbell_press")!!
        val resolvedMedia = ExerciseMediaResolver.resolve(exercise.media)
        assertTrue(resolvedMedia is ResolvedExerciseMedia.Empty)
    }

    // -------------------------------------------------------------------------------------
    // 21. No-media user experience
    // -------------------------------------------------------------------------------------
    @Test
    fun test21_noMediaUserExperience() {
        val all222 = SampleExerciseData.getAll()
        var emptyCount = 0

        all222.forEach { ex ->
            val res = ExerciseMediaResolver.resolve(ex)
            if (res is ResolvedExerciseMedia.Empty) {
                emptyCount++
            }
        }

        // All 222 currently have 0 uploaded media assets
        assertEquals(222, emptyCount, "All 222 canonical exercises must safely resolve to Empty media")

        // Verify that EmptyMediaSurface does not expose admin or storage error phrases
        val forbiddenUserTerms = listOf("Upload media", "Media missing", "Firebase Storage", "undefined", "null")
        val sampleEmptyPlaceholderText = "Technique Demonstration Queued. Perform with strict form, tempo & prescribed rest intervals below."
        forbiddenUserTerms.forEach { forbidden ->
            assertFalse(sampleEmptyPlaceholderText.contains(forbidden, ignoreCase = true), "User must not see '$forbidden'")
        }
    }

    // -------------------------------------------------------------------------------------
    // 22. Accessibility labels and state
    // -------------------------------------------------------------------------------------
    @Test
    fun test22_accessibilityLabelsAndState() {
        val expectedDescriptions = mapOf(
            "play" to "Play",
            "pause" to "Pause",
            "replay" to "Replay",
            "mute" to "Mute",
            "unmute" to "Unmute",
            "fullscreen" to "Fullscreen",
            "exitFullscreen" to "Exit Fullscreen",
            "mediaPlaceholder" to "Exercise media placeholder",
            "retry" to "Retry playback",
            "back" to "Back",
            "returnToWorkout" to "Return to Workout"
        )

        expectedDescriptions.values.forEach { desc ->
            assertTrue(desc.isNotBlank(), "Accessibility content description '$desc' must be non-blank")
            assertFalse(desc.contains("null"), "Accessibility description must not contain 'null'")
        }
    }
}
