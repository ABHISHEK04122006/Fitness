package com.example.fitness.feature.admin.data

import com.example.fitness.core.auth.RoleResolver
import com.example.fitness.core.auth.UserRole
import com.example.fitness.core.firebase.FitnessFirebase
import com.example.fitness.core.session.UserSession
import com.example.fitness.core.user.data.FirebaseUserRepository
import com.example.fitness.feature.admin.model.AdminAudienceType
import com.example.fitness.feature.admin.model.AdminNotificationRecord
import com.example.fitness.feature.admin.model.AdminNotificationRequest
import com.example.fitness.feature.admin.model.AdminNotificationSendResult
import com.example.fitness.feature.notifications.model.NotificationItem
import com.example.fitness.getCurrentEpochMillis
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.DocumentSnapshot

/**
 * Shared repository for the ATHGYM Admin Notification Composer & Broadcast Center.
 * Handles input validation, target audience resolution, Firestore notification persistence,
 * system announcement publishing, and admin audit trail logging.
 */
class AdminNotificationRepository(
    private val userRepository: FirebaseUserRepository = FirebaseUserRepository()
) {
    private val firestore get() = FitnessFirebase.firestore

    /**
     * Validates notification payload according to business rules.
     * Returns null if valid, or a user-facing error message if validation fails.
     */
    fun validate(request: AdminNotificationRequest): String? {
        val title = request.title.trim()
        if (title.isEmpty()) {
            return "Notification title is required."
        }
        if (title.length > 100) {
            return "Title cannot exceed 100 characters."
        }

        val message = request.message.trim()
        if (message.isEmpty()) {
            return "Notification message body is required."
        }
        if (message.length > 500) {
            return "Message cannot exceed 500 characters."
        }

        if (request.audience == AdminAudienceType.SINGLE_USER &&
            request.targetUid.isNullOrBlank() &&
            request.targetUids.isEmpty()) {
            return "Please select a target athlete or recipient for notification delivery."
        }

        return null
    }

    /**
     * Dispatches a notification from an authenticated administrator.
     * Resolves the target recipients, creates user-scoped notification documents,
     * updates global system announcements if applicable, and writes an audit record
     * into admin_notification_history/{id}.
     */
    suspend fun sendNotification(
        adminUid: String,
        request: AdminNotificationRequest
    ): AdminNotificationSendResult {
        // 1. Validation check
        val validationError = validate(request)
        if (validationError != null) {
            return AdminNotificationSendResult(
                success = false,
                notificationId = "",
                targetCount = 0,
                status = "VALIDATION_ERROR",
                message = validationError
            )
        }

        // 2. Admin Authentication & Custom Claim Verification
        val currentUser = try { Firebase.auth.currentUser } catch (_: Exception) { null }
        val currentUid = currentUser?.uid ?: adminUid.takeIf { it.isNotBlank() } ?: UserSession.currentUserId ?: ""
        val isAdmin = if (currentUser != null) {
            try {
                val role = RoleResolver.resolveRole(currentUser, forceRefresh = true)
                role == UserRole.ADMIN
            } catch (_: Exception) {
                UserSession.currentUserRole == UserRole.ADMIN
            }
        } else {
            UserSession.currentUserRole == UserRole.ADMIN
        }

        println("[AdminNotificationDebug]\ncurrentUserUid = $currentUid\nisAdmin = $isAdmin")

        if (currentUid.isBlank()) {
            return AdminNotificationSendResult(
                success = false,
                notificationId = "",
                targetCount = 0,
                status = "UNAUTHENTICATED",
                message = "Authenticated administrator UID is required."
            )
        }

        if (!isAdmin) {
            return AdminNotificationSendResult(
                success = false,
                notificationId = "",
                targetCount = 0,
                status = "PERMISSION_DENIED",
                message = "Permission denied: Current user does not have administrator privileges (custom claim admin == true)."
            )
        }

        // 3. Resolve target UIDs and target Names
        val targetUids = mutableListOf<String>()
        val targetNames = mutableListOf<String>()

        if (request.targetUids.isNotEmpty()) {
            targetUids.addAll(request.targetUids.map { it.trim() }.filter { it.isNotBlank() }.distinct())
            targetNames.addAll(request.targetNames.map { it.trim() }.filter { it.isNotBlank() })
        } else if (request.audience == AdminAudienceType.SINGLE_USER) {
            val singleUid = request.targetUid?.trim().orEmpty()
            if (singleUid.isNotBlank()) {
                targetUids.add(singleUid)
                if (!request.targetName.isNullOrBlank()) {
                    targetNames.add(request.targetName.trim())
                }
            }
        } else if (request.audience == AdminAudienceType.ALL_ATHLETES) {
            val allUsers = userRepository.getAllAdminUsers()
            val athletes = allUsers.filter { it.role == UserRole.USER }
            targetUids.addAll(athletes.map { it.uid }.filter { it.isNotBlank() })
            if (targetUids.isEmpty()) {
                targetUids.addAll(allUsers.map { it.uid }.filter { it.isNotBlank() })
            }
        } else if (request.audience == AdminAudienceType.GLOBAL_ANNOUNCEMENT) {
            val allUsers = userRepository.getAllAdminUsers()
            targetUids.addAll(allUsers.map { it.uid }.filter { it.isNotBlank() })
        }

        if (targetUids.isEmpty()) {
            return AdminNotificationSendResult(
                success = false,
                notificationId = "",
                targetCount = 0,
                status = "NO_RECIPIENTS",
                message = "Please select at least one recipient."
            )
        }

        println("[AdminNotificationDebug]\ntargetCount = ${targetUids.size}\ntargetUids = $targetUids")

        val broadcastId = "notif_admin_${getCurrentEpochMillis()}_${kotlin.random.Random.nextInt(1000, 9999)}"
        val now = getCurrentEpochMillis()

        return try {
            // Global announcement publishing if applicable
            if (request.audience == AdminAudienceType.GLOBAL_ANNOUNCEMENT) {
                try {
                    val announcementData = mutableMapOf<String, Any>(
                        "id" to broadcastId,
                        "title" to request.title.trim(),
                        "message" to request.message.trim(),
                        "category" to request.category,
                        "createdAt" to now,
                        "createdBy" to currentUid
                    )
                    if (!request.actionType.isNullOrBlank()) {
                        announcementData["actionType"] = request.actionType.trim()
                    }
                    if (!request.actionId.isNullOrBlank()) {
                        announcementData["actionId"] = request.actionId.trim()
                    }
                    firestore.collection("system_announcements").document(broadcastId).set(announcementData, merge = true)
                } catch (e: Exception) {
                    println("[WARN] AdminNotificationRepository: Failed to write to system_announcements: ${e.message}")
                }
            }

            // 4. Dispatch to users/{uid}/notifications/{id} for each recipient
            var totalDevicesFound = 0
            var successfulDispatches = 0
            var lastDispatchExceptionClass: String? = null
            var lastDispatchCode: String? = null
            var lastDispatchMessage: String? = null

            targetUids.forEach { uid ->
                val notificationId = if (targetUids.size == 1) broadcastId else "${broadcastId}_${uid.take(6)}"
                val notificationDoc = mutableMapOf<String, Any>(
                    "id" to notificationId,
                    "userId" to uid,
                    "title" to request.title.trim(),
                    "message" to request.message.trim(),
                    "type" to request.category,
                    "read" to false,
                    "createdAt" to now
                )
                if (!request.actionType.isNullOrBlank()) {
                    notificationDoc["actionType"] = request.actionType.trim()
                }
                if (!request.actionId.isNullOrBlank()) {
                    notificationDoc["actionId"] = request.actionId.trim()
                }

                try {
                    firestore.collection("users")
                        .document(uid)
                        .collection("notifications")
                        .document(notificationId)
                        .set(notificationDoc, merge = false)
                    successfulDispatches++

                    // Probe registered devices count from both fcmTokens and devices subcollections
                    try {
                        val fcmSnap = firestore.collection("users").document(uid).collection("fcmTokens").get()
                        val devSnap = firestore.collection("users").document(uid).collection("devices").get()
                        val allDeviceDocs = (fcmSnap.documents.map { it.id } + devSnap.documents.map { it.id }).distinct()
                        totalDevicesFound += allDeviceDocs.size
                    } catch (_: Exception) {}
                } catch (e: Exception) {
                    val exClass = e::class.simpleName ?: "Exception"
                    val rawMsg = e.message ?: "Unknown Firestore error"
                    val fbCode = when {
                        rawMsg.contains("PERMISSION_DENIED", ignoreCase = true) -> "PERMISSION_DENIED"
                        rawMsg.contains("UNAVAILABLE", ignoreCase = true) -> "UNAVAILABLE"
                        rawMsg.contains("NOT_FOUND", ignoreCase = true) -> "NOT_FOUND"
                        rawMsg.contains("UNAUTHENTICATED", ignoreCase = true) -> "UNAUTHENTICATED"
                        else -> "UNKNOWN"
                    }
                    println("""
[AdminNotificationDebug] SEND FAILED
operation = Firestore notification create
path = users/$uid/notifications/$notificationId
exception = $exClass
firebaseCode = $fbCode
message = $rawMsg
                    """.trimIndent())
                    lastDispatchExceptionClass = exClass
                    lastDispatchCode = fbCode
                    lastDispatchMessage = rawMsg
                }
            }

            // 5. Trigger push delivery via trusted backend Cloudflare Worker POST /v1/admin/notifications/push
            var pushAccepted = 0
            var pushFailed = 0
            var pushBackendStatus = "NOT_ATTEMPTED"

            if (successfulDispatches > 0) {
                try {
                    println("[FCM] Push send request started: recipients=${targetUids.size}, devices=$totalDevicesFound")
                    val idToken = try {
                        currentUser?.getIdToken(false)?.takeIf { it.isNotBlank() }
                            ?: currentUser?.getIdToken(true).orEmpty()
                    } catch (_: Exception) { "" }
                    val workerUrl = "https://athgym-r2-api.prayasingale0909.workers.dev/v1/admin/notifications/push"
                    val audienceType = if (request.audience == AdminAudienceType.ALL_ATHLETES || request.audience == AdminAudienceType.GLOBAL_ANNOUNCEMENT) {
                        "all"
                    } else if (targetUids.size == 1) {
                        "single"
                    } else {
                        "selected"
                    }
                    val jsonPayload = buildString {
                        append("{")
                        append("\"notificationId\":\"").append(escapeJson(broadcastId)).append("\",")
                        append("\"title\":\"").append(escapeJson(request.title.trim())).append("\",")
                        append("\"message\":\"").append(escapeJson(request.message.trim())).append("\",")
                        append("\"type\":\"").append(escapeJson(request.category)).append("\",")
                        append("\"audience\":\"").append(escapeJson(audienceType)).append("\",")
                        append("\"destination\":\"notifications\",")
                        if (targetUids.size == 1) {
                            append("\"uid\":\"").append(escapeJson(targetUids.first())).append("\",")
                        }
                        append("\"targetUids\":[")
                        targetUids.forEachIndexed { i, u ->
                            if (i > 0) append(",")
                            append("\"").append(escapeJson(u)).append("\"")
                        }
                        append("]")
                        if (!request.actionType.isNullOrBlank()) {
                            append(",\"actionType\":\"").append(escapeJson(request.actionType.trim())).append("\"")
                        }
                        if (!request.actionId.isNullOrBlank()) {
                            append(",\"actionId\":\"").append(escapeJson(request.actionId.trim())).append("\"")
                        }
                        append("}")
                    }

                    if (idToken.isNotBlank()) {
                        val (httpStatus, httpResponse) = com.example.fitness.httpPostJsonWithAuth(workerUrl, idToken, jsonPayload)
                        if (httpStatus in 200..299) {
                            pushBackendStatus = "SEND_ACCEPTED"
                            val acceptedRegex = Regex(""""pushAccepted"\s*:\s*(\d+)""")
                            val failedRegex = Regex(""""failed"\s*:\s*(\d+)""")
                            pushAccepted = acceptedRegex.find(httpResponse)?.groupValues?.get(1)?.toIntOrNull() ?: totalDevicesFound
                            pushFailed = failedRegex.find(httpResponse)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                            println("[FCM] Push send accepted: count=$pushAccepted, failed=$pushFailed")
                        } else {
                            println("[FCM] Push backend response: httpStatus=$httpStatus, body=$httpResponse")
                            pushBackendStatus = "HTTP_$httpStatus"
                        }
                    } else {
                        println("[FCM] Firebase ID token was blank, skipping remote Worker push dispatch")
                        pushBackendStatus = "UNAUTHENTICATED"
                    }
                } catch (e: Exception) {
                    println("[WARN] Push backend call exception: ${e.message}")
                    pushBackendStatus = "EXCEPTION"
                }
            }

            // 6. Record audit history in admin_notification_history/{broadcastId}
            val historyRecord = mutableMapOf<String, Any>(
                "id" to broadcastId,
                "title" to request.title.trim(),
                "message" to request.message.trim(),
                "category" to request.category,
                "audience" to if (targetUids.size > 1) "MULTI_USER" else request.audience.name,
                "targetCount" to targetUids.size,
                "createdAt" to now,
                "createdBy" to currentUid,
                "status" to if (successfulDispatches == targetUids.size) (if (pushBackendStatus == "SEND_ACCEPTED") "SEND_ACCEPTED" else "DELIVERED") else if (successfulDispatches > 0) "PARTIAL" else "FAILED",
                "devicesFound" to totalDevicesFound,
                "messagesSent" to successfulDispatches,
                "pushAccepted" to pushAccepted,
                "pushFailed" to pushFailed,
                "pushStatus" to pushBackendStatus
            )
            if (targetUids.size == 1) {
                historyRecord["targetUid"] = targetUids.first()
                if (targetNames.isNotEmpty()) historyRecord["targetName"] = targetNames.first()
            } else {
                if (targetNames.isNotEmpty()) historyRecord["targetNames"] = targetNames.take(5)
            }
            if (!request.actionType.isNullOrBlank()) {
                historyRecord["actionType"] = request.actionType.trim()
            }
            if (!request.actionId.isNullOrBlank()) {
                historyRecord["actionId"] = request.actionId.trim()
            }

            try {
                firestore.collection("admin_notification_history")
                    .document(broadcastId)
                    .set(historyRecord, merge = false)
            } catch (e: Exception) {
                println("[WARN] AdminNotificationRepository: Could not write admin audit record: ${e.message}")
            }

            if (successfulDispatches == targetUids.size) {
                val successMsg = if (pushBackendStatus == "SEND_ACCEPTED") {
                    if (pushAccepted == 0 && totalDevicesFound == 0) {
                        "Notification saved to Firestore.\nRecipients: ${targetUids.size} users\nPush: 0 devices registered (Selected recipients have no active phone tokens)."
                    } else {
                        "Notification sent\nRecipients: ${targetUids.size} users\nPush accepted: $pushAccepted\nFailed: $pushFailed"
                    }
                } else if (totalDevicesFound > 0) {
                    "Notification saved to Firestore.\nRecipients: ${targetUids.size} users (Devices: $totalDevicesFound)\nPush delivery: In-app synced"
                } else {
                    "Notification sent successfully to ${targetUids.size} recipient(s)."
                }
                AdminNotificationSendResult(
                    success = true,
                    notificationId = broadcastId,
                    targetCount = targetUids.size,
                    devicesFound = totalDevicesFound,
                    messagesSent = successfulDispatches,
                    status = if (pushBackendStatus == "SEND_ACCEPTED") "SEND_ACCEPTED" else "DELIVERED",
                    message = successMsg
                )
            } else if (successfulDispatches > 0) {
                val partialMsg = if (pushBackendStatus == "SEND_ACCEPTED") {
                    if (pushAccepted == 0 && totalDevicesFound == 0) {
                        "Notification saved to Firestore.\nRecipients: $successfulDispatches of ${targetUids.size} users\nPush: 0 devices registered."
                    } else {
                        "Notification sent\nRecipients: $successfulDispatches of ${targetUids.size} users\nPush accepted: $pushAccepted\nFailed: $pushFailed"
                    }
                } else {
                    "Notification saved, but delivery succeeded for only $successfulDispatches of ${targetUids.size} users."
                }
                AdminNotificationSendResult(
                    success = true,
                    notificationId = broadcastId,
                    targetCount = targetUids.size,
                    devicesFound = totalDevicesFound,
                    messagesSent = successfulDispatches,
                    status = "PARTIAL_SUCCESS",
                    message = partialMsg
                )
            } else {
                val failMsg = when {
                    lastDispatchCode == "PERMISSION_DENIED" || lastDispatchMessage?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                        "Permission Denied: Ensure administrator custom claim (admin == true) is active and Firestore rules are deployed."
                    !lastDispatchMessage.isNullOrBlank() ->
                        "Firestore error: $lastDispatchMessage"
                    else ->
                        "Failed to deliver notification to target recipients."
                }
                AdminNotificationSendResult(
                    success = false,
                    notificationId = broadcastId,
                    targetCount = targetUids.size,
                    devicesFound = totalDevicesFound,
                    messagesSent = 0,
                    status = lastDispatchCode ?: "FAILED",
                    message = failMsg
                )
            }
        } catch (e: Exception) {
            val exClass = e::class.simpleName ?: "Exception"
            val rawMsg = e.message ?: "An unexpected error occurred while dispatching the notification."
            val fbCode = when {
                rawMsg.contains("PERMISSION_DENIED", ignoreCase = true) -> "PERMISSION_DENIED"
                rawMsg.contains("UNAVAILABLE", ignoreCase = true) -> "UNAVAILABLE"
                rawMsg.contains("NOT_FOUND", ignoreCase = true) -> "NOT_FOUND"
                rawMsg.contains("UNAUTHENTICATED", ignoreCase = true) -> "UNAUTHENTICATED"
                else -> "UNKNOWN"
            }
            println("""
[AdminNotificationDebug] SEND FAILED
operation = Firestore notification dispatch
path = users/*/notifications/$broadcastId
exception = $exClass
firebaseCode = $fbCode
message = $rawMsg
            """.trimIndent())
            AdminNotificationSendResult(
                success = false,
                notificationId = broadcastId,
                targetCount = targetUids.size,
                status = fbCode,
                message = rawMsg
            )
        }
    }

    /**
     * Reads all historical notification broadcasts created by administrators,
     * ordered newest first.
     */
    suspend fun getNotificationHistory(): List<AdminNotificationRecord> {
        return try {
            val snapshot = firestore.collection("admin_notification_history").get()
            snapshot.documents.mapNotNull { parseHistoryDoc(it) }
                .sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            println("[DEBUG] AdminNotificationRepository.getNotificationHistory error: ${e.message}")
            emptyList()
        }
    }

    private fun parseHistoryDoc(doc: DocumentSnapshot): AdminNotificationRecord? {
        return try {
            val id = doc.id
            val title = try { doc.get<String>("title") } catch (_: Exception) { "" }
            val message = try { doc.get<String>("message") } catch (_: Exception) { "" }
            val category = try { doc.get<String>("category") } catch (_: Exception) { NotificationItem.TYPE_GENERAL }
            val audience = try { doc.get<String>("audience") } catch (_: Exception) { AdminAudienceType.SINGLE_USER.name }
            val targetUid = try { doc.get<String?>("targetUid") } catch (_: Exception) { null }
            val targetName = try { doc.get<String?>("targetName") } catch (_: Exception) { null }
            val targetCount = try {
                doc.get<Long>("targetCount").toInt()
            } catch (_: Exception) {
                try { doc.get<Double>("targetCount").toInt() } catch (_: Exception) { 1 }
            }
            val targetNames: List<String> = try {
                doc.get<List<String>?>("targetNames") ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
            val actionType = try { doc.get<String?>("actionType") } catch (_: Exception) { null }
            val actionId = try { doc.get<String?>("actionId") } catch (_: Exception) { null }
            val createdAt = try {
                doc.get<Long>("createdAt")
            } catch (_: Exception) {
                try { doc.get<Double>("createdAt").toLong() } catch (_: Exception) { getCurrentEpochMillis() }
            }
            val createdBy = try { doc.get<String>("createdBy") } catch (_: Exception) { "admin" }
            val status = try { doc.get<String>("status") } catch (_: Exception) { "DELIVERED" }
            val devicesFound = try {
                doc.get<Long>("devicesFound").toInt()
            } catch (_: Exception) {
                try { doc.get<Double>("devicesFound").toInt() } catch (_: Exception) { 0 }
            }
            val messagesSent = try {
                doc.get<Long>("messagesSent").toInt()
            } catch (_: Exception) {
                try { doc.get<Double>("messagesSent").toInt() } catch (_: Exception) { 0 }
            }
            val pushAccepted = try {
                doc.get<Long>("pushAccepted").toInt()
            } catch (_: Exception) {
                try { doc.get<Double>("pushAccepted").toInt() } catch (_: Exception) { 0 }
            }
            val pushFailed = try {
                doc.get<Long>("pushFailed").toInt()
            } catch (_: Exception) {
                try { doc.get<Double>("pushFailed").toInt() } catch (_: Exception) { 0 }
            }
            val pushStatus = try { doc.get<String?>("pushStatus") } catch (_: Exception) { null }
            val statusMessage = try { doc.get<String?>("statusMessage") } catch (_: Exception) { null }

            AdminNotificationRecord(
                id = id,
                title = title,
                message = message,
                category = category,
                audience = audience,
                targetUid = targetUid,
                targetName = targetName,
                targetCount = targetCount,
                targetNames = targetNames,
                actionType = actionType,
                actionId = actionId,
                createdAt = createdAt,
                createdBy = createdBy,
                status = status,
                devicesFound = devicesFound,
                messagesSent = messagesSent,
                pushAccepted = pushAccepted,
                pushFailed = pushFailed,
                pushStatus = pushStatus,
                statusMessage = statusMessage
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
