package com.example.fitness.core.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.OAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Interface implemented on the iOS Swift side using AuthenticationServices.
 */
interface IosAppleAuthHandler {
    fun signIn(
        onSuccess: (idToken: String, rawNonce: String) -> Unit,
        onCancel: () -> Unit,
        onError: (message: String) -> Unit
    )
}

/**
 * Bridge singleton accessible from Swift via IosAppleAuthBridge.shared.
 */
object IosAppleAuthBridge {
    var handler: IosAppleAuthHandler? = null
}

actual fun isAppleSignInNative(): Boolean = true

actual suspend fun signInWithApple(): FirebaseUser? {
    val handler = IosAppleAuthBridge.handler
        ?: throw IllegalStateException("Apple Sign-In handler not registered on iOS.")

    println("[DEBUG] APPLE_AUTH_REQUEST_STARTED")

    return suspendCancellableCoroutine { continuation ->
        handler.signIn(
            onSuccess = { idToken, rawNonce ->
                println("[DEBUG] APPLE_AUTHORIZATION_CALLBACK")
                println("[DEBUG] APPLE_IDENTITY_TOKEN_RECEIVED")
                println("[DEBUG] APPLE_FIREBASE_AUTH_STARTED")

                val credential = OAuthProvider.credential(
                    providerId = "apple.com",
                    idToken = idToken,
                    rawNonce = rawNonce,
                    accessToken = null
                )

                @OptIn(DelicateCoroutinesApi::class)
                GlobalScope.launch(com.example.fitness.core.coroutine.AppDispatchers.IO) {
                    try {
                        val authResult = Firebase.auth.signInWithCredential(credential)
                        println("[DEBUG] APPLE_FIREBASE_AUTH_SUCCESS")
                        if (continuation.isActive) {
                            continuation.resume(authResult.user)
                        }
                    } catch (e: Exception) {
                        println("[DEBUG] APPLE_FIREBASE_AUTH_FAILED: ${e.message}")
                        if (continuation.isActive) {
                            continuation.resumeWithException(e)
                        }
                    }
                }
            },
            onCancel = {
                println("[DEBUG] APPLE_AUTHORIZATION_CANCELLED")
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            },
            onError = { message ->
                println("[DEBUG] APPLE_FIREBASE_AUTH_FAILED: $message")
                if (continuation.isActive) {
                    continuation.resumeWithException(IllegalStateException(message))
                }
            }
        )
    }
}
