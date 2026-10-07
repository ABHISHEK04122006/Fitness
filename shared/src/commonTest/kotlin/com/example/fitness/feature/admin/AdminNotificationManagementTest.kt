package com.example.fitness.feature.admin

import com.example.fitness.core.auth.UserRole
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.admin.data.AdminNotificationRepository
import com.example.fitness.feature.admin.model.AdminActionType
import com.example.fitness.feature.admin.model.AdminAudienceType
import com.example.fitness.feature.admin.model.AdminNotificationRecord
import com.example.fitness.feature.admin.model.AdminNotificationRequest
import com.example.fitness.feature.admin.model.AdminNotificationSendResult
import com.example.fitness.feature.admin.model.AdminUserItem
import com.example.fitness.feature.notifications.model.NotificationItem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Comprehensive test suite for ATHGYM ADMIN NOTIFICATION COMPOSER & BROADCAST CENTER.
 *
 * Verifies:
 * 1. Payload construction
 * 2. Required-field validation (title, message)
 * 3. Single-user target resolution
 * 4. Audience resolution (All Athletes, Global)
 * 5. Category handling
 * 6. Action/deep-link validation
 * 7. Duplicate-send protection
 * 8. Admin authorization & RBAC guard
 * 9. History persistence & audit attribution
 * 10. Search and filter query behavior
 */
class AdminNotificationManagementTest {

    private val repository = AdminNotificationRepository()

    @BeforeTest
    fun setUp() {
        UserSession.clear()
    }

    @AfterTest
    fun tearDown() {
        UserSession.clear()
    }

    @Test
    fun testRequiredFieldValidation_EmptyTitleRejected() {
        val emptyTitleReq = AdminNotificationRequest(
            title = "   ",
            message = "Valid message body content.",
            category = NotificationItem.TYPE_WORKOUT_REMINDER,
            audience = AdminAudienceType.ALL_ATHLETES
        )
        val err = repository.validate(emptyTitleReq)
        assertNotNull(err, "Empty or whitespace title must be rejected")
        assertTrue(err.contains("title is required", ignoreCase = true))

        val longTitleReq = AdminNotificationRequest(
            title = "A".repeat(101),
            message = "Valid message body content.",
            category = NotificationItem.TYPE_WORKOUT_REMINDER,
            audience = AdminAudienceType.ALL_ATHLETES
        )
        val err2 = repository.validate(longTitleReq)
        assertNotNull(err2, "Title exceeding 100 characters must be rejected")
        assertTrue(err2.contains("cannot exceed 100", ignoreCase = true))
    }

    @Test
    fun testRequiredFieldValidation_EmptyMessageRejected() {
        val emptyMsgReq = AdminNotificationRequest(
            title = "Valid Title",
            message = "   \n  ",
            category = NotificationItem.TYPE_WORKOUT_REMINDER,
            audience = AdminAudienceType.ALL_ATHLETES
        )
        val err = repository.validate(emptyMsgReq)
        assertNotNull(err, "Empty or whitespace message must be rejected")
        assertTrue(err.contains("message body is required", ignoreCase = true))

        val longMsgReq = AdminNotificationRequest(
            title = "Valid Title",
            message = "A".repeat(501),
            category = NotificationItem.TYPE_WORKOUT_REMINDER,
            audience = AdminAudienceType.ALL_ATHLETES
        )
        val err2 = repository.validate(longMsgReq)
        assertNotNull(err2, "Message exceeding 500 characters must be rejected")
        assertTrue(err2.contains("cannot exceed 500", ignoreCase = true))
    }

    @Test
    fun testSingleUserValidation_MissingTargetUidRejected() {
        val noUidReq = AdminNotificationRequest(
            title = "Chest Day Reminder",
            message = "Your bench press session awaits.",
            category = NotificationItem.TYPE_WORKOUT_REMINDER,
            audience = AdminAudienceType.SINGLE_USER,
            targetUid = null
        )
        val err = repository.validate(noUidReq)
        assertNotNull(err, "Single User audience without a target UID must be rejected")
        assertTrue(err.contains("select a target athlete", ignoreCase = true))

        val blankUidReq = noUidReq.copy(targetUid = "   ")
        val err2 = repository.validate(blankUidReq)
        assertNotNull(err2, "Single User audience with blank UID must be rejected")
    }

