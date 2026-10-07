package com.example.fitness.feature.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun AdminPlaceholderScreen(
    module: AdminModule,
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current

    // Screen-level RBAC enforcement
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularIconButton(
                    icon = FitnessIcons.ArrowBack,
                    contentDescription = "Back to Dashboard",
                    onClick = { navigator.navigate(Screen.AdminDashboard) }
                )

                // Admin Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF101416),
                    border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "ADMIN • ${module.category.uppercase()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FitTheme.colors.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Icon & Module Title Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF0D1117),
                border = BorderStroke(1.dp, Color(0xFF1B2228)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Module Icon Circle
                    Surface(
                        shape = CircleShape,
                        color = FitTheme.colors.primary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, FitTheme.colors.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (module) {
                                    AdminModule.USERS -> FitnessIcons.Person
                                    AdminModule.NOTIFICATIONS -> FitnessIcons.Bell
                                    AdminModule.WORKOUTS -> FitnessIcons.Workout
                                    AdminModule.EXERCISES -> FitnessIcons.Play
                                    AdminModule.PROGRAMS -> FitnessIcons.Explore
                                    AdminModule.CONTENT -> FitnessIcons.Clipboard
                                    AdminModule.ANALYTICS -> FitnessIcons.Progress
                                    AdminModule.SETTINGS -> FitnessIcons.Settings
                                    else -> FitnessIcons.Home
                                },
                                contentDescription = module.title,
                                tint = FitTheme.colors.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = module.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF161E24),
                        border = BorderStroke(1.dp, Color(0xFF2B3A42))
                    ) {
                        Text(
                            text = "Coming soon • ${module.targetStep}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8E9BA4),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = module.description,
                        fontSize = 14.sp,
                        color = Color(0xFF8E9BA4),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Architecture Note Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0D1117),
                border = BorderStroke(1.dp, Color(0xFF1B2228)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Module Architecture",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "• Role Authorization: Protected exclusively via request.auth.token.admin == true.\n" +
                                "• Firestore Policy: Normal users cannot view or mutate administrative collections.\n" +
                                "• Implementation Stage: This module foundation is configured for Step 2. Detailed controls will be activated in upcoming steps.",
                        fontSize = 13.sp,
                        color = Color(0xFF8E9BA4),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Actions
            PrimaryButton(
                text = "Return to Dashboard",
                onClick = { navigator.navigate(Screen.AdminDashboard) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                onClick = { navigator.resetRoot(Screen.Home) },
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF101416),
                border = BorderStroke(1.dp, Color(0xFF222E35)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Switch to User App (Preview)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
