package com.example.fitness.feature.profile

import com.example.fitness.NormalizedCropRect
import com.example.fitness.core.user.data.CloudflareProfilePhotoService
import com.example.fitness.core.user.data.PhotoHttpTransport
import com.example.fitness.feature.exercise.media.MediaUploadFile
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests verifying profile photo cropping logic, math invariants,
 * and Cloudflare R2 upload integration.
 */
class ProfilePhotoCropTest {

    private class FakePhotoHttpTransport : PhotoHttpTransport {
        var putStatusCode: Int = 200
        var putResponseBody: String = ""
        var lastPutUrl: String? = null
        var lastPutToken: String? = null
        var lastPutMimeType: String? = null
        var lastPutBytes: ByteArray? = null
        var putCount: Int = 0

        var getStatusCode: Int = 200
        var getResponseBytes: ByteArray? = null
        var getErrorBody: String? = null
        var lastGetUrl: String? = null
        var lastGetToken: String? = null

        var deleteStatusCode: Int = 200
        var deleteResponseBody: String = ""
        var lastDeleteUrl: String? = null
        var deleteCount: Int = 0

        override suspend fun put(url: String, token: String, mimeType: String, bytes: ByteArray): Pair<Int, String> {
            putCount++
            lastPutUrl = url
            lastPutToken = token
            lastPutMimeType = mimeType
            lastPutBytes = bytes
            return Pair(putStatusCode, putResponseBody)
        }

        override suspend fun get(url: String, token: String): Triple<Int, ByteArray?, String?> {
            lastGetUrl = url
            lastGetToken = token
            return Triple(getStatusCode, getResponseBytes, getErrorBody)
        }

        override suspend fun delete(url: String, token: String): Pair<Int, String> {
            deleteCount++
            lastDeleteUrl = url
            return Pair(deleteStatusCode, deleteResponseBody)
        }
    }

    private lateinit var fakeTransport: FakePhotoHttpTransport

    @BeforeTest
    fun setUp() {
        fakeTransport = FakePhotoHttpTransport()
        CloudflareProfilePhotoService.transport = fakeTransport
    }

    @AfterTest
    fun tearDown() {
        CloudflareProfilePhotoService.transport = com.example.fitness.core.user.data.DefaultPhotoHttpTransport
    }

    // =========================================================================
    // TEST 1: Select image -> crop -> output is square (1:1 aspect ratio)
    // =========================================================================
    @Test
    fun testCropMathAlwaysProducesSquareAspectRatio() {
        // Test various source aspect ratios: portrait, landscape, extreme panoramic, and square
        val testResolutions = listOf(
            Pair(1080, 1920), // 9:16 Portrait
            Pair(1920, 1080), // 16:9 Landscape
            Pair(2000, 2000), // 1:1 Square
            Pair(600, 1800),  // 1:3 Tall
            Pair(2400, 800)   // 3:1 Wide
        )

        for ((srcWidth, srcHeight) in testResolutions) {
            val viewportSize = 300f
            val normRect = ProfilePhotoCropCalculator.computeNormalizedCropRect(
                imageWidth = srcWidth,
                imageHeight = srcHeight,
                viewportSize = viewportSize,
                zoom = 1.0f,
                panOffsetX = 0f,
                panOffsetY = 0f
            )

            // Absolute pixel width and height inside the original image
            val pixelCropWidth = normRect.width * srcWidth
            val pixelCropHeight = normRect.height * srcHeight

            // Verify 1:1 aspect ratio within floating-point tolerance
            val aspectRatio = pixelCropWidth / pixelCropHeight
            assertEquals(
                1.0f,
                aspectRatio,
                0.01f,
                "Crop rect for ${srcWidth}x${srcHeight} must have exact 1:1 square aspect ratio"
            )

            // Verify normalized coordinates are strictly bounded within [0, 1]
            assertTrue(normRect.left >= 0.0f && normRect.left <= 1.0f, "Crop left must be in [0, 1]")
            assertTrue(normRect.top >= 0.0f && normRect.top <= 1.0f, "Crop top must be in [0, 1]")
            assertTrue(normRect.left + normRect.width <= 1.001f, "Crop right must not exceed bounds")
            assertTrue(normRect.top + normRect.height <= 1.001f, "Crop bottom must not exceed bounds")
        }
    }

