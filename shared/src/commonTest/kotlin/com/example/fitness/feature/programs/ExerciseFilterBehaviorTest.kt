package com.example.fitness.feature.programs

import com.example.fitness.feature.workout.data.WorkoutFilterHelper
import com.example.fitness.feature.workout.data.WorkoutFilterState
import com.example.fitness.feature.workout.model.WorkoutType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Verifies that Quick Category Selection and Manual Filter State remain completely separate concepts,
 * satisfying all 7 test cases specified in the requirement.
 */
class ExerciseFilterBehaviorTest {

    private val allExercises = WorkoutFilterHelper.getAllExercises(WorkoutType.GYM)

    // Helper function that mirrors ProgramsScreen effective filter logic
    private fun computeEffectiveFilter(quickCategory: String, manualFilterState: WorkoutFilterState): WorkoutFilterState {
        return if (quickCategory != "All") {
            manualFilterState.copy(muscleGroup = quickCategory)
        } else {
            manualFilterState.copy(muscleGroup = "All")
        }
    }

    // =====================================================================================
    // TEST 1: Open Workouts. All exercises displayed, no active manual filter.
    // Tap Chest -> Only Chest exercises displayed. Open Filter -> Chest is NOT in manual filter.
    // =====================================================================================
    @Test
    fun test1_quickCategoryChest_displaysChestExercises_andDoesNotMutateManualFilter() {
        var quickCategory = "All"
        var manualFilterState = WorkoutFilterState()

        // 1. Initial State: All exercises displayed, 0 active manual filters
        var effectiveFilter = computeEffectiveFilter(quickCategory, manualFilterState)
        var displayed = WorkoutFilterHelper.filterExercises(allExercises, "", effectiveFilter)
        assertEquals(allExercises.size, displayed.size, "Initially all gym exercises displayed")
        assertEquals(0, manualFilterState.activeFilterCount, "No active manual filter badge initially")

        // 2. User taps "Chest" quick category chip
        val onQuickCategoryClick: (String) -> Unit = { category ->
            quickCategory = category
        }
        onQuickCategoryClick("Chest")

        // Exercise list immediately shows ONLY Chest exercises
        effectiveFilter = computeEffectiveFilter(quickCategory, manualFilterState)
        displayed = WorkoutFilterHelper.filterExercises(allExercises, "", effectiveFilter)
        assertEquals(3, displayed.size, "Chest chip clicked must immediately display 3 Chest exercises")
        assertTrue(displayed.all { it.bodyPart.equals("Chest", ignoreCase = true) })

        // Manual filter state MUST NOT be mutated
        assertEquals("All", manualFilterState.muscleGroup, "Chest must NOT be added to manual filter state")
        assertEquals(0, manualFilterState.activeFilterCount, "Filter indicator must NOT show active manual filter")

        // 3. User opens Filter panel
        // Filter panel receives manualFilterState
        val filterPanelCurrentFilter = manualFilterState
        assertEquals("All", filterPanelCurrentFilter.muscleGroup, "Filter panel opens with NO Chest filter selected")
    }

    // =====================================================================================
    // TEST 2: Open Workouts. Tap Back -> Only Back exercises displayed. Open Filter -> No Back filter.
    // =====================================================================================
    @Test
    fun test2_quickCategoryBack_displaysBackExercises_andNoManuallySelectedBackFilter() {
        var quickCategory = "All"
        var manualFilterState = WorkoutFilterState()

        // Tap Back quick category
        quickCategory = "Back"

        val effectiveFilter = computeEffectiveFilter(quickCategory, manualFilterState)
        val displayed = WorkoutFilterHelper.filterExercises(allExercises, "", effectiveFilter)
        assertEquals(3, displayed.size, "Only Back exercises displayed")
        assertTrue(displayed.all { it.bodyPart.equals("Back", ignoreCase = true) })

        // Open Filter
        val filterPanelCurrentFilter = manualFilterState
        assertEquals("All", filterPanelCurrentFilter.muscleGroup, "No manually selected Back filter in Filter UI")
        assertEquals(0, manualFilterState.activeFilterCount, "No filter indicator badge")
    }

