package com.example.fitness.core.perf

import com.example.fitness.platformLog
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Lightweight, high-precision performance tracker for app startup, screen navigation,
 * and media readiness flows.
 *
 * Tracks timings across key flows:
 * 1. App launch -> First usable screen
 * 2. Splash -> Home
 * 3. Home -> Today's Workout
 * 4. Today's Workout -> Workout
 * 5. Workout -> Exercise Details
 * 6. Exercise Details -> Video/Media Ready
 */
object AppPerfTracker {

    private const val TAG = "APP_PERF"
    private val marks = mutableMapOf<String, TimeMark>()

    fun start(flow: String) {
        val mark = TimeSource.Monotonic.markNow()
        marks[flow] = mark
        platformLog(TAG, "[$flow] START")
    }

    fun record(flow: String, milestone: String): Long {
        val mark = marks[flow] ?: return -1L
        val elapsedMs = mark.elapsedNow().inWholeMilliseconds
        platformLog(TAG, "[$flow] $milestone: ${elapsedMs}ms")
        return elapsedMs
    }

    fun finish(flow: String, milestone: String = "COMPLETE"): Long {
        val mark = marks.remove(flow) ?: return -1L
        val elapsedMs = mark.elapsedNow().inWholeMilliseconds
        platformLog(TAG, "[$flow] $milestone: ${elapsedMs}ms")
        return elapsedMs
    }

    fun getElapsed(flow: String): Long? {
        return marks[flow]?.elapsedNow()?.inWholeMilliseconds
    }

    fun reset() {
        marks.clear()
    }
}