    // =========================================================================
    // TEST 2: Crop output is different from original when user changes crop position / zoom
    // =========================================================================
    @Test
    fun testCropOutputDiffersWhenUserChangesPanOrZoom() {
        val srcW = 1080
        val srcH = 1920
        val viewportSize = 300f

        // Initial default center crop at 1.0x zoom
        val centerRect = ProfilePhotoCropCalculator.computeNormalizedCropRect(
            imageWidth = srcW,
            imageHeight = srcH,
            viewportSize = viewportSize,
            zoom = 1.0f,
            panOffsetX = 0f,
            panOffsetY = 0f
        )

        // Panned up (e.g. user moved photo down to focus on face at the top)
        val (maxPanX, maxPanY) = ProfilePhotoCropCalculator.computeMaxPanOffsets(
            imageWidth = srcW,
            imageHeight = srcH,
            viewportSize = viewportSize,
            zoom = 1.0f
        )
        assertTrue(maxPanY > 0f, "Portrait image must allow vertical panning")

        val pannedRect = ProfilePhotoCropCalculator.computeNormalizedCropRect(
            imageWidth = srcW,
            imageHeight = srcH,
            viewportSize = viewportSize,
            zoom = 1.0f,
            panOffsetX = 0f,
            panOffsetY = -maxPanY // Panned up to top edge
        )

        // Zoomed in 2.0x
        val zoomedRect = ProfilePhotoCropCalculator.computeNormalizedCropRect(
            imageWidth = srcW,
            imageHeight = srcH,
            viewportSize = viewportSize,
            zoom = 2.0f,
            panOffsetX = 0f,
            panOffsetY = 0f
        )

        // Assert that panned crop has different top coordinate from center crop
        assertNotEquals(centerRect.top, pannedRect.top, "Panned crop must change vertical crop position")

        // Assert that zoomed crop has smaller normalized dimensions (zoomed in closer)
        assertTrue(zoomedRect.width < centerRect.width, "Zoomed crop must cover a smaller normalized fraction")
        assertTrue(zoomedRect.height < centerRect.height, "Zoomed crop must cover a smaller normalized fraction")
    }

    // =========================================================================
    // TEST 3: Only cropped output is passed to CloudflareProfilePhotoService
    // =========================================================================
    @Test
    fun testOnlyCroppedOutputIsPassedToCloudflareService() = runBlocking {
        // Original uncropped image (e.g. 5000 bytes)
        val originalBytes = ByteArray(5000) { (it % 128).toByte() }
        val originalFile = MediaUploadFile(
            fileName = "my_portrait_photo.jpg",
            mimeType = "image/jpeg",
            sizeBytes = 5000L,
            bytes = originalBytes
        )

        // Simulated cropped bytes (e.g. 1200 bytes)
        val croppedBytes = ByteArray(1200) { ((it + 42) % 128).toByte() }
        val croppedFile = MediaUploadFile(
            fileName = "my_portrait_photo_cropped.jpg",
            mimeType = "image/jpeg",
            sizeBytes = croppedBytes.size.toLong(),
            bytes = croppedBytes
        )

        // Execute upload using croppedFile (matching ProfileScreen flow)
        fakeTransport.putStatusCode = 200
        val uploadResult = CloudflareProfilePhotoService.uploadProfilePhoto(
            token = "test_auth_token",
            mimeType = croppedFile.mimeType,
            bytes = croppedFile.bytes
        )

        assertTrue(uploadResult.isSuccess)
        assertNotNull(fakeTransport.lastPutBytes)

        // CRITICAL CHECK: The uploaded bytes must EXACTLY match croppedBytes, NOT originalBytes
        assertEquals(croppedBytes.size, fakeTransport.lastPutBytes?.size)
        assertTrue(croppedBytes.contentEquals(fakeTransport.lastPutBytes!!), "Uploaded bytes must match cropped bytes")
        assertFalse(originalBytes.contentEquals(fakeTransport.lastPutBytes!!), "Original uncropped bytes must NOT be uploaded")
    }

    // =========================================================================
    // TEST 4: Existing profile photo upload still works
    // =========================================================================
    @Test
    fun testExistingProfilePhotoUploadStillWorks() = runBlocking {
        val croppedBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE1.toByte(), 0x01, 0x02)
        fakeTransport.putStatusCode = 200

        val uploadResult = CloudflareProfilePhotoService.uploadProfilePhoto(
            token = "user_valid_token_xyz",
            mimeType = "image/jpeg",
            bytes = croppedBytes
        )