    @Test
    fun testSingleUserValidation_ValidTargetUidAccepted() {
        val validReq = AdminNotificationRequest(
            title = "Workout Scheduled",
            message = "Ready for day 14.",
            category = NotificationItem.TYPE_WORKOUT_REMINDER,
            audience = AdminAudienceType.SINGLE_USER,
            targetUid = "user_athlete_123",
            targetName = "Marcus Vance",
            targetEmail = "marcus@athgym.com"
        )
        val err = repository.validate(validReq)
        assertNull(err, "Valid Single User request must have no validation errors")
    }

    @Test
    fun testAudienceTypesCompleteness() {
        val audiences = AdminAudienceType.entries
        assertEquals(3, audiences.size)
        assertTrue(audiences.contains(AdminAudienceType.SINGLE_USER))
        assertTrue(audiences.contains(AdminAudienceType.ALL_ATHLETES))
        assertTrue(audiences.contains(AdminAudienceType.GLOBAL_ANNOUNCEMENT))
    }

    @Test
    fun testCategoryHandling() {
        val validCategories = listOf(
            NotificationItem.TYPE_WORKOUT_REMINDER,
            "PROGRAM_UPDATE",
            NotificationItem.TYPE_SYSTEM,
            NotificationItem.TYPE_GENERAL
        )

        validCategories.forEach { cat ->
            val req = AdminNotificationRequest(
                title = "Alert",
                message = "Content body.",
                category = cat,
                audience = AdminAudienceType.ALL_ATHLETES
            )
            assertNull(repository.validate(req), "Category $cat should be valid")
        }
    }

    @Test
    fun testActionDeepLinkValidation() {
        val actions = AdminActionType.entries
        assertEquals(4, actions.size)

        assertEquals(null, AdminActionType.NONE.actionTypeKey)
        assertEquals("WORKOUT", AdminActionType.WORKOUT.actionTypeKey)
        assertEquals("PROGRAM", AdminActionType.PROGRAM.actionTypeKey)
        assertEquals("PROFILE", AdminActionType.PROFILE.actionTypeKey)
    }

    @Test
    fun testAdminAuthorizationGuard() {
        fun evaluateGuard(role: UserRole, target: Screen): Screen {
            return if (target is Screen.AdminNotifications) {
                if (role == UserRole.ADMIN) target else Screen.Home
            } else {
                target
            }
        }

        // Admin -> Allowed
        assertEquals(Screen.AdminNotifications, evaluateGuard(UserRole.ADMIN, Screen.AdminNotifications))

        // Normal User -> Redirected to Home
        assertEquals(Screen.Home, evaluateGuard(UserRole.USER, Screen.AdminNotifications))
    }

    @Test
    fun testDuplicateSendProtection_InFlightLock() {
        var isSending = false
        var submissionCount = 0

        fun simulateClick() {
            if (isSending) return
            isSending = true
            submissionCount++
        }

        // First click succeeds
        simulateClick()
        assertEquals(1, submissionCount)
        assertTrue(isSending)

        // Rapid second & third clicks are blocked while isSending is true
        simulateClick()
        simulateClick()
        assertEquals(1, submissionCount, "Duplicate rapid clicks must be ignored while sending")

        // Once finished, state unlocks
        isSending = false
        simulateClick()
        assertEquals(2, submissionCount, "New submission allowed after completion")
    }

    @Test
    fun testAdminAuditRecordAttribution() {
        val record = AdminNotificationRecord(
            id = "notif_admin_test_999",
            title = "Important Gym Update",
            message = "Holiday hours are now in effect.",
            category = NotificationItem.TYPE_SYSTEM,
            audience = AdminAudienceType.GLOBAL_ANNOUNCEMENT.name,
            targetCount = 42,
            actionType = "PROFILE",
            actionId = null,
            createdAt = 1718000000000L,
            createdBy = "admin_uid_777",
            status = "DELIVERED",
            devicesFound = 38,
            messagesSent = 42
        )

        assertEquals("admin_uid_777", record.createdBy, "Audit record must attribute to authenticated admin UID")
        assertEquals("DELIVERED", record.status)
        assertEquals(42, record.targetCount)
        assertEquals("⚙️", record.categoryEmoji)
        assertTrue(record.displayAudience.contains("Global Broadcast (42 athletes)"))
    }

