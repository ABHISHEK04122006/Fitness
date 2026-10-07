package com.example.fitness.feature.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.fitness.core.components.CircularIconButton
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.components.PrimaryButton
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.program.model.Program
import com.example.fitness.feature.program.model.ProgramDay
import com.example.fitness.feature.program.model.ProgramType

/**
 * Detailed modal dialog displaying complete Program information, classification,
 * schedule metrics, ordered program days, and resolved exercise prescriptions.
 */
@Composable
fun AdminProgramDetailsDialog(
    program: Program,
    exercisesMap: Map<String, Exercise> = emptyMap(),
    onDismiss: () -> Unit,
    onEdit: (Program) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0D1117),
            border = BorderStroke(1.dp, Color(0xFF1B2228)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Top Action Bar with Title and Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (program.isActive) FitTheme.colors.primary else Color(0xFFFF5252),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            if (program.type.isNotBlank()) {
                                val typeTitle = ProgramType.fromString(program.type)?.title ?: program.type.replace('_', ' ')
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF161E24)
                                ) {
                                    Text(
                                        text = typeTitle,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF8E9BA4),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = program.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    CircularIconButton(
                        icon = FitnessIcons.Close,
                        contentDescription = "Close",
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Overview & Metrics
                SectionHeader(title = "PROGRAM OVERVIEW")
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF101416),
                    border = BorderStroke(1.dp, Color(0xFF1B2228)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        DetailItemRow(label = "Program ID", value = program.id)
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailItemRow(label = "Slug", value = program.slug.ifBlank { "None" })
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailItemRow(
                            label = "Experience Level",
                            value = program.experienceLevel.replace('_', ' ').ifBlank { "All Levels" },
                            valueColor = FitTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailItemRow(
                            label = "Program Duration",
                            value = "${program.durationDays} Days"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailItemRow(
                            label = "Weekly Frequency",
                            value = "${program.trainingDaysPerWeek} Days / Week"
                        )

                        if (program.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Description",
                                fontSize = 11.sp,
                                color = Color(0xFF8E9BA4),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = program.description,
                                fontSize = 12.sp,
                                color = Color.White,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Program Structure & Days
                SectionHeader(title = "PROGRAM STRUCTURE (${program.days.size} DAYS)")

                if (program.days.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF101416),
                        border = BorderStroke(1.dp, Color(0xFF1B2228)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No program days configured yet. Click 'Edit Program' to add days.",
                                fontSize = 12.sp,
                                color = Color(0xFF8E9BA4)
                            )
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        program.days.sortedBy { it.dayNumber }.forEach { day ->
                            AdminProgramDayCard(
                                day = day,
                                exercisesMap = exercisesMap
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 3: Audit Trail
                SectionHeader(title = "AUDIT & VERSION CONTROL")
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF101416),
                    border = BorderStroke(1.dp, Color(0xFF1B2228)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        DetailItemRow(label = "Version", value = "v${program.version}")
                        Spacer(modifier = Modifier.height(6.dp))
                        DetailItemRow(
                            label = "Created At",
                            value = program.createdAt?.let { "$it ms" } ?: "Not available"
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        DetailItemRow(
                            label = "Created By",
                            value = program.createdBy.ifBlank { "System" }.take(16)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        DetailItemRow(
                            label = "Updated At",
                            value = program.updatedAt?.let { "$it ms" } ?: "Not available"
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        DetailItemRow(
                            label = "Updated By",
                            value = program.updatedBy.ifBlank { "System" }.take(16)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
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
                            .height(48.dp)
                            .clickable { onDismiss() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Close",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    PrimaryButton(
                        text = "Edit Program",
                        onClick = {
                            onDismiss()
                            onEdit(program)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF5A6E7B),
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun DetailItemRow(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF8E9BA4),
            modifier = Modifier.weight(0.45f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            modifier = Modifier.weight(0.55f),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Compact, responsive, and content-driven Day/Workout card for Admin Program view.
 * Eliminates artificial vertical space, letter-by-letter badge wrapping, and exercise truncation.
 *
 * Structure:
 * [DAY 1] Day 1 — Acceleration & Sprint   [TRAINING]
 *
 * [Acceleration & Sprint]
 * Prioritize technical execution and movement quality over speed or load.
 *
 * EXERCISES (4)
 *
 * 1. 10m acceleration
 *    1 set × 6 reps • 90s rest
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminProgramDayCard(
    day: ProgramDay,
    exercisesMap: Map<String, Exercise> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val isRest = day.isRestDay || day.workoutType.equals("REST", ignoreCase = true)
    val statusText = if (isRest) {
        "REST"
    } else if (day.workoutType.isNotBlank() &&
        !day.workoutType.equals("gym", ignoreCase = true) &&
        !day.workoutType.equals("workout", ignoreCase = true)
    ) {
        day.workoutType.uppercase()
    } else {
        "TRAINING"
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF101416),
        border = BorderStroke(
            1.dp,
            if (isRest) Color(0xFF202A30) else Color(0xFF1B2228)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // 1. Day Header: [DAY 1] Day 1 — Acceleration & Sprint   [TRAINING]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left: Day number pill + Title (words wrap naturally)
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = FitTheme.colors.primary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(top = 1.dp)
                    ) {
                        Text(
                            text = "DAY ${day.dayNumber}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitTheme.colors.primary,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = day.title.ifBlank { "Session ${day.dayNumber}" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 18.sp,
                        softWrap = true,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right: Status badge [TRAINING] / [REST] (NEVER breaks vertically into letters)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isRest) Color(0xFF1E2837) else FitTheme.colors.primary.copy(alpha = 0.12f),
                    border = BorderStroke(
                        1.dp,
                        if (isRest) Color(0xFF2E3D52) else FitTheme.colors.primary.copy(alpha = 0.35f)
                    )
                ) {
                    Text(
                        text = statusText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRest) Color(0xFF80D8FF) else FitTheme.colors.primary,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // 2. Category / Focus Chips (wraps cleanly whole-chip without breaking mid-word)
            if (day.focus.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    day.focus.forEach { focusItem ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF161E24),
                            border = BorderStroke(1.dp, Color(0xFF25313A))
                        ) {
                            Text(
                                text = focusItem,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF8E9BA4),
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // 3. Description / Notes
            if (day.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = day.notes,
                    fontSize = 12.sp,
                    color = Color(0xFF9AA7B2),
                    lineHeight = 17.sp,
                    softWrap = true
                )
            }

            // 4. Exercises or Dedicated Recovery Session
            if (isRest || day.exercises.isEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF131A20),
                    border = BorderStroke(1.dp, Color(0xFF1F2B33)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF80D8FF).copy(alpha = 0.15f),
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = FitnessIcons.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF80D8FF),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isRest) "Designated Rest / Recovery Session." else "No exercises configured for this session.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isRest) Color(0xFF80D8FF) else Color(0xFF5A6E7B)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "EXERCISES (${day.exercises.size})",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A6E7B),
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    day.exercises.forEachIndexed { exIndex, ref ->
                        val resolvedExercise = exercisesMap[ref.exerciseId]
                        val displayName = resolvedExercise?.name ?: ref.exerciseId.replace('_', ' ').replaceFirstChar { it.uppercase() }
                        val isExerciseArchived = resolvedExercise != null && !resolvedExercise.isActive

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Exercise number "1."
                            Text(
                                text = "${exIndex + 1}.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = FitTheme.colors.primary,
                                modifier = Modifier.widthIn(min = 18.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                // Exercise Name (wraps safely without mid-word break) + Archived badge
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = displayName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        lineHeight = 17.sp,
                                        softWrap = true,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )

                                    if (isExerciseArchived) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFFF5252).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Archived",
                                                fontSize = 9.sp,
                                                color = Color(0xFFFF5252),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                // Workout prescription: "1 set × 6 reps • 90s rest"
                                Text(
                                    text = formatExercisePrescription(ref.sets, ref.reps, ref.restSeconds),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF8E9BA4),
                                    lineHeight = 15.sp,
                                    softWrap = true
                                )

                                if (ref.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Note: ${ref.notes}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF5A6E7B),
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Normalizes workout prescription string to display cleanly:
 * E.g., "1 set × 6 reps • 90s rest", "4 sets × 5 • 90s rest", "3 sets × 10/leg • 90s rest"
 */
private fun formatExercisePrescription(sets: Int, reps: String, restSeconds: Int): String {
    val setsPart = if (sets == 1) "1 set" else "$sets sets"
    val trimmedReps = reps.trim()
    val repsPart = if (trimmedReps.isNotBlank()) {
        if (trimmedReps.startsWith("×") || trimmedReps.startsWith("x")) {
            " $trimmedReps"
        } else {
            " × $trimmedReps"
        }
    } else {
        ""
    }
    val restPart = if (restSeconds > 0) {
        " • ${restSeconds}s rest"
    } else {
        ""
    }
    return "$setsPart$repsPart$restPart"
}
