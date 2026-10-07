package com.example.fitness.feature.workout.data

import com.example.fitness.createThreadSafeMap
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.admin.data.AdminExerciseCatalogOrganizer
import com.example.fitness.feature.admin.data.AdminWorkoutCatalogProvider
import com.example.fitness.feature.admin.data.ExerciseBodyPartSection
import com.example.fitness.feature.exercise.data.FullBodyExerciseLibrary
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.data.ThirtyDayExerciseLibrary
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.workout.model.BodyPartWorkout
import com.example.fitness.feature.workout.model.CustomExerciseConfig
import com.example.fitness.feature.workout.model.ExerciseItem
import com.example.fitness.feature.workout.model.ExperienceLevel
import com.example.fitness.feature.workout.model.WorkoutType
import fitness.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource

object WorkoutRepository {

    private val exercisesCache = createThreadSafeMap<String, List<ExerciseItem>>()

    fun clearExercisesCache() {
        exercisesCache.clear()
        cachedAllExercises = null
    }

    // Centralized repository - easily swappable with Firebase Firestore in the future
    private val gymWorkouts: List<BodyPartWorkout> = listOf(
        BodyPartWorkout(
            id = "chest",
            title = "Chest",
            subtitle = "Build strength & upper chest volume",
            bodyPart = "Chest",
            workoutType = WorkoutType.GYM,
            imageRes = Res.drawable.gym_bench_press,
            equipment = "Barbell, Dumbbells, Cables",
            duration = "45 min",
            difficulty = "Beginner",
            calories = "~360 kcal",
            howToPerform = "Keep your scapula retracted and back arched slightly. Lower the weight under control until your chest touches the bar, then drive upward explosively without flaring elbows.",
            exercises = listOf(
                ExerciseItem(
                    id = "gym_chest_1",
                    name = "Barbell Bench Press",
                    sets = "4 sets × 8-10 reps",
                    duration = "10 min",
                    description = "Foundational compound lift for overall chest mass, triceps, and anterior delts.",
                    imageRes = Res.drawable.gym_bench_press,
                    videoDemoUrl = "https://fitness.app/demo/bench_press.mp4",
                    tips = listOf("Tuck elbows at 45 degrees", "Drive through feet on the floor", "Control the 2-second negative")
                ),
                ExerciseItem(
                    id = "gym_chest_2",
                    name = "Incline Dumbbell Press",
                    sets = "3 sets × 12 reps",
                    duration = "8 min",
                    description = "Isolates the clavicular head of the pectorals with freedom of wrist rotation.",
                    imageRes = Res.drawable.gym_incline_dumbbell_press,
                    videoDemoUrl = "https://fitness.app/demo/incline_dumbbell.mp4",
                    tips = listOf("Bench angle at 30 degrees", "Squeeze at top without clanking dumbbells", "Full stretch at bottom")
                ),
                ExerciseItem(
                    id = "gym_chest_3",
                    name = "Cable Chest Fly",
                    sets = "3 sets × 15 reps",
                    duration = "7 min",
                    description = "Maintains constant tension through peak contraction of the inner chest fibers.",
                    imageRes = Res.drawable.gym_cable_chest_fly,
                    videoDemoUrl = "https://fitness.app/demo/cable_fly.mp4",
                    tips = listOf("Slight bend in elbows", "Hug a big tree movement", "Pause 1s at peak contraction")
                )
            )
        ),
        BodyPartWorkout(
            id = "back",
            title = "Back",
            subtitle = "Sculpt wide lats & spinal stability",
            bodyPart = "Back",
            workoutType = WorkoutType.GYM,
            imageRes = Res.drawable.gym_lat_pulldown,
            equipment = "Cable Pulley, Barbells, Machine",
            duration = "45 min",
            difficulty = "Advanced",
            calories = "~380 kcal",
            howToPerform = "Initiate each pull by depressing your shoulders and pulling through your elbows. Avoid excessive backward momentum.",
            exercises = listOf(
                ExerciseItem(
                    id = "gym_back_1",
                    name = "Wide-Grip Lat Pulldown",
                    sets = "4 sets × 10 reps",
                    duration = "10 min",
                    description = "Primary lat builder emphasizing vertical pulling and upper back width.",
                    imageRes = Res.drawable.gym_lat_pulldown,
                    videoDemoUrl = "https://fitness.app/demo/lat_pulldown.mp4",
                    tips = listOf("Pull bar to upper chest", "Lead with elbows", "Avoid leaning back past 15 degrees")
                ),
                ExerciseItem(
                    id = "gym_back_2",
                    name = "Seated Cable Row",
                    sets = "3 sets × 12 reps",
                    duration = "8 min",
                    description = "Develops middle trapezius and rhomboids for thickness and upright posture.",
                    imageRes = Res.drawable.gym_seated_cable_row,
                    videoDemoUrl = "https://fitness.app/demo/seated_cable_row.mp4",
                    tips = listOf("Keep chest proud", "Drive elbows straight back", "Control return stretch")
                ),
                ExerciseItem(
                    id = "gym_back_3",
                    name = "Barbell Bent-Over Row",
                    sets = "3 sets × 8-10 reps",
                    duration = "9 min",
                    description = "Heavy compound movement challenging the entire posterior chain and core.",
                    imageRes = Res.drawable.gym_barbell_row,
                    videoDemoUrl = "https://fitness.app/demo/barbell_row.mp4",
                    tips = listOf("Hinge at hips to 45 degrees", "Pull barbell towards belly button", "Neutral spine throughout")
                )
            )
        ),
        BodyPartWorkout(
            id = "shoulders",
            title = "Shoulders",
            subtitle = "Boulder deltoids & overhead power",
            bodyPart = "Shoulders",
            workoutType = WorkoutType.GYM,
            imageRes = Res.drawable.gym_shoulder_press,
            equipment = "Dumbbells, Cables, Smith Machine",
            duration = "40 min",
            difficulty = "Beginner",
            calories = "~320 kcal",
            howToPerform = "Maintain strict core bracing and engage all three deltoid heads through pressing and lateral raises.",
            exercises = listOf(
                ExerciseItem(
                    id = "gym_sh_1",
                    name = "Seated Dumbbell Shoulder Press",
                    sets = "4 sets × 10 reps",
                    duration = "10 min",
                    description = "Heavy overhead pressing for anterior and lateral deltoid mass.",
                    imageRes = Res.drawable.gym_shoulder_press,
                    videoDemoUrl = "https://fitness.app/demo/shoulder_press.mp4",
                    tips = listOf("Elbows slightly forward", "Press overhead in slight arc", "Do not hyperextend lower back")
                ),
                ExerciseItem(
                    id = "gym_sh_2",
                    name = "Dumbbell Lateral Raise",
                    sets = "4 sets × 15 reps",
                    duration = "8 min",
                    description = "Isolates the lateral head to broaden shoulder width and V-taper silhouette.",
                    imageRes = Res.drawable.gym_lateral_raise,
                    videoDemoUrl = "https://fitness.app/demo/lateral_raise.mp4",
                    tips = listOf("Lead with pinkies slightly higher", "Pause at parallel", "Use strict form without swinging")
                ),
                ExerciseItem(
                    id = "gym_sh_3",
                    name = "Cable Face Pull",
                    sets = "3 sets × 15 reps",
                    duration = "6 min",
                    description = "Strengthens rear deltoids and external rotators for bulletproof shoulder joints.",
                    imageRes = Res.drawable.gym_face_pull,
                    videoDemoUrl = "https://fitness.app/demo/face_pull.mp4",
                    tips = listOf("Pull rope towards eye level", "Externally rotate hands back", "Squeeze shoulder blades together")
                )
            )
        ),
        BodyPartWorkout(
            id = "biceps",
            title = "Biceps",
            subtitle = "Peak contraction & arm hypertrophy",
            bodyPart = "Biceps",
            workoutType = WorkoutType.GYM,
            imageRes = Res.drawable.gym_ez_bar_curl,
            equipment = "Barbell, EZ Bar, Dumbbells",
            duration = "30 min",
            difficulty = "Beginner to Inter.",
            calories = "~240 kcal",
            howToPerform = "Pin your elbows to your sides and supinate your wrists during the movement for full muscle belly stimulation.",
            exercises = listOf(
                ExerciseItem(
                    id = "gym_bi_1",
                    name = "Standing EZ-Bar Curl",
                    sets = "3 sets × 10 reps",
                    duration = "8 min",
                    description = "Ergonomic barbell curl maximizing bicep load with comfortable wrist positioning.",
                    imageRes = Res.drawable.gym_ez_bar_curl,
                    videoDemoUrl = "https://fitness.app/demo/ez_bar_curl.mp4",
                    tips = listOf("Do not swing shoulders", "Keep elbows steady", "Full range of motion")
                ),
                ExerciseItem(
                    id = "gym_bi_2",
                    name = "Incline Dumbbell Curl",
                    sets = "3 sets × 12 reps",
                    duration = "7 min",
                    description = "Places maximum stretch on the long head of the bicep to develop impressive peak.",
                    imageRes = Res.drawable.gym_incline_bicep_curl,
                    videoDemoUrl = "https://fitness.app/demo/incline_curl.mp4",
                    tips = listOf("Lean back against bench", "Keep arms hanging down perpendicular", "Rotate wrists outward")
                ),
                ExerciseItem(
                    id = "gym_bi_3",
                    name = "Hammer Rope Cable Curl",
                    sets = "3 sets × 12 reps",
                    duration = "6 min",
                    description = "Targets brachialis and forearm flexors for thicker, denser arms.",
                    imageRes = Res.drawable.gym_cable_hammer_curl,
                    videoDemoUrl = "https://fitness.app/demo/hammer_curl.mp4",
                    tips = listOf("Neutral grip with thumbs up", "Spread rope at the top", "Slow 3s eccentric phase")
                )
            )
        ),
        BodyPartWorkout(
            id = "triceps",
            title = "Triceps",
            subtitle = "Lockout strength & arm horseshoe",
            bodyPart = "Triceps",
            workoutType = WorkoutType.GYM,
            imageRes = Res.drawable.gym_tricep_pushdown,
            equipment = "Cable Station, Bench, Dumbbells",
            duration = "30 min",
            difficulty = "Beginner",
            calories = "~250 kcal",
            howToPerform = "Extend your arms completely at the bottom of the movement and pause to lock out the triceps lateral and long heads.",
            exercises = listOf(
                ExerciseItem(
                    id = "gym_tri_1",
                    name = "Cable Rope Tricep Pushdown",
                    sets = "4 sets × 12 reps",
                    duration = "8 min",
                    description = "Isolates the lateral and medial heads with constant pulley tension.",
                    imageRes = Res.drawable.gym_tricep_pushdown,
                    videoDemoUrl = "https://fitness.app/demo/tricep_pushdown.mp4",
                    tips = listOf("Keep elbows locked at sides", "Spread rope ends apart at bottom", "Lock out triceps hard")
                ),
                ExerciseItem(
                    id = "gym_tri_2",
                    name = "EZ-Bar Skull Crushers",
                    sets = "3 sets × 10 reps",
                    duration = "8 min",
                    description = "Deep stretch on the long head of the triceps overhead.",
                    imageRes = Res.drawable.gym_skull_crushers,
                    videoDemoUrl = "https://fitness.app/demo/skull_crushers.mp4",
                    tips = listOf("Lower bar towards hairline", "Maintain upper arm perpendicular to torso", "Keep elbows drawn in")
                ),
                ExerciseItem(
                    id = "gym_tri_3",
                    name = "Parallel Bar Dips",
                    sets = "3 sets × 12 reps",
                    duration = "7 min",
                    description = "Powerful bodyweight tricep compound movement emphasizing lockout strength.",
                    imageRes = Res.drawable.gym_tricep_dips,
                    videoDemoUrl = "https://fitness.app/demo/tricep_dips.mp4",
                    tips = listOf("Keep torso upright to focus triceps", "Lower until 90 degree bend", "Drive straight up")
                )
            )
        ),
        BodyPartWorkout(
            id = "legs",
            title = "Legs",
            subtitle = "Explosive quads, hamstrings & glutes",
            bodyPart = "Legs",
            workoutType = WorkoutType.GYM,
            imageRes = Res.drawable.gym_barbell_squat,
            equipment = "Squat Rack, Leg Press, Machines",
            duration = "50 min",
            difficulty = "Advanced",
            calories = "~480 kcal",
            howToPerform = "Plant feet shoulder-width, engage glutes and core, break at hips and knees simultaneously, descending below parallel.",
            exercises = listOf(
                ExerciseItem(
                    id = "gym_leg_1",
                    name = "Barbell Back Squat",
                    sets = "4 sets × 8 reps",
                    duration = "12 min",
                    description = "King of lower body exercises recruiting quads, glutes, hamstrings, and spinal erectors.",
                    imageRes = Res.drawable.gym_barbell_squat,
                    videoDemoUrl = "https://fitness.app/demo/back_squat.mp4",
                    tips = listOf("Weight balanced over midfoot", "Knees track over toes", "Drive up pushing the floor away")
                ),
                ExerciseItem(
                    id = "gym_leg_2",
                    name = "45° Incline Leg Press",
                    sets = "3 sets × 12 reps",
                    duration = "9 min",
                    description = "Heavy leg volume without spinal axial loading for massive quadricep development.",
                    imageRes = Res.drawable.gym_leg_press,
                    videoDemoUrl = "https://fitness.app/demo/leg_press.mp4",
                    tips = listOf("Do not lock knees completely at top", "Keep lower back flat against pad", "Full deep range")
                ),
                ExerciseItem(
                    id = "gym_leg_3",
                    name = "Lying Hamstring Leg Curl",
                    sets = "3 sets × 12-15 reps",
                    duration = "7 min",
                    description = "Isolates knee flexion of all hamstring heads for athletic symmetry and knee health.",
                    imageRes = Res.drawable.gym_hamstring_curl,
                    videoDemoUrl = "https://fitness.app/demo/hamstring_curl.mp4",
                    tips = listOf("Keep hips pressed to bench", "Point toes slightly", "Control eccentric release")
                )
            )
        ),
        BodyPartWorkout(
            id = "abs",
            title = "Abs / Core",
            subtitle = "Steel abdominal armor & deep core",
            bodyPart = "Abs",
            workoutType = WorkoutType.GYM,
            imageRes = Res.drawable.gym_hanging_leg_raise,
            equipment = "Pull-up Bar, Cable Machine, Mat",
            duration = "25 min",
            difficulty = "Beginner",
            calories = "~200 kcal",
            howToPerform = "Focus on flexing the spine and tilting the pelvis rather than using hip flexors to pull the weight.",
            exercises = listOf(
                ExerciseItem(
                    id = "gym_abs_1",
                    name = "Hanging Leg Raises",
                    sets = "3 sets × 12-15 reps",
                    duration = "7 min",
                    description = "Unmatched lower abdominal and hip flexor engagement with pelvic tilt.",
                    imageRes = Res.drawable.gym_hanging_leg_raise,
                    videoDemoUrl = "https://fitness.app/demo/hanging_leg_raise.mp4",
                    tips = listOf("Curl pelvis up towards ribs", "Avoid swinging momentum", "Slow controlled descent")
                ),
                ExerciseItem(
                    id = "gym_abs_2",
                    name = "Kneeling Cable Crunch",
                    sets = "3 sets × 15 reps",
                    duration = "6 min",
                    description = "Weighted rectus abdominis flexion providing direct overload resistance.",
                    imageRes = Res.drawable.gym_cable_crunch,
                    videoDemoUrl = "https://fitness.app/demo/cable_crunch.mp4",
                    tips = listOf("Keep hips stationary", "Curl elbows toward knees", "Breathe out all air at crunch")
                ),
                ExerciseItem(
                    id = "gym_abs_3",
                    name = "Ab Wheel Rollout",
                    sets = "3 sets × 10 reps",
                    duration = "6 min",
                    description = "Elite anti-extension challenge engaging transverse abdominis and serratus anterior.",
                    imageRes = Res.drawable.gym_ab_wheel,
                    videoDemoUrl = "https://fitness.app/demo/ab_wheel.mp4",
                    tips = listOf("Maintain slight hollow spine", "Roll out as far as control permits", "Pull back using abs")
                )
            )
        ),
        BodyPartWorkout(
            id = "full_body",
            title = "Full Body",
            subtitle = "Total systemic conditioning & athleticism",
            bodyPart = "Full Body",
            workoutType = WorkoutType.GYM,
            imageRes = Res.drawable.gym_clean_press,
            equipment = "Barbell, Dumbbell, Machines, Cables, Bodyweight",
            duration = "45-60 min",
            difficulty = "All Levels",
            calories = "~500 kcal",
            howToPerform = "Move seamlessly through compound and isolation patterns with proper form and disciplined recovery between sets.",
            exercises = FullBodyExerciseLibrary.getExerciseItemsForLevel(ExperienceLevel.BEGINNER)
        )
    )

