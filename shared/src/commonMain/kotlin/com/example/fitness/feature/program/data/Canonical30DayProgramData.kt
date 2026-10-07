package com.example.fitness.feature.program.data

import com.example.fitness.feature.exercise.model.ExerciseReference
import com.example.fitness.feature.program.model.Program
import com.example.fitness.feature.program.model.ProgramDay
import com.example.fitness.feature.program.model.ProgramType
import com.example.fitness.feature.program.model.ProgramWorkoutType
import com.example.fitness.feature.workout.model.ExperienceLevel

/**
 * Authoritative canonical definitions for the 30-Day Workout Plan across all 4 experience levels:
 * - NO_EXPERIENCE
 * - BEGINNER
 * - ADVANCED
 * - EXPERT
 *
 * Sourced directly from Athgym_30_Day_Workout_Developer_Spec.pdf and Athgym_Gym_Warmup_Cooldown_Exercise_Library.pdf.
 * Preserves exact day-by-day tables across all 30 days, Rotations A, B, C, D, and exact sets/reps.
 */
object Canonical30DayProgramData {

    // --- Authoritative Warm-Up Library Routines ---
    val WARMUP_CHEST_TRICEPS = listOf(
        "Treadmill walk — 3 min",
        "Arm circles — 10 each direction",
        "Band pull-aparts — 2×15",
        "Shoulder rotations — 10/side",
        "Scapular push-ups — 2×10",
        "Light push-ups — 1×10",
        "Bench press warm-up sets — 2–3 light sets"
    )

    val WARMUP_BACK_BICEPS = listOf(
        "Light rowing machine — 3 min",
        "Arm circles — 10 each",
        "Band pull-aparts — 2×15",
        "Scapular pull-ups — 2×8",
        "Cat-cow — 10",
        "Light lat pulldown — 1×12",
        "Light cable/biceps curls — 1×12"
    )

    val WARMUP_LEGS = listOf(
        "Bike — 4 min",
        "Leg swings — 10/leg",
        "Hip circles — 10/side",
        "Walking lunges — 10/leg",
        "Bodyweight squats — 2×10",
        "Glute bridges — 2×12",
        "Calf raises — 2×15",
        "Light squat warm-up sets — 2–3"
    )

    val WARMUP_SHOULDERS = listOf(
        "Bike/elliptical — 3 min",
        "Arm circles — 10 each",
        "Shoulder rolls — 10 each",
        "Band pull-aparts — 2×15",
        "Band external rotation — 2×10",
        "Scapular push-ups — 2×10",
        "Very light shoulder press — 1–2×12"
    )

    val WARMUP_ARMS = listOf(
        "Light cardio — 3 min",
        "Arm circles — 10 each",
        "Shoulder rolls — 10 each",
        "Band pull-aparts — 2×15",
        "Light biceps curls — 1×15",
        "Light triceps pushdowns — 1×15"
    )

    val WARMUP_CORE = listOf(
        "Light cardio — 3 min",
        "Torso rotations — 10/side",
        "Cat-cow — 10",
        "Bird dog — 2×8/side",
        "Dead bug — 2×8/side",
        "Glute bridge — 2×12",
        "Plank — 20–30 sec"
    )

    // --- Authoritative Cool-Down Library Routines ---
    val COOLDOWN_GENERAL = listOf(
        "Slow Walk — 2–3 min",
        "Deep Breathing — 1–2 min",
        "Standing Quad Stretch — 30 sec/leg",
        "Hamstring Stretch — 30 sec/leg",
        "Calf Stretch — 30 sec/leg",
        "Hip Flexor Stretch — 30 sec/leg"
    )

    val COOLDOWN_UPPER_BODY = listOf(
        "Cross-Body Shoulder Stretch — 30 sec/side",
        "Overhead Triceps Stretch — 30 sec/side",
        "Doorway Chest Stretch — 30 sec/side",
        "Lat Stretch — 30 sec/side",
        "Child's Pose — 30–45 sec",
        "Neck Side Stretch — 20–30 sec/side"
    )

    val COOLDOWN_LOWER_BODY = listOf(
        "Standing Quad Stretch — 30 sec/leg",
        "Seated Hamstring Stretch — 30 sec/leg",
        "Standing Calf Stretch — 30 sec/leg",
        "Hip Flexor Stretch — 30 sec/leg",
        "Figure-4 Glute Stretch — 30 sec/leg",
        "Adductor/Groin Stretch — 30 sec/side"
    )

    val COOLDOWN_CORE = listOf(
        "Child's Pose — 45 sec",
        "Cobra / Prone Press-Up — 20–30 sec",
        "Cat-Cow — 8–10 reps",
        "Kneeling Hip Flexor Stretch — 30 sec/side",
        "Supine Twist — 30 sec/side",
        "Deep Diaphragmatic Breathing — 1–2 min"
    )

