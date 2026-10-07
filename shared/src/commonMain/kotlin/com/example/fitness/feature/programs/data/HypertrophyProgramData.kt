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
import fitness.shared.generated.resources.workout_gym
import fitness.shared.generated.resources.workout_home
import fitness.shared.generated.resources.workout_incline_bench

object HypertrophyProgramData {

    val program = WorkoutProgram(
        id = "hypertrophy",
        title = "Hypertrophy",
        subtitle = "Build muscle through structured hypertrophy-focused training.",
        description = "A specialized 30-day hypertrophy program engineered to maximize muscle protein synthesis through progressive volume overload, optimal rep ranges (8-12 reps), and controlled rest intervals (60-90s).",
        duration = "30 Days",
        goal = "Muscle Growth & Hypertrophy",
        difficulty = "Intermediate / Advanced",
        workoutsCount = 30,
        focus = "Chest, Back, Quads, Hamstrings, Shoulders, Arms",
        category = "Muscle Growth",
        imageRes = Res.drawable.workout_incline_bench,
        schedule = (1..30).map { day ->
            val cycleDay = ((day - 1) % 7) + 1
            val (title, focus, isRest) = when (cycleDay) {
                1 -> Triple("Chest + Triceps", "Pectorals, Anterior Deltoids, Triceps", false)
                2 -> Triple("Back + Biceps", "Latissimus Dorsi, Rhomboids, Biceps", false)
                3 -> Triple("Quads + Hamstrings", "Quadriceps, Hamstrings, Glutes, Calves", false)
                4 -> Triple("Shoulders + Traps", "Lateral Deltoids, Posterior Deltoids, Trapezius", false)
                5 -> Triple("Chest + Back Antagonist", "Chest, Upper Back, Lats", false)
                6 -> Triple("Arms + Core", "Biceps, Triceps, Core Stabilizers", false)
                else -> Triple("Active Recovery & Mobility", "Full Body Mobility & Tissue Recovery", true)
            }
            ProgramScheduleItem(
                dayNumber = day,
                dayTitle = title,
                focus = focus,
                workoutId = "hypertrophy_day_$day",
                duration = if (isRest) "20 min" else "45-55 min",
                difficulty = if (isRest) "Low" else "High",
                exerciseCount = if (isRest) 4 else 5,
                isRestDay = isRest
            )
        }
    )

    fun getWorkoutForDay(dayNumber: Int): ThirtyDayWorkout {
        val safeDay = dayNumber.coerceIn(1, 30)
        val cycleDay = ((safeDay - 1) % 7) + 1
        val workoutId = "hypertrophy_day_$safeDay"

        return when (cycleDay) {
            1 -> createChestTricepsWorkout(safeDay, workoutId)
            2 -> createBackBicepsWorkout(safeDay, workoutId)
            3 -> createLegsWorkout(safeDay, workoutId)
            4 -> createShouldersTrapsWorkout(safeDay, workoutId)
            5 -> createChestBackWorkout(safeDay, workoutId)
            6 -> createArmsCoreWorkout(safeDay, workoutId)
            else -> createActiveRecoveryWorkout(safeDay, workoutId)
        }
    }

