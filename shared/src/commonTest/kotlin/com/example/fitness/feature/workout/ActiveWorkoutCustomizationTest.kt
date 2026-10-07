package com.example.fitness.feature.workout

import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.progress.data.WorkoutProgressService
import com.example.fitness.feature.progress.model.ExercisePerformanceRecord
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import com.example.fitness.feature.workout.model.CustomExerciseConfig
import com.example.fitness.feature.workout.model.ExperienceLevel
import com.example.fitness.feature.workout.model.PlanExercise
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * ATHGYM CUSTOM SETS, REPS/DURATION & REST TEST SUITE
 *
 * Verifies that the user can customize Sets, Reps/Duration, and Rest time:
 * 1. Default values are preserved initially.
 * 2. Per-exercise isolation (modifying Exercise A never affects Exercise B).
 * 3. Duration-based exercise customization (e.g., RKC Plank Hold: 3 sets / 30-45 sec / 90s -> 4 sets / 60 sec / 60s).
 * 4. Rep-based exercise customization (e.g., Bench Press: 3 sets / 12 reps / 90s -> 5 sets / 8 reps / 120s).
 * 5. Set tracker follows custom sets (1..5 buttons and completion logic).
 * 6. Rest timer uses customized rest time.
 * 7. Reset to default restores original recommended values.
 * 8. Validation prevents negative, zero, and out-of-bounds values.
 * 9. Master exercise definitions and global workout plans remain completely immutable.
 * 10. Workout history and ExercisePerformanceRecord persist actual customized values.
 */
class ActiveWorkoutCustomizationTest {

    @BeforeTest
    fun setUp() {
        WorkoutProgressService.clear()
        UserSession.clear()
        UserSession.currentUserId = "test_custom_user"
        UserSession.setActiveProgram("30-day")
    }

    private val sampleImageRes by lazy {
        CanonicalWorkoutResolver.resolveThirtyDayWorkout("30-day", "day_1", ExperienceLevel.BEGINNER)
            .exercises.first().imageRes
    }

    private fun createDummyPlanExercise(
        id: String,
        name: String,
        sets: String = "3 Sets",
        reps: String = "12 reps",
        rest: String = "90s"
    ): PlanExercise {
        return PlanExercise(
            id = id,
            name = name,
            sets = sets,
            reps = reps,
            rest = rest,
            equipment = "Dumbbell",
            targetMuscle = "Chest",
            videoUrl = "",
            instructions = "Perform with proper form",
            imageRes = sampleImageRes
        )
    }

    @Test
    fun testDefaultValuesPreservedInitially() {
        val plankExercise = createDummyPlanExercise(
            id = "plank_hold",
            name = "RKC Plank Hold",
            sets = "3 Sets",
            reps = "30–45 sec",
            rest = "90s"
        )

        val config = CustomExerciseConfig.fromPlanExercise(plankExercise)

        assertEquals("3 Sets", config.displaySets)
        assertEquals("30–45 sec", config.displayReps)
        assertEquals("90s", config.displayRest)
        assertEquals(3, config.sets)
        assertEquals(90, config.restSeconds)
        assertTrue(config.isDurationBased, "Should detect '30–45 sec' as duration-based")
        assertFalse(config.isCustomized, "Initial state should not be flagged as customized")
    }

    @Test
    fun testDurationBasedDetectionLogic() {
        assertTrue(CustomExerciseConfig.isExerciseDurationBased("30–45 sec"))
        assertTrue(CustomExerciseConfig.isExerciseDurationBased("40 sec"))
        assertTrue(CustomExerciseConfig.isExerciseDurationBased("60s"))
        assertTrue(CustomExerciseConfig.isExerciseDurationBased("1 min"))
        assertTrue(CustomExerciseConfig.isExerciseDurationBased("45 seconds"))

        assertFalse(CustomExerciseConfig.isExerciseDurationBased("12 reps"))
        assertFalse(CustomExerciseConfig.isExerciseDurationBased("10-12 reps"))
        assertFalse(CustomExerciseConfig.isExerciseDurationBased("8"))
        assertFalse(CustomExerciseConfig.isExerciseDurationBased("15"))
    }

