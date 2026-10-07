package com.example.fitness.feature.programs.data

import com.example.fitness.feature.programs.model.ProgramScheduleItem
import com.example.fitness.feature.programs.model.WorkoutProgram
import com.example.fitness.feature.workout.model.CoolDownItem
import com.example.fitness.feature.workout.model.PlanExercise
import com.example.fitness.feature.workout.model.ThirtyDayWorkout
import com.example.fitness.feature.workout.model.WarmUpItem
import fitness.shared.generated.resources.Res
import fitness.shared.generated.resources.bodypart_back
import fitness.shared.generated.resources.bodypart_chest
import fitness.shared.generated.resources.program_strength
import fitness.shared.generated.resources.workout_gym
import fitness.shared.generated.resources.workout_home
import fitness.shared.generated.resources.workout_incline_bench

object StrengthProgramData {

    val program = WorkoutProgram(
        id = "strength",
        title = "Strength",
        subtitle = "Strength-focused training with heavy compound progression.",
        description = "A dedicated powerlifting and maximal strength development program. Built around progressive overload on the core compound movements: Squat, Bench Press, Deadlift, and Overhead Press, with heavy load intensity (3-6 reps) and extended rest periods (120-180s) for full ATP-CP recovery.",
        duration = "30 Days",
        goal = "Maximal Strength & Neuromuscular Power",
        difficulty = "Advanced / Expert",
        workoutsCount = 30,
        focus = "Squat, Bench, Deadlift, Overhead Press, Pull Power",
        category = "Strength",
        imageRes = Res.drawable.program_strength,
        schedule = (1..30).map { day ->
            val cycleDay = ((day - 1) % 7) + 1
            val (title, focus, isRest) = when (cycleDay) {
                1 -> Triple("Heavy Squat + Lower Power", "Quadriceps, Glutes, Lumbar Bracing", false)
                2 -> Triple("Heavy Bench Press + Upper Strength", "Pectoralis, Triceps, Anterior Delts", false)
                3 -> Triple("CNS Deload & Active Mobility", "Spinal Decompression & Recovery", true)
                4 -> Triple("Heavy Deadlift + Posterior Chain", "Hamstrings, Spinal Erectors, Traps", false)
                5 -> Triple("Strict Overhead Press + Upper Power", "Deltoids, Upper Chest, Core Stability", false)
                6 -> Triple("Heavy Pull & Barbell Rows", "Latissimus Dorsi, Rhomboids, Grip", false)
                else -> Triple("Rest & Structural Recovery", "Central Nervous System Reset", true)
            }
            ProgramScheduleItem(
                dayNumber = day,
                dayTitle = title,
                focus = focus,
                workoutId = "strength_day_$day",
                duration = if (isRest) "20 min" else "50-60 min",
                difficulty = if (isRest) "Low" else "Expert",
                exerciseCount = if (isRest) 3 else 4,
                isRestDay = isRest
            )
        }
    )

    fun getWorkoutForDay(dayNumber: Int): ThirtyDayWorkout {
        val safeDay = dayNumber.coerceIn(1, 30)
        val cycleDay = ((safeDay - 1) % 7) + 1
        val workoutId = "strength_day_$safeDay"

        return when (cycleDay) {
            1 -> createSquatWorkout(safeDay, workoutId)
            2 -> createBenchWorkout(safeDay, workoutId)
            3 -> createDeloadRecoveryWorkout(safeDay, workoutId)
            4 -> createDeadliftWorkout(safeDay, workoutId)
            5 -> createOverheadPressWorkout(safeDay, workoutId)
            6 -> createHeavyRowWorkout(safeDay, workoutId)
            else -> createFullRestWorkout(safeDay, workoutId)
        }
    }