    private val homeWorkouts: List<BodyPartWorkout> = listOf(
        BodyPartWorkout(
            id = "chest",
            title = "Chest",
            subtitle = "Sculpt pectoral definition with calisthenics",
            bodyPart = "Chest",
            workoutType = WorkoutType.HOME,
            imageRes = Res.drawable.workout_home,
            equipment = "Bodyweight, Yoga Mat, Chair",
            duration = "30 min",
            difficulty = "Beginner to Inter.",
            calories = "~250 kcal",
            howToPerform = "Maintain a rigid plank posture from heels to shoulders. Lower chest to floor and press up through your palms.",
            exercises = listOf(
                ExerciseItem(
                    id = "home_chest_1",
                    name = "Classic Push-ups",
                    sets = "4 sets × 15 reps",
                    duration = "8 min",
                    description = "The gold-standard bodyweight chest, shoulder, and core exercise.",
                    imageRes = Res.drawable.workout_home,
                    videoDemoUrl = "https://fitness.app/demo/classic_pushup.mp4",
                    tips = listOf("Keep glutes and core squeezed", "Elbows back at 45 degrees", "Full chest contact with floor")
                ),
                ExerciseItem(
                    id = "home_chest_2",
                    name = "Incline Feet-Elevated Push-ups",
                    sets = "3 sets × 12 reps",
                    duration = "7 min",
                    description = "Elevating feet on a chair or bed shifts mechanical tension to the upper chest.",
                    imageRes = Res.drawable.workout_incline_bench,
                    videoDemoUrl = "https://fitness.app/demo/decline_pushup.mp4",
                    tips = listOf("Keep body in a straight line", "Hands slightly wider than shoulders", "Pause at bottom")
                ),
                ExerciseItem(
                    id = "home_chest_3",
                    name = "Wide-Grip Deficit Push-ups",
                    sets = "3 sets × 12 reps",
                    duration = "6 min",
                    description = "Expands pectoral range of motion for maximum muscle fiber stretch.",
                    imageRes = Res.drawable.bodypart_chest,
                    videoDemoUrl = "https://fitness.app/demo/wide_pushup.mp4",
                    tips = listOf("Use books or blocks for deficit", "Feel deep chest stretch", "Drive up smoothly")
                )
            )
        ),
        BodyPartWorkout(
            id = "back",
            title = "Back",
            subtitle = "Posture restoration & posterior chain",
            bodyPart = "Back",
            workoutType = WorkoutType.HOME,
            imageRes = Res.drawable.program_endurance,
            equipment = "Towel, Resistance Band, Floor Mat",
            duration = "30 min",
            difficulty = "Beginner to Inter.",
            calories = "~240 kcal",
            howToPerform = "Contract your scapular retractors and erect your upper spine without straining your neck.",
            exercises = listOf(
                ExerciseItem(
                    id = "home_back_1",
                    name = "Superman Holds with Lat Squeeze",
                    sets = "4 sets × 12 reps (3s hold)",
                    duration = "8 min",
                    description = "Strengthens erectors, glutes, and lower trapezius against gravity.",
                    imageRes = Res.drawable.program_endurance,
                    videoDemoUrl = "https://fitness.app/demo/superman.mp4",
                    tips = listOf("Lift chest and thighs off floor", "Pull elbows back towards hips", "Hold peak contraction")
                ),
                ExerciseItem(
                    id = "home_back_2",
                    name = "Reverse Snow Angels",
                    sets = "3 sets × 15 reps",
                    duration = "7 min",
                    description = "Tones rhomboids, rear delts, and scapular stabilizers for upright posture.",
                    imageRes = Res.drawable.bodypart_back,
                    videoDemoUrl = "https://fitness.app/demo/reverse_snow_angels.mp4",
                    tips = listOf("Keep palms facing floor", "Sweep arms in wide arc", "Never touch hands to ground")
                ),
                ExerciseItem(
                    id = "home_back_3",
                    name = "Doorframe Isometric Rows",
                    sets = "3 sets × 12 reps per side",
                    duration = "7 min",
                    description = "Uses doorframe anchor to generate horizontal pulling tension on the lats.",
                    imageRes = Res.drawable.workout_home,
                    videoDemoUrl = "https://fitness.app/demo/doorframe_row.mp4",
                    tips = listOf("Grip securely with one arm", "Lean back at 45 degrees", "Pull chest forward to frame")
                )
            )
        ),
        BodyPartWorkout(
            id = "shoulders",
            title = "Shoulders",
            subtitle = "Handstand prep & shoulder endurance",
            bodyPart = "Shoulders",
            workoutType = WorkoutType.HOME,
            imageRes = Res.drawable.gender_female,
            equipment = "Wall, Mat, Light Water Bottles",
            duration = "28 min",
            difficulty = "Beginner",
            calories = "~260 kcal",
            howToPerform = "Control your center of gravity and maintain straight arms during isometric holds.",
            exercises = listOf(
                ExerciseItem(
                    id = "home_sh_1",
                    name = "Pike Push-ups",
                    sets = "3 sets × 10 reps",
                    duration = "8 min",
                    description = "Simulates vertical overhead pressing with steep hip hinge angle.",
                    imageRes = Res.drawable.gender_female,
                    videoDemoUrl = "https://fitness.app/demo/pike_pushup.mp4",
                    tips = listOf("Walk feet in close to hands", "Lower head forward between hands", "Press up and back")
                ),
                ExerciseItem(
                    id = "home_sh_2",
                    name = "Plank Shoulder Taps",
                    sets = "3 sets × 20 alternating reps",
                    duration = "6 min",
                    description = "Deltoid stability and core anti-rotation builder in plank position.",
                    imageRes = Res.drawable.workout_home,
                    videoDemoUrl = "https://fitness.app/demo/shoulder_taps.mp4",
                    tips = listOf("Keep hips totally still", "Touch opposite shoulder lightly", "Breathe steadily")
                ),
                ExerciseItem(
                    id = "home_sh_3",
                    name = "Prone Y-T-W Shoulder Raises",
                    sets = "3 sets × 10 per letter",
                    duration = "7 min",
                    description = "Physical therapy grade rotator cuff and deltoid strengthening series.",
                    imageRes = Res.drawable.bodypart_back,
                    videoDemoUrl = "https://fitness.app/demo/ytw_raises.mp4",
                    tips = listOf("Thumbs pointing toward ceiling", "Lift through shoulder blades", "Control each tempo")
                )
            )
        ),
        BodyPartWorkout(
            id = "biceps",
            title = "Biceps",
            subtitle = "Calisthenic arm pulls & tension control",
            bodyPart = "Biceps",
            workoutType = WorkoutType.HOME,
            imageRes = Res.drawable.goal_athlete,
            equipment = "Towel, Sturdy Table, Doorframe",
            duration = "25 min",
            difficulty = "Beginner to Inter.",
            calories = "~210 kcal",
            howToPerform = "Isolate the bicep contraction using bodyweight leverage and constant manual resistance.",
            exercises = listOf(
                ExerciseItem(
                    id = "home_bi_1",
                    name = "Under-Table Inverted Curls",
                    sets = "3 sets × 10-12 reps",
                    duration = "7 min",
                    description = "Grip table edge underhand and curl upper body towards underside of table.",
                    imageRes = Res.drawable.goal_athlete,
                    videoDemoUrl = "https://fitness.app/demo/table_curl.mp4",
                    tips = listOf("Underhand shoulder-width grip", "Keep body in straight plank", "Pull nose to table edge")
                ),
                ExerciseItem(
                    id = "home_bi_2",
                    name = "Isometric Towel Bicep Curls",
                    sets = "4 sets × 15s max effort holds",
                    duration = "6 min",
                    description = "Step on towel with both feet and pull up with maximal bicep tension.",
                    imageRes = Res.drawable.gender_male,
                    videoDemoUrl = "https://fitness.app/demo/towel_curl.mp4",
                    tips = listOf("Lock elbows at 90 degrees", "Squeeze bicep with 100% effort", "Maintain steady breathing")
                ),
                ExerciseItem(
                    id = "home_bi_3",
                    name = "Chin-Up Hold Negative (if bar available) / Floor Pulls",
                    sets = "3 sets × 8 slow reps",
                    duration = "6 min",
                    description = "High-tension eccentric bicep training triggering fast-twitch fibers.",
                    imageRes = Res.drawable.workout_incline_bench,
                    videoDemoUrl = "https://fitness.app/demo/chinup_negative.mp4",
                    tips = listOf("Take 4 seconds to descend", "Keep chin high", "Control the full stretch")
                )
            )
        ),
        BodyPartWorkout(
            id = "triceps",
            title = "Triceps",
            subtitle = "Bench dips & bodyweight push-downs",
            bodyPart = "Triceps",
            workoutType = WorkoutType.HOME,
            imageRes = Res.drawable.welcome_hero,
            equipment = "Chair / Sofa, Floor Mat",
            duration = "25 min",
            difficulty = "Beginner to Inter.",
            calories = "~220 kcal",
            howToPerform = "Keep elbows pointing straight back to direct tension away from chest onto the triceps.",
            exercises = listOf(
                ExerciseItem(
                    id = "home_tri_1",
                    name = "Chair / Couch Dips",
                    sets = "4 sets × 15 reps",
                    duration = "8 min",
                    description = "Accessible, highly effective tricep builder using any sturdy household furniture.",
                    imageRes = Res.drawable.welcome_hero,
                    videoDemoUrl = "https://fitness.app/demo/chair_dips.mp4",
                    tips = listOf("Keep back close to chair edge", "Lower to 90 degrees", "Lock out triceps at top")
                ),
                ExerciseItem(
                    id = "home_tri_2",
                    name = "Diamond Push-ups",
                    sets = "3 sets × 10 reps",
                    duration = "7 min",
                    description = "Close-hand pushup placement overloading the medial and lateral tricep heads.",
                    imageRes = Res.drawable.workout_home,
                    videoDemoUrl = "https://fitness.app/demo/diamond_pushup.mp4",
                    tips = listOf("Touch thumbs and index fingers", "Lower chest to hands", "Keep body rigid")
                ),
                ExerciseItem(
                    id = "home_tri_3",
                    name = "Floor Tricep Extension (Cobra Push-up)",
                    sets = "3 sets × 12 reps",
                    duration = "6 min",
                    description = "Isolates the elbow hinge on the floor without requiring heavy equipment.",
                    imageRes = Res.drawable.bodypart_chest,
                    videoDemoUrl = "https://fitness.app/demo/cobra_pushup.mp4",
                    tips = listOf("Hands under shoulders", "Press palms to straighten arms", "Squeeze triceps at top")
                )
            )
        ),
        BodyPartWorkout(
            id = "legs",
            title = "Legs",
            subtitle = "Explosive quads, pistol squat prep & glutes",
            bodyPart = "Legs",
            workoutType = WorkoutType.HOME,
            imageRes = Res.drawable.program_sport,
            equipment = "Bodyweight, Wall, Chair",
            duration = "35 min",
            difficulty = "Beginner",
            calories = "~350 kcal",
            howToPerform = "Reach full depth on bodyweight squats, keep knees aligned over toes, and maintain upright torso.",
            exercises = listOf(
                ExerciseItem(
                    id = "home_leg_1",
                    name = "Air Squats with Pause",
                    sets = "4 sets × 20 reps (2s pause)",
                    duration = "9 min",
                    description = "Deep bodyweight squats building quadricep endurance and hip mobility.",
                    imageRes = Res.drawable.workout_home,
                    videoDemoUrl = "https://fitness.app/demo/air_squat.mp4",
                    tips = listOf("Hips back and down", "Knees pushing out", "Drive up through full foot")
                ),
                ExerciseItem(
                    id = "home_leg_2",
                    name = "Forward & Reverse Walking Lunges",
                    sets = "3 sets × 12 per leg",
                    duration = "8 min",
                    description = "Unilateral lower body training developing balance, glute power, and quad tone.",
                    imageRes = Res.drawable.program_sport,
                    videoDemoUrl = "https://fitness.app/demo/walking_lunges.mp4",
                    tips = listOf("90 degree bend in both knees", "Keep torso perpendicular", "Do not slam back knee to floor")
                ),
                ExerciseItem(
                    id = "home_leg_3",
                    name = "Single-Leg Glute Bridge",
                    sets = "3 sets × 15 per leg",
                    duration = "7 min",
                    description = "Isolates the gluteus maximus and hamstrings without lower back stress.",
                    imageRes = Res.drawable.goal_general,
                    videoDemoUrl = "https://fitness.app/demo/glute_bridge.mp4",
                    tips = listOf("Drive through heel", "Extend hips fully upward", "Hold 1s at top")
                )
            )
        ),
        BodyPartWorkout(
            id = "abs",
            title = "Abs / Core",
            subtitle = "Core stabilization & six-pack definition",
            bodyPart = "Abs",
            workoutType = WorkoutType.HOME,
            imageRes = Res.drawable.goal_general,
            equipment = "Yoga Mat",
            duration = "20 min",
            difficulty = "Beginner to Inter.",
            calories = "~180 kcal",
            howToPerform = "Press your lower back firmly against the floor to eliminate any lumbar gap throughout all supine movements.",
            exercises = listOf(
                ExerciseItem(
                    id = "home_abs_1",
                    name = "Bicycle Crunches",
                    sets = "3 sets × 25 alternating reps",
                    duration = "6 min",
                    description = "Ranks #1 in electromyography studies for simultaneous rectus abdominis and obliques activation.",
                    imageRes = Res.drawable.goal_general,
                    videoDemoUrl = "https://fitness.app/demo/bicycle_crunch.mp4",
                    tips = listOf("Slow controlled tempo", "Rotate torso from ribcage", "Do not pull on neck")
                ),
                ExerciseItem(
                    id = "home_abs_2",
                    name = "Hollow Body Rocker",
                    sets = "3 sets × 30-45s holds",
                    duration = "5 min",
                    description = "Gymnastic core cornerstone building iron-clad anterior pelvic control.",
                    imageRes = Res.drawable.workout_home,
                    videoDemoUrl = "https://fitness.app/demo/hollow_body.mp4",
                    tips = listOf("Lower back glued to floor", "Point toes and extend arms back", "Rock gently like a boat")
                ),
                ExerciseItem(
                    id = "home_abs_3",
                    name = "Mountain Climbers with Knee Cross",
                    sets = "3 sets × 30s fast intervals",
                    duration = "5 min",
                    description = "Dynamic high-cadence abdominal scorcher boosting metabolic heart rate.",
                    imageRes = Res.drawable.program_speed,
                    videoDemoUrl = "https://fitness.app/demo/mountain_climber.mp4",
                    tips = listOf("High plank position", "Drive knee to opposite elbow", "Keep hips low")
                )
            )
        ),
        BodyPartWorkout(
            id = "full_body",
            title = "Full Body",
            subtitle = "HIIT calisthenic metabolic furnace",
            bodyPart = "Full Body",
            workoutType = WorkoutType.HOME,
            imageRes = Res.drawable.workout_home,
            equipment = "Bodyweight, Towel, Water Bottle",
            duration = "35 min",
            difficulty = "Advanced",
            calories = "~440 kcal",
            howToPerform = "High-energy full body circuit alternating explosive compound movements with active recovery.",
            exercises = listOf(
                ExerciseItem(
                    id = "home_fb_1",
                    name = "Chest-to-Floor Burpees",
                    sets = "4 sets × 12 reps",
                    duration = "8 min",
                    description = "Full body explosive power combining pushup, squat thrust, and vertical leap.",
                    imageRes = Res.drawable.workout_home,
                    videoDemoUrl = "https://fitness.app/demo/burpees.mp4",
                    tips = listOf("Drop chest smoothly", "Snap feet forward under hips", "Jump high clapping overhead")
                ),
                ExerciseItem(
                    id = "home_fb_2",
                    name = "Squat Jump into Reverse Lunge",
                    sets = "3 sets × 10 per leg",
                    duration = "8 min",
                    description = "Plyometric leg and cardiovascular builder demanding rapid deceleration control.",
                    imageRes = Res.drawable.program_sport,
                    videoDemoUrl = "https://fitness.app/demo/squat_jump_lunge.mp4",
                    tips = listOf("Land softly on balls of feet", "Immediately step back into lunge", "Explode vertically")
                ),
                ExerciseItem(
                    id = "home_fb_3",
                    name = "Bear Crawl to Crab Walk Combo",
                    sets = "3 sets × 45s continuous",
                    duration = "7 min",
                    description = "Animal flow pattern building shoulder, core, wrist, and quad endurance.",
                    imageRes = Res.drawable.goal_athlete,
                    videoDemoUrl = "https://fitness.app/demo/bear_crawl.mp4",
                    tips = listOf("Keep knees hovering 2 inches off floor", "Step opposite hand and foot", "Switch fluidly")
                )
            )
        )
    )

