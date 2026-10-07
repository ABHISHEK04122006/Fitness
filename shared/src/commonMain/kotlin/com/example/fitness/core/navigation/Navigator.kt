package com.example.fitness.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.fitness.core.components.NavigationTab
import com.example.fitness.core.session.UserSession

@Stable
class Navigator(initialScreen: Screen = Screen.Auth(isSignUp = false)) {
    private val backStack = mutableStateListOf<Screen>(initialScreen)

    var currentScreen: Screen by mutableStateOf(initialScreen)
        private set

    var currentTab: NavigationTab by mutableStateOf(NavigationTab.HOME)
        private set

    var isNavigatingBack: Boolean by mutableStateOf(false)
        private set

    var selectedProgramsTab: String by mutableStateOf("Exercises")

    val canPop: Boolean
        get() = backStack.size > 1

    val backStackSize: Int
        get() = backStack.size

    fun getBackStack(): List<Screen> = backStack.toList()

    fun navigate(screen: Screen) {
        if (screen == currentScreen) return
        if (screen is Screen.Home) {
            switchTab(NavigationTab.HOME)
            return
        }
        if (backStack.isNotEmpty()) {
            val last = backStack.last()
            if (last == screen) return
            if (last is Screen.WorkoutOverview && screen is Screen.WorkoutOverview && last.workoutId == screen.workoutId) return
            if (last is Screen.ActiveWorkoutSession && screen is Screen.ActiveWorkoutSession && last.workoutId == screen.workoutId) return
            if (last is Screen.ProgramDetail && screen is Screen.ProgramDetail && last.programId == screen.programId) return
            if (last is Screen.BodyPartWorkoutDetail && screen is Screen.BodyPartWorkoutDetail && last.bodyPartId == screen.bodyPartId && last.initialExerciseId == screen.initialExerciseId) return
            if (last is Screen.ExerciseDetail && screen is Screen.ExerciseDetail && last.exerciseId == screen.exerciseId) return
        }
        isNavigatingBack = false
        backStack.add(screen)
        currentScreen = screen
        updateTabForScreen(screen)
    }

    fun replace(screen: Screen) {
        isNavigatingBack = false
        if (backStack.isNotEmpty()) {
            backStack.removeAt(backStack.lastIndex)
        }
        backStack.add(screen)
        currentScreen = screen
        updateTabForScreen(screen)
    }

    fun resetRoot(screen: Screen) {
        isNavigatingBack = false
        backStack.clear()
        backStack.add(screen)
        currentScreen = screen
        updateTabForScreen(screen)
    }

    fun pop(): Boolean {
        if (backStack.size > 1) {
            isNavigatingBack = true
            backStack.removeAt(backStack.lastIndex)
            currentScreen = backStack.last()
            updateTabForScreen(currentScreen)
            return true
        }
        return false
    }

    fun popToRoot() {
        if (backStack.size > 1) {
            isNavigatingBack = true
            while (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
            currentScreen = backStack.first()
            updateTabForScreen(currentScreen)
        }
    }

    fun switchTab(tab: NavigationTab) {
        val targetScreen: Screen = when (tab) {
            NavigationTab.HOME -> Screen.Home
            NavigationTab.EXPLORE -> if (UserSession.isFemale) Screen.FemalePlanSelection else Screen.Explore
            NavigationTab.PROGRESS -> Screen.Progress
            NavigationTab.PROFILE -> Screen.Profile
        }
        if (currentScreen == targetScreen) return
        if (tab == NavigationTab.EXPLORE && (currentScreen is Screen.Explore || currentScreen is Screen.Programs || currentScreen is Screen.FemalePlanSelection || currentScreen is Screen.Workout)) {
            currentTab = tab
            return
        }
        currentTab = tab

        if (targetScreen == Screen.Home) {
            isNavigatingBack = true
            // Pop back to Home if it exists in stack, else reset to Home
            val homeIndex = backStack.indexOfFirst { it is Screen.Home }
            if (homeIndex != -1) {
                while (backStack.lastIndex > homeIndex) {
                    backStack.removeAt(backStack.lastIndex)
                }
            } else {
                backStack.clear()
                backStack.add(Screen.Home)
            }
            currentScreen = Screen.Home
            updateTabForScreen(Screen.Home)
        } else {
            isNavigatingBack = false
            // If already on a secondary tab screen, replace it so back always returns to Home
            val lastIsSecondaryTab = backStack.lastOrNull()?.let {
                it is Screen.Explore || it is Screen.Programs || it is Screen.FemalePlanSelection || it is Screen.Workout || it is Screen.Progress || it is Screen.Profile
            } ?: false

            if (lastIsSecondaryTab) {
                backStack.removeAt(backStack.lastIndex)
            }
            backStack.add(targetScreen)
            currentScreen = targetScreen
            updateTabForScreen(targetScreen)
        }
    }

    private fun updateTabForScreen(screen: Screen) {
        when (screen) {
            is Screen.Home -> currentTab = NavigationTab.HOME
            is Screen.Explore, is Screen.Programs, is Screen.FemalePlanSelection, is Screen.ProgramDetail, is Screen.Workout -> currentTab = NavigationTab.EXPLORE
            is Screen.Progress -> currentTab = NavigationTab.PROGRESS
            is Screen.Profile -> currentTab = NavigationTab.PROFILE
            else -> Unit
        }
    }
}

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("No Navigator provided")
}

@Composable
fun rememberNavigator(initialScreen: Screen = Screen.Auth(isSignUp = false)): Navigator {
    return remember { Navigator(initialScreen) }
}
