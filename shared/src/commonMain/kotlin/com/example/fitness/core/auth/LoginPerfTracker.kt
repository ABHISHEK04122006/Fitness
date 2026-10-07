package com.example.fitness.core.auth

import com.example.fitness.platformLog
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Lightweight, high-precision performance stopwatch for authentication flows.
 * Provides granular stage timing across normal and affected devices (e.g. S24 vs standard Android).
 *
 * Strictly adheres to security requirements: NEVER logs passwords, tokens, API keys, or personal sensitive data.
 */
object LoginPerfTracker {

    private const val TAG = "LOGIN_PERF"
    private var startMark: TimeMark? = null
    private var lastMark: TimeMark? = null
    private val stageTimings = com.example.fitness.createThreadSafeMap<String, Long>()

    /**
     * Starts performance timing when the user taps an auth button.
     */
    fun start(provider: String = "Google") {
        stageTimings.clear()
        val now = TimeSource.Monotonic.markNow()
        startMark = now
        lastMark = now
        platformLog(TAG, "[PHASE 0] Button clicked (provider=$provider): 0ms")
    }

    /**
     * Records a milestone/step in the authentication pipeline with total elapsed latency
     * and delta latency since the previous phase.
     */
    fun record(step: String) {
        val start = startMark ?: return
        val now = TimeSource.Monotonic.markNow()
        val totalMs = start.elapsedNow().inWholeMilliseconds
        val deltaMs = (lastMark ?: start).elapsedNow().inWholeMilliseconds
        lastMark = now
        stageTimings[step] = deltaMs
        platformLog(TAG, "[$step] total=${totalMs}ms (+${deltaMs}ms)")
    }

    fun getStageTiming(step: String): Long? = stageTimings[step]

    fun getAllStageTimings(): Map<String, Long> = stageTimings.toMap()

    fun logSummary() {
        val start = startMark ?: return
        val totalMs = start.elapsedNow().inWholeMilliseconds
        val summary = buildString {
            appendLine("=== LOGIN PERFORMANCE BREAKDOWN (Total: ${totalMs}ms) ===")
            stageTimings.forEach { (stage, delta) ->
                appendLine(" - $stage: ${delta}ms")
            }
            appendLine("=========================================================")
        }
        platformLog(TAG, summary)
    }

    /**
     * Resets the tracker.
     */
    fun reset() {
        startMark = null
        lastMark = null
        stageTimings.clear()
    }

    /**
     * Returns the total elapsed milliseconds since start, or null if not running.
     */
    fun currentElapsedMillis(): Long? {
        return startMark?.elapsedNow()?.inWholeMilliseconds
    }
}
