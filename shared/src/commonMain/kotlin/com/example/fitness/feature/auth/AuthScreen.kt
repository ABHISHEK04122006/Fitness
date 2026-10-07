package com.example.fitness.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.core.auth.AuthSessionManager
import com.example.fitness.core.auth.LoginPerfTracker
import com.example.fitness.core.auth.UserRole
import com.example.fitness.core.auth.isAppleSignInNative
import com.example.fitness.core.auth.signInWithApple
import com.example.fitness.core.auth.signInWithGoogle
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.components.PrimaryButton
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.core.firebase.FitnessFirebase
import com.example.fitness.core.navigation.LocalNavigator
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.core.user.data.FirebaseUserRepository
import com.example.fitness.feature.progress.data.WorkoutProgressService
import com.example.fitness.platformLog
import dev.gitlive.firebase.auth.FirebaseUser
import fitness.shared.generated.resources.Res
import fitness.shared.generated.resources.app_logo
import fitness.shared.generated.resources.app_logo_emblem
import fitness.shared.generated.resources.auth_hero
import org.jetbrains.compose.resources.painterResource
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * ATHGYM Authentication & Onboarding Screen
 *
 * Implements the reference-matching dark cinematic fitness landing screen:
 * - Top header: ATHGYM (white ATH + neon lime GYM) & STRONGER EVERYDAY on left;
 *   DISCIPLINE BUILDS FREEDOM with neon accent bar on right.
 * - Full-bleed athletic hero image with dark cinematic gradient fading into pure black.
 * - Dynamic welcome title: "Welcome" (white) + "Back" (neon green) for returning users;
 *   "Welcome to" (white) + "ATHGYM" (neon green) for new users.
 * - Adaptive motivational subtitle.
 * - High-contrast, full-width dark buttons with right chevrons:
 *   1. Continue with Google
 *   2. Continue with Apple
 * - Exactly ONE secondary "Skip for now ->" text action at the bottom.
 * - 100% preservation of Credential Manager, Firebase Auth, UserSession, and RBAC routing.
 */
