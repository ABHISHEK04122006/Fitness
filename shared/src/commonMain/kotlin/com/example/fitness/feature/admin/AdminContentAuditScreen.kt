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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
import com.example.fitness.feature.exercise.audit.ContentAuditFilter
import com.example.fitness.feature.exercise.audit.ContentAuditSummary
import com.example.fitness.feature.exercise.audit.ControlledBatchReport
import com.example.fitness.feature.exercise.audit.ControlledBatchResultItem
import com.example.fitness.feature.exercise.audit.ExerciseContentAuditor
import com.example.fitness.feature.exercise.audit.ExerciseMediaAuditItem
import com.example.fitness.feature.exercise.audit.ExerciseMediaBulkImporter
import com.example.fitness.feature.exercise.audit.MediaReadiness
import com.example.fitness.feature.exercise.data.ExerciseRepository
import com.example.fitness.feature.exercise.data.FirebaseExerciseRepository
import com.example.fitness.feature.exercise.data.SampleExerciseData
import com.example.fitness.feature.exercise.media.CloudflareExerciseMediaUploadRepository
import com.example.fitness.feature.exercise.media.FirebaseExerciseStorageContract
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseMediaType
import kotlinx.coroutines.launch

/**
 * Admin Content Audit Screen.
 * Provides real-time visibility into catalog exercise content completeness and media coverage.
 */
