package com.example.fitness.feature.admin.model

import kotlinx.serialization.Serializable

/**
 * Pre-sync analysis preview showing entities to be created vs updated vs existing and duplicate warnings.
 */
@Serializable
data class CatalogSyncPreview(
    val exerciseNewCount: Int = 0,
    val exerciseExistingCount: Int = 0,
    val exerciseUpdateCount: Int = 0,
    val workoutNewCount: Int = 0,
    val workoutExistingCount: Int = 0,
    val workoutUpdateCount: Int = 0,
    val programNewCount: Int = 0,
    val programExistingCount: Int = 0,
    val programUpdateCount: Int = 0,
    val warnings: List<String> = emptyList()
) {
    val totalNew: Int get() = exerciseNewCount + workoutNewCount + programNewCount
    val totalExisting: Int get() = exerciseExistingCount + workoutExistingCount + programExistingCount
    val totalUpdates: Int get() = exerciseUpdateCount + workoutUpdateCount + programUpdateCount
    val totalEntities: Int get() = totalNew + totalExisting
}

/**
 * Breakdown of operations for a specific catalog entity type.
 */
@Serializable
data class EntitySyncSummary(
    val created: Int = 0,
    val updated: Int = 0,
    val skipped: Int = 0,
    val failed: Int = 0
)

/**
 * Real execution result of a catalog synchronization operation.
 */
@Serializable
data class CatalogSyncResult(
    val success: Boolean,
    val exerciseSummary: EntitySyncSummary = EntitySyncSummary(),
    val workoutSummary: EntitySyncSummary = EntitySyncSummary(),
    val programSummary: EntitySyncSummary = EntitySyncSummary(),
    val totalCreated: Int = 0,
    val totalUpdated: Int = 0,
    val totalSkipped: Int = 0,
    val totalFailed: Int = 0,
    val warnings: List<String> = emptyList(),
    val errors: List<String> = emptyList()
)

/**
 * Persistent audit trail document stored in Firestore root collection "admin_sync_history/{syncId}".
 */
@Serializable
data class AdminSyncHistoryRecord(
    val syncId: String,
    val adminUid: String,
    val startedAt: Long,
    val completedAt: Long,
    val status: String, // "SUCCESS", "PARTIAL_FAILURE", "FAILED"
    val exerciseSummary: EntitySyncSummary = EntitySyncSummary(),
    val workoutSummary: EntitySyncSummary = EntitySyncSummary(),
    val programSummary: EntitySyncSummary = EntitySyncSummary(),
    val totalCreated: Int = 0,
    val totalUpdated: Int = 0,
    val totalSkipped: Int = 0,
    val totalFailed: Int = 0,
    val warnings: List<String> = emptyList(),
    val errorSummary: String? = null
)
