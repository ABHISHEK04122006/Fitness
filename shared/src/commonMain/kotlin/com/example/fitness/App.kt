package com.example.fitness

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.fitness.core.auth.AuthSessionManager
import com.example.fitness.core.components.AthgymSplashScreen
import com.example.fitness.core.components.BottomNavigationBar
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.firebase.FitnessFirebase
import com.example.fitness.core.navigation.BackHandler
import com.example.fitness.core.navigation.LocalNavigator
import com.example.fitness.core.navigation.Navigator
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.navigation.rememberNavigator
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.auth.AuthScreen
import com.example.fitness.feature.gender.GenderSelectionScreen
import com.example.fitness.feature.goals.GoalSelectionScreen
import com.example.fitness.feature.home.HomeScreen
import com.example.fitness.feature.notifications.NotificationScreen
import com.example.fitness.feature.profile.ProfileScreen
import com.example.fitness.feature.programs.FemalePlanSelectionScreen
import com.example.fitness.feature.programs.ProgramDetailScreen
import com.example.fitness.feature.programs.ProgramsScreen
import com.example.fitness.feature.progress.ProgressScreen
import com.example.fitness.feature.welcome.WelcomeScreen
import com.example.fitness.feature.workout.ActiveWorkoutSessionScreen
import com.example.fitness.feature.workout.BodyPartSelectionScreen
import com.example.fitness.feature.workout.BodyPartWorkoutDetailScreen
import com.example.fitness.feature.workout.WorkoutOverviewScreen

@Composable
fun App() {
    var initialScreen by remember { mutableStateOf<Screen?>(null) }

    LaunchedEffect(Unit) {
        com.example.fitness.core.perf.AppPerfTracker.start("APP_STARTUP")
        com.example.fitness.core.perf.AppPerfTracker.start("SPLASH_TO_HOME")
        try {
            val user = AuthSessionManager.resolveInitialUser()
            if (user != null) {
                val destination = kotlinx.coroutines.withTimeoutOrNull(10_000L) {
                    AuthSessionManager.restoreUserSession(user)
                } ?: Screen.Home
                initialScreen = destination
            } else {
                initialScreen = Screen.Auth(isSignUp = false)
            }
        } catch (_: Exception) {
            initialScreen = Screen.Auth(isSignUp = false)
        }
    }

    FitTheme {
        if (initialScreen == null) {
            AthgymSplashScreen()
        } else {
            AppNavigationContent(initialScreen = initialScreen!!)
        }
    }
}

