package com.madhuranga.clashmanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResourceBudgetPlannerTest {
    @Test
    fun reportsExactShortageForEachRequiredResource() {
        val result = ResourceBudgetPlanner.compare(
            requiredCosts = mapOf("Gold" to 1_000L, "Elixir" to 500L),
            availableBalances = mapOf("Gold" to 700L, "Elixir" to 900L)
        )!!

        val gold = result.first { it.resource == "Gold" }
        val elixir = result.first { it.resource == "Elixir" }
        assertEquals(300L, gold.shortage)
        assertFalse(gold.sufficient)
        assertEquals(0L, elixir.shortage)
        assertTrue(elixir.sufficient)
    }

    @Test
    fun refusesToGuessWhenRequiredResourceBalanceIsMissing() {
        assertNull(
            ResourceBudgetPlanner.compare(
                requiredCosts = mapOf("Gold" to 1_000L),
                availableBalances = emptyMap()
            )
        )
    }

    @Test
    fun rejectsNegativeAmounts() {
        assertNull(
            ResourceBudgetPlanner.compare(
                requiredCosts = mapOf("Gold" to -1L),
                availableBalances = mapOf("Gold" to 100L)
            )
        )
    }

    @Test
    fun reportsNoShortageWhenBalanceMatchesRequirement() {
        val status = ResourceBudgetPlanner.compare(
            requiredCosts = mapOf("Gold" to 1_000L),
            availableBalances = mapOf("Gold" to 1_000L)
        )!!.single()

        assertEquals(0L, status.shortage)
        assertTrue(status.sufficient)
    }
}
