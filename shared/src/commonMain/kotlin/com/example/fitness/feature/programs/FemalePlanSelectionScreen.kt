package com.example.fitness.feature.programs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
import com.example.fitness.feature.programs.data.ProgramRegistry
import com.example.fitness.feature.programs.model.WorkoutProgram
import org.jetbrains.compose.resources.painterResource

/**
 * Dedicated female workout plan selection screen.
 * Displays all 9 authoritative female programs from AthGym_9_Female_30_Day_Workout_Programs.pdf
 * in large, vertically scrollable cards with dark gradient overlays, female imagery,
 * and clinician clearance safety handling for postpartum recovery.
 */
@Composable
fun FemalePlanSelectionScreen(
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current
    val programs: List<WorkoutProgram> = remember { ProgramRegistry.getFemalePrograms() }

    var visible by remember { mutableStateOf(false) }
    var pendingPostpartumProgramId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        visible = true
    }

    BackHandler(enabled = true) {
        if (!navigator.pop()) {
            navigator.switchTab(NavigationTab.HOME)
        }
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
                .navigationBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularIconButton(
                    icon = FitnessIcons.ArrowBack,
                    onClick = {
                        if (!navigator.pop()) {
                            navigator.switchTab(NavigationTab.HOME)
                        }
                    }
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Workout Plans",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FitTheme.colors.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Select your workout plan",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Description / Subtitle
            Text(
                text = "Authoritative 30-day curriculum with structured progression: Foundation → Build → Progress → Challenge → Consolidation.",
                fontSize = 13.5.sp,
                color = Color(0xFF9E9E9E),
                lineHeight = 19.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable Program Cards
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(
                        items = programs,
                        key = { _, program -> program.id }
                    ) { index, program ->
                        FemaleProgramCard(
                            program = program,
                            onClick = {
                                if (program.id == "female_postpartum_recovery") {
                                    pendingPostpartumProgramId = program.id
                                } else {
                                    navigator.navigate(Screen.ProgramDetail(programId = program.id))
                                }
                            }
                        )
                    }

                    // Safe bottom spacer for navigation insets
                    item {
                        Spacer(modifier = Modifier.height(36.dp))
                    }
                }
            }
        }

        // Postpartum Medical Clearance & Safety Notice Dialog
        if (pendingPostpartumProgramId != null) {
            PostpartumSafetyNoticeDialog(
                onConfirm = {
                    val targetId = pendingPostpartumProgramId
                    pendingPostpartumProgramId = null
                    if (targetId != null) {
                        navigator.navigate(Screen.ProgramDetail(programId = targetId))
                    }
                },
                onDismiss = {
                    pendingPostpartumProgramId = null
                }
            )
        }
    }
}

/**
 * Large vertically scrollable card for a female workout plan.
 */
@Composable
private fun FemaleProgramCard(
    program: WorkoutProgram,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp)
            .scale(cardScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0xFF262626)),
        color = Color(0xFF141414)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Female Background Image
            Image(
                painter = painterResource(program.imageRes),
                contentDescription = program.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dark Multi-Stop Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x20000000),
                                Color(0x60000000),
                                Color(0xD00A0A0A),
                                Color(0xF805080E)
                            )
                        )
                    )
            )

            // Card Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Tag Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0x33000000),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, FitTheme.colors.primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "30-DAY PROGRAM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitTheme.colors.primary,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0x66000000),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "30 Workouts",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE0E0E0),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Bottom Title & Goal
                Column {
                    Text(
                        text = program.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = program.goal,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFB0B0B0),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

/**
 * Informational safety dialog for Postpartum Recovery program.
 * Directly preserves authoritative medical guidance from PDF page 19.
 */
@Composable
private fun PostpartumSafetyNoticeDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF161922),
            border = BorderStroke(1.dp, Color(0xFF2E3345)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header with warning indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF332014), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FitnessIcons.Fire,
                            contentDescription = "Notice",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Clinical Safety Notice",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Authoritative safety guidance from PDF Page 19
                Text(
                    text = "Postpartum Recovery is a specialized program designed for gradual rehabilitation.",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FitTheme.colors.primary,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Postpartum implementation requires appropriate clinician clearance and should adapt to postpartum stage, delivery type, symptoms and recovery.\n\nStop immediately and seek professional guidance for concerning pain, bleeding, pelvic heaviness/bulging, wound issues, dizziness or other unexpected symptoms.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFCCCCCC),
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clickable { onDismiss() },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF242836)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    PrimaryButton(
                        text = "I Have Clearance",
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp)
                    )
                }
            }
        }
    }
}
