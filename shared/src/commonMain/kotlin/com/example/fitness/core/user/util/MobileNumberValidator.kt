package com.example.fitness.core.user.util

/**
 * Validates, normalizes, and formats Indian mobile numbers.
 *
 * Requirements:
 * - Indian mobile numbers consist of 10 digits starting with 6, 7, 8, or 9.
 * - Accepts user inputs with optional country code (+91 or 91), optional leading 0,
 *   spaces, hyphens, and parentheses.
 * - Normalizes valid numbers to the standard canonical storage format: "+91XXXXXXXXXX".
 * - Formats valid stored numbers for human presentation: "+91 XXXXX XXXXX".
 * - Strictly rejects letters, malformed strings, and non-conforming lengths.
 */
object MobileNumberValidator {

    /**
     * Validates and normalizes user input into canonical storage format "+91XXXXXXXXXX".
     * Returns null if the input is invalid or cannot be normalized.
     */
    fun normalize(input: String?): String? {
        if (input.isNullOrBlank()) return null

        val trimmed = input.trim()
        val clean = trimmed.replace(" ", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")

        // Must contain only digits and optional leading '+'
        if (!clean.matches(Regex("""^\+?[0-9]+$"""))) {
            return null
        }

        val digits = if (clean.startsWith("+")) clean.substring(1) else clean

        val tenDigits = when {
            digits.startsWith("91") && digits.length == 12 -> digits.substring(2)
            digits.startsWith("0") && digits.length == 11 -> digits.substring(1)
            digits.length == 10 -> digits
            else -> return null
        }

        // Indian mobile numbers must start with 6, 7, 8, or 9
        if (!tenDigits.matches(Regex("""^[6-9][0-9]{9}$"""))) {
            return null
        }

        return "+91$tenDigits"
    }

    /**
     * Returns true if the input can be successfully normalized into a valid Indian mobile number.
     */
    fun isValid(input: String?): Boolean = normalize(input) != null

    /**
     * Formats a stored canonical number (+91XXXXXXXXXX) or raw valid input into "+91 XXXXX XXXXX".
     * If blank or null, returns "Not set".
     */
    fun formatForDisplay(stored: String?): String {
        if (stored.isNullOrBlank()) return "Not set"
        val normalized = normalize(stored) ?: return stored
        val digits = normalized.removePrefix("+91")
        return if (digits.length == 10) {
            "+91 ${digits.substring(0, 5)} ${digits.substring(5)}"
        } else {
            normalized
        }
    }
}
