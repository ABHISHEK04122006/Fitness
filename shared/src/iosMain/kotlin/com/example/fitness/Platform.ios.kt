package com.example.fitness

import platform.UIKit.UIDevice
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitWeekday
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSDate
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.Foundation.dataWithContentsOfURL
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun getCurrentDayOfWeekIndex(): Int {
    val calendar = NSCalendar.currentCalendar
    val weekday = calendar.component(NSCalendarUnitWeekday, fromDate = NSDate()).toInt()
    return when (weekday) {
        2 -> 0 // Monday
        3 -> 1 // Tuesday
        4 -> 2 // Wednesday
        5 -> 3 // Thursday
        6 -> 4 // Friday
        7 -> 5 // Saturday
        1 -> 6 // Sunday
        else -> 0
    }
}

actual fun getCurrentWeekDayItems(): List<DayItemData> {
    val todayIdx = getCurrentDayOfWeekIndex()
    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val calendar = NSCalendar.currentCalendar
    val todayDom = calendar.component(NSCalendarUnitDay, fromDate = NSDate()).toInt()

    return (0..6).map { index ->
        val offset = index - todayIdx
        val dom = (todayDom + offset).let { if (it <= 0) it + 30 else it }
        DayItemData(
            dayOfWeek = dayNames[index],
            dayOfMonth = dom,
            completed = false,
            isToday = index == todayIdx
        )
    }
}

private const val APPLE_EPOCH_OFFSET_SECONDS = 978307200.0

actual fun getCurrentEpochMillis(): Long {
    val date = NSDate()
    val secondsSince1970 = date.timeIntervalSinceReferenceDate + APPLE_EPOCH_OFFSET_SECONDS
    return (secondsSince1970 * 1000.0).toLong()
}

actual fun getLocalDateString(epochMillis: Long): String {
    val secondsSince2001 = (epochMillis / 1000.0) - APPLE_EPOCH_OFFSET_SECONDS
    val date = NSDate(timeIntervalSinceReferenceDate = secondsSince2001)
    val formatter = platform.Foundation.NSDateFormatter()
    formatter.dateFormat = "yyyy-MM-dd"
    return formatter.stringFromDate(date)
}

actual fun getRelativeDateDisplay(epochMillis: Long): String {
    val secondsSince2001 = (epochMillis / 1000.0) - APPLE_EPOCH_OFFSET_SECONDS
    val workoutDate = NSDate(timeIntervalSinceReferenceDate = secondsSince2001)
    val today = NSDate()
    val calendar = NSCalendar.currentCalendar

    val isToday = calendar.isDateInToday(workoutDate)
    val isYesterday = calendar.isDateInYesterday(workoutDate)

    return when {
        isToday -> "Today"
        isYesterday -> "Yesterday"
        else -> {
            val formatter = platform.Foundation.NSDateFormatter()
            formatter.dateFormat = "MMM d"
            formatter.stringFromDate(workoutDate)
        }
    }
}

actual fun getCurrentYearMonthString(): String {
    val formatter = platform.Foundation.NSDateFormatter()
    formatter.dateFormat = "yyyy-MM"
    return formatter.stringFromDate(NSDate())
}

actual fun platformLog(tag: String, message: String, throwable: Throwable?) {
    if (throwable != null) {
        println("[$tag] $message: ${throwable.message}")
    } else {
        println("[$tag] $message")
    }
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun uploadStorageBytes(reference: dev.gitlive.firebase.storage.StorageReference, bytes: ByteArray) {
    val nsData: NSData = bytes.usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong())
    }
    reference.putData(dev.gitlive.firebase.storage.Data(nsData))
}

actual fun decodeByteArrayToBitmap(bytes: ByteArray): androidx.compose.ui.graphics.ImageBitmap? {
    return try {
        org.jetbrains.skia.Image.makeFromEncoded(bytes).toComposeImageBitmap()
    } catch (_: Exception) {
        null
    }
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun fetchImageBytes(url: String): ByteArray? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
    try {
        val nsUrl = platform.Foundation.NSURL.URLWithString(url) ?: return@withContext null
        val data: NSData = NSData.dataWithContentsOfURL(nsUrl) ?: return@withContext null
        val len = data.length.toInt()
        val bytes = ByteArray(len)
        if (len > 0) {
            bytes.usePinned { pinned ->
                platform.posix.memcpy(pinned.addressOf(0), data.bytes, data.length)
            }
        }
        bytes
    } catch (_: Exception) {
        null
    }
}

actual fun extractStorageErrorInfo(e: Throwable): Pair<Int?, String?> {
    return Pair(null, e.message)
}

actual suspend fun httpPutBytesWithAuth(
    url: String,
    bearerToken: String,
    mimeType: String,
    bytes: ByteArray
): Pair<Int, String> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
    Pair(0, "Not implemented on iOS")
}

actual suspend fun httpGetBytesWithAuth(
    url: String,
    bearerToken: String
): Triple<Int, ByteArray?, String?> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
    Triple(0, null, "Not implemented on iOS")
}

actual suspend fun httpDeleteWithAuth(
    url: String,
    bearerToken: String
): Pair<Int, String> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
    Pair(0, "Not implemented on iOS")
}

actual suspend fun httpPostJsonWithAuth(
    url: String,
    bearerToken: String,
    jsonPayload: String
): Pair<Int, String> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
    Pair(0, "Not implemented on iOS")
}

actual fun cropImageBytes(
    sourceBytes: ByteArray,
    cropRect: NormalizedCropRect,
    targetMaxDimension: Int
): ByteArray? {
    return try {
        val skiaImage = org.jetbrains.skia.Image.makeFromEncoded(sourceBytes)
        val origW = skiaImage.width
        val origH = skiaImage.height

        val rawCropX = (cropRect.left * origW).toFloat()
        val rawCropY = (cropRect.top * origH).toFloat()
        val rawCropW = (cropRect.width * origW).toFloat()
        val rawCropH = (cropRect.height * origH).toFloat()

        val cropSize = minOf(rawCropW, rawCropH).coerceIn(1f, minOf(origW, origH).toFloat())
        val cropX = rawCropX.coerceIn(0f, origW.toFloat() - cropSize)
        val cropY = rawCropY.coerceIn(0f, origH.toFloat() - cropSize)

        val finalSize = minOf(cropSize.toInt(), targetMaxDimension)
        val surface = org.jetbrains.skia.Surface.makeRasterN32Premul(finalSize, finalSize)
        val canvas = surface.canvas
        val srcRect = org.jetbrains.skia.Rect.makeXYWH(cropX, cropY, cropSize, cropSize)
        val dstRect = org.jetbrains.skia.Rect.makeXYWH(0f, 0f, finalSize.toFloat(), finalSize.toFloat())
        canvas.drawImageRect(skiaImage, srcRect, dstRect)

        val snapshot = surface.makeImageSnapshot()
        val encodedData = snapshot.encodeToData(org.jetbrains.skia.EncodedImageFormat.JPEG, 90) ?: return sourceBytes
        encodedData.bytes
    } catch (_: Throwable) {
        sourceBytes
    }
}

actual fun syncPlatformNotificationToken(uid: String) {
    // iOS APNs / FCM token synchronization handled via APNs delegate when configured
}

actual fun clearPlatformNotificationToken(uid: String) {
    // iOS token cleanup
}

actual fun <K, V> createThreadSafeMap(): MutableMap<K, V> = mutableMapOf<K, V>()

actual fun <R> synchronizedBlock(lock: Any, block: () -> R): R = block()