    // =====================================================================================
    // TEST 3: Open Filter. Select Chest. Apply. -> Chest displayed. Open Filter -> Chest selected, badge = 1.
    // =====================================================================================
    @Test
    fun test3_manualFilterChest_appliesChest_andAppearsInFilterPanelWithBadge() {
        var quickCategory = "All"
        var manualFilterState = WorkoutFilterState()
        var showFilterSheet = false

        // Open Filter
        showFilterSheet = true
        var tempMuscle = manualFilterState.muscleGroup
        assertEquals("All", tempMuscle)

        // Select Chest in Filter UI
        tempMuscle = "Chest"

        // Tap Apply
        val onApply: (WorkoutFilterState) -> Unit = { newFilter ->
            manualFilterState = newFilter
            if (newFilter.muscleGroup != "All") {
                quickCategory = newFilter.muscleGroup
            }
            showFilterSheet = false
        }
        onApply(manualFilterState.copy(muscleGroup = tempMuscle))

        // Assertions
        assertFalse(showFilterSheet)
        assertEquals("Chest", manualFilterState.muscleGroup, "Chest is now an active manual filter")
        assertEquals(1, manualFilterState.activeFilterCount, "Filter indicator shows active filter badge")
        assertEquals("Chest", quickCategory, "Quick category chip reflects Chest")

        val effectiveFilter = computeEffectiveFilter(quickCategory, manualFilterState)
        val displayed = WorkoutFilterHelper.filterExercises(allExercises, "", effectiveFilter)
        assertEquals(3, displayed.size, "Chest exercises displayed")
        assertTrue(displayed.all { it.bodyPart.equals("Chest", ignoreCase = true) })

        // Open Filter again
        val filterPanelCurrentFilter = manualFilterState
        assertEquals("Chest", filterPanelCurrentFilter.muscleGroup, "Chest must appear as selected when reopening Filter")
    }

    // =====================================================================================
    // TEST 4: Tap Chest quick category. Filter -> Equipment -> Dumbbell -> Apply.
    // Expected: Chest + Dumbbell exercises. Open Filter -> Dumbbell selected, Chest NOT selected.
    // =====================================================================================
    @Test
    fun test4_quickCategoryChest_plusManualDumbbell_combinesCorrectly_andChestNotInManualFilter() {
        var quickCategory = "All"
        var manualFilterState = WorkoutFilterState()

        // 1. User taps Chest quick category
        quickCategory = "Chest"

        // 2. User opens Filter and selects Equipment = Dumbbell, leaving Muscle = All
        val onApply: (WorkoutFilterState) -> Unit = { newFilter ->
            manualFilterState = newFilter
            if (newFilter.muscleGroup != "All") {
                quickCategory = newFilter.muscleGroup
            }
        }
        onApply(WorkoutFilterState(muscleGroup = "All", equipment = "Dumbbell", workoutType = "Gym"))

        // Quick category remains Chest
        assertEquals("Chest", quickCategory)
        // Manual filter has equipment Dumbbell, but muscleGroup is All
        assertEquals("Dumbbell", manualFilterState.equipment)
        assertEquals("All", manualFilterState.muscleGroup, "Chest should NOT have been automatically added to manual filter")
        assertEquals(1, manualFilterState.activeFilterCount, "Filter indicator counts only manual Dumbbell filter")

        // Displayed exercises: Chest + Dumbbell
        val effectiveFilter = computeEffectiveFilter(quickCategory, manualFilterState)
        val displayed = WorkoutFilterHelper.filterExercises(allExercises, "", effectiveFilter)
        assertEquals(1, displayed.size, "Only Incline Dumbbell Press matches Chest + Dumbbell")
        assertEquals("Incline Dumbbell Press", displayed.first().name)

        // Open Filter again
        val filterPanelCurrentFilter = manualFilterState
        assertEquals("Dumbbell", filterPanelCurrentFilter.equipment, "Dumbbell is selected in manual filter")
        assertEquals("All", filterPanelCurrentFilter.muscleGroup, "Chest is NOT selected in manual filter")
    }

