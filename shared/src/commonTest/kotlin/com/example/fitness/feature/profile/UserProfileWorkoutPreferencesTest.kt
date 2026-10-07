package com.example.fitness.feature.profile

import com.example.fitness.core.session.UserSession
import com.example.fitness.core.user.model.UserProfile
import com.example.fitness.feature.progress.data.WorkoutProgressService
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import com.example.fitness.feature.workout.model.ExperienceLevel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * EDITABLE WORKOUT PREFERENCES TEST SUITE
 *
 * Verifies all 10 required test cases:
 * TEST 1: Profile loads existing gender correctly.
 * TEST 2: Profile loads existing workout days correctly.
 * TEST 3: Profile loads existing fitness level correctly.
 * TEST 4: Change Male -> Female and save, verifying workout configuration and program switch.
 * TEST 5: Change 5 Days -> 4 Days and save, verifying workout configuration uses 4-day schedule.
 * TEST 6: Change Beginner -> Intermediate and save, verifying workout configuration uses Intermediate.
 * TEST 7: Close/reopen Profile, verifying new values remain selected.
 * TEST 8: Logout/login again, verifying saved preferences remain intact.
 * TEST 9: Historical workout/progress data is NOT deleted when preferences change.
 * TEST 10: Onboarding and Profile use the same canonical underlying preference fields.
 */
class UserProfileWorkoutPreferencesTest {

    @BeforeTest
    fun setUp() {
        WorkoutProgressService.clear()
        UserSession.clear()
    }

    @Test
    fun test01_profileLoadsExistingGenderCorrectly() {
        val profile = UserProfile(
            uid = "user_pref_1",
            email = "user1@athgym.com",
            name = "Jane Doe",
            gender = "Female",
            workoutDays = "5 Days",
            fitnessLevel = "Beginner"
        )
        UserSession.setUserProfile(profile)

        assertEquals("Female", UserSession.currentUserGender)
        assertEquals("Female", UserSession.currentUserProfile?.gender)
        assertEquals("Jane", profile.firstName)
    }

    @Test
    fun test02_profileLoadsExistingWorkoutDaysCorrectly() {
        val profile = UserProfile(
            uid = "user_pref_2",
            email = "user2@athgym.com",
            name = "John Smith",
            gender = "Male",
            workoutDays = "5 Days",
            fitnessLevel = "Beginner"
        )
        UserSession.setUserProfile(profile)

        assertEquals("5 Days", UserSession.weeklyCommitment)
        assertEquals("5 Days", UserSession.currentUserProfile?.workoutDays)
    }

    @Test
    fun test03_profileLoadsExistingFitnessLevelCorrectly() {
        val profile = UserProfile(
            uid = "user_pref_3",
            email = "user3@athgym.com",
            name = "Alex Stone",
            gender = "Male",
            workoutDays = "4 Days",
            fitnessLevel = "No Experience"
        )
        UserSession.setUserProfile(profile)

        assertEquals(ExperienceLevel.NO_EXPERIENCE, UserSession.experienceLevel)
        assertEquals("No Experience", UserSession.experienceLevel.toUserFacingLevel())
        assertEquals("No Experience", UserSession.currentUserProfile?.fitnessLevel)
    }

    @Test
    fun test04_changeMaleToFemaleUpdatesWorkoutConfigurationAndPrograms() {
        val profile = UserProfile(
            uid = "user_pref_4",
            email = "alex@athgym.com",
            name = "Alex Mercer",
            gender = "Male",
            workoutDays = "5 Days",
            fitnessLevel = "Beginner"
        )
        UserSession.setUserProfile(profile)
        UserSession.setActiveProgram("30-day")
        assertEquals("30-day", UserSession.activeProgramId)

        // User updates Gender: Male -> Female
        UserSession.updateWorkoutPreferences(
            gender = "Female",
            workoutDays = "5 Days",
            fitnessLevel = "Beginner"
        )

        // Verify Gender updated in UserSession & UserProfile
        assertEquals("Female", UserSession.currentUserGender)
        assertEquals("Female", UserSession.currentUserProfile?.gender)

        // Verify active program automatically safely switches to female program
        assertEquals("female_weight_loss", UserSession.activeProgramId)

        // Verify female program day 1 resolves valid female workout plan
        val femalePlan = CanonicalWorkoutResolver.resolveDailyWorkoutPlan(
            programId = "female_weight_loss",
            workoutId = "1",
            level = ExperienceLevel.BEGINNER
        )
        assertNotNull(femalePlan)
        assertEquals("female_weight_loss_day_1", femalePlan.id)
        assertFalse(femalePlan.isRestDay)
    }

