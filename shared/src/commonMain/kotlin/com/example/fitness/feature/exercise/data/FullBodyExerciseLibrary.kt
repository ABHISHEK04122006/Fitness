package com.example.fitness.feature.exercise.data

import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseCategory
import com.example.fitness.feature.exercise.model.ExerciseDifficulty
import com.example.fitness.feature.exercise.model.ExerciseMedia
import com.example.fitness.feature.exercise.model.MovementPattern
import com.example.fitness.feature.workout.model.ExerciseItem
import com.example.fitness.feature.workout.model.ExperienceLevel
import fitness.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource

/**
 * Authoritative AthGym Full Body Exercise Library.
 * Source of truth: AthGym_Full_Body_Exercise_Library.pdf (Pages 1–6).
 *
 * Difficulty levels:
 * - No Experience: 17 exercises (Page 2)
 * - Beginner: 21 exercises (Page 3)
 * - Advanced: 26 exercises (Page 4)
 * - Expert: 26 exercises (Page 5)
 * Total: 90 prescribed exercise entries with exact names, muscles, equipment, sets, reps, and rest.
 */
object FullBodyExerciseLibrary {

    private fun createFbExercise(
        id: String,
        name: String,
        primaryMuscle: String,
        equipment: List<String>,
        defaultSets: Int,
        defaultReps: String,
        defaultRestSeconds: Int,
        difficulty: String,
        movementPattern: String = "COMPOUND",
        imageRes: DrawableResource = Res.drawable.workout_gym,
        setupSteps: List<String> = emptyList(),
        executionSteps: List<String> = emptyList(),
        breathing: String = "",
        commonMistakes: List<String> = emptyList(),
        safetyNotes: List<String> = emptyList()
    ): Exercise = Exercise(
        id = id,
        name = name,
        slug = id.replace('_', '-'),
        description = "AthGym canonical full body library exercise targeting $primaryMuscle with ${equipment.joinToString(", ")}.",
        primaryMuscle = primaryMuscle,
        secondaryMuscles = if (primaryMuscle.equals("Full Body", ignoreCase = true)) emptyList() else listOf("Full Body"),
        muscleGroups = listOf(primaryMuscle, "Full Body").distinct(),
        equipment = equipment,
        category = "FULL_BODY",
        movementPattern = movementPattern,
        difficulty = difficulty,
        instructions = executionSteps,
        setupSteps = setupSteps,
        executionSteps = executionSteps,
        breathing = breathing,
        formTips = safetyNotes,
        commonMistakes = commonMistakes,
        defaultSets = defaultSets,
        defaultReps = defaultReps,
        defaultRestSeconds = defaultRestSeconds,
        media = ExerciseMedia(mediaType = "IMAGE"),
        isActive = true,
        version = 1
    )

    // =========================================================================
    // LEVEL 1: NO EXPERIENCE (Page 2 of PDF — 17 Exercises)
    // =========================================================================

    val NO_EXPERIENCE_EXERCISES: List<Exercise> = listOf(
        createFbExercise(
            id = "fb_noexp_bodyweight_squat",
            name = "Bodyweight Squat",
            primaryMuscle = "Legs",
            equipment = listOf("Bodyweight"),
            defaultSets = 3,
            defaultReps = "10–15",
            defaultRestSeconds = 75,
            difficulty = "NO_EXPERIENCE",
            movementPattern = MovementPattern.SQUAT.name,
            imageRes = Res.drawable.female_air_squat,
            setupSteps = listOf("Stand with feet shoulder-width apart, toes pointed slightly outward.", "Keep your chest tall and core gently braced with hands clasped in front."),
            executionSteps = listOf("Starting position: upright stance with weight balanced over mid-foot.", "Controlled eccentric phase: hinge hips back and bend knees, lowering hips towards knee level.", "Transition: pause briefly at the bottom without collapsing your torso.", "Concentric phase: push the floor away through midfoot and heels.", "Return to start: stand tall and squeeze glutes at the top."),
            breathing = "Inhale on the way down; exhale smoothly as you push up.",
            commonMistakes = listOf("Knees caving inward", "Heels lifting off the ground", "Excessive forward torso lean"),
            safetyNotes = listOf("Keep knees tracking in the same direction as your toes. Do not round lower back.")
        ),
        createFbExercise(
            id = "fb_noexp_box_squat",
            name = "Box Squat",
            primaryMuscle = "Legs",
            equipment = listOf("Bodyweight/Box"),
            defaultSets = 3,
            defaultReps = "10–15",
            defaultRestSeconds = 75,
            difficulty = "NO_EXPERIENCE",
            movementPattern = MovementPattern.SQUAT.name,
            imageRes = Res.drawable.female_air_squat,
            setupSteps = listOf("Position a sturdy box or bench behind you at knee height.", "Stand with feet shoulder-width apart and toes angled slightly out."),
            executionSteps = listOf("Starting position: standing upright directly in front of the box.", "Controlled eccentric phase: sit back under control towards the box.", "Transition: lightly touch the box with your glutes without relaxing your core tension.", "Concentric phase: drive through your heels and stand up powerfully.", "Return to start: fully extend hips at top."),
            breathing = "Inhale as you sit back to the box; exhale as you stand up.",
            commonMistakes = listOf("Plummeting or rocking onto the box", "Losing abdominal brace on contact"),
            safetyNotes = listOf("Maintain spinal neutrality throughout the touch-and-go contact with the box.")
        ),
        createFbExercise(
            id = "fb_noexp_leg_press",
            name = "Leg Press",
            primaryMuscle = "Quads/Glutes",
            equipment = listOf("Machine"),
            defaultSets = 3,
            defaultReps = "10–15",
            defaultRestSeconds = 75,
            difficulty = "NO_EXPERIENCE",
            movementPattern = MovementPattern.SQUAT.name,
            imageRes = Res.drawable.gym_leg_press,
            setupSteps = listOf("Sit back against the angled pad with your lower back flat.", "Place feet shoulder-width apart on the sled platform."),
            executionSteps = listOf("Starting position: legs extended without locking knees, releasing safety handles.", "Controlled eccentric phase: bend knees to lower sled until knees reach approximately 90 degrees.", "Transition: momentary controlled stop without letting hips lift from the seat.", "Concentric phase: press sled upward through full foot.", "Return to start: extend legs stopping just short of knee lockout."),
            breathing = "Inhale as the weight sled descends; exhale during the upward press.",
            commonMistakes = listOf("Locking knees hyperextended at top", "Allowing lower back to curl off the pad"),
            safetyNotes = listOf("Never lock your knees at full extension. Keep your tailbone anchored to the seat.")
        ),
        createFbExercise(
            id = "fb_noexp_lying_leg_curl",
            name = "Lying Leg Curl",
            primaryMuscle = "Hamstrings",
            equipment = listOf("Machine"),
            defaultSets = 3,
            defaultReps = "10–15",
            defaultRestSeconds = 75,
            difficulty = "NO_EXPERIENCE",
            movementPattern = MovementPattern.EXTENSION.name,
            imageRes = Res.drawable.gym_hamstring_curl,
            setupSteps = listOf("Lie facedown on the bench with the roller pad positioned just below your calf muscles.", "Grip the front handles for stability."),
            executionSteps = listOf("Starting position: legs extended straight with pad resting above heels.", "Controlled eccentric phase: lower the weight smoothly back to straight legs after curl.", "Transition: hold full stretch for a split second.", "Concentric phase: curl heels towards your glutes by flexing the hamstrings.", "Return to start: resist the load back down under control."),
            breathing = "Exhale as you curl your legs up; inhale as you lower the weight back.",
            commonMistakes = listOf("Hips lifting up off the bench pad", "Using momentum to swing the weight"),
            safetyNotes = listOf("Keep your pelvis firmly pressed into the bench pad to prevent lumbar hyperextension.")
        ),
        createFbExercise(
            id = "fb_noexp_machine_chest_press",
            name = "Machine Chest Press",
            primaryMuscle = "Chest",
            equipment = listOf("Machine"),
            defaultSets = 3,
            defaultReps = "10–15",
            defaultRestSeconds = 75,
            difficulty = "NO_EXPERIENCE",
            movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
            imageRes = Res.drawable.gym_bench_press,
            setupSteps = listOf("Adjust seat height so handles align with mid-chest.", "Plant feet flat on the floor and pull shoulder blades back into the pad."),
            executionSteps = listOf("Starting position: elbows bent, gripping handles at chest level.", "Controlled eccentric phase: lower handles back until chest reaches a comfortable stretch.", "Transition: brief pause before driving forward.", "Concentric phase: push handles forward until arms are almost straight.", "Return to start: slowly return handles to initial position."),
            breathing = "Exhale as you press forward; inhale as handles return back.",
            commonMistakes = listOf("Shoulders rolling forward out of the socket", "Elbows flaring too high"),
            safetyNotes = listOf("Keep shoulder blades retracted and depressed against the back pad throughout.")
        ),
        createFbExercise(
            id = "fb_noexp_incline_machine_press",
            name = "Incline Machine Press",
            primaryMuscle = "Upper Chest",
            equipment = listOf("Machine"),
            defaultSets = 3,
            defaultReps = "10–15",
            defaultRestSeconds = 75,
            difficulty = "NO_EXPERIENCE",
            movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
            imageRes = Res.drawable.gym_incline_dumbbell_press,
            setupSteps = listOf("Adjust seat so grips align with your clavicles / upper chest.", "Maintain feet firmly on the ground with back supported."),
            executionSteps = listOf("Starting position: hands on incline handles at upper chest height.", "Controlled eccentric phase: return under control feeling upper chest stretch.", "Transition: smooth reversal without bouncing.", "Concentric phase: press diagonally upward along the machine track.", "Return to start: lower slowly under 2-second negative."),
            breathing = "Exhale while pressing upward; inhale on the downward return.",
            commonMistakes = listOf("Arching lower back excessively", "Flaring elbows past 90 degrees"),
            safetyNotes = listOf("Keep back flat against the incline pad and wrists straight.")
        ),
        createFbExercise(
            id = "fb_noexp_lat_pulldown",
            name = "Lat Pulldown",
            primaryMuscle = "Back",
            equipment = listOf("Cable Machine"),
            defaultSets = 3,
            defaultReps = "10–15",
            defaultRestSeconds = 75,
            difficulty = "NO_EXPERIENCE",
            movementPattern = MovementPattern.VERTICAL_PULL.name,
            imageRes = Res.drawable.gym_lat_pulldown,
            setupSteps = listOf("Adjust the thigh pad so your legs are locked securely.", "Grip the wide bar with an overhand grip slightly wider than shoulder width."),
            executionSteps = listOf("Starting position: arms fully extended upward with chest proud.", "Controlled eccentric phase: allow bar to rise smoothly back overhead.", "Transition: feel full lat stretch at top without shrugging ears.", "Concentric phase: pull elbows down and back, bringing bar to upper chest.", "Return to start: reach full extension with control."),
            breathing = "Exhale as you pull the bar down; inhale as it rises.",
            commonMistakes = listOf("Leaning back excessively to yank the weight", "Pulling bar behind the neck"),
            safetyNotes = listOf("Always pull to the front of the chest, never behind the neck.")
        ),
        createFbExercise(
            id = "fb_noexp_seated_cable_row",
            name = "Seated Cable Row",
            primaryMuscle = "Back",
            equipment = listOf("Cable Machine"),
            defaultSets = 3,
            defaultReps = "10–15",
            defaultRestSeconds = 75,
            difficulty = "NO_EXPERIENCE",
            movementPattern = MovementPattern.HORIZONTAL_PULL.name,
            imageRes = Res.drawable.gym_seated_cable_row,
            setupSteps = listOf("Sit on the row bench with knees slightly bent and feet on the footplates.", "Grip the V-bar handle with a neutral grip and sit tall."),
            executionSteps = listOf("Starting position: torso upright, arms extended forward holding handle.", "Controlled eccentric phase: let arms straighten slowly under cable tension.", "Transition: pause briefly at full stretch keeping posture upright.", "Concentric phase: drive elbows back alongside ribs, squeezing mid-back.", "Return to start: control the return without rounding your spine."),
            breathing = "Exhale as you row inward; inhale as you release forward.",
            commonMistakes = listOf("Rocking back and forth from the lower back", "Shrugging shoulders into ears"),
            safetyNotes = listOf("Maintain a neutral spine and slight bend in knees throughout the movement.")
        ),
        createFbExercise(
            id = "fb_noexp_machine_shoulder_press",
            name = "Machine Shoulder Press",
            primaryMuscle = "Shoulders",
            equipment = listOf("Machine"),
            defaultSets = 3,
            defaultReps = "10–15",
            defaultRestSeconds = 75,
            difficulty = "NO_EXPERIENCE",
            movementPattern = MovementPattern.VERTICAL_PUSH.name,
            imageRes = Res.drawable.gym_shoulder_press,
            setupSteps = listOf("Adjust seat height so handles sit at ear to chin level.", "Plant feet flat on the ground and press back against the support pad."),
            executionSteps = listOf("Starting position: holding handles at shoulder height with neutral or pronated grip.", "Controlled eccentric phase: lower handles back to ear level over 2 seconds.", "Transition: steady turn without resting weights on the stack.", "Concentric phase: press overhead until arms are nearly straight.", "Return to start: lower under control."),
            breathing = "Exhale as you press overhead; inhale as you lower the handles.",
            commonMistakes = listOf("Hyperextending lower back off the pad", "Letting elbows flare excessively backward"),
            safetyNotes = listOf("Keep ribs tucked down and core engaged to protect the lumbar spine.")
        ),
        createFbExercise(
            id = "fb_noexp_cable_lateral_raise",
            name = "Cable Lateral Raise",
            primaryMuscle = "Side Delts",
            equipment = listOf("Cable"),
            defaultSets = 3,
            defaultReps = "12–15",
            defaultRestSeconds = 60,
            difficulty = "NO_EXPERIENCE",
            movementPattern = MovementPattern.ISOLATION.name,
            imageRes = Res.drawable.gym_lateral_raise,
            setupSteps = listOf("Set cable pulley to lowest position with a single handle.", "Stand sideways to the pulley and grasp the handle with your outside hand."),
            executionSteps = listOf("Starting position: handle held in front of hips with a slight bend in elbow.", "Controlled eccentric phase: lower handle back across body slowly.", "Transition: brief pause before initiating next rep.", "Concentric phase: raise arm out to the side until parallel to the floor.", "Return to start: lower with steady control."),
            breathing = "Exhale as you raise your arm; inhale as you lower it.",
            commonMistakes = listOf("Using body momentum or swinging hips", "Raising elbow significantly above shoulder level"),
            safetyNotes = listOf("Lead with your elbow, not your wrist, to properly engage the lateral deltoid.")
        ),
        createFbExercise(
            id = "fb_noexp_cable_biceps_curl",
            name = "Cable Biceps Curl",
            primaryMuscle = "Biceps",
            equipment = listOf("Cable"),
            defaultSets = 3,
            defaultReps = "10–15",
            defaultRestSeconds = 60,
            difficulty = "NO_EXPERIENCE",
            movementPattern = MovementPattern.ISOLATION.name,
            imageRes = Res.drawable.gym_ez_bar_curl,
            setupSteps = listOf("Attach a straight or EZ-curl bar to the lowest cable pulley.", "Stand facing the machine holding the bar with an underhand grip shoulder-width apart."),
            executionSteps = listOf("Starting position: arms fully extended at your sides, chest upright.", "Controlled eccentric phase: lower bar smoothly until arms are straight.", "Transition: pause for a split-second at the bottom stretch.", "Concentric phase: curl bar upward towards shoulders while keeping elbows pinned.", "Return to start: extend arms under resistance."),
            breathing = "Exhale as you curl the weight; inhale as you lower it.",
            commonMistakes = listOf("Swinging elbows forward or backward", "Leaning back to cheat the weight up"),
            safetyNotes = listOf("Keep upper arms pinned against the sides of your ribcage.")
        ),
        createFbExercise(
            id = "fb_noexp_cable_triceps_pushdown",
            name = "Cable Triceps Pushdown",
            primaryMuscle = "Triceps",
            equipment = listOf("Cable"),
            defaultSets = 3,
            defaultReps = "10–15",
            defaultRestSeconds = 60,
            difficulty = "NO_EXPERIENCE",
            movementPattern = MovementPattern.ISOLATION.name,
            imageRes = Res.drawable.gym_tricep_pushdown,
            setupSteps = listOf("Attach a straight bar or rope to a high cable pulley.", "Grip the attachment with elbows bent at 90 degrees at your sides."),
            executionSteps = listOf("Starting position: elbows tucked tightly to ribs, forearms parallel to floor.", "Controlled eccentric phase: allow forearms to rise back to 90 degrees.", "Transition: feel stretch at top without moving upper arms.", "Concentric phase: push down until arms are completely locked out.", "Return to start: return slowly under control."),
            breathing = "Exhale as you push down to lockout; inhale as you release upward.",
            commonMistakes = listOf("Letting elbows flare outwards or drift forward", "Using bodyweight to press down"),
            safetyNotes = listOf("Keep upper body stationary; only your forearms should move.")
        ),
        createFbExercise(
            id = "fb_noexp_glute_bridge",
            name = "Glute Bridge",
            primaryMuscle = "Glutes",
            equipment = listOf("Bodyweight"),
            defaultSets = 3,
            defaultReps = "12–15",
            defaultRestSeconds = 60,
            difficulty = "NO_EXPERIENCE",
            movementPattern = MovementPattern.HINGE.name,
            imageRes = Res.drawable.female_glute_bridge,
            setupSteps = listOf("Lie on your back on a mat with knees bent and feet flat on the floor hip-width apart.", "Place arms flat at your sides."),
            executionSteps = listOf("Starting position: back flat against the floor, heels dug in close to glutes.", "Controlled eccentric phase: lower hips down smoothly until tailbone touches mat.", "Transition: gentle tap on mat before immediately driving up.", "Concentric phase: drive through your heels and lift hips into a straight diagonal line.", "Return to start: squeeze glutes hard at top for 1 second."),
            breathing = "Exhale as you bridge hips upward; inhale as you lower.",
            commonMistakes = listOf("Hyperextending lower back at top", "Pushing from toes instead of heels"),
            safetyNotes = listOf("Lock out with glutes and abs, not by overarching the lumbar spine.")
        ),
        createFbExercise(
            id = "fb_noexp_seated_calf_raise",
            name = "Seated Calf Raise",
            primaryMuscle = "Calves",
            equipment = listOf("Machine"),
            defaultSets = 3,
            defaultReps = "12–15",
            defaultRestSeconds = 60,
            difficulty = "NO_EXPERIENCE",
            movementPattern = MovementPattern.ISOLATION.name,
            imageRes = Res.drawable.female_standing_calf_raise,
            setupSteps = listOf("Sit in the machine and position the balls of your feet on the lower platform.", "Lower the thigh pad over your lower thighs and release the safety lever."),
            executionSteps = listOf("Starting position: heels hanging down in a comfortable calf stretch.", "Controlled eccentric phase: lower heels down deeply over 2 seconds.", "Transition: hold deep stretch at the bottom for 1 second.", "Concentric phase: push through the balls of your feet to raise heels as high as possible.", "Return to start: hold peak contraction for 1 second before lowering."),
            breathing = "Exhale as you raise your heels; inhale as you lower them.",
            commonMistakes = listOf("Bouncing the weight using Achilles tendon rebound", "Incomplete range of motion"),
            safetyNotes = listOf("Avoid bouncing at the bottom of the stretch to protect the Achilles tendon.")
        ),
        createFbExercise(
            id = "fb_noexp_machine_crunch",
            name = "Machine Crunch",
            primaryMuscle = "Core",
            equipment = listOf("Machine"),
            defaultSets = 3,
            defaultReps = "12–15",
            defaultRestSeconds = 60,
            difficulty = "NO_EXPERIENCE",
            movementPattern = MovementPattern.FLEXION.name,
            imageRes = Res.drawable.gym_cable_crunch,
            setupSteps = listOf("Sit in the crunch machine and hook your feet behind the ankle pads.", "Grasp the overhead or chest handles lightly."),
            executionSteps = listOf("Starting position: seated tall with abs engaged.", "Controlled eccentric phase: slowly return to upright starting position.", "Transition: pause before extending backward too far.", "Concentric phase: flex your abs and curl your ribcage down towards your pelvis.", "Return to start: squeeze abdominals at peak contraction for 1 second."),
            breathing = "Exhale completely as you curl forward; inhale on the release.",
            commonMistakes = listOf("Pulling with arms rather than contracting abs", "Over-extending lower back at start"),
            safetyNotes = listOf("Focus on spinal flexion initiated by the rectus abdominis, not pulling with the arms.")
        ),
        createFbExercise(
            id = "fb_noexp_dead_bug",
            name = "Dead Bug",
            primaryMuscle = "Core",
            equipment = listOf("Bodyweight"),
            defaultSets = 3,
            defaultReps = "8–12/side",
            defaultRestSeconds = 60,
            difficulty = "NO_EXPERIENCE",
            movementPattern = MovementPattern.ANTI_ROTATION.name,
            imageRes = Res.drawable.female_air_squat,
            setupSteps = listOf("Lie on your back with arms reaching toward the ceiling.", "Bend knees to 90 degrees with shins parallel to the floor (tabletop position)."),
            executionSteps = listOf("Starting position: lower back firmly pressed into the floor.", "Controlled eccentric phase: slowly extend right arm overhead and left leg out straight.", "Transition: hover just above the floor without arching lower back.", "Concentric phase: return right arm and left leg to starting tabletop position.", "Return to start: repeat on opposite side (left arm, right leg)."),
            breathing = "Exhale as limbs extend; inhale as you return to center.",
            commonMistakes = listOf("Allowing lower back to arch off the floor", "Moving limbs too fast without control"),
            safetyNotes = listOf("The lower back must remain flat against the floor at all times.")
        ),
        createFbExercise(
            id = "fb_noexp_cable_squat_to_row",
            name = "Cable Squat to Row",
            primaryMuscle = "Full Body",
            equipment = listOf("Cable"),
            defaultSets = 3,
            defaultReps = "10–12",
            defaultRestSeconds = 75,
            difficulty = "NO_EXPERIENCE",
            movementPattern = "COMPOUND",
            imageRes = Res.drawable.gym_clean_press,
            setupSteps = listOf("Attach a handle or straight bar to a cable pulley at mid-chest height.", "Step back to create tension with feet shoulder-width apart."),
            executionSteps = listOf("Starting position: standing tall, arms extended holding cable.", "Controlled eccentric phase: lower into a squat as arms remain extended.", "Transition: pause briefly in squat before standing.", "Concentric phase: push through feet to stand up while simultaneously rowing handle to chest.", "Return to start: smoothly extend arms and prepare for next squat descent."),
            breathing = "Inhale as you squat down; exhale as you stand and row.",
            commonMistakes = listOf("Rowing too early before hips are extended", "Losing balance backward or forward"),
            safetyNotes = listOf("Coordinate the movement so the leg drive powers the row smoothly.")
        )
    )

