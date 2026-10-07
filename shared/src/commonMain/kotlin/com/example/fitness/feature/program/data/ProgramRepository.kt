package com.example.fitness.feature.program.data

import com.example.fitness.feature.program.model.Program

/**
 * Repository interface for managing workout programs in Firestore under programs/{programId}.
 * Provides centralized program definitions used across athlete workflows and admin management.
 */
interface ProgramRepository {

    /**
     * Retrieves a single program by its stable document ID.
     * Returns null if not found or inaccessible.
     */
    suspend fun getProgram(programId: String): Program?

    /**
     * Retrieves all active programs (isActive == true).
     * Used by standard application flows and athlete program enrollment.
     */
    suspend fun getActivePrograms(): List<Program>

    /**
     * Retrieves all programs including archived/inactive ones.
     * Restricted to admin operations.
     */
    suspend fun getAllProgramsForAdmin(): List<Program>

    /**
     * Creates a new program document with a stable unique ID.
     * Enforces audit fields (createdAt, updatedAt, createdBy, updatedBy, version = 1, isActive = true).
     */
    suspend fun createProgram(program: Program): Result<Unit>

    /**
     * Updates an existing program document.
     * Preserves createdAt and createdBy, updates updatedAt and updatedBy, increments version.
     */
    suspend fun updateProgram(program: Program): Result<Unit>

    /**
     * Soft-deletes / archives a program by setting isActive = false.
     * Preserves existing athlete enrollment and history while hiding the program from active selection.
     */
    suspend fun archiveProgram(programId: String): Result<Unit>

    /**
     * Restores an archived program by setting isActive = true.
     * Preserves createdAt and createdBy, updates updatedAt and updatedBy, increments version.
     */
    suspend fun restoreProgram(programId: String): Result<Unit>
}