@Composable
fun AdminContentAuditScreen(
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val exerciseRepository = remember { FirebaseExerciseRepository() }

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

    val exercises = remember { mutableStateListOf<Exercise>() }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(ContentAuditFilter.ALL) }
    var selectedExerciseForDetails by remember { mutableStateOf<Exercise?>(null) }
    var showPilotBatchDialog by remember { mutableStateOf(false) }

    fun loadAuditData() {
        isLoading = true
        coroutineScope.launch {
            try {
                val list = exerciseRepository.getAllExercisesForAdmin()
                exercises.clear()
                if (list.isNotEmpty()) {
                    exercises.addAll(list)
                } else {
                    // Fallback to canonical catalog in offline/mock environment
                    exercises.addAll(SampleExerciseData.getAll())
                }
            } catch (_: Exception) {
                exercises.clear()
                exercises.addAll(SampleExerciseData.getAll())
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadAuditData()
    }

    // Calculated audit items and summary metrics
    val auditedItems = remember(exercises.toList()) {
        exercises.map { ExerciseContentAuditor.auditExercise(it) }
    }

    val auditSummary = remember(auditedItems) {
        ExerciseContentAuditor.summarizeAudit(exercises)
    }

    // Filtered audit list
    val filteredAuditItems = remember(searchQuery, activeFilter, auditedItems) {
        val query = searchQuery.trim().lowercase()

        auditedItems.filter { item ->
            // Filter match
            val matchesFilter = when (activeFilter) {
                ContentAuditFilter.ALL -> true
                ContentAuditFilter.NO_MEDIA -> item.readiness == MediaReadiness.NO_MEDIA
                ContentAuditFilter.THUMBNAIL_MISSING -> !item.hasThumbnail
                ContentAuditFilter.ANIMATION_MISSING -> !item.hasAnimation
                ContentAuditFilter.VIDEO_MISSING -> !item.hasVideo
                ContentAuditFilter.COMPLETE -> item.readiness == MediaReadiness.COMPLETE
            }

            if (!matchesFilter) return@filter false

            // Search match (name, id, slug, primaryMuscle, equipment)
            if (query.isBlank()) return@filter true

            val ex = item.exercise
            ex.name.lowercase().contains(query) ||
                    ex.id.lowercase().contains(query) ||
                    ex.slug.lowercase().contains(query) ||
                    ex.primaryMuscle.lowercase().contains(query) ||
                    ex.equipment.any { it.lowercase().contains(query) }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07090A))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header Row: Back button, Title, Subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularIconButton(
                    icon = FitnessIcons.ArrowBack,
                    contentDescription = "Back",
                    onClick = { navigator.navigate(Screen.AdminDashboard) },
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CONTENT & MEDIA AUDIT",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Athgym Catalog Health • 222 Canonical Exercises",
                        fontSize = 11.sp,
                        color = Color(0xFF5A6E7B)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF161E24),
                        border = BorderStroke(1.dp, Color(0xFF2B3A42)),
                        modifier = Modifier.clickable { showPilotBatchDialog = true }
                    ) {
                        Text(
                            text = "Pilot Batch",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = FitTheme.colors.primary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "STEP 8",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 1. METRICS SUMMARY CARDS (6 Metrics)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "TOTAL",
                    value = "${auditSummary.totalExercises}",
                    subtitle = "Exercises",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "ANY MEDIA",
                    value = "${auditSummary.withAnyMedia}",
                    subtitle = "Populated",
                    highlight = auditSummary.withAnyMedia > 0,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "NO MEDIA",
                    value = "${auditSummary.withNoMedia}",
                    subtitle = "Awaiting",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "THUMBNAIL",
                    value = "${auditSummary.withThumbnail}",
                    subtitle = "Images",
                    highlight = auditSummary.withThumbnail > 0,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "ANIMATION",
                    value = "${auditSummary.withAnimation}",
                    subtitle = "Vectors",
                    highlight = auditSummary.withAnimation > 0,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "VIDEO",
                    value = "${auditSummary.withVideo}",
                    subtitle = "HD Demos",
                    highlight = auditSummary.withVideo > 0,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // 2. SEARCH BAR
            // ==========================================
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF101416),
                border = BorderStroke(1.dp, Color(0xFF1B2228)),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = FitnessIcons.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF5A6E7B),
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                        cursorBrush = SolidColor(FitTheme.colors.primary),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search by name, ID, muscle, equipment...",
                                    color = Color(0xFF5A6E7B),
                                    fontSize = 13.sp
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = FitnessIcons.Close,
                            contentDescription = "Clear",
                            tint = Color(0xFF8E9BA4),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { searchQuery = "" }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ==========================================
            // 3. FILTER CHIPS ROW
            // ==========================================
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(ContentAuditFilter.entries) { filter ->
                    val isSelected = activeFilter == filter
                    val count = when (filter) {
                        ContentAuditFilter.ALL -> auditedItems.size
                        ContentAuditFilter.NO_MEDIA -> auditSummary.withNoMedia
                        ContentAuditFilter.THUMBNAIL_MISSING -> auditedItems.count { !it.hasThumbnail }
                        ContentAuditFilter.ANIMATION_MISSING -> auditedItems.count { !it.hasAnimation }
                        ContentAuditFilter.VIDEO_MISSING -> auditedItems.count { !it.hasVideo }
                        ContentAuditFilter.COMPLETE -> auditSummary.completeMedia
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) FitTheme.colors.primary else Color(0xFF101416),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) FitTheme.colors.primary else Color(0xFF1B2228)
                        ),
                        modifier = Modifier.clickable { activeFilter = filter }
                    ) {
                        Text(
                            text = "${filter.title} ($count)",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.Black else Color(0xFF8E9BA4),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // 4. AUDIT RESULTS LIST
            // ==========================================
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = FitTheme.colors.primary
                    )
                }
            } else if (filteredAuditItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No matching exercises found",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your search query or filter selection.",
                            fontSize = 12.sp,
                            color = Color(0xFF5A6E7B)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = filteredAuditItems,
                        key = { it.exercise.id }
                    ) { auditItem ->
                        AuditExerciseCard(
                            auditItem = auditItem,
                            onManageMedia = { selectedExerciseForDetails = auditItem.exercise }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Modal Details & Media Dialog (Step 6E UI integration)
    selectedExerciseForDetails?.let { exercise ->
        AdminExerciseDetailsDialog(
            exercise = exercise,
            onDismiss = { selectedExerciseForDetails = null },
            onEdit = { /* details dialog has close */ },
            onExerciseUpdated = { updated ->
                selectedExerciseForDetails = updated
                loadAuditData()
            }
        )
    }

    // Modal Pilot Batch Import Dialog (Step 8 Controlled Batch Population)
    if (showPilotBatchDialog) {
        ControlledBatchImportDialog(
            exercises = exercises,
            exerciseRepository = exerciseRepository,
            onDismiss = { showPilotBatchDialog = false },
            onBatchCompleted = {
                loadAuditData()
            }
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF101416),
        border = BorderStroke(
            1.dp,
            if (highlight) FitTheme.colors.primary.copy(alpha = 0.5f) else Color(0xFF1B2228)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5A6E7B),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (highlight) FitTheme.colors.primary else Color.White
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = Color(0xFF8E9BA4)
            )
        }
    }
}