    // =========================================================================
    // LEVEL 2: BEGINNER (Page 3 of PDF — 21 Exercises)
    // =========================================================================

    val BEGINNER_EXERCISES: List<Exercise> = listOf(
        createFbExercise(
            id = "fb_beg_goblet_squat",
            name = "Goblet Squat",
            primaryMuscle = "Quads/Glutes",
            equipment = listOf("Dumbbell"),
            defaultSets = 3,
            defaultReps = "8–12",
            defaultRestSeconds = 90,
            difficulty = "BEGINNER",
            movementPattern = MovementPattern.SQUAT.name,
            imageRes = Res.drawable.female_goblet_squat,
            setupSteps = listOf("Hold a dumbbell vertically against your chest with both hands under the top plate.", "Stand with feet shoulder-width apart, toes turned slightly out."),
            executionSteps = listOf("Starting position: upright stance with dumbbell anchored at chest level.", "Controlled eccentric phase: sit back and down between knees until thighs are parallel.", "Transition: pause for 1 second maintaining upright posture.", "Concentric phase: drive through the midfoot to return to standing.", "Return to start: lock hips out at top with glutes squeezed."),
            breathing = "Inhale on the way down; exhale forcefully as you drive up.",
            commonMistakes = listOf("Dumbbell drifting away from chest", "Elbows flaring out instead of staying inside knees"),
            safetyNotes = listOf("Keep elbows inside knees at the bottom to maintain an upright thoracic spine.")
        ),
        createFbExercise(
            id = "fb_beg_leg_press",
            name = "Leg Press",
            primaryMuscle = "Quads/Glutes",
            equipment = listOf("Machine"),
            defaultSets = 3,
            defaultReps = "10–12",
            defaultRestSeconds = 90,
            difficulty = "BEGINNER",
            movementPattern = MovementPattern.SQUAT.name,
            imageRes = Res.drawable.gym_leg_press,
            setupSteps = listOf("Position yourself squarely on the leg press with back flat against the pad.", "Place feet shoulder-width in the center of the sled platform."),
            executionSteps = listOf("Starting position: legs extended with knees soft, releasing safety lever.", "Controlled eccentric phase: lower the sled under control until knees reach 90 degrees.", "Transition: smooth turn without bouncing off the stops.", "Concentric phase: press through the entire foot to drive the sled up.", "Return to start: stop just short of full knee lockout."),
            breathing = "Inhale on the descent; exhale on the press.",
            commonMistakes = listOf("Knees caving inwards under load", "Lifting pelvis off the backrest"),
            safetyNotes = listOf("Never lock your knees completely at the top.")
        ),
        createFbExercise(
            id = "fb_beg_bulgarian_split_squat",
            name = "Bulgarian Split Squat",
            primaryMuscle = "Quads/Glutes",
            equipment = listOf("Dumbbells"),
            defaultSets = 3,
            defaultReps = "8–12/leg",
            defaultRestSeconds = 90,
            difficulty = "BEGINNER",
            movementPattern = MovementPattern.LUNGE.name,
            imageRes = Res.drawable.female_bulgarian_split_squat,
            setupSteps = listOf("Stand two feet in front of a flat bench holding dumbbells at your sides.", "Reach one foot back and place top of foot flat on the bench."),
            executionSteps = listOf("Starting position: tall posture, weight centered over front foot.", "Controlled eccentric phase: lower hips down until front thigh is roughly parallel to floor.", "Transition: pause briefly at bottom stretch.", "Concentric phase: drive through the front heel to rise back up.", "Return to start: stand tall before next rep; complete all reps on one leg then switch."),
            breathing = "Inhale descending; exhale driving up.",
            commonMistakes = listOf("Front knee travelling excessively past toes", "Torso collapsing forward"),
            safetyNotes = listOf("Keep the front knee tracking over the second toe; maintain pelvic alignment.")
        ),
        createFbExercise(
            id = "fb_beg_dumbbell_romanian_deadlift",
            name = "Dumbbell Romanian Deadlift",
            primaryMuscle = "Hamstrings/Glutes",
            equipment = listOf("Dumbbells"),
            defaultSets = 3,
            defaultReps = "8–12",
            defaultRestSeconds = 90,
            difficulty = "BEGINNER",
            movementPattern = MovementPattern.HINGE.name,
            imageRes = Res.drawable.female_romanian_deadlift,
            setupSteps = listOf("Stand tall holding dumbbells in front of thighs, feet hip-width apart.", "Slightly unlock knees and retract shoulders."),
            executionSteps = listOf("Starting position: standing tall with core braced and lats engaged.", "Controlled eccentric phase: push hips straight back, sliding dumbbells down along shins.", "Transition: feel deep hamstring stretch below knees with flat spine.", "Concentric phase: drive hips forward and squeeze glutes to return upright.", "Return to start: stand tall without hyperextending lower back."),
            breathing = "Inhale as hips hinge back; exhale as hips drive forward.",
            commonMistakes = listOf("Rounding the lumbar spine", "Bending knees into a squat instead of hinging"),
            safetyNotes = listOf("Keep dumbbells tight to the legs throughout to minimize spinal lever arm.")
        ),
        createFbExercise(
            id = "fb_beg_seated_leg_curl",
            name = "Seated Leg Curl",
            primaryMuscle = "Hamstrings",
            equipment = listOf("Machine"),
            defaultSets = 3,
            defaultReps = "10–15",
            defaultRestSeconds = 75,
            difficulty = "BEGINNER",
            movementPattern = MovementPattern.EXTENSION.name,
            imageRes = Res.drawable.gym_hamstring_curl,
            setupSteps = listOf("Sit in the machine with back against pad and thigh clamp secured snugly.", "Position lower roller pad just behind your lower calves."),
            executionSteps = listOf("Starting position: legs extended forward, toes pointing straight up.", "Controlled eccentric phase: let the pad return slowly over 2-3 seconds.", "Transition: pause briefly at full stretch.", "Concentric phase: curl heels down and back under the seat.", "Return to start: hold contraction for 1 second before releasing."),
            breathing = "Exhale as you curl down; inhale as legs extend back.",
            commonMistakes = listOf("Allowing thighs to lift against the clamp", "Kicking heels instead of controlled curling"),
            safetyNotes = listOf("Ensure the knee joint axis aligns with the machine pivot point.")
        ),
        createFbExercise(
            id = "fb_beg_hip_thrust",
            name = "Hip Thrust",
            primaryMuscle = "Glutes",
            equipment = listOf("Barbell/Machine"),
            defaultSets = 3,
            defaultReps = "8–12",
            defaultRestSeconds = 90,
            difficulty = "BEGINNER",
            movementPattern = MovementPattern.HINGE.name,
            imageRes = Res.drawable.female_hip_thrust,
            setupSteps = listOf("Sit on the floor with upper back against a bench, barbell or machine pad across hips.", "Plant feet flat on the floor shoulder-width apart."),
            executionSteps = listOf("Starting position: hips lowered toward floor with chin tucked.", "Controlled eccentric phase: lower hips down with control.", "Transition: tap ground lightly before initiating extension.", "Concentric phase: drive through heels and extend hips until thighs and torso align.", "Return to start: hold peak glute squeeze for 1-2 seconds at top."),
            breathing = "Exhale as hips lift; inhale as hips lower.",
            commonMistakes = listOf("Looking at the ceiling instead of tucking chin", "Overarching lower back at top"),
            safetyNotes = listOf("Keep your chin tucked and ribs down to ensure glute recruitment rather than lumbar extension.")
        ),
        createFbExercise(
            id = "fb_beg_dumbbell_bench_press",
            name = "Dumbbell Bench Press",
            primaryMuscle = "Chest",
            equipment = listOf("Dumbbells/Bench"),
            defaultSets = 3,
            defaultReps = "8–12",
            defaultRestSeconds = 90,
            difficulty = "BEGINNER",
            movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
            imageRes = Res.drawable.female_dumbbell_bench_press,
            setupSteps = listOf("Lie flat on a bench holding dumbbells above chest with arms extended.", "Plant feet firmly on the floor and retract shoulder blades."),
            executionSteps = listOf("Starting position: dumbbells held above outer chest with wrists neutral.", "Controlled eccentric phase: lower dumbbells with elbows at a 45-degree angle.", "Transition: pause for 1 second at chest level with full stretch.", "Concentric phase: press dumbbells upward and slightly inward over chest.", "Return to start: avoid clanking dumbbells together at top."),
            breathing = "Inhale on the descent; exhale while pressing upward.",
            commonMistakes = listOf("Flaring elbows out at 90 degrees", "Bouncing dumbbells or clanking them at top"),
            safetyNotes = listOf("Keep elbows angled around 45–60 degrees from your torso to protect the rotator cuff.")
        ),
        createFbExercise(
            id = "fb_beg_incline_dumbbell_press",
            name = "Incline Dumbbell Press",
            primaryMuscle = "Upper Chest",
            equipment = listOf("Dumbbells/Bench"),
            defaultSets = 3,
            defaultReps = "8–12",
            defaultRestSeconds = 90,
            difficulty = "BEGINNER",
            movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
            imageRes = Res.drawable.gym_incline_dumbbell_press,
            setupSteps = listOf("Set bench to a 30–45 degree incline angle.", "Sit with dumbbells on knees, then kick them up to shoulder level as you lean back."),
            executionSteps = listOf("Starting position: dumbbells at upper chest level, palms facing forward.", "Controlled eccentric phase: lower weights smoothly to the sides of upper chest.", "Transition: brief pause with control.", "Concentric phase: press upward in a slight arc until arms are extended.", "Return to start: lower under 2-second eccentric cadence."),
            breathing = "Inhale as you lower; exhale as you press.",
            commonMistakes = listOf("Incline angle too steep (over 45 degrees shifts to shoulders)", "Wrists bending backwards"),
            safetyNotes = listOf("Keep wrists straight and directly above elbows throughout.")
        ),
        createFbExercise(
            id = "fb_beg_lat_pulldown",
            name = "Lat Pulldown",
            primaryMuscle = "Back",
            equipment = listOf("Cable Machine"),
            defaultSets = 3,
            defaultReps = "8–12",
            defaultRestSeconds = 90,
            difficulty = "BEGINNER",
            movementPattern = MovementPattern.VERTICAL_PULL.name,
            imageRes = Res.drawable.gym_lat_pulldown,
            setupSteps = listOf("Lock thighs beneath the support rollers with feet flat.", "Take a wide overhand grip on the lat bar."),
            executionSteps = listOf("Starting position: arms extended overhead, chest elevated.", "Controlled eccentric phase: let bar return up slowly under full lat stretch.", "Transition: pause briefly at full stretch.", "Concentric phase: pull elbows down and in toward ribs, bringing bar to collarbone.", "Return to start: extend arms back to start with control."),
            breathing = "Exhale pulling down; inhale returning up.",
            commonMistakes = listOf("Swinging torso backwards", "Pulling bar below the chest onto lap"),
            safetyNotes = listOf("Keep torso angle relatively stable with only a slight natural arch.")
        ),
        createFbExercise(
            id = "fb_beg_seated_cable_row",
            name = "Seated Cable Row",
            primaryMuscle = "Back",
            equipment = listOf("Cable Machine"),
            defaultSets = 3,
            defaultReps = "8–12",
            defaultRestSeconds = 90,
            difficulty = "BEGINNER",
            movementPattern = MovementPattern.HORIZONTAL_PULL.name,
            imageRes = Res.drawable.gym_seated_cable_row,
            setupSteps = listOf("Sit with feet on the footrests, knees slightly bent, back straight.", "Grip the double-D handle and sit upright."),
            executionSteps = listOf("Starting position: arms extended, shoulders relaxed forward slightly.", "Controlled eccentric phase: allow arms to extend forward smoothly.", "Transition: pause at full stretch without slouching.", "Concentric phase: pull handle towards lower abdomen, pinching shoulder blades.", "Return to start: control the weight forward."),
            breathing = "Exhale as you pull; inhale as you release.",
            commonMistakes = listOf("Using excessive momentum from hips", "Rounding shoulders forward at finish"),
            safetyNotes = listOf("Initiate the pull by retracting scapulae before bending elbows.")
        ),
        createFbExercise(
            id = "fb_beg_one_arm_dumbbell_row",
            name = "One-Arm Dumbbell Row",
            primaryMuscle = "Back",
            equipment = listOf("Dumbbell"),
            defaultSets = 3,
            defaultReps = "8–12/side",
            defaultRestSeconds = 90,
            difficulty = "BEGINNER",
            movementPattern = MovementPattern.HORIZONTAL_PULL.name,
            imageRes = Res.drawable.gym_barbell_row,
            setupSteps = listOf("Place one knee and same-side hand on a flat bench for support.", "Hold a dumbbell in opposite hand hanging straight down."),
            executionSteps = listOf("Starting position: flat spine parallel to floor, arm hanging with weight.", "Controlled eccentric phase: lower dumbbell smoothly back toward the floor.", "Transition: brief stretch at bottom.", "Concentric phase: row dumbbell toward hip crease, leading with elbow.", "Return to start: lower under control; repeat on other side."),
            breathing = "Exhale as you row upward; inhale as you lower.",
            commonMistakes = listOf("Rotating torso excessively to pull higher", "Pulling weight to shoulder instead of hip"),
            safetyNotes = listOf("Keep shoulders square to the bench throughout the row.")
        ),
        createFbExercise(
            id = "fb_beg_dumbbell_shoulder_press",
            name = "Dumbbell Shoulder Press",
            primaryMuscle = "Shoulders",
            equipment = listOf("Dumbbells"),
            defaultSets = 3,
            defaultReps = "8–12",
            defaultRestSeconds = 90,
            difficulty = "BEGINNER",
            movementPattern = MovementPattern.VERTICAL_PUSH.name,
            imageRes = Res.drawable.gym_shoulder_press,
            setupSteps = listOf("Sit on a bench with vertical back support holding dumbbells at shoulder level.", "Keep feet flat on the floor."),
            executionSteps = listOf("Starting position: dumbbells at ear level, elbows slightly in front of shoulders.", "Controlled eccentric phase: lower dumbbells back to ear height.", "Transition: pause without letting weights rest on shoulders.", "Concentric phase: press dumbbells overhead until arms are extended.", "Return to start: lower under control."),
            breathing = "Exhale on overhead press; inhale lowering.",
            commonMistakes = listOf("Flaring elbows directly out to the sides", "Arching lower back off the bench"),
            safetyNotes = listOf("Keep elbows angled slightly forward in the scapular plane.")
        ),
        createFbExercise(
            id = "fb_beg_dumbbell_lateral_raise",
            name = "Dumbbell Lateral Raise",
            primaryMuscle = "Side Delts",
            equipment = listOf("Dumbbells"),
            defaultSets = 3,
            defaultReps = "12–15",
            defaultRestSeconds = 60,
            difficulty = "BEGINNER",
            movementPattern = MovementPattern.ISOLATION.name,
            imageRes = Res.drawable.gym_lateral_raise,
            setupSteps = listOf("Stand tall holding light dumbbells at your sides.", "Slightly bend knees and keep a soft bend in elbows."),
            executionSteps = listOf("Starting position: weights hanging at sides with palms facing inward.", "Controlled eccentric phase: lower dumbbells slowly over 2 seconds.", "Transition: pause briefly at bottom without swinging.", "Concentric phase: raise arms out to the sides until parallel with floor.", "Return to start: lower with control."),
            breathing = "Exhale as you raise arms; inhale as you lower.",
            commonMistakes = listOf("Using too much weight and swinging torso", "Raising hands higher than elbows"),
            safetyNotes = listOf("Do not use heavy weights that force you to rock your body.")
        ),
        createFbExercise(
            id = "fb_beg_dumbbell_curl",
            name = "Dumbbell Curl",
            primaryMuscle = "Biceps",
            equipment = listOf("Dumbbells"),
            defaultSets = 3,
            defaultReps = "10–12",
            defaultRestSeconds = 60,
            difficulty = "BEGINNER",
            movementPattern = MovementPattern.ISOLATION.name,
            imageRes = Res.drawable.gym_ez_bar_curl,
            setupSteps = listOf("Stand holding dumbbells at your sides with palms facing each other (neutral grip).", "Keep chest upright and feet hip-width apart."),
            executionSteps = listOf("Starting position: arms hanging down with elbows locked at sides.", "Controlled eccentric phase: lower dumbbells under control to full arm extension.", "Transition: brief pause at bottom stretch.", "Concentric phase: curl weights upward while rotating palms to face up (supination).", "Return to start: squeeze biceps at top then lower."),
            breathing = "Exhale as you curl; inhale as you lower.",
            commonMistakes = listOf("Swinging elbows forward to assist the lift", "Dropping weights quickly on the eccentric phase"),
            safetyNotes = listOf("Keep elbows pinned to your sides to isolate the biceps.")
        ),
        createFbExercise(
            id = "fb_beg_hammer_curl",
            name = "Hammer Curl",
            primaryMuscle = "Biceps",
            equipment = listOf("Dumbbells"),
            defaultSets = 3,
            defaultReps = "10–12",
            defaultRestSeconds = 60,
            difficulty = "BEGINNER",
            movementPattern = MovementPattern.ISOLATION.name,
            imageRes = Res.drawable.gym_cable_hammer_curl,
            setupSteps = listOf("Stand with dumbbells at sides, palms facing each other in a neutral grip.", "Brace your core and roll shoulders back."),
            executionSteps = listOf("Starting position: neutral grip with arms extended.", "Controlled eccentric phase: lower dumbbells with control over 2 seconds.", "Transition: pause briefly at full extension.", "Concentric phase: curl dumbbells upward maintaining neutral palms.", "Return to start: contract brachialis at top before lowering."),
            breathing = "Exhale as you curl up; inhale on the descent.",
            commonMistakes = listOf("Rotating palms upward (breaking hammer grip)", "Using hip momentum"),
            safetyNotes = listOf("Maintain neutral wrists throughout the movement.")
        ),
        createFbExercise(
            id = "fb_beg_rope_triceps_pushdown",
            name = "Rope Triceps Pushdown",
            primaryMuscle = "Triceps",
            equipment = listOf("Cable"),
            defaultSets = 3,
            defaultReps = "10–15",
            defaultRestSeconds = 60,
            difficulty = "BEGINNER",
            movementPattern = MovementPattern.ISOLATION.name,
            imageRes = Res.drawable.gym_tricep_pushdown,
            setupSteps = listOf("Attach a rope to a high cable pulley.", "Grip the rope near the rubber stoppers and tuck elbows at sides."),
            executionSteps = listOf("Starting position: forearms parallel to floor, elbows pinned to ribs.", "Controlled eccentric phase: allow hands to rise back to elbow level.", "Transition: pause at 90 degrees.", "Concentric phase: push rope down and spread ends apart at bottom lockout.", "Return to start: return slowly under control."),
            breathing = "Exhale pushing down; inhale returning up.",
            commonMistakes = listOf("Elbows moving forward and backward", "Failing to spread rope ends at bottom"),
            safetyNotes = listOf("Spread rope ends apart at the bottom to achieve peak triceps contraction.")
        ),
        createFbExercise(
            id = "fb_beg_overhead_cable_extension",
            name = "Overhead Cable Extension",
            primaryMuscle = "Triceps",
            equipment = listOf("Cable"),
            defaultSets = 3,
            defaultReps = "10–15",
            defaultRestSeconds = 60,
            difficulty = "BEGINNER",
            movementPattern = MovementPattern.ISOLATION.name,
            imageRes = Res.drawable.gym_tricep_pushdown,
            setupSteps = listOf("Set cable pulley to mid or high position with rope attached.", "Turn facing away from the machine, stagger feet, and bring rope overhead."),
            executionSteps = listOf("Starting position: elbows bent overhead pointing forward, holding rope.", "Controlled eccentric phase: bend elbows allowing rope to stretch behind head.", "Transition: feel deep long-head triceps stretch.", "Concentric phase: extend arms forward and overhead until elbows lock.", "Return to start: return with control to stretched position."),
            breathing = "Exhale as arms extend; inhale as elbows bend.",
            commonMistakes = listOf("Flaring elbows wide out to sides", "Arching lower back under tension"),
            safetyNotes = listOf("Keep core braced and elbows pointing forward to protect shoulders.")
        ),
        createFbExercise(
            id = "fb_beg_standing_calf_raise",
            name = "Standing Calf Raise",
            primaryMuscle = "Calves",
            equipment = listOf("Machine/Bodyweight"),
            defaultSets = 3,
            defaultReps = "12–15",
            defaultRestSeconds = 60,
            difficulty = "BEGINNER",
            movementPattern = MovementPattern.ISOLATION.name,
            imageRes = Res.drawable.female_standing_calf_raise,
            setupSteps = listOf("Stand on a calf block or step with balls of feet, heels hanging off.", "Place shoulders under pads if using machine, or hold dumbbells."),
            executionSteps = listOf("Starting position: standing tall with heels level with the block.", "Controlled eccentric phase: lower heels down into a full calf stretch over 2 seconds.", "Transition: pause at full stretch for 1 second.", "Concentric phase: press through balls of feet to raise onto tiptoes.", "Return to start: hold top contraction for 1 second."),
            breathing = "Exhale rising on toes; inhale lowering heels.",
            commonMistakes = listOf("Bouncing at the bottom", "Bending knees during the calf raise"),
            safetyNotes = listOf("Keep knees slightly soft but stationary to target the gastrocnemius.")
        ),
        createFbExercise(
            id = "fb_beg_plank",
            name = "Plank",
            primaryMuscle = "Core",
            equipment = listOf("Bodyweight"),
            defaultSets = 3,
            defaultReps = "20–45 sec",
            defaultRestSeconds = 60,
            difficulty = "BEGINNER",
            movementPattern = MovementPattern.ANTI_ROTATION.name,
            imageRes = Res.drawable.workout_home,
            setupSteps = listOf("Lie facedown and prop yourself up on forearms and toes.", "Elbows directly under shoulders, feet hip-width apart."),
            executionSteps = listOf("Starting position: body forming a rigid straight line from head to heels.", "Controlled eccentric phase: hold isometric tension with glutes and abs contracted.", "Transition: maintain steady breathing throughout the duration.", "Concentric phase: active tension squeezing core toward spine.", "Return to start: lower knees gently to the floor when time is complete."),
            breathing = "Breathe steadily and rhythmically from the diaphragm.",
            commonMistakes = listOf("Hips sagging toward floor", "Piking hips too high in the air"),
            safetyNotes = listOf("Do not allow lower back to sag; maintain a neutral pelvis.")
        ),
        createFbExercise(
            id = "fb_beg_hanging_knee_raise",
            name = "Hanging Knee Raise",
            primaryMuscle = "Core",
            equipment = listOf("Pull-up Station"),
            defaultSets = 3,
            defaultReps = "8–15",
            defaultRestSeconds = 60,
            difficulty = "BEGINNER",
            movementPattern = MovementPattern.FLEXION.name,
            imageRes = Res.drawable.gym_hanging_leg_raise,
            setupSteps = listOf("Hang from a pull-up bar with an overhand grip, or use padded forearm supports.", "Legs hanging straight down."),
            executionSteps = listOf("Starting position: dead hang or supported hang with core engaged.", "Controlled eccentric phase: lower knees back down under control without swinging.", "Transition: pause at bottom to eliminate momentum.", "Concentric phase: raise knees toward chest by curling your pelvis up.", "Return to start: control the descent."),
            breathing = "Exhale as knees lift; inhale as legs lower.",
            commonMistakes = listOf("Swinging body back and forth", "Lifting knees with hip flexors only without curling pelvis"),
            safetyNotes = listOf("Focus on tilting the pelvis upward to activate the lower abdominals.")
        ),
        createFbExercise(
            id = "fb_beg_cable_crunch",
            name = "Cable Crunch",
            primaryMuscle = "Core",
            equipment = listOf("Cable"),
            defaultSets = 3,
            defaultReps = "10–15",
            defaultRestSeconds = 60,
            difficulty = "BEGINNER",
            movementPattern = MovementPattern.FLEXION.name,
            imageRes = Res.drawable.gym_cable_crunch,
            setupSteps = listOf("Attach rope to high pulley, kneel on floor a few feet from machine.", "Hold rope ends next to your ears or crown of head."),
            executionSteps = listOf("Starting position: kneeling upright with hips high and fixed.", "Controlled eccentric phase: slowly uncurl back to starting position.", "Transition: pause before extending backwards.", "Concentric phase: flex abs and crunch elbows toward knees.", "Return to start: hold peak contraction for 1 second."),
            breathing = "Exhale forcefully on crunch; inhale returning upright.",
            commonMistakes = listOf("Sitting back on heels like a prayer movement", "Pulling with arms instead of abs"),
            safetyNotes = listOf("Keep hips locked in place; movement must come from spinal flexion.")
        )
    )

