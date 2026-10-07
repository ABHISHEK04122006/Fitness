package com.example.fitness.feature.programs.data

import com.example.fitness.feature.programs.model.ProgramScheduleItem
import com.example.fitness.feature.programs.model.WorkoutProgram
import com.example.fitness.feature.workout.model.CoolDownItem
import com.example.fitness.feature.workout.model.PlanExercise
import com.example.fitness.feature.workout.model.ThirtyDayWorkout
import com.example.fitness.feature.workout.model.WarmUpItem
import fitness.shared.generated.resources.Res
import fitness.shared.generated.resources.goal_athlete
import fitness.shared.generated.resources.program_endurance
import fitness.shared.generated.resources.program_speed
import fitness.shared.generated.resources.workout_home

object AthletePerformanceProgramData {

    val program = WorkoutProgram(
        id = "athlete",
        title = "Athlete Performance",
        subtitle = "Speed, agility, power, conditioning and athletic mobility.",
        description = "A specialized performance training mode designed for elite athletes and multi-sport competitors. Unlike traditional bodybuilding splits, this program organizes training around functional athletic attributes: Sprinting mechanics, Multi-directional Agility, Explosive Power, Energy System Conditioning, Aerobic Endurance, and Joint Mobility.",
        duration = "30 Days",
        goal = "Athletic Performance & Functional Power",
        difficulty = "All Levels / Intermediate",
        workoutsCount = 30,
        focus = "Sprinting, Agility, Explosive Power, Conditioning, Endurance, Mobility",
        category = "Performance",
        imageRes = Res.drawable.goal_athlete,
        schedule = (1..30).map { day ->
            val cycleDay = ((day - 1) % 7) + 1
            val (title, focus, isRest) = when (cycleDay) {
                1 -> Triple("Sprinting & Acceleration", "Sprint Mechanics, First-Step Quickness, Drive Phase", false)
                2 -> Triple("Agility & Change of Direction", "Deceleration, Lateral Footwork, Cutting Angles", false)
                3 -> Triple("Explosive Power & Plyometrics", "Rate of Force Development, Elastic Recoil, Vertical Jump", false)
                4 -> Triple("Conditioning & Work Capacity", "Metabolic Engine, High-Intensity Capacity, Lactic Tolerance", false)
                5 -> Triple("Aerobic Endurance & Stamina", "VO2 Max, Sustained Pace, Cardiovascular Efficiency", false)
                6 -> Triple("Athletic Mobility & Durability", "Multi-Planar Hip Health, Thoracic Rotation, Ankle Flexion", false)
                else -> Triple("Regeneration & Athletic Recovery", "Tissue Restoration, CNS Down-Regulation", true)
            }
            ProgramScheduleItem(
                dayNumber = day,
                dayTitle = title,
                focus = focus,
                workoutId = "athlete_day_$day",
                duration = if (isRest) "20 min" else "40-50 min",
                difficulty = if (isRest) "Low" else "High",
                exerciseCount = if (isRest) 4 else 5,
                isRestDay = isRest
            )
        }
    )

    fun getWorkoutForDay(dayNumber: Int): ThirtyDayWorkout {
        val safeDay = dayNumber.coerceIn(1, 30)
        val cycleDay = ((safeDay - 1) % 7) + 1
        val workoutId = "athlete_day_$safeDay"

        return when (cycleDay) {
            1 -> createSprintingWorkout(safeDay, workoutId)
            2 -> createAgilityWorkout(safeDay, workoutId)
            3 -> createExplosivePowerWorkout(safeDay, workoutId)
            4 -> createConditioningWorkout(safeDay, workoutId)
            5 -> createEnduranceWorkout(safeDay, workoutId)
            6 -> createMobilityWorkout(safeDay, workoutId)
            else -> createRegenerationWorkout(safeDay, workoutId)
        }
    }

