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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.core.auth.UserRole
import com.example.fitness.core.components.CircularIconButton
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.navigation.LocalNavigator
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.core.user.data.FirebaseUserRepository
import com.example.fitness.feature.admin.model.AdminUserItem
import kotlinx.coroutines.launch

enum class UserRoleFilter(val label: String) {
    ALL("All"),
    USER("USER"),
    ADMIN("ADMIN")
}

@Composable
fun AdminUsersScreen(
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val userRepository = remember { FirebaseUserRepository() }

    // Screen-level RBAC guard
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

    val allUsers = remember { mutableStateListOf<AdminUserItem>() }
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf(UserRoleFilter.ALL) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedUser by remember { mutableStateOf<AdminUserItem?>(null) }

    fun loadUsers() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val users = userRepository.getAllAdminUsers()
                allUsers.clear()
                allUsers.addAll(users)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load users from Firestore."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadUsers()
    }

    val filteredUsers = remember(searchQuery, selectedRoleFilter, allUsers.toList()) {
        val query = searchQuery.trim().lowercase()
        allUsers.filter { user ->
            val matchesQuery = query.isBlank() ||
                    user.firstName.lowercase().contains(query) ||
                    user.lastName.lowercase().contains(query) ||
                    user.displayName.lowercase().contains(query) ||
                    user.displayEmail.lowercase().contains(query)
            val matchesFilter = when (selectedRoleFilter) {
                UserRoleFilter.ALL -> true
                UserRoleFilter.USER -> user.role == UserRole.USER
                UserRoleFilter.ADMIN -> user.role == UserRole.ADMIN
            }
            matchesQuery && matchesFilter
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

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularIconButton(
                        icon = FitnessIcons.ArrowBack,
                        contentDescription = "Back to Dashboard",
                        onClick = { navigator.navigate(Screen.AdminDashboard) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Users",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${filteredUsers.size} registered account${if (filteredUsers.size == 1) "" else "s"}",
                            fontSize = 12.sp,
                            color = Color(0xFF8E9BA4)
                        )
                    }
                }

                // Refresh Button
                CircularIconButton(
                    icon = FitnessIcons.Replay,
                    contentDescription = "Refresh Users",
                    onClick = { loadUsers() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0D1117),
                border = BorderStroke(1.dp, Color(0xFF1B2228)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = FitnessIcons.Search,
                        contentDescription = "Search Users Icon",
                        tint = Color(0xFF5A6E7B),
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search by name or email...",
                                fontSize = 13.sp,
                                color = Color(0xFF5A6E7B)
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(FitTheme.colors.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Search user field" }
                        )
                    }

                    if (searchQuery.isNotEmpty()) {
                        Text(
                            text = "Clear",
                            fontSize = 11.sp,
                            color = FitTheme.colors.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable { searchQuery = "" }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .semantics { contentDescription = "Clear search query" }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Role Filter Chips Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UserRoleFilter.entries.forEach { filter ->
                    val isSelected = selectedRoleFilter == filter
                    val count = when (filter) {
                        UserRoleFilter.ALL -> allUsers.size
                        UserRoleFilter.USER -> allUsers.count { it.role == UserRole.USER }
                        UserRoleFilter.ADMIN -> allUsers.count { it.role == UserRole.ADMIN }
                    }
                    Surface(
                        onClick = { selectedRoleFilter = filter },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) FitTheme.colors.primary.copy(alpha = 0.15f) else Color(0xFF0D1117),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) FitTheme.colors.primary else Color(0xFF1B2228)
                        ),
                        modifier = Modifier.semantics {
                            contentDescription = "Filter by ${filter.label} ($count users)"
                        }
                    ) {
                        Text(
                            text = "${filter.label} ($count)",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) FitTheme.colors.primary else Color(0xFF8E9BA4),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Users List / Loading / Error
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = FitTheme.colors.primary,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading users from Firestore...",
                                fontSize = 13.sp,
                                color = Color(0xFF8E9BA4)
                            )
                        }
                    }
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFF1E0E11),
                            border = BorderStroke(1.dp, Color(0xFF4A1A22)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Unable to Load Users",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF5252)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    fontSize = 12.sp,
                                    color = Color(0xFF8E9BA4),
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Surface(
                                    onClick = { loadUsers() },
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF2B1216)
                                ) {
                                    Text(
                                        text = "Retry",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF5252),
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                filteredUsers.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (searchQuery.isNotBlank() || selectedRoleFilter != UserRoleFilter.ALL) {
                                    "No users matching current filters"
                                } else {
                                    "No registered users found."
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Registered users from users/{uid} will appear here.",
                                fontSize = 12.sp,
                                color = Color(0xFF8E9BA4)
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredUsers, key = { it.uid }) { user ->
                            AdminUserCard(
                                user = user,
                                onClick = { selectedUser = user }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }

    // User Details Dialog
    selectedUser?.let { user ->
        AdminUserDetailsDialog(
            user = user,
            onDismiss = { selectedUser = null }
        )
    }
}

@Composable
private fun AdminUserCard(
    user: AdminUserItem,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0D1117),
        border = BorderStroke(
            1.dp,
            if (user.role == UserRole.ADMIN) FitTheme.colors.primary.copy(alpha = 0.35f) else Color(0xFF1B2228)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "User ${user.displayName}, role ${user.role}, provider ${user.displayProvider}"
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                shape = CircleShape,
                color = if (user.role == UserRole.ADMIN) FitTheme.colors.primary.copy(alpha = 0.15f) else Color(0xFF161E24),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.displayName.take(1).uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (user.role == UserRole.ADMIN) FitTheme.colors.primary else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = user.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (user.role == UserRole.ADMIN) FitTheme.colors.primary.copy(alpha = 0.15f) else Color(0xFF161E24)
                    ) {
                        Text(
                            text = if (user.role == UserRole.ADMIN) "ADMIN" else "USER",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (user.role == UserRole.ADMIN) FitTheme.colors.primary else Color(0xFF8E9BA4),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = user.displayEmail,
                    fontSize = 12.sp,
                    color = Color(0xFF8E9BA4),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Authentication Provider badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF161E24),
                        border = BorderStroke(1.dp, Color(0xFF26333D))
                    ) {
                        Text(
                            text = user.displayProvider,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF8E9BA4),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    if (user.gender.isNotBlank()) {
                        Text(
                            text = "• ${user.displayGender}",
                            fontSize = 10.sp,
                            color = Color(0xFF5A6E7B)
                        )
                    }
                }
            }

            Icon(
                imageVector = FitnessIcons.ChevronRight,
                contentDescription = "View Details",
                tint = Color(0xFF5A6E7B),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
