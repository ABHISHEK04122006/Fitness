package com.example.fitness.feature.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.example.fitness.core.navigation.BackHandler
import com.example.fitness.core.navigation.LocalNavigator
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.admin.data.AdminWorkoutCatalogProvider
import com.example.fitness.feature.exercise.data.FirebaseExerciseRepository
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.program.data.FirebaseProgramRepository
import com.example.fitness.feature.program.model.Program
import com.example.fitness.feature.workout.model.ExperienceLevel
import kotlinx.coroutines.launch

/**
 * Admin Program Management Screen.
 *
 * Implements Gender-First UX:
 * 1. Initial State: Shows [ Male Programs ] and [ Female Programs ] selection cards.
 * 2. On Gender Selection: Displays the unified programs available to the User platform for that gender.
 * 3. Includes live search, filters (Status), and program day overview.
 * 4. Add/Edit program flows using the existing Program model.
 * 5. Uses the EXACT SAME underlying program data as the User platform.
 */
@Composable
fun AdminProgramsScreen(
    initialGender: String? = null,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val programRepository = remember { FirebaseProgramRepository() }
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

    // Gender selection state: null = show selection cards; "Male" or "Female" = show list
    var selectedGender by remember { mutableStateOf(initialGender) }

    val allPrograms = remember { mutableStateListOf<Program>() }
    val exercisesMap = remember { mutableStateMapOf<String, Exercise>() }

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") } // ALL, ACTIVE, ARCHIVED
    var experienceFilter by remember { mutableStateOf("ALL") } // ALL or ExperienceLevel name

    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var editorErrorMessage by remember { mutableStateOf<String?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Modal controllers
    var selectedProgramForDetails by remember { mutableStateOf<Program?>(null) }
    var selectedProgramToEdit by remember { mutableStateOf<Program?>(null) }
    var isCreateDialogOpen by remember { mutableStateOf(false) }
    var programToArchive by remember { mutableStateOf<Program?>(null) }

    fun loadPrograms(gender: String) {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val programs = AdminWorkoutCatalogProvider.getUnifiedProgramsForGender(gender)
                allPrograms.clear()
                allPrograms.addAll(programs)

                // Cache exercises for reference resolving
                val exercises = exerciseRepository.getAllExercisesForAdmin()
                exercisesMap.clear()
                exercises.forEach { exercisesMap[it.id] = it }
            } catch (e: Exception) {
                errorMessage = "Failed to load programs: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedGender) {
        val current = selectedGender
        if (current != null) {
            loadPrograms(current)
        } else {
            allPrograms.clear()
        }
    }

    // In-memory filtered list
    val filteredPrograms = remember(
        searchQuery,
        statusFilter,
        experienceFilter,
        allPrograms.toList()
    ) {
        val query = searchQuery.trim().lowercase()

        allPrograms.filter { prog ->
            // 1. Status Filter
            val matchesStatus = when (statusFilter) {
                "ACTIVE" -> prog.isActive
                "ARCHIVED" -> !prog.isActive
                else -> true
            }

            // 2. Experience Level Filter
            val matchesExperience = if (experienceFilter == "ALL") {
                true
            } else {
                prog.experienceLevel.equals(experienceFilter, ignoreCase = true)
            }

            // 3. In-memory search
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                prog.name.lowercase().contains(query) ||
                        prog.id.lowercase().contains(query) ||
                        prog.slug.lowercase().contains(query) ||
                        prog.description.lowercase().contains(query) ||
                        prog.type.lowercase().contains(query) ||
                        prog.experienceLevel.lowercase().contains(query)
            }

            matchesStatus && matchesExperience && matchesQuery
        }
    }

    // ==========================================
    // 1. GENDER SELECTION VIEW (When no gender chosen)
    // ==========================================
    if (selectedGender == null) {
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

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularIconButton(
                        icon = FitnessIcons.ArrowBack,
                        contentDescription = "Back",
                        onClick = {
                            if (onNavigateBack != null) onNavigateBack() else navigator.navigate(Screen.AdminDashboard)
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Programs",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Select gender curriculum to manage",
                            fontSize = 12.sp,
                            color = Color(0xFF8E9BA4)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Male Programs Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF0D1117),
                    border = BorderStroke(1.dp, Color(0xFF1B2228)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedGender = "Male" }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = FitTheme.colors.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = FitnessIcons.Clipboard,
                                    contentDescription = "Male Programs",
                                    tint = FitTheme.colors.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Male Programs",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "30-Day Foundation, Hypertrophy, Strength & Athlete Performance plans",
                                fontSize = 12.sp,
                                color = Color(0xFF8E9BA4),
                                lineHeight = 16.sp
                            )
                        }

                        Icon(
                            imageVector = FitnessIcons.ArrowForward,
                            contentDescription = "Open Male Programs",
                            tint = FitTheme.colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Female Programs Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF0D1117),
                    border = BorderStroke(1.dp, Color(0xFF1B2228)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedGender = "Female" }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFF4081).copy(alpha = 0.15f),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = FitnessIcons.Clipboard,
                                    contentDescription = "Female Programs",
                                    tint = Color(0xFFFF4081),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Female Programs",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "9 Authoritative 30-Day curricula: Weight Loss, Sculpted Body, Perfect Butt, etc.",
                                fontSize = 12.sp,
                                color = Color(0xFF8E9BA4),
                                lineHeight = 16.sp
                            )
                        }

                        Icon(
                            imageVector = FitnessIcons.ArrowForward,
                            contentDescription = "Open Female Programs",
                            tint = Color(0xFFFF4081),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        return
    }

    // ==========================================
    // 2. PROGRAM LIST VIEW (For Selected Gender)
    // ==========================================
    val activeGender = selectedGender ?: "Male"

    // Intercept back navigation to return to gender selection
    BackHandler(enabled = true) {
        if (initialGender != null && onNavigateBack != null) {
            onNavigateBack()
        } else {
            selectedGender = null
        }
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
            // TOP ACTION BAR
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularIconButton(
                        icon = FitnessIcons.ArrowBack,
                        contentDescription = "Back",
                        onClick = {
                            if (initialGender != null && onNavigateBack != null) {
                                onNavigateBack()
                            } else {
                                selectedGender = null
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "$activeGender Programs",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${filteredPrograms.size} of ${allPrograms.size} program${if (allPrograms.size == 1) "" else "s"}",
                            fontSize = 12.sp,
                            color = Color(0xFF8E9BA4)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularIconButton(
                        icon = FitnessIcons.Replay,
                        contentDescription = "Refresh Programs",
                        onClick = { loadPrograms(activeGender) }
                    )

                    // + Add Program Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FitTheme.colors.primary,
                        modifier = Modifier
                            .height(38.dp)
                            .clickable {
                                editorErrorMessage = null
                                isCreateDialogOpen = true
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = FitnessIcons.Add,
                                contentDescription = "Add Program",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Transient message banner
            if (snackbarMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = FitTheme.colors.primary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = snackbarMessage ?: "",
                            fontSize = 12.sp,
                            color = FitTheme.colors.primary
                        )
                        Text(
                            text = "✕",
                            fontSize = 12.sp,
                            color = Color(0xFF8E9BA4),
                            modifier = Modifier.clickable { snackbarMessage = null }
                        )
                    }
                }
            }

            // Search Bar
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0D1117),
                border = BorderStroke(1.dp, Color(0xFF1B2228)),
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
                        tint = Color(0xFF5A6E7B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(FitTheme.colors.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search by name, slug, description, type...",
                                    fontSize = 13.sp,
                                    color = Color(0xFF5A6E7B)
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        Text(
                            text = "✕",
                            fontSize = 14.sp,
                            color = Color(0xFF8E9BA4),
                            modifier = Modifier
                                .clickable { searchQuery = "" }
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips: Status
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
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
                                text = "Loading $activeGender programs...",
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
                                text = "Error Loading Programs",
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
                                modifier = Modifier.clickable { loadPrograms(activeGender) }
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

                filteredPrograms.isEmpty() -> {
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
                                text = if (allPrograms.isEmpty()) "$activeGender curriculum is empty" else "No programs match your filters",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (allPrograms.isEmpty()) {
                                    "Create a new $activeGender program using the button below."
                                } else {
                                    "Try adjusting your search query or status filter."
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
                                        text = "+ Add Program",
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
                        items(filteredPrograms, key = { it.id }) { program ->
                            ProgramAdminCard(
                                program = program,
                                onView = { selectedProgramForDetails = program },
                                onEdit = { selectedProgramToEdit = program },
                                onArchive = { programToArchive = program },
                                onRestore = {
                                    coroutineScope.launch {
                                        val res = programRepository.restoreProgram(program.id)
                                        if (res.isSuccess) {
                                            snackbarMessage = "Restored '${program.name}'."
                                            loadPrograms(activeGender)
                                        } else {
                                            snackbarMessage = "Restore failed: ${res.exceptionOrNull()?.message}"
                                        }
                                    }
                                }
                            )
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
    // MODALS & DIALOGS
    // ==========================================

    // 1. Program Details Modal
    selectedProgramForDetails?.let { program ->
        AdminProgramDetailsDialog(
            program = program,
            exercisesMap = exercisesMap,
            onDismiss = { selectedProgramForDetails = null },
            onEdit = {
                selectedProgramForDetails = null
                selectedProgramToEdit = program
            }
        )
    }

    // 2. Program Editor Dialog (Create Mode)
    if (isCreateDialogOpen) {
        AdminProgramEditorDialog(
            programToEdit = null,
            defaultGender = activeGender,
            isSaving = isSaving,
            errorMessage = editorErrorMessage,
            onDismiss = { isCreateDialogOpen = false },
            onSave = { newProgram ->
                coroutineScope.launch {
                    isSaving = true
                    editorErrorMessage = null
                    val res = programRepository.createProgram(newProgram)
                    isSaving = false
                    if (res.isSuccess) {
                        isCreateDialogOpen = false
                        snackbarMessage = "Program '${newProgram.name}' created."
                        loadPrograms(activeGender)
                    } else {
                        editorErrorMessage = res.exceptionOrNull()?.message ?: "Failed to create program"
                    }
                }
            }
        )
    }

    // 3. Program Editor Dialog (Edit Mode)
    selectedProgramToEdit?.let { program ->
        AdminProgramEditorDialog(
            programToEdit = program,
            defaultGender = activeGender,
            isSaving = isSaving,
            errorMessage = editorErrorMessage,
            onDismiss = { selectedProgramToEdit = null },
            onSave = { updatedProgram ->
                coroutineScope.launch {
                    isSaving = true
                    editorErrorMessage = null
                    val res = programRepository.updateProgram(updatedProgram)
                    isSaving = false
                    if (res.isSuccess) {
                        selectedProgramToEdit = null
                        snackbarMessage = "Program '${updatedProgram.name}' updated."
                        loadPrograms(activeGender)
                    } else {
                        editorErrorMessage = res.exceptionOrNull()?.message ?: "Failed to update program"
                    }
                }
            }
        )
    }

    // 4. Archive Confirmation Dialog
    programToArchive?.let { program ->
        Dialog(onDismissRequest = { programToArchive = null }) {
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
                            text = "Archive Program",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Are you sure you want to archive '${program.name}'?\n\nThis will hide the program from the user catalog. Existing user enrollment and progress records are strictly preserved.",
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
                                .clickable { programToArchive = null }
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
                                    val id = program.id
                                    programToArchive = null
                                    coroutineScope.launch {
                                        val res = programRepository.archiveProgram(id)
                                        if (res.isSuccess) {
                                            snackbarMessage = "Archived '${program.name}'."
                                            loadPrograms(activeGender)
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
 * Program Card item in the admin list.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProgramAdminCard(
    program: Program,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit
) {
    val typeBadgeText = when {
        program.type.equals("THIRTY_DAY", ignoreCase = true) || program.id.contains("thirty_day") -> "FULL BODY"
        program.type.isNotBlank() -> program.type.replace('_', ' ').uppercase()
        else -> "PROGRAM"
    }
    val levelBadgeText = program.experienceLevel.replace('_', ' ').uppercase().ifBlank { "ALL LEVELS" }
    val daysBadgeText = "${program.durationDays} DAYS"
    val workoutsBadgeText = "${if (program.days.isNotEmpty()) program.days.size else program.durationDays} WORKOUTS"

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0D1117),
        border = BorderStroke(
            1.dp,
            if (program.isActive) Color(0xFF1B2228) else Color(0xFFFF5252).copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onView() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // 1. Top Row: Title & ID on Left, Status Badge on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = program.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "ID: ${program.id}",
                        fontSize = 11.sp,
                        color = Color(0xFF5A6E7B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (program.isActive) {
                        FitTheme.colors.primary.copy(alpha = 0.15f)
                    } else {
                        Color(0xFFFF5252).copy(alpha = 0.15f)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (program.isActive) {
                            FitTheme.colors.primary.copy(alpha = 0.4f)
                        } else {
                            Color(0xFFFF5252).copy(alpha = 0.4f)
                        }
                    )
                ) {
                    Text(
                        text = if (program.isActive) "ACTIVE" else "ARCHIVED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (program.isActive) FitTheme.colors.primary else Color(0xFFFF5252),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            // 2. Description (compact 2-line maximum)
            if (program.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = program.description,
                    fontSize = 12.sp,
                    color = Color(0xFF8E9BA4),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Metadata Badges (FlowRow ensures badges wrap whole without vertical word-break)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Badge 1: Type / Category (Accent neon green)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = FitTheme.colors.primary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = typeBadgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = FitTheme.colors.primary,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Badge 2: Experience Level
                if (levelBadgeText.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF161E24)
                    ) {
                        Text(
                            text = levelBadgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFBAC5D0),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Badge 3: Duration Days
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF161E24)
                ) {
                    Text(
                        text = daysBadgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFBAC5D0),
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Badge 4: Workouts Count
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF161E24)
                ) {
                    Text(
                        text = workoutsBadgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFBAC5D0),
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Actions Row (Right-Aligned)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF161E24),
                    border = BorderStroke(1.dp, Color(0xFF2B3A42)),
                    modifier = Modifier.clickable { onView() }
                ) {
                    Text(
                        text = "View Days",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF161E24),
                    border = BorderStroke(1.dp, Color(0xFF2B3A42)),
                    modifier = Modifier.clickable { onEdit() }
                ) {
                    Text(
                        text = "Edit",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FitTheme.colors.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (program.isActive) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF261214),
                        border = BorderStroke(1.dp, Color(0xFF4A1A22)),
                        modifier = Modifier.clickable { onArchive() }
                    ) {
                        Text(
                            text = "Archive",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5252),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = FitTheme.colors.primary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable { onRestore() }
                    ) {
                        Text(
                            text = "Restore",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
