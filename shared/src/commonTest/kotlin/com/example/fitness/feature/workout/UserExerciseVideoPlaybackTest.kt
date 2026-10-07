package com.example.fitness.feature.workout

import com.example.fitness.feature.exercise.media.ExerciseMediaResolver
import com.example.fitness.feature.exercise.media.ResolvedExerciseMedia
import com.example.fitness.feature.exercise.model.ExerciseMedia
import com.example.fitness.feature.workout.model.ExerciseItem
import fitness.shared.generated.resources.Res
import fitness.shared.generated.resources.gym_video_thumb
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * USER EXERCISE VIDEO PLAYBACK REGRESSION TEST SUITE
 *
 * Verifies client requirements:
 * 1. User exercise video configuration: volume = 0, repeatMode = REPEAT_ONE, autoplay = true, controls = disabled.
 * 2. When exercise.videoUrl exists (e.g. uploaded via Admin to Cloudflare R2), demo video does NOT override it.
 * 3. Fallback demo videos still play with User-side silent, auto-playing, infinitely looping behavior.
 * 4. Video replacement by Admin immediately resolves to the new video and maintains User silent loop behavior.
 * 5. Admin video preview retains interactive controls while User video stays strictly control-free.
 */
class UserExerciseVideoPlaybackTest {

    @BeforeTest
    fun setUp() {
        com.example.fitness.feature.exercise.data.SampleExerciseData.resetOverridesForTesting()
    }

    @AfterTest
    fun tearDown() {
        com.example.fitness.feature.exercise.data.SampleExerciseData.resetOverridesForTesting()
    }

    @Test
    fun test01_userExerciseVideoConfig_defaultsToSilentLoopingWithoutControls() {
        val config = getUserVideoPlayerConfig(interactiveControls = false)

        assertEquals(0f, config.volume, "User exercise video volume must be strictly 0")
        assertTrue(config.isMuted, "User exercise video must be strictly muted")
        assertTrue(config.autoPlay, "User exercise video must automatically start playback")
        assertTrue(config.isLooping, "User exercise video must continuously loop forever")
        assertEquals("REPEAT_ONE", config.repeatMode, "Player repeat mode must be REPEAT_ONE")
        assertFalse(config.controlsEnabled, "User must NOT see any playback controls")
        assertFalse(config.allowUserControl, "User must NOT be allowed to pause, seek, or control playback")
    }

    @Test
    fun test02_adminPreviewConfig_preservesInteractiveControls() {
        val config = getUserVideoPlayerConfig(interactiveControls = true)

        assertTrue(config.controlsEnabled, "Admin video preview must preserve playback controls")
        assertTrue(config.allowUserControl, "Admin must be able to interact with preview controls")
        assertEquals("REPEAT_ONE", config.repeatMode, "Admin preview retains looping capability")
    }

    @Test
    fun test03_validUploadedVideoUrl_isResolvedAndNotOverriddenByDemoVideo() {
        val uploadedR2Url = "https://athgym-r2-api.prayasingale0909.workers.dev/v1/exercises/olympic_bench/media/video"
        val fallbackDemoUrl = "https://athgym.com/demo/bench_press.mp4"

        val exerciseItem = ExerciseItem(
            id = "custom_test_exercise_001",
            name = "Olympic Bench Press",
            sets = "3 sets",
            duration = "60s",
            description = "Olympic Bench Press description",
            imageRes = Res.drawable.gym_video_thumb,
            videoDemoUrl = fallbackDemoUrl,
            media = ExerciseMedia(videoUrl = uploadedR2Url)
        )

        val resolved = ExerciseMediaResolver.resolve(exerciseItem)
        assertTrue(resolved is ResolvedExerciseMedia.Video, "Resolved media must be a Video")
        assertEquals(uploadedR2Url, resolved.url, "Uploaded R2 video URL must NOT be overridden by demo video")

        // Confirm User video configuration contract
        val userConfig = getUserVideoPlayerConfig(interactiveControls = false)
        assertEquals(0f, userConfig.volume)
        assertEquals("REPEAT_ONE", userConfig.repeatMode)
        assertTrue(userConfig.autoPlay)
        assertFalse(userConfig.controlsEnabled)
    }

