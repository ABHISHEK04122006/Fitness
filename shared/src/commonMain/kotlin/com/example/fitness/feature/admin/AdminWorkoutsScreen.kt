package com.example.fitness.feature.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.core.auth.UserRole
import com.example.fitness.core.components.CircularIconButton
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.navigation.BackHandler
import com.example.fitness.core.navigation.LocalNavigator
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import fitness.shared.generated.resources.Res
import fitness.shared.generated.resources.gender_female
import fitness.shared.generated.resources.workout_gym
import org.jetbrains.compose.resources.painterResource

/**
 * Admin Workout Management Entry Screen.
 *
 * Flow:
 * Admin -> Workout -> [ Male ] / [ Female ] -> Exercises
 *
 * Shows ONLY:
 * [ Male ]
 * [ Female ]
 *
 * Does NOT show:
 * - Exercises tab
 * - Programs tab
 * - Exercise Content
 * - Analytics
 * - Any duplicate gender selector
 *
 * Selecting Male directly opens the Male Exercise Catalog.
 * Selecting Female directly opens the Female Exercise Catalog.
 * After gender selection, the gender selector does NOT appear again.
 */
@Composable
fun AdminWorkoutsScreen(
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current

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

    var selectedGender by remember { mutableStateOf<String?>(null) }

    if (selectedGender == null) {
        // ==========================================
        // WORKOUT ENTRY: Show ONLY [ Male ] & [ Female ]
        // ==========================================
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

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            text = "WORKOUT",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Select gender to manage exercises",
                            fontSize = 12.sp,
                            color = Color(0xFF8E9BA4)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // 1. Male Exercise Option Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0E1318),
                    border = BorderStroke(1.dp, Color(0xFF1E2832)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedGender = "Male" }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF161F28)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.workout_gym),
                                contentDescription = "Male Exercises",
                                modifier = Modifier.size(36.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Male",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Browse and manage the male exercise catalog",
                                fontSize = 12.sp,
                                color = Color(0xFF8E9BA4)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF161F28)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FitnessIcons.ChevronRight,
                                contentDescription = "Open Male Exercises",
                                tint = FitTheme.colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Female Exercise Option Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0E1318),
                    border = BorderStroke(1.dp, Color(0xFF1E2832)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedGender = "Female" }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF161F28)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.gender_female),
                                contentDescription = "Female Exercises",
                                modifier = Modifier.size(36.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Female",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Browse and manage the female exercise catalog",
                                fontSize = 12.sp,
                                color = Color(0xFF8E9BA4)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF161F28)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FitnessIcons.ChevronRight,
                                contentDescription = "Open Female Exercises",
                                tint = FitTheme.colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Return to gender selection on system back gesture / back button
        BackHandler(enabled = true) {
            selectedGender = null
        }

        // Direct catalog for selected gender - no gender selector shown again
        AdminExercisesScreen(
            gender = selectedGender!!,
            onNavigateBack = { selectedGender = null }
        )
    }
}