    fun matchesBodyPart(exercise: Exercise, bodyPartId: String, bodyPartName: String): Boolean {
        if (AdminExerciseCatalogOrganizer.isCardioOrActivity(exercise)) {
            return bodyPartId.equals("cardio", ignoreCase = true) || bodyPartName.equals("cardio", ignoreCase = true)
        }

        val section = AdminExerciseCatalogOrganizer.classify(exercise)
        val id = bodyPartId.trim().lowercase()
        val name = bodyPartName.trim().lowercase()

        return when {
            id == "chest" || name == "chest" -> section == ExerciseBodyPartSection.CHEST
            id == "back" || name == "back" -> section == ExerciseBodyPartSection.BACK
            id == "shoulders" || name == "shoulders" -> section == ExerciseBodyPartSection.SHOULDERS
            id == "biceps" || name == "biceps" -> section == ExerciseBodyPartSection.BICEPS
            id == "triceps" || name == "triceps" -> section == ExerciseBodyPartSection.TRICEPS
            id == "arms" || name == "arms" -> section == ExerciseBodyPartSection.BICEPS || section == ExerciseBodyPartSection.TRICEPS
            id == "legs" || name == "legs" -> section in listOf(ExerciseBodyPartSection.LEGS, ExerciseBodyPartSection.GLUTES)
            id == "core" || id == "abs" || name == "core" || name == "abs" -> section == ExerciseBodyPartSection.CORE
            id == "full_body" || name == "full body" -> section == ExerciseBodyPartSection.FULL_BODY
            id == "mobility" || name == "mobility" -> section == ExerciseBodyPartSection.MOBILITY_RECOVERY
            id == "cardio" || name == "cardio" -> section == ExerciseBodyPartSection.CARDIO_DRILLS
            else -> false
        }
    }

