package com.example.fitness.core.auth

import android.app.Activity
import android.content.Intent
import java.lang.ref.WeakReference

/**
 * Retains a weak reference to the active Android Activity for Credential Manager presentation
 * and an asynchronous launcher callback for legacy Google Sign-In fallback.
 */
object ActivityProvider {
    private var currentActivityRef: WeakReference<Activity>? = null

    var currentActivity: Activity?
        get() = currentActivityRef?.get()
        set(value) {
            currentActivityRef = value?.let { WeakReference(it) }
        }

    var googleSignInLauncher: (suspend (Intent) -> Intent?)? = null
}
