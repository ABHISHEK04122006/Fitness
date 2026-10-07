package com.example.fitness.feature.profile

import com.example.fitness.core.user.data.CloudflareProfilePhotoService
import com.example.fitness.core.user.data.PhotoHttpTransport
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CloudflareProfilePhotoServiceTest {

    private class FakePhotoHttpTransport : PhotoHttpTransport {
        var putStatusCode: Int = 200
        var putResponseBody: String = ""
        var lastPutUrl: String? = null
        var lastPutToken: String? = null
        var lastPutMimeType: String? = null
        var lastPutBytes: ByteArray? = null

        var getStatusCode: Int = 200
        var getResponseBytes: ByteArray? = null
        var getErrorBody: String? = null
        var lastGetUrl: String? = null
        var lastGetToken: String? = null

        var deleteStatusCode: Int = 200
        var deleteResponseBody: String = ""
        var lastDeleteUrl: String? = null
        var lastDeleteToken: String? = null

        override suspend fun put(url: String, token: String, mimeType: String, bytes: ByteArray): Pair<Int, String> {
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
            lastDeleteUrl = url
            lastDeleteToken = token
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

    // 1. Authenticated upload
    @Test
    fun testAuthenticatedUploadSuccess() = runBlocking {
        val testBytes = ByteArray(1024) { (it % 256).toByte() }
        fakeTransport.putStatusCode = 200

        val result = CloudflareProfilePhotoService.uploadProfilePhoto(
            token = "valid_firebase_id_token",
            mimeType = "image/jpeg",
            bytes = testBytes
        )

        assertTrue(result.isSuccess)
        assertEquals(testBytes.size, result.getOrNull()?.size)
        assertEquals(CloudflareProfilePhotoService.PHOTO_ENDPOINT, fakeTransport.lastPutUrl)
        assertEquals("valid_firebase_id_token", fakeTransport.lastPutToken)
        assertEquals("image/jpeg", fakeTransport.lastPutMimeType)
    }

    // 2. Successful upload response
    @Test
    fun testSuccessfulUploadResponse() = runBlocking {
        val sampleImage = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte())
        fakeTransport.putStatusCode = 201

        val result = CloudflareProfilePhotoService.uploadProfilePhoto(
            token = "fresh_token_123",
            mimeType = "image/png",
            bytes = sampleImage
        )

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull().contentEquals(sampleImage))
    }

    // 3. GET profile photo success
    @Test
    fun testGetProfilePhotoSuccess() = runBlocking {
        val sampleImage = byteArrayOf(1, 2, 3, 4, 5)
        fakeTransport.getStatusCode = 200
        fakeTransport.getResponseBytes = sampleImage

        val result = CloudflareProfilePhotoService.getProfilePhoto("test_token")

        assertTrue(result.isSuccess)
        val bytes = result.getOrNull()
        assertTrue(bytes != null && bytes.contentEquals(sampleImage))
        assertEquals(CloudflareProfilePhotoService.PHOTO_ENDPOINT, fakeTransport.lastGetUrl)
        assertEquals("test_token", fakeTransport.lastGetToken)
    }

    // 4. Missing profile photo (404 on GET) returns null
    @Test
    fun testMissingProfilePhotoReturnsNull() = runBlocking {
        fakeTransport.getStatusCode = 404
        fakeTransport.getResponseBytes = null
        fakeTransport.getErrorBody = "Profile photo not found"

        val result = CloudflareProfilePhotoService.getProfilePhoto("valid_token")

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull(), "404 on GET must return Result.success(null)")
    }

    // 5. Unauthenticated request (401)
    @Test
    fun testUnauthenticatedUploadReturnsFailure() = runBlocking {
        fakeTransport.putStatusCode = 401
        fakeTransport.putResponseBody = "Invalid or expired Firebase ID token"

        val result = CloudflareProfilePhotoService.uploadProfilePhoto(
            token = "expired_token",
            mimeType = "image/jpeg",
            bytes = ByteArray(500)
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Authentication expired") == true)
    }

    @Test
    fun testBlankTokenReturnsFailureImmediately() = runBlocking {
        val result = CloudflareProfilePhotoService.uploadProfilePhoto(
            token = "   ",
            mimeType = "image/jpeg",
            bytes = ByteArray(100)
        )

        assertTrue(result.isFailure)
        assertNull(fakeTransport.lastPutToken, "No network request should be dispatched with blank token")
    }

    // 6. Oversized image (> 5 MB)
    @Test
    fun testOversizedImageFailsValidation() = runBlocking {
        val oversizedBytes = ByteArray((5 * 1024 * 1024) + 10)

        val result = CloudflareProfilePhotoService.uploadProfilePhoto(
            token = "token",
            mimeType = "image/jpeg",
            bytes = oversizedBytes
        )

        assertTrue(result.isFailure)
        val msg = result.exceptionOrNull()?.message.orEmpty()
        assertTrue(msg.contains("5 MB"), "Error message should mention the 5 MB limit: $msg")
        assertNull(fakeTransport.lastPutBytes, "No network request should be made for oversized payload")
    }

    @Test
    fun testServerOversizedResponse413() = runBlocking {
        fakeTransport.putStatusCode = 413
        fakeTransport.putResponseBody = "Payload Too Large"

        val result = CloudflareProfilePhotoService.uploadProfilePhoto(
            token = "token",
            mimeType = "image/jpeg",
            bytes = ByteArray(1000)
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("5 MB") == true)
    }

    // 7. Empty image fails validation
    @Test
    fun testEmptyImageFailsValidation() = runBlocking {
        val result = CloudflareProfilePhotoService.uploadProfilePhoto(
            token = "token",
            mimeType = "image/jpeg",
            bytes = ByteArray(0)
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("empty") == true)
    }

    // 8. DELETE profile photo
    @Test
    fun testDeleteProfilePhotoSuccess() = runBlocking {
        fakeTransport.deleteStatusCode = 200

        val result = CloudflareProfilePhotoService.deleteProfilePhoto("valid_token")

        assertTrue(result.isSuccess)
        assertEquals(CloudflareProfilePhotoService.PHOTO_ENDPOINT, fakeTransport.lastDeleteUrl)
        assertEquals("valid_token", fakeTransport.lastDeleteToken)
    }

    @Test
    fun testDeleteMissingProfilePhotoIsIdempotent() = runBlocking {
        fakeTransport.deleteStatusCode = 404 // Already absent

        val result = CloudflareProfilePhotoService.deleteProfilePhoto("valid_token")

        assertTrue(result.isSuccess, "Deleting an already-deleted photo should be idempotent success")
    }

    @Test
    fun testDeleteUnauthenticatedReturnsFailure() = runBlocking {
        fakeTransport.deleteStatusCode = 401

        val result = CloudflareProfilePhotoService.deleteProfilePhoto("bad_token")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Authentication expired") == true)
    }
}
