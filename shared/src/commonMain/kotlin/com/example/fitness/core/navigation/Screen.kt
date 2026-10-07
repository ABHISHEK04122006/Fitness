package com.example.fitness.core.navigation

import com.example.fitness.feature.workout.model.ExperienceLevel

sealed class Screen {
    // Onboarding
    data object Welcome : Screen()
    data class Auth(val isSignUp: Boolean = false) : Screen()
    data object GenderSelection : Screen()
    data object GoalSelection : Screen()

    // Main Tabs
    data object Home : Screen()
    data object Explore : Screen()
    data object Workout : Screen()
    data object Progress : Screen()
    data object Profile : Screen()

    // Notification Flow
    data object Notifications : Screen()

    // Program Flow
    data object Programs : Screen()
    data object FemalePlanSelection : Screen()
    data class ProgramDetail(val programId: String, val level: ExperienceLevel? = null) : Screen()

    // Workout Flow
    data class WorkoutOverview(val workoutId: String, val programId: String = "") : Screen()
    data class ActiveExercise(val workoutId: String) : Screen()
    data class ActiveWorkoutSession(val workoutId: String, val programId: String = "") : Screen()
    data class ExerciseDetail(val exerciseId: String, val sets: String = "", val reps: String = "", val rest: String = "") : Screen()
    data class WorkoutSummary(val workoutId: String, val durationSeconds: Int) : Screen()

    // Body Part Selection & Video Flow
    data class BodyPartSelection(val workoutType: String) : Screen()
    data class BodyPartWorkoutDetail(val workoutType: String, val bodyPartId: String, val initialExerciseId: String? = null) : Screen()

    // Admin Portal
    data object AdminDashboard : Screen()
    data object AdminUsers : Screen()
    data object AdminNotifications : Screen()
    data object AdminWorkouts : Screen()
    data object AdminExercises : Screen()
    data object AdminPrograms : Screen()
    data object AdminContent : Screen()
    data object AdminCatalogSync : Screen()
    data object AdminAnalytics : Screen()
    data object AdminSettings : Screen()
}