    private fun createChestTricepsWorkout(day: Int, id: String): ThirtyDayWorkout {
        return ThirtyDayWorkout(
            id = id,
            dayNumber = day,
            dayName = "Day $day",
            dayOfWeek = "Day $day",
            workoutTitle = "Chest + Triceps Hypertrophy",
            focus = "Chest, Triceps, Anterior Deltoids",
            duration = "48 min",
            difficulty = "Advanced",
            calories = "420 kcal",
            imageRes = Res.drawable.bodypart_chest,
            bodyPartId = "chest",
            workoutType = "gym",
            isRestDay = false,
            actionButtonText = "Start Hypertrophy",
            warmUp = listOf(
                WarmUpItem("w1", "Scapular Wall Slides", "2 x 12 reps", "Activate serratus anterior and lower traps."),
                WarmUpItem("w2", "Band Pull-Aparts", "2 x 15 reps", "Warm up rear delts and rotator cuffs."),
                WarmUpItem("w3", "Push-Up to Downward Dog", "2 x 10 reps", "Dynamic chest opener and core priming.")
            ),
            exercises = listOf(
                PlanExercise(
                    id = "hyp_incline_db_press",
                    name = "Incline Dumbbell Bench Press",
                    sets = "4 Sets",
                    reps = "8-10 Reps",
                    rest = "75s",
                    equipment = "Incline Bench, Dumbbells",
                    targetMuscle = "Upper Pectoralis Major",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    instructions = "Set bench to 30 degrees. Retract shoulder blades, press bells upwards with a slight arc.",
                    imageRes = Res.drawable.workout_incline_bench
                ),
                PlanExercise(
                    id = "hyp_flat_bb_bench",
                    name = "Flat Barbell Bench Press",
                    sets = "4 Sets",
                    reps = "8-10 Reps",
                    rest = "90s",
                    equipment = "Barbell, Flat Bench, Rack",
                    targetMuscle = "Mid Pectoralis Major",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    instructions = "Plant feet firmly. Lower bar with controlled 3-second eccentric to mid-sternum, press explosively.",
                    imageRes = Res.drawable.workout_gym
                ),
                PlanExercise(
                    id = "hyp_cable_fly",
                    name = "Standing Cable Flyes",
                    sets = "3 Sets",
                    reps = "12-15 Reps",
                    rest = "60s",
                    equipment = "Cable Machine",
                    targetMuscle = "Inner Pectoralis & Sternal Head",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    instructions = "Slight bend in elbows. Bring handles together in front with a hard 2-second peak contraction.",
                    imageRes = Res.drawable.bodypart_chest
                ),
                PlanExercise(
                    id = "hyp_skullcrusher",
                    name = "EZ-Bar Skull Crushers",
                    sets = "3 Sets",
                    reps = "10-12 Reps",
                    rest = "60s",
                    equipment = "EZ-Curl Bar, Bench",
                    targetMuscle = "Triceps (Long & Medial Heads)",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    instructions = "Keep upper arms perpendicular to torso. Lower bar towards forehead, extend through elbows.",
                    imageRes = Res.drawable.workout_incline_bench
                ),
                PlanExercise(
                    id = "hyp_rope_pushdown",
                    name = "Cable Rope Triceps Pushdown",
                    sets = "3 Sets",
                    reps = "12-15 Reps",
                    rest = "60s",
                    equipment = "Cable Machine, Rope Attachment",
                    targetMuscle = "Triceps (Lateral Head)",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    instructions = "Pin elbows to ribs. Flare rope outwards at the bottom of the contraction.",
                    imageRes = Res.drawable.workout_gym
                )
            ),
            coolDown = listOf(
                CoolDownItem("c1", "Doorway Chest Stretch", "45s per side", "Gently open anterior chest and front delts."),
                CoolDownItem("c2", "Overhead Triceps Stretch", "40s per side", "Lengthen long head of the triceps."),
                CoolDownItem("c3", "Child's Pose Breathing", "60s", "Deep diaphragmatic breathing to promote parasympathetic state.")
            )
        )
    }

