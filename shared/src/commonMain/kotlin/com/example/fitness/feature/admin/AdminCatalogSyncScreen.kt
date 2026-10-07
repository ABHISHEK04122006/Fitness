package com.example.fitness.feature.admin

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.fitness.core.auth.UserRole
import com.example.fitness.core.components.CircularIconButton
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.components.PrimaryButton
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.navigation.LocalNavigator
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.admin.data.AdminCatalogSyncRepository
import com.example.fitness.feature.admin.model.AdminSyncHistoryRecord
import com.example.fitness.feature.admin.model.CatalogSyncPreview
import com.example.fitness.feature.admin.model.CatalogSyncResult
import com.example.fitness.feature.admin.model.WorkoutCatalogItem
import com.example.fitness.feature.program.model.Program
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.model.Exercise
import kotlinx.coroutines.launch

/**
 * Tab definitions for the Admin Catalog Seed & Sync Center.
 */
enum class AdminCatalogTab(val label: String) {
    SYNC_CENTER("Sync Center"),
    EXERCISES("Exercises (222)"),
    WORKOUTS("Workouts (16)"),
    PROGRAMS("Programs (9)"),
    HISTORY("Audit History")
}

/**
 * Admin Catalog Management / Seed & Sync screen.
 *
 * Allows administrators to inspect canonical exercises, workouts, and programs,
 * run pre-flight sync preview analyses, safely synchronize catalog records with Firestore,
 * and review persistent audit logs.
 */
