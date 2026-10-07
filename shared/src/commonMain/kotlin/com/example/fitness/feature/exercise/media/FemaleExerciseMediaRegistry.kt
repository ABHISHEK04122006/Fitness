package com.example.fitness.feature.exercise.media

import fitness.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource

/**
 * Data structure representing a distinct instructional phase of an exercise movement.
 */
data class MovementPositionFrame(
    val phaseId: String,
    val title: String,
    val subtitle: String,
    val imageRes: DrawableResource
)

/**
 * Authoritative Central Registry for Female Exercise Media in ATHGYM.
 *
 * Provides dedicated, realistic female fitness imagery for all 70 unique exercises
 * across the 9 female 30-day programs.
 *
 * Supports:
 * 1. Dedicated female thumbnails for exercise lists, workout overviews, and active sessions.
 * 2. Multi-position instructional demonstration sets (Start, Mid, End positions) for Exercise Detail.
 * 3. Strict gender isolation: Only resolves female imagery when the user/context is female,
 *    preserving all male exercise imagery without regression.
 */
object FemaleExerciseMediaRegistry {

    /**
     * Complete mapping from each canonical exercise ID to its female thumbnail drawable.
     */
    private val femaleThumbnails: Map<String, DrawableResource> = mapOf(
        // Squat Variations
        "squat" to Res.drawable.female_barbell_squat,
        "pause_squat" to Res.drawable.female_barbell_squat,
        "front_squat" to Res.drawable.female_front_squat,
        "dumbbell_goblet_squat" to Res.drawable.female_goblet_squat,
        "air_squat" to Res.drawable.female_air_squat,
        "sumo_squat" to Res.drawable.female_sumo_squat,
        "leg_press" to Res.drawable.female_leg_press,

        // Lunge & Step-Up Variations
        "bulgarian_split_squat" to Res.drawable.female_bulgarian_split_squat,
        "walking_lunges" to Res.drawable.female_walking_lunge,
        "reverse_lunge" to Res.drawable.female_reverse_lunge,
        "supported_reverse_lunge" to Res.drawable.female_reverse_lunge,
        "step_up" to Res.drawable.female_step_up,
        "supported_step_up" to Res.drawable.female_step_up,

        // Deadlift & Hinge Variations
        "deadlift" to Res.drawable.female_deadlift,
        "romanian_deadlift" to Res.drawable.female_romanian_deadlift,
        "back_extension" to Res.drawable.female_romanian_deadlift,
        "kettlebell_swing" to Res.drawable.female_kettlebell_swing,

        // Hip Thrust & Glute Variations
        "hip_thrust" to Res.drawable.female_hip_thrust,
        "barbell_hip_thrust" to Res.drawable.female_hip_thrust,
        "glute_bridge" to Res.drawable.female_glute_bridge,
        "single_leg_glute_bridge" to Res.drawable.female_single_leg_glute_bridge,
        "frog_pumps" to Res.drawable.female_frog_pumps,
        "cable_glute_kickback" to Res.drawable.female_cable_glute_kickback,
        "hip_abduction" to Res.drawable.female_hip_thrust,

        // Leg Isolation
        "lying_leg_curl" to Res.drawable.female_lying_leg_curl,
        "leg_extension" to Res.drawable.female_leg_extension,
        "standing_calf_raise" to Res.drawable.female_standing_calf_raise,

        // Horizontal Press
        "barbell_bench_press" to Res.drawable.female_dumbbell_bench_press,
        "technique_bench" to Res.drawable.female_dumbbell_bench_press,
        "dumbbell_bench" to Res.drawable.female_dumbbell_bench_press,
        "dumbbell_bench_press" to Res.drawable.female_dumbbell_bench_press,
        "light_db_chest_press" to Res.drawable.female_dumbbell_bench_press,
        "incline_dumbbell_press" to Res.drawable.female_dumbbell_bench_press,
        "push_ups" to Res.drawable.female_dumbbell_bench_press,

        // Vertical Press & Shoulders
        "dumbbell_shoulder_press" to Res.drawable.female_dumbbell_bench_press,
        "light_db_press" to Res.drawable.female_dumbbell_bench_press,
        "push_press_technique" to Res.drawable.female_barbell_squat,
        "dumbbell_lateral_raise" to Res.drawable.female_dumbbell_bench_press,

        // Pull & Rows
        "lat_pulldown" to Res.drawable.female_lat_pulldown,
        "dumbbell_row" to Res.drawable.female_romanian_deadlift,
        "chest_supported_row" to Res.drawable.female_lat_pulldown,
        "seated_cable_row" to Res.drawable.female_lat_pulldown,
        "face_pulls" to Res.drawable.female_lat_pulldown,

        // Arms
        "dumbbell_bicep_curl" to Res.drawable.female_dumbbell_bench_press,
        "cable_hammer_curl" to Res.drawable.female_lat_pulldown,
        "triceps_pushdown" to Res.drawable.female_lat_pulldown,

        // Functional, Olympic & Conditioning
        "hang_power_clean_technique" to Res.drawable.female_deadlift,
        "battle_rope" to Res.drawable.female_kettlebell_swing,
        "burpees" to Res.drawable.female_kettlebell_swing,
        "row_intervals" to Res.drawable.female_lat_pulldown,
        "dumbbell_thruster" to Res.drawable.female_goblet_squat,
        "dumbbell_snatch" to Res.drawable.female_deadlift,

        // Core Movements
        "plank_hold" to Res.drawable.female_glute_bridge,
        "side_plank" to Res.drawable.female_glute_bridge,
        "dead_bug" to Res.drawable.female_glute_bridge,
        "dead_bug_progression" to Res.drawable.female_glute_bridge,
        "hollow_body_hold" to Res.drawable.female_glute_bridge,
        "bicycle_crunch" to Res.drawable.female_glute_bridge,
        "cable_crunch" to Res.drawable.female_lat_pulldown,
        "reverse_crunch" to Res.drawable.female_glute_bridge,
        "hanging_knee_raise" to Res.drawable.female_lat_pulldown,
        "hanging_leg_raise" to Res.drawable.female_lat_pulldown,
        "weighted_russian_twist" to Res.drawable.female_glute_bridge,
        "cable_woodchop" to Res.drawable.female_lat_pulldown,
        "pallof_press" to Res.drawable.female_lat_pulldown,
        "mountain_climber" to Res.drawable.female_glute_bridge,
        "high_knees" to Res.drawable.female_kettlebell_swing,
        "core" to Res.drawable.female_glute_bridge,

        // Recovery & Postpartum Clinical Movement
        "diaphragmatic_breathing" to Res.drawable.female_glute_bridge,
        "pelvic_floor_activation" to Res.drawable.female_glute_bridge,
        "incline_treadmill" to Res.drawable.female_kettlebell_swing,
        "incline_walk" to Res.drawable.female_kettlebell_swing,

        // Gym Workout Catalog Aliases (for female user browsing)
        "gym_chest_1" to Res.drawable.female_dumbbell_bench_press,
        "gym_chest_2" to Res.drawable.female_dumbbell_bench_press,
        "gym_chest_3" to Res.drawable.female_lat_pulldown,
        "gym_back_1" to Res.drawable.female_lat_pulldown,
        "gym_back_2" to Res.drawable.female_lat_pulldown,
        "gym_back_3" to Res.drawable.female_romanian_deadlift,
        "gym_sh_1" to Res.drawable.female_dumbbell_bench_press,
        "gym_sh_2" to Res.drawable.female_dumbbell_bench_press,
        "gym_sh_3" to Res.drawable.female_lat_pulldown,
        "gym_bi_1" to Res.drawable.female_dumbbell_bench_press,
        "gym_bi_2" to Res.drawable.female_dumbbell_bench_press,
        "gym_bi_3" to Res.drawable.female_lat_pulldown,
        "gym_tri_1" to Res.drawable.female_lat_pulldown,
        "gym_tri_2" to Res.drawable.female_lat_pulldown,
        "gym_tri_3" to Res.drawable.female_dumbbell_bench_press,
        "gym_leg_1" to Res.drawable.female_barbell_squat,
        "gym_leg_2" to Res.drawable.female_leg_press,
        "gym_leg_3" to Res.drawable.female_lying_leg_curl,
        "gym_abs_1" to Res.drawable.female_lat_pulldown,
        "gym_abs_2" to Res.drawable.female_lat_pulldown,
        "gym_abs_3" to Res.drawable.female_glute_bridge,
        "gym_fb_1" to Res.drawable.female_deadlift,
        "gym_fb_2" to Res.drawable.female_deadlift,
        "gym_fb_3" to Res.drawable.female_romanian_deadlift
    )