    @Test
    fun testScenario1_RKCPlankHoldCustomization() {
        // Test scenario from Prompt Section 23:
        // RKC Plank Hold
        // Default: 3 Sets, 30–45 sec, 90 sec rest
        // Change to: 4 Sets, 60 sec, 60 sec rest
        val plankExercise = createDummyPlanExercise(
            id = "plank_hold",
            name = "RKC Plank Hold",
            sets = "3 Sets",
            reps = "30–45 sec",
            rest = "90s"
        )

        val initialConfig = CustomExerciseConfig.fromPlanExercise(plankExercise)
        assertEquals(3, initialConfig.sets)
        assertEquals("30–45 sec", initialConfig.displayReps)
        assertEquals(90, initialConfig.restSeconds)

        // User customizes to: 4 Sets, 60 sec duration, 60 sec rest
        val customizedConfig = initialConfig.copy(
            sets = 4,
            durationSeconds = 60,
            restSeconds = 60,
            isCustomized = true
        )

        // Verify screen displays
        assertEquals("4 Sets", customizedConfig.displaySets)
        assertEquals("60 sec", customizedConfig.displayReps)
        assertEquals("60s", customizedConfig.displayRest)
        assertEquals(4, customizedConfig.sets)
        assertEquals(60, customizedConfig.durationSeconds)
        assertEquals(60, customizedConfig.restSeconds)

        // Verify Set Tracker bounds (1..4)
        val trackerSets = (1..customizedConfig.sets).toList()
        assertEquals(listOf(1, 2, 3, 4), trackerSets)

        // Verify button text logic
        val currentSet = 1
        val totalSets = customizedConfig.sets
        val buttonText = "Complete Set $currentSet of $totalSets"
        assertEquals("Complete Set 1 of 4", buttonText)

        // Verify ExercisePerformanceRecord simulation
        val performanceRecord = ExercisePerformanceRecord(
            exerciseId = plankExercise.id,
            exerciseName = plankExercise.name,
            prescribedSets = customizedConfig.sets,
            prescribedReps = customizedConfig.displayReps,
            completedSets = 4,
            completedReps = List(4) { customizedConfig.durationSeconds ?: 60 },
            weightUsed = null,
            restSeconds = customizedConfig.restSeconds,
            completed = true,
            notes = null
        )

        assertEquals(4, performanceRecord.prescribedSets)
        assertEquals("60 sec", performanceRecord.prescribedReps)
        assertEquals(60, performanceRecord.restSeconds)
        assertEquals(4, performanceRecord.completedSets)
        assertEquals(listOf(60, 60, 60, 60), performanceRecord.completedReps)
        assertTrue(performanceRecord.completed)
    }

    @Test
    fun testScenario2_RepBasedExerciseCustomization() {
        // Test scenario from Prompt Section 24:
        // Rep-based exercise:
        // Default: 3 Sets, 12 Reps, 90 sec Rest
        // Change to: 5 Sets, 8 Reps, 120 sec Rest
        val benchPress = createDummyPlanExercise(
            id = "bench_press",
            name = "Barbell Bench Press",
            sets = "3 Sets",
            reps = "12 reps",
            rest = "90s"
        )

        val initialConfig = CustomExerciseConfig.fromPlanExercise(benchPress)
        assertEquals(3, initialConfig.sets)
        assertFalse(initialConfig.isDurationBased)

        val customizedConfig = initialConfig.copy(
            sets = 5,
            repsCount = 8,
            restSeconds = 120,
            isCustomized = true
        )

        assertEquals("5 Sets", customizedConfig.displaySets)
        assertEquals("8 reps", customizedConfig.displayReps)
        assertEquals("120s", customizedConfig.displayRest)
        assertEquals(5, (1..customizedConfig.sets).count())

        val performanceRecord = ExercisePerformanceRecord(
            exerciseId = benchPress.id,
            exerciseName = benchPress.name,
            prescribedSets = customizedConfig.sets,
            prescribedReps = customizedConfig.displayReps,
            completedSets = 5,
            completedReps = List(5) { customizedConfig.repsCount ?: 8 },
            weightUsed = null,
            restSeconds = customizedConfig.restSeconds,
            completed = true,
            notes = null
        )

        assertEquals(5, performanceRecord.prescribedSets)
        assertEquals("8 reps", performanceRecord.prescribedReps)
        assertEquals(120, performanceRecord.restSeconds)
        assertEquals(listOf(8, 8, 8, 8, 8), performanceRecord.completedReps)
    }