    fun isGenderCompatible(exercise: Exercise, userGender: String?): Boolean {
        if (userGender.isNullOrBlank() || userGender.equals("All", ignoreCase = true)) {
            return true
        }
        val isTargetFemale = userGender.equals("Female", ignoreCase = true)
        val exGender = exercise.gender.trim()

        return if (isTargetFemale) {
            if (exGender.equals("Male", ignoreCase = true) ||
                exGender.equals("Men", ignoreCase = true) ||
                exGender.equals("Boys", ignoreCase = true)
            ) {
                false
            } else {
                true
            }
        } else {
            // Male target
            if (exGender.equals("Female", ignoreCase = true) ||
                exGender.equals("Women", ignoreCase = true) ||
                exGender.equals("Girls", ignoreCase = true)
            ) {
                false
            } else {
                true
            }
        }
    }

    fun isLevelCompatible(exerciseDifficulty: String, userLevel: ExperienceLevel?): Boolean {
        if (userLevel == null) return true
        val diff = exerciseDifficulty.trim().lowercase()
        if (diff.isBlank()) return false

        // Check explicit multi-level or universal tags if supported by existing data
        if (diff == "all" || diff.contains("all levels") || diff.contains("all_levels")) return true

        // Check if an exercise explicitly supports multiple comma-separated or slash-separated levels
        val hasNoExp = diff.contains("no_experience") || diff.contains("no experience") || diff.contains("noexp")
        val hasBeg = (diff.contains("beginner") || diff.contains("beg")) && !diff.contains("no_experience") && !diff.contains("no experience") && !diff.contains("noexp")
        val hasAdv = diff.contains("advanced") || diff.contains("adv") || diff.contains("advance")
        val hasExp = diff.contains("expert") || (diff.contains("exp") && !diff.contains("experience"))

        return when (userLevel) {
            ExperienceLevel.NO_EXPERIENCE -> hasNoExp
            ExperienceLevel.BEGINNER -> hasBeg
            ExperienceLevel.ADVANCED -> hasAdv
            ExperienceLevel.EXPERT -> hasExp
        }
    }

