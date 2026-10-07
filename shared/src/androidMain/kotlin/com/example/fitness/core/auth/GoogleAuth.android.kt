package com.example.fitness.core.auth

import android.app.Activity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.fitness.platformLog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.withTimeout

private const val TAG = "GoogleAuth"
private const val SERVER_CLIENT_ID = "378800500795-kgsv16r0r1eekn1c84nd0qa6g05r8kr2.apps.googleusercontent.com"

private val googleSignInOptions by lazy {
    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(SERVER_CLIENT_ID)
        .requestEmail()
        .build()
}

@Volatile
private var cachedCredentialManager: CredentialManager? = null

@Volatile
private var cachedGoogleSignInClient: GoogleSignInClient? = null

private object GoogleAuthLock

private fun getCredentialManager(activity: Activity): CredentialManager {
    return cachedCredentialManager ?: synchronized(GoogleAuthLock) {
        cachedCredentialManager ?: CredentialManager.create(activity.applicationContext).also {
            cachedCredentialManager = it
        }
    }
}

private fun getGoogleSignInClient(activity: Activity): GoogleSignInClient {
    return cachedGoogleSignInClient ?: synchronized(GoogleAuthLock) {
        cachedGoogleSignInClient ?: GoogleSignIn.getClient(activity.applicationContext, googleSignInOptions).also {
            cachedGoogleSignInClient = it
        }
    }
}

internal data class GoogleTokenResult(
    val idToken: String,
    val email: String
)

actual suspend fun signInWithGoogle(): FirebaseUser? {
    val activity = ActivityProvider.currentActivity
        ?: throw IllegalStateException("Google Sign-In failed: Active Activity not found.")

    // Step 1: Obtain Google ID token from Credential Manager (or GMS fallback if Credential Manager fails/unsupported)
    val tokenResult = performGoogleSignInFlow(
        credentialManagerCall = {
            obtainGoogleTokenFromCredentialManager(activity)
        },
        fallbackCall = {
            obtainGoogleTokenFromFallback(activity)
        }
    ) ?: return null // User intentionally cancelled account selection

    // Step 2: Authenticate once with Firebase using the selected account's ID token.
    // If Firebase Auth fails (e.g. network timeout), throw the error directly to AuthScreen.
    // NEVER trigger fallback or reopen account selection after user has already chosen their account.
    val firebaseCredential = GoogleAuthProvider.credential(
        idToken = tokenResult.idToken,
        accessToken = null
    )
    platformLog(TAG, "[LOGIN] Firebase Google credential creation")

    platformLog(TAG, "[LOGIN] Firebase sign-in START")
    LoginPerfTracker.record("Firebase auth started")
    val authResult = kotlinx.coroutines.withContext(com.example.fitness.core.coroutine.AppDispatchers.IO) {
        withTimeout(15_000L) {
            Firebase.auth.signInWithCredential(firebaseCredential)
        }
    }
    val firebaseUser = authResult.user
    val firebaseEmail = firebaseUser?.email ?: ""
    val maskedFirebaseEmail = if (firebaseEmail.contains("@")) {
        val name = firebaseEmail.substringBefore("@")
        val domain = firebaseEmail.substringAfter("@")
        val maskedName = if (name.length > 2) "${name.take(2)}***" else "${name.take(1)}***"
        "$maskedName@$domain"
    } else "authenticated_user"
    platformLog(TAG, "[LOGIN] Firebase sign-in SUCCESS (uid=${firebaseUser?.uid}, email=$maskedFirebaseEmail)")
    LoginPerfTracker.record("Firebase auth completed")
    return firebaseUser
}

/**
 * Primary Google Sign-In mechanism using Android Credential Manager.
 * Does NOT impose an arbitrary premature timeout so users can view and choose accounts at their own pace.
 * Catches user cancellation directly to prevent false errors or unexpected fallback prompts.
 */
private suspend fun obtainGoogleTokenFromCredentialManager(activity: Activity): GoogleTokenResult? {
    platformLog(TAG, "[LOGIN] Credential Manager START")
    LoginPerfTracker.record("Credential request started")
    val credentialManager = getCredentialManager(activity)

    // Explicitly disable auto-select and do not filter by authorized accounts.
    // This guarantees that the Google Account chooser bottom sheet is ALWAYS presented
    // allowing users with multiple Google accounts (e.g. Admin vs User) to choose which account to use.
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(SERVER_CLIENT_ID)
        .setAutoSelectEnabled(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    val result = try {
        credentialManager.getCredential(
            context = activity,
            request = request
        )
    } catch (e: GetCredentialCancellationException) {
        platformLog(TAG, "[LOGIN] User cancelled Google account selection in Credential Manager")
        LoginPerfTracker.record("CredentialManager cancelled by user")
        return null
    } catch (e: Exception) {
        if (isUserCancellation(e)) {
            platformLog(TAG, "[LOGIN] User cancelled Google account selection: ${e.message}")
            LoginPerfTracker.record("CredentialManager cancelled by user")
            return null
        }
        platformLog(TAG, "[LOGIN] Credential Manager technical error (${e::class.simpleName}): ${e.message}")
        throw e
    }

    val credential = result.credential
    platformLog(TAG, "[LOGIN] Credential returned (type=${credential::class.java.simpleName})")
    LoginPerfTracker.record("Credential returned")

    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val idToken = googleIdTokenCredential.idToken
        val selectedEmail = googleIdTokenCredential.id
        val maskedEmail = if (selectedEmail.contains("@")) {
            val name = selectedEmail.substringBefore("@")
            val domain = selectedEmail.substringAfter("@")
            val maskedName = if (name.length > 2) "${name.take(2)}***" else "${name.take(1)}***"
            "$maskedName@$domain"
        } else "selected_account"
        platformLog(TAG, "[LOGIN] User selected Google account: $maskedEmail")

        return GoogleTokenResult(idToken = idToken, email = selectedEmail)
    } else {
        platformLog(TAG, "[LOGIN] Unexpected credential type: ${credential::class.java.simpleName}")
        throw IllegalStateException("Unexpected credential type received: ${credential::class.java.simpleName}")
    }
}