    private fun createBackBicepsWorkout(day: Int, id: String): ThirtyDayWorkout {
        return ThirtyDayWorkout(
            id = id,
            dayNumber = day,
            dayName = "Day $day",
            dayOfWeek = "Day $day",
            workoutTitle = "Back + Biceps Hypertrophy",
            focus = "Latissimus Dorsi, Rhomboids, Biceps",
            duration = "50 min",
            difficulty = "Advanced",
            calories = "450 kcal",
            imageRes = Res.drawable.bodypart_back,
            bodyPartId = "back",
            workoutType = "gym",
            isRestDay = false,
            actionButtonText = "Start Hypertrophy",
            warmUp = listOf(
                WarmUpItem("w1", "Cat-Cow Dynamic Stretch", "2 x 10 reps", "Mobilize thoracic and lumbar spine."),
                WarmUpItem("w2", "Dead Hangs", "2 x 30s", "Decompress spinal column and stretch lat attachments."),
                WarmUpItem("w3", "Banded Face Pulls", "2 x 15 reps", "Activate rear delts and external rotators.")
            ),
            exercises = listOf(
                PlanExercise(
                    id = "hyp_bb_row",
                    name = "Overhand Barbell Row",
                    sets = "4 Sets",
                    reps = "8-10 Reps",
                    rest = "90s",
                    equipment = "Barbell, Weight Plates",
                    targetMuscle = "Latissimus Dorsi, Mid-Back",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    instructions = "Hinge at hips to 45 degrees. Pull bar towards lower sternum, squeezing scapulae.",
                    imageRes = Res.drawable.bodypart_back
                ),
                PlanExercise(
                    id = "hyp_lat_pulldown",
                    name = "Wide-Grip Lat Pulldown",
                    sets = "4 Sets",
                    reps = "10-12 Reps",
                    rest = "75s",
                    equipment = "Cable Lat Pulldown Machine",
                    targetMuscle = "Lats (Upper Width)",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    instructions = "Lean back slightly. Drive elbows downward towards hips without excessive momentum.",
                    imageRes = Res.drawable.workout_gym
                ),
                PlanExercise(
                    id = "hyp_seated_cable_row",
                    name = "Seated Neutral Cable Row",
                    sets = "3 Sets",
                    reps = "10-12 Reps",
                    rest = "60s",
                    equipment = "Cable Row Station, V-Bar",
                    targetMuscle = "Rhomboids, Lower Lats",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    instructions = "Chest proud. Pull V-bar towards belly button, pausing 1 second in peak contraction.",
                    imageRes = Res.drawable.bodypart_back
                ),
                PlanExercise(
                    id = "hyp_incline_db_curl",
                    name = "Incline Dumbbell Biceps Curl",
                    sets = "3 Sets",
                    reps = "10-12 Reps",
                    rest = "60s",
                    equipment = "Incline Bench, Dumbbells",
                    targetMuscle = "Biceps (Long Head Stretch)",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    instructions = "Sit back on 45-degree bench. Let arms hang into deep stretch, curl without moving elbows forward.",
                    imageRes = Res.drawable.workout_incline_bench
                ),
                PlanExercise(
                    id = "hyp_hammer_curl",
                    name = "Cross-Body Hammer Curl",
                    sets = "3 Sets",
                    reps = "12 Reps",
                    rest = "60s",
                    equipment = "Dumbbells",
                    targetMuscle = "Brachialis & Brachioradialis",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    instructions = "Neutral grip throughout. Curl bell across front torso to maximize arm thickness.",
                    imageRes = Res.drawable.workout_gym
                )
            ),
            coolDown = listOf(
                CoolDownItem("c1", "Lat Hang Stretch", "45s", "Stretch lats and open thoracic cavity."),
                CoolDownItem("c2", "Biceps Wall Stretch", "40s per side", "Lengthen biceps tendon and anterior shoulder."),
                CoolDownItem("c3", "Puppy Pose Thoracic Stretch", "60s", "Restore thoracic extension.")
            )
        )
    }

