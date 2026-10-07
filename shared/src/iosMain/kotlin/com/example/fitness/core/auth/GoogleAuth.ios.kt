package com.example.fitness.core.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Interface implemented on the iOS Swift side using GoogleSignIn-iOS.
 */
interface IosGoogleAuthHandler {
    fun signIn(
        onSuccess: (idToken: String, accessToken: String?) -> Unit,
        onCancel: () -> Unit,
        onError: (message: String) -> Unit
    )
}

/**
 * Bridge singleton accessible from Swift via IosGoogleAuthBridge.shared.
 */
object IosGoogleAuthBridge {
    var handler: IosGoogleAuthHandler? = null
}

actual suspend fun signInWithGoogle(): FirebaseUser? {
    val handler = IosGoogleAuthBridge.handler
        ?: throw IllegalStateException("Google Sign-In handler not registered on iOS.")

    return suspendCancellableCoroutine { continuation ->
        handler.signIn(
            onSuccess = { idToken, accessToken ->
                val credential = GoogleAuthProvider.credential(
                    idToken = idToken,
                    accessToken = accessToken
                )
                @OptIn(DelicateCoroutinesApi::class)
                GlobalScope.launch(com.example.fitness.core.coroutine.AppDispatchers.IO) {
                    try {
                        val authResult = Firebase.auth.signInWithCredential(credential)
                        if (continuation.isActive) {
                            continuation.resume(authResult.user)
                        }
                    } catch (e: Exception) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(e)
                        }
                    }
                }
            },
            onCancel = {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            },
            onError = { message ->
                if (continuation.isActive) {
                    continuation.resumeWithException(IllegalStateException(message))
                }
            }
        )
    }
}

actual suspend fun clearPlatformAuthState() {
    // Platform credential state on iOS is managed by GIDSignIn / Keychain
}