/**
 * Resilient fallback using Google Play Services GoogleSignInClient.
 * Obtains a Google ID token if Credential Manager is unsupported or fails with a provider error.
 * Clears any cached GoogleSignIn account beforehand to guarantee account selection UI is displayed.
 */
private suspend fun obtainGoogleTokenFromFallback(activity: Activity): GoogleTokenResult? {
    platformLog(TAG, "[LOGIN] Google Sign-In Fallback START")
    LoginPerfTracker.record("Google Sign-In Fallback START")

    val googleSignInClient = getGoogleSignInClient(activity)

    // Clear any cached account so Google Play Services always prompts the account selection sheet
    try {
        if (GoogleSignIn.getLastSignedInAccount(activity) != null) {
            googleSignInClient.signOut()
        }
    } catch (_: Exception) {}

    val signInIntent = googleSignInClient.signInIntent
    val launcher = ActivityProvider.googleSignInLauncher
        ?: throw IllegalStateException("Google Sign-In fallback launcher not initialized in Activity.")

    LoginPerfTracker.record("Fallback picker launched")
    val resultIntent = launcher(signInIntent)

    if (resultIntent == null) {
        platformLog(TAG, "[LOGIN] User cancelled Google account selection (Fallback null intent)")
        LoginPerfTracker.record("Fallback cancelled by user")
        return null
    }

    val task = GoogleSignIn.getSignedInAccountFromIntent(resultIntent)
    val account = try {
        task.getResult(ApiException::class.java)
    } catch (e: ApiException) {
        if (e.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED ||
            e.statusCode == CommonStatusCodes.CANCELED ||
            e.statusCode == 12501 || e.statusCode == 16) {
            platformLog(TAG, "[LOGIN] User cancelled Google account selection (Fallback statusCode=${e.statusCode})")
            LoginPerfTracker.record("Fallback cancelled by user (statusCode=${e.statusCode})")
            return null
        }
        platformLog(TAG, "[LOGIN] Fallback Google sign-in failed (statusCode=${e.statusCode}): ${e.message}")
        LoginPerfTracker.record("Fallback failed (statusCode=${e.statusCode})")
        throw IllegalStateException("Google sign-in failed: ${e.message}", e)
    }

    val idToken = account?.idToken
        ?: throw IllegalStateException("Google Sign-In fallback succeeded but ID token is null.")

    val selectedEmail = account?.email ?: ""
    val maskedEmail = if (selectedEmail.contains("@")) {
        val name = selectedEmail.substringBefore("@")
        val domain = selectedEmail.substringAfter("@")
        val maskedName = if (name.length > 2) "${name.take(2)}***" else "${name.take(1)}***"
        "$maskedName@$domain"
    } else "selected_fallback_account"
    platformLog(TAG, "[LOGIN] User selected Google account (Fallback): $maskedEmail")
    LoginPerfTracker.record("Google ID token received (Fallback)")

    return GoogleTokenResult(idToken = idToken, email = selectedEmail)
}

/**
 * Clears platform-specific Google Sign-In and Credential Manager session state on Android.
 * Called upon logout so subsequent sign-in attempts always prompt for account selection.
 */
actual suspend fun clearPlatformAuthState() {
    val activity = ActivityProvider.currentActivity ?: return
    try {
        val credentialManager = getCredentialManager(activity)
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
        platformLog(TAG, "[LOGOUT] Credential Manager credential state cleared")
    } catch (e: Exception) {
        platformLog(TAG, "[LOGOUT] Failed to clear CredentialManager state: ${e.message}")
    }
    try {
        val googleSignInClient = getGoogleSignInClient(activity)
        googleSignInClient.signOut()
        platformLog(TAG, "[LOGOUT] GoogleSignInClient state cleared")
    } catch (e: Exception) {
        platformLog(TAG, "[LOGOUT] Failed to clear GoogleSignInClient state: ${e.message}")
    }
}
