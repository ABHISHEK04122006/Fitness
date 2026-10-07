package com.example.fitness.core.auth

import dev.gitlive.firebase.auth.FirebaseUser

actual fun isAppleSignInNative(): Boolean = false

actual suspend fun signInWithApple(): FirebaseUser? {
    // Apple Sign-In is native to iOS/iPhone. Android displays an informational dialog on AuthScreen.
    return null
}
