package com.example.fitness.feature.admin.data

import com.example.fitness.feature.exercise.model.Exercise

/**
 * Standard Body Part / Muscle Group sections for the Exercise Catalog.
 * Order matches authoritative anatomical progression:
 * CHEST -> BACK -> SHOULDERS -> BICEPS -> TRICEPS -> LEGS -> CORE -> FULL BODY -> CARDIO & DRILLS -> OTHER.
 */
enum class ExerciseBodyPartSection(val displayName: String, val sortOrder: Int) {
    CHEST("CHEST", 1),
    BACK("BACK", 2),
    SHOULDERS("SHOULDERS", 3),
    BICEPS("BICEPS", 4),
    TRICEPS("TRICEPS", 5),
    GLUTES("GLUTES", 6),
    LEGS("LEGS", 7),
    CORE("CORE", 8),
    FULL_BODY("FULL BODY", 9),
    CARDIO_DRILLS("CARDIO / CONDITIONING", 10),
    MOBILITY_RECOVERY("MOBILITY / RECOVERY", 11),
    OTHER("OTHER", 12);

    companion object {
        val CARDIO_CONDITIONING get() = CARDIO_DRILLS
    }
}

/**
 * Categorization engine for organizing canonical and custom exercises into body-part sections.
 *
 * Rules:
 * 1. Distinguishes cardio/activity/conditioning/timed intervals (e.g. "10m sprints", shuttle runs, circuits)
 *    so they are NEVER incorrectly presented as normal strength body-part exercises.
 * 2. Uses existing model attributes (primaryMuscle, category, movementPattern, id, name)
 *    without mutating or duplicating existing exercise records.
 * 3. Fallback preserves unclassifiable items under [ExerciseBodyPartSection.OTHER] without guessing.
 */
object AdminExerciseCatalogOrganizer {

    /**
     * Determines whether an exercise record represents a cardio, sprint, drill,
     * shuttle run, or multi-movement conditioning circuit rather than a normal strength exercise.
     */
    fun isCardioOrActivity(exercise: Exercise): Boolean {
        val id = exercise.id.trim().lowercase()
        val name = exercise.name.trim().lowercase()
        val cat = exercise.category.trim().uppercase()

        // Mobility/recovery is not cardio
        if (cat == "MOBILITY" || cat == "RECOVERY" || id.contains("breathing") || id.contains("pelvic_floor")) {
            return false
        }

        return cat == "CARDIO" ||
                id.contains("rounds") || name.contains("rounds:") ||
                id.contains("circuit") || name.contains("circuit") ||
                id.contains("sprint") || name.contains("sprint") ||
                id.contains("strides") || name.contains("strides") ||
                id.contains("running") || name.contains("running") ||
                id.contains("shuttle") || id == "5_10_5" ||
                id.contains("beep") || name.contains("beep") ||
                id.contains("treadmill") || id == "incline_walk" ||
                id == "easy_cardio" || id == "row_intervals" ||
                id.contains("reaction_drill") || id.contains("cone_drill") || id.contains("cone_reaction") ||
                id == "falling_starts" || id == "build_up_runs" || id == "yo_yo_beep_style_conditioning_test" ||
                id == "hypertrophy_performance_check" ||
                id == "battle_rope" || id == "burpees"
    }

