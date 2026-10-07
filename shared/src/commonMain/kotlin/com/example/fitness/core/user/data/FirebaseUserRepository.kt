package com.example.fitness.core.user.data

import com.example.fitness.core.firebase.FitnessFirebase
import com.example.fitness.core.user.model.UserProfile
import com.example.fitness.core.user.util.MobileNumberValidator
import com.example.fitness.feature.exercise.media.MediaUploadFile
import com.example.fitness.getCurrentEpochMillis
import com.example.fitness.platformLog
import com.example.fitness.core.coroutine.AppDispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Repository managing Firestore persistence for user profiles in users/{uid}.
 * Uses GitLive Firebase Multiplatform SDK via FitnessFirebase.firestore.
 */
class FirebaseUserRepository {

    companion object {
        private val profileCache = com.example.fitness.createThreadSafeMap<String, UserProfile>()

        fun clearProfileCache() {
            profileCache.clear()
        }

        fun cacheProfile(profile: UserProfile?) {
            if (profile != null && profile.uid.isNotBlank()) {
                profileCache[profile.uid] = profile
            }
        }

        fun getCachedProfile(uid: String): UserProfile? = profileCache[uid]
    }

    private val TAG = "FirebaseUserRepository"
    private val firestore get() = FitnessFirebase.firestore
    private val usersCollection get() = firestore.collection("users")

    /**
     * Reads the user document from users/{uid}.
     * Returns UserProfile if document exists, or null if it does not exist or an error occurs.
     * Hits thread-safe memory cache first unless forceRefresh is true.
     * Enforces a 6s safety timeout to prevent infinite loading on Samsung/Android network stalls.
     */
    suspend fun getUserProfile(uid: String, forceRefresh: Boolean = false): UserProfile? = withContext(AppDispatchers.IO) {
        if (uid.isBlank()) return@withContext null
        if (!forceRefresh) {
            profileCache[uid]?.let { cached ->
                platformLog(TAG, "[PROFILE_CACHE_HIT] Returning cached profile for uid=$uid, gender='${cached.gender}'")
                return@withContext cached
            }
        }
        platformLog(TAG, "[LOGIN] Firestore profile START (users/$uid)")
        try {
            val snapshot = withTimeoutOrNull(6_000L) {
                usersCollection.document(uid).get()
            }
            if (snapshot == null) {
                platformLog(TAG, "[LOGIN] Firestore profile FAILURE: Reading users/$uid timed out after 6s")
                null
            } else if (!snapshot.exists) {
                platformLog(TAG, "[LOGIN] Firestore profile SUCCESS: users/$uid does not exist (new user)")
                null
            } else {
                val email: String = try { snapshot.get<String>("email") } catch (_: Exception) { "" }
                val name: String = try { snapshot.get<String>("name") } catch (_: Exception) { "" }
                val gender: String = try { snapshot.get<String>("gender") } catch (_: Exception) { "" }
                val mobileNumber: String = try { snapshot.get<String>("mobileNumber") } catch (_: Exception) { "" }
                val profileImageUrl: String = try { snapshot.get<String>("profileImageUrl") } catch (_: Exception) { "" }
                val workoutDays: String = try { snapshot.get<String>("workoutDays") } catch (_: Exception) { "" }
                val fitnessLevel: String = try { snapshot.get<String>("fitnessLevel") } catch (_: Exception) { "" }
                val profile = UserProfile(
                    uid = uid,
                    email = email,
                    name = name,
                    gender = gender,
                    mobileNumber = mobileNumber,
                    profileImageUrl = profileImageUrl,
                    workoutDays = workoutDays,
                    fitnessLevel = fitnessLevel
                )
                profileCache[uid] = profile
                platformLog(TAG, "[LOGIN] Firestore profile SUCCESS: Profile loaded for uid=$uid, gender='$gender'")
                profile
            }
        } catch (e: Exception) {
            platformLog(TAG, "[LOGIN] Firestore profile FAILURE: ${e.message}")
            null
        }
    }