    private fun createLegsWorkout(day: Int, id: String): ThirtyDayWorkout {
        return ThirtyDayWorkout(
            id = id,
            dayNumber = day,
            dayName = "Day $day",
            dayOfWeek = "Day $day",
            workoutTitle = "Quads + Hamstrings Hypertrophy",
            focus = "Quadriceps, Hamstrings, Glutes, Calves",
            duration = "52 min",
            difficulty = "Advanced",
            calories = "480 kcal",
            imageRes = Res.drawable.workout_gym,
            bodyPartId = "legs",
            workoutType = "gym",
            isRestDay = false,
            actionButtonText = "Start Hypertrophy",
            warmUp = listOf(
                WarmUpItem("w1", "World's Greatest Stretch", "2 x 6 reps/side", "Mobilize hips, thoracic spine, and ankles."),
                WarmUpItem("w2", "Bodyweight Air Squats with Pause", "2 x 10 reps", "Prime quad tracking and hip drive."),
                WarmUpItem("w3", "Banded Glute Bridges", "2 x 15 reps", "Awaken gluteus medius and maximus.")
            ),
            exercises = listOf(
                PlanExercise(
                    id = "hyp_bb_squat",
                    name = "Barbell Back Squat",
                    sets = "4 Sets",
                    reps = "8-10 Reps",
                    rest = "90s",
                    equipment = "Squat Rack, Barbell",
                    targetMuscle = "Quadriceps, Glutes",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    instructions = "Brace core tightly. Break at hips and knees together, sink to parallel, drive through full foot.",
                    imageRes = Res.drawable.workout_gym
                ),
                PlanExercise(
                    id = "hyp_rdl",
                    name = "Dumbbell Romanian Deadlift",
                    sets = "4 Sets",
                    reps = "10-12 Reps",
                    rest = "75s",
                    equipment = "Dumbbells",
                    targetMuscle = "Hamstrings, Glute-Ham Tie-In",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    instructions = "Soft knees, push hips backward until deep hamstring stretch is felt, squeeze glutes to return.",
                    imageRes = Res.drawable.workout_gym
                ),
                PlanExercise(
                    id = "hyp_leg_press",
                    name = "Leg Press (Foot Placement Mid)",
                    sets = "3 Sets",
                    reps = "12-15 Reps",
                    rest = "75s",
                    equipment = "45-Degree Leg Press Machine",
                    targetMuscle = "Vastus Lateralis & Medialis",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    instructions = "Keep lower back flat against pad. Lower weight under control, press without locking knees.",
                    imageRes = Res.drawable.workout_gym
                ),
                PlanExercise(
                    id = "hyp_lying_leg_curl",
                    name = "Lying Hamstring Leg Curl",
                    sets = "3 Sets",
                    reps = "12-15 Reps",
                    rest = "60s",
                    equipment = "Hamstring Curl Machine",
                    targetMuscle = "Biceps Femoris",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    instructions = "Point toes slightly. Curl pad to touch hamstrings, resist on the downward portion for 3s.",
                    imageRes = Res.drawable.workout_gym
                ),
                PlanExercise(
                    id = "hyp_standing_calf_raise",
                    name = "Standing Calf Raise",
                    sets = "4 Sets",
                    reps = "15-20 Reps",
                    rest = "45s",
                    equipment = "Calf Machine or Step + Dumbbell",
                    targetMuscle = "Gastrocnemius & Soleus",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    instructions = "Full stretch at bottom for 1 second pause, press through big toes for full contraction.",
                    imageRes = Res.drawable.workout_gym
                )
            ),
            coolDown = listOf(
                CoolDownItem("c1", "Standing Quad Stretch", "45s per side", "Relax rectus femoris and hip flexors."),
                CoolDownItem("c2", "Seated Hamstring Stretch", "45s per side", "Lengthen posterior thigh musculature."),
                CoolDownItem("c3", "Pigeon Pose Glute Stretch", "60s per side", "Release glutes and piriformis.")
            )
        )
    }

