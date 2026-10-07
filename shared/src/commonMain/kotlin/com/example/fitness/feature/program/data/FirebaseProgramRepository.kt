package com.example.fitness.feature.program.data

import com.example.fitness.core.coroutine.AppDispatchers
import com.example.fitness.core.firebase.FitnessFirebase
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.exercise.model.ExerciseReference
import com.example.fitness.feature.program.model.Program
import com.example.fitness.feature.program.model.ProgramDay
import com.example.fitness.getCurrentEpochMillis
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.withContext

/**
 * Production Firebase implementation of [ProgramRepository].
 * Operates on the root Firestore collection "programs/{programId}".
 *
 * Implements strict soft-delete archiving, audit field management (createdAt, updatedAt,
 * createdBy, updatedBy, version), relational validation with exercises, and canonical day ordering.
 */
class FirebaseProgramRepository : ProgramRepository {

    private val firestore get() = FitnessFirebase.firestore
    private val programsCollection get() = firestore.collection("programs")

    private fun currentAuthUid(): String {
        return FitnessFirebase.auth.currentUser?.uid
            ?: UserSession.currentUserId
            ?: ""
    }

    /**
     * Converts a program name into a URL/ID-safe slug (e.g. "30-Day Workout Plan" -> "30_day_workout_plan").
     */
    fun generateSlug(name: String): String {
        val cleaned = name.trim()
            .lowercase()
            .replace(Regex("[^a-z0-9\\s_]"), "")
            .replace(Regex("\\s+"), "_")
            .trim('_')
        return cleaned.ifBlank { "program_${getCurrentEpochMillis()}" }
    }

    /**
     * Validates domain constraints on a [Program]:
     * - Non-blank ID, name, slug.
     * - Non-negative durationDays and trainingDaysPerWeek.
     * - Positive and unique day numbers.
     * - Valid non-blank exercise references with positive sets and non-blank reps.
     */
    fun validateProgram(program: Program): Result<Unit> {
        if (program.id.isBlank()) {
            return Result.failure(IllegalArgumentException("Program ID cannot be blank"))
        }
        if (program.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Program Name cannot be blank"))
        }
        if (program.slug.isBlank()) {
            return Result.failure(IllegalArgumentException("Program Slug cannot be blank"))
        }
        if (program.durationDays < 0) {
            return Result.failure(IllegalArgumentException("Program durationDays must be non-negative"))
        }
        if (program.trainingDaysPerWeek < 0) {
            return Result.failure(IllegalArgumentException("Program trainingDaysPerWeek must be non-negative"))
        }

        // Validate program days
        val dayNumbers = mutableSetOf<Int>()
        for (day in program.days) {
            if (day.dayNumber <= 0) {
                return Result.failure(IllegalArgumentException("Program dayNumber must be positive (found ${day.dayNumber})"))
            }
            if (program.durationDays > 0 && day.dayNumber > program.durationDays) {
                return Result.failure(IllegalArgumentException("Program dayNumber ${day.dayNumber} exceeds durationDays (${program.durationDays})"))
            }
            if (!dayNumbers.add(day.dayNumber)) {
                return Result.failure(IllegalArgumentException("Duplicate dayNumber ${day.dayNumber} detected in program"))
            }

            // Validate exercises in day
            for (ex in day.exercises) {
                if (ex.exerciseId.isBlank()) {
                    return Result.failure(IllegalArgumentException("ExerciseReference in Day ${day.dayNumber} contains blank exerciseId"))
                }
                if (ex.sets <= 0) {
                    return Result.failure(IllegalArgumentException("Exercise ${ex.exerciseId} in Day ${day.dayNumber} must have sets > 0"))
                }
                if (ex.restSeconds < 0) {
                    return Result.failure(IllegalArgumentException("Exercise ${ex.exerciseId} in Day ${day.dayNumber} cannot have negative restSeconds"))
                }
                if (ex.reps.isBlank()) {
                    return Result.failure(IllegalArgumentException("Exercise ${ex.exerciseId} in Day ${day.dayNumber} must have a non-blank reps prescription"))
                }
            }
        }

        return Result.success(Unit)
    }