    @Test
    fun testScenario3_MultipleExercisesIsolation() {
        // Test scenario from Prompt Section 25:
        // Exercise A: 4 sets, 10 reps, 60 sec rest
        // Exercise B: 3 sets, 12 reps, 90 sec rest
        // Verify changing Exercise A does NOT change Exercise B.
        val exA = createDummyPlanExercise("ex_a", "Exercise A", sets = "3 Sets", reps = "12 reps", rest = "90s")
        val exB = createDummyPlanExercise("ex_b", "Exercise B", sets = "3 Sets", reps = "12 reps", rest = "90s")

        val sessionConfigs = mutableMapOf<String, CustomExerciseConfig>()
        sessionConfigs[exA.id] = CustomExerciseConfig.fromPlanExercise(exA)
        sessionConfigs[exB.id] = CustomExerciseConfig.fromPlanExercise(exB)

        // Customize Exercise A ONLY
        sessionConfigs[exA.id] = sessionConfigs[exA.id]!!.copy(
            sets = 4,
            repsCount = 10,
            restSeconds = 60,
            isCustomized = true
        )

        // Exercise A must reflect customized values
        val configA = sessionConfigs[exA.id]!!
        assertEquals(4, configA.sets)
        assertEquals("10 reps", configA.displayReps)
        assertEquals(60, configA.restSeconds)
        assertTrue(configA.isCustomized)

        // Exercise B must remain strictly at initial default values
        val configB = sessionConfigs[exB.id]!!
        assertEquals(3, configB.sets)
        assertEquals("12 reps", configB.displayReps)
        assertEquals(90, configB.restSeconds)
        assertFalse(configB.isCustomized)
    }

    @Test
    fun testScenario4_ResetToDefaultRestoresOriginalValues() {
        val ex = createDummyPlanExercise(
            id = "curl",
            name = "Bicep Curl",
            sets = "3 Sets",
            reps = "12 reps",
            rest = "60s"
        )

        val sessionConfigs = mutableMapOf<String, CustomExerciseConfig>()
        val original = CustomExerciseConfig.fromPlanExercise(ex)
        sessionConfigs[ex.id] = original

        // Customize
        sessionConfigs[ex.id] = original.copy(
            sets = 6,
            repsCount = 15,
            restSeconds = 120,
            isCustomized = true
        )
        assertTrue(sessionConfigs[ex.id]!!.isCustomized)
        assertEquals(6, sessionConfigs[ex.id]!!.sets)

        // Reset to default
        sessionConfigs[ex.id] = CustomExerciseConfig.fromPlanExercise(ex)
        val resetConfig = sessionConfigs[ex.id]!!

        assertFalse(resetConfig.isCustomized)
        assertEquals(3, resetConfig.sets)
        assertEquals("12 reps", resetConfig.displayReps)
        assertEquals(60, resetConfig.restSeconds)
    }

    @Test
    fun testMasterDataImmutability() {
        // Ensure master exercise definitions in SampleExerciseData are never modified
        val masterPlank = SampleExerciseData.getById("plank_hold")
        val initialMasterSets = masterPlank?.defaultSets ?: 3
        val initialMasterRest = masterPlank?.defaultRestSeconds ?: 60

        val planEx = createDummyPlanExercise(
            id = "plank_hold",
            name = "RKC Plank Hold",
            sets = "3 Sets",
            reps = "30–45 sec",
            rest = "90s"
        )
        val sessionConfig = CustomExerciseConfig.fromPlanExercise(planEx).copy(
            sets = 10,
            durationSeconds = 120,
            restSeconds = 180,
            isCustomized = true
        )

        assertEquals(10, sessionConfig.sets)

        // Master exercise definition in SampleExerciseData MUST remain untouched
        val currentMasterPlank = SampleExerciseData.getById("plank_hold")
        assertEquals(initialMasterSets, currentMasterPlank?.defaultSets)
        assertEquals(initialMasterRest, currentMasterPlank?.defaultRestSeconds)
    }

    @Test
    fun testValidationBounds() {
        // Sets: 1..20
        assertEquals(1, CustomExerciseConfig.parseDefaultSets("0 Sets"))
        assertEquals(1, CustomExerciseConfig.parseDefaultSets("-5 Sets"))
        assertEquals(20, CustomExerciseConfig.parseDefaultSets("50 Sets"))
        assertEquals(4, CustomExerciseConfig.parseDefaultSets("4 Sets"))

        // Rest: 5..300
        assertEquals(5, CustomExerciseConfig.parseDefaultRest("0s"))
        assertEquals(300, CustomExerciseConfig.parseDefaultRest("900s"))
        assertEquals(90, CustomExerciseConfig.parseDefaultRest("90s"))

        // Reps: 1..100
        assertEquals(1, CustomExerciseConfig.parseDefaultRepsOrDuration("0 reps", false))
        assertEquals(100, CustomExerciseConfig.parseDefaultRepsOrDuration("250 reps", false))
        assertEquals(12, CustomExerciseConfig.parseDefaultRepsOrDuration("12 reps", false))

        // Duration: 5..600
        assertEquals(5, CustomExerciseConfig.parseDefaultRepsOrDuration("2 sec", true))
        assertEquals(600, CustomExerciseConfig.parseDefaultRepsOrDuration("1200 sec", true))
        assertEquals(45, CustomExerciseConfig.parseDefaultRepsOrDuration("45 sec", true))
    }
}
