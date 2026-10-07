package com.example.fitness.core.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.auth.RoleResolver
import com.example.fitness.core.auth.UserRole
import com.example.fitness.core.user.model.UserProfile
import com.example.fitness.feature.workout.model.ExperienceLevel
import dev.gitlive.firebase.auth.FirebaseUser

object UserSession {
    var currentUserId by mutableStateOf<String?>(null)
    var currentUserEmail by mutableStateOf<String?>(null)
    var currentUserName by mutableStateOf<String?>(null)
    var currentUserGender by mutableStateOf<String?>(null)
    val isFemale: Boolean
        get() = (currentUserGender ?: currentUserProfile?.gender)?.trim()?.equals("Female", ignoreCase = true) == true
    var currentUserMobileNumber by mutableStateOf<String?>(null)
    var currentUserProfileImageUrl by mutableStateOf<String?>(null)
    var currentUserProfile by mutableStateOf<UserProfile?>(null)
    var currentUserRole by mutableStateOf(UserRole.USER)
    var unreadNotificationCount by mutableIntStateOf(0)
    var pendingScreen by mutableStateOf<Screen?>(null)
    var workoutReminderPreference by mutableStateOf(com.example.fitness.feature.notifications.model.WorkoutReminderPreference())
    var currentAuthProvider by mutableStateOf("Email")

    val isAuthenticated: Boolean
        get() {
            val hasSessionUid = !currentUserId.isNullOrBlank()
            val hasFirebaseUser = try {
                com.example.fitness.core.firebase.FitnessFirebase.auth.currentUser != null
            } catch (_: Exception) {
                false
            }
            return hasSessionUid || hasFirebaseUser
        }

    val favoriteExerciseIds = mutableStateListOf<String>()
    var favoritesLoadedForUserId by mutableStateOf<String?>(null)

    val favoriteWorkoutIds = mutableStateListOf<String>()
    var workoutFavoritesLoadedForUserId by mutableStateOf<String?>(null)

    fun isExerciseFavorite(exerciseId: String): Boolean {
        return favoriteExerciseIds.contains(exerciseId)
    }

    fun setFavoriteLocal(exerciseId: String, isFavorite: Boolean) {
        if (isFavorite) {
            if (!favoriteExerciseIds.contains(exerciseId)) {
                favoriteExerciseIds.add(exerciseId)
            }
        } else {
            favoriteExerciseIds.remove(exerciseId)
        }
    }

    fun syncFavorites(userId: String, ids: Collection<String>) {
        favoriteExerciseIds.clear()
        favoriteExerciseIds.addAll(ids)
        favoritesLoadedForUserId = userId
    }

    fun isWorkoutFavorite(workoutId: String): Boolean {
        return favoriteWorkoutIds.contains(workoutId)
    }

    fun setWorkoutFavoriteLocal(workoutId: String, isFavorite: Boolean) {
        if (isFavorite) {
            if (!favoriteWorkoutIds.contains(workoutId)) {
                favoriteWorkoutIds.add(workoutId)
            }
        } else {
            favoriteWorkoutIds.remove(workoutId)
        }
    }

    fun syncWorkoutFavorites(userId: String, ids: Collection<String>) {
        favoriteWorkoutIds.clear()
        favoriteWorkoutIds.addAll(ids)
        workoutFavoritesLoadedForUserId = userId
    }

    fun setAuthenticatedUser(user: FirebaseUser?) {
        currentUserId = user?.uid
        currentUserEmail = user?.email
        if (user?.displayName?.isNotBlank() == true) {
            currentUserName = user.displayName
        }
    }

    suspend fun updateRoleFromUser(user: FirebaseUser?, forceRefresh: Boolean = false) {
        currentUserRole = RoleResolver.resolveRole(user, forceRefresh)
    }

    fun updateRoleFromClaims(claims: Map<String, Any?>?) {
        currentUserRole = RoleResolver.resolveRoleFromClaims(claims)
    }

