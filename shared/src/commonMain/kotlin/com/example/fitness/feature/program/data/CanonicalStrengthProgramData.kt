package com.example.fitness.feature.program.data

import com.example.fitness.feature.exercise.model.ExerciseReference
import com.example.fitness.feature.program.model.Program
import com.example.fitness.feature.program.model.ProgramDay
import com.example.fitness.feature.program.model.ProgramType
import com.example.fitness.feature.program.model.ProgramWorkoutType
import com.example.fitness.feature.workout.model.ExperienceLevel

/**
 * Authoritative 30-Day Strength Program.
 * Sourced directly from Athgym_30_Day_Hypertrophy_Strength_Plans.pdf (Part 2).
 * Preserves major lifts, accessory lifts, sets, reps, and Day 30 Performance Test.
 */
object CanonicalStrengthProgramData {

    val PROGRAM_STRENGTH = Program(
        id = "strength_30_day",
        name = "30-Day Strength Program",
        slug = "strength-30-day",
        description = "Authoritative 30-day maximum strength training program focusing on heavy compound lifts and progressive overload.",
        type = ProgramType.STRENGTH.name,
        experienceLevel = ExperienceLevel.ADVANCED.name,
        durationDays = 30,
        trainingDaysPerWeek = 6,
        days = listOf(
            ProgramDay(
                dayNumber = 1,
                title = "Day 1 — Chest + Triceps",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Bench Press", "Chest", "Triceps"),
                isRestDay = false,
                warmUp = listOf("5 min treadmill/bike", "arm circles", "band pull-aparts", "scapular push-ups", "progressive bench warm-up sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "barbell_bench_press", sets = 5, reps = "5", restSeconds = 180),
                    ExerciseReference(exerciseId = "incline_barbell_press", sets = 4, reps = "6", restSeconds = 180),
                    ExerciseReference(exerciseId = "close_grip_bench_press", sets = 3, reps = "6", restSeconds = 120),
                    ExerciseReference(exerciseId = "chest_dips", sets = 3, reps = "6-8", restSeconds = 120),
                    ExerciseReference(exerciseId = "rope_pushdown", sets = 3, reps = "8-10", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
            ),
            ProgramDay(
                dayNumber = 2,
                title = "Day 2 — Back + Biceps",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Deadlift", "Back", "Biceps"),
                isRestDay = false,
                warmUp = listOf("Rowing + scapular activation + progressive pulling sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "deadlift", sets = 5, reps = "3", restSeconds = 180),
                    ExerciseReference(exerciseId = "bent_over_barbell_row", sets = 4, reps = "5", restSeconds = 180),
                    ExerciseReference(exerciseId = "weighted_pull_ups", sets = 4, reps = "5", restSeconds = 180),
                    ExerciseReference(exerciseId = "wide_grip_lat_pulldown", sets = 3, reps = "8", restSeconds = 120),
                    ExerciseReference(exerciseId = "barbell_bicep_curl", sets = 3, reps = "8", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
            ),
            ProgramDay(
                dayNumber = 3,
                title = "Day 3 — Legs",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Squat", "Legs"),
                isRestDay = false,
                warmUp = listOf("Bike + hip/ankle mobility + progressive squat sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "squat", sets = 5, reps = "5", restSeconds = 180),
                    ExerciseReference(exerciseId = "romanian_deadlift", sets = 4, reps = "6", restSeconds = 180),
                    ExerciseReference(exerciseId = "bulgarian_split_squat", sets = 3, reps = "6/leg", restSeconds = 120),
                    ExerciseReference(exerciseId = "leg_curl", sets = 3, reps = "8", restSeconds = 120),
                    ExerciseReference(exerciseId = "standing_calf_raise", sets = 4, reps = "10", restSeconds = 180),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
            ),
            ProgramDay(
                dayNumber = 4,
                title = "Day 4 — Shoulders",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Overhead Press", "Shoulders"),
                isRestDay = false,
                warmUp = listOf("5 min cardio + shoulder mobility + progressive press sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "barbell_overhead_press", sets = 5, reps = "5", restSeconds = 180),
                    ExerciseReference(exerciseId = "push_press", sets = 3, reps = "5", restSeconds = 120),
                    ExerciseReference(exerciseId = "dumbbell_shoulder_press", sets = 3, reps = "6", restSeconds = 120),
                    ExerciseReference(exerciseId = "dumbbell_lateral_raise", sets = 3, reps = "10", restSeconds = 120),
                    ExerciseReference(exerciseId = "face_pulls", sets = 3, reps = "10", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
            ),
            ProgramDay(
                dayNumber = 5,
                title = "Day 5 — Arms + Upper Strength",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Upper Body Strength", "Arms"),
                isRestDay = false,
                warmUp = listOf("Cardio + arm/shoulder mobility + light sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "close_grip_bench_press", sets = 4, reps = "5", restSeconds = 180),
                    ExerciseReference(exerciseId = "weighted_chin_up", sets = 4, reps = "5", restSeconds = 180),
                    ExerciseReference(exerciseId = "ez_bar_curl", sets = 3, reps = "6-8", restSeconds = 120),
                    ExerciseReference(exerciseId = "chest_dips", sets = 3, reps = "6", restSeconds = 120),
                    ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "8", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
            ),
            ProgramDay(
                dayNumber = 6,
                title = "Day 6 — Full-Body Strength",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Full Body Strength"),
                isRestDay = false,
                warmUp = listOf("Cardio + dynamic mobility + progressive compound warm-up sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "front_squat", sets = 4, reps = "5", restSeconds = 180),
                    ExerciseReference(exerciseId = "barbell_bench_press", sets = 4, reps = "5", restSeconds = 180),
                    ExerciseReference(exerciseId = "bent_over_barbell_row", sets = 4, reps = "5", restSeconds = 180),
                    ExerciseReference(exerciseId = "romanian_deadlift", sets = 3, reps = "6", restSeconds = 120),
                    ExerciseReference(exerciseId = "farmer_carry", sets = 3, reps = "30m", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
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
                notes = "Recovery day. Focus on nutrition, sleep, and mobility."
            ),
            ProgramDay(
                dayNumber = 8,
                title = "Day 8 — Bench Strength",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Bench Press", "Chest", "Triceps"),
                isRestDay = false,
                warmUp = listOf("Cardio + shoulder mobility + progressive bench sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "barbell_bench_press", sets = 5, reps = "4", restSeconds = 180),
                    ExerciseReference(exerciseId = "incline_barbell_press", sets = 4, reps = "5", restSeconds = 180),
                    ExerciseReference(exerciseId = "close_grip_bench_press", sets = 3, reps = "6", restSeconds = 120),
                    ExerciseReference(exerciseId = "weighted_dips", sets = 3, reps = "6", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
            ),
            ProgramDay(
                dayNumber = 9,
                title = "Day 9 — Deadlift Strength",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Deadlift", "Back", "Biceps"),
                isRestDay = false,
                warmUp = listOf("Rowing + hip mobility + progressive deadlift sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "deadlift", sets = 5, reps = "3", restSeconds = 180),
                    ExerciseReference(exerciseId = "bent_over_barbell_row", sets = 4, reps = "5", restSeconds = 180),
                    ExerciseReference(exerciseId = "weighted_pull_ups", sets = 4, reps = "5", restSeconds = 180),
                    ExerciseReference(exerciseId = "barbell_bicep_curl", sets = 3, reps = "8", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
            ),
            ProgramDay(
                dayNumber = 10,
                title = "Day 10 — Squat Strength",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Squat", "Legs"),
                isRestDay = false,
                warmUp = listOf("Bike + hip/ankle mobility + progressive squat sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "squat", sets = 5, reps = "4", restSeconds = 180),
                    ExerciseReference(exerciseId = "front_squat", sets = 3, reps = "5", restSeconds = 120),
                    ExerciseReference(exerciseId = "romanian_deadlift", sets = 3, reps = "6", restSeconds = 120),
                    ExerciseReference(exerciseId = "bulgarian_split_squat", sets = 3, reps = "6/leg", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
            ),
            ProgramDay(
                dayNumber = 11,
                title = "Day 11 — Overhead Strength",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Overhead Press", "Shoulders"),
                isRestDay = false,
                warmUp = listOf("Cardio + shoulder mobility + progressive pressing"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "barbell_overhead_press", sets = 5, reps = "4", restSeconds = 180),
                    ExerciseReference(exerciseId = "push_press", sets = 3, reps = "4", restSeconds = 120),
                    ExerciseReference(exerciseId = "dumbbell_press", sets = 3, reps = "6", restSeconds = 120),
                    ExerciseReference(exerciseId = "face_pulls", sets = 3, reps = "10", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
            ),
            ProgramDay(
                dayNumber = 12,
                title = "Day 12 — Upper Strength",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Upper Body Strength", "Arms"),
                isRestDay = false,
                warmUp = listOf("Rowing + scapular activation + progressive upper-body sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "weighted_chin_up", sets = 5, reps = "4", restSeconds = 180),
                    ExerciseReference(exerciseId = "close_grip_bench_press", sets = 4, reps = "5", restSeconds = 180),
                    ExerciseReference(exerciseId = "bent_over_barbell_row", sets = 4, reps = "5", restSeconds = 180),
                    ExerciseReference(exerciseId = "ez_bar_curl", sets = 3, reps = "8", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
            ),
            ProgramDay(
                dayNumber = 13,
                title = "Day 13 — Full Body",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Full Body Strength"),
                isRestDay = false,
                warmUp = listOf("Cardio + mobility + progressive compound warm-up sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "front_squat", sets = 4, reps = "4", restSeconds = 180),
                    ExerciseReference(exerciseId = "barbell_bench_press", sets = 4, reps = "4", restSeconds = 180),
                    ExerciseReference(exerciseId = "deadlift", sets = 3, reps = "3", restSeconds = 120),
                    ExerciseReference(exerciseId = "pull_ups", sets = 3, reps = "6", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
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
                notes = "Recovery day. Focus on nutrition, sleep, and mobility."
            ),
            ProgramDay(
                dayNumber = 15,
                title = "Day 15 — Bench",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Bench Press", "Chest", "Triceps"),
                isRestDay = false,
                warmUp = listOf("Cardio + shoulder mobility + progressive bench sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "barbell_bench_press", sets = 5, reps = "3", restSeconds = 180),
                    ExerciseReference(exerciseId = "incline_barbell_press", sets = 4, reps = "5", restSeconds = 180),
                    ExerciseReference(exerciseId = "close_grip_bench_press", sets = 3, reps = "5", restSeconds = 120),
                    ExerciseReference(exerciseId = "chest_dips", sets = 3, reps = "6", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
            ),
            ProgramDay(
                dayNumber = 16,
                title = "Day 16 — Deadlift",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Deadlift", "Back", "Biceps"),
                isRestDay = false,
                warmUp = listOf("Rowing + hip mobility + progressive deadlift sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "deadlift", sets = 5, reps = "2-3", restSeconds = 180),
                    ExerciseReference(exerciseId = "bent_over_barbell_row", sets = 4, reps = "4", restSeconds = 180),
                    ExerciseReference(exerciseId = "weighted_pull_ups", sets = 4, reps = "4", restSeconds = 180),
                    ExerciseReference(exerciseId = "barbell_bicep_curl", sets = 3, reps = "6-8", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
            ),
            ProgramDay(
                dayNumber = 17,
                title = "Day 17 — Squat",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Squat", "Legs"),
                isRestDay = false,
                warmUp = listOf("Bike + hip/ankle mobility + progressive squat sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "squat", sets = 5, reps = "3", restSeconds = 180),
                    ExerciseReference(exerciseId = "front_squat", sets = 3, reps = "4", restSeconds = 120),
                    ExerciseReference(exerciseId = "romanian_deadlift", sets = 3, reps = "5", restSeconds = 120),
                    ExerciseReference(exerciseId = "bulgarian_split_squat", sets = 3, reps = "6", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
            ),
            ProgramDay(
                dayNumber = 18,
                title = "Day 18 — Shoulders",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Overhead Press", "Shoulders"),
                isRestDay = false,
                warmUp = listOf("Cardio + shoulder mobility + progressive press sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "barbell_overhead_press", sets = 5, reps = "3", restSeconds = 180),
                    ExerciseReference(exerciseId = "push_press", sets = 3, reps = "3", restSeconds = 120),
                    ExerciseReference(exerciseId = "dumbbell_press", sets = 3, reps = "6", restSeconds = 120),
                    ExerciseReference(exerciseId = "face_pulls", sets = 3, reps = "10", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
            ),
            ProgramDay(
                dayNumber = 19,
                title = "Day 19 — Upper Strength",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Upper Body Strength", "Arms"),
                isRestDay = false,
                warmUp = listOf("Rowing + scapular activation + progressive upper-body sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "weighted_chin_up", sets = 4, reps = "4", restSeconds = 180),
                    ExerciseReference(exerciseId = "close_grip_bench_press", sets = 4, reps = "4", restSeconds = 180),
                    ExerciseReference(exerciseId = "bent_over_barbell_row", sets = 4, reps = "5", restSeconds = 180),
                    ExerciseReference(exerciseId = "dumbbell_hammer_curl", sets = 3, reps = "8", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
            ),
            ProgramDay(
                dayNumber = 20,
                title = "Day 20 — Full Body",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Full Body Strength"),
                isRestDay = false,
                warmUp = listOf("Cardio + dynamic mobility + progressive compound sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "squat", sets = 4, reps = "3", restSeconds = 180),
                    ExerciseReference(exerciseId = "barbell_bench_press", sets = 4, reps = "3", restSeconds = 180),
                    ExerciseReference(exerciseId = "deadlift", sets = 3, reps = "2-3", restSeconds = 120),
                    ExerciseReference(exerciseId = "farmer_carry", sets = 3, reps = "30m", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
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
                notes = "Recovery day. Focus on nutrition, sleep, and mobility."
            ),
            ProgramDay(
                dayNumber = 22,
                title = "Day 22 — Bench",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Bench Press", "Chest", "Triceps"),
                isRestDay = false,
                warmUp = listOf("Cardio + shoulder mobility + progressive bench sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "barbell_bench_press", sets = 5, reps = "3", restSeconds = 180),
                    ExerciseReference(exerciseId = "incline_barbell_press", sets = 3, reps = "5", restSeconds = 120),
                    ExerciseReference(exerciseId = "close_grip_bench_press", sets = 3, reps = "5", restSeconds = 120),
                    ExerciseReference(exerciseId = "chest_dips", sets = 3, reps = "6", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
            ),
            ProgramDay(
                dayNumber = 23,
                title = "Day 23 — Deadlift",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Deadlift", "Back", "Biceps"),
                isRestDay = false,
                warmUp = listOf("Rowing + hip mobility + progressive deadlift sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "deadlift", sets = 4, reps = "3", restSeconds = 180),
                    ExerciseReference(exerciseId = "bent_over_barbell_row", sets = 4, reps = "5", restSeconds = 180),
                    ExerciseReference(exerciseId = "weighted_pull_ups", sets = 4, reps = "5", restSeconds = 180),
                    ExerciseReference(exerciseId = "barbell_bicep_curl", sets = 3, reps = "8", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
            ),
            ProgramDay(
                dayNumber = 24,
                title = "Day 24 — Squat",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Squat", "Legs"),
                isRestDay = false,
                warmUp = listOf("Bike + hip/ankle mobility + progressive squat sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "squat", sets = 5, reps = "3", restSeconds = 180),
                    ExerciseReference(exerciseId = "front_squat", sets = 3, reps = "4", restSeconds = 120),
                    ExerciseReference(exerciseId = "romanian_deadlift", sets = 3, reps = "5", restSeconds = 120),
                    ExerciseReference(exerciseId = "bulgarian_split_squat", sets = 3, reps = "6", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
            ),
            ProgramDay(
                dayNumber = 25,
                title = "Day 25 — Overhead Press",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Overhead Press", "Shoulders"),
                isRestDay = false,
                warmUp = listOf("Cardio + shoulder mobility + progressive press sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "barbell_overhead_press", sets = 5, reps = "3", restSeconds = 180),
                    ExerciseReference(exerciseId = "push_press", sets = 3, reps = "3", restSeconds = 120),
                    ExerciseReference(exerciseId = "dumbbell_press", sets = 3, reps = "6", restSeconds = 120),
                    ExerciseReference(exerciseId = "dumbbell_lateral_raise", sets = 3, reps = "10", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
            ),
            ProgramDay(
                dayNumber = 26,
                title = "Day 26 — Upper Strength",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Upper Body Strength", "Arms"),
                isRestDay = false,
                warmUp = listOf("Rowing + scapular activation + progressive upper-body sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "weighted_chin_up", sets = 4, reps = "4", restSeconds = 180),
                    ExerciseReference(exerciseId = "close_grip_bench_press", sets = 4, reps = "4", restSeconds = 180),
                    ExerciseReference(exerciseId = "bent_over_barbell_row", sets = 4, reps = "5", restSeconds = 180),
                    ExerciseReference(exerciseId = "ez_bar_curl", sets = 3, reps = "8", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
            ),
            ProgramDay(
                dayNumber = 27,
                title = "Day 27 — Full-Body Strength",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Full Body Strength"),
                isRestDay = false,
                warmUp = listOf("Cardio + dynamic mobility + progressive compound sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "squat", sets = 4, reps = "3", restSeconds = 180),
                    ExerciseReference(exerciseId = "barbell_bench_press", sets = 4, reps = "3", restSeconds = 180),
                    ExerciseReference(exerciseId = "deadlift", sets = 3, reps = "2", restSeconds = 120),
                    ExerciseReference(exerciseId = "barbell_overhead_press", sets = 3, reps = "4", restSeconds = 120),
                    ExerciseReference(exerciseId = "farmer_carry", sets = 3, reps = "30m", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
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
                notes = "Recovery day. Focus on nutrition, sleep, and mobility."
            ),
            ProgramDay(
                dayNumber = 29,
                title = "Day 29 — Strength Test Preparation",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Strength"),
                isRestDay = false,
                warmUp = listOf("15–20 min: cardio, mobility, empty-bar work and progressive sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "squat", sets = 3, reps = "3 moderate-heavy", restSeconds = 120),
                    ExerciseReference(exerciseId = "barbell_bench_press", sets = 3, reps = "3 moderate-heavy", restSeconds = 120),
                    ExerciseReference(exerciseId = "deadlift", sets = 3, reps = "2 moderate-heavy", restSeconds = 120),
                ),
                notes = "Rest 2–5 min on major compound lifts, 60–120 sec on accessory movements."
            ),
            ProgramDay(
                dayNumber = 30,
                title = "Day 30 — Strength Performance Test",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Strength"),
                isRestDay = false,
                warmUp = listOf("15–20 min: general warm-up, mobility and progressive test-specific sets"),
                coolDown = listOf("5–10 min relevant mobility/stretching"),
                exercises = listOf(
                    ExerciseReference(exerciseId = "squat", sets = 3, reps = "1RM / 3-5RM", restSeconds = 180),
                    ExerciseReference(exerciseId = "barbell_bench_press", sets = 3, reps = "1RM / 3-5RM", restSeconds = 180),
                    ExerciseReference(exerciseId = "deadlift", sets = 3, reps = "1RM / 3-5RM", restSeconds = 180),
                    ExerciseReference(exerciseId = "barbell_overhead_press", sets = 3, reps = "1RM / 3-5RM", restSeconds = 180)
                ),
                notes = "Strength Performance Test: Squat, Bench Press, Deadlift, Overhead Press estimated 1RM or controlled 3–5RM."
            ),
        ),
        isActive = true,
        version = 1
    )
}
