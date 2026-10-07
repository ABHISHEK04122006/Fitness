package com.example.fitness.feature.programs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fitness.core.components.CircularIconButton
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.components.NavigationTab
import com.example.fitness.core.components.PrimaryButton
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.navigation.BackHandler
import com.example.fitness.core.navigation.LocalNavigator
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.programs.data.ProgramRegistry
import com.example.fitness.feature.programs.model.WorkoutProgram
import com.example.fitness.feature.workout.data.WorkoutExerciseEntry
import com.example.fitness.feature.workout.data.WorkoutFilterHelper
import com.example.fitness.feature.workout.data.WorkoutFilterState
import org.jetbrains.compose.resources.painterResource

private val programFilterCategories = listOf("All", "General", "Muscle Growth", "Strength")
private val workoutTabOptions = listOf("Exercises", "Programs")

object ProgramsScreenState {
    var selectedTab: String = "Exercises"
    var selectedProgramCategory: String = "All"
    var searchQuery: String = ""
    var quickCategory: String = "All"
    var manualFilterState: WorkoutFilterState = WorkoutFilterState()
}

@Composable
fun ProgramsScreen(
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current
    val focusManager = LocalFocusManager.current

    var selectedTab by remember(navigator.selectedProgramsTab) {
        mutableStateOf(
            if (navigator.selectedProgramsTab.isNotBlank()) navigator.selectedProgramsTab
            else ProgramsScreenState.selectedTab
        )
    }
    var selectedProgramCategory by remember { mutableStateOf(ProgramsScreenState.selectedProgramCategory) }
    var searchQuery by remember { mutableStateOf(ProgramsScreenState.searchQuery) }
    var quickCategory by remember { mutableStateOf(ProgramsScreenState.quickCategory) }
    var manualFilterState by remember { mutableStateOf(ProgramsScreenState.manualFilterState) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        ProgramsScreenState.selectedTab = selectedTab
        navigator.selectedProgramsTab = selectedTab
    }
    LaunchedEffect(selectedProgramCategory) {
        ProgramsScreenState.selectedProgramCategory = selectedProgramCategory
    }
    LaunchedEffect(searchQuery) {
        ProgramsScreenState.searchQuery = searchQuery
    }
    LaunchedEffect(quickCategory) {
        ProgramsScreenState.quickCategory = quickCategory
    }
    LaunchedEffect(manualFilterState) {
        ProgramsScreenState.manualFilterState = manualFilterState
    }

    LaunchedEffect(Unit) {
        visible = true
    }

    // Baseline datasets — Athlete Training is moved to Home screen, keeping standard workout programs here
    val allPrograms = remember(UserSession.experienceLevel) {
        ProgramRegistry.getWorkoutPrograms(UserSession.experienceLevel)
    }

    // Resolve user's preferred difficulty from profile or UserSession
    val userDefaultDifficulty = remember(UserSession.experienceLevel, UserSession.currentUserProfile) {
        val profileLevel = UserSession.currentUserProfile?.fitnessLevel?.trim()
        if (!profileLevel.isNullOrBlank()) {
            val lower = profileLevel.lowercase()
            when {
                lower.contains("no") || lower.contains("none") -> "No Experience"
                lower.contains("beginner") -> "Beginner"
                lower.contains("intermediate") -> "Beginner"
                lower.contains("advance") -> "Advanced"
                lower.contains("expert") -> "Expert"
                else -> "No Experience"
            }
        } else {
            when (UserSession.experienceLevel) {
                com.example.fitness.feature.workout.model.ExperienceLevel.NO_EXPERIENCE -> "No Experience"
                com.example.fitness.feature.workout.model.ExperienceLevel.BEGINNER -> "Beginner"
                com.example.fitness.feature.workout.model.ExperienceLevel.ADVANCED -> "Advanced"
                com.example.fitness.feature.workout.model.ExperienceLevel.EXPERT -> "Expert"
            }
        }
    }

    val activeDifficulty = if (manualFilterState.difficulty == "All") userDefaultDifficulty else manualFilterState.difficulty
    val activeLevel = remember(activeDifficulty) {
        com.example.fitness.feature.workout.model.ExperienceLevel.fromUserFacingOption(activeDifficulty)
    }

    val allExercises = remember(com.example.fitness.feature.exercise.data.SampleExerciseData.canonicalRevision, UserSession.currentUserGender, activeLevel) {
        WorkoutFilterHelper.getAllExercises(gender = UserSession.currentUserGender, level = activeLevel)
    }

    // Effective filter combining quickCategory, manualFilterState, and user experience level
    val effectiveFilter = remember(quickCategory, manualFilterState, activeDifficulty) {
        val targetMuscle = if (quickCategory != "All") quickCategory else manualFilterState.muscleGroup
        manualFilterState.copy(
            muscleGroup = targetMuscle,
            difficulty = activeDifficulty
        )
    }

    // Filtered Exercises (simultaneous search + quickCategory + manualFilterState)
    val filteredExercises = remember(allExercises, searchQuery, effectiveFilter) {
        WorkoutFilterHelper.filterExercises(allExercises, searchQuery, effectiveFilter)
    }

    // Filtered Programs
    val filteredPrograms = remember(allPrograms, selectedProgramCategory, searchQuery) {
        allPrograms.filter { program ->
            val matchesCategory = (selectedProgramCategory == "All") ||
                    (selectedProgramCategory == "General" && (program.category == "General" || program.id == "30-day")) ||
                    (selectedProgramCategory == "Muscle Growth" && (program.category == "Muscle Growth" || program.id == "hypertrophy")) ||
                    (selectedProgramCategory == "Strength" && (program.category == "Strength" || program.id == "strength"))
            val matchesSearch = searchQuery.trim().isEmpty() ||
                    program.title.contains(searchQuery.trim(), ignoreCase = true) ||
                    program.subtitle.contains(searchQuery.trim(), ignoreCase = true) ||
                    program.focus.contains(searchQuery.trim(), ignoreCase = true)

            matchesCategory && matchesSearch
        }
    }

    // Auto-switch to Exercises tab if user searches for an exercise that has 0 program matches
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank() && filteredPrograms.isEmpty() && filteredExercises.isNotEmpty() && selectedTab != "Exercises") {
            selectedTab = "Exercises"
            navigator.selectedProgramsTab = "Exercises"
        }
    }

    val handleBack: () -> Unit = {
        if (!navigator.pop()) {
            navigator.switchTab(NavigationTab.HOME)
        }
    }

    BackHandler(enabled = true) {
        handleBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 840.dp)
                .statusBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(10.dp))

        // Top Bar: Back Button, Title, Filter Button with active indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularIconButton(
                icon = FitnessIcons.ArrowBack,
                onClick = handleBack,
                contentDescription = "Back"
            )

            Text(
                text = "Workouts",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Box {
                CircularIconButton(
                    icon = FitnessIcons.Filter,
                    onClick = { showFilterSheet = true },
                    contentDescription = "Filter"
                )

                if (manualFilterState.activeFilterCount > 0) {
                    Surface(
                        shape = CircleShape,
                        color = FitTheme.colors.primary,
                        modifier = Modifier
                            .size(17.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${manualFilterState.activeFilterCount}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF000000)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Real Interactive Search Bar
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF07090C),
            border = BorderStroke(1.dp, Color(0xFF161E24)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(48.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = FitnessIcons.Search,
                    contentDescription = "Search",
                    tint = Color(0xFFA2B4C0),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search exercises (e.g., bench, press, curl)...",
                            fontSize = 13.sp,
                            color = Color(0xFF5A6B77),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        cursorBrush = SolidColor(FitTheme.colors.primary),
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Clear 'X' button
                if (searchQuery.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        onClick = { searchQuery = "" },
                        shape = CircleShape,
                        color = Color(0xFF141C22),
                        modifier = Modifier.size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "✕",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA2B4C0)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Tab Switcher: "Exercises" vs "Programs"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            workoutTabOptions.forEach { tab ->
                val isSelected = tab == selectedTab
                val count = if (tab == "Exercises") filteredExercises.size else filteredPrograms.size
                val bgColor by animateColorAsState(
                    if (isSelected) FitTheme.colors.primary else Color(0xFF07090C)
                )
                val textColor by animateColorAsState(
                    if (isSelected) Color(0xFF000000) else Color(0xFF8E9BA4)
                )

                Surface(
                    onClick = {
                        selectedTab = tab
                        navigator.selectedProgramsTab = tab
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = bgColor,
                    border = if (isSelected) null else BorderStroke(1.dp, Color(0xFF161E24)),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$tab ($count)",
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Contextual Secondary Filters:
        // When on Exercises: Quick Muscle Group Pills (All, Chest, Back, Shoulders, Biceps, Triceps, Legs, Abs, Full Body)
        // When on Programs: Category Pills (All, Muscle Growth, Strength, Performance)
        if (selectedTab == "Exercises") {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(WorkoutFilterHelper.availableMuscleGroups) { muscle ->
                    val isSelected = quickCategory.equals(muscle, ignoreCase = true)
                    val bgColor by animateColorAsState(
                        if (isSelected) FitTheme.colors.primary else Color(0xFF07090C)
                    )
                    val textColor by animateColorAsState(
                        if (isSelected) Color(0xFF000000) else Color(0xFF8E9BA4)
                    )

                    Surface(
                        onClick = {
                            quickCategory = muscle
                        },
                        shape = RoundedCornerShape(18.dp),
                        color = bgColor,
                        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFF161E24)),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = muscle,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }
            }

            val isFullBodyCategory = quickCategory.equals("Full Body", ignoreCase = true) ||
                    manualFilterState.muscleGroup.equals("Full Body", ignoreCase = true)
            if (isFullBodyCategory) {
                Spacer(modifier = Modifier.height(8.dp))
                val difficultyOptions = listOf("No Experience", "Beginner", "Advanced", "Expert")
                val activeDifficulty = if (manualFilterState.difficulty == "All") userDefaultDifficulty else manualFilterState.difficulty

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(difficultyOptions) { level ->
                        val isLevelSelected = activeDifficulty.equals(level, ignoreCase = true)
                        val levelBgColor by animateColorAsState(
                            if (isLevelSelected) FitTheme.colors.primary else Color(0xFF0D141A)
                        )
                        val levelTextColor by animateColorAsState(
                            if (isLevelSelected) Color(0xFF000000) else Color(0xFF8E9BA4)
                        )

                        Surface(
                            onClick = {
                                manualFilterState = manualFilterState.copy(difficulty = level)
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = levelBgColor,
                            border = if (isLevelSelected) null else BorderStroke(1.dp, Color(0xFF1B2730)),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = level,
                                    fontSize = 11.sp,
                                    fontWeight = if (isLevelSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = levelTextColor
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(programFilterCategories) { category ->
                    val isSelected = category == selectedProgramCategory
                    val bgColor by animateColorAsState(
                        if (isSelected) FitTheme.colors.primary else Color(0xFF07090C)
                    )
                    val textColor by animateColorAsState(
                        if (isSelected) Color(0xFF000000) else Color(0xFF8E9BA4)
                    )

                    Surface(
                        onClick = { selectedProgramCategory = category },
                        shape = RoundedCornerShape(20.dp),
                        color = bgColor,
                        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFF161E24)),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dynamic Result Count Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val countText = if (selectedTab == "Exercises") {
                "${filteredExercises.size} Exercises"
            } else {
                "${filteredPrograms.size} Programs"
            }

            Text(
                text = countText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFA2B4C0)
            )

            if (manualFilterState.activeFilterCount > 0 && selectedTab == "Exercises") {
                Text(
                    text = "Clear filters",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FitTheme.colors.primary,
                    modifier = Modifier.clickable {
                        manualFilterState = manualFilterState.reset()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main List Content
        if (selectedTab == "Exercises") {
            if (filteredExercises.isEmpty()) {
                WorkoutEmptyState(
                    onReset = {
                        searchQuery = ""
                        quickCategory = "All"
                        manualFilterState = manualFilterState.reset()
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(filteredExercises) { index, exercise ->
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                                    slideInVertically(
                                        initialOffsetY = { 20 * (index % 10 + 1) },
                                        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)
                                    )
                        ) {
                            WorkoutExerciseCard(
                                exercise = exercise,
                                onClick = {
                                    val setsText = if (exercise.sets.contains("×")) {
                                        exercise.sets.substringBefore("×").trim()
                                    } else {
                                        exercise.sets
                                    }
                                    val repsText = if (exercise.sets.contains("×")) {
                                        exercise.sets.substringAfter("×").trim()
                                    } else {
                                        exercise.duration
                                    }
                                    navigator.navigate(
                                        Screen.ExerciseDetail(
                                            exerciseId = exercise.id,
                                            sets = setsText,
                                            reps = repsText,
                                            rest = exercise.restTime
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        } else {
            if (filteredPrograms.isEmpty()) {
                WorkoutEmptyState(
                    title = "No programs found",
                    message = "Try changing your search query or category filter.",
                    onReset = {
                        searchQuery = ""
                        selectedProgramCategory = "All"
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(filteredPrograms) { index, program ->
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                                    slideInVertically(
                                        initialOffsetY = { 30 * (index + 1) },
                                        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)
                                    )
                        ) {
                            ProgramCard(
                                program = program,
                                onClick = {
                                    navigator.navigate(Screen.ProgramDetail(program.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    }

    // Filter Bottom Sheet / Dialog
    if (showFilterSheet) {
        WorkoutFilterBottomSheet(
            currentFilter = manualFilterState,
            onApply = { newFilter ->
                manualFilterState = newFilter
                quickCategory = newFilter.muscleGroup
                showFilterSheet = false
            },
            onReset = {
                manualFilterState = manualFilterState.reset()
                quickCategory = "All"
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }
}

/**
 * Exercise Card for Workout section, matching ATHGYM aesthetics and routine card design.
 */
@Composable
private fun WorkoutExerciseCard(
    exercise: WorkoutExerciseEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1.0f)

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF07090C),
        border = BorderStroke(1.dp, Color(0xFF182229)),
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail Image with Play Indicator Overlay
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                Image(
                    painter = painterResource(exercise.imageRes),
                    contentDescription = exercise.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark vignette
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF000000).copy(alpha = 0.35f))
                )

                // Play icon button
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF000000).copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(26.dp)
                        .align(Alignment.Center)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FitnessIcons.Play,
                            contentDescription = "Play",
                            tint = FitTheme.colors.primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Exercise Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Sets & Reps
                Text(
                    text = exercise.sets,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF9BAAB4)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Metadata Pills: Body Part, Target Muscle, Equipment & Rest
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF141C22),
                        border = BorderStroke(0.6.dp, Color(0xFF22303A))
                    ) {
                        Text(
                            text = exercise.bodyPart.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (exercise.targetMuscle.isNotBlank() && !exercise.targetMuscle.equals(exercise.bodyPart, ignoreCase = true)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF141C22),
                            border = BorderStroke(0.6.dp, Color(0xFF22303A))
                        ) {
                            Text(
                                text = exercise.targetMuscle.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4EE4FF),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (exercise.equipment.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF141C22),
                            border = BorderStroke(0.6.dp, Color(0xFF22303A))
                        ) {
                            Text(
                                text = exercise.equipment,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFB0BEC5),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF141C22),
                        border = BorderStroke(0.6.dp, Color(0xFF22303A))
                    ) {
                        Text(
                            text = exercise.restTime,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8E9BA4),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = FitnessIcons.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF5A6B77),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Filter Bottom Sheet with real options for Muscle Group, Level, Equipment, and Workout Type.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkoutFilterBottomSheet(
    currentFilter: WorkoutFilterState,
    onApply: (WorkoutFilterState) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var tempMuscle by remember { mutableStateOf(currentFilter.muscleGroup) }
    var tempDifficulty by remember { mutableStateOf(currentFilter.difficulty) }
    var tempEquipment by remember { mutableStateOf(currentFilter.equipment) }
    var tempWorkoutType by remember { mutableStateOf(currentFilter.workoutType) }

    val currentActiveCount = remember(tempMuscle, tempDifficulty, tempEquipment, tempWorkoutType) {
        var count = 0
        if (tempMuscle != "All") count++
        if (tempDifficulty != "All") count++
        if (tempEquipment != "All") count++
        if (tempWorkoutType != "Gym") count++
        count
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x99000000))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color(0xFF0D1318),
                border = BorderStroke(1.dp, Color(0xFF1F2A33)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* prevent dismissal when clicking inside sheet */ }
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 28.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter Workouts",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Clear All",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FitTheme.colors.primary,
                                modifier = Modifier
                                    .clickable {
                                        tempMuscle = "All"
                                        tempDifficulty = "All"
                                        tempEquipment = "All"
                                        tempWorkoutType = "Gym"
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            CircularIconButton(
                                icon = FitnessIcons.ArrowBack,
                                onClick = onDismiss,
                                contentDescription = "Close filter"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Muscle Group Section
                    Text(
                        text = "MUSCLE GROUP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF7A8B97)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WorkoutFilterHelper.availableMuscleGroups.forEach { muscle ->
                            FilterChipItem(
                                label = muscle,
                                isSelected = tempMuscle.equals(muscle, ignoreCase = true),
                                onClick = { tempMuscle = muscle }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Difficulty / Level Section
                    Text(
                        text = "LEVEL / DIFFICULTY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF7A8B97)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WorkoutFilterHelper.availableDifficulties.forEach { diff ->
                            FilterChipItem(
                                label = diff,
                                isSelected = tempDifficulty.equals(diff, ignoreCase = true),
                                onClick = { tempDifficulty = diff }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Equipment Section
                    Text(
                        text = "EQUIPMENT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF7A8B97)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WorkoutFilterHelper.availableEquipments.forEach { equip ->
                            FilterChipItem(
                                label = equip,
                                isSelected = tempEquipment.equals(equip, ignoreCase = true),
                                onClick = { tempEquipment = equip }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Workout Type Section
                    Text(
                        text = "WORKOUT TYPE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF7A8B97)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WorkoutFilterHelper.availableWorkoutTypes.forEach { type ->
                            FilterChipItem(
                                label = type,
                                isSelected = tempWorkoutType.equals(type, ignoreCase = true),
                                onClick = { tempWorkoutType = type }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Apply Button
                    PrimaryButton(
                        text = if (currentActiveCount > 0) "Apply Filters ($currentActiveCount)" else "Apply Filters",
                        onClick = {
                            onApply(
                                WorkoutFilterState(
                                    muscleGroup = tempMuscle,
                                    difficulty = tempDifficulty,
                                    equipment = tempEquipment,
                                    workoutType = tempWorkoutType
                                )
                            )
                        },
                        height = 48.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) FitTheme.colors.primary else Color(0xFF141C22)
    val textColor = if (isSelected) Color(0xFF000000) else Color(0xFFC0D0DC)
    val borderColor = if (isSelected) FitTheme.colors.primary else Color(0xFF22303A)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.height(34.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

/**
 * Clean Empty State when search or filter yields 0 matches.
 */
@Composable
private fun WorkoutEmptyState(
    title: String = "No exercises found",
    message: String = "Try changing your search or filters.",
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFF141C22),
            border = BorderStroke(1.dp, Color(0xFF22303A)),
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = FitnessIcons.Search,
                    contentDescription = null,
                    tint = Color(0xFF5A6B77),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = message,
            fontSize = 13.sp,
            color = Color(0xFF8E9BA4),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            onClick = onReset,
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF141C22),
            border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.5f)),
            modifier = Modifier.height(38.dp)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Reset Filters",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = FitTheme.colors.primary
                )
            }
        }
    }
}

@Composable
private fun ProgramCard(
    program: WorkoutProgram,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1.0f)

    val isActive = UserSession.activeProgramId == program.id
    val completedCount = UserSession.getProgramCompletedCount(program.id)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF07090C),
        border = BorderStroke(1.dp, if (isActive) FitTheme.colors.primary else Color(0xFF161E24))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Image Banner with Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Image(
                    painter = painterResource(program.imageRes),
                    contentDescription = program.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0x9907090C),
                                    Color(0xFF07090C)
                                )
                            )
                        )
                )

                // Top Pills: Category & Active Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xCC000000)
                    ) {
                        Text(
                            text = program.category.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (isActive) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FitTheme.colors.primary
                        ) {
                            Text(
                                text = "ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF000000),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Bottom Content on Image
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = program.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = program.subtitle,
                        fontSize = 13.sp,
                        color = Color(0xFFC0D0DC),
                        maxLines = 2,
                        softWrap = true
                    )
                }
            }

            // Bottom Specs Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    modifier = Modifier.weight(1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = FitnessIcons.Clock,
                            contentDescription = null,
                            tint = Color(0xFFA2B4C0),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = program.duration,
                            fontSize = 12.sp,
                            color = Color(0xFFA2B4C0)
                        )
                    }
                    Text(
                        text = " • ",
                        fontSize = 12.sp,
                        color = Color(0xFF333E46)
                    )
                    Text(
                        text = "${program.workoutsCount} Workouts",
                        fontSize = 12.sp,
                        color = Color(0xFFA2B4C0)
                    )
                    if (completedCount > 0) {
                        Text(
                            text = " • ",
                            fontSize = 12.sp,
                            color = Color(0xFF333E46)
                        )
                        Text(
                            text = "$completedCount completed",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FitTheme.colors.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = FitnessIcons.ChevronRight,
                    contentDescription = null,
                    tint = FitTheme.colors.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