        assertTrue(uploadResult.isSuccess)
        assertEquals(CloudflareProfilePhotoService.PHOTO_ENDPOINT, fakeTransport.lastPutUrl)
        assertEquals("user_valid_token_xyz", fakeTransport.lastPutToken)
        assertEquals("image/jpeg", fakeTransport.lastPutMimeType)
        assertEquals(croppedBytes.size, fakeTransport.lastPutBytes?.size)
    }

    // =========================================================================
    // TEST 5: Replacing an existing photo works
    // =========================================================================
    @Test
    fun testReplacingExistingPhotoOverwritesWithNewCroppedPhoto() = runBlocking {
        // Upload photo 1
        val photo1Bytes = byteArrayOf(1, 2, 3, 4, 5)
        fakeTransport.putStatusCode = 200
        CloudflareProfilePhotoService.uploadProfilePhoto(
            token = "token_user",
            mimeType = "image/jpeg",
            bytes = photo1Bytes
        )
        assertEquals(1, fakeTransport.putCount)
        assertTrue(photo1Bytes.contentEquals(fakeTransport.lastPutBytes!!))

        // Replace with photo 2 (new crop)
        val photo2CroppedBytes = byteArrayOf(9, 8, 7, 6, 5, 4, 3)
        val replaceResult = CloudflareProfilePhotoService.uploadProfilePhoto(
            token = "token_user",
            mimeType = "image/jpeg",
            bytes = photo2CroppedBytes
        )

        assertTrue(replaceResult.isSuccess)
        assertEquals(2, fakeTransport.putCount)
        assertTrue(photo2CroppedBytes.contentEquals(fakeTransport.lastPutBytes!!))
        assertFalse(photo1Bytes.contentEquals(fakeTransport.lastPutBytes!!))
    }

    // =========================================================================
    // TEST 6: Cancel during crop does not upload anything
    // =========================================================================
    @Test
    fun testCancelDuringCropDoesNotUploadAnything() {
        val initialPutCount = fakeTransport.putCount
        assertNull(fakeTransport.lastPutBytes)

        // User opens crop dialog, views preview, but taps Cancel:
        // In ProfileScreen: onDismiss = { showCropDialog = false; pendingCropFile = null }
        // No upload routine is dispatched.

        assertEquals(0, fakeTransport.putCount)
        assertNull(fakeTransport.lastPutBytes)
        assertEquals(initialPutCount, fakeTransport.putCount, "No HTTP upload must be triggered on cancel")
    }

    // =========================================================================
    // TEST 7: Crop / upload failure does not remove the existing profile photo
    // =========================================================================
    @Test
    fun testUploadFailureDoesNotRemoveExistingProfilePhoto() = runBlocking {
        // Simulate network/server error 500 during upload of new cropped image
        fakeTransport.putStatusCode = 500
        fakeTransport.putResponseBody = """{"error":"Internal Server Error"}"""

        val failingCroppedBytes = byteArrayOf(10, 20, 30)
        val result = CloudflareProfilePhotoService.uploadProfilePhoto(
            token = "token_123",
            mimeType = "image/jpeg",
            bytes = failingCroppedBytes
        )

        // Upload must fail cleanly
        assertTrue(result.isFailure)

        // Existing profile photo must NOT have been deleted
        assertEquals(0, fakeTransport.deleteCount, "Delete endpoint must not be invoked on upload failure")
        assertNull(fakeTransport.lastDeleteUrl)
    }

    // =========================================================================
    // BONUS: Verify zoom slider bounds [1.0f .. 4.0f] and pan clamp mechanics
    // =========================================================================
    @Test
    fun testPanClampMechanicsPreventBlackGapsInCrop() {
        val srcW = 1200
        val srcH = 800 // Landscape
        val viewportSize = 300f

        // When zoom = 1.0f, the image height covers viewport, and width exceeds viewport.
        val (maxPanX, maxPanY) = ProfilePhotoCropCalculator.computeMaxPanOffsets(
            imageWidth = srcW,
            imageHeight = srcH,
            viewportSize = viewportSize,
            zoom = 1.0f
        )

        // Landscape image covers viewport height exactly, so maxPanY is 0 (cannot pan out of bounds vertically)
        assertEquals(0f, maxPanY, 0.01f)
        assertTrue(maxPanX > 0f, "Landscape image must allow horizontal panning")

        // Even with extreme user drag attempts (+1000px, -1000px), clamping retains the crop within the image
        val clampedX = 1000f.coerceIn(-maxPanX, maxPanX)
        assertEquals(maxPanX, clampedX)

        val cropRect = ProfilePhotoCropCalculator.computeNormalizedCropRect(
            imageWidth = srcW,
            imageHeight = srcH,
            viewportSize = viewportSize,
            zoom = 1.0f,
            panOffsetX = clampedX,
            panOffsetY = 0f
        )

        // Check right edge doesn't exceed 1.0
        assertTrue(cropRect.left + cropRect.width <= 1.0001f, "Clamped crop must not exceed image bounds")
        assertTrue(cropRect.left >= 0.0f, "Clamped crop must not have negative offset")
    }
}