    // =========================================================================
    // LEVEL 3: ADVANCED (Page 4 of PDF — 26 Exercises)
    // =========================================================================

    val ADVANCED_EXERCISES: List<Exercise> = listOf(
        createFbExercise(
            id = "fb_adv_barbell_back_squat",
            name = "Barbell Back Squat",
            primaryMuscle = "Quads/Glutes",
            equipment = listOf("Barbell/Rack"),
            defaultSets = 4,
            defaultReps = "6–10",
            defaultRestSeconds = 150,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.SQUAT.name,
            imageRes = Res.drawable.gym_barbell_squat,
            setupSteps = listOf("Set barbell at mid-chest height in rack; step under and rest bar across upper traps.", "Grip bar firmly, unrack, and take 2-3 steps back into shoulder-width stance."),
            executionSteps = listOf("Starting position: standing tall, chest proud, core braced with deep breath.", "Controlled eccentric phase: break at hips and knees simultaneously, descending below parallel.", "Transition: pause momentarily at bottom depth without losing tightness.", "Concentric phase: drive forcefully through midfoot, keeping chest up.", "Return to start: lock out hips and glutes at top."),
            breathing = "Inhale deep diaphragmatic breath into abdomen before descent; exhale past sticking point.",
            commonMistakes = listOf("Knees caving inward (valgus collapse)", "Rounding lumbar spine at bottom ('butt wink')"),
            safetyNotes = listOf("Always squat inside a power rack with safety pins set at appropriate depth.")
        ),
        createFbExercise(
            id = "fb_adv_front_squat",
            name = "Front Squat",
            primaryMuscle = "Quads/Core",
            equipment = listOf("Barbell/Rack"),
            defaultSets = 4,
            defaultReps = "6–10",
            defaultRestSeconds = 150,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.SQUAT.name,
            imageRes = Res.drawable.female_front_squat,
            setupSteps = listOf("Rest bar across anterior deltoids, keeping elbows high with clean grip or cross-arm grip.", "Step back into shoulder-width stance."),
            executionSteps = listOf("Starting position: upright torso, elbows pointing straight forward.", "Controlled eccentric phase: descend vertically by bending knees and hips.", "Transition: maintain high elbows at bottom depth.", "Concentric phase: drive through midfoot keeping elbows elevated.", "Return to start: stand tall and reset breath."),
            breathing = "Inhale into chest/core before descending; exhale as you stand.",
            commonMistakes = listOf("Elbows dropping, causing bar to roll forward", "Excessive forward torso tilt"),
            safetyNotes = listOf("Keep elbows high throughout to prevent bar from slipping off anterior delts.")
        ),
        createFbExercise(
            id = "fb_adv_hack_squat",
            name = "Hack Squat",
            primaryMuscle = "Quads/Glutes",
            equipment = listOf("Machine"),
            defaultSets = 4,
            defaultReps = "8–12",
            defaultRestSeconds = 120,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.SQUAT.name,
            imageRes = Res.drawable.gym_leg_press,
            setupSteps = listOf("Position back against pad with shoulders under shoulder cushions.", "Place feet shoulder-width on platform."),
            executionSteps = listOf("Starting position: legs extended, disengaging safety locks.", "Controlled eccentric phase: lower carriage until thighs break 90 degrees.", "Transition: smooth reversal without bouncing.", "Concentric phase: press through feet to drive carriage up.", "Return to start: stop just short of lockout."),
            breathing = "Inhale on descent; exhale on upward drive.",
            commonMistakes = listOf("Heels lifting off platform", "Locking knees aggressively"),
            safetyNotes = listOf("Keep entire foot in firm contact with platform throughout the range.")
        ),
        createFbExercise(
            id = "fb_adv_walking_dumbbell_lunges",
            name = "Walking Dumbbell Lunges",
            primaryMuscle = "Legs/Glutes",
            equipment = listOf("Dumbbells"),
            defaultSets = 3,
            defaultReps = "10–14/leg",
            defaultRestSeconds = 90,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.LUNGE.name,
            imageRes = Res.drawable.female_walking_lunge,
            setupSteps = listOf("Hold dumbbells at sides with tall posture, open walkway ahead."),
            executionSteps = listOf("Starting position: standing feet together, core braced.", "Controlled eccentric phase: step forward into a lunge, back knee tapping ground softly.", "Transition: pause briefly with weight centered.", "Concentric phase: push through front heel to step forward into next lunge.", "Return to start: continue walking forward alternating legs."),
            breathing = "Inhale as you step and lower; exhale as you push forward.",
            commonMistakes = listOf("Front knee banging floor", "Leaning torso excessively forward"),
            safetyNotes = listOf("Keep step length consistent and knee tracking over toes.")
        ),
        createFbExercise(
            id = "fb_adv_barbell_romanian_deadlift",
            name = "Barbell Romanian Deadlift",
            primaryMuscle = "Hamstrings/Glutes",
            equipment = listOf("Barbell"),
            defaultSets = 4,
            defaultReps = "6–10",
            defaultRestSeconds = 150,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.HINGE.name,
            imageRes = Res.drawable.female_romanian_deadlift,
            setupSteps = listOf("Hold barbell with overhand grip just outside thighs, feet hip-width.", "Slightly unlock knees and brace lats."),
            executionSteps = listOf("Starting position: standing tall with bar against thighs.", "Controlled eccentric phase: push hips back, sliding bar down along shins.", "Transition: feel stretch in hamstrings below knees.", "Concentric phase: drive hips forward and stand tall.", "Return to start: squeeze glutes at top."),
            breathing = "Inhale on hinge descent; exhale on hip drive.",
            commonMistakes = listOf("Bar drifting away from body", "Rounding upper or lower back"),
            safetyNotes = listOf("Keep bar in continuous contact with your legs to reduce lumbar strain.")
        ),
        createFbExercise(
            id = "fb_adv_nordic_hamstring_curl",
            name = "Nordic Hamstring Curl",
            primaryMuscle = "Hamstrings",
            equipment = listOf("Bodyweight/Anchor"),
            defaultSets = 3,
            defaultReps = "5–10",
            defaultRestSeconds = 120,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.EXTENSION.name,
            imageRes = Res.drawable.gym_hamstring_curl,
            setupSteps = listOf("Kneel on a soft pad with ankles securely anchored under a bar or by a partner."),
            executionSteps = listOf("Starting position: upright kneeling posture, hips fully extended.", "Controlled eccentric phase: lower torso toward floor as slowly as possible using hamstrings.", "Transition: catch yourself with hands as you reach floor.", "Concentric phase: push lightly off floor and pull back up with hamstrings.", "Return to start: return to upright kneeling."),
            breathing = "Inhale as you lower; exhale as you return up.",
            commonMistakes = listOf("Bending at the hips instead of knees", "Collapsing without eccentric resistance"),
            safetyNotes = listOf("Keep hips extended in a straight line with torso throughout the descent.")
        ),
        createFbExercise(
            id = "fb_adv_barbell_hip_thrust",
            name = "Barbell Hip Thrust",
            primaryMuscle = "Glutes",
            equipment = listOf("Barbell/Bench"),
            defaultSets = 4,
            defaultReps = "8–12",
            defaultRestSeconds = 120,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.HINGE.name,
            imageRes = Res.drawable.female_hip_thrust,
            setupSteps = listOf("Sit with upper back against bench, padded barbell over hip crease.", "Plant feet flat hip-width apart."),
            executionSteps = listOf("Starting position: hips low, chin tucked.", "Controlled eccentric phase: lower hips under control.", "Transition: pause just off the ground.", "Concentric phase: drive through heels and lift hips into full extension.", "Return to start: hold top contraction for 1 second."),
            breathing = "Exhale driving up; inhale lowering down.",
            commonMistakes = listOf("Arching back instead of tilting pelvis", "Feet placed too far forward"),
            safetyNotes = listOf("Use a thick barbell pad across the pelvic bones for comfort and safety.")
        ),
        createFbExercise(
            id = "fb_adv_bulgarian_split_squat",
            name = "Bulgarian Split Squat",
            primaryMuscle = "Quads/Glutes",
            equipment = listOf("Dumbbells"),
            defaultSets = 3,
            defaultReps = "8–12/leg",
            defaultRestSeconds = 105,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.LUNGE.name,
            imageRes = Res.drawable.female_bulgarian_split_squat,
            setupSteps = listOf("Rear foot elevated on bench, hold heavier dumbbells at sides.", "Front foot planted firmly forward."),
            executionSteps = listOf("Starting position: upright stance with rear foot on bench.", "Controlled eccentric phase: lower back knee toward floor under control.", "Transition: deep hip flexion stretch.", "Concentric phase: drive through front midfoot to stand.", "Return to start: complete prescribed reps per side."),
            breathing = "Inhale lowering; exhale driving up.",
            commonMistakes = listOf("Losing balance sideways", "Front heel lifting"),
            safetyNotes = listOf("Keep front foot flat and core engaged for stabilization.")
        ),
        createFbExercise(
            id = "fb_adv_barbell_bench_press",
            name = "Barbell Bench Press",
            primaryMuscle = "Chest/Triceps",
            equipment = listOf("Barbell/Bench"),
            defaultSets = 4,
            defaultReps = "6–10",
            defaultRestSeconds = 150,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
            imageRes = Res.drawable.gym_bench_press,
            setupSteps = listOf("Lie on bench with eyes under barbell, grip slightly wider than shoulder width.", "Retract scapulae and plant feet firmly."),
            executionSteps = listOf("Starting position: unrack bar and hold over mid-chest with arms locked.", "Controlled eccentric phase: lower bar under control to touch mid-chest.", "Transition: brief pause on chest without bouncing.", "Concentric phase: drive bar upward and back over shoulders.", "Return to start: lock out elbows over chest."),
            breathing = "Inhale on descent; exhale past the sticking point.",
            commonMistakes = listOf("Bouncing bar off sternum", "Flaring elbows at 90 degrees"),
            safetyNotes = listOf("Always bench with a spotter or safety catches when lifting heavy.")
        ),
        createFbExercise(
            id = "fb_adv_incline_barbell_bench_press",
            name = "Incline Barbell Bench Press",
            primaryMuscle = "Upper Chest",
            equipment = listOf("Barbell/Bench"),
            defaultSets = 4,
            defaultReps = "6–10",
            defaultRestSeconds = 120,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
            imageRes = Res.drawable.gym_incline_dumbbell_press,
            setupSteps = listOf("Set bench at 30-degree incline, grip barbell slightly wider than shoulder width."),
            executionSteps = listOf("Starting position: unrack and hold bar over upper chest.", "Controlled eccentric phase: lower bar to touch clavicles.", "Transition: pause under control on upper chest.", "Concentric phase: press bar straight up to lockout.", "Return to start: reset and repeat."),
            breathing = "Inhale on lowering; exhale on press.",
            commonMistakes = listOf("Lowering bar too low on stomach", "Lifting hips off the bench"),
            safetyNotes = listOf("Lower bar to the upper chest / clavicle area.")
        ),
        createFbExercise(
            id = "fb_adv_weighted_dips",
            name = "Weighted Dips",
            primaryMuscle = "Chest/Triceps",
            equipment = listOf("Dip Station"),
            defaultSets = 3,
            defaultReps = "6–12",
            defaultRestSeconds = 120,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.VERTICAL_PUSH.name,
            imageRes = Res.drawable.gym_tricep_dips,
            setupSteps = listOf("Mount parallel dip bars with dip belt or vest, arms extended.", "Lean torso slightly forward for chest emphasis."),
            executionSteps = listOf("Starting position: locked arms, shoulders packed down.", "Controlled eccentric phase: lower body until elbows reach 90 degrees.", "Transition: pause briefly at bottom stretch.", "Concentric phase: press through palms to return to top.", "Return to start: full elbow extension at top."),
            breathing = "Inhale on descent; exhale on press.",
            commonMistakes = listOf("Going too deep and stressing shoulders", "Shrugging shoulders into ears"),
            safetyNotes = listOf("Do not descend past 90 degrees if you experience shoulder discomfort.")
        ),
        createFbExercise(
            id = "fb_adv_weighted_pull_ups",
            name = "Weighted Pull-Ups",
            primaryMuscle = "Back/Biceps",
            equipment = listOf("Pull-up Bar"),
            defaultSets = 4,
            defaultReps = "5–10",
            defaultRestSeconds = 150,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.VERTICAL_PULL.name,
            imageRes = Res.drawable.gym_lat_pulldown,
            setupSteps = listOf("Attach weight plate with dip belt, take overhand grip on pull-up bar wider than shoulders."),
            executionSteps = listOf("Starting position: dead hang with arms fully extended.", "Controlled eccentric phase: lower body smoothly back to full hang.", "Transition: avoid swinging or kipping at bottom.", "Concentric phase: pull elbows down and back until chin clears the bar.", "Return to start: full dead hang stretch."),
            breathing = "Exhale pulling up; inhale lowering down.",
            commonMistakes = listOf("Kipping or using leg swings", "Incomplete range of motion"),
            safetyNotes = listOf("Ensure chin clears the bar and reach a full controlled dead hang.")
        ),
        createFbExercise(
            id = "fb_adv_barbell_bent_over_row",
            name = "Barbell Bent-Over Row",
            primaryMuscle = "Back",
            equipment = listOf("Barbell"),
            defaultSets = 4,
            defaultReps = "6–10",
            defaultRestSeconds = 120,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.HORIZONTAL_PULL.name,
            imageRes = Res.drawable.gym_barbell_row,
            setupSteps = listOf("Hinge at hips with knees slightly bent until torso is 45 degrees to floor.", "Grip bar shoulder-width overhand."),
            executionSteps = listOf("Starting position: bar hanging below knees, spine neutral.", "Controlled eccentric phase: lower bar under control to arms length.", "Transition: brief pause with lats flared.", "Concentric phase: pull bar to lower ribcage / navel, squeezing back.", "Return to start: lower smoothly."),
            breathing = "Exhale pulling bar in; inhale on release.",
            commonMistakes = listOf("Jerking torso upright during row", "Rounding lumbar spine"),
            safetyNotes = listOf("Maintain neutral spine and hinge angle throughout all reps.")
        ),
        createFbExercise(
            id = "fb_adv_t_bar_row",
            name = "T-Bar Row",
            primaryMuscle = "Back",
            equipment = listOf("T-Bar Machine"),
            defaultSets = 4,
            defaultReps = "8–12",
            defaultRestSeconds = 120,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.HORIZONTAL_PULL.name,
            imageRes = Res.drawable.gym_seated_cable_row,
            setupSteps = listOf("Straddle the T-bar platform, bend at hips and grip handles.", "Chest supported or free-standing with flat back."),
            executionSteps = listOf("Starting position: arms extended with weights hanging.", "Controlled eccentric phase: lower weight under control.", "Transition: pause at full stretch.", "Concentric phase: pull handles into upper abdomen.", "Return to start: control the descent."),
            breathing = "Exhale rowing; inhale lowering.",
            commonMistakes = listOf("Hyperextending spine at top", "Using momentum"),
            safetyNotes = listOf("Keep back flat and avoid jerking with lower back.")
        ),
        createFbExercise(
            id = "fb_adv_chest_supported_row",
            name = "Chest-Supported Row",
            primaryMuscle = "Back",
            equipment = listOf("Machine/Dumbbells"),
            defaultSets = 3,
            defaultReps = "8–12",
            defaultRestSeconds = 105,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.HORIZONTAL_PULL.name,
            imageRes = Res.drawable.gym_barbell_row,
            setupSteps = listOf("Lie chest-down against an incline bench holding dumbbells, or use machine chest pad."),
            executionSteps = listOf("Starting position: arms hanging straight down.", "Controlled eccentric phase: lower weights back down smoothly.", "Transition: feel stretch in mid-back.", "Concentric phase: pull elbows back squeezing shoulder blades.", "Return to start: lower under control."),
            breathing = "Exhale on pull; inhale on lower.",
            commonMistakes = listOf("Lifting chest off pad", "Flaring elbows excessively"),
            safetyNotes = listOf("Keep chest glued to the pad to eliminate cheating.")
        ),
        createFbExercise(
            id = "fb_adv_barbell_overhead_press",
            name = "Barbell Overhead Press",
            primaryMuscle = "Shoulders/Triceps",
            equipment = listOf("Barbell"),
            defaultSets = 4,
            defaultReps = "6–10",
            defaultRestSeconds = 120,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.VERTICAL_PUSH.name,
            imageRes = Res.drawable.gym_shoulder_press,
            setupSteps = listOf("Grip bar at shoulder width resting on anterior deltoids.", "Stand with feet hip-width, glutes and abs braced tight."),
            executionSteps = listOf("Starting position: bar in front rack position, chin tucked slightly.", "Controlled eccentric phase: lower bar under control back to clavicles.", "Transition: pause on collarbone.", "Concentric phase: press bar vertically, moving head through once bar clears forehead.", "Return to start: lockout overhead with bar over midfoot."),
            breathing = "Inhale at bottom; exhale as bar passes forehead.",
            commonMistakes = listOf("Leaning back and turning into an incline bench", "Pressing bar forward in an arc"),
            safetyNotes = listOf("Keep glutes and abs squeezed tight to protect the lumbar spine.")
        ),
        createFbExercise(
            id = "fb_adv_arnold_press",
            name = "Arnold Press",
            primaryMuscle = "Shoulders",
            equipment = listOf("Dumbbells"),
            defaultSets = 3,
            defaultReps = "8–12",
            defaultRestSeconds = 90,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.VERTICAL_PUSH.name,
            imageRes = Res.drawable.gym_shoulder_press,
            setupSteps = listOf("Sit on a bench with vertical back support holding dumbbells at chin level, palms facing you."),
            executionSteps = listOf("Starting position: palms facing chest, elbows tucked.", "Controlled eccentric phase: lower dumbbells while rotating palms back toward chest.", "Transition: brief pause with control.", "Concentric phase: press overhead while rotating wrists outward so palms face forward at top.", "Return to start: lockout overhead and reverse rotation on descent."),
            breathing = "Exhale pressing and rotating up; inhale lowering and rotating in.",
            commonMistakes = listOf("Rushing the rotation phase", "Using excessively heavy weights"),
            safetyNotes = listOf("Perform rotation smoothly without jerky wrist movements.")
        ),
        createFbExercise(
            id = "fb_adv_cable_lateral_raise",
            name = "Cable Lateral Raise",
            primaryMuscle = "Side Delts",
            equipment = listOf("Cable"),
            defaultSets = 3,
            defaultReps = "10–15",
            defaultRestSeconds = 60,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.ISOLATION.name,
            imageRes = Res.drawable.gym_lateral_raise,
            setupSteps = listOf("Set pulley low, stand perpendicular, hold handle behind back or across front."),
            executionSteps = listOf("Starting position: cable in tension, arm across torso.", "Controlled eccentric phase: lower handle smoothly.", "Transition: pause at full stretch.", "Concentric phase: raise arm out to side until parallel to floor.", "Return to start: lower with control."),
            breathing = "Exhale raising; inhale lowering.",
            commonMistakes = listOf("Shrugging trap to lift weight", "Swinging body"),
            safetyNotes = listOf("Keep tension continuous by avoiding letting the weight stack touch down.")
        ),
        createFbExercise(
            id = "fb_adv_ez_bar_curl",
            name = "EZ-Bar Curl",
            primaryMuscle = "Biceps",
            equipment = listOf("EZ Bar"),
            defaultSets = 3,
            defaultReps = "8–12",
            defaultRestSeconds = 75,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.ISOLATION.name,
            imageRes = Res.drawable.gym_ez_bar_curl,
            setupSteps = listOf("Grip EZ-bar on inner camber with supinated grip, stand tall with elbows pinned."),
            executionSteps = listOf("Starting position: arms hanging down with bar against thighs.", "Controlled eccentric phase: lower bar under 2-3 second control.", "Transition: pause at full extension.", "Concentric phase: curl bar up to shoulder level.", "Return to start: squeeze biceps hard at top."),
            breathing = "Exhale curling; inhale lowering.",
            commonMistakes = listOf("Swinging hips or leaning back", "Elbows drifting forward"),
            safetyNotes = listOf("Keep wrists locked in neutral alignment with forearms.")
        ),
        createFbExercise(
            id = "fb_adv_incline_dumbbell_curl",
            name = "Incline Dumbbell Curl",
            primaryMuscle = "Biceps",
            equipment = listOf("Dumbbells/Bench"),
            defaultSets = 3,
            defaultReps = "8–12",
            defaultRestSeconds = 75,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.ISOLATION.name,
            imageRes = Res.drawable.gym_incline_bicep_curl,
            setupSteps = listOf("Sit back on a 45-degree incline bench with dumbbells hanging straight down."),
            executionSteps = listOf("Starting position: arms hanging behind torso in deep bicep stretch.", "Controlled eccentric phase: lower dumbbells under control to full stretch.", "Transition: hold deep stretch for 1 second.", "Concentric phase: curl dumbbells upward while supinating wrists.", "Return to start: lower with strict control."),
            breathing = "Exhale on curl; inhale on lower.",
            commonMistakes = listOf("Shoulders rolling forward out of position", "Incomplete stretch at bottom"),
            safetyNotes = listOf("Do not hyperextend elbows at the bottom stretch.")
        ),
        createFbExercise(
            id = "fb_adv_close_grip_bench_press",
            name = "Close-Grip Bench Press",
            primaryMuscle = "Triceps/Chest",
            equipment = listOf("Barbell/Bench"),
            defaultSets = 3,
            defaultReps = "6–10",
            defaultRestSeconds = 120,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
            imageRes = Res.drawable.gym_bench_press,
            setupSteps = listOf("Lie on flat bench, grip barbell shoulder-width apart (hands ~12 inches apart)."),
            executionSteps = listOf("Starting position: bar held above chest with arms extended.", "Controlled eccentric phase: lower bar to lower chest, keeping elbows tucked close to sides.", "Transition: pause on lower sternum.", "Concentric phase: press bar straight up locking out triceps.", "Return to start: full triceps lockout."),
            breathing = "Inhale lowering; exhale pressing.",
            commonMistakes = listOf("Grip too narrow (causing wrist strain)", "Elbows flaring wide"),
            safetyNotes = listOf("Grip should be shoulder-width, never narrower than 8–10 inches.")
        ),
        createFbExercise(
            id = "fb_adv_skull_crushers",
            name = "Skull Crushers",
            primaryMuscle = "Triceps",
            equipment = listOf("EZ Bar/Bench"),
            defaultSets = 3,
            defaultReps = "8–12",
            defaultRestSeconds = 90,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.ISOLATION.name,
            imageRes = Res.drawable.gym_skull_crushers,
            setupSteps = listOf("Lie flat holding EZ-bar above chest, upper arms angled slightly back toward head."),
            executionSteps = listOf("Starting position: arms extended with slight backward angle.", "Controlled eccentric phase: bend elbows to lower bar towards forehead or crown.", "Transition: feel deep stretch in triceps.", "Concentric phase: extend elbows to drive bar back to start.", "Return to start: squeeze triceps at lockout."),
            breathing = "Inhale lowering; exhale extending.",
            commonMistakes = listOf("Elbows flaring wide out", "Moving upper arms back and forth"),
            safetyNotes = listOf("Keep upper arms stationary throughout; lower bar under strict control.")
        ),
        createFbExercise(
            id = "fb_adv_standing_calf_raise",
            name = "Standing Calf Raise",
            primaryMuscle = "Calves",
            equipment = listOf("Machine"),
            defaultSets = 4,
            defaultReps = "10–15",
            defaultRestSeconds = 75,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.ISOLATION.name,
            imageRes = Res.drawable.female_standing_calf_raise,
            setupSteps = listOf("Stand in calf machine with pads on shoulders, balls of feet on platform."),
            executionSteps = listOf("Starting position: heels hanging in deep stretch.", "Controlled eccentric phase: lower heels over 2-3 seconds.", "Transition: hold deep stretch at bottom for 1 second.", "Concentric phase: explode up onto toes.", "Return to start: hold peak contraction for 1 second."),
            breathing = "Exhale pushing up; inhale lowering.",
            commonMistakes = listOf("Bouncing off bottom stretch", "Bending knees"),
            safetyNotes = listOf("Control the turnaround at the bottom to protect the Achilles.")
        ),
        createFbExercise(
            id = "fb_adv_ab_wheel_rollout",
            name = "Ab Wheel Rollout",
            primaryMuscle = "Core",
            equipment = listOf("Ab Wheel"),
            defaultSets = 3,
            defaultReps = "6–12",
            defaultRestSeconds = 75,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.ANTI_ROTATION.name,
            imageRes = Res.drawable.gym_ab_wheel,
            setupSteps = listOf("Kneel on a soft pad with ab wheel under shoulders, arms straight.", "Tuck pelvis slightly (posterior pelvic tilt)."),
            executionSteps = listOf("Starting position: kneeling with core braced tight.", "Controlled eccentric phase: roll wheel forward slowly, extending body out.", "Transition: hold extended position without sagging.", "Concentric phase: pull wheel back by flexing abdominals and lats.", "Return to start: return to starting kneeling position."),
            breathing = "Inhale rolling out; exhale pulling back.",
            commonMistakes = listOf("Lower back sagging into hyperextension", "Bending elbows to pull"),
            safetyNotes = listOf("Never let your lower back sag; roll out only as far as you can maintain a flat back.")
        ),
        createFbExercise(
            id = "fb_adv_hanging_leg_raise",
            name = "Hanging Leg Raise",
            primaryMuscle = "Core",
            equipment = listOf("Pull-up Station"),
            defaultSets = 3,
            defaultReps = "8–15",
            defaultRestSeconds = 75,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.FLEXION.name,
            imageRes = Res.drawable.gym_hanging_leg_raise,
            setupSteps = listOf("Hang from pull-up bar with straight arms, legs extended."),
            executionSteps = listOf("Starting position: dead hang with legs straight.", "Controlled eccentric phase: lower legs slowly without swinging.", "Transition: pause at bottom to cancel momentum.", "Concentric phase: raise straight legs until parallel to floor or higher by curling pelvis.", "Return to start: lower with control."),
            breathing = "Exhale raising legs; inhale lowering.",
            commonMistakes = listOf("Swinging legs with momentum", "Lifting legs without curling pelvis"),
            safetyNotes = listOf("Focus on posterior pelvic tilt to activate rectus abdominis.")
        ),
        createFbExercise(
            id = "fb_adv_cable_woodchopper",
            name = "Cable Woodchopper",
            primaryMuscle = "Core/Obliques",
            equipment = listOf("Cable"),
            defaultSets = 3,
            defaultReps = "10–15/side",
            defaultRestSeconds = 60,
            difficulty = "ADVANCED",
            movementPattern = MovementPattern.ROTATION.name,
            imageRes = Res.drawable.gym_cable_crunch,
            setupSteps = listOf("Set cable high, stand sideways with feet wide, hold handle with both hands."),
            executionSteps = listOf("Starting position: arms extended high toward pulley.", "Controlled eccentric phase: return along the diagonal path under control.", "Transition: pause before next rep.", "Concentric phase: rotate torso and pull handle diagonally down across body to opposite hip.", "Return to start: pivot on back foot during turn."),
            breathing = "Exhale on downward chop; inhale on return.",
            commonMistakes = listOf("Bending arms and muscling with biceps", "Failing to pivot feet"),
            safetyNotes = listOf("Rotate from the thoracic spine and hips, keeping core braced.")
        )
    )