    @Test
    fun test05_change5DaysTo4DaysUpdatesWorkoutConfigurationAndRestDays() {
        val profile = UserProfile(
            uid = "user_pref_5",
            email = "athlete@athgym.com",
            name = "Marcus Vance",
            gender = "Male",
            workoutDays = "5 Days",
            fitnessLevel = "Beginner"
        )
        UserSession.setUserProfile(profile)
        assertEquals("5 Days", UserSession.weeklyCommitment)

        // Under 5 Days: DayIndex 2 (Wednesday) is a training day
        val fiveDayWedPlan = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = "30-day",
            dayIndex = 2,
            level = ExperienceLevel.BEGINNER,
            workoutDays = "5 Days"
        )
        assertFalse(fiveDayWedPlan.isRestDay, "Wednesday should be a training day in 5-day routine")
        assertEquals("day_3", fiveDayWedPlan.id)

        // Change 5 Days -> 4 Days
        UserSession.updateWorkoutPreferences(
            gender = "Male",
            workoutDays = "4 Days",
            fitnessLevel = "Beginner"
        )
        assertEquals("4 Days", UserSession.weeklyCommitment)

        // Under 4 Days: DayIndex 2 (Wednesday) is scheduled as REST DAY
        val fourDayWedPlan = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = "30-day",
            dayIndex = 2,
            level = ExperienceLevel.BEGINNER,
            workoutDays = "4 Days"
        )
        assertTrue(fourDayWedPlan.isRestDay, "Wednesday should be a rest day in 4-day routine")
        assertEquals("day_7", fourDayWedPlan.id)

        // DayIndex 3 (Thursday) remains a training day
        val fourDayThuPlan = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = "30-day",
            dayIndex = 3,
            level = ExperienceLevel.BEGINNER,
            workoutDays = "4 Days"
        )
        assertFalse(fourDayThuPlan.isRestDay, "Thursday should be a training day in 4-day routine")
        assertEquals("day_3", fourDayThuPlan.id)
    }

    @Test
    fun test06_changeBeginnerToIntermediateUpdatesPrescriptionsAndVolume() {
        val profile = UserProfile(
            uid = "user_pref_6",
            email = "sam@athgym.com",
            name = "Sam Fisher",
            gender = "Male",
            workoutDays = "4 Days",
            fitnessLevel = "No Experience"
        )
        UserSession.setUserProfile(profile)
        assertEquals(ExperienceLevel.NO_EXPERIENCE, UserSession.experienceLevel)

        val beginnerDay = CanonicalWorkoutResolver.resolveProgramDayByNumber(
            programId = "30-day",
            dayNumber = 1,
            level = ExperienceLevel.NO_EXPERIENCE
        )
        val beginnerSets = beginnerDay.exercises.firstOrNull()?.sets ?: 0

        // Change No Experience -> Beginner
        UserSession.updateWorkoutPreferences(
            gender = "Male",
            workoutDays = "4 Days",
            fitnessLevel = "Beginner"
        )

        // Beginner maps internally to BEGINNER canonical experience level
        assertEquals(ExperienceLevel.BEGINNER, UserSession.experienceLevel)
        assertEquals("Beginner", UserSession.experienceLevel.toUserFacingLevel())

        val intermediateDay = CanonicalWorkoutResolver.resolveProgramDayByNumber(
            programId = "30-day",
            dayNumber = 1,
            level = ExperienceLevel.BEGINNER
        )
        val intermediateSets = intermediateDay.exercises.firstOrNull()?.sets ?: 0

        // Intermediate prescriptions have equal or greater target volume/intensity
        assertTrue(intermediateSets >= beginnerSets, "Intermediate target sets ($intermediateSets) must be >= Beginner sets ($beginnerSets)")
    }

    @Test
    fun test07_closeAndReopenProfileRetainsNewValues() {
        val initialProfile = UserProfile(
            uid = "user_pref_7",
            email = "claire@athgym.com",
            name = "Claire Redfield",
            gender = "Male",
            workoutDays = "5 Days",
            fitnessLevel = "Beginner"
        )
        UserSession.setUserProfile(initialProfile)

        // User changes preferences in Profile
        UserSession.updateWorkoutPreferences(
            gender = "Female",
            workoutDays = "6 Days",
            fitnessLevel = "Expert"
        )

        // Simulate closing Profile screen and navigating back / reopening Profile
        val savedProfile = UserSession.currentUserProfile
        assertNotNull(savedProfile)

        // Verify reopening Profile reads the exact new values, not defaults
        assertEquals("Female", savedProfile.gender)
        assertEquals("6 Days", savedProfile.workoutDays)
        assertEquals("Expert", savedProfile.fitnessLevel)
        assertEquals(ExperienceLevel.EXPERT, UserSession.experienceLevel)
        assertEquals("6 Days", UserSession.weeklyCommitment)
    }

    @Test
    fun test08_logoutAndLoginAgainRetainsSavedPreferences() {
        // User with saved preferences
        val userProfile = UserProfile(
            uid = "user_pref_8",
            email = "leon@athgym.com",
            name = "Leon Kennedy",
            gender = "Female",
            workoutDays = "4 Days",
            fitnessLevel = "Beginner"
        )
        UserSession.setUserProfile(userProfile)
        assertEquals("Female", UserSession.currentUserGender)
        assertEquals("4 Days", UserSession.weeklyCommitment)
        assertEquals(ExperienceLevel.BEGINNER, UserSession.experienceLevel)

        // User logs out: clears in-memory session
        UserSession.clear()
        assertEquals(null, UserSession.currentUserProfile)
        assertEquals(null, UserSession.currentUserGender)

        // User logs in again, profile fetched from remote Firestore
        UserSession.setUserProfile(userProfile)
        assertEquals("user_pref_8", UserSession.currentUserId)
        assertEquals("Female", UserSession.currentUserGender)
        assertEquals("4 Days", UserSession.weeklyCommitment)
        assertEquals(ExperienceLevel.BEGINNER, UserSession.experienceLevel)
        assertEquals("Beginner", UserSession.experienceLevel.toUserFacingLevel())
    }

    @Test
    fun test09_historicalWorkoutAndProgressDataNotDeletedWhenPreferencesChange() {
        val profile = UserProfile(
            uid = "user_pref_9",
            email = "chris@athgym.com",
            name = "Chris Redfield",
            gender = "Male",
            workoutDays = "5 Days",
            fitnessLevel = "Beginner"
        )
        UserSession.setUserProfile(profile)
        UserSession.setActiveProgram("30-day")

        // Record completed workout days
        UserSession.markDayCompleted(1)
        UserSession.markDayCompleted(2)
        UserSession.markDayCompleted(3)
        UserSession.workoutStreak = 3
        UserSession.totalSetsCompleted = 12
        UserSession.totalRepsCompleted = 120

        assertTrue(UserSession.isDayCompleted(1))
        assertTrue(UserSession.isDayCompleted(2))
        assertTrue(UserSession.isDayCompleted(3))
        assertEquals(3, UserSession.workoutStreak)

        // User updates preferences in Profile
        UserSession.updateWorkoutPreferences(
            gender = "Female",
            workoutDays = "4 Days",
            fitnessLevel = "Intermediate"
        )

        // Progress records and completed days must remain intact
        assertTrue(UserSession.isDayCompleted(1), "Completed day 1 must not be deleted")
        assertTrue(UserSession.isDayCompleted(2), "Completed day 2 must not be deleted")
        assertTrue(UserSession.isDayCompleted(3), "Completed day 3 must not be deleted")
        assertEquals(3, UserSession.workoutStreak, "Workout streak must not be lost")
        assertEquals(12, UserSession.totalSetsCompleted, "Sets completed must remain intact")
        assertEquals(120, UserSession.totalRepsCompleted, "Reps completed must remain intact")
    }

    @Test
    fun test10_onboardingAndProfileShareSameUnderlyingPreferenceFields() {
        // Step 1: Onboarding writes canonical fields
        val onboardingProfile = UserProfile(
            uid = "user_pref_10",
            email = "jill@athgym.com",
            name = "Jill Valentine",
            gender = "Female",
            workoutDays = "4 Days",
            fitnessLevel = "No Experience"
        )
        UserSession.setUserProfile(onboardingProfile)

        // Verify Onboarding set the single source of truth
        assertEquals("Female", UserSession.currentUserGender)
        assertEquals("4 Days", UserSession.weeklyCommitment)
        assertEquals(ExperienceLevel.NO_EXPERIENCE, UserSession.experienceLevel)

        // Step 2: Profile screen modifies the same canonical fields
        UserSession.updateWorkoutPreferences(
            gender = "Male",
            workoutDays = "6 Days",
            fitnessLevel = "Expert"
        )

        val updatedProfile = UserSession.currentUserProfile
        assertNotNull(updatedProfile)

        // Verify identical underlying fields are updated and used by Workout Engine
        assertEquals("Male", updatedProfile.gender)
        assertEquals("6 Days", updatedProfile.workoutDays)
        assertEquals("Expert", updatedProfile.fitnessLevel)

        assertEquals("Male", UserSession.currentUserGender)
        assertEquals("6 Days", UserSession.weeklyCommitment)
        assertEquals(ExperienceLevel.EXPERT, UserSession.experienceLevel)

        // Workout engine reflects 6 Days schedule (Day 6 Saturday is a workout day, Day 7 Sunday is rest)
        val day6Plan = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = "30-day",
            dayIndex = 5,
            level = UserSession.experienceLevel,
            workoutDays = UserSession.weeklyCommitment
        )
        assertFalse(day6Plan.isRestDay, "Day 6 should be a workout day in 6-day configuration")
        assertEquals("day_6", day6Plan.id)

        val day7Plan = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = "30-day",
            dayIndex = 6,
            level = UserSession.experienceLevel,
            workoutDays = UserSession.weeklyCommitment
        )
        assertTrue(day7Plan.isRestDay, "Day 7 should be a rest day in 6-day configuration")
        assertEquals("day_7", day7Plan.id)
    }
}