    fun mapExerciseToItem(exercise: Exercise, fallbackImage: DrawableResource, bodyPartName: String): ExerciseItem {
        val repsStr = exercise.defaultReps.ifBlank { "10-12 reps" }
        val setsStr = "${exercise.defaultSets} sets × $repsStr"
        val durStr = "${(exercise.defaultSets * 2).coerceAtLeast(5)} min"
        val desc = exercise.description.ifBlank { "Canonical movement targeting $bodyPartName and primary muscular recruitment." }
        val tips = if (exercise.formTips.isNotEmpty()) {
            exercise.formTips
        } else if (exercise.executionSteps.isNotEmpty()) {
            exercise.executionSteps
        } else {
            listOf("Maintain full control through the eccentric range", "Breathe smoothly and engage your core throughout")
        }
        return ExerciseItem(
            id = exercise.id,
            name = exercise.name,
            sets = setsStr,
            duration = durStr,
            description = desc,
            imageRes = fallbackImage,
            videoDemoUrl = exercise.media.videoUrl,
            tips = tips,
            restTime = "${exercise.defaultRestSeconds}s",
            media = exercise.media
        )
    }

    fun countWorkoutsContainingExercise(exerciseId: String): Int {
        val cleanId = exerciseId.trim().lowercase()
        if (cleanId.isBlank()) return 0
        val canonicalId = SampleExerciseData.resolveCanonicalId(cleanId).lowercase()
        val allWorkouts = gymWorkouts + homeWorkouts
        return allWorkouts.count { workout ->
            workout.exercises.any { item ->
                val itemId = item.id.trim().lowercase()
                val itemCanonical = SampleExerciseData.resolveCanonicalId(itemId).lowercase()
                itemId == cleanId || itemId == canonicalId || itemCanonical == cleanId || itemCanonical == canonicalId
            }
        }
    }

