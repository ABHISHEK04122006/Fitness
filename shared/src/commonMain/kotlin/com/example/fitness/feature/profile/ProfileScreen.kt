package com.example.fitness.feature.profile

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.core.components.CircularIconButton
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.components.ProfileImageMemoryCache
import com.example.fitness.core.components.UserProfileAvatar
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.firebase.FitnessFirebase
import com.example.fitness.core.navigation.LocalNavigator
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.core.user.data.FirebaseUserRepository
import com.example.fitness.core.user.util.MobileNumberValidator
import com.example.fitness.core.user.util.ProfileCompletionCalculator
import com.example.fitness.feature.exercise.media.MediaUploadFile
import com.example.fitness.feature.exercise.media.rememberMediaFilePickerLauncher
import com.example.fitness.feature.exercise.model.ExerciseMediaType
import com.example.fitness.feature.notifications.data.WorkoutReminderRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val userRepository = remember { FirebaseUserRepository() }
    val reminderRepository = remember { WorkoutReminderRepository() }

    var reminderPref by remember { mutableStateOf(UserSession.workoutReminderPreference) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var showRemovePhotoConfirmation by remember { mutableStateOf(false) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var photoActionError by remember { mutableStateOf<String?>(null) }
    var isRemovingPhoto by remember { mutableStateOf(false) }
    var pendingCropFile by remember { mutableStateOf<MediaUploadFile?>(null) }
    var showCropDialog by remember { mutableStateOf(false) }

    // Workout Preferences State
    var selectedGenderPref by remember {
        mutableStateOf(
            UserSession.currentUserGender?.lowercase()?.let {
                if (it.contains("female")) "Female" else "Male"
            } ?: "Male"
        )
    }
    var selectedDaysPref by remember {
        mutableStateOf(
            if (UserSession.weeklyCommitment.contains("4")) "4 Days"
            else if (UserSession.weeklyCommitment.contains("6")) "6 Days"
            else "5 Days"
        )
    }
    var selectedLevelPref by remember {
        mutableStateOf(
            UserSession.experienceLevel.toUserFacingLevel()
        )
    }
    var isSavingPreferences by remember { mutableStateOf(false) }
    var preferencesSaveSuccess by remember { mutableStateOf(false) }
    var preferencesSaveError by remember { mutableStateOf<String?>(null) }
    var showGenderChangeConfirmation by remember { mutableStateOf(false) }

    val executeSavePreferences: () -> Unit = {
        coroutineScope.launch {
            isSavingPreferences = true
            preferencesSaveError = null
            preferencesSaveSuccess = false
            try {
                val uid = UserSession.currentUserId ?: Firebase.auth.currentUser?.uid.orEmpty()
                if (uid.isNotBlank()) {
                    val result = userRepository.updateWorkoutPreferences(
                        uid = uid,
                        gender = selectedGenderPref,
                        workoutDays = selectedDaysPref,
                        fitnessLevel = selectedLevelPref
                    )
                    if (result.isSuccess) {
                        preferencesSaveSuccess = true
                    } else {
                        val errMsg = result.exceptionOrNull()?.message
                        preferencesSaveError = errMsg?.takeIf { it.isNotBlank() } ?: "Failed to update preferences. Please try again."
                    }
                } else {
                    preferencesSaveError = "No authenticated user session."
                }
            } catch (e: Exception) {
                preferencesSaveError = e.message ?: "Failed to save preferences."
            } finally {
                isSavingPreferences = false
            }
        }
    }

    LaunchedEffect(Unit) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            UserSession.setAuthenticatedUser(currentUser)
            UserSession.updateRoleFromUser(currentUser)
            val latestProfile = userRepository.getUserProfile(currentUser.uid)
            if (latestProfile != null) {
                UserSession.setUserProfile(latestProfile)
                if (latestProfile.gender.isNotBlank()) {
                    selectedGenderPref = if (latestProfile.gender.lowercase().contains("female")) "Female" else "Male"
                }
                if (latestProfile.workoutDays.isNotBlank()) {
                    selectedDaysPref = if (latestProfile.workoutDays.contains("4")) "4 Days" else if (latestProfile.workoutDays.contains("6")) "6 Days" else "5 Days"
                }
                if (latestProfile.fitnessLevel.isNotBlank()) {
                    selectedLevelPref = com.example.fitness.feature.workout.model.ExperienceLevel.fromUserFacingOption(latestProfile.fitnessLevel).toUserFacingLevel()
                }
            }
            val pref = reminderRepository.getPreference(currentUser.uid)
            reminderPref = pref
            UserSession.workoutReminderPreference = pref
        }
    }

    val profile = UserSession.currentUserProfile
    val currentUid = UserSession.currentUserId ?: Firebase.auth.currentUser?.uid.orEmpty()
    val currentPhotoUrl = UserSession.currentUserProfileImageUrl ?: profile?.profileImageUrl
    val displayName = profile?.name?.takeIf { it.isNotBlank() }
        ?: UserSession.currentUserName?.takeIf { it.isNotBlank() }
        ?: Firebase.auth.currentUser?.displayName?.takeIf { it.isNotBlank() }
        ?: ""

    val displayEmail = profile?.email?.takeIf { it.isNotBlank() }
        ?: UserSession.currentUserEmail?.takeIf { it.isNotBlank() }
        ?: Firebase.auth.currentUser?.email?.takeIf { it.isNotBlank() }
        ?: ""

    val displayGender = profile?.gender?.takeIf { it.isNotBlank() }
        ?: UserSession.currentUserGender?.takeIf { it.isNotBlank() }
        ?: ""

    val displayMobile = UserSession.currentUserMobileNumber ?: profile?.mobileNumber.orEmpty()

    // Media file picker launcher for avatar selection
    val imagePickerLauncher = rememberMediaFilePickerLauncher(
        onFileSelected = { file ->
            println("[ProfileImageDebug] STEP 1 picker result received: ${file.fileName} (${file.sizeBytes} bytes)")
            if (currentUid.isBlank()) {
                photoActionError = "No authenticated session found"
                println("[ProfileImageDebug] FAILED AT STEP 1: No authenticated session found")
                return@rememberMediaFilePickerLauncher
            }
            if (file.bytes.isEmpty()) {
                photoActionError = "Failed to load selected photo. Please try another image."
                return@rememberMediaFilePickerLauncher
            }
            photoActionError = null
            pendingCropFile = file
            showCropDialog = true
        },
        onError = { err ->
            photoActionError = err
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 700.dp)
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

        // Top Bar: Profile Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Profile",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 1. PROFILE HEADER: User Avatar, Photo Actions, Real Name, Real Email
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF07090C),
            border = BorderStroke(1.dp, Color(0x33A8FF3E)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Avatar Container (Directly clickable to pick photo)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clickable(
                            enabled = !isUploadingPhoto && !isRemovingPhoto,
                            onClick = {
                                photoActionError = null
                                imagePickerLauncher.launch(ExerciseMediaType.IMAGE)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    UserProfileAvatar(
                        imageUrl = currentPhotoUrl,
                        name = displayName,
                        email = displayEmail,
                        size = 96.dp,
                        borderWidth = 2.5.dp,
                        borderColor = FitTheme.colors.primary,
                        onClick = {
                            photoActionError = null
                            imagePickerLauncher.launch(ExerciseMediaType.IMAGE)
                        }
                    )

                    if (isUploadingPhoto || isRemovingPhoto) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0x99000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = FitTheme.colors.primary,
                                strokeWidth = 3.dp
                            )
                        }
                    }
                }

                // Photo Action Buttons
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        onClick = {
                            photoActionError = null
                            imagePickerLauncher.launch(ExerciseMediaType.IMAGE)
                        },
                        enabled = !isUploadingPhoto && !isRemovingPhoto,
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF161E24),
                        border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.semantics { contentDescription = "Change Profile Picture" }
                    ) {
                        Text(
                            text = if (currentPhotoUrl.isNullOrBlank()) "Add Photo" else "Change Photo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FitTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    if (!currentPhotoUrl.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            onClick = { showRemovePhotoConfirmation = true },
                            enabled = !isUploadingPhoto && !isRemovingPhoto,
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1A1214),
                            border = BorderStroke(1.dp, Color(0xFF5A1E24)),
                            modifier = Modifier.semantics { contentDescription = "Remove Profile Picture" }
                        ) {
                            Text(
                                text = "Remove",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFF6B6B),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                if (photoActionError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = photoActionError ?: "",
                        fontSize = 11.sp,
                        color = Color(0xFFFF5252),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Real Name
                Text(
                    text = displayName.ifBlank { "Name not set" },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                // Real Email
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = displayEmail.ifBlank { "Email not available" },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF8E9BA4),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 3. PROFILE / PERSONAL DETAILS (Consolidated, No Duplicate Cards)
        Text(
            text = "Profile Details",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF07090C),
            border = BorderStroke(1.dp, Color(0xFF161E24)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                // Name Row
                ProfileInfoRow(
                    label = "Full Name",
                    value = displayName.ifBlank { "Not set" },
                    isPlaceholder = displayName.isBlank()
                )

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF141C22)))

                // Gender Row
                ProfileInfoRow(
                    label = "Gender",
                    value = displayGender.ifBlank { "Not set" },
                    isPlaceholder = displayGender.isBlank()
                )

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF141C22)))

                // Mobile Number Row (Strictly labeled "Mobile Number", no "Verified" label)
                ProfileInfoRow(
                    label = "Mobile Number",
                    value = MobileNumberValidator.formatForDisplay(displayMobile),
                    isPlaceholder = displayMobile.isBlank()
                )

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF141C22)))

                // Sign-in Method Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sign-in Method",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF161E24),
                        border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = UserSession.currentAuthProvider.ifBlank { "Email" },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 4. WORKOUT PREFERENCES
        Text(
            text = "Workout Preferences",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF07090C),
            border = BorderStroke(1.dp, Color(0xFF161E24)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                // --- Gender ---
                Text(
                    text = "Gender",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val genders = listOf("Male", "Female")
                    genders.forEach { genderOption ->
                        val isSelected = selectedGenderPref.equals(genderOption, ignoreCase = true)
                        val bg = if (isSelected) FitTheme.colors.primary else Color(0xFF161E24)
                        val textCol = if (isSelected) Color(0xFF000000) else Color(0xFF8E9BA4)
                        val border = if (isSelected) BorderStroke(1.dp, FitTheme.colors.primary) else BorderStroke(1.dp, Color(0xFF263238))

                        Surface(
                            onClick = {
                                selectedGenderPref = genderOption
                                preferencesSaveSuccess = false
                                preferencesSaveError = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = bg,
                            border = border,
                            modifier = Modifier
                                .semantics { contentDescription = "Gender $genderOption" }
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = genderOption,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textCol,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF141C22)))
                Spacer(modifier = Modifier.height(16.dp))

                // --- Workout Days ---
                Text(
                    text = "Workout Days",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val daysOptions = listOf("4 Days", "5 Days", "6 Days")
                    daysOptions.forEach { daysOption ->
                        val isSelected = selectedDaysPref.equals(daysOption, ignoreCase = true)
                        val bg = if (isSelected) FitTheme.colors.primary else Color(0xFF161E24)
                        val textCol = if (isSelected) Color(0xFF000000) else Color(0xFF8E9BA4)
                        val border = if (isSelected) BorderStroke(1.dp, FitTheme.colors.primary) else BorderStroke(1.dp, Color(0xFF263238))

                        Surface(
                            onClick = {
                                selectedDaysPref = daysOption
                                preferencesSaveSuccess = false
                                preferencesSaveError = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = bg,
                            border = border,
                            modifier = Modifier
                                .semantics { contentDescription = "Workout Days $daysOption" }
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = daysOption,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textCol,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF141C22)))
                Spacer(modifier = Modifier.height(16.dp))

                // --- Fitness Level ---
                Text(
                    text = "Fitness Level",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val levelOptions = listOf("No Experience", "Beginner", "Advanced", "Expert")
                    levelOptions.forEach { levelOption ->
                        val isSelected = selectedLevelPref.equals(levelOption, ignoreCase = true)
                        val bg = if (isSelected) FitTheme.colors.primary else Color(0xFF161E24)
                        val textCol = if (isSelected) Color(0xFF000000) else Color(0xFF8E9BA4)
                        val border = if (isSelected) BorderStroke(1.dp, FitTheme.colors.primary) else BorderStroke(1.dp, Color(0xFF263238))

                        Surface(
                            onClick = {
                                selectedLevelPref = levelOption
                                preferencesSaveSuccess = false
                                preferencesSaveError = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = bg,
                            border = border,
                            modifier = Modifier
                                .semantics { contentDescription = "Fitness Level $levelOption" }
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = levelOption,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textCol,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- Save Changes Button ---
                Surface(
                    onClick = {
                        val currentGenderNorm = UserSession.currentUserGender?.trim()?.lowercase().orEmpty()
                        val newGenderNorm = selectedGenderPref.trim().lowercase()
                        val genderChanged = currentGenderNorm.isNotBlank() && currentGenderNorm != newGenderNorm
                        if (genderChanged) {
                            showGenderChangeConfirmation = true
                        } else {
                            executeSavePreferences()
                        }
                    },
                    enabled = !isSavingPreferences,
                    shape = RoundedCornerShape(14.dp),
                    color = FitTheme.colors.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .semantics { contentDescription = "Save Changes" }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSavingPreferences) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "Save Changes",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }

                if (preferencesSaveSuccess) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Workout preferences updated successfully!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = FitTheme.colors.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (preferencesSaveError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = preferencesSaveError ?: "Failed to save preferences",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFF5252),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 4. PREFERENCES (User-Specific Workout Reminders with Responsive Switch & Natural Time Picker)
        Text(
            text = "Preferences",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF07090C),
            border = BorderStroke(1.dp, Color(0xFF161E24)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                // Workout Reminders Row (Responsive layout: text consumes remaining width, switch never clips)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = "Workout Reminders",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (reminderPref.workoutReminderEnabled) {
                                "Daily notification at ${reminderPref.formattedTime()}"
                            } else {
                                "Workout notifications disabled"
                            },
                            fontSize = 11.sp,
                            color = if (reminderPref.workoutReminderEnabled) Color(0xFF8E9BA4) else Color(0xFFFF6B6B)
                        )
                    }

                    Switch(
                        checked = reminderPref.workoutReminderEnabled,
                        onCheckedChange = { enabled ->
                            val updated = reminderPref.copy(workoutReminderEnabled = enabled)
                            reminderPref = updated
                            UserSession.workoutReminderPreference = updated
                            coroutineScope.launch {
                                reminderRepository.savePreference(updated)
                                if (enabled) {
                                    com.example.fitness.feature.notifications.reminder.WorkoutReminderScheduler.schedule(
                                        updated.workoutReminderHour,
                                        updated.workoutReminderMinute
                                    )
                                } else {
                                    com.example.fitness.feature.notifications.reminder.WorkoutReminderScheduler.cancel()
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF000000),
                            checkedTrackColor = FitTheme.colors.primary,
                            checkedBorderColor = FitTheme.colors.primary,
                            uncheckedThumbColor = Color(0xFFFF5252),
                            uncheckedTrackColor = Color(0xFF2A0F12),
                            uncheckedBorderColor = Color(0xFFFF5252)
                        ),
                        modifier = Modifier.semantics {
                            contentDescription = "Workout Reminder"
                        }
                    )
                }

                if (reminderPref.workoutReminderEnabled) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF141C22)))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimePicker = true }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp)
                        ) {
                            Text(
                                text = "Reminder Time",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tap to change preferred time",
                                fontSize = 11.sp,
                                color = Color(0xFF8E9BA4)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF161E24),
                            border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.clickable { showTimePicker = true }
                        ) {
                            Text(
                                text = reminderPref.formattedTime(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = FitTheme.colors.primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 5. ACCOUNT ACTIONS (Edit Profile & Logout)
        Text(
            text = "Account Actions",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Edit Profile Action Card
        Surface(
            onClick = { showEditProfileDialog = true },
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF07090C),
            border = BorderStroke(1.dp, Color(0xFF161E24)),
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Edit Profile" }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = FitnessIcons.Edit,
                        contentDescription = "Edit Profile",
                        tint = FitTheme.colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Edit Profile",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "Update name, gender, mobile number",
                            fontSize = 11.sp,
                            color = Color(0xFF8E9BA4)
                        )
                    }
                }
                Icon(
                    imageVector = FitnessIcons.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF8E9BA4),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Admin Portal Entry (Admin Role Only)
        if (UserSession.currentUserRole == com.example.fitness.core.auth.UserRole.ADMIN) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                onClick = { navigator.navigate(Screen.AdminDashboard) },
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF101416),
                border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = FitnessIcons.Settings,
                            contentDescription = "Admin Portal",
                            tint = FitTheme.colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Admin Dashboard",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Manage workouts, exercises & media",
                                fontSize = 11.sp,
                                color = Color(0xFF8E9BA4)
                            )
                        }
                    }
                    Icon(
                        imageVector = FitnessIcons.ChevronRight,
                        contentDescription = null,
                        tint = FitTheme.colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Account Actions: Log Out for Authenticated Users, Log In for Guest Users
        if (UserSession.isAuthenticated) {
            Surface(
                onClick = { showLogoutConfirmation = true },
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF14080A),
                border = BorderStroke(1.dp, Color(0xFF42151B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .semantics { contentDescription = "Logout" }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = FitnessIcons.Logout,
                        contentDescription = "Logout",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log Out",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF5252)
                    )
                }
            }
        } else {
            Surface(
                onClick = { navigator.navigate(Screen.Auth(isSignUp = false)) },
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF0C161D),
                border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Login" }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = FitTheme.colors.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = FitnessIcons.ArrowForward,
                                    contentDescription = "Log In",
                                    tint = FitTheme.colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Log In",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Sign in to save your profile & data",
                                fontSize = 11.sp,
                                color = Color(0xFF8E9BA4)
                            )
                        }
                    }
                    Icon(
                        imageVector = FitnessIcons.ChevronRight,
                        contentDescription = null,
                        tint = FitTheme.colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        }

        // Natural Time Picker Dialog (Direct control over Hour [1-12], Minute [00-59], and Period [AM/PM])
        if (showTimePicker) {
            val initialH = reminderPref.workoutReminderHour
            var selectedHour12 by remember {
                mutableStateOf(
                    when {
                        initialH == 0 -> 12
                        initialH > 12 -> initialH - 12
                        else -> initialH
                    }
                )
            }
            var selectedMinute by remember { mutableStateOf(reminderPref.workoutReminderMinute) }
            var isAm by remember { mutableStateOf(initialH < 12) }

            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                containerColor = Color(0xFF0D1217),
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "Set Reminder Time",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Choose your preferred workout time",
                            fontSize = 12.sp,
                            color = Color(0xFF8E9BA4)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val displayH = selectedHour12
                        val displayM = if (selectedMinute < 10) "0$selectedMinute" else "$selectedMinute"
                        val displayPeriod = if (isAm) "AM" else "PM"

                        // Prominent Live Time Display
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF141C22),
                            border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "$displayH : $displayM $displayPeriod",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = FitTheme.colors.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Hour Stepper Row: [-] [Hour] [+]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Hour:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.width(64.dp)
                            )
                            androidx.compose.material3.IconButton(
                                onClick = {
                                    selectedHour12 = if (selectedHour12 <= 1) 12 else selectedHour12 - 1
                                },
                                modifier = Modifier.semantics { contentDescription = "Decrease hour" }
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF161E24),
                                    border = BorderStroke(1.dp, Color(0xFF26333D)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("−", color = FitTheme.colors.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF161E24),
                                border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.3f)),
                                modifier = Modifier.width(48.dp)
                            ) {
                                Text(
                                    text = "$selectedHour12",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }

                            androidx.compose.material3.IconButton(
                                onClick = {
                                    selectedHour12 = if (selectedHour12 >= 12) 1 else selectedHour12 + 1
                                },
                                modifier = Modifier.semantics { contentDescription = "Increase hour" }
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF161E24),
                                    border = BorderStroke(1.dp, Color(0xFF26333D)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("+", color = FitTheme.colors.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Minute Stepper Row: [-] [Minute] [+] (Supports full 00-59 range)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Minute:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.width(64.dp)
                            )
                            androidx.compose.material3.IconButton(
                                onClick = {
                                    selectedMinute = if (selectedMinute <= 0) 59 else selectedMinute - 1
                                },
                                modifier = Modifier.semantics { contentDescription = "Decrease minute" }
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF161E24),
                                    border = BorderStroke(1.dp, Color(0xFF26333D)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("−", color = FitTheme.colors.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF161E24),
                                border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.3f)),
                                modifier = Modifier.width(48.dp)
                            ) {
                                Text(
                                    text = displayM,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }

                            androidx.compose.material3.IconButton(
                                onClick = {
                                    selectedMinute = (selectedMinute + 1) % 60
                                },
                                modifier = Modifier.semantics { contentDescription = "Increase minute" }
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF161E24),
                                    border = BorderStroke(1.dp, Color(0xFF26333D)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("+", color = FitTheme.colors.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Minute Presets / Quick Jump Row
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(0, 15, 30, 45).forEach { presetMin ->
                                val isSelected = selectedMinute == presetMin
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) FitTheme.colors.primary.copy(alpha = 0.2f) else Color(0xFF12161A),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) FitTheme.colors.primary else Color(0xFF202930)
                                    ),
                                    modifier = Modifier
                                        .clickable { selectedMinute = presetMin }
                                        .semantics { contentDescription = "Set minute to $presetMin" }
                                ) {
                                    Text(
                                        text = ":${if (presetMin < 10) "0$presetMin" else "$presetMin"}",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) FitTheme.colors.primary else Color(0xFF8E9BA4),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Period Selector Row: [ AM ] [ PM ]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Period:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.width(64.dp)
                            )

                            // AM Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isAm) FitTheme.colors.primary else Color(0xFF161E24),
                                border = BorderStroke(
                                    1.dp,
                                    if (isAm) FitTheme.colors.primary else Color(0xFF26333D)
                                ),
                                modifier = Modifier
                                    .clickable { isAm = true }
                                    .semantics { contentDescription = "Select AM" }
                            ) {
                                Text(
                                    text = "AM",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAm) Color.Black else Color.White,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // PM Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (!isAm) FitTheme.colors.primary else Color(0xFF161E24),
                                border = BorderStroke(
                                    1.dp,
                                    if (!isAm) FitTheme.colors.primary else Color(0xFF26333D)
                                ),
                                modifier = Modifier
                                    .clickable { isAm = false }
                                    .semantics { contentDescription = "Select PM" }
                            ) {
                                Text(
                                    text = "PM",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isAm) Color.Black else Color.White,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val safeH24 = if (isAm) {
                                if (selectedHour12 == 12) 0 else selectedHour12
                            } else {
                                if (selectedHour12 == 12) 12 else selectedHour12 + 12
                            }
                            val safeM = selectedMinute.coerceIn(0, 59)
                            val updated = reminderPref.copy(
                                workoutReminderHour = safeH24,
                                workoutReminderMinute = safeM
                            )
                            reminderPref = updated
                            UserSession.workoutReminderPreference = updated
                            coroutineScope.launch {
                                reminderRepository.savePreference(updated)
                                if (updated.workoutReminderEnabled) {
                                    com.example.fitness.feature.notifications.reminder.WorkoutReminderScheduler.reschedule(
                                        safeH24,
                                        safeM
                                    )
                                }
                            }
                            showTimePicker = false
                        },
                        modifier = Modifier.semantics { contentDescription = "Save" }
                    ) {
                        Text("Save", color = FitTheme.colors.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showTimePicker = false },
                        modifier = Modifier.semantics { contentDescription = "Cancel" }
                    ) {
                        Text("Cancel", color = Color(0xFF8E9BA4))
                    }
                }
            )
        }

        // Edit Profile Dialog (Name, Gender, Indian Mobile Number)
        if (showEditProfileDialog) {
            val currentProfile = UserSession.currentUserProfile
            var editFirstName by remember {
                mutableStateOf(currentProfile?.firstName ?: displayName.split(" ").firstOrNull().orEmpty())
            }
            var editLastName by remember {
                mutableStateOf(currentProfile?.lastName ?: displayName.split(" ").drop(1).joinToString(" ").orEmpty())
            }
            var editGender by remember {
                mutableStateOf(currentProfile?.gender?.takeIf { it.isNotBlank() } ?: displayGender.ifBlank { "Male" })
            }
            var editMobile by remember {
                mutableStateOf(currentProfile?.mobileNumber ?: displayMobile)
            }
            var editError by remember { mutableStateOf<String?>(null) }
            var isSavingProfile by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = {
                    if (!isSavingProfile) showEditProfileDialog = false
                },
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .imePadding(),
                containerColor = Color(0xFF0D1217),
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "Edit Profile",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (editError != null) {
                            Text(
                                text = editError ?: "",
                                color = Color(0xFFFF5252),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                        }

                        // First Name (Required)
                        Text(
                            text = "First Name *",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editFirstName,
                            onValueChange = {
                                editFirstName = it
                                editError = null
                            },
                            placeholder = { Text("First Name", color = Color(0xFF6B7280)) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF161E24),
                                unfocusedContainerColor = Color(0xFF161E24),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = FitTheme.colors.primary,
                                unfocusedIndicatorColor = Color(0xFF26333D)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "First Name Input" }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Last Name (Optional)
                        Text(
                            text = "Last Name (Optional)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editLastName,
                            onValueChange = { editLastName = it },
                            placeholder = { Text("Last Name", color = Color(0xFF6B7280)) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF161E24),
                                unfocusedContainerColor = Color(0xFF161E24),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = FitTheme.colors.primary,
                                unfocusedIndicatorColor = Color(0xFF26333D)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Last Name Input" }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Gender Selection
                        Text(
                            text = "Gender",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Male", "Female", "Other").forEach { genderOption ->
                                val isSelected = editGender.equals(genderOption, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) FitTheme.colors.primary else Color(0xFF161E24),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) FitTheme.colors.primary else Color(0xFF26333D)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { editGender = genderOption }
                                        .semantics { contentDescription = "Select gender $genderOption" }
                                ) {
                                    Text(
                                        text = genderOption,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.Black else Color.White,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Mobile Number (Indian Format)
                        Text(
                            text = "Mobile Number (10 digits)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editMobile,
                            onValueChange = {
                                editMobile = it
                                editError = null
                            },
                            placeholder = { Text("e.g. 9876543210 or +91 98765 43210", color = Color(0xFF6B7280)) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF161E24),
                                unfocusedContainerColor = Color(0xFF161E24),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = FitTheme.colors.primary,
                                unfocusedIndicatorColor = Color(0xFF26333D)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Mobile Number Input" }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val trimmedFirst = editFirstName.trim()
                            if (trimmedFirst.isBlank()) {
                                editError = "First name is required and cannot be blank"
                                return@TextButton
                            }
                            val trimmedLast = editLastName.trim()
                            val fullName = if (trimmedLast.isNotBlank()) "$trimmedFirst $trimmedLast" else trimmedFirst

                            val trimmedMobile = editMobile.trim()
                            if (trimmedMobile.isNotBlank() && !MobileNumberValidator.isValid(trimmedMobile)) {
                                editError = "Please enter a valid 10-digit Indian mobile number (e.g. 9876543210)"
                                return@TextButton
                            }

                            val uid = currentUid
                            if (uid.isBlank()) {
                                editError = "No authenticated user session found"
                                return@TextButton
                            }

                            isSavingProfile = true
                            coroutineScope.launch {
                                val result = userRepository.updateUserProfile(
                                    uid = uid,
                                    name = fullName,
                                    gender = editGender,
                                    mobileNumber = trimmedMobile
                                )
                                isSavingProfile = false
                                if (result.isSuccess) {
                                    showEditProfileDialog = false
                                } else {
                                    editError = result.exceptionOrNull()?.message
                                        ?: "Failed to update profile. Please try again."
                                }
                            }
                        },
                        enabled = !isSavingProfile,
                        modifier = Modifier.semantics { contentDescription = "Save" }
                    ) {
                        Text("Save", color = FitTheme.colors.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showEditProfileDialog = false },
                        enabled = !isSavingProfile,
                        modifier = Modifier.semantics { contentDescription = "Cancel" }
                    ) {
                        Text("Cancel", color = Color(0xFF8E9BA4))
                    }
                }
            )
        }

        // Profile Photo Cropping Dialog
        if (showCropDialog && pendingCropFile != null) {
            val fileToCrop = pendingCropFile!!
            ProfilePhotoCropDialog(
                imageBytes = fileToCrop.bytes,
                onConfirmCrop = { croppedBytes ->
                    showCropDialog = false
                    val original = pendingCropFile
                    pendingCropFile = null

                    val baseName = original?.fileName?.substringBeforeLast('.')?.ifBlank { "profile_photo" } ?: "profile_photo"
                    val croppedFile = MediaUploadFile(
                        fileName = "${baseName}_cropped.jpg",
                        mimeType = "image/jpeg",
                        sizeBytes = croppedBytes.size.toLong(),
                        bytes = croppedBytes
                    )

                    isUploadingPhoto = true
                    photoActionError = null
                    coroutineScope.launch {
                        val uploadResult = userRepository.updateUserProfilePicture(currentUid, croppedFile)
                        isUploadingPhoto = false
                        if (uploadResult.isFailure) {
                            val rawMsg = uploadResult.exceptionOrNull()?.message.orEmpty()
                            photoActionError = rawMsg.ifBlank { "Failed to upload photo. Please try again." }
                        }
                    }
                },
                onDismiss = {
                    showCropDialog = false
                    pendingCropFile = null
                }
            )
        }

        // Remove Profile Photo Confirmation Dialog
        if (showRemovePhotoConfirmation) {
            AlertDialog(
                onDismissRequest = {
                    if (!isRemovingPhoto) showRemovePhotoConfirmation = false
                },
                containerColor = Color(0xFF0D1217),
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "Remove Profile Photo",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to remove your profile photo? Your avatar will display your initials instead.",
                        fontSize = 13.sp,
                        color = Color(0xFF8E9BA4)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (currentUid.isBlank()) return@TextButton
                            isRemovingPhoto = true
                            coroutineScope.launch {
                                val res = userRepository.removeUserProfilePicture(currentUid)
                                isRemovingPhoto = false
                                showRemovePhotoConfirmation = false
                                if (res.isFailure) {
                                    photoActionError = res.exceptionOrNull()?.message ?: "Failed to remove photo"
                                }
                            }
                        },
                        enabled = !isRemovingPhoto,
                        modifier = Modifier.semantics { contentDescription = "Remove" }
                    ) {
                        Text("Remove", color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showRemovePhotoConfirmation = false },
                        enabled = !isRemovingPhoto,
                        modifier = Modifier.semantics { contentDescription = "Cancel" }
                    ) {
                        Text("Cancel", color = Color(0xFF8E9BA4))
                    }
                }
            )
        }

        // Log Out Confirmation Dialog (MANDATORY CONFIRMATION)
        if (showLogoutConfirmation) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirmation = false },
                containerColor = Color(0xFF0D1217),
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "Log Out",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to log out of your account?",
                        fontSize = 14.sp,
                        color = Color(0xFF8E9BA4)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutConfirmation = false
                            coroutineScope.launch {
                                // Flush pending workout writes before revoking auth
                                try {
                                    com.example.fitness.feature.progress.data.WorkoutProgressService.retryAllPendingSyncs()
                                    com.example.fitness.feature.progress.data.WorkoutProgressService.awaitPendingSyncs(timeoutMillis = 4000L)
                                } catch (_: Exception) {}

                                val uid = try { Firebase.auth.currentUser?.uid } catch (_: Exception) { null }
                                if (!uid.isNullOrBlank()) {
                                    try {
                                        val devices = FitnessFirebase.firestore.collection("users").document(uid).collection("devices").get()
                                        devices.documents.forEach { doc ->
                                            try { doc.reference.delete() } catch (_: Exception) {}
                                        }
                                    } catch (_: Exception) {}
                                }
                                try {
                                    com.example.fitness.feature.notifications.reminder.WorkoutReminderScheduler.cancel()
                                } catch (_: Exception) {}
                                try {
                                    com.example.fitness.core.auth.clearPlatformAuthState()
                                } catch (_: Exception) {}
                                try {
                                    Firebase.auth.signOut()
                                } catch (_: Exception) {}
                                com.example.fitness.feature.notifications.data.WorkoutNotificationService.clearCache()
                                com.example.fitness.feature.progress.data.WorkoutProgressService.clearCache()
                                ProfileImageMemoryCache.clear()
                                UserSession.clear()
                                navigator.resetRoot(Screen.Auth(isSignUp = false))
                            }
                        },
                        modifier = Modifier.semantics { contentDescription = "Log Out" }
                    ) {
                        Text("Log Out", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showLogoutConfirmation = false },
                        modifier = Modifier.semantics { contentDescription = "Cancel" }
                    ) {
                        Text("Cancel", color = Color(0xFF8E9BA4))
                    }
                }
            )
        }

        // Gender Change Incompatibility Confirmation Dialog
        if (showGenderChangeConfirmation) {
            AlertDialog(
                onDismissRequest = { showGenderChangeConfirmation = false },
                containerColor = Color(0xFF0D1217),
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "Update Workout Preferences",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Changing your gender will update your available workout programs.",
                        fontSize = 14.sp,
                        color = Color(0xFF8E9BA4)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showGenderChangeConfirmation = false
                            executeSavePreferences()
                        },
                        modifier = Modifier.semantics { contentDescription = "Continue" }
                    ) {
                        Text("Continue", color = FitTheme.colors.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showGenderChangeConfirmation = false },
                        modifier = Modifier.semantics { contentDescription = "Cancel" }
                    ) {
                        Text("Cancel", color = Color(0xFF8E9BA4))
                    }
                }
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    isPlaceholder: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (isPlaceholder) FontWeight.Normal else FontWeight.SemiBold,
            color = if (isPlaceholder) Color(0xFF6B7280) else Color(0xFFDFE6ED)
        )
    }
}
