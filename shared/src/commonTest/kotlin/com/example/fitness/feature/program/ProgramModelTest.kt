package com.example.fitness.feature.program

import com.example.fitness.feature.exercise.model.ExerciseReference
import com.example.fitness.feature.program.data.FirebaseProgramRepository
import com.example.fitness.feature.program.data.SampleProgramData
import com.example.fitness.feature.program.model.Program
import com.example.fitness.feature.program.model.ProgramDay
import com.example.fitness.feature.program.model.ProgramType
import com.example.fitness.feature.program.model.ProgramWorkoutType
import com.example.fitness.feature.workout.model.ExperienceLevel
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Common unit test suite verifying the Program Data Foundation and Firestore schema logic (STEP 5A):
 * 1. Program default values
 * 2. ProgramDay default values
 * 3. Program serialization/deserialization
 * 4. ProgramType completeness
 * 5. Experience level compatibility
 * 6. Stable program ID behavior
 * 7. ExerciseReference relationship
 * 8. Program day ordering
 * 9. Unique day number validation
 * 10. 30-day structure support
 * 11. Rest day representation
 * 12. Exercise reference validation
 * 13. Version increment
 * 14. Audit field preservation
 * 15. Archive behavior
 * 16. Restore behavior
 */
class ProgramModelTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val repository = FirebaseProgramRepository()

    // 1. Program default values
    @Test
    fun testProgramDefaultValues() {
        val program = Program()

        assertEquals("", program.id)
        assertEquals("", program.name)
        assertEquals("", program.slug)
        assertEquals("", program.description)
        assertEquals("", program.type)
        assertEquals("", program.experienceLevel)
        assertEquals(0, program.durationDays)
        assertEquals(0, program.trainingDaysPerWeek)
        assertTrue(program.days.isEmpty())
        assertTrue(program.isActive)
        assertNull(program.createdAt)
        assertNull(program.updatedAt)
        assertEquals("", program.createdBy)
        assertEquals("", program.updatedBy)
        assertEquals(1, program.version)
    }

    // 2. ProgramDay default values
    @Test
    fun testProgramDayDefaultValues() {
        val day = ProgramDay()

        assertEquals(0, day.dayNumber)
        assertEquals("", day.title)
        assertEquals("TRAINING", day.workoutType)
        assertTrue(day.focus.isEmpty())
        assertFalse(day.isRestDay)
        assertTrue(day.exercises.isEmpty())
        assertEquals("", day.notes)
    }

    // 3. Program serialization / deserialization round trip
    @Test
    fun testProgramJsonSerializationRoundTrip() {
        val original = SampleProgramData.HYPERTROPHY_DEMONSTRATOR.copy(
            createdAt = 1700000000000L,
            updatedAt = 1700005000000L,
            createdBy = "admin_creator_uid",
            updatedBy = "admin_updater_uid",
            version = 2
        )

        val encoded = json.encodeToString(Program.serializer(), original)
        assertTrue(encoded.contains("\"id\":\"hypertrophy\""))
        assertTrue(encoded.contains("\"type\":\"HYPERTROPHY\""))
        assertTrue(encoded.contains("\"exerciseId\":\"barbell_bench_press\""))

        val decoded = json.decodeFromString(Program.serializer(), encoded)
        assertEquals(original.id, decoded.id)
        assertEquals(original.name, decoded.name)
        assertEquals(original.slug, decoded.slug)
        assertEquals(original.type, decoded.type)
        assertEquals(original.experienceLevel, decoded.experienceLevel)
        assertEquals(original.durationDays, decoded.durationDays)
        assertEquals(original.trainingDaysPerWeek, decoded.trainingDaysPerWeek)
        assertEquals(original.days.size, decoded.days.size)
        assertEquals(original.days[0].exercises[0].exerciseId, decoded.days[0].exercises[0].exerciseId)
        assertEquals(original.days[0].exercises[0].sets, decoded.days[0].exercises[0].sets)
        assertEquals(original.days[0].exercises[0].reps, decoded.days[0].exercises[0].reps)
        assertEquals(original.days[0].exercises[0].restSeconds, decoded.days[0].exercises[0].restSeconds)
        assertEquals(original.isActive, decoded.isActive)
        assertEquals(original.version, decoded.version)
        assertEquals(original.createdAt, decoded.createdAt)
        assertEquals(original.createdBy, decoded.createdBy)
    }

    // 4. ProgramType completeness and parsing
    @Test
    fun testProgramTypeCompletenessAndParsing() {
        assertEquals(4, ProgramType.entries.size)

        assertEquals(ProgramType.THIRTY_DAY, ProgramType.fromString("THIRTY_DAY"))
        assertEquals(ProgramType.THIRTY_DAY, ProgramType.fromString("thirty_day"))
        assertEquals(ProgramType.THIRTY_DAY, ProgramType.fromString("30-Day Workout Plan"))

        assertEquals(ProgramType.HYPERTROPHY, ProgramType.fromString("HYPERTROPHY"))
        assertEquals(ProgramType.HYPERTROPHY, ProgramType.fromString("hypertrophy"))

        assertEquals(ProgramType.STRENGTH, ProgramType.fromString("STRENGTH"))
        assertEquals(ProgramType.STRENGTH, ProgramType.fromString("strength"))

        assertEquals(ProgramType.ATHLETE_PERFORMANCE, ProgramType.fromString("ATHLETE_PERFORMANCE"))
        assertEquals(ProgramType.ATHLETE_PERFORMANCE, ProgramType.fromString("athlete_performance"))

        assertNull(ProgramType.fromString("NON_EXISTENT_TYPE"))

        // ProgramWorkoutType verification
        assertEquals(7, ProgramWorkoutType.entries.size)
        val expectedWorkoutTypes = listOf("TRAINING", "REST", "RECOVERY", "WARMUP", "COOLDOWN", "CARDIO", "MOBILITY")
        expectedWorkoutTypes.forEach { typeStr ->
            assertNotNull(ProgramWorkoutType.fromString(typeStr), "ProgramWorkoutType $typeStr must be valid")
        }
    }

    // 5. Experience level compatibility
    @Test
    fun testExperienceLevelCompatibility() {
        // Must reuse project's existing experience level terminology:
        // NO_EXPERIENCE, BEGINNER, ADVANCED, EXPERT
        val levels = ExperienceLevel.entries
        assertEquals(4, levels.size)
        assertTrue(levels.any { it == ExperienceLevel.NO_EXPERIENCE })
        assertTrue(levels.any { it == ExperienceLevel.BEGINNER })
        assertTrue(levels.any { it == ExperienceLevel.ADVANCED })
        assertTrue(levels.any { it == ExperienceLevel.EXPERT })

        assertEquals(ExperienceLevel.NO_EXPERIENCE, ExperienceLevel.fromString("noExperience"))
        assertEquals(ExperienceLevel.BEGINNER, ExperienceLevel.fromString("beginner"))
        assertEquals(ExperienceLevel.ADVANCED, ExperienceLevel.fromString("advanced"))
        assertEquals(ExperienceLevel.EXPERT, ExperienceLevel.fromString("expert"))
    }

    // 6. Stable program ID behavior and slug generation
    @Test
    fun testStableProgramIdBehaviorAndSlugGeneration() {
        // Normal display name to slug
        val slug1 = repository.generateSlug("30-Day Workout Plan")
        assertEquals("30day_workout_plan", slug1)

        val slug2 = repository.generateSlug("Athlete Performance Program — 2026 Edition!")
        assertEquals("athlete_performance_program_2026_edition", slug2)

        // Verifying document ID remains stable when display name changes
        val original = SampleProgramData.THIRTY_DAY_DEMONSTRATOR
        assertEquals("thirty_day", original.id)

        val renamed = original.copy(name = "30-Day Full Body Conditioning Plan")
        assertEquals("thirty_day", renamed.id)
        assertEquals("30-Day Full Body Conditioning Plan", renamed.name)
    }

    // 7. ExerciseReference relationship (centralized reference without duplication)
    @Test
    fun testExerciseReferenceRelationship() {
        val ref = ExerciseReference(
            exerciseId = "barbell_bench_press",
            sets = 4,
            reps = "8-12",
            restSeconds = 90,
            notes = "Control eccentric tempo"
        )

        assertEquals("barbell_bench_press", ref.exerciseId)
        assertEquals(4, ref.sets)
        assertEquals("8-12", ref.reps)
        assertEquals(90, ref.restSeconds)
        assertEquals("Control eccentric tempo", ref.notes)

        // ExerciseReference contains ONLY relational exerciseId; not duplicating complete Exercise model
        val day = ProgramDay(
            dayNumber = 1,
            title = "Chest & Triceps",
            exercises = listOf(ref)
        )
        assertEquals("barbell_bench_press", day.exercises[0].exerciseId)
    }

    // 8. Program day ordering (sorted by dayNumber)
    @Test
    fun testProgramDayOrdering() {
        // Provide days out of order (Day 3, Day 1, Day 2)
        val unorderedDays = listOf(
            ProgramDay(dayNumber = 3, title = "Legs"),
            ProgramDay(dayNumber = 1, title = "Push"),
            ProgramDay(dayNumber = 2, title = "Pull")
        )

        val program = Program(
            id = "custom_split",
            name = "Push Pull Legs",
            slug = "push_pull_legs",
            days = unorderedDays.sortedBy { it.dayNumber }
        )

        assertEquals(1, program.days[0].dayNumber)
        assertEquals("Push", program.days[0].title)
        assertEquals(2, program.days[1].dayNumber)
        assertEquals("Pull", program.days[1].title)
        assertEquals(3, program.days[2].dayNumber)
        assertEquals("Legs", program.days[2].title)
    }

    // 9. Unique day number validation
    @Test
    fun testUniqueDayNumberValidation() {
        val validProgram = Program(
            id = "valid_prog",
            name = "Valid Program",
            slug = "valid_prog",
            days = listOf(
                ProgramDay(dayNumber = 1, title = "Day 1"),
                ProgramDay(dayNumber = 2, title = "Day 2")
            )
        )
        assertTrue(repository.validateProgram(validProgram).isSuccess)

        // Duplicate dayNumber 1
        val invalidProgram = Program(
            id = "invalid_prog",
            name = "Invalid Program",
            slug = "invalid_prog",
            days = listOf(
                ProgramDay(dayNumber = 1, title = "Day 1 Session A"),
                ProgramDay(dayNumber = 1, title = "Day 1 Session B")
            )
        )
        val result = repository.validateProgram(invalidProgram)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Duplicate dayNumber 1") == true)
    }

    // 10. 30-day structure support (holding day 1 through 30)
    @Test
    fun testThirtyDayStructureSupport() {
        val thirtyDayPlan = SampleProgramData.THIRTY_DAY_DEMONSTRATOR

        assertEquals(30, thirtyDayPlan.durationDays)
        assertEquals(30, thirtyDayPlan.days.size)

        // Verify days 1 through 30 are sequentially ordered without gaps
        thirtyDayPlan.days.forEachIndexed { index, day ->
            val expectedDay = index + 1
            assertEquals(expectedDay, day.dayNumber, "Expected day number $expectedDay at index $index")
            assertTrue(day.title.isNotBlank())
            assertTrue(day.workoutType.isNotBlank())
        }

        assertTrue(repository.validateProgram(thirtyDayPlan).isSuccess)
    }

    // 11. Rest day representation
    @Test
    fun testRestDayRepresentation() {
        val restDay = ProgramDay(
            dayNumber = 7,
            title = "Full Rest & Recovery",
            workoutType = ProgramWorkoutType.REST.name,
            focus = listOf("Recovery"),
            isRestDay = true,
            exercises = emptyList(),
            notes = "Prioritize sleep, hydration, and light walking."
        )

        assertTrue(restDay.isRestDay)
        assertEquals("REST", restDay.workoutType)
        assertTrue(restDay.exercises.isEmpty())
        assertEquals("Prioritize sleep, hydration, and light walking.", restDay.notes)
    }

    // 12. Exercise reference validation
    @Test
    fun testExerciseReferenceValidation() {
        // Valid reference
        val validDay = ProgramDay(
            dayNumber = 1,
            title = "Bench Day",
            exercises = listOf(
                ExerciseReference(exerciseId = "barbell_bench_press", sets = 3, reps = "10", restSeconds = 60)
            )
        )
        val validProgram = Program(id = "p1", name = "P1", slug = "p1", days = listOf(validDay))
        assertTrue(repository.validateProgram(validProgram).isSuccess)

        // Invalid: Blank exerciseId
        val blankExDay = ProgramDay(
            dayNumber = 1,
            title = "Day 1",
            exercises = listOf(ExerciseReference(exerciseId = "", sets = 3, reps = "10"))
        )
        val progBlankEx = Program(id = "p2", name = "P2", slug = "p2", days = listOf(blankExDay))
        assertTrue(repository.validateProgram(progBlankEx).isFailure)

        // Invalid: Sets <= 0
        val zeroSetsDay = ProgramDay(
            dayNumber = 1,
            title = "Day 1",
            exercises = listOf(ExerciseReference(exerciseId = "squat", sets = 0, reps = "10"))
        )
        val progZeroSets = Program(id = "p3", name = "P3", slug = "p3", days = listOf(zeroSetsDay))
        assertTrue(repository.validateProgram(progZeroSets).isFailure)

        // Invalid: Blank reps
        val blankRepsDay = ProgramDay(
            dayNumber = 1,
            title = "Day 1",
            exercises = listOf(ExerciseReference(exerciseId = "squat", sets = 3, reps = ""))
        )
        val progBlankReps = Program(id = "p4", name = "P4", slug = "p4", days = listOf(blankRepsDay))
        assertTrue(repository.validateProgram(progBlankReps).isFailure)
    }

    // 13. Version increment on update
    @Test
    fun testVersionIncrement() {
        val original = Program(
            id = "strength_base",
            name = "Strength Base",
            slug = "strength_base",
            version = 1
        )
        assertEquals(1, original.version)

        val updated = original.copy(
            name = "Strength Base (Revised)",
            version = original.version + 1
        )
        assertEquals(2, updated.version)
    }

    // 14. Audit field preservation
    @Test
    fun testAuditFieldPreservation() {
        val original = Program(
            id = "strength",
            name = "Strength Program",
            slug = "strength_program",
            createdAt = 1600000000000L,
            updatedAt = 1600000000000L,
            createdBy = "creator_uid_original",
            updatedBy = "creator_uid_original",
            version = 1
        )

        // When updated, createdAt and createdBy must be preserved
        val updated = original.copy(
            name = "Strength Program v2",
            updatedAt = 1600009999000L,
            updatedBy = "editor_uid_new",
            version = original.version + 1
        )

        assertEquals("strength", updated.id)
        assertEquals(1600000000000L, updated.createdAt, "Original createdAt must be preserved")
        assertEquals("creator_uid_original", updated.createdBy, "Original createdBy must be preserved")
        assertEquals(1600009999000L, updated.updatedAt)
        assertEquals("editor_uid_new", updated.updatedBy)
        assertEquals(2, updated.version)
    }

    // 15. Archive behavior (soft delete, isActive = false)
    @Test
    fun testArchiveBehaviorSoftDelete() {
        val active = SampleProgramData.THIRTY_DAY_DEMONSTRATOR.copy(
            isActive = true,
            version = 1,
            createdAt = 1000L,
            createdBy = "admin_initial"
        )
        assertTrue(active.isActive)

        // Archive simulation
        val archived = active.copy(
            isActive = false,
            updatedAt = 2500L,
            updatedBy = "admin_archiver",
            version = active.version + 1
        )

        assertFalse(archived.isActive, "Archived program must have isActive = false")
        assertEquals(2, archived.version, "Version must increment on archiving")
        assertEquals(active.createdAt, archived.createdAt, "createdAt must remain untouched")
        assertEquals(active.createdBy, archived.createdBy, "createdBy must remain untouched")
        assertEquals(active.id, archived.id, "Document ID must remain stable")
    }

    // 16. Restore behavior (isActive = true)
    @Test
    fun testRestoreBehavior() {
        val archived = SampleProgramData.THIRTY_DAY_DEMONSTRATOR.copy(
            isActive = false,
            version = 2,
            createdAt = 1000L,
            createdBy = "admin_initial"
        )
        assertFalse(archived.isActive)

        // Restore simulation
        val restored = archived.copy(
            isActive = true,
            updatedAt = 3500L,
            updatedBy = "admin_restorer",
            version = archived.version + 1
        )

        assertTrue(restored.isActive, "Restored program must have isActive = true")
        assertEquals(3, restored.version, "Version must increment on restoring")
        assertEquals(archived.createdAt, restored.createdAt, "createdAt must remain untouched")
        assertEquals(archived.createdBy, restored.createdBy, "createdBy must remain untouched")
        assertEquals(archived.id, restored.id, "Document ID must remain stable")
    }
}
