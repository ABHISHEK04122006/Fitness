package com.example.fitness.feature.exercise

import com.example.fitness.feature.admin.data.AdminExerciseCatalogOrganizer
import com.example.fitness.feature.admin.data.ExerciseBodyPartSection
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.data.ThirtyDayExerciseLibrary
import kotlin.test.Test

class ExerciseClassificationAuditTest {

    private fun mapMuscleToBodyPartCategory(muscleOrBp: String): String {
        val s = muscleOrBp.trim().lowercase().replace('_', ' ')
        return when {
            s in listOf("chest", "pecs", "pectorals", "upper chest", "lower chest") -> "chest"
            s in listOf("back", "lats", "rhomboids", "lower back", "latissimus dorsi") -> "back"
            s in listOf("shoulders", "delts", "deltoids", "side delts", "front delts", "rear delts", "lateral delts", "traps") -> "shoulders"
            s in listOf("biceps", "bicep", "brachialis") -> "biceps"
            s in listOf("triceps", "tricep") -> "triceps"
            s in listOf("arms", "forearms") -> "arms"
            s in listOf("legs", "quads", "quadriceps", "hamstrings", "calves", "glutes", "hip flexors") -> "legs"
            s in listOf("abs", "core", "abdominals", "obliques") -> "abs"
            s in listOf("full body", "full_body") -> "full body"
            else -> s
        }
    }

    @Test
    fun auditThirtyDayLibraryWithAnatomicalMapping() {
        println("=== REAL DISCREPANCY AUDIT: ThirtyDayExerciseLibrary ===")
        val entries = ThirtyDayExerciseLibrary.ALL_ENTRIES
        var realMismatchCount = 0

        for (entry in entries) {
            val canonId = entry.canonicalId.trim().lowercase()
            val canonical = SampleExerciseData.getById(canonId)
                ?: SampleExerciseData.getById(entry.id)
                ?: SampleExerciseData.findByName(entry.name)

            if (canonical != null) {
                val entryCategory = mapMuscleToBodyPartCategory(entry.bodyPart)
                val canonicalCategory = mapMuscleToBodyPartCategory(canonical.primaryMuscle)

                val matches = if (entryCategory == "arms") {
                    canonicalCategory == "biceps" || canonicalCategory == "triceps" || canonicalCategory == "arms"
                } else {
                    entryCategory.equals(canonicalCategory, ignoreCase = true)
                }

                if (!matches) {
                    realMismatchCount++
                    println("REAL MISMATCH #$realMismatchCount: entryId=${entry.id}, name='${entry.name}', entryBodyPart='${entry.bodyPart}' (category=$entryCategory), canonId='$canonId', canonicalPm='${canonical.primaryMuscle}' (category=$canonicalCategory)")
                }
            } else {
                println("NO CANONICAL: entryId=${entry.id}, name='${entry.name}', canonId='$canonId'")
            }
        }
        println("Total real ThirtyDay mismatches: $realMismatchCount out of ${entries.size}")
        kotlin.test.assertEquals(0, realMismatchCount, "Expected 0 real mismatches in ThirtyDayExerciseLibrary")
    }