@Composable
fun AdminCatalogSyncScreen(
    initialTab: AdminCatalogTab = AdminCatalogTab.SYNC_CENTER,
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val syncRepo = remember { AdminCatalogSyncRepository() }

    // Screen-level RBAC guard
    LaunchedEffect(UserSession.currentUserRole) {
        if (UserSession.currentUserRole != UserRole.ADMIN) {
            navigator.resetRoot(Screen.Home)
        }
    }

    if (UserSession.currentUserRole != UserRole.ADMIN) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF000000)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Access Denied. Redirecting...", color = Color(0xFFFF5252), fontSize = 14.sp)
        }
        return
    }

    var currentTab by remember { mutableStateOf(initialTab) }
    var preview by remember { mutableStateOf<CatalogSyncPreview?>(null) }
    var isGeneratingPreview by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncResult by remember { mutableStateOf<CatalogSyncResult?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val historyRecords = remember { mutableStateListOf<AdminSyncHistoryRecord>() }
    var isLoadingHistory by remember { mutableStateOf(false) }

    val canonicalExercises = remember { syncRepo.getCanonicalExercises() }
    val canonicalWorkouts = remember { syncRepo.getCanonicalWorkouts() }
    val canonicalPrograms = remember { syncRepo.getCanonicalPrograms() }
    val canonicalExercisesCount = canonicalExercises.size

    fun refreshHistory() {
        isLoadingHistory = true
        coroutineScope.launch {
            try {
                val records = syncRepo.getSyncHistory()
                historyRecords.clear()
                historyRecords.addAll(records)
            } catch (e: Exception) {
                println("[WARN] AdminCatalogSyncScreen: Failed to load sync history: ${e.message}")
            } finally {
                isLoadingHistory = false
            }
        }
    }

    fun triggerPreview() {
        isGeneratingPreview = true
        errorMessage = null
        coroutineScope.launch {
            val res = syncRepo.generatePreview()
            isGeneratingPreview = false
            res.fold(
                onSuccess = { preview = it },
                onFailure = { errorMessage = it.message ?: "Failed to generate preview." }
            )
        }
    }

    fun executeSync() {
        isSyncing = true
        errorMessage = null
        coroutineScope.launch {
            val res = syncRepo.executeSync()
            isSyncing = false
            res.fold(
                onSuccess = {
                    syncResult = it
                    // Trigger history reload and update preview
                    refreshHistory()
                    triggerPreview()
                },
                onFailure = {
                    errorMessage = it.message ?: "Synchronization failed."
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        triggerPreview()
        refreshHistory()
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
                .widthIn(max = 640.dp)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // HEADER BAR
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularIconButton(
                        icon = FitnessIcons.ArrowBack,
                        contentDescription = "Back to Dashboard",
                        onClick = { navigator.navigate(Screen.AdminDashboard) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Catalog Seed / Sync",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Canonical Exercise, Workout & Program Sync",
                            fontSize = 12.sp,
                            color = Color(0xFF8E9BA4)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF151B26),
                    border = BorderStroke(1.dp, Color(0xFF222C3D))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ADMIN",
                            color = Color(0xFF00E676),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // TAB ROW
            // ==========================================
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(AdminCatalogTab.values()) { tab ->
                    val isSelected = currentTab == tab
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFF00E676).copy(alpha = 0.15f) else Color(0xFF151B26),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF00E676) else Color(0xFF222C3D)
                        ),
                        modifier = Modifier.clickable { currentTab = tab }
                    ) {
                        Text(
                            text = tab.label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF00E676) else Color(0xFF8E9BA4),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // ERROR BANNER (IF ANY)
            // ==========================================
            if (errorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF2A1215),
                    border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFFF5252),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ==========================================
            // ACTIVE TAB CONTENT
            // ==========================================
            when (currentTab) {
                AdminCatalogTab.SYNC_CENTER -> {
                    SyncCenterContent(
                        canonicalExercisesCount = canonicalExercisesCount,
                        canonicalWorkoutsCount = canonicalWorkouts.size,
                        canonicalProgramsCount = canonicalPrograms.size,
                        preview = preview,
                        isGeneratingPreview = isGeneratingPreview,
                        isSyncing = isSyncing,
                        syncResult = syncResult,
                        onRefreshPreview = { triggerPreview() },
                        onRequestSync = { showConfirmDialog = true }
                    )
                }
                AdminCatalogTab.EXERCISES -> {
                    CanonicalExercisesContent(exercises = canonicalExercises)
                }
                AdminCatalogTab.WORKOUTS -> {
                    CanonicalWorkoutsContent(workouts = canonicalWorkouts)
                }
                AdminCatalogTab.PROGRAMS -> {
                    CanonicalProgramsContent(programs = canonicalPrograms)
                }
                AdminCatalogTab.HISTORY -> {
                    SyncHistoryContent(
                        records = historyRecords,
                        isLoading = isLoadingHistory,
                        onRefresh = { refreshHistory() }
                    )
                }
            }
        }
    }

    // ==========================================
    // CONFIRMATION DIALOG
    // ==========================================
    if (showConfirmDialog) {
        Dialog(onDismissRequest = { if (!isSyncing) showConfirmDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF151B26),
                border = BorderStroke(1.dp, Color(0xFF222C3D)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Confirm Catalog Synchronization",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "This operation will synchronize canonical exercises (222), workouts (16), and programs (9) with Firebase Firestore.\n\n" +
                                "• IDEMPOTENT: Safe to run multiple times.\n" +
                                "• MISSING items will be created with stable IDs.\n" +
                                "• EXISTING items will be updated with canonical fields.\n" +
                                "• ARCHIVE SAFETY: Soft-deleted items (isActive=false) will remain preserved.\n" +
                                "• MEDIA SAFETY: Working video/image URLs will NOT be overwritten.\n" +
                                "• NO deletion of user data or workout history.",
                        fontSize = 13.sp,
                        color = Color(0xFFBAC5D0),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF222C3D),
                            modifier = Modifier.clickable { showConfirmDialog = false }
                        ) {
                            Text(
                                text = "Cancel",
                                color = Color.White,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        PrimaryButton(
                            text = "Start Sync",
                            onClick = {
                                showConfirmDialog = false
                                executeSync()
                            },
                            modifier = Modifier.height(40.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tab 1: Sync Center Content (Overview, Preview, Action, Live Results).
 */
@Composable
private fun SyncCenterContent(
    canonicalExercisesCount: Int,
    canonicalWorkoutsCount: Int,
    canonicalProgramsCount: Int,
    preview: CatalogSyncPreview?,
    isGeneratingPreview: Boolean,
    isSyncing: Boolean,
    syncResult: CatalogSyncResult?,
    onRefreshPreview: () -> Unit,
    onRequestSync: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Asset Overview Banner
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF151B26),
                border = BorderStroke(1.dp, Color(0xFF222C3D)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CANONICAL REPOSITORY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8E9BA4),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Idempotent Safe Engine",
                            fontSize = 11.sp,
                            color = Color(0xFF00E676)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatPill("Exercises", "$canonicalExercisesCount items")
                        StatPill("Workouts", "$canonicalWorkoutsCount routines")
                        StatPill("Programs", "$canonicalProgramsCount plans")
                    }
                }
            }
        }

        // 2. Pre-flight Preview Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF151B26),
                border = BorderStroke(1.dp, Color(0xFF222C3D)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PRE-FLIGHT SYNC PREVIEW",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8E9BA4),
                            letterSpacing = 1.sp
                        )
                        if (isGeneratingPreview) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color(0xFF00E676),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Re-analyze",
                                color = Color(0xFF00E676),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { onRefreshPreview() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (preview != null) {
                        PreviewEntityRow("Exercises", preview.exerciseNewCount, preview.exerciseExistingCount, preview.exerciseUpdateCount)
                        Spacer(modifier = Modifier.height(8.dp))
                        PreviewEntityRow("Workouts", preview.workoutNewCount, preview.workoutExistingCount, preview.workoutUpdateCount)
                        Spacer(modifier = Modifier.height(8.dp))
                        PreviewEntityRow("Programs", preview.programNewCount, preview.programExistingCount, preview.programUpdateCount)

                        if (preview.warnings.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF332005),
                                border = BorderStroke(1.dp, Color(0xFFFFA726).copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Duplicate / Collision Warnings (${preview.warnings.size})",
                                        color = Color(0xFFFFA726),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    preview.warnings.forEach { warn ->
                                        Text(
                                            text = "• $warn",
                                            color = Color(0xFFFFD54F),
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else if (!isGeneratingPreview) {
                        Text(
                            text = "Click Re-analyze to evaluate live Firestore status.",
                            fontSize = 13.sp,
                            color = Color(0xFF8E9BA4)
                        )
                    }
                }
            }
        }

        // 3. Execution Action Button
        item {
            if (isSyncing) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF151B26),
                    border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF00E676),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Synchronizing catalog with Firestore...",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                PrimaryButton(
                    text = "Seed / Sync Catalog",
                    onClick = onRequestSync,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )
            }
        }

        // 4. Real Execution Result Summary Card
        if (syncResult != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF151B26),
                    border = BorderStroke(
                        1.dp,
                        if (syncResult.success) Color(0xFF00E676).copy(alpha = 0.6f) else Color(0xFFFF5252).copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CATALOG SYNC COMPLETE",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (syncResult.success) Color(0xFF00E676) else Color(0xFFFF5252),
                                letterSpacing = 1.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (syncResult.success) Color(0xFF00E676).copy(alpha = 0.2f) else Color(0xFFFF5252).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (syncResult.success) "SUCCESS" else "HAS ISSUES",
                                    color = if (syncResult.success) Color(0xFF00E676) else Color(0xFFFF5252),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Detailed breakdown table
                        ResultRowHeader()
                        Spacer(modifier = Modifier.height(6.dp))
                        ResultDataRow("Exercises", syncResult.exerciseSummary.created, syncResult.exerciseSummary.updated, syncResult.exerciseSummary.skipped, syncResult.exerciseSummary.failed)
                        Spacer(modifier = Modifier.height(4.dp))
                        ResultDataRow("Workouts", syncResult.workoutSummary.created, syncResult.workoutSummary.updated, syncResult.workoutSummary.skipped, syncResult.workoutSummary.failed)
                        Spacer(modifier = Modifier.height(4.dp))
                        ResultDataRow("Programs", syncResult.programSummary.created, syncResult.programSummary.updated, syncResult.programSummary.skipped, syncResult.programSummary.failed)

                        Spacer(modifier = Modifier.height(10.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF222C3D)))
                        Spacer(modifier = Modifier.height(10.dp))

                        ResultDataRow("Total", syncResult.totalCreated, syncResult.totalUpdated, syncResult.totalSkipped, syncResult.totalFailed, isTotal = true)

                        if (syncResult.errors.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Failure Details:",
                                color = Color(0xFFFF5252),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            syncResult.errors.forEach { err ->
                                Text(
                                    text = "• $err",
                                    color = Color(0xFFFF8A80),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

/**
 * Tab 2: Canonical Exercises Content (Inspection of 222 canonical exercises).
 */
@Composable
private fun CanonicalExercisesContent(
    exercises: List<Exercise>
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf("ALL") }
    var expandedExerciseId by remember { mutableStateOf<String?>(null) }

    val muscleFilters = listOf("ALL", "Chest", "Back", "Legs", "Shoulders", "Arms", "Core")

    val filtered = remember(searchQuery, selectedMuscle, exercises) {
        val q = searchQuery.trim().lowercase()
        exercises.filter { ex ->
            val matchesMuscle = selectedMuscle == "ALL" || ex.primaryMuscle.equals(selectedMuscle, ignoreCase = true)
            val matchesSearch = q.isEmpty() ||
                    ex.name.lowercase().contains(q) ||
                    ex.id.lowercase().contains(q) ||
                    ex.primaryMuscle.lowercase().contains(q) ||
                    ex.equipment.any { it.lowercase().contains(q) }
            matchesMuscle && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF151B26),
                    border = BorderStroke(1.dp, Color(0xFF222C3D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
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
                            textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                            singleLine = true,
                            cursorBrush = SolidColor(Color(0xFF00E676)),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text("Search 222 exercises by name, muscle, equipment...", color = Color(0xFF6B7280), fontSize = 13.sp)
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(muscleFilters) { muscle ->
                        val isSelected = selectedMuscle.equals(muscle, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF00E676).copy(alpha = 0.2f) else Color(0xFF151B26),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF00E676) else Color(0xFF222C3D)),
                            modifier = Modifier.clickable { selectedMuscle = muscle }
                        ) {
                            Text(
                                text = muscle,
                                color = if (isSelected) Color(0xFF00E676) else Color(0xFF8E9BA4),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Showing ${filtered.size} of ${exercises.size} canonical exercises",
                        fontSize = 11.sp,
                        color = Color(0xFF8E9BA4)
                    )
                }
            }
        }

        items(filtered) { exercise ->
            val isExpanded = expandedExerciseId == exercise.id
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF151B26),
                border = BorderStroke(1.dp, Color(0xFF222C3D)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedExerciseId = if (isExpanded) null else exercise.id }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = exercise.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "ID: ${exercise.id} • ${exercise.primaryMuscle} • ${exercise.equipment}",
                                fontSize = 12.sp,
                                color = Color(0xFF8E9BA4)
                            )
                        }

                        val hasMedia = exercise.media.videoUrl.isNotBlank() || exercise.media.thumbnailUrl.isNotBlank() || exercise.media.animationUrl.isNotBlank()
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (hasMedia) Color(0xFF00E676).copy(alpha = 0.15f) else Color(0xFFFFA726).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (hasMedia) "MEDIA READY" else "MEDIA PENDING",
                                color = if (hasMedia) Color(0xFF00E676) else Color(0xFFFFA726),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF222C3D)))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Difficulty: ${exercise.difficulty}", fontSize = 11.sp, color = Color(0xFFBAC5D0))
                            Text(text = "Category: ${exercise.category}", fontSize = 11.sp, color = Color(0xFFBAC5D0))
                            Text(text = "Pattern: ${exercise.movementPattern}", fontSize = 11.sp, color = Color(0xFFBAC5D0))
                        }

                        if (exercise.instructions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Instructions (${exercise.instructions.size} steps):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E676)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            exercise.instructions.forEachIndexed { idx, step ->
                                Text(
                                    text = "${idx + 1}. $step",
                                    fontSize = 11.sp,
                                    color = Color(0xFFBAC5D0),
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

/**
 * Tab 3: Canonical Workouts Content (Inspection of 16 Gym/Home workouts).
 */
@Composable
private fun CanonicalWorkoutsContent(
    workouts: List<WorkoutCatalogItem>
) {
    var selectedType by remember { mutableStateOf<String?>("ALL") }
    var expandedWorkoutId by remember { mutableStateOf<String?>(null) }

    val filtered = remember(selectedType, workouts) {
        if (selectedType == "ALL" || selectedType == null) {
            workouts
        } else {
            workouts.filter { it.workoutType.equals(selectedType, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "GYM", "HOME").forEach { type ->
                    val isSelected = selectedType == type
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF00E676).copy(alpha = 0.2f) else Color(0xFF151B26),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF00E676) else Color(0xFF222C3D)),
                        modifier = Modifier.clickable { selectedType = type }
                    ) {
                        Text(
                            text = type,
                            color = if (isSelected) Color(0xFF00E676) else Color(0xFF8E9BA4),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        items(filtered) { workout ->
            val isExpanded = expandedWorkoutId == workout.id
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF151B26),
                border = BorderStroke(1.dp, Color(0xFF222C3D)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedWorkoutId = if (isExpanded) null else workout.id }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = workout.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "ID: ${workout.id} • ${workout.duration} • ${workout.difficulty}",
                                fontSize = 12.sp,
                                color = Color(0xFF8E9BA4)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (workout.workoutType == "GYM") Color(0xFF00E676).copy(alpha = 0.15f) else Color(0xFF2979FF).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = workout.workoutType,
                                color = if (workout.workoutType == "GYM") Color(0xFF00E676) else Color(0xFF2979FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (workout.subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = workout.subtitle,
                            fontSize = 12.sp,
                            color = Color(0xFFBAC5D0)
                        )
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF222C3D)))
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Exercises Prescription (${workout.exercises.size}):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        workout.exercises.forEach { ex ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${ex.order}. ${ex.name}",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = ex.sets,
                                    fontSize = 12.sp,
                                    color = Color(0xFF00E676),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

/**
 * Tab 3: Canonical Programs Content (Inspection of 9 programs with full 30-day inspection).
 */
@Composable
private fun CanonicalProgramsContent(
    programs: List<Program>
) {
    var expandedProgramId by remember { mutableStateOf<String?>(null) }
    val selectedDayByProgram = remember { mutableStateMapOf<String, Int>() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(programs) { program ->
            val isExpanded = expandedProgramId == program.id
            val selectedDayNum = selectedDayByProgram[program.id] ?: 1
            val selectedDay = program.days.find { it.dayNumber == selectedDayNum } ?: program.days.firstOrNull()

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF151B26),
                border = BorderStroke(1.dp, Color(0xFF222C3D)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedProgramId = if (isExpanded) null else program.id }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = program.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "ID: ${program.id} • ${program.durationDays} Days • ${program.experienceLevel} • ${program.type}",
                                fontSize = 12.sp,
                                color = Color(0xFF8E9BA4)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF00E676).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${program.days.size} Days",
                                color = Color(0xFF00E676),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (program.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = program.description,
                            fontSize = 12.sp,
                            color = Color(0xFFBAC5D0),
                            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF222C3D)))
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "PROGRAM DAYS INSPECTOR (${program.days.size} DAYS):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676),
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Day selector chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(program.days.sortedBy { it.dayNumber }) { day ->
                                val isDaySelected = day.dayNumber == (selectedDay?.dayNumber ?: 1)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when {
                                        isDaySelected -> Color(0xFF00E676)
                                        day.isRestDay -> Color(0xFF1E2837)
                                        else -> Color(0xFF0D1117)
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        if (isDaySelected) Color(0xFF00E676) else Color(0xFF2E3D52)
                                    ),
                                    modifier = Modifier.clickable {
                                        selectedDayByProgram[program.id] = day.dayNumber
                                    }
                                ) {
                                    Text(
                                        text = "Day ${day.dayNumber}${if (day.isRestDay) " (Rest)" else ""}",
                                        fontSize = 11.sp,
                                        fontWeight = if (isDaySelected) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isDaySelected -> Color.Black
                                            day.isRestDay -> Color(0xFF8E9BA4)
                                            else -> Color.White
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        // Selected Day Details Card
                        if (selectedDay != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0D1117),
                                border = BorderStroke(1.dp, Color(0xFF222C3D)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Day ${selectedDay.dayNumber}: ${selectedDay.title.ifBlank { "Session" }}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            if (selectedDay.focus.isNotEmpty()) {
                                                Text(
                                                    text = "Focus: ${selectedDay.focus.joinToString(", ")}",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF8E9BA4)
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (selectedDay.isRestDay) Color(0xFF374151) else Color(0xFF00E676).copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = if (selectedDay.isRestDay) "REST DAY" else "${selectedDay.exercises.size} EXERCISES",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selectedDay.isRestDay) Color(0xFFD1D5DB) else Color(0xFF00E676),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    if (selectedDay.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = selectedDay.notes,
                                            fontSize = 11.sp,
                                            color = Color(0xFFBAC5D0)
                                        )
                                    }

                                    if (selectedDay.warmUp.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Warm-up: ${selectedDay.warmUp.joinToString(" • ")}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF8E9BA4)
                                        )
                                    }

                                    if (selectedDay.isRestDay) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Scheduled active recovery or complete rest to allow muscular and neural adaptation.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF8E9BA4)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Prescriptions:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00E676)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        selectedDay.exercises.forEachIndexed { idx, exRef ->
                                            val exName = SampleExerciseData.getById(exRef.exerciseId)?.name
                                                ?: exRef.exerciseId.replace('_', ' ').split(" ").joinToString(" ") { word ->
                                                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                                }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "${idx + 1}. $exName",
                                                        fontSize = 12.sp,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = "ID: ${exRef.exerciseId}",
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF8E9BA4)
                                                    )
                                                }
                                                Text(
                                                    text = "${exRef.sets} sets × ${exRef.reps} • ${exRef.restSeconds}s",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF00E676),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    if (selectedDay.coolDown.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Cool-down: ${selectedDay.coolDown.joinToString(" • ")}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF8E9BA4)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