    private fun createSquatWorkout(day: Int, id: String): ThirtyDayWorkout {
        return ThirtyDayWorkout(
            id = id,
            dayNumber = day,
            dayName = "Day $day",
            dayOfWeek = "Day $day",
            workoutTitle = "Heavy Squat + Lower Power",
            focus = "Quadriceps, Gluteal Complex, Core Bracing",
            duration = "52 min",
            difficulty = "Expert",
            calories = "490 kcal",
            imageRes = Res.drawable.workout_gym,
            bodyPartId = "legs",
            workoutType = "gym",
            isRestDay = false,
            actionButtonText = "Start Strength Session",
            warmUp = listOf(
                WarmUpItem("sw1", "Glute Activation Bridges", "2 x 15 reps", "Establish neuromuscular connection before heavy loading."),
                WarmUpItem("sw2", "Deep Goblet Squat Prying", "2 x 8 reps", "Open hips, ankles, and adductors."),
                WarmUpItem("sw3", "Barbell Ramp-Up Sets", "3 sets x 5 reps (empty bar, 40%, 60%)", "Groove squat bar path and establish rigid 360 brace.")
            ),
            exercises = listOf(
                PlanExercise(
                    id = "str_back_squat",
                    name = "Competition Barbell Back Squat",
                    sets = "5 Sets",
                    reps = "3-5 Reps",
                    rest = "180s",
                    equipment = "Power Rack, Olympic Barbell, Chalk",
                    targetMuscle = "Quadriceps, Glutes, Core Bracing",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    instructions = "Take high tension deep breath and Valsalva brace. Descend below parallel, drive up aggressively.",
                    imageRes = Res.drawable.workout_gym
                ),
                PlanExercise(
                    id = "str_pause_squat",
                    name = "2-Second Pause Squats",
                    sets = "3 Sets",
                    reps = "4-6 Reps",
                    rest = "120s",
                    equipment = "Barbell, Squat Rack",
                    targetMuscle = "Rate of Force Development (RFD) in the Hole",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    instructions = "Halt momentum completely at the bottom for a counted 2 seconds, then explode upwards.",
                    imageRes = Res.drawable.workout_gym
                ),
                PlanExercise(
                    id = "str_bulgarian_split_squat",
                    name = "Heavy Dumbbell Bulgarian Split Squat",
                    sets = "3 Sets",
                    reps = "6-8 Reps/Side",
                    rest = "90s",
                    equipment = "Flat Bench, Heavy Dumbbells",
                    targetMuscle = "Unilateral Quad & Glute Power",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    instructions = "Elevate rear foot on bench. Keep front knee tracking over toes, drive straight up.",
                    imageRes = Res.drawable.workout_gym
                ),
                PlanExercise(
                    id = "str_barbell_good_morning",
                    name = "Barbell Good Mornings",
                    sets = "3 Sets",
                    reps = "8 Reps",
                    rest = "90s",
                    equipment = "Barbell",
                    targetMuscle = "Hamstrings & Lower Back Stability",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    instructions = "Bar on upper traps. Hinge hips back with a flat spine, feel intense hamstring tension, squeeze glutes.",
                    imageRes = Res.drawable.workout_gym
                )
            ),
            coolDown = listOf(
                CoolDownItem("sc1", "Adductor Half-Kneeling Stretch", "45s per side", "Release inner thighs and groin."),
                CoolDownItem("sc2", "Standing Quad Stretch", "45s per side", "Lengthen quadriceps fibers."),
                CoolDownItem("sc3", "Elevated Feet Spinal Decompression", "2 min", "Rest calves on bench, allow lumbar spine to decompress.")
            )
        )
    }

