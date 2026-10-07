package com.example.fitness.core.navigation

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on iOS platform; screen navigation controls handle pop()
}