    private fun createSprintingWorkout(day: Int, id: String): ThirtyDayWorkout {
        return ThirtyDayWorkout(
            id = id,
            dayNumber = day,
            dayName = "Day $day",
            dayOfWeek = "Day $day",
            workoutTitle = "Sprinting & Acceleration Mechanics",
            focus = "Sprint Mechanics, Drive Angle, Stride Frequency",
            duration = "42 min",
            difficulty = "Advanced",
            calories = "410 kcal",
            imageRes = Res.drawable.program_speed,
            bodyPartId = "athlete_sprint",
            workoutType = "gym",
            isRestDay = false,
            actionButtonText = "Start Sprint Training",
            warmUp = listOf(
                WarmUpItem("ath_sw1", "High Knee A-Skips", "2 x 20m", "Groove proper knee drive and dorsiflexion."),
                WarmUpItem("ath_sw2", "B-Skips with Foot Paw", "2 x 20m", "Active cyclical foot strike under hips."),
                WarmUpItem("ath_sw3", "Ankle Pogo Hops", "2 x 20 hops", "Stiffen Achilles tendon for ground reaction force.")
            ),
            exercises = listOf(
                PlanExercise(
                    id = "ath_wall_drill",
                    name = "Posture Wall March & Sprint Drill",
                    sets = "4 Sets",
                    reps = "10 Reps/Leg",
                    rest = "60s",
                    equipment = "Wall / Sturdy Post",
                    targetMuscle = "Hip Flexors, Core Anti-Extension, Stride Mechanics",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    instructions = "Assume 45-degree angle against wall. Drive knee high with toe dorsiflexed, strike down aggressively.",
                    imageRes = Res.drawable.program_speed
                ),
                PlanExercise(
                    id = "ath_10m_accel",
                    name = "10-Meter Staggered Stance Acceleration Starts",
                    sets = "5 Sets",
                    reps = "10m Sprint",
                    rest = "90s",
                    equipment = "Open Turf / Track",
                    targetMuscle = "Quad Drive, Glute Max Power, First-Step Quickness",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    instructions = "Load front leg 70%. Explode out low, keeping shin angles forward for first 4 steps.",
                    imageRes = Res.drawable.program_speed
                ),
                PlanExercise(
                    id = "ath_flying_30s",
                    name = "Flying 30-Meter Max Velocity Sprints",
                    sets = "4 Sets",
                    reps = "30m at 95%",
                    rest = "120s",
                    equipment = "Running Track / Field",
                    targetMuscle = "Top-Speed Stride Frequency & Hamstring Elasticity",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    instructions = "Gradual build-up for 15m, then hit top speed through the 30m timing zone standing tall.",
                    imageRes = Res.drawable.goal_athlete
                ),
                PlanExercise(
                    id = "ath_bounding",
                    name = "Alternate Leg Power Bounding",
                    sets = "3 Sets",
                    reps = "25 Meters",
                    rest = "90s",
                    equipment = "Turf / Track",
                    targetMuscle = "Unilateral Hip Extension & Horizontal Power",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    instructions = "Leap horizontally from one foot to the other with long hang time and high knee drive.",
                    imageRes = Res.drawable.program_speed
                ),
                PlanExercise(
                    id = "ath_nordic_curl",
                    name = "Nordic Hamstring Curl Negatives",
                    sets = "3 Sets",
                    reps = "5 Slow Negatives",
                    rest = "90s",
                    equipment = "Ankle Anchor / Partner, Mat",
                    targetMuscle = "Eccentric Hamstring Injury Prevention",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    instructions = "Keep hips locked straight. Lower torso towards ground as slowly as possible using hamstrings.",
                    imageRes = Res.drawable.workout_home
                )
            ),
            coolDown = listOf(
                CoolDownItem("ath_sc1", "Walking Hamstring Scoop", "40s per leg", "Active lengthening of posterior chain."),
                CoolDownItem("ath_sc2", "Standing Calf & Achilles Stretch", "45s per leg", "Relieve gastrocnemius tension."),
                CoolDownItem("ath_sc3", "Elevated Hip Flexor Stretch", "60s per leg", "Open psoas and quad.")
            )
        )
    }

