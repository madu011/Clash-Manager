package com.madhuranga.clashmanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecoveryProgressPlannerTest {
    @Test fun calculatesPerBuildingAndOverallProgress() {
        val summary = RecoveryProgressPlanner.calculate(
            listOf(
                BuilderSchedulePlanner.Target("Cannon", 10, 15),
                BuilderSchedulePlanner.Target("Archer Tower", 8, 12)
            ),
            mapOf("Cannon" to 12, "Archer Tower" to 10)
        )!!

        assertEquals(2, summary.buildings[0].completedLevels)
        assertEquals(3, summary.buildings[0].remainingLevels)
        assertEquals(40, summary.buildings[0].percentComplete)
        assertEquals(4, summary.completedLevels)
        assertEquals(9, summary.totalLevels)
        assertEquals(44, summary.percentComplete)
    }

    @Test fun missingCurrentLevelUsesExplicitStartingLevel() {
        val summary = RecoveryProgressPlanner.calculate(
            listOf(BuilderSchedulePlanner.Target("Cannon", 5, 8)),
            emptyMap()
        )!!
        assertEquals(0, summary.percentComplete)
    }

    @Test fun rejectsProgressOutsideTargetRange() {
        assertNull(
            RecoveryProgressPlanner.calculate(
                listOf(BuilderSchedulePlanner.Target("Cannon", 5, 8)),
                mapOf("Cannon" to 9)
            )
        )
    }
}
