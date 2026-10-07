package com.example.fitness.feature.exercise.data

import com.example.fitness.core.firebase.FitnessFirebase
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseMedia
import com.example.fitness.getCurrentEpochMillis
import com.example.fitness.core.coroutine.AppDispatchers
import com.example.fitness.createThreadSafeMap
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Production Firebase implementation of [ExerciseRepository].
 * Operates on the root Firestore collection "exercises/{exerciseId}".
 *
 * Implements strict soft-delete archiving, audit field management (createdAt, updatedAt,
 * createdBy, updatedBy, version), and stable unique ID enforcement.
 */
class FirebaseExerciseRepository : ExerciseRepository {

    companion object {
        private val exerciseCache = createThreadSafeMap<String, Exercise>()

        fun cacheExercise(exercise: Exercise) {
            val cleanId = exercise.id.trim()
            if (cleanId.isNotBlank()) {
                exerciseCache[cleanId] = exercise
                val canonId = SampleExerciseData.resolveCanonicalId(cleanId)
                if (canonId.isNotBlank() && canonId != cleanId) {
                    exerciseCache[canonId] = exercise
                }
            }
        }

        fun invalidateCache(exerciseId: String) {
            val cleanId = exerciseId.trim()
            exerciseCache.remove(cleanId)
            val canonId = SampleExerciseData.resolveCanonicalId(cleanId)
            if (canonId.isNotBlank() && canonId != cleanId) {
                exerciseCache.remove(canonId)
            }
        }

        fun clearCache() {
            exerciseCache.clear()
        }
    }

    private val firestore get() = FitnessFirebase.firestore
    private val exercisesCollection get() = firestore.collection("exercises")

    private fun currentAuthUid(): String {
        return FitnessFirebase.auth.currentUser?.uid
            ?: UserSession.currentUserId
            ?: ""
    }

    /**
     * Converts a display name into a URL/ID-safe slug (e.g., "Barbell Bench Press" -> "barbell_bench_press").
     */
    fun generateSlug(name: String): String {
        val cleaned = name.trim()
            .lowercase()
            .replace(Regex("[^a-z0-9\\s_]"), "")
            .replace(Regex("\\s+"), "_")
            .trim('_')
        return cleaned.ifBlank { "exercise_${getCurrentEpochMillis()}" }
    }