    private fun createAgilityWorkout(day: Int, id: String): ThirtyDayWorkout {
        return ThirtyDayWorkout(
            id = id,
            dayNumber = day,
            dayName = "Day $day",
            dayOfWeek = "Day $day",
            workoutTitle = "Agility & Change of Direction",
            focus = "Deceleration, Lateral Footwork, Cutting Mechanics",
            duration = "44 min",
            difficulty = "Intermediate",
            calories = "420 kcal",
            imageRes = Res.drawable.program_speed,
            bodyPartId = "athlete_agility",
            workoutType = "gym",
            isRestDay = false,
            actionButtonText = "Start Agility Training",
            warmUp = listOf(
                WarmUpItem("ath_aw1", "Lateral Shuffles with Arm Swing", "2 x 20m/side", "Warm up groin and lateral stabilizers."),
                WarmUpItem("ath_aw2", "Carioca Hip Swivel", "2 x 20m/side", "Mobilize hips in transverse plane."),
                WarmUpItem("ath_aw3", "Drop Squat Reactive Deceleration", "2 x 8 reps", "Train rapid center-of-mass drop.")
            ),
            exercises = listOf(
                PlanExercise(
                    id = "ath_pro_agility",
                    name = "5-10-5 Pro Agility Shuttle",
                    sets = "5 Sets",
                    reps = "1 Run/Side",
                    rest = "90s",
                    equipment = "3 Cones (5 yards apart)",
                    targetMuscle = "Lateral Change of Direction, Hip Sinks",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    instructions = "Start in 3-point stance. Sprint 5 yards right, touch line, sprint 10 yards left, sprint 5 yards through center.",
                    imageRes = Res.drawable.program_speed
                ),
                PlanExercise(
                    id = "ath_ladder_ickey",
                    name = "Agility Ladder Ickey Shuffle & In-Outs",
                    sets = "4 Sets",
                    reps = "4 Passes Through",
                    rest = "60s",
                    equipment = "Speed Ladder",
                    targetMuscle = "Footwork Cadence, Tibialis, Ankle Stability",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    instructions = "In-in-out rhythm. Stay on balls of feet with light, silent ground contacts.",
                    imageRes = Res.drawable.goal_athlete
                ),
                PlanExercise(
                    id = "ath_t_drill",
                    name = "T-Drill Sprint, Shuffle & Backpedal",
                    sets = "4 Sets",
                    reps = "1 Rep/Direction",
                    rest = "75s",
                    equipment = "4 Cones",
                    targetMuscle = "Multi-Planar Transitions (Sagittal & Frontal)",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    instructions = "Sprint 10 yards forward, lateral shuffle 5 yards left, 10 yards right, 5 yards back to center, backpedal to start.",
                    imageRes = Res.drawable.program_speed
                ),
                PlanExercise(
                    id = "ath_cone_slalom",
                    name = "Z-Pattern Cone Cutting Drills",
                    sets = "4 Sets",
                    reps = "3 Reps",
                    rest = "60s",
                    equipment = "6 Cones in Zig-Zag",
                    targetMuscle = "Sharp Cutting Angles & Outside Foot Plant",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    instructions = "Plant hard on outside foot, drop hips, drive out at 45 degrees towards next cone.",
                    imageRes = Res.drawable.goal_athlete
                ),
                PlanExercise(
                    id = "ath_lateral_bound",
                    name = "Heiden / Lateral Speed Skater Hops",
                    sets = "3 Sets",
                    reps = "10 Hops/Side",
                    rest = "60s",
                    equipment = "Turf / Mat",
                    targetMuscle = "Glute Medius Lateral Power & Single-Leg Balance",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    instructions = "Push laterally off outside foot, land softly absorbing force on opposite leg, hold 1s before bounding back.",
                    imageRes = Res.drawable.workout_home
                )
            ),
            coolDown = listOf(
                CoolDownItem("ath_ac1", "Frog Pose Groin Stretch", "60s", "Deep adductor release after lateral cutting."),
                CoolDownItem("ath_ac2", "Kneeling Ankle Mobilizer", "45s per side", "Restore talocrural dorsiflexion."),
                CoolDownItem("ath_ac3", "Supine Butterfly Stretch", "90s", "Relax pelvic floor and inner hips.")
            )
        )
    }

