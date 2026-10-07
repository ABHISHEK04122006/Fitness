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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.core.auth.UserRole
import com.example.fitness.core.components.CircularIconButton
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.components.PrimaryButton
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.navigation.LocalNavigator
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.admin.data.AdminNotificationRepository
import com.example.fitness.feature.admin.model.AdminNotificationRecord
import com.example.fitness.feature.admin.model.AdminNotificationSendResult
import com.example.fitness.feature.notifications.formatRelativeTime
import kotlinx.coroutines.launch

/**
 * Focused Admin Notification Management Screen.
 * Provides rapid compose, multi-user targeting, and historical broadcast tracking.
 */
@Composable
fun AdminNotificationsScreen(
    modifier: Modifier = Modifier,
    repository: AdminNotificationRepository = remember { AdminNotificationRepository() }
) {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()

    // Screen-level RBAC enforcement via Firebase custom claims
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

    val historyList = remember { mutableStateListOf<AdminNotificationRecord>() }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var isComposerOpen by remember { mutableStateOf(false) }
    var successNotification by remember { mutableStateOf<AdminNotificationSendResult?>(null) }

    fun loadHistory() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val records = repository.getNotificationHistory()
                historyList.clear()
                historyList.addAll(records)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load notification history."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadHistory()
    }

    val filteredHistory = remember(searchQuery, historyList.toList()) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) {
            historyList.toList()
        } else {
            historyList.filter { record ->
                record.title.lowercase().contains(q) ||
                        record.message.lowercase().contains(q) ||
                        (record.targetName?.lowercase()?.contains(q) == true) ||
                        (record.targetNames.any { it.lowercase().contains(q) }) ||
                        (record.displayAudience.lowercase().contains(q))
            }
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

            // 1. Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularIconButton(
                        icon = FitnessIcons.ArrowBack,
                        contentDescription = "Back to Dashboard",
                        onClick = { navigator.navigate(Screen.AdminDashboard) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Notifications",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Send notifications to ATHGYM users",
                            fontSize = 12.sp,
                            color = Color(0xFF8E9BA4)
                        )
                    }
                }

                CircularIconButton(
                    icon = FitnessIcons.Replay,
                    contentDescription = "Refresh History",
                    onClick = { loadHistory() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Primary Action Button
            PrimaryButton(
                text = "+ Create Notification",
                onClick = { isComposerOpen = true },
                modifier = Modifier.fillMaxWidth()
            )

            // 3. Success Notification Banner
            if (successNotification != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0C1910),
                    border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "✅", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Notification sent successfully",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FitTheme.colors.primary
                                )
                                Text(
                                    text = successNotification?.message ?: "Delivered to target recipients.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB0BAC0)
                                )
                            }
                        }

                        Text(
                            text = "Dismiss",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FitTheme.colors.primary,
                            modifier = Modifier
                                .clickable { successNotification = null }
                                .padding(4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Section Header: Recent Notifications
            Text(
                text = "Recent Notifications",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Clean Search Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0D1117),
                border = BorderStroke(1.dp, Color(0xFF1B2228)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = FitnessIcons.Search,
                        contentDescription = "Search notifications",
                        tint = Color(0xFF5A6E7B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                        cursorBrush = SolidColor(FitTheme.colors.primary),
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = "Search notifications" },
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search by title, message, or recipient...",
                                    color = Color(0xFF5A6E7B),
                                    fontSize = 13.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        Text(
                            text = "Clear",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FitTheme.colors.primary,
                            modifier = Modifier
                                .clickable { searchQuery = "" }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Recent Notification Cards / Loading / Empty
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = FitTheme.colors.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage ?: "Failed to load history",
                            fontSize = 13.sp,
                            color = Color(0xFFFF5252),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            onClick = { loadHistory() },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF13181E),
                            border = BorderStroke(1.dp, Color(0xFF222B35))
                        ) {
                            Text(
                                text = "Retry",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                filteredHistory.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 50.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0D1217)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FitnessIcons.Bell,
                                contentDescription = null,
                                tint = Color(0xFF5A6E7B),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty())
                                "No matching notifications found."
                            else
                                "No notifications sent yet.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty())
                                "Try adjusting your search query."
                            else
                                "Tap '+ Create Notification' above to reach athletes.",
                            fontSize = 12.sp,
                            color = Color(0xFF8E9BA4),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredHistory, key = { it.id }) { item ->
                            AdminNotificationCard(item = item)
                        }
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }

    // Composer Modal Dialog
    if (isComposerOpen) {
        AdminNotificationComposerDialog(
            onDismiss = { isComposerOpen = false },
            onNotificationSent = { result ->
                successNotification = result
                loadHistory()
            },
            repository = repository
        )
    }
}

/**
 * Clean, compact, responsive notification card for recent admin broadcasts.
 */
@Composable
private fun AdminNotificationCard(item: AdminNotificationRecord) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0D1117),
        border = BorderStroke(1.dp, Color(0xFF1B2228)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Category Badge, Audience Badge, and Relative Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.categoryEmoji,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF161E24)
                    ) {
                        Text(
                            text = item.category.replace("_", " "),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8E9BA4),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = FitTheme.colors.primary.copy(alpha = 0.12f),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = item.displayAudience,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = formatRelativeTime(item.createdAt),
                    fontSize = 11.sp,
                    color = Color(0xFF6E7C87),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Message Body
            Text(
                text = item.message,
                fontSize = 12.sp,
                color = Color(0xFFB0BAC0),
                lineHeight = 17.sp
            )

            // Deep link action & status footer
            if (!item.actionType.isNullOrBlank() || item.status.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!item.actionType.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF101920),
                            border = BorderStroke(1.dp, Color(0xFF1F2B35))
                        ) {
                            Text(
                                text = "Action: ${item.actionType}${if (!item.actionId.isNullOrBlank()) " [${item.actionId}]" else ""}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64B5F6),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Text(
                        text = "Status: ${item.status}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (item.status == "DELIVERED") FitTheme.colors.primary else if (item.status == "PARTIAL") Color(0xFFFFB300) else Color(0xFFFF5252)
                    )
                }
            }
        }
    }
}