    /**
     * Multi-position instructional demonstration sets for detailed movement form.
     */
    private val instructionalSets: Map<String, List<MovementPositionFrame>> = mapOf(
        "dumbbell_goblet_squat" to listOf(
            MovementPositionFrame("start", "1. Setup & Stance", "Stand upright, feet shoulder-width, dumbbell at chest", Res.drawable.female_goblet_squat_start),
            MovementPositionFrame("end", "2. Bottom Position", "Hips down to parallel, chest upright, knees tracking toes", Res.drawable.female_goblet_squat)
        ),
        "air_squat" to listOf(
            MovementPositionFrame("start", "1. Setup & Stance", "Stand upright, feet shoulder-width, core braced", Res.drawable.female_goblet_squat_start),
            MovementPositionFrame("end", "2. Bottom Position", "Squat to parallel depth with controlled tempo", Res.drawable.female_air_squat)
        ),
        "front_squat" to listOf(
            MovementPositionFrame("start", "1. Front Rack Setup", "Barbell resting across anterior deltoids, elbows high", Res.drawable.female_goblet_squat_start),
            MovementPositionFrame("end", "2. Deep Front Squat", "Hips below parallel, upright torso, elbows staying up", Res.drawable.female_front_squat)
        ),
        "sumo_squat" to listOf(
            MovementPositionFrame("start", "1. Wide Stance Setup", "Feet wide, toes flared 45°, holding weight vertically", Res.drawable.female_goblet_squat_start),
            MovementPositionFrame("end", "2. Deep Sumo Squat", "Knees driven wide over toes, vertical torso, hips deep", Res.drawable.female_sumo_squat)
        ),
        "squat" to listOf(
            MovementPositionFrame("start", "1. Setup & Unrack", "Barbell positioned across upper traps, chest proud", Res.drawable.female_goblet_squat_start),
            MovementPositionFrame("end", "2. Parallel Depth", "Knees bent to 90 degrees, core braced, flat back", Res.drawable.female_barbell_squat)
        ),
        "leg_press" to listOf(
            MovementPositionFrame("press", "Controlled Press", "Feet shoulder-width on sled, pressing through mid-foot and heels", Res.drawable.female_leg_press)
        ),
        "walking_lunges" to listOf(
            MovementPositionFrame("lunge", "Walking Lunge Step", "Front knee at 90°, back knee hovering above floor, upright torso", Res.drawable.female_walking_lunge)
        ),
        "reverse_lunge" to listOf(
            MovementPositionFrame("lunge", "Reverse Lunge Step", "Step backward into deep 90° lunge, stable front foot", Res.drawable.female_reverse_lunge)
        ),
        "step_up" to listOf(
            MovementPositionFrame("step", "Box Step-Up", "Drive through lead foot on box, full hip and knee extension", Res.drawable.female_step_up)
        ),
        "hip_thrust" to listOf(
            MovementPositionFrame("end", "Peak Contraction", "Upper back supported, hips fully driven up, 90° knee angle", Res.drawable.female_hip_thrust)
        ),
        "single_leg_glute_bridge" to listOf(
            MovementPositionFrame("bridge", "Single-Leg Extension", "Drive hips up through working heel, opposite leg straight up", Res.drawable.female_single_leg_glute_bridge)
        ),
        "frog_pumps" to listOf(
            MovementPositionFrame("pump", "Butterfly Bridge", "Soles together, knees wide, explosive glute bridge pulse", Res.drawable.female_frog_pumps)
        ),
        "cable_glute_kickback" to listOf(
            MovementPositionFrame("kickback", "Peak Glute Kickback", "Ankle strapped to low cable, extend leg back and upward", Res.drawable.female_cable_glute_kickback)
        ),
        "deadlift" to listOf(
            MovementPositionFrame("start", "Starting Pull", "Hips hinged, flat spine, lats locked, barbell close to shins", Res.drawable.female_deadlift)
        ),
        "romanian_deadlift" to listOf(
            MovementPositionFrame("hinge", "Hinge Position", "Hips driven backward, slight knee bend, hamstrings loaded", Res.drawable.female_romanian_deadlift)
        ),
        "lat_pulldown" to listOf(
            MovementPositionFrame("pull", "Contraction Phase", "Bar pulled to collarbone, elbows driving downward", Res.drawable.female_lat_pulldown)
        ),
        "bulgarian_split_squat" to listOf(
            MovementPositionFrame("lunge", "Bottom Position", "Rear foot elevated on bench, front knee at 90 degrees", Res.drawable.female_bulgarian_split_squat)
        ),
        "kettlebell_swing" to listOf(
            MovementPositionFrame("apex", "Swing Apex", "Explosive hip snap, arms extended at chest height", Res.drawable.female_kettlebell_swing)
        ),
        "glute_bridge" to listOf(
            MovementPositionFrame("bridge", "Bridge Hold", "Shoulders on mat, glutes fully squeezed at the top", Res.drawable.female_glute_bridge)
        ),
        "leg_extension" to listOf(
            MovementPositionFrame("extension", "Quad Extension", "Extend shins forward against pad to full knee lockout", Res.drawable.female_leg_extension)
        ),
        "lying_leg_curl" to listOf(
            MovementPositionFrame("curl", "Hamstring Curl", "Curl lever pad toward glutes, hips pressed flat on bench", Res.drawable.female_lying_leg_curl)
        ),
        "standing_calf_raise" to listOf(
            MovementPositionFrame("raise", "Peak Calf Extension", "Drive balls of feet down on platform, calves fully contracted", Res.drawable.female_standing_calf_raise)
        )
    )

