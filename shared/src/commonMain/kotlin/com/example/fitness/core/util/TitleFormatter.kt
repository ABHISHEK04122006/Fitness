package com.example.fitness.core.util

/**
 * Utility for formatting and presenting user-facing workout and exercise titles.
 * Strips internal rotation identifiers (e.g. "Rotation A", "(Rotation B)") from visible UI
 * while preserving internal scheduling and rotation logic intact.
 */
object TitleFormatter {

    private val rotationParensRegex = Regex("""\s*\(Rotation\s+[A-Za-z0-9]+\)""", RegexOption.IGNORE_CASE)
    private val rotationWordRegex = Regex("""\s+Rotation\s+[A-Za-z0-9]+""", RegexOption.IGNORE_CASE)

    /**
     * Strips rotation labels such as "(Rotation A)", "(Rotation B)", or "Rotation A"
     * from user-visible titles and descriptions.
     * Leaves other terms containing "rotat" (e.g. "Thoracic Spine Rotations") untouched.
     */
    fun stripRotation(text: String?): String {
        if (text.isNullOrBlank()) return ""
        return text
            .replace(rotationParensRegex, "")
            .replace(rotationWordRegex, "")
            .trim()
    }
}