    private fun createExplosivePowerWorkout(day: Int, id: String): ThirtyDayWorkout {
        return ThirtyDayWorkout(
            id = id,
            dayNumber = day,
            dayName = "Day $day",
            dayOfWeek = "Day $day",
            workoutTitle = "Explosive Power & Plyometrics",
            focus = "Rate of Force Development, Vertical Jump, Triple Extension",
            duration = "45 min",
            difficulty = "Advanced",
            calories = "440 kcal",
            imageRes = Res.drawable.goal_athlete,
            bodyPartId = "athlete_power",
            workoutType = "gym",
            isRestDay = false,
            actionButtonText = "Start Power Session",
            warmUp = listOf(
                WarmUpItem("ath_pw1", "Glute Bridge Pulses", "2 x 15 reps", "Activate posterior hip engine."),
                WarmUpItem("ath_pw2", "Countermovement Jump Swings", "2 x 6 reps", "Prime arm swing synchrony."),
                WarmUpItem("ath_pw3", "Snap Downs to Athletic Stance", "2 x 8 reps", "Teach rapid braking force.")
            ),
            exercises = listOf(
                PlanExercise(
                    id = "ath_box_jump",
                    name = "Non-Countermovement Box Jump",
                    sets = "5 Sets",
                    reps = "4 Reps",
                    rest = "90s",
                    equipment = "Plyo Box (24-30 inch)",
                    targetMuscle = "Starting Power & Vertical Displacement",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    instructions = "Start in deep athletic quarter-squat. Explode up to box without countermovement, land softly like a ninja.",
                    imageRes = Res.drawable.goal_athlete
                ),
                PlanExercise(
                    id = "ath_broad_jump",
                    name = "Consecutive Double Broad Jumps",
                    sets = "4 Sets",
                    reps = "3 Pairs",
                    rest = "90s",
                    equipment = "Turf Runway",
                    targetMuscle = "Horizontal Triple Extension & Stretch Shortening Cycle",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    instructions = "Jump for maximal horizontal distance, touch ground and immediately rebound into second jump.",
                    imageRes = Res.drawable.goal_athlete
                ),
                PlanExercise(
                    id = "ath_medball_slam",
                    name = "Rotational Medicine Ball Wall Slams",
                    sets = "4 Sets",
                    reps = "8 Reps/Side",
                    rest = "60s",
                    equipment = "Medicine Ball (6-10kg), Concrete Wall",
                    targetMuscle = "Transverse Core Power & Hip Snap",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    instructions = "Stand perpendicular to wall. Coil into back hip and violently slam ball sideways into wall.",
                    imageRes = Res.drawable.workout_home
                ),
                PlanExercise(
                    id = "ath_kb_swing",
                    name = "Explosive Kettlebell Hardstyle Swings",
                    sets = "4 Sets",
                    reps = "10 Reps",
                    rest = "60s",
                    equipment = "Heavy Kettlebell (24-32kg)",
                    targetMuscle = "Hip Hinge Power & Gluteus Maximus",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    instructions = "Snap hips forward into plank. The kettlebell floats to chest level propelled solely by hips.",
                    imageRes = Res.drawable.goal_athlete
                ),
                PlanExercise(
                    id = "ath_plyo_pushup",
                    name = "Explosive Clap / Release Push-Ups",
                    sets = "3 Sets",
                    reps = "6 Reps",
                    rest = "60s",
                    equipment = "Mat",
                    targetMuscle = "Upper Body Explosive Force & Pecs",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    instructions = "Lower quickly and propel hands completely off ground with maximum push force.",
                    imageRes = Res.drawable.workout_home
                )
            ),
            coolDown = listOf(
                CoolDownItem("ath_pc1", "Couch Stretch", "45s per side", "Lengthen hip flexors."),
                CoolDownItem("ath_pc2", "Pigeon Pose", "60s per side", "Release glutes after explosive jumping."),
                CoolDownItem("ath_pc3", "Foam Roll Quadriceps", "2 min", "Clear lactic metabolic byproducts.")
            )
        )
    }