    private fun createBenchWorkout(day: Int, id: String): ThirtyDayWorkout {
        return ThirtyDayWorkout(
            id = id,
            dayNumber = day,
            dayName = "Day $day",
            dayOfWeek = "Day $day",
            workoutTitle = "Heavy Bench Press + Upper Strength",
            focus = "Pectoralis Major, Triceps, Anterior Deltoids",
            duration = "50 min",
            difficulty = "Expert",
            calories = "460 kcal",
            imageRes = Res.drawable.bodypart_chest,
            bodyPartId = "chest",
            workoutType = "gym",
            isRestDay = false,
            actionButtonText = "Start Strength Session",
            warmUp = listOf(
                WarmUpItem("bw1", "Band Pull-Aparts & Dislocates", "2 x 15 reps", "Warm up rotator cuff and upper back shelf."),
                WarmUpItem("bw2", "Thoracic Foam Roll Extensions", "2 min", "Create arch and stable shoulder retraction."),
                WarmUpItem("bw3", "Ramp-Up Bench Press Sets", "3 sets x 5 reps (empty bar, 50%, 70%)", "Dial in leg drive and bar touchpoint.")
            ),
            exercises = listOf(
                PlanExercise(
                    id = "str_bench_press",
                    name = "Competition Barbell Bench Press",
                    sets = "5 Sets",
                    reps = "3-5 Reps",
                    rest = "180s",
                    equipment = "Olympic Barbell, Flat Competition Bench",
                    targetMuscle = "Pectoralis Major, Triceps Lockout",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    instructions = "Retract scapulae firmly into the bench. Lower bar under control to sternum, drive through feet.",
                    imageRes = Res.drawable.workout_incline_bench
                ),
                PlanExercise(
                    id = "str_close_grip_bench",
                    name = "Close-Grip Barbell Bench Press",
                    sets = "4 Sets",
                    reps = "5-6 Reps",
                    rest = "120s",
                    equipment = "Barbell, Flat Bench",
                    targetMuscle = "Triceps Power & Lockout Strength",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    instructions = "Grip shoulder-width apart. Tuck elbows close to torso, press explosively to full extension.",
                    imageRes = Res.drawable.workout_gym
                ),
                PlanExercise(
                    id = "str_weighted_dips",
                    name = "Weighted Chest/Triceps Dips",
                    sets = "3 Sets",
                    reps = "6 Reps",
                    rest = "120s",
                    equipment = "Dip Station, Dip Belt / Dumbbell",
                    targetMuscle = "Lower Pectoralis & Triceps",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    instructions = "Lean torso forward 15 degrees. Lower until upper arms break 90 degrees, press powerfully.",
                    imageRes = Res.drawable.workout_incline_bench
                ),
                PlanExercise(
                    id = "str_heavy_db_row",
                    name = "Heavy Single-Arm Dumbbell Row",
                    sets = "3 Sets",
                    reps = "6-8 Reps/Side",
                    rest = "90s",
                    equipment = "Heavy Dumbbells, Flat Bench",
                    targetMuscle = "Lats, Upper Back Antagonist Base",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    instructions = "Lock spine neutral. Pull heavy dumbbell to hip pocket without rotating upper body.",
                    imageRes = Res.drawable.bodypart_back
                )
            ),
            coolDown = listOf(
                CoolDownItem("bc1", "Doorway Pectoral Stretch", "45s per side", "Gently elongate pectoralis minor and major."),
                CoolDownItem("bc2", "Behind-the-Back Shoulder Extension", "45s", "Relieve anterior deltoid compression."),
                CoolDownItem("bc3", "Child's Pose with Lateral Reach", "60s", "Open lateral chest and side body.")
            )
        )
    }

    private fun createDeloadRecoveryWorkout(day: Int, id: String): ThirtyDayWorkout {
        return ThirtyDayWorkout(
            id = id,
            dayNumber = day,
            dayName = "Day $day",
            dayOfWeek = "Day $day",
            workoutTitle = "CNS Deload & Active Mobility",
            focus = "Spinal Decompression, Nervous System Recovery",
            duration = "20 min",
            difficulty = "Low",
            calories = "110 kcal",
            imageRes = Res.drawable.workout_home,
            bodyPartId = "recovery",
            workoutType = "home",
            isRestDay = true,
            actionButtonText = "Start Deload Flow",
            warmUp = listOf(
                WarmUpItem("dw1", "Box Breathing (4-4-4-4)", "3 min", "Shift from sympathetic fight-or-flight to recovery mode.")
            ),
            exercises = listOf(
                PlanExercise(
                    id = "str_deadhang",
                    name = "Passive Bar Dead Hang",
                    sets = "3 Sets",
                    reps = "45-60s",
                    rest = "45s",
                    equipment = "Pull-Up Bar",
                    targetMuscle = "Spinal Decompression & Shoulder Capsule",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    instructions = "Hang completely relaxed with feet off the ground. Allow gravity to decompress intervertebral discs.",
                    imageRes = Res.drawable.workout_home
                ),
                PlanExercise(
                    id = "str_cat_cow",
                    name = "Segmental Cat-Cow Flow",
                    sets = "2 Sets",
                    reps = "10 Slow Reps",
                    rest = "30s",
                    equipment = "Mat",
                    targetMuscle = "Vertebral Articulation",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    instructions = "Move vertebra by vertebra starting from pelvis to cervical spine.",
                    imageRes = Res.drawable.workout_home
                ),
                PlanExercise(
                    id = "str_glute_piriformis",
                    name = "Figure-Four Wall Piriformis Stretch",
                    sets = "2 Sets",
                    reps = "60s/Side",
                    rest = "30s",
                    equipment = "Wall, Mat",
                    targetMuscle = "Deep Hip Rotators & Sciatic Relief",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    instructions = "Cross ankle over opposite knee, use wall to adjust depth of hip stretch.",
                    imageRes = Res.drawable.workout_home
                )
            ),
            coolDown = listOf(
                CoolDownItem("dc1", "Lying Savasana", "3 min", "Complete muscular rest.")
            )
        )
    }