    /**
     * Creates or updates the user profile under users/{uid}.
     * If document exists, merges updates and NEVER overwrites a non-empty name with a blank value.
     * Protected by a 6s safety timeout.
     */
    suspend fun saveUserProfile(
        uid: String,
        email: String,
        name: String,
        gender: String = "",
        authProvider: String = "",
        mobileNumber: String = "",
        profileImageUrl: String = "",
        workoutDays: String = "",
        fitnessLevel: String = ""
    ): Boolean = withContext(AppDispatchers.IO) {
        if (uid.isBlank()) return@withContext false
        try {
            withTimeoutOrNull(6_000L) {
                val docRef = usersCollection.document(uid)
                val snapshot = docRef.get()
                val effectiveProvider = authProvider.ifBlank {
                    com.example.fitness.core.session.UserSession.currentAuthProvider
                }
                if (snapshot.exists) {
                    val existingName: String = try { snapshot.get<String>("name") } catch (_: Exception) { "" }
                    val existingEmail: String = try { snapshot.get<String>("email") } catch (_: Exception) { "" }
                    val existingGender: String = try { snapshot.get<String>("gender") } catch (_: Exception) { "" }
                    val existingMobile: String = try { snapshot.get<String>("mobileNumber") } catch (_: Exception) { "" }
                    val existingImage: String = try { snapshot.get<String>("profileImageUrl") } catch (_: Exception) { "" }
                    val existingDays: String = try { snapshot.get<String>("workoutDays") } catch (_: Exception) { "" }
                    val existingLevel: String = try { snapshot.get<String>("fitnessLevel") } catch (_: Exception) { "" }

                    val finalEmail = if (email.isNotBlank()) email else existingEmail
                    val finalName = if (name.isNotBlank()) name else existingName
                    val finalGender = if (gender.isNotBlank()) gender else existingGender
                    val finalMobile = if (mobileNumber.isNotBlank()) mobileNumber else existingMobile
                    val finalImage = if (profileImageUrl.isNotBlank()) profileImageUrl else existingImage
                    val finalDays = if (workoutDays.isNotBlank()) workoutDays else existingDays
                    val finalLevel = if (fitnessLevel.isNotBlank()) fitnessLevel else existingLevel

                    val data = mutableMapOf<String, String>()
                    if (finalEmail.isNotBlank()) data["email"] = finalEmail
                    if (finalName.isNotBlank()) data["name"] = finalName
                    if (finalGender.isNotBlank()) data["gender"] = finalGender
                    if (finalMobile.isNotBlank()) data["mobileNumber"] = finalMobile
                    if (finalImage.isNotBlank()) data["profileImageUrl"] = finalImage
                    if (finalDays.isNotBlank()) data["workoutDays"] = finalDays
                    if (finalLevel.isNotBlank()) data["fitnessLevel"] = finalLevel
                    if (effectiveProvider.isNotBlank()) data["authProvider"] = effectiveProvider

                    docRef.set(data, merge = true)
                } else {
                    val data = mutableMapOf<String, String>()
                    if (email.isNotBlank()) data["email"] = finalEmailOr(email)
                    if (name.isNotBlank()) data["name"] = name
                    if (gender.isNotBlank()) data["gender"] = gender
                    if (mobileNumber.isNotBlank()) data["mobileNumber"] = mobileNumber
                    if (profileImageUrl.isNotBlank()) data["profileImageUrl"] = profileImageUrl
                    if (workoutDays.isNotBlank()) data["workoutDays"] = workoutDays
                    if (fitnessLevel.isNotBlank()) data["fitnessLevel"] = fitnessLevel
                    if (effectiveProvider.isNotBlank()) data["authProvider"] = effectiveProvider
                    docRef.set(data)
                }
                true
            } ?: false
        } catch (e: Exception) {
            platformLog(TAG, "[PROFILE_SAVE_ERROR] Error saving users/$uid: ${e.message}")
            false
        }
    }

