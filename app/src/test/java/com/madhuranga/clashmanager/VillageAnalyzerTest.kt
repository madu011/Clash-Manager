package com.madhuranga.clashmanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VillageAnalyzerTest {
    @Test
    fun prioritizesAvailableBuildersAndRushRecovery() {
        val result = VillageAnalyzer.analyze(
            VillageSnapshot(
                playerTag = "#ABC",
                townHallLevel = 12,
                availableBuilders = 2,
                rushedBuildings = listOf("Archer Tower", "Cannon")
            )
        )

        assertEquals("Keep builders active", result.first().title)
        assertTrue(result.any { it.title == "Upgrade Archer Tower" })
        assertTrue(result.any { it.title == "Upgrade Cannon" })
    }

    @Test
    fun recommendsOnlyExplicitHeroAndTroopTargets() {
        val result = VillageAnalyzer.analyze(
            VillageSnapshot(
                playerTag = "#ABC",
                townHallLevel = 10,
                heroLevels = mapOf("Barbarian King" to 40, "Archer Queen" to 45),
                heroTargetLevels = mapOf("Barbarian King" to 50),
                troopLevels = mapOf("Dragon" to 5),
                troopTargetLevels = mapOf("Dragon" to 7)
            )
        )

        assertTrue(result.any { it.title == "Plan Barbarian King upgrade" })
        assertTrue(result.none { it.title == "Plan Archer Queen upgrade" })
        assertTrue(result.any { it.title == "Plan Dragon research" })
    }

    @Test
    fun returnsRecommendationsInPriorityOrder() {
        val result = VillageAnalyzer.analyze(
            VillageSnapshot(
                playerTag = "#ABC",
                townHallLevel = 11,
                availableBuilders = 1
            )
        )

        assertEquals(result.map { it.priority }.sortedDescending(), result.map { it.priority })
    }

    @Test
    fun alwaysIncludesResourceBalanceReminder() {
        val result = VillageAnalyzer.analyze(
            VillageSnapshot(playerTag = "#ABC", townHallLevel = 8)
        )

        assertTrue(result.any { it.title == "Balance resource spending" })
    }
    @Test
    fun recommendsBuildingsOnlyWhenBelowExplicitTarget() {
        val result = VillageAnalyzer.analyze(
            VillageSnapshot(
                playerTag = "#ABC",
                townHallLevel = 12,
                buildingLevels = mapOf("Archer Tower" to 10, "Cannon" to 15),
                buildingTargetLevels = mapOf("Archer Tower" to 15, "Cannon" to 15)
            )
        )

        assertTrue(result.any { it.title == "Plan Archer Tower upgrade" })
        assertTrue(result.none { it.title == "Plan Cannon upgrade" })
    }

}