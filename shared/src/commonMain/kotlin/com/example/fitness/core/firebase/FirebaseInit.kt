package com.example.fitness.core.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.FirebaseStorage
import dev.gitlive.firebase.storage.storage
import dev.gitlive.firebase.messaging.FirebaseMessaging
import dev.gitlive.firebase.messaging.messaging

/**
 * Initializes Firebase platform-specifically.
 * - Android: context is the Android Context (Application or Activity context).
 * - iOS: context is unused (Firebase uses GoogleService-Info.plist via native FIRApp).
 */
expect fun initializeFirebase(context: Any? = null)

/**
 * Accessor for Firebase services across commonMain.
 * Features in commonMain can access Auth, Firestore, Storage, and Messaging
 * via FitnessFirebase without needing platform-specific implementations.
 */
object FitnessFirebase {
    val auth: FirebaseAuth
        get() = Firebase.auth

    val firestore: FirebaseFirestore
        get() = Firebase.firestore

    val storage: FirebaseStorage
        get() = Firebase.storage

    val messaging: FirebaseMessaging
        get() = Firebase.messaging
}