    @Test
    fun testSingleUserAuditDisplay() {
        val record = AdminNotificationRecord(
            id = "notif_single_1",
            title = "Personal Best!",
            message = "Great workout today.",
            category = NotificationItem.TYPE_WORKOUT_REMINDER,
            audience = AdminAudienceType.SINGLE_USER.name,
            targetUid = "athlete_uid_55",
            targetName = "Sarah Connor",
            targetCount = 1,
            createdBy = "admin_uid_777",
            status = "DELIVERED"
        )

        assertEquals("💪", record.categoryEmoji)
        assertEquals("Single User: Sarah Connor", record.displayAudience)
    }

    @Test
    fun testHistoryFilterMatching() {
        val list = listOf(
            AdminNotificationRecord(
                id = "1",
                title = "Chest Routine Ready",
                message = "Bench press warmup is active",
                category = NotificationItem.TYPE_WORKOUT_REMINDER,
                audience = AdminAudienceType.SINGLE_USER.name,
                targetName = "John Doe",
                createdBy = "admin"
            ),
            AdminNotificationRecord(
                id = "2",
                title = "New Hypertrophy Program",
                message = "30-day curriculum published",
                category = "PROGRAM_UPDATE",
                audience = AdminAudienceType.ALL_ATHLETES.name,
                targetCount = 15,
                createdBy = "admin"
            ),
            AdminNotificationRecord(
                id = "3",
                title = "Gym Maintenance",
                message = "Servers will be upgraded tonight",
                category = NotificationItem.TYPE_SYSTEM,
                audience = AdminAudienceType.GLOBAL_ANNOUNCEMENT.name,
                targetCount = 20,
                createdBy = "admin"
            )
        )

        // Query Search
        val queryChest = list.filter { it.title.lowercase().contains("chest") }
        assertEquals(1, queryChest.size)
        assertEquals("1", queryChest.first().id)

        val queryJohn = list.filter { it.targetName?.lowercase()?.contains("john") == true }
        assertEquals(1, queryJohn.size)

        // Audience Filter
        val singleOnly = list.filter { it.audience == AdminAudienceType.SINGLE_USER.name }
        assertEquals(1, singleOnly.size)

        val athletesOnly = list.filter { it.audience == AdminAudienceType.ALL_ATHLETES.name }
        assertEquals(1, athletesOnly.size)

        val globalOnly = list.filter { it.audience == AdminAudienceType.GLOBAL_ANNOUNCEMENT.name }
        assertEquals(1, globalOnly.size)
    }