    // =========================================================================
    // LEVEL 4: EXPERT (Page 5 of PDF — 26 Exercises)
    // =========================================================================

    val EXPERT_EXERCISES: List<Exercise> = listOf(
        createFbExercise(
            id = "fb_exp_heavy_back_squat",
            name = "Heavy Back Squat",
            primaryMuscle = "Quads/Glutes",
            equipment = listOf("Barbell/Rack"),
            defaultSets = 4,
            defaultReps = "3–6",
            defaultRestSeconds = 210,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.SQUAT.name,
            imageRes = Res.drawable.gym_barbell_squat,
            setupSteps = listOf("Position barbell across upper traps, brace core with Valsalva maneuver, unrack firmly.", "Take two clean steps back into competition stance."),
            executionSteps = listOf("Starting position: fully locked and braced standing position.", "Controlled eccentric phase: descend under strict tension to below parallel.", "Transition: hit depth cleanly without losing abdominal pressure.", "Concentric phase: explode upward driving through midfoot.", "Return to start: stand tall and complete lockout."),
            breathing = "Deep diaphragmatic inhale and intra-abdominal brace before descent; exhale through teeth at top.",
            commonMistakes = listOf("Losing upper back tightness", "Shifting weight forward to toes"),
            safetyNotes = listOf("Always utilize heavy-duty safety spotter arms or qualified spotters.")
        ),
        createFbExercise(
            id = "fb_exp_pause_squat",
            name = "Pause Squat",
            primaryMuscle = "Quads/Glutes/Core",
            equipment = listOf("Barbell/Rack"),
            defaultSets = 4,
            defaultReps = "3–6",
            defaultRestSeconds = 180,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.SQUAT.name,
            imageRes = Res.drawable.gym_barbell_squat,
            setupSteps = listOf("Rack bar on back, step back into standard squat stance with core braced."),
            executionSteps = listOf("Starting position: upright stance with bar securely on traps.", "Controlled eccentric phase: descend to below parallel.", "Transition: hold a dead stop pause for 2 full seconds in the hole without relaxing.", "Concentric phase: drive aggressively out of the hole without bouncing.", "Return to start: lockout at top."),
            breathing = "Hold brace through the 2-second pause; exhale upon standing.",
            commonMistakes = listOf("Relaxing at the bottom", "Cutting the pause short"),
            safetyNotes = listOf("Maintain maximum isometric tightness during the pause at the bottom.")
        ),
        createFbExercise(
            id = "fb_exp_front_squat",
            name = "Front Squat",
            primaryMuscle = "Quads/Core",
            equipment = listOf("Barbell/Rack"),
            defaultSets = 4,
            defaultReps = "4–8",
            defaultRestSeconds = 150,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.SQUAT.name,
            imageRes = Res.drawable.female_front_squat,
            setupSteps = listOf("Barbell in clean grip on front delts, elbows parallel to floor.", "Stance shoulder-width with toes slightly out."),
            executionSteps = listOf("Starting position: tall chest, elbows elevated high.", "Controlled eccentric phase: sit down between knees with vertical spine.", "Transition: reach full depth while keeping elbows elevated.", "Concentric phase: drive through feet, keeping elbows up.", "Return to start: stand tall at lockout."),
            breathing = "Inhale deep into chest; exhale past midpoint.",
            commonMistakes = listOf("Dropping elbows forward", "Rounding thoracic spine"),
            safetyNotes = listOf("If wrist mobility is restricted, use straps or cross-arm grip.")
        ),
        createFbExercise(
            id = "fb_exp_conventional_deadlift",
            name = "Conventional Deadlift",
            primaryMuscle = "Posterior Chain",
            equipment = listOf("Barbell"),
            defaultSets = 4,
            defaultReps = "2–6",
            defaultRestSeconds = 240,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.HINGE.name,
            imageRes = Res.drawable.gym_trap_bar_deadlift,
            setupSteps = listOf("Feet hip-width apart under bar, shins 1 inch from bar.", "Hinge down, grip bar just outside knees, pull slack out of bar."),
            executionSteps = listOf("Starting position: hips set, lats engaged, spine neutral.", "Controlled eccentric phase: lower bar under control back to floor.", "Transition: bar comes to a complete dead stop on the floor.", "Concentric phase: push floor away with legs, driving hips forward into lockout.", "Return to start: stand tall with shoulders packed, then lower under control."),
            breathing = "Take deep breath into belly at bottom; brace and pull; exhale at lockout.",
            commonMistakes = listOf("Rounding lower back", "Jerking the bar off the floor without taking out slack"),
            safetyNotes = listOf("Never pull with a rounded lumbar spine; reset dead-stop every rep.")
        ),
        createFbExercise(
            id = "fb_exp_deficit_deadlift",
            name = "Deficit Deadlift",
            primaryMuscle = "Posterior Chain",
            equipment = listOf("Barbell/Platform"),
            defaultSets = 4,
            defaultReps = "3–6",
            defaultRestSeconds = 210,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.HINGE.name,
            imageRes = Res.drawable.female_deadlift,
            setupSteps = listOf("Stand on a 1–2 inch bumper plate or low platform, bar over midfoot.", "Set grip on bar and pull hips into deeper starting hinge."),
            executionSteps = listOf("Starting position: deep hinge start position with flat back.", "Controlled eccentric phase: lower bar through the increased range back to floor.", "Transition: full dead stop on floor.", "Concentric phase: push through legs with increased leg drive to break the floor.", "Return to start: lockout standing tall."),
            breathing = "Brace hard before lift; exhale at lockout.",
            commonMistakes = listOf("Allowing back to round due to deficit depth", "Using excessive platform height"),
            safetyNotes = listOf("Deficit should not exceed 1–2 inches to preserve lumbar safety.")
        ),
        createFbExercise(
            id = "fb_exp_barbell_walking_lunge",
            name = "Barbell Walking Lunge",
            primaryMuscle = "Legs/Glutes",
            equipment = listOf("Barbell"),
            defaultSets = 3,
            defaultReps = "8–12/leg",
            defaultRestSeconds = 120,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.LUNGE.name,
            imageRes = Res.drawable.female_walking_lunge,
            setupSteps = listOf("Barbell placed across upper back, clear walking pathway ahead.", "Stand tall with core braced."),
            executionSteps = listOf("Starting position: bar locked on traps, standing tall.", "Controlled eccentric phase: lunge forward, back knee kissing floor lightly.", "Transition: pause momentarily at bottom of stride.", "Concentric phase: drive through front heel to step directly into next forward lunge.", "Return to start: walk forward alternating legs under control."),
            breathing = "Inhale as you step down; exhale as you step up.",
            commonMistakes = listOf("Losing balance sideways with heavy bar", "Torso tilting forward"),
            safetyNotes = listOf("Keep steps controlled; use lighter weight until balance is mastered.")
        ),
        createFbExercise(
            id = "fb_exp_barbell_hip_thrust",
            name = "Barbell Hip Thrust",
            primaryMuscle = "Glutes",
            equipment = listOf("Barbell/Bench"),
            defaultSets = 4,
            defaultReps = "6–10",
            defaultRestSeconds = 150,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.HINGE.name,
            imageRes = Res.drawable.female_hip_thrust,
            setupSteps = listOf("Upper back across heavy bench, loaded barbell over hips with thick pad.", "Feet planted firmly at 90-degree knee angle."),
            executionSteps = listOf("Starting position: hips lowered near floor, chin tucked.", "Controlled eccentric phase: lower bar under control.", "Transition: pause briefly at bottom.", "Concentric phase: drive through heels into full hip extension.", "Return to start: hold top squeeze for 1-2 seconds."),
            breathing = "Exhale driving up; inhale lowering down.",
            commonMistakes = listOf("Hyperextending lumbar spine", "Feet too close or too far"),
            safetyNotes = listOf("Maintain eye contact forward and keep ribs tucked to avoid hyperextending back.")
        ),
        createFbExercise(
            id = "fb_exp_heavy_barbell_bench_press",
            name = "Heavy Barbell Bench Press",
            primaryMuscle = "Chest/Triceps",
            equipment = listOf("Barbell/Bench"),
            defaultSets = 4,
            defaultReps = "3–6",
            defaultRestSeconds = 180,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
            imageRes = Res.drawable.gym_bench_press,
            setupSteps = listOf("Lie on bench with tight arch, shoulder blades pinned together, firm leg drive.", "Grip bar tight outside shoulder width."),
            executionSteps = listOf("Starting position: unrack with arms locked over sternum.", "Controlled eccentric phase: lower bar under tight 2-second control to touch chest.", "Transition: clean touch without sinking or bouncing.", "Concentric phase: drive bar up violently while pushing feet into floor.", "Return to start: lockout elbows firmly."),
            breathing = "Hold breath and brace down; exhale forcefully through sticking point.",
            commonMistakes = listOf("Lifting hips off the bench", "Losing scapular retraction"),
            safetyNotes = listOf("Always utilize a spotter when lifting heavy low-rep loads.")
        ),
        createFbExercise(
            id = "fb_exp_incline_barbell_press",
            name = "Incline Barbell Press",
            primaryMuscle = "Upper Chest",
            equipment = listOf("Barbell/Bench"),
            defaultSets = 4,
            defaultReps = "4–8",
            defaultRestSeconds = 150,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
            imageRes = Res.drawable.gym_incline_dumbbell_press,
            setupSteps = listOf("Set incline bench to 30 degrees, grip bar with overhand grip.", "Retract shoulder blades into pad."),
            executionSteps = listOf("Starting position: bar locked out above upper chest.", "Controlled eccentric phase: lower bar to upper chest / clavicles.", "Transition: pause on upper chest with control.", "Concentric phase: press bar straight up to lockout.", "Return to start: repeat for prescribed reps."),
            breathing = "Inhale lowering; exhale pressing.",
            commonMistakes = listOf("Touching too low on sternum", "Elbows flaring too wide"),
            safetyNotes = listOf("Keep wrists stacked over elbows.")
        ),
        createFbExercise(
            id = "fb_exp_weighted_dips",
            name = "Weighted Dips",
            primaryMuscle = "Chest/Triceps",
            equipment = listOf("Dip Station"),
            defaultSets = 4,
            defaultReps = "5–10",
            defaultRestSeconds = 150,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.VERTICAL_PUSH.name,
            imageRes = Res.drawable.gym_tricep_dips,
            setupSteps = listOf("Mount dip bars with heavy plate on dip belt, arms locked.", "Lean torso slightly forward."),
            executionSteps = listOf("Starting position: arms extended, shoulders depressed.", "Controlled eccentric phase: lower body until elbows reach 90 degrees.", "Transition: pause at bottom depth.", "Concentric phase: press through palms to full lockout.", "Return to start: lockout elbows at top."),
            breathing = "Inhale on descent; exhale on press.",
            commonMistakes = listOf("Dropping too fast under heavy weight", "Excessive forward swinging"),
            safetyNotes = listOf("Ensure shoulders stay packed down throughout.")
        ),
        createFbExercise(
            id = "fb_exp_weighted_pull_up",
            name = "Weighted Pull-Up",
            primaryMuscle = "Back/Biceps",
            equipment = listOf("Pull-up Bar"),
            defaultSets = 4,
            defaultReps = "4–8",
            defaultRestSeconds = 150,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.VERTICAL_PULL.name,
            imageRes = Res.drawable.gym_lat_pulldown,
            setupSteps = listOf("Suspend heavy plate from dip belt, grasp bar with wide overhand grip.", "Hang in full dead hang."),
            executionSteps = listOf("Starting position: dead hang with lats engaged.", "Controlled eccentric phase: lower smoothly back to dead hang.", "Transition: pause at full stretch without swinging.", "Concentric phase: pull up aggressively until chin clears bar.", "Return to start: return to full extension."),
            breathing = "Exhale pulling up; inhale lowering down.",
            commonMistakes = listOf("Kipping with legs", "Incomplete range of motion"),
            safetyNotes = listOf("Maintain strict form without kicking legs.")
        ),
        createFbExercise(
            id = "fb_exp_pendlay_row",
            name = "Pendlay Row",
            primaryMuscle = "Back",
            equipment = listOf("Barbell"),
            defaultSets = 4,
            defaultReps = "5–8",
            defaultRestSeconds = 150,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.HORIZONTAL_PULL.name,
            imageRes = Res.drawable.gym_barbell_row,
            setupSteps = listOf("Barbell on floor, torso strictly parallel to ground with knees bent.", "Grip bar slightly wider than shoulder width."),
            executionSteps = listOf("Starting position: bar at rest on floor, spine horizontal and rigid.", "Controlled eccentric phase: lower bar back to the floor under control.", "Transition: bar comes to a dead stop on floor between every rep.", "Concentric phase: explode bar up into lower chest/upper abdomen.", "Return to start: rest on floor and reset brace."),
            breathing = "Inhale and brace at floor; exhale explosively on the pull.",
            commonMistakes = listOf("Rising torso up during the pull", "Bouncing bar off the floor"),
            safetyNotes = listOf("Torso must stay strictly parallel to the floor throughout each rep.")
        ),
        createFbExercise(
            id = "fb_exp_heavy_t_bar_row",
            name = "Heavy T-Bar Row",
            primaryMuscle = "Back",
            equipment = listOf("T-Bar"),
            defaultSets = 4,
            defaultReps = "6–10",
            defaultRestSeconds = 150,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.HORIZONTAL_PULL.name,
            imageRes = Res.drawable.gym_seated_cable_row,
            setupSteps = listOf("Straddle heavy T-bar, hinge at hips to 45 degrees, grip handles firmly."),
            executionSteps = listOf("Starting position: arms extended, spine flat.", "Controlled eccentric phase: lower weight slowly.", "Transition: pause at full stretch.", "Concentric phase: row weight up to chest with powerful back squeeze.", "Return to start: lower under control."),
            breathing = "Exhale pulling; inhale lowering.",
            commonMistakes = listOf("Jerking with lower back", "Rounding spine"),
            safetyNotes = listOf("Maintain strong core brace to protect lumbar spine.")
        ),
        createFbExercise(
            id = "fb_exp_push_press",
            name = "Push Press",
            primaryMuscle = "Shoulders/Full Body",
            equipment = listOf("Barbell"),
            defaultSets = 4,
            defaultReps = "3–6",
            defaultRestSeconds = 150,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.VERTICAL_PUSH.name,
            imageRes = Res.drawable.gym_clean_press,
            setupSteps = listOf("Barbell in front rack position across shoulders, feet hip-width apart.", "Torso upright and core braced."),
            executionSteps = listOf("Starting position: standing tall, bar resting on clavicles/delts.", "Controlled eccentric phase: lower bar back under control into front rack.", "Transition: dip knees 3–4 inches vertically without leaning forward.", "Concentric phase: explode up through legs and press bar overhead to lockout.", "Return to start: absorb bar smoothly at front rack."),
            breathing = "Inhale before dip; exhale as bar drives overhead.",
            commonMistakes = listOf("Dipping forward on toes", "Pressing before legs have fully extended"),
            safetyNotes = listOf("Keep torso vertical during the dip; drive through the heels.")
        ),
        createFbExercise(
            id = "fb_exp_strict_barbell_overhead_press",
            name = "Strict Barbell Overhead Press",
            primaryMuscle = "Shoulders/Triceps",
            equipment = listOf("Barbell"),
            defaultSets = 4,
            defaultReps = "4–8",
            defaultRestSeconds = 150,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.VERTICAL_PUSH.name,
            imageRes = Res.drawable.gym_shoulder_press,
            setupSteps = listOf("Grip bar at shoulder width resting on clavicles, feet hip-width.", "Zero knee dip allowed (strict press)."),
            executionSteps = listOf("Starting position: glutes and abs locked rock-solid.", "Controlled eccentric phase: lower bar under control back to collarbone.", "Transition: pause on chest.", "Concentric phase: press bar straight up, pulling head through at top.", "Return to start: lockout overhead over spine."),
            breathing = "Inhale deep at bottom; exhale as bar locks overhead.",
            commonMistakes = listOf("Bending knees (turning into push press)", "Excessive lumbar extension"),
            safetyNotes = listOf("Strictly avoid knee bend; rely purely on upper body pressing power.")
        ),
        createFbExercise(
            id = "fb_exp_heavy_dumbbell_press",
            name = "Heavy Dumbbell Press",
            primaryMuscle = "Shoulders",
            equipment = listOf("Dumbbells"),
            defaultSets = 4,
            defaultReps = "6–10",
            defaultRestSeconds = 120,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.VERTICAL_PUSH.name,
            imageRes = Res.drawable.gym_shoulder_press,
            setupSteps = listOf("Sit on vertical bench, kick heavy dumbbells up to shoulder level with knees."),
            executionSteps = listOf("Starting position: dumbbells at ear level, palms forward.", "Controlled eccentric phase: lower weights smoothly to ear height.", "Transition: pause without resting on shoulders.", "Concentric phase: press dumbbells overhead until arms are straight.", "Return to start: lower under control."),
            breathing = "Exhale pressing up; inhale lowering.",
            commonMistakes = listOf("Arching back away from pad", "Clanking dumbbells together at top"),
            safetyNotes = listOf("Keep core braced and wrists directly over elbows.")
        ),
        createFbExercise(
            id = "fb_exp_preacher_curl",
            name = "Preacher Curl",
            primaryMuscle = "Biceps",
            equipment = listOf("EZ Bar/Machine"),
            defaultSets = 3,
            defaultReps = "8–12",
            defaultRestSeconds = 90,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.ISOLATION.name,
            imageRes = Res.drawable.gym_ez_bar_curl,
            setupSteps = listOf("Sit at preacher bench with armpits snugly against top of angled pad.", "Grip EZ-bar with supinated grip."),
            executionSteps = listOf("Starting position: arms extended down the pad with slight elbow bend.", "Controlled eccentric phase: lower bar slowly feeling deep bicep stretch.", "Transition: pause before hyperextension.", "Concentric phase: curl bar upward until forearms are vertical.", "Return to start: squeeze biceps hard at top."),
            breathing = "Exhale curling; inhale lowering.",
            commonMistakes = listOf("Hyperextending elbows at the bottom", "Lifting body off the seat to yank weight"),
            safetyNotes = listOf("Never let elbows hyperextend aggressively at the bottom of the pad.")
        ),
        createFbExercise(
            id = "fb_exp_close_grip_bench_press",
            name = "Close-Grip Bench Press",
            primaryMuscle = "Triceps/Chest",
            equipment = listOf("Barbell/Bench"),
            defaultSets = 4,
            defaultReps = "5–8",
            defaultRestSeconds = 150,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.HORIZONTAL_PUSH.name,
            imageRes = Res.drawable.gym_bench_press,
            setupSteps = listOf("Grip barbell with shoulder-width hand spacing, lie flat on bench."),
            executionSteps = listOf("Starting position: bar unracked above chest.", "Controlled eccentric phase: lower bar to lower chest with elbows tucked.", "Transition: pause on sternum.", "Concentric phase: press bar straight up to lockout.", "Return to start: full lockout of triceps."),
            breathing = "Inhale lowering; exhale pressing.",
            commonMistakes = listOf("Hands touching (too close)", "Elbows flaring out"),
            safetyNotes = listOf("Keep hands roughly shoulder-width apart to protect wrists.")
        ),
        createFbExercise(
            id = "fb_exp_weighted_dips_triceps_focus",
            name = "Weighted Dips — Triceps Focus",
            primaryMuscle = "Triceps",
            equipment = listOf("Dip Station"),
            defaultSets = 3,
            defaultReps = "6–10",
            defaultRestSeconds = 120,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.VERTICAL_PUSH.name,
            imageRes = Res.drawable.gym_tricep_dips,
            setupSteps = listOf("Mount dip bars with weight belt, keep torso strictly upright.", "Legs straight down or crossed beneath body."),
            executionSteps = listOf("Starting position: arms locked out, upright vertical torso.", "Controlled eccentric phase: lower body with elbows tracking straight back.", "Transition: pause at 90-degree elbow bend.", "Concentric phase: drive through palms locking out triceps.", "Return to start: squeeze triceps hard at top."),
            breathing = "Inhale descending; exhale driving to lockout.",
            commonMistakes = listOf("Leaning forward (shifts emphasis to chest)", "Incomplete lockout at top"),
            safetyNotes = listOf("Keep torso as upright as possible to isolate the triceps.")
        ),
        createFbExercise(
            id = "fb_exp_heavy_standing_calf_raise",
            name = "Heavy Standing Calf Raise",
            primaryMuscle = "Calves",
            equipment = listOf("Machine"),
            defaultSets = 4,
            defaultReps = "8–12",
            defaultRestSeconds = 90,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.ISOLATION.name,
            imageRes = Res.drawable.female_standing_calf_raise,
            setupSteps = listOf("Load standing calf machine heavy, place balls of feet on edge.", "Shoulder pads secure."),
            executionSteps = listOf("Starting position: heels hanging in full deep stretch.", "Controlled eccentric phase: lower heels over 3 seconds into maximum stretch.", "Transition: hold deep stretch for 2 seconds.", "Concentric phase: drive up explosively onto big toes.", "Return to start: hold peak contraction for 1 second."),
            breathing = "Exhale pushing up; inhale lowering down.",
            commonMistakes = listOf("Bouncing the heavy stack", "Cutting the bottom stretch short"),
            safetyNotes = listOf("Do not bounce; control the heavy load through the full range.")
        ),
        createFbExercise(
            id = "fb_exp_weighted_hanging_leg_raise",
            name = "Weighted Hanging Leg Raise",
            primaryMuscle = "Core",
            equipment = listOf("Pull-up Station"),
            defaultSets = 4,
            defaultReps = "8–12",
            defaultRestSeconds = 90,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.FLEXION.name,
            imageRes = Res.drawable.gym_hanging_leg_raise,
            setupSteps = listOf("Hang from pull-up bar with a dumbbell gripped between feet or ankle weights attached."),
            executionSteps = listOf("Starting position: dead hang with weighted feet together.", "Controlled eccentric phase: lower legs with strict control over 2 seconds.", "Transition: pause at bottom without swinging.", "Concentric phase: raise legs until parallel or higher, curling pelvis up.", "Return to start: lower under control."),
            breathing = "Exhale raising legs; inhale lowering down.",
            commonMistakes = listOf("Swinging body with heavy weight", "Dumbbell slipping from feet"),
            safetyNotes = listOf("Secure the dumbbell firmly between your arches before starting.")
        ),
        createFbExercise(
            id = "fb_exp_ab_wheel_rollout",
            name = "Ab Wheel Rollout",
            primaryMuscle = "Core",
            equipment = listOf("Ab Wheel"),
            defaultSets = 3,
            defaultReps = "8–15",
            defaultRestSeconds = 90,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.ANTI_ROTATION.name,
            imageRes = Res.drawable.gym_ab_wheel,
            setupSteps = listOf("Kneel on pad holding wheel, brace core tight with posterior pelvic tilt."),
            executionSteps = listOf("Starting position: kneeling with hands under shoulders.", "Controlled eccentric phase: roll out until nose is inches from floor.", "Transition: hold full extension for a split second.", "Concentric phase: contract abs and lats to roll wheel back to knees.", "Return to start: return to start without arching back."),
            breathing = "Inhale rolling out; exhale contracting back.",
            commonMistakes = listOf("Lower back sagging", "Using hips to sit back instead of abs"),
            safetyNotes = listOf("Keep core hollowed out to avoid hyperextending lumbar spine.")
        ),
        createFbExercise(
            id = "fb_exp_barbell_rollout",
            name = "Barbell Rollout",
            primaryMuscle = "Core",
            equipment = listOf("Barbell"),
            defaultSets = 3,
            defaultReps = "6–12",
            defaultRestSeconds = 90,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.ANTI_ROTATION.name,
            imageRes = Res.drawable.gym_ab_wheel,
            setupSteps = listOf("Load Olympic barbell with round plates, kneel behind bar with shoulder-width overhand grip."),
            executionSteps = listOf("Starting position: kneeling with hands on barbell, arms straight.", "Controlled eccentric phase: roll barbell forward until torso is parallel to floor.", "Transition: pause at full horizontal extension.", "Concentric phase: pull barbell back toward knees using core and lats.", "Return to start: reset and repeat."),
            breathing = "Inhale rolling out; exhale pulling back.",
            commonMistakes = listOf("Hips sagging toward floor", "Losing shoulder stiffness"),
            safetyNotes = listOf("The barbell offers a wider grip than an ab wheel; maintain strict shoulder stability.")
        ),
        createFbExercise(
            id = "fb_exp_barbell_complex",
            name = "Barbell Complex",
            primaryMuscle = "Full Body",
            equipment = listOf("Barbell"),
            defaultSets = 4,
            defaultReps = "4–6 rounds",
            defaultRestSeconds = 150,
            difficulty = "EXPERT",
            movementPattern = "COMPOUND",
            imageRes = Res.drawable.gym_clean_press,
            setupSteps = listOf("Load barbell with moderate weight; bar must not leave hands until round is complete.", "Sequence: Deadlift -> Hang Clean -> Front Squat -> Push Press -> Back Squat."),
            executionSteps = listOf("Starting position: perform prescribed reps of each movement without setting bar down.", "Controlled eccentric phase: smooth transitions between Olympic and compound patterns.", "Transition: catch and reset quickly.", "Concentric phase: explosive power output through whole body chain.", "Return to start: rest 2–3 min between completed rounds."),
            breathing = "Maintain controlled breathing rhythm through each movement transition.",
            commonMistakes = listOf("Setting bar down mid-round", "Using too much weight causing form breakdown"),
            safetyNotes = listOf("Choose weight according to your weakest movement in the complex.")
        ),
        createFbExercise(
            id = "fb_exp_dumbbell_thruster",
            name = "Dumbbell Thruster",
            primaryMuscle = "Full Body",
            equipment = listOf("Dumbbells"),
            defaultSets = 4,
            defaultReps = "8–12",
            defaultRestSeconds = 105,
            difficulty = "EXPERT",
            movementPattern = "COMPOUND",
            imageRes = Res.drawable.goal_athlete,
            setupSteps = listOf("Stand with feet shoulder-width holding dumbbells at shoulder level."),
            executionSteps = listOf("Starting position: dumbbells resting on shoulders, core braced.", "Controlled eccentric phase: squat down to below parallel.", "Transition: explode up immediately out of the squat.", "Concentric phase: use leg drive to launch dumbbells overhead to lockout.", "Return to start: lower dumbbells back to shoulders as you descend into next squat."),
            breathing = "Inhale on squat descent; exhale forcefully overhead.",
            commonMistakes = listOf("Pausing between squat and press", "Pressing with arms before legs finish extending"),
            safetyNotes = listOf("The leg drive must propel the dumbbells overhead seamlessly.")
        ),
        createFbExercise(
            id = "fb_exp_kettlebell_swing",
            name = "Kettlebell Swing",
            primaryMuscle = "Full Body/Hamstrings",
            equipment = listOf("Kettlebell"),
            defaultSets = 4,
            defaultReps = "10–20",
            defaultRestSeconds = 75,
            difficulty = "EXPERT",
            movementPattern = MovementPattern.HINGE.name,
            imageRes = Res.drawable.female_kettlebell_swing,
            setupSteps = listOf("Stand with feet slightly wider than shoulders, kettlebell on floor 1 foot ahead.", "Hinge at hips, grasp handle with both hands."),
            executionSteps = listOf("Starting position: hike kettlebell back between upper thighs like a football snap.", "Controlled eccentric phase: allow bell to swing back between legs as hips hinge.", "Transition: catch momentum with loaded hamstrings and glutes.", "Concentric phase: snap hips forward violently, swinging kettlebell to chest level.", "Return to start: let bell float momentarily at chest before guiding back into hinge."),
            breathing = "Inhale on the back-swing; exhale sharply with hip snap.",
            commonMistakes = listOf("Squatting the kettlebell instead of hinging", "Lifting bell with arms and shoulders"),
            safetyNotes = listOf("The movement is a pure hip hinge; your arms merely act as ropes connecting to the bell.")
        )
    )

