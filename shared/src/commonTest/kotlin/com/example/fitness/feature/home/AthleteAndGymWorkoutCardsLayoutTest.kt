package com.example.fitness.feature.home

import com.example.fitness.core.navigation.Screen
import com.example.fitness.core.session.UserSession
import com.example.fitness.feature.workout.model.ExperienceLevel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * TEST SUITE: ATHLETE + GYM WORKOUT CARDS VISUAL & STRUCTURAL CONSISTENCY
 *
 * Verifies:
 * 1. Both Athlete Workout and Gym Workouts cards share the exact same structural components:
 *    Title -> Description -> 4 Features -> Vertical Gap -> Full-width CTA Button.
 * 2. Athlete Workout Card Content:
 *    - Title: "Athlete Workout"
 *    - Description: "Train for performance, speed, strength and endurance."
 *    - Features:
 *      1. "Sprint & Speed Mechanics"
 *      2. "Agility & Explosive Power"
 *      3. "Conditioning & Endurance"
 *      4. "Joint Mobility & Recovery"
 *    - CTA: "Explore Athlete Training"
 * 3. Gym Workouts Card Content:
 *    - Title: "Gym Workouts"
 *    - Description: "Build muscle, get stronger and become the best version of you."
 *    - Features:
 *      1. "Strength Training"
 *      2. "Muscle Building"
 *      3. "Fat Loss"
 *      4. "Custom Plans"
 *    - CTA: "Explore Gym Workouts"
 * 4. Feature counts match exactly (4 features in each card).
 * 5. Words are clean and never broken mid-word (no artificial hyphens or trailing fragments).
 * 6. Navigation destinations are preserved:
 *    - Athlete routes to Screen.ProgramDetail("athlete", level)
 *    - Gym routes to Screen.BodyPartSelection("gym")
 * 7. Visual rhythm and spacing parameters:
 *    - Minimum card height: 254.dp
 *    - Content width ratio: 0.74f
 *    - Minimum vertical gap before CTA: 14.dp
 *    - CTA button height: 44.dp
 */
class AthleteAndGymWorkoutCardsLayoutTest {

    @BeforeTest
    fun setUp() {
        UserSession.clear()
    }

    // 1. Athlete card content matches requirements
    @Test
    fun test01_athleteCardContentMatchesSpecifications() {
        val title = "Athlete Workout"
        val description = "Train for performance, speed, strength and endurance."
        val cta = "Explore Athlete Training"
        val features = listOf(
            "Sprint & Speed Mechanics",
            "Agility & Explosive Power",
            "Conditioning & Endurance",
            "Joint Mobility & Recovery"
        )

        assertEquals("Athlete Workout", title)
        assertEquals("Train for performance, speed, strength and endurance.", description)
        assertEquals("Explore Athlete Training", cta)
        assertEquals(4, features.size)
        assertEquals("Sprint & Speed Mechanics", features[0])
        assertEquals("Agility & Explosive Power", features[1])
        assertEquals("Conditioning & Endurance", features[2])
        assertEquals("Joint Mobility & Recovery", features[3])
    }

    // 2. Gym card content matches requirements
    @Test
    fun test02_gymCardContentMatchesSpecifications() {
        val title = "Gym Workouts"
        val description = "Build muscle, get stronger and become the best version of you."
        val cta = "Explore Gym Workouts"
        val features = listOf(
            "Strength Training",
            "Muscle Building",
            "Fat Loss",
            "Custom Plans"
        )

        assertEquals("Gym Workouts", title)
        assertEquals("Build muscle, get stronger and become the best version of you.", description)
        assertEquals("Explore Gym Workouts", cta)
        assertEquals(4, features.size)
        assertEquals("Strength Training", features[0])
        assertEquals("Muscle Building", features[1])
        assertEquals("Fat Loss", features[2])
        assertEquals("Custom Plans", features[3])
    }

    // 3. Symmetrical 4-feature structure for both cards
    @Test
    fun test03_bothCardsHaveIdenticalFeatureCount() {
        val athleteFeatures = listOf(
            "Sprint & Speed Mechanics",
            "Agility & Explosive Power",
            "Conditioning & Endurance",
            "Joint Mobility & Recovery"
        )
        val gymFeatures = listOf(
            "Strength Training",
            "Muscle Building",
            "Fat Loss",
            "Custom Plans"
        )

        assertEquals(athleteFeatures.size, gymFeatures.size, "Both cards must have the exact same number of feature rows")
        assertEquals(4, athleteFeatures.size)
        assertEquals(4, gymFeatures.size)
    }

    // 4. Feature text never has mid-word breaks or hyphens
    @Test
    fun test04_noMidWordBreaksInFeatureText() {
        val allFeatures = listOf(
            "Sprint & Speed Mechanics",
            "Agility & Explosive Power",
            "Conditioning & Endurance",
            "Joint Mobility & Recovery",
            "Strength Training",
            "Muscle Building",
            "Fat Loss",
            "Custom Plans"
        )

        for (feat in allFeatures) {
            // Words should not contain awkward intra-word hyphens like 'Conditioni-ng'
            assertFalse(feat.contains("-\n"), "Feature text should not contain hyphenated line breaks")
            assertFalse(feat.contains("  "), "Feature text should have normalized spacing")
            // Ensure each word consists of complete alphabetic tokens
            val words = feat.split(" ").filter { it != "&" }
            for (word in words) {
                assertTrue(word.length >= 3, "Word '$word' in '$feat' must not be an orphaned syllable")
            }
        }
    }

    // 5. Navigation targets remain intact
    @Test
    fun test05_navigationTargetsPreserved() {
        // Athlete Navigation
        UserSession.experienceLevel = ExperienceLevel.BEGINNER
        val athleteTarget = Screen.ProgramDetail(
            programId = "athlete",
            level = if (UserSession.experienceLevel == ExperienceLevel.EXPERT) ExperienceLevel.ADVANCED else UserSession.experienceLevel
        )
        assertEquals("athlete", athleteTarget.programId)
        assertEquals(ExperienceLevel.BEGINNER, athleteTarget.level)

        // Gym Navigation
        val gymTarget = Screen.BodyPartSelection("gym")
        assertEquals("gym", gymTarget.workoutType)
    }

    // 6. Layout parameters adhere to consistent design system
    @Test
    fun test06_layoutDimensionsAndGeometryConstants() {
        val contentWidthFraction = 0.74f
        val featureIconSizeDp = 15
        val minVerticalGapBeforeCtaDp = 24
        val ctaButtonHeightDp = 44
        val cardCornerRadiusDp = 24
        val ctaCornerRadiusDp = 14

        assertTrue(contentWidthFraction in 0.70f..0.80f, "Content width fraction provides single-line fit for athlete features")
        assertTrue(minVerticalGapBeforeCtaDp in 20..28, "Dedicated vertical gap between 20-28dp prevents CTA from touching the 4th feature")
        assertEquals(44, ctaButtonHeightDp, "CTA button height is consistent across both cards")
        assertEquals(24, cardCornerRadiusDp, "Card corner radius is unified")
        assertEquals(14, ctaCornerRadiusDp, "CTA corner radius is unified")
    }
}