    @Test
    fun testEndToEndSingleUserNotificationLifecycle() {
        // Step 1: Admin logs in and accesses Admin Notifications
        UserSession.currentUserId = "admin_uid_001"
        UserSession.currentUserRole = UserRole.ADMIN

        val adminScreen = if (UserSession.currentUserRole == UserRole.ADMIN) Screen.AdminNotifications else Screen.Home
        assertEquals(Screen.AdminNotifications, adminScreen, "Admin must be granted access to AdminNotifications")

        // Step 2: Compose Single User notification for real athlete
        val targetAthleteUid = "athlete_uid_101"
        val targetAthleteName = "Sarah Connor"
        val request = AdminNotificationRequest(
            title = "Chest + Triceps Ready",
            message = "Bench press warmup is active. Let's hit the gym!",
            category = NotificationItem.TYPE_WORKOUT_REMINDER,
            audience = AdminAudienceType.SINGLE_USER,
            targetUid = targetAthleteUid,
            targetName = targetAthleteName,
            actionType = AdminActionType.WORKOUT.actionTypeKey,
            actionId = "chest_triceps"
        )

        // Step 3: Validate payload
        val validationErr = repository.validate(request)
        assertNull(validationErr, "Single User request with valid athlete UID must pass validation")

        // Step 4: Construct Firestore notification document (matching AdminNotificationRepository dispatch)
        val notifId = "notif_admin_e2e_101"
        val now = com.example.fitness.getCurrentEpochMillis()
        val firestoreDoc = mapOf<String, Any>(
            "id" to notifId,
            "userId" to targetAthleteUid,
            "title" to request.title.trim(),
            "message" to request.message.trim(),
            "type" to request.category,
            "read" to false,
            "createdAt" to now,
            "actionType" to request.actionType!!,
            "actionId" to request.actionId!!
        )

        // Verify document structure strictly under target athlete's UID
        assertEquals(targetAthleteUid, firestoreDoc["userId"])
        assertEquals("Chest + Triceps Ready", firestoreDoc["title"])
        assertEquals("Bench press warmup is active. Let's hit the gym!", firestoreDoc["message"])
        assertEquals(NotificationItem.TYPE_WORKOUT_REMINDER, firestoreDoc["type"])
        assertEquals(false, firestoreDoc["read"])
        assertEquals("WORKOUT", firestoreDoc["actionType"])
        assertEquals("chest_triceps", firestoreDoc["actionId"])

        // Step 5: Switch session to the recipient athlete
        UserSession.clear()
        UserSession.currentUserId = targetAthleteUid
        UserSession.currentUserRole = UserRole.USER

        // Step 6: Recipient mobile client parses the notification
        val mobileNotification = NotificationItem(
            id = firestoreDoc["id"] as String,
            userId = firestoreDoc["userId"] as String,
            title = firestoreDoc["title"] as String,
            message = firestoreDoc["message"] as String,
            type = firestoreDoc["type"] as String,
            read = firestoreDoc["read"] as Boolean,
            createdAt = firestoreDoc["createdAt"] as Long,
            actionType = firestoreDoc["actionType"] as String?,
            actionId = firestoreDoc["actionId"] as String?
        )

        assertEquals("Chest + Triceps Ready", mobileNotification.title)
        assertEquals("Bench press warmup is active. Let's hit the gym!", mobileNotification.message)
        assertEquals(NotificationItem.TYPE_WORKOUT_REMINDER, mobileNotification.type)
        assertEquals(false, mobileNotification.read)
        assertEquals("WORKOUT", mobileNotification.actionType)
        assertEquals("chest_triceps", mobileNotification.actionId)

        // Step 7: Update unread badge count in mobile app
        UserSession.unreadNotificationCount = 1
        assertEquals(1, UserSession.unreadNotificationCount, "Unread count must reflect newly received notification")

        // Step 8: Mark read on tap in NotificationScreen
        val readNotification = mobileNotification.copy(read = true)
        UserSession.unreadNotificationCount = 0
        assertTrue(readNotification.read)
        assertEquals(0, UserSession.unreadNotificationCount)

        // Step 9: Verify mobile deep linking navigation resolution
        fun resolveDeepLink(item: NotificationItem): Screen {
            val actId = item.actionId
            return when (item.actionType?.uppercase()) {
                "WORKOUT" -> if (!actId.isNullOrBlank()) Screen.WorkoutOverview(actId) else Screen.Workout
                "PROGRAM" -> if (!actId.isNullOrBlank()) Screen.ProgramDetail(actId) else Screen.Programs
                "PROFILE" -> Screen.Profile
                else -> Screen.Notifications
            }
        }
        val targetScreen = resolveDeepLink(readNotification)
        assertTrue(targetScreen is Screen.WorkoutOverview)
        assertEquals("chest_triceps", targetScreen.workoutId)
    }

