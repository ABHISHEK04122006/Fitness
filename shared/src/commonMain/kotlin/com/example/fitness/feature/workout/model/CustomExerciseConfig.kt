package com.example.fitness.feature.workout.model

import kotlinx.serialization.Serializable

/**
 * Encapsulates the user-customized target configuration for an individual exercise
 * within an active workout session.
 *
 * Adheres strictly to the ATHGYM architecture:
 * - Master exercise and canonical program data remain immutable.
 * - Initial state reflects the canonical recommended values.
 * - Customization is isolated per exercise and per session.
 * - Single source of truth for UI display, Set Tracker, Rest Timer, and Workout History persistence.
 */
@Serializable
data class CustomExerciseConfig(
    val exerciseId: String,
    val sets: Int,
    val isDurationBased: Boolean,
    val repsCount: Int? = null,
    val durationSeconds: Int? = null,
    val rawRepsText: String = "",
    val restSeconds: Int = 60,
    val defaultSets: Int = 3,
    val defaultRepsText: String = "",
    val defaultRestSeconds: Int = 60,
    val isCustomized: Boolean = false
) {
    val displaySets: String get() = "$sets Sets"

    val displayReps: String
        get() = if (isCustomized) {
            if (isDurationBased) {
                "${durationSeconds ?: 30} sec"
            } else {
                "${repsCount ?: 10} reps"
            }
        } else {
            defaultRepsText
        }

    val displayRest: String get() = "${restSeconds}s"

    val activeRepsOrDurationCount: Int
        get() = if (isDurationBased) {
            durationSeconds ?: parseDefaultRepsOrDuration(rawRepsText, true)
        } else {
            repsCount ?: parseDefaultRepsOrDuration(rawRepsText, false)
        }

    companion object {
        fun isExerciseDurationBased(repsText: String): Boolean {
            val lower = repsText.lowercase().trim()
            return lower.contains("sec") || lower.contains("second") || lower.contains("min") ||
                    (lower.endsWith("s") && lower.dropLast(1).all { it.isDigit() })
        }

        fun parseDefaultSets(setsText: String): Int {
            val trimmed = setsText.trim()
            if (trimmed.startsWith("-")) return 1
            val num = trimmed.filter { it.isDigit() }.toIntOrNull() ?: 3
            return num.coerceIn(1, 20)
        }

        fun parseDefaultRest(restText: String): Int {
            val trimmed = restText.trim()
            if (trimmed.isBlank()) return 60
            if (trimmed.startsWith("-")) return 5
            // Directly sanitize 6090 concatenation bug
            if (trimmed.startsWith("6090")) return 60
            // Handle minute specifications e.g. "2 min", "2-3 min"
            if (trimmed.contains("min", ignoreCase = true)) {
                val match = Regex("""(\d+)""").find(trimmed)
                val mins = match?.value?.toIntOrNull()
                if (mins != null && mins in 1..10) return (mins * 60).coerceIn(5, 300)
            }
            // Match the first distinct integer - NEVER concatenate across ranges like "60-90"
            val match = Regex("""(\d+)""").find(trimmed)
            val num = match?.value?.toIntOrNull() ?: 60
            if (num == 6090) return 60
            return num.coerceIn(5, 300)
        }

        fun parseDefaultRepsOrDuration(repsText: String, isDuration: Boolean): Int {
            val trimmed = repsText.trim()
            if (trimmed.startsWith("-")) return if (isDuration) 5 else 1
            val numbers = Regex("""\d+""").findAll(repsText).mapNotNull { it.value.toIntOrNull() }.toList()
            return if (isDuration) {
                (numbers.firstOrNull() ?: 30).coerceIn(5, 600)
            } else {
                (numbers.lastOrNull() ?: 10).coerceIn(1, 100)
            }
        }

        fun fromPlanExercise(ex: PlanExercise): CustomExerciseConfig {
            val isDuration = isExerciseDurationBased(ex.reps)
            val defSets = parseDefaultSets(ex.sets)
            val defRest = parseDefaultRest(ex.rest)
            val defRepOrDur = parseDefaultRepsOrDuration(ex.reps, isDuration)

            return CustomExerciseConfig(
                exerciseId = ex.id,
                sets = defSets,
                isDurationBased = isDuration,
                repsCount = if (!isDuration) defRepOrDur else null,
                durationSeconds = if (isDuration) defRepOrDur else null,
                rawRepsText = ex.reps,
                restSeconds = defRest,
                defaultSets = defSets,
                defaultRepsText = ex.reps,
                defaultRestSeconds = defRest,
                isCustomized = false
            )
        }
    }
}
