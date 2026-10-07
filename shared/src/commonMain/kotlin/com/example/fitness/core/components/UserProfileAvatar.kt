package com.example.fitness.core.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.decodeByteArrayToBitmap
import com.example.fitness.fetchImageBytes

/**
 * Global in-memory cache for user profile avatars to avoid re-fetching on screen transitions.
 * Isolated per session and explicitly cleared on UserSession.clear() or photo removal.
 */
object ProfileImageMemoryCache {
    private val memoryCache = com.example.fitness.createThreadSafeMap<String, ImageBitmap>()

    fun get(url: String): ImageBitmap? = memoryCache[url]

    fun put(url: String, bitmap: ImageBitmap) {
        if (url.isNotBlank()) {
            memoryCache[url] = bitmap
        }
    }

    fun remove(url: String) {
        memoryCache.remove(url)
    }

    fun clear() {
        memoryCache.clear()
    }
}

/**
 * Calculates display initials (e.g. "JD" for "John Doe", "A" for single name) with fallback.
 */
fun calculateUserInitials(name: String?, email: String? = null): String {
    val cleanName = name?.trim().orEmpty()
    val placeholderNames = setOf("name not set", "athlete", "user", "fitness user")
    if (cleanName.isNotBlank() && !placeholderNames.contains(cleanName.lowercase())) {
        val parts = cleanName.split(" ").filter { it.isNotBlank() }
        if (parts.size >= 2) {
            val first = parts[0].firstOrNull()?.uppercaseChar()
            val second = parts[1].firstOrNull()?.uppercaseChar()
            if (first != null && second != null) return "$first$second"
        }
        val first = parts.firstOrNull()?.firstOrNull()?.uppercaseChar()
        if (first != null) return "$first"
    }

    val cleanEmail = email?.trim().orEmpty()
    if (cleanEmail.isNotBlank() && cleanEmail.contains("@")) {
        val emailPrefix = cleanEmail.substringBefore("@").trim()
        val first = emailPrefix.firstOrNull()?.uppercaseChar()
        if (first != null && first.isLetterOrDigit()) return "$first"
    }

    return "A"
}

/**
 * Dynamic User Profile Avatar component.
 * Displays real image if profileImageUrl is present, otherwise falls back to uppercase initials.
 */
@Composable
fun UserProfileAvatar(
    imageUrl: String?,
    name: String?,
    email: String? = null,
    size: Dp = 80.dp,
    borderWidth: Dp = 2.dp,
    borderColor: Color = FitTheme.colors.primary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    var loadedBitmap by remember(imageUrl) {
        mutableStateOf(imageUrl?.let { ProfileImageMemoryCache.get(it) })
    }

    LaunchedEffect(imageUrl) {
        if (!imageUrl.isNullOrBlank()) {
            val cached = ProfileImageMemoryCache.get(imageUrl)
            if (cached != null) {
                loadedBitmap = cached
            } else {
                val bmp = kotlinx.coroutines.withContext(com.example.fitness.core.coroutine.AppDispatchers.IO) {
                    val bytes = if (imageUrl == com.example.fitness.core.user.data.CloudflareProfilePhotoService.PHOTO_ENDPOINT) {
                        val currentUser = try { com.example.fitness.core.firebase.FitnessFirebase.auth.currentUser } catch (_: Exception) { null }
                        val token = try { currentUser?.getIdToken(false) } catch (_: Exception) { null }
                        if (!token.isNullOrBlank()) {
                            com.example.fitness.core.user.data.CloudflareProfilePhotoService.getProfilePhoto(token).getOrNull()
                        } else null
                    } else {
                        fetchImageBytes(imageUrl)
                    }
                    if (bytes != null && bytes.isNotEmpty()) {
                        kotlinx.coroutines.withContext(com.example.fitness.core.coroutine.AppDispatchers.Default) {
                            decodeByteArrayToBitmap(bytes)
                        }
                    } else null
                }
                if (bmp != null) {
                    ProfileImageMemoryCache.put(imageUrl, bmp)
                    loadedBitmap = bmp
                }
            }
        } else {
            loadedBitmap = null
        }
    }

    val initials = remember(name, email) {
        calculateUserInitials(name, email)
    }

    val clickableModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Box(
        modifier = clickableModifier
            .size(size)
            .clip(CircleShape)
            .background(FitTheme.colors.surfaceVariant)
            .border(borderWidth, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val bmp = loadedBitmap
        if (bmp != null) {
            Image(
                bitmap = bmp,
                contentDescription = "Profile Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val fontSize = (size.value * 0.36f).sp
            Text(
                text = initials,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = FitTheme.colors.primary
            )
        }
    }
}