    private fun createConditioningWorkout(day: Int, id: String): ThirtyDayWorkout {
        return ThirtyDayWorkout(
            id = id,
            dayNumber = day,
            dayName = "Day $day",
            dayOfWeek = "Day $day",
            workoutTitle = "Conditioning & Work Capacity",
            focus = "Metabolic Conditioning, Lactic Tolerance, High Heart Rate Intervals",
            duration = "38 min",
            difficulty = "Advanced",
            calories = "470 kcal",
            imageRes = Res.drawable.program_endurance,
            bodyPartId = "athlete_conditioning",
            workoutType = "gym",
            isRestDay = false,
            actionButtonText = "Start Conditioning",
            warmUp = listOf(
                WarmUpItem("ath_cw1", "Jumping Jacks & Seal Jumps", "2 min", "Elevate core temperature and heart rate."),
                WarmUpItem("ath_cw2", "Inchworm with Push-Up", "2 x 6 reps", "Full body dynamic flow.")
            ),
            exercises = listOf(
                PlanExercise(
                    id = "ath_rower_intervals",
                    name = "Rowing Machine Sprint Intervals (30s on / 30s off)",
                    sets = "6 Sets",
                    reps = "30s Max Pace",
                    rest = "30s",
                    equipment = "Concept2 Rower / Ergometer",
                    targetMuscle = "Full Body Aerobic/Anaerobic Power",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    instructions = "Drive with legs, finish with back and arms at sub-1:35/500m split pace.",
                    imageRes = Res.drawable.program_endurance
                ),
                PlanExercise(
                    id = "ath_sled_push",
                    name = "Weighted Turf Sled Push / Prowler",
                    sets = "4 Sets",
                    reps = "25 Meters",
                    rest = "60s",
                    equipment = "Prowler Sled, Bumper Plates",
                    targetMuscle = "Quad Stamina, Calves, Cardiovascular Grunt",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    instructions = "Arms locked straight, drive knees high, push sled with continuous forward momentum.",
                    imageRes = Res.drawable.goal_athlete
                ),
                PlanExercise(
                    id = "ath_burpee_broad_jump",
                    name = "Burpee Broad Jump Combos",
                    sets = "4 Sets",
                    reps = "8 Reps",
                    rest = "60s",
                    equipment = "Turf / Mat",
                    targetMuscle = "Total Body Work Capacity",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    instructions = "Drop to chest on floor, snap feet under, and immediately jump forward for max distance.",
                    imageRes = Res.drawable.workout_home
                ),
                PlanExercise(
                    id = "ath_battle_ropes",
                    name = "Battle Rope Alternating Waves & Slams",
                    sets = "4 Sets",
                    reps = "30 Seconds",
                    rest = "45s",
                    equipment = "Battle Ropes",
                    targetMuscle = "Shoulder Endurance, Forearms, Core",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    instructions = "Quarter squat stance. Alternate arms rapidly creating continuous high waves to anchor.",
                    imageRes = Res.drawable.goal_athlete
                ),
                PlanExercise(
                    id = "ath_hollow_body",
                    name = "Hollow Body Rocking Hold",
                    sets = "3 Sets",
                    reps = "45 Seconds",
                    rest = "45s",
                    equipment = "Mat",
                    targetMuscle = "Gymnastic Core Stamina",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    instructions = "Press lower back completely into floor, arms and legs extended in crescent bowl shape.",
                    imageRes = Res.drawable.workout_home
                )
            ),
            coolDown = listOf(
                CoolDownItem("ath_cc1", "Slow Walking Cool-Down", "3 min", "Gradually lower heart rate below 110 bpm."),
                CoolDownItem("ath_cc2", "Forward Bend Spinal Decompression", "60s", "Lengthen spine."),
                CoolDownItem("ath_cc3", "Lying Butterfly Breathing", "2 min", "Restore parasympathetic equilibrium.")
            )
        )
    }

