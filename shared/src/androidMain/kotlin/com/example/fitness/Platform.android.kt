package com.example.fitness

import android.os.Build
import androidx.compose.ui.graphics.asImageBitmap
import java.util.Calendar

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getCurrentDayOfWeekIndex(): Int {
    val cal = Calendar.getInstance()
    return when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6
        else -> 0
    }
}

actual fun getCurrentWeekDayItems(): List<DayItemData> {
    val todayCal = Calendar.getInstance()
    val todayDow = when (todayCal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6
        else -> 0
    }

    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_MONTH, -todayDow)

    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    return (0..6).map { index ->
        val dom = cal.get(Calendar.DAY_OF_MONTH)
        cal.add(Calendar.DAY_OF_MONTH, 1)
        DayItemData(
            dayOfWeek = dayNames[index],
            dayOfMonth = dom,
            completed = false,
            isToday = index == todayDow
        )
    }
}

actual fun getCurrentEpochMillis(): Long = System.currentTimeMillis()

actual fun getLocalDateString(epochMillis: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
    return sdf.format(java.util.Date(epochMillis))
}

actual fun getRelativeDateDisplay(epochMillis: Long): String {
    val todayCal = Calendar.getInstance()
    val workoutCal = Calendar.getInstance().apply { timeInMillis = epochMillis }

    val isSameYear = todayCal.get(Calendar.YEAR) == workoutCal.get(Calendar.YEAR)
    val isToday = isSameYear && todayCal.get(Calendar.DAY_OF_YEAR) == workoutCal.get(Calendar.DAY_OF_YEAR)

    todayCal.add(Calendar.DAY_OF_YEAR, -1)
    val isYesterday = isSameYear && todayCal.get(Calendar.DAY_OF_YEAR) == workoutCal.get(Calendar.DAY_OF_YEAR)

    return when {
        isToday -> "Today"
        isYesterday -> "Yesterday"
        else -> {
            val sdf = java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault())
            sdf.format(java.util.Date(epochMillis))
        }
    }
}

actual fun getCurrentYearMonthString(): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
    return sdf.format(java.util.Date())
}

actual fun platformLog(tag: String, message: String, throwable: Throwable?) {
    try {
        if (throwable != null) {
            android.util.Log.e(tag, message, throwable)
        } else {
            android.util.Log.i(tag, message)
        }
    } catch (_: Throwable) {
        // Unit test runner on JVM where android.util.Log is not mocked
    }
    println("[$tag] $message")
}

actual suspend fun uploadStorageBytes(reference: dev.gitlive.firebase.storage.StorageReference, bytes: ByteArray) {
    reference.putData(dev.gitlive.firebase.storage.Data(bytes))
}

actual fun decodeByteArrayToBitmap(bytes: ByteArray): androidx.compose.ui.graphics.ImageBitmap? {
    return try {
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        bitmap?.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}

actual suspend fun fetchImageBytes(url: String): ByteArray? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    try {
        java.net.URL(url).openStream().use { it.readBytes() }
    } catch (_: Exception) {
        null
    }
}

actual fun extractStorageErrorInfo(e: Throwable): Pair<Int?, String?> {
    val storageEx = (e as? com.google.firebase.storage.StorageException)
        ?: (e.cause as? com.google.firebase.storage.StorageException)
    return Pair(storageEx?.errorCode, storageEx?.message)
}

actual suspend fun httpPutBytesWithAuth(
    url: String,
    bearerToken: String,
    mimeType: String,
    bytes: ByteArray
): Pair<Int, String> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    var connection: java.net.HttpURLConnection? = null
    try {
        val urlObj = java.net.URL(url)
        connection = (urlObj.openConnection() as java.net.HttpURLConnection).apply {
            requestMethod = "PUT"
            setRequestProperty("Authorization", "Bearer $bearerToken")
            setRequestProperty("Content-Type", mimeType.ifBlank { "image/jpeg" })
            connectTimeout = 30000
            readTimeout = 30000
            doOutput = true
            setFixedLengthStreamingMode(bytes.size)
        }

        connection.outputStream.use { out ->
            out.write(bytes)
            out.flush()
        }

        val statusCode = connection.responseCode
        val stream = if (statusCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }
        val responseBody = stream?.bufferedReader()?.use { it.readText() } ?: ""
        Pair(statusCode, responseBody)
    } catch (e: Exception) {
        Pair(-1, e.message ?: "Network error during upload")
    } finally {
        try {
            connection?.disconnect()
        } catch (_: Exception) {}
    }
}

actual suspend fun httpGetBytesWithAuth(
    url: String,
    bearerToken: String
): Triple<Int, ByteArray?, String?> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    var connection: java.net.HttpURLConnection? = null
    try {
        val urlObj = java.net.URL(url)
        connection = (urlObj.openConnection() as java.net.HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Bearer $bearerToken")
            connectTimeout = 15000
            readTimeout = 15000
        }

        val statusCode = connection.responseCode
        if (statusCode in 200..299) {
            val bytes = connection.inputStream.use { it.readBytes() }
            Triple(statusCode, bytes, null)
        } else {
            val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }
            Triple(statusCode, null, errorBody)
        }
    } catch (e: Exception) {
        Triple(-1, null, e.message ?: "Network error")
    } finally {
        try {
            connection?.disconnect()
        } catch (_: Exception) {}
    }
}

