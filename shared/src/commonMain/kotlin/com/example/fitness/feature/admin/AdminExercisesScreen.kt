package com.example.fitness.feature.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.fitness.core.auth.UserRole
import com.example.fitness.core.components.CircularIconButton
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.navigation.LocalNavigator
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.admin.data.AdminExerciseCatalogOrganizer
import com.example.fitness.feature.admin.data.AdminWorkoutCatalogProvider
import com.example.fitness.feature.admin.data.ExerciseBodyPartSection
import com.example.fitness.feature.exercise.data.FirebaseExerciseRepository
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.media.FemaleExerciseMediaRegistry
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.workout.data.WorkoutRepository
import fitness.shared.generated.resources.Res
import fitness.shared.generated.resources.gender_female
import fitness.shared.generated.resources.workout_gym
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

/**
 * Admin Exercise Catalog Screen.
 *
 * Dedicated gender exercise catalog:
 * - Directly opens for the specified [gender] ("Male" or "Female").
 * - Does NOT show the Male/Female selector again.
 * - Organizes exercises into body-part / muscle-group sections:
 *   CHEST, BACK, SHOULDERS, BICEPS, TRICEPS, LEGS, CORE, etc.
 * - Distinguishes activities/sprints/drills so they are never presented as normal strength body parts.
 * - Uses the EXACT SAME underlying exercise source as the User platform.
 * - Preserves existing Search, Filters, Add/Edit dialog, and Cloudflare R2 media upload/delete flow.
 */