    fun setUserProfile(profile: UserProfile?) {
        currentUserProfile = profile
        if (profile != null) {
            if (profile.uid.isNotBlank()) {
                currentUserId = profile.uid
                com.example.fitness.core.user.data.FirebaseUserRepository.cacheProfile(profile)
            }
            if (profile.email.isNotBlank()) currentUserEmail = profile.email
            if (profile.name.isNotBlank()) currentUserName = profile.name
            if (profile.gender.isNotBlank()) currentUserGender = profile.gender
            currentUserMobileNumber = profile.mobileNumber.takeIf { it.isNotBlank() }
            currentUserProfileImageUrl = profile.profileImageUrl.takeIf { it.isNotBlank() }
            if (profile.workoutDays.isNotBlank()) {
                weeklyCommitment = profile.workoutDays
            }
            if (profile.fitnessLevel.isNotBlank()) {
                experienceLevel = ExperienceLevel.fromUserFacingOption(profile.fitnessLevel)
            }
        } else {
            currentUserMobileNumber = null
            currentUserProfileImageUrl = null
        }
    }

    fun updateWorkoutPreferences(gender: String, workoutDays: String, fitnessLevel: String) {
        val previousGender = currentUserGender
        if (gender.isNotBlank()) {
            currentUserGender = gender
        }
        if (workoutDays.isNotBlank()) {
            weeklyCommitment = workoutDays
        }
        if (fitnessLevel.isNotBlank()) {
            experienceLevel = ExperienceLevel.fromUserFacingOption(fitnessLevel)
        }
        currentUserProfile = currentUserProfile?.copy(
            gender = gender.ifBlank { currentUserProfile?.gender.orEmpty() },
            workoutDays = workoutDays.ifBlank { currentUserProfile?.workoutDays.orEmpty() },
            fitnessLevel = fitnessLevel.ifBlank { currentUserProfile?.fitnessLevel.orEmpty() }
        )

        // Handle gender-specific program compatibility safely
        val normalizedNewGender = gender.trim().lowercase()
        val normalizedPrevGender = previousGender?.trim()?.lowercase().orEmpty()
        if (normalizedNewGender.isNotBlank() && normalizedNewGender != normalizedPrevGender) {
            if (normalizedNewGender == "female" && !scheduledProgramId.startsWith("female_")) {
                setActiveProgram("female_weight_loss")
            } else if (normalizedNewGender == "male" && scheduledProgramId.startsWith("female_")) {
                setActiveProgram("30-day")
            }
        }
    }

    fun clear() {
        val oldUid = currentUserId
        if (!oldUid.isNullOrBlank()) {
            try {
                com.example.fitness.clearPlatformNotificationToken(oldUid)
            } catch (_: Exception) {}
        }
        com.example.fitness.core.components.ProfileImageMemoryCache.clear()
        com.example.fitness.core.user.data.FirebaseUserRepository.clearProfileCache()
        currentUserId = null
        currentUserEmail = null
        currentUserName = null
        currentUserGender = null
        currentUserMobileNumber = null
        currentUserProfileImageUrl = null
        currentUserProfile = null
        currentUserRole = UserRole.USER
        unreadNotificationCount = 0
        pendingScreen = null
        workoutStreak = 0
        completedDays.clear()
        totalSetsCompleted = 0
        totalRepsCompleted = 0
        currentDayNumber = 1
        scheduledProgramId = "30-day"
        activeProgramId = "30-day"
        weeklyCommitment = "4 Days"
        experienceLevel = ExperienceLevel.NO_EXPERIENCE
        programCompletedWorkouts.clear()
        programCompletedWorkouts["30-day"] = mutableListOf()
        programCompletedWorkouts["hypertrophy"] = mutableListOf()
        programCompletedWorkouts["strength"] = mutableListOf()
        programCompletedWorkouts["athlete"] = mutableListOf()
        workoutReminderPreference = com.example.fitness.feature.notifications.model.WorkoutReminderPreference()
        currentAuthProvider = "Email"
        favoriteExerciseIds.clear()
        favoritesLoadedForUserId = null
        favoriteWorkoutIds.clear()
        workoutFavoritesLoadedForUserId = null
    }

    /**
     * Resets only workout progress, streak, and completed program days in memory.
     * Preserves active user profile and credentials.
     */
    fun resetProgramProgress() {
        completedDays.clear()
        currentDayNumber = 1
        workoutStreak = 0
        totalSetsCompleted = 0
        totalRepsCompleted = 0
        programCompletedWorkouts.clear()
        programCompletedWorkouts["30-day"] = mutableListOf()
        programCompletedWorkouts["hypertrophy"] = mutableListOf()
        programCompletedWorkouts["strength"] = mutableListOf()
        programCompletedWorkouts["athlete"] = mutableListOf()
    }