    /**
     * Maps an exercise to its corresponding [ExerciseBodyPartSection].
     */
    fun classify(exercise: Exercise): ExerciseBodyPartSection {
        val id = exercise.id.trim().lowercase()
        val name = exercise.name.trim().lowercase()
        val pm = exercise.primaryMuscle.trim().lowercase()
        val cat = exercise.category.trim().uppercase()

        // 0. Explicit Mobility / Recovery
        if (cat == "MOBILITY" || cat == "RECOVERY" ||
            id.contains("breathing") || id.contains("cat_cow") || id.contains("childs_pose") ||
            id.contains("pelvic_floor") || id == "mobility"
        ) {
            return ExerciseBodyPartSection.MOBILITY_RECOVERY
        }

        // 0b. Explicit Activities / Cardio / Conditioning / Timed Intervals / Multi-movement Rounds
        if (isCardioOrActivity(exercise)) {
            return ExerciseBodyPartSection.CARDIO_DRILLS
        }

        // 0c. Explicit Full Body Library Exercises (AthGym Full Body PDF specification)
        if (cat == "FULL_BODY" || cat == "FULL BODY" || pm == "full body" || pm == "full_body" ||
            id.startsWith("fb_") || id.startsWith("gym_fb_") || id.startsWith("home_fb_")
        ) {
            return ExerciseBodyPartSection.FULL_BODY
        }

        // 1. CANONICAL PRIMARY MUSCLE (Single Source of Truth)
        // If the exercise has an authoritative primaryMuscle, it directly determines the canonical category.
        if (pm.isNotBlank()) {
            when {
                pm in listOf("chest", "pecs", "pectorals", "upper chest", "lower chest") ->
                    return ExerciseBodyPartSection.CHEST
                pm in listOf("back", "lats", "rhomboids", "lower back", "latissimus dorsi") ->
                    return ExerciseBodyPartSection.BACK
                pm in listOf("shoulders", "delts", "deltoids", "side delts", "front delts", "rear delts", "lateral delts", "traps") ->
                    return ExerciseBodyPartSection.SHOULDERS
                pm in listOf("biceps", "bicep", "brachialis") ->
                    return ExerciseBodyPartSection.BICEPS
                pm in listOf("triceps", "tricep") ->
                    return ExerciseBodyPartSection.TRICEPS
                pm == "glutes" ->
                    return ExerciseBodyPartSection.GLUTES
                pm in listOf("legs", "quads", "quadriceps", "hamstrings", "calves", "hip flexors") ->
                    return ExerciseBodyPartSection.LEGS
                pm in listOf("abs", "core", "abdominals", "obliques") ->
                    return ExerciseBodyPartSection.CORE
            }
        }

        // 2. FALLBACK BY NAME / ID KEYWORDS (Only when primaryMuscle is unspecified or unmapped)
        // 2a. CHEST
        if (id.contains("bench_press") || (id.contains("bench") && !id.contains("close_grip")) ||
            id.contains("dumbbell_fly") || id.contains("cable_crossover") ||
            (id.contains("push_up") && !id.contains("close_grip")) || id.contains("chest_press") ||
            id.contains("chest_dip") || id.contains("decline_press") || id.contains("incline_press") ||
            (id.contains("cable_fly") && !id.contains("rear_delt") && !id.contains("reverse")) ||
            id.contains("light_db_chest_press") || id.contains("technique_bench") ||
            id.contains("medicine_ball_chest_throw")
        ) {
            return ExerciseBodyPartSection.CHEST
        }

        // 2b. BACK
        if (id.contains("pulldown") || id.contains("barbell_row") || id.contains("dumbbell_row") ||
            id.contains("cable_row") || id.contains("seated_row") || id.contains("deadlift") ||
            id.contains("pull_up") || id.contains("pullup") || id.contains("chin_up") || id.contains("chinup") ||
            id.contains("back_extension") || id.contains("machine_pullover") ||
            id.contains("t_bar_row") || id.contains("lat_pulldown") || id.contains("assisted_pulldown")
        ) {
            return ExerciseBodyPartSection.BACK
        }

        // 2c. SHOULDERS
        if (id.contains("overhead_press") || id.contains("shoulder_press") ||
            id.contains("arnold_press") || id.contains("lateral_raise") || id.contains("front_raise") ||
            id.contains("rear_delt") || id.contains("shrug") || id.contains("face_pull") || id.contains("push_press")
        ) {
            return ExerciseBodyPartSection.SHOULDERS
        }

        // 2d. BICEPS
        if (id.contains("bicep_curl") || id.contains("biceps_curl") || id.contains("dumbbell_curl") ||
            id.contains("barbell_curl") || id.contains("hammer_curl") || id.contains("preacher_curl") ||
            id.contains("cable_curl") || id.contains("concentration_curl") || id.contains("bayesian_curl")
        ) {
            return ExerciseBodyPartSection.BICEPS
        }

        // 2e. TRICEPS
        if (id.contains("tricep") || id.contains("triceps") || id.contains("pushdown") ||
            id.contains("skull_crusher") || id.contains("overhead_extension") || id.contains("french_press") ||
            id.contains("dips") || id.contains("close_grip_bench") || id.contains("close_grip_push_up")
        ) {
            return ExerciseBodyPartSection.TRICEPS
        }

        // 2f. GLUTES
        if (id.contains("hip_thrust") || id.contains("glute_bridge") ||
            id.contains("glute_kickback") || id.contains("cable_glute") ||
            id.contains("frog_pump") || id.contains("hip_abduct")
        ) {
            return ExerciseBodyPartSection.GLUTES
        }

        // 2g. LEGS (Quads, Hamstrings, Calves, Squats, Lunges, Presses)
        if (id.contains("squat") || id.contains("lunge") || id.contains("leg_press") || id.contains("leg_extension") ||
            id.contains("leg_curl") || id.contains("calf_raise") ||
            id.contains("step_up") || id.contains("split_squat") ||
            id.contains("hip_adduction") ||
            id.contains("jump") || id.contains("bounds") || id.contains("skip") || id.contains("depth_drop")
        ) {
            return ExerciseBodyPartSection.LEGS
        }

        // 2h. CORE
        if (id.contains("crunch") || id.contains("plank") ||
            id.contains("russian_twist") || id.contains("leg_raise") || id.contains("knee_raise") ||
            id.contains("ab_rollout") || id.contains("woodchop") || id.contains("dead_bug") ||
            id.contains("bird_dog") || id.contains("hollow_body") || id.contains("side_bend") ||
            id.contains("heel_taps") || id == "core" ||
            id.contains("mountain_climber") || id.contains("knee_to_elbow")
        ) {
            return ExerciseBodyPartSection.CORE
        }

        // 2i. FULL BODY (Explicit athletic multi-joint lifts: clean, carries, slams, thruster, snatch)
        if (id.contains("clean") || id.contains("carry") || id.contains("slam") ||
            id.contains("thruster") || id.contains("snatch") ||
            id.contains("kb_swing") || id.contains("kettlebell_swing")
        ) {
            return ExerciseBodyPartSection.FULL_BODY
        }

        return ExerciseBodyPartSection.OTHER
    }

    /**
     * Groups a list of exercises into an ordered map of sections.
     */
    fun groupExercisesBySection(exercises: List<Exercise>): Map<ExerciseBodyPartSection, List<Exercise>> {
        val grouped = exercises.groupBy { classify(it) }
        return ExerciseBodyPartSection.entries
            .filter { grouped.containsKey(it) && grouped[it]!!.isNotEmpty() }
            .associateWith { grouped[it]!!.sortedBy { ex -> ex.name } }
    }
}
