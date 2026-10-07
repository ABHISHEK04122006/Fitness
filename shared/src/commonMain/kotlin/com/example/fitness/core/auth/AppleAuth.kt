package com.example.fitness.core.auth

import dev.gitlive.firebase.auth.FirebaseUser

/**
 * Initiates the platform-specific Apple Sign-In flow and authenticates the user
 * with GitLive Firebase Auth.
 *
 * @return The authenticated [FirebaseUser] if sign-in succeeded, or null if cancelled.
 * @throws Exception if sign-in failed due to an error.
 */
expect suspend fun signInWithApple(): FirebaseUser?

/**
 * Returns true if native Apple Sign-In (AuthenticationServices) is supported on this platform.
 */
expect fun isAppleSignInNative(): Boolean
