package com.example.fitness.feature.admin

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.fitness.core.auth.UserRole
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.firebase.FitnessFirebase
import com.example.fitness.core.navigation.LocalNavigator
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch

private data class AdminPortalModule(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val screen: Screen?,
    val isActive: Boolean = false
)

@Composable
fun AdminDashboardScreen(
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()

    // 1. SCREEN-LEVEL RBAC GUARD: strictly reject non-admins
    LaunchedEffect(UserSession.currentUserRole) {
        if (UserSession.currentUserRole != UserRole.ADMIN) {
            val target = if (Firebase.auth.currentUser != null) Screen.Home else Screen.Auth(isSignUp = false)
            navigator.resetRoot(target)
        }
    }

    LaunchedEffect(Unit) {
        com.example.fitness.core.auth.LoginPerfTracker.record("Home/Admin usable")
        com.example.fitness.core.auth.LoginPerfTracker.logSummary()
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

    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var isLoggingOut by remember { mutableStateOf(false) }
    var logoutErrorMessage by remember { mutableStateOf<String?>(null) }

    val adminEmail = UserSession.currentUserEmail ?: Firebase.auth.currentUser?.email ?: "admin@fitness.internal"
    val adminName = UserSession.currentUserName ?: Firebase.auth.currentUser?.displayName ?: "Administrator"

    // Portal modules definition: exactly 5 modules (Dashboard, Users, Notifications, Workouts, Programs)
    val portalModules = remember {
        listOf(
            AdminPortalModule(
                title = "Dashboard",
                subtitle = "Overview & real-time telemetry",
                icon = FitnessIcons.Home,
                screen = null,
                isActive = true
            ),
            AdminPortalModule(
                title = "Users",
                subtitle = "Manage user accounts & RBAC",
                icon = FitnessIcons.Person,
                screen = Screen.AdminUsers
            ),
            AdminPortalModule(
                title = "Notifications",
                subtitle = "Compose workout alerts & reminders",
                icon = FitnessIcons.Bell,
                screen = Screen.AdminNotifications
            ),
            AdminPortalModule(
                title = "Workouts",
                subtitle = "Configure routine plans & schedules",
                icon = FitnessIcons.Workout,
                screen = Screen.AdminWorkouts
            ),
            AdminPortalModule(
                title = "Programs",
                subtitle = "30-day training program builder",
                icon = FitnessIcons.Clipboard,
                screen = Screen.AdminPrograms
            )
        )
    }

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
                .padding(horizontal = 16.dp)
                .padding(bottom = 36.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // 1. ADMIN HEADER (Word-Safe & Aligned with User App)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FITNESS",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0C1015),
                            border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(FitTheme.colors.primary)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "ADMIN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FitTheme.colors.primary,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Dashboard Overview",
                        fontSize = 13.sp,
                        color = Color(0xFF8E9BA4),
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Header Actions: User App & Logout
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Switch to User App
                    Surface(
                        onClick = {
                            com.example.fitness.core.perf.AppPerfTracker.start("ADMIN_TO_USER_APP")
                            navigator.navigate(Screen.Home)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0C1015),
                        border = BorderStroke(1.dp, Color(0xFF1B2329)),
                        modifier = Modifier
                            .height(38.dp)
                            .semantics { contentDescription = "Switch to User App" }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = FitnessIcons.Home,
                                contentDescription = null,
                                tint = Color(0xFFA6B8C4),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "User App",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    // Logout Button: Triggers Confirmation Dialog
                    Surface(
                        onClick = { showLogoutConfirmation = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E0E11),
                        border = BorderStroke(1.dp, Color(0xFF4A1A22)),
                        modifier = Modifier
                            .size(38.dp)
                            .semantics { contentDescription = "Log Out" }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FitnessIcons.Logout,
                                contentDescription = "Log Out",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Administrator Identity Strip
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF07090C),
                border = BorderStroke(1.dp, Color(0xFF151C22)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF12181F),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = FitnessIcons.Person,
                                contentDescription = null,
                                tint = FitTheme.colors.primary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$adminName • $adminEmail",
                        fontSize = 11.sp,
                        color = Color(0xFF8E9BA4),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 2. PORTAL MODULES (Responsive Grid, Zero Horizontal Scroll)
            // ==========================================
            AdminSectionHeader(title = "PORTAL MODULES")

            Spacer(modifier = Modifier.height(10.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val columns = when {
                    maxWidth >= 700.dp -> 3
                    maxWidth >= 460.dp -> 2
                    else -> 1
                }

                if (columns == 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        portalModules.forEach { module ->
                            AdminPortalModuleCard(
                                title = module.title,
                                subtitle = module.subtitle,
                                icon = module.icon,
                                isActive = module.isActive,
                                onClick = {
                                    module.screen?.let { navigator.navigate(it) }
                                }
                            )
                        }
                    }
                } else {
                    val rows = portalModules.chunked(columns)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        rows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { module ->
                                    AdminPortalModuleCard(
                                        title = module.title,
                                        subtitle = module.subtitle,
                                        icon = module.icon,
                                        isActive = module.isActive,
                                        onClick = {
                                            module.screen?.let { navigator.navigate(it) }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowItems.size < columns) {
                                    repeat(columns - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmation) {
        Dialog(
            onDismissRequest = {
                if (!isLoggingOut) {
                    showLogoutConfirmation = false
                    logoutErrorMessage = null
                }
            }
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0D1117),
                border = BorderStroke(1.dp, Color(0xFF1B2228)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header with Logout icon
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFF5252).copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = FitnessIcons.Logout,
                                    contentDescription = null,
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Logout from Admin?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Are you sure you want to logout from your admin account?",
                        fontSize = 13.sp,
                        color = Color(0xFFBAC5D0),
                        lineHeight = 18.sp
                    )

                    if (logoutErrorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = logoutErrorMessage ?: "",
                            fontSize = 12.sp,
                            color = Color(0xFFFF5252),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Cancel Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF161E24),
                            border = BorderStroke(1.dp, Color(0xFF2B3A42)),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clickable(enabled = !isLoggingOut) {
                                    showLogoutConfirmation = false
                                    logoutErrorMessage = null
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Cancel",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Yes, Logout Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFF5252),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clickable(enabled = !isLoggingOut) {
                                    coroutineScope.launch {
                                        isLoggingOut = true
                                        logoutErrorMessage = null
                                        try {
                                            // 1. Flush and await pending workout writes before revoking auth credentials
                                            try {
                                                com.example.fitness.feature.progress.data.WorkoutProgressService.retryAllPendingSyncs()
                                                com.example.fitness.feature.progress.data.WorkoutProgressService.awaitPendingSyncs(timeoutMillis = 4000L)
                                            } catch (_: Exception) {}

                                            // 2. Clear devices subcollection if present
                                            try {
                                                val uid = Firebase.auth.currentUser?.uid
                                                if (!uid.isNullOrBlank()) {
                                                    val devices = FitnessFirebase.firestore.collection("users").document(uid).collection("devices").get()
                                                    devices.documents.forEach { doc ->
                                                        try { doc.reference.delete() } catch (_: Exception) {}
                                                    }
                                                }
                                            } catch (_: Exception) {}

                                            // 3. Platform & Firebase sign out
                                            try {
                                                com.example.fitness.core.auth.clearPlatformAuthState()
                                            } catch (_: Exception) {}
                                            Firebase.auth.signOut()

                                            // 4. Clear cache and user session
                                            com.example.fitness.feature.notifications.data.WorkoutNotificationService.clearCache()
                                            com.example.fitness.feature.progress.data.WorkoutProgressService.clearCache()
                                            UserSession.clear()

                                            // 5. Navigate to Login screen and reset navigation stack
                                            showLogoutConfirmation = false
                                            navigator.resetRoot(Screen.Auth(isSignUp = false))
                                        } catch (e: Exception) {
                                            logoutErrorMessage = "Logout failed: ${e.message ?: "Unknown error"}. Please try again."
                                            isLoggingOut = false
                                        }
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isLoggingOut) "Logging out..." else "Yes, Logout",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
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
private fun AdminSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF5A6E7B),
        letterSpacing = 1.2.sp
    )
}

@Composable
private fun AdminPortalModuleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f)
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        color = if (isActive) Color(0xFF0C1610) else Color(0xFF07090C),
        border = BorderStroke(
            1.dp,
            if (isActive) FitTheme.colors.primary.copy(alpha = 0.5f) else Color(0xFF161E24)
        ),
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .semantics { contentDescription = "Module $title" }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isActive) FitTheme.colors.primary.copy(alpha = 0.2f) else Color(0xFF12181F),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isActive) FitTheme.colors.primary else Color(0xFFA6B8C4),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) FitTheme.colors.primary else Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(FitTheme.colors.primary)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF8E9BA4),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!isActive) {
                Icon(
                    imageVector = FitnessIcons.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF5A6E7B),
                    modifier = Modifier.size(15.dp)
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = FitTheme.colors.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "ACTIVE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = FitTheme.colors.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