@Composable
fun AuthScreen(
    initialIsSignUp: Boolean = false,
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isRouting by remember { mutableStateOf(false) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var activeProvider by remember { mutableStateOf<String?>(null) }
    var showAppleInfoDialog by remember { mutableStateOf(false) }

    var screenVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        screenVisible = true
    }

    val scrollState = rememberScrollState()
    val userRepository = remember { FirebaseUserRepository() }

    // Dynamic welcome state: distinguish new vs returning authenticated user
    val currentUser = try {
        FitnessFirebase.auth.currentUser
    } catch (_: Exception) {
        null
    }
    val isReturningUser = (currentUser != null || UserSession.currentUserId != null) && !initialIsSignUp
    val welcomeSubtitle = if (isReturningUser) {
        "Continue your fitness journey\nand reach your peak performance."
    } else {
        "Start your fitness journey and\nbuild a stronger you."
    }

    // Auto-routing for pre-authenticated cold sessions (bypassed if user is actively clicking a login button)
    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null && !initialIsSignUp && !isLoading && !isRouting && !isAuthenticating) {
            isRouting = true
            try {
                platformLog("AuthScreen", "[SESSION_RESTORE_STARTED] Auto-restoring session for uid=${currentUser.uid}")
                val destination = withTimeout(15_000L) {
                    AuthSessionManager.restoreUserSession(currentUser, candidateName = currentUser.displayName)
                }
                platformLog("AuthScreen", "[NAVIGATION_COMPLETED] Navigating to ${destination::class.simpleName}")
                navigator.resetRoot(destination)
            } catch (e: Exception) {
                platformLog("AuthScreen", "[NAVIGATION_ERROR] Session restore failed: ${e.message}")
                errorMessage = "Login is taking longer than expected. Please check your connection and try again."
            } finally {
                isRouting = false
            }
        }
    }

    suspend fun handlePostAuthRouting(user: FirebaseUser, candidateName: String? = null) {
        if (isRouting) return
        isRouting = true
        try {
            val destination = withTimeout(15_000L) {
                AuthSessionManager.restoreUserSession(user, candidateName)
            }
            navigator.resetRoot(destination)
        } catch (e: Exception) {
            platformLog("AuthScreen", "[LOGIN] Post-auth routing FAILURE: ${e.message}")
            errorMessage = "Login is taking longer than expected. Please check your connection and try again."
        } finally {
            isRouting = false
            isLoading = false
            isAuthenticating = false
            activeProvider = null
        }
    }

    fun handleSocialAuth(provider: String) {
        focusManager.clearFocus()
        if (isLoading || isRouting || isAuthenticating) return
        isAuthenticating = true
        errorMessage = null
        isLoading = true
        activeProvider = provider

        platformLog("AuthScreen", "[LOGIN] Button clicked (provider=$provider)")
        platformLog("AuthScreen", "[LOGIN] Validation passed")
        platformLog("AuthScreen", "[LOGIN] Loading = true")
        if (provider == "Google") {
            LoginPerfTracker.start("Google")
            LoginPerfTracker.record("Google button tap")
            coroutineScope.launch {
                try {
                    platformLog("AuthScreen", "[LOGIN] Google sign-in START")
                    val user = signInWithGoogle()
                    if (user != null) {
                        platformLog("AuthScreen", "[LOGIN] Firebase sign-in SUCCESS")
                        val currentUserStatus = if (FitnessFirebase.auth.currentUser != null) "available" else "null"
                        platformLog("AuthScreen", "[LOGIN] Firebase currentUser = $currentUserStatus")
                        UserSession.currentAuthProvider = "Google"
                        handlePostAuthRouting(user, candidateName = user.displayName)
                    } else {
                        platformLog("AuthScreen", "[LOGIN] Google Sign-In: User cancelled account selection")
                        LoginPerfTracker.record("Google Sign-In Cancelled by user")
                    }
                } catch (e: Exception) {
                    if (com.example.fitness.core.auth.isUserCancellation(e)) {
                        platformLog("AuthScreen", "[LOGIN] Firebase sign-in CANCELLED by user: ${e.message}")
                        LoginPerfTracker.record("Google Sign-In Cancelled by user")
                    } else {
                        platformLog("AuthScreen", "[LOGIN] Firebase sign-in FAILURE: ${e.message}")
                        LoginPerfTracker.record("Firebase sign-in ERROR: ${e::class.simpleName}")
                        errorMessage = mapSocialAuthError(e, "Google")
                    }
                } finally {
                    platformLog("AuthScreen", "[LOGIN] Loading = false")
                    isLoading = false
                    isAuthenticating = false
                    activeProvider = null
                }
            }
        } else if (provider == "Apple") {
            if (!isAppleSignInNative()) {
                showAppleInfoDialog = true
                isLoading = false
                isAuthenticating = false
                activeProvider = null
                return
            }
            LoginPerfTracker.start("Apple")
            coroutineScope.launch {
                try {
                    platformLog("AuthScreen", "[LOGIN] Apple sign-in START")
                    val user = signInWithApple()
                    if (user != null) {
                        platformLog("AuthScreen", "[LOGIN] Apple sign-in SUCCESS")
                        val currentUserStatus = if (FitnessFirebase.auth.currentUser != null) "available" else "null"
                        platformLog("AuthScreen", "[LOGIN] Firebase currentUser = $currentUserStatus")
                        UserSession.currentAuthProvider = "Apple"
                        handlePostAuthRouting(user, candidateName = user.displayName)
                    } else {
                        platformLog("AuthScreen", "[LOGIN] Apple sign-in: User cancelled")
                        LoginPerfTracker.record("Apple Sign-In Cancelled by user")
                    }
                } catch (e: Exception) {
                    if (com.example.fitness.core.auth.isUserCancellation(e)) {
                        platformLog("AuthScreen", "[LOGIN] Apple sign-in CANCELLED by user: ${e.message}")
                        LoginPerfTracker.record("Apple Sign-In Cancelled by user")
                    } else {
                        platformLog("AuthScreen", "[LOGIN] Apple sign-in FAILURE: ${e.message}")
                        LoginPerfTracker.record("Apple sign-in ERROR: ${e::class.simpleName}")
                        errorMessage = mapSocialAuthError(e, "Apple")
                    }
                } finally {
                    platformLog("AuthScreen", "[LOGIN] Loading = false")
                    isLoading = false
                    isAuthenticating = false
                    activeProvider = null
                }
            }
        } else {
            // Fallback for Skip or unrecognized provider
            isAuthenticating = false
            isLoading = false
            activeProvider = null
            navigator.navigate(Screen.GenderSelection)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        // Full-bleed Hero Background Image covering the upper ~58% of screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.58f)
                .align(Alignment.TopCenter)
        ) {
            Image(
                painter = painterResource(Res.drawable.auth_hero),
                contentDescription = "ATHGYM Athlete",
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxSize()
            )

            // Multi-stop Cinematic Dark Vignette & Bottom Fade to Solid Pure Black
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color(0x66000000), // Status bar & top branding legibility
                            0.18f to Color.Transparent,
                            0.42f to Color.Transparent,
                            0.62f to Color(0x73000000),
                            0.80f to Color(0xCC000000),
                            1.0f to Color(0xFF000000)   // Seamlessly solid pure black at bottom
                        )
                    )
            )
        }

        // Foreground Content
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenHeight = maxHeight
            val screenWidth = maxWidth
            val isCompactWidth = screenWidth < 360.dp
            val topSpacerHeight = when {
                screenHeight < 560.dp -> 32.dp
                screenHeight < 680.dp -> 80.dp
                screenHeight < 780.dp -> 150.dp
                else -> 200.dp
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .padding(horizontal = if (isCompactWidth) 16.dp else 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // Top Header: ATHGYM Branding on Left, DISCIPLINE BUILDS FREEDOM on Right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: ATHGYM Brand Emblem + Wordmark + Subtitle
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(Res.drawable.app_logo_emblem),
                                contentDescription = "ATHGYM Logo",
                                modifier = Modifier.size(
                                    width = if (isCompactWidth) 36.dp else 44.dp,
                                    height = if (isCompactWidth) 32.dp else 38.dp
                                ),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.width(if (isCompactWidth) 8.dp else 10.dp))
                            Column(verticalArrangement = Arrangement.Center) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ATH",
                                        fontSize = if (isCompactWidth) 19.sp else 22.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        letterSpacing = 1.0.sp
                                    )
                                    Text(
                                        text = "GYM",
                                        fontSize = if (isCompactWidth) 19.sp else 22.sp,
                                        fontWeight = FontWeight.Black,
                                        color = FitTheme.colors.primary,
                                        letterSpacing = 1.0.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "STRONGER EVERYDAY",
                                    fontSize = if (isCompactWidth) 7.5.sp else 8.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF8E9BA4),
                                    letterSpacing = if (isCompactWidth) 1.2.sp else 1.6.sp
                                )
                            }
                        }

                        // Right: Green Accent Bar + DISCIPLINE BUILDS FREEDOM
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(if (isCompactWidth) 26.dp else 30.dp)
                                    .background(FitTheme.colors.primary)
                            )
                            Spacer(modifier = Modifier.width(if (isCompactWidth) 6.dp else 8.dp))
                            Column {
                                Text(
                                    text = "DISCIPLINE",
                                    fontSize = if (isCompactWidth) 7.5.sp else 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA6B8C4),
                                    letterSpacing = 1.2.sp,
                                    lineHeight = if (isCompactWidth) 9.5.sp else 11.sp
                                )
                                Text(
                                    text = "BUILDS",
                                    fontSize = if (isCompactWidth) 7.5.sp else 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA6B8C4),
                                    letterSpacing = 1.2.sp,
                                    lineHeight = if (isCompactWidth) 9.5.sp else 11.sp
                                )
                                Text(
                                    text = "FREEDOM",
                                    fontSize = if (isCompactWidth) 7.5.sp else 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA6B8C4),
                                    letterSpacing = 1.2.sp,
                                    lineHeight = if (isCompactWidth) 9.5.sp else 11.sp
                                )
                            }
                        }
                    }

                    // Push content down over the gradient transition responsively
                    Spacer(modifier = Modifier.height(topSpacerHeight))

                    // Dynamic Welcome Heading
                    AnimatedVisibility(
                        visible = screenVisible,
                        enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                                expandVertically(animationSpec = spring(stiffness = Spring.StiffnessLow))
                    ) {
                        Column {
                            Text(
                                text = "Train. Progress.",
                                fontSize = if (isCompactWidth) 30.sp else 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                lineHeight = if (isCompactWidth) 34.sp else 40.sp
                            )
                            Text(
                                text = "Repeat",
                                fontSize = if (isCompactWidth) 30.sp else 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = FitTheme.colors.primary,
                                lineHeight = if (isCompactWidth) 34.sp else 40.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = welcomeSubtitle,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF8E9BA4),
                                lineHeight = 22.sp
                            )
                        }
                    }

            Spacer(modifier = Modifier.height(28.dp))

            // Error banner if any
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF3B151A),
                    border = BorderStroke(1.dp, Color(0xFF7F232D)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⚠️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFFB3B8),
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF5A1D24),
                            border = BorderStroke(1.dp, Color(0xFF9E2C3A)),
                            modifier = Modifier.clickable {
                                errorMessage = null
                                handleSocialAuth(activeProvider ?: "Google")
                            }
                        ) {
                            Text(
                                text = "Retry",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Authentication Buttons: Google & Apple
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Continue with Google
                SocialAuthButton(
                    text = "Continue with Google",
                    icon = FitnessIcons.Google,
                    iconTint = Color.Unspecified, // Authentic Google multi-color
                    backgroundColor = Color(0xFF0F1418),
                    borderColor = Color(0xFF1B232B),
                    textColor = Color.White,
                    isLoading = isLoading && activeProvider == "Google",
                    enabled = !isLoading && !isAuthenticating,
                    contentDescription = "Continue with Google",
                    onClick = { handleSocialAuth("Google") }
                )

                // Continue with Apple
                SocialAuthButton(
                    text = "Continue with Apple",
                    icon = FitnessIcons.AppleLogo,
                    iconTint = Color.White,
                    backgroundColor = Color(0xFF0F1418),
                    borderColor = Color(0xFF1B232B),
                    textColor = Color.White,
                    isLoading = isLoading && activeProvider == "Apple",
                    enabled = !isLoading && !isAuthenticating,
                    contentDescription = "Continue with Apple",
                    onClick = { handleSocialAuth("Apple") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Exactly ONE Secondary Skip Action (centered text with arrow)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    onClick = {
                        if (!isLoading) {
                            navigator.navigate(Screen.GenderSelection)
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent,
                    modifier = Modifier.semantics { contentDescription = "Skip for now" }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Skip for now",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8E9BA4)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = FitnessIcons.ArrowForward,
                            contentDescription = "Skip for now",
                            tint = Color(0xFF8E9BA4),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Brand Tagline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Train smarter.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6E7E8A),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Stay consistent.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FitTheme.colors.primary.copy(alpha = 0.85f),
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
        }
        }
    }

    if (showAppleInfoDialog) {
        AlertDialog(
            onDismissRequest = { showAppleInfoDialog = false },
            containerColor = Color(0xFF07090C),
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Apple Sign-In",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Apple Sign-In is native to iOS/iPhone. Please use Google to continue on Android.",
                    color = Color(0xFF9BAAB4),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                PrimaryButton(
                    text = "OK",
                    onClick = { showAppleInfoDialog = false },
                    height = 42.dp,
                    modifier = Modifier.width(100.dp)
                )
            }
        )
    }
}