    /**
     * Centralized dynamic exercise-fetching mechanism.
     * Single source of truth for retrieving exercises matching:
     * 1. [bodyPart] (e.g. Chest, Back, Shoulders, Biceps, Triceps, Legs, Abs, Full Body)
     * 2. [experienceLevel] (NO_EXPERIENCE, BEGINNER, ADVANCED, EXPERT)
     * 3. [gender] (Male, Female, All/Null)
     * 4. [workoutType] (GYM, HOME)
     *
     * Seamlessly integrates:
     * - ThirtyDayExerciseLibrary (Chest, Back, Shoulders, Biceps, Triceps, Legs, Abs)
     * - FullBodyExerciseLibrary (Full Body)
     * - SampleExerciseData live canonical overrides & custom exercises
     */
    fun fetchExercises(
        bodyPart: String,
        experienceLevel: ExperienceLevel,
        gender: String? = UserSession.currentUserGender,
        workoutType: WorkoutType = WorkoutType.GYM
    ): List<ExerciseItem> {
        val cacheKey = "$bodyPart|$experienceLevel|${gender ?: ""}|$workoutType|${SampleExerciseData.canonicalRevision}"
        exercisesCache[cacheKey]?.let { return it }

        val parts = bodyPart.split("+", "&", ",").map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.size > 1) {
            val combined = mutableListOf<ExerciseItem>()
            val seenIds = mutableSetOf<String>()
            for (part in parts) {
                val subList = fetchExercises(part, experienceLevel, gender, workoutType)
                for (item in subList) {
                    val cleanId = item.id.trim().lowercase()
                    if (seenIds.add(cleanId)) {
                        combined.add(item)
                    }
                }
            }
            exercisesCache[cacheKey] = combined
            return combined
        }

        val bpNorm = bodyPart.trim().lowercase()
        val isFullBody = bpNorm == "full_body" || bpNorm == "full body"

        // 1. Fetch baseline exercises from authoritative libraries
        val baseItems: List<ExerciseItem> = if (isFullBody) {
            FullBodyExerciseLibrary.getExerciseItemsForLevel(experienceLevel)
        } else {
            ThirtyDayExerciseLibrary.getExerciseItems(bodyPart, experienceLevel)
        }

        // 2. Reconcile with SampleExerciseData (admin overrides, custom media, soft-deletions)
        val enrichedItems = baseItems.mapNotNull { item ->
            val cleanId = item.id.trim().lowercase()
            val aliasId = SampleExerciseData.resolveCanonicalId(item.id).lowercase()
            if (SampleExerciseData.isDeleted(cleanId) || SampleExerciseData.isDeleted(aliasId)) {
                return@mapNotNull null
            }
            if (SampleExerciseData.isArchived(cleanId) || SampleExerciseData.isArchived(aliasId)) {
                return@mapNotNull null
            }

            val canonical = SampleExerciseData.findCanonical(item.id, item.name)
                ?: SampleExerciseData.getById(item.id)
                ?: SampleExerciseData.getById(aliasId)

            if (canonical != null) {
                val canonId = canonical.id.trim().lowercase()
                if (!canonical.isActive || SampleExerciseData.isDeleted(canonId) || SampleExerciseData.isArchived(canonId)) {
                    return@mapNotNull null
                }
                if (!isGenderCompatible(canonical, gender)) {
                    return@mapNotNull null
                }
                val hasVideo = canonical.media.videoUrl.isNotBlank() && !canonical.media.videoUrl.contains("fitness.app/demo", ignoreCase = true)
                val isExplicitOverride = SampleExerciseData.getLiveOverrides().any { it.id.equals(canonical.id, ignoreCase = true) }
                val updatedName = if (isExplicitOverride && canonical.name.isNotBlank()) canonical.name else item.name
                val updatedDesc = if (isExplicitOverride && canonical.description.isNotBlank()) canonical.description else item.description
                val updatedTips = if (isExplicitOverride && canonical.formTips.isNotEmpty()) canonical.formTips else item.tips
                val updatedSets = if (isExplicitOverride && canonical.defaultSets > 0) {
                    val r = if (canonical.defaultReps.isNotBlank()) canonical.defaultReps else "10-12"
                    "${canonical.defaultSets} sets × $r"
                } else item.sets
                val updatedRest = if (canonical.defaultRestSeconds > 0) {
                    "${canonical.defaultRestSeconds}s"
                } else {
                    val sec = CustomExerciseConfig.parseDefaultRest(item.restTime)
                    "${sec}s"
                }

                item.copy(
                    name = updatedName,
                    sets = updatedSets,
                    description = updatedDesc,
                    tips = updatedTips,
                    restTime = updatedRest,
                    media = canonical.media,
                    videoDemoUrl = if (hasVideo) canonical.media.videoUrl else item.videoDemoUrl
                )
            } else {
                item
            }
        }