    private fun createShouldersTrapsWorkout(day: Int, id: String): ThirtyDayWorkout {
        return ThirtyDayWorkout(
            id = id,
            dayNumber = day,
            dayName = "Day $day",
            dayOfWeek = "Day $day",
            workoutTitle = "Shoulders + Traps Hypertrophy",
            focus = "Lateral Deltoids, Posterior Deltoids, Trapezius",
            duration = "46 min",
            difficulty = "Advanced",
            calories = "400 kcal",
            imageRes = Res.drawable.workout_incline_bench,
            bodyPartId = "shoulders",
            workoutType = "gym",
            isRestDay = false,
            actionButtonText = "Start Hypertrophy",
            warmUp = listOf(
                WarmUpItem("w1", "Arm Circles & Huggers", "2 x 15 reps", "Increase synovial fluid in glenohumeral joint."),
                WarmUpItem("w2", "Cuban Rotations", "2 x 12 reps", "Warm up supraspinatus and infraspinatus."),
                WarmUpItem("w3", "Banded Overhead Y-Raises", "2 x 10 reps", "Activate lower trapezius.")
            ),
            exercises = listOf(
                PlanExercise(
                    id = "hyp_db_ohp",
                    name = "Seated Dumbbell Shoulder Press",
                    sets = "4 Sets",
                    reps = "8-10 Reps",
                    rest = "75s",
                    equipment = "Adjustable Bench, Dumbbells",
                    targetMuscle = "Anterior & Lateral Deltoids",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    instructions = "Press dumbbells directly overhead without arching lower back. Control descent to ear level.",
                    imageRes = Res.drawable.workout_incline_bench
                ),
                PlanExercise(
                    id = "hyp_lateral_raise",
                    name = "Standing Dumbbell Lateral Raise",
                    sets = "4 Sets",
                    reps = "12-15 Reps",
                    rest = "60s",
                    equipment = "Dumbbells",
                    targetMuscle = "Lateral Deltoid (Shoulder Cap)",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    instructions = "Slight forward torso tilt. Raise arms in scapular plane with elbows slightly leading.",
                    imageRes = Res.drawable.workout_gym
                ),
                PlanExercise(
                    id = "hyp_face_pull",
                    name = "Rope Cable Face Pull",
                    sets = "4 Sets",
                    reps = "15 Reps",
                    rest = "60s",
                    equipment = "Cable Machine, Rope",
                    targetMuscle = "Posterior Deltoid & External Rotators",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    instructions = "Set cable at eye level. Pull towards nose while externally rotating hands backward.",
                    imageRes = Res.drawable.workout_gym
                ),
                PlanExercise(
                    id = "hyp_db_shrug",
                    name = "Dumbbell Shrug with Pause",
                    sets = "3 Sets",
                    reps = "12-15 Reps",
                    rest = "60s",
                    equipment = "Heavy Dumbbells",
                    targetMuscle = "Upper Trapezius",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    instructions = "Elevate shoulders straight towards ears. Hold peak contraction for 2 solid seconds.",
                    imageRes = Res.drawable.workout_gym
                ),
                PlanExercise(
                    id = "hyp_reverse_pec_deck",
                    name = "Reverse Pec Deck Flye",
                    sets = "3 Sets",
                    reps = "15 Reps",
                    rest = "60s",
                    equipment = "Rear Delt Machine",
                    targetMuscle = "Posterior Deltoids & Infraspinatus",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    instructions = "Keep elbows high and soft. Sweep handles back squeezing upper back.",
                    imageRes = Res.drawable.workout_gym
                )
            ),
            coolDown = listOf(
                CoolDownItem("c1", "Cross-Body Shoulder Stretch", "45s per side", "Lengthen posterior shoulder capsule."),
                CoolDownItem("c2", "Upper Trap Neck Stretch", "40s per side", "Gently ease cervical tension."),
                CoolDownItem("c3", "Eagle Arms Pose", "30s per side", "Stretch rhomboids and rear delts.")
            )
        )
    }

    private fun createChestBackWorkout(day: Int, id: String): ThirtyDayWorkout {
        return ThirtyDayWorkout(
            id = id,
            dayNumber = day,
            dayName = "Day $day",
            dayOfWeek = "Day $day",
            workoutTitle = "Chest + Back Antagonist Hypertrophy",
            focus = "Pectoralis Major, Latissimus Dorsi, Rhomboids",
            duration = "52 min",
            difficulty = "Advanced",
            calories = "460 kcal",
            imageRes = Res.drawable.bodypart_chest,
            bodyPartId = "chest",
            workoutType = "gym",
            isRestDay = false,
            actionButtonText = "Start Hypertrophy",
            warmUp = listOf(
                WarmUpItem("w1", "Thoracic Spine Foam Rolling", "2 min", "Mobilize thoracic extension for benching and rowing."),
                WarmUpItem("w2", "Band Overheads & Dislocates", "2 x 10 reps", "Shoulder mobility and chest activation."),
                WarmUpItem("w3", "Isometric Push-Up Hold", "2 x 20s", "Activate core and pectoralis.")
            ),
            exercises = listOf(
                PlanExercise(
                    id = "hyp_tbar_row",
                    name = "T-Bar Row (Chest Supported)",
                    sets = "4 Sets",
                    reps = "8-10 Reps",
                    rest = "75s",
                    equipment = "T-Bar Row Machine / Landmine",
                    targetMuscle = "Mid-Back, Latissimus Dorsi",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    instructions = "Pull handles towards sternum without jerking torso. Squeeze back at top.",
                    imageRes = Res.drawable.bodypart_back
                ),
                PlanExercise(
                    id = "hyp_incline_smith_bench",
                    name = "Incline Smith Machine Press",
                    sets = "4 Sets",
                    reps = "8-10 Reps",
                    rest = "75s",
                    equipment = "Smith Machine, Incline Bench",
                    targetMuscle = "Upper Chest Clavicular Head",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    instructions = "Smooth constant tension reps. Lower to clavicle, press to 95% extension.",
                    imageRes = Res.drawable.workout_incline_bench
                ),
                PlanExercise(
                    id = "hyp_close_grip_pulldown",
                    name = "Neutral Close-Grip Lat Pulldown",
                    sets = "3 Sets",
                    reps = "10-12 Reps",
                    rest = "60s",
                    equipment = "Lat Machine, Double-D Handle",
                    targetMuscle = "Lower Lats & Teres Major",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    instructions = "Drive elbows down and back, arch chest into bar at bottom.",
                    imageRes = Res.drawable.bodypart_back
                ),
                PlanExercise(
                    id = "hyp_cable_crossover",
                    name = "High-to-Low Cable Crossover",
                    sets = "3 Sets",
                    reps = "12-15 Reps",
                    rest = "60s",
                    equipment = "Dual Cable Pulley",
                    targetMuscle = "Lower Pectoralis Major",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    instructions = "Cross hands over at hips for maximum contraction of lower pectorals.",
                    imageRes = Res.drawable.bodypart_chest
                ),
                PlanExercise(
                    id = "hyp_db_pullover",
                    name = "Dumbbell Cross-Bench Pullover",
                    sets = "3 Sets",
                    reps = "12 Reps",
                    rest = "60s",
                    equipment = "Flat Bench, Heavy Dumbbell",
                    targetMuscle = "Serratus Anterior & Lats",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    instructions = "Keep hips low. Lower bell in arc behind head for deep rib cage expansion.",
                    imageRes = Res.drawable.workout_gym
                )
            ),
            coolDown = listOf(
                CoolDownItem("c1", "Lat Wall Stretch", "45s per side", "Lengthen latissimus dorsi fibers."),
                CoolDownItem("c2", "Chest Opener on Foam Roller", "90s", "Lay along roller spine, allow arms to spread wide."),
                CoolDownItem("c3", "Cat-Camel Decompression", "60s", "Gentle spinal flexion and extension.")
            )
        )
    }

