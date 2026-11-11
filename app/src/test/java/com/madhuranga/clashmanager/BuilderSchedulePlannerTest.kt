package com.madhuranga.clashmanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BuilderSchedulePlannerTest {
    @Test fun twoBuildersRunIndependentBuildingsInParallel() {
        val result = BuilderSchedulePlanner.create(
            listOf(
                BuilderSchedulePlanner.Target("Cannon", 1, 2),
                BuilderSchedulePlanner.Target("Archer Tower", 1, 2)
            ),
            builderCount = 2
        )!!

        assertEquals(120L, result.elapsedSeconds)
        assertEquals(150L, result.totalBuilderSeconds)
        assertEquals(3_000L, result.totalCosts["Gold"])
        assertEquals(2, result.upgrades.size)
        assertEquals(setOf(1, 2), result.upgrades.map { it.builderNumber }.toSet())
    }

    @Test fun oneBuilderRunsBuildingsSequentially() {
        val result = BuilderSchedulePlanner.create(
            listOf(
                BuilderSchedulePlanner.Target("Cannon", 1, 2),
                BuilderSchedulePlanner.Target("Archer Tower", 1, 2)
            ),
            builderCount = 1
        )!!

        assertEquals(150L, result.elapsedSeconds)
        assertEquals(150L, result.totalBuilderSeconds)
        assertEquals(listOf(0L, 30L), result.upgrades.map { it.startAfterSeconds })
    }

    @Test fun sameBuildingLevelsRemainSequential() {
        val result = BuilderSchedulePlanner.create(
            listOf(BuilderSchedulePlanner.Target("Cannon", 1, 3)),
            builderCount = 3
        )!!

        assertEquals(listOf(2, 3), result.upgrades.map { it.targetLevel })
        assertEquals(listOf(0L, 30L), result.upgrades.map { it.startAfterSeconds })
        assertEquals(150L, result.elapsedSeconds)
    }

    @Test fun unknownUpgradeDataDoesNotProduceAnInventedSchedule() {
        assertNull(
            BuilderSchedulePlanner.create(
                listOf(BuilderSchedulePlanner.Target("Unknown Building", 1, 2)),
                builderCount = 2
            )
        )
    }

    @Test fun invalidBuilderCountsAndTargetsAreRejected() {
        val target = listOf(BuilderSchedulePlanner.Target("Cannon", 1, 2))
        assertNull(BuilderSchedulePlanner.create(target, 0))
        assertNull(BuilderSchedulePlanner.create(target, 7))
        assertNull(BuilderSchedulePlanner.create(emptyList(), 2))
        assertNull(
            BuilderSchedulePlanner.create(
                listOf(BuilderSchedulePlanner.Target("Cannon", 2, 2)),
                2
            )
        )
    }
    @Test fun aggregatesCostsSeparatelyForGoldAndDarkElixir() {
        val result = BuilderSchedulePlanner.create(
            listOf(
                BuilderSchedulePlanner.Target("Cannon", 1, 2),
                BuilderSchedulePlanner.Target("Monolith", 4, 5)
            ),
            builderCount = 2
        )!!

        assertEquals(1_000L, result.totalCosts["Gold"])
        assertEquals(470_000L, result.totalCosts["Dark Elixir"])
        assertEquals(setOf("Gold", "Dark Elixir"), result.totalCosts.keys)
    }


    @Test fun schedulesLongestIndependentUpgradeFirstToReduceFinishTime() {
        val result = BuilderSchedulePlanner.create(
            listOf(
                BuilderSchedulePlanner.Target("Cannon", 1, 2),
                BuilderSchedulePlanner.Target("Archer Tower", 1, 2),
                BuilderSchedulePlanner.Target("Air Defense", 1, 2)
            ),
            builderCount = 2
        )!!

        assertEquals("Air Defense", result.upgrades.first().buildingName)
        assertEquals(7_200L, result.elapsedSeconds)
        assertEquals(1, result.upgrades.first().builderNumber)
    }


}