    private fun createDeadliftWorkout(day: Int, id: String): ThirtyDayWorkout {
        return ThirtyDayWorkout(
            id = id,
            dayNumber = day,
            dayName = "Day $day",
            dayOfWeek = "Day $day",
            workoutTitle = "Heavy Deadlift + Posterior Chain",
            focus = "Hamstrings, Gluteus Maximus, Erector Spinae, Traps",
            duration = "55 min",
            difficulty = "Expert",
            calories = "520 kcal",
            imageRes = Res.drawable.bodypart_back,
            bodyPartId = "back",
            workoutType = "gym",
            isRestDay = false,
            actionButtonText = "Start Strength Session",
            warmUp = listOf(
                WarmUpItem("dw1", "Bird-Dog Core Stabilizers", "2 x 10 reps/side", "Engage contralateral posterior chain and brace abs."),
                WarmUpItem("dw2", "Banded Hamstring Sweeps", "2 x 12 reps", "Prepare posterior chain flexibility."),
                WarmUpItem("dw3", "Barbell Deadlift Ramp-Ups", "3 sets x 4 reps (empty bar, 50%, 70%)", "Wedge hips, pull slack out of the bar, lock lats.")
            ),
            exercises = listOf(
                PlanExercise(
                    id = "str_conventional_deadlift",
                    name = "Heavy Conventional Barbell Deadlift",
                    sets = "5 Sets",
                    reps = "3-5 Reps",
                    rest = "180s",
                    equipment = "Olympic Barbell, Bumper/Iron Plates, Chalk",
                    targetMuscle = "Total Posterior Chain & Grip",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    instructions = "Feet hip-width. Pull slack out of the bar until click is heard, push the floor away with legs.",
                    imageRes = Res.drawable.workout_gym
                ),
                PlanExercise(
                    id = "str_deficit_deadlift",
                    name = "Deficit Deadlifts (2-Inch Platform)",
                    sets = "3 Sets",
                    reps = "4-5 Reps",
                    rest = "150s",
                    equipment = "Barbell, 2-Inch Platform",
                    targetMuscle = "Speed Off the Floor & Quad Drive",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    instructions = "Stand on plate. Increased range of motion requires greater leg drive and lumbar rigidity.",
                    imageRes = Res.drawable.bodypart_back
                ),
                PlanExercise(
                    id = "str_barbell_shrug",
                    name = "Heavy Barbell Power Shrug",
                    sets = "4 Sets",
                    reps = "6-8 Reps",
                    rest = "90s",
                    equipment = "Barbell, Straps",
                    targetMuscle = "Trapezius & Upper Back Thickness",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    instructions = "Explosive triple extension assist if needed, hold top for 1 count, lower under control.",
                    imageRes = Res.drawable.workout_gym
                ),
                PlanExercise(
                    id = "str_farmers_walk",
                    name = "Heavy Dumbbell Farmer's Carry",
                    sets = "3 Sets",
                    reps = "40 Meters",
                    rest = "90s",
                    equipment = "Heavy Dumbbells or Trap Bar",
                    targetMuscle = "Core Anti-Lateral Flexion & Grip Strength",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    instructions = "Stand tall with ribs down. Walk in steady heel-to-toe strides without swaying.",
                    imageRes = Res.drawable.workout_gym
                )
            ),
            coolDown = listOf(
                CoolDownItem("dlc1", "Seated Forward Fold Hamstring Stretch", "60s", "Lengthen hamstrings and calves."),
                CoolDownItem("dlc2", "Lying Spinal Twist", "45s per side", "Release thoracolumbar fascia."),
                CoolDownItem("dlc3", "Deep Diaphragmatic Breath in Child's Pose", "90s", "Systemic recovery.")
            )
        )
    }

