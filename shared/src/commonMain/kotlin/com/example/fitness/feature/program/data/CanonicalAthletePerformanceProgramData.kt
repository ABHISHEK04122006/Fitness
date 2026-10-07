package com.example.fitness.feature.program.data

import com.example.fitness.feature.exercise.model.ExerciseReference
import com.example.fitness.feature.program.model.Program
import com.example.fitness.feature.program.model.ProgramDay
import com.example.fitness.feature.program.model.ProgramType
import com.example.fitness.feature.program.model.ProgramWorkoutType
import com.example.fitness.feature.workout.model.ExperienceLevel

/**
 * Authoritative 30-Day Athlete Performance Program.
 * Sourced directly from Athgym_30_Day_Athlete_Performance_Program.pdf.
 * Preserves Speed, Strength, Power, Agility, Conditioning, Endurance, Mobility, and Day 30 Performance Test.
 */
object CanonicalAthletePerformanceProgramData {

    val PROGRAM_ATHLETE_PERFORMANCE = Program(
        id = "athlete_performance_30_day",
        name = "30-Day Athlete Performance Program",
        slug = "athlete-performance-30-day",
        description = "Authoritative 30-day multi-modal athletic training program developing speed, agility, power, strength, and conditioning.",
        type = ProgramType.ATHLETE_PERFORMANCE.name,
        experienceLevel = ExperienceLevel.ADVANCED.name,
        durationDays = 30,
        trainingDaysPerWeek = 6,
        days = listOf(
            ProgramDay(
                dayNumber = 1,
                title = "Day 1 — Acceleration & Sprint",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Acceleration & Sprint"),
                isRestDay = false,
                warmUp = listOf(
                    "Easy jog — 3 min",
                    "Ankle bounces — 2×20",
                    "Leg swings — 10/leg",
                    "Walking lunges — 10/leg",
                    "Ankling — 2×15m",
                    "A-skips — 2×15m",
                    "High knees — 2×15m",
                    "Straight-leg runs — 2×15m",
                    "Progressive acceleration — 2×15m @ 60–70%"
                ),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "ten_meter_acceleration", sets = 1, reps = "6 reps", restSeconds = 90),
                    ExerciseReference(exerciseId = "twenty_meter_sprint", sets = 1, reps = "5 reps", restSeconds = 90),
                    ExerciseReference(exerciseId = "thirty_meter_sprint", sets = 1, reps = "4 reps", restSeconds = 90),
                    ExerciseReference(exerciseId = "hill_sprint", sets = 4, reps = "15m", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 2,
                title = "Day 2 — Strength & Structural Power",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Strength & Structural Power"),
                isRestDay = false,
                warmUp = listOf(
                    "Bike/jog — 4 min",
                    "Hip circles — 10/side",
                    "World’s greatest stretch — 5/side",
                    "Bodyweight squats — 2×10",
                    "Glute bridges — 2×12",
                    "Band lateral walks — 2×10/side",
                    "2–3 progressive warm-up sets for first major lift"
                ),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "squat", sets = 4, reps = "6", restSeconds = 90),
                    ExerciseReference(exerciseId = "romanian_deadlift", sets = 3, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "bulgarian_split_squat", sets = 3, reps = "8/leg", restSeconds = 90),
                    ExerciseReference(exerciseId = "pull_ups", sets = 3, reps = "6-10", restSeconds = 90),
                    ExerciseReference(exerciseId = "push_ups", sets = 3, reps = "12", restSeconds = 90),
                    ExerciseReference(exerciseId = "farmer_carry", sets = 3, reps = "30m", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 3,
                title = "Day 3 — Agility & Change of Direction",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Agility & Change of Direction"),
                isRestDay = false,
                warmUp = listOf(
                    "Easy jog — 3 min",
                    "Lateral shuffle — 2×15m",
                    "Carioca — 2×15m",
                    "Leg swings — 10/leg",
                    "Hip openers — 10/side",
                    "A-skips — 2×15m",
                    "Deceleration drill — 3×10m"
                ),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "5_10_5_shuttle", sets = 1, reps = "5 reps", restSeconds = 90),
                    ExerciseReference(exerciseId = "lateral_shuffle", sets = 4, reps = "15m", restSeconds = 90),
                    ExerciseReference(exerciseId = "t_drill", sets = 1, reps = "4 reps", restSeconds = 90),
                    ExerciseReference(exerciseId = "cone_reaction_drill", sets = 1, reps = "5 reps", restSeconds = 90),
                    ExerciseReference(exerciseId = "deceleration_sprint", sets = 5, reps = "15m", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 4,
                title = "Day 4 — Explosive Power",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Explosive Power"),
                isRestDay = false,
                warmUp = listOf(
                    "Jog — 3 min",
                    "Dynamic lunges — 10/leg",
                    "Hip mobility — 2 min",
                    "Pogo jumps — 2×15",
                    "Snap-downs — 2×6",
                    "Low-intensity broad jumps — 3 reps"
                ),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "box_jump", sets = 4, reps = "5", restSeconds = 90),
                    ExerciseReference(exerciseId = "broad_jump", sets = 4, reps = "4", restSeconds = 90),
                    ExerciseReference(exerciseId = "medicine_ball_chest_throw", sets = 4, reps = "6", restSeconds = 90),
                    ExerciseReference(exerciseId = "medicine_ball_rotational_throw", sets = 3, reps = "6/side", restSeconds = 90),
                    ExerciseReference(exerciseId = "kettlebell_swing", sets = 4, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "jump_squat", sets = 3, reps = "5", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 5,
                title = "Day 5 — High-Output Conditioning",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("High-Output Conditioning"),
                isRestDay = false,
                warmUp = listOf(
                    "Easy bike/jog — 5 min",
                    "Dynamic leg swings",
                    "Walking lunges",
                    "Arm circles",
                    "Bodyweight squats",
                    "High knees",
                    "2 rounds of light workout movements"
                ),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "4_rounds_250m_run_15_kettlebell_swings_12_burpees_15_walking_lunges_10_push_ups_30_sec_battle_ropes", sets = 1, reps = "1", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 6,
                title = "Day 6 — Endurance + Mobility",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Endurance + Mobility"),
                isRestDay = false,
                warmUp = listOf(
                    "5 min easy jog/walk",
                    "Ankle mobility",
                    "Hip mobility",
                    "Leg swings",
                    "Walking lunges"
                ),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "easy_moderate_run", sets = 1, reps = "25-35 min", restSeconds = 90),
                    ExerciseReference(exerciseId = "strides", sets = 4, reps = "60m", restSeconds = 90),
                    ExerciseReference(exerciseId = "plank_hold", sets = 3, reps = "40 sec", restSeconds = 90),
                    ExerciseReference(exerciseId = "side_plank_3_30_sec_side", sets = 1, reps = "1", restSeconds = 90),
                    ExerciseReference(exerciseId = "mobility_hip_flexor_hamstring_calf_thoracic_rotation", sets = 1, reps = "1", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 7,
                title = "Day 7 — Recovery",
                workoutType = ProgramWorkoutType.REST.name,
                focus = listOf("Recovery"),
                isRestDay = true,
                warmUp = listOf(
                    "Easy walk — 10–15 min",
                    "Full-body mobility — 15 min"
                ),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = emptyList(),
                notes = "Recovery day. Light walking, mobility, hydration, and sleep."
            ),
            ProgramDay(
                dayNumber = 8,
                title = "Day 8 — Max Velocity",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Max Velocity"),
                isRestDay = false,
                warmUp = listOf("Sprint-specific dynamic warm-up — 12–15 min"),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "a_skip", sets = 2, reps = "20m", restSeconds = 90),
                    ExerciseReference(exerciseId = "build_up_runs", sets = 3, reps = "30m", restSeconds = 90),
                    ExerciseReference(exerciseId = "flying_20m_sprint", sets = 1, reps = "5 reps", restSeconds = 90),
                    ExerciseReference(exerciseId = "forty_meter_sprint", sets = 1, reps = "4 reps", restSeconds = 90),
                    ExerciseReference(exerciseId = "bounding", sets = 3, reps = "20m", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 9,
                title = "Day 9 — Lower-Body Strength",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Lower-Body Strength"),
                isRestDay = false,
                warmUp = listOf("10–15 min cardio + hip/ankle activation + progressive lifting sets"),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "front_squat", sets = 4, reps = "5", restSeconds = 90),
                    ExerciseReference(exerciseId = "deadlift", sets = 4, reps = "4", restSeconds = 90),
                    ExerciseReference(exerciseId = "walking_lunges", sets = 3, reps = "10/leg", restSeconds = 90),
                    ExerciseReference(exerciseId = "hip_thrust", sets = 3, reps = "8", restSeconds = 90),
                    ExerciseReference(exerciseId = "standing_calf_raise", sets = 3, reps = "15", restSeconds = 90),
                    ExerciseReference(exerciseId = "copenhagen_plank_3_20_sec_side", sets = 1, reps = "1", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 10,
                title = "Day 10 — Reactive Agility",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Reactive Agility"),
                isRestDay = false,
                warmUp = listOf(
                    "Jog",
                    "Lateral shuffle",
                    "Carioca",
                    "Hip mobility",
                    "Deceleration drills",
                    "Quick-feet drills"
                ),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "reaction_cone_drill", sets = 1, reps = "6 reps", restSeconds = 90),
                    ExerciseReference(exerciseId = "mirror_drill", sets = 5, reps = "20 sec", restSeconds = 90),
                    ExerciseReference(exerciseId = "5_10_5_shuttle", sets = 1, reps = "5 reps", restSeconds = 90),
                    ExerciseReference(exerciseId = "lateral_bound", sets = 3, reps = "6/side", restSeconds = 90),
                    ExerciseReference(exerciseId = "backpedal_sprint", sets = 1, reps = "5 reps", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 11,
                title = "Day 11 — Full-Body Power",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Full-Body Power"),
                isRestDay = false,
                warmUp = listOf("Dynamic mobility + pogo jumps + medicine-ball throws"),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "hang_power_clean", sets = 4, reps = "3", restSeconds = 90),
                    ExerciseReference(exerciseId = "push_press", sets = 4, reps = "5", restSeconds = 90),
                    ExerciseReference(exerciseId = "jump_squat", sets = 4, reps = "5", restSeconds = 90),
                    ExerciseReference(exerciseId = "medicine_ball_slam", sets = 4, reps = "6", restSeconds = 90),
                    ExerciseReference(exerciseId = "broad_jump", sets = 3, reps = "4", restSeconds = 90),
                    ExerciseReference(exerciseId = "explosive_push_up", sets = 3, reps = "5", restSeconds = 90),
                ),
                notes = "*Hang power clean requires appropriate technical proficiency and coaching. Prioritize technique and quality over load."
            ),
            ProgramDay(
                dayNumber = 12,
                title = "Day 12 — Metabolic Conditioning",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Metabolic Conditioning"),
                isRestDay = false,
                warmUp = listOf("5 min cardio + dynamic mobility + movement rehearsal"),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "5_rounds_200m_run_10_burpees_15_kb_swings_12_push_ups_15_air_squats_30_sec_battle_ropes", sets = 1, reps = "1", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 13,
                title = "Day 13 — Aerobic Endurance",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Aerobic Endurance"),
                isRestDay = false,
                warmUp = listOf("8–10 min easy movement + dynamic mobility"),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "run_cycle", sets = 1, reps = "35-45 min", restSeconds = 90),
                    ExerciseReference(exerciseId = "5_60m_relaxed_strides", sets = 1, reps = "1", restSeconds = 90),
                    ExerciseReference(exerciseId = "dead_bug", sets = 3, reps = "10/side", restSeconds = 90),
                    ExerciseReference(exerciseId = "plank_hold", sets = 3, reps = "45 sec", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 14,
                title = "Day 14 — Recovery",
                workoutType = ProgramWorkoutType.REST.name,
                focus = listOf("Recovery"),
                isRestDay = true,
                warmUp = listOf(
                    "15–20 min easy walk",
                    "15 min mobility"
                ),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = emptyList(),
                notes = "Recovery day. Light walking, mobility, hydration, and sleep."
            ),
            ProgramDay(
                dayNumber = 15,
                title = "Day 15 — Acceleration",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Acceleration"),
                isRestDay = false,
                warmUp = listOf("Full sprint warm-up — 12–15 min"),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "falling_starts", sets = 5, reps = "10m", restSeconds = 90),
                    ExerciseReference(exerciseId = "ten_meter_sprint", sets = 6, reps = "1 rep", restSeconds = 90),
                    ExerciseReference(exerciseId = "twenty_meter_sprint", sets = 5, reps = "1 rep", restSeconds = 90),
                    ExerciseReference(exerciseId = "resisted_sprint", sets = 4, reps = "15m", restSeconds = 90),
                    ExerciseReference(exerciseId = "bounding", sets = 3, reps = "20m", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 16,
                title = "Day 16 — Full-Body Strength",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Full-Body Strength"),
                isRestDay = false,
                warmUp = listOf("Cardio + dynamic mobility + activation + progressive sets"),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "squat", sets = 4, reps = "5", restSeconds = 90),
                    ExerciseReference(exerciseId = "barbell_bench_press", sets = 4, reps = "6", restSeconds = 90),
                    ExerciseReference(exerciseId = "romanian_deadlift", sets = 3, reps = "6", restSeconds = 90),
                    ExerciseReference(exerciseId = "pull_ups", sets = 4, reps = "6-10", restSeconds = 90),
                    ExerciseReference(exerciseId = "bulgarian_split_squat", sets = 3, reps = "8/leg", restSeconds = 90),
                    ExerciseReference(exerciseId = "farmer_carry", sets = 3, reps = "40m", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 17,
                title = "Day 17 — Change of Direction",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Change of Direction"),
                isRestDay = false,
                warmUp = listOf("Jog + lateral movement + hip/ankle mobility + deceleration"),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "5_10_5_shuttle", sets = 5, reps = "1 rep", restSeconds = 90),
                    ExerciseReference(exerciseId = "t_drill", sets = 4, reps = "1 rep", restSeconds = 90),
                    ExerciseReference(exerciseId = "lateral_sprint_5_side", sets = 1, reps = "1", restSeconds = 90),
                    ExerciseReference(exerciseId = "backpedal_sprint", sets = 5, reps = "1 rep", restSeconds = 90),
                    ExerciseReference(exerciseId = "reactive_cone_drill", sets = 6, reps = "1 rep", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 18,
                title = "Day 18 — Explosive Power",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Explosive Power"),
                isRestDay = false,
                warmUp = listOf("Dynamic mobility + landing mechanics + pogo jumps"),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "box_jump", sets = 4, reps = "4", restSeconds = 90),
                    ExerciseReference(exerciseId = "broad_jump", sets = 4, reps = "3", restSeconds = 90),
                    ExerciseReference(exerciseId = "split_jump", sets = 3, reps = "5/leg", restSeconds = 90),
                    ExerciseReference(exerciseId = "med_ball_slam", sets = 4, reps = "6", restSeconds = 90),
                    ExerciseReference(exerciseId = "rotational_throw", sets = 3, reps = "6/side", restSeconds = 90),
                    ExerciseReference(exerciseId = "kb_swing", sets = 4, reps = "8", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 19,
                title = "Day 19 — High-Intensity Conditioning",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("High-Intensity Conditioning"),
                isRestDay = false,
                warmUp = listOf("8–10 min dynamic warm-up"),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "6_rounds_30_sec_hard_run_10_burpees_15_kb_swings_10_push_ups_15_squats", sets = 1, reps = "1", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 20,
                title = "Day 20 — Tempo Endurance",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Tempo Endurance"),
                isRestDay = false,
                warmUp = listOf("8–10 min easy movement + dynamic mobility"),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "5_5_min_moderate_running", sets = 1, reps = "1", restSeconds = 90),
                    ExerciseReference(exerciseId = "4_60m_relaxed_strides", sets = 1, reps = "1", restSeconds = 90),
                    ExerciseReference(exerciseId = "core_circuit_3_rounds", sets = 1, reps = "1", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 21,
                title = "Day 21 — Recovery",
                workoutType = ProgramWorkoutType.REST.name,
                focus = listOf("Recovery"),
                isRestDay = true,
                warmUp = listOf(
                    "20 min easy walk/cycle",
                    "15–20 min mobility"
                ),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = emptyList(),
                notes = "Recovery day. Light walking, mobility, hydration, and sleep."
            ),
            ProgramDay(
                dayNumber = 22,
                title = "Day 22 — Sprint Mechanics",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Sprint Mechanics"),
                isRestDay = false,
                warmUp = listOf(
                    "Jog",
                    "Ankling",
                    "A-skips",
                    "High knees",
                    "Straight-leg runs",
                    "3 progressive accelerations"
                ),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "wall_sprint_drill", sets = 3, reps = "10 sec", restSeconds = 90),
                    ExerciseReference(exerciseId = "a_skip", sets = 3, reps = "20m", restSeconds = 90),
                    ExerciseReference(exerciseId = "flying_20m", sets = 5, reps = "1 rep", restSeconds = 90),
                    ExerciseReference(exerciseId = "thirty_meter_sprint", sets = 5, reps = "1 rep", restSeconds = 90),
                    ExerciseReference(exerciseId = "forty_meter_sprint", sets = 3, reps = "1 rep", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 23,
                title = "Day 23 — Maximum Strength",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Maximum Strength"),
                isRestDay = false,
                warmUp = listOf("12–15 min warm-up + progressive warm-up sets"),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "squat", sets = 5, reps = "3", restSeconds = 90),
                    ExerciseReference(exerciseId = "deadlift", sets = 4, reps = "3", restSeconds = 90),
                    ExerciseReference(exerciseId = "barbell_bench_press", sets = 4, reps = "5", restSeconds = 90),
                    ExerciseReference(exerciseId = "pull_ups", sets = 4, reps = "6", restSeconds = 90),
                    ExerciseReference(exerciseId = "bulgarian_split_squat", sets = 3, reps = "6/leg", restSeconds = 90),
                    ExerciseReference(exerciseId = "farmer_carry", sets = 3, reps = "30m", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 24,
                title = "Day 24 — Reaction + Agility",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Reaction + Agility"),
                isRestDay = false,
                warmUp = listOf(
                    "Jog — 3 min",
                    "Lateral shuffle",
                    "Carioca",
                    "Quick feet",
                    "Deceleration",
                    "Reaction preparation"
                ),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "reaction_cone_drill", sets = 6, reps = "1 rep", restSeconds = 90),
                    ExerciseReference(exerciseId = "mirror_drill", sets = 5, reps = "20 sec", restSeconds = 90),
                    ExerciseReference(exerciseId = "5_10_5", sets = 5, reps = "1 rep", restSeconds = 90),
                    ExerciseReference(exerciseId = "t_drill", sets = 4, reps = "1 rep", restSeconds = 90),
                    ExerciseReference(exerciseId = "reactive_sprint", sets = 6, reps = "15m", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 25,
                title = "Day 25 — Elastic + Explosive Power",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Elastic + Explosive Power"),
                isRestDay = false,
                warmUp = listOf(
                    "Jog",
                    "Dynamic mobility",
                    "Pogo jumps",
                    "Snap-downs",
                    "Landing practice"
                ),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "pogo_jump", sets = 3, reps = "20", restSeconds = 90),
                    ExerciseReference(exerciseId = "depth_drop_landing", sets = 3, reps = "5", restSeconds = 90),
                    ExerciseReference(exerciseId = "box_jump", sets = 4, reps = "4", restSeconds = 90),
                    ExerciseReference(exerciseId = "broad_jump", sets = 4, reps = "3", restSeconds = 90),
                    ExerciseReference(exerciseId = "bounds", sets = 3, reps = "20m", restSeconds = 90),
                    ExerciseReference(exerciseId = "medicine_ball_throw", sets = 4, reps = "6", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 26,
                title = "Day 26 — Athlete MetCon",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Athlete MetCon"),
                isRestDay = false,
                warmUp = listOf("10–12 min warm-up"),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "5_rounds_300m_run_12_kb_swings_10_burpees_15_walking_lunges_10_push_ups_30_sec_battle_ropes", sets = 1, reps = "1", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 27,
                title = "Day 27 — Endurance + Recovery",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Endurance + Recovery"),
                isRestDay = false,
                warmUp = listOf("8–10 min easy movement"),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "easy_run_cycle", sets = 1, reps = "30-40 min", restSeconds = 90),
                    ExerciseReference(exerciseId = "4_60m_relaxed_strides", sets = 1, reps = "1", restSeconds = 90),
                    ExerciseReference(exerciseId = "core", sets = 1, reps = "10 min", restSeconds = 90),
                    ExerciseReference(exerciseId = "mobility", sets = 1, reps = "15 min", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 28,
                title = "Day 28 — Recovery",
                workoutType = ProgramWorkoutType.REST.name,
                focus = listOf("Recovery"),
                isRestDay = true,
                warmUp = listOf(
                    "Easy walk — 15–20 min",
                    "Full-body mobility — 20 min"
                ),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = emptyList(),
                notes = "Recovery day. Light walking, mobility, hydration, and sleep."
            ),
            ProgramDay(
                dayNumber = 29,
                title = "Day 29 — Speed + Power",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Speed + Power"),
                isRestDay = false,
                warmUp = listOf(
                    "Easy jog — 3 min",
                    "Ankle bounces — 20",
                    "Leg swings — 10/leg",
                    "Walking lunges — 10/leg",
                    "A-skips — 2×15m",
                    "High knees — 2×15m",
                    "Pogo jumps — 2×15",
                    "3 progressive accelerations"
                ),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "ten_meter_sprint", sets = 4, reps = "1 rep", restSeconds = 90),
                    ExerciseReference(exerciseId = "twenty_meter_sprint", sets = 4, reps = "1 rep", restSeconds = 90),
                    ExerciseReference(exerciseId = "flying_20m", sets = 3, reps = "1 rep", restSeconds = 90),
                    ExerciseReference(exerciseId = "broad_jump", sets = 4, reps = "3", restSeconds = 90),
                    ExerciseReference(exerciseId = "box_jump", sets = 3, reps = "4", restSeconds = 90),
                    ExerciseReference(exerciseId = "medicine_ball_throw", sets = 3, reps = "6", restSeconds = 90),
                ),
                notes = "Prioritize technical execution and movement quality over speed or load."
            ),
            ProgramDay(
                dayNumber = 30,
                title = "Day 30 — Athlete Performance Test",
                workoutType = ProgramWorkoutType.TRAINING.name,
                focus = listOf("Athlete Performance Test"),
                isRestDay = false,
                warmUp = listOf(
                    "Easy jog",
                    "Dynamic mobility",
                    "A-skips",
                    "High knees",
                    "Ankling",
                    "Progressive accelerations",
                    "2–3 practice reps"
                ),
                coolDown = listOf("5–10 min easy walking + relevant mobility/stretching."),
                exercises = listOf(
                    ExerciseReference(exerciseId = "ten_meter_sprint", sets = 3, reps = "1 attempt", restSeconds = 90),
                    ExerciseReference(exerciseId = "thirty_meter_sprint", sets = 3, reps = "1 attempt", restSeconds = 90),
                    ExerciseReference(exerciseId = "standing_broad_jump", sets = 3, reps = "1 attempt", restSeconds = 90),
                    ExerciseReference(exerciseId = "vertical_jump", sets = 3, reps = "1 attempt", restSeconds = 90),
                    ExerciseReference(exerciseId = "5_10_5_shuttle", sets = 3, reps = "1 attempt", restSeconds = 90),
                    ExerciseReference(exerciseId = "yo_yo_beep_style_conditioning_test", sets = 1, reps = "1", restSeconds = 90),
                ),
                notes = "Day 30 Athlete Performance Test: calculate performance score across Speed, Power, Agility, and Conditioning."
            ),
        ),
        isActive = true,
        version = 1
    )
}