    var experienceLevel by mutableStateOf(ExperienceLevel.NO_EXPERIENCE)
    var weeklyCommitment by mutableStateOf("4 Days")
    var currentDayNumber by mutableIntStateOf(1) // 1 to 30
    var workoutStreak by mutableIntStateOf(0)
    val completedDays = mutableStateListOf<Int>()

    var totalSetsCompleted by mutableIntStateOf(0)
    var totalRepsCompleted by mutableIntStateOf(0)

    // Phase 4: Independent Active Program & Program Progress Tracking
    var scheduledProgramId by mutableStateOf("30-day")
    var activeProgramId by mutableStateOf("30-day") // "30-day", "hypertrophy", "strength", "athlete"
    private val programCompletedWorkouts = mutableStateMapOf<String, MutableList<Int>>()

    init {
        // Initialize independent storage for each program
        programCompletedWorkouts["30-day"] = mutableListOf()
        programCompletedWorkouts["hypertrophy"] = mutableListOf()
        programCompletedWorkouts["strength"] = mutableListOf()
        programCompletedWorkouts["athlete"] = mutableListOf()
    }

    fun normalizeProgramId(programId: String): String {
        val lower = programId.lowercase().trim()
        return when {
            lower.startsWith("female_") -> lower
            lower == "hypertrophy" -> "hypertrophy"
            lower == "strength" || lower == "hypertrophy_strength" -> "strength"
            lower == "athlete" || lower == "athlete_performance" || lower == "athlete_training" -> "athlete"
            else -> "30-day"
        }
    }

    fun setActiveProgram(programId: String) {
        val normalized = normalizeProgramId(programId)
        activeProgramId = normalized
        scheduledProgramId = normalized
    }

    fun setScheduledProgram(programId: String) {
        val normalized = normalizeProgramId(programId)
        scheduledProgramId = normalized
        activeProgramId = normalized
    }

    fun markDayCompleted(dayNumber: Int) {
        if (!completedDays.contains(dayNumber)) {
            completedDays.add(dayNumber)
            if (dayNumber >= currentDayNumber && currentDayNumber < 30) {
                currentDayNumber = dayNumber + 1
            }
        }
        markProgramDayCompleted("30-day", dayNumber)
    }

    fun markProgramDayCompleted(programId: String, dayNumber: Int) {
        val normalized = normalizeProgramId(programId)
        val list = programCompletedWorkouts.getOrPut(normalized) { mutableListOf() }
        if (!list.contains(dayNumber)) {
            list.add(dayNumber)
        }
        if (normalized == "30-day" && !completedDays.contains(dayNumber)) {
            completedDays.add(dayNumber)
            if (dayNumber >= currentDayNumber && currentDayNumber < 30) {
                currentDayNumber = dayNumber + 1
            }
        }
    }

    fun isDayCompleted(dayNumber: Int): Boolean {
        return completedDays.contains(dayNumber)
    }

    fun isProgramDayCompleted(programId: String, dayNumber: Int): Boolean {
        val normalized = normalizeProgramId(programId)
        return programCompletedWorkouts[normalized]?.contains(dayNumber) == true
    }

    fun getProgramCompletedCount(programId: String): Int {
        val normalized = normalizeProgramId(programId)
        return programCompletedWorkouts[normalized]?.size ?: 0
    }

    fun getProgramCurrentDay(programId: String): Int {
        val normalized = normalizeProgramId(programId)
        val completed = programCompletedWorkouts[normalized] ?: emptyList()
        if (completed.isEmpty()) return 1
        val firstMissing = (1..30).firstOrNull { !completed.contains(it) } ?: 30
        return if (firstMissing > 1) {
            firstMissing.coerceIn(1, 30)
        } else {
            // Day 1 was not completed, but user completed subsequent days (e.g. days 2, 4, 5, 6).
            // Advance active day based on completed count or highest completed day so progress is not stuck at 1.
            val maxCompleted = completed.maxOrNull() ?: 0
            maxOf(completed.size + 1, (maxCompleted + 1).coerceAtMost(30)).coerceIn(1, 30)
        }
    }

    fun getProgramProgressPercentage(programId: String, totalWorkouts: Int = 30): Float {
        val count = getProgramCompletedCount(programId)
        return (count.toFloat() / totalWorkouts.toFloat()).coerceIn(0f, 1f)
    }

    fun get30DayProgressPercentage(): Float {
        return (completedDays.size.toFloat() / 30f).coerceIn(0f, 1f)
    }
}
