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

    @Test
    fun largerRemainingLevelGapGetsHigherPriorityWithinSameCategory() {
        val result = VillageAnalyzer.analyze(
            VillageSnapshot(
                playerTag = "#ABC",
                townHallLevel = 12,
                buildingLevels = mapOf("Archer Tower" to 10, "Cannon" to 14),
                buildingTargetLevels = mapOf("Archer Tower" to 18, "Cannon" to 15)
            )
        )
        val archerTower = result.first { it.title == "Plan Archer Tower upgrade" }
        val cannon = result.first { it.title == "Plan Cannon upgrade" }

        assertTrue(archerTower.priority > cannon.priority)
        assertTrue(archerTower.reason.contains("8 level(s) remaining"))
        assertTrue(cannon.reason.contains("1 level(s) remaining"))
    }

    @Test
    fun priorityBonusIsCappedForVeryLargeManualTargets() {
        val result = VillageAnalyzer.analyze(
            VillageSnapshot(
                playerTag = "#ABC",
                townHallLevel = 12,
                buildingLevels = mapOf("Archer Tower" to 1),
                buildingTargetLevels = mapOf("Archer Tower" to 100)
            )
        )

        assertEquals(86, result.first { it.title == "Plan Archer Tower upgrade" }.priority)
    }
}
