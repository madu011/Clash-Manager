package com.madhuranga.clashmanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AttackAnalysisValidatorTest {
    @Test
    fun validScenarioHasNoErrors() {
        val errors = AttackAnalysisValidator.validate(
            playerTag = "#ABC123",
            ownTownHallLevel = 15,
            targetTownHallLevel = 16,
            starGoal = 3,
            hasScreenshot = true,
            notes = "Eagle artillery near centre"
        )
        assertTrue(errors.isEmpty())
    }

    @Test
    fun trimsAndNormalizesPlayerTag() {
        val errors = AttackAnalysisValidator.validate(
            playerTag = "  #abc123  ",
            ownTownHallLevel = 10,
            targetTownHallLevel = 10,
            starGoal = 2,
            hasScreenshot = true,
            notes = ""
        )
        assertTrue(errors.isEmpty())
    }

    @Test
    fun rejectsMissingTagAndScreenshot() {
        val errors = AttackAnalysisValidator.validate(
            playerTag = "",
            ownTownHallLevel = 10,
            targetTownHallLevel = 10,
            starGoal = 1,
            hasScreenshot = false,
            notes = ""
        )
        assertEquals(2, errors.size)
    }

    @Test
    fun rejectsTownHallAndStarValuesOutsideSupportedRange() {
        val errors = AttackAnalysisValidator.validate(
            playerTag = "#ABC123",
            ownTownHallLevel = 0,
            targetTownHallLevel = 19,
            starGoal = 4,
            hasScreenshot = true,
            notes = ""
        )
        assertEquals(3, errors.size)
    }

    @Test
    fun rejectsNotesLongerThanLimit() {
        val errors = AttackAnalysisValidator.validate(
            playerTag = "#ABC123",
            ownTownHallLevel = 10,
            targetTownHallLevel = 10,
            starGoal = 3,
            hasScreenshot = true,
            notes = "x".repeat(1501)
        )
        assertEquals(1, errors.size)
        assertEquals("Base notes must be 1,500 characters or fewer.", errors.single())
    }
}
