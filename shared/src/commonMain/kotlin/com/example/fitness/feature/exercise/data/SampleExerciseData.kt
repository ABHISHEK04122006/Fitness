package com.example.fitness.feature.exercise.data

import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseCategory
import com.example.fitness.feature.exercise.model.ExerciseDifficulty
import com.example.fitness.feature.exercise.model.MovementPattern
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.example.fitness.createThreadSafeMap

/**
 * Centralized canonical exercise registry for Athgym / Fitness platform.
 * Fully reconciled against the 4 authoritative PDF documents:
 * 1. Athgym_Gym_Warmup_Cooldown_Exercise_Library.pdf
 * 2. Athgym_30_Day_Workout_Developer_Spec.pdf
 * 3. Athgym_30_Day_Hypertrophy_Strength_Plans.pdf
 * 4. Athgym_30_Day_Athlete_Performance_Program.pdf
 *
 * Retains stable IDs for backward compatibility and supports complete exercise metadata.
 */
object SampleExerciseData {

    private fun createBasicExercise(
        id: String,
        name: String,
        primaryMuscle: String = "",
        secondaryMuscles: List<String> = emptyList(),
        muscleGroups: List<String> = if (primaryMuscle.isNotBlank()) listOf(primaryMuscle) else emptyList(),
        equipment: List<String> = emptyList(),
        category: String = ExerciseCategory.STRENGTH.name,
        movementPattern: String = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty: String = ExerciseDifficulty.BEGINNER.name,
        defaultSets: Int = 3,
        defaultReps: String = "10-12",
        defaultRestSeconds: Int = 60
    ): Exercise = Exercise(
        id = id,
        name = name,
        slug = id.replace('_', '-'),
        description = "",
        primaryMuscle = primaryMuscle,
        secondaryMuscles = secondaryMuscles,
        muscleGroups = muscleGroups,
        equipment = equipment,
        category = category,
        movementPattern = movementPattern,
        difficulty = difficulty,
        instructions = emptyList(),
        setupSteps = emptyList(),
        executionSteps = emptyList(),
        breathing = "",
        formTips = emptyList(),
        commonMistakes = emptyList(),
        defaultSets = defaultSets,
        defaultReps = defaultReps,
        defaultRestSeconds = defaultRestSeconds,
        isActive = true,
        version = 1
    )

    val EX_4_60M_RELAXED_STRIDES = createBasicExercise(
        id = "4_60m_relaxed_strides",
        name = "4×60m relaxed strides",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 1,
        defaultReps = "1",
        defaultRestSeconds = 60
    )

    val EX_4_ROUNDS_250M_RUN_15_KETTLEBELL_SWINGS_12_BURPEES_15_WALKING_LUNGES_10_PUSH_UPS_30_SEC_BATTLE_ROPES = createBasicExercise(
        id = "4_rounds_250m_run_15_kettlebell_swings_12_burpees_15_walking_lunges_10_push_ups_30_sec_battle_ropes",
        name = "4 rounds: 250m run; 15 kettlebell swings; 12 burpees; 15 walking lunges; 10 push-ups; 30-sec battle ropes",
        primaryMuscle = "Legs",
        equipment = listOf("Kettlebell"),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 1,
        defaultReps = "1",
        defaultRestSeconds = 60
    )

    val EX_5_10_5 = createBasicExercise(
        id = "5_10_5",
        name = "5-10-5",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 5,
        defaultReps = "1 rep",
        defaultRestSeconds = 60
    )

    val EX_5_10_5_SHUTTLE = createBasicExercise(
        id = "5_10_5_shuttle",
        name = "5-10-5 shuttle",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 1,
        defaultReps = "5 reps",
        defaultRestSeconds = 60
    )

    val EX_5_5_MIN_MODERATE_RUNNING = createBasicExercise(
        id = "5_5_min_moderate_running",
        name = "5×5 min moderate running",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 1,
        defaultReps = "1",
        defaultRestSeconds = 60
    )

    val EX_5_60M_RELAXED_STRIDES = createBasicExercise(
        id = "5_60m_relaxed_strides",
        name = "5×60m relaxed strides",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 1,
        defaultReps = "1",
        defaultRestSeconds = 60
    )

    val EX_5_ROUNDS_200M_RUN_10_BURPEES_15_KB_SWINGS_12_PUSH_UPS_15_AIR_SQUATS_30_SEC_BATTLE_ROPES = createBasicExercise(
        id = "5_rounds_200m_run_10_burpees_15_kb_swings_12_push_ups_15_air_squats_30_sec_battle_ropes",
        name = "5 rounds: 200m run; 10 burpees; 15 KB swings; 12 push-ups; 15 air squats; 30-sec battle ropes",
        primaryMuscle = "Legs",
        equipment = listOf("Kettlebell"),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 1,
        defaultReps = "1",
        defaultRestSeconds = 60
    )

    val EX_5_ROUNDS_300M_RUN_12_KB_SWINGS_10_BURPEES_15_WALKING_LUNGES_10_PUSH_UPS_30_SEC_BATTLE_ROPES = createBasicExercise(
        id = "5_rounds_300m_run_12_kb_swings_10_burpees_15_walking_lunges_10_push_ups_30_sec_battle_ropes",
        name = "5 rounds: 300m run; 12 KB swings; 10 burpees; 15 walking lunges; 10 push-ups; 30-sec battle ropes",
        primaryMuscle = "Legs",
        equipment = listOf("Kettlebell"),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 1,
        defaultReps = "1",
        defaultRestSeconds = 60
    )

    val EX_6_ROUNDS_30_SEC_HARD_RUN_10_BURPEES_15_KB_SWINGS_10_PUSH_UPS_15_SQUATS = createBasicExercise(
        id = "6_rounds_30_sec_hard_run_10_burpees_15_kb_swings_10_push_ups_15_squats",
        name = "6 rounds: 30-sec hard run; 10 burpees; 15 KB swings; 10 push-ups; 15 squats",
        primaryMuscle = "Legs",
        equipment = listOf("Kettlebell"),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 1,
        defaultReps = "1",
        defaultRestSeconds = 60
    )

    val A_SKIP = createBasicExercise(
        id = "a_skip",
        name = "A-skip",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "20m",
        defaultRestSeconds = 60
    )

    val AB_ROLLOUT = createBasicExercise(
        id = "ab_rollout",
        name = "Ab Rollout",
        primaryMuscle = "Abs",
        equipment = listOf("Ab Wheel"),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.ANTI_ROTATION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "8-12",
        defaultRestSeconds = 60
    )