actual suspend fun httpDeleteWithAuth(
    url: String,
    bearerToken: String
): Pair<Int, String> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    var connection: java.net.HttpURLConnection? = null
    try {
        val urlObj = java.net.URL(url)
        connection = (urlObj.openConnection() as java.net.HttpURLConnection).apply {
            requestMethod = "DELETE"
            setRequestProperty("Authorization", "Bearer $bearerToken")
            connectTimeout = 15000
            readTimeout = 15000
        }

        val statusCode = connection.responseCode
        val stream = if (statusCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }
        val responseBody = stream?.bufferedReader()?.use { it.readText() } ?: ""
        Pair(statusCode, responseBody)
    } catch (e: Exception) {
        Pair(-1, e.message ?: "Network error during delete")
    } finally {
        try {
            connection?.disconnect()
        } catch (_: Exception) {}
    }
}

actual suspend fun httpPostJsonWithAuth(
    url: String,
    bearerToken: String,
    jsonPayload: String
): Pair<Int, String> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    var connection: java.net.HttpURLConnection? = null
    try {
        val urlObj = java.net.URL(url)
        connection = (urlObj.openConnection() as java.net.HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            if (bearerToken.isNotBlank()) {
                setRequestProperty("Authorization", "Bearer $bearerToken")
            }
            connectTimeout = 15000
            readTimeout = 15000
        }

        connection.outputStream.use { os ->
            os.write(jsonPayload.toByteArray(Charsets.UTF_8))
            os.flush()
        }

        val statusCode = connection.responseCode
        val stream = if (statusCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }
        val responseBody = stream?.bufferedReader()?.use { it.readText() } ?: ""
        Pair(statusCode, responseBody)
    } catch (e: Exception) {
        Pair(-1, e.message ?: "Network error during POST request")
    } finally {
        try {
            connection?.disconnect()
        } catch (_: Exception) {}
    }
}

actual fun cropImageBytes(
    sourceBytes: ByteArray,
    cropRect: NormalizedCropRect,
    targetMaxDimension: Int
): ByteArray? {
    return try {
        var bitmap = android.graphics.BitmapFactory.decodeByteArray(sourceBytes, 0, sourceBytes.size) ?: return null

        // Handle EXIF orientation if present
        try {
            val exif = android.media.ExifInterface(java.io.ByteArrayInputStream(sourceBytes))
            val orientation = exif.getAttributeInt(
                android.media.ExifInterface.TAG_ORIENTATION,
                android.media.ExifInterface.ORIENTATION_NORMAL
            )
            val rotationDegrees = when (orientation) {
                android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            if (rotationDegrees != 0f) {
                val matrix = android.graphics.Matrix().apply { postRotate(rotationDegrees) }
                val rotated = android.graphics.Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (rotated != bitmap) {
                    bitmap.recycle()
                    bitmap = rotated
                }
            }
        } catch (_: Throwable) {}

        val origW = bitmap.width
        val origH = bitmap.height

        val rawCropX = (cropRect.left * origW).toInt()
        val rawCropY = (cropRect.top * origH).toInt()
        val rawCropW = (cropRect.width * origW).toInt()
        val rawCropH = (cropRect.height * origH).toInt()

        // Enforce square crop: size is minOf(rawCropW, rawCropH)
        val cropSize = minOf(rawCropW, rawCropH).coerceIn(1, minOf(origW, origH))
        val cropX = rawCropX.coerceIn(0, origW - cropSize)
        val cropY = rawCropY.coerceIn(0, origH - cropSize)

        val cropped = android.graphics.Bitmap.createBitmap(bitmap, cropX, cropY, cropSize, cropSize)
        val finalSize = minOf(cropSize, targetMaxDimension)
        val finalBitmap = if (finalSize != cropSize && finalSize > 0) {
            val scaled = android.graphics.Bitmap.createScaledBitmap(cropped, finalSize, finalSize, true)
            if (scaled != cropped) cropped.recycle()
            scaled
        } else {
            cropped
        }

        val outStream = java.io.ByteArrayOutputStream()
        finalBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outStream)
        if (finalBitmap != bitmap) finalBitmap.recycle()
        bitmap.recycle()

        outStream.toByteArray()
    } catch (_: Throwable) {
        null
    }
}

actual fun syncPlatformNotificationToken(uid: String) {
    if (uid.isNotBlank()) {
        val ctx = com.example.fitness.core.auth.ActivityProvider.currentActivity?.applicationContext
        if (ctx != null) {
            com.example.fitness.notifications.FcmTokenManager.syncToken(ctx, uid)
        } else {
            com.example.fitness.notifications.FcmTokenManager.syncTokenForCurrentUser()
        }
    }
}

actual fun clearPlatformNotificationToken(uid: String) {
    if (uid.isNotBlank()) {
        val ctx = com.example.fitness.core.auth.ActivityProvider.currentActivity?.applicationContext
        if (ctx != null) {
            com.example.fitness.notifications.FcmTokenManager.clearDeviceToken(ctx, uid)
        }
    }
}

actual fun <K, V> createThreadSafeMap(): MutableMap<K, V> = java.util.concurrent.ConcurrentHashMap<K, V>()

actual fun <R> synchronizedBlock(lock: Any, block: () -> R): R = synchronized(lock) { block() }