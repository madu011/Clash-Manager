package com.madhuranga.clashmanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RushRecoveryPlannerTest {
    @Test
    fun ranksKnownBuildingGapsByRecoveryPriority() {
        val tasks = RushRecoveryPlanner.create(
            VillageSnapshot(
                playerTag = "#ABC",
                townHallLevel = 12,
                buildingLevels = mapOf("Cannon" to 10, "Archer Tower" to 5),
                buildingTargetLevels = mapOf("Cannon" to 12, "Archer Tower" to 15)
            )
        )

        assertEquals(listOf("Archer Tower", "Cannon"), tasks.map { it.buildingName })
        assertEquals(10, tasks.first().remainingLevels)
        assertTrue(tasks.first().priority > tasks.last().priority)
    }

    @Test
    fun ignoresBuildingsWithoutExplicitCurrentLevelOrTarget() {
        val tasks = RushRecoveryPlanner.create(
            VillageSnapshot(
                playerTag = "#ABC",
                townHallLevel = 10,
                buildingLevels = mapOf("Cannon" to 5),
                buildingTargetLevels = mapOf("Cannon" to 8, "Mortar" to 10)
            )
        )

        assertEquals(1, tasks.size)
        assertEquals("Cannon", tasks.single().buildingName)
    }

    @Test
    fun rejectsInvalidTownHallAndCompletedOrInvalidTargets() {
        assertTrue(
            RushRecoveryPlanner.create(
                VillageSnapshot(
                    playerTag = "#ABC",
                    townHallLevel = 19,
                    buildingLevels = mapOf("Cannon" to 1),
                    buildingTargetLevels = mapOf("Cannon" to 5)
                )
            ).isEmpty()
        )
        assertTrue(
            RushRecoveryPlanner.create(
                VillageSnapshot(
                    playerTag = "#ABC",
                    townHallLevel = 10,
                    buildingLevels = mapOf("Cannon" to 5, "Mortar" to 8),
                    buildingTargetLevels = mapOf("Cannon" to 5, "Mortar" to 7)
                )
            ).isEmpty()
        )
    }
}
