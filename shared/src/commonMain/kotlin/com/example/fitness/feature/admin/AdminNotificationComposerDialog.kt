package com.example.fitness.feature.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fitness.core.auth.UserRole
import com.example.fitness.core.components.CircularIconButton
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.components.PrimaryButton
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.session.UserSession
import com.example.fitness.core.user.data.FirebaseUserRepository
import com.example.fitness.feature.admin.data.AdminNotificationRepository
import com.example.fitness.feature.admin.model.AdminActionType
import com.example.fitness.feature.admin.model.AdminAudienceType
import com.example.fitness.feature.admin.model.AdminNotificationRequest
import com.example.fitness.feature.admin.model.AdminNotificationSendResult
import com.example.fitness.feature.admin.model.AdminUserItem
import com.example.fitness.feature.notifications.model.NotificationItem
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch

/**
 * Modern, clean, and compact Admin Notification Composer Dialog with multi-user selection.
 * Enables selecting multiple ATHGYM users with search, selection persistence,
 * Select All / Clear All, and individual Firestore document dispatch per recipient.
 */
@Composable
fun AdminNotificationComposerDialog(
    onDismiss: () -> Unit,
    onNotificationSent: (AdminNotificationSendResult) -> Unit,
    repository: AdminNotificationRepository = remember { AdminNotificationRepository() },
    userRepository: FirebaseUserRepository = remember { FirebaseUserRepository() }
) {
    val coroutineScope = rememberCoroutineScope()

    // Multi-recipient state: keyed by user UID for O(1) lookups and guaranteed uniqueness
    val allUsers = remember { mutableStateListOf<AdminUserItem>() }
    val selectedUsers = remember { mutableStateMapOf<String, AdminUserItem>() }
    var userSearchQuery by remember { mutableStateOf("") }
    var isLoadingUsers by remember { mutableStateOf(true) }

    // Form fields
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(NotificationItem.TYPE_GENERAL) }
    var selectedAction by remember { mutableStateOf(AdminActionType.NONE) }
    var actionId by remember { mutableStateOf("") }

    // Execution state
    var isSending by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load registered users on composer open
    LaunchedEffect(Unit) {
        try {
            val users = userRepository.getAllAdminUsers()
            allUsers.clear()
            allUsers.addAll(users)
        } catch (_: Exception) {
            // Keep empty list gracefully
        } finally {
            isLoadingUsers = false
        }
    }

    // Filter users dynamically based on query without affecting selected state
    val filteredUsers = remember(userSearchQuery, allUsers.toList()) {
        val q = userSearchQuery.trim().lowercase()
        if (q.isBlank()) {
            allUsers.toList()
        } else {
            allUsers.filter {
                it.displayName.lowercase().contains(q) ||
                it.displayEmail.lowercase().contains(q)
            }
        }
    }

    val categories = listOf(
        NotificationItem.TYPE_GENERAL to "General 🔔",
        NotificationItem.TYPE_WORKOUT_REMINDER to "Workout 💪",
        "PROGRAM_UPDATE" to "Program 🎯",
        NotificationItem.TYPE_SYSTEM to "System ⚙️"
    )

    fun executeSend() {
        if (isSending) return
        validationError = null
        errorMessage = null

        val trimmedTitle = title.trim()
        val trimmedMessage = message.trim()

        // 1. Recipient validation
        if (selectedUsers.isEmpty()) {
            validationError = "Please select at least one recipient."
            return
        }

        // 2. Title validation
        if (trimmedTitle.isEmpty()) {
            validationError = "Notification title is required."
            return
        }
        if (trimmedTitle.length > 100) {
            validationError = "Title cannot exceed 100 characters."
            return
        }

        // 3. Message validation
        if (trimmedMessage.isEmpty()) {
            validationError = "Notification message body is required."
            return
        }
        if (trimmedMessage.length > 500) {
            validationError = "Message cannot exceed 500 characters."
            return
        }

        isSending = true

        val adminUid = Firebase.auth.currentUser?.uid ?: UserSession.currentUserId ?: "admin"
        val targetUids = selectedUsers.keys.toList()
        val targetNames = selectedUsers.values.map { it.displayName }
        val firstUser = selectedUsers.values.firstOrNull()

        val req = AdminNotificationRequest(
            title = trimmedTitle,
            message = trimmedMessage,
            category = selectedCategory,
            audience = if (targetUids.size > 1) AdminAudienceType.SINGLE_USER else AdminAudienceType.SINGLE_USER,
            targetUid = firstUser?.uid,
            targetName = firstUser?.displayName,
            targetEmail = firstUser?.email,
            targetUids = targetUids,
            targetNames = targetNames,
            actionType = selectedAction.actionTypeKey,
            actionId = actionId.trim().takeIf { it.isNotBlank() }
        )

        coroutineScope.launch {
            try {
                val result = repository.sendNotification(adminUid, req)
                if (result.success) {
                    onNotificationSent(result)
                    onDismiss()
                } else {
                    errorMessage = result.message ?: "Failed to send notification."
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "An unexpected network or permission error occurred."
            } finally {
                isSending = false
            }
        }
    }

    Dialog(
        onDismissRequest = { if (!isSending) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .imePadding()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF0B0E11),
                border = BorderStroke(1.dp, Color(0xFF1F2933)),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .fillMaxHeight(0.94f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Dialog Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Create Notification",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Send notifications to ATHGYM users",
                                fontSize = 12.sp,
                                color = Color(0xFF8E9BA4)
                            )
                        }

                        CircularIconButton(
                            icon = FitnessIcons.Close,
                            contentDescription = "Close Composer",
                            onClick = { if (!isSending) onDismiss() }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scrollable Form Body
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // ==========================================
                        // SECTION A: RECIPIENTS (MULTI-USER SELECTOR)
                        // ==========================================
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Recipients",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Selected count badge
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedUsers.isNotEmpty()) FitTheme.colors.primary.copy(alpha = 0.15f) else Color(0xFF161E24),
                                    border = BorderStroke(
                                        1.dp,
                                        if (selectedUsers.isNotEmpty()) FitTheme.colors.primary else Color(0xFF26333D)
                                    )
                                ) {
                                    Text(
                                        text = "Selected: ${selectedUsers.size} ${if (selectedUsers.size == 1) "user" else "users"}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (selectedUsers.isNotEmpty()) FitTheme.colors.primary else Color(0xFF8E9BA4),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                // Select All button
                                Surface(
                                    onClick = {
                                        filteredUsers.forEach { user ->
                                            selectedUsers[user.uid] = user
                                        }
                                        validationError = null
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF161E24),
                                    border = BorderStroke(1.dp, Color(0xFF26333D))
                                ) {
                                    Text(
                                        text = "Select All",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                // Clear All button
                                Surface(
                                    onClick = { selectedUsers.clear() },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF161E24),
                                    border = BorderStroke(1.dp, Color(0xFF26333D))
                                ) {
                                    Text(
                                        text = "Clear All",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF8E9BA4),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // User Search Input
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF13181E),
                            border = BorderStroke(1.dp, Color(0xFF222B35)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = FitnessIcons.Search,
                                    contentDescription = null,
                                    tint = Color(0xFF5A6E7B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                BasicTextField(
                                    value = userSearchQuery,
                                    onValueChange = { userSearchQuery = it },
                                    singleLine = true,
                                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                                    cursorBrush = SolidColor(FitTheme.colors.primary),
                                    modifier = Modifier
                                        .weight(1f)
                                        .semantics { contentDescription = "Search users input" },
                                    decorationBox = { innerTextField ->
                                        if (userSearchQuery.isEmpty()) {
                                            Text(
                                                text = "Search users...",
                                                color = Color(0xFF5A6E7B),
                                                fontSize = 13.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                                if (userSearchQuery.isNotEmpty()) {
                                    Text(
                                        text = "Clear",
                                        fontSize = 11.sp,
                                        color = FitTheme.colors.primary,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .clickable { userSearchQuery = "" }
                                            .padding(4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Scrollable User Selection Area (Bounded height to ensure smooth responsiveness)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F1419),
                            border = BorderStroke(
                                1.dp,
                                if (validationError != null && selectedUsers.isEmpty()) Color(0xFFFF5252) else Color(0xFF1F2933)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                        ) {
                            if (isLoadingUsers) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = FitTheme.colors.primary,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            } else if (filteredUsers.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (userSearchQuery.isBlank()) "No users registered in system." else "No users match '$userSearchQuery'",
                                        color = Color(0xFF6E7C87),
                                        fontSize = 12.sp
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(filteredUsers, key = { it.uid }) { user ->
                                        val isSelected = selectedUsers.containsKey(user.uid)
                                        Surface(
                                            onClick = {
                                                if (isSelected) {
                                                    selectedUsers.remove(user.uid)
                                                } else {
                                                    selectedUsers[user.uid] = user
                                                    validationError = null
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) FitTheme.colors.primary.copy(alpha = 0.12f) else Color(0xFF13181E),
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSelected) FitTheme.colors.primary.copy(alpha = 0.6f) else Color(0xFF1D262F)
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Checkbox indicator
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (isSelected) FitTheme.colors.primary else Color.Transparent)
                                                        .border(
                                                            width = 1.5.dp,
                                                            color = if (isSelected) FitTheme.colors.primary else Color(0xFF4A5568),
                                                            shape = RoundedCornerShape(6.dp)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (isSelected) {
                                                        Icon(
                                                            imageVector = FitnessIcons.Check,
                                                            contentDescription = "Selected",
                                                            tint = Color.Black,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                // User Initial Avatar
                                                Box(
                                                    modifier = Modifier
                                                        .size(30.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) FitTheme.colors.primary.copy(alpha = 0.25f) else Color(0xFF1F2933)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = user.displayName.take(1).uppercase(),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) FitTheme.colors.primary else Color.White
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                // Display Name and Secondary Info (Email / Role) - No technical UIDs
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = user.displayName,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color.White,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = user.displayEmail,
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF8E9BA4),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                if (user.role == UserRole.ADMIN) {
                                                    Text(
                                                        text = "ADMIN",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = FitTheme.colors.primary,
                                                        modifier = Modifier.padding(start = 6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ==========================================
                        // SECTION B: TITLE
                        // ==========================================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Title",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "${title.length}/100",
                                fontSize = 11.sp,
                                color = if (title.length > 100) Color(0xFFFF5252) else Color(0xFF6E7C87)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF13181E),
                            border = BorderStroke(
                                1.dp,
                                if (validationError != null && title.trim().isEmpty()) Color(0xFFFF5252) else Color(0xFF222B35)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BasicTextField(
                                value = title,
                                onValueChange = { if (it.length <= 120) title = it },
                                singleLine = true,
                                textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                                cursorBrush = SolidColor(FitTheme.colors.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                    .semantics { contentDescription = "Notification title input" },
                                decorationBox = { innerTextField ->
                                    if (title.isEmpty()) {
                                        Text(
                                            text = "e.g. Important Update",
                                            color = Color(0xFF5A6E7B),
                                            fontSize = 14.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ==========================================
                        // SECTION C: MESSAGE
                        // ==========================================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Message",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "${message.length}/500",
                                fontSize = 11.sp,
                                color = if (message.length > 500) Color(0xFFFF5252) else Color(0xFF6E7C87)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF13181E),
                            border = BorderStroke(
                                1.dp,
                                if (validationError != null && message.trim().isEmpty()) Color(0xFFFF5252) else Color(0xFF222B35)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BasicTextField(
                                value = message,
                                onValueChange = { if (it.length <= 550) message = it },
                                textStyle = TextStyle(color = Color.White, fontSize = 14.sp, lineHeight = 20.sp),
                                cursorBrush = SolidColor(FitTheme.colors.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(86.dp)
                                    .padding(14.dp)
                                    .semantics { contentDescription = "Notification message input" },
                                decorationBox = { innerTextField ->
                                    if (message.isEmpty()) {
                                        Text(
                                            text = "e.g. Please check today's workout schedule.",
                                            color = Color(0xFF5A6E7B),
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ==========================================
                        // SECTION D: NOTIFICATION TYPE
                        // ==========================================
                        Text(
                            text = "Notification Type",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { (catKey, catLabel) ->
                                val isSelected = selectedCategory == catKey
                                Surface(
                                    onClick = { selectedCategory = catKey },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) FitTheme.colors.primary.copy(alpha = 0.15f) else Color(0xFF13181E),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) FitTheme.colors.primary else Color(0xFF222B35)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = catLabel,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) FitTheme.colors.primary else Color(0xFF8E9BA4),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ==========================================
                        // SECTION E: ACTION / DEEP LINK (OPTIONAL)
                        // ==========================================
                        Text(
                            text = "Action / Deep Link (Optional)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AdminActionType.entries.forEach { action ->
                                val isSelected = selectedAction == action
                                Surface(
                                    onClick = { selectedAction = action },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) FitTheme.colors.primary.copy(alpha = 0.15f) else Color(0xFF13181E),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) FitTheme.colors.primary else Color(0xFF222B35)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = action.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) FitTheme.colors.primary else Color(0xFF8E9BA4),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }

                        // Action ID input if Workout or Program
                        if (selectedAction == AdminActionType.WORKOUT || selectedAction == AdminActionType.PROGRAM) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF13181E),
                                border = BorderStroke(1.dp, Color(0xFF222B35)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                BasicTextField(
                                    value = actionId,
                                    onValueChange = { actionId = it },
                                    singleLine = true,
                                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                                    cursorBrush = SolidColor(FitTheme.colors.primary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    decorationBox = { innerTextField ->
                                        if (actionId.isEmpty()) {
                                            Text(
                                                text = if (selectedAction == AdminActionType.WORKOUT)
                                                    "Workout ID (e.g. chest_triceps, upper_body)"
                                                else
                                                    "Program ID (e.g. hypertrophy, strength)",
                                                color = Color(0xFF5A6E7B),
                                                fontSize = 13.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }

                            // Suggestions
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val suggestions = if (selectedAction == AdminActionType.WORKOUT) {
                                    listOf("chest_triceps", "upper_body", "legs_core")
                                } else {
                                    listOf("hypertrophy", "strength", "athlete")
                                }
                                suggestions.forEach { sug ->
                                    Surface(
                                        onClick = { actionId = sug },
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF0F151A),
                                        border = BorderStroke(1.dp, Color(0xFF26333D))
                                    ) {
                                        Text(
                                            text = sug,
                                            fontSize = 10.sp,
                                            color = Color(0xFF8E9BA4),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Validation / Error Messages
                        if (validationError != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "⚠️ $validationError",
                                fontSize = 12.sp,
                                color = Color(0xFFFF5252),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "❌ $errorMessage",
                                fontSize = 12.sp,
                                color = Color(0xFFFF5252),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Dialog Actions Footer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            onClick = { if (!isSending) onDismiss() },
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF13181E),
                            border = BorderStroke(1.dp, Color(0xFF222B35)),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
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
                            text = if (isSending) "Sending..." else "Send Notification",
                            onClick = { executeSend() },
                            enabled = !isSending,
                            isLoading = isSending,
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp)
                        )
                    }
                }
            }
        }
    }
}