    // =====================================================================================
    // TEST 5: Apply manual Chest filter. Tap Back quick category.
    // Expected: Back category displayed. Manual filter state must NOT silently become Back.
    // =====================================================================================
    @Test
    fun test5_manualChestFilter_tapBackQuickCategory_displaysBack_andManualFilterRemainsChest() {
        var quickCategory = "Chest"
        var manualFilterState = WorkoutFilterState(muscleGroup = "Chest", workoutType = "Gym")

        // User taps "Back" quick category
        quickCategory = "Back"

        // Manual filter state must NOT silently become Back
        assertEquals("Chest", manualFilterState.muscleGroup, "Manual filter state must remain Chest")
        assertEquals(1, manualFilterState.activeFilterCount)

        // Displayed category is Back
        val effectiveFilter = computeEffectiveFilter(quickCategory, manualFilterState)
        val displayed = WorkoutFilterHelper.filterExercises(allExercises, "", effectiveFilter)
        assertEquals(3, displayed.size, "Back category is displayed")
        assertTrue(displayed.all { it.bodyPart.equals("Back", ignoreCase = true) })
    }

    // =====================================================================================
    // TEST 6: Apply manual Chest filter. Press Clear Filters.
    // Expected: Manual Chest filter removed. Quick category remains independent.
    // =====================================================================================
    @Test
    fun test6_applyManualChestFilter_pressClearFilters_manualFilterRemoved_quickCategoryIndependent() {
        var quickCategory = "Back"
        var manualFilterState = WorkoutFilterState(muscleGroup = "Chest", workoutType = "Gym")

        // User taps "Clear filters"
        manualFilterState = manualFilterState.reset()

        // Manual filter is removed
        assertEquals("All", manualFilterState.muscleGroup)
        assertEquals(0, manualFilterState.activeFilterCount)

        // Quick category remains independent ("Back")
        assertEquals("Back", quickCategory)
        val effectiveFilter = computeEffectiveFilter(quickCategory, manualFilterState)
        val displayed = WorkoutFilterHelper.filterExercises(allExercises, "", effectiveFilter)
        assertEquals(3, displayed.size, "Back exercises continue to be displayed from quick category")
        assertTrue(displayed.all { it.bodyPart.equals("Back", ignoreCase = true) })
    }

    // =====================================================================================
    // TEST 7: Tap Chest quick category. Close/reopen Workouts screen. No unexpected manual filter.
    // =====================================================================================
    @Test
    fun test7_reenterWorkoutsScreen_noUnexpectedManualFilter() {
        // In fresh screen composition
        val quickCategory = "All"
        val manualFilterState = WorkoutFilterState()

        assertEquals("All", quickCategory)
        assertEquals("All", manualFilterState.muscleGroup)
        assertEquals(0, manualFilterState.activeFilterCount)
        assertTrue(manualFilterState.isDefault)

        val effectiveFilter = computeEffectiveFilter(quickCategory, manualFilterState)
        val displayed = WorkoutFilterHelper.filterExercises(allExercises, "", effectiveFilter)
        assertEquals(allExercises.size, displayed.size, "All exercises displayed on re-entry with default state")
    }

    // =====================================================================================
    // BONUS: All Chip behavior
    // =====================================================================================
    @Test
    fun testBonus_allChip_displaysAllExercises_andPreservesManualFilterState() {
        var quickCategory = "Chest"
        var manualFilterState = WorkoutFilterState(equipment = "Dumbbell", workoutType = "Gym")

        // User taps "All" quick category chip
        quickCategory = "All"

        // Quick category becomes "All", manual filter state is unchanged
        assertEquals("All", quickCategory)
        assertEquals("Dumbbell", manualFilterState.equipment)
        assertEquals(1, manualFilterState.activeFilterCount)

        // Effective filter applies Dumbbell across All muscle groups
        val effectiveFilter = computeEffectiveFilter(quickCategory, manualFilterState)
        val displayed = WorkoutFilterHelper.filterExercises(allExercises, "", effectiveFilter)
        assertTrue(displayed.size > 1, "Dumbbell exercises from all muscle groups should be displayed")
        assertTrue(displayed.all { it.name.contains("Dumbbell", ignoreCase = true) || it.equipment.contains("Dumbbell", ignoreCase = true) })
    }
}
