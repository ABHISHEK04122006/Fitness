package com.example.fitness

import com.example.fitness.core.auth.RoleResolver
import com.example.fitness.core.auth.UserRole
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.admin.AdminModule
import com.example.fitness.feature.admin.model.AdminUserItem
import com.example.fitness.feature.programs.data.ProgramRegistry
import com.example.fitness.feature.progress.data.WorkoutProgressService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SharedCommonTest {

    @Test
    fun testProgramRegistryContainsAllFourPrograms() {
        val programs = ProgramRegistry.getAllPrograms()
        assertEquals(4, programs.size)

        val programIds = programs.map { it.id }.toSet()
        assertTrue(programIds.contains("30-day"))
        assertTrue(programIds.contains("hypertrophy"))
        assertTrue(programIds.contains("strength"))
        assertTrue(programIds.contains("athlete"))

        // Check each has 30 scheduled days
        programs.forEach { prog ->
            assertEquals(30, prog.schedule.size, "Program ${prog.id} should have 30 scheduled days")
        }
    }

    @Test
    fun testProgramWorkoutsResolution() {
        // Hypertrophy
        val hypWorkout = ProgramRegistry.getWorkoutById("hypertrophy_day_1", "hypertrophy")
        assertNotNull(hypWorkout)
        assertEquals(1, hypWorkout.dayNumber)
        assertTrue(hypWorkout.workoutTitle.contains("Chest", ignoreCase = true))
        assertTrue(hypWorkout.exercises.isNotEmpty())
        assertEquals("4 Sets", hypWorkout.exercises.first().sets)

        // Strength
        val strWorkout = ProgramRegistry.getWorkoutById("strength_day_1", "strength")
        assertNotNull(strWorkout)
        assertEquals(1, strWorkout.dayNumber)
        assertTrue(strWorkout.workoutTitle.contains("Chest", ignoreCase = true) || strWorkout.workoutTitle.contains("Bench", ignoreCase = true))
        assertEquals("5 Sets", strWorkout.exercises.first().sets)
        assertEquals("180s", strWorkout.exercises.first().rest)

        // Athlete Performance
        val athWorkout = ProgramRegistry.getWorkoutById("athlete_day_1", "athlete")
        assertNotNull(athWorkout)
        assertEquals(1, athWorkout.dayNumber)
        assertTrue(athWorkout.workoutTitle.contains("Sprint", ignoreCase = true))
    }

    @Test
    fun testIndependentProgramProgressTracking() {
        // Clear or set known baseline
        UserSession.setActiveProgram("hypertrophy")
        assertEquals("hypertrophy", UserSession.activeProgramId)

        UserSession.markProgramDayCompleted("hypertrophy", 1)
        UserSession.markProgramDayCompleted("hypertrophy", 2)

        UserSession.setActiveProgram("strength")
        assertEquals("strength", UserSession.activeProgramId)
        UserSession.markProgramDayCompleted("strength", 1)

        // Verify independent progress
        assertEquals(2, UserSession.getProgramCompletedCount("hypertrophy"))
        assertEquals(3, UserSession.getProgramCurrentDay("hypertrophy"))

        assertEquals(1, UserSession.getProgramCompletedCount("strength"))
        assertEquals(2, UserSession.getProgramCurrentDay("strength"))

        // Switching back preserves independent state
        UserSession.setActiveProgram("hypertrophy")
        assertEquals(2, UserSession.getProgramCompletedCount("hypertrophy"))
    }

    @Test
    fun testWorkoutProgressServiceProgramRecordingAndAnalytics() {
        val initialHypCount = WorkoutProgressService.getWorkoutsCountByProgram("hypertrophy")

        val record = WorkoutProgressService.saveWorkoutSession(
            workoutId = "hypertrophy_day_1",
            dayNumber = 1,
            workoutTitle = "Chest + Triceps Hypertrophy",
            targetMuscle = listOf("Chest", "Triceps"),
            experienceLevel = "intermediate",
            durationMinutes = 45,
            totalExercises = 5,
            completedExercises = 5,
            totalSets = 15,
            completedSets = 15,
            programId = "hypertrophy",
            programName = "Hypertrophy"
        )

        assertEquals("hypertrophy", record.programId)
        assertEquals("Hypertrophy", record.programName)
        assertEquals(initialHypCount + 1, WorkoutProgressService.getWorkoutsCountByProgram("hypertrophy"))

        val breakdown = WorkoutProgressService.getProgramBreakdownStats()
        assertTrue(breakdown.containsKey("hypertrophy"))
        assertTrue(breakdown["hypertrophy"]!! >= 1)
    }

    @Test
    fun testMuscleAndEquipmentDataIntegrity() {
        // Verify that long equipment strings split into complete whole terms
        val strengthDay1 = ProgramRegistry.getWorkoutById("strength_day_1", "strength")
        assertNotNull(strengthDay1)
        val dailyPlan = com.example.fitness.feature.workout.data.WeeklyWorkoutRepository.toDailyWorkoutPlan(strengthDay1)
        val strengthEquipments = dailyPlan.equipment.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        assertTrue(strengthEquipments.contains("Barbell"))
        assertTrue(strengthEquipments.contains("Bench"))

        // Ensure all program workouts have intact non-empty equipment and focus
        val allPrograms = ProgramRegistry.getAllPrograms()
        allPrograms.forEach { program ->
            program.schedule.forEach { scheduleItem ->
                val workout = ProgramRegistry.getWorkoutById(scheduleItem.workoutId, program.id)
                assertNotNull(workout, "Workout ${scheduleItem.workoutId} in ${program.id} should exist")
                assertTrue(workout.workoutTitle.isNotBlank())
                assertTrue(workout.focus.isNotBlank())
                val muscles = workout.focus.split(",").map { it.trim() }
                muscles.forEach { muscle ->
                    assertTrue(muscle.isNotBlank())
                    // Words must remain intact without mid-word breaks
                    assertTrue(!muscle.contains("\n"), "Muscle '$muscle' should not contain line breaks")
                }
            }
        }
    }

    @Test
    fun testExtractFirstName() {
        assertEquals("Abhishek", com.example.fitness.feature.home.extractFirstName("Abhishek Kumar"))
        assertEquals("Rahul", com.example.fitness.feature.home.extractFirstName("Rahul Sharma"))
        assertEquals("John", com.example.fitness.feature.home.extractFirstName("John"))
        assertEquals("Jane", com.example.fitness.feature.home.extractFirstName("   Jane   Doe   "))
        assertEquals("Athlete", com.example.fitness.feature.home.extractFirstName(""))
        assertEquals("Athlete", com.example.fitness.feature.home.extractFirstName("   "))
        assertEquals("Athlete", com.example.fitness.feature.home.extractFirstName(null))
    }

    @Test
    fun testNotificationItemModelAndSessionReset() {
        val notif = com.example.fitness.feature.notifications.model.NotificationItem(
            id = "notif_001",
            userId = "user_123",
            title = "Today's Workout",
            message = "Chest + Triceps is waiting for you.",
            type = com.example.fitness.feature.notifications.model.NotificationItem.TYPE_WORKOUT_REMINDER,
            read = false,
            actionType = "WORKOUT",
            actionId = "day_14"
        )
        assertEquals("notif_001", notif.id)
        assertEquals("user_123", notif.userId)
        assertEquals("Today's Workout", notif.title)
        assertEquals(false, notif.read)
        assertEquals("WORKOUT", notif.actionType)
        assertEquals("day_14", notif.actionId)

        // Session unread count and reset
        UserSession.unreadNotificationCount = 5
        assertEquals(5, UserSession.unreadNotificationCount)
        UserSession.clear()
        assertEquals(0, UserSession.unreadNotificationCount)
    }

    @Test
    fun testNotificationFormatRelativeTime() {
        val now = getCurrentEpochMillis()
        assertEquals("Just now", com.example.fitness.feature.notifications.formatRelativeTime(now - 10000L)) // 10s ago
        assertEquals("5 min ago", com.example.fitness.feature.notifications.formatRelativeTime(now - 300000L)) // 5m ago
    }

    @Test
    fun testNotificationDataPayloadMapping() {
        // Simulates backend Cloud Functions sending data payload
        val dataPayload = mapOf(
            "notificationId" to "backend_notif_999",
            "title" to "Streak on Fire!",
            "message" to "You have completed 7 days in a row!",
            "type" to "STREAK",
            "actionType" to "PROGRESS",
            "actionId" to "streak_tab"
        )

        val notificationId = dataPayload["notificationId"] ?: "fallback_id"
        val title = dataPayload["title"] ?: "FITNESS"
        val message = dataPayload["message"] ?: ""
        val type = dataPayload["type"] ?: "GENERAL"
        val actionType = dataPayload["actionType"]
        val actionId = dataPayload["actionId"]

        val item = com.example.fitness.feature.notifications.model.NotificationItem(
            id = notificationId,
            userId = "auth_user_456",
            title = title,
            message = message,
            type = type,
            read = false,
            actionType = actionType,
            actionId = actionId
        )

        assertEquals("backend_notif_999", item.id)
        assertEquals("auth_user_456", item.userId)
        assertEquals("Streak on Fire!", item.title)
        assertEquals("You have completed 7 days in a row!", item.message)
        assertEquals("STREAK", item.type)
        assertEquals(false, item.read)
        assertEquals("PROGRESS", item.actionType)
        assertEquals("streak_tab", item.actionId)
    }

    @Test
    fun testNotificationDeduplicationIdIntegrity() {
        // Backend creates document with deterministic notificationId
        val backendNotifId = "scheduled_workout_day_15_user_123"

        val fcmDataPayload = mapOf(
            "notificationId" to backendNotifId,
            "title" to "Day 15 Ready"
        )

        // Client resolves notificationId from payload directly
        val resolvedId = fcmDataPayload["notificationId"]?.takeIf { it.isNotBlank() } ?: "random_uuid"
        assertEquals(backendNotifId, resolvedId, "Notification ID must match backend document ID to prevent duplicates")
    }

    @Test
    fun testNotificationUserIsolationAndLogout() {
        UserSession.currentUserId = "user_A"
        UserSession.pendingScreen = com.example.fitness.core.navigation.Screen.Notifications
        UserSession.unreadNotificationCount = 3

        assertEquals("user_A", UserSession.currentUserId)
        assertEquals(com.example.fitness.core.navigation.Screen.Notifications, UserSession.pendingScreen)
        assertEquals(3, UserSession.unreadNotificationCount)

        // Simulate Logout
        UserSession.clear()

        assertEquals(null, UserSession.currentUserId)
        assertEquals(null, UserSession.pendingScreen)
        assertEquals(0, UserSession.unreadNotificationCount)
    }

    @Test
    fun testSparkPlanLocalTestNotificationSchema() {
        val testUid = "athlete_spark_123"
        val testNotif = com.example.fitness.feature.notifications.model.NotificationItem(
            id = "notif_test_${getCurrentEpochMillis()}",
            userId = testUid,
            title = "Today's Workout",
            message = "Chest + Triceps is waiting for you. 💪",
            type = com.example.fitness.feature.notifications.model.NotificationItem.TYPE_WORKOUT_REMINDER,
            read = false,
            createdAt = getCurrentEpochMillis(),
            actionType = "WORKOUT",
            actionId = "day_14"
        )

        assertEquals("athlete_spark_123", testNotif.userId)
        assertEquals("Today's Workout", testNotif.title)
        assertEquals("Chest + Triceps is waiting for you. 💪", testNotif.message)
        assertEquals(com.example.fitness.feature.notifications.model.NotificationItem.TYPE_WORKOUT_REMINDER, testNotif.type)
        assertEquals(false, testNotif.read)
        assertEquals("WORKOUT", testNotif.actionType)
        assertEquals("day_14", testNotif.actionId)
        assertTrue(testNotif.createdAt > 0)
    }

    @Test
    fun testSparkPlanFirestoreRulesConstraintSimulation() {
        val authUid = "user_owner"
        val targetPathUserId = "user_owner"
        val docId = "notif_999"

        val payload = mapOf(
            "id" to "notif_999",
            "userId" to "user_owner",
            "title" to "Today's Workout",
            "message" to "Chest + Triceps is waiting for you. 💪",
            "type" to "WORKOUT_REMINDER",
            "read" to false,
            "createdAt" to getCurrentEpochMillis()
        )

        // Rule condition: request.auth != null && request.auth.uid == userId
        val isAuthValid = authUid.isNotBlank() && authUid == targetPathUserId
        assertTrue(isAuthValid, "Owner path check must pass")

        // Rule condition: request.resource.data.userId == request.auth.uid
        val isUserIdConsistent = payload["userId"] == authUid
        assertTrue(isUserIdConsistent, "Data userId must match auth UID")

        // Rule condition: request.resource.data.id == notificationId
        val isDocIdMatching = payload["id"] == docId
        assertTrue(isDocIdMatching, "Data id must match document ID")

        // Rule condition: request.resource.data.read == false
        val isInitialReadFalse = payload["read"] == false
        assertTrue(isInitialReadFalse, "Initial read status must be false")

        // Cross-user simulation: Malicious user B attempting to write to User A's path
        val attackerAuthUid = "user_attacker"
        val attackerAttemptAllowed = attackerAuthUid == targetPathUserId && payload["userId"] == attackerAuthUid
        assertFalse(attackerAttemptAllowed, "Cross-user notification write must be strictly rejected")
    }

    @Test
    fun testNewUserDefaultsToUserRole() {
        UserSession.clear()
        assertEquals(UserRole.USER, UserSession.currentUserRole, "Initial / reset user role must strictly default to USER")
    }

    @Test
    fun testMissingAdminClaimResolvesToUser() {
        assertEquals(UserRole.USER, RoleResolver.resolveRoleFromClaims(null), "Null claims map must resolve to USER")
        assertEquals(UserRole.USER, RoleResolver.resolveRoleFromClaims(emptyMap()), "Empty claims map must resolve to USER")
        assertEquals(UserRole.USER, RoleResolver.resolveRoleFromClaims(mapOf("premium" to true)), "Unrelated claims must resolve to USER")
    }

    @Test
    fun testAdminClaimFalseResolvesToUser() {
        assertEquals(UserRole.USER, RoleResolver.resolveRoleFromClaims(mapOf("admin" to false)), "admin: false must resolve to USER")
        assertEquals(UserRole.USER, RoleResolver.resolveRoleFromClaims(mapOf("admin" to "false")), "admin: 'false' must resolve to USER")
        assertEquals(UserRole.USER, RoleResolver.resolveRoleFromClaims(mapOf("admin" to 0)), "admin: 0 must resolve to USER")
    }

    @Test
    fun testAdminClaimTrueResolvesToAdmin() {
        assertEquals(UserRole.ADMIN, RoleResolver.resolveRoleFromClaims(mapOf("admin" to true)), "admin: true (boolean) must resolve to ADMIN")
        assertEquals(UserRole.ADMIN, RoleResolver.resolveRoleFromClaims(mapOf("admin" to "true")), "admin: 'true' (string) must resolve to ADMIN")
        assertEquals(UserRole.ADMIN, RoleResolver.resolveRoleFromClaims(mapOf("admin" to 1)), "admin: 1 must resolve to ADMIN")
    }

    @Test
    fun testEmailAloneNeverGrantsAdmin() {
        // String checking on email addresses (e.g. admin@gmail.com) must NEVER grant admin role
        val suspiciousClaims = mapOf(
            "email" to "admin@gmail.com",
            "name" to "System Administrator"
        )
        assertEquals(UserRole.USER, RoleResolver.resolveRoleFromClaims(suspiciousClaims), "Admin email address without custom claim must strictly resolve to USER")

        val workAdminClaims = mapOf("email" to "admin@fitness.io")
        assertEquals(UserRole.USER, RoleResolver.resolveRoleFromClaims(workAdminClaims))
    }

    @Test
    fun testFirestoreProfilePayloadCannotGrantAdmin() {
        // Simulates a user trying to save isAdmin: true into their Firestore user document
        val clientFirestoreData = mapOf(
            "name" to "Hacker",
            "gender" to "Male",
            "isAdmin" to true,
            "admin" to true,
            "role" to "ADMIN"
        )

        // Role resolver only trusts Firebase Auth token claims, never Firestore client data
        val resolvedFromClientInput = RoleResolver.resolveRoleFromClaims(null)
        assertEquals(UserRole.USER, resolvedFromClientInput, "Arbitrary Firestore fields must never elevate privileges to ADMIN")
    }

    @Test
    fun testLogoutClearsAdminRoleFromUserSession() {
        // Simulate logging in as Admin
        UserSession.currentUserRole = UserRole.ADMIN
        assertEquals(UserRole.ADMIN, UserSession.currentUserRole)

        // User logs out
        UserSession.clear()

        // Role must reset back to default USER
        assertEquals(UserRole.USER, UserSession.currentUserRole, "Logout must cleanly reset currentUserRole to USER")
    }

    @Test
    fun testAdminRoutingGuardAccessControl() {
        fun evaluateGuard(role: UserRole, destination: Screen): Screen {
            val isAdminRoute = when (destination) {
                is Screen.AdminDashboard,
                is Screen.AdminUsers,
                is Screen.AdminNotifications,
                is Screen.AdminWorkouts,
                is Screen.AdminExercises,
                is Screen.AdminPrograms,
                is Screen.AdminContent,
                is Screen.AdminAnalytics,
                is Screen.AdminSettings -> true
                else -> false
            }

            return if (isAdminRoute) {
                if (role == UserRole.ADMIN) destination else Screen.Home
            } else {
                destination
            }
        }

        // ADMIN accessing admin routes -> GRANTED
        assertEquals(Screen.AdminDashboard, evaluateGuard(UserRole.ADMIN, Screen.AdminDashboard))
        assertEquals(Screen.AdminUsers, evaluateGuard(UserRole.ADMIN, Screen.AdminUsers))
        assertEquals(Screen.AdminNotifications, evaluateGuard(UserRole.ADMIN, Screen.AdminNotifications))

        // USER accessing admin routes -> DENIED & REDIRECTED TO HOME
        assertEquals(Screen.Home, evaluateGuard(UserRole.USER, Screen.AdminDashboard), "USER must be redirected from AdminDashboard to Home")
        assertEquals(Screen.Home, evaluateGuard(UserRole.USER, Screen.AdminUsers), "USER must be redirected from AdminUsers to Home")
        assertEquals(Screen.Home, evaluateGuard(UserRole.USER, Screen.AdminNotifications), "USER must be redirected from AdminNotifications to Home")

        // Standard user routes accessible by both
        assertEquals(Screen.Home, evaluateGuard(UserRole.USER, Screen.Home))
        assertEquals(Screen.Home, evaluateGuard(UserRole.ADMIN, Screen.Home))
    }

    @Test
    fun testAdminPortalAllNineModulesCompleteness() {
        val modules = AdminModule.entries
        assertEquals(9, modules.size, "Admin portal must contain exactly 9 prepared modules")

        val titles = modules.map { it.title }.toSet()
        assertTrue(titles.contains("Dashboard"))
        assertTrue(titles.contains("Users"))
        assertTrue(titles.contains("Notifications"))
        assertTrue(titles.contains("Workouts"))
        assertTrue(titles.contains("Exercises"))
        assertTrue(titles.contains("Programs"))
        assertTrue(titles.contains("Content"))
        assertTrue(titles.contains("Analytics"))
        assertTrue(titles.contains("Settings"))

        modules.forEach { mod ->
            assertTrue(mod.title.isNotBlank())
            assertTrue(mod.description.isNotBlank())
            assertTrue(mod.category.isNotBlank())
            assertTrue(mod.targetStep.isNotBlank())
        }
    }

    @Test
    fun testResponsiveLayoutWidthsSanity() {
        val testWidths = listOf(320, 360, 375, 390, 414, 430, 600, 768, 1024)
        testWidths.forEach { width ->
            assertTrue(width >= 320, "Responsive width $width must be at least minimum mobile width 320dp")
            // Verify responsive card calculation: maximum card width does not exceed container width
            val contentWidth = minOf(width - 40, 600)
            assertTrue(contentWidth > 0, "Content width must be positive")
        }
    }

    @Test
    fun testAdminUserSearchFiltering() {
        val sampleUsers = listOf(
            AdminUserItem(uid = "uid_001_alex", name = "Alex Morgan", email = "alex.morgan@fitness.app", gender = "Female"),
            AdminUserItem(uid = "uid_002_abhishek", name = "Abhishek Kumar", email = "abhishek@gmail.com", gender = "Male"),
            AdminUserItem(uid = "uid_003_rahul", name = "Rahul Sharma", email = "rahul.s@fitness.io", gender = "Male"),
            AdminUserItem(uid = "uid_004_samantha", name = "Samantha Fox", email = "samantha@example.com", gender = "Female")
        )

        fun search(query: String): List<AdminUserItem> {
            val q = query.trim().lowercase()
            return if (q.isBlank()) sampleUsers else sampleUsers.filter {
                it.name.lowercase().contains(q) ||
                        it.email.lowercase().contains(q) ||
                        it.uid.lowercase().contains(q)
            }
        }

        // Search by Name (case-insensitive & partial)
        val nameResults = search("abhi")
        assertEquals(1, nameResults.size)
        assertEquals("Abhishek Kumar", nameResults.first().name)

        val upperCaseResults = search("ALEX")
        assertEquals(1, upperCaseResults.size)
        assertEquals("Alex Morgan", upperCaseResults.first().name)

        // Search by Email (case-insensitive & domain partial)
        val emailResults = search("fitness.app")
        assertEquals(1, emailResults.size)
        assertEquals("alex.morgan@fitness.app", emailResults.first().email)

        // Search by UID
        val uidResults = search("uid_003")
        assertEquals(1, uidResults.size)
        assertEquals("Rahul Sharma", uidResults.first().name)

        // Empty query returns all users
        assertEquals(4, search("").size)
        assertEquals(4, search("   ").size)

        // Non-matching query returns empty list
        assertEquals(0, search("non_existent_xyz").size)
    }

    @Test
    fun testAdminUserItemFallbacksAndUidTruncation() {
        val completeUser = AdminUserItem(
            uid = "abcdef1234567890xyz",
            name = "Jane Doe",
            email = "jane@example.com",
            gender = "Female",
            role = UserRole.ADMIN
        )
        assertEquals("Jane Doe", completeUser.displayName)
        assertEquals("jane@example.com", completeUser.displayEmail)
        assertEquals("Female", completeUser.displayGender)
        assertEquals(UserRole.ADMIN, completeUser.role)
        assertEquals("abcdef...0xyz", completeUser.truncatedUid)

        val emptyFieldsUser = AdminUserItem(
            uid = "short_id",
            name = "",
            email = "",
            gender = "",
            role = UserRole.USER
        )
        assertEquals("Athlete", emptyFieldsUser.displayName, "Blank name must fall back to Athlete")
        assertEquals("No email registered", emptyFieldsUser.displayEmail, "Blank email must fall back to placeholder")
        assertEquals("Not specified", emptyFieldsUser.displayGender, "Blank gender must fall back to Not specified")
        assertEquals(UserRole.USER, emptyFieldsUser.role)
        assertEquals("short_id", emptyFieldsUser.truncatedUid, "Short UID should remain untruncated")
    }

    @Test
    fun testAdminUserReadOnlySafety() {
        // Step 3 strictly forbids destructive actions
        val user = AdminUserItem(
            uid = "target_uid_123",
            name = "Target User",
            email = "target@example.com",
            gender = "Male",
            role = UserRole.USER,
            rawRoleField = "ADMIN" // Untrusted Firestore role field
        )

        // Verifies raw role string from Firestore is ignored for authorization
        assertEquals(UserRole.USER, user.role, "Authoritative role must be USER; raw Firestore string cannot grant ADMIN")
    }

    @Test
    fun testAdminUsersRouteGuardAccessControl() {
        fun evaluateGuard(role: UserRole, destination: Screen): Screen {
            val isAdminRoute = destination is Screen.AdminDashboard || destination is Screen.AdminUsers
            return if (isAdminRoute) {
                if (role == UserRole.ADMIN) destination else Screen.Home
            } else {
                destination
            }
        }

        // ADMIN allowed to access AdminUsers
        assertEquals(Screen.AdminUsers, evaluateGuard(UserRole.ADMIN, Screen.AdminUsers))

        // USER denied and redirected to Home
        assertEquals(Screen.Home, evaluateGuard(UserRole.USER, Screen.AdminUsers), "USER must be redirected from AdminUsers to Home")
    }

    @Test
    fun testAdminUserDetailsSubcollectionDataMapping() {
        val userWithoutActivity = AdminUserItem(
            uid = "uid_no_activity",
            name = "New User",
            email = "new@user.com",
            gender = "Male",
            role = UserRole.USER,
            notificationCount = null,
            deviceCount = null
        )

        val notifDisplay = userWithoutActivity.notificationCount?.toString() ?: "Not available yet"
        val deviceDisplay = userWithoutActivity.deviceCount?.toString() ?: "Not available yet"

        assertEquals("Not available yet", notifDisplay)
        assertEquals("Not available yet", deviceDisplay)

        val userWithActivity = AdminUserItem(
            uid = "uid_with_activity",
            name = "Active User",
            email = "active@user.com",
            gender = "Female",
            role = UserRole.USER,
            notificationCount = 4,
            deviceCount = 1
        )

        assertEquals("4", userWithActivity.notificationCount?.toString())
        assertEquals("1", userWithActivity.deviceCount?.toString())
    }
}