    /**
     * Resiliently parses a program document from a Firestore snapshot.
     * Sorts days by [ProgramDay.dayNumber] to ensure canonical ordering.
     */
    fun parseProgram(doc: DocumentSnapshot): Program? {
        if (!doc.exists) return null

        // 1. Primary path: GitLive typed data deserialization
        try {
            val parsed = doc.data<Program>()
            val canonicalDays = parsed.days.sortedBy { it.dayNumber }
            return parsed.copy(
                id = parsed.id.ifBlank { doc.id },
                days = canonicalDays
            )
        } catch (_: Exception) {
            // Fall through to manual parsing for backward/forward compatibility
        }

        // 2. Resilient fallback path
        return try {
            val id = doc.id
            val name = try { doc.get<String>("name") } catch (_: Exception) { "" }
            val slug = try { doc.get<String>("slug") } catch (_: Exception) { id }
            val description = try { doc.get<String>("description") } catch (_: Exception) { "" }
            val type = try { doc.get<String>("type") } catch (_: Exception) { "" }
            val experienceLevel = try { doc.get<String>("experienceLevel") } catch (_: Exception) { "" }
            val durationDays = try { doc.get<Int>("durationDays") } catch (_: Exception) { 0 }
            val trainingDaysPerWeek = try { doc.get<Int>("trainingDaysPerWeek") } catch (_: Exception) { 0 }
            val days = try { doc.get<List<ProgramDay>>("days") } catch (_: Exception) { emptyList() }
            val isActive = try { doc.get<Boolean>("isActive") } catch (_: Exception) { true }
            val createdAt = try { doc.get<Long?>("createdAt") } catch (_: Exception) { null }
            val updatedAt = try { doc.get<Long?>("updatedAt") } catch (_: Exception) { null }
            val createdBy = try { doc.get<String>("createdBy") } catch (_: Exception) { "" }
            val updatedBy = try { doc.get<String>("updatedBy") } catch (_: Exception) { "" }
            val version = try { doc.get<Int>("version") } catch (_: Exception) { 1 }
            val gender = try { doc.get<String>("gender") } catch (_: Exception) { "" }

            Program(
                id = id,
                name = name,
                slug = slug,
                description = description,
                type = type,
                experienceLevel = experienceLevel,
                durationDays = durationDays,
                trainingDaysPerWeek = trainingDaysPerWeek,
                days = days.sortedBy { it.dayNumber },
                isActive = isActive,
                createdAt = createdAt,
                updatedAt = updatedAt,
                createdBy = createdBy,
                updatedBy = updatedBy,
                version = version,
                gender = gender
            )
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getProgram(programId: String): Program? = withContext(AppDispatchers.IO) {
        val stableId = programId.trim()
        if (stableId.isBlank()) return@withContext null
        try {
            val doc = programsCollection.document(stableId).get()
            withContext(AppDispatchers.Default) {
                parseProgram(doc)
            }
        } catch (e: Exception) {
            println("[ERROR] FirebaseProgramRepository.getProgram failed for $stableId: ${e.message}")
            null
        }
    }

    override suspend fun getActivePrograms(): List<Program> = withContext(AppDispatchers.IO) {
        try {
            val snapshot = try {
                programsCollection.where { "isActive" equalTo true }.get()
            } catch (_: Exception) {
                programsCollection.get()
            }

            withContext(AppDispatchers.Default) {
                snapshot.documents.mapNotNull { doc ->
                    parseProgram(doc)
                }.filter { it.isActive }
                    .sortedBy { it.name }
            }
        } catch (e: Exception) {
            println("[ERROR] FirebaseProgramRepository.getActivePrograms failed: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getAllProgramsForAdmin(): List<Program> = withContext(AppDispatchers.IO) {
        try {
            val snapshot = programsCollection.get()
            withContext(AppDispatchers.Default) {
                snapshot.documents.mapNotNull { doc ->
                    parseProgram(doc)
                }.sortedBy { it.name }
            }
        } catch (e: Exception) {
            println("[ERROR] FirebaseProgramRepository.getAllProgramsForAdmin failed: ${e.message}")
            emptyList()
        }
    }

    override suspend fun createProgram(program: Program): Result<Unit> = withContext(AppDispatchers.IO) {
        try {
            val stableId = program.id.trim().ifBlank { generateSlug(program.name) }
            val prepared = program.copy(
                id = stableId,
                slug = program.slug.trim().ifBlank { stableId },
                days = program.days.sortedBy { it.dayNumber },
                isActive = true,
                createdAt = getCurrentEpochMillis(),
                updatedAt = getCurrentEpochMillis(),
                createdBy = currentAuthUid(),
                updatedBy = currentAuthUid(),
                version = 1
            )

            val validation = validateProgram(prepared)
            if (validation.isFailure) {
                return@withContext validation
            }

            programsCollection.document(stableId).set(prepared)
            Result.success(Unit)
        } catch (e: Exception) {
            println("[ERROR] FirebaseProgramRepository.createProgram failed: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateProgram(program: Program): Result<Unit> = withContext(AppDispatchers.IO) {
        try {
            val stableId = program.id.trim()
            if (stableId.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Program ID cannot be blank"))
            }

            val docRef = programsCollection.document(stableId)
            val existingDoc = docRef.get()
            if (!existingDoc.exists) {
                return@withContext Result.failure(NoSuchElementException("Program with ID '$stableId' does not exist"))
            }

            val existing = parseProgram(existingDoc)
            val now = getCurrentEpochMillis()
            val uid = currentAuthUid()

            val updated = program.copy(
                id = stableId,
                slug = program.slug.trim().ifBlank { existing?.slug?.ifBlank { stableId } ?: stableId },
                days = program.days.sortedBy { it.dayNumber },
                createdAt = existing?.createdAt ?: now,
                createdBy = existing?.createdBy?.ifBlank { uid } ?: uid,
                updatedAt = now,
                updatedBy = uid,
                version = (existing?.version ?: 1) + 1
            )

            val validation = validateProgram(updated)
            if (validation.isFailure) {
                return@withContext validation
            }

            docRef.set(updated)
            Result.success(Unit)
        } catch (e: Exception) {
            println("[ERROR] FirebaseProgramRepository.updateProgram failed: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun archiveProgram(programId: String): Result<Unit> = withContext(AppDispatchers.IO) {
        try {
            val stableId = programId.trim()
            if (stableId.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Program ID cannot be blank"))
            }

            val docRef = programsCollection.document(stableId)
            val existingDoc = docRef.get()
            if (!existingDoc.exists) {
                return@withContext Result.failure(NoSuchElementException("Program with ID '$stableId' does not exist"))
            }

            val existing = parseProgram(existingDoc)
                ?: return@withContext Result.failure(IllegalStateException("Failed to parse existing program '$stableId'"))

            val now = getCurrentEpochMillis()
            val uid = currentAuthUid()

            val archived = existing.copy(
                isActive = false,
                updatedAt = now,
                updatedBy = uid,
                version = existing.version + 1
            )

            docRef.set(archived)
            Result.success(Unit)
        } catch (e: Exception) {
            println("[ERROR] FirebaseProgramRepository.archiveProgram failed: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun restoreProgram(programId: String): Result<Unit> = withContext(AppDispatchers.IO) {
        try {
            val stableId = programId.trim()
            if (stableId.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Program ID cannot be blank"))
            }

            val docRef = programsCollection.document(stableId)
            val existingDoc = docRef.get()
            if (!existingDoc.exists) {
                return@withContext Result.failure(NoSuchElementException("Program with ID '$stableId' does not exist"))
            }

            val existing = parseProgram(existingDoc)
                ?: return@withContext Result.failure(IllegalStateException("Failed to parse existing program '$stableId'"))

            val now = getCurrentEpochMillis()
            val uid = currentAuthUid()

            val restored = existing.copy(
                isActive = true,
                updatedAt = now,
                updatedBy = uid,
                version = existing.version + 1
            )

            docRef.set(restored)
            Result.success(Unit)
        } catch (e: Exception) {
            println("[ERROR] FirebaseProgramRepository.restoreProgram failed: ${e.message}")
            Result.failure(e)
        }
    }
}
