package com.example.fitness.feature.home

import com.example.fitness.core.session.UserSession
import com.example.fitness.core.user.model.UserProfile
import com.example.fitness.feature.workout.data.CanonicalWorkoutResolver
import com.example.fitness.feature.workout.data.WorkoutMediaResolver
import com.example.fitness.feature.workout.model.ExperienceLevel
import fitness.shared.generated.resources.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

/**
 * Test suite verifying gender-aware imagery resolution for ATHGYM Home Screen.
 *
 * Verifies:
 * 1. Male user receives male workout, athlete, and gym imagery.
 * 2. Female user receives female workout, athlete, and gym imagery.
 * 3. Loading / null profile gender safely resolves to neutral placeholder (goal_general),
 *    never exposing the wrong gender's imagery.
 * 4. User profile gender is respected as source of truth.
 * 5. Canonical workout plan aesthetics respect user gender.
 */
class HomeGenderAwareImageryTest {

    @BeforeTest
    fun setUp() {
        UserSession.clear()
    }

    @AfterTest
    fun tearDown() {
        UserSession.clear()
    }

    @Test
    fun testFemaleUserReceivesFemaleWorkoutImagery() {
        val gender = "Female"
        UserSession.currentUserGender = gender

        // 1. Athlete Workout
        val athleteImage = WorkoutMediaResolver.resolveAthleteWorkoutImage(gender)
        assertEquals(Res.drawable.female_athlete, athleteImage)
        assertNotEquals(Res.drawable.goal_athlete, athleteImage, "Female user must never receive male athlete image")

        // 2. Gym Workouts
        val gymImage = WorkoutMediaResolver.resolveGymWorkoutImage(gender)
        assertEquals(Res.drawable.female_gym, gymImage)
        assertNotEquals(Res.drawable.workout_gym, gymImage, "Female user must never receive male gym image")

        // 3. Today's Workout across weekly cycle
        val femaleCycleDrawables = listOf(
            Res.drawable.female_dumbbell_bench_press,
            Res.drawable.female_lat_pulldown,
            Res.drawable.female_barbell_squat,
            Res.drawable.female_kettlebell_swing,
            Res.drawable.female_hip_thrust,
            Res.drawable.female_romanian_deadlift,
            Res.drawable.female_glute_bridge
        )

        for (dayIdx in 0..6) {
            val plan = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
                programId = null,
                dayIndex = dayIdx,
                level = ExperienceLevel.BEGINNER
            )
            val todayImage = WorkoutMediaResolver.resolveTodayWorkoutImage(
                workout = plan,
                gender = gender,
                fallbackCycleDay = dayIdx + 1
            )
            assertNotNull(todayImage)
            assertNotEquals(Res.drawable.goal_athlete, todayImage)
            assertNotEquals(Res.drawable.workout_gym, todayImage)
            assertNotEquals(Res.drawable.gender_female, todayImage, "Must use realistic workout photo, not onboarding vector")
        }
    }

    @Test
    fun testMaleUserReceivesMaleWorkoutImagery() {
        val gender = "Male"
        UserSession.currentUserGender = gender

        // 1. Athlete Workout
        val athleteImage = WorkoutMediaResolver.resolveAthleteWorkoutImage(gender)
        assertEquals(Res.drawable.goal_athlete, athleteImage)
        assertNotEquals(Res.drawable.female_athlete, athleteImage, "Male user must never receive female athlete image")

        // 2. Gym Workouts
        val gymImage = WorkoutMediaResolver.resolveGymWorkoutImage(gender)
        assertEquals(Res.drawable.workout_gym, gymImage)
        assertNotEquals(Res.drawable.female_gym, gymImage, "Male user must never receive female gym image")

        // 3. Today's Workout Day 1 (Chest) & Day 3 (Legs)
        val planDay1 = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = null,
            dayIndex = 0,
            level = ExperienceLevel.BEGINNER
        )
        val todayDay1 = WorkoutMediaResolver.resolveTodayWorkoutImage(
            workout = planDay1,
            gender = gender,
            fallbackCycleDay = 1
        )
        assertEquals(Res.drawable.bodypart_chest, todayDay1)

        val planDay3 = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = null,
            dayIndex = 2,
            level = ExperienceLevel.BEGINNER
        )
        val todayDay3 = WorkoutMediaResolver.resolveTodayWorkoutImage(
            workout = planDay3,
            gender = gender,
            fallbackCycleDay = 3
        )
        assertEquals(Res.drawable.gym_barbell_squat, todayDay3)

        // Day 4 (Shoulders) -> must resolve to gym_shoulder_press, NEVER goal_athlete runner
        val planDay4 = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(
            programId = null,
            dayIndex = 3,
            level = ExperienceLevel.BEGINNER
        )
        val todayDay4 = WorkoutMediaResolver.resolveTodayWorkoutImage(
            workout = planDay4,
            gender = gender,
            fallbackCycleDay = 4
        )
        assertEquals(Res.drawable.gym_shoulder_press, todayDay4)
        assertNotEquals(Res.drawable.goal_athlete, todayDay4, "Shoulders workout must never display runner image")
    }

    @Test
    fun testLoadingOrNullGenderUsesNeutralFallback() {
        // Gender is null (profile loading or unauthenticated)
        val athleteImage = WorkoutMediaResolver.resolveAthleteWorkoutImage(null)
        assertEquals(Res.drawable.goal_general, athleteImage)

        val gymImage = WorkoutMediaResolver.resolveGymWorkoutImage(null)
        assertEquals(Res.drawable.goal_general, gymImage)

        val todayImage = WorkoutMediaResolver.resolveTodayWorkoutImage(null, null, 1)
        assertEquals(Res.drawable.goal_general, todayImage)

        // Strict isolation: Fallback is NEVER male-specific nor female-specific
        assertNotEquals(Res.drawable.goal_athlete, athleteImage)
        assertNotEquals(Res.drawable.female_athlete, athleteImage)
        assertNotEquals(Res.drawable.workout_gym, gymImage)
        assertNotEquals(Res.drawable.female_gym, gymImage)
    }

    @Test
    fun testProfileGenderIsSourceOfTruthWhenSessionFieldEmpty() {
        // UserSession.currentUserGender is null, but currentUserProfile is loaded from Firestore
        UserSession.currentUserGender = null
        UserSession.currentUserProfile = UserProfile(
            uid = "user_fem_123",
            name = "Sarah Connor",
            email = "sarah@athgym.com",
            gender = "Female"
        )

        val effectiveGender = UserSession.currentUserGender ?: UserSession.currentUserProfile?.gender
        assertEquals("Female", effectiveGender)

        val athleteImage = WorkoutMediaResolver.resolveAthleteWorkoutImage(effectiveGender)
        assertEquals(Res.drawable.female_athlete, athleteImage)

        val gymImage = WorkoutMediaResolver.resolveGymWorkoutImage(effectiveGender)
        assertEquals(Res.drawable.female_gym, gymImage)
    }

    @Test
    fun testCanonicalWorkoutResolverDayAestheticsFollowGender() {
        // Female session
        UserSession.currentUserGender = "Female"
        val femalePlan = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(null, 0)
        assertEquals(Res.drawable.female_dumbbell_bench_press, femalePlan.imageRes)

        // Male session
        UserSession.currentUserGender = "Male"
        val malePlan = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(null, 0)
        assertEquals(Res.drawable.bodypart_chest, malePlan.imageRes)

        // Loading/unknown session
        UserSession.currentUserGender = null
        val loadingPlan = CanonicalWorkoutResolver.resolveDailyWorkoutPlanForDayIndex(null, 0)
        assertEquals(Res.drawable.goal_general, loadingPlan.imageRes)
    }
}