    private fun finalEmailOr(email: String): String = email

    /**
     * Updates editable profile fields (name, gender, mobileNumber) for user with UID [uid].
     * Enforces required non-blank name validation, trims whitespace, validates Indian mobile number if non-empty,
     * and preserves existing data on failure.
     */
    suspend fun updateUserProfile(
        uid: String,
        name: String,
        gender: String = "",
        mobileNumber: String = ""
    ): Result<UserProfile> = withContext(AppDispatchers.IO) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Name cannot be blank"))
        }

        if (uid.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("User ID cannot be blank"))
        }

        val trimmedMobile = mobileNumber.trim()
        val normalizedMobile: String? = if (trimmedMobile.isNotBlank()) {
            val normalized = MobileNumberValidator.normalize(trimmedMobile)
            if (normalized == null) {
                return@withContext Result.failure(IllegalArgumentException("Please enter a valid 10-digit Indian mobile number"))
            }
            normalized
        } else {
            ""
        }

        try {
            val docRef = usersCollection.document(uid)
            val snapshot = docRef.get()
            val existingEmail: String = if (snapshot.exists) {
                try { snapshot.get<String>("email") } catch (_: Exception) { "" }
            } else ""
            val existingImage: String = if (snapshot.exists) {
                try { snapshot.get<String>("profileImageUrl") } catch (_: Exception) { "" }
            } else ""
            val existingDays: String = if (snapshot.exists) {
                try { snapshot.get<String>("workoutDays") } catch (_: Exception) { "" }
            } else ""
            val existingLevel: String = if (snapshot.exists) {
                try { snapshot.get<String>("fitnessLevel") } catch (_: Exception) { "" }
            } else ""

            val trimmedGender = gender.trim()
            val data = mutableMapOf<String, Any>(
                "name" to trimmedName
            )
            if (trimmedGender.isNotBlank()) {
                data["gender"] = trimmedGender
            }
            if (normalizedMobile != null) {
                data["mobileNumber"] = normalizedMobile
            }

            docRef.set(data, merge = true)

            val updatedProfile = UserProfile(
                uid = uid,
                email = existingEmail,
                name = trimmedName,
                gender = trimmedGender,
                mobileNumber = normalizedMobile ?: "",
                profileImageUrl = existingImage,
                workoutDays = existingDays,
                fitnessLevel = existingLevel
            )

            // Update in-memory UserSession
            com.example.fitness.core.session.UserSession.setUserProfile(updatedProfile)

            Result.success(updatedProfile)
        } catch (e: Exception) {
            println("[DEBUG] FirebaseUserRepository.updateUserProfile error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Updates workout preferences (gender, workoutDays, fitnessLevel) for user with UID [uid].
     * Persists to users/{uid} document with merge = true.
     * Updates in-memory UserSession and handles active program compatibility.
     */
    suspend fun updateWorkoutPreferences(
        uid: String,
        gender: String,
        workoutDays: String,
        fitnessLevel: String
    ): Result<UserProfile> = withContext(AppDispatchers.IO) {
        val trimmedUid = uid.trim()
        if (trimmedUid.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("User ID cannot be blank"))
        }

        val trimmedGender = gender.trim()
        val trimmedDays = workoutDays.trim()
        val trimmedLevel = fitnessLevel.trim()

        try {
            val docRef = usersCollection.document(trimmedUid)
            val snapshot = docRef.get()

            val existingEmail: String = if (snapshot.exists) {
                try { snapshot.get<String>("email") } catch (_: Exception) { "" }
            } else ""
            val existingName: String = if (snapshot.exists) {
                try { snapshot.get<String>("name") } catch (_: Exception) { "" }
            } else ""
            val existingMobile: String = if (snapshot.exists) {
                try { snapshot.get<String>("mobileNumber") } catch (_: Exception) { "" }
            } else ""
            val existingImage: String = if (snapshot.exists) {
                try { snapshot.get<String>("profileImageUrl") } catch (_: Exception) { "" }
            } else ""

            val data = mutableMapOf<String, Any>()
            if (trimmedGender.isNotBlank()) data["gender"] = trimmedGender
            if (trimmedDays.isNotBlank()) data["workoutDays"] = trimmedDays
            if (trimmedLevel.isNotBlank()) data["fitnessLevel"] = trimmedLevel

            docRef.set(data, merge = true)

            val updatedProfile = UserProfile(
                uid = trimmedUid,
                email = existingEmail.ifBlank { com.example.fitness.core.session.UserSession.currentUserEmail.orEmpty() },
                name = existingName.ifBlank { com.example.fitness.core.session.UserSession.currentUserName.orEmpty() },
                gender = trimmedGender.ifBlank { com.example.fitness.core.session.UserSession.currentUserGender.orEmpty() },
                mobileNumber = existingMobile,
                profileImageUrl = existingImage,
                workoutDays = trimmedDays,
                fitnessLevel = trimmedLevel
            )

            // Update in-memory UserSession & active program compatibility
            com.example.fitness.core.session.UserSession.updateWorkoutPreferences(
                gender = trimmedGender,
                workoutDays = trimmedDays,
                fitnessLevel = trimmedLevel
            )

            Result.success(updatedProfile)
        } catch (e: Exception) {
            println("[DEBUG] FirebaseUserRepository.updateWorkoutPreferences error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Uploads a new profile picture to Cloudflare Worker R2 endpoint (PUT /v1/profile/photo),
     * fetches the image back via GET /v1/profile/photo with the Bearer token,
     * updates Firestore users/{uid}.profileImageUrl, and updates UserSession.
     * Enforces the 5 MB maximum size limit.
     */
    suspend fun updateUserProfilePicture(
        uid: String,
        file: MediaUploadFile
    ): Result<String> = withContext(AppDispatchers.IO) {
        val currentUser = FitnessFirebase.auth.currentUser
        if (currentUser == null || currentUser.uid.isBlank()) {
            return@withContext Result.failure(IllegalStateException("No authenticated user found"))
        }

        val effectiveUid = currentUser.uid

        if (file.bytes.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Selected image file is empty"))
        }

        if (file.bytes.size > CloudflareProfilePhotoService.MAX_PHOTO_SIZE_BYTES ||
            file.sizeBytes > CloudflareProfilePhotoService.MAX_PHOTO_SIZE_BYTES) {
            return@withContext Result.failure(IllegalArgumentException("Selected image exceeds maximum allowed size of 5 MB."))
        }

        // Retrieve fresh Firebase ID token (forceRefresh = true)
        val token = try {
            currentUser.getIdToken(true)
        } catch (e: Exception) {
            return@withContext Result.failure(IllegalStateException("Failed to verify user credentials: ${e.message}"))
        }

        if (token.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("Authentication expired. Please sign in again."))
        }

        // STEP 1: Upload image to Cloudflare Worker R2 via PUT /v1/profile/photo
        val uploadResult = CloudflareProfilePhotoService.uploadProfilePhoto(
            token = token,
            mimeType = file.mimeType,
            bytes = file.bytes
        )

        if (uploadResult.isFailure) {
            return@withContext Result.failure(uploadResult.exceptionOrNull() ?: IllegalStateException("Failed to upload photo"))
        }

        // STEP 2: Fetch the uploaded photo through GET /v1/profile/photo with the Firebase ID token
        val fetchResult = CloudflareProfilePhotoService.getProfilePhoto(token)
        val fetchedBytes = fetchResult.getOrNull()
        if (fetchedBytes != null && fetchedBytes.isNotEmpty()) {
            val bmp = withContext(AppDispatchers.Default) {
                com.example.fitness.decodeByteArrayToBitmap(fetchedBytes)
            }
            if (bmp != null) {
                com.example.fitness.core.components.ProfileImageMemoryCache.put(CloudflareProfilePhotoService.PHOTO_ENDPOINT, bmp)
            }
        }

        // STEP 3: Update Firestore users/{uid}.profileImageUrl with the Cloudflare Worker photo endpoint
        val photoUrl = CloudflareProfilePhotoService.PHOTO_ENDPOINT
        try {
            val docRef = usersCollection.document(effectiveUid)
            docRef.set(mapOf("profileImageUrl" to photoUrl), merge = true)

            // Update UserSession
            com.example.fitness.core.session.UserSession.currentUserProfileImageUrl = photoUrl
            val currentProfile = com.example.fitness.core.session.UserSession.currentUserProfile
            if (currentProfile != null) {
                com.example.fitness.core.session.UserSession.currentUserProfile = currentProfile.copy(profileImageUrl = photoUrl)
            }

            Result.success(photoUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Removes the profile picture from users/{uid}, updating Firestore and UserSession.
     * Deletes the R2 object via Cloudflare Worker DELETE /v1/profile/photo idempotently.
     * Preserves the Firestore user profile document.
     */
    suspend fun removeUserProfilePicture(uid: String): Result<Unit> = withContext(AppDispatchers.IO) {
        val effectiveUid = FitnessFirebase.auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: uid.trim().takeIf { it.isNotBlank() }
            ?: com.example.fitness.core.session.UserSession.currentUserId

        if (effectiveUid.isNullOrBlank()) {
            return@withContext Result.failure(IllegalArgumentException("User ID cannot be blank"))
        }

        val currentUser = FitnessFirebase.auth.currentUser
        val token = try {
            currentUser?.getIdToken(true)
        } catch (_: Exception) {
            null
        }

        if (!token.isNullOrBlank()) {
            val deleteResult = CloudflareProfilePhotoService.deleteProfilePhoto(token)
            if (deleteResult.isFailure) {
                val err = deleteResult.exceptionOrNull()
                if (err is IllegalStateException && err.message?.contains("Server error") == true) {
                    return@withContext Result.failure(err)
                }
            }
        }

        try {
            // Update Firestore users/{uid}.profileImageUrl to empty without deleting user document
            val docRef = usersCollection.document(effectiveUid)
            docRef.set(mapOf("profileImageUrl" to ""), merge = true)

            // Update UserSession and clear memory cache
            com.example.fitness.core.components.ProfileImageMemoryCache.clear()
            com.example.fitness.core.session.UserSession.currentUserProfileImageUrl = null
            val currentProfile = com.example.fitness.core.session.UserSession.currentUserProfile
            if (currentProfile != null) {
                com.example.fitness.core.session.UserSession.currentUserProfile = currentProfile.copy(profileImageUrl = "")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates the user's gender in users/{uid}.
     * If document exists, merges gender without altering existing fields.
     * If document does not exist, creates it with fallback email/name.
     */
    suspend fun updateUserGender(
        uid: String,
        gender: String,
        fallbackEmail: String = "",
        fallbackName: String = ""
    ): Boolean = withContext(AppDispatchers.IO) {
        if (uid.isBlank()) return@withContext false
        try {
            val docRef = usersCollection.document(uid)
            val snapshot = docRef.get()
            if (snapshot.exists) {
                docRef.set(mapOf("gender" to gender), merge = true)
            } else {
                val data = mutableMapOf<String, String>()
                if (fallbackEmail.isNotBlank()) data["email"] = fallbackEmail
                if (fallbackName.isNotBlank()) data["name"] = fallbackName
                data["gender"] = gender
                docRef.set(data)
            }
            true
        } catch (e: Exception) {
            println("[DEBUG] FirebaseUserRepository.updateUserGender error: ${e.message}")
            false
        }
    }

    /**
     * ADMIN ONLY: Fetches registered user documents from users collection.
     * Note: Authoritative ADMIN role is enforced via Firebase Auth Custom Claims,
     * not arbitrary Firestore fields.
     */
    suspend fun getAllAdminUsers(): List<com.example.fitness.feature.admin.model.AdminUserItem> = withContext(AppDispatchers.IO) {
        try {
            val snapshot = usersCollection.get()
            withContext(AppDispatchers.Default) {
                snapshot.documents.map { doc ->
                    val name: String = try { doc.get<String>("name") } catch (_: Exception) { "" }
                    val email: String = try { doc.get<String>("email") } catch (_: Exception) { "" }
                    val gender: String = try { doc.get<String>("gender") } catch (_: Exception) { "" }
                    val rawRole: String? = try { doc.get<String?>("role") } catch (_: Exception) { null }
                    val provider: String = try {
                        val p = doc.get<String?>("authProvider") ?: doc.get<String?>("provider")
                        if (!p.isNullOrBlank()) p else "Email"
                    } catch (_: Exception) {
                        "Email"
                    }

                    val isCurrentAdmin = doc.id == com.example.fitness.core.session.UserSession.currentUserId &&
                            com.example.fitness.core.session.UserSession.currentUserRole == com.example.fitness.core.auth.UserRole.ADMIN
                    val role = if (isCurrentAdmin) com.example.fitness.core.auth.UserRole.ADMIN else com.example.fitness.core.auth.UserRole.USER

                    com.example.fitness.feature.admin.model.AdminUserItem(
                        uid = doc.id,
                        name = name,
                        email = email,
                        gender = gender,
                        role = role,
                        rawRoleField = rawRole,
                        authProvider = provider
                    )
                }
            }
        } catch (e: Exception) {
            println("[DEBUG] FirebaseUserRepository.getAllAdminUsers error: ${e.message}")
            emptyList()
        }
    }

    /**
     * ADMIN ONLY: Fetches full profile and subcollection metrics for a single user.
     */
    suspend fun getAdminUserDetail(uid: String): com.example.fitness.feature.admin.model.AdminUserItem? = withContext(AppDispatchers.IO) {
        if (uid.isBlank()) return@withContext null
        try {
            val doc = usersCollection.document(uid).get()
            if (!doc.exists) return@withContext null

            val notifCount: Int? = try {
                val notifs = usersCollection.document(uid).collection("notifications").get()
                notifs.documents.size
            } catch (_: Exception) {
                null
            }

            val deviceCount: Int? = try {
                val devices = usersCollection.document(uid).collection("devices").get()
                devices.documents.size
            } catch (_: Exception) {
                null
            }

            withContext(AppDispatchers.Default) {
                val name: String = try { doc.get<String>("name") } catch (_: Exception) { "" }
                val email: String = try { doc.get<String>("email") } catch (_: Exception) { "" }
                val gender: String = try { doc.get<String>("gender") } catch (_: Exception) { "" }
                val rawRole: String? = try { doc.get<String?>("role") } catch (_: Exception) { null }
                val provider: String = try {
                    val p = doc.get<String?>("authProvider") ?: doc.get<String?>("provider")
                    if (!p.isNullOrBlank()) p else "Email"
                } catch (_: Exception) {
                    "Email"
                }

                val isCurrentAdmin = uid == com.example.fitness.core.session.UserSession.currentUserId &&
                        com.example.fitness.core.session.UserSession.currentUserRole == com.example.fitness.core.auth.UserRole.ADMIN
                val role = if (isCurrentAdmin) com.example.fitness.core.auth.UserRole.ADMIN else com.example.fitness.core.auth.UserRole.USER

                com.example.fitness.feature.admin.model.AdminUserItem(
                    uid = uid,
                    name = name,
                    email = email,
                    gender = gender,
                    role = role,
                    notificationCount = notifCount,
                    deviceCount = deviceCount,
                    rawRoleField = rawRole,
                    authProvider = provider
                )
            }
        } catch (e: Exception) {
            println("[DEBUG] FirebaseUserRepository.getAdminUserDetail error: ${e.message}")
            null
        }
    }
}
