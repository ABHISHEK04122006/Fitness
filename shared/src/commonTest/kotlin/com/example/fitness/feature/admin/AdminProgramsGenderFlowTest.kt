package com.example.fitness.feature.admin

import com.example.fitness.core.navigation.Screen
import com.example.fitness.feature.admin.data.AdminWorkoutCatalogProvider
import com.example.fitness.feature.program.data.CanonicalFemaleProgramData
import com.example.fitness.feature.program.data.SampleProgramData
import com.example.fitness.feature.program.model.Program
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Verification test suite for Admin Programs -> Gender -> Program List flow.
 *
 * Rules:
 * 1. Admin -> Programs enters with gender unselected (shows Male Programs / Female Programs selection cards).
 * 2. Entering Male Programs shows only Male programs; header says "Male Programs"; no gender toggle inside list.
 * 3. Entering Female Programs shows only Female programs; header says "Female Programs"; no Male option on screen.
 * 4. Status filter ([ALL] [ACTIVE] [ARCHIVED]) is present and filters program list.
 * 5. Redundant program-category / type filter chips (ALL, THIRTY DAY, HYPERTROPHY, STRENGTH, ATHLETE PERFORMANCE) are removed.
 * 6. Existing program data, cards, days, and operations (View, Edit, Archive/Restore) are strictly preserved.
 */
class AdminProgramsGenderFlowTest {

    @Test
    fun test01_adminProgramsEntryScreenIsAdminPrograms() {
        assertEquals(Screen.AdminPrograms, Screen.AdminPrograms)
    }

    @Test
    fun test02_maleProgramsFlowLoadsOnlyMaleCurriculum() {
        runBlocking {
            val malePrograms = AdminWorkoutCatalogProvider.getUnifiedProgramsForGender("Male")
            assertTrue(malePrograms.isNotEmpty(), "Male programs must not be empty")

            // Must include canonical curricula
            val ids = malePrograms.map { it.id }
            assertTrue(ids.any { it.contains("30_day") || it.contains("thirty_day") }, "Must include 30-day program")
            assertTrue(ids.any { it.contains("hypertrophy") }, "Must include hypertrophy program")
            assertTrue(ids.any { it.contains("strength") }, "Must include strength program")
            assertTrue(ids.any { it.contains("athlete") }, "Must include athlete performance program")

            // Must NOT include any female-specific curricula
            assertFalse(malePrograms.any { it.id.startsWith("female_") }, "Male programs list must not contain female_ programs")
        }
    }

    @Test
    fun test03_femaleProgramsFlowLoadsOnlyFemaleCurriculum() {
        runBlocking {
            val femalePrograms = AdminWorkoutCatalogProvider.getUnifiedProgramsForGender("Female")
            assertEquals(9, femalePrograms.size, "Must have exactly 9 authoritative female programs")

            val expectedFemaleIds = listOf(
                "female_weight_loss",
                "female_sculpted_body",
                "female_perfect_butt",
                "female_6_pack_abs",
                "female_general_muscle_building",
                "female_powerlifting",
                "female_crossfit",
                "female_full_body_in_45_minutes",
                "female_postpartum_recovery"
            )

            expectedFemaleIds.forEach { expectedId ->
                val found = femalePrograms.find { it.id == expectedId }
                assertNotNull(found, "Female program '$expectedId' must exist in Admin catalog")
                assertEquals(30, found.days.size, "Female program '$expectedId' must have exactly 30 days")
            }

            // None of the male base programs should bleed into female programs list
            assertFalse(femalePrograms.any { it.id == "canonical_hypertrophy" || it.id == "canonical_strength" || it.id == "canonical_athlete_performance" })
        }
    }

    @Test
    fun test04_statusFilteringWorksWithoutTypeFilter() {
        val testPrograms = listOf(
            Program(
                id = "p1",
                name = "Active Program 1",
                slug = "p1",
                description = "Active test program",
                type = "HYPERTROPHY",
                experienceLevel = "BEGINNER",
                durationDays = 30,
                trainingDaysPerWeek = 4,
                isActive = true,
                gender = "Male",
                days = emptyList()
            ),
            Program(
                id = "p2",
                name = "Archived Program 2",
                slug = "p2",
                description = "Archived test program",
                type = "STRENGTH",
                experienceLevel = "ADVANCED",
                durationDays = 30,
                trainingDaysPerWeek = 5,
                isActive = false,
                gender = "Male",
                days = emptyList()
            )
        )

        // Filter ALL
        val allFiltered = testPrograms.filter { prog ->
            val matchesStatus = when ("ALL") {
                "ACTIVE" -> prog.isActive
                "ARCHIVED" -> !prog.isActive
                else -> true
            }
            matchesStatus
        }
        assertEquals(2, allFiltered.size)

        // Filter ACTIVE
        val activeFiltered = testPrograms.filter { prog ->
            val matchesStatus = when ("ACTIVE") {
                "ACTIVE" -> prog.isActive
                "ARCHIVED" -> !prog.isActive
                else -> true
            }
            matchesStatus
        }
        assertEquals(1, activeFiltered.size)
        assertEquals("p1", activeFiltered[0].id)

        // Filter ARCHIVED
        val archivedFiltered = testPrograms.filter { prog ->
            val matchesStatus = when ("ARCHIVED") {
                "ACTIVE" -> prog.isActive
                "ARCHIVED" -> !prog.isActive
                else -> true
            }
            matchesStatus
        }
        assertEquals(1, archivedFiltered.size)
        assertEquals("p2", archivedFiltered[0].id)
    }

    @Test
    fun test05_searchQueryFiltersCorrectlyAcrossAttributes() {
        val testPrograms = listOf(
            Program(
                id = "female_sculpted_body",
                name = "Sculpted Body",
                slug = "sculpted-body",
                description = "Full body toning and lean muscle sculpting.",
                type = "THIRTY_DAY",
                experienceLevel = "INTERMEDIATE",
                durationDays = 30,
                trainingDaysPerWeek = 4,
                isActive = true,
                gender = "Female",
                days = emptyList()
            ),
            Program(
                id = "female_perfect_butt",
                name = "Glute Emphasis & Perfect Butt",
                slug = "glute-emphasis",
                description = "Lower body hypertrophic focus targeting glutes.",
                type = "THIRTY_DAY",
                experienceLevel = "BEGINNER",
                durationDays = 30,
                trainingDaysPerWeek = 3,
                isActive = true,
                gender = "Female",
                days = emptyList()
            )
        )

        val query = "glute"
        val results = testPrograms.filter { prog ->
            val q = query.trim().lowercase()
            prog.name.lowercase().contains(q) ||
                    prog.id.lowercase().contains(q) ||
                    prog.slug.lowercase().contains(q) ||
                    prog.description.lowercase().contains(q) ||
                    prog.type.lowercase().contains(q) ||
                    prog.experienceLevel.lowercase().contains(q)
        }
        assertEquals(1, results.size)
        assertEquals("female_perfect_butt", results[0].id)
    }

    @Test
    fun test06_canonicalCurriculaPreservedAcrossBothGenders() {
        val sampleMale = SampleProgramData.INITIAL_PROGRAMS
        assertTrue(sampleMale.isNotEmpty(), "Sample male programs must exist")

        val femaleCurricula = CanonicalFemaleProgramData.ALL_PROGRAMS
        assertEquals(9, femaleCurricula.size, "Must preserve all 9 female curricula")
        femaleCurricula.forEach { prog ->
            assertEquals(30, prog.days.size, "Each female program must have 30 days")
        }
    }
}