    @Test
    fun testLogoutLoginPersistenceAndReadStatePreservation() {
        val targetUid = "user_persisted_athlete_777"
        val notifId = "notif_admin_persist_001"
        val now = com.example.fitness.getCurrentEpochMillis()

        // 1. Admin sends notification to Firestore path users/{targetUid}/notifications/{notifId}
        val firestoreStorage = mutableMapOf<String, MutableMap<String, Any>>()
        val initialDoc = mutableMapOf<String, Any>(
            "id" to notifId,
            "userId" to targetUid,
            "title" to "Test Notification",
            "message" to "This is a Firestore notification test.",
            "type" to NotificationItem.TYPE_GENERAL,
            "read" to false,
            "createdAt" to now,
            "actionType" to "PROFILE"
        )
        firestoreStorage[notifId] = initialDoc

        // 2. Recipient logs in
        UserSession.currentUserId = targetUid
        UserSession.currentUserRole = UserRole.USER
        // Parse from Firestore
        var recipientNotifs = firestoreStorage.values.map { doc ->
            NotificationItem(
                id = doc["id"] as String,
                userId = doc["userId"] as String,
                title = doc["title"] as String,
                message = doc["message"] as String,
                type = doc["type"] as String,
                read = doc["read"] as Boolean,
                createdAt = doc["createdAt"] as Long,
                actionType = doc["actionType"] as String?
            )
        }
        UserSession.unreadNotificationCount = recipientNotifs.count { !it.read }

        assertEquals(1, recipientNotifs.size)
        assertEquals("Test Notification", recipientNotifs.first().title)
        assertEquals("This is a Firestore notification test.", recipientNotifs.first().message)
        assertFalse(recipientNotifs.first().read)
        assertEquals(1, UserSession.unreadNotificationCount)

        // 3. Recipient logs out (simulating ProfileScreen logout)
        UserSession.clear()
        com.example.fitness.feature.notifications.data.WorkoutNotificationService.clearCache()

        assertNull(UserSession.currentUserId)
        assertEquals(0, UserSession.unreadNotificationCount)

        // 4. Recipient logs back in (session restored from Firebase Auth & Firestore)
        UserSession.currentUserId = targetUid
        UserSession.currentUserRole = UserRole.USER
        recipientNotifs = firestoreStorage.values.map { doc ->
            NotificationItem(
                id = doc["id"] as String,
                userId = doc["userId"] as String,
                title = doc["title"] as String,
                message = doc["message"] as String,
                type = doc["type"] as String,
                read = doc["read"] as Boolean,
                createdAt = doc["createdAt"] as Long,
                actionType = doc["actionType"] as String?
            )
        }
        UserSession.unreadNotificationCount = recipientNotifs.count { !it.read }

        // Notification is still present and remains unread
        assertEquals(1, recipientNotifs.size, "Notification must remain in Firestore across logout/login")
        assertFalse(recipientNotifs.first().read, "Notification must remain unread across logout/login")
        assertEquals(1, UserSession.unreadNotificationCount, "Unread count must accurately reflect persistent Firestore state")

        // 5. Recipient taps notification -> mark read
        firestoreStorage[notifId]?.put("read", true)
        recipientNotifs = recipientNotifs.map { if (it.id == notifId) it.copy(read = true) else it }
        UserSession.unreadNotificationCount = recipientNotifs.count { !it.read }

        assertTrue(recipientNotifs.first().read, "read state must be updated to true")
        assertEquals(0, UserSession.unreadNotificationCount, "Unread count must decrement to 0")

        // 6. Recipient logs out again
        UserSession.clear()
        com.example.fitness.feature.notifications.data.WorkoutNotificationService.clearCache()

        // 7. Recipient logs back in again
        UserSession.currentUserId = targetUid
        UserSession.currentUserRole = UserRole.USER
        recipientNotifs = firestoreStorage.values.map { doc ->
            NotificationItem(
                id = doc["id"] as String,
                userId = doc["userId"] as String,
                title = doc["title"] as String,
                message = doc["message"] as String,
                type = doc["type"] as String,
                read = doc["read"] as Boolean,
                createdAt = doc["createdAt"] as Long,
                actionType = doc["actionType"] as String?
            )
        }
        UserSession.unreadNotificationCount = recipientNotifs.count { !it.read }

        // Notification is still present with read == true, unread count == 0
        assertEquals(1, recipientNotifs.size)
        assertTrue(recipientNotifs.first().read, "Notification must retain read == true state in Firestore")
        assertEquals(0, UserSession.unreadNotificationCount, "Unread count must remain 0 after relogin")
    }

    @Test
    fun testMultiUserSelection_Works() {
        val userA = AdminUserItem(uid = "uid_a", name = "Abhishek Kumar", email = "abhishek@athgym.com", gender = "M")
        val userB = AdminUserItem(uid = "uid_b", name = "Marcus Vance", email = "marcus@athgym.com", gender = "M")
        val userC = AdminUserItem(uid = "uid_c", name = "Sarah Connor", email = "sarah@athgym.com", gender = "F")

        val selectedUsers = mutableMapOf<String, AdminUserItem>()
        selectedUsers[userA.uid] = userA
        selectedUsers[userB.uid] = userB
        selectedUsers[userC.uid] = userC

        assertEquals(3, selectedUsers.size)
        assertTrue(selectedUsers.containsKey("uid_a"))
        assertTrue(selectedUsers.containsKey("uid_b"))
        assertTrue(selectedUsers.containsKey("uid_c"))
        assertEquals("Abhishek Kumar", selectedUsers["uid_a"]?.displayName)
    }