    private fun buildProgramDays(level: ExperienceLevel): List<ProgramDay> {
        val days = mutableListOf<ProgramDay>()
        // --- Day 1 ---
        days.add(
            ProgramDay(
                dayNumber = 1,
                title = "Day 1 — Chest + Triceps (Rotation A)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Chest", "Triceps"),
                isRestDay = false,
                warmUp = WARMUP_CHEST_TRICEPS,
                coolDown = COOLDOWN_UPPER_BODY,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "machine_chest_press", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "incline_machine_press", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "pec_deck", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "rope_triceps_pushdown", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "overhead_cable_extension", sets = 2, reps = "12-15", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "dumbbell_bench_press", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "incline_dumbbell_press", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_fly", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "overhead_dumbbell_extension", sets = 3, reps = "10-12", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "barbell_bench_press", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "incline_dumbbell_press", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "chest_dips", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_fly", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "close_grip_bench_press", sets = 3, reps = "8-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "10-15", restSeconds = 90),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "barbell_bench_press", sets = 4, reps = "5-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "incline_barbell_press", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "decline_dumbbell_press", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "weighted_dips", sets = 3, reps = "6-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "skull_crushers", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "overhead_cable_extension", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "12-15", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 2 ---
        days.add(
            ProgramDay(
                dayNumber = 2,
                title = "Day 2 — Back + Biceps (Rotation A)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Back", "Biceps"),
                isRestDay = false,
                warmUp = WARMUP_BACK_BICEPS,
                coolDown = COOLDOWN_UPPER_BODY,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "wide_grip_lat_pulldown", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "seated_cable_row", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "chest_supported_machine_row", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "ez_bar_preacher_curl", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 2, reps = "12-15", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "wide_grip_lat_pulldown", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "seated_cable_row", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "single_arm_dumbbell_row", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "ez_bar_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "incline_dumbbell_bicep_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 2, reps = "12-15", restSeconds = 60),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "pull_ups", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "bent_over_barbell_row", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "t_bar_row", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "straight_arm_pulldown", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "ez_bar_curl", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "ez_bar_preacher_curl", sets = 3, reps = "10-12", restSeconds = 90),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "weighted_pull_ups", sets = 4, reps = "5-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "pendlay_row", sets = 4, reps = "6-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "t_bar_row", sets = 3, reps = "8-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "single_arm_lat_pulldown", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "incline_dumbbell_bicep_curl", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "ez_bar_preacher_curl", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "10-12", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 3 ---
        days.add(
            ProgramDay(
                dayNumber = 3,
                title = "Day 3 — Legs (Rotation A)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Quadriceps", "Hamstrings", "Calves"),
                isRestDay = false,
                warmUp = WARMUP_LEGS,
                coolDown = COOLDOWN_LOWER_BODY,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "leg_press", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "leg_extension", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "seated_leg_curl", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "glute_bridge", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "standing_calf_raise", sets = 2, reps = "15-20", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "goblet_squat", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "leg_press", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "romanian_deadlift", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "leg_extension", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "lying_leg_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "standing_calf_raise", sets = 3, reps = "15-20", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "squat", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "romanian_deadlift", sets = 4, reps = "8-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "bulgarian_split_squat", sets = 3, reps = "8-12 / leg", restSeconds = 90),
                        ExerciseReference(exerciseId = "leg_press", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "leg_curl", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "hip_thrust", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "standing_calf_raise", sets = 4, reps = "12-15", restSeconds = 120),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "squat", sets = 4, reps = "5-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "romanian_deadlift", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "hack_squat", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "bulgarian_split_squat", sets = 3, reps = "8-10 / leg", restSeconds = 90),
                        ExerciseReference(exerciseId = "leg_extension", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "lying_leg_curl", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "hip_thrust", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "seated_calf_raise", sets = 3, reps = "12-20", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 4 ---
        days.add(
            ProgramDay(
                dayNumber = 4,
                title = "Day 4 — Shoulders (Rotation A)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Shoulders"),
                isRestDay = false,
                warmUp = WARMUP_SHOULDERS,
                coolDown = COOLDOWN_UPPER_BODY,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "machine_shoulder_press", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "machine_lateral_raise", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "reverse_pec_deck", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "face_pulls", sets = 2, reps = "12-15", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "dumbbell_shoulder_press", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_lateral_raise", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "reverse_pec_deck", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "face_pulls", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_shrugs", sets = 3, reps = "12-15", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "barbell_overhead_press", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "arnold_press", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_lateral_raise", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "reverse_pec_deck", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "face_pulls", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "barbell_shrug", sets = 3, reps = "10-15", restSeconds = 90),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "standing_overhead_press", sets = 4, reps = "5-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "seated_dumbbell_press", sets = 3, reps = "8-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_lateral_raise", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "lean_away_lateral_raise", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "reverse_cable_fly", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "face_pulls", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "barbell_shrug", sets = 3, reps = "8-12", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 5 ---
        days.add(
            ProgramDay(
                dayNumber = 5,
                title = "Day 5 — Arms (Rotation A)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Biceps", "Triceps"),
                isRestDay = false,
                warmUp = WARMUP_ARMS,
                coolDown = COOLDOWN_UPPER_BODY,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "machine_biceps_curl", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "overhead_cable_extension", sets = 2, reps = "12-15", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "ez_bar_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "incline_dumbbell_bicep_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "skull_crushers", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "overhead_cable_extension", sets = 3, reps = "10-12", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "barbell_bicep_curl", sets = 4, reps = "8-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "ez_bar_preacher_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "close_grip_bench_press", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "skull_crushers", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "10-15", restSeconds = 90),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "ez_bar_curl", sets = 4, reps = "6-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "incline_dumbbell_bicep_curl", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "ez_bar_preacher_curl", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "bayesian_cable_curl", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "close_grip_bench_press", sets = 4, reps = "6-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "overhead_cable_extension", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "skull_crushers", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "12-15", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 6 ---
        days.add(
            ProgramDay(
                dayNumber = 6,
                title = "Day 6 — Core (Rotation A)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Abs", "Core"),
                isRestDay = false,
                warmUp = WARMUP_CORE,
                coolDown = COOLDOWN_CORE,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "dead_bug", sets = 2, reps = "10 / side", restSeconds = 60),
                        ExerciseReference(exerciseId = "bird_dog", sets = 2, reps = "10 / side", restSeconds = 60),
                        ExerciseReference(exerciseId = "glute_bridge", sets = 2, reps = "15", restSeconds = 60),
                        ExerciseReference(exerciseId = "knee_plank", sets = 2, reps = "20-30 sec", restSeconds = 60),
                        ExerciseReference(exerciseId = "heel_taps", sets = 2, reps = "12 / side", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "plank_hold", sets = 3, reps = "30-45 sec", restSeconds = 90),
                        ExerciseReference(exerciseId = "reverse_crunch", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "bicycle_crunch", sets = 3, reps = "10 / side", restSeconds = 90),
                        ExerciseReference(exerciseId = "dead_bug", sets = 3, reps = "10 / side", restSeconds = 90),
                        ExerciseReference(exerciseId = "side_plank", sets = 3, reps = "20-30 sec / side", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "hanging_leg_raise", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_crunch", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "ab_rollout", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_woodchop", sets = 3, reps = "10 / side", restSeconds = 90),
                        ExerciseReference(exerciseId = "side_plank", sets = 3, reps = "30-45 sec", restSeconds = 90),
                        ExerciseReference(exerciseId = "back_extension", sets = 3, reps = "12-15", restSeconds = 90),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "hanging_leg_raise", sets = 4, reps = "8-12", restSeconds = 120),
                        ExerciseReference(exerciseId = "weighted_cable_crunch", sets = 4, reps = "8-12", restSeconds = 120),
                        ExerciseReference(exerciseId = "ab_rollout", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_woodchop", sets = 3, reps = "10-15 / side", restSeconds = 90),
                        ExerciseReference(exerciseId = "pallof_press", sets = 3, reps = "10-15 / side", restSeconds = 90),
                        ExerciseReference(exerciseId = "weighted_side_bend", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "plank_hold", sets = 3, reps = "45-60 sec", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 7 ---
        days.add(
            ProgramDay(
                dayNumber = 7,
                title = "Day 7 — Rest / Recovery",
                workoutType = ProgramWorkoutType.REST.name,
                focus = listOf("Recovery"),
                isRestDay = true,
                warmUp = emptyList(),
                coolDown = COOLDOWN_GENERAL,
                exercises = emptyList(),
                notes = "No resistance-training workout. Focus on hydration, light walking, mobility, and recovery."
            )
        )

        // --- Day 8 ---
        days.add(
            ProgramDay(
                dayNumber = 8,
                title = "Day 8 — Chest + Triceps (Rotation A)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Chest", "Triceps"),
                isRestDay = false,
                warmUp = WARMUP_CHEST_TRICEPS,
                coolDown = COOLDOWN_UPPER_BODY,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "incline_machine_press", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "flat_machine_press", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "cable_fly", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "triceps_machine", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 2, reps = "12-15", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "barbell_bench_press", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "incline_machine_press", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "pec_deck", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "single_arm_pushdown", sets = 3, reps = "12-15", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "incline_barbell_press", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "dumbbell_bench_press", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "decline_machine_press", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "weighted_dips", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "skull_crushers", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "10-15", restSeconds = 90),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "incline_barbell_press", sets = 4, reps = "5-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "flat_dumbbell_press", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "decline_barbell_press", sets = 3, reps = "8-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "weighted_dips", sets = 3, reps = "6-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_crossover", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "overhead_cable_extension", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "reverse_grip_pushdown", sets = 3, reps = "12-15", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 9 ---
        days.add(
            ProgramDay(
                dayNumber = 9,
                title = "Day 9 — Back + Biceps (Rotation A)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Back", "Biceps"),
                isRestDay = false,
                warmUp = WARMUP_BACK_BICEPS,
                coolDown = COOLDOWN_UPPER_BODY,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "wide_grip_lat_pulldown", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "seated_cable_row", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "machine_pullover", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "machine_curl", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 2, reps = "12-15", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "wide_grip_lat_pulldown", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "chest_supported_row", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "one_arm_dumbbell_row", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "ez_bar_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "10-12", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "pull_ups", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "t_bar_row", sets = 4, reps = "8-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "seated_cable_row", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "straight_arm_pulldown", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "barbell_bicep_curl", sets = 3, reps = "8-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "incline_dumbbell_bicep_curl", sets = 3, reps = "10-12", restSeconds = 90),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "weighted_pull_ups", sets = 4, reps = "5-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "t_bar_row", sets = 4, reps = "6-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "chest_supported_row", sets = 3, reps = "8-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "straight_arm_pulldown", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "barbell_bicep_curl", sets = 3, reps = "6-8", restSeconds = 90),
                        ExerciseReference(exerciseId = "incline_dumbbell_bicep_curl", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "10-12", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 10 ---
        days.add(
            ProgramDay(
                dayNumber = 10,
                title = "Day 10 — Legs (Rotation A)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Quadriceps", "Hamstrings", "Calves"),
                isRestDay = false,
                warmUp = WARMUP_LEGS,
                coolDown = COOLDOWN_LOWER_BODY,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "leg_press", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "leg_extension", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "seated_leg_curl", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "glute_bridge", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "standing_calf_raise", sets = 2, reps = "15-20", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "goblet_squat", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "leg_press", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "romanian_deadlift", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "leg_extension", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "lying_leg_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "standing_calf_raise", sets = 3, reps = "15-20", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "squat", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "romanian_deadlift", sets = 4, reps = "8-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "bulgarian_split_squat", sets = 3, reps = "8-12 / leg", restSeconds = 90),
                        ExerciseReference(exerciseId = "leg_press", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "leg_curl", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "hip_thrust", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "standing_calf_raise", sets = 4, reps = "12-15", restSeconds = 120),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "squat", sets = 4, reps = "5-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "romanian_deadlift", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "hack_squat", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "bulgarian_split_squat", sets = 3, reps = "8-10 / leg", restSeconds = 90),
                        ExerciseReference(exerciseId = "leg_extension", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "lying_leg_curl", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "hip_thrust", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "seated_calf_raise", sets = 3, reps = "12-20", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 11 ---
        days.add(
            ProgramDay(
                dayNumber = 11,
                title = "Day 11 — Shoulders (Rotation A)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Shoulders"),
                isRestDay = false,
                warmUp = WARMUP_SHOULDERS,
                coolDown = COOLDOWN_UPPER_BODY,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "machine_shoulder_press", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "machine_lateral_raise", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "reverse_pec_deck", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "face_pulls", sets = 2, reps = "12-15", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "dumbbell_shoulder_press", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_lateral_raise", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "reverse_pec_deck", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "face_pulls", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_shrugs", sets = 3, reps = "12-15", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "barbell_overhead_press", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "arnold_press", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_lateral_raise", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "reverse_pec_deck", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "face_pulls", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "barbell_shrug", sets = 3, reps = "10-15", restSeconds = 90),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "standing_overhead_press", sets = 4, reps = "5-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "seated_dumbbell_press", sets = 3, reps = "8-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_lateral_raise", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "lean_away_lateral_raise", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "reverse_cable_fly", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "face_pulls", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "barbell_shrug", sets = 3, reps = "8-12", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 12 ---
        days.add(
            ProgramDay(
                dayNumber = 12,
                title = "Day 12 — Arms (Rotation A)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Biceps", "Triceps"),
                isRestDay = false,
                warmUp = WARMUP_ARMS,
                coolDown = COOLDOWN_UPPER_BODY,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "machine_biceps_curl", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "overhead_cable_extension", sets = 2, reps = "12-15", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "ez_bar_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "incline_dumbbell_bicep_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "skull_crushers", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "overhead_cable_extension", sets = 3, reps = "10-12", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "barbell_bicep_curl", sets = 4, reps = "8-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "ez_bar_preacher_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "close_grip_bench_press", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "skull_crushers", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "10-15", restSeconds = 90),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "ez_bar_curl", sets = 4, reps = "6-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "incline_dumbbell_bicep_curl", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "ez_bar_preacher_curl", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "bayesian_cable_curl", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "close_grip_bench_press", sets = 4, reps = "6-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "overhead_cable_extension", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "skull_crushers", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "12-15", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 13 ---
        days.add(
            ProgramDay(
                dayNumber = 13,
                title = "Day 13 — Core (Rotation A)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Abs", "Core"),
                isRestDay = false,
                warmUp = WARMUP_CORE,
                coolDown = COOLDOWN_CORE,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "dead_bug", sets = 2, reps = "10 / side", restSeconds = 60),
                        ExerciseReference(exerciseId = "bird_dog", sets = 2, reps = "10 / side", restSeconds = 60),
                        ExerciseReference(exerciseId = "glute_bridge", sets = 2, reps = "15", restSeconds = 60),
                        ExerciseReference(exerciseId = "knee_plank", sets = 2, reps = "20-30 sec", restSeconds = 60),
                        ExerciseReference(exerciseId = "heel_taps", sets = 2, reps = "12 / side", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "plank_hold", sets = 3, reps = "30-45 sec", restSeconds = 90),
                        ExerciseReference(exerciseId = "reverse_crunch", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "bicycle_crunch", sets = 3, reps = "10 / side", restSeconds = 90),
                        ExerciseReference(exerciseId = "dead_bug", sets = 3, reps = "10 / side", restSeconds = 90),
                        ExerciseReference(exerciseId = "side_plank", sets = 3, reps = "20-30 sec / side", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "hanging_leg_raise", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_crunch", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "ab_rollout", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_woodchop", sets = 3, reps = "10 / side", restSeconds = 90),
                        ExerciseReference(exerciseId = "side_plank", sets = 3, reps = "30-45 sec", restSeconds = 90),
                        ExerciseReference(exerciseId = "back_extension", sets = 3, reps = "12-15", restSeconds = 90),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "hanging_leg_raise", sets = 4, reps = "8-12", restSeconds = 120),
                        ExerciseReference(exerciseId = "weighted_cable_crunch", sets = 4, reps = "8-12", restSeconds = 120),
                        ExerciseReference(exerciseId = "ab_rollout", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_woodchop", sets = 3, reps = "10-15 / side", restSeconds = 90),
                        ExerciseReference(exerciseId = "pallof_press", sets = 3, reps = "10-15 / side", restSeconds = 90),
                        ExerciseReference(exerciseId = "weighted_side_bend", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "plank_hold", sets = 3, reps = "45-60 sec", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 14 ---
        days.add(
            ProgramDay(
                dayNumber = 14,
                title = "Day 14 — Rest / Recovery",
                workoutType = ProgramWorkoutType.REST.name,
                focus = listOf("Recovery"),
                isRestDay = true,
                warmUp = emptyList(),
                coolDown = COOLDOWN_GENERAL,
                exercises = emptyList(),
                notes = "No resistance-training workout. Focus on hydration, light walking, mobility, and recovery."
            )
        )

        // --- Day 15 ---
        days.add(
            ProgramDay(
                dayNumber = 15,
                title = "Day 15 — Chest + Triceps (Rotation B)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Chest", "Triceps"),
                isRestDay = false,
                warmUp = WARMUP_CHEST_TRICEPS,
                coolDown = COOLDOWN_UPPER_BODY,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "chest_press_machine", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "low_incline_machine_press", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "pec_deck", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "cable_triceps_extension", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 2, reps = "12-15", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "machine_chest_press", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "incline_dumbbell_press", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_crossover", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "overhead_rope_extension", sets = 3, reps = "10-12", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "flat_barbell_bench_press", sets = 4, reps = "6-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "incline_dumbbell_press", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "chest_dips", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "pec_deck", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "close_grip_bench_press", sets = 3, reps = "8-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "overhead_cable_extension", sets = 3, reps = "10-15", restSeconds = 90),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "paused_bench_press", sets = 4, reps = "4-6", restSeconds = 120),
                        ExerciseReference(exerciseId = "incline_dumbbell_press", sets = 4, reps = "8-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "decline_dumbbell_press", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "weighted_chest_dips", sets = 3, reps = "6-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "jm_press", sets = 3, reps = "8-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "overhead_cable_extension", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "12-15", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 16 ---
        days.add(
            ProgramDay(
                dayNumber = 16,
                title = "Day 16 — Back + Biceps (Rotation B)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Back", "Biceps"),
                isRestDay = false,
                warmUp = WARMUP_BACK_BICEPS,
                coolDown = COOLDOWN_UPPER_BODY,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "close_grip_lat_pulldown", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "machine_row", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "reverse_pec_deck", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "preacher_curl_machine", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 2, reps = "12-15", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "close_grip_pulldown", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "machine_row", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "reverse_pec_deck", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "ez_bar_preacher_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_curl", sets = 3, reps = "10-15", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "pull_ups", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "bent_over_barbell_row", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "one_arm_cable_row", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "reverse_pec_deck", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "ez_bar_preacher_curl", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "10-12", restSeconds = 90),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "neutral_grip_weighted_pull_up", sets = 4, reps = "5-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "bent_over_barbell_row", sets = 4, reps = "6-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "one_arm_cable_row", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "machine_pullover", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "ez_bar_preacher_curl", sets = 3, reps = "8-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "bayesian_curl", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "10-12", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 17 ---
        days.add(
            ProgramDay(
                dayNumber = 17,
                title = "Day 17 — Legs (Rotation B)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Quadriceps", "Hamstrings", "Calves"),
                isRestDay = false,
                warmUp = WARMUP_LEGS,
                coolDown = COOLDOWN_LOWER_BODY,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "hack_squat_machine", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "leg_extension", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "seated_leg_curl", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "hip_abduction", sets = 2, reps = "15-20", restSeconds = 60),
                        ExerciseReference(exerciseId = "seated_calf_raise", sets = 2, reps = "15-20", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "front_foot_elevated_split_squat", sets = 3, reps = "10 / leg", restSeconds = 90),
                        ExerciseReference(exerciseId = "hack_squat", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "hip_thrust", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "seated_leg_curl", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "leg_extension", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "seated_calf_raise", sets = 3, reps = "15-20", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "front_squat", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "walking_lunges", sets = 3, reps = "10 / leg", restSeconds = 90),
                        ExerciseReference(exerciseId = "hip_thrust", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "seated_leg_curl", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "leg_extension", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "hip_abduction", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "seated_calf_raise", sets = 4, reps = "12-20", restSeconds = 120),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "front_squat", sets = 4, reps = "5-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "walking_lunges", sets = 3, reps = "8-10 / leg", restSeconds = 90),
                        ExerciseReference(exerciseId = "barbell_hip_thrust", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "seated_leg_curl", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "leg_extension", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "hip_adduction", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "seated_calf_raise", sets = 4, reps = "12-20", restSeconds = 120),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 18 ---
        days.add(
            ProgramDay(
                dayNumber = 18,
                title = "Day 18 — Shoulders (Rotation B)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Shoulders"),
                isRestDay = false,
                warmUp = WARMUP_SHOULDERS,
                coolDown = COOLDOWN_UPPER_BODY,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "machine_shoulder_press", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "cable_lateral_raise", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "reverse_pec_deck", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "face_pulls", sets = 2, reps = "12-15", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "seated_machine_press", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_lateral_raise", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "reverse_pec_deck", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "face_pulls", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_shrugs", sets = 3, reps = "12-15", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "seated_dumbbell_press", sets = 4, reps = "8-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "landmine_press", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_lateral_raise", sets = 4, reps = "12-15", restSeconds = 120),
                        ExerciseReference(exerciseId = "rear_delt_cable_fly", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_shrugs", sets = 3, reps = "10-15", restSeconds = 90),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "seated_dumbbell_press", sets = 4, reps = "6-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "arnold_press", sets = 3, reps = "8-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_lateral_raise", sets = 4, reps = "10-15", restSeconds = 120),
                        ExerciseReference(exerciseId = "rear_delt_cable_fly", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "face_pulls", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "barbell_shrug", sets = 3, reps = "8-12", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 19 ---
        days.add(
            ProgramDay(
                dayNumber = 19,
                title = "Day 19 — Arms (Rotation B)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Biceps", "Triceps"),
                isRestDay = false,
                warmUp = WARMUP_ARMS,
                coolDown = COOLDOWN_UPPER_BODY,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "ez_bar_preacher_curl", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "cable_hammer_curl", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "cable_overhead_extension", sets = 2, reps = "12-15", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "ez_bar_preacher_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "reverse_curl", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "single_arm_pushdown", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "overhead_rope_extension", sets = 3, reps = "10-12", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "ez_bar_preacher_curl", sets = 4, reps = "8-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "cable_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "reverse_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "chest_dips", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "single_arm_pushdown", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "overhead_cable_extension", sets = 3, reps = "10-12", restSeconds = 90),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "ez_bar_preacher_curl", sets = 4, reps = "6-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "bayesian_curl", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "reverse_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "weighted_dips", sets = 4, reps = "6-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "single_arm_cable_extension", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "overhead_rope_extension", sets = 3, reps = "10-15", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 20 ---
        days.add(
            ProgramDay(
                dayNumber = 20,
                title = "Day 20 — Core (Rotation B)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Abs", "Core"),
                isRestDay = false,
                warmUp = WARMUP_CORE,
                coolDown = COOLDOWN_CORE,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "heel_taps", sets = 2, reps = "12 / side", restSeconds = 60),
                        ExerciseReference(exerciseId = "dead_bug", sets = 2, reps = "10 / side", restSeconds = 60),
                        ExerciseReference(exerciseId = "bird_dog", sets = 2, reps = "10 / side", restSeconds = 60),
                        ExerciseReference(exerciseId = "knee_plank", sets = 2, reps = "20-30 sec", restSeconds = 60),
                        ExerciseReference(exerciseId = "glute_bridge", sets = 2, reps = "15", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "reverse_crunch", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "plank_hold", sets = 3, reps = "30-45 sec", restSeconds = 90),
                        ExerciseReference(exerciseId = "mountain_climber", sets = 3, reps = "30 sec", restSeconds = 90),
                        ExerciseReference(exerciseId = "dead_bug", sets = 3, reps = "10 / side", restSeconds = 90),
                        ExerciseReference(exerciseId = "side_plank", sets = 3, reps = "20-30 sec / side", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "hanging_leg_raise", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "ab_rollout", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "russian_twist", sets = 3, reps = "12-15 / side", restSeconds = 90),
                        ExerciseReference(exerciseId = "pallof_press", sets = 3, reps = "10-15 / side", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_crunch", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "back_extension", sets = 3, reps = "12-15", restSeconds = 90),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "hanging_leg_raise", sets = 4, reps = "8-12", restSeconds = 120),
                        ExerciseReference(exerciseId = "ab_rollout", sets = 4, reps = "8-12", restSeconds = 120),
                        ExerciseReference(exerciseId = "weighted_russian_twist", sets = 3, reps = "10-15 / side", restSeconds = 90),
                        ExerciseReference(exerciseId = "pallof_press", sets = 3, reps = "10-15 / side", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_crunch", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "side_plank", sets = 3, reps = "45-60 sec / side", restSeconds = 90),
                        ExerciseReference(exerciseId = "back_extension", sets = 3, reps = "10-15", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 21 ---
        days.add(
            ProgramDay(
                dayNumber = 21,
                title = "Day 21 — Rest / Recovery",
                workoutType = ProgramWorkoutType.REST.name,
                focus = listOf("Recovery"),
                isRestDay = true,
                warmUp = emptyList(),
                coolDown = COOLDOWN_GENERAL,
                exercises = emptyList(),
                notes = "No resistance-training workout. Focus on hydration, light walking, mobility, and recovery."
            )
        )

        // --- Day 22 ---
        days.add(
            ProgramDay(
                dayNumber = 22,
                title = "Day 22 — Chest + Triceps (Rotation C)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Chest", "Triceps"),
                isRestDay = false,
                warmUp = WARMUP_CHEST_TRICEPS,
                coolDown = COOLDOWN_UPPER_BODY,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "machine_chest_press", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "incline_chest_press", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "cable_fly", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "single_arm_cable_extension", sets = 2, reps = "12-15", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "dumbbell_bench_press", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "incline_machine_press", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "pec_deck", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "overhead_dumbbell_extension", sets = 3, reps = "10-12", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "decline_barbell_press", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "incline_machine_press", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_bench_press", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "low_to_high_cable_fly", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "skull_crushers", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "12-15", restSeconds = 90),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "decline_barbell_press", sets = 4, reps = "5-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "incline_machine_press", sets = 3, reps = "8-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_bench_press", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "low_to_high_cable_fly", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "skull_crushers", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "single_arm_overhead_cable_extension", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "12-15", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 23 ---
        days.add(
            ProgramDay(
                dayNumber = 23,
                title = "Day 23 — Back + Biceps (Rotation C)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Back", "Biceps"),
                isRestDay = false,
                warmUp = WARMUP_BACK_BICEPS,
                coolDown = COOLDOWN_UPPER_BODY,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "wide_grip_lat_pulldown", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "chest_supported_row", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "seated_cable_row", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "ez_bar_preacher_curl", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "cable_curl", sets = 2, reps = "12-15", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "wide_grip_lat_pulldown", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_row", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "seated_cable_row", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "ez_bar_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "ez_bar_preacher_curl", sets = 3, reps = "10-12", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "close_grip_lat_pulldown", sets = 4, reps = "8-12", restSeconds = 120),
                        ExerciseReference(exerciseId = "pendlay_row", sets = 4, reps = "6-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "machine_row", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "face_pulls", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "ez_bar_curl", sets = 3, reps = "8-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_curl", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 2, reps = "12-15", restSeconds = 60),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "close_grip_weighted_pull_up", sets = 4, reps = "5-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "pendlay_row", sets = 4, reps = "5-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "machine_row", sets = 3, reps = "8-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "face_pulls", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "ez_bar_curl", sets = 3, reps = "6-8", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "10-12", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 24 ---
        days.add(
            ProgramDay(
                dayNumber = 24,
                title = "Day 24 — Legs (Rotation C)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Quadriceps", "Hamstrings", "Calves"),
                isRestDay = false,
                warmUp = WARMUP_LEGS,
                coolDown = COOLDOWN_LOWER_BODY,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "leg_press", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "supported_split_squat", sets = 2, reps = "10 / leg", restSeconds = 60),
                        ExerciseReference(exerciseId = "leg_curl", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "glute_bridge", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "standing_calf_raise", sets = 2, reps = "15-20", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "goblet_squat", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "reverse_lunge", sets = 3, reps = "10 / leg", restSeconds = 90),
                        ExerciseReference(exerciseId = "romanian_deadlift", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "leg_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "leg_extension", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "standing_calf_raise", sets = 3, reps = "15-20", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "hack_squat", sets = 4, reps = "8-12", restSeconds = 120),
                        ExerciseReference(exerciseId = "romanian_deadlift", sets = 4, reps = "8-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "walking_lunges", sets = 3, reps = "10 / leg", restSeconds = 90),
                        ExerciseReference(exerciseId = "leg_press", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "lying_leg_curl", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "standing_calf_raise", sets = 4, reps = "12-20", restSeconds = 120),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "hack_squat", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "romanian_deadlift", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "walking_lunges", sets = 3, reps = "8-10 / leg", restSeconds = 90),
                        ExerciseReference(exerciseId = "leg_press", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "lying_leg_curl", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "hip_abduction", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "standing_calf_raise", sets = 4, reps = "10-15", restSeconds = 120),
                        ExerciseReference(exerciseId = "seated_calf_raise", sets = 3, reps = "15-20", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 25 ---
        days.add(
            ProgramDay(
                dayNumber = 25,
                title = "Day 25 — Shoulders (Rotation C)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Shoulders"),
                isRestDay = false,
                warmUp = WARMUP_SHOULDERS,
                coolDown = COOLDOWN_UPPER_BODY,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "machine_press", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "lateral_raise_machine", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "reverse_pec_deck", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "face_pulls", sets = 2, reps = "12-15", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "dumbbell_press", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "arnold_press", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_lateral_raise", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "reverse_pec_deck", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "face_pulls", sets = 3, reps = "12-15", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "machine_shoulder_press", sets = 4, reps = "8-12", restSeconds = 120),
                        ExerciseReference(exerciseId = "landmine_press", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_lateral_raise", sets = 4, reps = "12-15", restSeconds = 120),
                        ExerciseReference(exerciseId = "reverse_pec_deck", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "face_pulls", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "shrug", sets = 3, reps = "10-15", restSeconds = 90),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "machine_shoulder_press", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "landmine_press", sets = 3, reps = "8-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_lateral_raise", sets = 4, reps = "10-15", restSeconds = 120),
                        ExerciseReference(exerciseId = "reverse_pec_deck", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "face_pulls", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_shrugs", sets = 3, reps = "8-12", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 26 ---
        days.add(
            ProgramDay(
                dayNumber = 26,
                title = "Day 26 — Arms (Rotation C)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Biceps", "Triceps"),
                isRestDay = false,
                warmUp = WARMUP_ARMS,
                coolDown = COOLDOWN_UPPER_BODY,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "machine_curl", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "triceps_machine", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 2, reps = "12-15", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "barbell_bicep_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "ez_bar_preacher_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "skull_crushers", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "overhead_cable_extension", sets = 3, reps = "10-12", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "ez_bar_curl", sets = 4, reps = "8-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "incline_dumbbell_bicep_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "close_grip_bench_press", sets = 4, reps = "6-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "skull_crushers", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "10-15", restSeconds = 90),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "barbell_bicep_curl", sets = 4, reps = "5-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "incline_dumbbell_bicep_curl", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "ez_bar_preacher_curl", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "close_grip_bench_press", sets = 4, reps = "5-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "skull_crushers", sets = 3, reps = "8-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "12-15", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 27 ---
        days.add(
            ProgramDay(
                dayNumber = 27,
                title = "Day 27 — Core (Rotation C)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Abs", "Core"),
                isRestDay = false,
                warmUp = WARMUP_CORE,
                coolDown = COOLDOWN_CORE,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "dead_bug", sets = 2, reps = "10 / side", restSeconds = 60),
                        ExerciseReference(exerciseId = "bird_dog", sets = 2, reps = "10 / side", restSeconds = 60),
                        ExerciseReference(exerciseId = "heel_taps", sets = 2, reps = "12 / side", restSeconds = 60),
                        ExerciseReference(exerciseId = "knee_plank", sets = 2, reps = "20-30 sec", restSeconds = 60),
                        ExerciseReference(exerciseId = "glute_bridge", sets = 2, reps = "15", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "bicycle_crunch", sets = 3, reps = "10 / side", restSeconds = 90),
                        ExerciseReference(exerciseId = "reverse_crunch", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "plank_hold", sets = 3, reps = "30-45 sec", restSeconds = 90),
                        ExerciseReference(exerciseId = "bird_dog", sets = 3, reps = "10 / side", restSeconds = 90),
                        ExerciseReference(exerciseId = "side_plank", sets = 3, reps = "20-30 sec", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "reverse_crunch", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_crunch", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "mountain_climber", sets = 3, reps = "30-45 sec", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_woodchop", sets = 3, reps = "10-12 / side", restSeconds = 90),
                        ExerciseReference(exerciseId = "side_plank", sets = 3, reps = "30-45 sec", restSeconds = 90),
                        ExerciseReference(exerciseId = "back_extension", sets = 3, reps = "12-15", restSeconds = 90),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "hanging_knee_to_elbow_raise", sets = 4, reps = "8-12", restSeconds = 120),
                        ExerciseReference(exerciseId = "weighted_cable_crunch", sets = 4, reps = "8-12", restSeconds = 120),
                        ExerciseReference(exerciseId = "cable_woodchop", sets = 3, reps = "10-15 / side", restSeconds = 90),
                        ExerciseReference(exerciseId = "pallof_press", sets = 3, reps = "10-15 / side", restSeconds = 90),
                        ExerciseReference(exerciseId = "weighted_side_bend", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "ab_rollout", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "plank_hold", sets = 3, reps = "45-60 sec", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 28 ---
        days.add(
            ProgramDay(
                dayNumber = 28,
                title = "Day 28 — Rest / Recovery",
                workoutType = ProgramWorkoutType.REST.name,
                focus = listOf("Recovery"),
                isRestDay = true,
                warmUp = emptyList(),
                coolDown = COOLDOWN_GENERAL,
                exercises = emptyList(),
                notes = "No resistance-training workout. Focus on hydration, light walking, mobility, and recovery."
            )
        )

        // --- Day 29 ---
        days.add(
            ProgramDay(
                dayNumber = 29,
                title = "Day 29 — Chest + Triceps (Rotation D)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Chest", "Triceps"),
                isRestDay = false,
                warmUp = WARMUP_CHEST_TRICEPS,
                coolDown = COOLDOWN_UPPER_BODY,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "machine_chest_press", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "incline_machine_press", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "pec_deck", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "overhead_cable_extension", sets = 2, reps = "12-15", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "dumbbell_bench_press", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "incline_dumbbell_press", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "chest_supported_machine_press", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "reverse_grip_pushdown", sets = 3, reps = "12-15", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "dumbbell_bench_press", sets = 4, reps = "8-10", restSeconds = 120),
                        ExerciseReference(exerciseId = "incline_dumbbell_press", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "chest_dips", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_crossover", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "close_grip_push_up", sets = 3, reps = "AMRAP with form", restSeconds = 90),
                        ExerciseReference(exerciseId = "overhead_rope_extension", sets = 3, reps = "10-15", restSeconds = 90),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "dumbbell_bench_press", sets = 4, reps = "6-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "incline_dumbbell_press", sets = 3, reps = "8-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "weighted_dips", sets = 3, reps = "6-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "decline_press", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "cable_fly", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "skull_crushers", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "12-15", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        // --- Day 30 ---
        days.add(
            ProgramDay(
                dayNumber = 30,
                title = "Day 30 — Back + Biceps (Rotation D)",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Back", "Biceps"),
                isRestDay = false,
                warmUp = WARMUP_BACK_BICEPS,
                coolDown = COOLDOWN_UPPER_BODY,
                exercises = when (level) {
                    ExperienceLevel.NO_EXPERIENCE -> listOf(
                        ExerciseReference(exerciseId = "assisted_pulldown", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "supported_row_machine", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "straight_arm_pulldown", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "cable_curl", sets = 2, reps = "12-15", restSeconds = 60),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 2, reps = "12-15", restSeconds = 60),
                    )
                    ExperienceLevel.BEGINNER -> listOf(
                        ExerciseReference(exerciseId = "pull_ups", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "chest_supported_row", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "straight_arm_pulldown", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "incline_dumbbell_bicep_curl", sets = 3, reps = "10-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "10-12", restSeconds = 90),
                    )
                    ExperienceLevel.ADVANCED -> listOf(
                        ExerciseReference(exerciseId = "weighted_pull_ups", sets = 4, reps = "6-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "pendlay_row", sets = 3, reps = "6-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "single_arm_dumbbell_row", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "straight_arm_pulldown", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "bayesian_cable_curl", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "10-12", restSeconds = 90),
                    )
                    ExperienceLevel.EXPERT -> listOf(
                        ExerciseReference(exerciseId = "weighted_pull_ups", sets = 4, reps = "5-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "pendlay_row", sets = 4, reps = "5-8", restSeconds = 120),
                        ExerciseReference(exerciseId = "single_arm_dumbbell_row", sets = 3, reps = "8-10", restSeconds = 90),
                        ExerciseReference(exerciseId = "straight_arm_pulldown", sets = 3, reps = "12-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "bayesian_cable_curl", sets = 3, reps = "10-15", restSeconds = 90),
                        ExerciseReference(exerciseId = "ez_bar_preacher_curl", sets = 3, reps = "8-12", restSeconds = 90),
                        ExerciseReference(exerciseId = "reverse_curl", sets = 3, reps = "10-12", restSeconds = 90),
                    )
                },
                notes = "Follow prescribed sets and rep ranges. Focus on controlled movement and proper form."
            )
        )

        return days
    }

    val PROGRAM_NO_EXPERIENCE = Program(
        id = "thirty_day_no_experience",
        name = "30-Day Workout Plan — No Experience",
        slug = "thirty_day_no_experience",
        description = "Authoritative 30-Day Workout Plan calibrated for No Experience level.",
        type = ProgramType.THIRTY_DAY.name,
        experienceLevel = ExperienceLevel.NO_EXPERIENCE.name,
        durationDays = 30,
        trainingDaysPerWeek = 6,
        days = buildProgramDays(ExperienceLevel.NO_EXPERIENCE),
        isActive = true,
        version = 1
    )

    val PROGRAM_BEGINNER = Program(
        id = "thirty_day_beginner",
        name = "30-Day Workout Plan — Beginner",
        slug = "thirty_day_beginner",
        description = "Authoritative 30-Day Workout Plan calibrated for Beginner level.",
        type = ProgramType.THIRTY_DAY.name,
        experienceLevel = ExperienceLevel.BEGINNER.name,
        durationDays = 30,
        trainingDaysPerWeek = 6,
        days = buildProgramDays(ExperienceLevel.BEGINNER),
        isActive = true,
        version = 1
    )

    val PROGRAM_ADVANCED = Program(
        id = "thirty_day_advanced",
        name = "30-Day Workout Plan — Advanced",
        slug = "thirty_day_advanced",
        description = "Authoritative 30-Day Workout Plan calibrated for Advanced level.",
        type = ProgramType.THIRTY_DAY.name,
        experienceLevel = ExperienceLevel.ADVANCED.name,
        durationDays = 30,
        trainingDaysPerWeek = 6,
        days = buildProgramDays(ExperienceLevel.ADVANCED),
        isActive = true,
        version = 1
    )

    val PROGRAM_EXPERT = Program(
        id = "thirty_day_expert",
        name = "30-Day Workout Plan — Expert",
        slug = "thirty_day_expert",
        description = "Authoritative 30-Day Workout Plan calibrated for Expert level.",
        type = ProgramType.THIRTY_DAY.name,
        experienceLevel = ExperienceLevel.EXPERT.name,
        durationDays = 30,
        trainingDaysPerWeek = 6,
        days = buildProgramDays(ExperienceLevel.EXPERT),
        isActive = true,
        version = 1
    )

    val ALL_CANONICAL_PROGRAMS = listOf(
        PROGRAM_NO_EXPERIENCE,
        PROGRAM_BEGINNER,
        PROGRAM_ADVANCED,
        PROGRAM_EXPERT
    )

    fun getProgramForLevel(level: ExperienceLevel): Program {
        return when (level) {
            ExperienceLevel.NO_EXPERIENCE -> PROGRAM_NO_EXPERIENCE
            ExperienceLevel.BEGINNER -> PROGRAM_BEGINNER
            ExperienceLevel.ADVANCED -> PROGRAM_ADVANCED
            ExperienceLevel.EXPERT -> PROGRAM_EXPERT
        }
    }
}