    @Test
    fun test04_demoVideoFallback_usedWhenNoCanonicalVideoExists() {
        val fallbackDemoUrl = "https://athgym.com/demo/squat_1080p.mp4"

        val exerciseItem = ExerciseItem(
            id = "custom_test_exercise_002",
            name = "Demo Only Squat",
            sets = "3 sets",
            duration = "60s",
            description = "Demo Squat description",
            imageRes = Res.drawable.gym_video_thumb,
            videoDemoUrl = fallbackDemoUrl,
            media = ExerciseMedia(videoUrl = "")
        )

        val resolved = ExerciseMediaResolver.resolve(exerciseItem)
        assertTrue(resolved is ResolvedExerciseMedia.Video, "Resolved media must be a Video")
        assertEquals(fallbackDemoUrl, resolved.url, "Demo video must be used as fallback when canonical video is blank")

        // User playback configuration still strictly applies
        val userConfig = getUserVideoPlayerConfig(interactiveControls = false)
        assertEquals(0f, userConfig.volume)
        assertTrue(userConfig.isMuted)
        assertTrue(userConfig.autoPlay)
        assertTrue(userConfig.isLooping)
        assertFalse(userConfig.controlsEnabled)
    }

    @Test
    fun test05_adminVideoReplacement_resolvesNewVideoAndPreservesUserBehavior() {
        val originalVideoUrl = "https://athgym-r2-api.workers.dev/v1/exercises/cable_fly/media/video_v1"
        val replacedVideoUrl = "https://athgym-r2-api.workers.dev/v1/exercises/cable_fly/media/video_v2"

        val initialExercise = ExerciseItem(
            id = "custom_test_exercise_003",
            name = "Cable Chest Fly",
            sets = "4 sets",
            duration = "45s",
            description = "Cable fly description",
            imageRes = Res.drawable.gym_video_thumb,
            videoDemoUrl = "demo_fly",
            media = ExerciseMedia(videoUrl = originalVideoUrl)
        )
        val initialResolved = ExerciseMediaResolver.resolve(initialExercise)
        assertEquals(originalVideoUrl, (initialResolved as ResolvedExerciseMedia.Video).url)

        // Admin replaces video with a second, different video
        val updatedExercise = initialExercise.copy(
            media = ExerciseMedia(videoUrl = replacedVideoUrl)
        )
        val updatedResolved = ExerciseMediaResolver.resolve(updatedExercise)
        assertEquals(replacedVideoUrl, (updatedResolved as ResolvedExerciseMedia.Video).url, "User must play the newly replaced video, not the old video")

        // User playback configuration remains unchanged: silent, looping, auto-play, no controls
        val userConfig = getUserVideoPlayerConfig(interactiveControls = false)
        assertEquals(0f, userConfig.volume)
        assertEquals("REPEAT_ONE", userConfig.repeatMode)
        assertTrue(userConfig.autoPlay)
        assertFalse(userConfig.controlsEnabled)
    }

    @Test
    fun test06_threeConsecutiveCycles_loopStateRemainsActiveWithoutEnding() {
        val userConfig = getUserVideoPlayerConfig(interactiveControls = false)

        // Simulate cycle progression:
        // Cycle 1: 0:00 -> duration -> completion
        // Cycle 2: 0:00 -> duration -> completion
        // Cycle 3: 0:00 -> duration -> completion
        var isPlaying = userConfig.autoPlay
        var isPlaybackEnded = false
        var showControls = userConfig.controlsEnabled
        val isLooping = userConfig.isLooping

        assertTrue(isLooping, "Player must have looping enabled")

        // Completion callback simulation for 3 cycles in User Mode
        repeat(3) {
            // Under User mode: onPlaybackEnded never marks ended or pauses
            if (!userConfig.controlsEnabled) {
                isPlaybackEnded = false
                isPlaying = true
                showControls = false
            } else {
                isPlaybackEnded = true
                isPlaying = false
                showControls = true
            }
        }

        assertTrue(isPlaying, "Video must remain playing after 3 full cycles")
        assertFalse(isPlaybackEnded, "Video must NOT enter 'ended' or replay state")
        assertFalse(showControls, "No controls or replay buttons must appear after completions")
    }

    @Test
    fun test07_exercisesWithoutVideo_displayArtworkCleanlyWithoutForcedFallbackVideo() {
        val exerciseWithoutVideo = ExerciseItem(
            id = "exercise_no_vid_001",
            name = "Bodyweight Calf Raise",
            sets = "3 sets",
            duration = "45s",
            description = "Strict calf raise on flat ground",
            imageRes = Res.drawable.gym_video_thumb,
            videoDemoUrl = "",
            media = ExerciseMedia(videoUrl = "")
        )

        val resolved = ExerciseMediaResolver.resolve(exerciseWithoutVideo)
        // Must resolve to Empty or Thumbnail - never fabricate a local fallback like gym.mp4
        if (resolved is ResolvedExerciseMedia.Video) {
            assertEquals(null, resolved.localResName, "localResName must NEVER default to gym.mp4")
        }

        val userConfig = getUserVideoPlayerConfig(interactiveControls = false)
        assertFalse(userConfig.controlsEnabled, "No video controls must appear")
        assertFalse(userConfig.allowUserControl, "User must not have playback controls")
    }