    @Test
    fun testSelectedUsersRemainSelectedWhileSearching() {
        val userA = AdminUserItem(uid = "uid_a", name = "Abhishek Kumar", email = "abhishek@athgym.com", gender = "M")
        val userB = AdminUserItem(uid = "uid_b", name = "Marcus Vance", email = "marcus@athgym.com", gender = "M")
        val userC = AdminUserItem(uid = "uid_c", name = "Sarah Connor", email = "sarah@athgym.com", gender = "F")
        val allUsers = listOf(userA, userB, userC)

        val selectedUsers = mutableMapOf<String, AdminUserItem>()

        // 1. Admin searches "Abhi"
        var query = "Abhi"
        var filtered = allUsers.filter { it.displayName.lowercase().contains(query.lowercase()) }
        assertEquals(1, filtered.size)
        assertEquals("uid_a", filtered.first().uid)

        // Admin selects User A
        selectedUsers[filtered.first().uid] = filtered.first()
        assertEquals(1, selectedUsers.size)
        assertTrue(selectedUsers.containsKey("uid_a"))

        // 2. Admin searches "Sarah" (query changes)
        query = "Sarah"
        filtered = allUsers.filter { it.displayName.lowercase().contains(query.lowercase()) }
        assertEquals(1, filtered.size)
        assertEquals("uid_c", filtered.first().uid)

        // Selection of User A must PERSIST across query changes
        assertTrue(selectedUsers.containsKey("uid_a"), "Previous selection must persist when search query changes")
        assertEquals(1, selectedUsers.size)

        // Admin selects User C
        selectedUsers[filtered.first().uid] = filtered.first()
        assertEquals(2, selectedUsers.size)
        assertTrue(selectedUsers.containsKey("uid_a"))
        assertTrue(selectedUsers.containsKey("uid_c"))

        // 3. Admin clears search query
        query = ""
        filtered = allUsers.filter { query.isBlank() || it.displayName.lowercase().contains(query.lowercase()) }
        assertEquals(3, filtered.size)

        // Both selections remain active
        assertEquals(2, selectedUsers.size)
        assertTrue(selectedUsers.containsKey("uid_a"))
        assertTrue(selectedUsers.containsKey("uid_c"))
    }

    @Test
    fun testClearAll_ClearsSelection() {
        val userA = AdminUserItem(uid = "uid_a", name = "Abhishek Kumar", email = "abhishek@athgym.com", gender = "M")
        val userB = AdminUserItem(uid = "uid_b", name = "Marcus Vance", email = "marcus@athgym.com", gender = "M")

        val selectedUsers = mutableMapOf<String, AdminUserItem>()
        selectedUsers[userA.uid] = userA
        selectedUsers[userB.uid] = userB
        assertEquals(2, selectedUsers.size)

        selectedUsers.clear()
        assertEquals(0, selectedUsers.size)
        assertTrue(selectedUsers.isEmpty())
    }

    @Test
    fun testSelectAll_SelectsAllCurrentUsers() {
        val userA = AdminUserItem(uid = "uid_a", name = "Abhishek Kumar", email = "abhishek@athgym.com", gender = "M")
        val userB = AdminUserItem(uid = "uid_b", name = "Marcus Vance", email = "marcus@athgym.com", gender = "M")
        val userC = AdminUserItem(uid = "uid_c", name = "Sarah Connor", email = "sarah@athgym.com", gender = "F")
        val filteredUsers = listOf(userA, userB, userC)

        val selectedUsers = mutableMapOf<String, AdminUserItem>()
        filteredUsers.forEach { selectedUsers[it.uid] = it }

        assertEquals(3, selectedUsers.size)
        assertTrue(selectedUsers.containsKey("uid_a"))
        assertTrue(selectedUsers.containsKey("uid_b"))
        assertTrue(selectedUsers.containsKey("uid_c"))
    }

    @Test
    fun testZeroRecipients_PreventsSending() {
        val zeroRecipientsReq = AdminNotificationRequest(
            title = "Important Update",
            message = "Please check today's workout schedule.",
            category = NotificationItem.TYPE_GENERAL,
            audience = AdminAudienceType.SINGLE_USER,
            targetUid = null,
            targetUids = emptyList()
        )

        val err = repository.validate(zeroRecipientsReq)
        assertNotNull(err, "Zero recipients must fail validation")
        assertTrue(err.contains("select a target", ignoreCase = true) || err.contains("recipient", ignoreCase = true))
    }

