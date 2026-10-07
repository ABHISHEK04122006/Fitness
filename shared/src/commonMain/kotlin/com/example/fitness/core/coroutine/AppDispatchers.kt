package com.example.fitness.core.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Centralized Coroutine Dispatchers provider for ATHGYM.
 * Facilitates clear separation between UI, IO, and CPU-intensive work,
 * and allows easy testing/mocking if needed.
 */
object AppDispatchers {
    val Main: CoroutineDispatcher get() = Dispatchers.Main
    val IO: CoroutineDispatcher get() = Dispatchers.IO
    val Default: CoroutineDispatcher get() = Dispatchers.Default
}
