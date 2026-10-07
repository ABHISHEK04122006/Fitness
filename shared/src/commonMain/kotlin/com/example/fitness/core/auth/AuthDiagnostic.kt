package com.example.fitness.core.auth

import com.example.fitness.core.firebase.FitnessFirebase
import com.example.fitness.platformLog
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

/**
 * Diagnostic utility for investigating device-specific authentication issues (e.g. Samsung Galaxy S24).
 *
 * Implements Section 10 of S24 Diagnostic Specification:
 * - Performs ONLY Firebase Authentication without:
 *   1. Firestore profile loading
 *   2. Session enrichment
 *   3. Navigation
 *   4. App initialization
 * - Safe 20-second timeout
 * - Logs strictly non-sensitive diagnostics
 */
object AuthDiagnostic {

    private const val TAG = "AuthDiagnostic"

    /**
     * Executes ONLY the Firebase Authentication phase and reports whether Firebase Auth
     * itself completes, fails with an exception, or times out.
     */
    suspend fun runFirebaseAuthOnlyTest(
        authAction: suspend () -> FirebaseUser?
    ): DiagnosticAuthResult {
        platformLog(TAG, "[LOGIN] [DIAGNOSTIC] Firebase Auth ONLY test STARTED")
        return try {
            val user = withTimeout(20_000L) {
                authAction()
            }
            if (user != null) {
                val currentStatus = if (FitnessFirebase.auth.currentUser != null) "available" else "null"
                platformLog(TAG, "[LOGIN] [DIAGNOSTIC] Firebase sign-in SUCCESS (uid=${user.uid})")
                platformLog(TAG, "[LOGIN] [DIAGNOSTIC] Firebase currentUser = $currentStatus")
                DiagnosticAuthResult.Success(uid = user.uid, currentUserStatus = currentStatus)
            } else {
                platformLog(TAG, "[LOGIN] [DIAGNOSTIC] Firebase sign-in CANCELLED (user returned null)")
                DiagnosticAuthResult.Cancelled
            }
        } catch (e: TimeoutCancellationException) {
            platformLog(TAG, "[LOGIN] [DIAGNOSTIC] Firebase sign-in TIMEOUT after 20s")
            DiagnosticAuthResult.Timeout(durationMillis = 20_000L)
        } catch (e: Exception) {
            val errClass = e::class.simpleName ?: "Exception"
            val errMsg = e.message ?: "Unknown error"
            platformLog(TAG, "[LOGIN] [DIAGNOSTIC] Firebase sign-in FAILURE: $errClass - $errMsg")
            DiagnosticAuthResult.Failure(exceptionClass = errClass, message = errMsg)
        }
    }
}

sealed class DiagnosticAuthResult {
    data class Success(val uid: String, val currentUserStatus: String) : DiagnosticAuthResult()
    data class Failure(val exceptionClass: String, val message: String) : DiagnosticAuthResult()
    data class Timeout(val durationMillis: Long) : DiagnosticAuthResult()
    data object Cancelled : DiagnosticAuthResult()
}