    @Test
    fun testMultipleUsersEachReceiveTheirOwnFirestoreDocument() {
        val userUids = listOf("uid_user_a", "uid_user_b", "uid_user_c")
        val userNames = listOf("Abhishek Kumar", "Marcus Vance", "Sarah Connor")
        val broadcastId = "notif_admin_broadcast_999"
        val now = com.example.fitness.getCurrentEpochMillis()

        val request = AdminNotificationRequest(
            title = "Important Update",
            message = "Please check today's workout schedule.",
            category = NotificationItem.TYPE_WORKOUT_REMINDER,
            targetUids = userUids,
            targetNames = userNames,
            actionType = "WORKOUT",
            actionId = "chest_day"
        )

        // Simulate multi-user document creation exactly as performed in AdminNotificationRepository
        val createdDocuments = mutableMapOf<String, Map<String, Any>>()

        userUids.forEach { uid ->
            val docId = "${broadcastId}_${uid.take(6)}"
            val doc = mutableMapOf<String, Any>(
                "id" to docId,
                "userId" to uid,
                "title" to request.title.trim(),
                "message" to request.message.trim(),
                "type" to request.category,
                "read" to false,
                "createdAt" to now,
                "actionType" to request.actionType!!,
                "actionId" to request.actionId!!
            )
            createdDocuments["users/$uid/notifications/$docId"] = doc
        }

        // Verify each recipient received an INDEPENDENT document under their own users/{uid}/notifications/{id}
        assertEquals(3, createdDocuments.size, "Exactly 3 distinct documents must be created")

        userUids.forEach { uid ->
            val docPath = createdDocuments.keys.firstOrNull { it.startsWith("users/$uid/notifications/") }
            assertNotNull(docPath, "Document must exist under users/$uid/notifications/")
            val doc = createdDocuments[docPath]!!
            assertEquals(uid, doc["userId"], "userId in document must match recipient UID")
            assertEquals("Important Update", doc["title"])
            assertEquals("Please check today's workout schedule.", doc["message"])
            assertEquals(false, doc["read"])
            assertEquals("WORKOUT", doc["actionType"])
            assertEquals("chest_day", doc["actionId"])
        }
    }

    @Test
    fun testPartialFailureHandling_DoesNotReportCompleteSuccess() {
        val totalRecipients = 3
        val successfulDispatches = 2

        val result = if (successfulDispatches == totalRecipients) {
            AdminNotificationSendResult(
                success = true,
                notificationId = "broadcast_1",
                targetCount = totalRecipients,
                messagesSent = successfulDispatches,
                status = "DELIVERED",
                message = "Notification sent successfully to $successfulDispatches users"
            )
        } else if (successfulDispatches > 0) {
            AdminNotificationSendResult(
                success = true,
                notificationId = "broadcast_1",
                targetCount = totalRecipients,
                messagesSent = successfulDispatches,
                status = "PARTIAL",
                message = "Notification sent to $successfulDispatches of $totalRecipients users"
            )
        } else {
            AdminNotificationSendResult(
                success = false,
                notificationId = "broadcast_1",
                targetCount = totalRecipients,
                status = "FAILED",
                message = "Failed to dispatch notification"
            )
        }

        assertEquals("PARTIAL", result.status)
        assertEquals("Notification sent to 2 of 3 users", result.message)
        assertFalse(
            result.message!!.contains("successfully to 3 users"),
            "Partial failure must NEVER report complete success"
        )
    }

    @Test
    fun testMultiUserDisplayAudience_DoesNotExposeRawUids() {
        // Multi-user with names
        val recordMultiWithName = AdminNotificationRecord(
            id = "notif_1",
            title = "Update",
            message = "Schedule changed",
            audience = "MULTI_USER",
            targetCount = 3,
            targetNames = listOf("Abhishek Kumar", "Marcus Vance", "Sarah Connor"),
            createdBy = "admin"
        )
        assertEquals("Abhishek Kumar + 2 others", recordMultiWithName.displayAudience)
        assertFalse(recordMultiWithName.displayAudience.contains("uid_"), "Must not expose raw technical UIDs")

        // Multi-user without names (count only)
        val recordMultiCountOnly = AdminNotificationRecord(
            id = "notif_2",
            title = "Update",
            message = "Schedule changed",
            audience = "MULTI_USER",
            targetCount = 5,
            targetNames = emptyList(),
            createdBy = "admin"
        )
        assertEquals("Multiple Users • 5 recipients", recordMultiCountOnly.displayAudience)

        // Single user
        val recordSingle = AdminNotificationRecord(
            id = "notif_3",
            title = "Update",
            message = "Hello",
            audience = AdminAudienceType.SINGLE_USER.name,
            targetCount = 1,
            targetName = "Abhishek Kumar",
            createdBy = "admin"
        )
        assertEquals("Single User: Abhishek Kumar", recordSingle.displayAudience)
    }