    private fun createOverheadPressWorkout(day: Int, id: String): ThirtyDayWorkout {
        return ThirtyDayWorkout(
            id = id,
            dayNumber = day,
            dayName = "Day $day",
            dayOfWeek = "Day $day",
            workoutTitle = "Strict Overhead Press + Upper Power",
            focus = "Anterior & Lateral Deltoids, Triceps, Upper Chest",
            duration = "48 min",
            difficulty = "Expert",
            calories = "440 kcal",
            imageRes = Res.drawable.workout_incline_bench,
            bodyPartId = "shoulders",
            workoutType = "gym",
            isRestDay = false,
            actionButtonText = "Start Strength Session",
            warmUp = listOf(
                WarmUpItem("ow1", "Banded Shoulder Pass-Throughs", "2 x 12 reps", "Mobilize shoulder overhead flexion."),
                WarmUpItem("ow2", "Half-Kneeling Kettlebell Halo", "2 x 8 reps/side", "Prime scapular tracking and core stability."),
                WarmUpItem("ow3", "Overhead Press Ramp-Up", "3 sets x 5 reps (empty bar, 50%, 70%)", "Establish glute squeeze and tight bar path.")
            ),
            exercises = listOf(
                PlanExercise(
                    id = "str_strict_ohp",
                    name = "Standing Military Overhead Press",
                    sets = "5 Sets",
                    reps = "4-6 Reps",
                    rest = "150s",
                    equipment = "Barbell, Squat Rack",
                    targetMuscle = "Deltoids, Triceps Lockout, Core Stabilization",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    instructions = "Squeeze glutes and quads. Press bar vertically clearing face, push head through at lockout.",
                    imageRes = Res.drawable.workout_incline_bench
                ),
                PlanExercise(
                    id = "str_push_press",
                    name = "Barbell Push Press",
                    sets = "3 Sets",
                    reps = "3-5 Reps",
                    rest = "120s",
                    equipment = "Barbell, Rack",
                    targetMuscle = "Triple Extension & Overhead Power",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    instructions = "Shallow dip of knees, explode through legs and drive barbell overhead to lockout.",
                    imageRes = Res.drawable.workout_gym
                ),
                PlanExercise(
                    id = "str_heavy_lateral_swing",
                    name = "Heavy Lateral Deltoid Swings",
                    sets = "4 Sets",
                    reps = "8-10 Reps",
                    rest = "75s",
                    equipment = "Dumbbells",
                    targetMuscle = "Lateral Deltoids",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    instructions = "Controlled swing from bottoms of dumbbells to parallel, focusing on side deltoid power.",
                    imageRes = Res.drawable.workout_gym
                ),
                PlanExercise(
                    id = "str_ab_wheel_rollout",
                    name = "Ab Wheel Rollouts",
                    sets = "3 Sets",
                    reps = "8-10 Reps",
                    rest = "60s",
                    equipment = "Ab Wheel, Mat",
                    targetMuscle = "Anti-Extension Core Rigidity",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    instructions = "From knees, roll forward maintaining posterior pelvic tilt. Pull back using abs.",
                    imageRes = Res.drawable.workout_gym
                )
            ),
            coolDown = listOf(
                CoolDownItem("oc1", "Puppy Pose Shoulder Stretch", "60s", "Lengthen lat attachments and thoracic spine."),
                CoolDownItem("oc2", "Cross-Body Triceps & Rear Delt Stretch", "45s per side", "Ease posterior capsule."),
                CoolDownItem("oc3", "Chest Wall Opener", "45s per side", "Release pec minor after overhead pressing.")
            )
        )
    }