    /**
     * Resiliently parses an exercise document from a Firestore snapshot.
     * Tries automatic kotlinx deserialization, with fallback to manual attribute extraction.
     */
    fun parseExercise(doc: DocumentSnapshot): Exercise? {
        if (!doc.exists) return null

        // 1. Primary path: GitLive typed data deserialization
        try {
            val parsed = doc.data<Exercise>()
            return parsed.copy(id = parsed.id.ifBlank { doc.id })
        } catch (_: Exception) {
            // Fall through to manual parsing for backward/forward compatibility
        }

        // 2. Resilient fallback path
        return try {
            val id = doc.id
            val name = try { doc.get<String>("name") } catch (_: Exception) { "" }
            val slug = try { doc.get<String>("slug") } catch (_: Exception) { id }
            val description = try { doc.get<String>("description") } catch (_: Exception) { "" }
            val primaryMuscle = try { doc.get<String>("primaryMuscle") } catch (_: Exception) { "" }
            val secondaryMuscles = try { doc.get<List<String>>("secondaryMuscles") } catch (_: Exception) { emptyList() }
            val muscleGroups = try { doc.get<List<String>>("muscleGroups") } catch (_: Exception) { emptyList() }
            val equipment = try { doc.get<List<String>>("equipment") } catch (_: Exception) { emptyList() }
            val category = try { doc.get<String>("category") } catch (_: Exception) { "" }
            val movementPattern = try { doc.get<String>("movementPattern") } catch (_: Exception) { "" }
            val difficulty = try { doc.get<String>("difficulty") } catch (_: Exception) { "" }
            val instructions = try { doc.get<List<String>>("instructions") } catch (_: Exception) { emptyList() }
            val setupSteps = try { doc.get<List<String>>("setupSteps") } catch (_: Exception) { emptyList() }
            val executionSteps = try { doc.get<List<String>>("executionSteps") } catch (_: Exception) { emptyList() }
            val breathing = try { doc.get<String>("breathing") } catch (_: Exception) { "" }
            val formTips = try { doc.get<List<String>>("formTips") } catch (_: Exception) { emptyList() }
            val commonMistakes = try { doc.get<List<String>>("commonMistakes") } catch (_: Exception) { emptyList() }
            val defaultSets = try { doc.get<Int>("defaultSets") } catch (_: Exception) { 3 }
            val defaultReps = try { doc.get<String>("defaultReps") } catch (_: Exception) { "" }
            val defaultRestSeconds = try { doc.get<Int>("defaultRestSeconds") } catch (_: Exception) { 90 }

            val media = try {
                doc.get<ExerciseMedia>("media")
            } catch (_: Exception) {
                // If nested ExerciseMedia object decoding fails, extract fields directly from map or root
                val mediaMap = try { doc.get<Map<String, Any?>>("media") } catch (_: Exception) { null }
                val videoUrl = (mediaMap?.get("videoUrl") as? String)
                    ?: (try { doc.get<String>("media.videoUrl") } catch (_: Exception) { null })
                    ?: (try { doc.get<String>("videoUrl") } catch (_: Exception) { "" })
                val thumbnailUrl = (mediaMap?.get("thumbnailUrl") as? String)
                    ?: (try { doc.get<String>("media.thumbnailUrl") } catch (_: Exception) { null })
                    ?: (try { doc.get<String>("thumbnailUrl") } catch (_: Exception) { "" })
                val animationUrl = (mediaMap?.get("animationUrl") as? String)
                    ?: (try { doc.get<String>("media.animationUrl") } catch (_: Exception) { null })
                    ?: (try { doc.get<String>("animationUrl") } catch (_: Exception) { "" })
                val mediaType = (mediaMap?.get("mediaType") as? String)
                    ?: (try { doc.get<String>("media.mediaType") } catch (_: Exception) { null })
                    ?: (try { doc.get<String>("mediaType") } catch (_: Exception) { "NONE" })

                ExerciseMedia(
                    thumbnailUrl = thumbnailUrl,
                    animationUrl = animationUrl,
                    videoUrl = videoUrl,
                    mediaType = mediaType
                )
            }

            val isActive = try { doc.get<Boolean>("isActive") } catch (_: Exception) { true }
            val createdAt = try { doc.get<Long?>("createdAt") } catch (_: Exception) { null }
            val updatedAt = try { doc.get<Long?>("updatedAt") } catch (_: Exception) { null }
            val createdBy = try { doc.get<String>("createdBy") } catch (_: Exception) { "" }
            val updatedBy = try { doc.get<String>("updatedBy") } catch (_: Exception) { "" }
            val version = try { doc.get<Int>("version") } catch (_: Exception) { 1 }
            val gender = try { doc.get<String>("gender") } catch (_: Exception) { "" }

            Exercise(
                id = id,
                name = name,
                slug = slug,
                description = description,
                primaryMuscle = primaryMuscle,
                secondaryMuscles = secondaryMuscles,
                muscleGroups = muscleGroups,
                equipment = equipment,
                category = category,
                movementPattern = movementPattern,
                difficulty = difficulty,
                instructions = instructions,
                setupSteps = setupSteps,
                executionSteps = executionSteps,
                breathing = breathing,
                formTips = formTips,
                commonMistakes = commonMistakes,
                defaultSets = defaultSets,
                defaultReps = defaultReps,
                defaultRestSeconds = defaultRestSeconds,
                media = media,
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

    override suspend fun getExercise(exerciseId: String): Exercise? {
        val rawId = exerciseId.trim()
        if (rawId.isBlank()) return null
        if (SampleExerciseData.isDeleted(rawId)) return null
        val canonicalId = SampleExerciseData.resolveCanonicalId(rawId)
        if (SampleExerciseData.isDeleted(canonicalId)) return null

        // 1. Fast in-memory cache check (<1ms)
        val cached = exerciseCache[rawId] ?: exerciseCache[canonicalId]
        if (cached != null && !SampleExerciseData.isDeleted(cached.id)) {
            return cached
        }

        // 2. Fast check of SampleExerciseData (already pre-loaded / synchronized)
        val inMemory = SampleExerciseData.findCanonical(canonicalId, rawId)
            ?: SampleExerciseData.findCanonical(rawId)
            ?: SampleExerciseData.getById(canonicalId)
            ?: SampleExerciseData.getById(rawId)
        if (inMemory != null && inMemory.media.videoUrl.isNotBlank() &&
            !inMemory.media.videoUrl.contains("fitness.app/demo", ignoreCase = true) &&
            !SampleExerciseData.isDeleted(inMemory.id)) {
            cacheExercise(inMemory)
            return inMemory
        }

        // 3. Remote Firestore query only when not already cached
        return withContext(AppDispatchers.IO) {
            try {
                var doc = exercisesCollection.document(canonicalId).get()
                if (!doc.exists && canonicalId != rawId) {
                    doc = exercisesCollection.document(rawId).get()
                }
                if (doc.exists) {
                    val parsed = withContext(AppDispatchers.Default) { parseExercise(doc) }
                    if (parsed != null) {
                        if (SampleExerciseData.isDeleted(parsed.id)) return@withContext null
                        val existing = SampleExerciseData.getById(parsed.id)
                        val effective = if (parsed.media.videoUrl.isBlank() && existing?.media?.videoUrl?.isNotBlank() == true) {
                            parsed.copy(media = existing.media)
                        } else {
                            parsed
                        }
                        cacheExercise(effective)
                        SampleExerciseData.updateCanonicalExercise(effective)
                        return@withContext effective
                    }
                }
                // Fallback to canonical baseline in-memory single source of truth
                val fallback = inMemory
                if (fallback != null && !SampleExerciseData.isDeleted(fallback.id)) {
                    cacheExercise(fallback)
                    fallback
                } else {
                    null
                }
            } catch (e: Exception) {
                println("[ERROR] FirebaseExerciseRepository.getExercise failed for $rawId ($canonicalId): ${e.message}")
                val fallback = inMemory
                if (fallback != null && !SampleExerciseData.isDeleted(fallback.id)) fallback else null
            }
        }
    }

    override suspend fun getActiveExercises(): List<Exercise> = withContext(AppDispatchers.IO) {
        try {
            val (activeDocs, archivedDocs) = coroutineScope {
                val activeDeferred = async {
                    try {
                        exercisesCollection.where { "isActive" equalTo true }.get().documents
                    } catch (_: Exception) {
                        try { exercisesCollection.get().documents } catch (_: Exception) { emptyList<DocumentSnapshot>() }
                    }
                }
                val archivedDeferred = async {
                    try {
                        exercisesCollection.where { "isActive" equalTo false }.get().documents
                    } catch (_: Exception) {
                        emptyList<DocumentSnapshot>()
                    }
                }
                Pair(activeDeferred.await(), archivedDeferred.await())
            }

            withContext(AppDispatchers.Default) {
                val firestoreList = activeDocs.mapNotNull { doc ->
                    parseExercise(doc)
                }.filter { it.isActive && !SampleExerciseData.isDeleted(it.id) }

                firestoreList.forEach { cacheExercise(it) }
                SampleExerciseData.registerCanonicalExercises(firestoreList)

                // Also synchronize archived exercises so in-memory overrides know which ones are inactive
                val firestoreArchived = archivedDocs.mapNotNull { doc ->
                    parseExercise(doc)
                }
                firestoreArchived.forEach { archived ->
                    invalidateCache(archived.id)
                    SampleExerciseData.archiveCanonicalExercise(archived.id)
                }

                val merged = SampleExerciseData.getAllCanonical()
                    .filter { it.isActive && !SampleExerciseData.isDeleted(it.id) && !SampleExerciseData.isArchived(it.id) }
                    .associateBy { it.id }
                    .toMutableMap()

                firestoreList.forEach { ex ->
                    if (ex.id.isNotBlank() && ex.isActive && !SampleExerciseData.isDeleted(ex.id) && !SampleExerciseData.isArchived(ex.id)) {
                        merged[ex.id] = ex
                    }
                }
                merged.values.filter { it.isActive && !SampleExerciseData.isDeleted(it.id) && !SampleExerciseData.isArchived(it.id) }.sortedBy { it.name }
            }
        } catch (e: Exception) {
            println("[ERROR] FirebaseExerciseRepository.getActiveExercises failed: ${e.message}")
            withContext(AppDispatchers.Default) {
                SampleExerciseData.getAllCanonical()
                    .filter { it.isActive && !SampleExerciseData.isDeleted(it.id) && !SampleExerciseData.isArchived(it.id) }
                    .sortedBy { it.name }
            }
        }
    }

    /**
     * Real-time Flow emitting list of active exercises from Firestore.
     * Updates SampleExerciseData and triggers UI recomposition whenever exercises are added or updated.
     */
    fun observeActiveExercises(): Flow<List<Exercise>> {
        return try {
            exercisesCollection.where { "isActive" equalTo true }.snapshots.map { snapshot ->
                val exercises = snapshot.documents.mapNotNull { parseExercise(it) }
                    .filter { it.isActive && !SampleExerciseData.isDeleted(it.id) && !SampleExerciseData.isArchived(it.id) }
                SampleExerciseData.registerCanonicalExercises(exercises)
                exercises
            }.flowOn(AppDispatchers.Default)
        } catch (e: Exception) {
            println("[ERROR] FirebaseExerciseRepository.observeActiveExercises error: ${e.message}")
            flowOf(emptyList())
        }
    }

    override suspend fun getExercisesForAdmin(status: String): List<Exercise> = withContext(AppDispatchers.IO) {
        try {
            val snapshot = when (status.uppercase()) {
                "ACTIVE" -> exercisesCollection.where { "isActive" equalTo true }.get()
                "ARCHIVED" -> exercisesCollection.where { "isActive" equalTo false }.get()
                else -> exercisesCollection.get()
            }
            withContext(AppDispatchers.Default) {
                val firestoreList = snapshot.documents.mapNotNull { doc ->
                    parseExercise(doc)
                }.filter { !SampleExerciseData.isDeleted(it.id) }
                SampleExerciseData.registerCanonicalExercises(firestoreList)

                val merged = SampleExerciseData.getAllCanonical()
                    .filter { !SampleExerciseData.isDeleted(it.id) }
                    .associateBy { it.id }
                    .toMutableMap()
                firestoreList.forEach { if (it.id.isNotBlank() && !SampleExerciseData.isDeleted(it.id)) merged[it.id] = it }

                val all = merged.values.filter { !SampleExerciseData.isDeleted(it.id) }
                val filtered = when (status.uppercase()) {
                    "ACTIVE" -> all.filter { it.isActive && !SampleExerciseData.isArchived(it.id) }
                    "ARCHIVED" -> all.filter { !it.isActive || SampleExerciseData.isArchived(it.id) }
                    else -> all
                }
                filtered.sortedBy { it.name }
            }
        } catch (e: Exception) {
            println("[ERROR] FirebaseExerciseRepository.getExercisesForAdmin failed: ${e.message}")
            withContext(AppDispatchers.Default) {
                val all = SampleExerciseData.getAllCanonical().filter { !SampleExerciseData.isDeleted(it.id) }
                when (status.uppercase()) {
                    "ACTIVE" -> all.filter { it.isActive && !SampleExerciseData.isArchived(it.id) }
                    "ARCHIVED" -> all.filter { !it.isActive || SampleExerciseData.isArchived(it.id) }
                    else -> all
                }.sortedBy { it.name }
            }
        }
    }

    override suspend fun getAllExercisesForAdmin(): List<Exercise> = withContext(AppDispatchers.IO) {
        getExercisesForAdmin("ALL")
    }

    /**
     * Validates domain constraints on an [Exercise]:
     * - Non-blank ID and name.
     * - Non-blank primaryMuscle.
     * - Positive defaultSets and non-negative defaultRestSeconds.
     */
    fun validateExercise(exercise: Exercise): Result<Unit> {
        if (exercise.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Exercise name cannot be blank"))
        }
        if (exercise.id.isBlank()) {
            return Result.failure(IllegalArgumentException("Exercise ID cannot be blank"))
        }
        if (exercise.primaryMuscle.isBlank()) {
            return Result.failure(IllegalArgumentException("Primary muscle cannot be blank"))
        }
        if (exercise.defaultSets <= 0) {
            return Result.failure(IllegalArgumentException("Default sets must be greater than 0"))
        }
        if (exercise.defaultRestSeconds < 0) {
            return Result.failure(IllegalArgumentException("Default rest seconds cannot be negative"))
        }
        return Result.success(Unit)
    }

    override suspend fun createExercise(exercise: Exercise): Result<Unit> = withContext(AppDispatchers.IO) {
        try {
            val stableId = exercise.id.trim().ifBlank { generateSlug(exercise.name) }
            if (stableId.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Exercise must have a non-empty ID or Name"))
            }

            val now = getCurrentEpochMillis()
            val uid = currentAuthUid()

            val prepared = exercise.copy(
                id = stableId,
                slug = exercise.slug.trim().ifBlank { stableId },
                isActive = exercise.isActive,
                createdAt = exercise.createdAt ?: now,
                updatedAt = now,
                createdBy = exercise.createdBy.ifBlank { uid },
                updatedBy = uid,
                version = exercise.version.coerceAtLeast(1)
            )

            val validation = validateExercise(prepared)
            if (validation.isFailure) {
                return@withContext validation
            }

            exercisesCollection.document(stableId).set(prepared)
            SampleExerciseData.updateCanonicalExercise(prepared)
            cacheExercise(prepared)
            Result.success(Unit)
        } catch (e: Exception) {
            println("[ERROR] FirebaseExerciseRepository.createExercise failed: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateExercise(exercise: Exercise): Result<Unit> = withContext(AppDispatchers.IO) {
        try {
            val stableId = exercise.id.trim()
            if (stableId.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Exercise ID cannot be blank"))
            }

            val docRef = exercisesCollection.document(stableId)
            val existingDoc = try { docRef.get() } catch (_: Exception) { null }
            val existing = if (existingDoc != null && existingDoc.exists) {
                parseExercise(existingDoc)
            } else {
                SampleExerciseData.getById(stableId)
            }

            val now = getCurrentEpochMillis()
            val uid = currentAuthUid()

            val updated = exercise.copy(
                id = stableId,
                slug = exercise.slug.trim().ifBlank { existing?.slug?.ifBlank { stableId } ?: stableId },
                createdAt = existing?.createdAt ?: now,
                createdBy = existing?.createdBy?.ifBlank { uid } ?: uid,
                updatedAt = now,
                updatedBy = uid,
                version = (existing?.version ?: 0) + 1
            )

            val validation = validateExercise(updated)
            if (validation.isFailure) {
                return@withContext validation
            }

            docRef.set(updated)
            SampleExerciseData.updateCanonicalExercise(updated)
            cacheExercise(updated)
            Result.success(Unit)
        } catch (e: Exception) {
            println("[ERROR] FirebaseExerciseRepository.updateExercise failed: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun archiveExercise(exerciseId: String): Result<Unit> = withContext(AppDispatchers.IO) {
        try {
            val stableId = exerciseId.trim()
            if (stableId.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Exercise ID cannot be blank"))
            }
            val canonicalId = SampleExerciseData.resolveCanonicalId(stableId)

            val docRef = exercisesCollection.document(stableId)
            val existingDoc = try { docRef.get() } catch (_: Exception) { null }
            val existing = if (existingDoc != null && existingDoc.exists) {
                parseExercise(existingDoc)
            } else {
                SampleExerciseData.getById(stableId) ?: SampleExerciseData.getById(canonicalId)
            } ?: return@withContext Result.failure(NoSuchElementException("Exercise with ID '$stableId' does not exist"))

            val now = getCurrentEpochMillis()
            val uid = currentAuthUid()

            val archived = existing.copy(
                isActive = false,
                updatedAt = now,
                updatedBy = uid,
                version = existing.version + 1
            )

            docRef.set(archived)
            if (canonicalId != stableId) {
                try { exercisesCollection.document(canonicalId).set(archived.copy(id = canonicalId)) } catch (_: Exception) {}
            }
            SampleExerciseData.archiveCanonicalExercise(stableId)
            SampleExerciseData.archiveCanonicalExercise(canonicalId)
            invalidateCache(stableId)
            invalidateCache(canonicalId)
            Result.success(Unit)
        } catch (e: Exception) {
            println("[ERROR] FirebaseExerciseRepository.archiveExercise failed: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun restoreExercise(exerciseId: String): Result<Unit> = withContext(AppDispatchers.IO) {
        try {
            val stableId = exerciseId.trim()
            if (stableId.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Exercise ID cannot be blank"))
            }
            val canonicalId = SampleExerciseData.resolveCanonicalId(stableId)

            val docRef = exercisesCollection.document(stableId)
            val existingDoc = try { docRef.get() } catch (_: Exception) { null }
            val existing = if (existingDoc != null && existingDoc.exists) {
                parseExercise(existingDoc)
            } else {
                SampleExerciseData.getById(stableId) ?: SampleExerciseData.getById(canonicalId)
            } ?: return@withContext Result.failure(NoSuchElementException("Exercise with ID '$stableId' does not exist"))

            val now = getCurrentEpochMillis()
            val uid = currentAuthUid()

            val restored = existing.copy(
                isActive = true,
                updatedAt = now,
                updatedBy = uid,
                version = existing.version + 1
            )

            docRef.set(restored)
            if (canonicalId != stableId) {
                try { exercisesCollection.document(canonicalId).set(restored.copy(id = canonicalId)) } catch (_: Exception) {}
            }
            SampleExerciseData.restoreCanonicalExercise(stableId)
            SampleExerciseData.restoreCanonicalExercise(canonicalId)
            cacheExercise(restored)
            Result.success(Unit)
        } catch (e: Exception) {
            println("[ERROR] FirebaseExerciseRepository.restoreExercise failed: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun deleteExercise(exerciseId: String): Result<Unit> = withContext(AppDispatchers.IO) {
        try {
            val stableId = exerciseId.trim()
            if (stableId.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Exercise ID cannot be blank"))
            }
            val canonicalId = SampleExerciseData.resolveCanonicalId(stableId)

            // 1. Permanently delete document from Firestore
            exercisesCollection.document(stableId).delete()
            if (canonicalId != stableId) {
                try { exercisesCollection.document(canonicalId).delete() } catch (_: Exception) {}
            }

            // 2. Permanently remove from memory and mark tombstone
            SampleExerciseData.deleteCanonicalExercise(stableId)
            SampleExerciseData.deleteCanonicalExercise(canonicalId)
            invalidateCache(stableId)
            invalidateCache(canonicalId)

            Result.success(Unit)
        } catch (e: Exception) {
            println("[ERROR] FirebaseExerciseRepository.deleteExercise failed: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateExerciseMedia(exerciseId: String, media: ExerciseMedia): Result<Unit> = withContext(AppDispatchers.IO) {
        try {
            val rawId = exerciseId.trim()
            if (rawId.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Exercise ID cannot be blank"))
            }
            val stableId = SampleExerciseData.resolveCanonicalId(rawId)

            val docRef = exercisesCollection.document(stableId)
            val existingDoc = try { docRef.get() } catch (_: Exception) { null }
            val existing = if (existingDoc != null && existingDoc.exists) {
                parseExercise(existingDoc)
            } else {
                SampleExerciseData.findCanonical(stableId)
                    ?: SampleExerciseData.findCanonical(rawId)
                    ?: SampleExerciseData.getById(stableId)
                    ?: SampleExerciseData.getById(rawId)
            } ?: return@withContext Result.failure(NoSuchElementException("Exercise with ID '$stableId' does not exist"))

            val now = getCurrentEpochMillis()
            val uid = currentAuthUid()

            // Preserves all other exercise metadata (names, instructions, sets/reps, biomechanics)
            val updated = existing.copy(
                media = media,
                updatedAt = now,
                updatedBy = uid,
                version = existing.version + 1
            )

            docRef.set(updated)
            SampleExerciseData.updateCanonicalExercise(updated)
            cacheExercise(updated)
            Result.success(Unit)
        } catch (e: Exception) {
            println("[ERROR] FirebaseExerciseRepository.updateExerciseMedia failed: ${e.message}")
            Result.failure(e)
        }
    }
}
