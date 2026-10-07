package com.example.fitness.core.firebase

import android.content.Context
import com.google.firebase.FirebaseApp

actual fun initializeFirebase(context: Any?) {
    val appContext = (context as? Context)?.applicationContext ?: return
    if (FirebaseApp.getApps(appContext).isEmpty()) {
        try {
            FirebaseApp.initializeApp(appContext)
        } catch (e: Exception) {
            println("Firebase initialization failed on Android: ${e.message}")
        }
    }
}
