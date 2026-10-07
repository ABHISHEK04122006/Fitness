package com.example.fitness.feature.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.fitness.core.components.CircularIconButton
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.components.PrimaryButton
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.feature.exercise.data.FirebaseExerciseRepository
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseReference
import com.example.fitness.feature.program.data.FirebaseProgramRepository
import com.example.fitness.feature.program.model.Program
import com.example.fitness.feature.program.model.ProgramDay
import com.example.fitness.feature.program.model.ProgramType
import com.example.fitness.feature.program.model.ProgramWorkoutType
import com.example.fitness.feature.workout.model.ExperienceLevel
import kotlinx.coroutines.launch

/**
 * Multi-section editor modal for Creating and Editing a [Program].
 *
 * ENFORCED CONSTRAINTS:
 * - When [programToEdit] is non-null (Edit mode), the [Program.id] is STRICTLY READ-ONLY.
 * - When [programToEdit] is null (Create mode), the admin can specify custom ID or auto-generate slug.
 * - Programs reference exercises strictly by [ExerciseReference.exerciseId], loaded from the centralized library.
 * - Program days preserve canonical [dayNumber] ordering.
 * - Reordering supported with Move Up / Move Down controls.
 */
@Composable
fun AdminProgramEditorDialog(
    programToEdit: Program? = null,
    defaultGender: String = "Male",
    isSaving: Boolean = false,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onSave: (Program) -> Unit
) {
    val isEditMode = programToEdit != null
    val coroutineScope = rememberCoroutineScope()
    val exerciseRepository = remember { FirebaseExerciseRepository() }
    val programRepository = remember { FirebaseProgramRepository() }

    // Root Program Fields
    var id by remember { mutableStateOf(programToEdit?.id ?: "") }
    var name by remember { mutableStateOf(programToEdit?.name ?: "") }
    var gender by remember { mutableStateOf(programToEdit?.gender?.ifBlank { defaultGender } ?: defaultGender) }
    var description by remember { mutableStateOf(programToEdit?.description ?: "") }
    var selectedType by remember {
        mutableStateOf(programToEdit?.type ?: ProgramType.THIRTY_DAY.name)
    }
    var selectedExperienceLevel by remember {
        mutableStateOf(programToEdit?.experienceLevel ?: ExperienceLevel.BEGINNER.name)
    }
    var durationDaysStr by remember { mutableStateOf(programToEdit?.durationDays?.toString() ?: "30") }
    var trainingDaysPerWeekStr by remember { mutableStateOf(programToEdit?.trainingDaysPerWeek?.toString() ?: "5") }
    var isActive by remember { mutableStateOf(programToEdit?.isActive ?: true) }

    // Program Days State List
    val programDays = remember {
        mutableStateListOf<ProgramDay>().apply {
            addAll(programToEdit?.days?.sortedBy { it.dayNumber } ?: emptyList())
        }
    }

    // Active exercises for selection
    val availableExercises = remember { mutableStateListOf<Exercise>() }
    LaunchedEffect(Unit) {
        val active = exerciseRepository.getActiveExercises()
        availableExercises.clear()
        availableExercises.addAll(active)
    }

    // Sub-Dialog state: Day Editor
    var dayUnderEdit by remember { mutableStateOf<ProgramDay?>(null) }
    var isNewDayAdding by remember { mutableStateOf(false) }

    var localValidationError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEditMode) "Edit Program" else "Create Program",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isEditMode) "Update program schedule and exercise structure" else "Define a new structured workout program in Firestore",
                            fontSize = 11.sp,
                            color = Color(0xFF8E9BA4)
                        )
                    }

                    CircularIconButton(
                        icon = FitnessIcons.Close,
                        contentDescription = "Close",
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Error Banners
                val displayedError = localValidationError ?: errorMessage
                if (displayedError != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFF5252).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Text(
                            text = displayedError,
                            fontSize = 12.sp,
                            color = Color(0xFFFF5252),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                // ==========================================
                // 1. IDENTIFICATION
                // ==========================================
                FormSectionTitle(title = "1. PROGRAM IDENTIFICATION")

                Text(
                    text = if (isEditMode) "Program ID (Immutable)" else "Program ID (Leave blank to auto-generate slug)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isEditMode) Color(0xFF8E9BA4) else Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (isEditMode) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF090D10),
                        border = BorderStroke(1.dp, Color(0xFF1B2228)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = FitnessIcons.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFF5A6E7B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = id,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8E9BA4)
                            )
                        }
                    }
                    Text(
                        text = "Program IDs cannot be modified after creation because programs may be referenced by user progress and workout history.",
                        fontSize = 10.sp,
                        color = Color(0xFF5A6E7B),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    FormInputField(
                        value = id,
                        onValueChange = { id = it.lowercase().trim().replace(' ', '_') },
                        placeholder = "e.g., thirty_day_plan (optional)"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Program Name *",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                FormInputField(
                    value = name,
                    onValueChange = {
                        name = it
                        localValidationError = null
                    },
                    placeholder = "e.g., 30-Day Workout Plan"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Gender *",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Male", "Female", "Both").forEach { g ->
                        val isSelected = gender.equals(g, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) FitTheme.colors.primary else Color(0xFF161E24),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) FitTheme.colors.primary else Color(0xFF2B3A42)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { gender = g }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = g,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else Color(0xFFBAC5D0)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Description",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                FormInputField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "Overview of training focus, frequency, and methodology...",
                    singleLine = false,
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 2. CLASSIFICATION
                // ==========================================
                FormSectionTitle(title = "2. PROGRAM CLASSIFICATION")

                Text(
                    text = "Program Type",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ProgramType.entries) { pt ->
                        val isSelected = selectedType == pt.name
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) FitTheme.colors.primary else Color(0xFF161E24),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) FitTheme.colors.primary else Color(0xFF2B3A42)
                            ),
                            modifier = Modifier.clickable { selectedType = pt.name }
                        ) {
                            Text(
                                text = pt.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Target Experience Level",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ExperienceLevel.entries.forEach { exp ->
                        val isSelected = selectedExperienceLevel == exp.name
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) FitTheme.colors.primary else Color(0xFF161E24),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) FitTheme.colors.primary else Color(0xFF2B3A42)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedExperienceLevel = exp.name }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = exp.title.take(8),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 3. DURATION & SCHEDULE
                // ==========================================
                FormSectionTitle(title = "3. SCHEDULE & DURATION")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Duration (Days)", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        FormInputField(
                            value = durationDaysStr,
                            onValueChange = { durationDaysStr = it.filter { c -> c.isDigit() } },
                            placeholder = "30",
                            keyboardType = KeyboardType.Number
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Days / Week", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        FormInputField(
                            value = trainingDaysPerWeekStr,
                            onValueChange = { trainingDaysPerWeekStr = it.filter { c -> c.isDigit() } },
                            placeholder = "5",
                            keyboardType = KeyboardType.Number
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 4. PROGRAM DAYS MANAGEMENT
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FormSectionTitle(title = "4. PROGRAM DAYS (${programDays.size})")

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = FitTheme.colors.primary,
                        modifier = Modifier
                            .height(32.dp)
                            .clickable {
                                val nextDayNum = if (programDays.isEmpty()) 1 else (programDays.maxOf { it.dayNumber } + 1)
                                dayUnderEdit = ProgramDay(
                                    dayNumber = nextDayNum,
                                    title = "Day $nextDayNum Training",
                                    workoutType = ProgramWorkoutType.TRAINING.name
                                )
                                isNewDayAdding = true
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = FitnessIcons.Add,
                                contentDescription = "Add Day",
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add Day",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }

                if (programDays.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF101416),
                        border = BorderStroke(1.dp, Color(0xFF1B2228)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "No program days added yet. Click '+ Add Day' above.",
                                fontSize = 12.sp,
                                color = Color(0xFF5A6E7B)
                            )
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Ensure sorted by dayNumber for display
                        val sortedDays = programDays.sortedBy { it.dayNumber }
                        sortedDays.forEachIndexed { index, day ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF101416),
                                border = BorderStroke(1.dp, Color(0xFF1B2228)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = FitTheme.colors.primary.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "DAY ${day.dayNumber}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = FitTheme.colors.primary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = day.title.ifBlank { "Session ${day.dayNumber}" },
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = if (day.isRestDay) "Rest Day" else "${day.workoutType} • ${day.exercises.size} exercise${if (day.exercises.size == 1) "" else "s"}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF8E9BA4)
                                        )
                                    }

                                    // Day Action Buttons: Move Up, Move Down, Edit, Remove
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Move Up
                                        CircularIconButton(
                                            icon = FitnessIcons.ArrowUp,
                                            contentDescription = "Move Up",
                                            enabled = index > 0,
                                            onClick = {
                                                if (index > 0) {
                                                    val prevDay = sortedDays[index - 1]
                                                    val currDay = sortedDays[index]
                                                    // Swap dayNumbers
                                                    val updatedCurr = currDay.copy(dayNumber = prevDay.dayNumber)
                                                    val updatedPrev = prevDay.copy(dayNumber = currDay.dayNumber)
                                                    programDays.remove(currDay)
                                                    programDays.remove(prevDay)
                                                    programDays.add(updatedCurr)
                                                    programDays.add(updatedPrev)
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        )

                                        // Move Down
                                        CircularIconButton(
                                            icon = FitnessIcons.ArrowDown,
                                            contentDescription = "Move Down",
                                            enabled = index < sortedDays.size - 1,
                                            onClick = {
                                                if (index < sortedDays.size - 1) {
                                                    val nextDay = sortedDays[index + 1]
                                                    val currDay = sortedDays[index]
                                                    // Swap dayNumbers
                                                    val updatedCurr = currDay.copy(dayNumber = nextDay.dayNumber)
                                                    val updatedNext = nextDay.copy(dayNumber = currDay.dayNumber)
                                                    programDays.remove(currDay)
                                                    programDays.remove(nextDay)
                                                    programDays.add(updatedCurr)
                                                    programDays.add(updatedNext)
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        )

                                        // Edit
                                        CircularIconButton(
                                            icon = FitnessIcons.Edit,
                                            contentDescription = "Edit Day",
                                            onClick = {
                                                dayUnderEdit = day
                                                isNewDayAdding = false
                                            },
                                            modifier = Modifier.size(28.dp)
                                        )

                                        // Remove
                                        CircularIconButton(
                                            icon = FitnessIcons.Close,
                                            contentDescription = "Remove Day",
                                            onClick = { programDays.remove(day) },
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 5. STATUS
                // ==========================================
                FormSectionTitle(title = "5. STATUS")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isActive = !isActive },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isActive) FitTheme.colors.primary else Color(0xFF2B3A42),
                        modifier = Modifier.size(20.dp)
                    ) {
                        if (isActive) {
                            Icon(
                                imageVector = FitnessIcons.Check,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.padding(3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (isActive) "Active Program" else "Archived Program",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) FitTheme.colors.primary else Color(0xFFFF5252)
                        )
                        Text(
                            text = if (isActive) "Available to users for program enrollment." else "Hidden from athlete enrollment catalog; preserves existing user progress.",
                            fontSize = 10.sp,
                            color = Color(0xFF8E9BA4)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save and Cancel Buttons
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
                            .clickable(enabled = !isSaving) { onDismiss() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Cancel",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSaving) FitTheme.colors.primary.copy(alpha = 0.6f) else FitTheme.colors.primary,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clickable(enabled = !isSaving) {
                                val cleanName = name.trim()
                                if (cleanName.isBlank()) {
                                    localValidationError = "Program Name is required."
                                    return@clickable
                                }

                                val finalId = if (isEditMode) {
                                    id.trim()
                                } else {
                                    val custom = id.trim()
                                    if (custom.isNotBlank()) custom else programRepository.generateSlug(cleanName)
                                }

                                if (finalId.isBlank()) {
                                    localValidationError = "Program ID could not be generated."
                                    return@clickable
                                }

                                val program = Program(
                                    id = finalId,
                                    name = cleanName,
                                    slug = if (isEditMode) (programToEdit.slug.ifBlank { finalId }) else finalId,
                                    description = description.trim(),
                                    type = selectedType,
                                    experienceLevel = selectedExperienceLevel,
                                    durationDays = durationDaysStr.toIntOrNull() ?: 0,
                                    trainingDaysPerWeek = trainingDaysPerWeekStr.toIntOrNull() ?: 0,
                                    days = programDays.sortedBy { it.dayNumber },
                                    isActive = isActive,
                                    createdAt = programToEdit?.createdAt,
                                    createdBy = programToEdit?.createdBy ?: "",
                                    version = programToEdit?.version ?: 1,
                                    gender = gender.trim()
                                )

                                val validationResult = programRepository.validateProgram(program)
                                if (validationResult.isFailure) {
                                    localValidationError = validationResult.exceptionOrNull()?.message
                                    return@clickable
                                }

                                onSave(program)
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    color = Color.Black,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isEditMode) "Save Changes" else "Create Program",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // SUB-DIALOG: PROGRAM DAY EDITOR
    // ==========================================
    dayUnderEdit?.let { currentDay ->
        ProgramDayEditorSubDialog(
            initialDay = currentDay,
            availableExercises = availableExercises,
            onDismiss = { dayUnderEdit = null },
            onSave = { updatedDay ->
                if (isNewDayAdding) {
                    programDays.add(updatedDay)
                } else {
                    val index = programDays.indexOfFirst { it.dayNumber == currentDay.dayNumber }
                    if (index >= 0) {
                        programDays[index] = updatedDay
                    } else {
                        programDays.add(updatedDay)
                    }
                }
                dayUnderEdit = null
            }
        )
    }
}

/**
 * Dedicated sub-dialog for editing a single [ProgramDay] and its exercise prescriptions.
 */
@Composable
private fun ProgramDayEditorSubDialog(
    initialDay: ProgramDay,
    availableExercises: List<Exercise>,
    onDismiss: () -> Unit,
    onSave: (ProgramDay) -> Unit
) {
    var dayNumberStr by remember { mutableStateOf(initialDay.dayNumber.toString()) }
    var title by remember { mutableStateOf(initialDay.title) }
    var selectedWorkoutType by remember { mutableStateOf(initialDay.workoutType) }
    var isRestDay by remember { mutableStateOf(initialDay.isRestDay) }
    var notes by remember { mutableStateOf(initialDay.notes) }

    val focusList = remember { mutableStateListOf<String>().apply { addAll(initialDay.focus) } }
    var newFocusText by remember { mutableStateOf("") }

    val exercises = remember {
        mutableStateListOf<ExerciseReference>().apply { addAll(initialDay.exercises) }
    }

    var isExerciseSelectorOpen by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
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
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Program Day",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    CircularIconButton(
                        icon = FitnessIcons.Close,
                        contentDescription = "Close",
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (validationError != null) {
                    Text(
                        text = validationError ?: "",
                        fontSize = 12.sp,
                        color = Color(0xFFFF5252),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                // Day Number & Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.width(90.dp)) {
                        Text("Day Number *", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        FormInputField(
                            value = dayNumberStr,
                            onValueChange = { dayNumberStr = it.filter { c -> c.isDigit() } },
                            placeholder = "1",
                            keyboardType = KeyboardType.Number
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Session Title *", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        FormInputField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = "e.g. Chest & Triceps"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Workout Type Selector
                Text("Workout Type", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ProgramWorkoutType.entries) { wt ->
                        val isSelected = selectedWorkoutType == wt.name
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) FitTheme.colors.primary else Color(0xFF161E24),
                            border = BorderStroke(1.dp, if (isSelected) FitTheme.colors.primary else Color(0xFF2B3A42)),
                            modifier = Modifier.clickable {
                                selectedWorkoutType = wt.name
                                if (wt == ProgramWorkoutType.REST) {
                                    isRestDay = true
                                }
                            }
                        ) {
                            Text(
                                text = wt.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rest Day Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isRestDay = !isRestDay },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isRestDay) FitTheme.colors.primary else Color(0xFF2B3A42),
                        modifier = Modifier.size(18.dp)
                    ) {
                        if (isRestDay) {
                            Icon(
                                imageVector = FitnessIcons.Check,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.padding(3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Designated Rest / Recovery Session",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Focus Tags
                Text("Target Focus Areas", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                if (focusList.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        focusList.toList().forEachIndexed { index, item ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF161E24),
                                border = BorderStroke(1.dp, Color(0xFF2B3A42))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(item, fontSize = 11.sp, color = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "✕",
                                        fontSize = 9.sp,
                                        color = Color(0xFFFF5252),
                                        modifier = Modifier.clickable { focusList.removeAt(index) }
                                    )
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF101416),
                        border = BorderStroke(1.dp, Color(0xFF1B2228)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            if (newFocusText.isEmpty()) {
                                Text("Add focus (e.g. Chest, Triceps)", color = Color(0xFF5A6E7B), fontSize = 12.sp)
                            }
                            BasicTextField(
                                value = newFocusText,
                                onValueChange = { newFocusText = it },
                                textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                                cursorBrush = SolidColor(FitTheme.colors.primary),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = FitTheme.colors.primary.copy(alpha = 0.15f),
                        modifier = Modifier
                            .size(38.dp)
                            .clickable {
                                val t = newFocusText.trim()
                                if (t.isNotBlank() && !focusList.contains(t)) {
                                    focusList.add(t)
                                    newFocusText = ""
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(FitnessIcons.Add, "Add", tint = FitTheme.colors.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                Text("Coaching Notes", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                FormInputField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "Instructions or advice for this workout day..."
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // EXERCISES IN PROGRAM DAY
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXERCISES (${exercises.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5A6E7B)
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = FitTheme.colors.primary,
                        modifier = Modifier
                            .height(28.dp)
                            .clickable { isExerciseSelectorOpen = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(FitnessIcons.Add, "Add", tint = Color.Black, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Exercise", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (exercises.isEmpty()) {
                    Text(
                        text = if (isRestDay) "Rest day has no exercises." else "No exercises added. Click '+ Add Exercise' to pick from library.",
                        fontSize = 11.sp,
                        color = Color(0xFF5A6E7B)
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        exercises.forEachIndexed { exIndex, ref ->
                            val matchedExercise = availableExercises.find { it.id == ref.exerciseId }
                            val exName = matchedExercise?.name ?: ref.exerciseId

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF101416),
                                border = BorderStroke(1.dp, Color(0xFF1B2228)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${exIndex + 1}. $exName",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Move Up
                                            CircularIconButton(
                                                icon = FitnessIcons.ArrowUp,
                                                contentDescription = "Move Up",
                                                enabled = exIndex > 0,
                                                onClick = {
                                                    if (exIndex > 0) {
                                                        val prev = exercises[exIndex - 1]
                                                        exercises[exIndex - 1] = ref
                                                        exercises[exIndex] = prev
                                                    }
                                                },
                                                modifier = Modifier.size(24.dp)
                                            )

                                            // Move Down
                                            CircularIconButton(
                                                icon = FitnessIcons.ArrowDown,
                                                contentDescription = "Move Down",
                                                enabled = exIndex < exercises.size - 1,
                                                onClick = {
                                                    if (exIndex < exercises.size - 1) {
                                                        val next = exercises[exIndex + 1]
                                                        exercises[exIndex + 1] = ref
                                                        exercises[exIndex] = next
                                                    }
                                                },
                                                modifier = Modifier.size(24.dp)
                                            )

                                            // Remove
                                            CircularIconButton(
                                                icon = FitnessIcons.Close,
                                                contentDescription = "Remove",
                                                onClick = { exercises.removeAt(exIndex) },
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Prescription Inputs: Sets, Reps (String), Rest
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Sets", fontSize = 10.sp, color = Color(0xFF8E9BA4))
                                            FormInputField(
                                                value = ref.sets.toString(),
                                                onValueChange = { s ->
                                                    val num = s.filter { it.isDigit() }.toIntOrNull() ?: 1
                                                    exercises[exIndex] = ref.copy(sets = num)
                                                },
                                                placeholder = "3",
                                                keyboardType = KeyboardType.Number
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1.4f)) {
                                            Text("Reps (e.g. 8-12, AMRAP)", fontSize = 10.sp, color = Color(0xFF8E9BA4))
                                            FormInputField(
                                                value = ref.reps,
                                                onValueChange = { r ->
                                                    exercises[exIndex] = ref.copy(reps = r)
                                                },
                                                placeholder = "8-12"
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Rest (sec)", fontSize = 10.sp, color = Color(0xFF8E9BA4))
                                            FormInputField(
                                                value = ref.restSeconds.toString(),
                                                onValueChange = { r ->
                                                    val num = r.filter { it.isDigit() }.toIntOrNull() ?: 60
                                                    exercises[exIndex] = ref.copy(restSeconds = num)
                                                },
                                                placeholder = "90",
                                                keyboardType = KeyboardType.Number
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Day button
                PrimaryButton(
                    text = "Save Day",
                    onClick = {
                        val dNum = dayNumberStr.toIntOrNull() ?: 0
                        if (dNum <= 0) {
                            validationError = "Day Number must be greater than 0."
                            return@PrimaryButton
                        }
                        if (title.trim().isBlank()) {
                            validationError = "Day Title is required."
                            return@PrimaryButton
                        }

                        val day = ProgramDay(
                            dayNumber = dNum,
                            title = title.trim(),
                            workoutType = selectedWorkoutType,
                            focus = focusList.toList(),
                            isRestDay = isRestDay,
                            exercises = exercises.toList(),
                            notes = notes.trim()
                        )

                        onSave(day)
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                )
            }
        }
    }

    // ==========================================
    // EXERCISE SELECTOR DIALOG
    // ==========================================
    if (isExerciseSelectorOpen) {
        ExercisePickerModal(
            availableExercises = availableExercises,
            onDismiss = { isExerciseSelectorOpen = false },
            onExerciseSelected = { selected ->
                exercises.add(
                    ExerciseReference(
                        exerciseId = selected.id,
                        sets = selected.defaultSets.coerceAtLeast(1),
                        reps = selected.defaultReps.ifBlank { "8-12" },
                        restSeconds = selected.defaultRestSeconds.coerceAtLeast(0),
                        notes = ""
                    )
                )
                isExerciseSelectorOpen = false
            }
        )
    }
}

/**
 * Clean exercise selector dialog filtering centralized library by name, muscle, equipment, pattern.
 */
@Composable
private fun ExercisePickerModal(
    availableExercises: List<Exercise>,
    onDismiss: () -> Unit,
    onExerciseSelected: (Exercise) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(searchQuery, availableExercises) {
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) {
            availableExercises
        } else {
            availableExercises.filter { ex ->
                ex.name.lowercase().contains(q) ||
                        ex.primaryMuscle.lowercase().contains(q) ||
                        ex.equipment.any { it.lowercase().contains(q) } ||
                        ex.movementPattern.lowercase().contains(q)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0D1117),
            border = BorderStroke(1.dp, Color(0xFF1B2228)),
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select Exercise", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    CircularIconButton(
                        icon = FitnessIcons.Close,
                        contentDescription = "Close",
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF101416),
                    border = BorderStroke(1.dp, Color(0xFF1B2228)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(FitnessIcons.Search, "Search", tint = Color(0xFF5A6E7B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                            cursorBrush = SolidColor(FitTheme.colors.primary),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filtered) { ex ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF161E24),
                            border = BorderStroke(1.dp, Color(0xFF2B3A42)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onExerciseSelected(ex) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ex.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${ex.primaryMuscle} • ${ex.category.replace('_', ' ')}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF8E9BA4)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = FitTheme.colors.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "+ Select",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FitTheme.colors.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
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

@Composable
private fun FormSectionTitle(title: String) {
    Column {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5A6E7B),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun FormInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF101416),
        border = BorderStroke(1.dp, Color(0xFF1B2228)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = Color(0xFF5A6E7B),
                    fontSize = 13.sp
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 13.sp
                ),
                cursorBrush = SolidColor(FitTheme.colors.primary),
                singleLine = singleLine,
                minLines = minLines,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