        // 3. Merge any live custom exercises matching this body part and experience level
        val seenIds = enrichedItems.map { it.id.trim().lowercase() }.toMutableSet()
        val defaultImage = if (isFullBody) Res.drawable.workout_gym else FullBodyExerciseLibrary.resolveExerciseImage(bodyPart, "")
        val liveCustomItems = SampleExerciseData.getLiveCustomExercises().filter { liveEx ->
            val liveId = liveEx.id.trim().lowercase()
            liveEx.isActive &&
                    !SampleExerciseData.isDeleted(liveId) &&
                    !SampleExerciseData.isArchived(liveId) &&
                    matchesBodyPart(liveEx, bpNorm, bodyPart) &&
                    !seenIds.contains(liveId) &&
                    isLevelCompatible(liveEx.difficulty, experienceLevel) &&
                    isGenderCompatible(liveEx, gender)
        }.map { mapExerciseToItem(it, defaultImage, bodyPart) }

        val result = enrichedItems + liveCustomItems
        exercisesCache[cacheKey] = result
        return result
    }

    fun getExercisesForBodyPartAndLevel(
        bodyPart: String,
        level: ExperienceLevel = UserSession.experienceLevel,
        gender: String? = UserSession.currentUserGender,
        workoutType: WorkoutType = WorkoutType.GYM
    ): List<ExerciseItem> {
        return fetchExercises(bodyPart, level, gender, workoutType)
    }

    private fun enrichWorkout(
        workout: BodyPartWorkout,
        gender: String? = UserSession.currentUserGender,
        level: ExperienceLevel? = null
    ): BodyPartWorkout {
        // If an explicit experience level is provided, dynamically fetch matching exercises
        if (level != null) {
            val exercises = fetchExercises(workout.bodyPart, level, gender, workout.workoutType)
            val diffLabel = when (level) {
                ExperienceLevel.NO_EXPERIENCE -> "No Experience"
                ExperienceLevel.BEGINNER -> "Beginner"
                ExperienceLevel.ADVANCED -> "Advanced"
                ExperienceLevel.EXPERT -> "Expert"
            }
            return workout.copy(
                difficulty = diffLabel,
                exercises = exercises
            )
        }

        // When level == null (baseline / unfiltered catalog query):
        val isFullBody = (workout.bodyPart.equals("Full Body", ignoreCase = true) || workout.id.equals("full_body", ignoreCase = true)) && workout.workoutType == WorkoutType.GYM
        if (isFullBody) {
            val baseItems = FullBodyExerciseLibrary.ALL_FULL_BODY_EXERCISES.map { ex ->
                FullBodyExerciseLibrary.toExerciseItem(ex, FullBodyExerciseLibrary.resolveExerciseImage(ex.name, ex.id))
            }
            val enrichedFbItems = baseItems.map { item ->
                val canonical = SampleExerciseData.findCanonical(item.id, item.name) ?: SampleExerciseData.getById(item.id)
                if (canonical != null) {
                    val hasVideo = canonical.media.videoUrl.isNotBlank()
                    val isExplicitOverride = SampleExerciseData.getLiveOverrides().any { it.id.equals(canonical.id, ignoreCase = true) }
                    val updatedName = if (isExplicitOverride && canonical.name.isNotBlank()) canonical.name else item.name
                    val updatedDesc = if (isExplicitOverride && canonical.description.isNotBlank()) canonical.description else item.description
                    val updatedTips = if (isExplicitOverride && canonical.formTips.isNotEmpty()) canonical.formTips else item.tips
                    item.copy(
                        name = updatedName,
                        description = updatedDesc,
                        tips = updatedTips,
                        media = canonical.media,
                        videoDemoUrl = if (hasVideo) canonical.media.videoUrl else item.videoDemoUrl
                    )
                } else item
            }

            val seenCanonicalIds = enrichedFbItems.map { it.id.trim().lowercase() }.toMutableSet()
            val liveFullBody = SampleExerciseData.getLiveCustomExercises().filter { liveEx ->
                val liveId = liveEx.id.trim().lowercase()
                liveEx.isActive &&
                        !SampleExerciseData.isDeleted(liveId) &&
                        !SampleExerciseData.isArchived(liveId) &&
                        matchesBodyPart(liveEx, workout.id, workout.bodyPart) &&
                        !seenCanonicalIds.contains(liveId) &&
                        isGenderCompatible(liveEx, gender)
            }.map { mapExerciseToItem(it, workout.imageRes, workout.bodyPart) }

            return workout.copy(
                difficulty = "All Levels",
                exercises = enrichedFbItems + liveFullBody
            )
        }

        // Baseline targeted seed exercises (preserves custom seed properties and live additions)
        val resolvedSeedExercises = mutableListOf<ExerciseItem>()
        val seenCanonicalIds = mutableSetOf<String>()

        for (item in workout.exercises) {
            val itemId = item.id.trim().lowercase()
            val aliasId = SampleExerciseData.resolveCanonicalId(item.id).lowercase()
            if (SampleExerciseData.isDeleted(itemId) || SampleExerciseData.isDeleted(aliasId)) continue
            if (SampleExerciseData.isArchived(itemId) || SampleExerciseData.isArchived(aliasId)) continue

            val canonical = SampleExerciseData.findCanonical(item.id, item.name)
                ?: SampleExerciseData.getById(item.id)

            if (canonical != null) {
                val canonicalId = canonical.id.trim().lowercase()
                seenCanonicalIds.add(canonicalId)
                seenCanonicalIds.add(aliasId)
                seenCanonicalIds.add(itemId)

                if (!canonical.isActive || SampleExerciseData.isDeleted(canonicalId) || SampleExerciseData.isArchived(canonicalId)) continue
                if (!isGenderCompatible(canonical, gender)) continue

                val hasVideo = canonical.media.videoUrl.isNotBlank()
                val isExplicitOverride = SampleExerciseData.getLiveOverrides().any { it.id.equals(canonical.id, ignoreCase = true) }
                val updatedName = if (isExplicitOverride && canonical.name.isNotBlank()) canonical.name else item.name
                val updatedDesc = if (isExplicitOverride && canonical.description.isNotBlank()) canonical.description else item.description
                val updatedTips = if (isExplicitOverride && canonical.formTips.isNotEmpty()) canonical.formTips else item.tips

                resolvedSeedExercises.add(
                    item.copy(
                        name = updatedName,
                        description = updatedDesc,
                        tips = updatedTips,
                        media = canonical.media,
                        videoDemoUrl = if (hasVideo) canonical.media.videoUrl else item.videoDemoUrl
                    )
                )
            } else {
                resolvedSeedExercises.add(item)
            }
        }

        val liveExercises = SampleExerciseData.getLiveCustomExercises()
        val dynamicAdditions = mutableListOf<ExerciseItem>()

        for (liveEx in liveExercises) {
            if (!liveEx.isActive) continue
            val liveId = liveEx.id.trim().lowercase()
            if (SampleExerciseData.isDeleted(liveId) || SampleExerciseData.isArchived(liveId)) continue
            if (seenCanonicalIds.contains(liveId)) continue

            if (!matchesBodyPart(liveEx, workout.id, workout.bodyPart)) continue
            if (!isGenderCompatible(liveEx, gender)) continue

            seenCanonicalIds.add(liveId)
            val mappedItem = mapExerciseToItem(liveEx, workout.imageRes, workout.bodyPart)
            dynamicAdditions.add(mappedItem)
        }

        val allMatchingExercises = resolvedSeedExercises + dynamicAdditions
        val finalExercises = if (allMatchingExercises.isNotEmpty()) allMatchingExercises else workout.exercises
        return workout.copy(exercises = finalExercises)
    }

    private var cachedGender: String? = null
    private var cachedLevel: ExperienceLevel? = null
    private var cachedRevision: Int = -1
    private var cachedAllExercises: List<ExerciseItem>? = null

    fun getAllExerciseItems(): List<ExerciseItem> {
        val effGender = UserSession.currentUserGender
        val effLevel = UserSession.experienceLevel
        val currentRev = SampleExerciseData.canonicalRevision
        val cached = cachedAllExercises
        if (cached != null && cachedGender == effGender && cachedLevel == effLevel && cachedRevision == currentRev) {
            return cached
        }
        val allGym = gymWorkouts.flatMap { enrichWorkout(it, effGender, effLevel).exercises }
        val allHome = homeWorkouts.flatMap { enrichWorkout(it, effGender, effLevel).exercises }
        val result = allGym + allHome
        cachedGender = effGender
        cachedLevel = effLevel
        cachedRevision = currentRev
        cachedAllExercises = result
        return result
    }

    fun findExerciseItem(idOrName: String): ExerciseItem? {
        val clean = idOrName.trim().lowercase()
        if (clean.isEmpty()) return null
        val canon = SampleExerciseData.resolveCanonicalId(idOrName).trim().lowercase()
        val allItems = getAllExerciseItems()
        val fromActive = allItems.find { item ->
            val itemId = item.id.trim().lowercase()
            itemId == clean || itemId == canon || item.name.trim().lowercase() == clean
        }
        if (fromActive != null) return fromActive

        // Fallback to static libraries so Exercise Details never fails even if called for another level
        val thirtyDay = ThirtyDayExerciseLibrary.findById(clean) ?: ThirtyDayExerciseLibrary.findByName(idOrName)
        if (thirtyDay != null) {
            val ex = ThirtyDayExerciseLibrary.toExercise(thirtyDay)
            val canonEx = SampleExerciseData.getById(thirtyDay.id) ?: SampleExerciseData.findCanonical(thirtyDay.canonicalId, thirtyDay.name) ?: ex
            return mapExerciseToItem(canonEx, FullBodyExerciseLibrary.resolveExerciseImage(canonEx.name, canonEx.id), thirtyDay.bodyPart)
        }

        val fb = FullBodyExerciseLibrary.findById(clean) ?: FullBodyExerciseLibrary.findByName(idOrName)
        if (fb != null) {
            val canonEx = SampleExerciseData.getById(fb.id) ?: SampleExerciseData.findCanonical(fb.id, fb.name) ?: fb
            return mapExerciseToItem(canonEx, FullBodyExerciseLibrary.resolveExerciseImage(canonEx.name, canonEx.id), "Full Body")
        }

        val canonEx = SampleExerciseData.getById(clean) ?: SampleExerciseData.getById(canon) ?: SampleExerciseData.findByName(idOrName)
        if (canonEx != null) {
            return mapExerciseToItem(canonEx, FullBodyExerciseLibrary.resolveExerciseImage(canonEx.name, canonEx.id), canonEx.primaryMuscle.ifBlank { "Full Body" })
        }

        return null
    }

    fun getWorkouts(
        type: WorkoutType,
        gender: String? = null,
        level: ExperienceLevel? = null
    ): List<BodyPartWorkout> {
        val raw = when (type) {
            WorkoutType.GYM -> gymWorkouts
            WorkoutType.HOME -> homeWorkouts
        }
        val effGender = gender ?: UserSession.currentUserGender
        return raw.map { enrichWorkout(it, effGender, level) }
    }

    fun getWorkout(
        type: WorkoutType,
        bodyPartId: String,
        gender: String? = null,
        level: ExperienceLevel? = null
    ): BodyPartWorkout? {
        val raw = when (type) {
            WorkoutType.GYM -> gymWorkouts
            WorkoutType.HOME -> homeWorkouts
        }
        val effGender = gender ?: UserSession.currentUserGender
        val matchRaw = raw.find { it.id.equals(bodyPartId, ignoreCase = true) }
        if (matchRaw != null) {
            return enrichWorkout(matchRaw, effGender, level)
        }
        val list = getWorkouts(type, effGender, level)
        val directMatch = list.find { it.id.equals(bodyPartId, ignoreCase = true) }
        if (directMatch != null) return directMatch

        // Composite multi-body-part workout ID support (e.g. "chest_triceps" or "chest+triceps")
        val cleanKey = bodyPartId.trim().lowercase()
        val parts = cleanKey.split("+", "_", ",", "&").map { it.trim() }.filter { it.isNotEmpty() }
        val knownBps = setOf("legs", "chest", "back", "shoulders", "biceps", "triceps", "abs", "core", "full_body", "full body")
        if (parts.size > 1 && parts.all { knownBps.contains(it) }) {
            val effLevel = level ?: UserSession.experienceLevel
            val combinedExercises = parts.flatMap { p -> fetchExercises(p, effLevel, effGender, type) }.distinctBy { it.id }
            val diffLabel = when (effLevel) {
                ExperienceLevel.NO_EXPERIENCE -> "No Experience"
                ExperienceLevel.BEGINNER -> "Beginner"
                ExperienceLevel.ADVANCED -> "Advanced"
                ExperienceLevel.EXPERT -> "Expert"
            }
            val title = parts.joinToString(" + ") { it.replaceFirstChar(Char::titlecase) }
            return BodyPartWorkout(
                id = bodyPartId,
                title = title,
                subtitle = "Targeted dynamic session for $title",
                bodyPart = title,
                workoutType = type,
                imageRes = Res.drawable.workout_gym,
                equipment = "Gym Equipment",
                duration = "45 min",
                difficulty = diffLabel,
                calories = "~400 kcal",
                howToPerform = "Perform each movement with strict form and disciplined recovery between sets.",
                exercises = combinedExercises
            )
        }

        return list.firstOrNull()
    }
}