    // =========================================================================
    // COMBINED COMPLETE LIBRARY (90 Exercises)
    // =========================================================================

    val ALL_FULL_BODY_EXERCISES: List<Exercise> by lazy {
        NO_EXPERIENCE_EXERCISES + BEGINNER_EXERCISES + ADVANCED_EXERCISES + EXPERT_EXERCISES
    }

    val exercisesById: Map<String, Exercise> by lazy {
        val map = HashMap<String, Exercise>(ALL_FULL_BODY_EXERCISES.size)
        for (ex in ALL_FULL_BODY_EXERCISES) {
            map[ex.id.trim().lowercase()] = ex
        }
        map
    }

    val exercisesByNameNormalized: Map<String, Exercise> by lazy {
        val map = HashMap<String, Exercise>(ALL_FULL_BODY_EXERCISES.size)
        val regex = Regex("[^a-z0-9]")
        for (ex in ALL_FULL_BODY_EXERCISES) {
            val cleanName = ex.name.trim().lowercase().replace(regex, "")
            map.putIfAbsent(cleanName, ex)
        }
        map
    }

    fun findById(id: String): Exercise? {
        val clean = id.trim().lowercase()
        return exercisesById[clean] ?: ALL_FULL_BODY_EXERCISES.find { it.id.equals(clean, ignoreCase = true) }
    }

