package com.example.fitness.core.firebase

import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual fun initializeFirebase(context: Any?) {
    if (cocoapods.FirebaseCore.FIRApp.defaultApp() == null) {
        cocoapods.FirebaseCore.FIRApp.configure()
    }
}
