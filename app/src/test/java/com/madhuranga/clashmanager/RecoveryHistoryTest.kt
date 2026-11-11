package com.madhuranga.clashmanager

import org.junit.Assert.assertEquals
import org.junit.Test

class RecoveryHistoryTest {
    @Test fun recordsOnlyNewLevelProgressAndAvoidsDuplicateEntries() {
        val first = RecoveryHistory.recordChanges(
            emptyList(), mapOf("Cannon" to 10), mapOf("Cannon" to 12), 100L
        )
        assertEquals(1, first.size)
        assertEquals(10, first[0].fromLevel)
        assertEquals(12, first[0].toLevel)

        val second = RecoveryHistory.recordChanges(
            first, mapOf("Cannon" to 10), mapOf("Cannon" to 12), 200L
        )
        assertEquals(1, second.size)
    }

    @Test fun recordsNextUpgradeFromLatestKnownLevel() {
        val first = listOf(RecoveryHistoryEntry("Cannon", 10, 12, 100L))
        val updated = RecoveryHistory.recordChanges(
            first, mapOf("Cannon" to 10), mapOf("Cannon" to 13), 300L
        )
        assertEquals(2, updated.size)
        assertEquals(12, updated.last().fromLevel)
        assertEquals(13, updated.last().toLevel)
    }

    @Test fun encodesAndDecodesHistory() {
        val original = listOf(RecoveryHistoryEntry("Air Defense", 5, 6, 123L))
        assertEquals(original, RecoveryHistory.decode(RecoveryHistory.encode(original)))
    }
}
