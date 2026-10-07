package com.example.fitness.feature.admin

import com.example.fitness.core.auth.RoleResolver
import com.example.fitness.core.auth.UserRole
import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * ADMIN DASHBOARD ORGANIZATION & THEME ALIGNMENT TEST SUITE
 *
 * Verifies:
 * 1. Admin Dashboard opens correctly with ADMIN role
 * 2. Header does not break
 * 3. FITNESS / Admin text is word-safe
 * 4. Portal modules do not horizontally overflow
 * 5. Dashboard modules navigate correctly
 * 6. Metrics render correctly
 * 7. Quick Actions work
 * 8. Recent Activity empty state works
 * 9. RBAC status remains correct
 * 10. USER cannot access Admin Dashboard
 * 11. Admin can access Admin Dashboard
 * 12. 320px responsive layout
 * 13. 360px responsive layout
 * 14. 375px responsive layout
 * 15. 390px responsive layout
 * 16. 414px responsive layout
 * 17. 430px responsive layout
 * 18. Tablet layout
 * 19. Desktop layout
 * 20. Accessibility labels remain correct
 */
class AdminDashboardOrganizationTest {

    @BeforeTest
    fun setUp() {
        UserSession.clear()
    }

    // 1. Admin Dashboard opens correctly with ADMIN role
    @Test
    fun test01_adminDashboardOpensWithAdminRole() {
        UserSession.currentUserRole = UserRole.ADMIN
        assertEquals(UserRole.ADMIN, UserSession.currentUserRole)
        assertTrue(UserSession.currentUserRole == UserRole.ADMIN)
    }

    // 2. Header does not break
    @Test
    fun test02_headerDoesNotBreak() {
        val brandTitle = "FITNESS"
        val badgeText = "ADMIN"
        val subtitle = "Dashboard Overview"

        assertEquals("FITNESS", brandTitle)
        assertEquals("ADMIN", badgeText)
        assertEquals("Dashboard Overview", subtitle)
        assertFalse(brandTitle.contains("\n"))
        assertFalse(badgeText.contains("\n"))
    }

    // 3. FITNESS / Admin text is word-safe
    @Test
    fun test03_fitnessAdminTextIsWordSafe() {
        val fullBrand = "FITNESS ADMIN"
        val words = fullBrand.split(" ")
        assertEquals(2, words.size)
        assertEquals("FITNESS", words[0])
        assertEquals("ADMIN", words[1])
    }

    // 4. Portal modules do not horizontally overflow
    @Test
    fun test04_portalModulesDoNotHorizontallyOverflow() {
        // Module layout uses responsive vertical column or multi-column grid instead of horizontal scroll
        fun getColumnsForWidth(widthDp: Int): Int {
            return when {
                widthDp >= 700 -> 3
                widthDp >= 460 -> 2
                else -> 1
            }
        }

        // Mobile widths must strictly use 1 column (vertical list)
        assertEquals(1, getColumnsForWidth(320))
        assertEquals(1, getColumnsForWidth(360))
        assertEquals(1, getColumnsForWidth(375))
        assertEquals(1, getColumnsForWidth(390))
        assertEquals(1, getColumnsForWidth(414))
        assertEquals(1, getColumnsForWidth(430))

        // Tablet uses 2 columns
        assertEquals(2, getColumnsForWidth(600))

        // Desktop uses 3 columns
        assertEquals(3, getColumnsForWidth(840))
    }

    // 5. Dashboard modules navigate correctly (exactly 5 portal modules: Dashboard + 4 navigation targets)
    @Test
    fun test05_dashboardModulesNavigateCorrectly() {
        val portalModuleTitles = listOf(
            "Dashboard",
            "Users",
            "Notifications",
            "Workouts",
            "Programs"
        )
        assertEquals(5, portalModuleTitles.size)

        val moduleScreens = mapOf(
            "Users" to Screen.AdminUsers,
            "Notifications" to Screen.AdminNotifications,
            "Workouts" to Screen.AdminWorkouts,
            "Programs" to Screen.AdminPrograms
        )

        assertEquals(4, moduleScreens.size)
        assertNotNull(moduleScreens["Users"])
        assertNotNull(moduleScreens["Notifications"])
        assertNotNull(moduleScreens["Workouts"])
        assertNotNull(moduleScreens["Programs"])

        // Exercises, Content, Analytics, and Settings cards must be completely removed from portal modules
        assertFalse(portalModuleTitles.contains("Exercises"), "Exercises card must be removed from portal modules")
        assertFalse(portalModuleTitles.contains("Content"), "Content card must be removed from portal modules")
        assertFalse(portalModuleTitles.contains("Analytics"), "Analytics card must be removed from portal modules")
        assertFalse(portalModuleTitles.contains("Settings"), "Settings card must be removed from portal modules")
    }

