package com.example.fitness.feature.workout

import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseMedia
import com.example.fitness.feature.exercise.model.ExerciseMediaType
import com.example.fitness.feature.workout.data.WorkoutRepository
import com.example.fitness.feature.workout.model.ExperienceLevel
import com.example.fitness.feature.workout.model.WorkoutType
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LiveExerciseCatalogIntegrationTest {

    @BeforeTest
    fun setup() {
        SampleExerciseData.resetOverridesForTesting()
        UserSession.currentUserGender = null
        UserSession.experienceLevel = ExperienceLevel.BEGINNER
    }

    @AfterTest
    fun tearDown() {
        SampleExerciseData.resetOverridesForTesting()
        UserSession.currentUserGender = null
        UserSession.experienceLevel = ExperienceLevel.BEGINNER
    }

    // TEST 1: Start with 3 Chest exercises. Verify User sees 3.
    @Test
    fun test1_startWith3ChestExercises_userSees3() {
        val chestWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest")
        assertNotNull(chestWorkout, "Chest workout must exist")
        assertEquals(3, chestWorkout.exercises.size, "Starting Chest workout must have exactly 3 baseline exercises")
    }

    // TEST 2: Admin creates a 4th Chest exercise. Verify User can see 4.
    @Test
    fun test2_adminCreates4thChestExercise_userSees4() {
        val ex4 = Exercise(
            id = "incline_cable_fly",
            name = "Incline Cable Fly",
            primaryMuscle = "Chest",
            category = "STRENGTH",
            difficulty = "Intermediate",
            isActive = true
        )
        SampleExerciseData.updateCanonicalExercise(ex4)

        val chestWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest")
        assertNotNull(chestWorkout)
        assertEquals(4, chestWorkout.exercises.size, "User must see 4 exercises after Admin creates a 4th Chest exercise")
        assertTrue(chestWorkout.exercises.any { it.id == "incline_cable_fly" || it.name == "Incline Cable Fly" })
    }

    // TEST 3: Admin creates a 5th Chest exercise. Verify User can see 5.
    @Test
    fun test3_adminCreates5thChestExercise_userSees5() {
        val ex4 = Exercise(
            id = "incline_cable_fly",
            name = "Incline Cable Fly",
            primaryMuscle = "Chest",
            category = "STRENGTH",
            difficulty = "Intermediate",
            isActive = true
        )
        val ex5 = Exercise(
            id = "machine_chest_press",
            name = "Machine Chest Press",
            primaryMuscle = "Chest",
            category = "STRENGTH",
            difficulty = "Intermediate",
            isActive = true
        )
        SampleExerciseData.updateCanonicalExercise(ex4)
        SampleExerciseData.updateCanonicalExercise(ex5)

        val chestWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest")
        assertNotNull(chestWorkout)
        assertEquals(5, chestWorkout.exercises.size, "User must see 5 exercises after Admin creates a 5th Chest exercise")
        assertTrue(chestWorkout.exercises.any { it.id == "incline_cable_fly" })
        assertTrue(chestWorkout.exercises.any { it.id == "machine_chest_press" })
    }

    // TEST 4: Create an exercise for Back. Verify it appears under Back, not Chest.
    @Test
    fun test4_createBackExercise_appearsUnderBackNotChest() {
        val backEx = Exercise(
            id = "chest_supported_tbar_row",
            name = "Chest-Supported T-Bar Row",
            primaryMuscle = "Back",
            category = "STRENGTH",
            difficulty = "Intermediate",
            isActive = true
        )
        SampleExerciseData.updateCanonicalExercise(backEx)

        val chest = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest")
        val back = WorkoutRepository.getWorkout(WorkoutType.GYM, "back")
        assertNotNull(chest)
        assertNotNull(back)

        assertFalse(chest.exercises.any { it.id == "chest_supported_tbar_row" }, "Back exercise must NOT appear under Chest")
        assertTrue(back.exercises.any { it.id == "chest_supported_tbar_row" }, "Back exercise MUST appear under Back")
    }

    // TEST 5: Create a Male-only exercise. Verify Female User does not see it.
    @Test
    fun test5_createMaleOnlyExercise_femaleUserDoesNotSeeIt() {
        val maleEx = Exercise(
            id = "male_heavy_chest_dip",
            name = "Heavy Weighted Chest Dip",
            primaryMuscle = "Chest",
            gender = "Male",
            difficulty = "Intermediate",
            isActive = true
        )
        SampleExerciseData.updateCanonicalExercise(maleEx)

        val femaleChest = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest", gender = "Female")
        val maleChest = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest", gender = "Male")
        assertNotNull(femaleChest)
        assertNotNull(maleChest)

        assertFalse(femaleChest.exercises.any { it.id == "male_heavy_chest_dip" }, "Female user must not see Male-only exercise")
        assertTrue(maleChest.exercises.any { it.id == "male_heavy_chest_dip" }, "Male user must see Male-only exercise")
    }

    // TEST 6: Create a Female-only exercise. Verify Male User does not see it.
    @Test
    fun test6_createFemaleOnlyExercise_maleUserDoesNotSeeIt() {
        val femaleEx = Exercise(
            id = "female_glute_bridge_press",
            name = "Glute Bridge Chest Press",
            primaryMuscle = "Chest",
            gender = "Female",
            difficulty = "Intermediate",
            isActive = true
        )
        SampleExerciseData.updateCanonicalExercise(femaleEx)

        val maleChest = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest", gender = "Male")
        val femaleChest = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest", gender = "Female")
        assertNotNull(maleChest)
        assertNotNull(femaleChest)

        assertFalse(maleChest.exercises.any { it.id == "female_glute_bridge_press" }, "Male user must not see Female-only exercise")
        assertTrue(femaleChest.exercises.any { it.id == "female_glute_bridge_press" }, "Female user must see Female-only exercise")
    }

    // TEST 7: Create an exercise compatible with Beginner + Intermediate + Expert. Verify it appears for all three appropriate users.
    @Test
    fun test7_createMultiLevelExercise_appearsForAllThreeUsers() {
        val multiLevelEx = Exercise(
            id = "push_up_variations",
            name = "Tempo Push Up Variations",
            primaryMuscle = "Chest",
            difficulty = "Beginner, Advanced, Expert",
            isActive = true
        )
        SampleExerciseData.updateCanonicalExercise(multiLevelEx)

        val beg = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest", level = ExperienceLevel.BEGINNER)
        val inter = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest", level = ExperienceLevel.ADVANCED)
        val exp = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest", level = ExperienceLevel.EXPERT)
        assertNotNull(beg)
        assertNotNull(inter)
        assertNotNull(exp)

        assertTrue(beg.exercises.any { it.id == "push_up_variations" }, "Beginner user must see Beginner+Advanced+Expert exercise")
        assertTrue(inter.exercises.any { it.id == "push_up_variations" }, "Advanced user must see Beginner+Advanced+Expert exercise")
        assertTrue(exp.exercises.any { it.id == "push_up_variations" }, "Expert user must see Beginner+Advanced+Expert exercise")
    }

    // TEST 8: Create an Expert-only exercise. Verify Beginner cannot see it.
    @Test
    fun test8_createExpertOnlyExercise_beginnerCannotSeeIt() {
        val expertEx = Exercise(
            id = "planche_push_up",
            name = "Planche Push Up",
            primaryMuscle = "Chest",
            difficulty = "Expert",
            isActive = true
        )
        SampleExerciseData.updateCanonicalExercise(expertEx)

        val beg = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest", level = ExperienceLevel.BEGINNER)
        val exp = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest", level = ExperienceLevel.EXPERT)
        assertNotNull(beg)
        assertNotNull(exp)

        assertFalse(beg.exercises.any { it.id == "planche_push_up" }, "Beginner user must NOT see Expert-only exercise")
        assertTrue(exp.exercises.any { it.id == "planche_push_up" }, "Expert user must see Expert-only exercise")
    }

    // TEST 9: Admin edits an existing exercise. Verify User sees updated data.
    @Test
    fun test9_adminEditsExistingExercise_userSeesUpdatedData() {
        val updated = Exercise(
            id = "barbell_bench_press",
            name = "Olympic Flat Barbell Bench Press",
            description = "Updated high-performance competition description",
            primaryMuscle = "Chest",
            formTips = listOf("Arch lower back slightly", "Leg drive through heels"),
            media = ExerciseMedia(videoUrl = "https://r2.athgym.com/videos/olympic_bench.mp4"),
            isActive = true
        )
        SampleExerciseData.updateCanonicalExercise(updated)

        val updatedWorkout = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest")
        assertNotNull(updatedWorkout)
        val item = updatedWorkout.exercises.first { it.id == "gym_chest_1" || it.id == "barbell_bench_press" }
        assertEquals("Olympic Flat Barbell Bench Press", item.name, "User catalog must reflect updated exercise name")
        assertEquals("Updated high-performance competition description", item.description, "User catalog must reflect updated description")
        assertEquals("https://r2.athgym.com/videos/olympic_bench.mp4", item.videoDemoUrl, "User catalog must reflect updated video URL")
    }

    // TEST 10: Admin archives an exercise. Verify User no longer sees it.
    @Test
    fun test10_adminArchivesExercise_userNoLongerSeesIt() {
        val ex = Exercise(
            id = "pec_deck_fly",
            name = "Pec Deck Fly",
            primaryMuscle = "Chest",
            difficulty = "Intermediate",
            isActive = true
        )
        SampleExerciseData.updateCanonicalExercise(ex)
        val beforeArchive = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest")
        assertTrue(beforeArchive!!.exercises.any { it.id == "pec_deck_fly" })

        val archived = ex.copy(isActive = false)
        SampleExerciseData.updateCanonicalExercise(archived)
        val afterArchive = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest")
        assertFalse(afterArchive!!.exercises.any { it.id == "pec_deck_fly" }, "Archived exercise must NOT appear in active user catalog")
    }

    // TEST 11: Admin restores the exercise. Verify User sees it again.
    @Test
    fun test11_adminRestoresExercise_userSeesItAgain() {
        val ex = Exercise(
            id = "pec_deck_fly",
            name = "Pec Deck Fly",
            primaryMuscle = "Chest",
            difficulty = "Intermediate",
            isActive = false
        )
        SampleExerciseData.updateCanonicalExercise(ex)
        val whileArchived = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest")
        assertFalse(whileArchived!!.exercises.any { it.id == "pec_deck_fly" })

        val restored = ex.copy(isActive = true)
        SampleExerciseData.updateCanonicalExercise(restored)
        val afterRestore = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest")
        assertTrue(afterRestore!!.exercises.any { it.id == "pec_deck_fly" }, "Restored exercise must reappear in user catalog")
    }

    // TEST 12: Admin uploads exercise video. Verify User resolves the same canonical video URL.
    @Test
    fun test12_adminUploadsMedia_userResolvesCanonicalMedia() {
        val videoUrl = "https://media.athgym.com/videos/cable_crossover_hd.mp4"
        val thumbUrl = "https://media.athgym.com/thumbs/cable_crossover.webp"
        val animUrl = "https://media.athgym.com/anim/cable_crossover.json"
        val exWithMedia = Exercise(
            id = "cable_crossover_live",
            name = "Cable Crossover Live",
            primaryMuscle = "Chest",
            difficulty = "Intermediate",
            media = ExerciseMedia(
                videoUrl = videoUrl,
                thumbnailUrl = thumbUrl,
                animationUrl = animUrl,
                mediaType = ExerciseMediaType.VIDEO.name
            ),
            isActive = true
        )
        SampleExerciseData.updateCanonicalExercise(exWithMedia)

        val workout = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest")
        val item = workout!!.exercises.first { it.id == "cable_crossover_live" }
        assertEquals(videoUrl, item.videoDemoUrl)
        assertEquals(videoUrl, item.media.videoUrl)
        assertEquals(thumbUrl, item.media.thumbnailUrl)
        assertEquals(animUrl, item.media.animationUrl)
    }

    // TEST 13: Create more than 10 exercises for one body part. Verify the User can access ALL of them.
    @Test
    fun test13_createMoreThan10Exercises_userAccessesAllWithout3ItemLimit() {
        val startingCount = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest")!!.exercises.size
        assertEquals(3, startingCount)

        for (i in 1..15) {
            val ex = Exercise(
                id = "chest_bulk_exercise_$i",
                name = "Chest Bulk Exercise $i",
                primaryMuscle = "Chest",
                difficulty = "Intermediate",
                isActive = true
            )
            SampleExerciseData.updateCanonicalExercise(ex)
        }

        val workoutWithBulk = WorkoutRepository.getWorkout(WorkoutType.GYM, "chest")
        assertNotNull(workoutWithBulk)
        assertEquals(startingCount + 15, workoutWithBulk.exercises.size, "User must see all 18 exercises (3 baseline + 15 live additions) without artificial 3-item cap")

        for (i in 1..15) {
            assertTrue(workoutWithBulk.exercises.any { it.id == "chest_bulk_exercise_$i" }, "Exercise $i must be accessible in user catalog")
        }
    }
}
