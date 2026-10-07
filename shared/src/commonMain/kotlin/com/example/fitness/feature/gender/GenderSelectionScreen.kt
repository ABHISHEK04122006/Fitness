package com.example.fitness.feature.gender

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.core.components.CircularIconButton
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.navigation.LocalNavigator
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.core.user.data.FirebaseUserRepository
import com.example.fitness.core.user.model.UserProfile
import fitness.shared.generated.resources.Res
import fitness.shared.generated.resources.gender_female
import fitness.shared.generated.resources.gender_male
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun GenderSelectionScreen(
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val userRepository = remember { FirebaseUserRepository() }
    var selectedGender by remember { mutableStateOf<String?>(UserSession.currentUserGender) }
    var isSaving by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    fun handleGenderSelection(gender: String) {
        if (isSaving) return
        isSaving = true
        selectedGender = gender
        UserSession.currentUserGender = gender

        coroutineScope.launch {
            try {
                val currentUid = UserSession.currentUserId
                if (!currentUid.isNullOrBlank()) {
                    userRepository.updateUserGender(
                        uid = currentUid,
                        gender = gender,
                        fallbackEmail = UserSession.currentUserEmail ?: "",
                        fallbackName = UserSession.currentUserName ?: ""
                    )
                    val updatedProfile = userRepository.getUserProfile(currentUid)
                    if (updatedProfile != null) {
                        UserSession.setUserProfile(updatedProfile)
                    } else {
                        UserSession.currentUserProfile = UserSession.currentUserProfile?.copy(gender = gender)
                            ?: UserProfile(
                                uid = currentUid,
                                email = UserSession.currentUserEmail ?: "",
                                name = UserSession.currentUserName ?: "",
                                gender = gender
                            )
                    }
                }
            } catch (e: Exception) {
                println("[DEBUG] Failed to save gender to Firestore: ${e.message}")
            } finally {
                isSaving = false
                navigator.navigate(Screen.GoalSelection)
            }
        }
    }

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp)
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                // Top Header: Back + Logo FITNESS and Skip Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (navigator.canPop) {
                            CircularIconButton(
                                icon = FitnessIcons.ArrowBack,
                                onClick = { navigator.pop() },
                                contentDescription = "Back",
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }

                        // FITNESS Logo
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "FIT",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "NESS",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = FitTheme.colors.primary,
                                letterSpacing = 1.2.sp
                            )
                        }
                    }

                    // Skip button
                    Surface(
                        onClick = { navigator.navigate(Screen.GoalSelection) },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0D1217),
                        border = BorderStroke(1.dp, Color(0x33A8FF3E))
                    ) {
                        Text(
                            text = "Skip",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FitTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Title & Subtitle with entrance animation
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                            slideInVertically(initialOffsetY = { -20 })
                ) {
                    Column {
                        Text(
                            text = "Gender",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 38.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Help us personalize your experience",
                            fontSize = 14.sp,
                            color = Color(0xFF8E9BA4),
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Adaptive Gender Cards: side-by-side on wide screens/tablets, stacked on mobile
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    if (maxWidth >= 600.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            GenderCard(
                                title = "Male",
                                subtitle = "TRAIN HARDER",
                                imageRes = Res.drawable.gender_male,
                                isSelected = selectedGender.equals("Male", ignoreCase = true),
                                onClick = { handleGenderSelection("Male") },
                                modifier = Modifier.weight(1f)
                            )
                            GenderCard(
                                title = "Female",
                                subtitle = "A STRONGER YOU",
                                imageRes = Res.drawable.gender_female,
                                isSelected = selectedGender.equals("Female", ignoreCase = true),
                                onClick = { handleGenderSelection("Female") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            GenderCard(
                                title = "Male",
                                subtitle = "TRAIN HARDER",
                                imageRes = Res.drawable.gender_male,
                                isSelected = selectedGender.equals("Male", ignoreCase = true),
                                onClick = { handleGenderSelection("Male") }
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            GenderCard(
                                title = "Female",
                                subtitle = "A STRONGER YOU",
                                imageRes = Res.drawable.gender_female,
                                isSelected = selectedGender.equals("Female", ignoreCase = true),
                                onClick = { handleGenderSelection("Female") }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}

@Composable
private fun GenderCard(
    title: String,
    subtitle: String,
    imageRes: DrawableResource,
    isSelected: Boolean,
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
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF07090C),
        border = BorderStroke(
            1.2.dp,
            if (isSelected) FitTheme.colors.primary else Color(0xFF19232B)
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 190.dp, max = 240.dp)
            .scale(scale)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            Image(
                painter = painterResource(imageRes),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(22.dp))
            )

            // Cinematic Dark Gradient Overlay (darkens left for readability and bottom)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0.0f to Color(0xFF000000).copy(alpha = 0.88f),
                                0.45f to Color(0xFF000000).copy(alpha = 0.60f),
                                1.0f to Color(0xFF000000).copy(alpha = 0.20f)
                            )
                        )
                    )
            )

            // Vertical subtle vignette
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.7f to Color.Transparent,
                                1.0f to Color(0xFF000000).copy(alpha = 0.50f)
                            )
                        )
                    )
            )

            // Text and Arrow layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8E9BA4),
                        letterSpacing = 1.4.sp
                    )
                }

                // Circular Action Button with Arrow
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF141A20).copy(alpha = 0.90f))
                        .border(1.dp, Color(0xFF26333D), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = FitnessIcons.ArrowForward,
                        contentDescription = "Select $title",
                        tint = if (isPressed) FitTheme.colors.primary else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