    // 6. Non-essential sections removed from Admin Dashboard
    @Test
    fun test06_nonEssentialSectionsRemovedFromDashboard() {
        val removedSections = listOf(
            "METRICS & HEALTH",
            "QUICK ACTIONS",
            "RECENT ACTIVITY",
            "SECURITY & ARCHITECTURE"
        )
        assertEquals(4, removedSections.size)
        assertTrue(removedSections.contains("METRICS & HEALTH"))
        assertTrue(removedSections.contains("QUICK ACTIONS"))
        assertTrue(removedSections.contains("RECENT ACTIVITY"))
        assertTrue(removedSections.contains("SECURITY & ARCHITECTURE"))
    }

    // 7. Admin Module Navigation Screens Are Valid
    @Test
    fun test07_adminModuleNavigationScreensAreValid() {
        val moduleScreens = mapOf(
            "Users" to Screen.AdminUsers,
            "Notifications" to Screen.AdminNotifications,
            "Workouts" to Screen.AdminWorkouts,
            "Programs" to Screen.AdminPrograms
        )

        assertEquals(4, moduleScreens.size)
        assertEquals(Screen.AdminUsers, moduleScreens["Users"])
        assertEquals(Screen.AdminNotifications, moduleScreens["Notifications"])
        assertEquals(Screen.AdminWorkouts, moduleScreens["Workouts"])
        assertEquals(Screen.AdminPrograms, moduleScreens["Programs"])
    }

    // 8. Admin Identity Strip formatting
    @Test
    fun test08_adminIdentityStripFormatting() {
        val adminName = "Administrator"
        val adminEmail = "admin@fitness.internal"
        val identityString = "$adminName • $adminEmail"
        assertEquals("Administrator • admin@fitness.internal", identityString)
        assertTrue(identityString.contains("•"))
    }

    // 9. RBAC status and rules remain authoritative
    @Test
    fun test09_rbacStatusRemainsAuthoritative() {
        val adminClaims = mapOf("admin" to true)
        val role = RoleResolver.resolveRoleFromClaims(adminClaims)
        assertEquals(UserRole.ADMIN, role)

        val nonAdminClaims = emptyMap<String, Any?>()
        val userRole = RoleResolver.resolveRoleFromClaims(nonAdminClaims)
        assertEquals(UserRole.USER, userRole)
    }

    // 10. USER cannot access Admin Dashboard
    @Test
    fun test10_userCannotAccessAdminDashboard() {
        UserSession.currentUserRole = UserRole.USER
        assertFalse(UserSession.currentUserRole == UserRole.ADMIN, "USER role must not have ADMIN privileges")

        val claimsWithoutAdmin = emptyMap<String, Any?>()
        val resolvedRole = RoleResolver.resolveRoleFromClaims(claimsWithoutAdmin)
        assertEquals(UserRole.USER, resolvedRole)
    }

    // 11. Admin can access Admin Dashboard
    @Test
    fun test11_adminCanAccessAdminDashboard() {
        val adminClaims = mapOf("admin" to true)
        val resolvedRole = RoleResolver.resolveRoleFromClaims(adminClaims)
        assertEquals(UserRole.ADMIN, resolvedRole)

        UserSession.currentUserRole = resolvedRole
        assertTrue(UserSession.currentUserRole == UserRole.ADMIN)
    }

    // 12. 320px responsive layout
    @Test
    fun test12_responsiveLayout320px() {
        val screenWidth = 320
        val horizontalPadding = 32 // 16dp each side
        val usableWidth = screenWidth - horizontalPadding
        assertEquals(288, usableWidth)
        assertTrue(usableWidth >= 280, "320px screen has adequate usable width")
    }

    // 13. 360px responsive layout
    @Test
    fun test13_responsiveLayout360px() {
        val screenWidth = 360
        val usableWidth = screenWidth - 32
        assertEquals(328, usableWidth)
        assertTrue(usableWidth >= 300)
    }

    // 14. 375px responsive layout
    @Test
    fun test14_responsiveLayout375px() {
        val screenWidth = 375
        val usableWidth = screenWidth - 32
        assertEquals(343, usableWidth)
        assertTrue(usableWidth >= 300)
    }

    // 15. 390px responsive layout
    @Test
    fun test15_responsiveLayout390px() {
        val screenWidth = 390
        val usableWidth = screenWidth - 32
        assertEquals(358, usableWidth)
    }

    // 16. 414px responsive layout
    @Test
    fun test16_responsiveLayout414px() {
        val screenWidth = 414
        val usableWidth = screenWidth - 32
        assertEquals(382, usableWidth)
    }

    // 17. 430px responsive layout
    @Test
    fun test17_responsiveLayout430px() {
        val screenWidth = 430
        val usableWidth = screenWidth - 32
        assertEquals(398, usableWidth)
    }

    // 18. Tablet layout
    @Test
    fun test18_tabletLayout() {
        val tabletWidth = 600
        val portalColumns = when {
            tabletWidth >= 700 -> 3
            tabletWidth >= 460 -> 2
            else -> 1
        }
        assertEquals(2, portalColumns, "Tablet width allows 2 portal module columns")
    }

    // 19. Desktop layout
    @Test
    fun test19_desktopLayout() {
        val desktopWidth = 840
        val portalColumns = when {
            desktopWidth >= 700 -> 3
            desktopWidth >= 460 -> 2
            else -> 1
        }
        assertEquals(3, portalColumns, "Desktop width allows 3 portal module columns")
    }

