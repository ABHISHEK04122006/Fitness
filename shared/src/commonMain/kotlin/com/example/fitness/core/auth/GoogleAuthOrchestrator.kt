package com.example.fitness.core.auth

import com.example.fitness.platformLog
import kotlinx.coroutines.TimeoutCancellationException

private const val TAG = "GoogleAuthOrchestrator"

/**
 * Checks whether an exception represents an intentional user cancellation
 * versus a technical failure or timeout.
 * Timeouts (such as [TimeoutCancellationException]) are explicitly NOT cancellations.
 */
fun isUserCancellation(throwable: Throwable): Boolean {
    var current: Throwable? = throwable
    while (current != null) {
        if (current is TimeoutCancellationException) {
            return false
        }
        val className = current::class.simpleName ?: ""
        val msg = current.message?.lowercase() ?: ""

        if (className.contains("Timeout", ignoreCase = true)) {
            return false
        }

        if (className.contains("Cancel", ignoreCase = true) ||
            className.contains("Dismiss", ignoreCase = true) ||
            className.contains("Abort", ignoreCase = true) ||
            msg.contains("user cancelled") ||
            msg.contains("user_cancel") ||
            msg.contains("cancelled by user") ||
            msg.contains("cancelled account selection") ||
            msg.contains("activity cancelled") ||
            msg.contains("request was cancelled") ||
            (msg.contains("cancel") && !msg.contains("timeout")) ||
            msg.contains("dismiss") ||
            msg.contains("abort")) {
            return true
        }
        current = current.cause
    }
    return false
}

/**
 * Orchestrates Google Sign-In between primary Credential Manager and legacy Google Sign-In fallback.
 *
 * Rules:
 * 1. Credential Manager is always attempted first.
 * 2. If it succeeds, the result is returned and fallback is NOT called.
 * 3. If user intentionally cancels Credential Manager (returns null or throws cancellation),
 *    the flow halts immediately; fallback is NOT called.
 * 4. If Credential Manager throws an unexpected technical error / unsupported provider exception,
 *    the fallback is invoked.
 * 5. Account selection UI is never shown twice sequentially.
 */
suspend fun <T> performGoogleSignInFlow(
    credentialManagerCall: suspend () -> T?,
    fallbackCall: suspend () -> T?
): T? {
    val result = try {
        credentialManagerCall()
    } catch (e: Exception) {
        if (isUserCancellation(e)) {
            platformLog(TAG, "[LOGIN] User cancelled Google account selection; fallback NOT invoked")
            LoginPerfTracker.record("CredentialManager cancelled by user")
            return null
        }
        platformLog(
            TAG,
            "[LOGIN] Credential Manager failed/unsupported (${e::class.simpleName}: ${e.message}); attempting Google Sign-In fallback"
        )
        LoginPerfTracker.record("CredentialManager failed (${e::class.simpleName}) -> Fallback triggered")
        return fallbackCall()
    }

    if (result != null) {
        return result
    }

    // Null returned without exception indicates user cancelled dialog
    platformLog(TAG, "[LOGIN] Credential Manager returned null (user cancelled); fallback NOT invoked")
    LoginPerfTracker.record("CredentialManager returned null (user cancelled)")
    return null
}
