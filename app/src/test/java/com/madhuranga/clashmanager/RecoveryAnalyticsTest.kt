package com.madhuranga.clashmanager

import org.junit.Assert.assertEquals
import org.junit.Test

class RecoveryAnalyticsTest {
    private val week = 7L * 24 * 60 * 60 * 1000

    @Test fun summarizesLevelsEventsAndBuildings() {
        val history = listOf(
            RecoveryHistoryEntry("Cannon", 10, 12, 2 * week + 100),
            RecoveryHistoryEntry("Air Defense", 5, 6, 2 * week + 200),
            RecoveryHistoryEntry("Cannon", 12, 13, 3 * week + 100)
        )
        val summary = RecoveryAnalytics.summarize(history, 3 * week + 500, 4)

        assertEquals(3, summary.totalUpgradeEvents)
        assertEquals(4, summary.totalLevelsGained)
        assertEquals(2, summary.buildingsImproved)
        assertEquals(3, summary.weekly[2].completedLevels)
        assertEquals(1, summary.weekly[3].completedLevels)
        assertEquals(1.0, summary.averageLevelsPerWeek, 0.001)
    }

    @Test fun ignoresFutureAndInvalidHistoryEntries() {
        val history = listOf(
            RecoveryHistoryEntry("Cannon", 1, 2, 100),
            RecoveryHistoryEntry("Future", 1, 5, week * 5),
            RecoveryHistoryEntry("Invalid", 3, 2, 200)
        )
        val summary = RecoveryAnalytics.summarize(history, week * 2, 3)

        assertEquals(1, summary.totalUpgradeEvents)
        assertEquals(1, summary.totalLevelsGained)
        assertEquals(1, summary.buildingsImproved)
    }

    @Test fun clampsRequestedWeekCount() {
        val summary = RecoveryAnalytics.summarize(emptyList(), week * 10, 100)
        assertEquals(52, summary.weekly.size)
    }
}