    private fun createEnduranceWorkout(day: Int, id: String): ThirtyDayWorkout {
        return ThirtyDayWorkout(
            id = id,
            dayNumber = day,
            dayName = "Day $day",
            dayOfWeek = "Day $day",
            workoutTitle = "Aerobic Endurance & Stamina",
            focus = "VO2 Max, Lactate Threshold, Cardiac Output",
            duration = "45 min",
            difficulty = "Intermediate",
            calories = "450 kcal",
            imageRes = Res.drawable.program_endurance,
            bodyPartId = "athlete_endurance",
            workoutType = "gym",
            isRestDay = false,
            actionButtonText = "Start Endurance Run",
            warmUp = listOf(
                WarmUpItem("ath_ew1", "Jogging Stride Prep", "5 min", "Low intensity warm-up jog."),
                WarmUpItem("ath_ew2", "Leg Swings (Front-to-Back & Lateral)", "2 x 10/leg", "Dynamic mobility for stride length.")
            ),
            exercises = listOf(
                PlanExercise(
                    id = "ath_tempo_intervals",
                    name = "Track / Treadmill 400m Intervals",
                    sets = "5 Sets",
                    reps = "400m at Zone 4 Pace",
                    rest = "90s Jog",
                    equipment = "Track / Treadmill",
                    targetMuscle = "Aerobic Power & Lactate Clearance",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    instructions = "Run at sustainable 85-90% maximum pace. Jog recoveries, do not sit down.",
                    imageRes = Res.drawable.program_endurance
                ),
                PlanExercise(
                    id = "ath_incline_stride",
                    name = "Treadmill Incline Power Walk & Jog",
                    sets = "3 Sets",
                    reps = "4 min at 8% Incline",
                    rest = "60s",
                    equipment = "Incline Treadmill",
                    targetMuscle = "Posterior Chain Endurance & Glute Drive",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    instructions = "Maintain upright posture on incline without holding onto side handrails.",
                    imageRes = Res.drawable.program_endurance
                ),
                PlanExercise(
                    id = "ath_step_ups",
                    name = "Alternating Box Step-Ups with Rhythm",
                    sets = "3 Sets",
                    reps = "15 Reps/Leg",
                    rest = "45s",
                    equipment = "Plyo Box (20 inch)",
                    targetMuscle = "Single-Leg Endurance & Hip Stability",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    instructions = "Step up through heel, fully extend standing hip, control down-step.",
                    imageRes = Res.drawable.goal_athlete
                ),
                PlanExercise(
                    id = "ath_plank_shoulder_tap",
                    name = "High Plank Shoulder Taps",
                    sets = "3 Sets",
                    reps = "20 Taps",
                    rest = "45s",
                    equipment = "Mat",
                    targetMuscle = "Anti-Rotational Core Endurance",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    instructions = "Keep hips as still as possible as opposite hand touches shoulder.",
                    imageRes = Res.drawable.workout_home
                )
            ),
            coolDown = listOf(
                CoolDownItem("ath_ec1", "Standing Quad & Hip Stretch", "60s per leg", "Flush quad fibers."),
                CoolDownItem("ath_ec2", "Downward Dog Ankle Pedals", "90s", "Lengthen Achilles and calf muscle complex."),
                CoolDownItem("ath_ec3", "Seated Wide Straddle Stretch", "2 min", "Release inner thighs.")
            )
        )
    }