@Composable
private fun AppNavigationContent(initialScreen: Screen) {
    val navigator = rememberNavigator(initialScreen = initialScreen)

    // Intercept hardware/system back button on Android
    BackHandler(enabled = navigator.canPop) {
        navigator.pop()
    }

    val pendingScreen = UserSession.pendingScreen
    LaunchedEffect(pendingScreen) {
        if (pendingScreen != null) {
            UserSession.pendingScreen = null
            navigator.navigate(pendingScreen)
        }
    }

    // Observe auth state changes for remote revocation / external sign-out
    LaunchedEffect(Unit) {
        try {
            FitnessFirebase.auth.authStateChanged.collect { user ->
                if (user == null && UserSession.currentUserId != null) {
                    try {
                        com.example.fitness.core.auth.clearPlatformAuthState()
                    } catch (_: Exception) {}
                    UserSession.clear()
                    navigator.resetRoot(Screen.Auth(isSignUp = false))
                }
            }
        } catch (_: Exception) {}
    }

    // Pre-warm active exercises from Firestore into canonical cache
    LaunchedEffect(Unit) {
        try {
            com.example.fitness.feature.exercise.data.FirebaseExerciseRepository().getActiveExercises()
        } catch (_: Exception) {}
    }

    CompositionLocalProvider(LocalNavigator provides navigator) {
        val isTabScreen = when (navigator.currentScreen) {
            is Screen.Home, is Screen.Explore, is Screen.Progress, is Screen.Profile,
            is Screen.Workout, is Screen.Programs, is Screen.FemalePlanSelection -> true
            else -> false
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF000000)),
            containerColor = Color(0xFF000000),
                bottomBar = {
                    if (isTabScreen) {
                        BottomNavigationBar(
                            selectedTab = navigator.currentTab,
                            onTabSelected = { tab ->
                                navigator.switchTab(tab)
                            }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (isTabScreen) innerPadding.calculateBottomPadding() else 0.dp)
                ) {
                    AnimatedContent(
                        targetState = navigator.currentScreen,
                        transitionSpec = {
                            val isTabState = { s: Screen ->
                                s is Screen.Home || s is Screen.Explore || s is Screen.Progress || s is Screen.Profile ||
                                        s is Screen.Workout || s is Screen.Programs || s is Screen.FemalePlanSelection
                            }
                            val isTabSwitch = isTabState(initialState) && isTabState(targetState)

                            if (isTabSwitch) {
                                (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.98f, animationSpec = tween(220))) togetherWith
                                        (fadeOut(animationSpec = tween(160)) + scaleOut(targetScale = 1.01f, animationSpec = tween(160)))
                            } else if (navigator.isNavigatingBack) {
                                (slideInHorizontally(
                                    animationSpec = tween(340, easing = FastOutSlowInEasing),
                                    initialOffsetX = { width -> -(width * 0.25f).toInt() }
                                ) + fadeIn(animationSpec = tween(300))) togetherWith
                                        (slideOutHorizontally(
                                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                                            targetOffsetX = { width -> (width * 0.25f).toInt() }
                                        ) + fadeOut(animationSpec = tween(220)))
                            } else {
                                (slideInHorizontally(
                                    animationSpec = tween(340, easing = FastOutSlowInEasing),
                                    initialOffsetX = { width -> (width * 0.25f).toInt() }
                                ) + fadeIn(animationSpec = tween(300))) togetherWith
                                        (slideOutHorizontally(
                                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                                            targetOffsetX = { width -> -(width * 0.20f).toInt() }
                                        ) + fadeOut(animationSpec = tween(220)))
                            }
                        }
                    ) { screen ->
                        when (screen) {
                            is Screen.Welcome -> WelcomeScreen()
                            is Screen.Auth -> AuthScreen(initialIsSignUp = screen.isSignUp)
                            is Screen.GenderSelection -> GenderSelectionScreen()
                            is Screen.GoalSelection -> GoalSelectionScreen()
                            is Screen.Home -> HomeScreen()
                            is Screen.Explore -> if (UserSession.isFemale) FemalePlanSelectionScreen() else ProgramsScreen()
                            is Screen.Programs -> if (UserSession.isFemale) FemalePlanSelectionScreen() else ProgramsScreen()
                            is Screen.FemalePlanSelection -> FemalePlanSelectionScreen()
                            is Screen.ProgramDetail -> ProgramDetailScreen(programId = screen.programId, experienceLevel = screen.level)
                            is Screen.WorkoutOverview -> WorkoutOverviewScreen(workoutId = screen.workoutId, programId = screen.programId)
                            is Screen.Progress -> ProgressScreen()
                            is Screen.Profile -> ProfileScreen()
                            is Screen.Notifications -> NotificationScreen()
                            is Screen.Workout -> if (UserSession.isFemale) FemalePlanSelectionScreen() else ProgramsScreen()
                            is Screen.ActiveExercise -> ActiveWorkoutSessionScreen(workoutId = screen.workoutId)
                            is Screen.ActiveWorkoutSession -> ActiveWorkoutSessionScreen(workoutId = screen.workoutId, programId = screen.programId)
                            is Screen.ExerciseDetail -> com.example.fitness.feature.exercise.ui.ExerciseDetailScreen(
                                exerciseId = screen.exerciseId,
                                prescribedSets = screen.sets,
                                prescribedReps = screen.reps,
                                prescribedRest = screen.rest
                            )
                            is Screen.WorkoutSummary -> HomeScreen()
                            is Screen.BodyPartSelection -> BodyPartSelectionScreen(workoutTypeString = screen.workoutType)
                            is Screen.BodyPartWorkoutDetail -> BodyPartWorkoutDetailScreen(
                                workoutTypeString = screen.workoutType,
                                bodyPartId = screen.bodyPartId,
                                initialExerciseId = screen.initialExerciseId
                            )

                            // Admin Portal
                            is Screen.AdminDashboard -> com.example.fitness.feature.admin.AdminDashboardScreen()
                            is Screen.AdminUsers -> com.example.fitness.feature.admin.AdminUsersScreen()
                            is Screen.AdminNotifications -> com.example.fitness.feature.admin.AdminNotificationsScreen()
                            is Screen.AdminWorkouts -> com.example.fitness.feature.admin.AdminWorkoutsScreen()
                            is Screen.AdminExercises -> com.example.fitness.feature.admin.AdminExercisesScreen()
                            is Screen.AdminPrograms -> com.example.fitness.feature.admin.AdminProgramsScreen()
                            is Screen.AdminContent -> com.example.fitness.feature.admin.AdminContentAuditScreen()
                            is Screen.AdminCatalogSync -> com.example.fitness.feature.admin.AdminCatalogSyncScreen()
                            is Screen.AdminAnalytics -> com.example.fitness.feature.admin.AdminPlaceholderScreen(module = com.example.fitness.feature.admin.AdminModule.ANALYTICS)
                            is Screen.AdminSettings -> com.example.fitness.feature.admin.AdminPlaceholderScreen(module = com.example.fitness.feature.admin.AdminModule.SETTINGS)
                        }
                    }
                }
            }
        }
    }