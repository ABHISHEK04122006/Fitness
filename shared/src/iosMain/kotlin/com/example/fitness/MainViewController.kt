package com.example.fitness

import androidx.compose.ui.window.ComposeUIViewController
import com.example.fitness.core.firebase.initializeFirebase

fun MainViewController() = run {
    initializeFirebase()
    ComposeUIViewController { App() }
}