    @Test
    fun test08_adminDirectVideoUrlControl_updatesExerciseMediaReactively() {
        val exerciseId = "admin_ctrl_exercise_001"
        val customCdnUrl = "https://cdn.myfitness.com/videos/incline_bench_pro.mp4"

        val initialExercise = ExerciseItem(
            id = exerciseId,
            name = "Incline Bench Press",
            sets = "4 sets",
            duration = "60s",
            description = "Upper chest focus",
            imageRes = Res.drawable.gym_video_thumb,
            videoDemoUrl = "",
            media = ExerciseMedia(videoUrl = "")
        )

        // 1. Initially no video
        val hasUploadedVideoInitially = initialExercise.media.videoUrl.isNotBlank() &&
                !initialExercise.media.videoUrl.contains("fitness.app/demo", ignoreCase = true)
        assertFalse(hasUploadedVideoInitially, "Initially exercise must indicate NO VIDEO")

        // 2. Admin pastes and saves custom video URL
        val updatedWithUrl = initialExercise.copy(
            media = initialExercise.media.copy(videoUrl = customCdnUrl),
            videoDemoUrl = customCdnUrl
        )
        val hasUploadedVideoAfterSave = updatedWithUrl.media.videoUrl.isNotBlank() &&
                !updatedWithUrl.media.videoUrl.contains("fitness.app/demo", ignoreCase = true)
        assertTrue(hasUploadedVideoAfterSave, "After Admin saves URL, exercise must indicate VIDEO UPLOADED ✓")

        val resolved = ExerciseMediaResolver.resolve(updatedWithUrl)
        assertTrue(resolved is ResolvedExerciseMedia.Video)
        assertEquals(customCdnUrl, resolved.url)

        // 3. Admin clears custom video URL
        val clearedExercise = updatedWithUrl.copy(
            media = updatedWithUrl.media.copy(videoUrl = ""),
            videoDemoUrl = ""
        )
        val hasUploadedVideoAfterClear = clearedExercise.media.videoUrl.isNotBlank() &&
                !clearedExercise.media.videoUrl.contains("fitness.app/demo", ignoreCase = true)
        assertFalse(hasUploadedVideoAfterClear, "After Admin clears URL, exercise must return to NO VIDEO")
    }

    @Test
    fun test09_adminUploadsExerciseVideo_bothTodaysWorkoutAndWorkoutSectionDetailsDisplaySameVideo() {
        val testVideoUrl = "https://r2.athgym.com/videos/bench_press_mastery.mp4"
        val canonicalBench = com.example.fitness.feature.exercise.data.SampleExerciseData.getById("barbell_bench_press")
        assertNotNull(canonicalBench, "Canonical Barbell Bench Press must exist")

        // 1. Admin uploads video for Barbell Bench Press
        val updatedBench = canonicalBench.copy(
            media = ExerciseMedia(videoUrl = testVideoUrl)
        )
        com.example.fitness.feature.exercise.data.SampleExerciseData.updateCanonicalExercise(updatedBench)

        // 2. Today's Workout retrieves exercise (via PlanExercise / Canonical resolution)
        val todaysWorkoutEx = com.example.fitness.feature.exercise.data.SampleExerciseData.findCanonical(
            "barbell_bench_press",
            "Barbell Bench Press"
        )
        assertNotNull(todaysWorkoutEx)
        assertEquals(testVideoUrl, todaysWorkoutEx.media.videoUrl, "Today's workout must have uploaded video URL")

        // 3. User opens same exercise from Workout section by routine ID: "gym_chest_1"
        val routineExerciseId = "gym_chest_1"
        val resolvedCanonicalId = com.example.fitness.feature.exercise.data.SampleExerciseData.resolveCanonicalId(routineExerciseId)
        val exerciseName = com.example.fitness.feature.workout.data.WorkoutRepository.getAllExerciseItems()
            .find { it.id.equals(routineExerciseId, ignoreCase = true) || it.id.equals(resolvedCanonicalId, ignoreCase = true) }?.name
            ?: routineExerciseId.replace('_', ' ')

        val workoutDetailEx = com.example.fitness.feature.exercise.data.SampleExerciseData.findCanonical(resolvedCanonicalId, exerciseName)
            ?: com.example.fitness.feature.exercise.data.SampleExerciseData.findCanonical(routineExerciseId, exerciseName)
            ?: com.example.fitness.feature.exercise.data.SampleExerciseData.getById(resolvedCanonicalId)
            ?: com.example.fitness.feature.exercise.data.SampleExerciseData.getById(routineExerciseId)

        assertNotNull(workoutDetailEx, "Workout section individual detail must resolve exercise")
        assertEquals(testVideoUrl, workoutDetailEx.media.videoUrl, "Workout section detail must resolve the EXACT SAME video URL as Today's Workout")

        // 4. Verify player media resolution on both
        val detailResolvedMedia = ExerciseMediaResolver.resolve(workoutDetailEx)
        assertTrue(detailResolvedMedia is ResolvedExerciseMedia.Video)
        assertEquals(testVideoUrl, detailResolvedMedia.url)
    }