@Composable
private fun AuditExerciseCard(
    auditItem: ExerciseMediaAuditItem,
    onManageMedia: () -> Unit
) {
    val ex = auditItem.exercise

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0D1117),
        border = BorderStroke(
            1.dp,
            if (auditItem.mediaCount > 0) FitTheme.colors.primary.copy(alpha = 0.35f) else Color(0xFF1B2228)
        ),
        modifier = Modifier.fillMaxWidth().clickable { onManageMedia() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Name and Coverage Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ex.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ID: ${ex.id}",
                        fontSize = 11.sp,
                        color = Color(0xFF5A6E7B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (auditItem.mediaCount == 3) {
                        FitTheme.colors.primary.copy(alpha = 0.2f)
                    } else if (auditItem.mediaCount > 0) {
                        Color(0xFFFFA726).copy(alpha = 0.15f)
                    } else {
                        Color(0xFF161E24)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (auditItem.mediaCount == 3) {
                            FitTheme.colors.primary.copy(alpha = 0.6f)
                        } else if (auditItem.mediaCount > 0) {
                            Color(0xFFFFA726).copy(alpha = 0.5f)
                        } else {
                            Color(0xFF2B3A42)
                        }
                    )
                ) {
                    Text(
                        text = "MEDIA ${auditItem.mediaCount}/3",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (auditItem.mediaCount == 3) {
                            FitTheme.colors.primary
                        } else if (auditItem.mediaCount > 0) {
                            Color(0xFFFFA726)
                        } else {
                            Color(0xFF5A6E7B)
                        },
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Middle Row: Primary Muscle & Equipment Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (ex.primaryMuscle.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = FitTheme.colors.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = ex.primaryMuscle,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = FitTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (ex.category.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF161E24)
                    ) {
                        Text(
                            text = ex.category.replace('_', ' '),
                            fontSize = 10.sp,
                            color = Color(0xFF8E9BA4),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (ex.equipment.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF161E24)
                    ) {
                        Text(
                            text = ex.equipment.first(),
                            fontSize = 10.sp,
                            color = Color(0xFF8E9BA4),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Row: Media Slots Status and Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Slots Breakdown
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MediaSlotPill(label = "THUMB", isPopulated = auditItem.hasThumbnail)
                    MediaSlotPill(label = "ANIM", isPopulated = auditItem.hasAnimation)
                    MediaSlotPill(label = "VIDEO", isPopulated = auditItem.hasVideo)
                }

                // Action Link
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF161E24),
                    border = BorderStroke(1.dp, Color(0xFF2B3A42)),
                    modifier = Modifier.clickable { onManageMedia() }
                ) {
                    Text(
                        text = "Manage Media",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaSlotPill(
    label: String,
    isPopulated: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = if (isPopulated) "✓" else "○",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPopulated) FitTheme.colors.primary else Color(0xFF5A6E7B)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isPopulated) Color.White else Color(0xFF5A6E7B)
        )
    }
}

/**
 * Controlled Batch Import Dialog for Step 8 Pilot Media Population.
 * Strictly adheres to the Real Media Source Policy:
 * - Only project-owned or authorized assets are uploaded.
 * - In the absence of authorized assets, reports "No authorized media asset available."
 * - Does not expose raw Firebase URLs in the primary UI.
 */
@Composable
private fun ControlledBatchImportDialog(
    exercises: List<Exercise>,
    exerciseRepository: ExerciseRepository = remember { FirebaseExerciseRepository() },
    onDismiss: () -> Unit,
    onBatchCompleted: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isExecuting by remember { mutableStateOf(false) }
    var batchReport by remember { mutableStateOf<ControlledBatchReport?>(null) }

    val pilotItems = remember(exercises.toList()) {
        ExerciseMediaBulkImporter.createPilotBatchPlan(exercises)
    }

    Dialog(onDismissRequest = { if (!isExecuting) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F1418),
            border = BorderStroke(1.dp, Color(0xFF232D34)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CONTROLLED BATCH IMPORT",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Pilot Batch • 5–10 Exercises Maximum",
                            fontSize = 11.sp,
                            color = Color(0xFF8E9BA4)
                        )
                    }

                    CircularIconButton(
                        icon = FitnessIcons.Close,
                        contentDescription = "Close",
                        onClick = { if (!isExecuting) onDismiss() },
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Real Media Source Policy Notice
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF161E24),
                    border = BorderStroke(1.dp, Color(0xFF263540)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "REAL MEDIA SOURCE POLICY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitTheme.colors.primary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Only media created by the team or explicitly licensed is permitted. If no authorized local asset exists for an exercise, that slot remains EMPTY.",
                            fontSize = 11.sp,
                            color = Color(0xFFC0CDD6),
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pilot Target Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pilot Target: ${pilotItems.size} exercises",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = "Authorized Files: 0",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB300)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Results or Target List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    val report = batchReport
                    if (report != null) {
                        items(report.results) { item ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF090D0F),
                                border = BorderStroke(1.dp, if (item.isSuccess) Color(0xFF1B5E20) else Color(0xFF371E20)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.exerciseName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = if (item.isSuccess) "SUCCESS" else "FAILED",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.isSuccess) Color(0xFF4CAF50) else Color(0xFFFF5252)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Storage: ${item.storagePath} • Firestore: ${item.firestoreStatus}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF7E8F9B)
                                    )
                                    if (item.failureReason != null) {
                                        Text(
                                            text = item.failureReason,
                                            fontSize = 10.sp,
                                            color = Color(0xFFFF8A80)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        items(pilotItems) { planItem ->
                            val exName = exercises.find { it.id == planItem.exerciseId }?.name ?: planItem.exerciseId
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF090D0F),
                                border = BorderStroke(1.dp, Color(0xFF1B2228)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = exName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Target: ${planItem.mediaType} • exercise-media/${planItem.exerciseId}/video",
                                            fontSize = 10.sp,
                                            color = Color(0xFF5A6E7B)
                                        )
                                    }
                                    Text(
                                        text = "No File",
                                        fontSize = 10.sp,
                                        color = Color(0xFF8E9BA4)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1B2228),
                        modifier = Modifier.clickable { onDismiss() }
                    ) {
                        Text(
                            text = if (batchReport != null) "Close" else "Cancel",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isExecuting) Color(0xFF333D44) else FitTheme.colors.primary,
                        modifier = Modifier.clickable(enabled = !isExecuting) {
                            coroutineScope.launch {
                                isExecuting = true
                                val importer = ExerciseMediaBulkImporter(
                                    exerciseRepository = exerciseRepository,
                                    uploadRepository = CloudflareExerciseMediaUploadRepository(exerciseRepository)
                                )
                                val report = importer.executeControlledBatch(pilotItems)
                                batchReport = report
                                isExecuting = false
                                onBatchCompleted()
                            }
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            if (isExecuting) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (isExecuting) "Running..." else "Run Pilot Batch",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isExecuting) Color.White else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