/**
 * Tab 4: Sync History Content (Real records from Firestore admin_sync_history).
 */
@Composable
private fun SyncHistoryContent(
    records: List<AdminSyncHistoryRecord>,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HISTORICAL SYNC AUDIT LOG",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8E9BA4),
                    letterSpacing = 1.sp
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color(0xFF00E676),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Refresh",
                        color = Color(0xFF00E676),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onRefresh() }
                    )
                }
            }
        }

        if (records.isEmpty() && !isLoading) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF151B26),
                    border = BorderStroke(1.dp, Color(0xFF222C3D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No catalog synchronization history recorded yet.\nExecute your first sync from the Sync Center tab.",
                        color = Color(0xFF8E9BA4),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(records) { record ->
                val isSuccess = record.status == "SUCCESS"
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF151B26),
                    border = BorderStroke(1.dp, Color(0xFF222C3D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = record.syncId,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSuccess) Color(0xFF00E676).copy(alpha = 0.2f) else Color(0xFFFF5252).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = record.status,
                                    color = if (isSuccess) Color(0xFF00E676) else Color(0xFFFF5252),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Admin: ${record.adminUid.ifBlank { "N/A" }}",
                            fontSize = 11.sp,
                            color = Color(0xFF8E9BA4)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Created: ${record.totalCreated}",
                                fontSize = 12.sp,
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Updated: ${record.totalUpdated}",
                                fontSize = 12.sp,
                                color = Color(0xFF2979FF),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Skipped: ${record.totalSkipped}",
                                fontSize = 12.sp,
                                color = Color(0xFFFFA726),
                                fontWeight = FontWeight.Bold
                            )
                            if (record.totalFailed > 0) {
                                Text(
                                    text = "Failed: ${record.totalFailed}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFFF5252),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (!record.errorSummary.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Error: ${record.errorSummary}",
                                fontSize = 11.sp,
                                color = Color(0xFFFF5252)
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// ==========================================
// COMPOSABLE HELPERS
// ==========================================

@Composable
private fun StatPill(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 11.sp, color = Color(0xFF8E9BA4))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun PreviewEntityRow(name: String, newCount: Int, existingCount: Int, updateCount: Int = 0) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "$newCount new",
                fontSize = 12.sp,
                color = if (newCount > 0) Color(0xFF00E676) else Color(0xFF8E9BA4),
                fontWeight = if (newCount > 0) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = "$existingCount existing",
                fontSize = 12.sp,
                color = Color(0xFF8E9BA4)
            )
            if (updateCount > 0) {
                Text(
                    text = "$updateCount updates",
                    fontSize = 12.sp,
                    color = Color(0xFF2979FF),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ResultRowHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Entity", fontSize = 11.sp, color = Color(0xFF8E9BA4), modifier = Modifier.weight(1.3f))
        Text(text = "Created", fontSize = 11.sp, color = Color(0xFF8E9BA4), textAlign = TextAlign.End, modifier = Modifier.weight(0.9f))
        Text(text = "Updated", fontSize = 11.sp, color = Color(0xFF8E9BA4), textAlign = TextAlign.End, modifier = Modifier.weight(0.9f))
        Text(text = "Skipped", fontSize = 11.sp, color = Color(0xFF8E9BA4), textAlign = TextAlign.End, modifier = Modifier.weight(0.9f))
        Text(text = "Failed", fontSize = 11.sp, color = Color(0xFF8E9BA4), textAlign = TextAlign.End, modifier = Modifier.weight(0.9f))
    }
}

@Composable
private fun ResultDataRow(name: String, created: Int, updated: Int, skipped: Int, failed: Int, isTotal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            fontSize = 12.sp,
            color = if (isTotal) Color.White else Color(0xFFBAC5D0),
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1.3f)
        )
        Text(
            text = "$created",
            fontSize = 12.sp,
            color = if (created > 0) Color(0xFF00E676) else Color(0xFF8E9BA4),
            fontWeight = if (created > 0 || isTotal) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.9f)
        )
        Text(
            text = "$updated",
            fontSize = 12.sp,
            color = if (updated > 0) Color(0xFF2979FF) else Color(0xFF8E9BA4),
            fontWeight = if (updated > 0 || isTotal) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.9f)
        )
        Text(
            text = "$skipped",
            fontSize = 12.sp,
            color = if (skipped > 0) Color(0xFFFFA726) else Color(0xFF8E9BA4),
            fontWeight = if (skipped > 0 || isTotal) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.9f)
        )
        Text(
            text = "$failed",
            fontSize = 12.sp,
            color = if (failed > 0) Color(0xFFFF5252) else Color(0xFF8E9BA4),
            fontWeight = if (failed > 0 || isTotal) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.9f)
        )
    }
}