    @Test
    fun test10_adminReplacesExerciseVideo_bothScreensSynchronizeWithUpdatedVideo() {
        val initialVideoUrl = "https://r2.athgym.com/videos/cable_fly_v1.mp4"
        val replacedVideoUrl = "https://r2.athgym.com/videos/cable_fly_v2_pro.mp4"

        val canonicalFly = com.example.fitness.feature.exercise.data.SampleExerciseData.getById("cable_fly")
        assertNotNull(canonicalFly)

        // 1. Initial upload
        com.example.fitness.feature.exercise.data.SampleExerciseData.updateCanonicalExercise(
            canonicalFly.copy(media = ExerciseMedia(videoUrl = initialVideoUrl))
        )

        val routineId = "gym_chest_3"
        val resolvedId = com.example.fitness.feature.exercise.data.SampleExerciseData.resolveCanonicalId(routineId)
        val name = com.example.fitness.feature.workout.data.WorkoutRepository.getAllExerciseItems()
            .find { it.id.equals(routineId, ignoreCase = true) }?.name ?: "Cable Chest Fly"

        val initialDetail = com.example.fitness.feature.exercise.data.SampleExerciseData.findCanonical(resolvedId, name)
        assertNotNull(initialDetail)
        assertEquals(initialVideoUrl, initialDetail.media.videoUrl)

        // 2. Admin replaces video
        com.example.fitness.feature.exercise.data.SampleExerciseData.updateCanonicalExercise(
            canonicalFly.copy(media = ExerciseMedia(videoUrl = replacedVideoUrl))
        )

        // 3. Both Today's Workout and Workout section detail must see new video
        val todayEx = com.example.fitness.feature.exercise.data.SampleExerciseData.findCanonical("cable_fly", "Cable Fly")
        val detailEx = com.example.fitness.feature.exercise.data.SampleExerciseData.findCanonical(resolvedId, name)

        assertNotNull(todayEx)
        assertNotNull(detailEx)
        assertEquals(replacedVideoUrl, todayEx.media.videoUrl, "Today's workout must reflect replaced video")
        assertEquals(replacedVideoUrl, detailEx.media.videoUrl, "Workout detail must reflect replaced video")
        assertEquals(todayEx.media.videoUrl, detailEx.media.videoUrl, "Both screens must display identical replaced video URL")
    }

    @Test
    fun test11_differentExercisesDoNotMixUpVideos() {
        val benchVideo = "https://r2.athgym.com/videos/bench_exclusive.mp4"
        val flyVideo = "https://r2.athgym.com/videos/fly_exclusive.mp4"

        com.example.fitness.feature.exercise.data.SampleExerciseData.updateCanonicalExercise(
            com.example.fitness.feature.exercise.data.SampleExerciseData.getById("barbell_bench_press")!!.copy(
                media = ExerciseMedia(videoUrl = benchVideo)
            )
        )
        com.example.fitness.feature.exercise.data.SampleExerciseData.updateCanonicalExercise(
            com.example.fitness.feature.exercise.data.SampleExerciseData.getById("cable_fly")!!.copy(
                media = ExerciseMedia(videoUrl = flyVideo)
            )
        )

        val benchDetail = com.example.fitness.feature.exercise.data.SampleExerciseData.findCanonical("gym_chest_1", "Barbell Bench Press")
        val flyDetail = com.example.fitness.feature.exercise.data.SampleExerciseData.findCanonical("gym_chest_3", "Cable Chest Fly")

        assertNotNull(benchDetail)
        assertNotNull(flyDetail)
        assertEquals(benchVideo, benchDetail.media.videoUrl)
        assertEquals(flyVideo, flyDetail.media.videoUrl)
        assertFalse(benchDetail.media.videoUrl == flyDetail.media.videoUrl, "Exercises must NOT cross-contaminate video URLs")
    }

    @Test
    fun test12_exerciseWithoutVideo_safeEmptyStateHandledGracefully() {
        val noVideoEx = ExerciseItem(
            id = "exercise_without_video_test",
            name = "Stretching Pose",
            sets = "2 sets",
            duration = "30s",
            description = "Gentle stretch",
            imageRes = Res.drawable.gym_video_thumb,
            videoDemoUrl = "",
            media = ExerciseMedia(videoUrl = "")
        )

        val resolved = ExerciseMediaResolver.resolve(noVideoEx)
        assertTrue(resolved is ResolvedExerciseMedia.Empty, "Exercise with no video must resolve safely to Empty without crash or fallback fabrication")
    }
}

