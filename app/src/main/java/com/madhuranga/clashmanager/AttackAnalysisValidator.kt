package com.madhuranga.clashmanager

/**
 * Validates user-provided attack inputs before they can be sent to an analysis provider.
 * This performs input checks only; it does not infer missing game data.
 */
object AttackAnalysisValidator {
    fun validate(
        playerTag: String,
        ownTownHallLevel: Int?,
        targetTownHallLevel: Int?,
        starGoal: Int,
        hasScreenshot: Boolean,
        notes: String
    ): List<String> {
        val errors = mutableListOf<String>()
        val normalizedTag = playerTag.trim().uppercase()

        if (!normalizedTag.matches(Regex("^#[A-Z0-9]{3,15}$"))) {
            errors += "Enter a valid Player Tag (for example, #ABC123)."
        }
        if (ownTownHallLevel !in 1..18) {
            errors += "Your Town Hall level must be between 1 and 18."
        }
        if (targetTownHallLevel !in 1..18) {
            errors += "Target Town Hall level must be between 1 and 18."
        }
        if (starGoal !in 1..3) {
            errors += "Choose an attack goal of 1, 2 or 3 stars."
        }
        if (!hasScreenshot) {
            errors += "Attach a screenshot of the target base."
        }
        if (notes.length > 1500) {
            errors += "Base notes must be 1,500 characters or fewer."
        }
        return errors
    }
}
