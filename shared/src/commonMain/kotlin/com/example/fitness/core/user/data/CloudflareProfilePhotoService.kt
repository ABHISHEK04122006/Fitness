package com.example.fitness.core.user.data

import com.example.fitness.httpDeleteWithAuth
import com.example.fitness.httpGetBytesWithAuth
import com.example.fitness.httpPutBytesWithAuth

/**
 * Transport interface for profile photo HTTP operations to Cloudflare Worker R2.
 * Allows decoupling and clean mock injection for unit testing.
 */
interface PhotoHttpTransport {
    suspend fun put(url: String, token: String, mimeType: String, bytes: ByteArray): Pair<Int, String>
    suspend fun get(url: String, token: String): Triple<Int, ByteArray?, String?>
    suspend fun delete(url: String, token: String): Pair<Int, String>
}

/**
 * Default HTTP transport delegating to platform-specific HttpURLConnection implementations.
 */
object DefaultPhotoHttpTransport : PhotoHttpTransport {
    override suspend fun put(url: String, token: String, mimeType: String, bytes: ByteArray): Pair<Int, String> =
        httpPutBytesWithAuth(url, token, mimeType, bytes)

    override suspend fun get(url: String, token: String): Triple<Int, ByteArray?, String?> =
        httpGetBytesWithAuth(url, token)

    override suspend fun delete(url: String, token: String): Pair<Int, String> =
        httpDeleteWithAuth(url, token)
}

/**
 * Service managing user profile photo interactions with Cloudflare Worker R2.
 * Endpoints:
 * - PUT /v1/profile/photo: Uploads image binary with MIME type and Bearer token (enforces max 5 MB).
 * - GET /v1/profile/photo: Retrieves image binary with Bearer token (returns null if 404).
 * - DELETE /v1/profile/photo: Deletes image with Bearer token.
 *
 * Security:
 * - Never prints, logs, or persists Firebase ID tokens.
 * - Does not store R2 access keys in client code (Worker mediates all R2 access).
 */
object CloudflareProfilePhotoService {
    const val WORKER_BASE_URL = "https://athgym-r2-api.prayasingale0909.workers.dev"
    const val PHOTO_ENDPOINT = "$WORKER_BASE_URL/v1/profile/photo"
    const val MAX_PHOTO_SIZE_BYTES = 5 * 1024 * 1024L // 5 MB Worker limit

    var transport: PhotoHttpTransport = DefaultPhotoHttpTransport

    /**
     * Uploads the user's profile photo to Cloudflare Worker R2 via PUT.
     * Enforces the 5 MB maximum size limit.
     */
    suspend fun uploadProfilePhoto(
        token: String,
        mimeType: String,
        bytes: ByteArray
    ): Result<ByteArray> {
        if (token.isBlank()) {
            return Result.failure(IllegalStateException("Authentication expired. Please sign in again."))
        }
        if (bytes.isEmpty()) {
            return Result.failure(IllegalArgumentException("Selected image file is empty."))
        }
        if (bytes.size > MAX_PHOTO_SIZE_BYTES) {
            return Result.failure(IllegalArgumentException("Selected image exceeds maximum allowed size of 5 MB."))
        }

        val effectiveMime = when {
            mimeType.isNotBlank() && mimeType.contains("image/") -> mimeType
            else -> "image/jpeg"
        }

        val (statusCode, responseBody) = transport.put(PHOTO_ENDPOINT, token, effectiveMime, bytes)
        return when (statusCode) {
            200, 201 -> {
                Result.success(bytes)
            }
            400 -> {
                Result.failure(IllegalArgumentException("Invalid image format. Please select a valid photo."))
            }
            401 -> {
                Result.failure(IllegalStateException("Authentication expired. Please sign in again."))
            }
            413 -> {
                Result.failure(IllegalArgumentException("Selected image exceeds maximum allowed size of 5 MB."))
            }
            else -> {
                val errorMsg = if (responseBody.isNotBlank() && responseBody.length < 100) {
                    responseBody.trim()
                } else {
                    "Failed to upload photo (Status $statusCode). Please try again."
                }
                Result.failure(IllegalStateException(errorMsg))
            }
        }
    }

    /**
     * Fetches the user's profile photo binary from Cloudflare Worker R2 via GET.
     * Returns Result.success(null) if no photo exists (404).
     */
    suspend fun getProfilePhoto(
        token: String
    ): Result<ByteArray?> {
        if (token.isBlank()) {
            return Result.failure(IllegalStateException("Authentication expired. Please sign in again."))
        }

        val (statusCode, bytes, errorBody) = transport.get(PHOTO_ENDPOINT, token)
        return when (statusCode) {
            200 -> {
                if (bytes != null && bytes.isNotEmpty()) {
                    Result.success(bytes)
                } else {
                    Result.success(null)
                }
            }
            404 -> {
                // Profile photo not found in R2
                Result.success(null)
            }
            401 -> {
                Result.failure(IllegalStateException("Authentication expired. Please sign in again."))
            }
            else -> {
                Result.failure(IllegalStateException("Failed to retrieve profile photo (Status $statusCode)."))
            }
        }
    }

    /**
     * Deletes the user's profile photo from Cloudflare Worker R2 via DELETE.
     * Treats 404 as successful (idempotent deletion).
     */
    suspend fun deleteProfilePhoto(
        token: String
    ): Result<Unit> {
        if (token.isBlank()) {
            return Result.failure(IllegalStateException("Authentication expired. Please sign in again."))
        }

        val (statusCode, responseBody) = transport.delete(PHOTO_ENDPOINT, token)
        return when (statusCode) {
            200, 204, 404 -> {
                Result.success(Unit)
            }
            401 -> {
                Result.failure(IllegalStateException("Authentication expired. Please sign in again."))
            }
            else -> {
                Result.failure(IllegalStateException("Failed to delete photo. Please try again."))
            }
        }
    }
}
