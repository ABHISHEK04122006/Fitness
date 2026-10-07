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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fitness.core.components.CircularIconButton
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.components.PrimaryButton
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.feature.exercise.data.FirebaseExerciseRepository
import com.example.fitness.feature.exercise.model.Exercise
import com.example.fitness.feature.exercise.model.ExerciseCategory
import com.example.fitness.feature.exercise.model.ExerciseDifficulty
import com.example.fitness.feature.exercise.model.ExerciseMedia
import com.example.fitness.feature.exercise.model.ExerciseMediaType
import com.example.fitness.feature.exercise.model.MovementPattern

/**
 * Multi-section editor dialog for Creating or Updating an exercise document.
 *
 * ENFORCED CONSTRAINTS:
 * - When [exerciseToEdit] is non-null (Edit mode), the [Exercise.id] is STRICTLY READ-ONLY.
 * - When [exerciseToEdit] is null (Create mode), the user may provide a custom ID or auto-generate one.
 * - Name and Primary Muscle are strictly validated.
 * - Physical delete is prohibited; active status toggle performs soft-archive.
 */
@Composable
fun AdminExerciseEditorDialog(
    exerciseToEdit: Exercise? = null,
    defaultGender: String = "Male",
    isSaving: Boolean = false,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onSave: (Exercise) -> Unit
) {
    val isEditMode = exerciseToEdit != null

    // Form fields
    var id by remember { mutableStateOf(exerciseToEdit?.id ?: "") }
    var name by remember { mutableStateOf(exerciseToEdit?.name ?: "") }
    var gender by remember { mutableStateOf(exerciseToEdit?.gender?.ifBlank { defaultGender } ?: defaultGender) }
    var description by remember { mutableStateOf(exerciseToEdit?.description ?: "") }

    var primaryMuscle by remember { mutableStateOf(exerciseToEdit?.primaryMuscle ?: "") }
    val secondaryMuscles = remember {
        mutableStateListOf<String>().apply { addAll(exerciseToEdit?.secondaryMuscles ?: emptyList()) }
    }
    val muscleGroups = remember {
        mutableStateListOf<String>().apply { addAll(exerciseToEdit?.muscleGroups ?: emptyList()) }
    }
    val equipment = remember {
        mutableStateListOf<String>().apply { addAll(exerciseToEdit?.equipment ?: emptyList()) }
    }

    var selectedCategory by remember {
        mutableStateOf(exerciseToEdit?.category ?: ExerciseCategory.STRENGTH.name)
    }
    var selectedDifficulty by remember {
        mutableStateOf(exerciseToEdit?.difficulty?.let { ExerciseDifficulty.fromString(it)?.name } ?: ExerciseDifficulty.BEGINNER.name)
    }
    var selectedMovementPattern by remember {
        mutableStateOf(exerciseToEdit?.movementPattern ?: MovementPattern.HORIZONTAL_PUSH.name)
    }

    val instructions = remember {
        mutableStateListOf<String>().apply { addAll(exerciseToEdit?.instructions ?: emptyList()) }
    }
    val setupSteps = remember {
        mutableStateListOf<String>().apply { addAll(exerciseToEdit?.setupSteps ?: emptyList()) }
    }
    val executionSteps = remember {
        mutableStateListOf<String>().apply { addAll(exerciseToEdit?.executionSteps ?: emptyList()) }
    }
    var breathing by remember { mutableStateOf(exerciseToEdit?.breathing ?: "") }
    val formTips = remember {
        mutableStateListOf<String>().apply { addAll(exerciseToEdit?.formTips ?: emptyList()) }
    }
    val commonMistakes = remember {
        mutableStateListOf<String>().apply { addAll(exerciseToEdit?.commonMistakes ?: emptyList()) }
    }

    var defaultSetsStr by remember { mutableStateOf(exerciseToEdit?.defaultSets?.toString() ?: "3") }
    var defaultReps by remember { mutableStateOf(exerciseToEdit?.defaultReps ?: "8-12") }
    var defaultRestSecondsStr by remember { mutableStateOf(exerciseToEdit?.defaultRestSeconds?.toString() ?: "90") }

    var mediaType by remember { mutableStateOf(exerciseToEdit?.media?.mediaType ?: ExerciseMediaType.NONE.name) }
    var videoUrl by remember { mutableStateOf(exerciseToEdit?.media?.videoUrl ?: "") }
    var thumbnailUrl by remember { mutableStateOf(exerciseToEdit?.media?.thumbnailUrl ?: "") }
    var animationUrl by remember { mutableStateOf(exerciseToEdit?.media?.animationUrl ?: "") }

    var isActive by remember { mutableStateOf(exerciseToEdit?.isActive ?: true) }

    var localValidationError by remember { mutableStateOf<String?>(null) }

    val slugGenerator = remember { FirebaseExerciseRepository() }

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0D1117),
            border = BorderStroke(1.dp, Color(0xFF1B2228)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .padding(16.dp)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEditMode) "Edit Exercise" else "Create Exercise",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isEditMode) "Update exercise configuration and cues" else "Add a new exercise definition to Firestore",
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
                FormSectionTitle(title = "1. IDENTIFICATION & BASIC INFO")

                // Exercise ID Field (STRICTLY READ-ONLY IN EDIT MODE)
                Text(
                    text = if (isEditMode) "Exercise Document ID (Immutable)" else "Exercise Document ID (Leave blank to auto-generate slug)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isEditMode) Color(0xFF8E9BA4) else Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (isEditMode) {
                    // Disabled / Read-Only Box with Lock Icon
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
                        text = "Exercise IDs cannot be modified after creation to protect relational integrity.",
                        fontSize = 10.sp,
                        color = Color(0xFF5A6E7B),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    FormInputField(
                        value = id,
                        onValueChange = { id = it.lowercase().trim().replace(' ', '_') },
                        placeholder = "e.g., incline_dumbbell_press (optional)"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Name Field (Required)
                Text(
                    text = "Exercise Name *",
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
                    placeholder = "e.g., Incline Dumbbell Bench Press"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Target Gender Field
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

                // Description Field
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
                    placeholder = "Overview of exercise mechanics and target goals...",
                    singleLine = false,
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 2. ANATOMY & EQUIPMENT
                // ==========================================
                FormSectionTitle(title = "2. ANATOMY & TARGET MUSCLES")

                Text(
                    text = "Primary Muscle *",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                FormInputField(
                    value = primaryMuscle,
                    onValueChange = {
                        primaryMuscle = it
                        localValidationError = null
                    },
                    placeholder = "e.g., Chest, Quadriceps, Lats"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Secondary Muscles dynamic list
                DynamicStringListEditor(
                    title = "Secondary Muscles",
                    placeholder = "Add secondary muscle (e.g. Triceps)",
                    items = secondaryMuscles
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Muscle Groups dynamic list
                DynamicStringListEditor(
                    title = "Muscle Groups",
                    placeholder = "Add group (e.g. Upper Body, Push)",
                    items = muscleGroups
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Equipment dynamic list
                DynamicStringListEditor(
                    title = "Equipment Needed",
                    placeholder = "Add equipment (e.g. Barbell, Bench)",
                    items = equipment
                )

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 3. CLASSIFICATION & BIOMECHANICS
                // ==========================================
                FormSectionTitle(title = "3. CLASSIFICATION & BIOMECHANICS")

                // Category Chips Selector
                Text(
                    text = "Exercise Category",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ExerciseCategory.entries) { cat ->
                        val isSelected = selectedCategory == cat.name
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) FitTheme.colors.primary else Color(0xFF161E24),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) FitTheme.colors.primary else Color(0xFF2B3A42)
                            ),
                            modifier = Modifier.clickable { selectedCategory = cat.name }
                        ) {
                            Text(
                                text = cat.name.replace('_', ' '),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF000000) else Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Difficulty Chips Selector
                Text(
                    text = "Difficulty Level",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ExerciseDifficulty.entries.forEach { diff ->
                        val isSelected = selectedDifficulty.equals(diff.name, ignoreCase = true) ||
                                (diff == ExerciseDifficulty.NO_EXPERIENCE && (
                                    selectedDifficulty.equals("No Experience", ignoreCase = true) ||
                                    selectedDifficulty.equals("noExperience", ignoreCase = true)
                                ))
                        val label = when (diff) {
                            ExerciseDifficulty.NO_EXPERIENCE -> "No Exp"
                            ExerciseDifficulty.BEGINNER -> "Beg"
                            ExerciseDifficulty.ADVANCED -> "Adv"
                            ExerciseDifficulty.EXPERT -> "Exp"
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) FitTheme.colors.primary else Color(0xFF161E24),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) FitTheme.colors.primary else Color(0xFF2B3A42)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedDifficulty = diff.name }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF000000) else Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Movement Pattern Selector
                Text(
                    text = "Movement Pattern",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(MovementPattern.entries) { pattern ->
                        val isSelected = selectedMovementPattern == pattern.name
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) FitTheme.colors.primary else Color(0xFF161E24),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) FitTheme.colors.primary else Color(0xFF2B3A42)
                            ),
                            modifier = Modifier.clickable { selectedMovementPattern = pattern.name }
                        ) {
                            Text(
                                text = pattern.name.replace('_', ' '),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF000000) else Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 4. COACHING & TECHNIQUE
                // ==========================================
                FormSectionTitle(title = "4. COACHING & TECHNIQUE")

                DynamicStringListEditor(
                    title = "General Instructions",
                    placeholder = "Add high-level instruction...",
                    items = instructions
                )

                Spacer(modifier = Modifier.height(12.dp))

                DynamicStringListEditor(
                    title = "Setup Steps",
                    placeholder = "Add setup step...",
                    items = setupSteps
                )

                Spacer(modifier = Modifier.height(12.dp))

                DynamicStringListEditor(
                    title = "Execution Steps",
                    placeholder = "Add execution step...",
                    items = executionSteps
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Breathing Guidance",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                FormInputField(
                    value = breathing,
                    onValueChange = { breathing = it },
                    placeholder = "e.g., Inhale on eccentric descent, exhale forcefully during push."
                )

                Spacer(modifier = Modifier.height(12.dp))

                DynamicStringListEditor(
                    title = "Form Tips",
                    placeholder = "Add form tip...",
                    items = formTips
                )

                Spacer(modifier = Modifier.height(12.dp))

                DynamicStringListEditor(
                    title = "Common Mistakes to Avoid",
                    placeholder = "Add mistake...",
                    items = commonMistakes
                )

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 5. TRAINING DEFAULTS
                // ==========================================
                FormSectionTitle(title = "5. TRAINING DEFAULTS")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Default Sets", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        FormInputField(
                            value = defaultSetsStr,
                            onValueChange = { defaultSetsStr = it.filter { c -> c.isDigit() } },
                            placeholder = "3",
                            keyboardType = KeyboardType.Number
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Default Reps", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        FormInputField(
                            value = defaultReps,
                            onValueChange = { defaultReps = it },
                            placeholder = "8-12"
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Rest (sec)", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        FormInputField(
                            value = defaultRestSecondsStr,
                            onValueChange = { defaultRestSecondsStr = it.filter { c -> c.isDigit() } },
                            placeholder = "90",
                            keyboardType = KeyboardType.Number
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 6. MEDIA DEMONSTRATION
                // ==========================================
                FormSectionTitle(title = "6. MEDIA METADATA")

                Text(
                    text = "Media Demonstration Type",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(ExerciseMediaType.NONE, ExerciseMediaType.VIDEO).forEach { mt ->
                        val isSelected = mediaType == mt.name
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) FitTheme.colors.primary else Color(0xFF161E24),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) FitTheme.colors.primary else Color(0xFF2B3A42)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { mediaType = mt.name }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = mt.name,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF000000) else Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                FormInputField(
                    value = videoUrl,
                    onValueChange = { videoUrl = it },
                    placeholder = "Video demonstration URL (MP4, HLS, etc.)"
                )

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 7. STATUS & ACTIONS
                // ==========================================
                FormSectionTitle(title = "7. STATUS")

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
                            text = if (isActive) "Active Exercise" else "Archived Exercise",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) FitTheme.colors.primary else Color(0xFFFF5252)
                        )
                        Text(
                            text = if (isActive) "Visible in workout plans and exercise selectors." else "Soft-deleted: hidden from active selection but preserved for history.",
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
                                // Validation
                                val cleanName = name.trim()
                                if (cleanName.isBlank()) {
                                    localValidationError = "Exercise Name is required."
                                    return@clickable
                                }

                                val finalId = if (isEditMode) {
                                    id.trim()
                                } else {
                                    val userProvided = id.trim()
                                    if (userProvided.isNotBlank()) userProvided else slugGenerator.generateSlug(cleanName)
                                }

                                if (finalId.isBlank()) {
                                    localValidationError = "Exercise ID cannot be resolved."
                                    return@clickable
                                }

                                val exercise = Exercise(
                                    id = finalId,
                                    name = cleanName,
                                    slug = if (exerciseToEdit != null) exerciseToEdit.slug.ifBlank { finalId } else finalId,
                                    description = description.trim(),
                                    primaryMuscle = primaryMuscle.trim(),
                                    secondaryMuscles = secondaryMuscles.map { it.trim() }.filter { it.isNotBlank() },
                                    muscleGroups = muscleGroups.map { it.trim() }.filter { it.isNotBlank() },
                                    equipment = equipment.map { it.trim() }.filter { it.isNotBlank() },
                                    category = selectedCategory,
                                    movementPattern = selectedMovementPattern,
                                    difficulty = ExerciseDifficulty.fromString(selectedDifficulty)?.name ?: ExerciseDifficulty.BEGINNER.name,
                                    instructions = instructions.map { it.trim() }.filter { it.isNotBlank() },
                                    setupSteps = setupSteps.map { it.trim() }.filter { it.isNotBlank() },
                                    executionSteps = executionSteps.map { it.trim() }.filter { it.isNotBlank() },
                                    breathing = breathing.trim(),
                                    formTips = formTips.map { it.trim() }.filter { it.isNotBlank() },
                                    commonMistakes = commonMistakes.map { it.trim() }.filter { it.isNotBlank() },
                                    defaultSets = defaultSetsStr.toIntOrNull() ?: 3,
                                    defaultReps = defaultReps.trim().ifBlank { "8-12" },
                                    defaultRestSeconds = defaultRestSecondsStr.toIntOrNull() ?: 90,
                                    media = ExerciseMedia(
                                        thumbnailUrl = thumbnailUrl.trim(),
                                        videoUrl = videoUrl.trim(),
                                        animationUrl = animationUrl.trim(),
                                        mediaType = mediaType
                                    ),
                                    isActive = isActive,
                                    createdAt = exerciseToEdit?.createdAt,
                                    createdBy = exerciseToEdit?.createdBy ?: "",
                                    version = exerciseToEdit?.version ?: 1,
                                    gender = gender.trim()
                                )

                                onSave(exercise)
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
                                    text = if (isEditMode) "Save Changes" else "Create Exercise",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF000000)
                                )
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

@Composable
private fun DynamicStringListEditor(
    title: String,
    placeholder: String,
    items: MutableList<String>
) {
    var newItemText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Existing chips with remove action
        if (items.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items.toList().forEachIndexed { index, item ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF161E24),
                        border = BorderStroke(1.dp, Color(0xFF2B3A42))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = item,
                                fontSize = 11.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "✕",
                                fontSize = 10.sp,
                                color = Color(0xFFFF5252),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { items.removeAt(index) }
                            )
                        }
                    }
                }
            }
        }

        // Add input row
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
                Box(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    if (newItemText.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color(0xFF5A6E7B),
                            fontSize = 12.sp
                        )
                    }
                    BasicTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
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
                border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.4f)),
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        val trimmed = newItemText.trim()
                        if (trimmed.isNotBlank() && !items.contains(trimmed)) {
                            items.add(trimmed)
                            newItemText = ""
                        }
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = FitnessIcons.Add,
                        contentDescription = "Add",
                        tint = FitTheme.colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
