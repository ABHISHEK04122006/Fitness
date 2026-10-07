package com.example.fitness.core.auth

import dev.gitlive.firebase.auth.FirebaseUser

/**
 * Initiates the platform-specific Google Sign-In flow and authenticates the user
 * with GitLive Firebase Auth.
 *
 * @return The authenticated [FirebaseUser] if sign-in succeeded, or null if cancelled.
 * @throws Exception if sign-in failed due to an error.
 */
expect suspend fun signInWithGoogle(): FirebaseUser?

/**
 * Clears platform-specific Google Sign-In and Credential Manager session state.
 * Ensures subsequent sign-in attempts always prompt the account selection UI.
 */
expect suspend fun clearPlatformAuthState()