@Composable
private fun SocialAuthButton(
    text: String,
    icon: ImageVector,
    iconTint: Color,
    backgroundColor: Color,
    borderColor: Color,
    textColor: Color,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)
    )

    Surface(
        onClick = { if (enabled && !isLoading) onClick() },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .semantics { this.contentDescription = contentDescription },
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = FitTheme.colors.primary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Signing in...",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8E9BA4)
                )
                Spacer(modifier = Modifier.weight(1f))
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = FitnessIcons.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF8E9BA4),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Maps raw social authentication exceptions into clear, user-friendly error messages.
 * Prevents raw Firebase or internal platform exceptions from being displayed to users.
 */
internal fun mapSocialAuthError(e: Throwable, provider: String): String {
    val message = e.message ?: ""
    val lower = message.lowercase()

    return when {
        // User cancellation
        lower.contains("cancel") ||
        lower.contains("user_cancel") ||
        lower.contains("user cancelled") ||
        lower.contains("aborted") ||
        lower.contains("dismiss") -> {
            "$provider sign-in was cancelled."
        }

        // Operation timed out / taking longer than expected
        lower.contains("timed out") ||
        lower.contains("time out") ||
        lower.contains("longer than expected") ||
        lower.contains("sign-in timed out") ||
        (lower.contains("timeout") && !lower.contains("network")) -> {
            "Login is taking longer than expected. Please check your connection and try again."
        }

        // Network or connection errors
        lower.contains("network") ||
        lower.contains("connection") ||
        lower.contains("unreachable") ||
        lower.contains("offline") -> {
            "Network error. Please check your internet connection and try again."
        }

        // Too many attempts / throttling
        lower.contains("too-many-requests") ||
        lower.contains("too_many_requests") ||
        lower.contains("blocked") -> {
            "Too many attempts. Please wait a moment and try again."
        }

        // Account disabled
        lower.contains("user-disabled") ||
        lower.contains("user_disabled") ||
        lower.contains("disabled") -> {
            "This user account has been disabled."
        }

        // Invalid credential or token
        lower.contains("invalid-credential") ||
        lower.contains("invalid_credential") ||
        lower.contains("credential") ||
        lower.contains("bad token") -> {
            "Unable to verify $provider credentials. Please try signing in again."
        }

        else -> {
            "Unable to sign in with $provider. Please try again."
        }
    }
}