    private fun createArmsCoreWorkout(day: Int, id: String): ThirtyDayWorkout {
        return ThirtyDayWorkout(
            id = id,
            dayNumber = day,
            dayName = "Day $day",
            dayOfWeek = "Day $day",
            workoutTitle = "Arms + Core Hypertrophy",
            focus = "Biceps, Triceps, Rectus Abdominis, Obliques",
            duration = "45 min",
            difficulty = "Advanced",
            calories = "390 kcal",
            imageRes = Res.drawable.workout_incline_bench,
            bodyPartId = "arms",
            workoutType = "gym",
            isRestDay = false,
            actionButtonText = "Start Hypertrophy",
            warmUp = listOf(
                WarmUpItem("w1", "Wrist Flexor & Extensor Mobility", "2 min", "Prepare wrists and elbows for high curl and press volume."),
                WarmUpItem("w2", "Inchworm with Plank", "2 x 6 reps", "Activate core anterior chain."),
                WarmUpItem("w3", "Banded Biceps & Triceps Blood Flow", "2 x 20 reps", "Pump synovial fluids into joints.")
            ),
            exercises = listOf(
                PlanExercise(
                    id = "hyp_preacher_curl",
                    name = "EZ-Bar Preacher Curl",
                    sets = "4 Sets",
                    reps = "10-12 Reps",
                    rest = "60s",
                    equipment = "Preacher Bench, EZ-Bar",
                    targetMuscle = "Biceps Short Head (Peak)",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    instructions = "Isolate biceps against angled pad. Stop just short of vertical to maintain tension.",
                    imageRes = Res.drawable.workout_incline_bench
                ),
                PlanExercise(
                    id = "hyp_overhead_cable_triceps",
                    name = "Overhead Cable Rope Triceps Extension",
                    sets = "4 Sets",
                    reps = "10-12 Reps",
                    rest = "60s",
                    equipment = "Cable Machine, Rope",
                    targetMuscle = "Triceps Long Head",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    instructions = "Step forward in split stance. Extend arms overhead, flaring rope at lockout.",
                    imageRes = Res.drawable.workout_gym
                ),
                PlanExercise(
                    id = "hyp_concentration_curl",
                    name = "Seated Dumbbell Concentration Curl",
                    sets = "3 Sets",
                    reps = "12 Reps",
                    rest = "45s",
                    equipment = "Flat Bench, Dumbbell",
                    targetMuscle = "Biceps Brachii",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    instructions = "Brace triceps against inner thigh. Curl upward with zero swinging.",
                    imageRes = Res.drawable.workout_incline_bench
                ),
                PlanExercise(
                    id = "hyp_hanging_leg_raise",
                    name = "Hanging Knee/Leg Raise",
                    sets = "3 Sets",
                    reps = "12-15 Reps",
                    rest = "45s",
                    equipment = "Pull-Up Bar",
                    targetMuscle = "Lower Rectus Abdominis",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    instructions = "Roll pelvis upward toward ribs rather than merely swinging legs.",
                    imageRes = Res.drawable.workout_gym
                ),
                PlanExercise(
                    id = "hyp_cable_woodchopper",
                    name = "Diagonal Cable Woodchoppers",
                    sets = "3 Sets",
                    reps = "15 Reps/Side",
                    rest = "45s",
                    equipment = "Cable Pulley, Handle",
                    targetMuscle = "Internal & External Obliques",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    instructions = "Pivot back foot, rotate torso with straight arms across body.",
                    imageRes = Res.drawable.workout_gym
                )
            ),
            coolDown = listOf(
                CoolDownItem("c1", "Biceps Wall Extension", "40s per side", "Deep stretch across biceps tendon."),
                CoolDownItem("c2", "Cobra Pose Abdominal Stretch", "60s", "Lengthen rectus abdominis."),
                CoolDownItem("c3", "Child's Pose", "60s", "Decompress lower back.")
            )
        )
    }

