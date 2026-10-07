package com.example.fitness.core.navigation

import androidx.compose.runtime.Composable

/**
 * Multiplatform BackHandler for intercepting system back press / gestures.
 * On Android, this delegates to [androidx.activity.compose.BackHandler].
 * On iOS, this is a no-op as back navigation is handled by in-screen controls.
 */
@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)
