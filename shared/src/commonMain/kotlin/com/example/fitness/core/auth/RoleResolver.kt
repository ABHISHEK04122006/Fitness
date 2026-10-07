package com.example.fitness.core.auth

import com.example.fitness.platformLog
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Authoritative role resolver for the FITNESS application.
 *
 * Rules:
 * 1. Default role for any authenticated user is strictly [UserRole.USER].
 * 2. [UserRole.ADMIN] is granted ONLY when the Firebase Authentication ID token
 *    contains the custom claim `admin: true`.
 * 3. Never grants ADMIN based on email address, username, or client-provided Firestore fields.
 * 4. Fails closed to [UserRole.USER] upon any network, token, or parsing error.
 */
object RoleResolver {

    private const val TAG = "RoleResolver"

    /**
     * Resolves the role strictly from an extracted Firebase ID token claims map.
     */
    fun resolveRoleFromClaims(claims: Map<String, Any?>?): UserRole {
        if (claims == null) return UserRole.USER

        val adminClaim = claims["admin"]
        val isAdmin = when (adminClaim) {
            true -> true
            "true" -> true
            1, 1L -> true
            else -> false
        }

        return if (isAdmin) UserRole.ADMIN else UserRole.USER
    }

    /**
     * Queries the authenticated [FirebaseUser]'s token result and evaluates custom claims.
     * Always returns [UserRole.USER] if user is null, network times out, or claim check fails.
     * Guarantees non-blocking execution via a 5s safety timeout.
     */
    suspend fun resolveRole(user: FirebaseUser?, forceRefresh: Boolean = false): UserRole {
        if (user == null) return UserRole.USER

        platformLog(TAG, "[ROLE_RESOLUTION_STARTED] Resolving role for uid=${user.uid}, forceRefresh=$forceRefresh")
        return try {
            val tokenResult = kotlinx.coroutines.withContext(com.example.fitness.core.coroutine.AppDispatchers.IO) {
                withTimeoutOrNull(5_000L) {
                    user.getIdTokenResult(forceRefresh)
                }
            }
            if (tokenResult != null) {
                val role = resolveRoleFromClaims(tokenResult.claims)
                platformLog(TAG, "[ROLE_RESOLUTION_SUCCESS] Resolved role: $role for uid=${user.uid}")
                role
            } else {
                platformLog(TAG, "[ROLE_RESOLUTION_TIMEOUT] getIdTokenResult timed out after 5s; failing closed to USER")
                UserRole.USER
            }
        } catch (e: Exception) {
            platformLog(TAG, "[ROLE_RESOLUTION_ERROR] Error getting token result: ${e.message}; failing closed to USER")
            UserRole.USER
        }
    }
}
