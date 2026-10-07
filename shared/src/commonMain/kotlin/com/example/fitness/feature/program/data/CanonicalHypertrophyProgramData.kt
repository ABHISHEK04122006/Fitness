package com.example.fitness.feature.program.data

import com.example.fitness.feature.exercise.model.ExerciseReference
import com.example.fitness.feature.program.model.Program
import com.example.fitness.feature.program.model.ProgramDay
import com.example.fitness.feature.program.model.ProgramType
import com.example.fitness.feature.program.model.ProgramWorkoutType
import com.example.fitness.feature.workout.model.ExperienceLevel

/**
 * Authoritative 30-Day Hypertrophy Program.
 * Sourced directly from Athgym_30_Day_Hypertrophy_Strength_Plans.pdf (Part 1).
 * Preserves exact day structure, workout focus, exercise selection, sets, reps, and Day 30 performance check.
 */
object CanonicalHypertrophyProgramData {

    val PROGRAM_HYPERTROPHY = Program(
        id = "hypertrophy_30_day",
        name = "30-Day Hypertrophy Program",
        slug = "hypertrophy-30-day",
        description = "Authoritative 30-day muscle hypertrophy training program designed for maximum muscle growth.",
        type = ProgramType.HYPERTROPHY.name,
        experienceLevel = ExperienceLevel.ADVANCED.name,
        durationDays = 30,
        trainingDaysPerWeek = 6,
        days = listOf(
            ProgramDay(
                dayNumber = 1,
                title = "Day 1 — Chest + Triceps",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Chest", "Triceps"),
                isRestDay = false,
                warmUp = listOf("Treadmill 3 min", "arm circles", "band pull-aparts 2×15", "scapular push-ups 2×10", "2 light bench sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "barbell_bench_press", sets = 4, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "incline_dumbbell_press", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "machine_chest_press", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "cable_fly", sets = 3, reps = "12-15", restSeconds = 90),
                    ExerciseReference(exerciseId = "rope_triceps_pushdown", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "overhead_cable_extension", sets = 3, reps = "12", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 2,
                title = "Day 2 — Back + Biceps",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Back", "Biceps"),
                isRestDay = false,
                warmUp = listOf("Rowing 4 min", "band activation", "light pulldowns"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "wide_grip_lat_pulldown", sets = 4, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "bent_over_barbell_row", sets = 3, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "seated_cable_row", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "single_arm_dumbbell_row", sets = 3, reps = "10/side", restSeconds = 90),
                    ExerciseReference(exerciseId = "barbell_bicep_curl", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "12", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 3,
                title = "Day 3 — Legs",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Quadriceps", "Hamstrings", "Calves"),
                isRestDay = false,
                warmUp = listOf("Bike 4 min", "leg swings", "hip mobility", "2 squat warm-up sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "squat", sets = 4, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "leg_press", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "romanian_deadlift", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "leg_extension", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "leg_curl", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "standing_calf_raise", sets = 4, reps = "15", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 4,
                title = "Day 4 — Shoulders",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Shoulders"),
                isRestDay = false,
                warmUp = listOf("5 min cardio", "shoulder mobility", "band activation"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "dumbbell_shoulder_press", sets = 4, reps = "8-10", restSeconds = 90),
                    ExerciseReference(exerciseId = "dumbbell_lateral_raise", sets = 4, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "machine_shoulder_press", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "reverse_pec_deck", sets = 3, reps = "15", restSeconds = 90),
                    ExerciseReference(exerciseId = "face_pulls", sets = 3, reps = "15", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 5,
                title = "Day 5 — Arms",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Arms", "Biceps", "Triceps"),
                isRestDay = false,
                warmUp = listOf("5 min cardio", "arm circles", "light curls/pushdowns"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "ez_bar_curl", sets = 4, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "incline_dumbbell_bicep_curl", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "close_grip_bench_press", sets = 3, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "overhead_cable_extension", sets = 3, reps = "12", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 6,
                title = "Day 6 — Core + Full Body",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Core", "Full Body"),
                isRestDay = false,
                warmUp = listOf("5–8 min cardio + dynamic mobility"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "goblet_squat", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "dumbbell_bench_press", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "wide_grip_lat_pulldown", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "seated_cable_row", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "hanging_leg_raise", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "cable_crunch", sets = 3, reps = "15", restSeconds = 90),
                    ExerciseReference(exerciseId = "plank_hold", sets = 3, reps = "40 sec", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 7,
                title = "Day 7 — Rest",
                workoutType = ProgramWorkoutType.REST.name,
                focus = listOf("Recovery"),
                isRestDay = true,
                warmUp = listOf("Easy walking + light mobility"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = emptyList(),
                notes = "Recovery day. Light walking and mobility."
            ),
            ProgramDay(
                dayNumber = 8,
                title = "Day 8 — Chest + Triceps",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Chest", "Triceps"),
                isRestDay = false,
                warmUp = listOf("General cardio + chest/shoulder mobility + light pressing"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "incline_barbell_press", sets = 4, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "dumbbell_bench_press", sets = 4, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "decline_machine_press", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "pec_deck", sets = 3, reps = "15", restSeconds = 90),
                    ExerciseReference(exerciseId = "skull_crushers", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "12-15", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 9,
                title = "Day 9 — Back + Biceps",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Back", "Biceps"),
                isRestDay = false,
                warmUp = listOf("Rowing + scapular activation + light pulldown"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "pull_ups", sets = 4, reps = "6-10", restSeconds = 90),
                    ExerciseReference(exerciseId = "chest_supported_row", sets = 4, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "close_grip_pulldown", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "straight_arm_pulldown", sets = 3, reps = "15", restSeconds = 90),
                    ExerciseReference(exerciseId = "ez_bar_preacher_curl", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "cable_curl", sets = 3, reps = "12", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 10,
                title = "Day 10 — Legs",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Quadriceps", "Hamstrings", "Calves"),
                isRestDay = false,
                warmUp = listOf("Bike + hip/ankle mobility + squat preparation"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "front_squat", sets = 4, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "hack_squat", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "dumbbell_romanian_deadlift", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "walking_lunges", sets = 3, reps = "10/leg", restSeconds = 90),
                    ExerciseReference(exerciseId = "seated_leg_curl", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "seated_calf_raise", sets = 4, reps = "15", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 11,
                title = "Day 11 — Shoulders",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Shoulders"),
                isRestDay = false,
                warmUp = listOf("5 min cardio + shoulder mobility + band work"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "barbell_overhead_press", sets = 4, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "arnold_press", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "cable_lateral_raise", sets = 4, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "reverse_pec_deck", sets = 3, reps = "15", restSeconds = 90),
                    ExerciseReference(exerciseId = "face_pulls", sets = 3, reps = "15", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 12,
                title = "Day 12 — Arms",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Arms", "Biceps", "Triceps"),
                isRestDay = false,
                warmUp = listOf("Light cardio + elbow/shoulder mobility + light curls/pushdowns"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "barbell_bicep_curl", sets = 4, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "spider_curl", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "chest_dips", sets = 3, reps = "8-10", restSeconds = 90),
                    ExerciseReference(exerciseId = "skull_crushers", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "15", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 13,
                title = "Day 13 — Core + Full Body",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Core", "Full Body"),
                isRestDay = false,
                warmUp = listOf("5–8 min cardio + dynamic mobility"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "dumbbell_goblet_squat", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "incline_dumbbell_press", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "seated_cable_row", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "dumbbell_romanian_deadlift", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "ab_rollout", sets = 3, reps = "8-12", restSeconds = 90),
                    ExerciseReference(exerciseId = "cable_crunch", sets = 3, reps = "15", restSeconds = 90),
                    ExerciseReference(exerciseId = "side_plank_3_30_sec_side", sets = 3, reps = "10-12", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 14,
                title = "Day 14 — Rest",
                workoutType = ProgramWorkoutType.REST.name,
                focus = listOf("Recovery"),
                isRestDay = true,
                warmUp = listOf("Easy walking + mobility"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = emptyList(),
                notes = "Recovery day. Light walking and mobility."
            ),
            ProgramDay(
                dayNumber = 15,
                title = "Day 15 — Chest + Triceps",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Chest", "Triceps"),
                isRestDay = false,
                warmUp = listOf("Chest/shoulder dynamic mobility + progressive pressing sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "barbell_bench_press", sets = 4, reps = "6-8", restSeconds = 90),
                    ExerciseReference(exerciseId = "incline_dumbbell_press", sets = 4, reps = "8-10", restSeconds = 90),
                    ExerciseReference(exerciseId = "cable_crossover", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "pec_deck", sets = 3, reps = "15", restSeconds = 90),
                    ExerciseReference(exerciseId = "weighted_dips", sets = 3, reps = "8-10", restSeconds = 90),
                    ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "12", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 16,
                title = "Day 16 — Back + Biceps",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Back", "Biceps"),
                isRestDay = false,
                warmUp = listOf("Rowing + scapular activation + light pulling sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "bent_over_barbell_row", sets = 4, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "wide_grip_lat_pulldown", sets = 4, reps = "8-10", restSeconds = 90),
                    ExerciseReference(exerciseId = "t_bar_row", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "single_arm_cable_row", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "ez_bar_curl", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "incline_dumbbell_bicep_curl", sets = 3, reps = "12", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 17,
                title = "Day 17 — Legs",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Quadriceps", "Hamstrings", "Calves"),
                isRestDay = false,
                warmUp = listOf("Bike + mobility + progressive squat sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "squat", sets = 4, reps = "6-8", restSeconds = 90),
                    ExerciseReference(exerciseId = "leg_press", sets = 4, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "romanian_deadlift", sets = 3, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "leg_extension", sets = 3, reps = "15", restSeconds = 90),
                    ExerciseReference(exerciseId = "lying_leg_curl", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "standing_calf_raise", sets = 4, reps = "15", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 18,
                title = "Day 18 — Shoulders",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Shoulders"),
                isRestDay = false,
                warmUp = listOf("Cardio + shoulder mobility + band activation"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "barbell_overhead_press", sets = 4, reps = "6-8", restSeconds = 90),
                    ExerciseReference(exerciseId = "dumbbell_lateral_raise", sets = 4, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "cable_lateral_raise", sets = 3, reps = "15", restSeconds = 90),
                    ExerciseReference(exerciseId = "reverse_pec_deck", sets = 3, reps = "15", restSeconds = 90),
                    ExerciseReference(exerciseId = "face_pulls", sets = 3, reps = "15", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 19,
                title = "Day 19 — Arms",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Arms", "Biceps", "Triceps"),
                isRestDay = false,
                warmUp = listOf("Cardio + arm mobility + light curls/pushdowns"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "ez_bar_curl", sets = 4, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "ez_bar_preacher_curl", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "close_grip_bench_press", sets = 4, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "overhead_cable_extension", sets = 3, reps = "12", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 20,
                title = "Day 20 — Core + Full Body",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Core", "Full Body"),
                isRestDay = false,
                warmUp = listOf("5–8 min cardio + dynamic mobility"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "front_squat", sets = 3, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "dumbbell_bench", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "pull_ups", sets = 3, reps = "6-10", restSeconds = 90),
                    ExerciseReference(exerciseId = "hip_thrust", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "hanging_leg_raise", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "cable_crunch", sets = 3, reps = "15", restSeconds = 90),
                    ExerciseReference(exerciseId = "plank_hold", sets = 3, reps = "45 sec", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 21,
                title = "Day 21 — Rest",
                workoutType = ProgramWorkoutType.REST.name,
                focus = listOf("Recovery"),
                isRestDay = true,
                warmUp = listOf("Easy walking + mobility"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = emptyList(),
                notes = "Recovery day. Light walking and mobility."
            ),
            ProgramDay(
                dayNumber = 22,
                title = "Day 22 — Chest + Triceps",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Chest", "Triceps"),
                isRestDay = false,
                warmUp = listOf("Cardio + chest/shoulder mobility + light press sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "barbell_bench_press", sets = 4, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "incline_machine_press", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "dumbbell_fly", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "cable_crossover", sets = 3, reps = "15", restSeconds = 90),
                    ExerciseReference(exerciseId = "overhead_cable_extension", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "15", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 23,
                title = "Day 23 — Back + Biceps",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Back", "Biceps"),
                isRestDay = false,
                warmUp = listOf("Rowing + scapular activation + light pulling"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "weighted_pull_ups", sets = 4, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "t_bar_row", sets = 4, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "neutral_grip_pulldown", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "seated_cable_row", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "ez_bar_preacher_curl", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "12", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 24,
                title = "Day 24 — Legs",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Quadriceps", "Hamstrings", "Calves"),
                isRestDay = false,
                warmUp = listOf("Bike + hip/ankle mobility + squat preparation"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "hack_squat", sets = 4, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "bulgarian_split_squat", sets = 3, reps = "10/leg", restSeconds = 90),
                    ExerciseReference(exerciseId = "hip_thrust", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "leg_extension", sets = 3, reps = "15", restSeconds = 90),
                    ExerciseReference(exerciseId = "leg_curl", sets = 3, reps = "15", restSeconds = 90),
                    ExerciseReference(exerciseId = "standing_calf_raise", sets = 4, reps = "15", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 25,
                title = "Day 25 — Shoulders",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Shoulders"),
                isRestDay = false,
                warmUp = listOf("Cardio + shoulder mobility + band activation"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "dumbbell_shoulder_press", sets = 4, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "arnold_press", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "cable_lateral_raise", sets = 4, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "reverse_pec_deck", sets = 4, reps = "15", restSeconds = 90),
                    ExerciseReference(exerciseId = "face_pulls", sets = 3, reps = "15", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 26,
                title = "Day 26 — Arms",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Arms", "Biceps", "Triceps"),
                isRestDay = false,
                warmUp = listOf("Light cardio + arm mobility + light sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "ez_bar_curl", sets = 4, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "cable_curl", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "close_grip_bench_press", sets = 4, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "overhead_cable_extension", sets = 3, reps = "12", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 27,
                title = "Day 27 — Full Body + Core",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Core", "Full Body"),
                isRestDay = false,
                warmUp = listOf("5–8 min cardio + dynamic mobility"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "goblet_squat", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "dumbbell_bench_press", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "wide_grip_lat_pulldown", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "romanian_deadlift", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "hanging_leg_raise", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "cable_crunch", sets = 3, reps = "15", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 28,
                title = "Day 28 — Rest",
                workoutType = ProgramWorkoutType.REST.name,
                focus = listOf("Recovery"),
                isRestDay = true,
                warmUp = listOf("Easy walking + mobility"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = emptyList(),
                notes = "Recovery day. Light walking and mobility."
            ),
            ProgramDay(
                dayNumber = 29,
                title = "Day 29 — Hypertrophy Full Body",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Core", "Full Body"),
                isRestDay = false,
                warmUp = listOf("8–10 min general cardio + full-body dynamic mobility"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "leg_press", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "incline_dumbbell_press", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "seated_cable_row", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "romanian_deadlift", sets = 3, reps = "10", restSeconds = 90),
                    ExerciseReference(exerciseId = "dumbbell_lateral_raise", sets = 3, reps = "15", restSeconds = 90),
                    ExerciseReference(exerciseId = "cable_curl", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "12", restSeconds = 90),
                ),
                notes = "Prioritize muscle contraction quality and progressive load. Rest 60–120 sec between sets."
            ),
            ProgramDay(
                dayNumber = 30,
                title = "Day 30 — Hypertrophy Performance Check",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Hypertrophy"),
                isRestDay = false,
                warmUp = listOf("8–10 min cardio + dynamic mobility + light warm-up sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "barbell_bench_press",
                        sets = 3,
                        reps = "3-5RM",
                        restSeconds = 120
                    ),
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 3,
                        reps = "3-5RM",
                        restSeconds = 120
                    )
                ),
                notes = "Controlled rep-max check on selected lifts; compare reps/load with Day 1 baseline. Optional measurements: body weight, training volume, circumference tracking."
            ),
        ),
        isActive = true,
        version = 1
    )
}
