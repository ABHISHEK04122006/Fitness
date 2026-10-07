package com.example.fitness.feature.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.core.components.CircularIconButton
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.navigation.LocalNavigator
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.notifications.data.FirebaseNotificationRepository
import com.example.fitness.feature.notifications.model.NotificationItem
import com.example.fitness.getCurrentEpochMillis
import com.example.fitness.getLocalDateString
import com.example.fitness.getRelativeDateDisplay
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch

internal fun formatRelativeTime(epochMillis: Long): String {
    val now = getCurrentEpochMillis()
    val diff = now - epochMillis
    if (diff < 0L) return "Just now"
    val seconds = diff / 1000L
    val minutes = seconds / 60L
    val hours = minutes / 60L
    val days = hours / 24L

    return when {
        minutes < 1L -> "Just now"
        minutes < 60L -> "$minutes min ago"
        hours < 24L && getLocalDateString(now) == getLocalDateString(epochMillis) -> {
            if (hours == 1L) "1 hour ago" else "$hours hours ago"
        }
        days <= 1L || getRelativeDateDisplay(epochMillis) == "Yesterday" -> "Yesterday"
        else -> getRelativeDateDisplay(epochMillis)
    }
}

@Composable
fun NotificationScreen(
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { FirebaseNotificationRepository() }
    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentUid = Firebase.auth.currentUser?.uid ?: UserSession.currentUserId

    fun refreshNotifications() {
        if (currentUid.isNullOrBlank()) {
            isLoading = false
            return
        }
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val remoteList = repository.getNotifications(currentUid)
                val localItems = com.example.fitness.feature.notifications.data.WorkoutNotificationService.getNotifications(currentUid)
                val map = linkedMapOf<String, NotificationItem>()
                localItems.forEach { map[it.id] = it }
                remoteList.forEach { map[it.id] = it }
                val merged = map.values.sortedByDescending { it.createdAt }
                notifications = merged
                UserSession.unreadNotificationCount = merged.count { !it.read }
            } catch (e: Exception) {
                val localItems = com.example.fitness.feature.notifications.data.WorkoutNotificationService.getNotifications(currentUid)
                if (localItems.isNotEmpty()) {
                    notifications = localItems
                    UserSession.unreadNotificationCount = localItems.count { !it.read }
                } else {
                    errorMessage = "Unable to load notifications. Please try again."
                }
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(currentUid) {
        refreshNotifications()
    }

    val unreadCount = notifications.count { !it.read }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularIconButton(
                    icon = FitnessIcons.ArrowBack,
                    onClick = { navigator.pop() },
                    contentDescription = "Back",
                    modifier = Modifier.padding(end = 10.dp)
                )
                Text(
                    text = "Notifications",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable {
                        val uid = Firebase.auth.currentUser?.uid ?: UserSession.currentUserId
                        if (!uid.isNullOrBlank()) {
                            coroutineScope.launch {
                                repository.createLocalTestNotification(uid)
                            }
                        }
                    }
                )
            }

            if (unreadCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    onClick = {
                        val uid = currentUid ?: return@Surface
                        notifications = notifications.map { it.copy(read = true) }
                        UserSession.unreadNotificationCount = 0
                        com.example.fitness.feature.notifications.data.WorkoutNotificationService.markAllAsRead(uid)
                        coroutineScope.launch {
                            repository.markAllAsRead(uid)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0D140E),
                    border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Mark all as read",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FitTheme.colors.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Body Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                            .fillMaxSize()
                            .padding(top = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage ?: "Error loading notifications",
                            fontSize = 14.sp,
                            color = Color(0xFFFF5252),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            onClick = { refreshNotifications() },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF14191C),
                            border = BorderStroke(1.dp, Color(0xFF222B2F))
                        ) {
                            Text(
                                text = "Retry",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                notifications.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 100.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0D1217))
                                .border(1.dp, Color(0xFF1B242C), CircleShape)
                                .clickable {
                                    val uid = Firebase.auth.currentUser?.uid ?: UserSession.currentUserId
                                    if (!uid.isNullOrBlank()) {
                                        coroutineScope.launch {
                                            repository.createLocalTestNotification(uid)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FitnessIcons.Bell,
                                contentDescription = "Tap to trigger test notification in Dev Mode",
                                tint = Color(0xFF8E9BA4),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "You're all caught up!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No new notifications right now.",
                            fontSize = 14.sp,
                            color = Color(0xFF8E9BA4),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    val now = getCurrentEpochMillis()
                    val todayString = getLocalDateString(now)
                    val todayItems = notifications.filter { getLocalDateString(it.createdAt) == todayString }
                    val earlierItems = notifications.filter { getLocalDateString(it.createdAt) != todayString }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 32.dp)
                    ) {
                        if (todayItems.isNotEmpty()) {
                            SectionHeader(title = "TODAY")
                            Spacer(modifier = Modifier.height(10.dp))
                            todayItems.forEach { item ->
                                NotificationCard(
                                    item = item,
                                    onItemClick = {
                                        val uid = currentUid ?: return@NotificationCard
                                        if (!item.read) {
                                            notifications = notifications.map {
                                                if (it.id == item.id) it.copy(read = true) else it
                                            }
                                            UserSession.unreadNotificationCount = notifications.count { !it.read }
                                            com.example.fitness.feature.notifications.data.WorkoutNotificationService.markAsRead(uid, item.id)
                                            coroutineScope.launch {
                                                repository.markAsRead(uid, item.id)
                                            }
                                        }
                                        handleAction(item, navigator)
                                    }
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }

                        if (earlierItems.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            SectionHeader(title = "EARLIER")
                            Spacer(modifier = Modifier.height(10.dp))
                            earlierItems.forEach { item ->
                                NotificationCard(
                                    item = item,
                                    onItemClick = {
                                        val uid = currentUid ?: return@NotificationCard
                                        if (!item.read) {
                                            notifications = notifications.map {
                                                if (it.id == item.id) it.copy(read = true) else it
                                            }
                                            UserSession.unreadNotificationCount = notifications.count { !it.read }
                                            com.example.fitness.feature.notifications.data.WorkoutNotificationService.markAsRead(uid, item.id)
                                            coroutineScope.launch {
                                                repository.markAsRead(uid, item.id)
                                            }
                                        }
                                        handleAction(item, navigator)
                                    }
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF6E7C87),
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 4.dp, top = 6.dp)
    )
}

@Composable
private fun NotificationCard(
    item: NotificationItem,
    onItemClick: () -> Unit
) {
    val isUnread = !item.read

    val backgroundColor = if (isUnread) Color(0xFF09120B) else Color(0xFF07090C)
    val borderColor = if (isUnread) FitTheme.colors.primary.copy(alpha = 0.45f) else Color(0xFF161E24)
    val titleColor = if (isUnread) Color.White else Color(0xFFB0BAC0)
    val messageColor = if (isUnread) Color(0xFFD1D8DC) else Color(0xFF6E7C87)

    Surface(
        onClick = onItemClick,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Type Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isUnread) Color(0xFF112214) else Color(0xFF101518))
                    .border(
                        1.dp,
                        if (isUnread) FitTheme.colors.primary.copy(alpha = 0.4f) else Color(0xFF1C252B),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getTypeIconEmoji(item.type),
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Body
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                        color = titleColor,
                        modifier = Modifier.weight(1f)
                    )
                    if (isUnread) {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(FitTheme.colors.primary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.message,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = messageColor,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = formatRelativeTime(item.createdAt),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6E7C87)
                )
            }
        }
    }
}

private fun getTypeIconEmoji(type: String): String {
    return when (type.uppercase().trim()) {
        NotificationItem.TYPE_WORKOUT_REMINDER, "WORKOUT" -> "💪"
        NotificationItem.TYPE_WORKOUT_COMPLETED -> "🎉"
        NotificationItem.TYPE_STREAK, NotificationItem.TYPE_STREAK_MILESTONE -> "🔥"
        NotificationItem.TYPE_PROGRESS, NotificationItem.TYPE_PROGRAM_MILESTONE -> "🎯"
        NotificationItem.TYPE_SYSTEM -> "⚙️"
        else -> "🔔"
    }
}

private fun handleAction(
    item: NotificationItem,
    navigator: com.example.fitness.core.navigation.Navigator
) {
    val actionType = item.actionType?.uppercase()?.trim() ?: return
    val actionId = item.actionId?.trim() ?: ""

    when (actionType) {
        "WORKOUT" -> {
            if (actionId.isNotBlank()) {
                navigator.navigate(Screen.WorkoutOverview(actionId))
            } else {
                navigator.navigate(Screen.Workout)
            }
        }
        "PROGRAM" -> {
            if (actionId.isNotBlank()) {
                navigator.navigate(Screen.ProgramDetail(actionId))
            } else {
                navigator.navigate(Screen.Programs)
            }
        }
        "PROGRESS", "HISTORY" -> {
            navigator.navigate(Screen.Progress)
        }
        "PROFILE" -> {
            navigator.navigate(Screen.Profile)
        }
    }
}