    @Test
    fun auditSampleExerciseData() {
        println("=== CANONICAL DATA AUDIT: SampleExerciseData ===")
        val exercises = SampleExerciseData.ALL_EXERCISES
        var anomalyCount = 0

        for (ex in exercises) {
            val id = ex.id.lowercase()
            val name = ex.name.lowercase()
            val pm = ex.primaryMuscle.lowercase()
            val cat = ex.category.uppercase()
            val mov = ex.movementPattern.uppercase()

            val isAnomaly = when {
                // Leg curls must not be Biceps
                (id.contains("leg_curl") || name.contains("leg curl")) && pm == "biceps" -> true
                // Leg extension must not be Triceps
                (id.contains("leg_extension") || name.contains("leg extension")) && pm == "triceps" -> true
                // Shoulder/overhead/arnold press must not be Quadriceps / legs
                (id.contains("shoulder_press") || id.contains("overhead_press") || id.contains("arnold_press")) && pm in listOf("quadriceps", "legs") -> true
                // Chest-supported rows must not be Chest
                (id.contains("chest_supported") && (id.contains("row") || name.contains("row"))) && pm == "chest" -> true
                // Back extension must not be Triceps
                (id.contains("back_extension") || name.contains("back extension")) && pm == "triceps" -> true
                // Lateral raises must not be Back
                (id.contains("lateral_raise") || name.contains("lateral raise")) && pm == "back" -> true
                // Low incline press must not be Quadriceps
                (id.contains("incline") && (id.contains("press") || name.contains("press"))) && pm in listOf("quadriceps", "legs") -> true
                // Triceps extensions must not be Shoulders
                (id.contains("overhead_rope_extension") || id.contains("cable_overhead_extension") || id.contains("single_arm_overhead_cable_extension")) && pm == "shoulders" -> true
                // JM press must not be Quadriceps
                id.contains("jm_press") && pm in listOf("quadriceps", "legs") -> true
                // Pallof press must not be Quadriceps
                id.contains("pallof_press") && pm in listOf("quadriceps", "legs") -> true
                // Bicycle crunch must not be Legs
                id.contains("bicycle_crunch") && pm in listOf("legs", "quadriceps") -> true
                // Dumbbell shrugs should have primary Shoulders, not Traps
                id == "dumbbell_shrugs" && pm != "shoulders" -> true
                // Seated machine press, machine press, landmine press in shoulders
                (id == "seated_machine_press" || id == "machine_press" || id == "landmine_press" || id == "dumbbell_press") && pm in listOf("quadriceps", "legs") -> true
                else -> false
            }

            if (isAnomaly) {
                anomalyCount++
                println("CANONICAL ANOMALY #$anomalyCount: id=${ex.id}, name='${ex.name}', primaryMuscle='${ex.primaryMuscle}', movementPattern='${ex.movementPattern}', category='${ex.category}'")
            }
        }
        println("Total SampleExerciseData anomalies found: $anomalyCount out of ${exercises.size}")
        kotlin.test.assertEquals(0, anomalyCount, "Expected 0 anomalies in SampleExerciseData canonical catalog")

        val muscleCounts = exercises.groupingBy { it.primaryMuscle }.eachCount()
        println("Distinct primaryMuscle counts in SampleExerciseData: $muscleCounts")
    }

    @Test
    fun testBarbellShrugClassificationSingleSourceOfTruth() {
        val barbellShrug = SampleExerciseData.getById("barbell_shrug")
        kotlin.test.assertNotNull(barbellShrug, "barbell_shrug must exist in canonical catalog")

        // 1. Details Screen Primary and Secondary
        kotlin.test.assertEquals("Shoulders", barbellShrug.primaryMuscle)
        kotlin.test.assertTrue(barbellShrug.secondaryMuscles.contains("Traps"), "Traps must be a secondary muscle")

        // 2. Admin classification
        val adminSection = AdminExerciseCatalogOrganizer.classify(barbellShrug)
        kotlin.test.assertEquals(ExerciseBodyPartSection.SHOULDERS, adminSection, "Admin section must be SHOULDERS")

        // 3. Workout repository body-part matching
        val matchesShoulders = com.example.fitness.feature.workout.data.WorkoutRepository.matchesBodyPart(barbellShrug, "shoulders", "Shoulders")
        val matchesBack = com.example.fitness.feature.workout.data.WorkoutRepository.matchesBodyPart(barbellShrug, "back", "Back")
        kotlin.test.assertTrue(matchesShoulders, "Barbell Shrug MUST match Shoulders filter")
        kotlin.test.assertFalse(matchesBack, "Barbell Shrug must NOT match Back filter")

        // 4. ThirtyDayExerciseLibrary entries for Barbell Shrug
        val advShrug = ThirtyDayExerciseLibrary.findById("back_adv_barbell_shrug")
        kotlin.test.assertNotNull(advShrug)
        kotlin.test.assertEquals("Shoulders", advShrug.bodyPart, "back_adv_barbell_shrug bodyPart must be Shoulders")

        val expShrug = ThirtyDayExerciseLibrary.findById("back_exp_barbell_shrug")
        kotlin.test.assertNotNull(expShrug)
        kotlin.test.assertEquals("Shoulders", expShrug.bodyPart, "back_exp_barbell_shrug bodyPart must be Shoulders")
    }

    @Test
    fun testMultiMuscleAndSecondaryMusclesDoNotDriveClassification() {
        for (ex in SampleExerciseData.ALL_EXERCISES) {
            val section = AdminExerciseCatalogOrganizer.classify(ex)

            // Multi-muscle exercises must NOT be Full Body unless explicitly defined as Full Body
            if (ex.secondaryMuscles.isNotEmpty() && !ex.primaryMuscle.equals("Full Body", ignoreCase = true)) {
                kotlin.test.assertNotEquals(
                    ExerciseBodyPartSection.FULL_BODY,
                    section,
                    "Exercise ${ex.id} with primary=${ex.primaryMuscle} and secondary=${ex.secondaryMuscles} must not be classified as Full Body"
                )
            }
        }
    }
}

