package com.example.fitness.feature.program.data

import com.example.fitness.feature.exercise.model.ExerciseReference
import com.example.fitness.feature.program.model.Program
import com.example.fitness.feature.program.model.ProgramDay
import com.example.fitness.feature.workout.model.ExperienceLevel

/**
 * Authoritative Canonical Female 30-Day Workout Programs.
 * Sourced exclusively from AthGym_9_Female_30_Day_Workout_Programs.pdf.
 * 9 programs × 30 days = 270 programmed days.
 */
object CanonicalFemaleProgramData {

    val PROGRAM_FEMALE_WEIGHT_LOSS: Program = Program(
        id = "female_weight_loss",
        name = "Weight Loss",
        slug = "female_weight_loss",
        description = "Fat-loss-oriented conditioning with resistance training. Progression: Days 1–7 Foundation → 8–14 Build → 15–21 Progress → 22–28 Challenge → 29–30 Consolidation.",
        type = "THIRTY_DAY",
        experienceLevel = ExperienceLevel.BEGINNER.name,
        durationDays = 30,
        trainingDaysPerWeek = 5,
        days = listOf(
            ProgramDay(
                dayNumber = 1,
                title = "Day 1 — Full Body + Cardio",
                workoutType = "TRAINING",
                focus = listOf("Full Body", "Cardio"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "mountain_climber",
                        sets = 3,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 2,
                title = "Day 2 — Lower Body",
                workoutType = "TRAINING",
                focus = listOf("Lower Body"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "glute_bridge",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "Incline Walk 15 min",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 3,
                title = "Day 3 — HIIT + Core",
                workoutType = "TRAINING",
                focus = listOf("HIIT", "Core"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "battle_rope",
                        sets = 6,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "kettlebell_swing",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "high_knees",
                        sets = 4,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "bicycle_crunch",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "plank_hold",
                        sets = 3,
                        reps = "40 sec",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 4,
                title = "Day 4 — Upper Body",
                workoutType = "TRAINING",
                focus = listOf("Upper Body"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_lateral_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bicep_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "triceps_pushdown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 5,
                title = "Day 5 — Lower Body + Conditioning",
                workoutType = "TRAINING",
                focus = listOf("Lower Body", "Conditioning"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "step_up",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_extension",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "standing_calf_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_treadmill",
                        sets = 3,
                        reps = "Incline Treadmill 15 min",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 6,
                title = "Day 6 — Full Body Circuit",
                workoutType = "TRAINING",
                focus = listOf("Full Body Circuit"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "Squat 12 Push-up 10 DB Row 10 Reverse Lunge 10/leg Shoulder Press 10 Bicycle Crunch 20 — 3 rounds",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 7,
                title = "Day 7 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 8,
                title = "Day 8 — Full Body + Cardio",
                workoutType = "TRAINING",
                focus = listOf("Full Body", "Cardio"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "mountain_climber",
                        sets = 3,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 9,
                title = "Day 9 — Lower Body",
                workoutType = "TRAINING",
                focus = listOf("Lower Body"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "glute_bridge",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "Incline Walk 15 min",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 10,
                title = "Day 10 — HIIT + Core",
                workoutType = "TRAINING",
                focus = listOf("HIIT", "Core"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "battle_rope",
                        sets = 6,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "kettlebell_swing",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "high_knees",
                        sets = 4,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "bicycle_crunch",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "plank_hold",
                        sets = 3,
                        reps = "40 sec",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 11,
                title = "Day 11 — Upper Body",
                workoutType = "TRAINING",
                focus = listOf("Upper Body"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_lateral_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bicep_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "triceps_pushdown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 12,
                title = "Day 12 — Lower Body + Conditioning",
                workoutType = "TRAINING",
                focus = listOf("Lower Body", "Conditioning"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "step_up",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_extension",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "standing_calf_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_treadmill",
                        sets = 3,
                        reps = "Incline Treadmill 15 min",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 13,
                title = "Day 13 — Full Body Circuit",
                workoutType = "TRAINING",
                focus = listOf("Full Body Circuit"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "Squat 12 Push-up 10 DB Row 10 Reverse Lunge 10/leg Shoulder Press 10 Bicycle Crunch 20 — 3 rounds",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 14,
                title = "Day 14 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 15,
                title = "Day 15 — Full Body + Cardio",
                workoutType = "TRAINING",
                focus = listOf("Full Body", "Cardio"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "mountain_climber",
                        sets = 3,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 16,
                title = "Day 16 — Lower Body",
                workoutType = "TRAINING",
                focus = listOf("Lower Body"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "glute_bridge",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "Incline Walk 15 min",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 17,
                title = "Day 17 — HIIT + Core",
                workoutType = "TRAINING",
                focus = listOf("HIIT", "Core"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "battle_rope",
                        sets = 6,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "kettlebell_swing",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "high_knees",
                        sets = 4,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "bicycle_crunch",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "plank_hold",
                        sets = 3,
                        reps = "40 sec",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 18,
                title = "Day 18 — Upper Body",
                workoutType = "TRAINING",
                focus = listOf("Upper Body"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_lateral_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bicep_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "triceps_pushdown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 19,
                title = "Day 19 — Lower Body + Conditioning",
                workoutType = "TRAINING",
                focus = listOf("Lower Body", "Conditioning"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "step_up",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_extension",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "standing_calf_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_treadmill",
                        sets = 3,
                        reps = "Incline Treadmill 15 min",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 20,
                title = "Day 20 — Full Body Circuit",
                workoutType = "TRAINING",
                focus = listOf("Full Body Circuit"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "Squat 12 Push-up 10 DB Row 10 Reverse Lunge 10/leg Shoulder Press 10 Bicycle Crunch 20 — 3 rounds",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 21,
                title = "Day 21 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 22,
                title = "Day 22 — Full Body + Cardio",
                workoutType = "TRAINING",
                focus = listOf("Full Body", "Cardio"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "mountain_climber",
                        sets = 3,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 23,
                title = "Day 23 — Lower Body",
                workoutType = "TRAINING",
                focus = listOf("Lower Body"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "glute_bridge",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "Incline Walk 15 min",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 24,
                title = "Day 24 — HIIT + Core",
                workoutType = "TRAINING",
                focus = listOf("HIIT", "Core"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "battle_rope",
                        sets = 6,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "kettlebell_swing",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "high_knees",
                        sets = 4,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "bicycle_crunch",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "plank_hold",
                        sets = 3,
                        reps = "40 sec",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 25,
                title = "Day 25 — Upper Body",
                workoutType = "TRAINING",
                focus = listOf("Upper Body"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_lateral_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bicep_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "triceps_pushdown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 26,
                title = "Day 26 — Lower Body + Conditioning",
                workoutType = "TRAINING",
                focus = listOf("Lower Body", "Conditioning"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "step_up",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_extension",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "standing_calf_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_treadmill",
                        sets = 3,
                        reps = "Incline Treadmill 15 min",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 27,
                title = "Day 27 — Full Body Circuit",
                workoutType = "TRAINING",
                focus = listOf("Full Body Circuit"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "Squat 12 Push-up 10 DB Row 10 Reverse Lunge 10/leg Shoulder Press 10 Bicycle Crunch 20 — 3 rounds",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 28,
                title = "Day 28 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 29,
                title = "Day 29 — Full Body + Cardio",
                workoutType = "TRAINING",
                focus = listOf("Full Body", "Cardio"),
                isRestDay = false,
                notes = "Consolidate progress; avoid unnecessary max-effort work.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "mountain_climber",
                        sets = 3,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 30,
                title = "Day 30 — Lower Body",
                workoutType = "TRAINING",
                focus = listOf("Lower Body"),
                isRestDay = false,
                notes = "Consolidate progress; avoid unnecessary max-effort work.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "glute_bridge",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "Incline Walk 15 min",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                )
            ),
        )
    )

    val PROGRAM_FEMALE_SCULPTED_BODY: Program = Program(
        id = "female_sculpted_body",
        name = "Sculpted Body",
        slug = "female_sculpted_body",
        description = "Muscle development, definition and balanced physique training. Progression: Days 1–7 Foundation → 8–14 Build → 15–21 Progress → 22–28 Challenge → 29–30 Consolidation.",
        type = "THIRTY_DAY",
        experienceLevel = ExperienceLevel.BEGINNER.name,
        durationDays = 30,
        trainingDaysPerWeek = 5,
        days = listOf(
            ProgramDay(
                dayNumber = 1,
                title = "Day 1 — Glutes + Hamstrings",
                workoutType = "TRAINING",
                focus = listOf("Glutes", "Hamstrings"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 4,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 2,
                title = "Day 2 — Back + Shoulders",
                workoutType = "TRAINING",
                focus = listOf("Back", "Shoulders"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 4,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_lateral_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "face_pulls",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 3,
                title = "Day 3 — Quads + Glutes",
                workoutType = "TRAINING",
                focus = listOf("Quads", "Glutes"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 4,
                        reps = "8–10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "12/leg",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_extension",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 4,
                title = "Day 4 — Chest + Arms + Core",
                workoutType = "TRAINING",
                focus = listOf("Chest", "Arms", "Core"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_bench_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_dumbbell_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bicep_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "triceps_pushdown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_crunch",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "plank_hold",
                        sets = 3,
                        reps = "45 sec",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 5,
                title = "Day 5 — Glute Focus",
                workoutType = "TRAINING",
                focus = listOf("Glute Focus"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "sumo_squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "step_up",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 6,
                title = "Day 6 — Full Body",
                workoutType = "TRAINING",
                focus = listOf("Full Body"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "push_ups",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "reverse_lunge",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "mountain_climber",
                        sets = 3,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 7,
                title = "Day 7 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 8,
                title = "Day 8 — Glutes + Hamstrings",
                workoutType = "TRAINING",
                focus = listOf("Glutes", "Hamstrings"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 4,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 9,
                title = "Day 9 — Back + Shoulders",
                workoutType = "TRAINING",
                focus = listOf("Back", "Shoulders"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 4,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_lateral_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "face_pulls",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 10,
                title = "Day 10 — Quads + Glutes",
                workoutType = "TRAINING",
                focus = listOf("Quads", "Glutes"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 4,
                        reps = "8–10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "12/leg",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_extension",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 11,
                title = "Day 11 — Chest + Arms + Core",
                workoutType = "TRAINING",
                focus = listOf("Chest", "Arms", "Core"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_bench_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_dumbbell_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bicep_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "triceps_pushdown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_crunch",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "plank_hold",
                        sets = 3,
                        reps = "45 sec",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 12,
                title = "Day 12 — Glute Focus",
                workoutType = "TRAINING",
                focus = listOf("Glute Focus"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "sumo_squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "step_up",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 13,
                title = "Day 13 — Full Body",
                workoutType = "TRAINING",
                focus = listOf("Full Body"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "push_ups",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "reverse_lunge",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "mountain_climber",
                        sets = 3,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 14,
                title = "Day 14 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 15,
                title = "Day 15 — Glutes + Hamstrings",
                workoutType = "TRAINING",
                focus = listOf("Glutes", "Hamstrings"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 4,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 16,
                title = "Day 16 — Back + Shoulders",
                workoutType = "TRAINING",
                focus = listOf("Back", "Shoulders"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 4,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_lateral_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "face_pulls",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 17,
                title = "Day 17 — Quads + Glutes",
                workoutType = "TRAINING",
                focus = listOf("Quads", "Glutes"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 4,
                        reps = "8–10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "12/leg",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_extension",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 18,
                title = "Day 18 — Chest + Arms + Core",
                workoutType = "TRAINING",
                focus = listOf("Chest", "Arms", "Core"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_bench_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_dumbbell_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bicep_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "triceps_pushdown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_crunch",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "plank_hold",
                        sets = 3,
                        reps = "45 sec",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 19,
                title = "Day 19 — Glute Focus",
                workoutType = "TRAINING",
                focus = listOf("Glute Focus"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "sumo_squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "step_up",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 20,
                title = "Day 20 — Full Body",
                workoutType = "TRAINING",
                focus = listOf("Full Body"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "push_ups",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "reverse_lunge",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "mountain_climber",
                        sets = 3,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 21,
                title = "Day 21 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 22,
                title = "Day 22 — Glutes + Hamstrings",
                workoutType = "TRAINING",
                focus = listOf("Glutes", "Hamstrings"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 4,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 23,
                title = "Day 23 — Back + Shoulders",
                workoutType = "TRAINING",
                focus = listOf("Back", "Shoulders"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 4,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_lateral_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "face_pulls",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 24,
                title = "Day 24 — Quads + Glutes",
                workoutType = "TRAINING",
                focus = listOf("Quads", "Glutes"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 4,
                        reps = "8–10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "12/leg",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_extension",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 25,
                title = "Day 25 — Chest + Arms + Core",
                workoutType = "TRAINING",
                focus = listOf("Chest", "Arms", "Core"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_bench_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_dumbbell_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bicep_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "triceps_pushdown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_crunch",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "plank_hold",
                        sets = 3,
                        reps = "45 sec",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 26,
                title = "Day 26 — Glute Focus",
                workoutType = "TRAINING",
                focus = listOf("Glute Focus"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "sumo_squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "step_up",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 27,
                title = "Day 27 — Full Body",
                workoutType = "TRAINING",
                focus = listOf("Full Body"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "push_ups",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "reverse_lunge",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "mountain_climber",
                        sets = 3,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 28,
                title = "Day 28 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 29,
                title = "Day 29 — Glutes + Hamstrings",
                workoutType = "TRAINING",
                focus = listOf("Glutes", "Hamstrings"),
                isRestDay = false,
                notes = "Consolidate progress; avoid unnecessary max-effort work.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 4,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 30,
                title = "Day 30 — Back + Shoulders",
                workoutType = "TRAINING",
                focus = listOf("Back", "Shoulders"),
                isRestDay = false,
                notes = "Consolidate progress; avoid unnecessary max-effort work.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 4,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_lateral_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "face_pulls",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                )
            ),
        )
    )

    val PROGRAM_FEMALE_PERFECT_BUTT: Program = Program(
        id = "female_perfect_butt",
        name = "Perfect Butt",
        slug = "female_perfect_butt",
        description = "Glute-focused hypertrophy with supporting lower-body work. Progression: Days 1–7 Foundation → 8–14 Build → 15–21 Progress → 22–28 Challenge → 29–30 Consolidation.",
        type = "THIRTY_DAY",
        experienceLevel = ExperienceLevel.BEGINNER.name,
        durationDays = 30,
        trainingDaysPerWeek = 5,
        days = listOf(
            ProgramDay(
                dayNumber = 1,
                title = "Day 1 — Glute Strength",
                workoutType = "TRAINING",
                focus = listOf("Glute Strength"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "barbell_hip_thrust",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 2,
                title = "Day 2 — Upper Body",
                workoutType = "TRAINING",
                focus = listOf("Upper Body"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_lateral_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bicep_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 3,
                title = "Day 3 — Glute + Quads",
                workoutType = "TRAINING",
                focus = listOf("Glute", "Quads"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 4,
                        reps = "8–10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "12/leg",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "step_up",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 4,
                title = "Day 4 — Active Recovery",
                workoutType = "REST",
                focus = listOf("Active Recovery"),
                isRestDay = true,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 5,
                title = "Day 5 — Glute Isolation",
                workoutType = "TRAINING",
                focus = listOf("Glute Isolation"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 4,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "single_leg_glute_bridge",
                        sets = 3,
                        reps = "12/leg",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "frog_pumps",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 4,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 6,
                title = "Day 6 — Legs + Glutes",
                workoutType = "TRAINING",
                focus = listOf("Legs", "Glutes"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "standing_calf_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 7,
                title = "Day 7 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 8,
                title = "Day 8 — Glute Strength",
                workoutType = "TRAINING",
                focus = listOf("Glute Strength"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "barbell_hip_thrust",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 9,
                title = "Day 9 — Upper Body",
                workoutType = "TRAINING",
                focus = listOf("Upper Body"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_lateral_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bicep_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 10,
                title = "Day 10 — Glute + Quads",
                workoutType = "TRAINING",
                focus = listOf("Glute", "Quads"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 4,
                        reps = "8–10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "12/leg",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "step_up",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 11,
                title = "Day 11 — Active Recovery",
                workoutType = "REST",
                focus = listOf("Active Recovery"),
                isRestDay = true,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 12,
                title = "Day 12 — Glute Isolation",
                workoutType = "TRAINING",
                focus = listOf("Glute Isolation"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 4,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "single_leg_glute_bridge",
                        sets = 3,
                        reps = "12/leg",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "frog_pumps",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 4,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 13,
                title = "Day 13 — Legs + Glutes",
                workoutType = "TRAINING",
                focus = listOf("Legs", "Glutes"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "standing_calf_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 14,
                title = "Day 14 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 15,
                title = "Day 15 — Glute Strength",
                workoutType = "TRAINING",
                focus = listOf("Glute Strength"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "barbell_hip_thrust",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 16,
                title = "Day 16 — Upper Body",
                workoutType = "TRAINING",
                focus = listOf("Upper Body"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_lateral_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bicep_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 17,
                title = "Day 17 — Glute + Quads",
                workoutType = "TRAINING",
                focus = listOf("Glute", "Quads"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 4,
                        reps = "8–10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "12/leg",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "step_up",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 18,
                title = "Day 18 — Active Recovery",
                workoutType = "REST",
                focus = listOf("Active Recovery"),
                isRestDay = true,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 19,
                title = "Day 19 — Glute Isolation",
                workoutType = "TRAINING",
                focus = listOf("Glute Isolation"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 4,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "single_leg_glute_bridge",
                        sets = 3,
                        reps = "12/leg",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "frog_pumps",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 4,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 20,
                title = "Day 20 — Legs + Glutes",
                workoutType = "TRAINING",
                focus = listOf("Legs", "Glutes"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "standing_calf_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 21,
                title = "Day 21 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 22,
                title = "Day 22 — Glute Strength",
                workoutType = "TRAINING",
                focus = listOf("Glute Strength"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "barbell_hip_thrust",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 23,
                title = "Day 23 — Upper Body",
                workoutType = "TRAINING",
                focus = listOf("Upper Body"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_lateral_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bicep_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 24,
                title = "Day 24 — Glute + Quads",
                workoutType = "TRAINING",
                focus = listOf("Glute", "Quads"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 4,
                        reps = "8–10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "12/leg",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "step_up",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 25,
                title = "Day 25 — Active Recovery",
                workoutType = "REST",
                focus = listOf("Active Recovery"),
                isRestDay = true,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 26,
                title = "Day 26 — Glute Isolation",
                workoutType = "TRAINING",
                focus = listOf("Glute Isolation"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 4,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "single_leg_glute_bridge",
                        sets = 3,
                        reps = "12/leg",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "frog_pumps",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 4,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 27,
                title = "Day 27 — Legs + Glutes",
                workoutType = "TRAINING",
                focus = listOf("Legs", "Glutes"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "standing_calf_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 28,
                title = "Day 28 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 29,
                title = "Day 29 — Glute Strength",
                workoutType = "TRAINING",
                focus = listOf("Glute Strength"),
                isRestDay = false,
                notes = "Consolidate progress; avoid unnecessary max-effort work.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "barbell_hip_thrust",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 30,
                title = "Day 30 — Upper Body",
                workoutType = "TRAINING",
                focus = listOf("Upper Body"),
                isRestDay = false,
                notes = "Consolidate progress; avoid unnecessary max-effort work.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_lateral_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bicep_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                )
            ),
        )
    )

    val PROGRAM_FEMALE_6_PACK_ABS: Program = Program(
        id = "female_6_pack_abs",
        name = "6-Pack Abs",
        slug = "female_6_pack_abs",
        description = "Core strength and development plus full-body training. Progression: Days 1–7 Foundation → 8–14 Build → 15–21 Progress → 22–28 Challenge → 29–30 Consolidation.",
        type = "THIRTY_DAY",
        experienceLevel = ExperienceLevel.BEGINNER.name,
        durationDays = 30,
        trainingDaysPerWeek = 5,
        days = listOf(
            ProgramDay(
                dayNumber = 1,
                title = "Day 1 — Upper Abs",
                workoutType = "TRAINING",
                focus = listOf("Upper Abs"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "cable_crunch",
                        sets = 4,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "reverse_crunch",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "plank_hold",
                        sets = 3,
                        reps = "45 sec",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dead_bug",
                        sets = 3,
                        reps = "12/side",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 2,
                title = "Day 2 — Full Body + Core",
                workoutType = "TRAINING",
                focus = listOf("Full Body", "Core"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "push_ups",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "12/leg",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "mountain_climber",
                        sets = 3,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 3,
                title = "Day 3 — Lower Abs",
                workoutType = "TRAINING",
                focus = listOf("Lower Abs"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hanging_knee_raise",
                        sets = 4,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "reverse_crunch",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "hanging_leg_raise",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "hollow_body_hold",
                        sets = 3,
                        reps = "20–30 sec",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 4,
                title = "Day 4 — Obliques",
                workoutType = "TRAINING",
                focus = listOf("Obliques"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "cable_woodchop",
                        sets = 3,
                        reps = "12/side",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "side_plank",
                        sets = 3,
                        reps = "30 sec/side",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "weighted_russian_twist",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "pallof_press",
                        sets = 3,
                        reps = "12/side",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 5,
                title = "Day 5 — Full Body",
                workoutType = "TRAINING",
                focus = listOf("Full Body"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_crunch",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 6,
                title = "Day 6 — Core Circuit",
                workoutType = "TRAINING",
                focus = listOf("Core Circuit"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "bicycle_crunch",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "hanging_leg_raise",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "mountain_climber",
                        sets = 3,
                        reps = "30",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "plank_hold",
                        sets = 3,
                        reps = "45 sec",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "Dead Bug ×12/side — 4 rounds",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 7,
                title = "Day 7 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 8,
                title = "Day 8 — Upper Abs",
                workoutType = "TRAINING",
                focus = listOf("Upper Abs"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "cable_crunch",
                        sets = 4,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "reverse_crunch",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "plank_hold",
                        sets = 3,
                        reps = "45 sec",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dead_bug",
                        sets = 3,
                        reps = "12/side",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 9,
                title = "Day 9 — Full Body + Core",
                workoutType = "TRAINING",
                focus = listOf("Full Body", "Core"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "push_ups",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "12/leg",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "mountain_climber",
                        sets = 3,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 10,
                title = "Day 10 — Lower Abs",
                workoutType = "TRAINING",
                focus = listOf("Lower Abs"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hanging_knee_raise",
                        sets = 4,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "reverse_crunch",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "hanging_leg_raise",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "hollow_body_hold",
                        sets = 3,
                        reps = "20–30 sec",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 11,
                title = "Day 11 — Obliques",
                workoutType = "TRAINING",
                focus = listOf("Obliques"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "cable_woodchop",
                        sets = 3,
                        reps = "12/side",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "side_plank",
                        sets = 3,
                        reps = "30 sec/side",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "weighted_russian_twist",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "pallof_press",
                        sets = 3,
                        reps = "12/side",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 12,
                title = "Day 12 — Full Body",
                workoutType = "TRAINING",
                focus = listOf("Full Body"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_crunch",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 13,
                title = "Day 13 — Core Circuit",
                workoutType = "TRAINING",
                focus = listOf("Core Circuit"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "bicycle_crunch",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "hanging_leg_raise",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "mountain_climber",
                        sets = 3,
                        reps = "30",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "plank_hold",
                        sets = 3,
                        reps = "45 sec",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "Dead Bug ×12/side — 4 rounds",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 14,
                title = "Day 14 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 15,
                title = "Day 15 — Upper Abs",
                workoutType = "TRAINING",
                focus = listOf("Upper Abs"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "cable_crunch",
                        sets = 4,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "reverse_crunch",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "plank_hold",
                        sets = 3,
                        reps = "45 sec",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dead_bug",
                        sets = 3,
                        reps = "12/side",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 16,
                title = "Day 16 — Full Body + Core",
                workoutType = "TRAINING",
                focus = listOf("Full Body", "Core"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "push_ups",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "12/leg",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "mountain_climber",
                        sets = 3,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 17,
                title = "Day 17 — Lower Abs",
                workoutType = "TRAINING",
                focus = listOf("Lower Abs"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hanging_knee_raise",
                        sets = 4,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "reverse_crunch",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "hanging_leg_raise",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "hollow_body_hold",
                        sets = 3,
                        reps = "20–30 sec",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 18,
                title = "Day 18 — Obliques",
                workoutType = "TRAINING",
                focus = listOf("Obliques"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "cable_woodchop",
                        sets = 3,
                        reps = "12/side",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "side_plank",
                        sets = 3,
                        reps = "30 sec/side",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "weighted_russian_twist",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "pallof_press",
                        sets = 3,
                        reps = "12/side",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 19,
                title = "Day 19 — Full Body",
                workoutType = "TRAINING",
                focus = listOf("Full Body"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_crunch",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 20,
                title = "Day 20 — Core Circuit",
                workoutType = "TRAINING",
                focus = listOf("Core Circuit"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "bicycle_crunch",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "hanging_leg_raise",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "mountain_climber",
                        sets = 3,
                        reps = "30",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "plank_hold",
                        sets = 3,
                        reps = "45 sec",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "Dead Bug ×12/side — 4 rounds",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 21,
                title = "Day 21 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 22,
                title = "Day 22 — Upper Abs",
                workoutType = "TRAINING",
                focus = listOf("Upper Abs"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "cable_crunch",
                        sets = 4,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "reverse_crunch",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "plank_hold",
                        sets = 3,
                        reps = "45 sec",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dead_bug",
                        sets = 3,
                        reps = "12/side",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 23,
                title = "Day 23 — Full Body + Core",
                workoutType = "TRAINING",
                focus = listOf("Full Body", "Core"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "push_ups",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "12/leg",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "mountain_climber",
                        sets = 3,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 24,
                title = "Day 24 — Lower Abs",
                workoutType = "TRAINING",
                focus = listOf("Lower Abs"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hanging_knee_raise",
                        sets = 4,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "reverse_crunch",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "hanging_leg_raise",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "hollow_body_hold",
                        sets = 3,
                        reps = "20–30 sec",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 25,
                title = "Day 25 — Obliques",
                workoutType = "TRAINING",
                focus = listOf("Obliques"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "cable_woodchop",
                        sets = 3,
                        reps = "12/side",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "side_plank",
                        sets = 3,
                        reps = "30 sec/side",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "weighted_russian_twist",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "pallof_press",
                        sets = 3,
                        reps = "12/side",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 26,
                title = "Day 26 — Full Body",
                workoutType = "TRAINING",
                focus = listOf("Full Body"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_crunch",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 27,
                title = "Day 27 — Core Circuit",
                workoutType = "TRAINING",
                focus = listOf("Core Circuit"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "bicycle_crunch",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "hanging_leg_raise",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "mountain_climber",
                        sets = 3,
                        reps = "30",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "plank_hold",
                        sets = 3,
                        reps = "45 sec",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "Dead Bug ×12/side — 4 rounds",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 28,
                title = "Day 28 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 29,
                title = "Day 29 — Upper Abs",
                workoutType = "TRAINING",
                focus = listOf("Upper Abs"),
                isRestDay = false,
                notes = "Consolidate progress; avoid unnecessary max-effort work.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "cable_crunch",
                        sets = 4,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "reverse_crunch",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "plank_hold",
                        sets = 3,
                        reps = "45 sec",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "dead_bug",
                        sets = 3,
                        reps = "12/side",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 30,
                title = "Day 30 — Full Body + Core",
                workoutType = "TRAINING",
                focus = listOf("Full Body", "Core"),
                isRestDay = false,
                notes = "Consolidate progress; avoid unnecessary max-effort work. App note: Core training strengthens the abdominal muscles; visible abdominal definition also depends on overall body composition and individual physiology.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work. App note: Core training strengthens the abdominal muscles; visible abdominal definition also depends on overall body composition and individual physiology."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work. App note: Core training strengthens the abdominal muscles; visible abdominal definition also depends on overall body composition and individual physiology."
                    ),
                    ExerciseReference(
                        exerciseId = "push_ups",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work. App note: Core training strengthens the abdominal muscles; visible abdominal definition also depends on overall body composition and individual physiology."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "12/leg",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work. App note: Core training strengthens the abdominal muscles; visible abdominal definition also depends on overall body composition and individual physiology."
                    ),
                    ExerciseReference(
                        exerciseId = "mountain_climber",
                        sets = 3,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work. App note: Core training strengthens the abdominal muscles; visible abdominal definition also depends on overall body composition and individual physiology."
                    ),
                )
            ),
        )
    )

    val PROGRAM_FEMALE_GENERAL_MUSCLE_BUILDING: Program = Program(
        id = "female_general_muscle_building",
        name = "General Muscle Building",
        slug = "female_general_muscle_building",
        description = "General hypertrophy and progressive strength. Progression: Days 1–7 Foundation → 8–14 Build → 15–21 Progress → 22–28 Challenge → 29–30 Consolidation.",
        type = "THIRTY_DAY",
        experienceLevel = ExperienceLevel.BEGINNER.name,
        durationDays = 30,
        trainingDaysPerWeek = 5,
        days = listOf(
            ProgramDay(
                dayNumber = 1,
                title = "Day 1 — Glutes + Hamstrings",
                workoutType = "TRAINING",
                focus = listOf("Glutes", "Hamstrings"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 4,
                        reps = "8–10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 2,
                title = "Day 2 — Back + Biceps",
                workoutType = "TRAINING",
                focus = listOf("Back", "Biceps"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 4,
                        reps = "8–10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_row",
                        sets = 3,
                        reps = "10/side",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "face_pulls",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bicep_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_hammer_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 3,
                title = "Day 3 — Quads + Calves",
                workoutType = "TRAINING",
                focus = listOf("Quads", "Calves"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_extension",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "standing_calf_raise",
                        sets = 4,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 4,
                title = "Day 4 — Chest + Shoulders + Triceps",
                workoutType = "TRAINING",
                focus = listOf("Chest", "Shoulders", "Triceps"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_bench_press",
                        sets = 4,
                        reps = "8–10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_dumbbell_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_lateral_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "triceps_pushdown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 5,
                title = "Day 5 — Glutes + Hamstrings",
                workoutType = "TRAINING",
                focus = listOf("Glutes", "Hamstrings"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 4,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "step_up",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 6,
                title = "Day 6 — Full Body",
                workoutType = "TRAINING",
                focus = listOf("Full Body"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 7,
                title = "Day 7 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 8,
                title = "Day 8 — Glutes + Hamstrings",
                workoutType = "TRAINING",
                focus = listOf("Glutes", "Hamstrings"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 4,
                        reps = "8–10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 9,
                title = "Day 9 — Back + Biceps",
                workoutType = "TRAINING",
                focus = listOf("Back", "Biceps"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 4,
                        reps = "8–10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_row",
                        sets = 3,
                        reps = "10/side",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "face_pulls",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bicep_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_hammer_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 10,
                title = "Day 10 — Quads + Calves",
                workoutType = "TRAINING",
                focus = listOf("Quads", "Calves"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_extension",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "standing_calf_raise",
                        sets = 4,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 11,
                title = "Day 11 — Chest + Shoulders + Triceps",
                workoutType = "TRAINING",
                focus = listOf("Chest", "Shoulders", "Triceps"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_bench_press",
                        sets = 4,
                        reps = "8–10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_dumbbell_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_lateral_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "triceps_pushdown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 12,
                title = "Day 12 — Glutes + Hamstrings",
                workoutType = "TRAINING",
                focus = listOf("Glutes", "Hamstrings"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 4,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "step_up",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 13,
                title = "Day 13 — Full Body",
                workoutType = "TRAINING",
                focus = listOf("Full Body"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 14,
                title = "Day 14 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 15,
                title = "Day 15 — Glutes + Hamstrings",
                workoutType = "TRAINING",
                focus = listOf("Glutes", "Hamstrings"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 4,
                        reps = "8–10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 16,
                title = "Day 16 — Back + Biceps",
                workoutType = "TRAINING",
                focus = listOf("Back", "Biceps"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 4,
                        reps = "8–10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_row",
                        sets = 3,
                        reps = "10/side",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "face_pulls",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bicep_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_hammer_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 17,
                title = "Day 17 — Quads + Calves",
                workoutType = "TRAINING",
                focus = listOf("Quads", "Calves"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_extension",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "standing_calf_raise",
                        sets = 4,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 18,
                title = "Day 18 — Chest + Shoulders + Triceps",
                workoutType = "TRAINING",
                focus = listOf("Chest", "Shoulders", "Triceps"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_bench_press",
                        sets = 4,
                        reps = "8–10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_dumbbell_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_lateral_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "triceps_pushdown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 19,
                title = "Day 19 — Glutes + Hamstrings",
                workoutType = "TRAINING",
                focus = listOf("Glutes", "Hamstrings"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 4,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "step_up",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 20,
                title = "Day 20 — Full Body",
                workoutType = "TRAINING",
                focus = listOf("Full Body"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 21,
                title = "Day 21 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 22,
                title = "Day 22 — Glutes + Hamstrings",
                workoutType = "TRAINING",
                focus = listOf("Glutes", "Hamstrings"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 4,
                        reps = "8–10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 23,
                title = "Day 23 — Back + Biceps",
                workoutType = "TRAINING",
                focus = listOf("Back", "Biceps"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 4,
                        reps = "8–10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_row",
                        sets = 3,
                        reps = "10/side",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "face_pulls",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bicep_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_hammer_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 24,
                title = "Day 24 — Quads + Calves",
                workoutType = "TRAINING",
                focus = listOf("Quads", "Calves"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_extension",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "standing_calf_raise",
                        sets = 4,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 25,
                title = "Day 25 — Chest + Shoulders + Triceps",
                workoutType = "TRAINING",
                focus = listOf("Chest", "Shoulders", "Triceps"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_bench_press",
                        sets = 4,
                        reps = "8–10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_dumbbell_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_lateral_raise",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "triceps_pushdown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 26,
                title = "Day 26 — Glutes + Hamstrings",
                workoutType = "TRAINING",
                focus = listOf("Glutes", "Hamstrings"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 4,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "step_up",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_abduction",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 27,
                title = "Day 27 — Full Body",
                workoutType = "TRAINING",
                focus = listOf("Full Body"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 28,
                title = "Day 28 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 29,
                title = "Day 29 — Glutes + Hamstrings",
                workoutType = "TRAINING",
                focus = listOf("Glutes", "Hamstrings"),
                isRestDay = false,
                notes = "Consolidate progress; avoid unnecessary max-effort work.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 4,
                        reps = "8–10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_glute_kickback",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 30,
                title = "Day 30 — Back + Biceps",
                workoutType = "TRAINING",
                focus = listOf("Back", "Biceps"),
                isRestDay = false,
                notes = "Consolidate progress; avoid unnecessary max-effort work.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 4,
                        reps = "8–10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_row",
                        sets = 3,
                        reps = "10/side",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "face_pulls",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bicep_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_hammer_curl",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                )
            ),
        )
    )

    val PROGRAM_FEMALE_POWERLIFTING: Program = Program(
        id = "female_powerlifting",
        name = "Powerlifting",
        slug = "female_powerlifting",
        description = "Squat, bench press and deadlift strength development. Progression: Days 1–7 Foundation → 8–14 Build → 15–21 Progress → 22–28 Challenge → 29–30 Consolidation.",
        type = "THIRTY_DAY",
        experienceLevel = ExperienceLevel.BEGINNER.name,
        durationDays = 30,
        trainingDaysPerWeek = 5,
        days = listOf(
            ProgramDay(
                dayNumber = 1,
                title = "Day 1 — Squat",
                workoutType = "TRAINING",
                focus = listOf("Squat"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 5,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "pause_squat",
                        sets = 3,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 2,
                title = "Day 2 — Bench Press",
                workoutType = "TRAINING",
                focus = listOf("Bench Press"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "barbell_bench_press",
                        sets = 5,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_dumbbell_press",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "triceps_pushdown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "chest_supported_row",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 3,
                title = "Day 3 — Deadlift",
                workoutType = "TRAINING",
                focus = listOf("Deadlift"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "deadlift",
                        sets = 5,
                        reps = "3",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "back_extension",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 4,
                title = "Day 4 — Technique + Recovery",
                workoutType = "TRAINING",
                focus = listOf("Technique", "Recovery"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "air_squat",
                        sets = 3,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "technique_bench",
                        sets = 3,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "core",
                        sets = 3,
                        reps = "As Prescribed",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "20–30 min easy walk",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 5,
                title = "Day 5 — Squat + Bench",
                workoutType = "TRAINING",
                focus = listOf("Squat", "Bench"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 4,
                        reps = "4",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "barbell_bench_press",
                        sets = 4,
                        reps = "4",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "8/leg",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 6,
                title = "Day 6 — Deadlift + Bench",
                workoutType = "TRAINING",
                focus = listOf("Deadlift", "Bench"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "deadlift",
                        sets = 4,
                        reps = "3",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "barbell_bench_press",
                        sets = 4,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 7,
                title = "Day 7 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 8,
                title = "Day 8 — Squat",
                workoutType = "TRAINING",
                focus = listOf("Squat"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 5,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "pause_squat",
                        sets = 3,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 9,
                title = "Day 9 — Bench Press",
                workoutType = "TRAINING",
                focus = listOf("Bench Press"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "barbell_bench_press",
                        sets = 5,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_dumbbell_press",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "triceps_pushdown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "chest_supported_row",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 10,
                title = "Day 10 — Deadlift",
                workoutType = "TRAINING",
                focus = listOf("Deadlift"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "deadlift",
                        sets = 5,
                        reps = "3",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "back_extension",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 11,
                title = "Day 11 — Technique + Recovery",
                workoutType = "TRAINING",
                focus = listOf("Technique", "Recovery"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "air_squat",
                        sets = 3,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "technique_bench",
                        sets = 3,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "core",
                        sets = 3,
                        reps = "As Prescribed",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "20–30 min easy walk",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 12,
                title = "Day 12 — Squat + Bench",
                workoutType = "TRAINING",
                focus = listOf("Squat", "Bench"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 4,
                        reps = "4",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "barbell_bench_press",
                        sets = 4,
                        reps = "4",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "8/leg",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 13,
                title = "Day 13 — Deadlift + Bench",
                workoutType = "TRAINING",
                focus = listOf("Deadlift", "Bench"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "deadlift",
                        sets = 4,
                        reps = "3",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "barbell_bench_press",
                        sets = 4,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 14,
                title = "Day 14 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 15,
                title = "Day 15 — Squat",
                workoutType = "TRAINING",
                focus = listOf("Squat"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 5,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "pause_squat",
                        sets = 3,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 16,
                title = "Day 16 — Bench Press",
                workoutType = "TRAINING",
                focus = listOf("Bench Press"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "barbell_bench_press",
                        sets = 5,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_dumbbell_press",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "triceps_pushdown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "chest_supported_row",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 17,
                title = "Day 17 — Deadlift",
                workoutType = "TRAINING",
                focus = listOf("Deadlift"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "deadlift",
                        sets = 5,
                        reps = "3",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "back_extension",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 18,
                title = "Day 18 — Technique + Recovery",
                workoutType = "TRAINING",
                focus = listOf("Technique", "Recovery"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "air_squat",
                        sets = 3,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "technique_bench",
                        sets = 3,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "core",
                        sets = 3,
                        reps = "As Prescribed",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "20–30 min easy walk",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 19,
                title = "Day 19 — Squat + Bench",
                workoutType = "TRAINING",
                focus = listOf("Squat", "Bench"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 4,
                        reps = "4",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "barbell_bench_press",
                        sets = 4,
                        reps = "4",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "8/leg",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 20,
                title = "Day 20 — Deadlift + Bench",
                workoutType = "TRAINING",
                focus = listOf("Deadlift", "Bench"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "deadlift",
                        sets = 4,
                        reps = "3",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "barbell_bench_press",
                        sets = 4,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 21,
                title = "Day 21 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 22,
                title = "Day 22 — Squat",
                workoutType = "TRAINING",
                focus = listOf("Squat"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 5,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "pause_squat",
                        sets = 3,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 23,
                title = "Day 23 — Bench Press",
                workoutType = "TRAINING",
                focus = listOf("Bench Press"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "barbell_bench_press",
                        sets = 5,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_dumbbell_press",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "triceps_pushdown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "chest_supported_row",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 24,
                title = "Day 24 — Deadlift",
                workoutType = "TRAINING",
                focus = listOf("Deadlift"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "deadlift",
                        sets = 5,
                        reps = "3",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "back_extension",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 25,
                title = "Day 25 — Technique + Recovery",
                workoutType = "TRAINING",
                focus = listOf("Technique", "Recovery"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "air_squat",
                        sets = 3,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "technique_bench",
                        sets = 3,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "core",
                        sets = 3,
                        reps = "As Prescribed",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "20–30 min easy walk",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 26,
                title = "Day 26 — Squat + Bench",
                workoutType = "TRAINING",
                focus = listOf("Squat", "Bench"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 4,
                        reps = "4",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "barbell_bench_press",
                        sets = 4,
                        reps = "4",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "bulgarian_split_squat",
                        sets = 3,
                        reps = "8/leg",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 27,
                title = "Day 27 — Deadlift + Bench",
                workoutType = "TRAINING",
                focus = listOf("Deadlift", "Bench"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "deadlift",
                        sets = 4,
                        reps = "3",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "barbell_bench_press",
                        sets = 4,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 28,
                title = "Day 28 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 29,
                title = "Day 29 — Squat",
                workoutType = "TRAINING",
                focus = listOf("Squat"),
                isRestDay = false,
                notes = "Consolidate progress; avoid unnecessary max-effort work.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 5,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "pause_squat",
                        sets = 3,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "lying_leg_curl",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 30,
                title = "Day 30 — Bench Press",
                workoutType = "TRAINING",
                focus = listOf("Bench Press"),
                isRestDay = false,
                notes = "Consolidate progress; avoid unnecessary max-effort work.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "barbell_bench_press",
                        sets = 5,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_dumbbell_press",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "triceps_pushdown",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "chest_supported_row",
                        sets = 4,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                )
            ),
        )
    )

    val PROGRAM_FEMALE_CROSSFIT: Program = Program(
        id = "female_crossfit",
        name = "CrossFit",
        slug = "female_crossfit",
        description = "Mixed strength, conditioning and functional fitness. Progression: Days 1–7 Foundation → 8–14 Build → 15–21 Progress → 22–28 Challenge → 29–30 Consolidation.",
        type = "THIRTY_DAY",
        experienceLevel = ExperienceLevel.BEGINNER.name,
        durationDays = 30,
        trainingDaysPerWeek = 5,
        days = listOf(
            ProgramDay(
                dayNumber = 1,
                title = "Day 1 — Strength + WOD",
                workoutType = "TRAINING",
                focus = listOf("Strength", "WOD"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 5,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "12-min AMRAP: 10 KB Swings + 10 Box Step-Ups + 10 Push-ups + 200 m Run",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 2,
                title = "Day 2 — Conditioning",
                workoutType = "TRAINING",
                focus = listOf("Conditioning"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "5 rounds: 400 m Run + 15 Air Squats + 12 DB Thrusters + 10 Burpees",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 3,
                title = "Day 3 — Olympic Technique",
                workoutType = "TRAINING",
                focus = listOf("Olympic Technique"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hang_power_clean_technique",
                        sets = 3,
                        reps = "Hang Power Clean technique",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "push_press_technique",
                        sets = 3,
                        reps = "Push Press technique",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "front_squat",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "row_intervals",
                        sets = 3,
                        reps = "Row intervals",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 4,
                title = "Day 4 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 5,
                title = "Day 5 — Strength + WOD",
                workoutType = "TRAINING",
                focus = listOf("Strength", "WOD"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "deadlift",
                        sets = 5,
                        reps = "3",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "10-min AMRAP: 8 DB Snatches + 10 Lunges + 8 Burpees",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 6,
                title = "Day 6 — Full WOD",
                workoutType = "TRAINING",
                focus = listOf("Full WOD"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "For time: 400 m Run + 30 KB Swings + 20 Push-ups + 30 Walking Lunges + 400 m Run",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 7,
                title = "Day 7 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 8,
                title = "Day 8 — Strength + WOD",
                workoutType = "TRAINING",
                focus = listOf("Strength", "WOD"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 5,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "12-min AMRAP: 10 KB Swings + 10 Box Step-Ups + 10 Push-ups + 200 m Run",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 9,
                title = "Day 9 — Conditioning",
                workoutType = "TRAINING",
                focus = listOf("Conditioning"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "5 rounds: 400 m Run + 15 Air Squats + 12 DB Thrusters + 10 Burpees",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 10,
                title = "Day 10 — Olympic Technique",
                workoutType = "TRAINING",
                focus = listOf("Olympic Technique"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hang_power_clean_technique",
                        sets = 3,
                        reps = "Hang Power Clean technique",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "push_press_technique",
                        sets = 3,
                        reps = "Push Press technique",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "front_squat",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "row_intervals",
                        sets = 3,
                        reps = "Row intervals",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 11,
                title = "Day 11 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 12,
                title = "Day 12 — Strength + WOD",
                workoutType = "TRAINING",
                focus = listOf("Strength", "WOD"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "deadlift",
                        sets = 5,
                        reps = "3",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "10-min AMRAP: 8 DB Snatches + 10 Lunges + 8 Burpees",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 13,
                title = "Day 13 — Full WOD",
                workoutType = "TRAINING",
                focus = listOf("Full WOD"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "For time: 400 m Run + 30 KB Swings + 20 Push-ups + 30 Walking Lunges + 400 m Run",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 14,
                title = "Day 14 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 15,
                title = "Day 15 — Strength + WOD",
                workoutType = "TRAINING",
                focus = listOf("Strength", "WOD"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 5,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "12-min AMRAP: 10 KB Swings + 10 Box Step-Ups + 10 Push-ups + 200 m Run",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 16,
                title = "Day 16 — Conditioning",
                workoutType = "TRAINING",
                focus = listOf("Conditioning"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "5 rounds: 400 m Run + 15 Air Squats + 12 DB Thrusters + 10 Burpees",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 17,
                title = "Day 17 — Olympic Technique",
                workoutType = "TRAINING",
                focus = listOf("Olympic Technique"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hang_power_clean_technique",
                        sets = 3,
                        reps = "Hang Power Clean technique",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "push_press_technique",
                        sets = 3,
                        reps = "Push Press technique",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "front_squat",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "row_intervals",
                        sets = 3,
                        reps = "Row intervals",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 18,
                title = "Day 18 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 19,
                title = "Day 19 — Strength + WOD",
                workoutType = "TRAINING",
                focus = listOf("Strength", "WOD"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "deadlift",
                        sets = 5,
                        reps = "3",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "10-min AMRAP: 8 DB Snatches + 10 Lunges + 8 Burpees",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 20,
                title = "Day 20 — Full WOD",
                workoutType = "TRAINING",
                focus = listOf("Full WOD"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "For time: 400 m Run + 30 KB Swings + 20 Push-ups + 30 Walking Lunges + 400 m Run",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 21,
                title = "Day 21 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 22,
                title = "Day 22 — Strength + WOD",
                workoutType = "TRAINING",
                focus = listOf("Strength", "WOD"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 5,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "12-min AMRAP: 10 KB Swings + 10 Box Step-Ups + 10 Push-ups + 200 m Run",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 23,
                title = "Day 23 — Conditioning",
                workoutType = "TRAINING",
                focus = listOf("Conditioning"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "5 rounds: 400 m Run + 15 Air Squats + 12 DB Thrusters + 10 Burpees",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 24,
                title = "Day 24 — Olympic Technique",
                workoutType = "TRAINING",
                focus = listOf("Olympic Technique"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hang_power_clean_technique",
                        sets = 3,
                        reps = "Hang Power Clean technique",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "push_press_technique",
                        sets = 3,
                        reps = "Push Press technique",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "front_squat",
                        sets = 3,
                        reps = "8",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "row_intervals",
                        sets = 3,
                        reps = "Row intervals",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 25,
                title = "Day 25 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 26,
                title = "Day 26 — Strength + WOD",
                workoutType = "TRAINING",
                focus = listOf("Strength", "WOD"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "deadlift",
                        sets = 5,
                        reps = "3",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "10-min AMRAP: 8 DB Snatches + 10 Lunges + 8 Burpees",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 27,
                title = "Day 27 — Full WOD",
                workoutType = "TRAINING",
                focus = listOf("Full WOD"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "For time: 400 m Run + 30 KB Swings + 20 Push-ups + 30 Walking Lunges + 400 m Run",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 28,
                title = "Day 28 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 29,
                title = "Day 29 — Strength + WOD",
                workoutType = "TRAINING",
                focus = listOf("Strength", "WOD"),
                isRestDay = false,
                notes = "Consolidate progress; avoid unnecessary max-effort work.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 5,
                        reps = "5",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "12-min AMRAP: 10 KB Swings + 10 Box Step-Ups + 10 Push-ups + 200 m Run",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 30,
                title = "Day 30 — Conditioning",
                workoutType = "TRAINING",
                focus = listOf("Conditioning"),
                isRestDay = false,
                notes = "Consolidate progress; avoid unnecessary max-effort work.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "5 rounds: 400 m Run + 15 Air Squats + 12 DB Thrusters + 10 Burpees",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                )
            ),
        )
    )

    val PROGRAM_FEMALE_FULL_BODY_IN_45_MINUTES: Program = Program(
        id = "female_full_body_in_45_minutes",
        name = "Full Body in 45 Minutes",
        slug = "female_full_body_in_45_minutes",
        description = "Efficient full-body sessions capped at approximately 45 minutes. Progression: Days 1–7 Foundation → 8–14 Build → 15–21 Progress → 22–28 Challenge → 29–30 Consolidation.",
        type = "THIRTY_DAY",
        experienceLevel = ExperienceLevel.BEGINNER.name,
        durationDays = 30,
        trainingDaysPerWeek = 5,
        days = listOf(
            ProgramDay(
                dayNumber = 1,
                title = "Day 1 — Full Body A",
                workoutType = "TRAINING",
                focus = listOf("Full Body A"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "plank_hold",
                        sets = 3,
                        reps = "40 sec",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 2,
                title = "Day 2 — Full Body B",
                workoutType = "TRAINING",
                focus = listOf("Full Body B"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "bicycle_crunch",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 3,
                title = "Day 3 — Full Body C",
                workoutType = "TRAINING",
                focus = listOf("Full Body C"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_dumbbell_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "step_up",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "mountain_climber",
                        sets = 3,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 4,
                title = "Day 4 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 5,
                title = "Day 5 — Full Body D",
                workoutType = "TRAINING",
                focus = listOf("Full Body D"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_crunch",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 6,
                title = "Day 6 — Full Body Circuit",
                workoutType = "TRAINING",
                focus = listOf("Full Body Circuit"),
                isRestDay = false,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "4 rounds: Squat ×12 + DB Row ×10 + Push-up ×10 + Reverse Lunge ×10/leg + Shoulder Press ×10",
                        restSeconds = 60,
                        notes = "Choose a manageable load; leave ~2–3 reps in reserve."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 7,
                title = "Day 7 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Choose a manageable load; leave ~2–3 reps in reserve.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 8,
                title = "Day 8 — Full Body A",
                workoutType = "TRAINING",
                focus = listOf("Full Body A"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "plank_hold",
                        sets = 3,
                        reps = "40 sec",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 9,
                title = "Day 9 — Full Body B",
                workoutType = "TRAINING",
                focus = listOf("Full Body B"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "bicycle_crunch",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 10,
                title = "Day 10 — Full Body C",
                workoutType = "TRAINING",
                focus = listOf("Full Body C"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_dumbbell_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "step_up",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "mountain_climber",
                        sets = 3,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 11,
                title = "Day 11 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 12,
                title = "Day 12 — Full Body D",
                workoutType = "TRAINING",
                focus = listOf("Full Body D"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_crunch",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 13,
                title = "Day 13 — Full Body Circuit",
                workoutType = "TRAINING",
                focus = listOf("Full Body Circuit"),
                isRestDay = false,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "4 rounds: Squat ×12 + DB Row ×10 + Push-up ×10 + Reverse Lunge ×10/leg + Shoulder Press ×10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps or a small load increase if form is solid."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 14,
                title = "Day 14 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Add 1–2 reps or a small load increase if form is solid.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 15,
                title = "Day 15 — Full Body A",
                workoutType = "TRAINING",
                focus = listOf("Full Body A"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "plank_hold",
                        sets = 3,
                        reps = "40 sec",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 16,
                title = "Day 16 — Full Body B",
                workoutType = "TRAINING",
                focus = listOf("Full Body B"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "bicycle_crunch",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 17,
                title = "Day 17 — Full Body C",
                workoutType = "TRAINING",
                focus = listOf("Full Body C"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_dumbbell_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "step_up",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "mountain_climber",
                        sets = 3,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 18,
                title = "Day 18 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 19,
                title = "Day 19 — Full Body D",
                workoutType = "TRAINING",
                focus = listOf("Full Body D"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_crunch",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 20,
                title = "Day 20 — Full Body Circuit",
                workoutType = "TRAINING",
                focus = listOf("Full Body Circuit"),
                isRestDay = false,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "4 rounds: Squat ×12 + DB Row ×10 + Push-up ×10 + Reverse Lunge ×10/leg + Shoulder Press ×10",
                        restSeconds = 60,
                        notes = "Progress the main lifts slightly; keep technique strict."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 21,
                title = "Day 21 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Progress the main lifts slightly; keep technique strict.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 22,
                title = "Day 22 — Full Body A",
                workoutType = "TRAINING",
                focus = listOf("Full Body A"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "plank_hold",
                        sets = 3,
                        reps = "40 sec",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 23,
                title = "Day 23 — Full Body B",
                workoutType = "TRAINING",
                focus = listOf("Full Body B"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "bicycle_crunch",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 24,
                title = "Day 24 — Full Body C",
                workoutType = "TRAINING",
                focus = listOf("Full Body C"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "hip_thrust",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_dumbbell_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "step_up",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "mountain_climber",
                        sets = 3,
                        reps = "30 sec",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 25,
                title = "Day 25 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 26,
                title = "Day 26 — Full Body D",
                workoutType = "TRAINING",
                focus = listOf("Full Body D"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                    ExerciseReference(
                        exerciseId = "cable_crunch",
                        sets = 3,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 27,
                title = "Day 27 — Full Body Circuit",
                workoutType = "TRAINING",
                focus = listOf("Full Body Circuit"),
                isRestDay = false,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "burpees",
                        sets = 1,
                        reps = "4 rounds: Squat ×12 + DB Row ×10 + Push-up ×10 + Reverse Lunge ×10/leg + Shoulder Press ×10",
                        restSeconds = 60,
                        notes = "Use the upper end of the rep range or a small load increase."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 28,
                title = "Day 28 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Use the upper end of the rep range or a small load increase.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 29,
                title = "Day 29 — Full Body A",
                workoutType = "TRAINING",
                focus = listOf("Full Body A"),
                isRestDay = false,
                notes = "Consolidate progress; avoid unnecessary max-effort work.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bench_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "romanian_deadlift",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "plank_hold",
                        sets = 3,
                        reps = "40 sec",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 30,
                title = "Day 30 — Full Body B",
                workoutType = "TRAINING",
                focus = listOf("Full Body B"),
                isRestDay = false,
                notes = "Consolidate progress; avoid unnecessary max-effort work.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "leg_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_shoulder_press",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "walking_lunges",
                        sets = 3,
                        reps = "10/leg",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                    ExerciseReference(
                        exerciseId = "bicycle_crunch",
                        sets = 3,
                        reps = "20",
                        restSeconds = 60,
                        notes = "Consolidate progress; avoid unnecessary max-effort work."
                    ),
                )
            ),
        )
    )

    val PROGRAM_FEMALE_POSTPARTUM_RECOVERY: Program = Program(
        id = "female_postpartum_recovery",
        name = "Postpartum Recovery",
        slug = "female_postpartum_recovery",
        description = "Gradual recovery-focused movement after appropriate medical clearance. Progression: Days 1–7 Foundation → 8–14 Build → 15–21 Progress → 22–28 Challenge → 29–30 Consolidation.",
        type = "THIRTY_DAY",
        experienceLevel = ExperienceLevel.BEGINNER.name,
        durationDays = 30,
        trainingDaysPerWeek = 5,
        days = listOf(
            ProgramDay(
                dayNumber = 1,
                title = "Day 1 — Breathing + Core",
                workoutType = "TRAINING",
                focus = listOf("Breathing", "Core"),
                isRestDay = false,
                notes = "Easy, symptom-free effort; focus on breathing and technique.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "diaphragmatic_breathing",
                        sets = 1,
                        reps = "Diaphragmatic Breathing 5 min",
                        restSeconds = 30,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                    ExerciseReference(
                        exerciseId = "pelvic_floor_activation",
                        sets = 1,
                        reps = "Pelvic Floor Relaxation/Activation",
                        restSeconds = 30,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                    ExerciseReference(
                        exerciseId = "dead_bug_progression",
                        sets = 2,
                        reps = "8/side",
                        restSeconds = 60,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                    ExerciseReference(
                        exerciseId = "glute_bridge",
                        sets = 2,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "Easy walk 15–20 min",
                        restSeconds = 60,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 2,
                title = "Day 2 — Lower Body",
                workoutType = "TRAINING",
                focus = listOf("Lower Body"),
                isRestDay = false,
                notes = "Easy, symptom-free effort; focus on breathing and technique.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "air_squat",
                        sets = 2,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                    ExerciseReference(
                        exerciseId = "glute_bridge",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                    ExerciseReference(
                        exerciseId = "supported_reverse_lunge",
                        sets = 2,
                        reps = "8/leg",
                        restSeconds = 60,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                    ExerciseReference(
                        exerciseId = "standing_calf_raise",
                        sets = 2,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "Walk 15–25 min",
                        restSeconds = 60,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 3,
                title = "Day 3 — Upper Body",
                workoutType = "TRAINING",
                focus = listOf("Upper Body"),
                isRestDay = false,
                notes = "Easy, symptom-free effort; focus on breathing and technique.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 2,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                    ExerciseReference(
                        exerciseId = "light_db_chest_press",
                        sets = 2,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_lateral_raise",
                        sets = 2,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bicep_curl",
                        sets = 2,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 4,
                title = "Day 4 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Easy, symptom-free effort; focus on breathing and technique.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 5,
                title = "Day 5 — Full Body",
                workoutType = "TRAINING",
                focus = listOf("Full Body"),
                isRestDay = false,
                notes = "Easy, symptom-free effort; focus on breathing and technique.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 2,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                    ExerciseReference(
                        exerciseId = "glute_bridge",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                    ExerciseReference(
                        exerciseId = "light_db_press",
                        sets = 2,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                    ExerciseReference(
                        exerciseId = "supported_step_up",
                        sets = 2,
                        reps = "8/leg",
                        restSeconds = 60,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 6,
                title = "Day 6 — Mobility + Walking",
                workoutType = "TRAINING",
                focus = listOf("Mobility", "Walking"),
                isRestDay = false,
                notes = "Easy, symptom-free effort; focus on breathing and technique.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "Low-intensity walking 20–30 min",
                        restSeconds = 60,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                    ExerciseReference(
                        exerciseId = "air_squat",
                        sets = 3,
                        reps = "Gentle mobility",
                        restSeconds = 60,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                    ExerciseReference(
                        exerciseId = "diaphragmatic_breathing",
                        sets = 1,
                        reps = "Breathing",
                        restSeconds = 30,
                        notes = "Easy, symptom-free effort; focus on breathing and technique."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 7,
                title = "Day 7 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Easy, symptom-free effort; focus on breathing and technique.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 8,
                title = "Day 8 — Breathing + Core",
                workoutType = "TRAINING",
                focus = listOf("Breathing", "Core"),
                isRestDay = false,
                notes = "Add 1–2 reps where comfortable; keep effort easy–moderate.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "diaphragmatic_breathing",
                        sets = 1,
                        reps = "Diaphragmatic Breathing 5 min",
                        restSeconds = 30,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                    ExerciseReference(
                        exerciseId = "pelvic_floor_activation",
                        sets = 1,
                        reps = "Pelvic Floor Relaxation/Activation",
                        restSeconds = 30,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                    ExerciseReference(
                        exerciseId = "dead_bug_progression",
                        sets = 2,
                        reps = "8/side",
                        restSeconds = 60,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                    ExerciseReference(
                        exerciseId = "glute_bridge",
                        sets = 2,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "Easy walk 15–20 min",
                        restSeconds = 60,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 9,
                title = "Day 9 — Lower Body",
                workoutType = "TRAINING",
                focus = listOf("Lower Body"),
                isRestDay = false,
                notes = "Add 1–2 reps where comfortable; keep effort easy–moderate.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "air_squat",
                        sets = 2,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                    ExerciseReference(
                        exerciseId = "glute_bridge",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                    ExerciseReference(
                        exerciseId = "supported_reverse_lunge",
                        sets = 2,
                        reps = "8/leg",
                        restSeconds = 60,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                    ExerciseReference(
                        exerciseId = "standing_calf_raise",
                        sets = 2,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "Walk 15–25 min",
                        restSeconds = 60,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 10,
                title = "Day 10 — Upper Body",
                workoutType = "TRAINING",
                focus = listOf("Upper Body"),
                isRestDay = false,
                notes = "Add 1–2 reps where comfortable; keep effort easy–moderate.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 2,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                    ExerciseReference(
                        exerciseId = "light_db_chest_press",
                        sets = 2,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_lateral_raise",
                        sets = 2,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bicep_curl",
                        sets = 2,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 11,
                title = "Day 11 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Add 1–2 reps where comfortable; keep effort easy–moderate.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 12,
                title = "Day 12 — Full Body",
                workoutType = "TRAINING",
                focus = listOf("Full Body"),
                isRestDay = false,
                notes = "Add 1–2 reps where comfortable; keep effort easy–moderate.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 2,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                    ExerciseReference(
                        exerciseId = "glute_bridge",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                    ExerciseReference(
                        exerciseId = "light_db_press",
                        sets = 2,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                    ExerciseReference(
                        exerciseId = "supported_step_up",
                        sets = 2,
                        reps = "8/leg",
                        restSeconds = 60,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 13,
                title = "Day 13 — Mobility + Walking",
                workoutType = "TRAINING",
                focus = listOf("Mobility", "Walking"),
                isRestDay = false,
                notes = "Add 1–2 reps where comfortable; keep effort easy–moderate.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "Low-intensity walking 20–30 min",
                        restSeconds = 60,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                    ExerciseReference(
                        exerciseId = "air_squat",
                        sets = 3,
                        reps = "Gentle mobility",
                        restSeconds = 60,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                    ExerciseReference(
                        exerciseId = "diaphragmatic_breathing",
                        sets = 1,
                        reps = "Breathing",
                        restSeconds = 30,
                        notes = "Add 1–2 reps where comfortable; keep effort easy–moderate."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 14,
                title = "Day 14 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Add 1–2 reps where comfortable; keep effort easy–moderate.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 15,
                title = "Day 15 — Breathing + Core",
                workoutType = "TRAINING",
                focus = listOf("Breathing", "Core"),
                isRestDay = false,
                notes = "Small progression only if symptom-free; prioritize control.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "diaphragmatic_breathing",
                        sets = 1,
                        reps = "Diaphragmatic Breathing 5 min",
                        restSeconds = 30,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                    ExerciseReference(
                        exerciseId = "pelvic_floor_activation",
                        sets = 1,
                        reps = "Pelvic Floor Relaxation/Activation",
                        restSeconds = 30,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                    ExerciseReference(
                        exerciseId = "dead_bug_progression",
                        sets = 2,
                        reps = "8/side",
                        restSeconds = 60,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                    ExerciseReference(
                        exerciseId = "glute_bridge",
                        sets = 2,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "Easy walk 15–20 min",
                        restSeconds = 60,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 16,
                title = "Day 16 — Lower Body",
                workoutType = "TRAINING",
                focus = listOf("Lower Body"),
                isRestDay = false,
                notes = "Small progression only if symptom-free; prioritize control.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "air_squat",
                        sets = 2,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                    ExerciseReference(
                        exerciseId = "glute_bridge",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                    ExerciseReference(
                        exerciseId = "supported_reverse_lunge",
                        sets = 2,
                        reps = "8/leg",
                        restSeconds = 60,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                    ExerciseReference(
                        exerciseId = "standing_calf_raise",
                        sets = 2,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "Walk 15–25 min",
                        restSeconds = 60,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 17,
                title = "Day 17 — Upper Body",
                workoutType = "TRAINING",
                focus = listOf("Upper Body"),
                isRestDay = false,
                notes = "Small progression only if symptom-free; prioritize control.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 2,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                    ExerciseReference(
                        exerciseId = "light_db_chest_press",
                        sets = 2,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_lateral_raise",
                        sets = 2,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bicep_curl",
                        sets = 2,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 18,
                title = "Day 18 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Small progression only if symptom-free; prioritize control.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 19,
                title = "Day 19 — Full Body",
                workoutType = "TRAINING",
                focus = listOf("Full Body"),
                isRestDay = false,
                notes = "Small progression only if symptom-free; prioritize control.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 2,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                    ExerciseReference(
                        exerciseId = "glute_bridge",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                    ExerciseReference(
                        exerciseId = "light_db_press",
                        sets = 2,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                    ExerciseReference(
                        exerciseId = "supported_step_up",
                        sets = 2,
                        reps = "8/leg",
                        restSeconds = 60,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 20,
                title = "Day 20 — Mobility + Walking",
                workoutType = "TRAINING",
                focus = listOf("Mobility", "Walking"),
                isRestDay = false,
                notes = "Small progression only if symptom-free; prioritize control.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "Low-intensity walking 20–30 min",
                        restSeconds = 60,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                    ExerciseReference(
                        exerciseId = "air_squat",
                        sets = 3,
                        reps = "Gentle mobility",
                        restSeconds = 60,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                    ExerciseReference(
                        exerciseId = "diaphragmatic_breathing",
                        sets = 1,
                        reps = "Breathing",
                        restSeconds = 30,
                        notes = "Small progression only if symptom-free; prioritize control."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 21,
                title = "Day 21 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Small progression only if symptom-free; prioritize control.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 22,
                title = "Day 22 — Breathing + Core",
                workoutType = "TRAINING",
                focus = listOf("Breathing", "Core"),
                isRestDay = false,
                notes = "Repeat or slightly progress only if previously tolerated.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "diaphragmatic_breathing",
                        sets = 1,
                        reps = "Diaphragmatic Breathing 5 min",
                        restSeconds = 30,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                    ExerciseReference(
                        exerciseId = "pelvic_floor_activation",
                        sets = 1,
                        reps = "Pelvic Floor Relaxation/Activation",
                        restSeconds = 30,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                    ExerciseReference(
                        exerciseId = "dead_bug_progression",
                        sets = 2,
                        reps = "8/side",
                        restSeconds = 60,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                    ExerciseReference(
                        exerciseId = "glute_bridge",
                        sets = 2,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "Easy walk 15–20 min",
                        restSeconds = 60,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 23,
                title = "Day 23 — Lower Body",
                workoutType = "TRAINING",
                focus = listOf("Lower Body"),
                isRestDay = false,
                notes = "Repeat or slightly progress only if previously tolerated.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "air_squat",
                        sets = 2,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                    ExerciseReference(
                        exerciseId = "glute_bridge",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                    ExerciseReference(
                        exerciseId = "supported_reverse_lunge",
                        sets = 2,
                        reps = "8/leg",
                        restSeconds = 60,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                    ExerciseReference(
                        exerciseId = "standing_calf_raise",
                        sets = 2,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "Walk 15–25 min",
                        restSeconds = 60,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 24,
                title = "Day 24 — Upper Body",
                workoutType = "TRAINING",
                focus = listOf("Upper Body"),
                isRestDay = false,
                notes = "Repeat or slightly progress only if previously tolerated.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                    ExerciseReference(
                        exerciseId = "lat_pulldown",
                        sets = 2,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                    ExerciseReference(
                        exerciseId = "light_db_chest_press",
                        sets = 2,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_lateral_raise",
                        sets = 2,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                    ExerciseReference(
                        exerciseId = "dumbbell_bicep_curl",
                        sets = 2,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 25,
                title = "Day 25 — Recovery",
                workoutType = "REST",
                focus = listOf("Recovery"),
                isRestDay = true,
                notes = "Repeat or slightly progress only if previously tolerated.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 26,
                title = "Day 26 — Full Body",
                workoutType = "TRAINING",
                focus = listOf("Full Body"),
                isRestDay = false,
                notes = "Repeat or slightly progress only if previously tolerated.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "dumbbell_goblet_squat",
                        sets = 2,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                    ExerciseReference(
                        exerciseId = "seated_cable_row",
                        sets = 3,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                    ExerciseReference(
                        exerciseId = "glute_bridge",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                    ExerciseReference(
                        exerciseId = "light_db_press",
                        sets = 2,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                    ExerciseReference(
                        exerciseId = "supported_step_up",
                        sets = 2,
                        reps = "8/leg",
                        restSeconds = 60,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 27,
                title = "Day 27 — Mobility + Walking",
                workoutType = "TRAINING",
                focus = listOf("Mobility", "Walking"),
                isRestDay = false,
                notes = "Repeat or slightly progress only if previously tolerated.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "Low-intensity walking 20–30 min",
                        restSeconds = 60,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                    ExerciseReference(
                        exerciseId = "air_squat",
                        sets = 3,
                        reps = "Gentle mobility",
                        restSeconds = 60,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                    ExerciseReference(
                        exerciseId = "diaphragmatic_breathing",
                        sets = 1,
                        reps = "Breathing",
                        restSeconds = 30,
                        notes = "Repeat or slightly progress only if previously tolerated."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 28,
                title = "Day 28 — Rest",
                workoutType = "REST",
                focus = listOf("Rest"),
                isRestDay = true,
                notes = "Repeat or slightly progress only if previously tolerated.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = emptyList()
            ),
            ProgramDay(
                dayNumber = 29,
                title = "Day 29 — Breathing + Core",
                workoutType = "TRAINING",
                focus = listOf("Breathing", "Core"),
                isRestDay = false,
                notes = "Consolidate technique; do not chase fatigue.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "diaphragmatic_breathing",
                        sets = 1,
                        reps = "Diaphragmatic Breathing 5 min",
                        restSeconds = 30,
                        notes = "Consolidate technique; do not chase fatigue."
                    ),
                    ExerciseReference(
                        exerciseId = "pelvic_floor_activation",
                        sets = 1,
                        reps = "Pelvic Floor Relaxation/Activation",
                        restSeconds = 30,
                        notes = "Consolidate technique; do not chase fatigue."
                    ),
                    ExerciseReference(
                        exerciseId = "dead_bug_progression",
                        sets = 2,
                        reps = "8/side",
                        restSeconds = 60,
                        notes = "Consolidate technique; do not chase fatigue."
                    ),
                    ExerciseReference(
                        exerciseId = "glute_bridge",
                        sets = 2,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Consolidate technique; do not chase fatigue."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "Easy walk 15–20 min",
                        restSeconds = 60,
                        notes = "Consolidate technique; do not chase fatigue."
                    ),
                )
            ),
            ProgramDay(
                dayNumber = 30,
                title = "Day 30 — Lower Body",
                workoutType = "TRAINING",
                focus = listOf("Lower Body"),
                isRestDay = false,
                notes = "Consolidate technique; do not chase fatigue. Postpartum implementation: Require appropriate clinician clearance and adapt to postpartum stage, delivery type, symptoms and recovery. Stop and seek professional guidance for concerning pain, bleeding, pelvic heaviness/bulging, wound issues, dizziness or other unexpected symptoms.",
                warmUp = listOf("5–10 min dynamic warm-up & joint mobility"),
                coolDown = listOf("5 min cool-down stretching & mobility"),
                exercises = listOf(
                    ExerciseReference(
                        exerciseId = "air_squat",
                        sets = 2,
                        reps = "10",
                        restSeconds = 60,
                        notes = "Consolidate technique; do not chase fatigue. Postpartum implementation: Require appropriate clinician clearance and adapt to postpartum stage, delivery type, symptoms and recovery. Stop and seek professional guidance for concerning pain, bleeding, pelvic heaviness/bulging, wound issues, dizziness or other unexpected symptoms."
                    ),
                    ExerciseReference(
                        exerciseId = "glute_bridge",
                        sets = 3,
                        reps = "12",
                        restSeconds = 60,
                        notes = "Consolidate technique; do not chase fatigue. Postpartum implementation: Require appropriate clinician clearance and adapt to postpartum stage, delivery type, symptoms and recovery. Stop and seek professional guidance for concerning pain, bleeding, pelvic heaviness/bulging, wound issues, dizziness or other unexpected symptoms."
                    ),
                    ExerciseReference(
                        exerciseId = "supported_reverse_lunge",
                        sets = 2,
                        reps = "8/leg",
                        restSeconds = 60,
                        notes = "Consolidate technique; do not chase fatigue. Postpartum implementation: Require appropriate clinician clearance and adapt to postpartum stage, delivery type, symptoms and recovery. Stop and seek professional guidance for concerning pain, bleeding, pelvic heaviness/bulging, wound issues, dizziness or other unexpected symptoms."
                    ),
                    ExerciseReference(
                        exerciseId = "standing_calf_raise",
                        sets = 2,
                        reps = "15",
                        restSeconds = 60,
                        notes = "Consolidate technique; do not chase fatigue. Postpartum implementation: Require appropriate clinician clearance and adapt to postpartum stage, delivery type, symptoms and recovery. Stop and seek professional guidance for concerning pain, bleeding, pelvic heaviness/bulging, wound issues, dizziness or other unexpected symptoms."
                    ),
                    ExerciseReference(
                        exerciseId = "incline_walk",
                        sets = 1,
                        reps = "Walk 15–25 min",
                        restSeconds = 60,
                        notes = "Consolidate technique; do not chase fatigue. Postpartum implementation: Require appropriate clinician clearance and adapt to postpartum stage, delivery type, symptoms and recovery. Stop and seek professional guidance for concerning pain, bleeding, pelvic heaviness/bulging, wound issues, dizziness or other unexpected symptoms."
                    ),
                )
            ),
        )
    )

    val ALL_PROGRAMS: List<Program> = listOf(
        PROGRAM_FEMALE_WEIGHT_LOSS,
        PROGRAM_FEMALE_SCULPTED_BODY,
        PROGRAM_FEMALE_PERFECT_BUTT,
        PROGRAM_FEMALE_6_PACK_ABS,
        PROGRAM_FEMALE_GENERAL_MUSCLE_BUILDING,
        PROGRAM_FEMALE_POWERLIFTING,
        PROGRAM_FEMALE_CROSSFIT,
        PROGRAM_FEMALE_FULL_BODY_IN_45_MINUTES,
        PROGRAM_FEMALE_POSTPARTUM_RECOVERY,
    )

    fun getAll(): List<Program> = ALL_PROGRAMS

    fun getById(programId: String): Program? {
        val norm = programId.lowercase().trim()
        return ALL_PROGRAMS.find { it.id.equals(norm, ignoreCase = true) }
    }
}