    val ARNOLD_PRESS = createBasicExercise(
        id = "arnold_press",
        name = "Arnold Press",
        primaryMuscle = "Shoulders",
        secondaryMuscles = listOf("Triceps"),
        muscleGroups = listOf("Shoulders"),
        equipment = listOf("Dumbbells"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PUSH.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "8-12",
        defaultRestSeconds = 60
    )

    val ASSISTED_PULLDOWN = createBasicExercise(
        id = "assisted_pulldown",
        name = "Assisted Pulldown",
        primaryMuscle = "Back",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val BACK_EXTENSION = createBasicExercise(
        id = "back_extension",
        name = "Back Extension",
        primaryMuscle = "Back",
        secondaryMuscles = listOf("Glutes", "Hamstrings"),
        muscleGroups = listOf("Back"),
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HINGE.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val BACKPEDAL_SPRINT = createBasicExercise(
        id = "backpedal_sprint",
        name = "Backpedal → sprint",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 1,
        defaultReps = "5 reps",
        defaultRestSeconds = 60
    )

    val BARBELL_BENCH_PRESS = createBasicExercise(
        id = "barbell_bench_press",
        name = "Barbell Bench Press",
        primaryMuscle = "Chest",
        equipment = listOf("Barbell", "Bench"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "8-10",
        defaultRestSeconds = 90
    )

    val BARBELL_BICEP_CURL = createBasicExercise(
        id = "barbell_bicep_curl",
        name = "Barbell Bicep Curl",
        primaryMuscle = "Biceps",
        equipment = listOf("Barbell"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val BARBELL_HIP_THRUST = createBasicExercise(
        id = "barbell_hip_thrust",
        name = "Barbell Hip Thrust",
        primaryMuscle = "Glutes",
        equipment = listOf("Barbell"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HINGE.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 4,
        defaultReps = "6-10",
        defaultRestSeconds = 60
    )

    val BARBELL_OVERHEAD_PRESS = createBasicExercise(
        id = "barbell_overhead_press",
        name = "Barbell Overhead Press",
        primaryMuscle = "Shoulders",
        secondaryMuscles = listOf("Triceps"),
        muscleGroups = listOf("Shoulders"),
        equipment = listOf("Barbell"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PUSH.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 4,
        defaultReps = "6-10",
        defaultRestSeconds = 60
    )

    val BARBELL_SHRUG = createBasicExercise(
        id = "barbell_shrug",
        name = "Barbell Shrug",
        primaryMuscle = "Shoulders",
        secondaryMuscles = listOf("Traps"),
        muscleGroups = listOf("Shoulders"),
        equipment = listOf("Barbell"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-15",
        defaultRestSeconds = 60
    )

    val BAYESIAN_CABLE_CURL = createBasicExercise(
        id = "bayesian_cable_curl",
        name = "Bayesian Cable Curl",
        primaryMuscle = "Biceps",
        equipment = listOf("Cable Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-15",
        defaultRestSeconds = 60
    )

    val BAYESIAN_CURL = createBasicExercise(
        id = "bayesian_curl",
        name = "Bayesian Curl",
        primaryMuscle = "Biceps",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-15",
        defaultRestSeconds = 60
    )

    val BENT_OVER_BARBELL_ROW = createBasicExercise(
        id = "bent_over_barbell_row",
        name = "Bent-Over Barbell Row",
        primaryMuscle = "Back",
        equipment = listOf("Barbell"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PULL.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "8-10",
        defaultRestSeconds = 60
    )

    val BICYCLE_CRUNCH = createBasicExercise(
        id = "bicycle_crunch",
        name = "Bicycle Crunch",
        primaryMuscle = "Abs",
        secondaryMuscles = listOf("Obliques"),
        muscleGroups = listOf("Abs"),
        equipment = emptyList(),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.FLEXION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10 / side",
        defaultRestSeconds = 60
    )

    val BIRD_DOG = createBasicExercise(
        id = "bird_dog",
        name = "Bird Dog",
        primaryMuscle = "Abs",
        equipment = emptyList(),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "10 / side",
        defaultRestSeconds = 60
    )

    val BOUNDING = createBasicExercise(
        id = "bounding",
        name = "Bounding",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "20m",
        defaultRestSeconds = 60
    )

    val BOUNDS = createBasicExercise(
        id = "bounds",
        name = "Bounds",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.ATHLETE_PERFORMANCE.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "20m",
        defaultRestSeconds = 60
    )

    val BOX_JUMP = createBasicExercise(
        id = "box_jump",
        name = "Box jump",
        primaryMuscle = "Legs",
        equipment = listOf("Plyo Box"),
        category = ExerciseCategory.ATHLETE_PERFORMANCE.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 4,
        defaultReps = "5",
        defaultRestSeconds = 60
    )

    val BROAD_JUMP = createBasicExercise(
        id = "broad_jump",
        name = "Broad jump",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.ATHLETE_PERFORMANCE.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 4,
        defaultReps = "4",
        defaultRestSeconds = 60
    )

    val BUILD_UP_RUNS = createBasicExercise(
        id = "build_up_runs",
        name = "Build-up runs",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "30m",
        defaultRestSeconds = 60
    )

    val BULGARIAN_SPLIT_SQUAT = createBasicExercise(
        id = "bulgarian_split_squat",
        name = "Bulgarian Split Squat",
        primaryMuscle = "Quadriceps",
        equipment = listOf("Dumbbells", "Bench"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.LUNGE.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "8-12",
        defaultRestSeconds = 90
    )

    val CABLE_CONCENTRATION_CURL = createBasicExercise(
        id = "cable_concentration_curl",
        name = "Cable Concentration Curl",
        primaryMuscle = "Biceps",
        equipment = listOf("Cable Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val CABLE_CROSSOVER = createBasicExercise(
        id = "cable_crossover",
        name = "Cable Crossover",
        primaryMuscle = "Chest",
        equipment = listOf("Cable Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val CABLE_CRUNCH = createBasicExercise(
        id = "cable_crunch",
        name = "Cable Crunch",
        primaryMuscle = "Abs",
        equipment = listOf("Cable Machine"),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.FLEXION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "10-15",
        defaultRestSeconds = 60
    )

    val CABLE_CURL = createBasicExercise(
        id = "cable_curl",
        name = "Cable Curl",
        primaryMuscle = "Biceps",
        equipment = listOf("Cable Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-15",
        defaultRestSeconds = 60
    )

    val CABLE_FLY = createBasicExercise(
        id = "cable_fly",
        name = "Cable Fly",
        primaryMuscle = "Chest",
        equipment = listOf("Cable Tower"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val CABLE_HAMMER_CURL = createBasicExercise(
        id = "cable_hammer_curl",
        name = "Cable Hammer Curl",
        primaryMuscle = "Biceps",
        equipment = listOf("Cable Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val CABLE_LATERAL_RAISE = createBasicExercise(
        id = "cable_lateral_raise",
        name = "Cable Lateral Raise",
        primaryMuscle = "Shoulders",
        secondaryMuscles = emptyList(),
        muscleGroups = listOf("Shoulders"),
        equipment = listOf("Cable Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val CABLE_OVERHEAD_EXTENSION = createBasicExercise(
        id = "cable_overhead_extension",
        name = "Cable Overhead Extension",
        primaryMuscle = "Triceps",
        secondaryMuscles = emptyList(),
        muscleGroups = listOf("Triceps"),
        equipment = listOf("Cable Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val CABLE_TRICEPS_EXTENSION = createBasicExercise(
        id = "cable_triceps_extension",
        name = "Cable Triceps Extension",
        primaryMuscle = "Triceps",
        equipment = listOf("Cable Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val CABLE_WOODCHOP = createBasicExercise(
        id = "cable_woodchop",
        name = "Cable Woodchop",
        primaryMuscle = "Abs",
        equipment = listOf("Cable Machine"),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.ROTATION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "10 / side",
        defaultRestSeconds = 60
    )

    val CABLE_WOODCHOPPERS = createBasicExercise(
        id = "cable_woodchoppers",
        name = "Cable Woodchoppers",
        primaryMuscle = "Abs",
        equipment = listOf("Cable Machine"),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.ROTATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val CHEST_DIPS = createBasicExercise(
        id = "chest_dips",
        name = "Chest Dips",
        primaryMuscle = "Chest",
        equipment = listOf("Dip Bars"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "8-12",
        defaultRestSeconds = 90
    )

    val CHEST_PRESS_MACHINE = createBasicExercise(
        id = "chest_press_machine",
        name = "Chest Press Machine",
        primaryMuscle = "Chest",
        equipment = listOf("Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val CHEST_SUPPORTED_MACHINE_PRESS = createBasicExercise(
        id = "chest_supported_machine_press",
        name = "Chest-Supported Machine Press",
        primaryMuscle = "Chest",
        equipment = listOf("Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val CHEST_SUPPORTED_MACHINE_ROW = createBasicExercise(
        id = "chest_supported_machine_row",
        name = "Chest-Supported Machine Row",
        primaryMuscle = "Back",
        secondaryMuscles = listOf("Biceps"),
        muscleGroups = listOf("Back"),
        equipment = listOf("Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val CHEST_SUPPORTED_ROW = createBasicExercise(
        id = "chest_supported_row",
        name = "Chest-Supported Row",
        primaryMuscle = "Back",
        secondaryMuscles = listOf("Biceps"),
        muscleGroups = listOf("Back"),
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val CLOSE_GRIP_BENCH_PRESS = createBasicExercise(
        id = "close_grip_bench_press",
        name = "Close-Grip Bench Press",
        primaryMuscle = "Triceps",
        equipment = listOf("Barbell", "Bench"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "8-10",
        defaultRestSeconds = 90
    )

    val CLOSE_GRIP_LAT_PULLDOWN = createBasicExercise(
        id = "close_grip_lat_pulldown",
        name = "Close-Grip Lat Pulldown",
        primaryMuscle = "Back",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val CLOSE_GRIP_PULLDOWN = createBasicExercise(
        id = "close_grip_pulldown",
        name = "Close-Grip Pulldown",
        primaryMuscle = "Back",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val CLOSE_GRIP_PUSH_UP = createBasicExercise(
        id = "close_grip_push_up",
        name = "Close-Grip Push-Up",
        primaryMuscle = "Triceps",
        secondaryMuscles = listOf("Chest"),
        muscleGroups = listOf("Triceps"),
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "AMRAP with form",
        defaultRestSeconds = 60
    )

    val CLOSE_GRIP_WEIGHTED_PULL_UP = createBasicExercise(
        id = "close_grip_weighted_pull_up",
        name = "Close-Grip Weighted Pull-Up",
        primaryMuscle = "Back",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 4,
        defaultReps = "5-8",
        defaultRestSeconds = 60
    )

    val CONE_REACTION_DRILL = createBasicExercise(
        id = "cone_reaction_drill",
        name = "Cone reaction drill",
        primaryMuscle = "Legs",
        equipment = listOf("Cones"),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 1,
        defaultReps = "5 reps",
        defaultRestSeconds = 60
    )

    val COPENHAGEN_PLANK_3_20_SEC_SIDE = createBasicExercise(
        id = "copenhagen_plank_3_20_sec_side",
        name = "Copenhagen plank — 3×20 sec/side",
        primaryMuscle = "Abs",
        equipment = emptyList(),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 1,
        defaultReps = "1",
        defaultRestSeconds = 60
    )

    val CORE = createBasicExercise(
        id = "core",
        name = "Core",
        primaryMuscle = "Abs",
        equipment = emptyList(),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 1,
        defaultReps = "10 min",
        defaultRestSeconds = 60
    )

    val CORE_CIRCUIT_3_ROUNDS = createBasicExercise(
        id = "core_circuit_3_rounds",
        name = "Core circuit — 3 rounds",
        primaryMuscle = "Abs",
        equipment = emptyList(),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 1,
        defaultReps = "1",
        defaultRestSeconds = 60
    )

    val DEAD_BUG = createBasicExercise(
        id = "dead_bug",
        name = "Dead Bug",
        primaryMuscle = "Abs",
        equipment = emptyList(),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "10 / side",
        defaultRestSeconds = 60
    )

    val DEADLIFT = createBasicExercise(
        id = "deadlift",
        name = "Conventional Deadlift",
        primaryMuscle = "Hamstrings",
        equipment = listOf("Barbell"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HINGE.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "5",
        defaultRestSeconds = 180
    )

    val DECELERATION_SPRINT = createBasicExercise(
        id = "deceleration_sprint",
        name = "Deceleration sprint",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 5,
        defaultReps = "15m",
        defaultRestSeconds = 60
    )

    val DECLINE_BARBELL_PRESS = createBasicExercise(
        id = "decline_barbell_press",
        name = "Decline Barbell Press",
        primaryMuscle = "Chest",
        equipment = listOf("Barbell"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "8-10",
        defaultRestSeconds = 60
    )

    val DECLINE_DUMBBELL_PRESS = createBasicExercise(
        id = "decline_dumbbell_press",
        name = "Decline Dumbbell Press",
        primaryMuscle = "Chest",
        equipment = listOf("Dumbbells", "Decline Bench"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "8-12",
        defaultRestSeconds = 90
    )

    val DECLINE_MACHINE_PRESS = createBasicExercise(
        id = "decline_machine_press",
        name = "Decline Machine Press",
        primaryMuscle = "Chest",
        equipment = listOf("Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val DECLINE_PRESS = createBasicExercise(
        id = "decline_press",
        name = "Decline Press",
        primaryMuscle = "Chest",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "8-12",
        defaultRestSeconds = 60
    )

    val DEPTH_DROP_LANDING = createBasicExercise(
        id = "depth_drop_landing",
        name = "Depth/drop landing",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.ATHLETE_PERFORMANCE.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "5",
        defaultRestSeconds = 60
    )

    val DUMBBELL_BENCH = createBasicExercise(
        id = "dumbbell_bench",
        name = "Dumbbell Bench",
        primaryMuscle = "Chest",
        equipment = listOf("Dumbbells"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10",
        defaultRestSeconds = 60
    )

    val DUMBBELL_BENCH_PRESS = createBasicExercise(
        id = "dumbbell_bench_press",
        name = "Dumbbell Bench Press",
        primaryMuscle = "Chest",
        equipment = listOf("Dumbbells", "Bench"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val DUMBBELL_FLY = createBasicExercise(
        id = "dumbbell_fly",
        name = "Dumbbell Fly",
        primaryMuscle = "Chest",
        equipment = listOf("Dumbbells"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12",
        defaultRestSeconds = 60
    )

    val DUMBBELL_GOBLET_SQUAT = createBasicExercise(
        id = "dumbbell_goblet_squat",
        name = "Dumbbell Goblet Squat",
        primaryMuscle = "Quadriceps",
        equipment = listOf("Dumbbells"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.SQUAT.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12",
        defaultRestSeconds = 60
    )

    val DUMBBELL_HAMMER_CURL = createBasicExercise(
        id = "dumbbell_hammer_curl",
        name = "Dumbbell Hammer Curl",
        primaryMuscle = "Biceps",
        equipment = listOf("Dumbbells"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val DUMBBELL_LATERAL_RAISE = createBasicExercise(
        id = "dumbbell_lateral_raise",
        name = "Dumbbell Lateral Raise",
        primaryMuscle = "Shoulders",
        equipment = listOf("Dumbbells"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val DUMBBELL_PRESS = createBasicExercise(
        id = "dumbbell_press",
        name = "Dumbbell Press",
        primaryMuscle = "Shoulders",
        secondaryMuscles = listOf("Triceps"),
        muscleGroups = listOf("Shoulders"),
        equipment = listOf("Dumbbells"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val DUMBBELL_ROMANIAN_DEADLIFT = createBasicExercise(
        id = "dumbbell_romanian_deadlift",
        name = "Dumbbell Romanian Deadlift",
        primaryMuscle = "Hamstrings",
        equipment = listOf("Dumbbells"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HINGE.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10",
        defaultRestSeconds = 60
    )

    val DUMBBELL_ROW = createBasicExercise(
        id = "dumbbell_row",
        name = "Dumbbell Row",
        primaryMuscle = "Back",
        equipment = listOf("Dumbbells"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val DUMBBELL_SHOULDER_PRESS = createBasicExercise(
        id = "dumbbell_shoulder_press",
        name = "Dumbbell Shoulder Press",
        primaryMuscle = "Shoulders",
        secondaryMuscles = listOf("Triceps"),
        muscleGroups = listOf("Shoulders"),
        equipment = listOf("Dumbbells"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val DUMBBELL_SHRUGS = createBasicExercise(
        id = "dumbbell_shrugs",
        name = "Dumbbell Shrugs",
        primaryMuscle = "Shoulders",
        secondaryMuscles = listOf("Traps"),
        muscleGroups = listOf("Shoulders"),
        equipment = listOf("Dumbbells"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val EASY_MODERATE_RUN = createBasicExercise(
        id = "easy_moderate_run",
        name = "Easy/moderate run",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 1,
        defaultReps = "25-35 min",
        defaultRestSeconds = 60
    )

    val EASY_RUN_CYCLE = createBasicExercise(
        id = "easy_run_cycle",
        name = "Easy run/cycle",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 1,
        defaultReps = "30-40 min",
        defaultRestSeconds = 60
    )

    val EXPLOSIVE_PUSH_UP = createBasicExercise(
        id = "explosive_push_up",
        name = "Explosive push-up",
        primaryMuscle = "Chest",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "5",
        defaultRestSeconds = 60
    )

    val EZ_BAR_CURL = createBasicExercise(
        id = "ez_bar_curl",
        name = "EZ-Bar Curl",
        primaryMuscle = "Biceps",
        equipment = listOf("EZ Bar"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val EZ_BAR_PREACHER_CURL = createBasicExercise(
        id = "ez_bar_preacher_curl",
        name = "EZ-Bar Preacher Curl",
        primaryMuscle = "Biceps",
        equipment = listOf("EZ Bar", "Preacher Bench"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val FACE_PULLS = createBasicExercise(
        id = "face_pulls",
        name = "Face Pulls with Cable Rope",
        primaryMuscle = "Shoulders",
        equipment = listOf("Cable Machine", "Rope Attachment"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val FALLING_STARTS = createBasicExercise(
        id = "falling_starts",
        name = "Falling starts",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 5,
        defaultReps = "10m",
        defaultRestSeconds = 60
    )

    val FARMER_CARRY = createBasicExercise(
        id = "farmer_carry",
        name = "Farmer Carry",
        primaryMuscle = "Full Body",
        equipment = emptyList(),
        category = ExerciseCategory.ATHLETE_PERFORMANCE.name,
        movementPattern = MovementPattern.CARRY.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "30m",
        defaultRestSeconds = 60
    )

    val FLAT_BARBELL_BENCH_PRESS = createBasicExercise(
        id = "flat_barbell_bench_press",
        name = "Flat Barbell Bench Press",
        primaryMuscle = "Chest",
        equipment = listOf("Barbell"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 4,
        defaultReps = "6-8",
        defaultRestSeconds = 60
    )

    val FLAT_DUMBBELL_PRESS = createBasicExercise(
        id = "flat_dumbbell_press",
        name = "Flat Dumbbell Press",
        primaryMuscle = "Chest",
        equipment = listOf("Dumbbells"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 4,
        defaultReps = "6-10",
        defaultRestSeconds = 60
    )

    val FLAT_MACHINE_PRESS = createBasicExercise(
        id = "flat_machine_press",
        name = "Flat Machine Press",
        primaryMuscle = "Chest",
        equipment = listOf("Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val FLYING_20M = createBasicExercise(
        id = "flying_20m",
        name = "Flying 20m",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 5,
        defaultReps = "1 rep",
        defaultRestSeconds = 60
    )

    val FLYING_20M_SPRINT = createBasicExercise(
        id = "flying_20m_sprint",
        name = "Flying 20m sprint",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 1,
        defaultReps = "5 reps",
        defaultRestSeconds = 60
    )

    val FORTY_METER_SPRINT = createBasicExercise(
        id = "forty_meter_sprint",
        name = "40m sprint",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 1,
        defaultReps = "4 reps",
        defaultRestSeconds = 60
    )

    val FRONT_FOOT_ELEVATED_SPLIT_SQUAT = createBasicExercise(
        id = "front_foot_elevated_split_squat",
        name = "Front-Foot Elevated Split Squat",
        primaryMuscle = "Quadriceps",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.SQUAT.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10 / leg",
        defaultRestSeconds = 60
    )

    val FRONT_SQUAT = createBasicExercise(
        id = "front_squat",
        name = "Front Squat",
        primaryMuscle = "Quadriceps",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.SQUAT.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 4,
        defaultReps = "6-10",
        defaultRestSeconds = 60
    )

    val GLUTE_BRIDGE = createBasicExercise(
        id = "glute_bridge",
        name = "Glute Bridge",
        primaryMuscle = "Glutes",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.LUNGE.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val GOBLET_SQUAT = createBasicExercise(
        id = "goblet_squat",
        name = "Goblet Squat",
        primaryMuscle = "Quadriceps",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.SQUAT.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val HACK_SQUAT = createBasicExercise(
        id = "hack_squat",
        name = "Hack Squat",
        primaryMuscle = "Quadriceps",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.SQUAT.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "8-12",
        defaultRestSeconds = 60
    )

    val HACK_SQUAT_MACHINE = createBasicExercise(
        id = "hack_squat_machine",
        name = "Hack Squat Machine",
        primaryMuscle = "Quadriceps",
        equipment = listOf("Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.SQUAT.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val HANG_POWER_CLEAN = createBasicExercise(
        id = "hang_power_clean",
        name = "Hang power clean*",
        primaryMuscle = "Full Body",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 4,
        defaultReps = "3",
        defaultRestSeconds = 60
    )

    val HANGING_KNEE_TO_ELBOW_RAISE = createBasicExercise(
        id = "hanging_knee_to_elbow_raise",
        name = "Hanging Knee-to-Elbow Raise",
        primaryMuscle = "Abs",
        equipment = emptyList(),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.FLEXION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 4,
        defaultReps = "8-12",
        defaultRestSeconds = 60
    )

    val HANGING_LEG_RAISE = createBasicExercise(
        id = "hanging_leg_raise",
        name = "Hanging Knee / Leg Raise",
        primaryMuscle = "Abs",
        equipment = listOf("Pull-up Bar"),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.FLEXION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val HEEL_TAPS = createBasicExercise(
        id = "heel_taps",
        name = "Heel Taps",
        primaryMuscle = "Abs",
        equipment = emptyList(),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12 / side",
        defaultRestSeconds = 60
    )

    val HILL_SPRINT = createBasicExercise(
        id = "hill_sprint",
        name = "Hill sprint",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 4,
        defaultReps = "15m",
        defaultRestSeconds = 60
    )

    val HIP_ABDUCTION = createBasicExercise(
        id = "hip_abduction",
        name = "Hip Abduction",
        primaryMuscle = "Glutes",
        secondaryMuscles = listOf("Legs"),
        muscleGroups = listOf("Glutes", "Legs"),
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "15-20",
        defaultRestSeconds = 60
    )

    val HIP_ADDUCTION = createBasicExercise(
        id = "hip_adduction",
        name = "Hip Adduction",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val HIP_THRUST = createBasicExercise(
        id = "hip_thrust",
        name = "Hip Thrust",
        primaryMuscle = "Glutes",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HINGE.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "8-12",
        defaultRestSeconds = 60
    )

    val HYPERTROPHY_PERFORMANCE_CHECK = createBasicExercise(
        id = "hypertrophy_performance_check",
        name = "Hypertrophy Performance Check",
        primaryMuscle = "Full Body",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 1,
        defaultReps = "3-5RM",
        defaultRestSeconds = 60
    )

    val INCLINE_BARBELL_PRESS = createBasicExercise(
        id = "incline_barbell_press",
        name = "Incline Barbell Press",
        primaryMuscle = "Chest",
        equipment = listOf("Barbell", "Incline Bench"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 4,
        defaultReps = "6-10",
        defaultRestSeconds = 90
    )

    val INCLINE_CHEST_PRESS = createBasicExercise(
        id = "incline_chest_press",
        name = "Incline Chest Press",
        primaryMuscle = "Chest",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val INCLINE_DUMBBELL_BICEP_CURL = createBasicExercise(
        id = "incline_dumbbell_bicep_curl",
        name = "Incline Dumbbell Bicep Curl",
        primaryMuscle = "Biceps",
        equipment = listOf("Dumbbells", "Incline Bench"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val INCLINE_DUMBBELL_PRESS = createBasicExercise(
        id = "incline_dumbbell_press",
        name = "Incline Dumbbell Press",
        primaryMuscle = "Chest",
        equipment = listOf("Dumbbells", "Incline Bench"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val INCLINE_MACHINE_PRESS = createBasicExercise(
        id = "incline_machine_press",
        name = "Incline Machine Press",
        primaryMuscle = "Chest",
        equipment = listOf("Incline Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val JM_PRESS = createBasicExercise(
        id = "jm_press",
        name = "JM Press",
        primaryMuscle = "Triceps",
        secondaryMuscles = listOf("Chest"),
        muscleGroups = listOf("Triceps"),
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "8-10",
        defaultRestSeconds = 60
    )

    val JUMP_SQUAT = createBasicExercise(
        id = "jump_squat",
        name = "Jump squat",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.ATHLETE_PERFORMANCE.name,
        movementPattern = MovementPattern.SQUAT.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "5",
        defaultRestSeconds = 60
    )

    val KB_SWING = createBasicExercise(
        id = "kb_swing",
        name = "KB swing",
        primaryMuscle = "Hamstrings",
        secondaryMuscles = listOf("Glutes"),
        muscleGroups = listOf("Hamstrings", "Glutes", "Legs"),
        equipment = listOf("Kettlebell"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HINGE.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 4,
        defaultReps = "8",
        defaultRestSeconds = 60
    )

    val KETTLEBELL_SWING = createBasicExercise(
        id = "kettlebell_swing",
        name = "Kettlebell swing",
        primaryMuscle = "Hamstrings",
        secondaryMuscles = listOf("Glutes"),
        muscleGroups = listOf("Hamstrings", "Glutes", "Legs"),
        equipment = listOf("Kettlebell"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HINGE.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 4,
        defaultReps = "8",
        defaultRestSeconds = 60
    )

    val KNEE_PLANK = createBasicExercise(
        id = "knee_plank",
        name = "Knee Plank",
        primaryMuscle = "Abs",
        equipment = emptyList(),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "20-30 sec",
        defaultRestSeconds = 60
    )

    val LANDMINE_PRESS = createBasicExercise(
        id = "landmine_press",
        name = "Landmine Press",
        primaryMuscle = "Shoulders",
        secondaryMuscles = listOf("Triceps", "Upper Chest"),
        muscleGroups = listOf("Shoulders"),
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "8-12",
        defaultRestSeconds = 60
    )

    val LATERAL_BOUND = createBasicExercise(
        id = "lateral_bound",
        name = "Lateral bound",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.ATHLETE_PERFORMANCE.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "6/side",
        defaultRestSeconds = 60
    )

    val LATERAL_RAISE_MACHINE = createBasicExercise(
        id = "lateral_raise_machine",
        name = "Lateral Raise Machine",
        primaryMuscle = "Shoulders",
        equipment = listOf("Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val LATERAL_SHUFFLE = createBasicExercise(
        id = "lateral_shuffle",
        name = "Lateral shuffle",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 4,
        defaultReps = "15m",
        defaultRestSeconds = 60
    )

    val LATERAL_SPRINT_5_SIDE = createBasicExercise(
        id = "lateral_sprint_5_side",
        name = "Lateral sprint — 5/side",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 1,
        defaultReps = "1",
        defaultRestSeconds = 60
    )

    val LEAN_AWAY_LATERAL_RAISE = createBasicExercise(
        id = "lean_away_lateral_raise",
        name = "Lean-Away Lateral Raise",
        primaryMuscle = "Shoulders",
        secondaryMuscles = listOf("Side Delts"),
        muscleGroups = listOf("Shoulders"),
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val LEG_CURL = createBasicExercise(
        id = "leg_curl",
        name = "Leg Curl",
        primaryMuscle = "Legs",
        secondaryMuscles = listOf("Hamstrings"),
        muscleGroups = listOf("Legs"),
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-15",
        defaultRestSeconds = 60
    )

    val LEG_EXTENSION = createBasicExercise(
        id = "leg_extension",
        name = "Leg Extension",
        primaryMuscle = "Legs",
        secondaryMuscles = listOf("Quadriceps"),
        muscleGroups = listOf("Legs"),
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val LEG_PRESS = createBasicExercise(
        id = "leg_press",
        name = "Leg Press Machine",
        primaryMuscle = "Quadriceps",
        equipment = listOf("Leg Press Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.SQUAT.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 90
    )

    val LOW_INCLINE_MACHINE_PRESS = createBasicExercise(
        id = "low_incline_machine_press",
        name = "Low-Incline Machine Press",
        primaryMuscle = "Chest",
        secondaryMuscles = listOf("Triceps", "Front Delts"),
        muscleGroups = listOf("Chest"),
        equipment = listOf("Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val LOW_TO_HIGH_CABLE_FLY = createBasicExercise(
        id = "low_to_high_cable_fly",
        name = "Low-to-High Cable Fly",
        primaryMuscle = "Chest",
        equipment = listOf("Cable Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val LYING_LEG_CURL = createBasicExercise(
        id = "lying_leg_curl",
        name = "Lying Leg Curl",
        primaryMuscle = "Legs",
        secondaryMuscles = listOf("Hamstrings"),
        muscleGroups = listOf("Legs"),
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val MACHINE_BICEPS_CURL = createBasicExercise(
        id = "machine_biceps_curl",
        name = "Machine Biceps Curl",
        primaryMuscle = "Biceps",
        equipment = listOf("Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val MACHINE_CHEST_PRESS = createBasicExercise(
        id = "machine_chest_press",
        name = "Machine Chest Press",
        primaryMuscle = "Chest",
        equipment = listOf("Chest Press Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val MACHINE_CURL = createBasicExercise(
        id = "machine_curl",
        name = "Machine Curl",
        primaryMuscle = "Biceps",
        equipment = listOf("Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val MACHINE_LATERAL_RAISE = createBasicExercise(
        id = "machine_lateral_raise",
        name = "Machine Lateral Raise",
        primaryMuscle = "Shoulders",
        secondaryMuscles = listOf("Side Delts"),
        muscleGroups = listOf("Shoulders"),
        equipment = listOf("Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val MACHINE_PRESS = createBasicExercise(
        id = "machine_press",
        name = "Machine Press",
        primaryMuscle = "Shoulders",
        secondaryMuscles = listOf("Triceps"),
        muscleGroups = listOf("Shoulders"),
        equipment = listOf("Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val MACHINE_PULLOVER = createBasicExercise(
        id = "machine_pullover",
        name = "Machine Pullover",
        primaryMuscle = "Back",
        equipment = listOf("Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val MACHINE_ROW = createBasicExercise(
        id = "machine_row",
        name = "Machine Row",
        primaryMuscle = "Back",
        equipment = listOf("Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val MACHINE_SHOULDER_PRESS = createBasicExercise(
        id = "machine_shoulder_press",
        name = "Machine Shoulder Press",
        primaryMuscle = "Shoulders",
        equipment = listOf("Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val MED_BALL_SLAM = createBasicExercise(
        id = "med_ball_slam",
        name = "Med-ball slam",
        primaryMuscle = "Full Body",
        equipment = listOf("Medicine Ball"),
        category = ExerciseCategory.ATHLETE_PERFORMANCE.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 4,
        defaultReps = "6",
        defaultRestSeconds = 60
    )

    val MEDICINE_BALL_CHEST_THROW = createBasicExercise(
        id = "medicine_ball_chest_throw",
        name = "Medicine-ball chest throw",
        primaryMuscle = "Chest",
        equipment = listOf("Medicine Ball"),
        category = ExerciseCategory.ATHLETE_PERFORMANCE.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 4,
        defaultReps = "6",
        defaultRestSeconds = 60
    )

    val MEDICINE_BALL_ROTATIONAL_THROW = createBasicExercise(
        id = "medicine_ball_rotational_throw",
        name = "Medicine-ball rotational throw",
        primaryMuscle = "Abs",
        equipment = listOf("Medicine Ball"),
        category = ExerciseCategory.ATHLETE_PERFORMANCE.name,
        movementPattern = MovementPattern.ROTATION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "6/side",
        defaultRestSeconds = 60
    )

    val MEDICINE_BALL_SLAM = createBasicExercise(
        id = "medicine_ball_slam",
        name = "Medicine-ball slam",
        primaryMuscle = "Full Body",
        equipment = listOf("Medicine Ball"),
        category = ExerciseCategory.ATHLETE_PERFORMANCE.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 4,
        defaultReps = "6",
        defaultRestSeconds = 60
    )

    val MEDICINE_BALL_THROW = createBasicExercise(
        id = "medicine_ball_throw",
        name = "Medicine-ball throw",
        primaryMuscle = "Full Body",
        equipment = listOf("Medicine Ball"),
        category = ExerciseCategory.ATHLETE_PERFORMANCE.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 4,
        defaultReps = "6",
        defaultRestSeconds = 60
    )

    val MIRROR_DRILL = createBasicExercise(
        id = "mirror_drill",
        name = "Mirror drill",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 5,
        defaultReps = "20 sec",
        defaultRestSeconds = 60
    )

    val MOBILITY = createBasicExercise(
        id = "mobility",
        name = "Mobility",
        primaryMuscle = "Full Body",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 1,
        defaultReps = "15 min",
        defaultRestSeconds = 60
    )

    val MOBILITY_HIP_FLEXOR_HAMSTRING_CALF_THORACIC_ROTATION = createBasicExercise(
        id = "mobility_hip_flexor_hamstring_calf_thoracic_rotation",
        name = "Mobility: hip flexor, hamstring, calf, thoracic rotation",
        primaryMuscle = "Calves",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.LUNGE.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 1,
        defaultReps = "1",
        defaultRestSeconds = 60
    )

    val MOUNTAIN_CLIMBER = createBasicExercise(
        id = "mountain_climber",
        name = "Mountain Climber",
        primaryMuscle = "Abs",
        equipment = emptyList(),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "30 sec",
        defaultRestSeconds = 60
    )

    val NEUTRAL_GRIP_PULLDOWN = createBasicExercise(
        id = "neutral_grip_pulldown",
        name = "Neutral-Grip Pulldown",
        primaryMuscle = "Back",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10",
        defaultRestSeconds = 60
    )

    val NEUTRAL_GRIP_WEIGHTED_PULL_UP = createBasicExercise(
        id = "neutral_grip_weighted_pull_up",
        name = "Neutral-Grip Weighted Pull-Up",
        primaryMuscle = "Back",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 4,
        defaultReps = "5-8",
        defaultRestSeconds = 60
    )

    val ONE_ARM_CABLE_ROW = createBasicExercise(
        id = "one_arm_cable_row",
        name = "One-Arm Cable Row",
        primaryMuscle = "Back",
        equipment = listOf("Cable Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val ONE_ARM_DUMBBELL_ROW = createBasicExercise(
        id = "one_arm_dumbbell_row",
        name = "One-Arm Dumbbell Row",
        primaryMuscle = "Back",
        equipment = listOf("Dumbbells"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val OVERHEAD_CABLE_EXTENSION = createBasicExercise(
        id = "overhead_cable_extension",
        name = "Overhead Cable Extension",
        primaryMuscle = "Triceps",
        equipment = listOf("Cable Pulley"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val OVERHEAD_DUMBBELL_EXTENSION = createBasicExercise(
        id = "overhead_dumbbell_extension",
        name = "Overhead Dumbbell Extension",
        primaryMuscle = "Triceps",
        equipment = listOf("Dumbbell"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val OVERHEAD_ROPE_EXTENSION = createBasicExercise(
        id = "overhead_rope_extension",
        name = "Overhead Rope Extension",
        primaryMuscle = "Triceps",
        muscleGroups = listOf("Triceps"),
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val PALLOF_PRESS = createBasicExercise(
        id = "pallof_press",
        name = "Pallof Press",
        primaryMuscle = "Abs",
        secondaryMuscles = listOf("Obliques"),
        muscleGroups = listOf("Abs"),
        equipment = emptyList(),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.ANTI_ROTATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-15 / side",
        defaultRestSeconds = 60
    )

    val PAUSED_BENCH_PRESS = createBasicExercise(
        id = "paused_bench_press",
        name = "Paused Bench Press",
        primaryMuscle = "Chest",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 4,
        defaultReps = "4-6",
        defaultRestSeconds = 60
    )

    val PEC_DECK = createBasicExercise(
        id = "pec_deck",
        name = "Pec Deck",
        primaryMuscle = "Chest",
        equipment = listOf("Pec Deck Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val PENDLAY_ROW = createBasicExercise(
        id = "pendlay_row",
        name = "Pendlay Row",
        primaryMuscle = "Back",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 4,
        defaultReps = "6-8",
        defaultRestSeconds = 60
    )

    val PLANK_HOLD = createBasicExercise(
        id = "plank_hold",
        name = "RKC Plank Hold",
        primaryMuscle = "Abs",
        equipment = listOf("Mat"),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "30-60 sec",
        defaultRestSeconds = 60
    )

    val POGO_JUMP = createBasicExercise(
        id = "pogo_jump",
        name = "Pogo jump",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.ATHLETE_PERFORMANCE.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "20",
        defaultRestSeconds = 60
    )

    val PREACHER_CURL_MACHINE = createBasicExercise(
        id = "preacher_curl_machine",
        name = "Preacher Curl Machine",
        primaryMuscle = "Biceps",
        equipment = listOf("Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val PULL_UPS = createBasicExercise(
        id = "pull_ups",
        name = "Pull-Ups",
        primaryMuscle = "Back",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 4,
        defaultReps = "6-10",
        defaultRestSeconds = 60
    )

    val PUSH_PRESS = createBasicExercise(
        id = "push_press",
        name = "Push Press",
        primaryMuscle = "Shoulders",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "5",
        defaultRestSeconds = 60
    )

    val PUSH_UPS = createBasicExercise(
        id = "push_ups",
        name = "Push-ups",
        primaryMuscle = "Chest",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12",
        defaultRestSeconds = 60
    )

    val REACTION_CONE_DRILL = createBasicExercise(
        id = "reaction_cone_drill",
        name = "Reaction cone drill",
        primaryMuscle = "Legs",
        equipment = listOf("Cones"),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 1,
        defaultReps = "6 reps",
        defaultRestSeconds = 60
    )

    val REACTIVE_CONE_DRILL = createBasicExercise(
        id = "reactive_cone_drill",
        name = "Reactive cone drill",
        primaryMuscle = "Legs",
        equipment = listOf("Cones"),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 6,
        defaultReps = "1 rep",
        defaultRestSeconds = 60
    )

    val REACTIVE_SPRINT = createBasicExercise(
        id = "reactive_sprint",
        name = "Reactive sprint",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 6,
        defaultReps = "15m",
        defaultRestSeconds = 60
    )

    val REAR_DELT_CABLE_FLY = createBasicExercise(
        id = "rear_delt_cable_fly",
        name = "Rear-Delt Cable Fly",
        primaryMuscle = "Shoulders",
        equipment = listOf("Cable Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val RESISTED_SPRINT = createBasicExercise(
        id = "resisted_sprint",
        name = "Resisted sprint",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 4,
        defaultReps = "15m",
        defaultRestSeconds = 60
    )

    val REVERSE_CABLE_FLY = createBasicExercise(
        id = "reverse_cable_fly",
        name = "Reverse Cable Fly",
        primaryMuscle = "Shoulders",
        equipment = listOf("Cable Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val REVERSE_CRUNCH = createBasicExercise(
        id = "reverse_crunch",
        name = "Reverse Crunch",
        primaryMuscle = "Abs",
        equipment = emptyList(),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.FLEXION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val REVERSE_CURL = createBasicExercise(
        id = "reverse_curl",
        name = "Reverse Curl",
        primaryMuscle = "Biceps",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val REVERSE_GRIP_PUSHDOWN = createBasicExercise(
        id = "reverse_grip_pushdown",
        name = "Reverse-Grip Pushdown",
        primaryMuscle = "Triceps",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val REVERSE_LUNGE = createBasicExercise(
        id = "reverse_lunge",
        name = "Reverse Lunge",
        primaryMuscle = "Quadriceps",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.LUNGE.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10 / leg",
        defaultRestSeconds = 60
    )

    val REVERSE_PEC_DECK = createBasicExercise(
        id = "reverse_pec_deck",
        name = "Reverse Pec Deck",
        primaryMuscle = "Shoulders",
        equipment = listOf("Pec Deck Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val ROMANIAN_DEADLIFT = createBasicExercise(
        id = "romanian_deadlift",
        name = "Romanian Deadlift (RDL)",
        primaryMuscle = "Hamstrings",
        equipment = listOf("Barbell"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HINGE.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 90
    )

    val ROPE_PUSHDOWN = createBasicExercise(
        id = "rope_pushdown",
        name = "Rope Pushdown",
        primaryMuscle = "Triceps",
        equipment = listOf("Cable Pulley", "Rope"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val ROPE_TRICEPS_PUSHDOWN = createBasicExercise(
        id = "rope_triceps_pushdown",
        name = "Rope Triceps Pushdown",
        primaryMuscle = "Triceps",
        equipment = listOf("Cable Pulley", "Rope"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val ROTATIONAL_THROW = createBasicExercise(
        id = "rotational_throw",
        name = "Rotational throw",
        primaryMuscle = "Abs",
        equipment = emptyList(),
        category = ExerciseCategory.ATHLETE_PERFORMANCE.name,
        movementPattern = MovementPattern.ROTATION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "6/side",
        defaultRestSeconds = 60
    )

    val RUN_CYCLE = createBasicExercise(
        id = "run_cycle",
        name = "Run/cycle",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 1,
        defaultReps = "35-45 min",
        defaultRestSeconds = 60
    )

    val RUSSIAN_TWIST = createBasicExercise(
        id = "russian_twist",
        name = "Russian Twist",
        primaryMuscle = "Abs",
        equipment = emptyList(),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12-15 / side",
        defaultRestSeconds = 60
    )

    val RUSSIAN_TWISTS = createBasicExercise(
        id = "russian_twists",
        name = "Weighted Russian Twists",
        primaryMuscle = "Abs",
        equipment = listOf("Weight Plate"),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.ROTATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "15-20",
        defaultRestSeconds = 60
    )

    val SEATED_CABLE_ROW = createBasicExercise(
        id = "seated_cable_row",
        name = "Seated Cable Row",
        primaryMuscle = "Back",
        equipment = listOf("Cable Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val SEATED_CALF_RAISE = createBasicExercise(
        id = "seated_calf_raise",
        name = "Seated Calf Raise",
        primaryMuscle = "Calves",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.LUNGE.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12-20",
        defaultRestSeconds = 60
    )

    val SEATED_DUMBBELL_PRESS = createBasicExercise(
        id = "seated_dumbbell_press",
        name = "Seated Dumbbell Overhead Press",
        primaryMuscle = "Shoulders",
        equipment = listOf("Dumbbells", "Bench"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val SEATED_LEG_CURL = createBasicExercise(
        id = "seated_leg_curl",
        name = "Seated Leg Curl",
        primaryMuscle = "Legs",
        secondaryMuscles = listOf("Hamstrings"),
        muscleGroups = listOf("Legs"),
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val SEATED_MACHINE_PRESS = createBasicExercise(
        id = "seated_machine_press",
        name = "Seated Machine Press",
        primaryMuscle = "Shoulders",
        secondaryMuscles = listOf("Triceps"),
        muscleGroups = listOf("Shoulders"),
        equipment = listOf("Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val SHRUG = createBasicExercise(
        id = "shrug",
        name = "Shrug",
        primaryMuscle = "Shoulders",
        secondaryMuscles = listOf("Traps"),
        muscleGroups = listOf("Shoulders"),
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-15",
        defaultRestSeconds = 60
    )

    val SIDE_PLANK = createBasicExercise(
        id = "side_plank",
        name = "Side Plank",
        primaryMuscle = "Abs",
        equipment = emptyList(),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "20-30 sec / side",
        defaultRestSeconds = 60
    )

    val SIDE_PLANK_3_30_SEC_SIDE = createBasicExercise(
        id = "side_plank_3_30_sec_side",
        name = "Side Plank 3×30 sec/side",
        primaryMuscle = "Abs",
        equipment = emptyList(),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val SINGLE_ARM_CABLE_EXTENSION = createBasicExercise(
        id = "single_arm_cable_extension",
        name = "Single-Arm Cable Extension",
        primaryMuscle = "Triceps",
        equipment = listOf("Cable Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val SINGLE_ARM_CABLE_ROW = createBasicExercise(
        id = "single_arm_cable_row",
        name = "Single-Arm Cable Row",
        primaryMuscle = "Back",
        equipment = listOf("Cable Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12",
        defaultRestSeconds = 60
    )

    val SINGLE_ARM_DUMBBELL_ROW = createBasicExercise(
        id = "single_arm_dumbbell_row",
        name = "Single-Arm Dumbbell Row",
        primaryMuscle = "Back",
        equipment = listOf("Dumbbells"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val SINGLE_ARM_LAT_PULLDOWN = createBasicExercise(
        id = "single_arm_lat_pulldown",
        name = "Single-Arm Lat Pulldown",
        primaryMuscle = "Back",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val SINGLE_ARM_OVERHEAD_CABLE_EXTENSION = createBasicExercise(
        id = "single_arm_overhead_cable_extension",
        name = "Single-Arm Overhead Cable Extension",
        primaryMuscle = "Triceps",
        muscleGroups = listOf("Triceps"),
        equipment = listOf("Cable Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val SINGLE_ARM_PUSHDOWN = createBasicExercise(
        id = "single_arm_pushdown",
        name = "Single-Arm Pushdown",
        primaryMuscle = "Triceps",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val SKULL_CRUSHERS = createBasicExercise(
        id = "skull_crushers",
        name = "Skull Crushers",
        primaryMuscle = "Triceps",
        equipment = listOf("EZ Bar", "Bench"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "8-12",
        defaultRestSeconds = 90
    )

    val SPIDER_CURL = createBasicExercise(
        id = "spider_curl",
        name = "Spider Curl",
        primaryMuscle = "Biceps",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12",
        defaultRestSeconds = 60
    )

    val SPLIT_JUMP = createBasicExercise(
        id = "split_jump",
        name = "Split jump",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.ATHLETE_PERFORMANCE.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "5/leg",
        defaultRestSeconds = 60
    )

    val SQUAT = createBasicExercise(
        id = "squat",
        name = "Barbell Back Squat",
        primaryMuscle = "Quadriceps",
        equipment = listOf("Barbell", "Squat Rack"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.SQUAT.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "8-10",
        defaultRestSeconds = 120
    )

    val STANDING_BROAD_JUMP = createBasicExercise(
        id = "standing_broad_jump",
        name = "Standing Broad Jump",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.ATHLETE_PERFORMANCE.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "1 attempt",
        defaultRestSeconds = 60
    )

    val STANDING_CALF_RAISE = createBasicExercise(
        id = "standing_calf_raise",
        name = "Standing Calf Raises",
        primaryMuscle = "Calves",
        equipment = listOf("Calf Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "15-20",
        defaultRestSeconds = 60
    )

    val STANDING_OVERHEAD_PRESS = createBasicExercise(
        id = "standing_overhead_press",
        name = "Standing Overhead Press",
        primaryMuscle = "Shoulders",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 4,
        defaultReps = "5-8",
        defaultRestSeconds = 60
    )

    val STRAIGHT_ARM_PULLDOWN = createBasicExercise(
        id = "straight_arm_pulldown",
        name = "Straight-Arm Pulldown",
        primaryMuscle = "Back",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val STRIDES = createBasicExercise(
        id = "strides",
        name = "Strides",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 4,
        defaultReps = "60m",
        defaultRestSeconds = 60
    )

    val SUPPORTED_ROW_MACHINE = createBasicExercise(
        id = "supported_row_machine",
        name = "Supported Row Machine",
        primaryMuscle = "Back",
        equipment = listOf("Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val SUPPORTED_SPLIT_SQUAT = createBasicExercise(
        id = "supported_split_squat",
        name = "Supported Split Squat",
        primaryMuscle = "Quadriceps",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.SQUAT.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "10 / leg",
        defaultRestSeconds = 60
    )

    val T_BAR_ROW = createBasicExercise(
        id = "t_bar_row",
        name = "T-Bar Row",
        primaryMuscle = "Back",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "8-12",
        defaultRestSeconds = 60
    )

    val T_DRILL = createBasicExercise(
        id = "t_drill",
        name = "T-drill",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 1,
        defaultReps = "4 reps",
        defaultRestSeconds = 60
    )

    val TEN_METER_ACCELERATION = createBasicExercise(
        id = "ten_meter_acceleration",
        name = "10m acceleration",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 1,
        defaultReps = "6 reps",
        defaultRestSeconds = 60
    )

    val TEN_METER_SPRINT = createBasicExercise(
        id = "ten_meter_sprint",
        name = "10m sprints",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 6,
        defaultReps = "1 rep",
        defaultRestSeconds = 60
    )

    val THIRTY_METER_SPRINT = createBasicExercise(
        id = "thirty_meter_sprint",
        name = "30m sprint",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 1,
        defaultReps = "4 reps",
        defaultRestSeconds = 60
    )

    val TRICEPS_MACHINE = createBasicExercise(
        id = "triceps_machine",
        name = "Triceps Machine",
        primaryMuscle = "Triceps",
        equipment = listOf("Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "12-15",
        defaultRestSeconds = 60
    )

    val TWENTY_METER_SPRINT = createBasicExercise(
        id = "twenty_meter_sprint",
        name = "20m sprint",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 1,
        defaultReps = "5 reps",
        defaultRestSeconds = 60
    )

    val VERTICAL_JUMP = createBasicExercise(
        id = "vertical_jump",
        name = "Vertical Jump",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.ATHLETE_PERFORMANCE.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "1 attempt",
        defaultRestSeconds = 60
    )

    val WALKING_LUNGES = createBasicExercise(
        id = "walking_lunges",
        name = "Walking Lunge",
        primaryMuscle = "Quadriceps",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.LUNGE.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10 / leg",
        defaultRestSeconds = 60
    )

    val WALL_SPRINT_DRILL = createBasicExercise(
        id = "wall_sprint_drill",
        name = "Wall sprint drill",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "10 sec",
        defaultRestSeconds = 60
    )

    val WEIGHTED_CABLE_CRUNCH = createBasicExercise(
        id = "weighted_cable_crunch",
        name = "Weighted Cable Crunch",
        primaryMuscle = "Abs",
        equipment = listOf("Cable Machine"),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.FLEXION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 4,
        defaultReps = "8-12",
        defaultRestSeconds = 60
    )

    val WEIGHTED_CHEST_DIPS = createBasicExercise(
        id = "weighted_chest_dips",
        name = "Weighted Chest Dips",
        primaryMuscle = "Chest",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "6-10",
        defaultRestSeconds = 60
    )

    val WEIGHTED_CHIN_UP = createBasicExercise(
        id = "weighted_chin_up",
        name = "Weighted Chin-Up",
        primaryMuscle = "Back",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 4,
        defaultReps = "5",
        defaultRestSeconds = 60
    )

    val WEIGHTED_DIPS = createBasicExercise(
        id = "weighted_dips",
        name = "Weighted Dips",
        primaryMuscle = "Triceps",
        secondaryMuscles = listOf("Chest"),
        muscleGroups = listOf("Triceps"),
        equipment = listOf("Dip Bars", "Dip Belt"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "6-10",
        defaultRestSeconds = 90
    )

    val WEIGHTED_PULL_UPS = createBasicExercise(
        id = "weighted_pull_ups",
        name = "Weighted Pull-Ups",
        primaryMuscle = "Back",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 4,
        defaultReps = "5-8",
        defaultRestSeconds = 60
    )

    val WEIGHTED_RUSSIAN_TWIST = createBasicExercise(
        id = "weighted_russian_twist",
        name = "Weighted Russian Twist",
        primaryMuscle = "Abs",
        equipment = emptyList(),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-15 / side",
        defaultRestSeconds = 60
    )

    val WEIGHTED_SIDE_BEND = createBasicExercise(
        id = "weighted_side_bend",
        name = "Weighted Side Bend",
        primaryMuscle = "Abs",
        equipment = emptyList(),
        category = ExerciseCategory.CORE.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-15",
        defaultRestSeconds = 60
    )

    val WIDE_GRIP_LAT_PULLDOWN = createBasicExercise(
        id = "wide_grip_lat_pulldown",
        name = "Wide-Grip Lat Pulldown",
        primaryMuscle = "Back",
        equipment = listOf("Lat Pulldown Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10-12",
        defaultRestSeconds = 60
    )

    val YO_YO_BEEP_STYLE_CONDITIONING_TEST = createBasicExercise(
        id = "yo_yo_beep_style_conditioning_test",
        name = "Yo-Yo/beep-style conditioning test",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 1,
        defaultReps = "1",
        defaultRestSeconds = 60
    )

    val DUMBBELL_BICEP_CURL = createBasicExercise(
        id = "dumbbell_bicep_curl",
        name = "Dumbbell Bicep Curl",
        primaryMuscle = "Biceps",
        equipment = listOf("Dumbbell"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12",
        defaultRestSeconds = 60
    )

    val INCLINE_TREADMILL = createBasicExercise(
        id = "incline_treadmill",
        name = "Incline Treadmill",
        primaryMuscle = "Legs",
        equipment = listOf("Treadmill"),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 1,
        defaultReps = "15 min",
        defaultRestSeconds = 60
    )

    val EASY_CARDIO = createBasicExercise(
        id = "easy_cardio",
        name = "Easy Cardio",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 1,
        defaultReps = "20-30 min",
        defaultRestSeconds = 60
    )

    val AIR_SQUAT = createBasicExercise(
        id = "air_squat",
        name = "Air Squat",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.SQUAT.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "15",
        defaultRestSeconds = 45
    )

    val BATTLE_ROPE = createBasicExercise(
        id = "battle_rope",
        name = "Battle Rope",
        primaryMuscle = "Full Body",
        equipment = listOf("Battle Rope"),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 6,
        defaultReps = "30 sec",
        defaultRestSeconds = 30
    )

    val BURPEES = createBasicExercise(
        id = "burpees",
        name = "Burpees",
        primaryMuscle = "Full Body",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "8-10",
        defaultRestSeconds = 60
    )

    val CABLE_GLUTE_KICKBACK = createBasicExercise(
        id = "cable_glute_kickback",
        name = "Cable Glute Kickback",
        primaryMuscle = "Glutes",
        equipment = listOf("Cable Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "15",
        defaultRestSeconds = 45
    )

    val HIGH_KNEES = createBasicExercise(
        id = "high_knees",
        name = "High Knees",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 4,
        defaultReps = "30 sec",
        defaultRestSeconds = 30
    )

    val INCLINE_WALK = createBasicExercise(
        id = "incline_walk",
        name = "Incline Walk",
        primaryMuscle = "Legs",
        equipment = listOf("Treadmill"),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.LOCOMOTION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 1,
        defaultReps = "15 min",
        defaultRestSeconds = 60
    )

    val STEP_UP = createBasicExercise(
        id = "step_up",
        name = "Step-Up",
        primaryMuscle = "Legs",
        equipment = listOf("Bench"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.LUNGE.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10/leg",
        defaultRestSeconds = 60
    )

    val LAT_PULLDOWN = createBasicExercise(
        id = "lat_pulldown",
        name = "Lat Pulldown",
        primaryMuscle = "Back",
        equipment = listOf("Lat Pulldown Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PULL.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12",
        defaultRestSeconds = 60
    )

    val TRICEPS_PUSHDOWN = createBasicExercise(
        id = "triceps_pushdown",
        name = "Triceps Pushdown",
        primaryMuscle = "Triceps",
        equipment = listOf("Cable Machine"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12",
        defaultRestSeconds = 60
    )

    val HANG_POWER_CLEAN_TECHNIQUE = createBasicExercise(
        id = "hang_power_clean_technique",
        name = "Hang Power Clean technique",
        primaryMuscle = "Full Body",
        equipment = listOf("Barbell"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PULL.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "5",
        defaultRestSeconds = 90
    )

    val PUSH_PRESS_TECHNIQUE = createBasicExercise(
        id = "push_press_technique",
        name = "Push Press technique",
        primaryMuscle = "Shoulders",
        equipment = listOf("Barbell"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PUSH.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "5",
        defaultRestSeconds = 90
    )

    val ROW_INTERVALS = createBasicExercise(
        id = "row_intervals",
        name = "Row intervals",
        primaryMuscle = "Back",
        equipment = listOf("Rowing Machine"),
        category = ExerciseCategory.CARDIO.name,
        movementPattern = MovementPattern.HORIZONTAL_PULL.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 5,
        defaultReps = "500m",
        defaultRestSeconds = 60
    )

    val DEAD_BUG_PROGRESSION = createBasicExercise(
        id = "dead_bug_progression",
        name = "Dead Bug progression",
        primaryMuscle = "Abs",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "8/side",
        defaultRestSeconds = 60
    )

    val FROG_PUMPS = createBasicExercise(
        id = "frog_pumps",
        name = "Frog Pumps",
        primaryMuscle = "Glutes",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "20",
        defaultRestSeconds = 45
    )

    val SINGLE_LEG_GLUTE_BRIDGE = createBasicExercise(
        id = "single_leg_glute_bridge",
        name = "Single-Leg Glute Bridge",
        primaryMuscle = "Glutes",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HINGE.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "10/leg",
        defaultRestSeconds = 60
    )

    val SUMO_SQUAT = createBasicExercise(
        id = "sumo_squat",
        name = "Sumo Squat",
        primaryMuscle = "Legs",
        equipment = listOf("Dumbbell"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.SQUAT.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12",
        defaultRestSeconds = 60
    )

    val PAUSE_SQUAT = createBasicExercise(
        id = "pause_squat",
        name = "Pause Squat",
        primaryMuscle = "Legs",
        equipment = listOf("Barbell"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.SQUAT.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "5",
        defaultRestSeconds = 90
    )

    val SUPPORTED_REVERSE_LUNGE = createBasicExercise(
        id = "supported_reverse_lunge",
        name = "Supported Reverse Lunge",
        primaryMuscle = "Legs",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.LUNGE.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "8/leg",
        defaultRestSeconds = 60
    )

    val SUPPORTED_STEP_UP = createBasicExercise(
        id = "supported_step_up",
        name = "Supported Step-Up",
        primaryMuscle = "Legs",
        equipment = listOf("Bench"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.LUNGE.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "8/leg",
        defaultRestSeconds = 60
    )

    val HOLLOW_BODY_HOLD = createBasicExercise(
        id = "hollow_body_hold",
        name = "Hollow Body Hold",
        primaryMuscle = "Abs",
        equipment = emptyList(),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "30 sec",
        defaultRestSeconds = 60
    )

    val HANGING_KNEE_RAISE = createBasicExercise(
        id = "hanging_knee_raise",
        name = "Hanging Knee Raise",
        primaryMuscle = "Abs",
        equipment = listOf("Pull-Up Bar"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "12",
        defaultRestSeconds = 60
    )

    val DIAPHRAGMATIC_BREATHING = createBasicExercise(
        id = "diaphragmatic_breathing",
        name = "Diaphragmatic Breathing",
        primaryMuscle = "Core",
        equipment = emptyList(),
        category = ExerciseCategory.MOBILITY.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 1,
        defaultReps = "5 min",
        defaultRestSeconds = 30
    )

    val PELVIC_FLOOR_ACTIVATION = createBasicExercise(
        id = "pelvic_floor_activation",
        name = "Pelvic Floor Relaxation/Activation",
        primaryMuscle = "Core",
        equipment = emptyList(),
        category = ExerciseCategory.MOBILITY.name,
        movementPattern = MovementPattern.ISOLATION.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 1,
        defaultReps = "5 min",
        defaultRestSeconds = 30
    )

    val LIGHT_DB_PRESS = createBasicExercise(
        id = "light_db_press",
        name = "Light DB Press",
        primaryMuscle = "Shoulders",
        equipment = listOf("Dumbbell"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.VERTICAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "10",
        defaultRestSeconds = 60
    )

    val LIGHT_DB_CHEST_PRESS = createBasicExercise(
        id = "light_db_chest_press",
        name = "Light DB Chest Press",
        primaryMuscle = "Chest",
        equipment = listOf("Dumbbell"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 2,
        defaultReps = "10",
        defaultRestSeconds = 60
    )

    val TECHNIQUE_BENCH = createBasicExercise(
        id = "technique_bench",
        name = "Technique Bench",
        primaryMuscle = "Chest",
        equipment = listOf("Barbell"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
        difficulty = ExerciseDifficulty.BEGINNER.name,
        defaultSets = 3,
        defaultReps = "5",
        defaultRestSeconds = 90
    )

    val DUMBBELL_THRUSTER = createBasicExercise(
        id = "dumbbell_thruster",
        name = "DB Thrusters",
        primaryMuscle = "Full Body",
        secondaryMuscles = listOf("Quadriceps", "Shoulders", "Glutes"),
        muscleGroups = listOf("Full Body", "Legs", "Shoulders"),
        equipment = listOf("Dumbbell"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.SQUAT.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "12",
        defaultRestSeconds = 60
    )

    val DUMBBELL_SNATCH = createBasicExercise(
        id = "dumbbell_snatch",
        name = "DB Snatches",
        primaryMuscle = "Full Body",
        secondaryMuscles = listOf("Shoulders", "Hamstrings", "Glutes", "Back"),
        muscleGroups = listOf("Full Body", "Shoulders", "Back"),
        equipment = listOf("Dumbbell"),
        category = ExerciseCategory.STRENGTH.name,
        movementPattern = MovementPattern.HINGE.name,
        difficulty = ExerciseDifficulty.ADVANCED.name,
        defaultSets = 3,
        defaultReps = "8-10",
        defaultRestSeconds = 60
    )

    val INITIAL_EXERCISES: List<Exercise> = listOf(
        BARBELL_BENCH_PRESS,
        SQUAT,
        DEADLIFT
    )

    val ALL_EXERCISES: List<Exercise> = listOf(
        EX_4_60M_RELAXED_STRIDES,
        EX_4_ROUNDS_250M_RUN_15_KETTLEBELL_SWINGS_12_BURPEES_15_WALKING_LUNGES_10_PUSH_UPS_30_SEC_BATTLE_ROPES,
        EX_5_10_5,
        EX_5_10_5_SHUTTLE,
        EX_5_5_MIN_MODERATE_RUNNING,
        EX_5_60M_RELAXED_STRIDES,
        EX_5_ROUNDS_200M_RUN_10_BURPEES_15_KB_SWINGS_12_PUSH_UPS_15_AIR_SQUATS_30_SEC_BATTLE_ROPES,
        EX_5_ROUNDS_300M_RUN_12_KB_SWINGS_10_BURPEES_15_WALKING_LUNGES_10_PUSH_UPS_30_SEC_BATTLE_ROPES,
        EX_6_ROUNDS_30_SEC_HARD_RUN_10_BURPEES_15_KB_SWINGS_10_PUSH_UPS_15_SQUATS,
        A_SKIP,
        AB_ROLLOUT,
        ARNOLD_PRESS,
        ASSISTED_PULLDOWN,
        BACK_EXTENSION,
        BACKPEDAL_SPRINT,
        BARBELL_BENCH_PRESS,
        BARBELL_BICEP_CURL,
        BARBELL_HIP_THRUST,
        BARBELL_OVERHEAD_PRESS,
        BARBELL_SHRUG,
        BAYESIAN_CABLE_CURL,
        BAYESIAN_CURL,
        BENT_OVER_BARBELL_ROW,
        BICYCLE_CRUNCH,
        BIRD_DOG,
        BOUNDING,
        BOUNDS,
        BOX_JUMP,
        BROAD_JUMP,
        BUILD_UP_RUNS,
        BULGARIAN_SPLIT_SQUAT,
        CABLE_CONCENTRATION_CURL,
        CABLE_CROSSOVER,
        CABLE_CRUNCH,
        CABLE_CURL,
        CABLE_FLY,
        CABLE_HAMMER_CURL,
        CABLE_LATERAL_RAISE,
        CABLE_OVERHEAD_EXTENSION,
        CABLE_TRICEPS_EXTENSION,
        CABLE_WOODCHOP,
        CABLE_WOODCHOPPERS,
        CHEST_DIPS,
        CHEST_PRESS_MACHINE,
        CHEST_SUPPORTED_MACHINE_PRESS,
        CHEST_SUPPORTED_MACHINE_ROW,
        CHEST_SUPPORTED_ROW,
        CLOSE_GRIP_BENCH_PRESS,
        CLOSE_GRIP_LAT_PULLDOWN,
        CLOSE_GRIP_PULLDOWN,
        CLOSE_GRIP_PUSH_UP,
        CLOSE_GRIP_WEIGHTED_PULL_UP,
        CONE_REACTION_DRILL,
        COPENHAGEN_PLANK_3_20_SEC_SIDE,
        CORE,
        CORE_CIRCUIT_3_ROUNDS,
        DEAD_BUG,
        DEADLIFT,
        DECELERATION_SPRINT,
        DECLINE_BARBELL_PRESS,
        DECLINE_DUMBBELL_PRESS,
        DECLINE_MACHINE_PRESS,
        DECLINE_PRESS,
        DEPTH_DROP_LANDING,
        DUMBBELL_BENCH,
        DUMBBELL_BENCH_PRESS,
        DUMBBELL_FLY,
        DUMBBELL_GOBLET_SQUAT,
        DUMBBELL_HAMMER_CURL,
        DUMBBELL_LATERAL_RAISE,
        DUMBBELL_PRESS,
        DUMBBELL_ROMANIAN_DEADLIFT,
        DUMBBELL_ROW,
        DUMBBELL_SHOULDER_PRESS,
        DUMBBELL_SHRUGS,
        EASY_MODERATE_RUN,
        EASY_RUN_CYCLE,
        EXPLOSIVE_PUSH_UP,
        EZ_BAR_CURL,
        EZ_BAR_PREACHER_CURL,
        FACE_PULLS,
        FALLING_STARTS,
        FARMER_CARRY,
        FLAT_BARBELL_BENCH_PRESS,
        FLAT_DUMBBELL_PRESS,
        FLAT_MACHINE_PRESS,
        FLYING_20M,
        FLYING_20M_SPRINT,
        FORTY_METER_SPRINT,
        FRONT_FOOT_ELEVATED_SPLIT_SQUAT,
        FRONT_SQUAT,
        GLUTE_BRIDGE,
        GOBLET_SQUAT,
        HACK_SQUAT,
        HACK_SQUAT_MACHINE,
        HANG_POWER_CLEAN,
        HANGING_KNEE_TO_ELBOW_RAISE,
        HANGING_LEG_RAISE,
        HEEL_TAPS,
        HILL_SPRINT,
        HIP_ABDUCTION,
        HIP_ADDUCTION,
        HIP_THRUST,
        HYPERTROPHY_PERFORMANCE_CHECK,
        INCLINE_BARBELL_PRESS,
        INCLINE_CHEST_PRESS,
        INCLINE_DUMBBELL_BICEP_CURL,
        INCLINE_DUMBBELL_PRESS,
        INCLINE_MACHINE_PRESS,
        JM_PRESS,
        JUMP_SQUAT,
        KB_SWING,
        KETTLEBELL_SWING,
        KNEE_PLANK,
        LANDMINE_PRESS,
        LATERAL_BOUND,
        LATERAL_RAISE_MACHINE,
        LATERAL_SHUFFLE,
        LATERAL_SPRINT_5_SIDE,
        LEAN_AWAY_LATERAL_RAISE,
        LEG_CURL,
        LEG_EXTENSION,
        LEG_PRESS,
        LOW_INCLINE_MACHINE_PRESS,
        LOW_TO_HIGH_CABLE_FLY,
        LYING_LEG_CURL,
        MACHINE_BICEPS_CURL,
        MACHINE_CHEST_PRESS,
        MACHINE_CURL,
        MACHINE_LATERAL_RAISE,
        MACHINE_PRESS,
        MACHINE_PULLOVER,
        MACHINE_ROW,
        MACHINE_SHOULDER_PRESS,
        MED_BALL_SLAM,
        MEDICINE_BALL_CHEST_THROW,
        MEDICINE_BALL_ROTATIONAL_THROW,
        MEDICINE_BALL_SLAM,
        MEDICINE_BALL_THROW,
        MIRROR_DRILL,
        MOBILITY,
        MOBILITY_HIP_FLEXOR_HAMSTRING_CALF_THORACIC_ROTATION,
        MOUNTAIN_CLIMBER,
        NEUTRAL_GRIP_PULLDOWN,
        NEUTRAL_GRIP_WEIGHTED_PULL_UP,
        ONE_ARM_CABLE_ROW,
        ONE_ARM_DUMBBELL_ROW,
        OVERHEAD_CABLE_EXTENSION,
        OVERHEAD_DUMBBELL_EXTENSION,
        OVERHEAD_ROPE_EXTENSION,
        PALLOF_PRESS,
        PAUSED_BENCH_PRESS,
        PEC_DECK,
        PENDLAY_ROW,
        PLANK_HOLD,
        POGO_JUMP,
        PREACHER_CURL_MACHINE,
        PULL_UPS,
        PUSH_PRESS,
        PUSH_UPS,
        REACTION_CONE_DRILL,
        REACTIVE_CONE_DRILL,
        REACTIVE_SPRINT,
        REAR_DELT_CABLE_FLY,
        RESISTED_SPRINT,
        REVERSE_CABLE_FLY,
        REVERSE_CRUNCH,
        REVERSE_CURL,
        REVERSE_GRIP_PUSHDOWN,
        REVERSE_LUNGE,
        REVERSE_PEC_DECK,
        ROMANIAN_DEADLIFT,
        ROPE_PUSHDOWN,
        ROPE_TRICEPS_PUSHDOWN,
        ROTATIONAL_THROW,
        RUN_CYCLE,
        RUSSIAN_TWIST,
        RUSSIAN_TWISTS,
        SEATED_CABLE_ROW,
        SEATED_CALF_RAISE,
        SEATED_DUMBBELL_PRESS,
        SEATED_LEG_CURL,
        SEATED_MACHINE_PRESS,
        SHRUG,
        SIDE_PLANK,
        SIDE_PLANK_3_30_SEC_SIDE,
        SINGLE_ARM_CABLE_EXTENSION,
        SINGLE_ARM_CABLE_ROW,
        SINGLE_ARM_DUMBBELL_ROW,
        SINGLE_ARM_LAT_PULLDOWN,
        SINGLE_ARM_OVERHEAD_CABLE_EXTENSION,
        SINGLE_ARM_PUSHDOWN,
        SKULL_CRUSHERS,
        SPIDER_CURL,
        SPLIT_JUMP,
        SQUAT,
        STANDING_BROAD_JUMP,
        STANDING_CALF_RAISE,
        STANDING_OVERHEAD_PRESS,
        STRAIGHT_ARM_PULLDOWN,
        STRIDES,
        SUPPORTED_ROW_MACHINE,
        SUPPORTED_SPLIT_SQUAT,
        T_BAR_ROW,
        T_DRILL,
        TEN_METER_ACCELERATION,
        TEN_METER_SPRINT,
        THIRTY_METER_SPRINT,
        TRICEPS_MACHINE,
        TWENTY_METER_SPRINT,
        VERTICAL_JUMP,
        WALKING_LUNGES,
        WALL_SPRINT_DRILL,
        WEIGHTED_CABLE_CRUNCH,
        WEIGHTED_CHEST_DIPS,
        WEIGHTED_CHIN_UP,
        WEIGHTED_DIPS,
        WEIGHTED_PULL_UPS,
        WEIGHTED_RUSSIAN_TWIST,
        WEIGHTED_SIDE_BEND,
        WIDE_GRIP_LAT_PULLDOWN,
        YO_YO_BEEP_STYLE_CONDITIONING_TEST,
    )

    val FEMALE_SPECIFIC_EXERCISES: List<Exercise> = listOf(
        DUMBBELL_BICEP_CURL,
        INCLINE_TREADMILL,
        EASY_CARDIO,
        AIR_SQUAT,
        BATTLE_ROPE,
        BURPEES,
        CABLE_GLUTE_KICKBACK,
        HIGH_KNEES,
        INCLINE_WALK,
        STEP_UP,
        LAT_PULLDOWN,
        TRICEPS_PUSHDOWN,
        HANG_POWER_CLEAN_TECHNIQUE,
        PUSH_PRESS_TECHNIQUE,
        ROW_INTERVALS,
        DEAD_BUG_PROGRESSION,
        FROG_PUMPS,
        SINGLE_LEG_GLUTE_BRIDGE,
        SUMO_SQUAT,
        PAUSE_SQUAT,
        SUPPORTED_REVERSE_LUNGE,
        SUPPORTED_STEP_UP,
        HOLLOW_BODY_HOLD,
        HANGING_KNEE_RAISE,
        DIAPHRAGMATIC_BREATHING,
        PELVIC_FLOOR_ACTIVATION,
        LIGHT_DB_PRESS,
        LIGHT_DB_CHEST_PRESS,
        TECHNIQUE_BENCH,
        DUMBBELL_THRUSTER,
        DUMBBELL_SNATCH,
    )

    private val baselineExercises: Map<String, Exercise> by lazy {
        (ALL_EXERCISES + FEMALE_SPECIFIC_EXERCISES).associateBy { it.id }
    }

    /**
     * Observable revision counter for Jetpack Compose reactivity.
     * Whenever an exercise is updated or synced, this increments, triggering
     * recomposition of all UI screens consuming canonical exercises.
     */
    var canonicalRevision by mutableIntStateOf(0)
        private set

    /**
     * Authoritative alias mapping from legacy workout catalog IDs and synonyms
     * to canonical Exercise IDs.
     */
    val legacyAliasMap: Map<String, String> = mapOf(
        // Gym Chest
        "gym_chest_1" to "barbell_bench_press",
        "gym_chest_2" to "incline_dumbbell_press",
        "gym_chest_3" to "cable_fly",
        // Gym Back
        "gym_back_1" to "wide_grip_lat_pulldown",
        "gym_back_2" to "seated_cable_row",
        "gym_back_3" to "bent_over_barbell_row",
        // Gym Shoulders
        "gym_sh_1" to "dumbbell_shoulder_press",
        "gym_sh_2" to "dumbbell_lateral_raise",
        "gym_sh_3" to "face_pulls",
        // Gym Biceps
        "gym_bi_1" to "barbell_bicep_curl",
        "gym_bi_2" to "incline_dumbbell_bicep_curl",
        "gym_bi_3" to "ez_bar_preacher_curl",
        // Gym Triceps
        "gym_tri_1" to "rope_pushdown",
        "gym_tri_2" to "skull_crushers",
        "gym_tri_3" to "overhead_dumbbell_tricep_extension",
        // Gym Legs
        "gym_leg_1" to "squat",
        "gym_leg_2" to "leg_press",
        "gym_leg_3" to "romanian_deadlift",
        // Gym Abs
        "gym_abs_1" to "hanging_leg_raise",
        "gym_abs_2" to "cable_crunch",
        "gym_abs_3" to "ab_rollout",
        // Gym Full Body
        "gym_fb_1" to "deadlift",
        "gym_fb_2" to "hang_power_clean",
        "gym_fb_3" to "kettlebell_swing",

        // Home Chest
        "home_chest_1" to "push_ups",
        "home_chest_2" to "close_grip_push_up",
        "home_chest_3" to "push_ups",
        // Home Back
        "home_back_1" to "superman_hold",
        "home_back_2" to "reverse_snow_angels",
        "home_back_3" to "doorframe_isometric_rows",
        // Home Shoulders
        "home_sh_1" to "pike_push_ups",
        "home_sh_2" to "dumbbell_lateral_raise",
        "home_sh_3" to "scapular_push_ups",
        // Home Biceps
        "home_bi_1" to "barbell_bicep_curl",
        "home_bi_2" to "incline_dumbbell_bicep_curl",
        "home_bi_3" to "hammer_curl",
        // Home Triceps
        "home_tri_1" to "bench_dips",
        "home_tri_2" to "close_grip_push_up",
        "home_tri_3" to "overhead_dumbbell_tricep_extension",
        // Home Legs
        "home_leg_1" to "air_squat",
        "home_leg_2" to "walking_lunges",
        "home_leg_3" to "single_leg_glute_bridge",
        // Home Abs
        "home_abs_1" to "plank",
        "home_abs_2" to "bicycle_crunch",
        "home_abs_3" to "mountain_climber",
        // Home Full Body
        "home_fb_1" to "burpees",
        "home_fb_2" to "jump_squat",
        "home_fb_3" to "high_knees",

        // Common Synonyms and Alternate Program References
        "overhead_press" to "barbell_overhead_press",
        "standing_overhead_press" to "barbell_overhead_press",
        "barbell_squat" to "squat",
        "barbell_back_squat" to "squat",
        "lat_pulldown" to "wide_grip_lat_pulldown",
        "barbell_bent_over_row" to "bent_over_barbell_row",
        "barbell_deadlift" to "deadlift",
        "lateral_raise" to "dumbbell_lateral_raise",
        "bicep_curl" to "barbell_bicep_curl",
        "incline_dumbbell_curl" to "incline_dumbbell_bicep_curl",
        "cable_chest_fly" to "cable_fly",
        "face_pull" to "face_pulls",
        "preacher_curl" to "ez_bar_preacher_curl",
        "tricep_rope_pushdown" to "rope_pushdown",
        "rope_triceps_pushdown" to "rope_pushdown",
        "triceps_pushdown" to "rope_pushdown",
        "ab_wheel_rollout" to "ab_rollout",
        "clean_and_press" to "hang_power_clean",
        "push_up" to "push_ups",
        "pushups" to "push_ups",
        "classic_push_ups" to "push_ups",
        "back_squat" to "squat",
        "barbell_row" to "bent_over_barbell_row",
        "dumbbell_row" to "one_arm_dumbbell_row",
        "machine_preacher_curl" to "ez_bar_preacher_curl",
        "barbell_curl" to "barbell_bicep_curl",
        "hammer_curl" to "incline_dumbbell_bicep_curl",
        "skull_crusher" to "skull_crushers",
        "ez_bar_skull_crusher" to "skull_crushers",
        "overhead_extension" to "overhead_dumbbell_tricep_extension",
        "overhead_tricep_extension" to "overhead_dumbbell_tricep_extension",
        "calf_raise" to "standing_calf_raise",
        "rear_delt_fly" to "face_pulls",
        "dumbbell_shrug" to "barbell_shrug",
        "assisted_pull_ups" to "assisted_pull_up",
        "assisted_dips" to "bench_dips",
        "dips" to "weighted_dips",
        "seated_row" to "seated_cable_row",
        "bodyweight_squats" to "air_squat",
        "bodyweight_squat" to "air_squat",
        "air_squats" to "air_squat",
        "superman" to "superman_hold",
        "superman_holds" to "superman_hold",
        "plank_shoulder_taps" to "plank",
        "chair_dips" to "bench_dips",
        "pushup" to "push_ups",
        "pushups" to "push_ups"
    )

    private val deletedExerciseIds = createThreadSafeMap<String, Boolean>()
    private val archivedExerciseIds = createThreadSafeMap<String, Boolean>()

    fun isDeleted(id: String): Boolean {
        val clean = id.trim().lowercase()
        if (clean.isBlank()) return false
        val canonical = legacyAliasMap[clean] ?: clean
        return deletedExerciseIds.containsKey(clean) || deletedExerciseIds.containsKey(canonical)
    }

    fun isArchived(id: String): Boolean {
        val clean = id.trim().lowercase()
        if (clean.isBlank()) return false
        val canonical = legacyAliasMap[clean] ?: clean
        return archivedExerciseIds.containsKey(clean) || archivedExerciseIds.containsKey(canonical)
    }

    fun deleteCanonicalExercise(exerciseId: String) {
        val raw = exerciseId.trim()
        if (raw.isBlank()) return
        val canonical = resolveCanonicalId(raw)
        val ids = setOf(raw, raw.lowercase(), canonical, canonical.lowercase())
        ids.forEach { id ->
            liveCanonicalOverrides.remove(id)
            liveCustomExercises.remove(id)
            deletedExerciseIds[id] = true
            archivedExerciseIds.remove(id)
        }
        canonicalRevision++
    }

    fun archiveCanonicalExercise(exerciseId: String) {
        val raw = exerciseId.trim()
        if (raw.isBlank()) return
        val canonical = resolveCanonicalId(raw)
        val ids = setOf(raw, raw.lowercase(), canonical, canonical.lowercase())
        ids.forEach { id ->
            archivedExerciseIds[id] = true
            deletedExerciseIds.remove(id)
        }
        val existing = getById(raw) ?: getById(canonical)
        if (existing != null) {
            val updated = existing.copy(isActive = false)
            ids.forEach { id ->
                liveCanonicalOverrides[id] = updated
                liveCustomExercises[id] = updated
            }
        }
        canonicalRevision++
    }

    fun restoreCanonicalExercise(exerciseId: String) {
        val raw = exerciseId.trim()
        if (raw.isBlank()) return
        val canonical = resolveCanonicalId(raw)
        val ids = setOf(raw, raw.lowercase(), canonical, canonical.lowercase())
        ids.forEach { id ->
            archivedExerciseIds.remove(id)
            deletedExerciseIds.remove(id)
        }
        val existing = getById(raw) ?: getById(canonical)
        if (existing != null) {
            val updated = existing.copy(isActive = true)
            ids.forEach { id ->
                liveCanonicalOverrides[id] = updated
                liveCustomExercises[id] = updated
            }
        }
        canonicalRevision++
    }

    private val liveCanonicalOverrides = createThreadSafeMap<String, Exercise>()

    fun getAll(): List<Exercise> {
        val merged = ALL_EXERCISES.associateBy { it.id }.toMutableMap()
        liveCanonicalOverrides.forEach { (id, ex) ->
            if (merged.containsKey(id)) {
                merged[id] = ex
            }
        }
        return merged.values
            .filter { !isDeleted(it.id) }
            .map { if (isArchived(it.id)) it.copy(isActive = false) else it }
            .toList()
    }

    fun getAllCanonical(): List<Exercise> {
        val merged = (baselineExercises.values + FullBodyExerciseLibrary.ALL_FULL_BODY_EXERCISES + ThirtyDayExerciseLibrary.ALL_EXERCISES)
            .associateBy { it.id }
            .toMutableMap()
        liveCanonicalOverrides.forEach { (id, ex) ->
            if (merged.containsKey(id)) {
                merged[id] = ex
            }
        }
        return merged.values
            .filter { !isDeleted(it.id) }
            .map { if (isArchived(it.id)) it.copy(isActive = false) else it }
            .toList()
    }

    private val canonicalIdCache = createThreadSafeMap<String, String>()

    private val staticExercisesByName: Map<String, Exercise> by lazy {
        val map = HashMap<String, Exercise>(1024)
        for (ex in baselineExercises.values) {
            map.putIfAbsent(ex.name.trim().lowercase(), ex)
        }
        for (ex in FullBodyExerciseLibrary.ALL_FULL_BODY_EXERCISES) {
            map.putIfAbsent(ex.name.trim().lowercase(), ex)
        }
        for (entry in ThirtyDayExerciseLibrary.ALL_ENTRIES) {
            map.putIfAbsent(entry.name.trim().lowercase(), ThirtyDayExerciseLibrary.toExercise(entry))
        }
        map
    }

    private val staticExercisesByNormalizedName: Map<String, Exercise> by lazy {
        val map = HashMap<String, Exercise>(1024)
        val regex = Regex("[^a-z0-9]")
        for (ex in baselineExercises.values) {
            val norm = ex.name.trim().lowercase().replace(regex, "")
            map.putIfAbsent(norm, ex)
        }
        for (ex in FullBodyExerciseLibrary.ALL_FULL_BODY_EXERCISES) {
            val norm = ex.name.trim().lowercase().replace(regex, "")
            map.putIfAbsent(norm, ex)
        }
        for (entry in ThirtyDayExerciseLibrary.ALL_ENTRIES) {
            val norm = entry.name.trim().lowercase().replace(regex, "")
            map.putIfAbsent(norm, ThirtyDayExerciseLibrary.toExercise(entry))
        }
        map
    }

    fun resolveCanonicalId(id: String): String {
        val clean = id.trim().lowercase()
        if (clean.isEmpty()) return ""
        canonicalIdCache[clean]?.let { return it }

        val resolved = computeCanonicalId(clean)
        canonicalIdCache[clean] = resolved
        return resolved
    }

    private fun computeCanonicalId(clean: String): String {
        // 1. Direct legacy alias map check
        legacyAliasMap[clean]?.let { return it }

        // 2. ThirtyDayExerciseLibrary lookup by ID or canonicalId
        ThirtyDayExerciseLibrary.findById(clean)?.canonicalId?.let { canon ->
            val cleanCanon = canon.trim().lowercase()
            return legacyAliasMap[cleanCanon] ?: cleanCanon
        }

        // 3. FullBodyExerciseLibrary lookup by ID
        FullBodyExerciseLibrary.findById(clean)?.let { fbEx ->
            val cleanFbName = fbEx.name.trim().lowercase()
            val norm = cleanFbName.replace(Regex("[^a-z0-9]"), "")
            val foundBaseline = staticExercisesByName[cleanFbName]
                ?: staticExercisesByNormalizedName[norm]
            if (foundBaseline != null) {
                return foundBaseline.id
            }
        }

        // 4. Strip common prefix patterns: e.g. "fb_adv_", "fb_beg_", "fb_noexp_", "fb_exp_",
        //    "chest_adv_", "back_beg_", "legs_exp_", "shoulders_noexp_", "gym_chest_", etc.
        val stripped = clean
            .replace(Regex("^(fb|chest|back|shoulders|sh|biceps|bi|triceps|tri|legs|leg|abs)_(noexp|beg|adv|exp)_"), "")
            .replace(Regex("^(home|gym)_(chest|back|sh|bi|tri|leg|abs|fb)_"), "")
        if (stripped != clean && stripped.isNotEmpty()) {
            legacyAliasMap[stripped]?.let { return it }
            if (baselineExercises.containsKey(stripped) || liveCanonicalOverrides.containsKey(stripped)) {
                return stripped
            }
        }

        return clean
    }

    fun getById(id: String): Exercise? {
        val cleanId = id.trim()
        if (cleanId.isEmpty() || isDeleted(cleanId)) return null
        val canonicalId = resolveCanonicalId(cleanId)
        if (isDeleted(canonicalId)) return null

        // 1. Direct or canonical override (e.g. uploaded video from Admin)
        val override = liveCanonicalOverrides[cleanId]
            ?: liveCanonicalOverrides[cleanId.lowercase()]
            ?: liveCanonicalOverrides[canonicalId]
            ?: liveCanonicalOverrides[canonicalId.lowercase()]

        // 2. Baseline or library exercise: check cleanId FIRST across all libraries
        val base = baselineExercises[cleanId]
            ?: baselineExercises[cleanId.lowercase()]
            ?: FullBodyExerciseLibrary.findById(cleanId)
            ?: ThirtyDayExerciseLibrary.findById(cleanId)?.let { ThirtyDayExerciseLibrary.toExercise(it) }
            // Only if cleanId was not found in any library, fall back to canonicalId
            ?: baselineExercises[canonicalId]
            ?: baselineExercises[canonicalId.lowercase()]
            ?: FullBodyExerciseLibrary.findById(canonicalId)
            ?: ThirtyDayExerciseLibrary.findById(canonicalId)?.let { ThirtyDayExerciseLibrary.toExercise(it) }

        val ex = when {
            override != null && base != null -> {
                val isDistinctLibraryExercise = ThirtyDayExerciseLibrary.findById(cleanId) != null ||
                    FullBodyExerciseLibrary.findById(cleanId) != null
                val isAliasOfOverride = cleanId.equals(override.id, ignoreCase = true) ||
                    (!isDistinctLibraryExercise && canonicalId.equals(override.id, ignoreCase = true))

                if (isAliasOfOverride) {
                    override
                } else {
                    // For library exercises (e.g. chest_adv_barbell_bench_press or fb_noexp_bodyweight_squat),
                    // retain library exercise identity (id, name, difficulty, target muscle, sets/reps)
                    // but inherit the uploaded media and form tips from the override
                    base.copy(
                        media = override.media,
                        description = if (override.description.isNotBlank()) override.description else base.description,
                        formTips = if (override.formTips.isNotEmpty()) override.formTips else base.formTips,
                        isActive = override.isActive && base.isActive
                    )
                }
            }
            override != null -> override
            base != null -> {
                val canonicalBase = if (canonicalId != cleanId) (baselineExercises[canonicalId] ?: baselineExercises[canonicalId.lowercase()]) else null
                if (canonicalBase != null && canonicalBase.media.videoUrl.isNotBlank() && base.media.videoUrl.isBlank()) {
                    base.copy(media = canonicalBase.media)
                } else {
                    base
                }
            }
            else -> null
        }

        return if (ex != null && (isArchived(cleanId) || isArchived(canonicalId))) {
            ex.copy(isActive = false)
        } else {
            ex
        }
    }

    fun findCanonical(id: String? = null, name: String? = null): Exercise? {
        val cleanId = id?.trim() ?: ""
        if (cleanId.isNotEmpty()) {
            val byId = getById(cleanId)
            if (byId != null) return byId
            val canonicalId = resolveCanonicalId(cleanId)
            if (canonicalId != cleanId) {
                val byCanon = getById(canonicalId)
                if (byCanon != null) return byCanon
            }
        }
        val cleanName = name?.trim() ?: ""
        if (cleanName.isNotEmpty()) {
            val foundByName = findByName(cleanName)
            if (foundByName != null) {
                val canonId = resolveCanonicalId(foundByName.id)
                val override = liveCanonicalOverrides[canonId] ?: liveCanonicalOverrides[foundByName.id]
                return if (override != null && override.media.videoUrl.isNotBlank()) override else (getById(canonId) ?: foundByName)
            }
        }
        return null
    }

    fun findByName(name: String): Exercise? {
        val cleanName = name.trim().lowercase()
        if (cleanName.isEmpty()) return null
        val normalized = cleanName.replace(Regex("[^a-z0-9]"), "")

        // 1. Fast check live overrides first (typically 0-5 items)
        val liveMatch = liveCanonicalOverrides.values.firstOrNull { liveEx ->
            !isDeleted(liveEx.id) && (
                liveEx.name.trim().lowercase() == cleanName ||
                liveEx.name.lowercase().replace(Regex("[^a-z0-9]"), "") == normalized
            )
        }

        // 2. Fast O(1) index lookup across all static libraries
        val found = liveMatch
            ?: staticExercisesByName[cleanName]?.takeIf { !isDeleted(it.id) }
            ?: staticExercisesByNormalizedName[normalized]?.takeIf { !isDeleted(it.id) }

        if (found == null) return null
        val canonId = resolveCanonicalId(found.id)
        val override = liveCanonicalOverrides[canonId] ?: liveCanonicalOverrides[found.id]
        val result = if (override != null && override.media.videoUrl.isNotBlank()) {
            override
        } else if (override != null) {
            found.copy(media = override.media)
        } else {
            found
        }
        return if (isArchived(result.id) || isArchived(canonId)) {
            result.copy(isActive = false)
        } else {
            result
        }
    }

    private val liveCustomExercises = createThreadSafeMap<String, Exercise>()

    fun getLiveOverrides(): List<Exercise> {
        return liveCanonicalOverrides.values
            .filter { !isDeleted(it.id) }
            .map { if (isArchived(it.id)) it.copy(isActive = false) else it }
            .distinctBy { it.id }
    }

    fun getLiveCustomExercises(): List<Exercise> {
        return liveCustomExercises.values
            .filter { !isDeleted(it.id) }
            .map { if (isArchived(it.id)) it.copy(isActive = false) else it }
            .distinctBy { it.id }
    }

    fun updateCanonicalExercise(exercise: Exercise) {
        val cleanId = exercise.id.trim()
        if (cleanId.isNotBlank()) {
            val canonicalId = resolveCanonicalId(cleanId)
            if (exercise.isActive) {
                archivedExerciseIds.remove(cleanId.lowercase())
                deletedExerciseIds.remove(cleanId.lowercase())
                archivedExerciseIds.remove(canonicalId.lowercase())
                deletedExerciseIds.remove(canonicalId.lowercase())
            } else {
                archivedExerciseIds[cleanId.lowercase()] = true
                archivedExerciseIds[canonicalId.lowercase()] = true
            }
            liveCustomExercises[cleanId] = exercise
            liveCanonicalOverrides[cleanId] = exercise
            if (canonicalId != cleanId && canonicalId.isNotBlank()) {
                liveCanonicalOverrides[canonicalId] = exercise
                liveCustomExercises[canonicalId] = exercise
            }
            canonicalRevision++
        }
    }

    fun registerCanonicalExercises(exercises: Collection<Exercise>) {
        if (exercises.isEmpty()) return
        var changed = false
        exercises.forEach { ex ->
            val cleanId = ex.id.trim()
            if (cleanId.isNotBlank()) {
                val canonicalId = resolveCanonicalId(cleanId)
                if (ex.isActive) {
                    archivedExerciseIds.remove(cleanId.lowercase())
                    deletedExerciseIds.remove(cleanId.lowercase())
                    archivedExerciseIds.remove(canonicalId.lowercase())
                    deletedExerciseIds.remove(canonicalId.lowercase())
                } else {
                    archivedExerciseIds[cleanId.lowercase()] = true
                    archivedExerciseIds[canonicalId.lowercase()] = true
                }
                val existing = liveCanonicalOverrides[cleanId]
                if (existing != ex) {
                    changed = true
                    liveCustomExercises[cleanId] = ex
                    liveCanonicalOverrides[cleanId] = ex
                    if (canonicalId != cleanId && canonicalId.isNotBlank()) {
                        liveCanonicalOverrides[canonicalId] = ex
                        liveCustomExercises[canonicalId] = ex
                    }
                }
            }
        }
        if (changed) {
            canonicalRevision++
        }
    }

    fun resetOverridesForTesting() {
        deletedExerciseIds.clear()
        archivedExerciseIds.clear()
        liveCanonicalOverrides.clear()
        liveCustomExercises.clear()
        canonicalIdCache.clear()
        canonicalRevision++
    }
}
