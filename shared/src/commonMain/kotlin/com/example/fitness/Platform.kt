package com.example.fitness

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

data class DayItemData(
    val dayOfWeek: String,
    val dayOfMonth: Int,
    val completed: Boolean,
    val isToday: Boolean
)

expect fun getCurrentDayOfWeekIndex(): Int // 0=Mon, 1=Tue, 2=Wed, 3=Thu, 4=Fri, 5=Sat, 6=Sun
expect fun getCurrentWeekDayItems(): List<DayItemData>

expect fun getCurrentEpochMillis(): Long
expect fun getLocalDateString(epochMillis: Long): String // "YYYY-MM-DD"
expect fun getRelativeDateDisplay(epochMillis: Long): String // "Today", "Yesterday", or "MMM d"
expect fun getCurrentYearMonthString(): String // "YYYY-MM"

expect fun platformLog(tag: String, message: String, throwable: Throwable? = null)

expect suspend fun uploadStorageBytes(reference: dev.gitlive.firebase.storage.StorageReference, bytes: ByteArray)

expect fun decodeByteArrayToBitmap(bytes: ByteArray): androidx.compose.ui.graphics.ImageBitmap?

expect suspend fun fetchImageBytes(url: String): ByteArray?

expect fun extractStorageErrorInfo(e: Throwable): Pair<Int?, String?>

expect suspend fun httpPutBytesWithAuth(url: String, bearerToken: String, mimeType: String, bytes: ByteArray): Pair<Int, String>

expect suspend fun httpGetBytesWithAuth(url: String, bearerToken: String): Triple<Int, ByteArray?, String?>

expect suspend fun httpDeleteWithAuth(url: String, bearerToken: String): Pair<Int, String>

expect suspend fun httpPostJsonWithAuth(url: String, bearerToken: String, jsonPayload: String): Pair<Int, String>

data class NormalizedCropRect(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
)

expect fun cropImageBytes(
    sourceBytes: ByteArray,
    cropRect: NormalizedCropRect,
    targetMaxDimension: Int = 720
): ByteArray?

expect fun syncPlatformNotificationToken(uid: String)
expect fun clearPlatformNotificationToken(uid: String)

expect fun <K, V> createThreadSafeMap(): MutableMap<K, V>
expect fun <R> synchronizedBlock(lock: Any, block: () -> R): R