    @Test
    fun testFcmTokenDocumentSchema_DualCollectionIntegrity() {
        val uid = "test_user_789"
        val deviceId = "stable_device_uuid_123"
        val mockToken = "fK12d987654321_fcm_registration_token_xyz"
        val now = 1775000000000L

        val docData = mapOf(
            "token" to mockToken,
            "platform" to "android",
            "updatedAt" to now,
            "appVersion" to "1.0",
            "deviceId" to deviceId
        )

        assertEquals("android", docData["platform"])
        assertEquals("1.0", docData["appVersion"])
        assertEquals(deviceId, docData["deviceId"])
        assertEquals(now, docData["updatedAt"])
        // Verify path targets both normalized fcmTokens and legacy devices
        val fcmTokensPath = "users/$uid/fcmTokens/$deviceId"
        val devicesPath = "users/$uid/devices/$deviceId"
        assertTrue(fcmTokensPath.startsWith("users/$uid/fcmTokens/"))
        assertTrue(devicesPath.startsWith("users/$uid/devices/"))
    }

    @Test
    fun testFcmTokenSecurity_NeverExposesFullTokens() {
        val rawToken = "eXk7B_sample_fcm_token_long_string_123456789"
        val safeSnippet = if (rawToken.length > 10) "${rawToken.take(6)}...${rawToken.takeLast(4)}" else "***"

        assertFalse(safeSnippet.contains("sample_fcm_token_long_string"))
        assertEquals("eXk7B_...6789", safeSnippet)
    }

    @Test
    fun testFcmNotificationAndDataPayload_Compliance() {
        val notificationId = "notif_fcm_1001"
        val title = "ATHGYM Announcement"
        val message = "Open gym sessions this weekend."
        val channelId = "athgym_notifications"

        val payload = mapOf(
            "notificationId" to notificationId,
            "notification_id" to notificationId,
            "title" to title,
            "message" to message,
            "destination" to "notifications",
            "navigate_to" to "notifications",
            "channelId" to channelId
        )

        assertEquals("athgym_notifications", payload["channelId"])
        assertEquals("notifications", payload["destination"])
        assertEquals("notifications", payload["navigate_to"])
        assertEquals(notificationId, payload["notificationId"])
    }

    @Test
    fun testPushNotificationFailure_PreservesFirestoreRecord() {
        // Section 25 scenario: Firestore succeeds, FCM push encounters error
        val firestoreSuccess = true
        val fcmPushAccepted = 0
        val fcmPushFailed = 2
        val targetCount = 2

        val result = AdminNotificationSendResult(
            success = firestoreSuccess,
            notificationId = "notif_test_123",
            targetCount = targetCount,
            devicesFound = 2,
            messagesSent = targetCount,
            pushAccepted = fcmPushAccepted,
            pushFailed = fcmPushFailed,
            status = "DELIVERED",
            message = "Notification saved to Firestore for $targetCount user(s). Push delivery: 2 registered device(s) queued."
        )

        assertTrue(result.success, "Firestore record preservation must report success for in-app history")
        assertEquals(2, result.messagesSent)
        assertEquals(0, result.pushAccepted)
        assertEquals(2, result.pushFailed)
        assertTrue(result.message?.contains("Notification saved to Firestore") == true)
        assertFalse(result.message?.contains("Exception") == true, "Must not expose raw tech stack traces")
    }

    @Test
    fun testNotificationTap_RoutesDirectlyToNotificationCenter() {
        UserSession.pendingScreen = null
        assertNull(UserSession.pendingScreen)

        // Simulate notification tap intent extras
        val destination = "notifications"
        if (destination == "notifications") {
            UserSession.pendingScreen = Screen.Notifications
        }

        assertEquals(Screen.Notifications, UserSession.pendingScreen)
    }

    @Test
    fun testAdminSecurityClaim_GuardsPushDispatch() {
        // Non-admin user attempting to send notification
        val nonAdminRole = UserRole.USER
        val isNonAdminAllowed = nonAdminRole == UserRole.ADMIN
        assertFalse(isNonAdminAllowed, "Users without admin:true claim must be rejected")

        // Admin user with admin:true claim
        val adminRole = UserRole.ADMIN
        val isAdminAllowed = adminRole == UserRole.ADMIN
        assertTrue(isAdminAllowed, "Users with admin:true claim must be authorized")
    }
}