    private fun createMobilityWorkout(day: Int, id: String): ThirtyDayWorkout {
        return ThirtyDayWorkout(
            id = id,
            dayNumber = day,
            dayName = "Day $day",
            dayOfWeek = "Day $day",
            workoutTitle = "Athletic Mobility & Durability",
            focus = "Joint Capsules, Multi-Planar Flexibility, Tissue Resilience",
            duration = "30 min",
            difficulty = "All Levels",
            calories = "160 kcal",
            imageRes = Res.drawable.workout_home,
            bodyPartId = "athlete_mobility",
            workoutType = "home",
            isRestDay = false,
            actionButtonText = "Start Mobility Flow",
            warmUp = listOf(
                WarmUpItem("ath_mw1", "Gentle Arm Circles & Neck Rolls", "2 min", "Lubricate upper kinetic chain.")
            ),
            exercises = listOf(
                PlanExercise(
                    id = "ath_worlds_greatest",
                    name = "World's Greatest Stretch Complex",
                    sets = "3 Sets",
                    reps = "6 Reps/Side",
                    rest = "30s",
                    equipment = "Mat",
                    targetMuscle = "Thoracic Rotation, Hips, Hamstrings",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    instructions = "Lunge forward, place elbow inside ankle, rotate chest to sky, rock back to hamstring stretch.",
                    imageRes = Res.drawable.workout_home
                ),
                PlanExercise(
                    id = "ath_90_90_flow",
                    name = "90/90 Hip Flow with Forward Lean",
                    sets = "3 Sets",
                    reps = "8 Rotations/Side",
                    rest = "30s",
                    equipment = "Mat",
                    targetMuscle = "Hip Capsule Internal & External Rotation",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    instructions = "Maintain tall posture. Fold over front knee to open glute, rotate smoothly to opposite side.",
                    imageRes = Res.drawable.workout_home
                ),
                PlanExercise(
                    id = "ath_ankle_dorsi",
                    name = "Half-Kneeling Ankle Dorsiflexion Mobilization",
                    sets = "2 Sets",
                    reps = "10 Reps/Ankle",
                    rest = "30s",
                    equipment = "Wall / Mat",
                    targetMuscle = "Talocrural Joint Mobility",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    instructions = "Drive knee forward past toes while keeping heel flat on the floor.",
                    imageRes = Res.drawable.workout_home
                ),
                PlanExercise(
                    id = "ath_thoracic_bridge",
                    name = "Tabletop Thoracic Bridge & Reach",
                    sets = "2 Sets",
                    reps = "6 Reps/Side",
                    rest = "30s",
                    equipment = "Mat",
                    targetMuscle = "Posterior Chain Activation & Anterior Openers",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    instructions = "Drive hips high into reverse tabletop, reach one arm across body behind head.",
                    imageRes = Res.drawable.workout_home
                )
            ),
            coolDown = listOf(
                CoolDownItem("ath_mc1", "Reclined Supine Twist", "60s per side", "Gently rotate spinal column."),
                CoolDownItem("ath_mc2", "Corpse Pose Deep Breathing", "3 min", "Relax into parasympathetic rest.")
            )
        )
    }

    private fun createRegenerationWorkout(day: Int, id: String): ThirtyDayWorkout {
        return ThirtyDayWorkout(
            id = id,
            dayNumber = day,
            dayName = "Day $day",
            dayOfWeek = "Day $day",
            workoutTitle = "Regeneration & Athletic Recovery",
            focus = "Myofascial Soft Tissue Restoration, Lymphatic Flush",
            duration = "20 min",
            difficulty = "Low",
            calories = "90 kcal",
            imageRes = Res.drawable.workout_home,
            bodyPartId = "recovery",
            workoutType = "home",
            isRestDay = true,
            actionButtonText = "Start Regeneration Flow",
            warmUp = listOf(
                WarmUpItem("ath_rw1", "Breath Focus (5s in, 5s out)", "3 min", "System down-regulation.")
            ),
            exercises = listOf(
                PlanExercise(
                    id = "ath_foam_roll_itb",
                    name = "Full Lower Body Foam Roll Session",
                    sets = "2 Sets",
                    reps = "60s per Zone",
                    rest = "30s",
                    equipment = "Foam Roller",
                    targetMuscle = "Quads, IT Band, Glutes, Calves",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    instructions = "Roll tender spots slowly, applying tolerable pressure to encourage blood flow.",
                    imageRes = Res.drawable.workout_home
                ),
                PlanExercise(
                    id = "ath_legs_up_wall",
                    name = "Legs-Up-The-Wall Inversion",
                    sets = "1 Set",
                    reps = "5 Minutes",
                    rest = "0s",
                    equipment = "Wall, Mat",
                    targetMuscle = "Venous Return & Lymphatic Drainage",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    instructions = "Lie on back with hips near wall, legs resting vertically. Breathe into diaphragm.",
                    imageRes = Res.drawable.workout_home
                )
            ),
            coolDown = listOf(
                CoolDownItem("ath_rc1", "Total Relaxation", "2 min", "Rest and recover for next cycle.")
            )
        )
    }
}