@Composable
fun AdminExercisesScreen(
    gender: String = "Male",
    initialGender: String? = null,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val activeGender = initialGender ?: gender
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val exerciseRepository = remember { FirebaseExerciseRepository() }

    // Screen-level RBAC guard
    LaunchedEffect(UserSession.currentUserRole) {
        if (UserSession.currentUserRole != UserRole.ADMIN) {
            navigator.resetRoot(Screen.Home)
        }
    }

    if (UserSession.currentUserRole != UserRole.ADMIN) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF000000)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Access Denied. Redirecting...",
                color = Color(0xFFFF5252),
                fontSize = 14.sp
            )
        }
        return
    }

    val allExercises = remember { mutableStateListOf<Exercise>() }
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") } // ALL, ACTIVE, ARCHIVED
    var bodyPartFilter by remember { mutableStateOf("ALL") } // ALL or section name

    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var editorErrorMessage by remember { mutableStateOf<String?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Dialog controllers
    var selectedExerciseForDetails by remember { mutableStateOf<Exercise?>(null) }
    var selectedExerciseToEdit by remember { mutableStateOf<Exercise?>(null) }
    var isCreateDialogOpen by remember { mutableStateOf(false) }
    var exerciseToArchive by remember { mutableStateOf<Exercise?>(null) }

    fun loadExercises() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val exercises = AdminWorkoutCatalogProvider.getUnifiedExercisesForGender(activeGender)
                allExercises.clear()
                allExercises.addAll(exercises)
            } catch (e: Exception) {
                errorMessage = "Failed to load exercises: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(activeGender) {
        loadExercises()
    }

    // In-memory filtered list for current gender
    val filteredExercises = remember(
        searchQuery,
        statusFilter,
        bodyPartFilter,
        allExercises.toList()
    ) {
        val query = searchQuery.trim().lowercase()

        allExercises.filter { ex ->
            // 1. Status Filter
            val matchesStatus = when (statusFilter) {
                "ACTIVE" -> ex.isActive
                "ARCHIVED" -> !ex.isActive
                else -> true
            }

            // 2. Body Part Section Filter
            val matchesBodyPart = if (bodyPartFilter == "ALL") {
                true
            } else {
                val section = AdminExerciseCatalogOrganizer.classify(ex)
                section.displayName.equals(bodyPartFilter, ignoreCase = true) ||
                        section.name.equals(bodyPartFilter, ignoreCase = true)
            }

            // 3. Live search across name, muscles, equipment, movement, id
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                ex.name.lowercase().contains(query) ||
                        ex.primaryMuscle.lowercase().contains(query) ||
                        ex.movementPattern.lowercase().contains(query) ||
                        ex.secondaryMuscles.any { it.lowercase().contains(query) } ||
                        ex.equipment.any { it.lowercase().contains(query) } ||
                        ex.id.lowercase().contains(query)
            }

            matchesStatus && matchesBodyPart && matchesQuery
        }
    }

    // Group filtered exercises into body-part sections
    val groupedSections = remember(filteredExercises) {
        AdminExerciseCatalogOrganizer.groupExercisesBySection(filteredExercises)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // HEADER: Back button + Title + Add Exercise
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    CircularIconButton(
                        icon = FitnessIcons.ArrowBack,
                        contentDescription = "Back",
                        onClick = {
                            if (onNavigateBack != null) {
                                onNavigateBack()
                            } else {
                                navigator.navigate(Screen.AdminWorkouts)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${activeGender.uppercase()} EXERCISES",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${allExercises.size} exercises across body parts",
                            fontSize = 11.sp,
                            color = Color(0xFF8E9BA4)
                        )
                    }
                }

                // Add Exercise Button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = FitTheme.colors.primary,
                    modifier = Modifier
                        .height(38.dp)
                        .clickable { isCreateDialogOpen = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = FitnessIcons.Add,
                            contentDescription = "Add Exercise",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Add Exercise",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // SEARCH BAR
            // ==========================================
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0E1318),
                border = BorderStroke(1.dp, Color(0xFF1E2832)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = FitnessIcons.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF8E9BA4),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(FitTheme.colors.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search by exercise name, muscle, equipment...",
                                    fontSize = 13.sp,
                                    color = Color(0xFF5A6872)
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = FitnessIcons.Close,
                            contentDescription = "Clear",
                            tint = Color(0xFF8E9BA4),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { searchQuery = "" }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ==========================================
            // FILTER CHIPS: Status & Body Parts
            // ==========================================
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Status filters
                items(listOf("ALL", "ACTIVE", "ARCHIVED")) { status ->
                    val isSelected = statusFilter == status
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) FitTheme.colors.primary else Color(0xFF0D1117),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) FitTheme.colors.primary else Color(0xFF1B2228)
                        ),
                        modifier = Modifier.clickable { statusFilter = status }
                    ) {
                        Text(
                            text = status,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.Black else Color(0xFFBAC5D0),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                // Separator pill
                item {
                    Box(
                        modifier = Modifier
                            .height(26.dp)
                            .width(1.dp)
                            .background(Color(0xFF222B35))
                    )
                }

                // Body part sections
                val bodyPartOptions = listOf("ALL") + ExerciseBodyPartSection.entries.map { it.displayName }
                items(bodyPartOptions) { bp ->
                    val isSelected = bodyPartFilter == bp
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) FitTheme.colors.primary.copy(alpha = 0.2f) else Color(0xFF0D1117),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) FitTheme.colors.primary else Color(0xFF1B2228)
                        ),
                        modifier = Modifier.clickable { bodyPartFilter = bp }
                    ) {
                        Text(
                            text = bp,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) FitTheme.colors.primary else Color(0xFF8E9BA4),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // CONTENT: LIST / LOADING / EMPTY STATES
            // ==========================================
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = FitTheme.colors.primary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading $activeGender exercises...",
                                fontSize = 13.sp,
                                color = Color(0xFF8E9BA4)
                            )
                        }
                    }
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Error Loading Exercises",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5252)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = errorMessage ?: "Unknown error occurred.",
                                fontSize = 12.sp,
                                color = Color(0xFF8E9BA4),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF161E24),
                                border = BorderStroke(1.dp, Color(0xFF2B3A42)),
                                modifier = Modifier.clickable { loadExercises() }
                            ) {
                                Text(
                                    text = "Retry",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                filteredExercises.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            Text(
                                text = if (allExercises.isEmpty()) "$activeGender library is empty" else "No exercises match your filters",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (allExercises.isEmpty()) {
                                    "Create a new $activeGender exercise to get started."
                                } else {
                                    "Try adjusting your search query, status, or body-part filter."
                                },
                                fontSize = 12.sp,
                                color = Color(0xFF8E9BA4),
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = FitTheme.colors.primary,
                                modifier = Modifier
                                    .height(42.dp)
                                    .clickable { isCreateDialogOpen = true }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    Text(
                                        text = "+ Add Exercise",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Render exercises grouped into Body-Part / Muscle-Group sections
                        groupedSections.forEach { (section, exercisesInSection) ->
                            item(key = "section_${section.name}") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp, bottom = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = section.displayName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FitTheme.colors.primary,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF161E24),
                                        border = BorderStroke(1.dp, Color(0xFF26333D))
                                    ) {
                                        Text(
                                            text = "${exercisesInSection.size}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFBAC5D0),
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(1.dp)
                                            .background(Color(0xFF1E2832))
                                    )
                                }
                            }

                            items(exercisesInSection, key = { it.id }) { exercise ->
                                ExerciseAdminCard(
                                    exercise = exercise,
                                    isFemaleGender = activeGender.equals("Female", ignoreCase = true),
                                    onView = { selectedExerciseForDetails = SampleExerciseData.getById(exercise.id) ?: exercise },
                                    onEdit = { selectedExerciseToEdit = exercise },
                                    onArchive = { exerciseToArchive = exercise },
                                    onRestore = {
                                        coroutineScope.launch {
                                            val res = exerciseRepository.restoreExercise(exercise.id)
                                            if (res.isSuccess) {
                                                snackbarMessage = "Restored '${exercise.name}'."
                                                loadExercises()
                                            } else {
                                                snackbarMessage = "Restore failed: ${res.exceptionOrNull()?.message}"
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOGS & OVERLAYS
    // ==========================================

    // 1. Details Dialog (Includes Cloudflare R2 Media Upload/Replace/Remove)
    selectedExerciseForDetails?.let { ex ->
        AdminExerciseDetailsDialog(
            exercise = ex,
            onDismiss = { selectedExerciseForDetails = null },
            onEdit = {
                selectedExerciseForDetails = null
                selectedExerciseToEdit = ex
            },
            onExerciseUpdated = { updated ->
                SampleExerciseData.updateCanonicalExercise(updated)
                selectedExerciseForDetails = updated
                loadExercises()
            }
        )
    }

    // 2. Editor Dialog (Create Mode)
    if (isCreateDialogOpen) {
        AdminExerciseEditorDialog(
            exerciseToEdit = null,
            defaultGender = activeGender,
            isSaving = isSaving,
            errorMessage = editorErrorMessage,
            onDismiss = { isCreateDialogOpen = false },
            onSave = { newExercise ->
                coroutineScope.launch {
                    isSaving = true
                    editorErrorMessage = null
                    val res = exerciseRepository.createExercise(newExercise)
                    isSaving = false
                    if (res.isSuccess) {
                        SampleExerciseData.updateCanonicalExercise(newExercise)
                        isCreateDialogOpen = false
                        snackbarMessage = "Exercise '${newExercise.name}' created."
                        loadExercises()
                    } else {
                        editorErrorMessage = res.exceptionOrNull()?.message ?: "Failed to create exercise"
                    }
                }
            }
        )
    }

    // 3. Editor Dialog (Edit Mode)
    selectedExerciseToEdit?.let { exercise ->
        AdminExerciseEditorDialog(
            exerciseToEdit = exercise,
            defaultGender = activeGender,
            isSaving = isSaving,
            errorMessage = editorErrorMessage,
            onDismiss = { selectedExerciseToEdit = null },
            onSave = { updatedExercise ->
                coroutineScope.launch {
                    isSaving = true
                    editorErrorMessage = null
                    val res = exerciseRepository.updateExercise(updatedExercise)
                    isSaving = false
                    if (res.isSuccess) {
                        SampleExerciseData.updateCanonicalExercise(updatedExercise)
                        selectedExerciseToEdit = null
                        snackbarMessage = "Exercise '${updatedExercise.name}' updated."
                        loadExercises()
                    } else {
                        editorErrorMessage = res.exceptionOrNull()?.message ?: "Failed to update exercise"
                    }
                }
            }
        )
    }

    // 4. Archive Confirmation Dialog
    exerciseToArchive?.let { exercise ->
        Dialog(onDismissRequest = { exerciseToArchive = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0D1117),
                border = BorderStroke(1.dp, Color(0xFF1B2228)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFF5252).copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = FitnessIcons.Archive,
                                    contentDescription = "Archive",
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Archive Exercise",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Are you sure you want to archive '${exercise.name}'?\n\nThis exercise will be hidden from routine creation but preserved for user history. Permanent deletion is prohibited.",
                        fontSize = 13.sp,
                        color = Color(0xFFBAC5D0),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF161E24),
                            border = BorderStroke(1.dp, Color(0xFF2B3A42)),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clickable { exerciseToArchive = null }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Cancel",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFF5252),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clickable {
                                    val id = exercise.id
                                    exerciseToArchive = null
                                    coroutineScope.launch {
                                        val res = exerciseRepository.archiveExercise(id)
                                        if (res.isSuccess) {
                                            snackbarMessage = "Archived '${exercise.name}'."
                                            loadExercises()
                                        } else {
                                            snackbarMessage = "Archive failed: ${res.exceptionOrNull()?.message}"
                                        }
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Archive",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }


}

/**
 * Exercise Card item in the admin list.
 * Displays thumbnail preview, exercise name, active/archived badge, category, difficulty,
 * primary muscle, equipment, and actions.
 */
@Composable
private fun ExerciseAdminCard(
    exercise: Exercise,
    isFemaleGender: Boolean = false,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0D1117),
        border = BorderStroke(
            1.dp,
            if (exercise.isActive) Color(0xFF1B2228) else Color(0xFFFF5252).copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onView() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail Preview
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF161E24),
                    border = BorderStroke(1.dp, Color(0xFF2B3A42)),
                    modifier = Modifier.size(54.dp)
                ) {
                    val fallbackRes = if (isFemaleGender) {
                        FemaleExerciseMediaRegistry.resolveExerciseImage(
                            exercise.id,
                            isFemale = true,
                            defaultFallback = Res.drawable.gender_female
                        )
                    } else {
                        Res.drawable.workout_gym
                    }

                    Image(
                        painter = painterResource(fallbackRes),
                        contentDescription = exercise.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Exercise Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = exercise.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (exercise.isActive) {
                                FitTheme.colors.primary.copy(alpha = 0.15f)
                            } else {
                                Color(0xFFFF5252).copy(alpha = 0.15f)
                            },
                            border = BorderStroke(
                                1.dp,
                                if (exercise.isActive) FitTheme.colors.primary.copy(alpha = 0.4f) else Color(0xFFFF5252).copy(alpha = 0.4f)
                            )
                        ) {
                            Text(
                                text = if (exercise.isActive) "ACTIVE" else "ARCHIVED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (exercise.isActive) FitTheme.colors.primary else Color(0xFFFF5252),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Muscle: ${exercise.primaryMuscle.ifBlank { "Full Body" }}${if (exercise.equipment.isNotEmpty()) " • ${exercise.equipment.joinToString(", ")}" else ""}",
                        fontSize = 11.sp,
                        color = Color(0xFF8E9BA4),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (exercise.difficulty.isNotBlank()) {
                            val formattedDiff = when (exercise.difficulty.trim().uppercase()) {
                                "NO_EXPERIENCE", "NO EXPERIENCE", "NOEXPERIENCE" -> "No Experience"
                                "BEGINNER" -> "Beginner"
                                "ADVANCED" -> "Advanced"
                                "EXPERT" -> "Expert"
                                else -> exercise.difficulty.replace('_', ' ').lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF161E24)
                            ) {
                                Text(
                                    text = formattedDiff,
                                    fontSize = 10.sp,
                                    color = Color(0xFFBAC5D0),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (exercise.category.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF161E24)
                            ) {
                                Text(
                                    text = exercise.category.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                                    fontSize = 10.sp,
                                    color = Color(0xFF8E9BA4),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        val hasUploadedVideo = exercise.media.videoUrl.isNotBlank() && !exercise.media.videoUrl.contains("fitness.app/demo", ignoreCase = true)
                        if (hasUploadedVideo) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = FitTheme.colors.primary.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(FitTheme.colors.primary)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "VIDEO UPLOADED ✓",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FitTheme.colors.primary
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF1E262E),
                                border = BorderStroke(1.dp, Color(0xFF33424D))
                            ) {
                                Text(
                                    text = "NO VIDEO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF8E9BA4),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val hasUploadedVideo = exercise.media.videoUrl.isNotBlank() && !exercise.media.videoUrl.contains("fitness.app/demo", ignoreCase = true)

                // Dedicated Video Upload / Manage Action Button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (hasUploadedVideo) Color(0xFF161E24) else FitTheme.colors.primary.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, if (hasUploadedVideo) Color(0xFF26333D) else FitTheme.colors.primary.copy(alpha = 0.6f)),
                    modifier = Modifier.clickable { onView() }
                ) {
                    Text(
                        text = if (hasUploadedVideo) "Manage Video" else "+ Upload Video",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasUploadedVideo) Color.White else FitTheme.colors.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // View Details Button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF161E24),
                    border = BorderStroke(1.dp, Color(0xFF26333D)),
                    modifier = Modifier.clickable { onView() }
                ) {
                    Text(
                        text = "Details",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Edit Button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF161E24),
                    border = BorderStroke(1.dp, Color(0xFF26333D)),
                    modifier = Modifier.clickable { onEdit() }
                ) {
                    Text(
                        text = "Edit",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFBAC5D0),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Archive / Restore Button
                if (exercise.isActive) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF161E24),
                        border = BorderStroke(1.dp, Color(0xFF26333D)),
                        modifier = Modifier.clickable { onArchive() }
                    ) {
                        Text(
                            text = "Archive",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFF5252),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = FitTheme.colors.primary.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable { onRestore() }
                    ) {
                        Text(
                            text = "Restore",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FitTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}