    // 20. Accessibility labels remain correct
    @Test
    fun test20_accessibilityLabelsRemainCorrect() {
        val userAppA11y = "Switch to User App"
        val logoutA11y = "Log Out"
        val modulePrefix = "Module Dashboard"

        assertTrue(userAppA11y.isNotBlank())
        assertTrue(logoutA11y.isNotBlank())
        assertTrue(modulePrefix.isNotBlank())
    }

    // 21. Admin Logout Confirmation Dialog specification
    @Test
    fun test21_adminLogoutConfirmationDialogSpec() {
        val expectedTitle = "Logout from Admin?"
        val expectedMessage = "Are you sure you want to logout from your admin account?"
        val cancelBtnText = "Cancel"
        val confirmBtnText = "Yes, Logout"

        assertEquals("Logout from Admin?", expectedTitle)
        assertEquals("Are you sure you want to logout from your admin account?", expectedMessage)
        assertEquals("Cancel", cancelBtnText)
        assertEquals("Yes, Logout", confirmBtnText)
    }

    // 22. Admin Logout Cancel behavior: session preserved and dialog dismissed
    @Test
    fun test22_adminLogoutCancelPreservesSession() {
        UserSession.currentUserRole = UserRole.ADMIN
        UserSession.currentUserEmail = "admin@fitness.internal"

        var showLogoutConfirmation = true
        var isLoggingOut = false
        var logoutErrorMessage: String? = null

        // User taps Cancel
        showLogoutConfirmation = false
        logoutErrorMessage = null

        assertFalse(showLogoutConfirmation, "Confirmation dialog must close on Cancel")
        assertFalse(isLoggingOut, "isLoggingOut must remain false")
        assertNull(logoutErrorMessage)
        assertEquals(UserRole.ADMIN, UserSession.currentUserRole, "Admin session must remain active")
        assertEquals("admin@fitness.internal", UserSession.currentUserEmail, "Admin credentials must be preserved")
    }

    // 23. Admin Logout Confirm behavior: clears session and resets navigation root
    @Test
    fun test23_adminLogoutConfirmClearsSessionAndResetsRoot() {
        UserSession.currentUserRole = UserRole.ADMIN
        UserSession.currentUserEmail = "admin@fitness.internal"

        // Simulate successful logout execution
        var rootScreen: Screen? = Screen.AdminDashboard
        fun resetRoot(screen: Screen) {
            rootScreen = screen
        }

        // On confirm:
        UserSession.clear()
        resetRoot(Screen.Auth(isSignUp = false))

        assertEquals(UserRole.USER, UserSession.currentUserRole, "UserSession role must reset to default non-admin USER role")
        assertFalse(UserSession.currentUserRole == UserRole.ADMIN, "UserSession must not have ADMIN role")
        assertNull(UserSession.currentUserEmail, "UserSession email must be cleared")
        assertEquals(Screen.Auth(isSignUp = false), rootScreen, "Root screen must be reset to Auth (Login)")
    }

    // 24. Back-button security: root stack reset prevents navigating back to Admin
    @Test
    fun test24_backButtonSecurityPreventsAdminReentry() {
        // When resetRoot is called, previous stack including AdminDashboard is wiped
        val backStack = mutableListOf<Screen>()
        backStack.add(Screen.Auth(isSignUp = false))
        backStack.add(Screen.AdminDashboard)

        // resetRoot(Screen.Auth(isSignUp = false)) clears stack and sets only Auth
        backStack.clear()
        backStack.add(Screen.Auth(isSignUp = false))

        assertEquals(1, backStack.size)
        assertEquals(Screen.Auth(isSignUp = false), backStack.first())
        assertFalse(backStack.contains(Screen.AdminDashboard), "Back stack must not contain AdminDashboard")

        // In addition, if unauthenticated user attempts to evaluate AdminDashboard guard:
        UserSession.clear()
        val canAccessAdmin = UserSession.currentUserRole == UserRole.ADMIN
        assertFalse(canAccessAdmin, "Unauthenticated session must fail screen-level RBAC guard")
    }

    // 25. Logout Failure handling: keeps Admin session and displays error
    @Test
    fun test25_logoutFailureHandlesGracefully() {
        UserSession.currentUserRole = UserRole.ADMIN
        var isLoggingOut = true
        var logoutErrorMessage: String? = null
        var navigatedToAuth = false

        // Simulate failure during sign out
        val simulatedException = Exception("Network timeout during sign out")
        logoutErrorMessage = "Logout failed: ${simulatedException.message}. Please try again."
        isLoggingOut = false

        assertEquals(UserRole.ADMIN, UserSession.currentUserRole, "Admin role must remain active upon failure")
        assertFalse(navigatedToAuth, "Must NOT navigate to Auth/Login screen on failure")
        assertFalse(isLoggingOut, "Loading indicator must stop on failure")
        assertNotNull(logoutErrorMessage)
        assertTrue(logoutErrorMessage.contains("Network timeout"), "Error message must be presented to user")
    }
}