    private fun createHeavyRowWorkout(day: Int, id: String): ThirtyDayWorkout {
        return ThirtyDayWorkout(
            id = id,
            dayNumber = day,
            dayName = "Day $day",
            dayOfWeek = "Day $day",
            workoutTitle = "Heavy Pull & Barbell Rows",
            focus = "Latissimus Dorsi, Rhomboids, Posterior Delts, Biceps",
            duration = "50 min",
            difficulty = "Expert",
            calories = "470 kcal",
            imageRes = Res.drawable.bodypart_back,
            bodyPartId = "back",
            workoutType = "gym",
            isRestDay = false,
            actionButtonText = "Start Strength Session",
            warmUp = listOf(
                WarmUpItem("rw1", "Thoracic Spine Rotations", "2 x 10 reps", "Prime rotational mobility."),
                WarmUpItem("rw2", "Scapular Pull-Ups", "2 x 8 reps", "Engage latissimus dorsi and lower traps."),
                WarmUpItem("rw3", "Barbell Row Ramp-Up Sets", "3 sets x 5 reps (empty bar, 50%, 70%)", "Dial in 45-degree hinge stability.")
            ),
            exercises = listOf(
                PlanExercise(
                    id = "str_pendlay_row",
                    name = "Strict Pendlay Barbell Row",
                    sets = "5 Sets",
                    reps = "5 Reps",
                    rest = "120s",
                    equipment = "Olympic Barbell, Bumper Plates",
                    targetMuscle = "Upper Back, Lats, Rhomboids",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    instructions = "Start each rep dead from the floor. Explode bar to touch upper abdomen, return to dead stop.",
                    imageRes = Res.drawable.bodypart_back
                ),
                PlanExercise(
                    id = "str_weighted_pullup",
                    name = "Weighted Pull-Ups",
                    sets = "4 Sets",
                    reps = "4-6 Reps",
                    rest = "120s",
                    equipment = "Pull-Up Bar, Weight Belt / Dumbbell",
                    targetMuscle = "Latissimus Dorsi & Biceps Strength",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    instructions = "Full dead hang at bottom. Pull chin completely over bar with zero kipping.",
                    imageRes = Res.drawable.bodypart_back
                ),
                PlanExercise(
                    id = "str_heavy_chest_supported_row",
                    name = "Heavy Seal / Incline Bench Dumbbell Row",
                    sets = "3 Sets",
                    reps = "6-8 Reps",
                    rest = "90s",
                    equipment = "Incline Bench, Dumbbells",
                    targetMuscle = "Mid-Back Thickness (No Lower Back Fatigue)",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    instructions = "Chest glued to bench. Row heavy bells up with elbows flaring out 45 degrees.",
                    imageRes = Res.drawable.workout_gym
                ),
                PlanExercise(
                    id = "str_bb_cheat_curl",
                    name = "Heavy Barbell Power Curls",
                    sets = "3 Sets",
                    reps = "6-8 Reps",
                    rest = "90s",
                    equipment = "Straight Barbell, Plates",
                    targetMuscle = "Biceps Brachii & Forearm Grip",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    instructions = "Controlled hip momentum to initiate concentric, fight eccentric descent for 3 seconds.",
                    imageRes = Res.drawable.workout_gym
                )
            ),
            coolDown = listOf(
                CoolDownItem("rc1", "Lat Bar Hang", "60s", "Stretch lat fibers."),
                CoolDownItem("rc2", "Seated Upper Back Stretch", "45s", "Interlace fingers forward and round upper back."),
                CoolDownItem("rc3", "Biceps Wall Extension", "40s per side", "Release elbow flexors.")
            )
        )
    }

    private fun createFullRestWorkout(day: Int, id: String): ThirtyDayWorkout {
        return ThirtyDayWorkout(
            id = id,
            dayNumber = day,
            dayName = "Day $day",
            dayOfWeek = "Day $day",
            workoutTitle = "Rest & Structural Recovery",
            focus = "Central Nervous System Reset, Hydration, Recovery",
            duration = "15 min",
            difficulty = "Low",
            calories = "80 kcal",
            imageRes = Res.drawable.workout_home,
            bodyPartId = "recovery",
            workoutType = "home",
            isRestDay = true,
            actionButtonText = "Start Rest Protocol",
            warmUp = listOf(
                WarmUpItem("rw1", "Hydration & Gentle Walking", "15 min", "Low impact movement to promote blood flow without fatigue.")
            ),
            exercises = listOf(
                PlanExercise(
                    id = "str_foam_roll_glutes",
                    name = "Foam Roll Hips & Glutes",
                    sets = "2 Sets",
                    reps = "60s/Side",
                    rest = "30s",
                    equipment = "Foam Roller",
                    targetMuscle = "Gluteal Complex",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    instructions = "Slow, deliberate rolling to release deep tissue tension from heavy squats and deadlifts.",
                    imageRes = Res.drawable.workout_home
                ),
                PlanExercise(
                    id = "str_childs_pose_breath",
                    name = "Extended Child's Pose Breathing",
                    sets = "2 Sets",
                    reps = "90s",
                    rest = "30s",
                    equipment = "Mat",
                    targetMuscle = "Thoracolumbar Decompression",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    instructions = "Breathe deeply expanding lower back against ribs.",
                    imageRes = Res.drawable.workout_home
                )
            ),
            coolDown = listOf(
                CoolDownItem("rc1", "Full Rest", "5 min", "Enjoy rest and replenish glycogen.")
            )
        )
    }
}