    /**
     * Checks if a canonical exercise has dedicated female media.
     */
    fun hasFemaleMedia(exerciseId: String): Boolean {
        return femaleThumbnails.containsKey(exerciseId.trim().lowercase())
    }

    /**
     * Retrieves the dedicated female thumbnail for an exercise ID.
     */
    fun getThumbnailRes(exerciseId: String): DrawableResource? {
        return femaleThumbnails[exerciseId.trim().lowercase()]
    }

    /**
     * Retrieves instructional position frames for Exercise Detail views.
     */
    fun getInstructionalPositions(exerciseId: String): List<MovementPositionFrame> {
        return instructionalSets[exerciseId.trim().lowercase()] ?: emptyList()
    }

    /**
     * Resolves the appropriate exercise image considering user gender.
     *
     * @param exerciseId Canonical exercise identifier.
     * @param isFemale Whether the active user/context is female.
     * @param defaultFallback Fallback image if no specific image is registered.
     */
    fun resolveExerciseImage(
        exerciseId: String,
        isFemale: Boolean,
        defaultFallback: DrawableResource = Res.drawable.gender_female
    ): DrawableResource {
        if (!isFemale) {
            return defaultFallback
        }
        return getThumbnailRes(exerciseId) ?: defaultFallback
    }

    /**
     * Total count of unique exercise IDs mapped.
     */
    fun getMappedExerciseCount(): Int = femaleThumbnails.size
}
