package com.example.fitness.feature.programs.model

import org.jetbrains.compose.resources.DrawableResource

data class ProgramScheduleItem(
    val dayNumber: Int,
    val dayTitle: String,
    val focus: String,
    val workoutId: String,
    val duration: String,
    val difficulty: String,
    val exerciseCount: Int,
    val isRestDay: Boolean = false
)

data class WorkoutProgram(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val duration: String,
    val goal: String,
    val difficulty: String,
    val workoutsCount: Int,
    val focus: String,
    val category: String, // "Muscle Growth", "Strength", "Performance", "General"
    val imageRes: DrawableResource,
    val schedule: List<ProgramScheduleItem>
)
