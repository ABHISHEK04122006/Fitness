package com.example.fitness.feature.admin.data

import com.example.fitness.core.coroutine.AppDispatchers
import com.example.fitness.feature.exercise.data.FirebaseExerciseRepository
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.media.FemaleExerciseMediaRegistry
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.program.data.CanonicalFemaleProgramData
import com.example.fitness.feature.program.data.FirebaseProgramRepository
import com.example.fitness.feature.program.data.SampleProgramData
import com.example.fitness.feature.program.model.Program
import kotlinx.coroutines.withContext

/**
 * Unified catalog provider ensuring ADMIN and USER use the EXACT SAME underlying
 * exercise and program datasets.
 *
 * Reconciles Firestore documents with authoritative canonical definitions:
 * - Firestore modifications (custom media, descriptions, active flags) take precedence.
 * - Authoritative canonical definitions provide immediate availability and fallback.
 * - Male and Female subsets strictly mirror User-facing program and routine availability.
 */
object AdminWorkoutCatalogProvider {

    private val exerciseRepository = FirebaseExerciseRepository()
    private val programRepository = FirebaseProgramRepository()

    /**
     * Unique exercise IDs referenced across all 9 authoritative female 30-day programs.
     */
    val femaleProgramExerciseIds: Set<String> by lazy {
        CanonicalFemaleProgramData.ALL_PROGRAMS.flatMap { program ->
            program.days.flatMap { day ->
                day.exercises.map { it.exerciseId.trim().lowercase() }
            }
        }.toSet()
    }

    /**
     * Unique exercise IDs referenced across canonical male / general programs.
     */
    val maleProgramExerciseIds: Set<String> by lazy {
        SampleProgramData.INITIAL_PROGRAMS.flatMap { program ->
            program.days.flatMap { day ->
                day.exercises.map { it.exerciseId.trim().lowercase() }
            }
        }.toSet()
    }

    /**
     * Loads unified exercises for a selected gender ("Male" or "Female").
     */
    suspend fun getUnifiedExercisesForGender(gender: String): List<Exercise> = withContext(AppDispatchers.Default) {
        val isFemaleTarget = gender.equals("Female", ignoreCase = true)

        // 1. Start with canonical baseline from SampleExerciseData
        val exercisesMap = mutableMapOf<String, Exercise>()
        SampleExerciseData.getAllCanonical()
            .filter { !SampleExerciseData.isDeleted(it.id) }
            .forEach { exercisesMap[it.id] = it }
        com.example.fitness.feature.exercise.data.FullBodyExerciseLibrary.ALL_FULL_BODY_EXERCISES
            .filter { !SampleExerciseData.isDeleted(it.id) }
            .forEach { exercisesMap[it.id] = it }
        com.example.fitness.feature.exercise.data.ThirtyDayExerciseLibrary.ALL_EXERCISES
            .filter { !SampleExerciseData.isDeleted(it.id) }
            .forEach { exercisesMap[it.id] = it }
        SampleExerciseData.getLiveCustomExercises()
            .filter { !SampleExerciseData.isDeleted(it.id) }
            .forEach { exercisesMap[it.id] = it }

        // 2. Fetch live Firestore exercises and merge (Firestore overrides canonical)
        try {
            val firestoreExercises = exerciseRepository.getAllExercisesForAdmin()
            firestoreExercises.forEach { firestoreEx ->
                if (firestoreEx.id.isNotBlank() && !SampleExerciseData.isDeleted(firestoreEx.id)) {
                    exercisesMap[firestoreEx.id] = firestoreEx
                }
            }
        } catch (e: Exception) {
            println("[WARN] AdminWorkoutCatalogProvider: Firestore exercise fetch error: ${e.message}")
        }

        // 3. Filter for selected gender and exclude any deleted items
        exercisesMap.values.filter { !SampleExerciseData.isDeleted(it.id) }.filter { ex ->
            val exId = ex.id.trim().lowercase()
            val exGender = ex.gender.trim()

            val isFullBody = ex.category.equals("FULL_BODY", ignoreCase = true) || ex.id.startsWith("fb_")
            if (isFullBody) return@filter true

            if (isFemaleTarget) {
                // Female catalog: Strictly exclude explicitly Male exercises
                if (exGender.equals("Male", ignoreCase = true)) {
                    false
                } else if (exGender.equals("Female", ignoreCase = true) || exGender.equals("Both", ignoreCase = true)) {
                    true
                } else {
                    // Blank gender: check female canonical datasets (from the 9 30-day programs & registry)
                    femaleProgramExerciseIds.contains(exId) ||
                            SampleExerciseData.FEMALE_SPECIFIC_EXERCISES.any { it.id.equals(exId, ignoreCase = true) } ||
                            FemaleExerciseMediaRegistry.hasFemaleMedia(exId)
                }
            } else {
                // Male catalog: Strictly exclude explicitly Female exercises
                if (exGender.equals("Female", ignoreCase = true)) {
                    false
                } else if (exGender.equals("Male", ignoreCase = true) || exGender.equals("Both", ignoreCase = true)) {
                    true
                } else {
                    // Blank gender: check male canonical datasets (exclude purely female-specific routines)
                    val isFemaleExclusive = SampleExerciseData.FEMALE_SPECIFIC_EXERCISES.any { it.id.equals(exId, ignoreCase = true) } &&
                            !maleProgramExerciseIds.contains(exId)
                    !isFemaleExclusive && (
                            maleProgramExerciseIds.contains(exId) ||
                            SampleExerciseData.ALL_EXERCISES.any { it.id.equals(exId, ignoreCase = true) }
                    )
                }
            }
        }.sortedBy { it.name }
    }

    /**
     * Loads unified programs for a selected gender ("Male" or "Female").
     */
    suspend fun getUnifiedProgramsForGender(gender: String): List<Program> = withContext(AppDispatchers.Default) {
        val isFemaleTarget = gender.equals("Female", ignoreCase = true)

        // 1. Start with canonical baseline
        val programsMap = mutableMapOf<String, Program>()
        if (isFemaleTarget) {
            CanonicalFemaleProgramData.ALL_PROGRAMS.forEach { programsMap[it.id] = it }
        } else {
            SampleProgramData.INITIAL_PROGRAMS.forEach { programsMap[it.id] = it }
        }

        // 2. Fetch live Firestore programs and merge (Firestore overrides canonical)
        try {
            val firestorePrograms = programRepository.getAllProgramsForAdmin()
            firestorePrograms.forEach { firestoreProg ->
                val pId = firestoreProg.id.trim().lowercase()
                val pGender = firestoreProg.gender.trim()

                val belongsToFemale = pId.startsWith("female_") || pGender.equals("Female", ignoreCase = true)
                val belongsToMale = (!pId.startsWith("female_") && !pGender.equals("Female", ignoreCase = true)) ||
                        pGender.equals("Male", ignoreCase = true)

                if (isFemaleTarget && belongsToFemale) {
                    programsMap[firestoreProg.id] = firestoreProg
                } else if (!isFemaleTarget && belongsToMale) {
                    programsMap[firestoreProg.id] = firestoreProg
                }
            }
        } catch (e: Exception) {
            println("[WARN] AdminWorkoutCatalogProvider: Firestore program fetch error: ${e.message}")
        }

        programsMap.values.sortedBy { it.name }
    }
}