    fun findByName(name: String): Exercise? {
        val clean = name.trim().lowercase()
        val norm = clean.replace(Regex("[^a-z0-9]"), "")
        return exercisesByNameNormalized[norm]
            ?: ALL_FULL_BODY_EXERCISES.find { it.name.trim().equals(clean, ignoreCase = true) }
    }

    /**
     * Resolves the canonical Full Body exercises for a specific [ExperienceLevel].
     */
    fun getExercisesForLevel(level: ExperienceLevel): List<Exercise> = when (level) {
        ExperienceLevel.NO_EXPERIENCE -> NO_EXPERIENCE_EXERCISES
        ExperienceLevel.BEGINNER -> BEGINNER_EXERCISES
        ExperienceLevel.ADVANCED -> ADVANCED_EXERCISES
        ExperienceLevel.EXPERT -> EXPERT_EXERCISES
    }

    /**
     * Maps an [Exercise] to a user-facing [ExerciseItem] for workout displays.
     */
    fun toExerciseItem(exercise: Exercise, fallbackImage: DrawableResource = Res.drawable.workout_gym): ExerciseItem {
        val repsStr = exercise.defaultReps.ifBlank { "10-12" }
        val setsStr = "${exercise.defaultSets} sets × $repsStr"
        val durStr = "${(exercise.defaultSets * 2).coerceAtLeast(5)} min"
        val desc = exercise.description.ifBlank { "Full body exercise targeting ${exercise.primaryMuscle}." }
        val tips = if (exercise.formTips.isNotEmpty()) {
            exercise.formTips
        } else if (exercise.executionSteps.isNotEmpty()) {
            exercise.executionSteps
        } else {
            listOf("Maintain steady form and full range of motion", "Breathe smoothly throughout the movement")
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

    /**
     * Resolves [ExerciseItem] list for a specific [ExperienceLevel].
     */
    fun getExerciseItemsForLevel(level: ExperienceLevel): List<ExerciseItem> {
        val exercises = getExercisesForLevel(level)
        return exercises.map { ex ->
            val resolvedImage = resolveExerciseImage(ex.name, ex.id)
            toExerciseItem(ex, resolvedImage)
        }
    }

    /**
     * Selects the most accurate matching drawable resource from available assets.
     */
    fun resolveExerciseImage(name: String, id: String): DrawableResource {
        val lower = name.lowercase()
        val idLower = id.lowercase()

        return when {
            lower.contains("squat to row") -> Res.drawable.gym_clean_press
            lower.contains("back squat") || lower.contains("bodyweight squat") || lower.contains("box squat") -> Res.drawable.gym_barbell_squat
            lower.contains("front squat") -> Res.drawable.female_front_squat
            lower.contains("goblet squat") -> Res.drawable.female_goblet_squat
            lower.contains("split squat") || lower.contains("bulgarian") -> Res.drawable.female_bulgarian_split_squat
            lower.contains("lunge") -> Res.drawable.female_walking_lunge
            lower.contains("hack squat") || lower.contains("leg press") -> Res.drawable.gym_leg_press
            lower.contains("leg curl") || lower.contains("nordic") -> Res.drawable.gym_hamstring_curl
            lower.contains("hip thrust") || lower.contains("glute bridge") -> Res.drawable.female_hip_thrust
            lower.contains("bench press") || lower.contains("chest press") -> {
                if (lower.contains("incline")) Res.drawable.gym_incline_dumbbell_press
                else Res.drawable.gym_bench_press
            }
            lower.contains("lat pulldown") || lower.contains("pull-up") || lower.contains("pull up") -> Res.drawable.gym_lat_pulldown
            lower.contains("cable row") || lower.contains("t-bar") || lower.contains("bent-over row") || lower.contains("pendlay") || lower.contains("dumbbell row") -> {
                if (lower.contains("cable")) Res.drawable.gym_seated_cable_row
                else Res.drawable.gym_barbell_row
            }
            lower.contains("shoulder press") || lower.contains("overhead press") || lower.contains("arnold") -> Res.drawable.gym_shoulder_press
            lower.contains("lateral raise") -> Res.drawable.gym_lateral_raise
            lower.contains("biceps curl") || lower.contains("dumbbell curl") || lower.contains("ez-bar curl") || lower.contains("preacher") -> Res.drawable.gym_ez_bar_curl
            lower.contains("hammer curl") -> Res.drawable.gym_cable_hammer_curl
            lower.contains("pushdown") || lower.contains("cable extension") || lower.contains("skull crusher") -> Res.drawable.gym_tricep_pushdown
            lower.contains("dips") -> Res.drawable.gym_tricep_dips
            lower.contains("calf raise") -> Res.drawable.female_standing_calf_raise
            lower.contains("rollout") || lower.contains("ab wheel") -> Res.drawable.gym_ab_wheel
            lower.contains("hanging") && (lower.contains("leg raise") || lower.contains("knee raise")) -> Res.drawable.gym_hanging_leg_raise
            lower.contains("crunch") || lower.contains("woodchopper") -> Res.drawable.gym_cable_crunch
            lower.contains("deadlift") -> Res.drawable.gym_trap_bar_deadlift
            lower.contains("clean") || lower.contains("push press") || lower.contains("complex") || lower.contains("thruster") -> Res.drawable.gym_clean_press
            lower.contains("kettlebell swing") || lower.contains("kb swing") -> Res.drawable.female_kettlebell_swing
            lower.contains("plank") || lower.contains("dead bug") -> Res.drawable.workout_home
            else -> Res.drawable.workout_gym
        }
    }
}