    private fun createActiveRecoveryWorkout(day: Int, id: String): ThirtyDayWorkout {
        return ThirtyDayWorkout(
            id = id,
            dayNumber = day,
            dayName = "Day $day",
            dayOfWeek = "Day $day",
            workoutTitle = "Active Recovery & Hypertrophy Mobility",
            focus = "Myofascial Release, Joint Decompression, Flexibility",
            duration = "22 min",
            difficulty = "Low",
            calories = "120 kcal",
            imageRes = Res.drawable.workout_home,
            bodyPartId = "recovery",
            workoutType = "home",
            isRestDay = true,
            actionButtonText = "Start Recovery Flow",
            warmUp = listOf(
                WarmUpItem("w1", "Diaphragmatic Breathing", "2 min", "Down-regulate autonomic nervous system.")
            ),
            exercises = listOf(
                PlanExercise(
                    id = "hyp_foam_roll_lats",
                    name = "Foam Rolling Lats & Upper Back",
                    sets = "2 Sets",
                    reps = "60s",
                    rest = "30s",
                    equipment = "Foam Roller",
                    targetMuscle = "Thoracic Extensors & Lats",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    instructions = "Slowly roll along lateral borders of back, pausing on trigger points.",
                    imageRes = Res.drawable.workout_home
                ),
                PlanExercise(
                    id = "hyp_90_90_hips",
                    name = "90/90 Hip Mobility Rotations",
                    sets = "2 Sets",
                    reps = "10 Reps/Side",
                    rest = "30s",
                    equipment = "Mat",
                    targetMuscle = "Hip Internal & External Rotators",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    instructions = "Sit upright with knees at 90 degrees. Transition smoothly side to side.",
                    imageRes = Res.drawable.workout_home
                ),
                PlanExercise(
                    id = "hyp_thoracic_opener",
                    name = "Thread the Needle Thoracic Openers",
                    sets = "2 Sets",
                    reps = "8 Reps/Side",
                    rest = "30s",
                    equipment = "Mat",
                    targetMuscle = "Mid-Back & Rear Shoulders",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    instructions = "From quadruped position, slide arm underneath chest, rotating torso with deep exhale.",
                    imageRes = Res.drawable.workout_home
                ),
                PlanExercise(
                    id = "hyp_couch_stretch",
                    name = "Couch Stretch Hip Flexor Flow",
                    sets = "2 Sets",
                    reps = "45s/Side",
                    rest = "30s",
                    equipment = "Mat / Wall",
                    targetMuscle = "Psoas & Rectus Femoris",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    instructions = "Rear shin vertical against wall, squeeze glute of back leg to open front hip.",
                    imageRes = Res.drawable.workout_home
                )
            ),
            coolDown = listOf(
                CoolDownItem("c1", "Corpse Pose Meditation", "3 min", "Total relaxation and systemic recovery.")
            )
        )
    }
}
